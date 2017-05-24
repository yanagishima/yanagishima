package yanagishima.service;

import yanagishima.exception.QueryErrorException;
import yanagishima.result.PrestoQueryResult;


public interface PrestoService {
	
	public PrestoQueryResult doQuery(String datasource, String query, String userName, boolean storeFlag) throws QueryErrorException;

	public String doQueryAsync(String datasource, String query, String userName);

}
