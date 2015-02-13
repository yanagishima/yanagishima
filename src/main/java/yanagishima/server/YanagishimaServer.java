package yanagishima.server;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import yanagishima.module.PrestoServiceModule;
import yanagishima.module.PrestoServletModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;

public class YanagishimaServer {

	private static Logger LOGGER = LoggerFactory
			.getLogger(YanagishimaServer.class);

	public static void main(String[] args) throws Exception {
		
		PrestoServiceModule prestoServiceModule = new PrestoServiceModule();
		PrestoServletModule prestoServletModule = new PrestoServletModule();
		Injector injector = Guice.createInjector(prestoServiceModule, prestoServletModule);
		
		int port = 8080;
		Server server = new Server(port);

		ServletContextHandler servletContextHandler = new ServletContextHandler(
				server, "/", ServletContextHandler.SESSIONS);
		servletContextHandler.addFilter(GuiceFilter.class, "/*",
				EnumSet.allOf(DispatcherType.class));

		servletContextHandler.addServlet(DefaultServlet.class, "/");
		
		servletContextHandler.setResourceBase("web");

		LOGGER.info("Yanagishima Server started...");

		server.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {

			public void run() {
				LOGGER.info("Shutting down Yanagishima Server...");
				try {
					server.stop();
					server.destroy();
				} catch (Exception e) {
					LOGGER.error(
							"Error while shutting down Yanagishima Server.", e);
				}
			}
		});
		LOGGER.info("Yanagishima Server running port " + port + ".");
	}

}
