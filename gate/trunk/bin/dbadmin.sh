this will become the end-user create DB script...
$Id$



the makefile should be manually edited with the path to sqlplus and the
database Net8 description and the GATEADMIN password

then for "make oracle_db" the action is

cp $GATE/src/database/Oracle/*  $GATE/build/persist/Oracle/
cd $GATE/build/persist/Oracle
$SQLPLUS gateadmin/$PASS@$SERVICE_NAME @createSchema.sql

the generated log file (install.log) could be parsed for "ORA-" statements
that indicate errors

for the target for creating the test data  the action is

cp $GATE/src/database/Oracle/*  $GATE/build/persist/Oracle/
cd $GATE/build/persist/Oracle
$SQLPLUS gateadmin/$PASS@$SERVICE_NAME @createSchemaDev.sql

and the log is "dev.log"

notify me when the makefile is changed, so that I'll modify the sql scripts
to look for packages/scripts in the current directory and not under
/src/database
