create or replace package persist is

/*
 *  persist.spc
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

  ENCODING_UTF constant varchar2(16) := 'UTF8';
  
  procedure get_timestamp(p_timestamp  OUT number);

  
  procedure get_lr_name(p_lr_id     IN number,
                        p_lr_name   OUT varchar2);
  
  
  procedure delete_lr(p_lr_id     IN number,
                      p_lr_type   IN varchar2);


  procedure create_lr(p_usr_id           IN number,
                      p_grp_id           IN number,
                      p_lr_type          IN varchar2,
                      p_lr_name          IN varchar2,
                      p_lr_permissions   IN number,
                      p_lr_parent_id     IN number,
                      p_lr_id            OUT number);

  
  procedure create_document(p_lr_id        IN number,
                            p_url          IN varchar2,
                            p_encoding     IN varchar2,
                            p_start_offset IN number,
                            p_end_offset   IN number,
                            p_is_mrk_aware IN number,
                            p_corpus_id    IN number,
                            p_doc_id       OUT number,
                            p_content_id   OUT number);

                            
  procedure create_annotation_set(p_doc_id           IN number,
                                  p_as_name          IN varchar2,
                                  p_as_id            OUT number);


  procedure create_annotation(p_doc_id           IN number,
                              p_as_id            IN number,
                              p_ann_start_offset IN number,  
                              p_ann_end_offset   IN number,                                
                              p_ann_type         IN varchar2,
                              p_ann_id           OUT number);

    
end persist;
/
