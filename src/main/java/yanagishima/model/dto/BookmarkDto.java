package yanagishima.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import yanagishima.model.db.Bookmark;

@Getter
@Setter
public class BookmarkDto {
  @JsonProperty("bookmarkList")
  private List<Bookmark> bookmarks;
  private String error;
}
