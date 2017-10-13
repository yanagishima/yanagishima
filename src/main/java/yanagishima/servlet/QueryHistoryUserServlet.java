package yanagishima.servlet;

import io.airlift.units.DataSize;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.JsonUtil;
import yanagishima.util.PathUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class QueryHistoryUserServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(QueryHistoryUserServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public QueryHistoryUserServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
            String datasource = HttpRequestUtil.getParam(request, "datasource");
            if(yanagishimaConfig.isCheckDatasource()) {
                if(!AccessControlUtil.validateDatasource(request, datasource)) {
                    try {
                        response.sendError(SC_FORBIDDEN);
                        return;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            String engine = HttpRequestUtil.getParam(request, "engine");
            String userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
            String limit = HttpRequestUtil.getParam(request, "limit");
            String offset = HttpRequestUtil.getParam(request, "offset");
            String search = HttpRequestUtil.getParam(request, "search");
            List<Query> queryList = null;
            if(limit.length() > 0 && offset.length() > 0) {
                queryList = db.search(Query.class).where("datasource = ? and engine = ? and user = ? and query_string LIKE '%" + search + "%'", datasource, engine, userName).orderBy("query_id desc").limit(Long.valueOf(limit)).offset(Long.valueOf(offset)).execute();
            } else {
                queryList = db.search(Query.class).where("datasource = ? and engine = ? and user = ?", datasource, engine, userName).where("query_string LIKE '%" + search + "%'").orderBy("query_id desc").execute();
            }

            List<List<Object>> queryHistoryList = new ArrayList<List<Object>>();
            for (Query query : queryList) {
                List<Object> row = new ArrayList<>();
                String queryid = query.getQueryId();

                Path resultFilePath = PathUtil.getResultFilePath(datasource, queryid, false);
                if(resultFilePath.toFile().exists()) {
                    row.add(queryid);
                    row.add(query.getQueryString());

                    LocalDateTime submitTimeLdt = LocalDateTime.parse(queryid.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                    ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
                    String fetchResultTimeString = query.getFetchResultTimeString();
                    ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
                    long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);
                    row.add(elapsedTimeMillis);

                    long size = Files.size(resultFilePath);
                    DataSize rawDataSize = new DataSize(size, DataSize.Unit.BYTE);
                    row.add(rawDataSize.convertToMostSuccinctDataSize().toString());
                    row.add(query.getEngine());
                    row.add(fetchResultTimeString);

                    queryHistoryList.add(row);
                }

            }
            retVal.put("headers", Arrays.asList("Id", "Query", "Time", "rawDataSize", "engine", "finishedTime"));
            retVal.put("results", queryHistoryList);
            retVal.put("hit", queryHistoryList.size());
            long total = db.count(Query.class).where("datasource = ? and engine = ? and user = ?", datasource, engine, userName).execute();
            retVal.put("total", total);

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
