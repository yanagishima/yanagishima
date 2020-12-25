package yanagishima.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import yanagishima.model.db.Label;
import yanagishima.repository.LabelRepository;

@Deprecated
@Service
@RequiredArgsConstructor
public class LabelService {
  private final LabelRepository labelRepository;

  public Optional<Label> get(String datasource, String engine, String label) {
    return labelRepository.findByDatasourceAndEngineAndQueryid(datasource, engine, label);
  }

  public void insert(String datasource, String engine, String queryId, String name) {
    Label label = new Label();
    label.setDatasource(datasource);
    label.setEngine(engine);
    label.setQueryid(queryId);
    label.setLabelName(name);
    labelRepository.save(label);
  }

  public void delete(Label label) {
    labelRepository.delete(label);
  }
}
