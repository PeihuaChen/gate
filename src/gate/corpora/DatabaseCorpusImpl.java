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
import gate.security.SecurityInfo;


public class DatabaseCorpusImpl extends CorpusImpl
                                implements DatastoreListener,
                                           EventAwareCorpus {

  /** Debug flag */
  private static final boolean DEBUG = false;

  private boolean featuresChanged;
  private boolean nameChanged;
  /**
   * The listener for the events coming from the features.
   */
  protected EventsHandler eventHandler;
  protected List documentData;
  protected List removedDocuments;

  public DatabaseCorpusImpl() {
    super();
  }


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
//    this.supportList = _dbDocs;
    this.documentData =  _dbDocs;
    this.supportList = new ArrayList(this.documentData.size());
    this.removedDocuments = new ArrayList();

    //init the document list
    for (int i=0; i< this.documentData.size(); i++) {
      this.supportList.add(null);
    }

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

    //add to doc data too
/* Was:
    DocumentData newDocData = new DocumentData(doc.getName(),null);
*/
    DocumentData newDocData = new DocumentData(doc.getName(),
                                               doc.getLRPersistenceId());

    this.documentData.add(newDocData);

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

      //add to doc data too
      DocumentData newDocData = new DocumentData(doc.getName(),null);
      this.documentData.add(index,newDocData);

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

      this.removedDocuments.clear();
    }
  }


  public void resourceUnloaded(CreoleEvent e) {

    Assert.assertNotNull(e);
    Assert.assertNotNull(e.getResource());

    Resource res = e.getResource();

    if (res instanceof Document) {

      Document doc = (Document) res;

      if (DEBUG) {
        Out.prln("resource Unloaded called ");
      }

      //remove from the corpus too, if a transient one
      if (null == doc.getLRPersistenceId()) {
        //@FIXME - not sure we need this
        super.remove(doc);
      }
      else {
        //unload all occurences
        //see if we can find it first. If not, then judt return
        int index = findDocument(doc);
        if (index == -1) {
          //not our document
          return;
        }
        else {
          //3. unload from internal data structures

          //@FIXME - not sure we need this
          //super.remove(doc);

          //remove from the list of loaded documents
          Document oldDoc = (Document) this.supportList.remove(index);
        }

        if (DEBUG)
          Out.prln("corpus: document "+ index + " unloaded and set to null");
      } //if
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

  /**
   * Returns true of an LR has been modified since the last sync.
   * Always returns false for transient LRs.
   */
  public boolean isModified() {
    return this.isResourceChanged(EventAwareLanguageResource.RES_FEATURES) ||
            this.isResourceChanged(EventAwareLanguageResource.RES_NAME);
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



  public void setInitData__$$__(Object data) {

    HashMap initData = (HashMap)data;

    this.name = (String)initData.get("CORP_NAME");
    this.dataStore = (DatabaseDataStore)initData.get("DS");
    this.lrPersistentId = (Long)initData.get("LR_ID");
    this.features = (FeatureMap)initData.get("CORP_FEATURES");
    this.supportList = (List)initData.get("CORP_SUPPORT_LIST");

    this.documentData = new ArrayList(this.supportList.size());
    this.removedDocuments = new ArrayList();

    //init the documentData list
    for (int i=0; i< this.supportList.size(); i++) {
      Document dbDoc = (Document)this.supportList.get(i);
      DocumentData dd = new DocumentData(dbDoc.getName(),dbDoc.getLRPersistenceId());
      this.documentData.add(dd);
    }

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

  public Object getInitData__$$__(Object initData) {
    return null;
  }

  /**
   * Gets the names of the documents in this corpus.
   * @return a {@link List} of Strings representing the names of the documents
   * in this corpus.
   */
  public List getDocumentNames(){

    List docsNames = new ArrayList();

    if(this.documentData == null)
      return docsNames;

    Iterator iter = this.documentData.iterator();
    while (iter.hasNext()) {
      DocumentData data = (DocumentData)iter.next();
      docsNames.add(data.getDocumentName());
    }

    return docsNames;
  }


  /**
   * Gets the name of a document in this corpus.
   * @param index the index of the document
   * @return a String value representing the name of the document at
   * <tt>index</tt> in this corpus.<P>
   */
  public String getDocumentName(int index){

    if (index >= this.documentData.size()) return "No such document";

    return ((DocumentData)this.documentData.get(index)).getDocumentName();
  }

  /**
   * returns a document in the coprus by index
   * @param index the index of the document
   * @return an Object value representing DatabaseDocumentImpl
   */
  public Object get(int index){

    //0. preconditions
    Assert.assertTrue(index >= 0);
    Assert.assertTrue(index < this.documentData.size());
    Assert.assertTrue(index < this.supportList.size());

    if (index >= this.documentData.size())
      return null;

    Object res = this.supportList.get(index);

    //if the document is null, then I must get it from the database
    if (null == res) {
      Long currLRID = (Long)((DocumentData)this.documentData.get(index)).getPersistentID();
      FeatureMap params = Factory.newFeatureMap();
      params.put(DataStore.DATASTORE_FEATURE_NAME, this.getDataStore());
      params.put(DataStore.LR_ID_FEATURE_NAME, currLRID);

      try {
        Document dbDoc = (Document)Factory.createResource(DBHelper.DOCUMENT_CLASS, params);

        if (DEBUG) {
          Out.prln("Loaded document :" + dbDoc.getName());
        }

        //change the result to the newly loaded doc
        res = dbDoc;

        //finally replace the doc with the instantiated version
        Assert.assertNull(this.supportList.get(index));
        this.supportList.set(index, dbDoc);
      }
      catch (ResourceInstantiationException ex) {
        Err.prln("Error reading document inside a serialised corpus.");
        throw new GateRuntimeException(ex.getMessage());
      }
    }

    return res;
  }

  public Object remove(int index){

    //1. get the persistent id and add it to the removed list
    DocumentData docData = (DocumentData)this.documentData.get(index);
    Long removedID = (Long)docData.getPersistentID();
//    Assert.assertTrue(null != removedID);
    //removedID may be NULL if the doc is still transient

    //2. add to the list of removed documents
    if (null != removedID) {
      this.removedDocuments.add(removedID);
    }

    //3. delete
    this.documentData.remove(index);
    Document res = (Document)this.supportList.remove(index);

    //4, fire events
    fireDocumentRemoved(new CorpusEvent(DatabaseCorpusImpl.this,
                                        res,
                                        index,
                                        CorpusEvent.DOCUMENT_REMOVED));
    return res;

  }


  public boolean remove(Object obj){

    //0. preconditions
    Assert.assertNotNull(obj);
    Assert.assertTrue(obj instanceof DatabaseDocumentImpl);

    if (false == obj instanceof Document) {
      return false;
    }

    Document doc = (Document) obj;

    //see if we can find it first. If not, then judt return
    int index = findDocument(doc);
    if (index == -1) {
      return false;
    }

    if(index < this.documentData.size()) {
      //we found it, so remove it

      //1. get the persistent id and add it to the removed list
      DocumentData docData = (DocumentData)this.documentData.get(index);
      Long removedID = (Long)docData.getPersistentID();
      //Assert.assertTrue(null != removedID);
      //removed ID may be null - doc is still transient

      //2. add to the list of removed documents
      if (null != removedID) {
        this.removedDocuments.add(removedID);
      }

      //3. delete
      this.documentData.remove(index);
      Document oldDoc = (Document) this.supportList.remove(index);

      fireDocumentRemoved(new CorpusEvent(DatabaseCorpusImpl.this,
                                          oldDoc,
                                          index,
                                          CorpusEvent.DOCUMENT_REMOVED));
    }

    return true;
  }


  public int findDocument(Document doc) {

    boolean found = false;
    DocumentData docData = null;

    //first try finding the document in memory
    int index = this.supportList.indexOf(doc);

    if (index > -1 && index < this.documentData.size()) {
      return index;
    }

    //else try finding a document with the same name and persistent ID
    Iterator iter = this.documentData.iterator();

    for (index = 0;  iter.hasNext(); index++) {
      docData = (DocumentData) iter.next();
      if (docData.getDocumentName().equals(doc.getName()) &&
          docData.getPersistentID().equals(doc.getLRPersistenceId())) {
        found = true;
        break;
      }
    }

    if (found && index < this.documentData.size()) {
      return index;
    }
    else {
      return -1;
    }
  }//findDocument


  public boolean contains(Object o){
    //return true if:
    // - the document data list contains a document with such a name
    //   and persistent id

    if(false == o instanceof Document)
      return false;

    int index = findDocument((Document) o);

    if (index < 0) {
      return false;
    }
    else {
      return true;
    }
  }

  public Iterator iterator(){
    return new DatabaseCorpusIterator(this.documentData);
  }

  public List getLoadedDocuments() {
    return new ArrayList(this.supportList);
  }

  public List getRemovedDocuments() {
    return new ArrayList(this.removedDocuments);
  }

  private class DatabaseCorpusIterator implements Iterator {

      private Iterator docDataIter;
      private List docDataList;

      public DatabaseCorpusIterator(List docDataList) {
        this.docDataList = docDataList;
        this.docDataIter = this.docDataList.iterator();
      }

      public boolean hasNext() {
        return docDataIter.hasNext();
      }

      public Object next(){

        //try finding a document with the same name and persistent ID
        DocumentData docData = (DocumentData)docDataIter.next();
        int index = this.docDataList.indexOf(docData);
        return DatabaseCorpusImpl.this.get(index);
      }

      public void remove() {
        throw new UnsupportedOperationException("DatabaseCorpusImpl does not " +
                    "support remove in the iterators");
      }
  }


  /**
   * Unloads the document from memory, but calls sync() first, to store the
   * changes
   */
  public void unloadDocument(int index) {

    //preconditions
    Assert.assertTrue(index >= 0);

    //1. check whether its been loaded and is a persistent one
    // if a persistent doc is not loaded, there's nothing we need to do
    if ( (! isDocumentLoaded(index)) && isPersistentDocument(index)) {
      return;
    }

    //2. sync the document before releasing it from memory, because the
    //creole register garbage collects all LRs which are not used any more
    Document doc = (Document)this.supportList.get(index);
    Assert.assertNotNull(doc);

    try {

      //if the document is not already adopted, we need to do that first
      if (doc.getLRPersistenceId() == null) {

        //3.2 get the security info for the corpus
        SecurityInfo si = this.getDataStore().getSecurityInfo(this);
        Document dbDoc = (Document) this.getDataStore().adopt(doc, si);
      }
      else {
        //if it is adopted, just sync it
        this.getDataStore().sync(doc);
      }

      //3. remove the document from the memory
      //do this, only if the saving has succeeded
      this.supportList.remove(index);
    }
    catch (PersistenceException pex) {
      throw new GateRuntimeException("Error unloading document from corpus"
                      + "because document sync failed: " + pex.getMessage());
    }
    catch (gate.security.SecurityException sex) {
      throw new GateRuntimeException("Error unloading document from corpus"
                      + "because of document access error: " + sex.getMessage());
    }

  }

  /**
   * Unloads a document from memory
   */
  public void unloadDocument(Document doc) {

    Assert.assertNotNull(doc);

    //1. determine the index of the document; if not there, do nothing
    int index = findDocument(doc);

    if (index == -1) {
      return;
    }

    unloadDocument(index);
  }


  /**
   * This method returns true when the document is already loaded in memory
   */
  public boolean isDocumentLoaded(int index) {

    //preconditions
    Assert.assertTrue(index >= 0);

    if (this.supportList == null || this.supportList.isEmpty()) {
      return false;
    }

    return this.supportList.get(index) != null;
  }

  /**
   * This method returns true when the document is already stored on disk
   * i.e., is not transient
   */
  public boolean isPersistentDocument(int index) {

    //preconditions
    Assert.assertTrue(index >= 0);

    if (this.supportList == null || this.supportList.isEmpty()) {
      return false;
    }

    return (((DocumentData)this.documentData.get(index)).getPersistentID() != null);
  }

}