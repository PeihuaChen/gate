create or replace package security is

/*
 *  security.pck
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 18/Sep/2001
 *
 *      $Id$
 */
  

  PERM_WR_GW constant number := 1;
  PERM_GR_GW constant number := 2;  
  PERM_GR_OW constant number := 3;
  PERM_OR_OW constant number := 4;
    
  /* exceptions */

  
  /* Group related functionality */
  
  /*  -- */
  procedure set_group_name(p_group_id  IN number,
                           p_new_name  IN varchar2);

  /*  -- */                           
  procedure add_user_to_group(p_group_id  IN number,
                              p_user_id   IN number);
                           
  /*  -- */                           
  procedure remove_user_from_group(p_group_id  IN number,
                                   p_user_id   IN number);

  /* User related functionality */                                   
  /*  -- */
  procedure set_user_name(p_user_id  IN number,
                          p_new_name IN varchar2);

  /*  -- */
  procedure set_user_password(p_user_id  IN number,
                              p_new_pass IN varchar2);

  
  /* AccessController related functionality */
                                
  /*  -- */
  procedure create_group(p_grp_name  IN varchar2,
                         p_grp_id    OUT number);

  /*  -- */
  procedure create_user(p_usr_name  IN varchar2,
                        p_usr_pass IN varchar2,
                        p_usr_id    OUT number);
                         

  /*  -- */
  procedure delete_group(p_grp_id    IN number);

  /*  -- */
  procedure delete_user(p_usr_id    IN number);

  /*  -- */
  procedure login(p_usr_name        IN varchar2,
                  p_usr_pass        IN varchar2,
                  p_pref_grp_id     IN number);

  /*  -- */
  procedure has_access_to_lr(p_lr_id   IN  number,
                             p_usr_id  IN  number,
                             p_grp_id  IN  number,
                             p_mode    IN  number,                             
                             p_result  OUT boolean);

  /*  -- */                           
  procedure can_delete_group(p_grp_id     IN  number,
                             p_result     OUT boolean);

  /*  -- */                           
  procedure can_delete_user(p_usr_id     IN  number,
                            p_result     OUT boolean);
                           
end security;
/
