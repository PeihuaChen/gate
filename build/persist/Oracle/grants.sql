/*
 *  grants.sql
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 01/Oct/2001
 *
 *  $Id$
 *
 *      DO NOT EDIT !
 *      THIS FILE IS GENERATED FROM THE generateGrants.sql script
 */




grant execute on GATEADMIN.ERROR to GATEUSER;                                   
grant execute on GATEADMIN.PERSIST to GATEUSER;                                 
grant execute on GATEADMIN.SECURITY to GATEUSER;                                

grant select on GATEADMIN.SEQ_ANNOTATION to GATEUSER;                           
grant select on GATEADMIN.SEQ_ANNOTATION_TYPE to GATEUSER;                      
grant select on GATEADMIN.SEQ_ANNOT_SET to GATEUSER;                            
grant select on GATEADMIN.SEQ_AS_ANNOTATION to GATEUSER;                        
grant select on GATEADMIN.SEQ_CORPUS to GATEUSER;                               
grant select on GATEADMIN.SEQ_CORPUS_DOCUMENT to GATEUSER;                      
grant select on GATEADMIN.SEQ_DOCUMENT to GATEUSER;                             
grant select on GATEADMIN.SEQ_DOC_CONTENT to GATEUSER;                          
grant select on GATEADMIN.SEQ_DOC_ENCODING to GATEUSER;                         
grant select on GATEADMIN.SEQ_FEATURE to GATEUSER;                              
grant select on GATEADMIN.SEQ_GROUP to GATEUSER;                                
grant select on GATEADMIN.SEQ_LANG_RESOURCE to GATEUSER;                        
grant select on GATEADMIN.SEQ_LR_TYPE to GATEUSER;                              
grant select on GATEADMIN.SEQ_NODE to GATEUSER;                                 
grant select on GATEADMIN.SEQ_TIMESTAMP to GATEUSER;                            
grant select on GATEADMIN.SEQ_USER to GATEUSER;                                 
grant select on GATEADMIN.SEQ_USER_GROUP to GATEUSER;                           

grant select,insert,update,delete on GATEADMIN.T_ANNOTATION to GATEUSER;        
grant select,insert,update,delete on GATEADMIN.T_ANNOTATION_TYPE to GATEUSER;   
grant select,insert,update,delete on GATEADMIN.T_ANNOT_SET to GATEUSER;         
grant select,insert,update,delete on GATEADMIN.T_AS_ANNOTATION to GATEUSER;     
grant select,insert,update,delete on GATEADMIN.T_CORPUS to GATEUSER;            
grant select,insert,update,delete on GATEADMIN.T_CORPUS_DOCUMENT to GATEUSER;   
grant select,insert,update,delete on GATEADMIN.T_DOCUMENT to GATEUSER;          
grant select,insert,update,delete on GATEADMIN.T_DOC_CONTENT to GATEUSER;       
grant select,insert,update,delete on GATEADMIN.T_DOC_ENCODING to GATEUSER;      
grant select,insert,update,delete on GATEADMIN.T_FEATURE to GATEUSER;           
grant select,insert,update,delete on GATEADMIN.T_GROUP to GATEUSER;             
grant select,insert,update,delete on GATEADMIN.T_LANG_RESOURCE to GATEUSER;     
grant select,insert,update,delete on GATEADMIN.T_LR_TYPE to GATEUSER;           
grant select,insert,update,delete on GATEADMIN.T_NODE to GATEUSER;              
grant select,insert,update,delete on GATEADMIN.T_USER to GATEUSER;              
grant select,insert,update,delete on GATEADMIN.T_USER_GROUP to GATEUSER;        
grant select,insert,update,delete on GATEADMIN.T_PARAMETER to GATEUSER;        

grant select on GATEADMIN.V_ANNOTATION to GATEUSER;
grant select on GATEADMIN.V_ANNOTATION_FEATURES to GATEUSER;
grant select on GATEADMIN.V_ANNOTATION_SET to GATEUSER;
grant select on GATEADMIN.V_CONTENT to GATEUSER;
grant select on GATEADMIN.V_DOCUMENT to GATEUSER;
grant select on GATEADMIN.V_LR to GATEUSER;