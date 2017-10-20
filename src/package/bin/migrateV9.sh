#!/bin/bash

if [ $# -ne 2 ]; then
  echo "usage: $0 src dest"
  exit 1
fi

src=$1
dest=$2

yanagishima_dir=$(dirname $0)/..

for file in $yanagishima_dir/lib/*.jar;
do
  CLASSPATH=$CLASSPATH:$file
done

java -cp $CLASSPATH yanagishima.migration.MigrateV9 $src $dest
