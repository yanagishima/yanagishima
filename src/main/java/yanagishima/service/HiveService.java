package yanagishima.service;

import yanagishima.exception.HiveQueryErrorException;
import yanagishima.result.HiveQueryResult;


public interface HiveService {

    public HiveQueryResult doQuery(String datasource, String query, String userName, boolean storeFlag, int limit) throws HiveQueryErrorException;

    public String doQueryAsync(String datasource, String query, String userName);

}
