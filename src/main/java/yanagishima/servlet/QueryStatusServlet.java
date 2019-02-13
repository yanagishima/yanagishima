package yanagishima.servlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.facebook.presto.client.OkHttpUtil.basicAuth;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class QueryStatusServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(QueryStatusServlet.class);

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	private OkHttpClient httpClient = new OkHttpClient();

	@Inject
	public QueryStatusServlet(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String datasource = HttpRequestUtil.getParam(request, "datasource");
		if(yanagishimaConfig.isCheckDatasource()) {
			if(!AccessControlUtil.validateDatasource(request, datasource)) {
				try {
					response.sendError(SC_FORBIDDEN);
					return;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		String queryid = Optional.ofNullable(request.getParameter("queryid")).get();
		String prestoCoordinatorServer = yanagishimaConfig.getPrestoCoordinatorServer(datasource);
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		String json = null;
		okhttp3.Request prestoRequest = new okhttp3.Request.Builder().url(prestoCoordinatorServer + "/v1/query/" + queryid).build();
		Optional<String> prestoUser = Optional.ofNullable(request.getParameter("user"));
		Optional<String> prestoPassword = Optional.ofNullable(request.getParameter("password"));
		if (prestoUser.isPresent() && prestoPassword.isPresent()) {
			OkHttpClient.Builder clientBuilder = httpClient.newBuilder();
			clientBuilder.addInterceptor(basicAuth(prestoUser.get(), prestoPassword.get()));
			try (Response prestoResponse = clientBuilder.build().newCall(prestoRequest).execute()) {
				if(prestoResponse.isSuccessful()) {
					json = prestoResponse.body().string();
				} else {
					writer.println(getError(prestoResponse.code(), prestoResponse.message()));
					return;
				}
			}
		} else {
			try (Response prestoResponse = httpClient.newCall(prestoRequest).execute()) {
				if(prestoResponse.isSuccessful()) {
					json = prestoResponse.body().string();
				} else {
					writer.println(getError(prestoResponse.code(), prestoResponse.message()));
					return;
				}
			}
		}

		Map map = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			map = mapper.readValue(json, Map.class);
			if(map.containsKey("outputStage")) {
				map.remove("outputStage");
			}
			if(map.containsKey("session")) {
				map.remove("session");
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			map = new HashMap();
			map.put("state", "FAILED");
			map.put("failureInfo", "");
		}
		writer.println(mapper.writeValueAsString(map));
	}

	private String getError(int code, String message) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("state", "FAILED");
			map.put("failureInfo", "");
			map.put("error", String.format("code=%d, message=%s", code, message));
			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
