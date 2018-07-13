package yanagishima.exception;

import java.sql.SQLException;

public class ElasticsearchQueryErrorException extends Exception {

    private static final long serialVersionUID = 1L;

    private String queryId;

    public ElasticsearchQueryErrorException(SQLException cause) {
        super(cause);
    }

    public ElasticsearchQueryErrorException(String queryId, SQLException cause) {
        super(cause);
        this.queryId = queryId;
    }

    public String getQueryId() {
        return queryId;
    }

}
