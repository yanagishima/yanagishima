package yanagishima.config;

import yanagishima.util.PropertiesUtil;

import java.util.*;

public class YanagishimaConfig {

	private Properties properties;

	public YanagishimaConfig(Properties properties) {
		this.properties = properties;
	}

	public String getPrestoCoordinatorServer(String datasource) {
		return PropertiesUtil.getParam(properties, "presto.coordinator.server." + datasource);
	}

	public String getPrestoRedirectServer(String datasource) {
		return PropertiesUtil.getParam(properties, "presto.redirect.server." + datasource);
	}

	public String getCatalog(String datasource) {
		return PropertiesUtil.getParam(properties, "catalog." + datasource);
	}

	public String getSchema(String datasource) {
		return PropertiesUtil.getParam(properties, "schema." + datasource);
	}
	
	public String getUser() {
		return "yanagishima";
	}
	
	public String getSource() {
		return "yanagishima";
	}

	public int getSelectLimit() {
		return Integer.parseInt(PropertiesUtil.getParam(properties, "select.limit"));
	}

	public String getAuditHttpHeaderName() {
		return properties.getProperty("audit.http.header.name");
	}

	public List<String> getDatasources(String engine) {
		String datasourceProperties = properties.getProperty(engine + ".datasources");
		if(datasourceProperties == null) {
			return new ArrayList<>();
		} else {
			return Arrays.asList(datasourceProperties.split(","));
		}
	}

	public List<String> getDatasources() {
		List<String> datasources = new ArrayList<>();
		List<String> engines = getEngines();
		for(String engine : engines) {
			List<String> datasourceList = getDatasources(engine);
			for(String datasource : datasourceList) {
				if(!datasources.contains(datasource)) {
					datasources.add(datasource);
				}
			}
		}
		return new ArrayList<>(datasources);
	}

	public List<String> getEngines() {
		return Arrays.asList(PropertiesUtil.getParam(properties, "sql.query.engines").split(","));
	}

	public double getQueryMaxRunTimeSeconds() {
		return Double.parseDouble(PropertiesUtil.getParam(properties, "presto.query.max-run-time-seconds"));
	}

	public double getQueryMaxRunTimeSeconds(String datasource) {
		String property = properties.getProperty("presto.query.max-run-time-seconds" + "." + datasource);
		if(property == null) {
			return getQueryMaxRunTimeSeconds();
		} else {
			return Double.parseDouble(property);
		}
	}

	public int getMaxResultFileByteSize() {
		return Integer.parseInt(PropertiesUtil.getParam(properties,"presto.max-result-file-byte-size"));
	}

	public int getToValuesQueryLimit() {
		return Integer.parseInt(PropertiesUtil.getParam(properties,"to.values.query.limit"));
	}

	public boolean isCheckDatasource() {
		return Boolean.parseBoolean(Optional.ofNullable(properties.getProperty("check.datasource")).orElse("false"));
	}

	public List<String> getInvisibleSchemas(String datasource, String catalog) {
		String invisibleSchemas = properties.getProperty("invisible.schema." + datasource + "." + catalog);
		if(invisibleSchemas == null) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(invisibleSchemas.split(","));
		}
	}

	public Optional<String> getFluentdExecutedTag() {
		return Optional.ofNullable(properties.getProperty("fluentd.executed.tag"));
	}

	public Optional<String> getFluentdFaliedTag() {
		return Optional.ofNullable(properties.getProperty("fluentd.failed.tag"));
	}

	public Optional<String> getFluentdHost() {
		return Optional.ofNullable(properties.getProperty("fluentd.host"));
	}

	public Optional<String> getFluentdPort() {
		return Optional.ofNullable(properties.getProperty("fluentd.port"));
	}

	public boolean isUserRequired() {
		return Boolean.parseBoolean(Optional.ofNullable(properties.getProperty("user.require")).orElse("false"));
	}

	public Optional<String> getHiveJdbcUrl(String datasource) {
		return Optional.ofNullable(properties.getProperty("hive.jdbc.url." + datasource));
	}

	public Optional<String> getHiveJdbcUser(String datasource) {
		return Optional.ofNullable(properties.getProperty("hive.jdbc.user." + datasource));
	}

	public Optional<String> getHiveJdbcPassword(String datasource) {
		return Optional.ofNullable(properties.getProperty("hive.jdbc.password." + datasource));
	}

	public double getHiveQueryMaxRunTimeSeconds() {
		return Double.parseDouble(properties.getProperty("hive.query.max-run-time-seconds"));
	}

	public double getHiveQueryMaxRunTimeSeconds(String datasource) {
		String property = properties.getProperty("hive.query.max-run-time-seconds" + "." + datasource);
		if(property == null) {
			return getHiveQueryMaxRunTimeSeconds();
		} else {
			return Double.parseDouble(property);
		}
	}

	public Optional<String> getResourceManagerUrl(String datasource) {
		return Optional.ofNullable(properties.getProperty("resource.manager.url." + datasource));
	}

}
