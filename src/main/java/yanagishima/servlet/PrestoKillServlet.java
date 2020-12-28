package yanagishima.servlet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import yanagishima.client.presto.PrestoClient;
import yanagishima.config.YanagishimaConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;

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
			Response killResponse = new PrestoClient(coordinatorUrl, userName, user, password).kill(queryId);
			return Map.of("code", killResponse.code(), "message", killResponse.message(), "url", killResponse.request().url());
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
			return Map.of("error", e.getMessage());
		}
	}
}
