package yanagishima.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import yanagishima.model.db.SessionProperty;

@Repository
public interface SessionPropertyRepository extends CrudRepository<SessionProperty, Long> {
  List<SessionProperty> findAllByDatasourceAndEngineAndQueryId(String datasource, String engine,
                                                               String queryId);
}
