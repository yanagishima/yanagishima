package yanagishima.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublishDto {
  @JsonProperty("publish_id")
  private String publishId;

  private String error;
}
