package yanagishima.migration;

import me.geso.tinyorm.TinyORM;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import yanagishima.row.Query;
import yanagishima.row.QueryV14;
import yanagishima.util.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MigrateV14 {
    public static void main(String[] args) throws Throwable {
        if (args.length != 2) {
            System.out.println("please specify db file path and result file directory");
            System.exit(1);
        }

        String dbFile = args[0];
        String resultDirectory = args[1];

        if (!Paths.get(dbFile).toFile().exists()) {
            System.out.println("db file doesn't exist");
            System.exit(1);
        }

        if (!Paths.get(resultDirectory).toFile().exists()) {
            System.out.println("result file directory doesn't exist");
            System.exit(1);
        }

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
        TinyORM db = new TinyORM(connection);
        try (Statement statement = db.getConnection().createStatement()) {
            statement.executeUpdate(""
                                    + "CREATE TABLE IF NOT EXISTS query_v14 ("
                                    + "datasource TEXT, "
                                    + "engine TEXT, "
                                    + "query_id TEXT, "
                                    + "fetch_result_time_string TEXT, "
                                    + "query_string TEXT, "
                                    + "user TEXT, "
                                    + "status TEXT, "
                                    + "elapsed_time_millis INTEGER, "
                                    + "result_file_size INTEGER, "
                                    + "linenumber INTEGER, "
                                    + "PRIMARY KEY (datasource, engine, query_id))");
        }
        List<Query> queryList = db.search(Query.class).execute();
        for (Query query : queryList) {
            String queryId = query.getQueryId();
            String fetchResultTimeString = query.getFetchResultTimeString();
            long elapsedTimeMillis = getElapsedTime(queryId, fetchResultTimeString);

            String yyyymmdd = queryId.substring(0, 8);
            Path resultFilePath = Paths.get(String.format("%s/%s/%s/%s.tsv", resultDirectory, query.getDatasource(), yyyymmdd, queryId));
            if (resultFilePath.toFile().exists()) {
                db.insert(QueryV14.class)
                        .value("datasource", query.getDatasource())
                        .value("engine", query.getEngine())
                        .value("query_id", queryId)
                        .value("fetch_result_time_string", fetchResultTimeString)
                        .value("query_string", query.getQueryString())
                        .value("user", query.getUser())
                        .value("status", Status.SUCCEED.name())
                        .value("elapsed_time_millis", elapsedTimeMillis)
                        .value("result_file_size", Files.size(resultFilePath))
                        .value("linenumber", getRowNumbers(resultFilePath))
                        .execute();
            } else {
                Path errorFilePath = Paths.get(String.format("%s/%s/%s/%s.err", resultDirectory, query.getDatasource(), yyyymmdd, queryId));
                if (errorFilePath.toFile().exists()) {
                    db.insert(QueryV14.class)
                            .value("datasource", query.getDatasource())
                            .value("engine", query.getEngine())
                            .value("query_id", queryId)
                            .value("fetch_result_time_string", fetchResultTimeString)
                            .value("query_string", query.getQueryString())
                            .value("user", query.getUser())
                            .value("status", Status.FAILED.name())
                            .value("elapsed_time_millis", elapsedTimeMillis)
                            .value("result_file_size", Files.size(errorFilePath))
                            .execute();
                } else {
                    db.insert(QueryV14.class)
                            .value("datasource", query.getDatasource())
                            .value("engine", query.getEngine())
                            .value("query_id", queryId)
                            .value("fetch_result_time_string", fetchResultTimeString)
                            .value("query_string", query.getQueryString())
                            .value("user", query.getUser())
                            .value("status", Status.SUCCEED.name())
                            .value("elapsed_time_millis", elapsedTimeMillis)
                            .execute();
                }
            }
        }
    }

    private static int getRowNumbers(Path resultFilePath) throws IOException {
        int rowNumber = 0;
        try (BufferedReader reader = Files.newBufferedReader(resultFilePath, StandardCharsets.UTF_8)) {
            CSVParser parser = CSVFormat.EXCEL.withDelimiter('\t').withNullString("\\N").parse(reader);
            for (CSVRecord record : parser) {
                rowNumber++;
            }
        }
        return rowNumber;
    }

    private static long getElapsedTime(String queryId, String resultTime) {
        LocalDateTime submitTimeLdt = LocalDateTime.parse(queryId.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
        ZonedDateTime fetchResultTime = ZonedDateTime.parse(resultTime);
        return ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);
    }
}
