package yanagishima.servlet;

import static java.lang.String.format;
import static java.lang.String.join;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prestosql.client.ClientException;
import yanagishima.config.YanagishimaConfig;
import yanagishima.service.PrestoService;

@Singleton
public class TableListServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(TableListServlet.class);
    private static final long serialVersionUID = 1L;

    private final PrestoService prestoService;
    private final YanagishimaConfig config;

    @Inject
    public TableListServlet(PrestoService prestoService, YanagishimaConfig config) {
        this.prestoService = prestoService;
        this.config = config;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> responseBody = new HashMap<>();
        try {
            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }

            String coordinatorServer = config.getPrestoCoordinatorServerOrNull(datasource);
            if (coordinatorServer == null) {
                writeJSON(response, responseBody);
                return;
            }

            String catalog = getRequiredParameter(request, "catalog");
            String userName = getUsername(request);
            Optional<String> prestoUser = Optional.ofNullable(request.getParameter("user"));
            Optional<String> prestoPassword = Optional.ofNullable(request.getParameter("password"));
            if (prestoUser.isPresent() && prestoPassword.isPresent() && prestoUser.get().isEmpty()) {
                responseBody.put("error", "user is empty");
                writeJSON(response, responseBody);
                return;
            }
            List<String> invisibleSchemas = config.getInvisibleSchemas(datasource, catalog);
            String query = format("%sSELECT table_catalog || '.' || table_schema || '.' || table_name "
                                  + "FROM %s.information_schema.tables "
                                  + "WHERE table_schema NOT IN ('%s')",
                                  YANAGISHIMA_COMMENT, catalog, join("','", invisibleSchemas));
            try {
                List<String> tables = prestoService.doQuery(datasource, query, userName, prestoUser, prestoPassword, false, Integer.MAX_VALUE).getRecords().stream()
                                                   .map(list -> list.get(0))
                                                   .collect(Collectors.toList());
                responseBody.put("tableList", tables);
            } catch (ClientException e) {
                if (prestoUser.isPresent()) {
                    LOGGER.error(format("%s failed to be authenticated", prestoUser.get()));
                }
                LOGGER.error(e.getMessage(), e);
                responseBody.put("error", e.getMessage());
            } catch (Throwable e) {
                LOGGER.error(e.getMessage(), e);
                responseBody.put("error", e.getMessage());
            }

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

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
