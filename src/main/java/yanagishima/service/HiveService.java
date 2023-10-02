package yanagishima.service;

import static java.lang.String.format;
import static yanagishima.util.Constants.YANAGISHIAM_HIVE_JOB_PREFIX;
import static yanagishima.util.PathUtil.getResultFilePath;
import static yanagishima.util.QueryEngine.hive;
import static yanagishima.util.QueryEngine.spark;
import static yanagishima.util.TypeCoerceUtil.objectToString;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import io.airlift.units.DataSize;
import io.airlift.units.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.client.fluentd.FluencyClient;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.HiveQueryErrorException;
import yanagishima.model.hive.HiveQueryResult;
import yanagishima.pool.StatementPool;
import yanagishima.util.QueryIdUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class HiveService {
  private final QueryService queryService;
  private final YanagishimaConfig config;
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);
  private final FluencyClient fluencyClient;
  private final StatementPool statementPool;

  public String doQueryAsync(String engine, String datasource, String query, String userName,
                             Optional<String> hiveUser, Optional<String> hivePassword) {
    String queryId = QueryIdUtil.generate(datasource, query, engine);
    executorService.submit(new Task(queryId, engine, datasource, query, userName, hiveUser, hivePassword));
    return queryId;
  }

  public class Task implements Runnable {
    private final String queryId;
    private final String engine;
    private final String datasource;
    private final String query;
    private final String userName;
    private final Optional<String> hiveUser;
    private final Optional<String> hivePassword;

    public Task(String queryId, String engine, String datasource, String query, String userName,
                Optional<String> hiveUser, Optional<String> hivePassword) {
      this.queryId = queryId;
      this.engine = engine;
      this.datasource = datasource;
      this.query = query;
      this.userName = userName;
      this.hiveUser = hiveUser;
      this.hivePassword = hivePassword;
    }

    @Override
    public void run() {
      try {
        int limit = config.getSelectLimit();
        getHiveQueryResult(queryId, engine, datasource, query, true, limit, userName, hiveUser, hivePassword,
                           true);
      } catch (Throwable e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  public HiveQueryResult doQuery(String engine,
                                 String datasource,
                                 String query,
                                 String userName,
                                 Optional<String> hiveUser,
                                 Optional<String> hivePassword,
                                 boolean storeFlag,
                                 int limit) throws HiveQueryErrorException {
    String queryId = QueryIdUtil.generate(datasource, query, engine);
    return getHiveQueryResult(queryId, engine, datasource, query, storeFlag, limit, userName, hiveUser,
                              hivePassword, false);
  }

  private HiveQueryResult getHiveQueryResult(String queryId,
                                             String engine,
                                             String datasource,
                                             String query,
                                             boolean storeFlag,
                                             int limit,
                                             String userName,
                                             Optional<String> hiveUser,
                                             Optional<String> hivePassword,
                                             boolean async) throws HiveQueryErrorException {
    checkDisallowedKeyword(userName, query, datasource, queryId, engine);
    checkSecretKeyword(userName, query, datasource, queryId, engine);
    checkRequiredCondition(userName, query, datasource, queryId, engine);

    try {
      Class.forName(config.getHiveDriverClassName(datasource));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }

    String url = getJdbcUrl(datasource, engine, userName);
    Credential credential = new Credential(config.getHiveJdbcUser(datasource),
                                           config.getHiveJdbcPassword(datasource), hiveUser, hivePassword);

    try (Connection connection = DriverManager.getConnection(url, credential.getUser(),
                                                             credential.getPassword())) {
      long start = System.currentTimeMillis();
      HiveQueryResult hiveQueryResult = new HiveQueryResult();
      hiveQueryResult.setQueryId(queryId);
      processData(engine, datasource, query, limit, userName, connection, queryId, start, hiveQueryResult,
                  async);
      if (storeFlag) {
        queryService.save(datasource, engine, query, userName, queryId, hiveQueryResult.getLineNumber());
      }
      emitExecutedEvent(userName, query, queryId, datasource, engine, System.currentTimeMillis() - start);
      return hiveQueryResult;

    } catch (SQLException e) {
      queryService.saveError(datasource, engine, queryId, query, userName, e.getMessage());
      throw new HiveQueryErrorException(queryId, e);
    }
  }

  private void processData(String engine,
                           String datasource,
                           String query,
                           int limit,
                           String userName,
                           Connection connection,
                           String queryId,
                           long start,
                           HiveQueryResult queryResult,
                           boolean async) throws SQLException {
    Duration queryMaxRunTime = new Duration(config.getHiveQueryMaxRunTimeSeconds(datasource), TimeUnit.SECONDS);
    try (Statement statement = connection.createStatement()) {
      int timeout = (int) queryMaxRunTime.toMillis() / 1000;
      statement.setQueryTimeout(timeout);
      if (engine.equals(hive.name())) {
        statement.execute("set mapreduce.job.name=" + toJobName(queryId, userName));
        List<String> hiveSetupQueryList = config.getHiveSetupQueryList(datasource);
        for (String hiveSetupQuery : hiveSetupQueryList) {
          statement.execute(hiveSetupQuery);
        }
      }

      if (async && config.isUseJdbcCancel(datasource)) {
        statementPool.put(datasource, queryId, statement);
      }

      boolean hasResultSet = statement.execute(query);
      if (!hasResultSet) {
        try {
          Path dst = getResultFilePath(datasource, queryId, false);
          dst.toFile().createNewFile();
          queryResult.setLineNumber(0);
          queryResult.setRawDataSize(new DataSize(0, DataSize.Unit.BYTE));
          queryResult.setRecords(new ArrayList<>());
          queryResult.setColumns(new ArrayList<>());
          return;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      try (ResultSet resultSet = statement.getResultSet()) {
        ResultSetMetaData metadata = resultSet.getMetaData();
        int columnCount = metadata.getColumnCount();
        List<String> columnNameList = new ArrayList<>();
        for (int i = 1; i <= columnCount; i++) {
          columnNameList.add(metadata.getColumnName(i));
        }

        Path dst = getResultFilePath(datasource, queryId, false);
        int lineNumber = 0;
        long maxResultFileByteSize = config.getHiveMaxResultFileByteSize();
        long resultBytes = 0;
        try (BufferedWriter bw = Files.newBufferedWriter(dst, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(bw, CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N")
                                                                    .withRecordSeparator(System.getProperty(
                                                                        "line.separator")))) {
          printer.printRecord(columnNameList);
          lineNumber++;
          queryResult.setColumns(columnNameList);

          List<List<String>> rows = new ArrayList<>();
          while (resultSet.next()) {
            List<String> row = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
              row.add(objectToString(resultSet.getObject(i)));
            }

            printer.printRecord(row);
            lineNumber++;
            resultBytes += row.toString().getBytes(StandardCharsets.UTF_8).length;
            if (resultBytes > maxResultFileByteSize) {
              String message = format("Result file size exceeded %s bytes. queryId=%s, datasource=%s",
                                      maxResultFileByteSize, queryId, datasource);
              queryService.saveError(datasource, engine, queryId, query, userName, message);
              throw new RuntimeException(message);
            }

            if (query.toLowerCase().startsWith("show") || rows.size() < limit) {
              rows.add(row);
            } else {
              queryResult.setWarningMessage(
                  format("now fetch size is %d. This is more than %d. So, fetch operation stopped.",
                         rows.size(), limit));
            }

            queryService.saveTimeout(queryMaxRunTime, start, datasource, engine, queryId, query, userName);
          }
          queryResult.setLineNumber(lineNumber);
          queryResult.setRecords(rows);
          if (async && config.isUseJdbcCancel(datasource)) {
            statementPool.remove(datasource, queryId);
          }

          queryResult.setRawDataSize(new DataSize(Files.size(dst), DataSize.Unit.BYTE).convertToMostSuccinctDataSize());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private void checkDisallowedKeyword(String userName, String query, String datasource, String queryId,
                                      String engine) {
    for (String keyword : config.getHiveDisallowedKeywords(datasource)) {
      if (query.trim().toLowerCase().startsWith(keyword)) {
        String message = format("query contains %s. This is the disallowed keywords in %s", keyword,
                                datasource);
        queryService.saveError(datasource, engine, queryId, query, userName, message);
        throw new RuntimeException(message);
      }
    }
  }

  private void checkSecretKeyword(String userName, String query, String datasource, String queryId,
                                  String engine) {
    for (String keyword : config.getHiveSecretKeywords(datasource)) {
      if (query.contains(keyword)) {
        String message = "query error occurs";
        queryService.saveError(datasource, engine, queryId, query, userName, message);
        throw new RuntimeException(message);
      }
    }
  }

  private void checkRequiredCondition(String userName, String query, String datasource, String queryId,
                                      String engine) {
    for (String requiredCondition : config.getHiveMustSpecifyConditions(datasource)) {
      String[] conditions = requiredCondition.split(",");
      for (String condition : conditions) {
        String table = condition.split(":")[0];
        if (!query.startsWith("SHOW") && !query.startsWith("DESCRIBE") && query.contains(table)) {
          String[] partitionKeys = condition.split(":")[1].split("\\|");
          for (String partitionKey : partitionKeys) {
            if (!query.contains(partitionKey)) {
              String message = format("If you query %s, you must specify %s in where clause", table,
                                      partitionKey);
              queryService.saveError(datasource, engine, queryId, query, userName, message);
              throw new RuntimeException(message);
            }
          }
        }
      }
    }
  }

  private void emitExecutedEvent(String username, String query, String queryId, String datasource,
                                 String engine, long elapsedTime) {
    Map<String, Object> event = new HashMap<>();
    event.put("elapsed_time_millseconds", elapsedTime);
    event.put("user", username);
    event.put("query", query);
    event.put("query_id", queryId);
    event.put("datasource", datasource);
    event.put("engine", engine);

    fluencyClient.emitExecuted(event);
  }

  private String getJdbcUrl(String datasource, String engine, String userName) {
    if (engine.equals(hive.name())) {
      if (config.isHiveImpersonation(datasource)) {
        return format("%s;hive.server2.proxy.user=%s", config.getHiveJdbcUrl(datasource), userName);
      }
      return config.getHiveJdbcUrl(datasource);
    }
    if (engine.equals(spark.name())) {
      return config.getSparkJdbcUrl(datasource);
    }
    throw new IllegalArgumentException(engine + " is illegal");
  }

  private static String toJobName(String queryId, String userName) {
    if (userName == null) {
      return YANAGISHIAM_HIVE_JOB_PREFIX + queryId;
    }
    return YANAGISHIAM_HIVE_JOB_PREFIX + userName + "-" + queryId;
  }

  static class Credential {
    private final String user;
    private final String password;

    Credential(String user, String password, Optional<String> hiveUser, Optional<String> hivePassword) {
      if (hiveUser.isPresent() && hivePassword.isPresent()) {
        this.user = hiveUser.get();
        this.password = hivePassword.get();
      } else {
        this.user = user;
        this.password = password;
      }
    }

    public String getUser() {
      return user;
    }

    public String getPassword() {
      return password;
    }
  }
}
