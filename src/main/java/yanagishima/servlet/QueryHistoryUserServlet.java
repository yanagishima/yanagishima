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
import java.util.*;

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
            String search = request.getParameter("search");

            retVal.put("headers", Arrays.asList("Id", "Query", "Time", "rawDataSize", "engine", "finishedTime"));
            List<Query> queryList = db.search(Query.class).where("datasource = ? and engine = ? and user = ? and query_string LIKE '%" + Optional.ofNullable(search).orElse("") + "%'", datasource, engine, userName).orderBy("query_id desc").execute();
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
            retVal.put("hit", queryHistoryList.size());

            String offsetStr = request.getParameter("offset");
            int offset = 0;
            if(offsetStr != null && offsetStr.length() > 0) {
                offset = Integer.parseInt(offsetStr);
            }

            String limitStr = request.getParameter("limit");
            int limit = 100;
            if(limitStr != null && limitStr.length() > 0) {
                limit = Integer.parseInt(limitStr);
            }

            List<List<Object>> subQueryHistoryList = null;
            if(offset + limit <= queryHistoryList.size()) {
                subQueryHistoryList = queryHistoryList.subList(offset, offset + limit);
            } else {
                subQueryHistoryList = queryHistoryList.subList(offset, queryHistoryList.size());
            }
            retVal.put("results", subQueryHistoryList);

            List<Query> totalQueryList = db.searchBySQL(Query.class, String.format("select query_id from query where datasource='%s' and engine='%s' and user='%s'", datasource, engine, userName));
            int total = 0;
            for (Query query : totalQueryList) {
                Path resultFilePath = PathUtil.getResultFilePath(datasource, query.getQueryId(), false);
                if (resultFilePath.toFile().exists()) {
                    total++;
                }
            }
            retVal.put("total", total);

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
