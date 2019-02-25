package yanagishima.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    public static String getSparkJdbcApplicationId(String sparkWebUrl) {
        OkHttpClient client = new OkHttpClient();
        Request okhttpRequest = new Request.Builder().url(sparkWebUrl).build();
        try (Response okhttpResponse = client.newCall(okhttpRequest).execute()) {
            HttpUrl url = okhttpResponse.request().url();
            // http://spark.thrift.server:4040 -> http://resourcemanager:8088/proxy/redirect/application_xxxxxxx/
            String sparkJdbcApplicationId = url.pathSegments().get(url.pathSize() - 2);
            if (!sparkJdbcApplicationId.startsWith("application_")) {
                throw new IllegalArgumentException(sparkJdbcApplicationId + " is illegal");
            }
            return sparkJdbcApplicationId;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static long getElapsedTimeMillis(String submissionTime, String queryId) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSzzz");
        ZonedDateTime submissionTimeZdt =  ZonedDateTime.parse(submissionTime, dtf);
        LocalDateTime submitTimeLdt = LocalDateTime.parse(queryId.substring(0, "yyyyMMdd_HHmmss".length()), DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        ZonedDateTime submitTimeZdt = submitTimeLdt.atZone(ZoneId.of("GMT", ZoneId.SHORT_IDS));
        long elapsedTimeMillis = ChronoUnit.MILLIS.between(submitTimeZdt, submissionTimeZdt);
        return elapsedTimeMillis;
    }

    public static List<Map> getJobList(String resourceManagerUrl, String sparkJdbcApplicationId) {

        try {
            String originalJson = org.apache.http.client.fluent.Request.Get(resourceManagerUrl + "/proxy/" + sparkJdbcApplicationId + "/api/v1/applications/" + sparkJdbcApplicationId + "/jobs")
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
            for(Map m : jobList) {
                if(m.get("status").equals("RUNNING")) {
                    int numTasks = (int)m.get("numTasks");
                    int numCompletedTasks = (int)m.get("numCompletedTasks");
                    double progress = ((double)numCompletedTasks/numTasks)*100;
                    m.put("progress", progress);
                } else {
                    String submissionTime = (String)m.get("submissionTime");
                    String completionTime = (String)m.get("completionTime");
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSzzz");
                    ZonedDateTime submissionTimeZdt =  ZonedDateTime.parse(submissionTime, dtf);
                    ZonedDateTime completionTimeZdt =  ZonedDateTime.parse(completionTime, dtf);
                    long elapsedTimeMillis = ChronoUnit.MILLIS.between(submissionTimeZdt, completionTimeZdt);
                    m.put("elapsedTime", elapsedTimeMillis);
                }
            }
            return jobList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
