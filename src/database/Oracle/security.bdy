create or replace package body security is

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

  
  READ_ACCESS constant   number := 0;
  WRITE_ACCESS constant number := 1;

  ORACLE_TRUE  constant number := 1;
  ORACLE_FALSE constant number := 0;
  
  
  /*******************************************************************************************/  
  function is_member_of_group(p_user_id number,p_grp_id number) 
     return boolean 
  is
    cnt number;
  begin
     
    select count(ugrp_id)
    into   cnt
    from   t_user_group
    where  ugrp_user_id = p_user_id
           and ugrp_group_id = p_grp_id;
  
    return (cnt > 0);
  end;
  
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
       values (seq_user_group.nextval,
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
       raise error.x_duplicate_group_name;
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
    has_documents boolean;
  begin
       -- check for documents
       -- if the group ownes documents then fail
       can_delete_group(p_grp_id,has_documents);
       
       if (has_documents = true) then
          raise error.x_group_owns_resources;
       end if;
  
       -- delete group users from t_user_group
       delete from t_user_group
       where  ugrp_group_id = p_grp_id;
/*       
       --set LRs owned by group as OWNER_READ/OWNER_WRITE
       update t_lang_resource
       set    lr_owner_group_id = null,
              lr_access_mode = PERM_OR_OW
       where  lr_owner_group_id = p_grp_id;
*/       
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
       raise error.x_duplicate_user_name;
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
     has_documents boolean;
  begin
       -- check for documents
       -- if the user owns documents then fail
       can_delete_group(p_usr_id,has_documents);
       
       if (has_documents = true) then
          raise error.x_user_owns_resources;
       end if;
  
       -- delete user from t_user_group
       delete from t_user_group
       where  ugrp_user_id = p_usr_id;
       
/*       --set LRs owned by user as orphan
       update t_lang_resource
       set    lr_owner_user_id = null,
              lr_access_mode = PERM_GR_GW
       where  lr_owner_user_id = p_usr_id;
*/

       -- unlock LRs locked by user 
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
          raise error.x_invalid_user_name;
       end if;

       --valid passw?
       select count(usr_id)
       into   usr_cnt
       from   t_user
       where  usr_pass= p_usr_pass;
       
       if (usr_cnt = 0) then
          raise error.x_invalid_user_pass;
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
          raise error.x_invalid_user_group;
       end if;
       
  end;                                                                                                        

  procedure has_access_to_lr(p_lr_id   IN  number,
                             p_usr_id  IN  number,
                             p_grp_id  IN  number,
                             p_mode    IN  number,                             
                             p_result  OUT boolean)
  is
    cnt          number;
    owner_group  number;
    owner_user   number;
    locking_user number;
    access_mode  number;
    
  begin
       
       --preconditions
       if (p_mode <> READ_ACCESS and p_mode <> WRITE_ACCESS) then
          raise error.x_invalid_argument;
       end if;
       
       select nvl(lr_owner_user_id,0),
              nvl(lr_owner_group_id,0),
              nvl(lr_locking_user_id,0),
              lr_access_mode
       into   owner_user, 
              owner_group,
              locking_user,
              access_mode
       from   t_lang_resource
       where  lr_id = p_lr_id;

       
       if (p_mode = WRITE_ACCESS) then
       
          --is the document locked?
          if locking_user <> 0 then 
             
             if locking_user <> p_usr_id then
                --locked by someone else, fail
                p_result := false;
             else
                --locked by me, success             
                p_result := true;
             end if;
             
             return;
          
          else                       
             --not locked but check permissions
             -- write access is granted :
             -- 1a. permissions are USER_WRITE and OWNER_USER == p_usr_id
             -- 1b. permissions are GROUP_WRITE and 
             --       member_of(p_usr_id,OWNER_GROUP) and 
             --       OWNER_GROUP == p_grp_id
             
             --user is owner, and permisssions are OWNER_WRITE
             if (owner_user = p_usr_id and 
                 (access_mode = PERM_GR_OW or access_mode = PERM_OR_OW)) then
                -- case 1a
                p_result := true;
                return;
             end if;
             
             --user is in owning group
             if (is_member_of_group(p_usr_id,owner_group) and
                 owner_group = p_grp_id                   and 
                 (access_mode = PERM_GR_GW or access_mode = PERM_WR_GW)) then
                -- case 1b                 
                p_result := true;
                return;
             end if;

             --fail             
             p_result := false;
             return;
                         
          end if;

       else   
          -- read access request
          -- check read persmissions
          -- read access is granted :
          -- 1a. permissions are USER_READ and OWNER_USER == p_usr_id
          -- 1b. permissions are GROUP_READ and member_of(p_usr_id,OWNER_GROUP)
          -- 1c. permissions are WORLD_READ
          
          if (access_mode = PERM_WR_GW) then
             -- case 1c
             p_result := true;
             return;             
          end if;
          
          if ((access_mode = PERM_GR_GW or access_mode = PERM_GR_OW) and 
               is_member_of_group(p_usr_id,owner_group)              and
               owner_group = p_grp_id)  then
             -- case 1b
             p_result := true;
             return;             
          end if;

          if (access_mode = PERM_WR_GW) then
             -- case 1c
             p_result := true;
             return;             
          end if;
          
          --fail
          p_result := false;
          return;                       
       
       end if;
       
       
       
       
   exception
       --do we have such LR?       
       when NO_DATA_FOUND then
          raise error.x_invalid_lr; 
              
  end;
  
  
  
  /*******************************************************************************************/
  procedure can_delete_group(p_grp_id     IN  number,
                             p_result     OUT boolean)
  is
    cnt number;  
  begin
  
     --if there are resources owned by group then fail
     select count(lr_owner_group_id)
     into   cnt
     from   t_lang_resource
     where  lr_owner_group_id = p_grp_id;
  
     p_result:= (cnt = 0);
          
  end;                                                                                                        

  /*******************************************************************************************/
  procedure can_delete_user(p_usr_id     IN  number,
                             p_result     OUT boolean)
  is
    cnt number;  
  begin
  
     --if there are resources owned by group then fail
     select count(lr_owner_user_id)
     into   cnt
     from   t_lang_resource
     where  lr_owner_user_id = p_usr_id;
  
     p_result:= (cnt = 0);
          
  end;                                                                                                        
  
/*begin
  -- Initialization
  <Statement>; */
end security;
/
