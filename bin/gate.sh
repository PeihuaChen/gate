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
# When CDPATH is set, the cd command prints out the dir name. Because of this
# wee need to execute the cd command separately, and only then get the value
# via `pwd`
cd "$GATE_HOME"
export GATE_HOME=`pwd`
export ANT_HOME=$GATE_HOME
cd $GATE_HOME

exec bin/ant run "$@"
