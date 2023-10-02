package yanagishima.controller;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static yanagishima.util.Constants.DATASOURCE_HEADER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import yanagishima.config.YanagishimaConfig;

@RestController
@RequiredArgsConstructor
public class DatasourceAuthController {
  private final YanagishimaConfig config;

  @GetMapping("datasourceAuth")
  public Map<String, Object> get(@RequestHeader(name = DATASOURCE_HEADER, required = false) String header) {
    Map<String, Object> responseBody = new HashMap<>();
    if (config.isCheckDatasource()) {
      requireNonNull(header, format("Missing required header '%s'", DATASOURCE_HEADER));
      if (header.equals("*")) {
        responseBody.put("datasources", getDatasourceEngineList(config.getDatasources()));
      } else {
        List<String> headerDatasources = Arrays.asList(header.split(","));
        List<String> allowedDatasources = config.getDatasources().stream().filter(headerDatasources::contains)
                                                .collect(Collectors.toList());
        responseBody.put("datasources", getDatasourceEngineList(allowedDatasources));
      }
    } else {
      responseBody.put("datasources", getDatasourceEngineList(config.getDatasources()));
    }
    return responseBody;
  }

  private List<Map<String, Map<String, Object>>> getDatasourceEngineList(List<String> allowedDatasources) {
    List<Map<String, Map<String, Object>>> datasourceEngines = new ArrayList<>();
    for (String datasource : allowedDatasources) {
      List<String> engines = config.getEngines().stream()
                                   .filter(engine -> config.getDatasources(engine).contains(datasource))
                                   .collect(Collectors.toList());

      Map<String, Object> context = Map.of(
              "engines", engines,
              "auth", config.isAuth(datasource),
              "metadataService", false, // Deprecated
              "datetimePartitionHasHyphen", config.isDatatimePartitionHasHyphen(datasource));

      datasourceEngines.add(Map.of(datasource, context));
    }
    return datasourceEngines;
  }
}
