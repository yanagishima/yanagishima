<p align="center">
    <img alt="Yanagishima Logo" src="docs/images/yanagishima.png" width="25%" />
</p>
<p align="center">Yanagishima is an open-source Web application for Trino, Presto, Hive and Spark.</p>
<p align="center">Visit <a href="https://yanagishima.github.io/yanagishima">the official website</a> for more information.</p>
<p align="center">
   <a href="https://github.com/yanagishima/yanagishima/actions?query=workflow%3ACI+event%3Apush+branch%3Amaster">
       <img src="https://github.com/yanagishima/yanagishima/workflows/CI/badge.svg" alt="CI" />
   </a>
   <a href="http://www.youtube.com/watch?v=SoneFYNCXJEr">
       <img src="https://img.shields.io/badge/YouTube-Video-FF0000" alt="YouTube Video" />
   </a>
</p>

# Build requirements

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
vim config/application.yml
nohup bin/yanagishima-start.sh >y.log 2>&1 &
```
see http://localhost:8080/

## Stop
```
bin/yanagishima-shutdown.sh
```
