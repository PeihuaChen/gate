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

CREATE OR REPLACE FUNCTION persist_create_feature(int4,int2,varchar,int4,varchar,int2) RETURNS int4 AS '

   DECLARE
      p_entity_id           alias for $1;
      p_entity_type         alias for $2;
      p_key                 alias for $3;
      p_value_number        alias for $4;
      p_value_varchar       alias for $5;
      p_value_type          alias for $6;

      l_feature_key_id int4;
      cnt int4;

      x_invalid_feature_type varchar = ''x_invalid_feature_type'';

   BEGIN
      if (false = persist_is_valid_feature_type(p_value_type)) then
         raise exception ''%'',x_invalid_feature_type;
      end if;
  
      /* 1. find feature_key id */
      select fk_id
      into   l_feature_key_id
      from   t_feature_key
      where  fk_string = p_key;

      if not FOUND then
         /* 2. if there is no such key then create one and get the id */
         insert into t_feature_key(fk_id,
                                   fk_string)
         values(nextval(''SEQ_FK_ID''),
                p_key);

         l_feature_key_id := currval(''SEQ_FK_ID'');

      end if;

      insert into t_feature(ft_id,
                            ft_entity_id,
                            ft_entity_type,
                            ft_key_id,
                            ft_number_value,
                            ft_binary_value,
                            ft_character_value,
                            ft_long_character_value,
                            ft_value_type)
      values(nextval(''SEQ_FT_ID''),
             p_entity_id,
             p_entity_type,
             l_feature_key_id,
             p_value_number,
             null /*empty_blob()*/,
             p_value_varchar,
             null /*empty_clob()*/,
             p_value_type);

      return currval(''SEQ_FT_ID'');

   END;
'
LANGUAGE 'plpgsql'
