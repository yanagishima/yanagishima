package yanagishima.servlet;

import yanagishima.config.YanagishimaConfig;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
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
        String resourceManagerUrl = yanagishimaConfig.getResourceManagerUrl(datasource).get();
        String queryId = request.getParameter("queryid");
        Optional<Map> applicationOptional = YarnUtil.getApplication(resourceManagerUrl, queryId);
        applicationOptional.ifPresent(application -> {
            String applicationId = (String) application.get("id");
            try {
                response.sendRedirect(resourceManagerUrl + "/cluster/app/" + applicationId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
