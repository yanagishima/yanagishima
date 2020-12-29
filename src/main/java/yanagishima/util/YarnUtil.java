package yanagishima.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static yanagishima.util.Constants.YANAGISHIAM_HIVE_JOB_PREFIX;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class YarnUtil {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static String kill(String resourceManagerUrl, String applicationId) {
    try {
      Request request = Request.Put(resourceManagerUrl + "/ws/v1/cluster/apps/" + applicationId + "/state");
      request.addHeader(new BasicHeader("Content-Type", APPLICATION_JSON));
      String json = OBJECT_MAPPER.writeValueAsString(Map.of("state", "KILLED"));
      request.body(new StringEntity(json, UTF_8));
      return request.execute().returnContent().asString(UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Optional<Map> getApplication(String resourceManagerUrl, String queryId, String userName,
                                             Optional<String> beginTime) {
    List<Map> yarnJobs = getJobList(resourceManagerUrl, beginTime);
    if (userName == null) {
      return yarnJobs.stream().filter(job -> job.get("name").equals(YANAGISHIAM_HIVE_JOB_PREFIX + queryId))
                     .findFirst();
    }
    return yarnJobs.stream().filter(
        job -> job.get("name").equals(YANAGISHIAM_HIVE_JOB_PREFIX + userName + "-" + queryId)).findFirst();
  }

  public static List<Map> getJobList(String resourceManagerUrl, Optional<String> beginTime) {
    try {
      String json;
      if (beginTime.isPresent()) {
        long currentTimeMillis = System.currentTimeMillis();
        long startedTimeBegin = currentTimeMillis - Long.valueOf(beginTime.get());
        json = Request.Get(resourceManagerUrl + "/ws/v1/cluster/apps?startedTimeBegin=" + startedTimeBegin)
                      .execute().returnContent().asString(UTF_8);
      } else {
        json = Request.Get(resourceManagerUrl + "/ws/v1/cluster/apps")
                      .execute().returnContent().asString(UTF_8);
      }
      return jsonToMaps(json);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @VisibleForTesting
  public static List<Map> jsonToMaps(String json) throws IOException {
    Map map = OBJECT_MAPPER.readValue(json, Map.class);
    if (map.get("apps") == null) {
      return List.of();
    }
    return (List) ((Map) map.get("apps")).get("app");
  }
}
