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
@Table(name = "starred_schema")
public class StarredSchema {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "starred_schema_id")
  @JsonProperty("starred_schema_id")
  private int starredSchemaId;

  @Column(name = "datasource")
  private String datasource;

  @Column(name = "engine")
  private String engine;

  @Column(name = "catalog")
  private String catalog;

  @Column(name = "`schema`")
  private String schema;

  @Column(name = "userid")
  private String userid;
}
