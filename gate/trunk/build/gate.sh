#!/bin/sh
# gate.sh - UNIX front-end for GATE 2. Hamish, summer 98
# $Id$

make run
exit

# what are we running on?
case `uname -a` in
  CYGWIN*) SYS=cygwin ;;
  *) SYS=unix ;;
esac

CLASSPATH=\
GATEBASE/gate/lib/swingall.jar:\
GATEBASE/gate/classes:\
GATEBASE/gate/lib/jbcl2.0-res.jar:\
GATEBASE/gate/lib/jbcl2.0.jar:\
GATEBASE/gate/lib/jgl3.1.0.jar:\
GATEBASE/gate/lib:\
GATEBASE/creole2/gazetteer/classes:\
GATEBASE/creole2/tokeniser/classes:\
GATEBASE/creole2/chunker/classes:\
GATEBASE/creole2/namematcher/classes:\
GATEBASE/creole2/directunlookup/classes:\
GATEBASE/creole2/directlookup/classes:\
GATEBASE/creole2/lottie/classes:\
GATEBASE/creole2/jane/classes

# adjust classpath for platform, and add GATE location
if [ $SYS = cygwin ]
then
  GATEBASE='z:\code'
  CLASSPATH=`echo $CLASSPATH |sed -e 's,/,\\\\,g' -e 's,:,;,g'`
  CLASSPATH=`echo $CLASSPATH |sed -e 's,GATEBASE,'${GATEBASE}',g'`
  JAVABASE='//w/jdk/jdk1.2'
  CLASSPATH="W:\\jdk\\jdk1.2\\jre\\lib\\rt.jar;${CLASSPATH}"
elif [ $SYS=unix ] 
then
  # GATEBASE=/usr/local/pgk/gate
  GATEBASE=/share/nlp/projects/aventinus/AVIE3
  CLASSPATH=`echo $CLASSPATH |sed -e 's,GATEBASE,'${GATEBASE}',g'`
  THREADS_FLAG=native
  LD_LIBRARY_PATH=\
/usr/local/lib/sparc/native_threads:/usr/local/lib:${LD_LIBRARY_PATH}
  export LD_LIBRARY_PATH
  JAVABASE=/usr/local/pkg/jdk/jdk1.2fcs
  CLASSPATH="${JAVABASE}/jre/lib/rt.jar:${CLASSPATH}"
fi

if [ x${GATE_CREOLE_PATH} = x ]
then
  GATE_CREOLE_PATH=${GATEBASE}/creole2
else
  GATE_CREOLE_PATH=${GATE_CREOLE_PATH}:${GATEBASE}/creole2
fi

set -x
PATH=${JAVABASE}/bin:$PATH \
${JAVABASE}/bin/java -mx200m -noclassgc -classpath ${CLASSPATH} \
  gate.Main -p ${GATE_CREOLE_PATH} $*

