package yanagishima.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;


public class SparkUtil {

    public static Optional<Map> getRunningJob(String resourceManagerUrl, String sparkJdbcApplicationId, String queryId) {
        List<Map> jobList = getJobList(resourceManagerUrl, sparkJdbcApplicationId);
        for(Map m : jobList) {
            if(!m.get("status").equals("RUNNING")) {
                continue;
            }
            String submissionTime = (String)m.get("submissionTime");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSzzz");
            ZonedDateTime submissionTimeZdt =  ZonedDateTime.parse(submissionTime, dtf);
            LocalDateTime submitTimeLdt = LocalDateTime.parse(queryId.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
            long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, submissionTimeZdt);
            if(elapsedTimeMillis/1000 < 3) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public static List<Map> getJobList(String resourceManagerUrl, String sparkJdbcApplicationId) {

        try {
            String originalJson = Request.Get(resourceManagerUrl + "/proxy/" + sparkJdbcApplicationId + "/api/v1/applications/" + sparkJdbcApplicationId + "/jobs")
                    .execute().returnContent().asString(UTF_8);
/*
[ {
  "jobId" : 15,
  "name" : "run at AccessController.java:0",
  "description" : "SELECT * FROM ... LIMIT 100",
  "submissionTime" : "2019-02-21T02:23:54.513GMT",
  "completionTime" : "2019-02-21T02:24:02.561GMT",
  "stageIds" : [ 21 ],
  "jobGroup" : "b46b9cee-bb9d-4d10-8b44-5f7328ce5748",
  "status" : "SUCCEEDED",
  "numTasks" : 1,
  "numActiveTasks" : 0,
  "numCompletedTasks" : 1,
  "numSkippedTasks" : 0,
  "numFailedTasks" : 0,
  "numActiveStages" : 0,
  "numCompletedStages" : 1,
  "numSkippedStages" : 0,
  "numFailedStages" : 0
}, {
  "jobId" : 14,
  "name" : "run at AccessController.java:0",
  "description" : "SELECT * FROM ... LIMIT 100",
  "submissionTime" : "2019-02-21T02:19:38.946GMT",
  "completionTime" : "2019-02-21T02:19:50.317GMT",
  "stageIds" : [ 20 ],
  "jobGroup" : "7040a016-2f3c-47ee-a28e-086dcada02a4",
  "status" : "SUCCEEDED",
  "numTasks" : 1,
  "numActiveTasks" : 0,
  "numCompletedTasks" : 1,
  "numSkippedTasks" : 0,
  "numFailedTasks" : 0,
  "numActiveStages" : 0,
  "numCompletedStages" : 1,
  "numSkippedStages" : 0,
  "numFailedStages" : 0
},
...
*/
            ObjectMapper mapper = new ObjectMapper();
            List<Map> jobList = mapper.readValue(originalJson, List.class);
            return jobList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
