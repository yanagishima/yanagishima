package yanagishima.service;

import com.facebook.presto.client.*;
import com.google.common.collect.Lists;
import io.airlift.http.client.HttpClientConfig;
import io.airlift.http.client.jetty.JettyHttpClient;
import io.airlift.json.JsonCodec;
import io.airlift.units.DataSize;
import io.airlift.units.Duration;
import me.geso.tinyorm.TinyORM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.result.PrestoQueryResult;
import yanagishima.row.Query;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.airlift.json.JsonCodec.jsonCodec;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MINUTES;

public class PrestoServiceImpl implements PrestoService {

    private static Logger LOGGER = LoggerFactory.getLogger(PrestoServiceImpl.class);

    private YanagishimaConfig yanagishimaConfig;

    private JettyHttpClient httpClient;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Inject
    private TinyORM db;

    @Inject
    public PrestoServiceImpl(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
        HttpClientConfig httpClientConfig = new HttpClientConfig().setConnectTimeout(new Duration(10, TimeUnit.SECONDS));
        this.httpClient = new JettyHttpClient(httpClientConfig);
    }

    @Override
    public String doQueryAsync(String datasource, String query, String userName) {
        StatementClient client = getStatementClient(datasource, query, userName);
        executorService.submit(new Task(datasource, query, client));
        return client.current().getId();
    }

    public class Task implements Runnable {
        private String datasource;
        private String query;
        private StatementClient client;

        public Task(String datasource, String query, StatementClient client) {
            this.datasource = datasource;
            this.query = query;
            this.client = client;
        }

        @Override
        public void run() {
            try {
                getPrestoQueryResult(this.datasource, this.query, this.client, true);
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                if(this.client != null) {
                    this.client.close();
                }
            }
        }
    }

    @Override
    public PrestoQueryResult doQuery(String datasource, String query, String userName) throws QueryErrorException {
        try (StatementClient client = getStatementClient(datasource, query, userName)) {
            return getPrestoQueryResult(datasource, query, client, false);
        }
    }

    private PrestoQueryResult getPrestoQueryResult(String datasource, String query, StatementClient client, boolean asyncFlag) throws QueryErrorException {
        long start = System.currentTimeMillis();
        Duration queryMaxRunTime = new Duration(yanagishimaConfig.getQueryMaxRunTimeSeconds(), TimeUnit.SECONDS);
        while (client.isValid() && (client.current().getData() == null)) {
            client.advance();
            if(System.currentTimeMillis() - start > queryMaxRunTime.toMillis()) {
                String message = "Query exceeded maximum time limit of " + queryMaxRunTime;
                storeError(datasource, client.current().getId(), query, message);
                throw new RuntimeException(message);
            }
        }

        if ((!client.isFailed()) && (!client.isGone()) && (!client.isClosed())) {
            QueryResults results = client.isValid() ? client.current() : client.finalResults();
            String queryId = results.getId();
            if (results.getColumns() == null) {
                throw new QueryErrorException(new SQLException(format("Query %s has no columns\n", results.getId())));
            } else {
                PrestoQueryResult prestoQueryResult = new PrestoQueryResult();
                prestoQueryResult.setQueryId(queryId);
                prestoQueryResult.setUpdateType(results.getUpdateType());
                List<String> columns = Lists.transform(results.getColumns(), Column::getName);
                prestoQueryResult.setColumns(columns);
                List<List<String>> rowDataList = new ArrayList<List<String>>();
                processData(client, datasource, queryId, query, prestoQueryResult, columns, rowDataList);
                prestoQueryResult.setRecords(rowDataList);
                if(asyncFlag) {
                    insertQueryHistory(datasource, query, queryId);
                }
                return prestoQueryResult;
            }
        }

        if (client.isClosed()) {
            throw new RuntimeException("Query aborted by user");
        } else if (client.isGone()) {
            throw new RuntimeException("Query is gone (server restarted?)");
        } else if (client.isFailed()) {
            QueryResults results = client.finalResults();
            storeError(datasource, results.getId(), query, results.getError().getMessage());
            throw resultsException(results);
        }
        throw new RuntimeException("should not reach");
    }

