package yanagishima.pool;

import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class StatementPool {
  private final ConcurrentHashMap<String, ConcurrentHashMap<String, Statement>> statementMap =
      new ConcurrentHashMap<>();

  public void put(String datasource, String queryId, Statement statement) {
    ConcurrentHashMap<String, Statement> map = statementMap.getOrDefault(datasource, new ConcurrentHashMap<>());
    map.put(queryId, statement);
    statementMap.put(datasource, map);
  }

  public Statement get(String datasource, String queryId) {
    return statementMap.get(datasource).get(queryId);
  }

  public void remove(String datasource, String queryId) {
    statementMap.get(datasource).remove(queryId);
  }
}
