#!/bin/bash

bin=$(cd "$(dirname $0)"; pwd)
YANAGISHIMA_HOME="${bin}/.."

if [ -z "$YANAGISHIMA_CONF_DIR" ]; then
  YANAGISHIMA_CONF_DIR=$YANAGISHIMA_HOME/config/
fi

for file in "$YANAGISHIMA_HOME"/lib/*.jar;
do
  CLASSPATH=$CLASSPATH:$file
done

if [ -z "$YANAGISHIMA_OPTS" ]; then
  YANAGISHIMA_OPTS=-Xmx3G
fi
