package yanagishima.exception;

import io.prestosql.client.QueryError;

import java.sql.SQLException;

public class QueryErrorException extends Exception {

	private static final long serialVersionUID = 1L;

	private String queryId;
	
	public QueryErrorException(SQLException cause) {
		super(cause);
	}
	
	public QueryErrorException(String queryId, SQLException cause) {
		super(cause);
		this.queryId = queryId;
	}

	public String getQueryId() { return queryId; }

}
