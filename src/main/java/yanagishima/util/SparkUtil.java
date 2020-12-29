package yanagishima.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import lombok.experimental.UtilityClass;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import yanagishima.model.spark.SparkSqlJob;

@UtilityClass
public final class SparkUtil {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static String getSparkJdbcApplicationId(String sparkWebUrl) {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(sparkWebUrl).build();
    try (Response response = client.newCall(request).execute()) {
      HttpUrl url = response.request().url();
      // http://spark.thrift.server:4040 -> http://resourcemanager:8088/proxy/redirect/application_xxxxxxx/
      String sparkJdbcApplicationId = url.pathSegments().get(url.pathSize() - 2);
      checkArgument(sparkJdbcApplicationId.startsWith("application_"));
      return sparkJdbcApplicationId;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<SparkSqlJob> getSparkSqlJobFromSqlserver(String resourceManagerUrl,
                                                              String sparkJdbcApplicationId) {
    try {
      List<SparkSqlJob> sparkSqlJobs = new ArrayList<>();
      Document document = Jsoup.connect(resourceManagerUrl + "/proxy/" + sparkJdbcApplicationId + "/sqlserver")
                               .get();
      // SQL Statistics
      // User JobID GroupID Start Time Finish Time Duration Statement State Detail
      Element table = document.getElementsByTag("tbody").last();
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

  public static List<Map> getSparkRunningJobListWithProgress(String resourceManagerUrl,
                                                             String sparkJdbcApplicationId) {
    List<Map> jobs = getSparkJobs(resourceManagerUrl, sparkJdbcApplicationId).stream()
                                                                             .filter(job -> job.get("status")
                                                                                               .equals(
                                                                                                   "RUNNING"))
                                                                             .collect(Collectors.toList());
    for (Map job : jobs) {
      int numTasks = (int) job.get("numTasks");
      int numCompletedTasks = (int) job.get("numCompletedTasks");
      double progress = ((double) numCompletedTasks / numTasks) * 100;
      job.put("progress", progress);
    }
    return jobs;
  }

  private static List<Map> getSparkJobs(String resourceManagerUrl, String sparkJdbcApplicationId) {
    try {
      String json = org.apache.http.client.fluent.Request.Get(
          resourceManagerUrl + "/proxy/" + sparkJdbcApplicationId + "/api/v1/applications/"
          + sparkJdbcApplicationId + "/jobs")
                                                         .execute().returnContent().asString(UTF_8);
      return jsonToMaps(json);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @VisibleForTesting
  public static List<Map> jsonToMaps(String json) throws IOException {
    return OBJECT_MAPPER.readValue(json, List.class);
  }
}
