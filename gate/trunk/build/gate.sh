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

${JAVA} -Xmx200m -Dsicstus.path=/opt/sicstus/lib/sicstus-3.8.6 -Djava.library.path=/opt/sicstus/lib/sicstus-3.8.6/lib:/opt/sicstus/lib -Djava.ext.dirs=`${CYGPATH} ../lib/ext` -classpath `${CYGPATHP} /opt/sicstus/lib/sicstus-3.8.6/bin/jasper.jar:gate.jar:../lib/ext/guk.jar` gate.Main $*
