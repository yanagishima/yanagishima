package yanagishima.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StarredSchemaCreateDto {
  @JsonProperty("starred_schema_id")
  private int starredSchemaId;
  private String error;
}
