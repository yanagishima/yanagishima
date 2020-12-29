package yanagishima.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static yanagishima.util.YarnUtil.jsonToMaps;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class YarnUtilTest {
  @Test
  void testJsonToMaps() throws IOException {
    //language=JSON
    String json = "{"
                  + "\"apps\": {\n"
                  + "    \"app\": [\n"
                  + "      {\n"
                  + "        \"amNodeLabelExpression\": \"\",\n"
                  + "        \"finishedTime\": 1502212980368,\n"
                  + "        \"startedTime\": 1502212927109,\n"
                  + "        \"priority\": 0,\n"
                  + "        \"applicationTags\": \"\",\n"
                  + "        \"applicationType\": \"MAPREDUCE\",\n"
                  + "        \"clusterId\": 1495768363173,\n"
                  + "        \"diagnostics\": \"\",\n"
                  + "        \"trackingUrl\": \"http://localhost:8088/proxy/application_1495768363173_330723/\",\n"
                  + "        \"id\": \"application_1495768363173_330723\",\n"
                  + "        \"user\": \"hoge\",\n"
                  + "        \"name\": \"yanagishima-hive-20170809_164758_ac5624e46a802ea3acdcff3fdfa100d1\",\n"
                  + "        \"queue\": \"default\",\n"
                  + "        \"state\": \"FINISHED\",\n"
                  + "        \"finalStatus\": \"SUCCEEDED\",\n"
                  + "        \"progress\": 100,\n"
                  + "        \"trackingUI\": \"History\",\n"
                  + "        \"elapsedTime\": 53259,\n"
                  + "        \"amContainerLogs\": \"http://aaa:8042/node/containerlogs/container_e21_1495768363173_330723_01_000001/hoge\",\n"
                  + "        \"amHostHttpAddress\": \"aaa:8042\",\n"
                  + "        \"allocatedMB\": -1,\n"
                  + "        \"allocatedVCores\": -1,\n"
                  + "        \"runningContainers\": -1,\n"
                  + "        \"memorySeconds\": 1018067,\n"
                  + "        \"vcoreSeconds\": 246,\n"
                  + "        \"queueUsagePercentage\": 0,\n"
                  + "        \"clusterUsagePercentage\": 0,\n"
                  + "        \"preemptedResourceMB\": 0,\n"
                  + "        \"preemptedResourceVCores\": 0,\n"
                  + "        \"numNonAMContainerPreempted\": 0,\n"
                  + "        \"numAMContainerPreempted\": 0,\n"
                  + "        \"logAggregationStatus\": \"SUCCEEDED\",\n"
                  + "        \"unmanagedApplication\": false\n"
                  + "     }\n"
                  + "    ]\n"
                  + "  }\n"
                  + "}";
    List<Map> maps = jsonToMaps(json);
    assertEquals(1, maps.size());

    Map map = maps.get(0);
    assertEquals("", map.get("amNodeLabelExpression"));
    assertEquals(1502212980368L, map.get("finishedTime"));
    assertEquals("MAPREDUCE", map.get("applicationType"));
    assertEquals(0, map.get("priority"));
    assertFalse((boolean) map.get("unmanagedApplication"));
  }
}
