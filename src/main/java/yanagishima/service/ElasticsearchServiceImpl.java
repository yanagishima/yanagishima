package yanagishima.service;

import com.github.wyukawa.elasticsearch.unofficial.jdbc.driver.ElasticsearchTranslateClient;
import io.airlift.units.DataSize;
import io.airlift.units.Duration;
import me.geso.tinyorm.TinyORM;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.komamitsu.fluency.Fluency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.ElasticsearchQueryErrorException;
import yanagishima.result.ElasticsearchQueryResult;
import yanagishima.util.QueryIdUtil;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.wyukawa.elasticsearch.unofficial.jdbc.driver.ElasticsearchDriver.DRIVER_URL_START;
import static yanagishima.util.DbUtil.insertQueryHistory;
import static yanagishima.util.DbUtil.storeError;
import static yanagishima.util.FluentdUtil.buildStaticFluency;
import static yanagishima.util.PathUtil.getResultFilePath;
import static yanagishima.util.TimeoutUtil.checkTimeout;
import static yanagishima.util.QueryEngine.elasticsearch;
import static yanagishima.util.TypeCoerceUtil.objectToString;

public class ElasticsearchServiceImpl implements ElasticsearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchServiceImpl.class);
    private static final CSVFormat CSV_FORMAT = CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").withRecordSeparator(System.getProperty("line.separator"));

    private final YanagishimaConfig config;
    private final TinyORM db;
    private final Fluency fluency;

    @Inject
    public ElasticsearchServiceImpl(YanagishimaConfig config, TinyORM db) {
        this.config = config;
        this.db = db;
        this.fluency = buildStaticFluency(config);
    }

    @Override
    public ElasticsearchQueryResult doQuery(String datasource, String query, String userName, boolean storeFlag, int limit) throws ElasticsearchQueryErrorException {
        String queryId = QueryIdUtil.generate(datasource, query, elasticsearch.name());
        return getElasticsearchQueryResult(queryId, datasource, query, storeFlag, limit, userName);
    }

    @Override
    public ElasticsearchQueryResult doTranslate(String datasource, String query, String userName, boolean storeFlag, int limit) throws ElasticsearchQueryErrorException {
        String queryId = QueryIdUtil.generate(datasource, query, elasticsearch.name());
        String jdbcUrl = config.getElasticsearchJdbcUrl(datasource);
        String httpUrl = "http://" + jdbcUrl.substring(DRIVER_URL_START.length());
        ElasticsearchTranslateClient translateClient = new ElasticsearchTranslateClient(httpUrl);
        try {
            long start = System.currentTimeMillis();
            String luceneQuery = translateClient.translate(query);
            ElasticsearchQueryResult result = new ElasticsearchQueryResult();
            result.setQueryId(queryId);
            List<String> columnNameList = new ArrayList<>();
            columnNameList.add("lucene_query");
            Path dst = getResultFilePath(datasource, queryId, false);
            int lineNumber = 0;
            int maxResultFileByteSize = config.getElasticsearchMaxResultFileByteSize();
            int resultBytes = 0;
            try (BufferedWriter writer = Files.newBufferedWriter(dst, StandardCharsets.UTF_8);
                 CSVPrinter printer = new CSVPrinter(writer, CSV_FORMAT)) {
                printer.printRecord(columnNameList);
                lineNumber++;
                result.setColumns(columnNameList);
                List<List<String>> rowDataList = new ArrayList<>();
                List<String> columnDataList = new ArrayList<>();
                columnDataList.add(luceneQuery);
                printer.printRecord(columnDataList);
                lineNumber++;
                resultBytes += columnDataList.toString().getBytes(StandardCharsets.UTF_8).length;
                if (resultBytes > maxResultFileByteSize) {
                    String message = String.format("Result file size exceeded %s bytes. queryId=%s, datasource=%s", maxResultFileByteSize, queryId, datasource);
                    storeError(db, datasource, "elasticsearch", queryId, query, userName, message);
                    throw new RuntimeException(message);
                }
                rowDataList.add(columnDataList);
                result.setLineNumber(lineNumber);
                result.setRecords(rowDataList);

                long size = Files.size(dst);
                DataSize rawDataSize = new DataSize(size, DataSize.Unit.BYTE);
                result.setRawDataSize(rawDataSize.convertToMostSuccinctDataSize());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (storeFlag) {
                insertQueryHistory(db, datasource, elasticsearch.name(), query, userName, queryId, result.getLineNumber());
            }
            emitExecutedEvent(userName, query, queryId, datasource, System.currentTimeMillis() - start);
            return result;
        } catch (SQLException e) {
            storeError(db, datasource, elasticsearch.name(), queryId, query, userName, e.getMessage());
            throw new ElasticsearchQueryErrorException(queryId, e);
        }
    }

    private ElasticsearchQueryResult getElasticsearchQueryResult(String queryId, String datasource, String query, boolean storeFlag, int limit, String userName) throws ElasticsearchQueryErrorException {
        checkDisallowedKeyword(query, datasource, queryId, userName);
        checkSecretKeyword(query, datasource, queryId, userName);
        checkRequiredCondition(query, datasource, queryId, userName);

        String url = config.getElasticsearchJdbcUrl(datasource);

        try (Connection connection = DriverManager.getConnection(url)) {
            long start = System.currentTimeMillis();
            ElasticsearchQueryResult result = new ElasticsearchQueryResult();
            result.setQueryId(queryId);
            processData(datasource, query, limit, userName, connection, queryId, start, result);
            if (storeFlag) {
                insertQueryHistory(db, datasource, "elasticsearch", query, userName, queryId, result.getLineNumber());
            }
            emitExecutedEvent(userName, query, queryId, datasource, System.currentTimeMillis() - start);
            return result;

        } catch (SQLException e) {
            storeError(db, datasource, elasticsearch.name(), queryId, query, userName, e.getMessage());
            throw new ElasticsearchQueryErrorException(queryId, e);
        }
    }

    private void processData(String datasource, String query, int limit, String userName, Connection connection, String queryId, long start, ElasticsearchQueryResult result) throws SQLException {
        Duration queryMaxRunTime = new Duration(config.getElasticsearchQueryMaxRunTimeSeconds(datasource), TimeUnit.SECONDS);
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData metadata = resultSet.getMetaData();
                int columnCount = metadata.getColumnCount();
                List<String> columnNameList = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    columnNameList.add(metadata.getColumnName(i));
                }

                Path dst = getResultFilePath(datasource, queryId, false);
                int lineNumber = 0;
                int maxResultFileByteSize = config.getElasticsearchMaxResultFileByteSize();
                int resultBytes = 0;
                try (BufferedWriter writer = Files.newBufferedWriter(dst, StandardCharsets.UTF_8);
                     CSVPrinter printer = new CSVPrinter(writer, CSV_FORMAT)) {
                    printer.printRecord(columnNameList);
                    lineNumber++;
                    result.setColumns(columnNameList);
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
                            String message = String.format("Result file size exceeded %s bytes. queryId=%s, datasource=%s", maxResultFileByteSize, queryId, datasource);
                            storeError(db, datasource, elasticsearch.name(), queryId, query, userName, message);
                            throw new RuntimeException(message);
                        }

                        if (query.toLowerCase().startsWith("show") || rows.size() < limit) {
                            rows.add(row);
                        } else {
                            result.setWarningMessage(String.format("now fetch size is %d. This is more than %d. So, fetch operation stopped.", rows.size(), limit));
                        }

                        checkTimeout(db, queryMaxRunTime, start, datasource, elasticsearch.name(), queryId, query, userName);
                    }
                    result.setLineNumber(lineNumber);
                    result.setRecords(rows);

                    long size = Files.size(dst);
                    DataSize rawDataSize = new DataSize(size, DataSize.Unit.BYTE);
                    result.setRawDataSize(rawDataSize.convertToMostSuccinctDataSize());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void checkDisallowedKeyword(String query, String datasource, String queryId, String username) {
        for (String keyword : config.getElasticsearchDisallowedKeywords(datasource)) {
            if (query.trim().toLowerCase().startsWith(keyword)) {
                String message = String.format("query contains %s. This is the disallowed keywords in %s", keyword, datasource);
                storeError(db, datasource, elasticsearch.name(), queryId, query, username, message);
                throw new RuntimeException(message);
            }
        }
    }

    private void checkSecretKeyword(String query, String datasource, String queryId, String username) {
        for (String keyword : config.getElasticsearchSecretKeywords(datasource)) {
            if (query.contains(keyword)) {
                String message = "query error occurs";
                storeError(db, datasource, elasticsearch.name(), queryId, query, username, message);
                throw new RuntimeException(message);
            }
        }
    }

    private void checkRequiredCondition(String query, String datasource, String queryId, String username) {
        List<String> requiredConditions = config.getElasticsearchMustSpecifyConditions(datasource);
        for (String requiredCondition : requiredConditions) {
            String[] conditions = requiredCondition.split(",");
            for (String condition : conditions) {
                String table = condition.split(":")[0];
                if (query.contains(table)) {
                    String[] partitionKeys = condition.split(":")[1].split("\\|");
                    for (String partitionKey : partitionKeys) {
                        if (!query.contains(partitionKey)) {
                            String message = String.format("If you query %s, you must specify %s in where clause", table, partitionKey);
                            storeError(db, datasource, elasticsearch.name(), queryId, query, username, message);
                            throw new RuntimeException(message);
                        }
                    }
                }
            }
        }
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
        event.put("engine", "elasticsearch");

        try {
            fluency.emit(config.getFluentdExecutedTag().get(), event);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
