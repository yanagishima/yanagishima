package yanagishima.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import yanagishima.model.db.Publish;

@Repository
public interface PublishRepository extends CrudRepository<Publish, String> {
  Optional<Publish> findByDatasourceAndEngineAndQueryId(String datasource, String engine, String queryId);

  Optional<Publish> findByPublishId(String publishId);

  List<Publish> findAllByDatasourceAndEngineAndUseridOrderByQueryIdDesc(String datasource, String engine,
                                                                        String userid, Pageable pageable);
}
