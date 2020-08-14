Visit [the official web site](https://yanagishima.github.io/yanagishima) for more information.

# yanagishima [![Build Status](https://github.com/yanagishima/yanagishima/workflows/CI/badge.svg)](https://github.com/yanagishima/yanagishima/actions?query=workflow%3ACI+event%3Apush+branch%3Amaster)

*yanagishima* is an open-source Web application for Presto, Hive, Elasticsearch and Spark.

[![IMAGE ALT TEXT HERE](http://img.youtube.com/vi/SoneFYNCXJE/maxresdefault.jpg)](http://www.youtube.com/watch?v=SoneFYNCXJE)


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
