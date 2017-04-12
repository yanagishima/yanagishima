package yanagishima.servlet;

import org.apache.http.client.fluent.Request;
import yanagishima.config.YanagishimaConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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

		String datasource = Optional.ofNullable(request.getParameter("datasource")).get();
		String queryid = Optional.ofNullable(request.getParameter("queryid")).get();
		String prestoCoordinatorServer = yanagishimaConfig
				.getPrestoCoordinatorServer(datasource);
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		String json = Request.Get(prestoCoordinatorServer + "/v1/query/" + queryid)
				.execute().returnContent().asString(StandardCharsets.UTF_8);
		writer.println(json);
	}

}
