#!/bin/sh
# list.sh
# $Id$

clear
cd ../../
find gate2 -print | \
  grep -v '^gate2/lib/' | sed 's,gate2/lib,gate2/lib/...,' | \
  grep -v '^gate2/doc/' | sed 's,gate2/doc/,gate2/doc/...,' | \
  grep -v '^gate2/classes/' | sed 's,gate2/classes,gate2/classes/...,' | \
  grep -v '^gate2/bin/images/' | \
  grep -v 'Default.vfPackage' | \
  sed 's,gate2/bin/images,gate2/bin/images/...,' | \
  sort