    private void storeError(String datasource, String queryId, String query, String errorMessage) {
        db.insert(Query.class)
                .value("datasource", datasource)
                .value("query_id", queryId)
                .value("fetch_result_time_string", ZonedDateTime.now().toString())
                .value("query_string", query)
                .execute();
        Path dst = getResultFilePath(datasource, queryId, true);
        String message = format("Query failed (#%s): %s", queryId, errorMessage);

        try (BufferedWriter bw = Files.newBufferedWriter(dst, StandardCharsets.UTF_8)) {
            bw.write(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private void insertQueryHistory(String datasource, String query, String queryId) {
        db.insert(Query.class)
                .value("datasource", datasource)
                .value("query_id", queryId)
                .value("fetch_result_time_string", ZonedDateTime.now().toString())
                .value("query_string", query)
                .execute();
    }

    private void processData(StatementClient client, String datasource, String queryId, String query, PrestoQueryResult prestoQueryResult, List<String> columns, List<List<String>> rowDataList) {
        int limit = yanagishimaConfig.getSelectLimit();
        Path dst = getResultFilePath(datasource, queryId, false);
        int lineNumber = 0;
        int maxResultFileByteSize = yanagishimaConfig.getMaxResultFileByteSize();
        int resultBytes = 0;
        try (BufferedWriter bw = Files.newBufferedWriter(dst, StandardCharsets.UTF_8)) {
            bw.write(String.join("\t", columns));
            bw.write("\n");
            lineNumber++;
            while (client.isValid()) {
                Iterable<List<Object>> data = client.current().getData();
                if (data != null) {
                    for(List<Object> row : data) {
                        List<String> columnDataList = new ArrayList<>();
                        List<Object> tmpColumnDataList = row.stream().collect(Collectors.toList());
                        for (Object tmpColumnData : tmpColumnDataList) {
                            if (tmpColumnData instanceof Long) {
                                columnDataList.add(((Long) tmpColumnData).toString());
                            } else {
                                if (tmpColumnData == null) {
                                    columnDataList.add(null);
                                } else {
                                    columnDataList.add(tmpColumnData.toString());
                                }
                            }
                        }
                        try {
                            String resultStr = String.join("\t", columnDataList) + "\n";
                            bw.write(resultStr);
                            lineNumber++;
                            resultBytes += resultStr.getBytes(StandardCharsets.UTF_8).length;
                            if(resultBytes > maxResultFileByteSize) {
                                String message = String.format("Result file size exceeded %s bytes. queryId=%s", maxResultFileByteSize, queryId);
                                storeError(datasource, client.current().getId(), query, message);
                                throw new RuntimeException(message);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (client.getQuery().toLowerCase().startsWith("show") || rowDataList.size() < limit) {
                            rowDataList.add(columnDataList);
                        } else {
                            prestoQueryResult.setWarningMessage(String.format("now fetch size is %d. This is more than %d. So, fetch operation stopped.", rowDataList.size(), limit));
                        }
                    }
                }
                client.advance();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        prestoQueryResult.setLineNumber(lineNumber);
        try {
            long size = Files.size(dst);
            DataSize rawDataSize = new DataSize(size, DataSize.Unit.BYTE);
            prestoQueryResult.setRawDataSize(rawDataSize.convertToMostSuccinctDataSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getResultFilePath(String datasource, String queryId, boolean error) {
        String currentPath = new File(".").getAbsolutePath();
        String yyyymmdd = queryId.substring(0, 8);
        File datasourceDir = new File(String.format("%s/result/%s", currentPath, datasource));
        if (!datasourceDir.isDirectory()) {
            datasourceDir.mkdir();
        }
        File yyyymmddDir = new File(String.format("%s/result/%s/%s", currentPath, datasource, yyyymmdd));
        if (!yyyymmddDir.isDirectory()) {
            yyyymmddDir.mkdir();
        }
        if(error) {
            return Paths.get(String.format("%s/result/%s/%s/%s.err", currentPath, datasource, yyyymmdd, queryId));
        } else {
            return Paths.get(String.format("%s/result/%s/%s/%s.tsv", currentPath, datasource, yyyymmdd, queryId));
        }
    }

    private StatementClient getStatementClient(String datasource, String query, String userName) {
        String prestoCoordinatorServer = yanagishimaConfig
                .getPrestoCoordinatorServer(datasource);
        String catalog = yanagishimaConfig.getCatalog(datasource);
        String schema = yanagishimaConfig.getSchema(datasource);
        String user = null;
        if(userName == null ) {
            user = yanagishimaConfig.getUser();
        } else {
            user = userName;
        }
        String source = yanagishimaConfig.getSource();

        JsonCodec<QueryResults> jsonCodec = jsonCodec(QueryResults.class);

        ClientSession clientSession = new ClientSession(
                URI.create(prestoCoordinatorServer), user, source, null, catalog,
                schema, TimeZone.getDefault().getID(), Locale.getDefault(),
                new HashMap<String, String>(), null, false, new Duration(2, MINUTES));
        return new StatementClient(httpClient, jsonCodec, clientSession, query);
    }

    private QueryErrorException resultsException(QueryResults results) {
        QueryError error = results.getError();
        String message = format("Query failed (#%s): %s", results.getId(), error.getMessage());
        Throwable cause = (error.getFailureInfo() == null) ? null : error.getFailureInfo().toException();
        return new QueryErrorException(results.getId(), error, new SQLException(message, error.getSqlState(), error.getErrorCode(), cause));
    }

}
