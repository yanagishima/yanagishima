package yanagishima.controller;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.dto.PublishDto;
import yanagishima.service.PublishService;
import yanagishima.service.QueryService;

@Slf4j
@Api(tags = "publish")
@RestController
@RequiredArgsConstructor
public class PublishController {
  private final PublishService publishService;
  private final QueryService queryService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("publish")
  public PublishDto post(@RequestParam String datasource, @RequestParam String engine,
                         @RequestParam String queryid,
                         HttpServletRequest request) {
    PublishDto publishDto = new PublishDto();
    try {
      String userName = request.getHeader(config.getAuditHttpHeaderName());
      if (config.isAllowOtherReadResult(datasource)) {
        publishDto.setPublishId(publishService.publish(datasource, engine, queryid, userName).getPublishId());
        return publishDto;
      }
      requireNonNull(userName, "Username must exist when auditing header name is enabled");
      queryService.get(queryid, datasource, userName)
                  .orElseThrow(
                      () -> new RuntimeException(format("Cannot find query id (%s) for publish", queryid)));
      publishDto.setPublishId(publishService.publish(datasource, engine, queryid, userName).getPublishId());
      return publishDto;
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      publishDto.setError(e.getMessage());
    }
    return publishDto;
  }
}
