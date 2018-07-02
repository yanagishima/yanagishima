package yanagishima.pool;

import javax.inject.Singleton;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class StatementPool {

    private String queryId;

    private ConcurrentHashMap<String, Statement> statementMap = new ConcurrentHashMap<>();

    public void putStatement(String queryId, Statement statement) {
        statementMap.put(queryId, statement);
    }

    public Statement getStatement(String queryId) {
        return statementMap.get(queryId);
    }

    public Statement removeStatement(String queryId) {
        return statementMap.remove(queryId);
    }
}
