package yanagishima.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@IdClass(CommentId.class)
@Table(name = "comment")
public class Comment {
  @Id
  @Column(name = "datasource")
  private String datasource;

  @Id
  @Column(name = "engine")
  private String engine;

  @Id
  @Column(name = "query_id")
  private String queryid;

  @Column(name = "update_time_string")
  private String updateTimeString;

  @Column(name = "content")
  private String content;

  @Column(name = "userid")
  private String userid;

  @Column(name = "like_count")
  private int likeCount;
}
