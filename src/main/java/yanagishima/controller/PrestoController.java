package yanagishima.controller;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.trino.client.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.model.presto.PrestoQueryResult;
import yanagishima.service.PrestoService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrestoController {
  private final PrestoService prestoService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("presto")
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam(name = "query") Optional<String> queryOptional,
                                  @RequestParam(name = "user", required = false) Optional<String> prestoUser,
                                  @RequestParam(name = "password", required = false)
                                      Optional<String> prestoPassword,
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
        String coordinatorServer = config.getPrestoCoordinatorServerOrNull(datasource);
        if (coordinatorServer == null) {
          return responseBody;
        }
        if (userName != null) {
          log.info(format("%s executed %s in %s", userName, query, datasource));
        }
        if (prestoUser.isPresent() && prestoPassword.isPresent()) {
          if (prestoUser.get().length() == 0) {
            responseBody.put("error", "user is empty");
            return responseBody;
          }
        }
        PrestoQueryResult prestoQueryResult;
        if (query.startsWith(YANAGISHIMA_COMMENT)) {
          prestoQueryResult = prestoService.doQuery(datasource, query, userName, prestoUser, prestoPassword,
                                                    store, Integer.MAX_VALUE);
        } else {
          prestoQueryResult = prestoService.doQuery(datasource, query, userName, prestoUser, prestoPassword,
                                                    store, config.getSelectLimit());
        }
        String queryId = prestoQueryResult.getQueryId();
        responseBody.put("queryid", queryId);
        if (prestoQueryResult.getUpdateType() == null) {
          responseBody.put("headers", prestoQueryResult.getColumns());

          if (query.startsWith(YANAGISHIMA_COMMENT + "SHOW SCHEMAS FROM")) {
            String catalog = query.substring((YANAGISHIMA_COMMENT + "SHOW SCHEMAS FROM").length()).trim();
            List<String> invisibleSchemas = config.getInvisibleSchemas(datasource, catalog);
            responseBody.put("results", prestoQueryResult.getRecords().stream().filter(
                list -> !invisibleSchemas.contains(list.get(0))).collect(Collectors.toList()));
          } else {
            responseBody.put("results", prestoQueryResult.getRecords());
          }
          responseBody.put("lineNumber", Integer.toString(prestoQueryResult.getLineNumber()));
          responseBody.put("rawDataSize", prestoQueryResult.getRawDataSize().toString());
          Optional<String> warningMessageOptinal = Optional.ofNullable(prestoQueryResult.getWarningMessage());
          warningMessageOptinal.ifPresent(warningMessage -> {
            responseBody.put("warn", warningMessage);
          });
        }
      } catch (QueryErrorException e) {
        log.warn(e.getCause().getMessage());
        responseBody.put("error", e.getCause().getMessage());
        responseBody.put("queryid", e.getQueryId());
      } catch (ClientException e) {
        if (prestoUser.isPresent()) {
          log.error(format("%s failed to be authenticated", prestoUser.get()));
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
