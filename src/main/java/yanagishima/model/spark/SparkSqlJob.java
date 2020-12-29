package yanagishima.model.spark;

import java.util.List;

import lombok.Data;

@Data
public class SparkSqlJob {

  private String user;

  private List<Integer> jobIds;

  private String groupId;

  private String startTime;

  private String finishTime;

  private String duration;

  private String statement;

  private String state;

  private String detail;

}
