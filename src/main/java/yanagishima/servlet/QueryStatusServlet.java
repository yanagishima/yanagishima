package yanagishima.servlet;

import static io.prestosql.client.OkHttpUtil.basicAuth;
import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import yanagishima.config.YanagishimaConfig;

@Slf4j
@RestController
@RequiredArgsConstructor
public class QueryStatusServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final YanagishimaConfig config;
    private final OkHttpClient httpClient = new OkHttpClient();

    @PostMapping("queryStatus")
    public Map<?, ?> post(@RequestParam String datasource,
                          @RequestParam(name = "queryid", required = false) String queryId,
                          HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
			sendForbiddenError(response);
			return Map.of();
		}

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
				return toMap(prestoResponse.code(), prestoResponse.message());
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
		return status;
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
