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
import java.math.BigDecimal;
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
import static yanagishima.util.PathUtil.getResultFilePath;
import static yanagishima.util.TimeoutUtil.checkTimeout;

public class ElasticsearchServiceImpl implements ElasticsearchService {

    private static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchServiceImpl.class);

    private YanagishimaConfig yanagishimaConfig;

    private Fluency fluency;

    @Inject
    private TinyORM db;

    @Inject
    public ElasticsearchServiceImpl(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
        if (yanagishimaConfig.getFluentdExecutedTag().isPresent() || yanagishimaConfig.getFluentdFaliedTag().isPresent()) {
            String fluentdHost = yanagishimaConfig.getFluentdHost().orElse("localhost");
            int fluentdPort = Integer.parseInt(yanagishimaConfig.getFluentdPort().orElse("24224"));
            try {
                fluency = Fluency.defaultFluency(fluentdHost, fluentdPort);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public ElasticsearchQueryResult doQuery(String datasource, String query, String userName, boolean storeFlag, int limit) throws ElasticsearchQueryErrorException {
        String queryId = QueryIdUtil.generate(datasource, query, "elasticsearch");
        return getElasticsearchQueryResult(queryId, datasource, query, storeFlag, limit, userName);
    }

    @Override
    public ElasticsearchQueryResult doTranslate(String datasource, String query, String userName, boolean storeFlag, int limit) throws ElasticsearchQueryErrorException {
        String queryId = QueryIdUtil.generate(datasource, query, "elasticsearch");
        String jdbcUrl = yanagishimaConfig.getElasticsearchJdbcUrl(datasource);
        String httpUrl = "http://" + jdbcUrl.substring(DRIVER_URL_START.length());
        ElasticsearchTranslateClient translateClient = new ElasticsearchTranslateClient(httpUrl);
        try {
            long start = System.currentTimeMillis();
            String luceneQuery = translateClient.translate(query);
            ElasticsearchQueryResult elasticsearchQueryResult = new ElasticsearchQueryResult();
            elasticsearchQueryResult.setQueryId(queryId);
            List<String> columnNameList = new ArrayList<>();
            columnNameList.add("lucene_query");
            Path dst = getResultFilePath(datasource, queryId, false);
            int lineNumber = 0;
            int maxResultFileByteSize = yanagishimaConfig.getElasticsearchMaxResultFileByteSize();
            int resultBytes = 0;
            try (BufferedWriter bw = Files.newBufferedWriter(dst, StandardCharsets.UTF_8);
                 CSVPrinter csvPrinter = new CSVPrinter(bw, CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").withRecordSeparator(System.getProperty("line.separator")));) {
                csvPrinter.printRecord(columnNameList);
                lineNumber++;
                elasticsearchQueryResult.setColumns(columnNameList);
                List<List<String>> rowDataList = new ArrayList<>();
                List<String> columnDataList = new ArrayList<>();
                columnDataList.add(luceneQuery);
                csvPrinter.printRecord(columnDataList);
                lineNumber++;
                resultBytes += columnDataList.toString().getBytes(StandardCharsets.UTF_8).length;
                if (resultBytes > maxResultFileByteSize) {
                    String message = String.format("Result file size exceeded %s bytes. queryId=%s, datasource=%s", maxResultFileByteSize, queryId, datasource);
                    storeError(db, datasource, "elasticsearch", queryId, query, userName, message);
                    throw new RuntimeException(message);
                }
                rowDataList.add(columnDataList);
                elasticsearchQueryResult.setLineNumber(lineNumber);
                elasticsearchQueryResult.setRecords(rowDataList);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                long size = Files.size(dst);
                DataSize rawDataSize = new DataSize(size, DataSize.Unit.BYTE);
                elasticsearchQueryResult.setRawDataSize(rawDataSize.convertToMostSuccinctDataSize());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (storeFlag) {
                insertQueryHistory(db, datasource, "elasticsearch", query, userName, queryId, elasticsearchQueryResult.getLineNumber());
            }
            if (yanagishimaConfig.getFluentdExecutedTag().isPresent()) {
                try {
                    long end = System.currentTimeMillis();
                    String tag = yanagishimaConfig.getFluentdExecutedTag().get();
                    Map<String, Object> event = new HashMap<>();
                    event.put("elapsed_time_millseconds", end - start);
                    event.put("user", userName);
                    event.put("query", query);
                    event.put("query_id", queryId);
                    event.put("datasource", datasource);
                    event.put("engine", "elasticsearch");
                    fluency.emit(tag, event);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            return elasticsearchQueryResult;
        } catch (SQLException e) {
            storeError(db, datasource, "elasticsearch", queryId, query, userName, e.getMessage());
            throw new ElasticsearchQueryErrorException(queryId, e);
        }
    }

    private ElasticsearchQueryResult getElasticsearchQueryResult(String queryId, String datasource, String query, boolean storeFlag, int limit, String userName) throws ElasticsearchQueryErrorException {

        List<String> elasticsearchDisallowedKeywords = yanagishimaConfig.getElasticsearchDisallowedKeywords(datasource);
        for (String elasticsearchDisallowedKeyword : elasticsearchDisallowedKeywords) {
            if (query.trim().toLowerCase().startsWith(elasticsearchDisallowedKeyword)) {
                String message = String.format("query contains %s. This is the disallowed keywords in %s", elasticsearchDisallowedKeyword, datasource);
                storeError(db, datasource, "elasticsearch", queryId, query, userName, message);
                throw new RuntimeException(message);
            }
        }

        List<String> elasticsearchSecretKeywords = yanagishimaConfig.getElasticsearchSecretKeywords(datasource);
        for (String elasticsearchSecretKeyword : elasticsearchSecretKeywords) {
            if (query.indexOf(elasticsearchSecretKeyword) != -1) {
                String message = "query error occurs";
                storeError(db, datasource, "elasticsearch", queryId, query, userName, message);
                throw new RuntimeException(message);
            }
        }

        List<String> elasticsearchMustSpectifyConditions = yanagishimaConfig.getElasticsearchMustSpectifyConditions(datasource);
        for (String elasticsearchMustSpectifyCondition : elasticsearchMustSpectifyConditions) {
            String[] conditions = elasticsearchMustSpectifyCondition.split(",");
            for (String condition : conditions) {
                String table = condition.split(":")[0];
                if (!query.startsWith("SHOW") && !query.startsWith("DESCRIBE") && query.indexOf(table) != -1) {
                    String[] partitionKeys = condition.split(":")[1].split("\\|");
                    for (String partitionKey : partitionKeys) {
                        if (query.indexOf(partitionKey) == -1) {
                            String message = String.format("If you query %s, you must specify %s in where clause", table, partitionKey);
                            storeError(db, datasource, "elasticsearch", queryId, query, userName, message);
                            throw new RuntimeException(message);
                        }
                    }
                }
            }
        }

        String url = yanagishimaConfig.getElasticsearchJdbcUrl(datasource);

        try (Connection connection = DriverManager.getConnection(url)) {
            long start = System.currentTimeMillis();
            ElasticsearchQueryResult elasticsearchQueryResult = new ElasticsearchQueryResult();
            elasticsearchQueryResult.setQueryId(queryId);
            processData(datasource, query, limit, userName, connection, queryId, start, elasticsearchQueryResult);
            if (storeFlag) {
                insertQueryHistory(db, datasource, "elasticsearch", query, userName, queryId, elasticsearchQueryResult.getLineNumber());
            }
            if (yanagishimaConfig.getFluentdExecutedTag().isPresent()) {
                try {
                    long end = System.currentTimeMillis();
                    String tag = yanagishimaConfig.getFluentdExecutedTag().get();
                    Map<String, Object> event = new HashMap<>();
                    event.put("elapsed_time_millseconds", end - start);
                    event.put("user", userName);
                    event.put("query", query);
                    event.put("query_id", queryId);
                    event.put("datasource", datasource);
                    event.put("engine", "elasticsearch");
                    fluency.emit(tag, event);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            return elasticsearchQueryResult;

        } catch (SQLException e) {
            storeError(db, datasource, "elasticsearch", queryId, query, userName, e.getMessage());
            throw new ElasticsearchQueryErrorException(queryId, e);
        }
    }

    private void processData(String datasource, String query, int limit, String userName, Connection connection, String queryId, long start, ElasticsearchQueryResult elasticsearchQueryResult) throws SQLException {
        Duration queryMaxRunTime = new Duration(this.yanagishimaConfig.getElasticsearchQueryMaxRunTimeSeconds(datasource), TimeUnit.SECONDS);
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();
                List<String> columnNameList = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    columnNameList.add(resultSetMetaData.getColumnName(i));
                }

                Path dst = getResultFilePath(datasource, queryId, false);
                int lineNumber = 0;
                int maxResultFileByteSize = yanagishimaConfig.getElasticsearchMaxResultFileByteSize();
                int resultBytes = 0;
                try (BufferedWriter bw = Files.newBufferedWriter(dst, StandardCharsets.UTF_8);
                     CSVPrinter csvPrinter = new CSVPrinter(bw, CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").withRecordSeparator(System.getProperty("line.separator")));) {
                    csvPrinter.printRecord(columnNameList);
                    lineNumber++;
                    elasticsearchQueryResult.setColumns(columnNameList);
                    List<List<String>> rowDataList = new ArrayList<>();
                    while (resultSet.next()) {
                        List<String> columnDataList = new ArrayList<>();
                        for (int i = 1; i <= columnCount; i++) {
                            Object resultObject = resultSet.getObject(i);
                            if (resultObject instanceof Long) {
                                columnDataList.add(((Long) resultObject).toString());
                            } else if (resultObject instanceof Double) {
                                if (Double.isNaN((Double) resultObject) || Double.isInfinite((Double) resultObject)) {
                                    columnDataList.add(resultObject.toString());
                                } else {
                                    columnDataList.add(BigDecimal.valueOf((Double) resultObject).toPlainString());
                                }
                            } else {
                                if (resultObject == null) {
                                    columnDataList.add(null);
                                } else {
                                    columnDataList.add(resultObject.toString());
                                }
                            }
                        }

                        try {
                            csvPrinter.printRecord(columnDataList);
                            lineNumber++;
                            resultBytes += columnDataList.toString().getBytes(StandardCharsets.UTF_8).length;
                            if (resultBytes > maxResultFileByteSize) {
                                String message = String.format("Result file size exceeded %s bytes. queryId=%s, datasource=%s", maxResultFileByteSize, queryId, datasource);
                                storeError(db, datasource, "elasticsearch", queryId, query, userName, message);
                                throw new RuntimeException(message);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (query.toLowerCase().startsWith("show") || rowDataList.size() < limit) {
                            rowDataList.add(columnDataList);
                        } else {
                            elasticsearchQueryResult.setWarningMessage(String.format("now fetch size is %d. This is more than %d. So, fetch operation stopped.", rowDataList.size(), limit));
                        }

                        checkTimeout(db, queryMaxRunTime, start, datasource, "elasticsearch", queryId, query, userName);
                    }
                    elasticsearchQueryResult.setLineNumber(lineNumber);
                    elasticsearchQueryResult.setRecords(rowDataList);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try {
                    long size = Files.size(dst);
                    DataSize rawDataSize = new DataSize(size, DataSize.Unit.BYTE);
                    elasticsearchQueryResult.setRawDataSize(rawDataSize.convertToMostSuccinctDataSize());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }

}
