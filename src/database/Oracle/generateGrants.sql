/*
 *  generateGrants.sql
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 28/Sep/2001
 *
 *  $Id$
 */

spool result.sql;
/

 
select 'grant select,insert,update,delete on ' || owner || '.' || table_name || ' to GATEUSER' 
as x
from sys.all_tables
where owner='GATEADMIN';
/


select 'grant select on ' || sequence_owner || '.' || sequence_name || ' to GATEUSER' 
as x
from sys.all_sequences
where sequence_owner='GATEADMIN';
/


select 'grant select on ' || owner || '.' || view_name || ' to GATEUSER' 
from sys.all_views
where owner='GATEADMIN';
/


select 'grant execute on ' || owner || '.' || object_name || ' to GATEUSER' 
from sys.all_objects
where owner='GATEADMIN'
      and object_type = 'PACKAGE';
/


spool off;
/