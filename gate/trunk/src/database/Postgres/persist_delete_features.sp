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


DROP FUNCTION persist_delete_features(int4,int2);
CREATE FUNCTION persist_delete_features(int4,int2) RETURNS boolean AS '

   DECLARE
      p_ent_id     alias for $1;
      p_ent_type   alias for $2;

   BEGIN

      delete from t_feature
      where  ft_entity_id = p_ent_id
      and    ft_entity_type = p_ent_type;

      /* dummy */
      return true;

   END;
'
LANGUAGE 'plpgsql'
