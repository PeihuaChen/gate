

start createSchema.sql

spool dev.log
whenever sqlerror continue

set termout     off
@../../../src/database/Oracle/test.spc 
@../../../src/database/Oracle/test.bdy
set termout     on
prompt >>>>>> Package TEST successfully  created...
clear buffer

set termout     off
exec test.create_test_data();
commit;
set termout     on
prompt >>>>>> test data successfully  inserted...
clear buffer


spool off

