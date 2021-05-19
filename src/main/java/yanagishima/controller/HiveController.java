package yanagishima.controller;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.HiveQueryErrorException;
import yanagishima.model.hive.HiveQueryResult;
import yanagishima.service.HiveService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HiveController {
  private final YanagishimaConfig config;
  private final HiveService hiveService;

  @DatasourceAuth
  @PostMapping(path = { "hive", "spark" })
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam String engine,
                                  @RequestParam(name = "user", required = false) Optional<String> hiveUser,
                                  @RequestParam(name = "password", required = false)
                                      Optional<String> hivePassword,
                                  @RequestParam(required = false) String query,
                                  @RequestParam(defaultValue = "false") boolean store,
                                  HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> responseBody = new HashMap<>();
    if (query == null) {
      return responseBody;
    }

    try {
      String user = getUsername(request);
      if (config.isUserRequired() && user == null) {
        sendForbiddenError(response);
        return responseBody;
      }

      if (user != null) {
        log.info("{} executed {} in datasource={}, engine={}", user, query, datasource, engine);
      }

      HiveQueryResult queryResult = hiveService.doQuery(engine, datasource, query, user, hiveUser, hivePassword,
                                                        store, config.getSelectLimit());
      responseBody.put("queryid", queryResult.getQueryId());
      responseBody.put("headers", queryResult.getColumns());
      responseBody.put("lineNumber", Integer.toString(queryResult.getLineNumber()));
      responseBody.put("rawDataSize", queryResult.getRawDataSize().toString());
      responseBody.put("results", queryResult.getRecords());
      // TODO: Make HiveQueryResult.warningMessage Optional<String>
      Optional.ofNullable(queryResult.getWarningMessage()).ifPresent(
          warningMessage -> responseBody.put("warn", warningMessage));

      // Query specific operations
      if (query.startsWith("SHOW SCHEMAS")) {
        responseBody.put("results", queryResult.getRecords().stream()
                                               .filter(list -> !config.getInvisibleDatabases(datasource)
                                                                      .contains(list.get(0)))
                                               .collect(Collectors.toList()));
      }
    } catch (Throwable e) {
      if (e instanceof HiveQueryErrorException) {
        responseBody.put("queryid", ((HiveQueryErrorException) e).getQueryId());
      }

      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }

  private String getUsername(HttpServletRequest request) {
    if (config.isUseAuditHttpHeaderName()) {
      return request.getHeader(config.getAuditHttpHeaderName());
    }
    String user = request.getParameter("user");
    String password = request.getParameter("password");
    if (user != null && password != null) {
      return user;
    }
    return null;
  }
}
