create or replace package body persist is

/*
 *  persist.bdy
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
 *  $Id$
 *
 */

  /*******************************************************************************************/
  procedure get_timestamp(p_timestamp  OUT number)
  is
  
  begin
       select SEQ_TIMESTAMP.nextval
       into p_timestamp
       from dual;
  end;                                                                                                        


  /*******************************************************************************************/
  procedure get_lr_name(p_lr_id     IN number,
                        p_lr_name   OUT varchar2)
  is
  
  begin
       select lr_name
       into p_lr_name
       from t_lang_resource;

  exception
       when NO_DATA_FOUND then
          raise error.x_invalid_lr;

  end;                                                                                                        

  /*******************************************************************************************/
  procedure delete_lr(p_lr_id     IN number,
                      p_lr_type   IN varchar2)
  is
  
  begin
     raise error.x_not_implemented;
  end;                                                                                                        


  /*******************************************************************************************/
  procedure create_lr(p_usr_id           IN number,
                      p_grp_id           IN number,
                      p_lr_type          IN varchar2,
                      p_lr_name          IN varchar2,
                      p_lr_permissions   IN number,
                      p_lr_parent_id     IN number,
                      p_lr_id            OUT number)
  is
  
  begin
     raise error.x_not_implemented;
  end;                                                                                                        


  /*******************************************************************************************/
  procedure create_document(p_lr_id        IN number,
                            p_url          IN varchar2,
                            p_start_offset IN number,
                            p_end_offset   IN number,
                            p_is_mrk_aware IN boolean,
                            p_corpus_id    IN number,
                            p_doc_id       OUT number,
                            p_content_id   OUT number)
  is
  
  begin
     raise error.x_not_implemented;
  end;                                                                                                        
  
  
/*begin
  -- Initialization
  <Statement>; */
end persist;
/
