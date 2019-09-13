package yanagishima.config;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.base.Splitter;
import yanagishima.util.PropertiesUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class YanagishimaConfig {
	private static final Splitter SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

	private final Properties properties;

	public YanagishimaConfig(Properties properties) {
		this.properties = properties;
	}

	public String getPrestoCoordinatorServer(String datasource) {
		return PropertiesUtil.getParam(properties, "presto.coordinator.server." + datasource);
	}

	public String getPrestoCoordinatorServerOrNull(String datasource) {
		return properties.getProperty("presto.coordinator.server." + datasource);
	}

	public String getPrestoRedirectServer(String datasource) {
		String redirectStr = properties.getProperty("presto.redirect.server." + datasource);
		return firstNonNull(redirectStr, getPrestoCoordinatorServer(datasource));
	}

	public String getCatalog(String datasource) {
		return PropertiesUtil.getParam(properties, "catalog." + datasource);
	}

	public String getSchema(String datasource) {
		return PropertiesUtil.getParam(properties, "schema." + datasource);
	}

	public String getUser(String datasource) {
		String user = properties.getProperty("user." + datasource);
		return firstNonNull(user, "yanagishima");
	}
	
	public String getSource(String datasource) {
		String source = properties.getProperty("source." + datasource);
		return firstNonNull(source, "yanagishima");
	}

	public int getSelectLimit() {
		String limitStr = properties.getProperty("select.limit");
		return Integer.parseInt(firstNonNull(limitStr, "500"));
	}

	public String getAuditHttpHeaderName() {
		return properties.getProperty("audit.http.header.name");
	}

	public boolean isUseAuditHttpHeaderName() {
		return Boolean.parseBoolean(properties.getProperty("use.audit.http.header.name"));
	}

	public List<String> getDatasources(String engine) {
		String datasourceProperties = properties.getProperty(engine + ".datasources");
		if (datasourceProperties == null) {
			return Collections.emptyList();
		}
		return SPLITTER.splitToList(datasourceProperties);
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

	public List<Map<String, List<String>>> getDatasourceEngineList() {
		List<Map<String, List<String>>> datasourceEngineList = new ArrayList<>();
		List<String> datasourceList = getDatasources();
		for(String datasource : datasourceList) {
			Map<String, List<String>> datasourceMap = new HashMap<>();
			List<String> allEngines = getEngines();
			List<String> engines = new ArrayList<>();
			for(String engine : allEngines) {
				if(getDatasources(engine).contains(datasource)) {
					engines.add(engine);
				}
			}
			datasourceMap.put(datasource, engines);
			datasourceEngineList.add(datasourceMap);
		}
		return datasourceEngineList;
	}

	public List<String> getEngines() {
		return Arrays.asList(PropertiesUtil.getParam(properties, "sql.query.engines").split(","));
	}

	public double getQueryMaxRunTimeSeconds() {
		String secondsStr = properties.getProperty("presto.query.max-run-time-seconds");
		return Double.parseDouble(firstNonNull(secondsStr, "3600"));
	}

	public double getQueryMaxRunTimeSeconds(String datasource) {
		String property = properties.getProperty("presto.query.max-run-time-seconds" + "." + datasource);
		if(property == null) {
			return getQueryMaxRunTimeSeconds();
		}
		return Double.parseDouble(property);
	}

	public int getMaxResultFileByteSize() {
		String sizeStr = properties.getProperty("presto.max-result-file-byte-size");
		return Integer.parseInt(firstNonNull(sizeStr, "1073741824"));
	}

	public int getHiveMaxResultFileByteSize() {
		String sizeStr = properties.getProperty("hive.max-result-file-byte-size");
		return Integer.parseInt(firstNonNull(sizeStr, "1073741824"));
	}

	public int getToValuesQueryLimit() {
		String limitStr = properties.getProperty("to.values.query.limit");
		return Integer.parseInt(firstNonNull(limitStr, "500"));
	}

	public boolean isCheckDatasource() {
		return Boolean.parseBoolean(properties.getProperty("check.datasource"));
	}

	public List<String> getInvisibleSchemas(String datasource, String catalog) {
		String invisibleSchemas = properties.getProperty("invisible.schema." + datasource + "." + catalog);
		if(invisibleSchemas == null) {
			return Collections.emptyList();
		}
		return SPLITTER.splitToList(invisibleSchemas);
	}

	public List<String> getInvisibleDatabases(String datasource) {
		String invisibleDatabases = properties.getProperty("invisible.database." + datasource);
		if(invisibleDatabases == null) {
			return Collections.emptyList();
		}
		return SPLITTER.splitToList(invisibleDatabases);
	}

	public Optional<String> getFluentdExecutedTag() {
		return Optional.ofNullable(properties.getProperty("fluentd.executed.tag"));
	}

	public Optional<String> getFluentdFaliedTag() {
		return Optional.ofNullable(properties.getProperty("fluentd.failed.tag"));
	}

	public String getFluentdHost() {
		return firstNonNull(properties.getProperty("fluentd.host"), "localhost");
	}

	public int getFluentdPort() {
		return Integer.parseInt(firstNonNull(properties.getProperty("fluentd.port"), "24224"));
	}

	public boolean isUserRequired() {
		return Boolean.parseBoolean(properties.getProperty("user.require"));
	}

	public String getHiveJdbcUrl(String datasource) {
		return PropertiesUtil.getParam(properties, "hive.jdbc.url." + datasource);
	}

	public String getSparkJdbcUrl(String datasource) {
		return PropertiesUtil.getParam(properties, "spark.jdbc.url." + datasource);
	}

	public String getSparkWebUrl(String datasource) {
		return PropertiesUtil.getParam(properties, "spark.web.url." + datasource);
	}

	public String getHiveJdbcUser(String datasource) {
		return properties.getProperty("hive.jdbc.user." + datasource);
	}

	public String getHiveJdbcPassword(String datasource) {
		return properties.getProperty("hive.jdbc.password." + datasource);
	}

	public double getHiveQueryMaxRunTimeSeconds() {
		return Double.parseDouble(properties.getProperty("hive.query.max-run-time-seconds"));
	}

	public double getHiveQueryMaxRunTimeSeconds(String datasource) {
		String property = properties.getProperty("hive.query.max-run-time-seconds" + "." + datasource);
		if(property == null) {
			return getHiveQueryMaxRunTimeSeconds();
		}
		return Double.parseDouble(property);
	}

	public String getResourceManagerUrl(String datasource) {
		return PropertiesUtil.getParam(properties, "resource.manager.url." + datasource);
	}

	public Optional<String> getResourceManagerBegin(String datasource) {
		return Optional.ofNullable(properties.getProperty("resource.manager.url." + datasource + ".begin"));
	}

	public List<String> getHiveDisallowedKeywords(String datasource) {
		String property = properties.getProperty("hive.disallowed.keywords." + datasource);
		if(property == null) {
				return Collections.emptyList();
		}
		return SPLITTER.splitToList(property);
	}

	public List<String> getHiveSetupQueryList(String datasource) {
		String property = properties.getProperty("hive.setup.query.path." + datasource);
		List<String> hiveSetupQueryList = new ArrayList<>();
		if(property == null) {
			return Collections.emptyList();
		} else {
			try (BufferedReader br = Files.newBufferedReader(Paths.get(property), StandardCharsets.UTF_8)) {
				String line = br.readLine();
				while (line != null) {
					hiveSetupQueryList.add(line);
					line = br.readLine();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return hiveSetupQueryList;
		}
	}

	public boolean isAuth(String datasource) {
		return Boolean.parseBoolean(properties.getProperty("auth." + datasource));
	}

	public List<String> getHiveSecretKeywords(String datasource) {
		String property = properties.getProperty("hive.secret.keywords." + datasource);
		if(property == null) {
			return Collections.emptyList();
		}
		return SPLITTER.splitToList(property);
	}

	public List<String> getPrestoSecretKeywords(String datasource) {
		String property = properties.getProperty("presto.secret.keywords." + datasource);
		if(property == null) {
			return Collections.emptyList();
		}
		return SPLITTER.splitToList(property);
	}

	public List<String> getPrestoMustSpecifyConditions(String datasource) {
		String property = properties.getProperty("presto.must.specify.conditions." + datasource);
		if(property == null) {
			return Collections.emptyList();
		}
		return SPLITTER.splitToList(property);
	}

	public List<String> getHiveMustSpecifyConditions(String datasource) {
		String property = properties.getProperty("hive.must.specify.conditions." + datasource);
		if(property == null) {
			return Collections.emptyList();
		}
		return SPLITTER.splitToList(property);
	}

	public boolean isAllowOtherReadResult(String datasource) {
		String property = properties.getProperty("allow.other.read.result." + datasource);
		if (property == null) {
			return true;
		}
		return Boolean.parseBoolean(property);
	}

	public Optional<String> getWebhdfsUrl(String datasource, String catalog, String schema, String table) {
		return Optional.ofNullable(properties.getProperty(String.format("webhdfs.url.%s.%s.%s.%s", datasource, catalog, schema, table)));
	}

	public boolean isUseNewShowPartitions(String datasource) {
		String property = properties.getProperty("use.new.show.partitions." + datasource);
		if (property == null) {
			return true;
		}
		return Boolean.parseBoolean(property);
	}

	public boolean isUseJdbcCancel(String datasource) {
		return Boolean.parseBoolean(properties.getProperty("use.jdbc.cancel." + datasource));
	}

	public List<String> getElasticsearchSecretKeywords(String datasource) {
		String property = properties.getProperty("elasticsearch.secret.keywords." + datasource);
		if(property == null) {
			return Collections.emptyList();
		}
		return SPLITTER.splitToList(property);
	}

	public List<String> getElasticsearchMustSpecifyConditions(String datasource) {
		String property = properties.getProperty("elasticsearch.must.specify.conditions." + datasource);
		if(property == null) {
			return Collections.emptyList();
		}
		return SPLITTER.splitToList(property);
	}

	public String getElasticsearchJdbcUrl(String datasource) {
		return PropertiesUtil.getParam(properties, "elasticsearch.jdbc.url." + datasource);
	}

	public List<String> getElasticsearchDisallowedKeywords(String datasource) {
		String property = properties.getProperty("elasticsearch.disallowed.keywords." + datasource);
		if(property == null) {
			return Collections.emptyList();
		}
		return SPLITTER.splitToList(property);
	}

	public int getElasticsearchMaxResultFileByteSize() {
		String sizeStr = properties.getProperty("elasticsearch.max-result-file-byte-size");
		return Integer.parseInt(firstNonNull(sizeStr, "1073741824"));
	}

	public double getElasticsearchQueryMaxRunTimeSeconds() {
		String secondsStr = properties.getProperty("elasticsearch.query.max-run-time-seconds");
		return Double.parseDouble(firstNonNull(secondsStr, "3600"));
	}

	public double getElasticsearchQueryMaxRunTimeSeconds(String datasource) {
		String property = properties.getProperty("elasticsearch.query.max-run-time-seconds" + "." + datasource);
		if(property == null) {
			return getElasticsearchQueryMaxRunTimeSeconds();
		}
		return Double.parseDouble(property);
	}

	public Optional<String> getMetadataServiceUrl(String datasource) {
		return Optional.ofNullable(properties.getProperty(String.format("metadata.service.url.%s", datasource)));
	}

	public boolean isMetadataService(String datasource) {
		String property = properties.getProperty(String.format("metadata.service.url.%s", datasource));
		if (property == null) {
			return false;
		}
		return true;
	}

	public boolean isHiveImpersonation(String datasource) {
		return Boolean.parseBoolean(properties.getProperty("hive.jdbc.impersonation." + datasource));
	}

	public Optional<String> getWebhdfsProxyUser(String datasource) {
		return Optional.ofNullable(properties.getProperty(String.format("webhdfs.proxy.user.%s", datasource)));
	}

	public Optional<String> getWebhdfsProxyPassword(String datasource) {
		return Optional.ofNullable(properties.getProperty(String.format("webhdfs.proxy.password.%s", datasource)));
	}

	public Optional<String> getDatabaseType() {
		return Optional.ofNullable(properties.getProperty("database.type"));
	}

	public String getMysqlHost() {
		return PropertiesUtil.getParam(properties, "mysql.host");
	}

	public String getMysqlPort() {
		return PropertiesUtil.getParam(properties, "mysql.port");
	}

	public String getMysqlDatabase() {
		return PropertiesUtil.getParam(properties, "mysql.database");
	}

	public String getMysqlUser() {
		return PropertiesUtil.getParam(properties, "mysql.user");
	}

	public String getMysqlPassword() {
		return PropertiesUtil.getParam(properties, "mysql.password");
	}

	public boolean IsDatatimePartitionHasHyphen(String datasource) {
		String property = properties.getProperty("datetime.partition.has.hyphen." + datasource);
		if (property == null) {
			return false;
		}
		return true;
	}

	public boolean isUseOldPresto(String datasource) {
		return Boolean.parseBoolean(properties.getProperty("use.old.presto." + datasource));
	}
}
