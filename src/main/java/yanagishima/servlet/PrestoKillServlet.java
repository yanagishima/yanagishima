package yanagishima.servlet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import yanagishima.config.YanagishimaConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static io.prestosql.client.OkHttpUtil.basicAuth;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrestoKillServlet {
    private final YanagishimaConfig config;
    private final OkHttpClient httpClient = new OkHttpClient();

    @PostMapping("kill")
    public Map<String, Object> post(@RequestParam String datasource,
                                    @RequestParam(name = "queryid", required = false) String queryId,
                                    @RequestParam Optional<String> user,
                                    @RequestParam Optional<String> password,
                                    HttpServletRequest request, HttpServletResponse response) {
		if (queryId == null) {
			return Map.of();
		}

		try {
			if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
				sendForbiddenError(response);
				return Map.of();
			}

			String coordinatorUrl = config.getPrestoCoordinatorServer(datasource);
			String userName = request.getHeader(config.getAuditHttpHeaderName());
			Response killResponse = getKillResponse(coordinatorUrl, queryId, userName, user, password);
			return Map.of("code", killResponse.code(), "message", killResponse.message(), "url", killResponse.request().url());
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return Map.of("error", e.getMessage());
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
