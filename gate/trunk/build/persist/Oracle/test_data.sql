/*
 *  test_data.sql
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 24/Sep/2001
 *
 *  $Id$
 *
 */

 
 -- create groups
 
 insert into t_group(grp_id,
                     grp_name)
 values (101,'English Language Group');
                                            
 insert into t_group(grp_id,
                     grp_name)
 values (102,'Suahili Group');
 
 
 -- create users
 
 insert into t_user(usr_id,
                    usr_login,
                    usr_pass)
 values(1,'hamish','sesame');

 insert into t_user(usr_id,
                    usr_login,
                    usr_pass)
 values(2,'kalina','sesame');

 insert into t_user(usr_id,
                    usr_login,
                    usr_pass)
 values(3,'diana','sesame');
 
 insert into t_user(usr_id,
                    usr_login,
                    usr_pass)
 values(4,'valentin','sesame');
 
 insert into t_user(usr_id,
                    usr_login,
                    usr_pass)
 values(5,'cristian','sesame');

 
 -- put users in groups
 
 insert into t_user_group(ugrp_id,
                          ugrp_user_id,
                          ugrp_group_id)
 values(301,1,101);

 insert into t_user_group(ugrp_id,
                          ugrp_user_id,
                          ugrp_group_id)
 values(302,2,101);
 
 insert into t_user_group(ugrp_id,
                          ugrp_user_id,
                          ugrp_group_id)
 values(303,3,101);
 
 insert into t_user_group(ugrp_id,
                          ugrp_user_id,
                          ugrp_group_id)
 values(304,4,101);


 insert into t_user_group(ugrp_id,
                          ugrp_user_id,
                          ugrp_group_id)
 values(305,2,102);

 insert into t_user_group(ugrp_id,
                          ugrp_user_id,
                          ugrp_group_id)
 values(306,3,102);

 insert into t_user_group(ugrp_id,
                          ugrp_user_id,
                          ugrp_group_id)
 values(307,5,102);
                 
                       
 -- create documents
 
/* insert into t_lang_resource(lr_id,
                             lr_type_id,
                             lr_owner_id,
                             lr_name,
                             lr_is_private,
                             lr_parent_id)
*/                             
