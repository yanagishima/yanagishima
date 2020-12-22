package yanagishima.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yanagishima.model.db.StarredSchema;
import yanagishima.repository.StarredSchemaRepository;

@Service
@RequiredArgsConstructor
public class StarredSchemaService {
  private final StarredSchemaRepository starredSchemaRepository;

  public List<StarredSchema> getAll(String datasource, String engine, String catalog, String user) {
    return starredSchemaRepository.findAllByDatasourceAndEngineAndCatalogAndUser(datasource, engine, catalog, user);
  }

  public StarredSchema insert(String datasource, String engine, String catalog, String schema, String user) {
    StarredSchema starredSchema = new StarredSchema();
    starredSchema.setDatasource(datasource);
    starredSchema.setEngine(engine);
    starredSchema.setCatalog(catalog);
    starredSchema.setSchema(schema);
    starredSchema.setUser(user);
    return starredSchemaRepository.save(starredSchema);
  }

  @Transactional
  public void delete(int starredSchemaId) {
    starredSchemaRepository.deleteByStarredSchemaId(starredSchemaId);
  }
}
