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

CREATE SEQUENCE "SEQ_FT_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20;

CREATE SEQUENCE "SEQ_FK_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

CREATE SEQUENCE "SEQ_USR_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

CREATE SEQUENCE "SEQ_GRP_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

CREATE SEQUENCE "SEQ_UGRP_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

CREATE SEQUENCE "SEQ_ENC_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

CREATE SEQUENCE "SEQ_DC_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

CREATE SEQUENCE "SEQ_LRTP_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;

CREATE SEQUENCE "SEQ_LR_ID"
       start 1
       increment 1
       maxvalue 2147483647
       minvalue 1
       cache 20 ;
