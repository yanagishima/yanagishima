#!/bin/bash

bin=$(cd "$(dirname $0)"; pwd)
. "${bin}/yanagishima-config.sh"

exec java $YANAGISHIMA_OPTS -jar lib/yanagishima.jar --spring.config.location=$YANAGISHIMA_CONF_DIR "$@"
