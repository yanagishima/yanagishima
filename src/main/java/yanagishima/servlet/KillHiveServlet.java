package yanagishima.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.pool.StatementPool;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class KillHiveServlet extends HttpServlet {

    private static Logger LOGGER = LoggerFactory.getLogger(KillHiveServlet.class);

    private static final long serialVersionUID = 1L;

    private YanagishimaConfig yanagishimaConfig;

    @Inject
    private StatementPool statementPool;

    @Inject
    public KillHiveServlet(YanagishimaConfig yanagishimaConfig) {
        this.yanagishimaConfig = yanagishimaConfig;
    }

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {

        Optional<String> idOptinal = Optional.ofNullable(request.getParameter("id"));
        idOptinal.ifPresent(id -> {
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

            String resourceManagerUrl = yanagishimaConfig.getResourceManagerUrl(datasource);
            String userName = null;
            Optional<String> hiveUser = Optional.ofNullable(request.getParameter("user"));
            if(yanagishimaConfig.isUseAuditHttpHeaderName()) {
                userName = request.getHeader(yanagishimaConfig.getAuditHttpHeaderName());
            } else {
                if (hiveUser.isPresent()) {
                    userName = hiveUser.get();
                }
            }
            if (id.startsWith("application_")) {
                try {
                    String json = YarnUtil.kill(resourceManagerUrl, id);
                    response.setContentType("application/json");
                    PrintWriter writer = response.getWriter();
                    writer.println(json);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                if(yanagishimaConfig.isUseJdbcCancel(datasource)) {
                    LOGGER.info(String.format("killing %s in %s by Statement#cancel", id, datasource));
                    try(Statement statement = statementPool.getStatement(datasource, id)) {
                        if(statement == null) {
                            LOGGER.error("statement is null");
                        } else {
                            statement.cancel();
                        }
                        statementPool.removeStatement(datasource, id);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Optional<Map> applicationOptional = YarnUtil.getApplication(resourceManagerUrl, id, userName, yanagishimaConfig.getResourceManagerBegin(datasource));
                    applicationOptional.ifPresent(application -> {
                        String applicationId = (String) application.get("id");
                        try {
                            String json = YarnUtil.kill(resourceManagerUrl, applicationId);
                            response.setContentType("application/json");
                            PrintWriter writer = response.getWriter();
                            writer.println(json);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        });

    }

}
