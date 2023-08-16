package yanagishima.controller;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import okhttp3.Response;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.client.presto.PrestoClient;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Query;
import yanagishima.service.QueryService;

@RestController
@RequiredArgsConstructor
public class PrestoQueryController {
  private static final int LIMIT = 100;
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final QueryService queryService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("prestoQuery")
  public Object post(@RequestParam String datasource,
                     @RequestParam Optional<String> user,
                     @RequestParam Optional<String> password,
                     HttpServletRequest request) throws IOException {
    String coordinatorServer = config.getPrestoCoordinatorServerOrNull(datasource);
    if (coordinatorServer == null) {
      return List.of();
    }

    String userName = request.getHeader(config.getAuditHttpHeaderName());
    String originalJson;
    PrestoClient prestoClient = null;
    if (config.isPrestoImpersonation(datasource)) {
      prestoClient = new PrestoClient(coordinatorServer, userName,
              config.getPrestoImpersonatedUser(datasource), config.getPrestoImpersonatedPassword(datasource));
    } else {
      prestoClient = new PrestoClient(coordinatorServer, userName, user, password);
    }
    try (Response prestoResponse = prestoClient.get()) {
      originalJson = prestoResponse.body().string();
      int code = prestoResponse.code();
      if (code != SC_OK) {
        return Map.of("code", code, "error", prestoResponse.message());
      }
    }

    List<Map> list = OBJECT_MAPPER.readValue(originalJson, List.class);
    List<Map> runningList = list.stream().filter(m -> m.get("state").equals("RUNNING")).collect(
        Collectors.toList());
    List<Map> notRunningList = list.stream().filter(m -> !m.get("state").equals("RUNNING")).collect(
        Collectors.toList());
    runningList.sort(
        (a, b) -> String.class.cast(b.get("queryId")).compareTo(String.class.cast(a.get("queryId"))));
    notRunningList.sort(
        (a, b) -> String.class.cast(b.get("queryId")).compareTo(String.class.cast(a.get("queryId"))));

    List<Map> limitedList = new ArrayList<>();
    limitedList.addAll(runningList);
    limitedList.addAll(notRunningList.subList(0, Math.min(LIMIT, list.size()) - runningList.size()));

    List<String> queryIds = limitedList.stream().map(query -> (String) query.get("queryId")).collect(
        Collectors.toList());
    List<Query> queries = queryService.getAll(datasource, "presto", queryIds);

    List<String> existDbQueryIds = new ArrayList<>();
    for (Query query : queries) {
      existDbQueryIds.add(query.getQueryId());
    }
    List<Map> userQueryList = new ArrayList<>();
    for (Map query : limitedList) {
      String queryId = (String) query.get("queryId");
      query.put("existdb", existDbQueryIds.contains(queryId));
      Map session = (Map) query.get("session");
      if (session.get("user").equals(userName)) {
        userQueryList.add(query);
      }
    }
    return userQueryList;
  }
}
