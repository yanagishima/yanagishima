package yanagishima.controller;

import static yanagishima.util.FormatUtil.toSuccinctDataSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.model.db.Query;
import yanagishima.service.QueryService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class QueryHistoryController {
  private final QueryService queryService;

  @DatasourceAuth
  @RequestMapping(value = "/queryHistory", method = { RequestMethod.GET, RequestMethod.POST })
  public Map<String, Object> get(@RequestParam String datasource,
                                 @RequestParam(required = false) String queryids) {
    Map<String, Object> responseBody = new HashMap<>();
    try {
      String[] queryIds = queryids.split(","); // TODO: Fix NPE
      responseBody.put("headers", Arrays
          .asList("Id", "Query", "Time", "rawDataSize", "engine", "finishedTime", "linenumber", "status"));
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
    return queryService.getAll(datasource, Lists.newArrayList(queryIds));
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
    row.add(query.getStatus());
    return row;
  }
}
