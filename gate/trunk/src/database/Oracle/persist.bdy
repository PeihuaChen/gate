/*
 *  persist.bdy
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
 *
 */


create or replace package body persist is

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
  procedure get_timestamp(p_timestamp  OUT number)
  is
  
  begin
       select SEQ_TIMESTAMP.nextval
       into p_timestamp
       from dual;
  end;                                                                                                        


  /*******************************************************************************************/
  procedure get_lr_name(p_lr_id     IN number,
                        p_lr_name   OUT varchar2)
  is
  
  begin
       select lr_name
       into p_lr_name
       from t_lang_resource;

  exception
       when NO_DATA_FOUND then
          raise error.x_invalid_lr;

  end;                                                                                                        


/*begin
  -- Initialization
  <Statement>; */
end persist;
/
