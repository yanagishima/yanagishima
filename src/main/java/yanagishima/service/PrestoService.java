package yanagishima.service;

import java.sql.SQLException;
import java.util.List;

public interface PrestoService {
	
	public List<List<Object>> doQuery(String query) throws SQLException;

}
