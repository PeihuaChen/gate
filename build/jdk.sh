#!/bin/sh
# jdk.sh - crossplatform jdk invocation
#
# We get called with e.g. a java or javac command in UNIX style,
# then, depending on platform, we do:
#   path prefixing on $1
#   sed 's,:,;,g' on $* 
#   sed 's,/,\\\\,g' on $* 
# Options that contain e.g. ':' (like -g:none to suppress debugging
# output to class files, which gains about a 15% reduction in size) must
# be placed following a -s as argument 2 (e.g. within 's to suppress
# separation). These will be copied verbatim into the command.
# Where the string ___JAVABASE___ occurs in the command, it will be
# substituted for the location of the JDK.
#
# $Id$

# usage
USAGE1="usage: $0 [ -s special_options ] command options/files"
USAGE2="(special options are ones containing e.g. ':' and are used verbatim)"
[ $# -lt 2 ] && { echo $USAGE1; echo $USAGE2; exit 1; }

# what are we running on?
case `uname -a` in
  CYGWIN*) SYS=cygwin ;;
  *) SYS=unix ;;
esac

# process -s
if [ x$1 = x-s ]
then
  COMMOPTS=$2
  shift; shift
fi

# the command to run, e.g. java or javac
COMMAND=$1
shift

# the part of the command that needs substitutions
SUBS=$*

# where to find Java
if [ $SYS = cygwin ]
then
  JAVABASE=//w/apps/jdk1.2.2
  JAVABASE1='w:\apps\jdk1.2.2'
elif [ $SYS=unix ] 
then
  JAVABASE=/usr/local/pkg/jdk/jdk1.2fcs
fi

# convert from UNIX paths to Windoze paths if necessary; do ___JAVABASE___
if [ $SYS = cygwin ]
then
  SUBS=`echo $SUBS |sed -e 's,:,;,g'`
  SUBS=`echo $SUBS |sed -e "s,___JAVABASE___,${JAVABASE1},g"`
  SUBS=`echo $SUBS |sed -e 's,/,\\\\,g'`
fi

# run the command
echo ${JAVABASE}/bin/${COMMAND} ${COMMOPTS} ${SUBS}
${JAVABASE}/bin/${COMMAND} ${COMMOPTS} ${SUBS}
