package yanagishima.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.model.db.Label;
import yanagishima.service.LabelService;

@Slf4j
@Deprecated
@RestController
@RequiredArgsConstructor
public class LabelController {
  private final LabelService labelService;

  @DatasourceAuth
  @PostMapping("label")
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam String engine,
                                  @RequestParam(name = "queryid") String queryId,
                                  @RequestParam String labelName) {
    Map<String, Object> responseBody = new HashMap<>();
    try {
      labelService.insert(datasource, engine, queryId, labelName);

      responseBody.put("datasource", datasource);
      responseBody.put("engine", engine);
      responseBody.put("queryid", queryId);
      responseBody.put("labelName", labelName);
      responseBody.put("count", 1);

    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }

  @DatasourceAuth
  @GetMapping("label")
  public Map<String, Object> get(@RequestParam String datasource,
                                 @RequestParam String engine,
                                 @RequestParam(name = "queryid") String queryId) {
    Map<String, Object> responseBody = new HashMap<>();

    try {
      Optional<Label> optionalLabel = labelService.get(datasource, engine, queryId);
      if (optionalLabel.isPresent()) {
        responseBody.put("label", optionalLabel.get().getLabelName());
      }
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }

  @DatasourceAuth
  @DeleteMapping("label")
  public Map<String, Object> delete(@RequestParam String datasource,
                                    @RequestParam String engine,
                                    @RequestParam(name = "queryid") String queryId) {
    Map<String, Object> responseBody = new HashMap<>();
    try {
      labelService.get(datasource, engine, queryId).ifPresent(labelService::delete);
      responseBody.put("datasource", datasource);
      responseBody.put("engine", engine);
      responseBody.put("queryid", queryId);
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }
}
