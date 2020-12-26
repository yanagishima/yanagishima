package yanagishima.servlet;

import static java.lang.String.format;
import static yanagishima.util.AccessControlUtil.sendForbiddenError;
import static yanagishima.util.AccessControlUtil.validateDatasource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import yanagishima.client.presto.PrestoClient;
import yanagishima.config.YanagishimaConfig;

@Slf4j
@RestController
@RequiredArgsConstructor
public class QueryStatusServlet {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final YanagishimaConfig config;

    @PostMapping("queryStatus")
    public Map<?, ?> post(@RequestParam String datasource,
                          @RequestParam(name = "queryid", required = false) String queryId,
                          @RequestParam Optional<String> user,
                          @RequestParam Optional<String> password,
                          HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (config.isCheckDatasource() && !validateDatasource(request, datasource)) {
			sendForbiddenError(response);
			return Map.of();
		}

		String coordinatorServer = config.getPrestoCoordinatorServer(datasource);
		String json;
		String userName = request.getHeader(config.getAuditHttpHeaderName());
		try (Response prestoResponse = new PrestoClient(coordinatorServer, userName, user, password).get(queryId)) {
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

	private Map<String, String> toMap(int code, String message) {
		return Map.of(
				"state", "FAILED",
				"failureInfo", "",
				"error", format("code=%d, message=%s", code, message));
	}
}
