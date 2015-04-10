package yanagishima.service;

import java.sql.SQLException;

import yanagishima.result.PrestoQueryResult;


public interface PrestoService {
	
	public PrestoQueryResult doQuery(String query, int limit) throws SQLException;

}
