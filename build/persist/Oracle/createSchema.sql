
@createDB_Oracle.sql
pause >>>>>> Tables and sequences created. Press ENTER to continue...
clear buffer


@../../../src/database/Oracle/error.spc 
pause >>>>>> Package ERROR created. Press ENTER to continue...
clear buffer

@../../../src/database/Oracle/security.spc 
@../../../src/database/Oracle/security.bdy
pause >>>>>> Package SECURITY created. Press ENTER to continue...
clear buffer

@../../../src/database/Oracle/persist.spc 
@../../../src/database/Oracle/persist.bdy
pause >>>>>> Package PERSIST created. Press ENTER to continue...
clear buffer

@../../../src/database/Oracle/triggers.trg 
pause >>>>>> Triggers created. Press ENTER to continue...
clear buffer

@initData_Oracle.sql 
pause >>>>>> Lookup tables initialized. Press ENTER to continue...
clear buffer

PROMPT
PROMPT DONE!
PROMPT
