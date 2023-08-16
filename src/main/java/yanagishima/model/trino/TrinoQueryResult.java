package yanagishima.model.trino;

import io.airlift.units.DataSize;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TrinoQueryResult {
  private String updateType;
  private List<String> columns;
  private List<List<String>> records;
  private String warningMessage;
  private String queryId;
  private int lineNumber;
  private DataSize rawDataSize;
}
