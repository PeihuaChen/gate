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
                                implements DatastoreListener,
                                           EventAwareLanguageResource {


  private boolean featuresChanged;
  private boolean nameChanged;
  /**
   * The listener for the events coming from the features.
   */
  protected EventsHandler eventHandler;


  public DatabaseCorpusImpl(String _name,
                            DatabaseDataStore _ds,
                            Long _persistenceID,
                            FeatureMap _features,
                            Vector _dbDocs) {

    super();

    this.name = _name;
    this.dataStore = _ds;
    this.lrPersistentId = _persistenceID;
    this.features = _features;
    this.supportList = _dbDocs;

    this.featuresChanged = false;
    this.nameChanged = false;

    //3. add the listeners for the features
    if (eventHandler == null)
      eventHandler = new EventsHandler();
    this.features.addFeatureMapListener(eventHandler);


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
                                        this.supportList.size()-1,
                                        CorpusEvent.DOCUMENT_ADDED));
    }

    return result;
  }


  public void add(int index, Object element){

    Assert.assertNotNull(element);
    Assert.assertTrue(index >= 0);

    long    collInitialSize = this.supportList.size();

    //accept only documents
    if (false == element instanceof Document) {
      throw new IllegalArgumentException();
    }

    Document doc = (Document)element;

    //assert docs are either transient or from the same datastore
    if (isValidForAdoption(doc)) {
      super.add(index,doc);

      //if added then fire event
      if (this.supportList.size() > collInitialSize) {
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
    int collInitialSize = this.supportList.size();
    int currIndex = index;

    Iterator it = c.iterator();
    while (it.hasNext()) {
      Document doc = (Document)it.next();
      if (isValidForAdoption(doc)) {
        add(currIndex++,doc);
      }
    }

    return (this.supportList.size() > collInitialSize);
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
    Long  deletedID = (Long)evt.getResourceID();
    Assert.assertNotNull(deletedID);

    //unregister self as listener from the DataStore
    if (deletedID.equals(this.getLRPersistenceId())) {
      //someone deleted this corpus
      this.supportList.clear();
      getDataStore().removeDatastoreListener(this);
    }

    //check if the ID is of a document the corpus contains
    Iterator it = this.supportList.iterator();
    while (it.hasNext()) {
      Document doc = (Document)it.next();
      if (doc.getLRPersistenceId().equals(deletedID)) {
        this.supportList.remove(doc);
        break;
      }
    }


  }

  public void resourceWritten(DatastoreEvent evt){
    Assert.assertNotNull(evt);
    Assert.assertNotNull(evt.getResourceID());

    //is the event for us?
    if (evt.getResourceID().equals(this.getLRPersistenceId())) {
      //wow, the event is for me
      //clear all flags, the content is synced with the DB
          this.featuresChanged =
            this.nameChanged = false;
    }
  }

  public boolean isResourceChanged(int changeType) {

    switch(changeType) {

      case EventAwareLanguageResource.RES_FEATURES:
        return this.featuresChanged;
      case EventAwareLanguageResource.RES_NAME:
        return this.nameChanged;
      default:
        throw new IllegalArgumentException();
    }

  }

  /** Sets the name of this resource*/
  public void setName(String name){
    super.setName(name);

    this.nameChanged = true;
  }


  /** Set the feature set */
  public void setFeatures(FeatureMap features) {
    //1. save them first, so we can remove the listener
    FeatureMap oldFeatures = this.features;

    super.setFeatures(features);

    this.featuresChanged = true;

    //4. sort out the listeners
    if (eventHandler != null)
      oldFeatures.removeFeatureMapListener(eventHandler);
    else
      eventHandler = new EventsHandler();
    this.features.addFeatureMapListener(eventHandler);
  }


  /**
   * All the events from the features are handled by
   * this inner class.
   */
  class EventsHandler implements gate.event.FeatureMapListener {
    public void featureMapUpdated(){
      //tell the document that its features have been updated
      featuresChanged = true;
    }
  }

  /**
   * Overriden to remove the features listener, when the document is closed.
   */
  public void cleanup() {
    super.cleanup();
    if (eventHandler != null)
      this.features.removeFeatureMapListener(eventHandler);
  }///inner class EventsHandler

}