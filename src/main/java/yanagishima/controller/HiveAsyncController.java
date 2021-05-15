package yanagishima.controller;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.service.HiveService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HiveAsyncController {
  private final YanagishimaConfig config;
  private final HiveService hiveService;

  @DatasourceAuth
  @PostMapping(path = { "hiveAsync", "sparkAsync" })
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam String engine,
                                  @RequestParam(name = "user", required = false) Optional<String> hiveUser,
                                  @RequestParam(name = "password", required = false)
                                      Optional<String> hivePassword,
                                  @RequestParam(required = false) String query,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
    if (query == null) {
      return Map.of();
    }

    Map<String, Object> responseBody = new HashMap<>();
    try {
      String userName = getUsername(request);
      if (config.isUserRequired() && userName == null) {
        sendForbiddenError(response);
        return responseBody;
      }

      if (userName != null) {
        log.info("{} executed {} in datasource={}, engine={}", userName, query, datasource, engine);
      }

      String queryId = hiveService.doQueryAsync(engine, datasource, query, userName, hiveUser, hivePassword);
      responseBody.put("queryid", queryId);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }

  @Nullable
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
