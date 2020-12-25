package yanagishima.servlet;

import io.airlift.units.DataSize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.model.db.Query;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.join;
import static java.util.Collections.nCopies;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class QueryHistoryServlet {
    private final YanagishimaConfig config;
    private final TinyOrm db;

    @RequestMapping(value = "/queryHistory", method = { RequestMethod.GET, RequestMethod.POST })
    public Map<String, Object> get(@RequestParam String datasource,
                                   @RequestParam(required = false) String queryids,
                                   @RequestParam(required = false) String label, // Deprecated
                                   HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return responseBody;
            }
            String[] queryIds = queryids.split(","); // TODO: Fix NPE
            responseBody.put("headers", Arrays.asList("Id", "Query", "Time", "rawDataSize", "engine", "finishedTime", "linenumber", "labelName", "status"));
            responseBody.put("results", getHistories(datasource, queryIds));

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        return responseBody;
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
