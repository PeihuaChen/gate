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
import gate.security.SecurityException;

//The initial design was to implement this on the basis of a WeakValueHashMap.
//However this creates problems, because the user might e.g., add a transient
//document to the corpus and then if the Document variable goes out of scope
//before sync() is called, nothing will be saved of the new document. Bad!
//Instead, to cope with the unloading for memory saving use, I implemented
//a documentUnload() method, which sets the in-memory copy to null but can
//always restore the doc, because it has its persistence ID.

public class SerialCorpusImpl extends
          AbstractLanguageResource
                      implements Corpus, CreoleListener, DatastoreListener {

  /** Debug flag */
  private static final boolean DEBUG = false;

  static final long serialVersionUID = 3632609241787241616L;

  private transient Vector corpusListeners;
  private java.util.List docDataList = null;

  //here I keep document index as key (same as the index in docDataList
  //which defines the document order) and Documents as value
  private transient List documents = null;

  public SerialCorpusImpl() {
  }

  /**
   * Constructor to create a SerialCorpus from a transient one.
   * This is called by adopt() to store the transient corpus
   * and re-route the methods calls to it, until the corpus is
   * sync-ed on disk. After that, the transientCorpus will always
   * be null, so the new functionality will be used instead.
   */
  protected SerialCorpusImpl(Corpus tCorpus){
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
  public void unloadDocument(int index) {
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
      documents.set(index, null);

    } catch (PersistenceException ex) {
        throw new GateRuntimeException("Error unloading document from corpus"
                      + "because document sync failed: " + ex.getMessage());
    } catch (gate.security.SecurityException ex1) {
        throw new GateRuntimeException("Error unloading document from corpus"
                      + "because of document access error: " + ex1.getMessage());
    }

  }

  /**
   * Unloads a document from memory
   */
  public void unloadDocument(Document doc) {
    if (DEBUG) Out.prln("Document to be unloaded :" + doc.getName());
    //1. determine the index of the document; if not there, do nothing
    int index = findDocument(doc);
    if (index == -1)
      return;
    if (DEBUG) Out.prln("Index of doc: " + index);
    if (DEBUG) Out.prln("Size of corpus: " + documents.size());
    unloadDocument(index);
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

  /**
   * Every LR that is a CreoleListener (and other Listeners too) must
   * override this method and make sure it removes itself from the
   * objects which it has been listening to. Otherwise, the object will
   * not be released from memory (memory leak!).
   */
  public void cleanup() {
    if (corpusListeners != null)
      corpusListeners.clear();
    documents.clear();
    docDataList.clear();
    Gate.getCreoleRegister().removeCreoleListener(this);
    if (this.dataStore != null) {
      this.dataStore.removeDatastoreListener(this);
      this.dataStore = null;
    }
  }

  /**
   * Fills this corpus with documents created from files in a directory.
   * @param filter the file filter used to select files from the target
   * directory. If the filter is <tt>null</tt> all the files will be accepted.
   * @param directory the directory from which the files will be picked. This
   * parameter is an URL for uniformity. It needs to be a URL of type file
   * otherwise an InvalidArgumentException will be thrown.
   * An implementation for this method is provided as a static method at
   * {@link gate.corpora.CorpusImpl#populate(Corpus,URL,FileFilter,boolean)}.
   * @param encoding the encoding to be used for reading the documents
   * @param recurseDirectories should the directory be parsed recursively?. If
   * <tt>true</tt> all the files from the provided directory and all its
   * children directories (on as many levels as necessary) will be picked if
   * accepted by the filter otherwise the children directories will be ignored.
   */
  public void populate(URL directory, FileFilter filter, String encoding,
                       boolean recurseDirectories)
              throws IOException, ResourceInstantiationException{
    CorpusImpl.populate(this, directory, filter, encoding, recurseDirectories);
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
    if (res instanceof Document) {
    if (DEBUG) Out.prln("Unload called ");
    //unload all occurences, but no need to remove them from the corpus too
    if (DEBUG) Out.prln("is contained? " + this.contains(res));
    if (this.contains(res))
      unloadDocument((Document) res);
    } else if (res instanceof Corpus) {
      //check if we were not unloaded. If so, we must cleanup
      if (this.equals(res))
        this.cleanup();
    }
  }
  public void datastoreOpened(CreoleEvent e) {
  }
  public void datastoreCreated(CreoleEvent e) {
  }
  public void datastoreClosed(CreoleEvent e) {
    Gate.getCreoleRegister().removeCreoleListener(this);
  }
  /**
   * Called by a datastore when a new resource has been adopted
   */
  public void resourceAdopted(DatastoreEvent evt){
  }

  /**
   * Called by a datastore when a resource has been deleted
   */
  public void resourceDeleted(DatastoreEvent evt){
    DataStore ds = (DataStore)evt.getSource();
    //1. check whether this datastore fired the event. If not, return.
    if (!ds.equals(this.dataStore))
      return;

    Object docID = evt.getResourceID();
    if (docID == null)
      return;

    if (DEBUG) Out.prln("Resource deleted called for: " + docID);
    boolean isDirty=false;
    //the problem here is that I only have the doc persistent ID
    //and nothing else, so I need to determine the index of the doc first
    for (int i=0; i< docDataList.size(); i++) {
      DocumentData docData = (DocumentData)docDataList.get(i);
      //we've found the correct document
      //don't break the loop, because it might appear more than once
      if (docID.equals(docData.getPersistentID())) {
        remove(i);
        isDirty = true;
      }//if
    }//for loop through the doc data

    if (isDirty)
      try {
        this.dataStore.sync(this);
      } catch (PersistenceException ex) {
        throw new GateRuntimeException("SerialCorpusImpl: " + ex.getMessage());
      } catch (SecurityException sex) {
        throw new GateRuntimeException("SerialCorpusImpl: " + sex.getMessage());
      }
  }//resourceDeleted

  /**
   * Called by a datastore when a resource has been wrote into the datastore
   */
  public void resourceWritten(DatastoreEvent evt){
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
    //if we've got the doc in memory and it's the same, then return true
    if (DEBUG) Out.prln("Serial corpus: contained called");
    if (this.documents.contains(doc))
      return true;

    //there is no need to search through the docDataList because if someone
    //has got our document, then it must be loaded
    //let's see if there'll be counter examples

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

    //make it accept only docs from its own datastore
    if (doc.getDataStore() != null
        && !this.dataStore.equals(doc.getDataStore())) {
      Err.prln("Error: Persistent corpus can only accept documents " +
               "from its own datastore!");
      return false;
    }//if

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
                                      docDataList.size()-1,
                                      CorpusEvent.DOCUMENT_ADDED));

    return result;
  }

  public boolean remove(Object o){
    if (DEBUG) Out.prln("SerialCorpus:Remove object called");
    if (! (o instanceof Document))
      return false;
    Document doc = (Document) o;

    //see if we can find it first. If not, then judt return
    int index = findDocument(doc);
    if (index == -1)
      return false;

    if(index < docDataList.size()) { //we found it, so remove it
      docDataList.remove(index);
      Document oldDoc =  (Document) documents.remove(index);
      if (DEBUG) Out.prln("documents after remove of " + oldDoc.getName()
                          + " are " + documents);
      fireDocumentRemoved(new CorpusEvent(SerialCorpusImpl.this,
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
    int index = documents.indexOf(doc);
    if (index > -1 && index < docDataList.size())
      return index;

    //else try finding a document with the same name and persistent ID
    Iterator iter = docDataList.iterator();
    for (index = 0;  iter.hasNext() && !found; index++) {
      docData = (DocumentData) iter.next();
      if (docData.getDocumentName().equals(doc.getName()) &&
          docData.getPersistentID().equals(doc.getLRPersistenceId()))
        found = true;
    }
    if (found && index < docDataList.size())
      return index;
    else
      return -1;
  }//findDocument

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

    DocumentData docData = new DocumentData(doc.getName(),
                                            doc.getLRPersistenceId());
    docDataList.add(index, docData);

    documents.add(index, doc);
    fireDocumentAdded(new CorpusEvent(SerialCorpusImpl.this,
                                      doc,
                                      index,
                                      CorpusEvent.DOCUMENT_ADDED));

  }

  public Object remove(int index){
    if (DEBUG) Out.prln("Remove index called");
    docDataList.remove(index);
    Document res = (Document) documents.remove(index);
    fireDocumentRemoved(new CorpusEvent(SerialCorpusImpl.this,
                                        res,
                                        index,
                                        CorpusEvent.DOCUMENT_REMOVED));
    return res;

  }

  public int indexOf(Object o){
    if (o instanceof Document)
      return findDocument((Document) o);

    return -1;
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

  public void setDataStore(DataStore dataStore)
                throws gate.persist.PersistenceException {
    super.setDataStore( dataStore);
    if (this.dataStore != null)
      this.dataStore.addDatastoreListener(this);
  }

  public void setTransientSource(Object source) {
    if (! (source instanceof Corpus))
      return;

    //the following initialisation is only valid when we're constructing
    //this object from a transient one. If it has already been stored in
    //a datastore, then the initialisation is done in readObject() since
    //this method is the one called by serialisation, when objects
    //are restored.
    if (this.dataStore != null && this.lrPersistentId != null)
      return;

    Corpus tCorpus = (Corpus) source;

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

  //we don't keep the transient source, so always return null
  //Sill this must be implemented, coz of the GUI and Factory
  public Object setTransientSource() {
    return null;
  }


  public Resource init() throws gate.creole.ResourceInstantiationException {
    super.init();

    return this;

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
    corpusListeners = new Vector();
    //finally set the creole listeners if the LR is like that
    Gate.getCreoleRegister().addCreoleListener(this);
    if (this.dataStore != null)
      this.dataStore.addDatastoreListener(this);

  }//readObject

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