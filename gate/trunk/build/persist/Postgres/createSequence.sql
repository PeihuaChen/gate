/*
 *  DDL script for PostgreSQL 7.2
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 12/Mar/2002
 *
 *
 *  $Id$
 *
 */

drop sequence  seq_ft_id ;
create sequence  seq_ft_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20;

drop sequence  seq_fk_id ;
create sequence  seq_fk_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence   seq_usr_id ;
create sequence  seq_usr_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence    seq_grp_id ;
create sequence  seq_grp_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence    seq_ugrp_id ;
create sequence  seq_ugrp_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence    seq_enc_id ;
create sequence  seq_enc_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence    seq_dc_id ;
create sequence  seq_dc_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence    seq_lrtp_id ;
create sequence  seq_lrtp_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence    seq_lr_id ;
create sequence  seq_lr_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence    seq_doc_id ;
create sequence  seq_doc_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence    seq_node_global_id ;
create sequence  seq_node_global_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence    seq_at_id ;
create sequence  seq_at_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence  seq_ann_global_id ;
create sequence  seq_ann_global_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence  seq_as_id ;
create sequence  seq_as_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence  seq_asann_id ;
create sequence  seq_asann_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence  seq_corp_id ;
create sequence  seq_corp_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;


drop sequence  seq_cd_id ;
create sequence  seq_cd_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

drop sequence  seq_par_id ;
create sequence  seq_par_id
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;
