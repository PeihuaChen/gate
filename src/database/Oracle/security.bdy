/*
 *  security.bdy
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 18/Sep/2001
 *
 *  $Id$
 *
 */

create or replace package body security is

  -- Private type declarations
/*  type <TypeName> is <Datatype>;

  -- Private constant declarations
  <ConstantName> constant <Datatype> := <Value>;

  -- Private variable declarations
  <VariableName> <Datatype>;

  -- Function and procedure implementations
  function <FunctionName>(<Parameter> <Datatype>) return <Datatype> is
    <LocalVariable> <Datatype>;
  begin
    <Statement>;
    return(<Result>);
  end;
*/

  /*******************************************************************************************/
  procedure set_group_name(p_group_id  IN number,
                           p_new_name  IN varchar2)
  is
  
  begin
       update t_group
       set grp_name = p_new_name
       where grp_id = p_group_id;
  end;                                                                                                        


  /*******************************************************************************************/
  procedure add_user_to_group(p_group_id  IN number,
                              p_user_id   IN number)
  is
  
  begin
       insert into t_user_group(ugrp_id,
                                ugrp_user_id,
                                ugrp_group_id)
       values (gateusr.seq_user_group.nextval,
               p_user_id,
               p_group_id);                                                                
  end;                                                                                                        

  
  /*******************************************************************************************/
  procedure remove_user_from_group(p_group_id  IN number,
                                   p_user_id   IN number)
  is
  
  begin
       delete from t_user_group
       where ugrp_user_id = p_user_id
             and ugrp_group_id = p_group_id;
  end;                                                                                                        


  /*******************************************************************************************/
  procedure set_user_name(p_user_id  IN number,
                          p_new_name IN varchar2)
  is
  
  begin
       update t_user
       set    usr_login = p_new_name
       where  usr_id = p_user_id;
  end;                                                                                                        


  /*******************************************************************************************/
  procedure set_user_password(p_user_id  IN number,
                              p_new_pass IN varchar2)
  is
  
  begin
       update t_user
       set    usr_pass = p_new_pass
       where  usr_id = p_user_id;
  end;                                                                                                        


  /*******************************************************************************************/
  procedure create_group(p_grp_name  IN varchar2,
                         p_grp_id    OUT number)
  is
    grp_cnt number;
  begin
  
    select count(grp_name)
    into   grp_cnt
    from   t_group
    where  grp_name = p_grp_name;
  
    if (grp_cnt > 0) then
       raise x_duplicate_group_name;
    end if;  
    
    insert into t_group(grp_id,
                        grp_name)
    values(seq_group.nextval,
           p_grp_name)
    returning grp_id into p_grp_id;
       
  end;                                                                                                        

  /*******************************************************************************************/
  procedure delete_group(p_grp_id  IN number)
  is
  
  begin
       -- delete group users from t_user_group
       delete from t_user_group
       where  ugrp_group_id = p_grp_id;
       
       --set LRs owned by group as orphan
       update t_lang_resource
       set    lr_owner_id = null
       where  lr_owner_id = p_grp_id;
       
       --delete the group
       delete from t_group
       where grp_id = p_grp_id;
  end;                                                                                                        

  /*******************************************************************************************/
  procedure create_user(p_usr_name  IN varchar2,
                        p_usr_pass  IN varchar2,
                        p_usr_id    OUT number)
  is
    usr_cnt number;
  begin
  
    select count(usr_login)
    into   usr_cnt
    from   t_user
    where  usr_login = p_usr_name;
  
    if (usr_cnt > 0) then
       raise x_duplicate_user_name;
    end if;  
    
    insert into t_user(usr_id,
                       usr_login,
                       usr_pass)
    values(seq_user.nextval,
           p_usr_name,
           p_usr_pass)
    returning usr_id into p_usr_id;
       
  end;                                                                                                        

  /*******************************************************************************************/
  procedure delete_user(p_usr_id  IN number)
  is
  
  begin
       -- delete user from t_user_group
       delete from t_user_group
       where  ugrp_user_id = p_usr_id;
       
       --set LRs locked by group as orphan
       update t_lang_resource
       set    lr_locking_user_id = null
       where  lr_locking_user_id = p_usr_id;
       
       --delete the user
       delete from t_user
       where usr_id = p_usr_id;
  end;                                                                                                        


  /*******************************************************************************************/
  procedure login(p_usr_name        IN varchar2,
                  p_usr_pass        IN varchar2,
                  p_pref_grp_id     IN number)
  is
    usr_cnt number;  
  begin
       --valid user?
       select count(usr_id)
       into   usr_cnt
       from   t_user
       where  usr_login = p_usr_name;
       
       if (usr_cnt = 0) then
          raise x_invalid_user_name;
       end if;

       --valid passw?
       select count(usr_id)
       into   usr_cnt
       from   t_user
       where  usr_pass= p_usr_pass;
       
       if (usr_cnt = 0) then
          raise x_invalid_user_pass;
       end if;

       --valid group?
       select count(ugrp_id)
       into   usr_cnt
       from   t_user_group UG,
              t_user U
       where  UG.ugrp_group_id = p_pref_grp_id
              and UG.ugrp_user_id = U.usr_id
              and U.usr_login = p_usr_name;
       
       
       if (usr_cnt = 0) then
          raise x_invalid_user_pass;
       end if;
       
  end;                                                                                                        
  
/*begin
  -- Initialization
  <Statement>; */
end security;
/
