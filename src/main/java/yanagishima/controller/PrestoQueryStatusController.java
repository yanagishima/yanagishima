package yanagishima.controller;

import static java.lang.String.format;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.client.presto.PrestoClient;
import yanagishima.config.YanagishimaConfig;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrestoQueryStatusController {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("prestoQueryStatus")
  public Map<?, ?> post(@RequestParam String datasource,
                        @RequestParam(name = "queryid", required = false) String queryId,
                        @RequestParam Optional<String> user,
                        @RequestParam Optional<String> password,
                        HttpServletRequest request) throws IOException {
    String coordinatorServer = config.getPrestoCoordinatorServer(datasource);
    String json;
    String userName = request.getHeader(config.getAuditHttpHeaderName());
    PrestoClient prestoClient = null;
    if (config.isPrestoImpersonation(datasource)) {
      prestoClient = new PrestoClient(coordinatorServer, userName,
              config.getPrestoImpersonatedUser(datasource), config.getPrestoImpersonatedPassword(datasource));
    } else {
      prestoClient = new PrestoClient(coordinatorServer, userName, user, password);
    }
    try (Response prestoResponse = prestoClient.get(queryId)) {
      if (!prestoResponse.isSuccessful() || prestoResponse.body() == null) {
        return toMap(prestoResponse.code(), prestoResponse.message());
      }
      json = prestoResponse.body().string();
    }

    Map status = new HashMap();
    try {
      status = OBJECT_MAPPER.readValue(json, Map.class);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      status.put("state", "FAILED");
      status.put("failureInfo", "");
    }
    status.remove("outputStage");
    status.remove("session");
    return status;
  }

  private Map<String, String> toMap(int code, String message) {
    return Map.of(
            "state", "FAILED",
            "failureInfo", "",
            "error", format("code=%d, message=%s", code, message));
  }
}
