package yanagishima.controller;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.prestosql.client.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.service.PrestoService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrestoAsyncController {
  private final PrestoService prestoService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("prestoAsync")
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam(required = false) String query,
                                  @RequestParam(name = "user") Optional<String> prestoUser,
                                  @RequestParam(name = "password") Optional<String> prestoPassword,
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
      if (prestoUser.isPresent() && prestoPassword.isPresent() && prestoUser.get().isEmpty()) {
        responseBody.put("error", "user is empty");
        return responseBody;
      }
      try {
        String queryId = executeQuery(datasource, query, sessionProperty, user, prestoUser, prestoPassword);
        responseBody.put("queryid", queryId);
      } catch (ClientException e) {
        if (prestoUser.isPresent()) {
          log.error("{} failed to be authenticated", prestoUser.get());
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
                              String user, Optional<String> prestoUser, Optional<String> prestoPassword) {
    return prestoService.doQueryAsync(datasource, query, sessionPropertyOptional, user, prestoUser,
                                      prestoPassword);
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
