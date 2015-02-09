#!/bin/bash

yanagishima_dir=$(dirname $0)/..

for file in $yanagishima_dir/lib/*.jar;
do
  CLASSPATH=$CLASSPATH:$file
done

echo $yanagishima_dir;
echo $CLASSPATH;

executorport=`cat $yanagishima_dir/conf/yanagishima.properties | grep executor.port | cut -d = -f 2`
serverpath=`pwd`

if [ -z $YANAGISHIMA_OPTS ]; then
  YANAGISHIMA_OPTS=-Xmx3G
fi

java $YANAGISHIMA_OPTS -cp $CLASSPATH yanagishima.server.YanagishimaServer -conf $yanagishima_dir/conf $@ &

echo $! > $yanagishima_dir/currentpid
