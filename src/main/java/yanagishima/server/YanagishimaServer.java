package yanagishima.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Properties;

import javax.servlet.DispatcherType;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

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
		
		Properties properties = loadProps(args, new OptionParser());
		int jettyPort = Integer.parseInt(properties.getProperty("jetty.port"));
		String webResourceDir = properties.getProperty("web.resource.dir");
		String prestoCoordinatorServer = properties.getProperty("presto.coordinator.server");
		String catalog = properties.getProperty("catalog");
		String schema = properties.getProperty("schema");
		String user = properties.getProperty("user");
		String source = properties.getProperty("source");
		
		PrestoServiceModule prestoServiceModule = new PrestoServiceModule(jettyPort, webResourceDir, prestoCoordinatorServer, catalog, schema, user, source);
		PrestoServletModule prestoServletModule = new PrestoServletModule();
		@SuppressWarnings("unused")
		Injector injector = Guice.createInjector(prestoServiceModule,
				prestoServletModule);


		Server server = new Server(jettyPort);

		ServletContextHandler servletContextHandler = new ServletContextHandler(
				server, "/", ServletContextHandler.SESSIONS);
		servletContextHandler.addFilter(GuiceFilter.class, "/*",
				EnumSet.allOf(DispatcherType.class));

		servletContextHandler.addServlet(DefaultServlet.class, "/");
		
		servletContextHandler.setResourceBase(webResourceDir);

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
		LOGGER.info("Yanagishima Server running port " + jettyPort + ".");
	}

	public static Properties loadProps(String[] args, OptionParser parser) {
		OptionSpec<String> configDirectory = parser
				.acceptsAll(Arrays.asList("c", "conf"),
						"The conf directory for Yanagishima.")
				.withRequiredArg().describedAs("conf").ofType(String.class);

		OptionSet options = parser.parse(args);

		if (options.has(configDirectory)) {
			String path = options.valueOf(configDirectory);
			LOGGER.info("Loading yanagishima settings file from " + path);
			File dir = new File(path);
			if (!dir.exists()) {
				throw new RuntimeException("Conf directory " + path
						+ " doesn't exist.");
			} else if (!dir.isDirectory()) {
				throw new RuntimeException("Conf directory " + path
						+ " isn't a directory.");
			} else {
				Properties yanagishimaSettings = loadYanagishimaConfigurationFromDirectory(dir);
				return yanagishimaSettings;
			}
		} else {
			throw new RuntimeException("Conf parameter not set.");
		}

	}

	private static Properties loadYanagishimaConfigurationFromDirectory(File dir) {

		File yanagishimaPropertiesFile = new File(dir, "yanagishima.properties");

		if (yanagishimaPropertiesFile.exists()
				&& yanagishimaPropertiesFile.isFile()) {
			LOGGER.info("Loading yanagishima properties file");
			try (InputStream inputStream = new BufferedInputStream(
					new FileInputStream(yanagishimaPropertiesFile))) {
				Properties properties = new Properties();
				properties.load(inputStream);
				return properties;
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			throw new RuntimeException("yanagishima.properties is not found.");
		}

	}

}
