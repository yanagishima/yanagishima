package yanagishima.controller;

import static yanagishima.util.SparkUtil.getSparkJdbcApplicationId;
import static yanagishima.util.SparkUtil.getSparkRunningJobListWithProgress;
import static yanagishima.util.SparkUtil.getSparkSqlJobFromSqlserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.spark.SparkSqlJob;

@RestController
@RequiredArgsConstructor
public class SparkJobListController {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final YanagishimaConfig config;

  @DatasourceAuth
  @GetMapping("sparkJobList")
  public void get(@RequestParam String datasource,
                  HttpServletResponse response) throws IOException {
    String resourceManagerUrl = config.getResourceManagerUrl(datasource);
    String sparkJdbcApplicationId = getSparkJdbcApplicationId(config.getSparkWebUrl(datasource));
    List<Map> runningJobs = getSparkRunningJobListWithProgress(resourceManagerUrl, sparkJdbcApplicationId);
    List<SparkSqlJob> sparkSqlJobs = getSparkSqlJobFromSqlserver(resourceManagerUrl, sparkJdbcApplicationId);
    for (Map group : runningJobs) {
      String groupId = (String) group.get("jobGroup");
      for (SparkSqlJob job : sparkSqlJobs) {
        if (job.getGroupId().equals(groupId)) {
          group.put("jobIds", job.getJobIds());
          group.put("user", job.getUser());
          group.put("query", job.getStatement());
          group.put("duration", job.getDuration());
          break;
        }
      }
    }
    List<SparkSqlJob> completedJobs = sparkSqlJobs.stream()
                                                  .filter(job -> !job.getJobIds().isEmpty())
                                                  .filter(job -> "FINISHED".equals(job.getState()) || "FAILED"
                                                      .equals(job.getState()))
                                                  .sorted(
                                                      (a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                                                  .limit(100)
                                                  .collect(Collectors.toList());
    ;

    List<Map> limitedJobs = new ArrayList<>();
    limitedJobs.addAll(runningJobs);
    for (SparkSqlJob ssj : completedJobs) {
      Map<String, Object> job = new HashMap<>();
      job.put("jobGroup", ssj.getGroupId());
      job.put("jobIds", ssj.getJobIds());
      job.put("user", ssj.getUser());
      job.put("query", ssj.getStatement());
      job.put("status", ssj.getState());
      job.put("submissionTime", ssj.getStartTime());
      job.put("duration", ssj.getDuration());
      limitedJobs.add(job);
    }

    response.setContentType("application/json");
    try (PrintWriter writer = response.getWriter()) {
      writer.println(OBJECT_MAPPER.writeValueAsString(limitedJobs));
    }
  }
}
