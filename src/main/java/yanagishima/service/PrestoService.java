package yanagishima.service;

import yanagishima.exception.QueryErrorException;
import yanagishima.result.PrestoQueryResult;

import java.util.Optional;

public interface PrestoService {
	PrestoQueryResult doQuery(String datasource, String query, String userName, Optional<String> prestoUser, Optional<String> prestoPassword, boolean storeFlag, int limit) throws QueryErrorException;

	String doQueryAsync(String datasource, String query, Optional<String> sessionPropertyOptional, String userName, Optional<String> prestoUser, Optional<String> prestoPassword);
}
