package yanagishima.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

@Singleton
public class PrestoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private final PrestoService prestoService;

	@Inject
	public PrestoServlet(PrestoService prestoService) {
		this.prestoService = prestoService;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HashMap<String, Object> retVal = new HashMap<String, Object>();
		String query = getParam(request, "query");
		List<String> list = prestoService.doQuery(query);
		retVal.put("results", list);
		writeJSON(response, retVal);

	}

	private void writeJSON(HttpServletResponse resp, Object obj)
			throws IOException {
		resp.setContentType("application/json");
		ObjectMapper mapper = new ObjectMapper();
		OutputStream stream = resp.getOutputStream();
		mapper.writeValue(stream, obj);
	}

	private String getParam(HttpServletRequest request, String name)
			throws ServletException {
		String p = request.getParameter(name);
		if (p == null)
			throw new ServletException("Missing required parameter '" + name
					+ "'.");
		else
			return p;
	}

}
