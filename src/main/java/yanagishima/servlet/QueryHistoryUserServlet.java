package yanagishima.servlet;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getOrDefaultParameter;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.airlift.units.DataSize;
import me.geso.tinyorm.TinyORM;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.Status;

@Singleton
public class QueryHistoryUserServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryHistoryUserServlet.class);
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyORM db;

    @Inject
    public QueryHistoryUserServlet(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String engine = getRequiredParameter(request, "engine");
            String userName = request.getHeader(config.getAuditHttpHeaderName());
            String search = request.getParameter("search");

            responseBody.put("headers", Arrays.asList("Id", "Query", "Time", "rawDataSize", "engine", "finishedTime", "linenumber", "labelName"));

            String limit = getOrDefaultParameter(request, "limit", "100");
            String label = request.getParameter("label");
            List<Query> queryList;
            if (label == null || label.isEmpty()) {
                String joinWhere = "LEFT OUTER JOIN label b on a.datasource = b.datasource AND a.engine = b.engine AND a.query_id = b.query_id "
                                   + "WHERE a.datasource=\'" + datasource + "\' "
                                   + "and a.engine=\'" + engine + "\' and "
                                   + "a.user=\'" + userName + "\' "
                                   + "and a.status != 'FAILED' "
                                   + "and a.query_string LIKE '%" + Optional.ofNullable(search).orElse("") + "%' ORDER BY a.query_id DESC LIMIT " + limit;
                String countSql = "SELECT count(*) FROM query a " + joinWhere;
                String fetchSql = "SELECT "
                                  + "a.engine, "
                                  + "a.query_id, "
                                  + "a.fetch_result_time_string, "
                                  + "a.query_string, "
                                  + "a.status, "
                                  + "a.elapsed_time_millis, "
                                  + "a.result_file_size, "
                                  + "a.linenumber, "
                                  + "b.label_name AS label_name "
                                  + "FROM query a " + joinWhere;
                responseBody.put("hit", db.queryForLong(countSql).getAsLong());
                queryList = db.searchBySQL(Query.class, fetchSql);
            } else {
                queryList = db.searchBySQL(Query.class,
                                           "SELECT a.engine, "
                                           + "a.query_id, "
                                           + "a.fetch_result_time_string, "
                                           + "a.query_string, "
                                           + "a.status, "
                                           + "a.elapsed_time_millis, "
                                           + "a.result_file_size, "
                                           + "a.linenumber, "
                                           + "b.label_name AS label_name "
                                           + "FROM query a LEFT OUTER JOIN label b on a.datasource = b.datasource AND a.engine = b.engine AND a.query_id = b.query_id "
                                           + "WHERE a.status != 'FAILED' and b.label_name = \'" + label
                                           + "\' and a.datasource=\'" + datasource + "\' and a.engine=\'" + engine + "\' and a.user=\'" + userName + "\' LIMIT " + limit);
                responseBody.put("hit", queryList.size());
            }

            List<List<Object>> queryHistoryList = new ArrayList<>();
            for (Query query : queryList) {
                List<Object> row = new ArrayList<>();
                row.add(query.getQueryId());
                row.add(query.getQueryString());
                row.add(query.getElapsedTimeMillis());
                if (query.getResultFileSize() == null) {
                    row.add(null);
                } else {
                    DataSize rawDataSize = new DataSize(query.getResultFileSize(), DataSize.Unit.BYTE);
                    row.add(rawDataSize.convertToMostSuccinctDataSize().toString());
                }
                row.add(query.getEngine());
                row.add(query.getFetchResultTimeString());
                row.add(query.getLinenumber());
                row.add(query.getExtraColumn("label_name"));
                queryHistoryList.add(row);
            }

            if (queryHistoryList.isEmpty()) {
                responseBody.put("results", Collections.emptyList());
            } else {
                responseBody.put("results", queryHistoryList);
            }

            long totalCount = db.count(Query.class)
                                .where("datasource=?", datasource)
                                .where("engine=?", engine)
                                .where("user=?", userName)
                                .where("status=?", Status.SUCCEED.name())
                                .execute();
            responseBody.put("total", totalCount);

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }
}
