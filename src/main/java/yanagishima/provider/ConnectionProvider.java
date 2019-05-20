package yanagishima.provider;

import lombok.extern.slf4j.Slf4j;
import yanagishima.config.YanagishimaConfig;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This is a JDBC connection provider.
 */
@Slf4j
public class ConnectionProvider implements Provider<Connection> {

	@Inject
	private YanagishimaConfig yanagishimaConfig;

	private Connection connection;

	@Override
	public Connection get() {
		try {
			if (connection == null) {
				if(yanagishimaConfig.getDatabaseType().isPresent()) {
					if(yanagishimaConfig.getDatabaseType().get().equals("mysql")) {
						String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false", yanagishimaConfig.getMysqlHost(), yanagishimaConfig.getMysqlPort(), yanagishimaConfig.getMysqlDatabase());
						String user = yanagishimaConfig.getMysqlUser();
						String password = yanagishimaConfig.getMysqlPassword();
						connection = DriverManager.getConnection(url, user, password);
					} else {
						throw new IllegalArgumentException(yanagishimaConfig.getDatabaseType().get() + " is illegal database.type");
					}
				} else {
					connection = DriverManager.getConnection("jdbc:sqlite:data/yanagishima.db");
				}
			}
			return connection;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
