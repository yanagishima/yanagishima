package yanagishima.servlet;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.airlift.units.DataSize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.repository.TinyOrm;
import yanagishima.model.db.Query;

@Slf4j
@RestController
@RequiredArgsConstructor
public class QueryHistoryUserServlet {
    private final YanagishimaConfig config;
    private final TinyOrm db;

    @GetMapping("queryHistoryUser")
    public Map<String, Object> get(@RequestParam String datasource,
                                   @RequestParam String engine,
                                   @RequestParam(defaultValue = "") String search,
                                   @RequestParam(required = false) String label, // Deprecated
                                   @RequestParam(defaultValue = "100") String limit,
                                   HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return responseBody;
            }

            String userName = request.getHeader(config.getAuditHttpHeaderName());

            responseBody.put("headers", Arrays.asList("Id", "Query", "Time", "rawDataSize", "engine", "finishedTime", "linenumber", "labelName", "status"));

            List<Query> queryList;
            String where = "WHERE a.datasource=\'" + datasource + "\' "
                               + "and a.engine=\'" + engine + "\' and "
                               + "a.user=\'" + userName + "\' "
                               + "and a.query_string LIKE '%" + search + "%' ORDER BY a.query_id DESC LIMIT " + limit;
            String countSql = "SELECT count(*) FROM query a " + where;
            String fetchSql = "SELECT "
                              + "a.engine, "
                              + "a.query_id, "
                              + "a.fetch_result_time_string, "
                              + "a.query_string, "
                              + "a.status, "
                              + "a.elapsed_time_millis, "
                              + "a.result_file_size, "
                              + "a.linenumber, "
                              + "null AS label_name " // Deprecated
                              + "FROM query a " + where;
            responseBody.put("hit", db.queryForLong(countSql).getAsLong());
            queryList = db.searchBySQL(Query.class, fetchSql);

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
                row.add(query.getStatus());
                queryHistoryList.add(row);
            }

            if (queryHistoryList.isEmpty()) {
                responseBody.put("results", Collections.emptyList());
            } else {
                responseBody.put("results", queryHistoryList);
            }

            long totalCount = db.countQuery("datasource=? and engine=? and user=?", datasource, engine, userName);
            responseBody.put("total", totalCount);

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        return responseBody;
    }
}
