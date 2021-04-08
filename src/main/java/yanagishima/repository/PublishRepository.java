package yanagishima.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import yanagishima.model.db.Publish;

@Repository
public interface PublishRepository extends CrudRepository<Publish, String> {
  Optional<Publish> findByDatasourceAndEngineAndQueryId(String datasource, String engine, String queryId);

  Optional<Publish> findByPublishId(String publishId);

  List<Publish> findAllByDatasourceAndEngineAndUser(String datasource, String engine, String user);
}
