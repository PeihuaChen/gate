#!/bin/sh
# gate.sh - UNIX front-end for GATE binary distributions. Hamish, May 2001
# $Id$

E=""
[ x$1 = x-debug ] && E=echo

# set LOCAT to where we hope gate.jar etc. are located
if [ x$GATE_HOME != x ]
then
  LOCAT=$GATE_HOME/bin
else
  LOCAT=`(cd \`dirname $0\` && pwd)`
fi

# set SITEGATEXML to where we thing gate.xml is (or "" if not around)
SITEGATEXML=""
if [ x$GATE_CONFIG != x ]
then
  SITEGATEXML="$GATE_CONFIG"
else
  [ -f $LOCAT/gate.xml ] && SITEGATEXML="$LOCAL/gate.xml"
fi

# set GATEJAR and GUK to gate.jar and ext locations
GATEJAR=${LOCAT}/gate.jar
GUK=${LOCAT}/ext
[ ! -f $GATEJAR ] && GATEJAR=${LOCAT}/../build/gate.jar
[ ! -f $GUK/guk.jar ] && GUK=${LOCAT}/../lib/ext

# set JAVA
JAVA=$JAVA_HOME/bin/java
[ ! -f $JAVA ] && JAVA=java

# set CLASSPATH
OLD_CLASSPATH=$CLASSPATH
CLASSPATH="${GATEJAR}:$CLASSPATH"

# munge filenames if we're on cygwin
CYG=false
case `uname` in CYGWIN*) CYG=true ;; esac
if [ $CYG = true ]
then
  # change all the vars except JAVA (cygwin uses it to start java)
  LOCAT=`cygpath -w $LOCAT`
  [ x$SITEGATEXML != x ] && SITEGATEXML=`cygpath -w $SITEGATEXML`
  GATEJAR=`cygpath -w $GATEJAR`
  GUK=`cygpath -w $GUK`
  CLASSPATH="`cygpath -w -p ${GATEJAR}`;${OLD_CLASSPATH}"
fi

# if we have a site gate.xml set a var including the -i
FLAGS=""
if [ x$SITEGATEXML != x ]
then
  FLAGS="-i $SITEGATEXML"
fi

# run the beast
$E ${JAVA} -Xmx200m \
  -Djava.ext.dirs=${GUK} -classpath $CLASSPATH gate.Main $FLAGS $* 

