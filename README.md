# yanagishima

yanagishima is a Web UI for presto like MySQL Workbench.

![yanagishima](screenshot/yanagishima.png)

# Features
* easy to install
* easy to use like MySQL Workbench(for example, right click operation)
* query history for self by using local storage
* query bookmark
* show query execution list
* kill running query
* format query
* show columns
* show partitions
* TSV download
* incremental search
* show presto view ddl
* complete of query(Ctrl+Space)
* share query
* syntax highlight
* search table

# Limitation

* paging results is not supported

# Quick Start
```
wget https://bintray.com/artifact/download/wyukawa/generic/yanagishima-0.8.zip
unzip yanagishima-0.8.zip
cd yanagishima-0.8
vim conf/yanagishima.properties
nohup bin/yanagishima-start.sh >y.log 2>&1 &
```
see http://localhost:8080/

# Configuration

You need to edit conf/yanagishima.properties.

At least, you need to edit ```presto.coordinator.server``` and ```catalog``` and ```schema```.
```
jetty.port=8080 # yanagishima web port
presto.coordinator.server=http://presto.coordinator:8080 # presto coordinator url
presto.redirect.server=http://presto.coordinator:8080 # almost same as presto coordinator url. If you use reverse proxy, specify it
select.limit=500 # if query result exceeds this limit, to show rest of result is skipped
catalog=hive # presto catalog name
schema=default # presto schema name
```

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
