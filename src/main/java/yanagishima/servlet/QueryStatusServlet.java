package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;
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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

@Singleton
public class QueryStatusServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	private static final int LIMIT = 100;

	@Inject
	public QueryStatusServlet(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doGet(HttpServletRequest request,
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
		String prestoCoordinatorServer = yanagishimaConfig
				.getPrestoCoordinatorServer(datasource);
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		String json = Request.Get(prestoCoordinatorServer + "/v1/query/" + queryid)
				.execute().returnContent().asString(StandardCharsets.UTF_8);
		ObjectMapper mapper = new ObjectMapper();
		Map map = mapper.readValue(json, Map.class);
		if(map.containsKey("outputStage")) {
			map.remove("outputStage");
		}
		writer.println(mapper.writeValueAsString(map));
	}

}
