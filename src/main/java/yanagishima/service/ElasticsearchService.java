package yanagishima.service;

import yanagishima.exception.ElasticsearchQueryErrorException;
import yanagishima.result.ElasticsearchQueryResult;


public interface ElasticsearchService {

    public ElasticsearchQueryResult doQuery(String datasource, String query, String userName, boolean storeFlag, int limit) throws ElasticsearchQueryErrorException;

    public ElasticsearchQueryResult doTranslate(String datasource, String query, String userName, boolean storeFlag, int limit) throws ElasticsearchQueryErrorException;

}
