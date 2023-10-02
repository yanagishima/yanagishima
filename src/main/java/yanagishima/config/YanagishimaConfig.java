package yanagishima.config;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class YanagishimaConfig {
  private static final Splitter SPLITTER = Splitter.on(',').trimResults().omitEmptyStrings();

  private final Environment environment;

  public boolean corsEnabled() {
    return Boolean.parseBoolean(environment.getProperty("cors.enabled", "false"));
  }

  public String getPrestoCoordinatorServer(String datasource) {
    return environment.getRequiredProperty("presto.coordinator.server." + datasource);
  }

  public String getPrestoCoordinatorServerOrNull(String datasource) {
    return environment.getProperty("presto.coordinator.server." + datasource);
  }

  public String getPrestoRedirectServer(String datasource) {
    String redirectStr = environment.getProperty("presto.redirect.server." + datasource);
    return firstNonNull(redirectStr, getPrestoCoordinatorServer(datasource));
  }

  public String getTrinoCoordinatorServer(String datasource) {
    return environment.getRequiredProperty("trino.coordinator.server." + datasource);
  }

  public String getTrinoCoordinatorServerOrNull(String datasource) {
    return environment.getProperty("trino.coordinator.server." + datasource);
  }

  public String getTrinoRedirectServer(String datasource) {
    String redirectStr = environment.getProperty("trino.redirect.server." + datasource);
    return firstNonNull(redirectStr, getTrinoCoordinatorServer(datasource));
  }

  public String getCatalog(String datasource) {
    return environment.getRequiredProperty("catalog." + datasource);
  }

  public String getSchema(String datasource) {
    return environment.getRequiredProperty("schema." + datasource);
  }

  public String getUser(String datasource) {
    String user = environment.getProperty("user." + datasource);
    return firstNonNull(user, "yanagishima");
  }

  public String getSource(String datasource) {
    String source = environment.getProperty("source." + datasource);
    return firstNonNull(source, "yanagishima");
  }

  public int getSelectLimit() {
    String limitStr = environment.getProperty("select.limit");
    return Integer.parseInt(firstNonNull(limitStr, "500"));
  }

  public String getAuditHttpHeaderName() {
    return environment.getProperty("audit.http.header.name");
  }

  public boolean isUseAuditHttpHeaderName() {
    return Boolean.parseBoolean(environment.getProperty("use.audit.http.header.name"));
  }

  public List<String> getDatasources(String engine) {
    String datasourceProperties = environment.getProperty(engine + ".datasources");
    if (datasourceProperties == null) {
      return Collections.emptyList();
    }
    return SPLITTER.splitToList(datasourceProperties);
  }

  public List<String> getDatasources() {
    List<String> datasources = new ArrayList<>();
    List<String> engines = getEngines();
    for (String engine : engines) {
      List<String> datasourceList = getDatasources(engine);
      for (String datasource : datasourceList) {
        if (!datasources.contains(datasource)) {
          datasources.add(datasource);
        }
      }
    }
    return new ArrayList<>(datasources);
  }

  public List<String> getEngines() {
    return Arrays.asList(environment.getRequiredProperty("sql.query.engines").split(","));
  }

  public double getPrestoQueryMaxRunTimeSeconds() {
    String secondsStr = environment.getProperty("presto.query.max-run-time-seconds");
    return Double.parseDouble(firstNonNull(secondsStr, "3600"));
  }

  public double getPrestoQueryMaxRunTimeSeconds(String datasource) {
    String property = environment.getProperty("presto.query.max-run-time-seconds" + "." + datasource);
    if (property == null) {
      return getPrestoQueryMaxRunTimeSeconds();
    }
    return Double.parseDouble(property);
  }

  public long getPrestoMaxResultFileByteSize() {
    String sizeStr = environment.getProperty("presto.max-result-file-byte-size");
    return Long.parseLong(firstNonNull(sizeStr, "1073741824"));
  }

  public long getTrinoMaxResultFileByteSize() {
    String sizeStr = environment.getProperty("trino.max-result-file-byte-size");
    return Long.parseLong(firstNonNull(sizeStr, "1073741824"));
  }

  public double getTrinoQueryMaxRunTimeSeconds() {
    String secondsStr = environment.getProperty("trino.query.max-run-time-seconds");
    return Double.parseDouble(firstNonNull(secondsStr, "3600"));
  }

  public double getTrinoQueryMaxRunTimeSeconds(String datasource) {
    String property = environment.getProperty("trino.query.max-run-time-seconds" + "." + datasource);
    if (property == null) {
      return getPrestoQueryMaxRunTimeSeconds();
    }
    return Double.parseDouble(property);
  }

  public long getHiveMaxResultFileByteSize() {
    String sizeStr = environment.getProperty("hive.max-result-file-byte-size");
    return Long.parseLong(firstNonNull(sizeStr, "1073741824"));
  }

  public int getToValuesQueryLimit() {
    String limitStr = environment.getProperty("to.values.query.limit");
    return Integer.parseInt(firstNonNull(limitStr, "500"));
  }

  public boolean isCheckDatasource() {
    return Boolean.parseBoolean(environment.getProperty("check.datasource"));
  }

  public List<String> getInvisibleSchemas(String datasource, String catalog) {
    String invisibleSchemas = environment.getProperty("invisible.schema." + datasource + "." + catalog);
    if (invisibleSchemas == null) {
      return Collections.emptyList();
    }
    return SPLITTER.splitToList(invisibleSchemas);
  }

  public List<String> getInvisibleDatabases(String datasource) {
    String invisibleDatabases = environment.getProperty("invisible.database." + datasource);
    if (invisibleDatabases == null) {
      return Collections.emptyList();
    }
    return SPLITTER.splitToList(invisibleDatabases);
  }

  public Optional<String> getFluentdExecutedTag() {
    return Optional.ofNullable(environment.getProperty("fluentd.executed.tag"));
  }

  public Optional<String> getFluentdFaliedTag() {
    return Optional.ofNullable(environment.getProperty("fluentd.failed.tag"));
  }

  public Optional<String> getFluentdPublishTag() {
    return Optional.ofNullable(environment.getProperty("fluentd.publish.tag"));
  }

  public String getFluentdHost() {
    return firstNonNull(environment.getProperty("fluentd.host"), "localhost");
  }

  public int getFluentdPort() {
    return Integer.parseInt(firstNonNull(environment.getProperty("fluentd.port"), "24224"));
  }

  public boolean isUserRequired() {
    return Boolean.parseBoolean(environment.getProperty("user.require"));
  }

  public String getHiveJdbcUrl(String datasource) {
    return environment.getRequiredProperty("hive.jdbc.url." + datasource);
  }

  public String getSparkJdbcUrl(String datasource) {
    return environment.getRequiredProperty("spark.jdbc.url." + datasource);
  }

  public String getSparkWebUrl(String datasource) {
    return environment.getRequiredProperty("spark.web.url." + datasource);
  }

  public String getHiveJdbcUser(String datasource) {
    return environment.getProperty("hive.jdbc.user." + datasource);
  }

  public String getHiveJdbcPassword(String datasource) {
    return environment.getProperty("hive.jdbc.password." + datasource);
  }

  private double getHiveQueryMaxRunTimeSeconds() {
    String seconds = environment.getProperty("hive.query.max-run-time-seconds");
    return Double.parseDouble(firstNonNull(seconds, "3600"));
  }

  public double getHiveQueryMaxRunTimeSeconds(String datasource) {
    String property = environment.getProperty("hive.query.max-run-time-seconds" + "." + datasource);
    if (property == null) {
      return getHiveQueryMaxRunTimeSeconds();
    }
    return Double.parseDouble(property);
  }

  public String getResourceManagerUrl(String datasource) {
    return environment.getRequiredProperty("resource.manager.url." + datasource);
  }

  public Optional<String> getResourceManagerBegin(String datasource) {
    return Optional.ofNullable(environment.getProperty("resource.manager.url." + datasource + ".begin"));
  }

  public List<String> getHiveDisallowedKeywords(String datasource) {
    String property = environment.getProperty("hive.disallowed.keywords." + datasource);
    if (property == null) {
      return Collections.emptyList();
    }
    return SPLITTER.splitToList(property);
  }

  public List<String> getHiveSetupQueryList(String datasource) {
    String property = environment.getProperty("hive.setup.query.path." + datasource);
    List<String> hiveSetupQueryList = new ArrayList<>();
    if (property == null) {
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
      return ImmutableList.copyOf(hiveSetupQueryList);
    }
  }

  public boolean isAuth(String datasource) {
    return Boolean.parseBoolean(environment.getProperty("auth." + datasource));
  }

  public List<String> getHiveSecretKeywords(String datasource) {
    String property = environment.getProperty("hive.secret.keywords." + datasource);
    if (property == null) {
      return Collections.emptyList();
    }
    return SPLITTER.splitToList(property);
  }

  public List<String> getPrestoSecretKeywords(String datasource) {
    String property = environment.getProperty("presto.secret.keywords." + datasource);
    if (property == null) {
      return Collections.emptyList();
    }
    return SPLITTER.splitToList(property);
  }

  public List<String> getPrestoMustSpecifyConditions(String datasource) {
    String property = environment.getProperty("presto.must.specify.conditions." + datasource);
    if (property == null) {
      return Collections.emptyList();
    }
    return SPLITTER.splitToList(property);
  }

  public List<String> getTrinoSecretKeywords(String datasource) {
    String property = environment.getProperty("trino.secret.keywords." + datasource);
    if (property == null) {
      return Collections.emptyList();
    }
    return SPLITTER.splitToList(property);
  }

  public List<String> getTrinoMustSpecifyConditions(String datasource) {
    String property = environment.getProperty("trino.must.specify.conditions." + datasource);
    if (property == null) {
      return Collections.emptyList();
    }
    return SPLITTER.splitToList(property);
  }

  public List<String> getHiveMustSpecifyConditions(String datasource) {
    String property = environment.getProperty("hive.must.specify.conditions." + datasource);
    if (property == null) {
      return Collections.emptyList();
    }
    return SPLITTER.splitToList(property);
  }

  public boolean isAllowOtherReadResult(String datasource) {
    String property = environment.getProperty("allow.other.read.result." + datasource);
    if (property == null) {
      return true;
    }
    return Boolean.parseBoolean(property);
  }

  public boolean isUseNewShowPartitions(String datasource) {
    String property = environment.getProperty("use.new.show.partitions." + datasource);
    if (property == null) {
      return true;
    }
    return Boolean.parseBoolean(property);
  }

  public boolean isUseJdbcCancel(String datasource) {
    return Boolean.parseBoolean(environment.getProperty("use.jdbc.cancel." + datasource));
  }

  public boolean isHiveImpersonation(String datasource) {
    return Boolean.parseBoolean(environment.getProperty("hive.jdbc.impersonation." + datasource));
  }

  public boolean isDatatimePartitionHasHyphen(String datasource) {
    String property = environment.getProperty("datetime.partition.has.hyphen." + datasource);
    if (property == null) {
      return false;
    }
    return true;
  }

  public String getHiveDriverClassName(String datawsource) {
    String property = environment.getProperty("hive.jdbc.driver." + datawsource,
                                              "org.apache.hive.jdbc.HiveDriver");
    return property;
  }

  public boolean isPrestoImpersonation(String datasource) {
    return Boolean.parseBoolean(environment.getProperty("presto.impersonation." + datasource));
  }

  public Optional<String> getPrestoImpersonatedUser(String datasource) {
    return Optional.ofNullable(environment.getProperty("presto.impersonated.user." + datasource));
  }

  public Optional<String> getPrestoImpersonatedPassword(String datasource) {
    return Optional.ofNullable(environment.getProperty("presto.impersonated.password." + datasource));
  }

  public boolean isTrinoImpersonation(String datasource) {
    return Boolean.parseBoolean(environment.getProperty("trino.impersonation." + datasource));
  }

  public Optional<String> getTrinoImpersonatedUser(String datasource) {
    return Optional.ofNullable(environment.getProperty("trino.impersonated.user." + datasource));
  }

  public Optional<String> getTrinoImpersonatedPassword(String datasource) {
    return Optional.ofNullable(environment.getProperty("trino.impersonated.password." + datasource));
  }
}
