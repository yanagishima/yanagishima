package yanagishima.servlet;

import io.airlift.units.DataSize;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.row.Query;
import yanagishima.util.Status;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class QueryHistoryServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryHistoryServlet.class);
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyORM db;

    @Inject
    public QueryHistoryServlet(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }
            String[] queryIds = request.getParameter("queryids").split(",");
            String label = request.getParameter("label");

            responseBody.put("headers", Arrays.asList("Id", "Query", "Time", "rawDataSize", "engine", "finishedTime", "linenumber", "labelName"));
            responseBody.put("results", getHistories(label, datasource, queryIds));

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    private List<List<Object>> getHistories(String label, String datasource, String[] queryIds) {
        List<List<Object>> queryHistories = new ArrayList<>();
        for (Query query : getQueries(label, datasource, queryIds)) {
            if (query.getStatus().equals(Status.FAILED.name())) {
                continue;
            }
            queryHistories.add(toQueryHistory(query));
        }
        return queryHistories;
    }

    private List<Query> getQueries(String label, String datasource, String[] queryIds) {
        String placeholder = join(", ", nCopies(queryIds.length, "?"));
        if (isNullOrEmpty(label)) {
            return db.searchBySQL(Query.class,
                                  "SELECT "
                                  + "a.engine, "
                                  + "a.query_id, "
                                  + "a.fetch_result_time_string, "
                                  + "a.query_string, "
                                  + "a.status, "
                                  + "a.elapsed_time_millis, "
                                  + "a.result_file_size, "
                                  + "a.linenumber, "
                                  + "b.label_name AS label_name "
                                  + "FROM query a "
                                  + "LEFT OUTER JOIN label b "
                                  + "on a.datasource = b.datasource AND a.engine = b.engine AND a.query_id = b.query_id "
                                  + "WHERE a.datasource=\'" + datasource + "\' and a.query_id IN (" + placeholder + ")",
                                  Arrays.stream(queryIds).collect(Collectors.toList()));
        }
        return db.searchBySQL(Query.class,
                              "SELECT "
                              + "a.engine, "
                              + "a.query_id, "
                              + "a.fetch_result_time_string, "
                              + "a.query_string, "
                              + "a.status, "
                              + "a.elapsed_time_millis, "
                              + "a.result_file_size, "
                              + "a.linenumber, "
                              + "b.label_name AS label_name "
                              + "FROM query a "
                              + "LEFT OUTER JOIN label b "
                              + "on a.datasource = b.datasource AND a.engine = b.engine AND a.query_id = b.query_id "
                              + "WHERE b.label_name = \'" + label + "\' and a.datasource=\'" + datasource + "\' and a.query_id IN (" + placeholder + ")",
                              Arrays.stream(queryIds).collect(Collectors.toList()));

    }

    private static List<Object> toQueryHistory(Query query) {
        List<Object> row = new ArrayList<>();
        row.add(query.getQueryId());
        row.add(query.getQueryString());
        row.add(query.getElapsedTimeMillis());
        row.add(toSuccinctDataSize(query.getResultFileSize()));
        row.add(query.getEngine());
        row.add(query.getFetchResultTimeString());
        row.add(query.getLinenumber());
        row.add(query.getExtraColumn("label_name"));
        return row;
    }

    @Nullable
    private static String toSuccinctDataSize(Integer size) {
        if (size == null) {
            return null;
        }
        DataSize dataSize = new DataSize(size, DataSize.Unit.BYTE);
        return dataSize.convertToMostSuccinctDataSize().toString();
    }
}
