#!/bin/sh
# jdk.sh - crossplatform jdk invocation


# REDUNDANT: replaced with CYGPATH stuff in Makefile


#
# This is a bit of a mess; it would best be replaced with some
# judicious use of "cygpath". But it works, so....
#
# To modify: edit the settings of JAVABASE* in the big if clause near
# the bottom. The first case is for CYGWIN, and the last for UNIX
# where a path is set, and the middle one for UNIX where JAVA_HOME
# is set.
#
# How it works:
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
  Linux*) SYS=linux ;;
  *) SYS=unix ;;
esac

# process -n
if [ x$1 = x-n ]
then
  SILENT=yes
  shift
fi

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
  JAVABASE=//w/jdk/jdk1.3
  JAVABASE1='w:\jdk\jdk1.3'
  JAVABASE2='w:\\jdk\\jdk1.3'
#  JAVABASE=//w/jdk/jdk1.2
#  JAVABASE1='w:\jdk\jdk1.2'
#  JAVABASE2='w:\\jdk\\jdk1.2'
#  JAVABASE=//w/apps/jdk1.2.2
#  JAVABASE1='w:\apps\jdk1.2.2'
elif [ $SYS=linux ]
then
  JAVABASE=$JAVA_HOME
elif [ $SYS=unix ] 
then
  JAVABASE=/usr/bin
fi

# convert from UNIX paths to Windoze paths if necessary; do ___JAVABASE___
if [ $SYS = cygwin ]
then
  SUBS=`echo $SUBS |sed -e 's,\\\/,___SLASH___,g'`
  SUBS=`echo $SUBS |sed -e 's,:,;,g'`
  SUBS=`echo $SUBS |sed -e 's,___COLON___,:,g'`
  SUBS=`echo $SUBS |sed -e "s,___JAVABASE___,${JAVABASE2},g"`
  SUBS=`echo $SUBS |sed -e 's,/,\\\\,g'`
  SUBS=`echo $SUBS |sed -e 's,___SLASH___,/,g'`
fi

# run the command
[ x$SILENT = x ] && echo ${JAVABASE}/bin/${COMMAND} ${COMMOPTS} ${SUBS}
${JAVABASE}/bin/${COMMAND} ${COMMOPTS} ${SUBS}
