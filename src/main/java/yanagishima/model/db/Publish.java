package yanagishima.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "publish")
public class Publish {
  @Id
  @Column(name = "publish_id")
  private String publishId;

  @Column(name = "datasource")
  private String datasource;

  @Column(name = "engine")
  private String engine;

  @Column(name = "query_id")
  private String queryId;

  @Column(name = "userid")
  private String userid;

  @Column(name = "viewers")
  private String viewers;

}
