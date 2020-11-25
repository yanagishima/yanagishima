package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import io.prestosql.sql.parser.ParsingException;
import io.prestosql.sql.parser.ParsingOptions;
import io.prestosql.sql.parser.SqlParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.exception.QueryErrorException;
import yanagishima.result.PrestoQueryResult;
import yanagishima.service.PrestoService;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.prestosql.client.OkHttpUtil.basicAuth;
import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.Constants.YANAGISHIMA_COMMENT;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class CheckPrestoQueryServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckPrestoQueryServlet.class);
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PrestoService prestoService;
    private final YanagishimaConfig config;
    private final OkHttpClient httpClient = new OkHttpClient();

    @Inject
    public CheckPrestoQueryServlet(PrestoService prestoService, YanagishimaConfig config) {
        this.prestoService = prestoService;
        this.config = config;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> responseBody = new HashMap<>();
        String query = request.getParameter("query");
        if (query == null) {
            writeJSON(response, responseBody);
            return;
        }

        String datasource = getRequiredParameter(request, "datasource");
        if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
            sendForbiddenError(response);
            return;
        }

        try {
            new SqlParser().createStatement(query, new ParsingOptions());
        } catch (ParsingException e) {
            responseBody.put("error", e.getMessage());
            responseBody.put("errorLineNumber", e.getLineNumber());
            responseBody.put("errorColumnNumber", e.getColumnNumber());
            writeJSON(response, responseBody);
            return;
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
            return;
        }

        try {
            String user = getUsername(request);
            Optional<String> prestoUser = Optional.ofNullable(request.getParameter("user"));
            Optional<String> prestoPassword = Optional.ofNullable(request.getParameter("password"));
            String explainQuery = format("%sEXPLAIN ANALYZE\n%s", YANAGISHIMA_COMMENT, query);
            String queryId = null;
            try {
                PrestoQueryResult prestoQueryResult = prestoService.doQuery(datasource, explainQuery, user, prestoUser, prestoPassword, false, Integer.MAX_VALUE);
                queryId = prestoQueryResult.getQueryId();
            } catch (QueryErrorException e) {
                LOGGER.error(e.getMessage(), e);
                queryId = e.getQueryId();
            }

            String coordinatorServer = config.getPrestoCoordinatorServer(datasource);
            Request prestoRequest = new Request.Builder().url(coordinatorServer + "/v1/query/" + queryId).build();
            try (Response prestoResponse = buildClient(request).newCall(prestoRequest).execute()) {
                if (prestoResponse.isSuccessful() && prestoResponse.body() != null) {
                    String json = prestoResponse.body().string();
                    Map status = OBJECT_MAPPER.readValue(json, Map.class);
                    String state = (String)status.get("state");
                    if (state.equals("FAILED")) {
                        Map failureInfo = (Map)status.get("failureInfo");
                        if (failureInfo != null) {
                            //line 2:8: Column 'foo' cannot be resolved
                            String message = (String)failureInfo.get("message");
                            List<String> strings = Splitter.on(' ').splitToList(message);
                            String prefix = strings.get(0) + " " + strings.get(1) + " ";
                            responseBody.put("error", message.substring(prefix.length()));
                            Map errorLocation = (Map)failureInfo.get("errorLocation");
                            if (errorLocation != null) {
                                int lineNumber = (Integer)errorLocation.get("lineNumber");
                                int columnNumber = (Integer)errorLocation.get("columnNumber");
                                responseBody.put("errorLineNumber", lineNumber - 1);
                                responseBody.put("errorColumnNumber", columnNumber);
                            }
                        }
                    } else if (state.equals("FINISHED")) {
                        Map queryStats = (Map) status.get("queryStats");
                        responseBody.put("physicalInputDataSize", queryStats.get("physicalInputDataSize"));
                        responseBody.put("physicalInputPositions", queryStats.get("physicalInputPositions"));
                    } else {
                        throw new IllegalArgumentException("Illegal state: " + state);
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            responseBody.put("error", e.getMessage());
        }
        writeJSON(response, responseBody);
    }

    private OkHttpClient buildClient(HttpServletRequest request) {
        String user = request.getParameter("user");
        String password = request.getParameter("password");
        if (user != null && password != null) {
            OkHttpClient.Builder builder = httpClient.newBuilder();
            builder.addInterceptor(basicAuth(user, password));
            return builder.build();
        }
        return httpClient;
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
