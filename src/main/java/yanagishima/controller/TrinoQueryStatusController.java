package yanagishima.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.client.presto.PrestoClient;
import yanagishima.client.trino.TrinoClient;
import yanagishima.config.YanagishimaConfig;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TrinoQueryStatusController {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("trinoQueryStatus")
  public Map<?, ?> post(@RequestParam String datasource,
                        @RequestParam(name = "queryid", required = false) String queryId,
                        @RequestParam Optional<String> user,
                        @RequestParam Optional<String> password,
                        HttpServletRequest request) throws IOException {
    String coordinatorServer = config.getTrinoCoordinatorServer(datasource);
    String json;
    String userName = request.getHeader(config.getAuditHttpHeaderName());
    TrinoClient trinoClient = null;
    if (config.isPrestoImpersonation(datasource)) {
      trinoClient = new TrinoClient(coordinatorServer, userName,
              config.getTrinoImpersonatedUser(datasource), config.getTrinoImpersonatedPassword(datasource));
    } else {
      trinoClient = new TrinoClient(coordinatorServer, userName, user, password);
    }
    try (Response prestoResponse = trinoClient.get(queryId)) {
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
