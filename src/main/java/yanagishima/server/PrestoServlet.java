package yanagishima.server;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.JarResourceLoader;
import org.codehaus.jackson.map.ObjectMapper;

@Singleton
public class PrestoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_MIME_TYPE = "text/html";
	
	public static final String JSON_MIME_TYPE = "application/json";

	private final PrestoService prestoService;

	@Inject
	public PrestoServlet(PrestoService prestoService) {
		this.prestoService = prestoService;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		if (hasParam(request, "ajax")) {
			HashMap<String, Object> retVal = new HashMap<String, Object>();
			String query = getParam(request, "query");
			List<String> list = prestoService.doQuery(query);
			retVal.put("results", list);
			writeJSON(response, retVal);
		} else {
			VelocityEngine engine = new VelocityEngine();
			engine.setProperty("resource.loader", "classpath, jar");
			engine.setProperty("classpath.resource.loader.class",
					ClasspathResourceLoader.class.getName());
			engine.setProperty("jar.resource.loader.class",
					JarResourceLoader.class.getName());
			String template = "yanagishima/velocity/index.vm";
			VelocityContext context = new VelocityContext();

			context.put("context", request.getContextPath());

			response.setHeader("Content-type", "text/html; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.setContentType(DEFAULT_MIME_TYPE);
			engine.mergeTemplate(template, "UTF-8", context,
					response.getWriter());
		}

	}
	
	private void writeJSON(HttpServletResponse resp, Object obj)
			throws IOException {
		resp.setContentType(JSON_MIME_TYPE);
		ObjectMapper mapper = new ObjectMapper();
		OutputStream stream = resp.getOutputStream();
		mapper.writeValue(stream, obj);
	}

	private boolean hasParam(HttpServletRequest request, String param) {
		return request.getParameter(param) != null;
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
