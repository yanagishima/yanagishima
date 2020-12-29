package yanagishima.model.elasticsearch;

import java.util.List;

import io.airlift.units.DataSize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElasticsearchQueryResult {
  private List<String> columns;
  private List<List<String>> records;
  private String warningMessage;
  private String queryId;
  private int lineNumber;
  private DataSize rawDataSize;
}
