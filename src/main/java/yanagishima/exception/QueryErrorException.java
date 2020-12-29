package yanagishima.exception;

import java.sql.SQLException;

public class QueryErrorException extends Exception {
  private static final long serialVersionUID = 1L;

  private final String queryId;

  public QueryErrorException(SQLException cause) {
    this(null, cause);
  }

  public QueryErrorException(String queryId, SQLException cause) {
    super(cause);
    this.queryId = queryId;
  }

  public String getQueryId() {
    return queryId;
  }
}
