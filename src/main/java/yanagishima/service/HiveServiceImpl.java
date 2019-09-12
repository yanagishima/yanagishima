package yanagishima.service;

import io.airlift.units.DataSize;
import io.airlift.units.Duration;
import me.geso.tinyorm.TinyORM;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.komamitsu.fluency.Fluency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.HiveQueryErrorException;
import yanagishima.pool.StatementPool;
import yanagishima.result.HiveQueryResult;
import yanagishima.util.QueryIdUtil;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static yanagishima.util.Constants.YANAGISHIAM_HIVE_JOB_PREFIX;
import static yanagishima.util.DbUtil.insertQueryHistory;
import static yanagishima.util.DbUtil.storeError;
import static yanagishima.util.FluentdUtil.buildStaticFluency;
import static yanagishima.util.PathUtil.getResultFilePath;
import static yanagishima.util.QueryEngine.hive;
import static yanagishima.util.QueryEngine.spark;
import static yanagishima.util.TimeoutUtil.checkTimeout;

public class HiveServiceImpl implements HiveService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HiveServiceImpl.class);

    private final YanagishimaConfig yanagishimaConfig;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final TinyORM db;
    private final Fluency fluency;
    private final StatementPool statementPool;

    @Inject
    public HiveServiceImpl(YanagishimaConfig yanagishimaConfig, TinyORM db, StatementPool statementPool) {
        this.yanagishimaConfig = yanagishimaConfig;
        this.db = db;
        this.fluency = buildStaticFluency(yanagishimaConfig);
        this.statementPool = statementPool;
    }

    @Override
    public String doQueryAsync(String engine, String datasource, String query, String userName, Optional<String> hiveUser, Optional<String> hivePassword) {
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

        public Task(String queryId, String engine, String datasource, String query, String userName, Optional<String> hiveUser, Optional<String> hivePassword) {
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
                int limit = yanagishimaConfig.getSelectLimit();
                getHiveQueryResult(queryId, engine, datasource, query, true, limit, userName, hiveUser, hivePassword, true);
            } catch (HiveQueryErrorException e) {
                LOGGER.warn(e.getCause().getMessage());
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public HiveQueryResult doQuery(String engine, String datasource, String query, String userName, Optional<String> hiveUser, Optional<String> hivePassword, boolean storeFlag, int limit) throws HiveQueryErrorException {
        String queryId = QueryIdUtil.generate(datasource, query, engine);
        return getHiveQueryResult(queryId, engine, datasource, query, storeFlag, limit, userName, hiveUser, hivePassword, false);
    }

    private HiveQueryResult getHiveQueryResult(String queryId, String engine, String datasource, String query, boolean storeFlag, int limit, String userName, Optional<String> hiveUser, Optional<String> hivePassword, boolean async) throws HiveQueryErrorException {

        List<String> hiveDisallowedKeywords = yanagishimaConfig.getHiveDisallowedKeywords(datasource);
        for (String hiveDisallowedKeyword : hiveDisallowedKeywords) {
            if (query.trim().toLowerCase().startsWith(hiveDisallowedKeyword)) {
                String message = String.format("query contains %s. This is the disallowed keywords in %s", hiveDisallowedKeyword, datasource);
                storeError(db, datasource, engine, queryId, query, userName, message);
                throw new RuntimeException(message);
            }
        }

        List<String> hiveSecretKeywords = yanagishimaConfig.getHiveSecretKeywords(datasource);
        for (String hiveSecretKeyword : hiveSecretKeywords) {
            if (query.indexOf(hiveSecretKeyword) != -1) {
                String message = "query error occurs";
                storeError(db, datasource, engine, queryId, query, userName, message);
                throw new RuntimeException(message);
            }
        }

        List<String> hiveMustSpecifyConditions = yanagishimaConfig.getHiveMustSpecifyConditions(datasource);
        for (String hiveMustSpecifyCondition : hiveMustSpecifyConditions) {
            String[] conditions = hiveMustSpecifyCondition.split(",");
            for (String condition : conditions) {
                String table = condition.split(":")[0];
                if (!query.startsWith("SHOW") && !query.startsWith("DESCRIBE") && query.indexOf(table) != -1) {
                    String[] partitionKeys = condition.split(":")[1].split("\\|");
                    for (String partitionKey : partitionKeys) {
                        if (query.indexOf(partitionKey) == -1) {
                            String message = String.format("If you query %s, you must specify %s in where clause", table, partitionKey);
                            storeError(db, datasource, engine, queryId, query, userName, message);
                            throw new RuntimeException(message);
                        }
                    }
                }
            }
        }

        try {
            Class.forName("org.apache.hive.jdbc.HiveDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        String url = null;
        if(engine.equals(hive.name())) {
            url = yanagishimaConfig.getHiveJdbcUrl(datasource);
            if(yanagishimaConfig.isHiveImpersonation(datasource)) {
                url += ";hive.server2.proxy.user=" + userName;
            }
        } else if(engine.equals(spark.name())) {
            url = yanagishimaConfig.getSparkJdbcUrl(datasource);
        } else {
            throw new IllegalArgumentException(engine + " is illegal");
        }
        String user = yanagishimaConfig.getHiveJdbcUser(datasource);
        String password = yanagishimaConfig.getHiveJdbcPassword(datasource);

        if (hiveUser.isPresent() && hivePassword.isPresent()) {
            user = hiveUser.get();
            password = hivePassword.get();
        }

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            long start = System.currentTimeMillis();
            HiveQueryResult hiveQueryResult = new HiveQueryResult();
            hiveQueryResult.setQueryId(queryId);
            processData(engine, datasource, query, limit, userName, connection, queryId, start, hiveQueryResult, async);
            if (storeFlag) {
                insertQueryHistory(db, datasource, engine, query, userName, queryId, hiveQueryResult.getLineNumber());
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
                    event.put("engine", engine);
                    fluency.emit(tag, event);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            return hiveQueryResult;

        } catch (SQLException e) {
            storeError(db, datasource, engine, queryId, query, userName, e.getMessage());
            throw new HiveQueryErrorException(queryId, e);
        }
    }

    private void processData(String engine, String datasource, String query, int limit, String userName, Connection connection, String queryId, long start, HiveQueryResult hiveQueryResult, boolean async) throws SQLException {
        Duration queryMaxRunTime = new Duration(yanagishimaConfig.getHiveQueryMaxRunTimeSeconds(datasource), TimeUnit.SECONDS);
        try (Statement statement = connection.createStatement()) {
            int timeout = (int) queryMaxRunTime.toMillis() / 1000;
            statement.setQueryTimeout(timeout);
            if(engine.equals(hive.name())) {
                String jobName = null;
                if (userName == null) {
                    jobName = YANAGISHIAM_HIVE_JOB_PREFIX + queryId;
                } else {
                    jobName = YANAGISHIAM_HIVE_JOB_PREFIX + userName + "-" + queryId;
                }
                statement.execute("set mapreduce.job.name=" + jobName);
                List<String> hiveSetupQueryList = yanagishimaConfig.getHiveSetupQueryList(datasource);
                for (String hiveSetupQuery : hiveSetupQueryList) {
                    statement.execute(hiveSetupQuery);
                }
            }

            if (async && yanagishimaConfig.isUseJdbcCancel(datasource)) {
                statementPool.putStatement(datasource, queryId, statement);
            }

            if(query.trim().toLowerCase().startsWith("create") || query.trim().toLowerCase().startsWith("drop")) {
                try {
                    statement.execute(query);
                    Path dst = getResultFilePath(datasource, queryId, false);
                    dst.toFile().createNewFile();
                    hiveQueryResult.setLineNumber(0);
                    hiveQueryResult.setRawDataSize(new DataSize(0, DataSize.Unit.BYTE));
                    hiveQueryResult.setRecords(new ArrayList<>());
                    hiveQueryResult.setColumns(new ArrayList<>());
                    return;
                } catch (SQLException | IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try (ResultSet resultSet = statement.executeQuery(query)) {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                int columnCount = resultSetMetaData.getColumnCount();
                List<String> columnNameList = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    columnNameList.add(resultSetMetaData.getColumnName(i));
                }

                Path dst = getResultFilePath(datasource, queryId, false);
                int lineNumber = 0;
                int maxResultFileByteSize = yanagishimaConfig.getHiveMaxResultFileByteSize();
                int resultBytes = 0;
                try (BufferedWriter bw = Files.newBufferedWriter(dst, StandardCharsets.UTF_8);
                     CSVPrinter csvPrinter = new CSVPrinter(bw, CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").withRecordSeparator(System.getProperty("line.separator")));) {
                    csvPrinter.printRecord(columnNameList);
                    lineNumber++;
                    hiveQueryResult.setColumns(columnNameList);
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
                                storeError(db, datasource, engine, queryId, query, userName, message);
                                throw new RuntimeException(message);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (query.toLowerCase().startsWith("show") || rowDataList.size() < limit) {
                            rowDataList.add(columnDataList);
                        } else {
                            hiveQueryResult.setWarningMessage(String.format("now fetch size is %d. This is more than %d. So, fetch operation stopped.", rowDataList.size(), limit));
                        }

                        checkTimeout(db, queryMaxRunTime, start, datasource, engine, queryId, query, userName);
                    }
                    hiveQueryResult.setLineNumber(lineNumber);
                    hiveQueryResult.setRecords(rowDataList);
                    if (async && yanagishimaConfig.isUseJdbcCancel(datasource)) {
                        statementPool.removeStatement(datasource, queryId);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try {
                    long size = Files.size(dst);
                    DataSize rawDataSize = new DataSize(size, DataSize.Unit.BYTE);
                    hiveQueryResult.setRawDataSize(rawDataSize.convertToMostSuccinctDataSize());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
