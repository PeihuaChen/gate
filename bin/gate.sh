#!/bin/sh
# gate.sh - UNIX front-end for GATE binary distributions. Hamish, May 2001
# $Id$

E=""
[ x$1 = x-debug ] && E=echo


# set NLS_LANG for Oracle
NLS_LANG=AMERICAN_AMERICA.UTF8

# set GATE_HOME to the parent directory if not already set.
[ x${GATE_HOME} = x ] &&  GATE_HOME=`(cd .. && pwd)`


# set TOOLSJAR to where we hope tools.jar is are located
[ x${TOOLSJAR} = x ] &&
[ -f ${JAVA_HOME}/lib/tools.jar ] && TOOLSJAR=${JAVA_HOME}/lib/tools.jar

[ x${TOOLSJAR} = x ] && TOOLSJAR=${GATE_HOME}/bin/tools14.jar


# set GATE_CONFIG to where we think gate.xml is (or "" if not around)
# if not already set
[ x${GATE_CONFIG} = x ] [ -f ${GATE_HOME}/bin/gate.xml ] &&
GATE_CONFIG=${GATE_HOME}/bin/gate.xml


# set GATEJAR and EXTDIR to gate.jar and ext locations
[ x${GATEJAR} = x ] && [ -f ${GATE_HOME}/bin/gate.jar ] &&
GATEJAR=${GATE_HOME}/bin/gate.jar

[ x${GATEJAR} = x ] && [ -f ${GATE_HOME}/build/gate.jar ] &&
GATEJAR=${GATE_HOME}/build/gate.jar

EXTDIR=

[ x${EXTDIR} = x ] &&
[ -f ${GATE_HOME}/bin/ext/guk.jar ] && EXTDIR=${GATE_HOME}/bin/ext

[ x${EXTDIR} = x ] &&
[ -f ${GATE_HOME}/lib/ext/guk.jar ] && EXTDIR=${GATE_HOME}/lib/ext


# set JAVA
[ x${JAVA} = x ] &&
[ -f ${JAVA_HOME}/bin/java ] && JAVA=${JAVA_HOME}/bin/java

[ x${JAVA} = x ] &&
[ -f ${JAVA_HOME}/bin/java.exe ] && JAVA=${JAVA_HOME}/bin/java.exe

[ x${JAVA} = x ] &&
[ -f ${GATE_HOME}/jre1.4/bin/java ] && JAVA=${GATE_HOME}/jre1.4/bin/java

[ x${JAVA} = x ] &&
[ -f ${GATE_HOME}/jre1.4/bin/java.exe ] && JAVA=${GATE_HOME}/jre1.4/bin/java.exe

[ x${JAVA} = x ] && JAVA=java


# set CLASSPATH
OLD_CLASSPATH=$CLASSPATH
CLASSPATH="${GATEJAR}:${TOOLSJAR}:$CLASSPATH"


# munge filenames if we're on cygwin
CYG=false
case `uname` in CYGWIN*) CYG=true ;; esac
if [ $CYG = true ]
then
  # change all the vars except JAVA (cygwin uses it to start java)
  [ x$GATE_CONFIG != x ] && GATE_CONFIG=`cygpath -w $GATE_CONFIG`
  GATEJAR=`cygpath -w $GATEJAR`
  EXTDIR=`cygpath -w $EXTDIR`
  CLASSPATH="`cygpath -w -p ${CLASSPATH}`;${OLD_CLASSPATH}"
fi


# if we have a site gate.xml set a var including the -i
FLAGS=""
if [ x$GATE_CONFIG != x ]
then
  FLAGS="-i $GATE_CONFIG"
fi


# run the beast
$E ${JAVA} -Xmx200m \
  -Djava.ext.dirs=${EXTDIR} -classpath $CLASSPATH gate.Main $FLAGS $*
