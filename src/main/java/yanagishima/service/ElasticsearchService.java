package yanagishima.service;

import yanagishima.exception.ElasticsearchQueryErrorException;
import yanagishima.model.elasticsearch.ElasticsearchQueryResult;

public interface ElasticsearchService {
    ElasticsearchQueryResult doQuery(String datasource, String query, String userName, boolean storeFlag, int limit) throws ElasticsearchQueryErrorException;

    ElasticsearchQueryResult doTranslate(String datasource, String query, String userName, boolean storeFlag, int limit) throws ElasticsearchQueryErrorException;
}
