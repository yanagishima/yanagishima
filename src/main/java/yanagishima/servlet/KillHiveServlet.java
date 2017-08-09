package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.io.PrintWriter;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class KillHiveServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(KillHiveServlet.class);

    private static final long serialVersionUID = 1L;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    public KillHiveServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        Optional<String> queryIdOptinal = Optional.ofNullable(request.getParameter("queryid"));
        queryIdOptinal.ifPresent(queryId -> {
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
            Optional<String> applicationIdOptional = YarnUtil.getApplicationId(resourceManagerUrl, queryId);
            applicationIdOptional.ifPresent(applicationId -> {
                try {
                    String json = YarnUtil.kill(resourceManagerUrl, applicationId);
                    response.setContentType("application/json");
                    PrintWriter writer = response.getWriter();
                    writer.println(json);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        });

    }

}
