package yanagishima.server;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class PrestoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final PrestoService prestoService;

	@Inject
	public PrestoServlet(PrestoService prestoService) {
		this.prestoService = prestoService;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		String sql = "SELECT * FROM sys.node";

		List<String> list = prestoService.doQuery(sql);
		for (String result : list) {
			resp.getWriter().println(result);
		}

	}

}
