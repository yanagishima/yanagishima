package yanagishima.controller;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.client.presto.PrestoClient;
import yanagishima.config.YanagishimaConfig;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrestoKillController {
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("kill")
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam(name = "queryid", required = false) String queryId,
                                  @RequestParam Optional<String> user,
                                  @RequestParam Optional<String> password,
                                  HttpServletRequest request) {
    if (queryId == null) {
      return Map.of();
    }

    try {
      String coordinatorUrl = config.getPrestoCoordinatorServer(datasource);
      String userName = request.getHeader(config.getAuditHttpHeaderName());
      PrestoClient prestoClient = null;
      if (config.isPrestoImpersonation(datasource)) {
        prestoClient = new PrestoClient(coordinatorUrl, userName,
                config.getPrestoImpersonatedUser(datasource), config.getPrestoImpersonatedPassword(datasource));
      } else {
        prestoClient = new PrestoClient(coordinatorUrl, userName, user, password);
      }
      Response killResponse = prestoClient.kill(queryId);
      return Map.of("code", killResponse.code(), "message", killResponse.message(), "url",
                    killResponse.request().url());
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      return Map.of("error", e.getMessage());
    }
  }
}
