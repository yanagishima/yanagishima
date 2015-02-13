package yanagishima.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import yanagishima.config.YanagishimaConfig;

public class PrestoServiceImpl implements PrestoService {
	
	private YanagishimaConfig yanagishimaConfig;
	
	@Inject
	public PrestoServiceImpl(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	public List<String> doQuery(String query) {
		String prestoCoordinatorServer = yanagishimaConfig.getPrestoCoordinatorServer();
		String catalog = yanagishimaConfig.getCatalog();
		String schema = yanagishimaConfig.getSchema();
		String url = String.format("jdbc:presto://%s/%s/%s", prestoCoordinatorServer, catalog, schema);
		try (Connection connection = DriverManager.getConnection(url, "test",
				null)) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet rs = statement.executeQuery(query)) {
					ArrayList<String> list = new ArrayList<String>();
					while (rs.next()) {
						list.add(rs.getString("node_id"));
					}
					return list;
				}
			}
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
