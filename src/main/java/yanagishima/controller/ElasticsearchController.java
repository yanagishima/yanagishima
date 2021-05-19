package yanagishima.controller;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;
import static yanagishima.util.JsonUtil.writeJSON;
import static yanagishima.util.QueryIdUtil.datetimeOf;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.ElasticsearchQueryErrorException;
import yanagishima.model.db.Query;
import yanagishima.model.dto.ElasticsearchQueryStatusDto;
import yanagishima.model.elasticsearch.ElasticsearchQueryResult;
import yanagishima.service.ElasticsearchService;
import yanagishima.service.QueryService;
import yanagishima.util.Status;

@Slf4j
@Api(tags = "elasticsearch")
@RestController
@RequiredArgsConstructor
public class ElasticsearchController {
  private final ElasticsearchService elasticsearchService;
  private final QueryService queryService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("elasticsearch")
  public Map<String, Object> post(@RequestParam String datasource, HttpServletRequest request,
                                  HttpServletResponse response) {
    Map<String, Object> responseBody = new HashMap<>();
    String query = request.getParameter("query");
    if (query == null) {
      writeJSON(response, responseBody);
      return responseBody;
    }

    try {
      String user = config.isUseAuditHttpHeaderName()
                    ? request.getHeader(config.getAuditHttpHeaderName()) : null;
      if (config.isUserRequired() && user == null) {
        sendForbiddenError(response);
        return responseBody;
      }
      try {
        if (user != null) {
          log.info("{} executed {} in {}", user, query, datasource);
        }
        ElasticsearchQueryResult queryResult = executeQuery(request, query, datasource, user);

        responseBody.put("queryid", queryResult.getQueryId());
        responseBody.put("headers", queryResult.getColumns());
        responseBody.put("results", queryResult.getRecords());
        responseBody.put("lineNumber", Integer.toString(queryResult.getLineNumber()));
        responseBody.put("rawDataSize", queryResult.getRawDataSize().toString());
        // TODO: Make ElasticsearchQueryResult.warningMessage Optional<String>
        Optional.ofNullable(queryResult.getWarningMessage()).ifPresent(warningMessage ->
                                                                           responseBody
                                                                               .put("warn", warningMessage)
        );
        queryService.getByEngine(queryResult.getQueryId(), datasource, "elasticsearch")
          .ifPresent(queryData -> responseBody.put("elapsedTimeMillis",
                                                   toElapsedTimeMillis(queryResult.getQueryId(), queryData))
        );
      } catch (ElasticsearchQueryErrorException e) {
        log.error(e.getMessage(), e);
        responseBody.put("queryid", e.getQueryId());
        responseBody.put("error", e.getCause().getMessage());
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

  @DatasourceAuth
  @PostMapping("elasticsearchQueryStatus")
  public ElasticsearchQueryStatusDto getStatus(@RequestParam String datasource,
                                               @RequestParam(name = "queryid") String queryId) {
    ElasticsearchQueryStatusDto elasticsearchQueryStatusDto = new ElasticsearchQueryStatusDto();
    Optional<Query> query = queryService.getByEngine(queryId, datasource, "elasticsearch");
    elasticsearchQueryStatusDto.setState(getStatus(query));
    return elasticsearchQueryStatusDto;
  }

  private ElasticsearchQueryResult executeQuery(HttpServletRequest request, String query, String datasource,
                                                String userName) throws ElasticsearchQueryErrorException {
    int limit = query.startsWith(YANAGISHIMA_COMMENT) ? Integer.MAX_VALUE : config.getSelectLimit();

    if (request.getParameter("translate") != null) {
      return elasticsearchService.doTranslate(datasource, query, userName, true, limit);
    }
    if (query.startsWith(YANAGISHIMA_COMMENT)) {
      return elasticsearchService.doQuery(datasource, query, userName, false, limit);
    }
    return elasticsearchService.doQuery(datasource, query, userName, true, limit);
  }

  private static long toElapsedTimeMillis(String queryId, Query query) {
    LocalDateTime submitTimeLdt = datetimeOf(queryId);
    ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
    String fetchResultTimeString = query.getFetchResultTimeString();
    ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
    return ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);
  }

  private static String getStatus(Optional<Query> query) {
    if (query.isEmpty()) {
      return "RUNNING";
    }
    String status = query.get().getStatus();
    if (status.equals(Status.SUCCEED.name())) {
      return "FINISHED";
    }
    if (status.equals(Status.FAILED.name())) {
      return "FAILED";
    }
    throw new IllegalArgumentException(format("unknown status=%s", status));
  }
}
