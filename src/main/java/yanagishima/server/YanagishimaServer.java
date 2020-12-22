package yanagishima.server;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.msgpack.core.annotations.VisibleForTesting;

import org.eclipse.jetty.servlet.ServletHolder;
import yanagishima.config.YanagishimaConfig;
import yanagishima.filter.YanagishimaFilter;
import yanagishima.module.*;
import yanagishima.repository.TinyOrm;

import javax.servlet.DispatcherType;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Properties;

@Slf4j
public class YanagishimaServer {
    private static final String PROPERTY_FILENAME = "yanagishima.properties";

    private static final String CONFIG_LOCATION_PACKAGE = "yanagishima.config";

    // Expose to JVM singleton value for sharing with Spring DI
    public static Injector injector;

    public static void main(String[] args) throws Exception {
        Properties properties = loadProperties(args, new OptionParser());
        YanagishimaConfig config = new YanagishimaConfig(properties);

        injector = createInjector(properties);

        createTables(injector.getInstance(TinyOrm.class), config.getDatabaseType());

        Server server = new Server(config.getServerPort());
        server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", -1);

        // Original
        ServletContextHandler servletContextHandler = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        servletContextHandler.addFilter(new FilterHolder(new YanagishimaFilter(config.corsEnabled(), config.getAuditHttpHeaderName())), "/*", EnumSet.of(DispatcherType.REQUEST));
        servletContextHandler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
        servletContextHandler.setResourceBase(properties.getProperty("web.resource.dir", "web"));

        // Spring
        WebApplicationContext webAppContext = getWebApplicationContext();
        DispatcherServlet dispatcherServlet = new DispatcherServlet(webAppContext);
        ServletHolder springServletHolder = new ServletHolder("mvc-dispatcher", dispatcherServlet);
        servletContextHandler.addServlet(springServletHolder, "/*");
        servletContextHandler.addEventListener(new ContextLoaderListener(webAppContext));

        // Default servlet
        ServletHolder defaultServlet = new ServletHolder("default", DefaultServlet.class);
        defaultServlet.setInitParameter("dirAllowed", "true");
        servletContextHandler.addServlet(defaultServlet, "/");

        log.info("Yanagishima Server started...");
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.info("Shutting down Yanagishima Server...");
                try {
                    server.stop();
                    server.destroy();
                } catch (Exception e) {
                    log.error("Error while shutting down Yanagishima Server", e);
                }
            }
        });
        log.info("Yanagishima Server running port " + config.getServerPort());
    }

    private static WebApplicationContext getWebApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation(CONFIG_LOCATION_PACKAGE);
        return context;
    }

    private static Injector createInjector(Properties properties) {
        return Guice.createInjector(
                new PrestoServiceModule(properties),
                new PrestoServletModule(),
                new HiveServiceModule(),
                new HiveServletModule(),
                new DbModule(),
                new PoolModule(),
                new ElasticsearchServiceModule());
    }

    @VisibleForTesting
    public static void createTables(TinyOrm db, YanagishimaConfig.DatabaseType databaseType) throws SQLException {
        try (Connection connection = db.getConnection();
             Statement statement = connection.createStatement()) {
            switch (databaseType) {
                case SQLITE:
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS query ("
                                            + "datasource text, "
                                            + "engine text, "
                                            + "query_id text, "
                                            + "fetch_result_time_string text, "
                                            + "query_string text, "
                                            + "user text, "
                                            + "status text, "
                                            + "elapsed_time_millis integer, "
                                            + "result_file_size integer, "
                                            + "linenumber integer, "
                                            + "primary key(datasource, engine, query_id))");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS publish ("
                                            + "publish_id text, "
                                            + "datasource text, "
                                            + "engine text, "
                                            + "query_id text, "
                                            + "user text, "
                                            + "primary key(publish_id))");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS bookmark ("
                                            + "bookmark_id integer primary key autoincrement, "
                                            + "datasource text, "
                                            + "engine text, "
                                            + "query text, "
                                            + "title text, "
                                            + "user text, "
                                            + "snippet text)");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS comment ("
                                            + "datasource text, "
                                            + "engine text, "
                                            + "query_id text, "
                                            + "content text, "
                                            + "update_time_string text, "
                                            + "user text, "
                                            + "like_count integer, "
                                            + "primary key(datasource, engine, query_id))");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS label ("
                                            + "datasource text, "
                                            + "engine text, "
                                            + "query_id text, "
                                            + "label_name text, "
                                            + "primary key(datasource, engine, query_id))");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS starred_schema ("
                                            + "starred_schema_id integer primary key autoincrement, "
                                            + "datasource text not null, "
                                            + "engine text not null, "
                                            + "catalog text not null, "
                                            + "schema text not null, "
                                            + "user text)");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS session_property ("
                                            + "session_property_id integer primary key autoincrement, "
                                            + "datasource text not null, "
                                            + "engine text not null, "
                                            + "query_id text not null, "
                                            + "session_key text not null, "
                                            + "session_value text not null)");
                    break;
                case MYSQL:
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS query ("
                                            + "datasource varchar(256), "
                                            + "engine varchar(256), "
                                            + "query_id varchar(256), "
                                            + "fetch_result_time_string varchar(256), "
                                            + "query_string mediumtext, "
                                            + "user varchar(256), "
                                            + "status varchar(256), "
                                            + "elapsed_time_millis integer, "
                                            + "result_file_size integer, "
                                            + "linenumber integer, "
                                            + "primary key(datasource, engine, query_id))");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS publish ("
                                            + "publish_id varchar(256), "
                                            + "datasource varchar(256), "
                                            + "engine varchar(256), "
                                            + "query_id varchar(256), "
                                            + "user varchar(256), "
                                            + "primary key(publish_id))");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS bookmark ("
                                            + "bookmark_id integer primary key auto_increment, "
                                            + "datasource varchar(256), "
                                            + "engine varchar(256), "
                                            + "query text, "
                                            + "title varchar(256), "
                                            + "user varchar(256), "
                                            + "snippet varchar(256))");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS comment ("
                                            + "datasource varchar(256), "
                                            + "engine varchar(256), "
                                            + "query_id varchar(256), "
                                            + "content text, "
                                            + "update_time_string varchar(256), "
                                            + "user varchar(256), "
                                            + "like_count integer, "
                                            + "primary key(datasource, engine, query_id))");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS label ("
                                            + "datasource varchar(256), "
                                            + "engine varchar(256), "
                                            + "query_id varchar(256), "
                                            + "label_name varchar(256), "
                                            + "primary key(datasource, engine, query_id))");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS starred_schema ("
                                            + "starred_schema_id integer primary key auto_increment, "
                                            + "datasource varchar(256) not null, "
                                            + "engine varchar(256) not null, "
                                            + "catalog varchar(256) not null, "
                                            + "`schema` varchar(256) not null, "
                                            + "user varchar(256))");
                    statement.executeUpdate(""
                                            + "CREATE TABLE IF NOT EXISTS session_property ("
                                            + "session_property_id integer primary key auto_increment, "
                                            + "datasource varchar(256) not null, "
                                            + "engine varchar(256) not null, "
                                            + "query_id varchar(256) not null, "
                                            + "session_key varchar(256) not null, "
                                            + "session_value varchar(256) not null)");
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
        log.info("Loading yanagishima settings file from " + path);
        File directory = new File(path);
        return loadConfiguration(directory);
    }

    private static Properties loadConfiguration(File directory) throws IOException {
        File propertyFile = new File(directory, PROPERTY_FILENAME);
        if (!propertyFile.exists()) {
            throw new FileNotFoundException(propertyFile.getPath());
        }

        log.info("Loading yanagishima properties file");
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(propertyFile))) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties;
        }
    }
}
