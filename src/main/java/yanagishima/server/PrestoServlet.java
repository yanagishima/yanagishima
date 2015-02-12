package yanagishima.server;

import java.io.IOException;
import java.util.List;

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

@Singleton
public class PrestoServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_MIME_TYPE = "text/html";

	private final PrestoService prestoService;

	@Inject
	public PrestoServlet(PrestoService prestoService) {
		this.prestoService = prestoService;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String query = request.getParameter("query");
		
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty("resource.loader", "classpath, jar");
		engine.setProperty("classpath.resource.loader.class",
				ClasspathResourceLoader.class.getName());
		engine.setProperty("jar.resource.loader.class",
				JarResourceLoader.class.getName());
		String template = "yanagishima/velocity/index.vm";
		VelocityContext context = new VelocityContext();
		
		context.put("context", request.getContextPath());
		
		if(query != null) {
			List<String> list = prestoService.doQuery(query);
			context.put("test", list.get(0));
		}
		
		
		response.setHeader("Content-type", "text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType(DEFAULT_MIME_TYPE);
		engine.mergeTemplate(template, "UTF-8", context, response.getWriter());

	}

}
