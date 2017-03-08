package yanagishima.provider;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Provider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This is a JDBC connection provider.
 */
@Slf4j
public class ConnectionProvider implements Provider<Connection> {

	private Connection connection;

	@Override
	public Connection get() {
		try {
			if (connection == null) {
				connection = DriverManager.getConnection("jdbc:sqlite:data/yanagishima.db");
			}

			return connection;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
