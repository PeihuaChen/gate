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
 *  auto generated: Tue Oct 16 12:59:57 2001
 *
 *  $Id$
 *
 */


CREATE UNIQUE INDEX XT_ANNOTATION_TYPE_01 ON T_ANNOTATION_TYPE
(
       AT_NAME                        
);

CREATE UNIQUE INDEX XT_DOC_ENCODING_01 ON T_DOC_ENCODING
(
       ENC_NAME                       
);

CREATE UNIQUE INDEX XT_GROUP_01 ON T_GROUP
(
       GRP_NAME                       
);

CREATE INDEX XT_LANG_RESOURCE_01 ON T_LANG_RESOURCE
(
       LR_LOCKING_USER_ID             
);

CREATE INDEX XT_LANG_RESOURCE_02 ON T_LANG_RESOURCE
(
       LR_OWNER_GROUP_ID              
);

CREATE INDEX XT_LANG_RESOURCE_03 ON T_LANG_RESOURCE
(
       LR_OWNER_USER_ID               
);

CREATE UNIQUE INDEX XT_LR_TYPE_01 ON T_LR_TYPE
(
       LRTP_TYPE                      
);

CREATE UNIQUE INDEX XT_USER_01 ON T_USER
(
       USR_LOGIN                      
);

CREATE INDEX XT_USER_02 ON T_USER
(
       USR_PASS                       
);

CREATE UNIQUE INDEX XT_USER_GROUP_01 ON T_USER_GROUP
(
       UGRP_USER_ID                   ,
       UGRP_GROUP_ID                  
);

CREATE INDEX XT_USER_GROUP_02 ON T_USER_GROUP
(
       UGRP_GROUP_ID                  
);



