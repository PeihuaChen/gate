/*
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 22/Mar/2002
 *
 *  $Id$
 *
 */

DROP FUNCTION persist_change_content_type(int4,int2);
CREATE FUNCTION persist_change_content_type(int4,int2) RETURNS boolean AS '

   DECLARE
      p_cont_id alias for $1;
      p_new_type alias for $2;

      x_invalid_content_type varchar = ''x_invalid_content_type'';

      C_CHARACTER_CONTENT  int4 = 1;
      C_BINARY_CONTENT     int4 = 2;
      C_EMPTY_CONTENT      int4 = 3;


   BEGIN
     if (p_new_type not in (C_CHARACTER_CONTENT,
                            C_BINARY_CONTENT,
                            C_EMPTY_CONTENT)) then
                           
        raise error.x_invalid_content_type;
     end if;
    
     update t_doc_content
     set    dc_content_type = p_new_type
     where  dc_id = p_cont_id;

     /* dummy */
     return true;

   END;
'
LANGUAGE 'plpgsql'
