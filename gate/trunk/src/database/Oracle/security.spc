create or replace package security is

/*
 *  security.pck
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
 *      $Id$
 */
  
  -- Public type declarations
/*  type <TypeName> is <Datatype>;
  
  -- Public constant declarations
  <ConstantName> constant <Datatype> := <Value>;

  -- Public variable declarations
  <VariableName> <Datatype>;

  -- Public function and procedure declarations
  function <FunctionName>(<Parameter> <Datatype>) return <Datatype>;
*/


  /* Group related functionality */
  
  /*  -- */
  procedure set_group_name(p_group_id  IN number,
                           p_new_name  IN varchar2);

  /*  -- */                           
  procedure add_user_to_group(p_group_id  IN number,
                              p_user_id   IN number);
                           
  /*  -- */                           
  procedure remove_user_from_group(p_group_id  IN number,
                                   p_user_id   IN number);

  /* User related functionality */                                   
  /*  -- */
  procedure set_user_name(p_user_id  IN number,
                          p_new_name IN varchar2);

  /*  -- */
  procedure set_user_password(p_user_id  IN number,
                              p_new_pass IN varchar2);
                           
end security;
/
