package yanagishima.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.client.trino.TrinoClient;
import yanagishima.config.YanagishimaConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TrinoKillController {
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("killTrino")
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam(name = "queryid", required = false) String queryId,
                                  @RequestParam Optional<String> user,
                                  @RequestParam Optional<String> password,
                                  HttpServletRequest request) {
    if (queryId == null) {
      return Map.of();
    }

    try {
      String coordinatorUrl = config.getTrinoCoordinatorServer(datasource);
      String userName = request.getHeader(config.getAuditHttpHeaderName());
      TrinoClient trinoClient = null;
      if (config.isTrinoImpersonation(datasource)) {
        trinoClient = new TrinoClient(coordinatorUrl, userName,
                config.getTrinoImpersonatedUser(datasource), config.getTrinoImpersonatedPassword(datasource));
      } else {
        trinoClient = new TrinoClient(coordinatorUrl, userName, user, password);
      }
      Response killResponse = trinoClient.kill(queryId);
      return Map.of("code", killResponse.code(), "message", killResponse.message(), "url",
                    killResponse.request().url());
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      return Map.of("error", e.getMessage());
    }
  }
}
