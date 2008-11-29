#!/bin/sh
# adapted from ant starting script
PRG="$0"
# need this for relative symlinks
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done
GATE_HOME=`dirname "$PRG"`/..
# make it fully qualified
export GATE_HOME=`cd "$GATE_HOME" && pwd`
cd $GATE_HOME 
exec bin/ant run "$@"
