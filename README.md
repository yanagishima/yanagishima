# yanagishima

yanagishima is a Web UI for presto.

![preview](v4.gif)

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
* search table
* handle multiple presto clusters
* auto detection of partition key
* show progress of running query
* query parameters substitution
* insert chart
* format query
* run in the background
* convert from TSV to values query
* function, table completion(ESC)
* validation(Shift+Enter)

# Limitation

* paging results is not supported
* Access Control
* Authentication

# Quick Start
```
wget https://bintray.com/artifact/download/wyukawa/generic/yanagishima-5.0.zip
unzip yanagishima-5.0.zip
cd yanagishima-5.0
vim conf/yanagishima.properties
nohup bin/yanagishima-start.sh >y.log 2>&1 &
```
see http://localhost:8080/

# Configuration

You need to edit conf/yanagishima.properties.
```
jetty.port=8080 # yanagishima web port
presto.query.max-run-time-seconds=1800 # 30 minutes. If presto query exceeds this time, yanagishima cancel the query.
presto.max-result-file-byte-size=1073741824 # 1GB. If presto query result file size exceeds this value, yanagishima cancel the query.
presto.datasources=your-presto # you can speciy freely. But you need to spedify same name to presto.coordinator.server.[...] and presto.redirect.server.[...] and catalog.[...] and schema.[...]
presto.coordinator.server.your-presto=http://presto.coordinator:8080 # presto coordinator url
presto.redirect.server.your-presto=http://presto.coordinator:8080 # almost same as presto coordinator url. If you use reverse proxy, specify it
catalog.your-presto=hive # presto catalog name
schema.your-presto=default # presto schema name
select.limit=500 # if query result exceeds this limit, to show rest of result is skipped
audit.http.header.name=some.auth.header # http header name for audit log
to.values.query.limit=500 # limit to convert from csv to values query
check.datasource=false # authroization feature
```

If you want to handle multiple presto clusters, you need to specify as follows.
```
presto.datasources=presto1,presto2
presto.coordinator.server.presto1=http://presto1.coordinator:8080
presto.redirect.server.presto1=http://presto1.coordinator:8080
presto.coordinator.server.presto2=http://presto2.coordinator:8080
presto.redirect.server.presto2=http://presto2.coordinator:8080
catalog.presto1=hive
schema.presto1=default
catalog.presto2=hive
schema.presto2=default
```

# Audit Logging
yanagishima doesn't have authentication feature.
but, if you use reverse proxy server like Nginx for authentication, you can add audit logging.
In this case, please specify ```audit.http.header.name``` which is http header name to be passed through Nginx.

# Start
```
bin/yanagishima-start.sh
```

# Stop
```
bin/yanagishima-stop.sh
```

# Requirements

* Java 8

## Build yanagishima

```
./gradlew distZip
```

## For Front-end Engineer

### File organization

|File|Description|Copy to docroot|Build index.js|
|:--|:--|:-:|:-:|
|build/index.html|SPA body|Yes||
|build/index.js|Static assets (JS/CSS/IMG)|Yes||
|build/favicon.ico|Favorite icon|Yes||
|build/share/index.html|Published result page (readonly)|Yes||
|build/error/index.html|Error page template|Yes||
|source/config.js|Config for yanagishima||Yes|
|source/core.js|SPA core||Yes|
|source/plugin.js|Vue plugin for Ace Editor||Yes|
|source/yanagishima.svg|Logo/Background image||Yes|
|source/scss/bootstrap.scss|CSS based on Bootstrap||Yes|
|webpack.config.json|Config for webpack|-|-|
|browsersync.config.json|Config for Browsersync|-|-|

### Framework/Plugin

- CSS
	- Bootstrap 4.0.0 alpha.6
	- FontAwesome 4.7.0
	- Google Fonts "[Droid+Sans](https://fonts.google.com/specimen/Droid+Sans)"
- JavaScript
	- Vue 2.3.0
	- Ace Editor 1.2.6
	- Sugar 2.0.4
	- jQuery 3.2.1
- Build/Serving tool
	- webpack 2.2.1
	- browser-sync 2.18.8

### Deep customization

#### Installation

	$ cd web
	$ npm install

#### Build/Serving and Livereload

	$ npm start
