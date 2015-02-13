package yanagishima.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PrestoServiceImpl implements PrestoService {

	@Override
	public List<String> doQuery(String query) {
		ArrayList<String> list = new ArrayList<String>();
		String url = "jdbc:presto://localhost:8080/hive/default";
		try (Connection connection = DriverManager.getConnection(url, "test",
				null)) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet rs = statement.executeQuery(query)) {
					while (rs.next()) {
						list.add(rs.getString("node_id"));
					}
				}
			}
			return list;
		} catch(SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
