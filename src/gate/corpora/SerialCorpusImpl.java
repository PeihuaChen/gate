/*
 *  SerialCorpusImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 19/Oct/2001
 *
 *  $Id$
 */

package gate.corpora;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.annotation.*;
import gate.persist.*;
import java.io.*;
import java.net.*;
import gate.event.*;
import gate.creole.*;

//The initial design was to implement this on the basis of a WeakValueHashMap.
//However this creates problems, because the user might e.g., add a transient
//document to the corpus and then if the Document variable goes out of scope
//before sync() is called, nothing will be saved of the new document. Bad!
//Instead, to cope with the unloading for memory saving use, I implemented
//a documentUnload() method, which sets the in-memory copy to null but can
//always restore the doc, because it has its persistence ID.

public class SerialCorpusImpl extends
          AbstractLanguageResource
                              implements Corpus, CreoleListener {

  /** Debug flag */
  private static final boolean DEBUG = false;

  static final long serialVersionUID = 3632609241787241616L;

  private transient Vector corpusListeners;
  private java.util.List docDataList = null;

  //here I keep document index as key (same as the index in docDataList
  //which defines the document order) and Documents as value
  private transient List documents = null;

  public SerialCorpusImpl(){
    docDataList = new ArrayList();

    //make sure we fire events when docs are added/removed/etc
    documents = new ArrayList();

    Gate.getCreoleRegister().addCreoleListener(this);
  }

  /**
   * Constructor to create a SerialCorpus from a transient one.
   * This is called by adopt() to store the transient corpus
   * and re-route the methods calls to it, until the corpus is
   * sync-ed on disk. After that, the transientCorpus will always
   * be null, so the new functionality will be used instead.
   */
  public SerialCorpusImpl(Corpus tCorpus){
    //copy the corpus name and features from the one in memory
    this.setName(tCorpus.getName());
    this.setFeatures(tCorpus.getFeatures());

    docDataList = new ArrayList();
    //now cache the names of all docs for future use
    Iterator iter = tCorpus.getDocumentNames().iterator();
    while (iter.hasNext())
      docDataList.add(new DocumentData((String) iter.next(), null));

    //copy all the documents from the transient corpus
    documents = new ArrayList();
    documents.addAll(tCorpus);

    //make sure we fire events when docs are added/removed/etc
    Gate.getCreoleRegister().addCreoleListener(this);
  }

  /**
   * Gets the names of the documents in this corpus.
   * @return a {@link List} of Strings representing the names of the documents
   * in this corpus.
   */
  public List getDocumentNames(){
    List docsNames = new ArrayList();
    Iterator iter = docDataList.iterator();
    while (iter.hasNext()) {
      DocumentData data = (DocumentData) iter.next();
      docsNames.add(data.getDocumentName());
    }
    return docsNames;
  }

  /**
   * This method should only be used by the Serial Datastore to set
   */
  public void setDocumentPersistentID(int index, Object persID){
    if (index >= docDataList.size()) return;
    ((DocumentData)docDataList.get(index)).setPersistentID(persID);
    if (DEBUG) Out.prln("IDs are now: " + docDataList);
  }

  /**
   * Gets the name of a document in this corpus.
   * @param index the index of the document
   * @return a String value representing the name of the document at
   * <tt>index</tt> in this corpus.<P>
   */
  public String getDocumentName(int index){
    if (index >= docDataList.size()) return "No such document";

    return ((DocumentData) docDataList.get(index)).getDocumentName();
  }

  /**
   * Unloads the document from memory, but calls sync() first, to store the
   * changes
   */
  public void unloadDocumentAt(int index) {
    //1. check whether its been loaded and is a persistent one
    // if a persistent doc is not loaded, there's nothing we need to do
    if ( (! isDocumentLoaded(index)) && isPersistentDocument(index))
      return;

    //2. sync the document before releasing it from memory, because the
    //creole register garbage collects all LRs which are not used any more
    Document doc = (Document) documents.get(index);
    try {
      //if the document is not already adopted, we need to do that first
      if (doc.getLRPersistenceId() == null) {
        doc = (Document) this.getDataStore().adopt(doc, null);
        this.getDataStore().sync(doc);
        this.setDocumentPersistentID(index, doc.getLRPersistenceId());
      } else //if it is adopted, just sync it
        this.getDataStore().sync(doc);

      //3. remove the document from the memory
      //do this, only if the saving has succeeded
      documents.remove(index);
    } catch (PersistenceException ex) {
        throw new GateRuntimeException("Error unloading document from corpus"
                      + "because document sync failed: " + ex.getMessage());
    } catch (gate.security.SecurityException ex1) {
        throw new GateRuntimeException("Error unloading document from corpus"
                      + "because of document access error: " + ex1.getMessage());
    }

  }

  /**
   * This method NEEDS IMPLEMENTING
   */
  public void unloadDocument(Document doc) {
//    documents.remove(new Integer(index));
  }

  /**
   * This method returns true when the document is already loaded in memory
   */
  public boolean isDocumentLoaded(int index) {
    if (documents == null || documents.isEmpty()) return false;
    return documents.get(index) != null;
  }

  /**
   * This method returns true when the document is already stored on disk
   * i.e., is not transient
   */
  public boolean isPersistentDocument(int index) {
    if (documents == null || documents.isEmpty()) return false;
    return (((DocumentData)docDataList.get(index)).getPersistentID() != null);
  }

  public synchronized void removeCorpusListener(CorpusListener l) {
    if (corpusListeners != null && corpusListeners.contains(l)) {
      Vector v = (Vector) corpusListeners.clone();
      v.removeElement(l);
      corpusListeners = v;
    }
  }
  public synchronized void addCorpusListener(CorpusListener l) {
    Vector v = corpusListeners == null ? new Vector(2) : (Vector) corpusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      corpusListeners = v;
    }
  }
  protected void fireDocumentAdded(CorpusEvent e) {
    if (corpusListeners != null) {
      Vector listeners = corpusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CorpusListener) listeners.elementAt(i)).documentAdded(e);
      }
    }
  }
  protected void fireDocumentRemoved(CorpusEvent e) {
    if (corpusListeners != null) {
      Vector listeners = corpusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CorpusListener) listeners.elementAt(i)).documentRemoved(e);
      }
    }
  }
  public void resourceLoaded(CreoleEvent e) {
  }
  public void resourceUnloaded(CreoleEvent e) {
    Resource res = e.getResource();
    if (! (res instanceof Document))
      return;
    //unload all occurences, but no need to remove them from the corpus too
    while(contains(res)) unloadDocument((Document) res);
  }
  public void datastoreOpened(CreoleEvent e) {
  }
  public void datastoreCreated(CreoleEvent e) {
  }
  public void datastoreClosed(CreoleEvent e) {
  }


  //List methods
  //java docs will be automatically copied from the List interface.

  public int size() {
    return docDataList.size();
  }

  public boolean isEmpty() {
    return docDataList.isEmpty();
  }

  public boolean contains(Object o){
    //return true if:
    // - the document data list contains a document with such a name
    //   and persistent id

    if (! (o instanceof Document))
      return false;
    Document doc = (Document) o;
    Iterator iter = docDataList.iterator();
    while (iter.hasNext()) {
      DocumentData docData = (DocumentData) iter.next();
      if (docData.getDocumentName().equals(doc.getName())
          &&
          ((docData.getPersistentID() == null &&
              doc.getLRPersistenceId() == null)
              ||
            docData.getPersistentID().equals(doc.getLRPersistenceId()))
          )
        return true;
    }
    return false;
  }

  public Iterator iterator(){
    return new Iterator(){
      Iterator docDataIter = docDataList.iterator();

      public boolean hasNext() {
        return docDataIter.hasNext();
      }

      public Object next(){

        //try finding a document with the same name and persistent ID
        DocumentData docData = (DocumentData) docDataIter.next();
        int index = docDataList.indexOf(docData);
        return SerialCorpusImpl.this.get(index);
      }

      public void remove() {
        throw new UnsupportedOperationException("SerialCorpusImpl does not " +
                    "support remove in the iterators");
      }
    }; //return

  }//iterator

  public String toString() {
    return "document data " + docDataList.toString() + " documents " + documents;
  }

  public Object[] toArray(){
    //there is a problem here, because some docs might not be instantiated
    throw new MethodNotImplementedException(
                "toArray() is not implemented for SerialCorpusImpl");
  }

  public Object[] toArray(Object[] a){
    //there is a problem here, because some docs might not be instantiated
    throw new MethodNotImplementedException(
                "toArray(Object[] a) is not implemented for SerialCorpusImpl");
  }

  public boolean add(Object o){
    if (! (o instanceof Document) || o == null)
      return false;
    Document doc = (Document) o;

    //add the document with its index in the docDataList
    //in this case, since it's going to be added to the end
    //the index will be the size of the docDataList before
    //the addition
    DocumentData docData = new DocumentData(doc.getName(),
                                            doc.getLRPersistenceId());
    boolean result = docDataList.add(docData);
    documents.add(doc);
    fireDocumentAdded(new CorpusEvent(SerialCorpusImpl.this,
                                      doc,
                                      documents.size()-1,
                                      CorpusEvent.DOCUMENT_ADDED));

    return result;
  }

  public boolean remove(Object o){
    if (! (o instanceof Document))
      return false;
    Document doc = (Document) o;

    boolean found = false;
    DocumentData docData = null;

    int index;
    //try finding a document with the same name and persistent ID
    Iterator iter = docDataList.iterator();
    for (index = 0;  iter.hasNext() && !found; index++) {
      docData = (DocumentData) iter.next();
      if (docData.getDocumentName().equals(doc.getName()) &&
          docData.getPersistentID().equals(doc.getLRPersistenceId()))
        found = true;
    }

    if(found && index < docDataList.size()) { //we found it, so remove it
      docDataList.remove(index);
      Document oldDoc =  (Document) documents.remove(index);
      fireDocumentRemoved(new CorpusEvent(SerialCorpusImpl.this,
                                          oldDoc,
                                          index,
                                          CorpusEvent.DOCUMENT_REMOVED));
    }

    return found;
  }

  public boolean containsAll(Collection c){
    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      if (! contains(iter.next()))
        return false;
    }
    return true;
  }

  public boolean addAll(Collection c){
    boolean allAdded = true;
    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      if (! add(iter.next()))
        allAdded = false;
    }
    return allAdded;
  }

  public boolean addAll(int index, Collection c){
    throw new UnsupportedOperationException();
  }

  public boolean removeAll(Collection c){
    boolean allRemoved = true;
    Iterator iter = c.iterator();
    while (iter.hasNext()) {
      if (! remove(iter.next()))
        allRemoved = false;
    }
    return allRemoved;

  }

  public boolean retainAll(Collection c){
    throw new UnsupportedOperationException();
  }

  public void clear(){
    documents.clear();
    docDataList.clear();
  }

  public boolean equals(Object o){
    if (! (o instanceof SerialCorpusImpl))
      return false;
    SerialCorpusImpl oCorpus = (SerialCorpusImpl) o;
    if ((this == null && oCorpus != null) || oCorpus == null && this != null)
      return false;
    if (oCorpus == this)
      return true;
    if ((oCorpus.lrPersistentId == this.lrPersistentId ||
         oCorpus.lrPersistentId.equals(this.lrPersistentId))
        &&
        oCorpus.name.equals(this.name)
        &&
        (oCorpus.dataStore == this.dataStore
          || oCorpus.dataStore.equals(this.dataStore))
        &&
        oCorpus.docDataList.equals(docDataList))
      return true;
    return false;
  }

  public int hashCode(){
    return docDataList.hashCode();
  }

  public Object get(int index){
      if (index >= docDataList.size())
        return null;

      Object res = documents.get(index);

      if (DEBUG) Out.prln("SerialCorpusImpl: get(): index "
                          + index + "result: " + res);

      //if the document is null, then I must get it from the DS
      if (res == null) {
        FeatureMap features = Factory.newFeatureMap();
        features.put(DataStore.DATASTORE_FEATURE_NAME, this.dataStore);
        try {
          features.put(DataStore.LR_ID_FEATURE_NAME,
                      ((DocumentData)docDataList.get(index)).getPersistentID());
          Resource lr = Factory.createResource( "gate.corpora.DocumentImpl",
                                                features);
          if (DEBUG) Out.prln("Loaded document :" + lr.getName());
          //change the result to the newly loaded doc
          res = lr;

          //finally replace the doc with the instantiated version
          documents.set(index, lr);
        } catch (ResourceInstantiationException ex) {
          Err.prln("Error reading document inside a serialised corpus.");
          throw new GateRuntimeException(ex.getMessage());
        }
      }

      return res;
  }

  public Object set(int index, Object element){
    throw new gate.util.MethodNotImplementedException();
        //fire the 2 events
/*        fireDocumentRemoved(new CorpusEvent(SerialCorpusImpl.this,
                                            oldDoc,
                                            ((Integer) key).intValue(),
                                            CorpusEvent.DOCUMENT_REMOVED));
        fireDocumentAdded(new CorpusEvent(SerialCorpusImpl.this,
                                          newDoc,
                                          ((Integer) key).intValue(),
                                          CorpusEvent.DOCUMENT_ADDED));
*/
  }

  public void add(int index, Object o){
    if (! (o instanceof Document) || o == null)
      return;
    Document doc = (Document) o;

    //add the document with its index in the docDataList
    //in this case, since it's going to be added to the end
    //the index will be the size of the docDataList before
    //the addition
    DocumentData docData = new DocumentData(doc.getName(),
                                            doc.getLRPersistenceId());
    docDataList.add(index, docData);

    //PROBLEM: I need to change the documents to a list, so the
    //order gets automatically changed with inserts.
    documents.add(index, doc);
    fireDocumentAdded(new CorpusEvent(SerialCorpusImpl.this,
                                      doc,
                                      index,
                                      CorpusEvent.DOCUMENT_ADDED));

  }

  public Object remove(int index){
    docDataList.remove(index);
    Document res = (Document) documents.remove(index);
    fireDocumentRemoved(new CorpusEvent(SerialCorpusImpl.this,
                                        res,
                                        index,
                                        CorpusEvent.DOCUMENT_REMOVED));
    return res;

  }

  public int indexOf(Object o){
    throw new gate.util.MethodNotImplementedException();
  }

  public int lastIndexOf(Object o){
    throw new gate.util.MethodNotImplementedException();
  }

  public ListIterator listIterator(){
    throw new gate.util.MethodNotImplementedException();
  }

  public ListIterator listIterator(int index){
    throw new gate.util.MethodNotImplementedException();
  }

  /**
   * persistent Corpus does not support this method as all
   * the documents might no be in memory
   */
  public List subList(int fromIndex, int toIndex){
    throw new gate.util.MethodNotImplementedException();
  }

  /**
   * readObject - calls the default readObject() and then initialises the
   * transient data
   *
   * @serialData Read serializable fields. No optional data read.
   */
  private void readObject(ObjectInputStream s)
      throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    documents = new ArrayList(docDataList.size());
    for (int i = 0; i < docDataList.size(); i++)
      documents.add(null);
  }//readObject

  /**
   * Class used for the documents structure. This is a {@link java.util.HashMap}
   * that fires events when elements are added/removed/set
   */
