package yanagishima.service;

import yanagishima.exception.QueryErrorException;
import yanagishima.result.PrestoQueryResult;


public interface PrestoService {
	
	public PrestoQueryResult doQuery(String query) throws QueryErrorException;

}
