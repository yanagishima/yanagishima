package yanagishima.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import yanagishima.bean.SparkSqlJob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static List<SparkSqlJob> getSparkSqlJobFromSqlserver(String resourceManagerUrl, String sparkJdbcApplicationId) {
        try {
            List<SparkSqlJob> sparkSqlJobs = new ArrayList<>();
            Document doc = Jsoup.connect(resourceManagerUrl + "/proxy/" + sparkJdbcApplicationId + "/sqlserver").get();
            // SQL Statistics
            // User	JobID	GroupID	Start Time	Finish Time	Duration	Statement	State	Detail
            Element table = doc.getElementsByTag("tbody").last();
            if (table == null) {
                return sparkSqlJobs;
            }
            for (Element row : table.getElementsByTag("tr")) {
                SparkSqlJob sparkSqlJob = new SparkSqlJob();
                Elements td = row.getElementsByTag("td");
                sparkSqlJob.setUser(td.get(0).text());
                Element jobIds = td.get(1);
                List<Integer> jobIdList = new ArrayList<>();
                if (jobIds.childNodeSize() > 1) {
                    for (Element a : jobIds.getElementsByTag("a")) {
                        String str = a.text();
                        jobIdList.add(Integer.parseInt(str.substring(1, str.length() - 1)));
                    }
                }
                sparkSqlJob.setJobIds(jobIdList);
                sparkSqlJob.setGroupId(td.get(2).text());
                sparkSqlJob.setStartTime(td.get(3).text());
                sparkSqlJob.setFinishTime(td.get(4).text());
                sparkSqlJob.setDuration(td.get(5).text());
                sparkSqlJob.setStatement(td.get(6).text());
                sparkSqlJob.setState(td.get(7).text());
                sparkSqlJob.setDetail(td.get(8).text());
                sparkSqlJobs.add(sparkSqlJob);
            }
            return sparkSqlJobs;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map> getSparkJobList(String resourceManagerUrl, String sparkJdbcApplicationId) {
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
            return jobList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map> getSparkRunningJobListWithProgress(String resourceManagerUrl, String sparkJdbcApplicationId) {
        List<Map> jobList = getSparkJobList(resourceManagerUrl, sparkJdbcApplicationId).stream().filter(m -> m.get("status").equals("RUNNING")).collect(Collectors.toList());
        for (Map m : jobList) {
            int numTasks = (int) m.get("numTasks");
            int numCompletedTasks = (int) m.get("numCompletedTasks");
            double progress = ((double) numCompletedTasks / numTasks) * 100;
            m.put("progress", progress);
        }
        return jobList;
    }
}
