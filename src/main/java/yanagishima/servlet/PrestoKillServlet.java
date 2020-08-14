package yanagishima.servlet;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static io.prestosql.client.OkHttpUtil.basicAuth;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;
import static yanagishima.util.HttpRequestUtil.getRequiredParameter;
import static yanagishima.util.JsonUtil.writeJSON;

@Singleton
public class PrestoKillServlet extends HttpServlet {
	private static final Logger LOGGER = LoggerFactory.getLogger(PrestoKillServlet.class);
	private static final long serialVersionUID = 1L;

	private final YanagishimaConfig config;
	private final OkHttpClient httpClient = new OkHttpClient();

	@Inject
	public PrestoKillServlet(YanagishimaConfig config) {
		this.config = config;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Optional<String> queryIdOptinal = Optional.ofNullable(request.getParameter("queryid"));
		if (queryIdOptinal.isEmpty()) {
			writeJSON(response, Map.of());
			return;
		}

		try {
			String datasource = getRequiredParameter(request, "datasource");
			if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
				sendForbiddenError(response);
				return;
			}

			String coordinatorUrl = config.getPrestoCoordinatorServer(datasource);
			Optional<String> user = Optional.ofNullable(request.getParameter("user"));
			Optional<String> password = Optional.ofNullable(request.getParameter("password"));
			String userName = request.getHeader(config.getAuditHttpHeaderName());
			Response killResponse = getKillResponse(coordinatorUrl, queryIdOptinal.get(), userName, user, password);
			writeJSON(response, Map.of("code", killResponse.code(), "message", killResponse.message(), "url", killResponse.request().url()));
		} catch (Throwable e) {
			LOGGER.error(e.getMessage(), e);
			writeJSON(response, Map.of("error", e.getMessage()));
		}
	}

	private Response getKillResponse(String coordinatorUrl, String queryId, String userName, Optional<String> user, Optional<String> password) throws IOException {
		okhttp3.Request request;
		if (userName == null) {
			request = new okhttp3.Request.Builder().url(coordinatorUrl + "/v1/query/" + queryId).delete().build();
		} else {
			request = new okhttp3.Request.Builder().url(coordinatorUrl + "/v1/query/" + queryId).addHeader("X-Presto-User", userName).delete().build();
		}
		if (user.isPresent() && password.isPresent()) {
			OkHttpClient.Builder clientBuilder = httpClient.newBuilder();
			clientBuilder.addInterceptor(basicAuth(user.get(), password.get()));
			return clientBuilder.build().newCall(request).execute();
		}
		return httpClient.newCall(request).execute();
	}
}