/*
  protected class VerboseHashMap extends HashMap{
    static final long serialVersionUID = -3320104879514836097L;

    public void clear() {
      //override, so events are fired
      Iterator iter = this.keySet().iterator();
      while (iter.hasNext())
        remove(iter.next());
    }

    public boolean isNullValue(Object key) {
      return (super.get(key) == null);
    }

    public Object put(Object key, Object value) {
      if (! (value instanceof Document))
        throw new UnsupportedOperationException(
          getClass().getName() +
          " only accepts gate.Document values as members!\n" +
          value.getClass().getName() + " is not a gate.Document");

      if (containsKey(key)) { //we're replacing a document
        Document oldDoc = (Document) documents.get(key); ;
        Document newDoc = (Document)value;

        //fire the 2 events
        fireDocumentRemoved(new CorpusEvent(SerialCorpusImpl.this,
                                            oldDoc,
                                            ((Integer) key).intValue(),
                                            CorpusEvent.DOCUMENT_REMOVED));
        fireDocumentAdded(new CorpusEvent(SerialCorpusImpl.this,
                                          newDoc,
                                          ((Integer) key).intValue(),
                                          CorpusEvent.DOCUMENT_ADDED));
      } else { //we're adding a document
        Document newDoc = (Document)value;

        fireDocumentAdded(new CorpusEvent(SerialCorpusImpl.this,
                                          newDoc,
                                          ((Integer) key).intValue(),
                                          CorpusEvent.DOCUMENT_ADDED));
      }

      return super.put(key, value);
    }  //put

    public Object remove(Object key){
      //the remove event is now fired from the SerialCorpusImpl class, because
      //I want to be able to remove values, so I can unload documents from
      //memory, but that should not make anybody think that the document
      //has been removed from the corpus
      Document oldDoc = (Document) super.remove(key);
      return oldDoc;
    }//public Object remove(Object key)

    public Object get(Object key){
      if (! (key instanceof Integer))
        return null;
      Object res = super.get(key);
      int index = ((Integer) key).intValue();
      if (index >= docDataList.size())
        return null;

      if (DEBUG) Out.prln("index " + index + "result: " + res);

      //if the document is null, then I must get it from the DS
      if (res == null) {
        FeatureMap features = Factory.newFeatureMap();
        features.put(DataStore.DATASTORE_FEATURE_NAME,
                     SerialCorpusImpl.this.dataStore);
        try {
          features.put(DataStore.LR_ID_FEATURE_NAME,
                      ((DocumentData)docDataList.get(index)).getPersistentID());
          Resource lr = Factory.createResource( "gate.corpora.DocumentImpl",
                                                features);
          if (DEBUG) Out.prln("Loading document :" + lr.getName());
          res = lr;
          super.put(key, res);
        } catch (ResourceInstantiationException ex) {
          Err.prln("Error reading document inside a serialised corpus.");
          throw new GateRuntimeException(ex.getMessage());
        }

      }
      return res;
    }//public Object get(Object key)

  }//protected class VerboseHashMap extends HashMap
*/

  protected class DocumentData implements Serializable {
    //fix the ID for serialisation
    static final long serialVersionUID = 4192762901421847525L;

    DocumentData(String name, Object ID){
      docName = name;
      persistentID = ID;
    }

    public String getDocumentName() {
      return docName;
    }

    public Object getPersistentID() {
      return persistentID;
    }

    public void setPersistentID(Object newID) {
      persistentID = newID;
    }

    public String toString() {
      return new String("DocumentData: " + docName + ", " + persistentID);
    }

    String docName;
    Object persistentID;
  }
}