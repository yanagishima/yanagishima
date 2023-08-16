package yanagishima.model.db;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@IdClass(QueryId.class)
@Table(name = "query")
public class Query {

  @Id
  @Column(name = "datasource")
  private String datasource;

  @Id
  @Column(name = "engine")
  private String engine;

  @Id
  @Column(name = "query_id")
  private String queryId;

  @Column(name = "fetch_result_time_string")
  private String fetchResultTimeString;

  @Column(name = "query_string")
  private String queryString;

  @Column(name = "userid")
  private String userid;

  @Column(name = "status")
  private String status;

  @Column(name = "elapsed_time_millis")
  private Integer elapsedTimeMillis;

  @Column(name = "result_file_size")
  private Long resultFileSize;

  @Column(name = "linenumber")
  private Integer linenumber;
}
