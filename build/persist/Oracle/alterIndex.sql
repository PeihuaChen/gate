/*
 *  DDL script for Oracle 8.x and Oracle 9.x
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 31/Jan/2001
 *
 *  $Id$
 *
 */

ALTER INDEX xpkt_annotation REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_annotation_type REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_annot_set REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_as_annotation REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_corpus REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_corpus_document REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_document REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_doc_content REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_doc_encoding REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_feature REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_feature_key REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_group REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_lang_resource REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_lr_type REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_node REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_parameter REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_user REBUILD TABLESPACE gate01is;
ALTER INDEX xpkt_user_group REBUILD TABLESPACE gate01is;

alter index xpkt_annotation rebuild reverse;

