#!/bin/sh
# checkSql.sh
# $Id$

# find instances of constants in sql and java (disregard case)
#
# looks at all .sql and .spc files in ../src and all .java
# files in ../src/gate/persist

CONSTANTS=\
"invalid_user_pass incomplete_data"

JAVA=`find ../src/gate/persist -name '*.java' -print`
SQL=\
"`find ../src -name '*.sql' -print` `find ../src -name '*.spc' -print`"

for c in $CONSTANTS
do
  echo looking for $c...
  SQL_E=`grep -i "$c,.*[0-9]" $SQL |sed 's,.*\(-[0-9]*\)).*,\1,`
  JAVA_E=`grep -i "$c.*=" $JAVA |sed 's,.*=\(.*\);,\1,`
  echo SQL constant value = $SQL_E
  echo JAVA constant value = $JAVA_E
  echo
done
