Visit [the official web site](https://yanagishima.github.io/yanagishima) for more information.

# yanagishima [![Build Status](https://travis-ci.com/yanagishima/yanagishima.svg?branch=master)](https://travis-ci.com/yanagishima/yanagishima)

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
