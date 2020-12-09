package yanagishima.servlet;

import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.SparkUtil.getSparkJdbcApplicationId;
import static yanagishima.util.SparkUtil.getSparkRunningJobListWithProgress;
import static yanagishima.util.SparkUtil.getSparkSqlJobFromSqlserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import yanagishima.model.spark.SparkSqlJob;
import yanagishima.config.YanagishimaConfig;

@Singleton
public class SparkJobListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final YanagishimaConfig config;

    @Inject
    public SparkJobListServlet(YanagishimaConfig config) {
        this.config = config;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String datasource = getRequiredParameter(request, "datasource");
        if (config.isCheckDatasource() & !validateDatasource(request, datasource)) {
            sendForbiddenError(response);
            return;
        }
        String resourceManagerUrl = config.getResourceManagerUrl(datasource);
        String sparkJdbcApplicationId = getSparkJdbcApplicationId(config.getSparkWebUrl(datasource));
        List<Map> runningJobs = getSparkRunningJobListWithProgress(resourceManagerUrl, sparkJdbcApplicationId);
        List<SparkSqlJob> sparkSqlJobs = getSparkSqlJobFromSqlserver(resourceManagerUrl, sparkJdbcApplicationId);
        for (Map group : runningJobs) {
            String groupId = (String) group.get("jobGroup");
            for (SparkSqlJob job : sparkSqlJobs) {
                if (job.getGroupId().equals(groupId)) {
                    group.put("jobIds", job.getJobIds());
                    group.put("user", job.getUser());
                    group.put("query", job.getStatement());
                    group.put("duration", job.getDuration());
                    break;
                }
            }
        }
        List<SparkSqlJob> completedJobs = sparkSqlJobs.stream()
                                                          .filter(job -> !job.getJobIds().isEmpty())
                                                          .filter(job -> "FINISHED".equals(job.getState()) || "FAILED".equals(job.getState()))
                                                          .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                                                          .limit(100)
                                                          .collect(Collectors.toList());
        ;

        List<Map> limitedJobs = new ArrayList<>();
        limitedJobs.addAll(runningJobs);
        for (SparkSqlJob ssj : completedJobs) {
            Map<String, Object> job = new HashMap<>();
            job.put("jobGroup", ssj.getGroupId());
            job.put("jobIds", ssj.getJobIds());
            job.put("user", ssj.getUser());
            job.put("query", ssj.getStatement());
            job.put("status", ssj.getState());
            job.put("submissionTime", ssj.getStartTime());
            job.put("duration", ssj.getDuration());
            limitedJobs.add(job);
        }

        response.setContentType("application/json");
        try (PrintWriter writer = response.getWriter()) {
            writer.println(OBJECT_MAPPER.writeValueAsString(limitedJobs));
        }
    }
}
