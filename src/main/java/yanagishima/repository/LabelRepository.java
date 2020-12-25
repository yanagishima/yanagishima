package yanagishima.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import yanagishima.model.db.Label;
import yanagishima.model.db.LabelId;

@Deprecated
@Repository
public interface LabelRepository extends CrudRepository<Label, LabelId> {
  Optional<Label> findByDatasourceAndEngineAndQueryid(String datasource, String engine, String queryid);
}
