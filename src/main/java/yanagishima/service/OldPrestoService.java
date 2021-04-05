package yanagishima.service;

import static com.facebook.presto.client.OkHttpUtil.basicAuth;
import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static yanagishima.util.PathUtil.getResultFilePath;
import static yanagishima.util.QueryEngine.presto;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import com.facebook.presto.client.ClientSession;
import com.facebook.presto.client.Column;
import com.facebook.presto.client.QueryError;
import com.facebook.presto.client.QueryStatusInfo;
import com.facebook.presto.client.StatementClient;
import com.facebook.presto.client.StatementClientFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import io.airlift.units.DataSize;
import io.airlift.units.Duration;
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
public class OldPrestoService {
  private static final CSVFormat CSV_FORMAT = CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N")
                                                             .withRecordSeparator(
                                                                 System.getProperty("line.separator"));

  private final QueryService queryService;
  private final YanagishimaConfig config;
  private final OkHttpClient httpClient = new OkHttpClient.Builder()
      .connectTimeout(5, SECONDS)
      .readTimeout(5, SECONDS)
      .writeTimeout(5, SECONDS)
      .build();
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);
  private final FluencyClient fluencyClient;

  public String doQueryAsync(String datasource, String query, String userName, Optional<String> prestoUser,
                             Optional<String> prestoPassword) {
    StatementClient client = getStatementClient(datasource, query, userName, prestoUser, prestoPassword);
    executorService.submit(new Task(datasource, query, client, userName));
    return client.currentStatusInfo().getId();
  }

  public class Task implements Runnable {
    private final String datasource;
    private final String query;
    private final StatementClient client;
    private final String userName;

    public Task(String datasource, String query, StatementClient client, String userName) {
      this.datasource = datasource;
      this.query = query;
      this.client = client;
      this.userName = userName;
    }

    @Override
    public void run() {
      try {
        int limit = config.getSelectLimit();
        getPrestoQueryResult(datasource, query, client, true, limit, userName);
      } catch (QueryErrorException e) {
        log.warn(e.getCause().getMessage());
      } catch (Throwable e) {
        log.error(e.getMessage(), e);
      } finally {
        if (client != null) {
          client.close();
        }
      }
    }
  }

  public PrestoQueryResult doQuery(String datasource,
                                   String query,
                                   String userName,
                                   Optional<String> prestoUser,
                                   Optional<String> prestoPassword,
                                   boolean storeFlag,
                                   int limit) throws QueryErrorException {
    try (StatementClient client = getStatementClient(datasource, query, userName, prestoUser, prestoPassword)) {
      return getPrestoQueryResult(datasource, query, client, storeFlag, limit, userName);
    }
  }

  private PrestoQueryResult getPrestoQueryResult(String datasource, String query, StatementClient client,
                                                 boolean storeFlag, int limit, String userName)
      throws QueryErrorException {
    checkSecretKeyword(userName, query, client.currentStatusInfo().getId(), datasource);
    checkRequiredCondition(userName, query, client.currentStatusInfo().getId(), datasource);

    Duration queryMaxRunTime = new Duration(config.getQueryMaxRunTimeSeconds(datasource), SECONDS);
    long start = System.currentTimeMillis();
    while (client.isRunning() && client.currentData().getData() == null) {
      try {
        client.advance();
      } catch (RuntimeException e) {
        QueryStatusInfo results = client.isRunning() ? client.currentStatusInfo() : client.finalStatusInfo();
        String queryId = results.getId();
        String message = format("Query failed (#%s) in %s: presto internal error message=%s", queryId,
                                datasource, e.getMessage());
        queryService.saveError(datasource, presto.name(), queryId, query, userName, message);
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

    PrestoQueryResult prestoQueryResult = new PrestoQueryResult();
    // if running or finished
    if (client.isRunning() || (client.isFinished() && client.finalStatusInfo().getError() == null)) {
      QueryStatusInfo results = client.isRunning() ? client.currentStatusInfo() : client.finalStatusInfo();
      String queryId = results.getId();
      if (results.getColumns() == null) {
        throw new QueryErrorException(new SQLException(format("Query %s has no columns\n", results.getId())));
      }
      prestoQueryResult.setQueryId(queryId);
      prestoQueryResult.setUpdateType(results.getUpdateType());
      List<String> columns = Lists.transform(results.getColumns(), Column::getName);
      prestoQueryResult.setColumns(columns);
      List<List<String>> rows = processData(client, datasource, queryId, query, prestoQueryResult, columns,
                                            start, limit, userName);
      prestoQueryResult.setRecords(rows);
      if (storeFlag) {
        queryService.save(datasource, presto.name(), query, userName, queryId,
                          prestoQueryResult.getLineNumber());
      }

      emitExecutedEvent(userName, query, queryId, datasource, System.currentTimeMillis() - start);
    }

    checkState(!client.isRunning());

    if (client.isClientAborted()) {
      throw new RuntimeException("Query aborted by user");
    }
    if (client.isClientError()) {
      throw new RuntimeException("Query is gone (server restarted?)");
    }
    verify(client.isFinished());

    if (client.finalStatusInfo().getError() != null) {
      QueryStatusInfo results = client.finalStatusInfo();
      if (prestoQueryResult.getQueryId() == null) {
        String queryId = results.getId();
        String message = format("Query failed (#%s) in %s: %s", queryId, datasource,
                                results.getError().getMessage());
        queryService.saveError(datasource, presto.name(), queryId, query, userName, message);
      } else {
        Path successDst = getResultFilePath(datasource, prestoQueryResult.getQueryId(), false);
        try {
          Files.delete(successDst);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
        Path dst = getResultFilePath(datasource, prestoQueryResult.getQueryId(), true);
        String message = format("Query failed (#%s) in %s: %s", prestoQueryResult.getQueryId(), datasource,
                                results.getError().getMessage());

        try (BufferedWriter bw = Files.newBufferedWriter(dst, StandardCharsets.UTF_8)) {
          bw.write(message);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      emitFailedEvent(userName, query, datasource, results, System.currentTimeMillis() - start);
      throw resultsException(results, datasource);
    }
    return prestoQueryResult;
  }

  private List<List<String>> processData(StatementClient client,
                                         String datasource,
                                         String queryId,
                                         String query,
                                         PrestoQueryResult queryResult,
                                         List<String> columns,
                                         long start,
                                         int limit,
                                         String userName) {
    List<List<String>> rows = new ArrayList<>();

    Duration queryMaxRunTime = new Duration(config.getQueryMaxRunTimeSeconds(datasource), SECONDS);
    Path dst = getResultFilePath(datasource, queryId, false);
    int lineNumber = 0;
    long maxResultFileByteSize = config.getMaxResultFileByteSize();
    long resultBytes = 0;
    try (BufferedWriter writer = Files.newBufferedWriter(dst, StandardCharsets.UTF_8);
         CSVPrinter printer = new CSVPrinter(writer, CSV_FORMAT)) {
      printer.printRecord(columns);
      lineNumber++;

      while (client.isRunning()) {
        Iterable<List<Object>> datum = client.currentData().getData();
        if (datum != null) {
          for (List<Object> data : datum) {
            List<String> row = data.stream().map(TypeCoerceUtil::objectToString).collect(Collectors.toList());
            printer.printRecord(row);

            lineNumber++;
            resultBytes += row.toString().getBytes(StandardCharsets.UTF_8).length;
            if (resultBytes > maxResultFileByteSize) {
              String message = format("Result file size exceeded %s bytes. queryId=%s, datasource=%s",
                                      maxResultFileByteSize, queryId, datasource);
              queryService.saveError(datasource, presto.name(), client.currentStatusInfo().getId(), query,
                                     userName, message);
              throw new RuntimeException(message);
            }

            if (client.getQuery().toLowerCase().startsWith("show") || rows.size() < limit) {
              rows.add(row);
            } else {
              queryResult.setWarningMessage(
                  format("now fetch size is %d. This is more than %d. So, fetch operation stopped.",
                         rows.size(), limit));
            }
          }
        }
        client.advance();
        queryService.saveTimeout(queryMaxRunTime, start, datasource, presto.name(), queryId, query, userName);
      }

      queryResult.setLineNumber(lineNumber);
      long size = Files.size(dst);
      DataSize rawDataSize = new DataSize(size, DataSize.Unit.BYTE);
      queryResult.setRawDataSize(rawDataSize.convertToMostSuccinctDataSize());
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

  private void emitFailedEvent(String username, String query, String datasource, QueryStatusInfo results,
                               long elapsedTime) {
    Map<String, Object> event = new HashMap<>();
    event.put("elapsed_time_millseconds", elapsedTime);
    event.put("user", username);
    event.put("query", query);
    event.put("query_id", results.getId());
    event.put("datasource", datasource);
    event.put("errorName", results.getError().getErrorName());
    event.put("errorType", results.getError().getErrorType());
    event.put("message", results.getError().getMessage());

    fluencyClient.emitFailed(event);
  }

  private void checkSecretKeyword(String username, String query, String queryId, String datasource) {
    for (String keyword : config.getPrestoSecretKeywords(datasource)) {
      if (query.contains(keyword)) {
        String message = "query error occurs";
        queryService.saveError(datasource, presto.name(), queryId, query, username, message);
        throw new RuntimeException(message);
      }
    }
  }

  private void checkRequiredCondition(String username, String query, String queryId, String datasource) {
    for (String requiredCondition : config.getPrestoMustSpecifyConditions(datasource)) {
      String[] conditions = requiredCondition.split(",");
      for (String condition : conditions) {
        String table = condition.split(":")[0];
        if (!query.startsWith(Constants.YANAGISHIMA_COMMENT) && query.contains(table)) {
          String[] partitionKeys = condition.split(":")[1].split("\\|");
          for (String partitionKey : partitionKeys) {
            if (!query.contains(partitionKey)) {
              String message = format("If you query %s, you must specify %s in where clause", table,
                                      partitionKey);
              queryService.saveError(datasource, presto.name(), queryId, query, username, message);
              throw new RuntimeException(message);
            }
          }
        }
      }
    }
  }

  private StatementClient getStatementClient(String datasource, String query, String userName,
                                             Optional<String> prestoUser, Optional<String> prestoPassword) {
    String server = config.getPrestoCoordinatorServer(datasource);
    String catalog = config.getCatalog(datasource);
    String schema = config.getSchema(datasource);
    String source = config.getSource(datasource);

    if (prestoUser.isPresent() && prestoPassword.isPresent()) {
      ClientSession clientSession = buildClientSession(server, prestoUser.get(), source, catalog, schema);
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
      ClientSession clientSession = buildClientSession(server, user, source, catalog, schema);
      checkArgument(clientSession.getServer().getScheme().equalsIgnoreCase("https"),
              "Authentication using username/password requires HTTPS to be enabled");
      OkHttpClient.Builder clientBuilder = httpClient.newBuilder();
      clientBuilder.addInterceptor(basicAuth(prestoImpersonatedUser.get(), prestoImpersonatedPassword.get()));
      return StatementClientFactory.newStatementClient(clientBuilder.build(), clientSession, query);
    }
    ClientSession clientSession = buildClientSession(server, user, source, catalog, schema);
    return StatementClientFactory.newStatementClient(httpClient, clientSession, query);
  }

  private static ClientSession buildClientSession(String server, String user, String source, String catalog,
                                                  String schema) {
    return new ClientSession(URI.create(server), user, source, Optional.empty(), ImmutableSet.of(), null,
                             catalog,
                             schema, null, TimeZone.getDefault().getID(), Locale.getDefault(),
                             ImmutableMap.of(), ImmutableMap.of(), emptyMap(), null, new Duration(2, MINUTES));
  }

  private static QueryErrorException resultsException(QueryStatusInfo results, String datasource) {
    QueryError error = results.getError();
    String message = format("Query failed (#%s) in %s: %s", results.getId(), datasource, error.getMessage());
    Throwable cause = (error.getFailureInfo() == null) ? null : error.getFailureInfo().toException();
    return new QueryErrorException(results.getId(),
                                   new SQLException(message, error.getSqlState(), error.getErrorCode(), cause));
  }
}
