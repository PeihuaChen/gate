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
 *  auto generated: Mon Oct 15 17:36:34 2001
 *
 *  $Id$
 *
 */


DROP INDEX XT_GROUP_01;

CREATE UNIQUE INDEX XT_GROUP_01 ON T_GROUP
(
       GRP_NAME                       
);

DROP INDEX XT_USER_GROUP_01;

CREATE UNIQUE INDEX XT_USER_GROUP_01 ON T_USER_GROUP
(
       UGRP_USER_ID                   ,
       UGRP_GROUP_ID                  
);



