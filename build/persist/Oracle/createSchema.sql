
@createTable.sql
pause >>>>>> Tables successfully  created. Press ENTER to continue...
clear buffer

@createSequence.sql
pause >>>>>> Sequences successfully  created. Press ENTER to continue...
clear buffer

@createIndex.sql
pause >>>>>> Indexes successfully  created. Press ENTER to continue...
clear buffer
 
@../../../src/database/Oracle/views.sql
pause >>>>>> Views successfully  created. Press ENTER to continue...
clear buffer

@../../../src/database/Oracle/error.spc 
pause >>>>>> Package ERROR successfully  created. Press ENTER to continue...
clear buffer

@../../../src/database/Oracle/security.spc 
@../../../src/database/Oracle/security.bdy
pause >>>>>> Package SECURITY successfully  created. Press ENTER to continue...
clear buffer

@../../../src/database/Oracle/persist.spc 
@../../../src/database/Oracle/persist.bdy
pause >>>>>> Package PERSIST successfully  created. Press ENTER to continue...
clear buffer

@../../../src/database/Oracle/triggers.trg 
pause >>>>>> Triggers successfully  created. Press ENTER to continue...
clear buffer

@initData_Oracle.sql 
pause >>>>>> Lookup tables successfully  initialized. Press ENTER to continue...
clear buffer

@grants.sql 
pause >>>>>> Access to GATEADMIN objects successfully  granted to GATEUSER. Press ENTER to continue...
clear buffer

PROMPT
PROMPT DONE!
PROMPT
