#!/bin/sh
# gate.sh - UNIX front-end for GATE binary distributions. Hamish, May 2001
# $Id$

E=""
[ x$1 = x-debug ] && E=echo

LOCAT=`dirname $0`
GATEJAR=${LOCAT}/gate.jar
GUK=${LOCAT}/ext
[ ! -f $GATEJAR ] && GATEJAR=${LOCAT}/../build/gate.jar
[ ! -f $GUK ] && GUK=${LOCAT}/../lib/ext

JAVA=$JAVA_HOME/bin/java
[ ! -f $JAVA ] && JAVA=java

$E ${JAVA} -Xmx200m \
  -Djava.ext.dirs=${GUK} \
  -classpath ${GATEJAR}:$CLASSPATH gate.Main $*
