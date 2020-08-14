package yanagishima.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.prestosql.client.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.airlift.units.DataSize;
import io.airlift.units.Duration;
import me.geso.tinyorm.TinyORM;
import okhttp3.OkHttpClient;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.komamitsu.fluency.Fluency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.result.PrestoQueryResult;
import yanagishima.util.Constants;
import yanagishima.util.TypeCoerceUtil;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;
import static io.airlift.units.DataSize.Unit.BYTE;
import static io.prestosql.client.OkHttpUtil.basicAuth;
import static io.prestosql.client.OkHttpUtil.setupTimeouts;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static yanagishima.util.DbUtil.insertQueryHistory;
import static yanagishima.util.DbUtil.insertSessionProperty;
import static yanagishima.util.DbUtil.storeError;
import static yanagishima.util.FluentdUtil.buildStaticFluency;
import static yanagishima.util.PathUtil.getResultFilePath;
import static yanagishima.util.QueryEngine.presto;
import static yanagishima.util.TimeoutUtil.checkTimeout;

public class PrestoServiceImpl implements PrestoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrestoServiceImpl.class);
    private static final CSVFormat CSV_FORMAT = CSVFormat.EXCEL
            .withDelimiter('\t')
            .withNullString("\\N")
            .withRecordSeparator(System.getProperty("line.separator"));

    private final YanagishimaConfig config;
    private final OkHttpClient httpClient;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final Fluency fluency;
    private final TinyORM db;

    private final int maxResultFileByteSize;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private Map<String, String> properties = ImmutableMap.of();

    @Inject
    public PrestoServiceImpl(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        setupTimeouts(builder, 5, SECONDS);
        httpClient = builder.build();
        this.fluency = buildStaticFluency(config);
        this.maxResultFileByteSize = config.getMaxResultFileByteSize();
    }

    @Override
    public String doQueryAsync(String datasource, String query, Optional<String> sessionPropertyOptional, String userName, Optional<String> prestoUser, Optional<String> prestoPassword) {
        sessionPropertyOptional.ifPresent(sessionProperty -> {
            try {
                properties = OBJECT_MAPPER.readValue(sessionProperty, Map.class);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });

        StatementClient client = getStatementClient(datasource, query, userName, prestoUser, prestoPassword);
        executorService.submit(new Task(client, datasource, query, userName));
        return client.currentStatusInfo().getId();
    }

    public class Task implements Runnable {
        private final StatementClient client;
        private final String datasource;
        private final String query;
        private final String userName;

        Task(StatementClient client, String datasource, String query, String userName) {
            this.client = client;
            this.datasource = datasource;
            this.query = query;
            this.userName = userName;
        }

        @Override
        public void run() {
            try {
                getPrestoQueryResult(this.datasource, this.query, this.client, true, config.getSelectLimit(), this.userName);
            } catch (QueryErrorException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                if (this.client != null) {
                    this.client.close();
                }
            }
        }
    }

    @Override
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

    private PrestoQueryResult getPrestoQueryResult(String datasource, String query, StatementClient client, boolean storeQueryHistory, int limit, String userName) throws QueryErrorException {
        checkSecretKeyword(query, datasource, client.currentStatusInfo().getId(), userName, config.getPrestoSecretKeywords(datasource));
        checkRequiredCondition(datasource, query, client.currentStatusInfo().getId(), userName,  config.getPrestoMustSpecifyConditions(datasource));
        List<String> prestoDisallowedKeywords = config.getPrestoDisallowedKeywords(datasource);
        for (String prestoDisallowedKeyword : prestoDisallowedKeywords) {
            if (query.trim().toLowerCase().startsWith(prestoDisallowedKeyword)) {
                String message = String.format("query contains %s. This is the disallowed keywords in %s", prestoDisallowedKeyword, datasource);
                storeError(db, datasource, "presto", client.currentStatusInfo().getId(), query, userName, message);
                throw new RuntimeException(message);
            }
        }
        Duration queryMaxRunTime = new Duration(config.getQueryMaxRunTimeSeconds(datasource), SECONDS);
        long start = System.currentTimeMillis();
        while (client.isRunning() && client.currentData().getData() == null) {
            try {
                client.advance();
            } catch (RuntimeException e) {
                QueryStatusInfo statusInfo = client.isRunning() ? client.currentStatusInfo() : client.finalStatusInfo();
                String message = format("Query failed (#%s) in %s: presto internal error message=%s", statusInfo.getId(), datasource, e.getMessage());
                storeError(db, datasource, presto.name(), statusInfo.getId(), query, userName, message);
                throw e;
            }

            if (System.currentTimeMillis() - start > queryMaxRunTime.toMillis()) {
                String queryId = client.currentStatusInfo().getId();
                String message = format("Query failed (#%s) in %s: Query exceeded maximum time limit of %s", queryId, datasource, queryMaxRunTime.toString());
                storeError(db, datasource, presto.name(), queryId, query, userName, message);
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
            List<String> columnNames = results.getColumns().stream().map(Column::getName).collect(Collectors.toList());
            queryResult.setColumns(columnNames);
            List<List<String>> rows = processData(client, datasource, results.getId(), queryResult, columnNames, start, limit, userName);
            queryResult.setRecords(rows);
            if (storeQueryHistory) {
                insertQueryHistory(db, datasource, presto.name(), query, userName, results.getId(), queryResult.getLineNumber());
                insertSessionProperty(db, datasource, presto.name(), results.getId(), properties);
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
                storeError(db, datasource, presto.name(), results.getId(), query, userName, message);
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
        sb.append(failureInfo.getType() + ": " + failureInfo.getMessage() + separator + join(separator, failureInfo.getStack()));
        FailureInfo cause = failureInfo.getCause();
        while (cause != null) {
            sb.append(System.lineSeparator() + "Caused by: " + cause.getType() + ": " + cause.getMessage() + separator + join(separator, cause.getStack()));
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
        int resultBytes = 0;
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
                        if (resultBytes > maxResultFileByteSize) {
                            String message = format("Result file size exceeded %s bytes. queryId=%s, datasource=%s", maxResultFileByteSize, queryId, datasource);
                            storeError(db, datasource, presto.name(), client.currentStatusInfo().getId(), client.getQuery(), userName, message);
                            throw new RuntimeException(message);
                        }
                        if (client.getQuery().toLowerCase().startsWith("show") || rows.size() < maxRowLimit) {
                            rows.add(row);
                        } else {
                            queryResult.setWarningMessage(format("now fetch size is %d. This is more than %d. So, fetch operation stopped.", rows.size(), maxRowLimit));
                        }
                        rowNumber++;
                    }
                }
                client.advance();
                checkTimeout(db, queryMaxRunTime, startTime, datasource, presto.name(), queryId, client.getQuery(), userName);
            }

            queryResult.setLineNumber(rowNumber);
            DataSize rawDataSize = new DataSize(Files.size(resultPath), BYTE);
            queryResult.setRawDataSize(rawDataSize.convertToMostSuccinctDataSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return rows;
    }

    private void emitExecutedEvent(String username, String query, String queryId, String datasource, long elapsedTime) {
        if (config.getFluentdExecutedTag().isEmpty()) {
            return;
        }

        Map<String, Object> event = new HashMap<>();
        event.put("elapsed_time_millseconds", elapsedTime);
        event.put("user", username);
        event.put("query", query);
        event.put("query_id", queryId);
        event.put("datasource", datasource);
        event.put("engine", presto.name());

        try {
            fluency.emit(config.getFluentdExecutedTag().get(), event);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void emitFailedEvent(String username, String query, String datasource, QueryStatusInfo status, long elapsedTime) {
        if (config.getFluentdFaliedTag().isEmpty()) {
            return;
        }

        Map<String, Object> event = new HashMap<>();
        event.put("elapsed_time_millseconds", elapsedTime);
        event.put("user", username);
        event.put("query", query);
        event.put("query_id", status.getId());
        event.put("datasource", datasource);
        event.put("errorName", status.getError().getErrorName());
        event.put("errorType", status.getError().getErrorType());
        event.put("message", status.getError().getMessage());

        try {
            fluency.emit(config.getFluentdFaliedTag().get(), event);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void checkSecretKeyword(String query, String datasource, String id, String username, List<String> secretKeywords) {
        for (String secretKeyword : secretKeywords) {
            if (query.contains(secretKeyword)) {
                String message = "query error occurs";
                storeError(db, datasource, presto.name(), id, query, username, message);
                throw new RuntimeException(message);
            }
        }
    }

    private void checkRequiredCondition(String datasource, String query, String id, String username, List<String> requiredConditions) {
        for (String requiredCondition : requiredConditions) {
            String[] conditions = requiredCondition.split(",");
            for (String condition : conditions) {
                String table = condition.split(":")[0];
                if (!query.startsWith(Constants.YANAGISHIMA_COMMENT) && query.contains(table)) {
                    String[] partitionKeys = condition.split(":")[1].split("\\|");
                    for (String partitionKey : partitionKeys) {
                        if (!query.contains(partitionKey)) {
                            String message = format("If you query %s, you must specify %s in where clause", table, partitionKey);
                            storeError(db, datasource, presto.name(), id, query, username, message);
                            throw new RuntimeException(message);
                        }
                    }
                }
            }
        }
    }

    private StatementClient getStatementClient(String datasource, String query, String userName, Optional<String> prestoUser, Optional<String> prestoPassword) {
        String server = config.getPrestoCoordinatorServer(datasource);
        String catalog = config.getCatalog(datasource);
        String schema = config.getSchema(datasource);
        String source = config.getSource(datasource);

        if (prestoUser.isPresent() && prestoPassword.isPresent()) {
            ClientSession clientSession = buildClientSession(server, prestoUser.get(), source, catalog, schema, properties);
            checkArgument(clientSession.getServer().getScheme().equalsIgnoreCase("https"), "Authentication using username/password requires HTTPS to be enabled");
            OkHttpClient.Builder clientBuilder = httpClient.newBuilder();
            clientBuilder.addInterceptor(basicAuth(prestoUser.get(), prestoPassword.get()));
            return StatementClientFactory.newStatementClient(clientBuilder.build(), clientSession, query);
        }

        String user = firstNonNull(userName, config.getUser(datasource));
        ClientSession clientSession = buildClientSession(server, user, source, catalog, schema, properties);
        return StatementClientFactory.newStatementClient(httpClient, clientSession, query);
    }

    private static ClientSession buildClientSession(String server, String user, String source, String catalog, String schema, Map<String, String> properties) {
        return new ClientSession(URI.create(server), user, source, Optional.empty(), ImmutableSet.of(), null, catalog,
                                 schema, null, ZoneId.systemDefault(), false, Locale.getDefault(),
                                 ImmutableMap.of(), properties, emptyMap(), emptyMap(), ImmutableMap.of(), null, new Duration(2, MINUTES));
    }

    private static QueryErrorException resultsException(QueryStatusInfo results, String datasource) {
        QueryError error = results.getError();
        String message = format("Query failed (#%s) in %s: %s", results.getId(), datasource, error.getMessage());
        Throwable cause = (error.getFailureInfo() == null) ? null : error.getFailureInfo().toException();
        return new QueryErrorException(results.getId(), new SQLException(message, error.getSqlState(), error.getErrorCode(), cause));
    }
}

