package yanagishima.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import yanagishima.model.db.Publish;

import java.util.List;

@Getter
@Setter
public class PublishListDto {
  @JsonProperty("publishList")
  private List<Publish> publishList;
  private String error;
}
