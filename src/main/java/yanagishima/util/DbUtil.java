package yanagishima.util;

import me.geso.tinyorm.TinyORM;
import yanagishima.row.Query;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;

import static java.lang.String.format;
import static yanagishima.util.PathUtil.getResultFilePath;

public class DbUtil {

    public static void storeError(TinyORM db, String datasource, String engine, String queryId, String query, String errorMessage) {
        db.insert(Query.class)
                .value("datasource", datasource)
                .value("engine", engine)
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

    public static void insertQueryHistory(TinyORM db, String datasource, String engine, String query, String queryId) {
        db.insert(Query.class)
                .value("datasource", datasource)
                .value("engine", engine)
                .value("query_id", queryId)
                .value("fetch_result_time_string", ZonedDateTime.now().toString())
                .value("query_string", query)
                .execute();
    }
}
