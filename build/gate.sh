#!/bin/sh
# gate.sh - UNIX front-end for GATE 2. Hamish, summer 98
# $Id$

# if the correct version of java isn't in your path, or in the
# JAVA_HOME variable, then set JAVA_HOME to the correct location here:
#JAVA_HOME='w:\jdk\jdk1.3\bin

JAVA=java

if [ x$JAVA_HOME != x ]
then
  JAVA=${JAVA_HOME}/bin/java
