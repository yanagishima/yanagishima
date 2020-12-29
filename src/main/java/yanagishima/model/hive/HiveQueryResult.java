package yanagishima.model.hive;

import java.util.List;

import io.airlift.units.DataSize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HiveQueryResult {
  private List<String> columns;
  private List<List<String>> records;
  private String warningMessage;
  private String queryId;
  private int lineNumber;
  private DataSize rawDataSize;
}
