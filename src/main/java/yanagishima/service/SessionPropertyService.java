package yanagishima.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yanagishima.model.db.SessionProperty;
import yanagishima.repository.SessionPropertyRepository;

@Service
@RequiredArgsConstructor
public class SessionPropertyService {
  private final SessionPropertyRepository sessionPropertyRepository;

  public List<SessionProperty> getAll(String datasource, String engine, String queryId) {
    return sessionPropertyRepository.findAllByDatasourceAndEngineAndQueryId(datasource, engine, queryId);
  }

  public void insert(String datasource, String engine, String queryId, Map<String, String> properties) {
    for (Entry<String, String> property : properties.entrySet()) {
      SessionProperty sessionProperty = new SessionProperty();
      sessionProperty.setDatasource(datasource);
      sessionProperty.setEngine(engine);
      sessionProperty.setQueryId(queryId);
      sessionProperty.setSessionKey(property.getKey());
      sessionProperty.setSessionValue(property.getValue());
      sessionPropertyRepository.save(sessionProperty);
    }
  }
}
