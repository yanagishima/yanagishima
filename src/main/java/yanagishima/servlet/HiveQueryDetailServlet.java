package yanagishima.servlet;

import yanagishima.config.YanagishimaConfig;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.SparkUtil;
import yanagishima.util.YarnUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class HiveQueryDetailServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public HiveQueryDetailServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        String datasource = HttpRequestUtil.getParam(request, "datasource");
        if (yanagishimaConfig.isCheckDatasource()) {
            if (!AccessControlUtil.validateDatasource(request, datasource)) {
                try {
                    response.sendError(SC_FORBIDDEN);
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        String engine = HttpRequestUtil.getParam(request, "engine");
        String resourceManagerUrl = yanagishimaConfig.getResourceManagerUrl(datasource);
        Optional<String> idOptinal = Optional.ofNullable(request.getParameter("id"));
        if(engine.equals("hive")) {
            idOptinal.ifPresent(id -> {
                if (id.startsWith("application_")) {
                    try {
                        response.sendRedirect(resourceManagerUrl + "/cluster/app/" + id);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String userName = null;
                    Optional<String> hiveUser = Optional.ofNullable(request.getParameter("user"));
                    if(yanagishimaConfig.isUseAuditHttpHeaderName()) {
                        userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
                    } else {
                        if (hiveUser.isPresent()) {
                            userName = hiveUser.get();
                        }
                    }
                    Optional<Map> applicationOptional = YarnUtil.getApplication(resourceManagerUrl, id, userName, yanagishimaConfig.getResourceManagerBegin(datasource));
                    applicationOptional.ifPresent(application -> {
                        String applicationId = (String) application.get("id");
                        try {
                            response.sendRedirect(resourceManagerUrl + "/cluster/app/" + applicationId);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            });
        } else if(engine.equals("spark")) {
            String sparkWebUrl = yanagishimaConfig.getSparkWebUrl(datasource);
            if(idOptinal.isPresent()) {
                String jobId = idOptinal.get();
                try {
                    Integer.parseInt(jobId);
                    String sparkJdbcApplicationId = SparkUtil.getSparkJdbcApplicationId(sparkWebUrl);
                    response.sendRedirect(resourceManagerUrl + "/proxy/" + sparkJdbcApplicationId + "/jobs/job?id=" + jobId);
                } catch (NumberFormatException e) {
                    // we can't specify spark jobId when user pushes info button in Query List tab
                    response.sendRedirect(sparkWebUrl);
                }
            } else {
                response.sendRedirect(sparkWebUrl);
            }
        } else {
            throw new IllegalArgumentException(engine + " is illegal");
        }

    }

}
