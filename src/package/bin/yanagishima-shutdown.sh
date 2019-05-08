#!/bin/bash
bin=$(cd "$(dirname $0)"; pwd)
. "${bin}/yanagishima-config.sh"

proc=$(cat "$YANAGISHIMA_HOME"/currentpid)
echo "killing YanagishimaServer"
kill $proc

cat /dev/null > "$YANAGISHIMA_HOME"/currentpid
