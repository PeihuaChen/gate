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


DROP TABLE t_feature_key ;

CREATE TABLE  t_feature_key  (
    fk_id          int4 NOT NULL DEFAULT NEXTVAL('seq_feature_key'),
    fk_string      varchar(128) NOT NULL,
 PRIMARY KEY ( fk_id )
);


DROP TABLE  t_user ;

CREATE TABLE  t_user  (
    usr_id         int4 NOT NULL DEFAULT NEXTVAL('seq_user'),
    usr_login      varchar(16) NOT NULL,
    usr_pass       varchar(16) NOT NULL,
 PRIMARY KEY ( usr_id )
);


DROP TABLE  t_group ;

CREATE TABLE  t_group  (
    grp_id         int4 NOT NULL DEFAULT NEXTVAL('seq_group'),
    grp_name       varchar(128) NOT NULL,
 PRIMARY KEY ( grp_id )
);


DROP TABLE  t_user_group ;

CREATE TABLE  t_user_group  (
    ugrp_id            int4 DEFAULT nextval('seq_user_group')  NOT NULL ,
    ugrp_user_id       int4 NOT NULL ,
    ugrp_group_id      int4 NOT NULL ,
   FOREIGN KEY ( ugrp_user_id )
         REFERENCES  t_user ( usr_id )
         MATCH FULL ,
   FOREIGN KEY ( ugrp_group_id )
         REFERENCES  t_group ( grp_id )
         MATCH FULL ,
   PRIMARY KEY ( ugrp_id )
);


DROP TABLE  t_doc_encoding ;

CREATE TABLE  t_doc_encoding  (
    enc_id         int4 NOT NULL DEFAULT NEXTVAL('seq_doc_encoding'),
    enc_name       varchar(16) NOT NULL,
 PRIMARY KEY ( enc_id )
);

DROP TABLE  t_doc_content ;

CREATE TABLE  t_doc_content  (
    dc_id              int4 DEFAULT nextval('seq_doc_content')  NOT NULL ,
    dc_encoding_id     int4,
    dc_character_content  text,
    dc_binary_content  oid,
    dc_content_type    int2 NOT NULL ,
   FOREIGN KEY ( dc_encoding_id )
      REFERENCES  t_doc_encoding ( enc_id )
      MATCH FULL ,
   PRIMARY KEY ( dc_id )
);

DROP TABLE  t_feature ;

CREATE TABLE  t_feature  (
    ft_id              int4 DEFAULT nextval('seq_feature')  NOT NULL ,
    ft_entity_id       int4 NOT NULL ,
    ft_entity_type     int2 NOT NULL ,
    ft_key_id          int4 NOT NULL ,
    ft_number_value    int4,
    ft_binary_value    oid,
    ft_character_value  varchar(4000),
    ft_long_character_value  text,
    ft_value_type      int2 NOT NULL ,
   FOREIGN KEY ( ft_key_id )
      REFERENCES  t_feature_key ( fk_id )
      MATCH FULL ,
   PRIMARY KEY ( ft_id )
);

DROP TABLE  t_lr_type ;

CREATE TABLE  t_lr_type  (
    lrtp_id        int4 NOT NULL DEFAULT NEXTVAL('seq_lr_type'),
    lrtp_type      varchar(128) NOT NULL,
 PRIMARY KEY ( lrtp_id )
);


DROP TABLE  t_lang_resource ;

CREATE TABLE  t_lang_resource  (
    lr_id              int4 DEFAULT nextval('seq_lang_resource')  NOT NULL ,
    lr_owner_user_id   int4,
    lr_owner_group_id  int4,
    lr_locking_user_id  int4,
    lr_type_id         int4 NOT NULL ,
    lr_name            varchar(128) NOT NULL ,
    lr_access_mode     int2 NOT NULL ,
    lr_parent_id       int4,
   FOREIGN KEY ( lr_parent_id )
      REFERENCES  t_lang_resource ( lr_id )
      MATCH FULL ,
   FOREIGN KEY ( lr_locking_user_id )
      REFERENCES  t_user ( usr_id )
      MATCH FULL ,
   FOREIGN KEY ( lr_owner_user_id )
      REFERENCES  t_user ( usr_id )
      MATCH FULL ,
   FOREIGN KEY ( lr_owner_group_id )
      REFERENCES  t_group ( grp_id )
      MATCH FULL ,
   PRIMARY KEY ( lr_id )
);


DROP TABLE  t_document ;

