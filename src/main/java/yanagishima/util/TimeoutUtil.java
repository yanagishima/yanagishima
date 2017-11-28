package yanagishima.util;

import io.airlift.units.Duration;
import me.geso.tinyorm.TinyORM;

import static java.lang.String.format;
import static yanagishima.util.DbUtil.storeError;

public class TimeoutUtil {

    public static void checkTimeout(TinyORM db, Duration queryMaxRunTime, long start, String datasource, String engine, String queryId, String query, String user) {
        if (System.currentTimeMillis() - start > queryMaxRunTime.toMillis()) {
            String message = format("Query failed (#%s) in %s: Query exceeded maximum time limit of %s", queryId, datasource, queryMaxRunTime.toString());
            storeError(db, datasource, engine, queryId, query, user, message);
            throw new RuntimeException(message);
        }
    }
}
