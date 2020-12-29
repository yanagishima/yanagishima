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
@Table(name = "session_property")
public class SessionProperty {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "session_property_id")
  @JsonProperty("session_property_id")
  private int sessionPropertyId;

  @Column(name = "datasource")
  private String datasource;

  @Column(name = "engine")
  private String engine;

  @Column(name = "query_id")
  @JsonProperty("query_id")
  private String queryId;

  @Column(name = "session_key")
  @JsonProperty("session_key")
  private String sessionKey;

  @Column(name = "session_value")
  @JsonProperty("session_value")
  private String sessionValue;
}
