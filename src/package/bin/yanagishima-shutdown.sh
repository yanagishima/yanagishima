#!/bin/bash
yanagishima_dir=$(dirname $0)/..

proc=`cat $yanagishima_dir/currentpid`
echo "killing YanagishimaServer"
kill $proc

cat /dev/null > $yanagishima_dir/currentpid