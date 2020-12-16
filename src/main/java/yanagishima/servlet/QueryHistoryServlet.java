package yanagishima.servlet;

import io.airlift.units.DataSize;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.model.db.Query;

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

import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Slf4j
@Singleton
public class QueryHistoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final TinyOrm db;

    @Inject
    public QueryHistoryServlet(YanagishimaConfig config, TinyOrm db) {
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

            responseBody.put("headers", Arrays.asList("Id", "Query", "Time", "rawDataSize", "engine", "finishedTime", "linenumber", "labelName", "status"));
            responseBody.put("results", getHistories(datasource, queryIds));

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    private List<List<Object>> getHistories(String datasource, String[] queryIds) {
        List<List<Object>> queryHistories = new ArrayList<>();
        for (Query query : getQueries(datasource, queryIds)) {
            queryHistories.add(toQueryHistory(query));
        }
        return queryHistories;
    }

    private List<Query> getQueries(String datasource, String[] queryIds) {
        String placeholder = join(", ", nCopies(queryIds.length, "?"));
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
                              + "null AS label_name " // Deprecated
                              + "FROM query a "
                              + "WHERE a.datasource=\'" + datasource + "\' and a.query_id IN (" + placeholder + ")",
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
        row.add(query.getStatus());
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
