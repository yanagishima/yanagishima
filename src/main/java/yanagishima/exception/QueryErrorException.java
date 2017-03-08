package yanagishima.exception;

import com.facebook.presto.client.QueryError;

import java.sql.SQLException;

public class QueryErrorException extends Exception {

	private static final long serialVersionUID = 1L;

	private String queryId;
	
	private QueryError queryError;
	
	public QueryErrorException(SQLException cause) {
		super(cause);
	}
	
	public QueryErrorException(String queryId, QueryError queryError, SQLException cause) {
		super(cause);
		this.queryId = queryId;
		this.queryError = queryError;
	}

	public String getQueryId() { return queryId; }

	public QueryError getQueryError() {
		return queryError;
	}

}
