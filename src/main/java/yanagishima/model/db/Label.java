package yanagishima.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@IdClass(LabelId.class)
@Table(name = "label")
public class Label {
  @Id
  @Column(name = "datasource")
  private String datasource;

  @Id
  @Column(name = "engine")
  private String engine;

  @Id
  @Column(name = "query_id")
  private String queryid;

  @Column(name = "label_name")
  private String labelName;
}
