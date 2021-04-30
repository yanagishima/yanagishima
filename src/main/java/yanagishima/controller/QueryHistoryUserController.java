package yanagishima.controller;

import static yanagishima.util.FormatUtil.toSuccinctDataSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Query;
import yanagishima.service.QueryService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class QueryHistoryUserController {
  private final QueryService queryService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @GetMapping("queryHistoryUser")
  public Map<String, Object> get(@RequestParam String datasource,
                                 @RequestParam String engine,
                                 @RequestParam(defaultValue = "") String search,
                                 @RequestParam(defaultValue = "100") int limit,
                                 HttpServletRequest request) {
    Map<String, Object> responseBody = new HashMap<>();
    try {
      String userName = request.getHeader(config.getAuditHttpHeaderName());

      responseBody.put("headers", Arrays
          .asList("Id", "Query", "Time", "rawDataSize", "engine", "finishedTime", "linenumber", "status"));

      List<Query> queryList = queryService.getAll(datasource, engine, userName, search, limit);
      responseBody.put("hit", queryList.size());

      List<List<Object>> queryHistoryList = new ArrayList<>();
      for (Query query : queryList) {
        List<Object> row = new ArrayList<>();
        row.add(query.getQueryId());
        row.add(query.getQueryString());
        row.add(query.getElapsedTimeMillis());
        if (query.getResultFileSize() == null) {
          row.add(null);
        } else {
          row.add(toSuccinctDataSize(query.getResultFileSize()));
        }
        row.add(query.getEngine());
        row.add(query.getFetchResultTimeString());
        row.add(query.getLinenumber());
        row.add(query.getStatus());
        queryHistoryList.add(row);
      }

      responseBody.put("results", queryHistoryList);

      long totalCount = queryService.count(datasource, engine, userName);
      responseBody.put("total", totalCount);

    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }
}
