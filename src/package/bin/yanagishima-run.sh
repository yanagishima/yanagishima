#!/bin/bash

bin=$(cd "$(dirname $0)"; pwd)
. "${bin}/yanagishima-config.sh"

exec java $YANAGISHIMA_OPTS -cp $CLASSPATH yanagishima.server.YanagishimaServer -conf $YANAGISHIMA_CONF_DIR "$@"
