package yanagishima.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static yanagishima.util.Constants.YANAGISHIAM_HIVE_JOB_PREFIX;


public class YarnUtil {

    public static String kill(String resourceManagerUrl, String applicationId) {
        try {
            Request put = Request.Put(resourceManagerUrl + "/ws/v1/cluster/apps/" + applicationId + "/state");
            put.addHeader(new BasicHeader("Content-Type", "application/json"));
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> m = new HashMap<>();
            m.put("state", "KILLED");
            String json = mapper.writeValueAsString(m);
            put.body(new StringEntity(json, UTF_8));
            return put.execute().returnContent().asString(UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<String> getApplicationId(String resourceManagerUrl, String queryId) {
        List<Map> yarnJoblist = getJobList(resourceManagerUrl);
        Optional<Map> jobOptional = yarnJoblist.stream().filter(m -> m.get("name").equals(YANAGISHIAM_HIVE_JOB_PREFIX + queryId)).findFirst();
        if(jobOptional.isPresent()) {
            return Optional.of((String)jobOptional.get().get("id"));
        } else {
            return Optional.empty();
        }
    }

    public static List<Map> getJobList(String resourceManagerUrl) {

        try {
            String originalJson = Request.Get(resourceManagerUrl + "/ws/v1/cluster/apps")
                    .execute().returnContent().asString(UTF_8);
/*
  "apps": {
    "app": [
      {
        "amNodeLabelExpression": "",
        "finishedTime": 1502212980368,
        "startedTime": 1502212927109,
        "priority": 0,
        "applicationTags": "",
        "applicationType": "MAPREDUCE",
        "clusterId": 1495768363173,
        "diagnostics": "",
        "trackingUrl": "http://localhost:8088/proxy/application_1495768363173_330723/",
        "id": "application_1495768363173_330723",
        "user": "hoge",
        "name": "yanagishima-hive-20170809_164758_ac5624e46a802ea3acdcff3fdfa100d1",
        "queue": "default",
        "state": "FINISHED",
        "finalStatus": "SUCCEEDED",
        "progress": 100,
        "trackingUI": "History",
        "elapsedTime": 53259,
        "amContainerLogs": "http://aaa:8042/node/containerlogs/container_e21_1495768363173_330723_01_000001/hoge",
        "amHostHttpAddress": "aaa:8042",
        "allocatedMB": -1,
        "allocatedVCores": -1,
        "runningContainers": -1,
        "memorySeconds": 1018067,
        "vcoreSeconds": 246,
        "queueUsagePercentage": 0,
        "clusterUsagePercentage": 0,
        "preemptedResourceMB": 0,
        "preemptedResourceVCores": 0,
        "numNonAMContainerPreempted": 0,
        "numAMContainerPreempted": 0,
        "logAggregationStatus": "SUCCEEDED",
        "unmanagedApplication": false
      },
      {
*/
            ObjectMapper mapper = new ObjectMapper();
            Map map = mapper.readValue(originalJson, Map.class);
            List<Map> yarnJoblist = (List) ((Map) map.get("apps")).get("app");
            return yarnJoblist;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
