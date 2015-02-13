package yanagishima.servlet;

import java.io.IOException;

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
public class IndexServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

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
		response.setContentType("text/html");
		engine.mergeTemplate(template, "UTF-8", context, response.getWriter());

	}

}
