package yanagishima.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yanagishima.model.User;
import yanagishima.model.db.StarredSchema;
import yanagishima.repository.StarredSchemaRepository;

@Service
@RequiredArgsConstructor
public class StarredSchemaService {
  private final StarredSchemaRepository starredSchemaRepository;

  public List<StarredSchema> getAll(String datasource, String engine, String catalog, User user) {
    return starredSchemaRepository.findAllByDatasourceAndEngineAndCatalogAndUserid(datasource, engine, catalog,
                                                                                 user.getId());
  }

  public StarredSchema insert(String datasource, String engine, String catalog, String schema, User user) {
    StarredSchema starredSchema = new StarredSchema();
    starredSchema.setDatasource(datasource);
    starredSchema.setEngine(engine);
    starredSchema.setCatalog(catalog);
    starredSchema.setSchema(schema);
    starredSchema.setUserid(user.getId());
    return starredSchemaRepository.save(starredSchema);
  }

  @Transactional
  public void delete(int starredSchemaId) {
    starredSchemaRepository.deleteByStarredSchemaId(starredSchemaId);
  }
}
