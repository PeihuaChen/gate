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
       into   p_lr_name
       from   t_lang_resource
       where  lr_id = p_lr_id;

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
    l_lr_type number;
  begin
     
     -- 1. sanity check
     if (false = security.is_valid_security_data(p_lr_permissions,p_grp_id,p_usr_id)) then
        raise error.x_incomplete_data;
     end if;
     
     -- 3. check if the LR type supplied is valid
     select lrtp_id
     into   l_lr_type
     from   t_lr_type
     where  lrtp_type = p_lr_type;
     
     
     -- 2. create a lang_resource record
     insert into t_lang_resource(lr_id,
                                 lr_type_id,
                                 lr_owner_user_id,
                                 lr_locking_user_id,
                                 lr_owner_group_id,
                                 lr_name,
                                 lr_access_mode,
                                 lr_parent_id)
     values (seq_lang_resource.nextval,
            l_lr_type,
            p_usr_id,
            null,
            p_grp_id,
            p_lr_name,
            p_lr_permissions,
            p_lr_parent_id)
     returning lr_id into p_lr_id;           
     
     
     exception
        when NO_DATA_FOUND then
           raise error.x_invalid_lr_type;
           
     
  end;                                                                                                        


  /*******************************************************************************************/
  procedure create_document(p_lr_id        IN number,
                            p_url          IN varchar2,
                            p_encoding     IN varchar2,
                            p_start_offset IN number,
                            p_end_offset   IN number,
                            p_is_mrk_aware IN number,
                            p_corpus_id    IN number,
                            p_doc_id       OUT number,
                            p_content_id   OUT number)
  is
     l_encoding_id number;
     l_encoding varchar2(16);
     cnt number;
  begin
  
     -- -1. if encoding is null, then set it to UTF8
     l_encoding := p_encoding;
     if (l_encoding is null) then
        l_encoding := persist.ENCODING_UTF;
     end if;
  
     --0. get encoding ID if any, otherwise create a new
     -- entry in T_DOC_ENCODING
     select count(enc_id)
     into   cnt
     from   t_doc_encoding
     where  enc_name = l_encoding;         
     
     if (cnt = 0) then

       --oops new encoding
       --add it 
       insert into t_doc_encoding(enc_id,
                                  enc_name)
       values (seq_doc_encoding.nextval,
               l_encoding)
       returning enc_id into l_encoding_id;

     else

       --get encoding id
       select enc_id
       into   l_encoding_id
       from   t_doc_encoding
       where  enc_name = l_encoding;                

     end if;
     
     
     
  
     --1. create a document_content entry
     insert into t_doc_content(dc_id,
                               dc_encoding_id,
                               dc_character_content,
                               dc_binary_content,
                               dc_content_type)
     values(seq_doc_content.nextval,
            l_encoding_id,
            empty_clob(),
            empty_blob(),
            persist.EMPTY_CONTENT)
     returning dc_id into p_content_id;
     
     --2. create a document entry  
     insert into t_document(doc_id,
                            doc_content_id,
                            doc_lr_id,
                            doc_url,
                            doc_start,
                            doc_end,
                            doc_is_markup_aware)
     values(seq_document.nextval,
            p_content_id,
            p_lr_id,
            p_url,
            p_start_offset,
            p_end_offset,
            p_is_mrk_aware)
     returning doc_id into p_doc_id;
                 
     --3. if part of a corpus create a corpus_document entry
     if (p_corpus_id is not null) then
        insert into t_corpus_document(cd_id,
                                      cd_corp_id,
                                      cd_doc_id)
        values (seq_corpus_document.nextval,
                p_corpus_id,
                p_doc_id);                
     end if;     
                                     
  end;                                                                                                        
  

  /*******************************************************************************************/
  procedure create_annotation_set(p_doc_id           IN number,
                                  p_as_name          IN varchar2,
                                  p_as_id            OUT number)
  is
  
  begin
  
     insert into t_annot_set(as_id,
                             as_doc_id,
                             as_name)
     values(seq_annot_set.nextval,
            p_doc_id,
            p_as_name)
     returning as_id into p_as_id;
                                 
  end;
  
  
  /*******************************************************************************************/
  procedure create_annotation(p_doc_id           IN number,
                              p_ann_local_id         IN number,  
                              p_as_id            IN number,
                              p_node_start_lid   IN number,                                
                              p_node_start_offset IN number,  
                              p_node_end_lid      IN number,                                                              
                              p_node_end_offset   IN number,  
                              p_ann_type         IN varchar2,
                              p_ann_global_id    OUT number)
  is
     l_start_node_gid number;
     l_end_node_gid   number;     
     l_ann_type_id   number;
     cnt             number;
  begin
     
     -- 1. store nodes in DB only if they're new
     -- (nodes are shared between annotations so the chances 
     -- a node is used by more than one annotation is high)
     
     -- 1.1. start node
     select count(node_global_id)
     into   cnt     
     from   t_node
     where  node_doc_id = p_doc_id
            and node_local_id = p_node_start_lid;
     
     if (0 = cnt) then
        -- add to DB
        insert into t_node(node_global_id,
                           node_doc_id,
                           node_local_id,
                           node_offset)
        values (seq_node.nextval,
                p_doc_id,
                p_node_start_lid,
                p_node_start_offset)
        returning node_global_id into l_start_node_gid;        
     else
        -- select the global ID
        select node_global_id
        into   l_start_node_gid
        from   t_node
        where  node_doc_id = p_doc_id
               and node_local_id = p_node_start_lid;        
     end if;
     
     -- 1.2. end node     

     select count(node_global_id)
     into   cnt     
     from   t_node
     where  node_doc_id = p_doc_id
            and node_local_id = p_node_end_lid;
     
     if (0 = cnt) then
        -- add to DB
        insert into t_node(node_global_id,
                           node_doc_id,
                           node_local_id,
                           node_offset)
        values (seq_node.nextval,
                p_doc_id,
                p_node_end_lid,
                p_node_end_offset)
        returning node_global_id into l_end_node_gid;        
     else
        -- select the global ID
        select node_global_id
        into   l_end_node_gid
        from   t_node
        where  node_doc_id = p_doc_id
               and node_local_id = p_node_end_lid;        
     end if;
     
     
     -- 2. store annotation in DB
     
     -- 2.1 get the anotation type ID
     select count(at_id)
     into   cnt
     from   t_annotation_type
     where  at_name = p_ann_type;
     
     -- 2.2 if there is no such type, then create one
     if (cnt = 0) then
        
        --oops, new type
        --add it
        insert into t_annotation_type(at_id,
                                      at_name)
        values (seq_annotation_type.nextval,
                p_ann_type)
        returning at_id into l_ann_type_id;
        
     else
     
        -- get the id
        select at_id
        into   l_ann_type_id
        from   t_annotation_type
        where  at_name = p_ann_type;

     end if;
     
     -- 2.3 insert annotation
     insert into t_annotation(ann_global_id,
                              ann_doc_id,
                              ann_local_id,
                              ann_at_id,
                              ann_startnode_id,
                              ann_endnode_id)
     values (seq_annotation.nextval,
             p_doc_id,
             p_ann_local_id,
             l_ann_type_id,
             l_start_node_gid,
             l_end_node_gid)
     returning ann_global_id into p_ann_global_id;
     
     -- 3. create a annotation-to-aset mapping
     insert into t_as_annotation(asann_id,
                                 asann_ann_id,
                                 asann_as_id)
     values (seq_as_annotation.nextval,
             p_ann_global_id,
             p_as_id);
     
          
  end;


  /*******************************************************************************************/
  procedure create_corpus(p_lr_id     IN number,
                          p_corp_id   OUT number)
  is
  begin
     
     insert into t_corpus(corp_id,
                          corp_lr_id)
     values (seq_corpus.nextval,
             p_lr_id)
     returning corp_id into p_corp_id;
     
  end;

  
  /*******************************************************************************************/
  function is_valid_feature_type(p_type          IN number)
     return boolean
  is
  begin
     
     return (p_type in (persist.VALUE_TYPE_INTEGER,
                       persist.VALUE_TYPE_LONG,
                       persist.VALUE_TYPE_BOOLEAN,
                       persist.VALUE_TYPE_STRING,
                       persist.VALUE_TYPE_BINARY,
                       persist.VALUE_TYPE_FLOAT));
     
  end;
  
  /*******************************************************************************************/
  procedure create_feature(p_entity_id           IN number,
                           p_entity_type         IN number,
                           p_key                 IN varchar2,  
                           p_value_number        IN number,                                
                           p_value_varchar       IN varchar2,
                           p_value_type          IN number,
                           p_feat_id             OUT number)                      
  is
  begin
  
  
     if (false = is_valid_feature_type(p_value_type)) then
        raise error.x_invalid_feature_type;
     end if;  
  
     
     insert into t_feature(ft_id,
                           ft_entity_id,
                           ft_entity_type,
                           ft_key,
                           ft_number_value,
                           ft_binary_value,
                           ft_character_value,
                           ft_long_character_value,
                           ft_value_type)
     values(seq_feature.nextval,
            p_entity_id,
            p_entity_type,
            p_key,
            p_value_number,
            empty_blob(),
            p_value_varchar,
            empty_clob(),
            p_value_type)
     returning ft_id into p_feat_id;
     
  end;


  /*******************************************************************************************/
  procedure change_content_type(p_cont_id        in number,
                                p_new_type       in number)     
  is         
  begin
    
    if (p_new_type not in (persist.CHARACTER_CONTENT,
                           persist.BINARY_CONTENT,
                           persist.EMPTY_CONTENT)) then
                           
       raise error.x_invalid_content_type;
    end if;
    
    update t_doc_content
    set    dc_content_type = p_new_type
    where  dc_id = p_cont_id;
      
  end;

  /*******************************************************************************************/
  procedure set_lr_name(p_lr_id     IN number,
                        p_lr_name   IN varchar2)
  is
    cnt number;
  begin
    
    --1. is there such LR?
    select count(LR_ID)
    into   cnt
    from   t_lang_resource
    where  lr_id = p_lr_id;
  
    --2. update it
    update t_lang_resource
    set    lr_name = p_lr_name
    where  lr_id = p_lr_id;

  exception
    when NO_DATA_FOUND then
       raise error.x_invalid_lr;

  end;                                                                                                        

  /*******************************************************************************************/
  procedure get_id_lot(p_id1        out number,
                       p_id2        out number,
                       p_id3        out number,
                       p_id4        out number,
                       p_id5        out number,
                       p_id6        out number,
                       p_id7        out number,
                       p_id8        out number,
                       p_id9        out number,
                       p_id10       out number)
  is

  begin
     select seq_annotation.nextval into p_id1 from dual;
     select seq_annotation.nextval into p_id2 from dual;
     select seq_annotation.nextval into p_id3 from dual;
     select seq_annotation.nextval into p_id4 from dual;
     select seq_annotation.nextval into p_id5 from dual;
     select seq_annotation.nextval into p_id6 from dual;
     select seq_annotation.nextval into p_id7 from dual;
     select seq_annotation.nextval into p_id8 from dual;
     select seq_annotation.nextval into p_id9 from dual;
     select seq_annotation.nextval into p_id10 from dual;
  end;


  /*******************************************************************************************/
  procedure update_document(p_lr_id        IN number,
                            p_url          IN varchar2,
                            p_start_offset IN number,
                            p_end_offset   IN number,
                            p_is_mrk_aware IN number)
  is
    cnt number;
  begin
     
     -- 1. get the doc_id
     select count(doc_id)
     into   cnt
     from   t_document
     where  doc_lr_id = p_lr_id;
     
     if (cnt = 0) then
        raise error.x_invalid_lr;                              
     end if;
     
     update t_document
     set    doc_url = p_url,
            doc_start = p_start_offset,
            doc_end = p_end_offset,
            doc_is_markup_aware = p_is_mrk_aware
     where  doc_lr_id = p_lr_id;     
  end;

  /*******************************************************************************************/
  procedure delete_features(p_ent_id        IN number,
                            p_ent_type      IN number)
  is
  begin
     delete from t_feature
     where  ft_entity_id = p_ent_id
     and    ft_entity_type = p_ent_type; 
  end;
  
/*begin
  -- Initialization
  <Statement>; */
end persist;
/
