
spool install.log
whenever sqlerror continue

set termout     off
start createTable.sql
set termout     on
prompt >>>>>> Tables successfully  created...
clear buffer

set termout     off
@createSequence.sql
set termout     on
prompt  >>>>>> Sequences successfully  created...
clear buffer

set termout     off
@createIndex.sql
set termout     on
prompt >>>>>> Indexes successfully  created...
clear buffer
 
set termout     off 
@createView.sql
set termout     on
prompt >>>>>> Views successfully  created...
clear buffer

set termout     off
@../../../src/database/Oracle/error.spc 
set termout     on
prompt >>>>>> Package ERROR successfully  created...
clear buffer

set termout     off
@../../../src/database/Oracle/security.spc 
@../../../src/database/Oracle/security.bdy
set termout     on
prompt >>>>>> Package SECURITY successfully  created...
clear buffer

set termout     off
@../../../src/database/Oracle/persist.spc 
@../../../src/database/Oracle/persist.bdy
set termout     on
prompt >>>>>> Package PERSIST successfully  created...
clear buffer

set termout     off
@../../../src/database/Oracle/triggers.trg 
set termout     on
prompt >>>>>> Triggers successfully  created...
clear buffer

set termout     off
@initData_Oracle.sql 
set termout     on
prompt >>>>>> Lookup tables successfully  initialized...
clear buffer

set termout     off
@grants.sql 
set termout     on
prompt >>>>>> Access to GATEADMIN objects successfully  granted to GATEUSER...
clear buffer

spool off

PROMPT
PROMPT DONE!
PROMPT

