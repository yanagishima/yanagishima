package yanagishima.controller;

import static yanagishima.util.QueryIdUtil.datetimeOf;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import yanagishima.annotation.DatasourceAuth;
import yanagishima.config.YanagishimaConfig;
import yanagishima.model.db.Query;
import yanagishima.model.spark.SparkSqlJob;
import yanagishima.service.QueryService;
import yanagishima.util.JsonUtil;
import yanagishima.util.SparkUtil;
import yanagishima.util.Status;
import yanagishima.util.YarnUtil;

@RestController
@RequiredArgsConstructor
public class HiveQueryStatusController {
  private final QueryService queryService;
  private final YanagishimaConfig yanagishimaConfig;

  @DatasourceAuth
  @PostMapping(path = { "hiveQueryStatus", "sparkQueryStatus" })
  public void post(@RequestParam String datasource,
                   @RequestParam String engine,
                   @RequestParam String queryid,
                   @RequestParam(name = "user") Optional<String> hiveUser,
                   HttpServletRequest request, HttpServletResponse response) {
    String resourceManagerUrl = yanagishimaConfig.getResourceManagerUrl(datasource);
    String userName = null;
    if (yanagishimaConfig.isUseAuditHttpHeaderName()) {
      userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
    } else {
      if (hiveUser.isPresent()) {
        userName = hiveUser.get();
      }
    }

    Optional<Query> queryOptional = queryService.getByEngine(queryid, datasource, engine);
    if (engine.equals("hive")) {
      Optional<Map> applicationOptional = YarnUtil.getApplication(resourceManagerUrl, queryid, userName,
                                                                  yanagishimaConfig
                                                                      .getResourceManagerBegin(datasource));
      if (applicationOptional.isPresent()) {
        try {
          response.setContentType("application/json");
          PrintWriter writer = response.getWriter();
          ObjectMapper mapper = new ObjectMapper();
          String json = mapper.writeValueAsString(applicationOptional.get());
          writer.println(json);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        if (queryOptional.isEmpty()) {
          HashMap<String, Object> retVal = new HashMap<>();
          retVal.put("state", "RUNNING");
          retVal.put("progress", 0);
          retVal.put("elapsedTime", 0);
          JsonUtil.writeJSON(response, retVal);
        }
      }
    } else if (engine.equals("spark")) {
      HashMap<String, Object> retVal = new HashMap<>();
      if (queryOptional.isPresent()) {
        if (queryOptional.get().getStatus().equals(Status.SUCCEED.name())) {
          retVal.put("state", "FINISHED");
        } else if (queryOptional.get().getStatus().equals(Status.FAILED.name())) {
          retVal.put("state", "FAILED");
        } else {
          throw new IllegalArgumentException(
              String.format("unknown status=%s", queryOptional.get().getStatus()));
        }
      } else {
        retVal.put("state", "RUNNING");

        String sparkJdbcApplicationId = SparkUtil.getSparkJdbcApplicationId(
            yanagishimaConfig.getSparkWebUrl(datasource));
        List<Map> runningList = SparkUtil.getSparkRunningJobListWithProgress(resourceManagerUrl,
                                                                             sparkJdbcApplicationId);
        if (runningList.isEmpty()) {
          retVal.put("progress", 0);
        } else {
          List<SparkSqlJob> sparkSqlJobList = SparkUtil.getSparkSqlJobFromSqlserver(resourceManagerUrl,
                                                                                    sparkJdbcApplicationId);
          for (Map m : runningList) {
            String groupId = (String) m.get("jobGroup");
            for (SparkSqlJob ssj : sparkSqlJobList) {
              if (ssj.getGroupId().equals(groupId) && ssj.getUser().equals(hiveUser.orElse(null)) && !ssj
                  .getJobIds().isEmpty()) {
                int numTasks = (int) m.get("numTasks");
                int numCompletedTasks = (int) m.get("numCompletedTasks");
                double progress = ((double) numCompletedTasks / numTasks) * 100;
                retVal.put("progress", progress);
                break;
              }
            }
          }
        }

        LocalDateTime submitTimeLdt = datetimeOf(queryid);
        ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
        long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, ZonedDateTime.now(ZoneId.of("GMT")));
        retVal.put("elapsedTime", elapsedTimeMillis);
      }
      JsonUtil.writeJSON(response, retVal);
    } else {
      throw new IllegalArgumentException(engine + " is illegal");
    }
  }
}
