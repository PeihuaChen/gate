/*
 *  persist.spc
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

create or replace package persist is
  
  -- Public type declarations
/*  type <TypeName> is <Datatype>;
  
  -- Public constant declarations
  <ConstantName> constant <Datatype> := <Value>;

  -- Public variable declarations
  <VariableName> <Datatype>;

  -- Public function and procedure declarations
  function <FunctionName>(<Parameter> <Datatype>) return <Datatype>;
*/

  procedure get_timestamp(p_timestamp  OUT number);

  procedure get_lr_name(p_lr_id     IN number,
                        p_lr_name   OUT varchar2);
  
  procedure delete_lr(p_lr_id     IN number,
                      p_lr_type   IN varchar2);

    
end persist;
/