CREATE TABLE  t_document  (
    doc_id             int4 DEFAULT nextval('seq_document')  NOT NULL ,
    doc_content_id     int4,
    doc_lr_id          int4 NOT NULL ,
    doc_url            varchar(4000) NOT NULL ,
    doc_start          int4,
    doc_end            int4,
    doc_is_markup_aware  bool NOT NULL ,
   FOREIGN KEY ( doc_content_id )
      REFERENCES  t_doc_content ( dc_id )
      MATCH FULL ,
   FOREIGN KEY ( doc_lr_id )
      REFERENCES  t_lang_resource ( lr_id )
      MATCH FULL ,
   PRIMARY KEY ( doc_id )
);


DROP TABLE  t_node ;

CREATE TABLE  t_node  (
    node_global_id     int4 DEFAULT nextval('seq_node')  NOT NULL ,
    node_doc_id        int4 NOT NULL ,
    node_local_id      int4 NOT NULL ,
    node_offset        int4 NOT NULL ,
   FOREIGN KEY ( node_doc_id )
      REFERENCES  t_document ( doc_id )
      MATCH FULL ,
   PRIMARY KEY ( node_global_id )
);


DROP TABLE  t_annotation_type ;

CREATE TABLE  t_annotation_type  (
    at_id          int4 NOT NULL DEFAULT NEXTVAL('seq_annotation_type'),
    at_name        varchar(128) NULL,
   PRIMARY KEY ( at_id )
);


DROP TABLE  t_annotation ;

CREATE TABLE  t_annotation  (
    ann_global_id      int4 DEFAULT nextval('seq_annotation')  NOT NULL ,
    ann_doc_id         int4,
    ann_local_id       int4 NOT NULL ,
    ann_at_id          int4 NOT NULL ,
    ann_startnode_id   int4 NOT NULL ,
    ann_endnode_id     int4 NOT NULL ,
   FOREIGN KEY ( ann_doc_id )
      REFERENCES  t_document ( doc_id )
      MATCH FULL ,
   FOREIGN KEY ( ann_at_id )
      REFERENCES  t_annotation_type ( at_id )
      MATCH FULL ,
   FOREIGN KEY ( ann_startnode_id )
      REFERENCES  t_node ( node_global_id )
      MATCH FULL ,
   FOREIGN KEY ( ann_endnode_id )
      REFERENCES  t_node ( node_global_id )
      MATCH FULL ,
   PRIMARY KEY ( ann_global_id )
);


DROP TABLE  t_annot_set ;

CREATE TABLE  t_annot_set  (
    as_id              int4 DEFAULT nextval('seq_annot_set')  NOT NULL ,
    as_name            varchar(128) NOT NULL ,
    as_doc_id          int4 NOT NULL ,
   FOREIGN KEY ( as_doc_id )
      REFERENCES  t_document ( doc_id )
      MATCH FULL ,
   PRIMARY KEY ( as_id )
);

DROP TABLE  t_as_annotation ;

CREATE TABLE  t_as_annotation  (
    asann_id           int4 DEFAULT nextval('seq_as_annotation')  NOT NULL ,
    asann_ann_id       int4 NOT NULL ,
    asann_as_id        int4 NOT NULL ,
   FOREIGN KEY ( asann_ann_id )
      REFERENCES  t_annotation ( ann_global_id )
      MATCH FULL ,
   FOREIGN KEY ( asann_as_id )
      REFERENCES  t_annot_set ( as_id )
      MATCH FULL ,
   PRIMARY KEY ( asann_id )
);


DROP TABLE  t_corpus ;

CREATE TABLE  t_corpus  (
    corp_id            int4 DEFAULT nextval('seq_corpus')  NOT NULL ,
    corp_lr_id         int4 NOT NULL ,
   FOREIGN KEY ( corp_lr_id )
      REFERENCES  t_lang_resource ( lr_id )
      MATCH FULL ,
   PRIMARY KEY ( corp_id )
);


DROP TABLE  t_corpus_document ;

CREATE TABLE  t_corpus_document  (
    cd_id              int4 DEFAULT nextval('seq_corpus_document')  NOT NULL ,
    cd_corp_id         int4 NOT NULL ,
    cd_doc_id          int4 NOT NULL ,
   FOREIGN KEY ( cd_corp_id )
      REFERENCES  t_corpus ( corp_id )
      MATCH FULL ,
   FOREIGN KEY ( cd_doc_id )
      REFERENCES  t_document ( doc_id )
      MATCH FULL ,
   PRIMARY KEY ( cd_id )
);


DROP TABLE  t_parameter ;

CREATE TABLE  t_parameter  (
    par_id         int4 NOT NULL DEFAULT NEXTVAL('seq_parameter'),
    par_key        varchar(16) NOT NULL,
    par_value_string  varchar(128) NULL,
    par_value_date  date NULL,
    par_value_number  int4 NULL,
   PRIMARY KEY ( par_id )
);
