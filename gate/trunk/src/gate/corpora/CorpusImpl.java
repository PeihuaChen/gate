/*
 *  CorpusImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 11/Feb/2000
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

/** Corpora are sets of Document. They are ordered by lexicographic collation
  * on Url.
  */
public class CorpusImpl extends AbstractLanguageResource
                        implements Corpus, CreoleListener {

  /** Debug flag */
  private static final boolean DEBUG = false;

  public CorpusImpl(){
    supportList = Collections.synchronizedList(new VerboseList());
    Gate.getCreoleRegister().addCreoleListener(this);
  }


  /**
   * Gets the names of the documents in this corpus.
   * @return a {@link List} of Strings representing the names of the documents
   * in this corpus.
   */
  public List getDocumentNames(){
    ArrayList res = new ArrayList(supportList.size());
    Iterator docIter = supportList.iterator();
    while(docIter.hasNext()){
      res.add(((Document)docIter.next()).getName());
    }
    return res;
  }

  /**
   * Gets the name of a document in this corpus.
   * @param index the index of the document
   * @return a String value representing the name of the document at
   * <tt>index</tt> in this corpus.
   */
  public String getDocumentName(int index){
    return ((Document)supportList.get(index)).getName();
  }

  /**
   * This method does not make sense for transient corpora, so it does
   * nothing.
   */
  public void unloadDocument(Document doc) {
    return;
  }


  /**
   * The underlying list that holds the documents in this corpus.
   */
  protected List supportList = null;

  /**
   * A proxy list that stores the actual data in an internal list and forwards
   * all operations to that one but it also fires the appropiate corpus events
   * when necessary.
   * It also does some type checking so only Documents are accepted as corpus
   * members.
   */
  protected class VerboseList extends AbstractList implements Serializable{



    VerboseList(){
      data = new ArrayList();
    }

    public Object get(int index){
      return data.get(index);
    }

    public int size(){
      return data.size();
    }

    public Object set(int index, Object element){
      if(element instanceof Document){
        Document oldDoc = (Document)data.set(index, element);
        Document newDoc = (Document)element;

        //fire the 2 events
        fireDocumentRemoved(new CorpusEvent(CorpusImpl.this,
                                            oldDoc,
                                            index,
                                            CorpusEvent.DOCUMENT_REMOVED));
        fireDocumentAdded(new CorpusEvent(CorpusImpl.this,
                                          newDoc,
                                          index,
                                          CorpusEvent.DOCUMENT_ADDED));
        return oldDoc;
      }else{
        throw new UnsupportedOperationException(
          getClass().getName() +
          " only accepts gate.Document values as members!\n" +
          element.getClass().getName() + " is not a gate.Document");
      }
    }

    public void add(int index, Object element){
      if(element instanceof Document){
        data.add(index, element);

        //fire the event
        fireDocumentAdded(new CorpusEvent(CorpusImpl.this,
                                          (Document)element,
                                          index,
                                          CorpusEvent.DOCUMENT_ADDED));
      }else{
        throw new UnsupportedOperationException(
          getClass().getName() +
          " only accepts gate.Document values as members!\n" +
          element.getClass().getName() + " is not a gate.Document");
      }
    }

    public Object remove(int index){
      Document oldDoc = (Document)data.remove(index);

      fireDocumentRemoved(new CorpusEvent(CorpusImpl.this,
                                          oldDoc,
                                          index,
                                          CorpusEvent.DOCUMENT_REMOVED));
      return oldDoc;
    }

    /**
     * The List containing the actual data.
     */
    ArrayList data;
  }

  /**
   * This method returns true when the document is already loaded in memory
   */
  public boolean isDocumentLoaded(int index) {
    return true;
  }


  protected void clearDocList() {
    if (supportList == null)
      return;
    supportList.clear();
  }


  //List methods
  //java docs will be automatically copied from the List interface.

  public int size() {
    return supportList.size();
  }

  public boolean isEmpty() {
    return supportList.isEmpty();
  }

  public boolean contains(Object o){
    return supportList.contains(o);
  }

  public Iterator iterator(){
    return supportList.iterator();
  }

  public Object[] toArray(){
    return supportList.toArray();
  }

  public Object[] toArray(Object[] a){
    return supportList.toArray(a);
  }

  public boolean add(Object o){
    return supportList.add(o);
  }

  public boolean remove(Object o){
    return supportList.remove(o);
  }

  public boolean containsAll(Collection c){
    return supportList.containsAll(c);
  }

  public boolean addAll(Collection c){
    return supportList.addAll(c);
  }

  public boolean addAll(int index, Collection c){
    return supportList.addAll(index, c);
  }

  public boolean removeAll(Collection c){
    return supportList.removeAll(c);
  }

  public boolean retainAll(Collection c){
    return supportList.retainAll(c);
  }

  public void clear(){
    supportList.clear();
  }

  public boolean equals(Object o){
    if (! (o instanceof CorpusImpl))
      return false;

    return supportList.equals(o);
  }

  public int hashCode(){
    return supportList.hashCode();
  }

  public Object get(int index){
    return supportList.get(index);
  }

  public Object set(int index, Object element){
    return supportList.set(index, element);
  }

  public void add(int index, Object element){
    supportList.add(index, element);
  }

  public Object remove(int index){
    return supportList.remove(index);
  }

  public int indexOf(Object o){
    return supportList.indexOf(o);
  }

  public int lastIndexOf(Object o){
    return lastIndexOf(o);
  }

  public ListIterator listIterator(){
    return supportList.listIterator();
  }

  public ListIterator listIterator(int index){
    return supportList.listIterator(index);
  }

  public List subList(int fromIndex, int toIndex){
    return supportList.subList(fromIndex, toIndex);
  }


  /** Construction */

  public void cleanup(){
  }

  /** Initialise this resource, and return it. */
  public Resource init() {
    if(documentsList != null && !documentsList.isEmpty()){
      addAll(documentsList);
    }
    return this;
  } // init()


  /**
   * Fills the provided corpus with documents created on the fly from selected
   * files in a directory. Uses a link {@FileFilter} to select which files will
   * be used and which will be ignored.
   * A simple file filter based on extensions is provided in the Gate
   * distribution ({@link gate.util.ExtensionFileFilter}).
   * @param corpus the corpus to be populated
   * @param directory the directory from which the files will be picked. This
   * parameter is an URL for uniformity. It needs to be a URL of type file
   * otherwise an InvalidArgumentException will be thrown.
   * @param filter the file filter used to select files from the target
   * directory. If the filter is <tt>null</tt> all the files will be accepted.
   * @param encoding the encoding to be used for reading the documents
   * @param recurseDirectories should the directory be parsed recursively?. If
   * <tt>true</tt> all the files from the provided directory and all its
   * children directories (on as many levels as necessary) will be picked if
   * accepted by the filter otherwise the children directories will be ignored.
   */
  public static void populate(Corpus corpus, URL directory, FileFilter filter,
                              String encoding, boolean recurseDirectories)
                     throws IOException, ResourceInstantiationException{
    //check input
    if(!directory.getProtocol().equalsIgnoreCase("file"))
      throw new IllegalArgumentException(
        "The URL provided is not of type \"file:\"!");

    File dir = new File(directory.getPath());
    if(!dir.exists())
      throw new FileNotFoundException(dir.toString());

    if(!dir.isDirectory())
      throw new IllegalArgumentException(
        dir.getAbsolutePath() + " is not a directory!");

    //populate the corpus
    File[] files = dir.listFiles(filter);
    if(files != null){
      for(int i = 0; i < files.length; i++){
        File aFile = files[i];
        if(aFile.isDirectory()){
          //recurse dir if required
          if(recurseDirectories){
            populate(corpus, aFile.toURL(), filter,
                     encoding, recurseDirectories);
          }
        }else{
          //create the doc
          StatusListener sListener = (StatusListener)
                                     gate.gui.MainFrame.getListeners().
                                     get("gate.event.StatusListener");
          if(sListener != null) sListener.statusChanged(
            "Reading: " + aFile.getName());
          String docName = aFile.getName() + "_" + Gate.genSym();
          FeatureMap params = Factory.newFeatureMap();
          params.put("sourceUrl", aFile.toURL());
          if(encoding != null) params.put("encoding", encoding);

          corpus.add(Factory.createResource(DocumentImpl.class.getName(),
                                            params, null, docName));
          if(sListener != null) sListener.statusChanged(
            aFile.getName() + " read");
        }
      }
    }
  }//public static void populate

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
    populate(this, directory, filter, encoding, recurseDirectories);
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

  /** Freeze the serialization UID. */
  static final long serialVersionUID = -1113142759053898456L;
  private transient Vector corpusListeners;
  protected transient java.util.List documentsList;


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
  public void setDocumentsList(java.util.List documentsList) {
    this.documentsList = documentsList;
  }
  public java.util.List getDocumentsList() {
    return documentsList;
  }
  public void resourceLoaded(CreoleEvent e) {
  }
  public void resourceUnloaded(CreoleEvent e) {
    Resource res = e.getResource();
    //remove all occurences
    if(res instanceof Document) while(contains(res)) remove(res);
  }

  public void resourceRenamed(Resource resource, String oldName,
                              String newName){
  }

  public void datastoreOpened(CreoleEvent e) {
  }
  public void datastoreCreated(CreoleEvent e) {
  }
  public void datastoreClosed(CreoleEvent e) {
  }
} // class CorpusImpl
