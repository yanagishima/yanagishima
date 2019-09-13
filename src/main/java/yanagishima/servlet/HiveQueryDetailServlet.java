package yanagishima.servlet;

import yanagishima.bean.HttpRequestContext;
import yanagishima.config.YanagishimaConfig;
import yanagishima.util.AccessControlUtil;
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

import static java.util.Objects.requireNonNull;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class HiveQueryDetailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private YanagishimaConfig config;

    @Inject
    public HiveQueryDetailServlet(YanagishimaConfig config) {
        this.config = config;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpRequestContext context = new HttpRequestContext(request);
        requireNonNull(context.getDatasource(), "datasource is null");
        requireNonNull(context.getEngine(), "engine is null");

        if (config.isCheckDatasource()) {
            if (!AccessControlUtil.validateDatasource(request, context.getDatasource())) {
                try {
                    response.sendError(SC_FORBIDDEN);
                    return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        String resourceManagerUrl = config.getResourceManagerUrl(context.getDatasource());
        Optional<String> idOptinal = Optional.ofNullable(context.getId());
        if("hive".equals(context.getEngine())) {
            idOptinal.ifPresent(id -> {
                if (id.startsWith("application_")) {
                    try {
                        response.sendRedirect(resourceManagerUrl + "/cluster/app/" + id);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    String userName = null;
                    Optional<String> hiveUser = Optional.ofNullable(context.getUser());
                    if(config.isUseAuditHttpHeaderName()) {
                        userName = request.getHeader(config.getAuditHttpHeaderName());
                    } else {
                        if (hiveUser.isPresent()) {
                            userName = hiveUser.get();
                        }
                    }
                    Optional<Map> applicationOptional = YarnUtil.getApplication(resourceManagerUrl, id, userName, config.getResourceManagerBegin(context.getDatasource()));
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
        } else if("spark".equals(context.getEngine())) {
            String sparkWebUrl = config.getSparkWebUrl(context.getDatasource());
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
            throw new IllegalArgumentException(context.getEngine() + " is illegal");
        }
    }
}
