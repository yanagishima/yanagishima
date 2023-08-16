package yanagishima.controller;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.User;
import yanagishima.model.dto.PublishDto;
import yanagishima.model.dto.PublishListDto;
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
                         User user) {
    PublishDto publishDto = new PublishDto();
    try {
      if (config.isAllowOtherReadResult(datasource)) {
        publishDto.setPublishId(publishService.publish(datasource, engine, queryid, user).getPublishId());
        return publishDto;
      }
      requireNonNull(user.getId(), "Username must exist when auditing header name is enabled");
      queryService.get(queryid, datasource, user)
                  .orElseThrow(
                      () -> new RuntimeException(format("Cannot find query id (%s) for publish", queryid)));
      publishDto.setPublishId(publishService.publish(datasource, engine, queryid, user).getPublishId());
      return publishDto;
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      publishDto.setError(e.getMessage());
    }
    return publishDto;
  }

  @DatasourceAuth
  @GetMapping("publishList")
  public PublishListDto list(@RequestParam String datasource, @RequestParam String engine, User user,
                             @RequestParam(defaultValue = "100") int limit) {
    PublishListDto publishListDto = new PublishListDto();
    try {
      publishListDto.setPublishList(publishService.getAll(datasource, engine, user, limit));
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      publishListDto.setError(e.getMessage());
    }
    return publishListDto;
  }
}
