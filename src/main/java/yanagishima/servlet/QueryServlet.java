package yanagishima.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yanagishima.config.YanagishimaConfig;

@Singleton
public class QueryServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(QueryServlet.class);

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	@Inject
	public QueryServlet(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String prestoCoordinatorServer = yanagishimaConfig
				.getPrestoCoordinatorServer();
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		writer.println(Request.Get(prestoCoordinatorServer + "/v1/query")
				.execute().returnContent().asString());

	}

}
