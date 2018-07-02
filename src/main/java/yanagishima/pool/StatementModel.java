package yanagishima.pool;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.sql.Statement;

@Data
@AllArgsConstructor
public class StatementModel {

    private String queryId;

    private String user;

    private Statement statement;
}
