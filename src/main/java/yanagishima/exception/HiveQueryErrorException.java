package yanagishima.exception;

import java.sql.SQLException;

public class HiveQueryErrorException extends Exception {
  private static final long serialVersionUID = 1L;

  private final String queryId;

  public HiveQueryErrorException(String queryId, SQLException cause) {
    super(cause);
    this.queryId = queryId;
  }

  public String getQueryId() {
    return queryId;
  }
}
