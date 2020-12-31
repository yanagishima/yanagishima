#!/bin/bash

bin=$(cd "$(dirname $0)"; pwd)
. "${bin}/yanagishima-config.sh"

java "$YANAGISHIMA_OPTS" -cp "$CLASSPATH" yanagishima.YanagishimaApplication --spring.config.location="$YANAGISHIMA_CONF_DIR" "$@" &

echo $! > "$YANAGISHIMA_HOME"/currentpid
