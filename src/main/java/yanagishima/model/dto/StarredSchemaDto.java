package yanagishima.model.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import yanagishima.model.db.StarredSchema;

@Getter
@Setter
public class StarredSchemaDto {
  private List<StarredSchema> starredSchemaList;
  private String error;
}
