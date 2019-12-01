# yanagishima [![Build Status](https://github.com/yanagishima/yanagishima/workflows/CI/badge.svg)](https://github.com/yanagishima/yanagishima/actions?query=workflow%3ACI+event%3Apush+branch%3Amaster)

yanagishima is a Web UI for presto/hive.

[![IMAGE ALT TEXT HERE](http://img.youtube.com/vi/SoneFYNCXJE/maxresdefault.jpg)](http://www.youtube.com/watch?v=SoneFYNCXJE)

# Features
* easy to install
* easy to use
* run query(Ctrl+Enter)
* query history
* query bookmark
* show query execution list
* kill running query
* show columns
* show partitions
* show query result data size
* show query result line number
* TSV download
* CSV download
* show presto view ddl
* share query
* share query result
* search table(presto only)
* handle multiple presto/hive clusters
* auto detection of partition key
* show progress of running query
* query parameters substitution
* insert chart
* format query(Ctrl+Shift+F, presto only)
* convert from TSV to values query(presto only)
* function, table completion(Ctrl+Space, presto only)
* validation(Shift+Enter, presto only)
* export/import history
* export/import bookmark
* desktop notification(HTTPS only)
* pretty print for json/map data
* enable to compare query result
* comment about query
* convert hive/presto query
* support graphviz to visualize presto explain result
* support Elasticsearch SQL
* label
* pivot
* support Spark SQL
* show stats for presto

# Releases

See [RELEASE.md](RELEASE.md) for release note.

# Requirements to build yanagishima

* Java 11
* Node.js

## Quick Start
```
git clone https://github.com/yanagishima/yanagishima.git
cd yanagishima
git checkout -b [version] refs/tags/[version]
./gradlew distZip
cd build/distributions
unzip yanagishima-[version].zip
cd yanagishima-[version]
vim conf/yanagishima.properties
nohup bin/yanagishima-start.sh >y.log 2>&1 &
```
see http://localhost:8080/

# Stop
```
bin/yanagishima-shutdown.sh
```

# Deploy in production
Highly recommend to deploy in HTTPS due to security, clipboard copy, desktop notification

# Configuration

You need to edit conf/yanagishima.properties.
```
# yanagishima web port
jetty.port=8080
# 30 minutes. If presto query exceeds this time, yanagishima cancel the query.
presto.query.max-run-time-seconds=1800
# 1GB. If presto query result file size exceeds this value, yanagishima cancel the query.
presto.max-result-file-byte-size=1073741824
# you can specify freely. But you need to specify same name to presto.coordinator.server.[...] and presto.redirect.server.[...] and catalog.[...] and schema.[...]
presto.datasources=your-presto
# presto coordinator url
presto.coordinator.server.your-presto=http://presto.coordinator:8080
# almost same as presto coordinator url. If you use reverse proxy, specify it
presto.redirect.server.your-presto=http://presto.coordinator:8080
# presto catalog name
catalog.your-presto=hive
# presto schema name
schema.your-presto=default

# presto user
user.your-presto=yanagishima
# presto source
source.your-presto=yanagishima

# if query result exceeds this limit, to show rest of result is skipped
select.limit=500
# http header name for audit log
audit.http.header.name=some.auth.header
# limit to convert from tsv to values query
to.values.query.limit=500
# authorization feature
check.datasource=false
hive.jdbc.url.your-hive=jdbc:hive2://localhost:10000/default;auth=noSasl
hive.jdbc.user.your-hive=yanagishima-hive
hive.jdbc.password.your-hive=yanagishima-hive
hive.query.max-run-time-seconds=3600
hive.query.max-run-time-seconds.your-hive=3600
resource.manager.url.your-hive=http://localhost:8088
sql.query.engines=presto,hive
hive.datasources=your-hive
hive.disallowed.keywords.your-hive=insert,drop
# 1GB. If hive query result file size exceeds this value, yanagishima cancel the query.
hive.max-result-file-byte-size=1073741824
# setup initial hive query(for example, set hive.mapred.mode=strict)
hive.setup.query.path.your-hive=/usr/local/yanagishima/conf/hive_setup_query_your-hive
# CORS setting
cors.enabled=false
```

If you use a single presto cluster, you need to specify as follows.
```
jetty.port=8080
presto.datasources=your-presto
presto.coordinator.server.your-presto=http://presto.coordinator:8080
catalog.your-presto=hive
schema.your-presto=default
sql.query.engines=presto
```

If you want to handle multiple presto clusters, you need to specify as follows.
```
jetty.port=8080
presto.datasources=presto1,presto2
presto.coordinator.server.presto1=http://presto1.coordinator:8080
presto.coordinator.server.presto2=http://presto2.coordinator:8080
catalog.presto1=hive
schema.presto1=default
catalog.presto2=hive
schema.presto2=default
sql.query.engines=presto
```

If you use a single hive cluster, you need to specify as follows.
```
jetty.port=8080
hive.jdbc.url.your-hive=jdbc:hive2://localhost:10000/default;auth=noSasl
hive.jdbc.user.your-hive=yanagishima-hive
hive.jdbc.password.your-hive=yanagishima-hive
resource.manager.url.your-hive=http://localhost:8088
sql.query.engines=hive
hive.datasources=your-hive
```

If you use a hive kerberized cluster and want to kill query, you need to specify as follows.
```
jetty.port=8080
hive.jdbc.url.your-hive=jdbc:hive2://localhost:10000/default;auth=noSasl
hive.jdbc.user.your-hive=yanagishima-hive
hive.jdbc.password.your-hive=yanagishima-hive
resource.manager.url.your-hive=http://localhost:8088
sql.query.engines=hive
hive.datasources=your-hive
use.jdbc.cancel.your-hive=true
```

If you use presto and hive, you need to specify as follows.
```
jetty.port=8080
presto.datasources=your-cluster
presto.coordinator.server.your-cluster=http://presto.coordinator:8080
catalog.your-cluster=hive
schema.your-cluster=default
hive.jdbc.url.your-cluster=jdbc:hive2://localhost:10000/default;auth=noSasl
hive.jdbc.user.your-cluster=yanagishima-hive
hive.jdbc.password.your-cluster=yanagishima-hive
resource.manager.url.your-cluster=http://localhost:8088
sql.query.engines=presto,hive
hive.datasources=your-cluster
```

If you use an elasticsearch, you need to specify as follows.
```
jetty.port=8080
elasticsearch.jdbc.url.your-elasticsearch=jdbc:es:localhost:9200
elasticsearch.datasources=your-elasticsearch
sql.query.engines=elasticsearch
```

If you use a spark, you need to start a spark thrift server and specify as follows.
```
jetty.port=8080
spark.jdbc.url.your-spark=jdbc:hive2://sparkthriftserver:10000
spark.web.url.your-spark=http://sparkthriftserver:4040
resource.manager.url.your-hive=http://localhost:8088
sql.query.engines=spark
spark.datasources=your-spark
```

# Authentication and authorization
yanagishima doesn't have authentication/authorization feature.

But, if you have any reverse proxy server for yanagishima and that reverse proxy server provides HTTP level authentication, you can use it for yanagishima too.
yanagishima can log username for each query executions and authorize per datasource.

If your reverse proxy server sets username on HTTP header just after authentication, before proxied requests you can use it.

In this case, please specify ```audit.http.header.name``` which is http header name to be passed through your proxy.

If you want to deny to access without usename, please specify ```user.require=true```

If you set ```check.datasource=true``` and datasource list which you want to allow on HTTP header ```X-yanagishima-datasources``` through your proxy, authorization feature is enabled.

For example, if there are three datasources(aaa and bbb and ccc) and ```X-yanagishima-datasources=aaa,bbb``` is set, user can't access to datasource ccc.

If you use a presto with LDAP, you need to specify ```auth.xxx=true``` in your yanagishima.properties
```
jetty.port=8080
presto.datasources=your-presto
presto.coordinator.server.your-presto=http://presto.coordinator:8080
catalog.your-presto=hive
schema.your-presto=default
sql.query.engines=presto
auth.your-presto=true
```

## How to upgrade
If you want to ugprade yanagishima from xxx to yyyy, steps are as follows
```
cd yanagishima-xxx
bin/yanagishima-shutdown.sh
cd ..
unzip yanagishima-yyy.zip
cd yanagishima-yyy
mv result result_bak
mv yanagishima-xxx/result .
cp yanagishima-xxx/data/yanagishima.db data/
cp yanagishima-xxx/conf/yanagishima.properties conf/
bin/yanagishima-start.sh
```
If it is necessary to migrate yanagishima.db or result file, you need to migrate.

## For Front-end Engineer

### File organization

|File|Description|Copy to docroot|Build index.js|
|:--|:--|:-:|:-:|
|index.html|Mount point for Vue|Yes||
|static/favicon.ico|Favorite icon|Yes||
|src|Source files||Yes|
|src/main.js|Entry point||Yes|
|src/App.vue|Root component||Yes|
|src/components|Vue components||Yes|
|src/router|Vue Router routes||Yes|
|src/store|Vuex store||Yes|
|src/views|Views which are switched by Vue Router||Yes|
|src/assets/yanagishima.svg|Logo/Background image||Yes|
|src/assets/scss/bootstrap.scss|CSS based on Bootstrap||Yes|
|build|Build scripts for webpack|-|-|
|config|Build configs for webpack|-|-|

### Framework/Plugin

- CSS
  - Bootstrap 4.1.3
  - FontAwesome 5.3.1
  - Google Fonts "[Droid+Sans](https://fonts.google.com/specimen/Droid+Sans)"
- JavaScript
  - Vue 2.5.2
  - Vuex 3.0.1
  - Vue Router 3.0.1
  - Ace Editor 1.3.3
  - Sugar 2.0.4
  - jQuery 3.3.1
- Build/Serve tool
  - webpack 3.6.0

### Deep customization

#### Dependence

- [Xcode](https://developer.apple.com/jp/xcode/)
- [Node.js](https://nodejs.org/ja/)

#### Install dependencies

```bash
$ cd web
$ npm install
```

#### Build

```bash
$ npm run build
```

#### Build/Serve and Livereload (for Front-end Engineer)

```bash
$ npm start
```
