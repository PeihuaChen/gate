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

DROP SEQUENCE "SEQ_FT_ID";
CREATE SEQUENCE "SEQ_FT_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20;

DROP SEQUENCE "SEQ_FK_ID";
CREATE SEQUENCE "SEQ_FK_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE  "SEQ_USR_ID";
CREATE SEQUENCE "SEQ_USR_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE   "SEQ_GRP_ID";
CREATE SEQUENCE "SEQ_GRP_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE   "SEQ_UGRP_ID";
CREATE SEQUENCE "SEQ_UGRP_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE   "SEQ_ENC_ID";
CREATE SEQUENCE "SEQ_ENC_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE   "SEQ_DC_ID";
CREATE SEQUENCE "SEQ_DC_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE   "SEQ_LRTP_ID";
CREATE SEQUENCE "SEQ_LRTP_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE   "SEQ_LR_ID";
CREATE SEQUENCE "SEQ_LR_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE   "SEQ_DOC_ID";
CREATE SEQUENCE "SEQ_DOC_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE   "SEQ_NODE_GLOBAL_ID";
CREATE SEQUENCE "SEQ_NODE_GLOBAL_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE   "SEQ_AT_ID";
CREATE SEQUENCE "SEQ_AT_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE "SEQ_ANN_GLOBAL_ID";
CREATE SEQUENCE "SEQ_ANN_GLOBAL_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE "SEQ_AS_ID";
CREATE SEQUENCE "SEQ_AS_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE "SEQ_ASANN_ID";
CREATE SEQUENCE "SEQ_ASANN_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE "SEQ_CORP_ID";
CREATE SEQUENCE "SEQ_CORP_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;


DROP SEQUENCE "SEQ_CD_ID";
CREATE SEQUENCE "SEQ_CD_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

DROP SEQUENCE "SEQ_PAR_ID";
CREATE SEQUENCE "SEQ_PAR_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;
