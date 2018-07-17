#!/bin/bash

if [ $# -ne 2 ]; then
  echo "usage: $0 db result"
  exit 1
fi

db=$1
result=$2

yanagishima_dir=$(dirname $0)/..

for file in $yanagishima_dir/lib/*.jar;
do
  CLASSPATH=$CLASSPATH:$file
done

java -cp $CLASSPATH yanagishima.migration.MigrateV14 $db $result
