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


DROP TABLE "T_FEATURE_KEY";

CREATE TABLE "T_FEATURE_KEY" (
   "FK_ID"         int4 NOT NULL DEFAULT NEXTVAL('SEQ_FK_ID'),
   "FK_STRING"     varchar(128) NOT NULL,
 PRIMARY KEY ("FK_ID")
);


DROP TABLE "T_USER";

CREATE TABLE "T_USER" (
   "USR_ID"        int4 NOT NULL DEFAULT NEXTVAL('SEQ_USR_ID'),
   "USR_LOGIN"     varchar(16) NOT NULL,
   "USR_PASS"      varchar(16) NOT NULL,
 PRIMARY KEY ("USR_ID")
);


DROP TABLE "T_GROUP";

CREATE TABLE "T_GROUP" (
   "GRP_ID"        int4 NOT NULL DEFAULT NEXTVAL('SEQ_GRP_ID'),
   "GRP_NAME"      varchar(128) NOT NULL,
 PRIMARY KEY ("GRP_ID")
);


DROP TABLE "T_USER_GROUP";

CREATE TABLE "T_USER_GROUP" (
   "UGRP_ID"           int4 DEFAULT nextval('SEQ_UGRP_ID')  NOT NULL ,
   "UGRP_USER_ID"      int4 NOT NULL ,
   "UGRP_GROUP_ID"     int4 NOT NULL ,
   FOREIGN KEY ("UGRP_USER_ID")
         REFERENCES "T_USER"("USR_ID")
         MATCH FULL ,
   FOREIGN KEY ("UGRP_GROUP_ID")
         REFERENCES "T_GROUP"("GRP_ID")
         MATCH FULL ,
   PRIMARY KEY ("UGRP_ID")
);


DROP TABLE "T_DOC_ENCODING";

CREATE TABLE "T_DOC_ENCODING" (
   "ENC_ID"        int4 NOT NULL DEFAULT NEXTVAL('SEQ_ENC_ID'),
   "ENC_NAME"      varchar(16) NOT NULL,
 PRIMARY KEY ("ENC_ID")
);

DROP TABLE "T_DOC_CONTENT";

CREATE TABLE "T_DOC_CONTENT" (
   "DC_ID"             int4 DEFAULT nextval('SEQ_DC_ID')  NOT NULL ,
   "DC_ENCODING_ID"    int4,
   "DC_CHARACTER_CONTENT" text,
   "DC_BINARY_CONTENT" oid,
   "DC_CONTENT_TYPE"   int2 NOT NULL ,
   FOREIGN KEY ("DC_ENCODING_ID")
      REFERENCES "T_DOC_ENCODING"("ENC_ID")
      MATCH FULL ,
   PRIMARY KEY ("DC_ID")
);

DROP TABLE "T_FEATURE";

CREATE TABLE "T_FEATURE" (
   "FT_ID"             int4 DEFAULT nextval('SEQ_FT_ID')  NOT NULL ,
   "FT_ENTITY_ID"      int4 NOT NULL ,
   "FT_ENTITY_TYPE"    int2 NOT NULL ,
   "FT_KEY_ID"         int4 NOT NULL ,
   "FT_NUMBER_VALUE"   int4,
   "FT_BINARY_VALUE"   oid,
   "FT_CHARACTER_VALUE" varchar(4000),
   "FT_LONG_CHARACTER_VALUE" text,
   "FT_VALUE_TYPE"     int2 NOT NULL ,
   FOREIGN KEY ("FT_KEY_ID")
      REFERENCES "T_FEATURE_KEY"("FK_ID")
      MATCH FULL ,
   PRIMARY KEY ("FT_ID")
);

DROP TABLE "t_lr_type";
CREATE TABLE "t_lr_type" (
   "LRTP_ID"       int4 NOT NULL DEFAULT NEXTVAL('SEQ_LRTP_ID'),
   "LRTP_TYPE"     varchar(128) NOT NULL,
 PRIMARY KEY ("LRTP_ID")
);


DROP TABLE "T_LANG_RESOURCE";
CREATE TABLE "T_LANG_RESOURCE" (
   "LR_ID"             int4 DEFAULT nextval('SEQ_LR_ID')  NOT NULL ,
   "LR_OWNER_USER_ID"  int4,
   "LR_OWNER_GROUP_ID" int4,
   "LR_LOCKING_USER_ID" int4,
   "LR_TYPE_ID"        int4 NOT NULL ,
   "LR_NAME"           varchar(128) NOT NULL ,
   "LR_ACCESS_MODE"    int2 NOT NULL ,
   "LR_PARENT_ID"      int4,
   FOREIGN KEY ("LR_PARENT_ID")
      REFERENCES "T_LANG_RESOURCE"("LR_ID")
      MATCH FULL ,
   FOREIGN KEY ("LR_LOCKING_USER_ID")
      REFERENCES "T_USER"("USR_ID")
      MATCH FULL ,
   FOREIGN KEY ("LR_OWNER_USER_ID")
      REFERENCES "T_USER"("USR_ID")
      MATCH FULL ,
   FOREIGN KEY ("LR_OWNER_GROUP_ID")
      REFERENCES "T_GROUP"("GRP_ID")
      MATCH FULL ,
   PRIMARY KEY ("LR_ID")
);

