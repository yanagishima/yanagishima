package yanagishima.servlet;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getOrDefaultParameter;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.HiveQueryErrorException;
import yanagishima.result.HiveQueryResult;
import yanagishima.service.HiveService;

@Slf4j
@Singleton
public class HiveServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final YanagishimaConfig config;
    private final HiveService hiveService;

    @Inject
    public HiveServlet(YanagishimaConfig config, HiveService hiveService) {
        this.config = config;
        this.hiveService = hiveService;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> reponseBody = new HashMap<>();
        String query = request.getParameter("query");
        if (query == null) {
            writeJSON(response, reponseBody);
            return;
        }

        try {
            String user = getUsername(request);
            if (config.isUserRequired() && user == null) {
                sendForbiddenError(response);
                return;
            }

            String datasource = getRequiredParameter(request, "datasource");
            if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
                sendForbiddenError(response);
                return;
            }
            String engine = getRequiredParameter(request, "engine");
            if (user != null) {
                log.info(format("%s executed %s in datasource=%s, engine=%s", user, query, datasource, engine));
            }

            Optional<String> hiveUser = Optional.ofNullable(request.getParameter("user"));
            Optional<String> hivePassword = Optional.ofNullable(request.getParameter("password"));
            boolean storeFlag = getOrDefaultParameter(request, "store", false);

            HiveQueryResult queryResult = hiveService.doQuery(engine, datasource, query, user, hiveUser, hivePassword, storeFlag, config.getSelectLimit());
            reponseBody.put("queryid", queryResult.getQueryId());
            reponseBody.put("headers", queryResult.getColumns());
            reponseBody.put("lineNumber", Integer.toString(queryResult.getLineNumber()));
            reponseBody.put("rawDataSize", queryResult.getRawDataSize().toString());
            reponseBody.put("results", queryResult.getRecords());
            // TODO: Make HiveQueryResult.warningMessage Optioanl<String>
            Optional.ofNullable(queryResult.getWarningMessage()).ifPresent(warningMessage -> reponseBody.put("warn", warningMessage));

            // Query specific operations
            if (query.startsWith("SHOW SCHEMAS")) {
                reponseBody.put("results", queryResult.getRecords().stream()
                                                      .filter(list -> !config.getInvisibleDatabases(datasource).contains(list.get(0)))
                                                      .collect(Collectors.toList()));
            }
        } catch (Throwable e) {
            if (e instanceof HiveQueryErrorException) {
                reponseBody.put("queryid", ((HiveQueryErrorException) e).getQueryId());
            }

            log.error(e.getMessage(), e);
            reponseBody.put("error", e.getMessage());
        }
        writeJSON(response, reponseBody);
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

    private static String toTableName(String engine, String tableName) {
        if ("hive".equals(engine)) {
            return tableName.substring(1, tableName.length() - 1);
        }
        if ("spark".equals(engine)) {
            return tableName;
        }
        throw new IllegalArgumentException("Illegal engine: " + engine);
    }
}
