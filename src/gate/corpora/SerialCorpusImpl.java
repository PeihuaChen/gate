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
  private transient Corpus transientCorpus;
  private java.util.List docDataList = null;

  //here I keep document index as key (same as the index in docDataList
  //which defines the document order) and Documents as value
  private transient java.util.HashMap documents = null;

  public SerialCorpusImpl(){
    docDataList = new ArrayList();

    //make sure we fire events when docs are added/removed/etc
    documents = new VerboseHashMap();
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
    transientCorpus = tCorpus;
    //copy the corpus name and features from the one in memory
    this.setName(tCorpus.getName());
    this.setFeatures(tCorpus.getFeatures());

    docDataList = new ArrayList();

    //make sure we fire events when docs are added/removed/etc
    documents = new VerboseHashMap();
    Gate.getCreoleRegister().addCreoleListener(this);
  }

  /**
   * Gets the names of the documents in this corpus.
   * @return a {@link List} of Strings representing the names of the documents
   * in this corpus.
   */
  public List getDocumentNames(){
    if (transientCorpus != null)
      return transientCorpus.getDocumentNames();

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
  public void setDocumentData(List docNames, List IDs){
    Iterator iter1 = docNames.iterator();
    Iterator iter2 = IDs.iterator();
    while (iter1.hasNext() && iter2.hasNext()) {
      DocumentData data = new DocumentData((String) iter1.next(), iter2.next());
      docDataList.add(data);
    }
  }

  /**
   * Gets the name of a document in this corpus.
   * @param index the index of the document
   * @return a String value representing the name of the document at
   * <tt>index</tt> in this corpus.<P>
   */
  public String getDocumentName(int index){
    if (transientCorpus != null)
      return transientCorpus.getDocumentName(index);

    return ((DocumentData) docDataList.get(index)).getDocumentName();
  }

  /**
   * This method NEEDS IMPLEMENTING
   */
  public void unloadDocument(Document doc) {
    throw new MethodNotImplementedException();
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
    Out.prln("document unloaded " + res);
    //remove all occurences
    while(contains(res)) remove(res);
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
    if (transientCorpus != null)
      return transientCorpus.size();

    return docDataList.size();
  }

  public boolean isEmpty() {
    if (transientCorpus != null)
      return transientCorpus.isEmpty();

    return docDataList.isEmpty();
  }

  public boolean contains(Object o){
    //return true if:
    // - the transient corpus contains the object
    // - the document data list contains a document with such a name
    //   and persistent id

    if (transientCorpus != null)
      return transientCorpus.contains(o);

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
    if (transientCorpus != null)
      return transientCorpus.iterator();

    //there is a problem here, because some docs might not be instantiated
    //I actually need to do the trick from the WeakValueHashMap
    return documents.values().iterator();
  }

  public Object[] toArray(){
    if (transientCorpus != null)
      return transientCorpus.toArray();

    //there is a problem here, because some docs might not be instantiated
    throw new MethodNotImplementedException(
                "toArray() is not implemented for SerialCorpusImpl");
  }

  public Object[] toArray(Object[] a){
    if (transientCorpus != null)
      return transientCorpus.toArray(a);

    //there is a problem here, because some docs might not be instantiated
    throw new MethodNotImplementedException(
                "toArray(Object[] a) is not implemented for SerialCorpusImpl");
  }

  public boolean add(Object o){
    if (transientCorpus != null)
      return transientCorpus.add(o);

    if (! (o instanceof Document) || o == null)
      return false;
    Document doc = (Document) o;

    //add the document with its index in the docDataList
    //in this case, since it's going to be added to the end
    //the index will be the size of the docDataList before
    //the addition
    documents.put(new Integer(docDataList.size()), doc);
    DocumentData docData = new DocumentData(doc.getName(),
                                            doc.getLRPersistenceId());
    return docDataList.add(docData);
  }

  public boolean remove(Object o){
    if (transientCorpus != null)
      return transientCorpus.remove(o);

    if (! (o instanceof Document))
      return false;
    Document doc = (Document) o;

    boolean found = false;
    DocumentData docData = null;

    int index = 0;
    //try finding a document with the same name and persistent ID
    Iterator iter = docDataList.iterator();
    while (iter.hasNext() && !found) {
      docData = (DocumentData) iter.next();
      if (docData.getDocumentName().equals(doc.getName()) &&
          docData.getPersistentID().equals(doc.getLRPersistenceId()))
        found = true;
      index++;
    }

    if(found) { //we found it, so remove it
      docDataList.remove(docData);
      documents.remove(new Integer(index));
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
    if (transientCorpus != null)
      return transientCorpus.equals(o);

    if (! (o instanceof SerialCorpusImpl))
      return false;
    SerialCorpusImpl oCorpus = (SerialCorpusImpl) o;
    if ((this == null && oCorpus != null) || oCorpus == null && this != null)
      return false;
    if (oCorpus == this)
      return true;
    if ((oCorpus.lrPersistentId == this.lrPersistentId ||
         oCorpus.lrPersistentId.equals(this.lrPersistentId)) &&
        oCorpus.name.equals(this.name) &&
        oCorpus.dataStore.equals(dataStore) &&
        oCorpus.docDataList.equals(docDataList))
      return true;
    return false;
  }

  public int hashCode(){
    if (transientCorpus != null)
      return transientCorpus.hashCode();
    return docDataList.hashCode();
  }

  public Object get(int index){
    if (transientCorpus != null)
      return transientCorpus.get(index);

    return documents.get(new Integer(index));
  }

  public Object set(int index, Object element){
    throw new gate.util.MethodNotImplementedException();
  }

  public void add(int index, Object element){
    throw new gate.util.MethodNotImplementedException();
  }

  public Object remove(int index){
    if (transientCorpus != null)
      return transientCorpus.remove(index);

    docDataList.remove(index);
    return documents.remove(new Integer(index));
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
    documents = new VerboseHashMap();
  }//readObject

  /**
   * Class used for the documents structure. This is a {@link java.util.HashMap}
   * that fires events when elements are added/removed/set
   */
  protected class VerboseHashMap extends HashMap{

    public void clear() {
      //override, so events are fired
      Iterator iter = this.keySet().iterator();
      while (iter.hasNext())
        remove(iter.next());
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
                                            CorpusEvent.DOCUMENT_REMOVED));
        fireDocumentAdded(new CorpusEvent(SerialCorpusImpl.this,
                                          newDoc,
                                          CorpusEvent.DOCUMENT_ADDED));
      } else { //we're adding a document
        Document newDoc = (Document)value;

        fireDocumentAdded(new CorpusEvent(SerialCorpusImpl.this,
                                          newDoc,
                                          CorpusEvent.DOCUMENT_ADDED));
      }

      return super.put(key, value);
    }  //put

    public Object remove(Object key){
      Document oldDoc = (Document) super.get(key);
      Object res = super.remove(key);
      if(res != null)
        fireDocumentRemoved(new CorpusEvent(SerialCorpusImpl.this,
                                            oldDoc,
                                            CorpusEvent.DOCUMENT_REMOVED));

      return res;
    }//public Object remove(Object key)

    public Object get(Object key){
      if (! (key instanceof Integer))
        return null;
      Object res = super.get(key);
      int index = ((Integer) key).intValue();

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
          res = lr;
        } catch (ResourceInstantiationException ex) {
          Err.prln("Error reading document inside a serialised corpus.");
          throw new GateRuntimeException(ex.getMessage());
        }

      }
      return res;
    }//public Object get(Object key)



  }//protected class VerboseHashMap extends HashMap

  protected class DocumentData implements Serializable {
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

    String docName;
    Object persistentID;
  }
}