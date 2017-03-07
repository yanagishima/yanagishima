package yanagishima.config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class YanagishimaConfig {

	private Properties properties;

	public YanagishimaConfig(Properties properties) {
		this.properties = properties;
	}

	public String getPrestoCoordinatorServer(String datasource) {
		return Optional.ofNullable(properties.getProperty("presto.coordinator.server." + datasource)).get();
	}

	public String getPrestoRedirectServer(String datasource) {
		return Optional.ofNullable(properties.getProperty("presto.redirect.server." + datasource)).get();
	}

	public String getCatalog(String datasource) {
		return Optional.ofNullable(properties.getProperty("catalog." + datasource)).get();
	}

	public String getSchema(String datasource) {
		return Optional.ofNullable(properties.getProperty("schema." + datasource)).get();
	}
	
	public String getUser() {
		return "yanagishima";
	}
	
	public String getSource() {
		return "yanagishima";
	}

	public int getSelectLimit() {
		return Integer.parseInt(properties.getProperty("select.limit"));
	}

	public String getAuditHttpHeaderName() {
		return properties.getProperty("audit.http.header.name");
	}

	public List<String> getDatasources() {
		return Arrays.asList(Optional.ofNullable(properties.getProperty("presto.datasources")).get().split(","));
	}

}
