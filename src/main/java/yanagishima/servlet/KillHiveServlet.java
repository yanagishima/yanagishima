package yanagishima.servlet;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.pool.StatementPool;
import yanagishima.util.YarnUtil;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KillHiveServlet {
    private final StatementPool statements;
    private final YanagishimaConfig config;

    @PostMapping("killHive")
    public void post(@RequestParam(required = false) String id,
                     @RequestParam String datasource,
                     HttpServletRequest request, HttpServletResponse response) {
        if (id == null) {
            return;
        }

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
