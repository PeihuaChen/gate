/*
 *  LuceneIndexManager.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Rosen Marinov, 19/Apr/2002
 *
 */

package gate.creole.ir.lucene;

import gate.*;
import gate.creole.ir.*;

import org.apache.lucene.index.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.store.*;

import java.io.*;
import java.util.*;

public class LuceneIndexManager implements IndexManager{

  public final static String DOCUMENT_ID = "DOCUMENT_ID";
  private IndexDefinition idef;
  private Corpus corpus;

  public LuceneIndexManager(IndexDefinition def, Corpus corpus){
    this.idef = def;
    this.corpus = corpus;
  }

  public void createIndex() throws IndexException{
    String location = idef.getIndexLocation();
    try {
      File file = new File(location);
      if (file.exists()){
        if (file.isDirectory() || file.list().length>0){
          throw new IndexException("Directory is not empty");
        }
        if (!file.isDirectory()){
          throw new IndexException("Only empty directory can be index path");
        }
      }

      IndexWriter writer = new IndexWriter(location,(Analyzer) idef.getAnalyzer(), true);

      for(int i = 0; i<corpus.size(); i++) {
        gate.Document gateDoc = (gate.Document) corpus.get(i);
        writer.addDocument(getLuceneDoc(gateDoc));
      }//for (all documents)

      writer.close();
    } catch (java.io.IOException ioe){
      throw new IndexException(ioe.getMessage());
    }
  }

  public void optimizeIndex() throws IndexException{
    try {
      IndexWriter writer = new IndexWriter(idef.getIndexLocation(), (Analyzer) idef.getAnalyzer(), false);
      writer.optimize();
      writer.close();
    } catch (java.io.IOException ioe){
      throw new IndexException(ioe.getMessage());
    }
  }

  public void deleteIndex() throws IndexException{
    File file = new File(idef.getIndexLocation());
    boolean isDeleted = file.delete();
    if (!isDeleted) {
      throw new IndexException("Can't delete directory" + idef.getIndexLocation());
    }
  }

  public void sync(List added, List removed, List changed) throws IndexException{
    String location = idef.getIndexLocation();
    try {

      IndexReader reader = IndexReader.open(location);

      for (int i = 0; i<removed.size(); i++) {
        gate.Document gateDoc = (gate.Document) removed.get(i);
        String id = gateDoc.getLRPersistenceId().toString();
        org.apache.lucene.index.Term term = new org.apache.lucene.index.Term(DOCUMENT_ID,id);
        reader.delete(term);
      }//for (remove all removed documents)

      for (int i = 0; i<changed.size(); i++) {
        gate.Document gateDoc = (gate.Document) changed.get(i);
        String id = gateDoc.getLRPersistenceId().toString();
        org.apache.lucene.index.Term term = new org.apache.lucene.index.Term(DOCUMENT_ID,id);
        reader.delete(term);
      }//for (remove all changed documents)

      reader.close();

      IndexWriter writer = new IndexWriter(location,(Analyzer) idef.getAnalyzer(), false);

      for(int i = 0; i<added.size(); i++) {
        gate.Document gateDoc = (gate.Document) added.get(i);
        writer.addDocument(getLuceneDoc(gateDoc));
      }//for (add all added documents)

      for(int i = 0; i<changed.size(); i++) {
        gate.Document gateDoc = (gate.Document) changed.get(i);
        writer.addDocument(getLuceneDoc(gateDoc));
      }//for (add all changed documents)

      writer.close();
    } catch (java.io.IOException ioe) {
      throw new IndexException(ioe.getMessage());
    }
  }

  private org.apache.lucene.document.Document getLuceneDoc(gate.Document gateDoc) {
    org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
    Iterator fields = idef.getIndexFields();

    luceneDoc.add(Field.Keyword(DOCUMENT_ID,gateDoc.getLRPersistenceId().toString()));

    while (fields.hasNext()) {
      IndexField field = (IndexField) fields.next();
      String valueForIndexing;

      if (field.getReader() == null){
        valueForIndexing = gateDoc.getFeatures().get(field.getName()).toString();
      } else {
        valueForIndexing = field.getReader().getRpopertyValue(gateDoc);
      } //if-else reader or feature

      if (field.isPreseved()) {
        luceneDoc.add(Field.Keyword(field.getName(),valueForIndexing));
      } else {
        luceneDoc.add(Field.Text(field.getName(),valueForIndexing));
      } // if-else keyword or text

    }// while (add all fields)

    return luceneDoc;
  }

}