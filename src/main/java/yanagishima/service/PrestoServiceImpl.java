package yanagishima.service;

import com.facebook.presto.client.*;
import com.google.common.collect.Lists;
import io.airlift.http.client.HttpClientConfig;
import io.airlift.http.client.jetty.JettyHttpClient;
import io.airlift.json.JsonCodec;
import io.airlift.units.Duration;
import io.airlift.units.DataSize;
import me.geso.tinyorm.TinyORM;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.airlift.json.JsonCodec.jsonCodec;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MINUTES;

public class PrestoServiceImpl implements PrestoService {

    private YanagishimaConfig yanagishimaConfig;

    private JettyHttpClient httpClient;

    @Inject
    private TinyORM db;

    @Inject
    public PrestoServiceImpl(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
        HttpClientConfig httpClientConfig = new HttpClientConfig().setConnectTimeout(new Duration(10, TimeUnit.SECONDS));
        this.httpClient = new JettyHttpClient(httpClientConfig);
    }

    @Override
    public PrestoQueryResult doQuery(String query, String userName) throws QueryErrorException {

        try (StatementClient client = getStatementClient(query, userName)) {
            while (client.isValid() && (client.current().getData() == null)) {
                client.advance();
            }

            if ((!client.isFailed()) && (!client.isGone()) && (!client.isClosed())) {
                QueryResults results = client.isValid() ? client.current() : client.finalResults();
                String queryId = results.getId();
                if (results.getUpdateType() != null) {
                    PrestoQueryResult prestoQueryResult = new PrestoQueryResult();
                    prestoQueryResult.setQueryId(queryId);
                    prestoQueryResult.setUpdateType(results.getUpdateType());
                    insertQueryHistory(query, queryId);
                    return prestoQueryResult;
                } else if (results.getColumns() == null) {
                    throw new QueryErrorException(new SQLException(format("Query %s has no columns\n", results.getId())));
                } else {
                    PrestoQueryResult prestoQueryResult = new PrestoQueryResult();
                    prestoQueryResult.setQueryId(queryId);
                    prestoQueryResult.setUpdateType(results.getUpdateType());
                    List<String> columns = Lists.transform(results.getColumns(), Column::getName);
                    prestoQueryResult.setColumns(columns);
                    List<List<String>> rowDataList = new ArrayList<List<String>>();
                    processData(client, queryId, prestoQueryResult, columns, rowDataList);
                    prestoQueryResult.setRecords(rowDataList);
                    insertQueryHistory(query, queryId);
                    return prestoQueryResult;
                }
            }

            if (client.isClosed()) {
                throw new RuntimeException("Query aborted by user");
            } else if (client.isGone()) {
                throw new RuntimeException("Query is gone (server restarted?)");
            } else if (client.isFailed()) {
                QueryResults results = client.finalResults();
                String queryId = results.getId();
                db.insert(Query.class)
                        .value("query_id", queryId)
                        .value("fetch_result_time_string", ZonedDateTime.now().toString())
                        .value("query_string", query)
                        .execute();
                Path dst = getResultFilePath(queryId, true);
                QueryError error = results.getError();
                String message = format("Query failed (#%s): %s", results.getId(), error.getMessage());
                try {
                    try (BufferedWriter bw = Files.newBufferedWriter(dst, StandardCharsets.UTF_8)) {
                        bw.write(message);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                throw resultsException(results);
            }

        }
        throw new RuntimeException("should not reach");

    }

    private void insertQueryHistory(String query, String queryId) {
        if(!query.toLowerCase().startsWith("show") && !query.toLowerCase().startsWith("explain")) {
            db.insert(Query.class)
                    .value("query_id", queryId)
                    .value("fetch_result_time_string", ZonedDateTime.now().toString())
                    .value("query_string", query)
                    .execute();
        }
    }

    private void processData(StatementClient client, String queryId, PrestoQueryResult prestoQueryResult, List<String> columns, List<List<String>> rowDataList) {
        int limit = yanagishimaConfig.getSelectLimit();
        Path dst = getResultFilePath(queryId, false);
        int lineNumber = 0;
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
                            bw.write(String.join("\t", columnDataList));
                            bw.write("\n");
                            lineNumber++;
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

    private Path getResultFilePath(String queryId, boolean error) {
        String currentPath = new File(".").getAbsolutePath();
        String yyyymmdd = queryId.substring(0, 8);
        File yyyymmddDir = new File(String.format("%s/result/%s", currentPath, yyyymmdd));
        if (!yyyymmddDir.isDirectory()) {
            yyyymmddDir.mkdir();
        }
        if(error) {
            return Paths.get(String.format("%s/result/%s/%s.err", currentPath, yyyymmdd, queryId));
        } else {
            return Paths.get(String.format("%s/result/%s/%s.tsv", currentPath, yyyymmdd, queryId));
        }
    }

    private StatementClient getStatementClient(String query, String userName) {
        String prestoCoordinatorServer = yanagishimaConfig
                .getPrestoCoordinatorServer();
        String catalog = yanagishimaConfig.getCatalog();
        String schema = yanagishimaConfig.getSchema();
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
