package yanagishima.servlet;

import static io.prestosql.client.OkHttpUtil.basicAuth;
import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import yanagishima.config.YanagishimaConfig;

@Slf4j
@Singleton
public class QueryStatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final YanagishimaConfig config;
	private final OkHttpClient httpClient = new OkHttpClient();

	@Inject
	public QueryStatusServlet(YanagishimaConfig config) {
		this.config = config;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String datasource = getRequiredParameter(request, "datasource");
		if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
			sendForbiddenError(response);
			return;
		}

		String queryId = request.getParameter("queryid");
		String coordinatorServer = config.getPrestoCoordinatorServer(datasource);
		String json;
		String userName = request.getHeader(config.getAuditHttpHeaderName());
		Request prestoRequest;
		if (userName == null) {
			prestoRequest = new Request.Builder().url(coordinatorServer + "/v1/query/" + queryId).build();
		} else {
			prestoRequest = new Request.Builder().url(coordinatorServer + "/v1/query/" + queryId).addHeader("X-Presto-User", userName).build();
		}
		try (Response prestoResponse = buildClient(request).newCall(prestoRequest).execute()) {
			if (!prestoResponse.isSuccessful() || prestoResponse.body() == null) {
				writeJSON(response, toMap(prestoResponse.code(), prestoResponse.message()));
				return;
			}
			json = prestoResponse.body().string();
		}

		Map status = new HashMap();
		try {
			status = OBJECT_MAPPER.readValue(json, Map.class);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			status.put("state", "FAILED");
			status.put("failureInfo", "");
		}
		status.remove("outputStage");
		status.remove("session");
		writeJSON(response, status);
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

	private Map<String, String> toMap(int code, String message) {
		return Map.of(
				"state", "FAILED",
				"failureInfo", "",
				"error", format("code=%d, message=%s", code, message));
	}
}
