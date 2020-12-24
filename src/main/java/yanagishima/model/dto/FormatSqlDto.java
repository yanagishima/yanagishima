package yanagishima.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FormatSqlDto {
  private String formattedQuery;
}
