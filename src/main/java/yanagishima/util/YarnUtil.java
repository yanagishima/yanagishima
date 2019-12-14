package yanagishima.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static yanagishima.util.Constants.YANAGISHIAM_HIVE_JOB_PREFIX;

public final class YarnUtil {
    private YarnUtil() {}

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

    public static Optional<Map> getApplication(String resourceManagerUrl, String queryId, String userName, Optional<String> beginOptional) {
        List<Map> yarnJoblist = getJobList(resourceManagerUrl, beginOptional);
        if(userName == null) {
            return yarnJoblist.stream().filter(m -> m.get("name").equals(YANAGISHIAM_HIVE_JOB_PREFIX + queryId)).findFirst();
        } else {
            return yarnJoblist.stream().filter(m -> m.get("name").equals(YANAGISHIAM_HIVE_JOB_PREFIX + userName + "-" + queryId)).findFirst();
        }

    }

    public static List<Map> getJobList(String resourceManagerUrl, Optional<String> beginOptional) {

        try {
            String originalJson = null;
            if(beginOptional.isPresent()) {
                long currentTimeMillis = System.currentTimeMillis();
                String startedTimeBegin = String.valueOf(currentTimeMillis - Long.valueOf(beginOptional.get()));
                originalJson = Request.Get(resourceManagerUrl + "/ws/v1/cluster/apps?startedTimeBegin=" + startedTimeBegin)
                        .execute().returnContent().asString(UTF_8);
            } else {
                originalJson = Request.Get(resourceManagerUrl + "/ws/v1/cluster/apps")
                        .execute().returnContent().asString(UTF_8);
            }

            return jsonToMaps(originalJson);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    public static List<Map> jsonToMaps(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(json, Map.class);
        List<Map> yarnJoblist = (List) ((Map) map.get("apps")).get("app");
        return yarnJoblist;
    }
}
