/*
 *  test.bdy
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 25/Sep/2001
 *
 *  $Id$
 *
 */


create or replace package body test is


  GROUP_1_ID constant number := 101;
  GROUP_2_ID constant number := 102;
  
  USER_1_ID constant number := 1;
  USER_2_ID constant number := 2;
  USER_3_ID constant number := 3;
  USER_4_ID constant number := 4;
  USER_5_ID constant number := 5;

  UG_ID_1 constant number := 301;
  UG_ID_2 constant number := 302;  
  UG_ID_3 constant number := 303;
  UG_ID_4 constant number := 304;
  UG_ID_5 constant number := 305;
  UG_ID_6 constant number := 306;
  UG_ID_7 constant number := 307;
  
  DOC_1_ID constant number := 501;
  DOC_2_ID constant number := 502;
  DOC_3_ID constant number := 503;
  DOC_4_ID constant number := 504;
  DOC_5_ID constant number := 505;
  
  LR_TYPE_1_ID constant number := 1;
  
  --ACCESS_read_write
  ACCESS_WORLD_GROUP constant number := 1;
  ACCESS_GROUP_GROUP constant number := 2;  
  ACCESS_GROUP_OWNER constant number := 3;
  ACCESS_OWNER_OWNER constant number := 4;

  /*******************************************************************************************/
  procedure create_test_data
  is
  
  begin

    --first remove the previous test data
    test.remove_test_data();
  
    -- create groups
 
     insert into t_group(grp_id,
                         grp_name)
     values (GROUP_1_ID,'English Language Group');
                                            
     insert into t_group(grp_id,
                         grp_name)
     values (GROUP_2_ID,'Suahili Group');
 
 


     -- create users
 
     insert into t_user(usr_id,
                        usr_login,
                        usr_pass)
     values(USER_1_ID,'hamish','sesame');

     insert into t_user(usr_id,
                        usr_login,
                        usr_pass)
     values(USER_2_ID,'kalina','sesame');

     insert into t_user(usr_id,
                        usr_login,
                        usr_pass)
     values(USER_3_ID,'diana','sesame');
 
     insert into t_user(usr_id,
                        usr_login,
                        usr_pass)
     values(USER_4_ID,'valentin','sesame');
 
     insert into t_user(usr_id,
                        usr_login,
                        usr_pass)
     values(USER_5_ID,'cristian','sesame');

 


     -- put users in groups
 
     insert into t_user_group(ugrp_id,
                              ugrp_user_id,
                              ugrp_group_id)
     values(UG_ID_1,USER_1_ID,GROUP_1_ID);

     insert into t_user_group(ugrp_id,
                              ugrp_user_id,
                              ugrp_group_id)
     values(UG_ID_2,USER_2_ID,GROUP_1_ID);
 
     insert into t_user_group(ugrp_id,
                              ugrp_user_id,
                              ugrp_group_id)
     values(UG_ID_3,USER_3_ID,GROUP_1_ID);
 
     insert into t_user_group(ugrp_id,
                              ugrp_user_id,
                              ugrp_group_id)
     values(UG_ID_4,USER_4_ID,GROUP_1_ID);


     insert into t_user_group(ugrp_id,
                              ugrp_user_id,
                              ugrp_group_id)
     values(UG_ID_5,USER_2_ID,GROUP_2_ID);

     insert into t_user_group(ugrp_id,
                              ugrp_user_id,
                              ugrp_group_id)
     values(UG_ID_6,USER_3_ID,GROUP_2_ID);

     insert into t_user_group(ugrp_id,
                              ugrp_user_id,
                              ugrp_group_id)
     values(UG_ID_7,USER_5_ID,GROUP_2_ID);
                 
                    
     -- create documents
 
     insert into t_lang_resource(lr_id,
                                 lr_type_id,
                                 lr_owner_group_id,
                                 lr_owner_user_id,                                 
                                 lr_name,
                                 lr_access_mode,
                                 lr_parent_id)
     values(DOC_1_ID,LR_TYPE_1_ID,GROUP_1_ID,null,'doc1',ACCESS_WORLD_GROUP,null);                             


     insert into t_lang_resource(lr_id,
                                 lr_type_id,
                                 lr_owner_group_id,
                                 lr_owner_user_id,                                 
                                 lr_name,
                                 lr_access_mode,
                                 lr_parent_id)
     values(DOC_2_ID,LR_TYPE_1_ID,null,USER_2_ID,'doc2',ACCESS_OWNER_OWNER,null);                             

     insert into t_lang_resource(lr_id,
                                 lr_type_id,
                                 lr_owner_group_id,
                                 lr_owner_user_id,                                 
                                 lr_name,
                                 lr_access_mode,
                                 lr_parent_id)
     values(DOC_3_ID,LR_TYPE_1_ID,GROUP_2_ID,null,'doc3',ACCESS_GROUP_GROUP,null);                             

     insert into t_lang_resource(lr_id,
                                 lr_type_id,
                                 lr_owner_group_id,
                                 lr_owner_user_id,                                 
                                 lr_name,
                                 lr_access_mode,
                                 lr_parent_id)
     values(DOC_4_ID,LR_TYPE_1_ID,GROUP_2_ID,USER_5_ID,'doc4',ACCESS_GROUP_OWNER,null);                             

     insert into t_lang_resource(lr_id,
                                 lr_type_id,
                                 lr_owner_group_id,
                                 lr_owner_user_id,                                 
                                 lr_name,
                                 lr_access_mode,
                                 lr_parent_id)
     values(DOC_5_ID,LR_TYPE_1_ID,GROUP_1_ID,null,'doc5',ACCESS_GROUP_GROUP,null);                             

  end;                                                                                                        



  /*******************************************************************************************/
  procedure remove_test_data
  is
  
  begin

     -- delete documents
 
     delete from t_lang_resource
     where  lr_id in (DOC_1_ID,DOC_2_ID,DOC_3_ID,DOC_4_ID,DOC_5_ID);


     -- remove users in groups
 
     delete from t_user_group
     where ugrp_id in (UG_ID_1,UG_ID_2,UG_ID_3,UG_ID_4,UG_ID_5,UG_ID_6,UG_ID_7);

    -- delete groups
 
     delete from t_group
     where grp_id in (GROUP_1_ID,GROUP_2_ID);

     -- delete users
 
     delete from t_user
     where  usr_id in (USER_1_ID,USER_2_ID,USER_3_ID,USER_4_ID,USER_5_ID);                                  

  end;                                                                                                        


  /*******************************************************************************************/
  procedure remove_all_data
  is
  
  begin

     -- delete documents
     delete from t_lang_resource;

     -- remove users in groups
     delete from t_user_group
     where ugrp_id > 0;

    -- delete groups
     delete from t_group
     where grp_id >0;

     -- delete users
     delete from t_user
     where usr_id >0;

  end;                                                                                                        
  
/*begin
  -- Initialization
  <Statement>; */
end test;
/
