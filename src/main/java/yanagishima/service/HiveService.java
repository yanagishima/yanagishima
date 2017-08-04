package yanagishima.service;

import yanagishima.result.HiveQueryResult;


public interface HiveService {

    public HiveQueryResult doQuery(String datasource, String query, String userName, boolean storeFlag, int limit);

    public String doQueryAsync(String datasource, String query, String userName);

}
