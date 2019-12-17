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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import yanagishima.config.YanagishimaConfig;

@Singleton
public class QueryStatusServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryStatusServlet.class);
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
		Request prestoRequest = new Request.Builder().url(coordinatorServer + "/v1/query/" + queryId).build();
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
			LOGGER.error(e.getMessage(), e);
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
			Builder builder = httpClient.newBuilder();
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
