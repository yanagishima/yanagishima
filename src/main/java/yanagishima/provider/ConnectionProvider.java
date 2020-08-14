package yanagishima.provider;

import yanagishima.config.YanagishimaConfig;

import javax.inject.Inject;
import javax.inject.Provider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionProvider implements Provider<Connection> {
	private final YanagishimaConfig config;

	@Inject
	public ConnectionProvider(YanagishimaConfig config) {
		this.config = config;
	}

	@Override
	public Connection get() {
		try {
			return DriverManager.getConnection(config.getConnectionUrl(), config.getConnectionUsername(), config.getConnectionPassword());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
