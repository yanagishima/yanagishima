package yanagishima.service;

import yanagishima.exception.HiveQueryErrorException;
import yanagishima.result.HiveQueryResult;

import java.util.Optional;


public interface HiveService {

    public HiveQueryResult doQuery(String engine, String datasource, String query, String userName, Optional<String> hiveUser, Optional<String> hivePassword, boolean storeFlag, int limit) throws HiveQueryErrorException;

    public String doQueryAsync(String engine, String datasource, String query, String userName, Optional<String> hiveUser, Optional<String> hivePassword);

}
