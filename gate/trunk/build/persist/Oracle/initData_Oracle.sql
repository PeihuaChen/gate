/*
 *  
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 27/Sep/2001
 * 
 *  $Id$
 *
 */

     -- init the LR_TYPE table
     
     insert into t_lr_type(lrtp_id,lrtp_type)
     values(1,'gate.corpora.DocumentImpl');
     
     --
     insert into t_lr_type(lrtp_id,lrtp_type)
     values(2,'gate.corpora.CorpusImpl');

     
     --create the ADMIN user and group
     insert into t_user(usr_id,
                        usr_login,
                        usr_pass)
     values (0,
             'ADMIN',
             'sesame');
             
     --
     insert into t_group(grp_id,
                         grp_name)
     values (0,
             'ADMINS');
             
     --
     insert into t_user_group(ugrp_id,
                              ugrp_user_id,
                              ugrp_group_id)
     values (0,0,0);
     
     --commit;                                        