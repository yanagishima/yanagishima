package yanagishima.controller;

import static java.lang.String.format;

import java.util.Optional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.model.db.Query;
import yanagishima.model.dto.ElasticsearchQueryStatusDto;
import yanagishima.service.QueryService;
import yanagishima.util.Status;

@Api(tags = "elasticsearch")
@RestController
@RequiredArgsConstructor
public class ElasticsearchQueryStatusController {
  private final QueryService queryService;

  @DatasourceAuth
  @PostMapping("elasticsearchQueryStatus")
  public ElasticsearchQueryStatusDto post(@RequestParam String datasource,
                                          @RequestParam(name = "queryid") String queryId) {
    ElasticsearchQueryStatusDto elasticsearchQueryStatusDto = new ElasticsearchQueryStatusDto();
    Optional<Query> query = queryService.getByEngine(queryId, datasource, "elasticsearch");
    elasticsearchQueryStatusDto.setState(getStatus(query));
    return elasticsearchQueryStatusDto;
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
