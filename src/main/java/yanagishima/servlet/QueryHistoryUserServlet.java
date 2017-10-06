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
            String limit = request.getParameter("limit");
            String offset = request.getParameter("offset");
            String search = request.getParameter("search");
            List<Query> queryList = null;
            if (search != null) {
                queryList = db.search(Query.class).where("datasource = ? and engine = ? and user = ?", datasource, engine, userName).where("query_string LIKE '%" + search + "%'").execute();
            } else if(limit != null && offset != null) {
                queryList = db.search(Query.class).where("datasource = ? and engine = ? and user = ?", datasource, engine, userName).orderBy("query_id").limit(Long.valueOf(limit) + 1).offset(Long.valueOf(offset)).execute();
                if(queryList.size() == Integer.parseInt(limit) + 1) {
                    queryList.remove(queryList.size() - 1);
                    retVal.put("hasNext", true);
                } else {
                    retVal.put("hasNext", false);
                }
            } else {
                queryList = db.search(Query.class).where("datasource = ? and engine = ? and user =?", datasource, engine, userName).execute();
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

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
