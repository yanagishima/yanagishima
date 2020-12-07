package yanagishima.servlet;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Statement;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.pool.StatementPool;
import yanagishima.util.YarnUtil;

@Slf4j
@Singleton
public class KillHiveServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final StatementPool statements;
    private final YanagishimaConfig config;

    @Inject
    public KillHiveServlet(StatementPool statements, YanagishimaConfig config) {
        this.statements = statements;
        this.config = config;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id == null) {
            return;
        }

        String datasource = getRequiredParameter(request, "datasource");
        if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
            sendForbiddenError(response);
            return;
        }

        String resourceManagerUrl = config.getResourceManagerUrl(datasource);
        if (id.startsWith("application_")) {
            killApplication(response, resourceManagerUrl, id);
            return;
        }
        if (config.isUseJdbcCancel(datasource)) {
            log.info(format("killing %s in %s by Statement#cancel", id, datasource));
            try (Statement statement = statements.getStatement(datasource, id)) {
                if (statement == null) {
                    log.error("statement is null");
                } else {
                    statement.cancel();
                }
                statements.removeStatement(datasource, id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        YarnUtil.getApplication(resourceManagerUrl, id, getUsername(request), config.getResourceManagerBegin(datasource)).ifPresent(application -> {
            String applicationId = (String) application.get("id");
            killApplication(response, resourceManagerUrl, applicationId);
        });
    }

    private String getUsername(HttpServletRequest request) {
        if (config.isUseAuditHttpHeaderName()) {
            return request.getHeader(config.getAuditHttpHeaderName());
        }
        return request.getParameter("user");
    }

    private static void killApplication(HttpServletResponse response, String resourceManagerUrl, String id) {
        try {
            String json = YarnUtil.kill(resourceManagerUrl, id);
            response.setContentType("application/json");
            PrintWriter writer = response.getWriter();
            writer.println(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
