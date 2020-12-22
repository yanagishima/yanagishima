package yanagishima.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import yanagishima.model.db.StarredSchema;

@Repository
public interface StarredSchemaRepository extends CrudRepository<StarredSchema, Integer> {
  List<StarredSchema> findAllByDatasourceAndEngineAndCatalogAndUser(
      String datasource, String engine, String catalog, String user);

  void deleteByStarredSchemaId(int starredSchemaId);
}
