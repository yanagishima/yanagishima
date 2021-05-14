# Configuration
You need to edit `application.yml` file.
```yaml
# yanagishima web port
server.port: 8080
# 30 minutes. If presto query exceeds this time, yanagishima cancel the query.
presto.query.max-run-time-seconds: 1800
# 1GB. If presto query result file size exceeds this value, yanagishima cancel the query.
presto.max-result-file-byte-size: 1073741824
# you can specify freely. But you need to specify same name to presto.coordinator.server.[...] and presto.redirect.server.[...] and catalog.[...] and schema.[...]
presto.datasources: your-presto
# presto coordinator url
presto.coordinator.server.your-presto: http://presto.coordinator:8080
# almost same as presto coordinator url. If you use reverse proxy, specify it
presto.redirect.server.your-presto: http://presto.coordinator:8080
# presto catalog name
catalog.your-presto: hive
# presto schema name
schema.your-presto: default

# presto user
user.your-presto: yanagishima
# presto source
source.your-presto: yanagishima

# if query result exceeds this limit, to show rest of result is skipped
select.limit: 500
# http header name for audit log
audit.http.header.name: some.auth.header
# limit to convert from tsv to values query
to.values.query.limit: 500
# authorization feature
check.datasource: false
hive.jdbc.url.your-hive: jdbc:hive2://localhost:10000/default;auth=noSasl
hive.jdbc.user.your-hive: yanagishima-hive
hive.jdbc.password.your-hive: yanagishima-hive
hive.query.max-run-time-seconds: 3600
hive.query.max-run-time-seconds.your-hive: 3600
resource.manager.url.your-hive: http://localhost:8088
sql.query.engines: presto,hive
hive.datasources: your-hive
hive.disallowed.keywords.your-hive: insert,drop
# 1GB. If hive query result file size exceeds this value, yanagishima cancel the query.
hive.max-result-file-byte-size: 1073741824
# setup initial hive query(for example, set hive.mapred.mode=strict)
hive.setup.query.path.your-hive: /usr/local/yanagishima/conf/hive_setup_query_your-hive
# CORS setting
cors.enabled: false
```

## Single Trino cluster
```yaml
server.port: 8080
presto.datasources: your-presto
presto.coordinator.server.your-presto: http://presto.coordinator:8080
catalog.your-presto: hive
schema.your-presto: default
sql.query.engines: presto
```

## Multiple Trino clusters
```yaml
server.port: 8080
presto.datasources: presto1,presto2
presto.coordinator.server.presto1: http://presto1.coordinator:8080
presto.coordinator.server.presto2: http://presto2.coordinator:8080
catalog.presto1: hive
schema.presto1: default
catalog.presto2: hive
schema.presto2: default
sql.query.engines: presto
```

## Single Hive cluster
```yaml
server.port: 8080
hive.jdbc.url.your-hive: jdbc:hive2://localhost:10000/default;auth=noSasl
hive.jdbc.user.your-hive: yanagishima-hive
hive.jdbc.password.your-hive: yanagishima-hive
resource.manager.url.your-hive: http://localhost:8088
sql.query.engines: hive
hive.datasources: your-hive
```

## Kerberized Hive cluster
```yaml
server.port: 8080
hive.jdbc.url.your-hive: jdbc:hive2://localhost:10000/default;auth=noSasl
hive.jdbc.user.your-hive: yanagishima-hive
hive.jdbc.password.your-hive: yanagishima-hive
resource.manager.url.your-hive: http://localhost:8088
sql.query.engines: hive
hive.datasources: your-hive
use.jdbc.cancel.your-hive: true
```

## Trino and Hive
```yaml
server.port: 8080
presto.datasources: your-cluster
presto.coordinator.server.your-cluster: http://presto.coordinator:8080
catalog.your-cluster: hive
schema.your-cluster: default
hive.jdbc.url.your-cluster: jdbc:hive2://localhost:10000/default;auth=noSasl
hive.jdbc.user.your-cluster: yanagishima-hive
hive.jdbc.password.your-cluster: yanagishima-hive
resource.manager.url.your-cluster: http://localhost:8088
sql.query.engines: presto,hive
hive.datasources: your-cluster
```

## Elasticsearch
```yaml
server.port: 8080
elasticsearch.jdbc.url.your-elasticsearch: jdbc:es:localhost:9200
elasticsearch.datasources: your-elasticsearch
sql.query.engines: elasticsearch
```

## Spark
```yaml
server.port: 8080
spark.jdbc.url.your-spark: jdbc:hive2://sparkthriftserver:10000
spark.web.url.your-spark: http://sparkthriftserver:4040
resource.manager.url.your-hive: http://localhost:8088
sql.query.engines: spark
spark.datasources: your-spark
```
