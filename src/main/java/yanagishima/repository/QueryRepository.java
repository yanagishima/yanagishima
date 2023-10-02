package yanagishima.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import yanagishima.model.db.Query;
import yanagishima.model.db.QueryId;

@Repository
public interface QueryRepository extends CrudRepository<Query, QueryId> {
  List<Query> findAllByDatasourceAndEngineAndUseridAndQueryStringContainsOrderByQueryIdDesc(
      String datasource, String engine, String user, String query, Pageable pageable);

  List<Query> findAllByDatasourceAndEngineAndQueryIdIn(String datasource, String engine, List<String> queryIds);

  long countAllByDatasourceAndEngineAndUserid(String datasource, String engine, String user);

  List<Query> findAllByDatasourceAndQueryIdIn(String datasource, List<String> queryIds);

  Optional<Query> findByQueryIdAndDatasourceAndUserid(String queryId, String datasource, String user);

  Optional<Query> findByQueryIdAndDatasourceAndEngine(String queryId, String datasource, String engine);

  Optional<Query> findByQueryIdAndDatasource(String queryId, String datasource);
}
