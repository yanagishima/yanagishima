package yanagishima.controller;

import io.trino.client.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.model.trino.TrinoQueryResult;
import yanagishima.service.TrinoService;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TrinoController {
  private final TrinoService trinoService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("trino")
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam(name = "query") Optional<String> queryOptional,
                                  @RequestParam(name = "trinoUser", required = false) Optional<String> trinoUser,
                                  @RequestParam(name = "trinoPassword", required = false)
                                      Optional<String> trinoPassword,
                                  @RequestParam(defaultValue = "false") boolean store,
                                  HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> responseBody = new HashMap<>();

    try {
      if (queryOptional.isEmpty()) {
        return responseBody;
      }

      String query = queryOptional.get();
      String userName = getUsername(request);
      if (config.isUserRequired() && userName == null) {
        sendForbiddenError(response);
        return responseBody;
      }
      try {
        String coordinatorServer = config.getTrinoCoordinatorServerOrNull(datasource);
        if (coordinatorServer == null) {
          return responseBody;
        }
        if (userName != null) {
          log.info("{} executed {} in {}", userName, query, datasource);
        }
        if (trinoUser.isPresent() && trinoPassword.isPresent()) {
          if (trinoUser.get().isEmpty()) {
            responseBody.put("error", "user is empty");
            return responseBody;
          }
        }
        TrinoQueryResult trinoQueryResult;
        if (query.startsWith(YANAGISHIMA_COMMENT)) {
          trinoQueryResult = trinoService.doQuery(datasource, query, userName, trinoUser, trinoPassword,
                  Map.of(), store, Integer.MAX_VALUE);
        } else {
          trinoQueryResult = trinoService.doQuery(datasource, query, userName, trinoUser, trinoPassword,
                  Map.of(), store, config.getSelectLimit());
        }
        String queryId = trinoQueryResult.getQueryId();
        responseBody.put("queryid", queryId);
        if (trinoQueryResult.getUpdateType() == null) {
          responseBody.put("headers", trinoQueryResult.getColumns());

          if (query.startsWith(YANAGISHIMA_COMMENT + "SHOW SCHEMAS FROM")) {
            String catalog = query.substring((YANAGISHIMA_COMMENT + "SHOW SCHEMAS FROM").length()).trim();
            List<String> invisibleSchemas = config.getInvisibleSchemas(datasource, catalog);
            responseBody.put("results", trinoQueryResult.getRecords().stream().filter(
                list -> !invisibleSchemas.contains(list.get(0))).collect(Collectors.toList()));
          } else {
            responseBody.put("results", trinoQueryResult.getRecords());
          }
          responseBody.put("lineNumber", Integer.toString(trinoQueryResult.getLineNumber()));
          responseBody.put("rawDataSize", trinoQueryResult.getRawDataSize().toString());
          Optional<String> warningMessageOptinal = Optional.ofNullable(trinoQueryResult.getWarningMessage());
          warningMessageOptinal.ifPresent(warningMessage -> {
            responseBody.put("warn", warningMessage);
          });
        }
      } catch (QueryErrorException e) {
        log.warn(e.getCause().getMessage());
        responseBody.put("error", e.getCause().getMessage());
        responseBody.put("queryid", e.getQueryId());
      } catch (ClientException e) {
          trinoUser.ifPresent(s -> log.error("{} failed to be authenticated", s));
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
