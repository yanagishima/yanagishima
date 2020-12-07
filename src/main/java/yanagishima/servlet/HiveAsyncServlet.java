package yanagishima.servlet;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.service.HiveService;

@Slf4j
@Singleton
public class HiveAsyncServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final HiveService hiveService;

    @Inject
    public HiveAsyncServlet(YanagishimaConfig config, HiveService hiveService) {
        this.config = config;
        this.hiveService = hiveService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter("query");
        if (query == null) {
            writeJSON(response, Map.of());
            return;
        }

        Map<String, Object> responseBody = new HashMap<>();
        try {
            String userName = getUsername(request);
            if (config.isUserRequired() && userName == null) {
                sendForbiddenError(response);
                return;
            }

            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }
            String engine = getRequiredParameter(request, "engine");
            if (userName != null) {
                log.info(format("%s executed %s in datasource=%s, engine=%s", userName, query, datasource, engine));
            }

            Optional<String> hiveUser = Optional.ofNullable(request.getParameter("user"));
            Optional<String> hivePassword = Optional.ofNullable(request.getParameter("password"));
            String queryId = hiveService.doQueryAsync(engine, datasource, query, userName, hiveUser, hivePassword);
            responseBody.put("queryid", queryId);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    @Nullable
    private String getUsername(HttpServletRequest request) {
        if (config.isUseAuditHttpHeaderName()) {
            return request.getHeader(config.getAuditHttpHeaderName());
        }

        String user = request.getParameter("user");
        String password = request.getParameter("password");
        if (user != null && password != null) {
            return user;
        }
        return null;
    }
}
