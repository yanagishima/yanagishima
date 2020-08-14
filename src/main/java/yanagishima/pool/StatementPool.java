package yanagishima.pool;

import javax.inject.Singleton;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class StatementPool {

    private ConcurrentHashMap<String, ConcurrentHashMap<String, Statement>> statementMap = new ConcurrentHashMap<>();

    public void putStatement(String datasource, String queryId, Statement statement) {
        ConcurrentHashMap<String, Statement> map = statementMap.getOrDefault(datasource, new ConcurrentHashMap<>());
        map.put(queryId, statement);
        statementMap.put(datasource, map);
    }

    public Statement getStatement(String datasource, String queryId) {
        return statementMap.get(datasource).get(queryId);
    }

    public Statement removeStatement(String datasource, String queryId) {
        return statementMap.get(datasource).remove(queryId);
    }
}
