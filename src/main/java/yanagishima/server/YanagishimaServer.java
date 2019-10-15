package yanagishima.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import me.geso.tinyorm.TinyORM;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;
import yanagishima.filter.YanagishimaFilter;
import yanagishima.module.*;

import javax.servlet.DispatcherType;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Properties;

public class YanagishimaServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(YanagishimaServer.class);
    private static final String PROPERTY_FILENAME = "yanagishima.properties";

    public static void main(String[] args) throws Exception {
        Properties properties = loadProperties(args, new OptionParser());
        YanagishimaConfig config = new YanagishimaConfig(properties);

        Injector injector = createInjector(properties);

        createTables(injector.getInstance(TinyORM.class), config.getDatabaseType());

        Server server = new Server(config.getServerPort());
        server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", -1);

        ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        servletContextHandler.addFilter(new FilterHolder(new YanagishimaFilter(config.corsEnabled(), config.getAuditHttpHeaderName())), "/*", EnumSet.of(DispatcherType.REQUEST));
        servletContextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.addServlet(new ServletHolder(new ServletContainer(new YanagishimaResourceConfig(config))), "/*");
        servletContextHandler.addServlet(DefaultServlet.class, "/"); // The default servlet for "/" path should be added lastly
        servletContextHandler.setResourceBase(properties.getProperty("web.resource.dir", "web"));

        LOGGER.info("Yanagishima Server started...");
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOGGER.info("Shutting down Yanagishima Server...");
                try {
                    server.stop();
                    server.destroy();
                } catch (Exception e) {
                    LOGGER.error("Error while shutting down Yanagishima Server", e);
                }
            }
        });
        LOGGER.info("Yanagishima Server running port " + config.getServerPort());
    }

    private static Injector createInjector(Properties properties) {
        return Guice.createInjector(
                new PrestoServiceModule(properties),
                new PrestoServletModule(),
                new HiveServiceModule(properties),
                new HiveServletModule(),
                new DbModule(),
                new PoolModule(),
                new ElasticsearchServiceModule(properties),
                new ElasticsearchServletModule());
    }

    private static void createTables(TinyORM db, YanagishimaConfig.DatabaseType databaseType) throws SQLException {
        try (Connection connection = db.getConnection();
             Statement statement = connection.createStatement()) {
            switch (databaseType) {
                case SQLITE:
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS query (datasource text, engine text, query_id text, fetch_result_time_string text, query_string text, user text, status text, elapsed_time_millis integer, result_file_size integer, linenumber integer, primary key(datasource, engine, query_id))");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS publish (publish_id text, datasource text, engine text, query_id text, user text, primary key(publish_id))");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS bookmark (bookmark_id integer primary key autoincrement, datasource text, engine text, query text, title text, user text)");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS comment (datasource text, engine text, query_id text, content text, update_time_string text, user text, like_count integer, primary key(datasource, engine, query_id))");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS label (datasource text, engine text, query_id text, label_name text, primary key(datasource, engine, query_id))");
                    break;
                case MYSQL:
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS query (datasource varchar(256), engine varchar(256), query_id varchar(256), fetch_result_time_string varchar(256), query_string mediumtext, user varchar(256), status varchar(256), elapsed_time_millis integer, result_file_size integer, linenumber integer, primary key(datasource, engine, query_id))");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS publish (publish_id varchar(256), datasource varchar(256), engine varchar(256), query_id varchar(256), user varchar(256), primary key(publish_id))");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS bookmark (bookmark_id integer primary key auto_increment, datasource varchar(256), engine varchar(256), query text, title varchar(256), user varchar(256))");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS comment (datasource varchar(256), engine varchar(256), query_id varchar(256), content text, update_time_string varchar(256), user varchar(256), like_count integer, primary key(datasource, engine, query_id))");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS label (datasource varchar(256), engine varchar(256), query_id varchar(256), label_name varchar(256), primary key(datasource, engine, query_id))");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS starred_schema (starred_schema_id integer primary key auto_increment, datasource varchar(256) not null, engine varchar(256) not null, catalog varchar(256) not null, `schema` varchar(256) not null, user varchar(256))");
                    break;
                default:
                    throw new IllegalArgumentException("Illegal database type: " + databaseType);
            }
        }
    }

    private static Properties loadProperties(String[] args, OptionParser parser) throws IOException {
        OptionSpec<String> configDirectory = parser
                .acceptsAll(Arrays.asList("c", "conf"), "The conf directory for Yanagishima")
                .withRequiredArg().describedAs("conf").ofType(String.class);

        OptionSet options = parser.parse(args);
        if (!options.has(configDirectory)) {
            throw new RuntimeException("Conf parameter not set");
        }

        String path = options.valueOf(configDirectory);
        LOGGER.info("Loading yanagishima settings file from " + path);
        File directory = new File(path);
        return loadConfiguration(directory);
    }

    private static Properties loadConfiguration(File directory) throws IOException {
        File propertyFile = new File(directory, PROPERTY_FILENAME);
        if (!propertyFile.exists()) {
            throw new FileNotFoundException(propertyFile.getPath());
        }

        LOGGER.info("Loading yanagishima properties file");
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(propertyFile))) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }
}
