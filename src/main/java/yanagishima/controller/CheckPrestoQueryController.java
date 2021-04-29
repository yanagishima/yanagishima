package yanagishima.controller;

import static java.lang.String.format;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;

import io.trino.sql.parser.ParsingException;
import io.trino.sql.parser.ParsingOptions;
import io.trino.sql.parser.SqlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.client.presto.PrestoClient;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.model.presto.PrestoQueryResult;
import yanagishima.service.PrestoService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CheckPrestoQueryController {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final PrestoService prestoService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("checkPrestoQuery")
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam(required = false) String query,
                                  @RequestParam(name = "user", required = false) Optional<String> prestoUser,
                                  @RequestParam(name = "password", required = false)
                                      Optional<String> prestoPassword,
                                  HttpServletRequest request) {
    Map<String, Object> responseBody = new HashMap<>();
    if (query == null) {
      return responseBody;
    }

    try {
      new SqlParser().createStatement(query, new ParsingOptions());
    } catch (ParsingException e) {
      responseBody.put("error", e.getMessage());
      responseBody.put("errorLineNumber", e.getLineNumber());
      responseBody.put("errorColumnNumber", e.getColumnNumber());
      return responseBody;
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
      return responseBody;
    }

    try {
      String user = getUsername(request);
      String explainQuery = format("%sEXPLAIN ANALYZE\n%s", YANAGISHIMA_COMMENT, query);
      String queryId = null;
      try {
        PrestoQueryResult prestoQueryResult = prestoService.doQuery(datasource, explainQuery, user, prestoUser,
                                                                    prestoPassword, false, Integer.MAX_VALUE);
        queryId = prestoQueryResult.getQueryId();
      } catch (QueryErrorException e) {
        log.error(e.getMessage(), e);
        queryId = e.getQueryId();
      }

      String coordinatorServer = config.getPrestoCoordinatorServer(datasource);
      PrestoClient prestoClient = null;
      if (config.isPrestoImpersonation(datasource)) {
        prestoClient = new PrestoClient(coordinatorServer, user,
                config.getPrestoImpersonatedUser(datasource), config.getPrestoImpersonatedPassword(datasource));
      } else {
        prestoClient = new PrestoClient(coordinatorServer, user, prestoUser, prestoPassword);
      }
      try (Response prestoResponse = prestoClient.get(queryId)) {
        if (prestoResponse.isSuccessful() && prestoResponse.body() != null) {
          String json = prestoResponse.body().string();
          Map status = OBJECT_MAPPER.readValue(json, Map.class);
          String state = (String) status.get("state");
          if (state.equals("FAILED")) {
            Map failureInfo = (Map) status.get("failureInfo");
            if (failureInfo != null) {
              //line 2:8: Column 'foo' cannot be resolved
              String message = (String) failureInfo.get("message");
              List<String> strings = Splitter.on(' ').splitToList(message);
              String prefix = strings.get(0) + " " + strings.get(1) + " ";
              responseBody.put("error", message.substring(prefix.length()));
              Map errorLocation = (Map) failureInfo.get("errorLocation");
              if (errorLocation != null) {
                int lineNumber = (Integer) errorLocation.get("lineNumber");
                int columnNumber = (Integer) errorLocation.get("columnNumber");
                responseBody.put("errorLineNumber", lineNumber - 1);
                responseBody.put("errorColumnNumber", columnNumber);
              }
            }
          } else if (state.equals("FINISHED")) {
            Map queryStats = (Map) status.get("queryStats");
            responseBody.put("physicalInputDataSize", queryStats.get("physicalInputDataSize"));
            responseBody.put("physicalInputPositions", queryStats.get("physicalInputPositions"));
          } else {
            throw new IllegalArgumentException("Illegal state: " + state);
          }
        }
      }
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
