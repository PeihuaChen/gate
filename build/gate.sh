#!/bin/sh
# gate.sh - UNIX front-end for GATE. Hamish, summer 98
# $Id$

# if the correct version of java isn't in your path, or in the
# JAVA_HOME variable, then set JAVA_HOME to the correct location here

if [ x${JAVA_HOME} != x ]
then
  JAVA=${JAVA_HOME}/bin/java
else
  JAVA=java
fi

case `uname` in
  CYGWIN*) CYGPATH="cygpath -w"; CYGPATHP="cygpath -wp" ;;
  *) CYGPATH=echo; CYGPATHP=echo ;;
esac

${JAVA} -Djava.ext.dirs=`${CYGPATH} ../lib/ext` -classpath `${CYGPATHP} gate.jar:../lib/ext/guk.jar` -Xmx200m gate.Main $*
