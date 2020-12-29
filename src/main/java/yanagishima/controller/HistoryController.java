package yanagishima.controller;

import static yanagishima.util.HistoryUtil.createHistoryResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Query;
import yanagishima.model.db.SessionProperty;
import yanagishima.service.QueryService;
import yanagishima.service.SessionPropertyService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HistoryController {
  private final SessionPropertyService sessionPropertyService;
  private final QueryService queryService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @GetMapping("history")
  public Map<String, Object> get(@RequestParam String datasource,
                                 @RequestParam(required = false) String engine,
                                 @RequestParam(name = "queryid", required = false) String queryId,
                                 HttpServletRequest request) {
    Map<String, Object> responseBody = new HashMap<>();
    if (queryId == null) {
      return responseBody;
    }

    try {
      Optional<Query> queryOptional;
      if (engine == null) {
        queryOptional = queryService.get(queryId, datasource);
      } else {
        queryOptional = queryService.getByEngine(queryId, datasource, engine);
      }

      String user = request.getHeader(config.getAuditHttpHeaderName());
      Optional<Query> userQueryOptional = queryService.get(queryId, datasource, user);
      responseBody.put("editLabel", userQueryOptional.isPresent());

      queryOptional.ifPresent(query -> {
        responseBody.put("engine", query.getEngine());
        boolean resultVisible;
        if (config.isAllowOtherReadResult(datasource)) {
          resultVisible = true;
        } else {
          resultVisible = userQueryOptional.isPresent();
        }
        List<SessionProperty> sessionPropertyList = sessionPropertyService.getAll(datasource, engine, queryId);
        createHistoryResult(responseBody, config.getSelectLimit(), datasource, query, resultVisible,
                            sessionPropertyList);
      });
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }
}
