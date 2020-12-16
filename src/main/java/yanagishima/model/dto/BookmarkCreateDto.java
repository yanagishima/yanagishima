package yanagishima.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookmarkCreateDto {
  @JsonProperty("bookmark_id")
  private int bookmarkId;
  private String error;
}
