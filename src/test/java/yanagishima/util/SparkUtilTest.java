package yanagishima.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static yanagishima.util.SparkUtil.jsonToMaps;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SparkUtilTest {
    @Test
    void testJsonToMaps() throws IOException {
        //language=JSON
        String json = "[ {\n"
                      + "  \"jobId\" : 15,\n"
                      + "  \"name\" : \"run at AccessController.java:0\",\n"
                      + "  \"description\" : \"SELECT * FROM ... LIMIT 100\",\n"
                      + "  \"submissionTime\" : \"2019-02-21T02:23:54.513GMT\",\n"
                      + "  \"completionTime\" : \"2019-02-21T02:24:02.561GMT\",\n"
                      + "  \"stageIds\" : [ 21 ],\n"
                      + "  \"jobGroup\" : \"b46b9cee-bb9d-4d10-8b44-5f7328ce5748\",\n"
                      + "  \"status\" : \"SUCCEEDED\",\n"
                      + "  \"numTasks\" : 1,\n"
                      + "  \"numActiveTasks\" : 0,\n"
                      + "  \"numCompletedTasks\" : 1,\n"
                      + "  \"numSkippedTasks\" : 0,\n"
                      + "  \"numFailedTasks\" : 0,\n"
                      + "  \"numActiveStages\" : 0,\n"
                      + "  \"numCompletedStages\" : 1,\n"
                      + "  \"numSkippedStages\" : 0,\n"
                      + "  \"numFailedStages\" : 0\n"
                      + "}, {\n"
                      + "  \"jobId\" : 14,\n"
                      + "  \"name\" : \"run at AccessController.java:0\",\n"
                      + "  \"description\" : \"SELECT * FROM ... LIMIT 100\",\n"
                      + "  \"submissionTime\" : \"2019-02-21T02:19:38.946GMT\",\n"
                      + "  \"completionTime\" : \"2019-02-21T02:19:50.317GMT\",\n"
                      + "  \"stageIds\" : [ 20 ],\n"
                      + "  \"jobGroup\" : \"7040a016-2f3c-47ee-a28e-086dcada02a4\",\n"
                      + "  \"status\" : \"SUCCEEDED\",\n"
                      + "  \"numTasks\" : 1,\n"
                      + "  \"numActiveTasks\" : 0,\n"
                      + "  \"numCompletedTasks\" : 1,\n"
                      + "  \"numSkippedTasks\" : 0,\n"
                      + "  \"numFailedTasks\" : 0,\n"
                      + "  \"numActiveStages\" : 0,\n"
                      + "  \"numCompletedStages\" : 1,\n"
                      + "  \"numSkippedStages\" : 0,\n"
                      + "  \"numFailedStages\" : 0\n"
                      + " }\n"
                      + "]";

        List<Map> maps = jsonToMaps(json);
        assertEquals(2, maps.size());

        Map map = maps.get(0);
        assertEquals(15, map.get("jobId"));
        assertEquals("run at AccessController.java:0", map.get("name"));
        assertEquals(List.of(21), map.get("stageIds"));
    }
}
