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
  
/*begin
  -- Initialization
  <Statement>; */
end security;
/
