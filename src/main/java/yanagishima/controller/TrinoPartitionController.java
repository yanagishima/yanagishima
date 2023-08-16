package yanagishima.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.trino.TrinoQueryResult;
import yanagishima.service.TrinoService;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static java.lang.String.format;
import static java.lang.String.join;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TrinoPartitionController {
  private final TrinoService trinoService;
  private final YanagishimaConfig config;

  @DatasourceAuth
  @PostMapping("trinoPartition")
  public Map<String, Object> post(@RequestParam String datasource,
                                  @RequestParam String catalog,
                                  @RequestParam String schema,
                                  @RequestParam String table,
                                  @RequestParam(required = false) String partitionColumn,
                                  @RequestParam(required = false) String partitionColumnType,
                                  @RequestParam(required = false) String partitionValue,
                                  @RequestParam(name = "user") Optional<String> trinoUser,
                                  @RequestParam(name = "password") Optional<String> trinoPassword,
                                  HttpServletRequest request) {
    Map<String, Object> responseBody = new HashMap<>();
    try {
      String user = getUsername(request);
      if (partitionColumn == null || partitionValue == null) {
        String query = buildGetPartitionsQuery(datasource, catalog, schema, table);
        if (user != null) {
          log.info("{} executed {} in {}", user, query, datasource);
        }
        TrinoQueryResult trinoQueryResult = trinoService.doQuery(
            datasource, query, user, trinoUser, trinoPassword, Map.of(), false, Integer.MAX_VALUE);
        responseBody.put("column", trinoQueryResult.getColumns().get(0));
        Set<String> partitions = new TreeSet<>();
        List<List<String>> records = trinoQueryResult.getRecords();
        for (List<String> row : records) {
          String data = row.get(0);
          if (data != null) {
            partitions.add(data);
          }
        }
        responseBody.put("partitions", partitions);
      } else {
        String[] partitionColumnArray = partitionColumn.split(",");
        String[] partitionColumnTypeArray = partitionColumnType.split(",");
        String[] partitionValuesArray = partitionValue.split(",");
        if (partitionColumnArray.length != partitionValuesArray.length) {
          throw new RuntimeException("The number of partitionColumn must be same as partitionValue");
        }

        List<String> whereList = new ArrayList<>();
        for (int i = 0; i < partitionColumnArray.length; i++) {
          if (partitionColumnTypeArray[i].equals("varchar") || partitionColumnTypeArray[i].equals("string")) {
            whereList.add(format("%s = '%s'", partitionColumnArray[i], partitionValuesArray[i]));
          } else if (partitionColumnTypeArray[i].equals("date")) {
            whereList.add(format("%s = DATE '%s'", partitionColumnArray[i], partitionValuesArray[i]));
          } else {
            whereList.add(format("%s = %s", partitionColumnArray[i], partitionValuesArray[i]));
          }
        }
        String query = buildGetPartitionsQuery(datasource, catalog, schema, table, whereList);
        if (user != null) {
          log.info("{} executed {} in {}", user, query, datasource);
        }
        TrinoQueryResult trinoQueryResult = trinoService.doQuery(
            datasource, query, user, trinoUser, trinoPassword, Map.of(), false, Integer.MAX_VALUE);
        List<String> columns = trinoQueryResult.getColumns();
        int index = 0;
        for (String column : columns) {
          if (column.equals(partitionColumnArray[partitionColumnArray.length - 1])) {
            break;
          }
          index++;
        }
        responseBody.put("column", columns.get(index + 1));
        Set<String> partitions = new TreeSet<>();
        List<List<String>> records = trinoQueryResult.getRecords();
        for (List<String> row : records) {
          String partition = row.get(index + 1);
          if (partition != null) {
            partitions.add(partition);
          }
        }
        responseBody.put("partitions", partitions);
      }
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      responseBody.put("error", e.getMessage());
    }
    return responseBody;
  }

  private String buildGetPartitionsQuery(String datasource, String catalog, String schema, String table) {
    if (config.isUseNewShowPartitions(datasource)) {
      return format("%sSELECT * FROM  %s.%s.\"%s$partitions\"", YANAGISHIMA_COMMENT, catalog, schema, table);
    }
    return format("%sSHOW PARTITIONS FROM %s.%s.\"%s\"", YANAGISHIMA_COMMENT, catalog, schema, table);
  }

  private String buildGetPartitionsQuery(String datasource, String catalog, String schema, String table,
                                         List<String> condition) {
    if (config.isUseNewShowPartitions(datasource)) {
      return format("%sSELECT * FROM  %s.%s.\"%s$partitions\" WHERE %s", YANAGISHIMA_COMMENT, catalog, schema,
                    table, join(" AND ", condition));
    }
    return format("%sSHOW PARTITIONS FROM %s.%s.%s WHERE %s", YANAGISHIMA_COMMENT, catalog, schema, table,
                  join(" AND ", condition));
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
