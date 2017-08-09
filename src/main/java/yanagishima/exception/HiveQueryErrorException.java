package yanagishima.exception;

import com.facebook.presto.client.QueryError;

import java.sql.SQLException;

public class HiveQueryErrorException extends Exception {

    private static final long serialVersionUID = 1L;

    private String queryId;

    public HiveQueryErrorException(SQLException cause) {
        super(cause);
    }

    public HiveQueryErrorException(String queryId, SQLException cause) {
        super(cause);
        this.queryId = queryId;
    }

    public String getQueryId() {
        return queryId;
    }

}
