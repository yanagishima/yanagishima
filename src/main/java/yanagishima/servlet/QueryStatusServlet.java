package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Response;
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
import java.util.Map;
import java.util.Optional;

import static com.facebook.presto.client.OkHttpUtil.basicAuth;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class QueryStatusServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	private OkHttpClient httpClient = new OkHttpClient();

	@Inject
	public QueryStatusServlet(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doGet(HttpServletRequest request,
						 HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
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
		Optional<String> prestoUser = Optional.ofNullable(request.getParameter("presto_user"));
		Optional<String> prestoPassword = Optional.ofNullable(request.getParameter("presto_password"));
		if (prestoUser.isPresent() && prestoPassword.isPresent()) {
			OkHttpClient.Builder clientBuilder = httpClient.newBuilder();
			clientBuilder.addInterceptor(basicAuth(prestoUser.get(), prestoPassword.get()));
			try (Response prestoResponse = clientBuilder.build().newCall(prestoRequest).execute()) {
				json = prestoResponse.body().string();
			}
		} else {
			try (Response prestoResponse = httpClient.newCall(prestoRequest).execute()) {
				json = prestoResponse.body().string();
			}
		}

		ObjectMapper mapper = new ObjectMapper();
		Map map = mapper.readValue(json, Map.class);
		if(map.containsKey("outputStage")) {
			map.remove("outputStage");
		}
		if(map.containsKey("session")) {
			map.remove("session");
		}
		writer.println(mapper.writeValueAsString(map));
	}

}
