package yanagishima.util;

import me.geso.tinyorm.TinyORM;
import yanagishima.row.Query;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static yanagishima.util.PathUtil.getResultFilePath;

public class DbUtil {

    public static void storeError(TinyORM db, String datasource, String engine, String queryId, String query, String user, String errorMessage) {
        try {
            LocalDateTime submitTimeLdt = LocalDateTime.parse(queryId.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
            String fetchResultTimeString = ZonedDateTime.now().toString();
            ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
            long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);

            Path dst = getResultFilePath(datasource, queryId, true);
            try (BufferedWriter bw = Files.newBufferedWriter(dst, StandardCharsets.UTF_8)) {
                bw.write(errorMessage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            long resultFileSize = Files.size(dst);
            db.insert(Query.class)
                    .value("datasource", datasource)
                    .value("engine", engine)
                    .value("query_id", queryId)
                    .value("fetch_result_time_string", fetchResultTimeString)
                    .value("query_string", query)
                    .value("user", user)
                    .value("status", Status.FAILED.name())
                    .value("elapsed_time_millis", elapsedTimeMillis)
                    .value("result_file_size", resultFileSize)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertQueryHistory(TinyORM db, String datasource, String engine, String query, String user, String queryId, int linenumber) {
        try {
            LocalDateTime submitTimeLdt = LocalDateTime.parse(queryId.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
            String fetchResultTimeString = ZonedDateTime.now().toString();
            ZonedDateTime fetchResultTime = ZonedDateTime.parse(fetchResultTimeString);
            long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, fetchResultTime);

            Path resultFilePath = PathUtil.getResultFilePath(datasource, queryId, false);
            long resultFileSize = Files.size(resultFilePath);
            db.insert(Query.class)
                    .value("datasource", datasource)
                    .value("engine", engine)
                    .value("query_id", queryId)
                    .value("fetch_result_time_string", fetchResultTimeString)
                    .value("query_string", query)
                    .value("user", user)
                    .value("status", Status.SUCCEED.name())
                    .value("elapsed_time_millis", elapsedTimeMillis)
                    .value("result_file_size", resultFileSize)
                    .value("linenumber", linenumber)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
