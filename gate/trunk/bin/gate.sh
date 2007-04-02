#!/bin/sh

GATE_BIN_DIR=`dirname $0`
export GATE_HOME=`dirname $GATE_BIN_DIR`
cd $GATE_HOME 

exec bin/ant run
