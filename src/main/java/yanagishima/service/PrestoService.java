package yanagishima.service;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static io.prestosql.client.OkHttpUtil.basicAuth;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static yanagishima.util.PathUtil.getResultFilePath;
import static yanagishima.util.QueryEngine.presto;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.airlift.units.DataSize;
import io.airlift.units.Duration;
import io.prestosql.client.ClientSession;
import io.prestosql.client.Column;
import io.prestosql.client.FailureInfo;
import io.prestosql.client.QueryError;
import io.prestosql.client.QueryStatusInfo;
import io.prestosql.client.StatementClient;
import io.prestosql.client.StatementClientFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import yanagishima.client.fluentd.FluencyClient;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.model.presto.PrestoQueryResult;
import yanagishima.util.Constants;
import yanagishima.util.TypeCoerceUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrestoService {
  private static final CSVFormat CSV_FORMAT = CSVFormat.EXCEL
      .withDelimiter('\t')
      .withNullString("\\N")
      .withRecordSeparator(System.getProperty("line.separator"));

  private final YanagishimaConfig config;
  private final OkHttpClient httpClient = new OkHttpClient.Builder()
      .connectTimeout(java.time.Duration.ofSeconds(5))
      .readTimeout(java.time.Duration.ofSeconds(5))
      .writeTimeout(java.time.Duration.ofSeconds(5))
      .build();
  private final SessionPropertyService sessionPropertyService;
  private final QueryService queryService;
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);
  private final FluencyClient fluencyClient;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public String doQueryAsync(String datasource, String query, Optional<String> sessionPropertyOptional,
                             String userName, Optional<String> prestoUser, Optional<String> prestoPassword) {
    Map<String, String> properties = ImmutableMap.of();
    if (sessionPropertyOptional.isPresent()) {
      try {
        properties = OBJECT_MAPPER.readValue(sessionPropertyOptional.get(), Map.class);
      } catch (IOException e) {
        log.error(e.getMessage(), e);
        throw new RuntimeException(e);
      }
    }

    StatementClient client = getStatementClient(
        datasource, query, userName, prestoUser, prestoPassword, properties);
    executorService.submit(new Task(client, datasource, query, userName, properties));
    return client.currentStatusInfo().getId();
  }

  public class Task implements Runnable {
    private final StatementClient client;
    private final String datasource;
    private final String query;
    private final String userName;
    private final Map<String, String> properties;

    Task(StatementClient client, String datasource, String query,
         String userName, Map<String, String> properties) {
      this.client = client;
      this.datasource = datasource;
      this.query = query;
      this.userName = userName;
      this.properties = properties;
    }

    @Override
    public void run() {
      try {
        getPrestoQueryResult(this.datasource, this.query, this.client, true, config.getSelectLimit(),
                             this.userName, this.properties);
      } catch (QueryErrorException e) {
        log.error(e.getMessage(), e);
      } catch (Throwable e) {
        log.error(e.getMessage(), e);
      } finally {
        if (this.client != null) {
          this.client.close();
        }
      }
    }
  }

  public PrestoQueryResult doQuery(String datasource,
                                   String query,
                                   String userName,
                                   Optional<String> prestoUser,
                                   Optional<String> prestoPassword,
                                   Map<String, String> properties,
                                   boolean storeFlag,
                                   int limit) throws QueryErrorException {
    try (StatementClient client = getStatementClient(datasource, query, userName,
                                                     prestoUser, prestoPassword, properties)) {
      return getPrestoQueryResult(datasource, query, client, storeFlag, limit, userName, properties);
    }
  }

  private PrestoQueryResult getPrestoQueryResult(
      String datasource, String query, StatementClient client, boolean storeQueryHistory,
      int limit, String userName, Map<String, String> properties) throws QueryErrorException {
    checkSecretKeyword(query, datasource, client.currentStatusInfo().getId(), userName,
                       config.getPrestoSecretKeywords(datasource));
    checkRequiredCondition(datasource, query, client.currentStatusInfo().getId(), userName,
                           config.getPrestoMustSpecifyConditions(datasource));

    Duration queryMaxRunTime = new Duration(config.getQueryMaxRunTimeSeconds(datasource), SECONDS);
    long start = System.currentTimeMillis();
    while (client.isRunning() && client.currentData().getData() == null) {
      try {
        client.advance();
      } catch (RuntimeException e) {
        QueryStatusInfo statusInfo = client.isRunning() ? client.currentStatusInfo() : client.finalStatusInfo();
        String message = format("Query failed (#%s) in %s: presto internal error message=%s",
                                statusInfo.getId(), datasource, e.getMessage());
        queryService.saveError(datasource, presto.name(), statusInfo.getId(), query, userName, message);
        throw e;
      }

      if (System.currentTimeMillis() - start > queryMaxRunTime.toMillis()) {
        String queryId = client.currentStatusInfo().getId();
        String message = format("Query failed (#%s) in %s: Query exceeded maximum time limit of %s", queryId,
                                datasource, queryMaxRunTime.toString());
        queryService.saveError(datasource, presto.name(), queryId, query, userName, message);
        throw new RuntimeException(message);
      }
    }

    PrestoQueryResult queryResult = new PrestoQueryResult();
    // if running or finished
    if (client.isRunning() || (client.isFinished() && client.finalStatusInfo().getError() == null)) {
      QueryStatusInfo results = client.isRunning() ? client.currentStatusInfo() : client.finalStatusInfo();
      if (results.getColumns() == null) {
        throw new QueryErrorException(new SQLException(format("Query %s has no columns\n", results.getId())));
      }
      queryResult.setQueryId(results.getId());
      queryResult.setUpdateType(results.getUpdateType());
      List<String> columnNames = results.getColumns().stream().map(Column::getName).collect(
          Collectors.toList());
      queryResult.setColumns(columnNames);
      List<List<String>> rows = processData(client, datasource, results.getId(), queryResult, columnNames,
                                            start, limit, userName);
      queryResult.setRecords(rows);
      if (storeQueryHistory) {
        queryService.save(datasource, presto.name(), query, userName, results.getId(),
                          queryResult.getLineNumber());
        sessionPropertyService.insert(datasource, presto.name(), results.getId(), properties);
      }
      emitExecutedEvent(userName, query, results.getId(), datasource, System.currentTimeMillis() - start);
    }

    checkState(!client.isRunning());
    checkState(!client.isClientAborted(), "Query aborted by user");
    checkState(!client.isClientError(), "Query is gone (server restarted?)");
    verify(client.isFinished());

    if (client.finalStatusInfo().getError() != null) {
      QueryStatusInfo results = client.finalStatusInfo();
      String message = getErrorMessage(results.getError().getFailureInfo());
      if (queryResult.getQueryId() == null) {
        queryService.saveError(datasource, presto.name(), results.getId(), query, userName, message);
      } else {
        Path successFile = getResultFilePath(datasource, queryResult.getQueryId(), false);
        Path errorFile = getResultFilePath(datasource, queryResult.getQueryId(), true);
        try {
          Files.delete(successFile);
          try (BufferedWriter writer = Files.newBufferedWriter(errorFile, UTF_8)) {
            writer.write(message);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      emitFailedEvent(userName, query, datasource, results, System.currentTimeMillis() - start);
      throw resultsException(results, datasource);
    }
    return queryResult;
  }

  private String getErrorMessage(FailureInfo failureInfo) {
    String separator = System.lineSeparator() + "    at ";
    StringBuilder sb = new StringBuilder();
    sb.append(failureInfo.getType() + ": " + failureInfo.getMessage() + separator + join(separator, failureInfo
        .getStack()));
    FailureInfo cause = failureInfo.getCause();
    while (cause != null) {
      sb.append(System.lineSeparator() + "Caused by: " + cause.getType() + ": " + cause.getMessage() + separator
                + join(separator, cause.getStack()));
      cause = cause.getCause();
    }
    return sb.toString();
  }

  private List<List<String>> processData(StatementClient client,
                                         String datasource,
                                         String queryId,
                                         PrestoQueryResult queryResult,
                                         List<String> columnNames,
                                         long startTime,
                                         int maxRowLimit,
                                         String userName) {
    List<List<String>> rows = new ArrayList<>();
    Duration queryMaxRunTime = new Duration(config.getQueryMaxRunTimeSeconds(datasource), SECONDS);
    Path resultPath = getResultFilePath(datasource, queryId, false);

    int rowNumber = 0;
    long resultBytes = 0;
    try (BufferedWriter writer = Files.newBufferedWriter(resultPath, UTF_8);
         CSVPrinter printer = new CSVPrinter(writer, CSV_FORMAT)) {

      printer.printRecord(columnNames);
      rowNumber++;

      while (client.isRunning()) {
        Iterable<List<Object>> datum = client.currentData().getData();
        if (datum != null) {
          for (List<Object> data : datum) {
            List<String> row = data.stream().map(TypeCoerceUtil::objectToString).collect(Collectors.toList());
            printer.printRecord(row);
            resultBytes += row.toString().getBytes(UTF_8).length;
            if (resultBytes > config.getMaxResultFileByteSize()) {
              String message = format("Result file size exceeded %s bytes. queryId=%s, datasource=%s",
                                      config.getMaxResultFileByteSize(), queryId, datasource);
              queryService.saveError(datasource, presto.name(), client.currentStatusInfo().getId(),
                                     client.getQuery(), userName, message);
              throw new RuntimeException(message);
            }
            if (client.getQuery().toLowerCase().startsWith("show") || rows.size() < maxRowLimit) {
              rows.add(row);
            } else {
              queryResult.setWarningMessage(
                  format("now fetch size is %d. This is more than %d. So, fetch operation stopped.",
                         rows.size(), maxRowLimit));
            }
            rowNumber++;
          }
        }
        client.advance();
        queryService.saveTimeout(queryMaxRunTime, startTime, datasource, presto.name(), queryId,
                                 client.getQuery(), userName);
      }

      queryResult.setLineNumber(rowNumber);
      queryResult.setRawDataSize(DataSize.ofBytes(Files.size(resultPath)).succinct());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return rows;
  }

  private void emitExecutedEvent(String username, String query, String queryId, String datasource,
                                 long elapsedTime) {
    Map<String, Object> event = new HashMap<>();
    event.put("elapsed_time_millseconds", elapsedTime);
    event.put("user", username);
    event.put("query", query);
    event.put("query_id", queryId);
    event.put("datasource", datasource);
    event.put("engine", presto.name());

    fluencyClient.emitExecuted(event);
  }

  private void emitFailedEvent(String username, String query, String datasource, QueryStatusInfo status,
                               long elapsedTime) {
    Map<String, Object> event = new HashMap<>();
    event.put("elapsed_time_millseconds", elapsedTime);
    event.put("user", username);
    event.put("query", query);
    event.put("query_id", status.getId());
    event.put("datasource", datasource);
    event.put("errorName", status.getError().getErrorName());
    event.put("errorType", status.getError().getErrorType());
    event.put("message", status.getError().getMessage());

    fluencyClient.emitFailed(event);
  }

  private void checkSecretKeyword(String query, String datasource, String id, String username,
                                  List<String> secretKeywords) {
    for (String secretKeyword : secretKeywords) {
      if (query.contains(secretKeyword)) {
        String message = "query error occurs";
        queryService.saveError(datasource, presto.name(), id, query, username, message);
        throw new RuntimeException(message);
      }
    }
  }

  private void checkRequiredCondition(String datasource, String query, String id, String username,
                                      List<String> requiredConditions) {
    for (String requiredCondition : requiredConditions) {
      String[] conditions = requiredCondition.split(",");
      for (String condition : conditions) {
        String table = condition.split(":")[0];
        if (!query.startsWith(Constants.YANAGISHIMA_COMMENT) && query.contains(table)) {
          String[] partitionKeys = condition.split(":")[1].split("\\|");
          for (String partitionKey : partitionKeys) {
            if (!query.contains(partitionKey)) {
              String message = format("If you query %s, you must specify %s in where clause", table,
                                      partitionKey);
              queryService.saveError(datasource, presto.name(), id, query, username, message);
              throw new RuntimeException(message);
            }
          }
        }
      }
    }
  }

  private StatementClient getStatementClient(String datasource, String query, String userName,
                                             Optional<String> prestoUser, Optional<String> prestoPassword,
                                             Map<String, String> properties) {
    String server = config.getPrestoCoordinatorServer(datasource);
    String catalog = config.getCatalog(datasource);
    String schema = config.getSchema(datasource);
    String source = config.getSource(datasource);

    if (prestoUser.isPresent() && prestoPassword.isPresent()) {
      ClientSession clientSession = buildClientSession(server, prestoUser.get(), source, catalog, schema,
                                                       properties);
      checkArgument(clientSession.getServer().getScheme().equalsIgnoreCase("https"),
                    "Authentication using username/password requires HTTPS to be enabled");
      OkHttpClient.Builder clientBuilder = httpClient.newBuilder();
      clientBuilder.addInterceptor(basicAuth(prestoUser.get(), prestoPassword.get()));
      return StatementClientFactory.newStatementClient(clientBuilder.build(), clientSession, query);
    }

    String user = firstNonNull(userName, config.getUser(datasource));
    Optional<String> prestoImpersonatedUser = config.getPrestoImpersonatedUser(datasource);
    Optional<String> prestoImpersonatedPassword = config.getPrestoImpersonatedPassword(datasource);
    if (config.isPrestoImpersonation(datasource)
            && prestoImpersonatedUser.isPresent() && prestoImpersonatedPassword.isPresent()) {
      ClientSession clientSession = buildClientSession(server, user, source, catalog, schema,
              properties);
      checkArgument(clientSession.getServer().getScheme().equalsIgnoreCase("https"),
              "Authentication using username/password requires HTTPS to be enabled");
      OkHttpClient.Builder clientBuilder = httpClient.newBuilder();
      clientBuilder.addInterceptor(basicAuth(prestoImpersonatedUser.get(), prestoImpersonatedPassword.get()));
      return StatementClientFactory.newStatementClient(clientBuilder.build(), clientSession, query);
    }
    ClientSession clientSession = buildClientSession(server, user, source, catalog, schema, properties);
    return StatementClientFactory.newStatementClient(httpClient, clientSession, query);
  }

  private static ClientSession buildClientSession(String server, String user, String source, String catalog,
                                                  String schema, Map<String, String> properties) {
    return new ClientSession(URI.create(server), user, source, Optional.empty(), ImmutableSet.of(), null,
                             catalog,
                             schema, null, ZoneId.systemDefault(), Locale.getDefault(),
                             ImmutableMap.of(), properties, emptyMap(), emptyMap(), ImmutableMap.of(), null,
                             new Duration(2, MINUTES), false);
  }

  private static QueryErrorException resultsException(QueryStatusInfo results, String datasource) {
    QueryError error = results.getError();
    String message = format("Query failed (#%s) in %s: %s", results.getId(), datasource, error.getMessage());
    Throwable cause = (error.getFailureInfo() == null) ? null : error.getFailureInfo().toException();
    return new QueryErrorException(results.getId(),
                                   new SQLException(message, error.getSqlState(), error.getErrorCode(), cause));
  }
}
