package yanagishima.util;

import io.airlift.units.Duration;
import me.geso.tinyorm.TinyORM;

import static yanagishima.util.DbUtil.storeError;

public class TimeoutUtil {

    public static void checkTimeout(TinyORM db, Duration queryMaxRunTime, long start, String datasource, String engine, String queryId, String query) {
        if (System.currentTimeMillis() - start > queryMaxRunTime.toMillis()) {
            String message = "Query exceeded maximum time limit of " + queryMaxRunTime;
            storeError(db, datasource, engine, queryId, query, message);
            throw new RuntimeException(message);
        }
    }
}
