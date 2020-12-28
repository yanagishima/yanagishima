package yanagishima.model.db;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentId implements Serializable {
  private String datasource;
  private String engine;
  private String queryid;
}
