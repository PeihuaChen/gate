#!/bin/sh
# gate.sh - UNIX front-end for GATE binary distributions. Hamish, May 2001
# $Id$

LOCAT=`basename $0`
GATEJAR=${LOCAT}/gate.jar
GUKJAR=${LOCAT}/ext/guk.jar
[ ! -f $GATEJAR ] && GATEJAR=${LOCAT}/../build/gate.jar
[ ! -f $GUKJAR ] && GUKJAR=${LOCAT}/../lib/ext/guk.jar

JAVA=$JAVA_HOME/bin/java
[ ! -f $JAVA ] && JAVA=java

${JAVA} -Xmx200m \
  -Djava.ext.dirs=${GUKJAR} \
  -classpath ${GATEJAR}/gate.jar:$CLASSPATH gate.Main $*
