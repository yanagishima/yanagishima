package yanagishima.exception;

import java.sql.SQLException;

import com.facebook.presto.client.QueryError;

public class QueryErrorException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private QueryError queryError;
	
	public QueryErrorException(SQLException cause) {
		super(cause);
	}
	
	public QueryErrorException(QueryError queryError, SQLException cause) {
		super(cause);
		this.queryError = queryError;
	}

	public QueryError getQueryError() {
		return queryError;
	}

}
