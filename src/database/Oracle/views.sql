/*
 *  views.sql
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 16/Oct/2001
 *
 *  $Id$
 *
 */


create or replace view V_LR as
  select *
  from   t_lang_resource a,
         t_lr_type       b
  where  a.lr_type_id = b.lrtp_id;
    
    
create or replace view V_DOCUMENT as
  select *
  from   t_document a;
    
    
create or replace view V_FEATURE as
  select *
  from   t_feature;
    
   