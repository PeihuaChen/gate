#!/bin/sh
# list.sh - make a pretty list of the gate source, missing class files etc.
# ___AUTHOR___, ___DATE___
# $Id$ 

clear
cd ../../
find Template -print | \
  grep -v '^Template/lib/' | sed 's,Template/lib,Template/lib/...,' | \
  grep -v '^Template/doc/javadoc/' | \
  sed 's,Template/doc/javadoc,Template/doc/javadoc/...,' | \
  grep -v '^Template/misc/' | sed 's,Template/misc,Template/misc/...,' | \
  grep -v '^Template/classes/' | sed 's,Template/classes,Template/classes/...,' | \
  grep -v '^Template/bin/images/' | \
  grep -v 'Default.vfPackage' | \
  sed 's,Template/bin/images,Template/bin/images/...,' | \
  sort
