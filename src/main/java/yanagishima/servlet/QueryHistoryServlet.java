package yanagishima.servlet;

import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.row.Query;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class QueryHistoryServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory
            .getLogger(QueryHistoryServlet.class);

    private static final long serialVersionUID = 1L;

    @Inject
    private TinyORM db;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        HashMap<String, Object> retVal = new HashMap<String, Object>();

        try {
            String datasource = HttpRequestUtil.getParam(request, "datasource");
            AccessControlUtil.checkDatasource(request, datasource);
            String[] queryids = Optional.ofNullable(request.getParameter("queryids")).get().split(",");

            String placeholder = Arrays.stream(queryids).map(r -> "?").collect(Collectors.joining(", "));
            List<Query> queryList = db.searchBySQL(Query.class,
                    "SELECT query_id, fetch_result_time_string, query_string FROM query WHERE datasource=\'" + datasource + "\' and query_id IN (" + placeholder + ")",
                    Arrays.stream(queryids).collect(Collectors.toList()));

            List<List<Object>> queryHistoryList = new ArrayList<List<Object>>();
            for (Query query : queryList) {
                List<Object> row = new ArrayList<>();
                String queryid = query.getQueryId();
                row.add(queryid);
                row.add(query.getQueryString());

                LocalDateTime submitTimeLdt = LocalDateTime.parse(queryid.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
                String fetchResultTimeString = query.getFetchResultTimeString();
                ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
                long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);
                row.add(elapsedTimeMillis);

                queryHistoryList.add(row);
            }
            retVal.put("headers", Arrays.asList("Id", "Query", "Time"));
            retVal.put("results", queryHistoryList);

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            retVal.put("error", e.getMessage());
        }

        JsonUtil.writeJSON(response, retVal);

    }

}
