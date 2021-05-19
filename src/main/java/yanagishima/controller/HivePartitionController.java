package yanagishima.controller;

import static java.lang.String.format;
import static java.lang.String.join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.hive.HiveQueryResult;
import yanagishima.service.HiveService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class HivePartitionController {
  private final HiveService hiveService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping(path = { "hivePartition", "sparkPartition" })
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam String engine,
                                  @RequestParam String schema,
                                  @RequestParam String table,
                                  @RequestParam(required = false) String partitionColumn,
                                  @RequestParam(required = false) String partitionColumnType,
                                  @RequestParam(required = false) String partitionValue,
                                  @RequestParam(name = "user") Optional<String> hiveUser,
                                  @RequestParam(name = "password") Optional<String> hivePassword,
                                  HttpServletRequest request) {
    Map<String, Object> responseBody = new HashMap<>();

    try {
      String user = getUsername(request);
      if (partitionColumn == null || partitionValue == null) {
        String query = format("SHOW PARTITIONS %s.`%s`", schema, table);
        if (user != null) {
          log.info("{} executed {} in {}", user, query, datasource);
        }
        HiveQueryResult hiveQueryResult = hiveService.doQuery(engine, datasource, query, user, hiveUser,
                                                              hivePassword, false, Integer.MAX_VALUE);
        Set<String> partitions = new TreeSet<>();
        List<List<String>> records = hiveQueryResult.getRecords();
        if (records.size() > 0) {
          String cell = records.get(0).get(0); // part1=val1/part2=val2/part3=val3'...
          responseBody.put("column", cell.split("/")[0].split("=")[0]);
          for (List<String> row : records) {
            partitions.add(row.get(0).split("/")[0].split("=")[1]);
          }
        }
        responseBody.put("partitions", partitions);
      } else {
        String[] partitionColumns = partitionColumn.split(",");
        String[] partitionColumnTypes = partitionColumnType.split(",");
        String[] partitionValues = partitionValue.split(",");
        if (partitionColumns.length != partitionValues.length) {
          throw new RuntimeException("The number of partitionColumn must be same as partitionValue");
        }
        List<String> whereList = new ArrayList<>();
        for (int i = 0; i < partitionColumns.length; i++) {
          if (partitionColumnTypes[i].equals("string")) {
            whereList.add(format("%s = '%s'", partitionColumns[i], partitionValues[i]));
          } else {
            whereList.add(format("%s = %s", partitionColumns[i], partitionValues[i]));
          }
        }
        String query = format("SHOW PARTITIONS %s.`%s` PARTITION(%s)", schema, table, join(", ", whereList));
        if (user != null) {
          log.info("{} executed {} in {}", user, query, datasource);
        }
        HiveQueryResult hiveQueryResult = hiveService.doQuery(engine, datasource, query, user, hiveUser,
                                                              hivePassword, false, Integer.MAX_VALUE);
        List<List<String>> records = hiveQueryResult.getRecords();
        String cell = records.get(0).get(0); // part1=val1/part2=val2/part3=val3'...
        String[] keyValues = cell.split("/");
        int index = 0;
        for (String keyValue : keyValues) {
          if (keyValue.split("=")[0].equals(partitionColumns[partitionColumns.length - 1])) {
            break;
          }
          index++;
        }
        responseBody.put("column", keyValues[index + 1].split("=")[0]);
        Set<String> partitions = new TreeSet<>();
        for (List<String> row : records) {
          partitions.add(row.get(0).split("/")[index + 1].split("=")[1]);
        }
        responseBody.put("partitions", partitions);
      }
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }

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
