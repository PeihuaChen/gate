/*
 *  DDL script for Oracle 8.x
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 19/Sep/2001
 * 
 *  auto generated: Wed Nov 07 20:42:12 2001
 *
 *  $Id$
 *
 */


CREATE UNIQUE INDEX XT_ANNOT_SET_01 ON T_ANNOT_SET
(
       AS_DOC_ID                      ,
       AS_NAME                        
) tablespace GATE01IS;

CREATE UNIQUE INDEX XT_ANNOTATION_01 ON T_ANNOTATION
(
       ANN_DOC_ID                     ,
       ANN_LOCAL_ID                   
) tablespace GATE01IS;

CREATE INDEX XT_ANNOTATION_02 ON T_ANNOTATION
(
       ANN_STARTNODE_ID               
) tablespace GATE01IS;

CREATE INDEX XT_ANNOTATION_03 ON T_ANNOTATION
(
       ANN_ENDNODE_ID                 
) tablespace GATE01IS;

CREATE UNIQUE INDEX XT_ANNOTATION_TYPE_01 ON T_ANNOTATION_TYPE
(
       AT_NAME                        
) tablespace GATE01IS;

CREATE UNIQUE INDEX XT_AS_ANNOTATION_02 ON T_AS_ANNOTATION
(
       ASANN_ANN_ID                   
) tablespace GATE01IS;

CREATE INDEX XT_AS_ANNOTATION_01 ON T_AS_ANNOTATION
(
       ASANN_AS_ID                    
) tablespace GATE01IS;

CREATE UNIQUE INDEX XT_DOC_ENCODING_01 ON T_DOC_ENCODING
(
       ENC_NAME                       
) tablespace GATE01IS;

CREATE UNIQUE INDEX XT_DOCUMENT_01 ON T_DOCUMENT
(
       DOC_LR_ID                      
) tablespace GATE01IS;

CREATE INDEX XT_FEATURE_01 ON T_FEATURE
(
       FT_ENTITY_ID                   ,
       FT_ENTITY_TYPE                 
) tablespace GATE01IS;

CREATE UNIQUE INDEX XT_GROUP_01 ON T_GROUP
(
       GRP_NAME                       
) tablespace GATE01IS;

CREATE INDEX XT_LANG_RESOURCE_01 ON T_LANG_RESOURCE
(
       LR_LOCKING_USER_ID             
) tablespace GATE01IS;

CREATE INDEX XT_LANG_RESOURCE_02 ON T_LANG_RESOURCE
(
       LR_OWNER_GROUP_ID              
) tablespace GATE01IS;

CREATE INDEX XT_LANG_RESOURCE_03 ON T_LANG_RESOURCE
(
       LR_OWNER_USER_ID               
) tablespace GATE01IS;

CREATE UNIQUE INDEX XT_LR_TYPE_01 ON T_LR_TYPE
(
       LRTP_TYPE                      
) tablespace GATE01IS;

CREATE UNIQUE INDEX XT_NODE_01 ON T_NODE
(
       NODE_DOC_ID                    ,
       NODE_LOCAL_ID                  
) tablespace GATE01IS;

CREATE UNIQUE INDEX XT_PARAMETER_01 ON T_PARAMETER
(
       PAR_KEY                        
) tablespace GATE01IS;

CREATE UNIQUE INDEX XT_USER_01 ON T_USER
(
       USR_LOGIN                      
) tablespace GATE01IS;

CREATE INDEX XT_USER_02 ON T_USER
(
       USR_PASS                       
) tablespace GATE01IS;

CREATE UNIQUE INDEX XT_USER_GROUP_01 ON T_USER_GROUP
(
       UGRP_USER_ID                   ,
       UGRP_GROUP_ID                  
) tablespace GATE01IS;

CREATE INDEX XT_USER_GROUP_02 ON T_USER_GROUP
(
       UGRP_GROUP_ID                  
) tablespace GATE01IS;



