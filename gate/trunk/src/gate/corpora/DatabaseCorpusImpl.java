/*
 *  DatabaseCorpusImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 05/Nov/2001
 *
 *  $Id$
 */

package gate.corpora;

import java.util.*;

import junit.framework.*;

import gate.*;
import gate.persist.*;
import gate.annotation.*;
import gate.creole.*;
import gate.event.*;
import gate.util.*;


public class DatabaseCorpusImpl extends CorpusImpl
                                implements DatastoreListener {

  public DatabaseCorpusImpl() {

    super();

    //4. add self as listener for the data store, so that we'll know when the DS is
    //synced and we'll clear the isXXXChanged flags
    this.dataStore.addDatastoreListener(this);
  }

  public boolean add(Object o){

    Assert.assertNotNull(o);
    boolean result = false;

    //accept only documents
    if (false == o instanceof Document) {
      throw new IllegalArgumentException();
    }

    Document doc = (Document)o;

    //assert docs are either transient or from the same datastore
    if (isValidForAdoption(doc)) {
      result = super.add(doc);
    }

    if (result) {
      fireDocumentAdded(new CorpusEvent(this,
                                        doc,
                                        this.documentsList.size()-1,
                                        CorpusEvent.DOCUMENT_ADDED));
    }

    return result;
  }


  public void add(int index, Object element){

    Assert.assertNotNull(element);
    Assert.assertTrue(index >= 0);

    long    collInitialSize = this.documentsList.size();

    //accept only documents
    if (false == element instanceof Document) {
      throw new IllegalArgumentException();
    }

    Document doc = (Document)element;

    //assert docs are either transient or from the same datastore
    if (isValidForAdoption(doc)) {
      super.add(index,doc);

      //if added then fire event
      if (this.documentsList.size() > collInitialSize) {
        fireDocumentAdded(new CorpusEvent(this,
                                          doc,
                                          index,
                                          CorpusEvent.DOCUMENT_ADDED));
      }
    }
  }



  public boolean addAll(Collection c){

    boolean collectionChanged = false;

    Iterator it = c.iterator();
    while (it.hasNext()) {
      Document doc = (Document)it.next();
      if (isValidForAdoption(doc)) {
        collectionChanged |= add(doc);
      }
    }

    return collectionChanged;
  }


  public boolean addAll(int index, Collection c){

    Assert.assertTrue(index >=0);

    //funny enough add(index,element) returns void and not boolean
    //so we can't use it
    boolean collectionChanged = false;
    int collInitialSize = this.documentsList.size();
    int currIndex = index;

    Iterator it = c.iterator();
    while (it.hasNext()) {
      Document doc = (Document)it.next();
      if (isValidForAdoption(doc)) {
        add(currIndex++,doc);
      }
    }

    return (this.documentsList.size() > collInitialSize);
  }


  private boolean isValidForAdoption(LanguageResource lr) {

    Long lrID = (Long)lr.getLRPersistenceId();

    if (null == lrID ||
        (this.getDataStore() != null && lr.getDataStore().equals(this.getDataStore()))) {
      return true;
    }
    else {
      return false;
    }
  }

  public void resourceAdopted(DatastoreEvent evt){
  }

  public void resourceDeleted(DatastoreEvent evt){

    Assert.assertNotNull(evt);
    Assert.assertNotNull(evt.getResourceID());

    //unregister self as listener from the DataStore
    if (evt.getResourceID().equals(this.getLRPersistenceId())) {
      //someone deleted this document
      getDataStore().removeDatastoreListener(this);
    }
  }

  public void resourceWritten(DatastoreEvent evt){
  }
}