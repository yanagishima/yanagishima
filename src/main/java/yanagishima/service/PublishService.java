package yanagishima.service;

import static org.apache.commons.codec.digest.DigestUtils.md5Hex;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yanagishima.model.db.Publish;
import yanagishima.repository.PublishRepository;

@Service
@RequiredArgsConstructor
public class PublishService {
  private final PublishRepository publishRepository;

  public Optional<Publish> get(String datasource, String engine, String queryId) {
    return publishRepository.findByDatasourceAndEngineAndQueryId(datasource, engine, queryId);
  }

  public Optional<Publish> get(String publishId) {
    return publishRepository.findByPublishId(publishId);
  }

  public Publish publish(String datasource, String engine, String queryId, String user) {
    Optional<Publish> publishedQuery = get(datasource, engine, queryId);
    return publishedQuery.orElseGet(() -> insert(datasource, engine, queryId, user));
  }

  private Publish insert(String datasource, String engine, String queryId, String user) {
    Publish publish = new Publish();
    publish.setPublishId(md5Hex(datasource + ";" + engine + ";" + queryId));
    publish.setDatasource(datasource);
    publish.setEngine(engine);
    publish.setQueryId(queryId);
    publish.setUser(user);
    return publishRepository.save(publish);
  }
}
