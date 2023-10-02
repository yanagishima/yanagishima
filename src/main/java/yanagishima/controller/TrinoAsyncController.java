package yanagishima.controller;

import io.trino.client.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.service.TrinoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TrinoAsyncController {
  private final TrinoService trinoService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("trinoAsync")
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam(required = false) String query,
                                  @RequestParam(name = "trinoUser") Optional<String> trinoUser,
                                  @RequestParam(name = "trinoPassword") Optional<String> trinoPassword,
                                  @RequestParam(name = "session_property") Optional<String> sessionProperty,
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
        log.info("{} executed {} in {}", user, query, datasource);
      }
      if (trinoUser.isPresent() && trinoPassword.isPresent() && trinoUser.get().isEmpty()) {
        responseBody.put("error", "user is empty");
        return responseBody;
      }
      try {
        String queryId = executeQuery(datasource, query, sessionProperty, user, trinoUser, trinoPassword);
        responseBody.put("queryid", queryId);
      } catch (ClientException e) {
        if (trinoUser.isPresent()) {
          log.error("{} failed to be authenticated", trinoUser.get());
        }
        log.error(e.getMessage(), e);
        responseBody.put("error", e.getMessage());
      } catch (Throwable e) {
        log.error(e.getMessage(), e);
        responseBody.put("error", e.getMessage());
      }
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }

  private String executeQuery(String datasource, String query, Optional<String> sessionPropertyOptional,
                              String user, Optional<String> trinoUser, Optional<String> trinoPassword) {
    return trinoService.doQueryAsync(datasource, query, sessionPropertyOptional, user, trinoUser,
                                      trinoPassword);
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
