#!/bin/sh
# addjars.sh - conc jar files onto an existing jar
# ___AUTHOR___, ___DATE___
# $Id$

# usage
USAGE1="usage: $0 jarToUpdate jarsToAdd"
USAGE2=""
[ $# -lt 2 ] && { echo $USAGE1; echo $USAGE2; exit 1; }

case `pwd` in
  *build) ;;
  *build/) ;;
  *) echo must be run in the build dir; exit 2 ;;
esac

# the command to run, e.g. java or javac
JARTOUPDATE=$1
shift

for f in $*
do
  BASE=`echo $f |sed 's,.*/,,'`
  rm -rf $$.$BASE
  mkdir $$.$BASE
  cp $f $$.$BASE
  cd $$.$BASE
  jar xf $BASE
  rm $BASE
  rm -rf META-INF
  ../jdk.sh jar uf ../$JARTOUPDATE *
  cd ..
  rm -rf $$.$BASE
done
