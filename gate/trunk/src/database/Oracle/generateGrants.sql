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

/*
 *  usage:
 *
 *  sqlplus USER/PASS@SID @generateGrants.sql
 *  
 *  where USER is a user with SELECT_CATALOG_ROLE granted
 *  so that he can access the ALL_xxx views
 *  (use SYSTEM if u're lazy)
 *  
 *  the result.sql file contains all the grant statemments 
 *  plus some junk lines (containing 'XX') that shouls be 
 *  removed manually (or with script)
 *  
 */
 
 
spool result.sql;
/

 
select 'grant select,insert,update,delete on ' || owner || '.' || table_name || ' to GATEUSER' 
as xx
from sys.all_tables
where owner='GATEADMIN'
union select 'grant select on ' || sequence_owner || '.' || sequence_name || ' to GATEUSER;' 
as xx
from sys.all_sequences
where sequence_owner='GATEADMIN'
union select 'grant select on ' || owner || '.' || view_name || ' to GATEUSER;' 
as xx
from sys.all_views
where owner='GATEADMIN'
union select 'grant execute on ' || owner || '.' || object_name || ' to GATEUSER;' 
as xx
from sys.all_objects
where owner='GATEADMIN'
      and object_type = 'PACKAGE';
/


spool off;
/