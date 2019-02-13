package yanagishima.servlet;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.util.AccessControlUtil;
import yanagishima.util.HttpRequestUtil;
import yanagishima.util.JsonUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

import static com.facebook.presto.client.OkHttpUtil.basicAuth;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class KillServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(KillServlet.class);

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	private OkHttpClient httpClient = new OkHttpClient();

	@Inject
	public KillServlet(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HashMap<String, Object> retVal = new HashMap<String, Object>();

		Optional<String> queryIdOptinal = Optional.ofNullable(request.getParameter("queryid"));
		queryIdOptinal.ifPresent(queryId -> {
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
			try {
				String prestoCoordinatorServer = yanagishimaConfig.getPrestoCoordinatorServer(datasource);
				okhttp3.Request prestoRequest = new okhttp3.Request.Builder().url(prestoCoordinatorServer + "/v1/query/" + queryId).delete().build();
				Optional<String> prestoUser = Optional.ofNullable(request.getParameter("user"));
				Optional<String> prestoPassword = Optional.ofNullable(request.getParameter("password"));
				Response prestoResponse = null;
				if (prestoUser.isPresent() && prestoPassword.isPresent()) {
					OkHttpClient.Builder clientBuilder = httpClient.newBuilder();
					clientBuilder.addInterceptor(basicAuth(prestoUser.get(), prestoPassword.get()));
					try {
						prestoResponse = clientBuilder.build().newCall(prestoRequest).execute();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				} else {
					try {
						prestoResponse = httpClient.newCall(prestoRequest).execute();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				retVal.put("code", prestoResponse.code());
				retVal.put("message", prestoResponse.message());
				retVal.put("url", prestoResponse.request().url());
			} catch (Throwable e) {
				LOGGER.error(e.getMessage(), e);
				retVal.put("error", e.getMessage());
			}

			JsonUtil.writeJSON(response, retVal);

		});

	}

}
