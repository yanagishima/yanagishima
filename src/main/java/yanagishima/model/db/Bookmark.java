package yanagishima.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Entity
@Table(name = "bookmark")
public class Bookmark {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "bookmark_id")
  @JsonProperty("bookmark_id")
  private int bookmarkId;

  @Column(name = "datasource")
  private String datasource;

  @Column(name = "engine")
  private String engine;

  @Column(name = "query")
  private String query;

  @Column(name = "title")
  private String title;

  @Column(name = "userid")
  private String userid;

  @Column(name = "snippet")
  private String snippet;
}
