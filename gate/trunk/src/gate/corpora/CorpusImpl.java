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

/** Corpora are sets of Document. They are ordered by lexicographic collation
  * on Url.
  */
public class CorpusImpl extends TreeSet implements Corpus {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public CorpusImpl() {
  } // Construction

  public CorpusImpl(String name) {
    setName(name);
  } // Construction

  /** The data store this LR lives in. */
  protected transient DataStore dataStore;

  /** Initialise this resource, and return it. */
  public Resource init() {
    return this;
  } // init()


  /** Get the data store the document lives in. */
  public DataStore getDataStore() {
    return dataStore;
  }


  /** Set the data store that this LR lives in. */
  public void setDataStore(DataStore dataStore) throws PersistenceException {
    this.dataStore = dataStore;
  } // setDataStore(DS)

  /** Save: synchonise the in-memory image of the corpus with the persistent
    * image.
    */
  public void sync() throws PersistenceException {
    if(dataStore == null)
      throw new PersistenceException("LR has no DataStore");

    dataStore.sync(this);
  } // sync()


  /** Get the features associated with this corpus. */
  public FeatureMap getFeatures() { return features; }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; }

  /* Two corpus are equal if they have the same documents
   * the same features and the same name
   */
  public boolean equals(Object other) {

    if (!super.equals(other)) return false;

    Corpus corpus;
    if (!(other instanceof CorpusImpl)) return false;
    else corpus = (Corpus)other;

    // verify the name
    String name = getName();
    if ((name == null)^(corpus.getName() == null)) return false;
    if ((name != null)&& (!name.equals(corpus.getName()))) return false;

    // verify the features
    if ((features == null) ^ (corpus.getFeatures() == null)) return false;
    if ((features != null)&&(!features.equals(corpus.getFeatures())))return false;

    return true;
  }

  /** A Hash value for this corpus */
  public int hashCode() {
    int hash = 0;
    int docHash = 0;
    Iterator iter = this.iterator();
    while (iter.hasNext()) {
      Document currentDoc = (Document)iter.next();
      docHash = (currentDoc == null ? 0 : currentDoc.hashCode());
      hash += docHash;
    }
    int nameHash = (getName() == null ? 0 : getName().hashCode());
    int featureHash = (features == null ? 0 : features.hashCode());

    return hash ^ featureHash ^ nameHash;
  } // hashCode


  /**
   * Overridden so it returns an iterator that generates events when elements
   * are removed.
   */
  public Iterator iterator(){
    return new VerboseIterator(super.iterator());
  }

  /**
   * Overridden so it can check the input and notify the listeners of the
   * addition.
   */
  public boolean add(Object o) {
    if(o instanceof Document){
      boolean res = super.add(o);
      if(res) fireDocumentAdded(new CorpusEvent(this, (Document)o,
                                CorpusEvent.DOCUMENT_ADDED));
      return res;
    }else{
      throw new IllegalArgumentException(
        "Cannot add a " + o.getClass().toString() + " to a corpus");
    }
  }

  /**
   * Overridden so it can check the input and notify the listeners of the
   * addition.
   */
  public boolean addAll(Collection c){
    boolean modified = false;
    Iterator e = c.iterator();
    while (e.hasNext()) {
        if(add(e.next()))
            modified = true;
    }
    return modified;
  }

  /**
   * Overridden so it can check the input and notify the listeners of the
   * removal.
   */
  public boolean remove(Object o) {
    if(o instanceof Document){
      boolean res = super.remove(o);
      if(res)
        fireDocumentRemoved(new CorpusEvent(this, (Document)o,
                                  CorpusEvent.DOCUMENT_REMOVED));
      return res;
    }else{
      throw new IllegalArgumentException(
        "gate.Corpus.remove():\n" +
        "A corpus cannot contain a " + o.getClass().toString() + "!");
    }
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


  /** Sets the name of this resource*/
  public void setName(String name){
    FeatureMap fm = getFeatures();
    if(fm == null){
      fm = Factory.newFeatureMap();
      setFeatures(fm);
    }
    Gate.setName(fm, name);
  }

  /** Returns the name of this resource*/
  public String getName(){
    FeatureMap fm = getFeatures();
    if(fm == null) return null;
    else return Gate.getName(fm);
  }

  /** The features associated with this corpus. */
  protected FeatureMap features;


  /** Freeze the serialization UID. */
  static final long serialVersionUID = 404036675903473841L;
  private transient Vector corpusListeners;


  class VerboseIterator implements Iterator{
    VerboseIterator (Iterator iterator){
      this.iterator = iterator;
    }

    public boolean hasNext(){
      return iterator.hasNext();
    }

    public Object next(){
      return lastNext = iterator.next();
    }

    public void remove(){
      iterator.remove();
//      if (! Main.batchMode)
        fireDocumentRemoved(new CorpusEvent(CorpusImpl.this, (Document)lastNext,
                                  CorpusEvent.DOCUMENT_REMOVED));
    }
    Iterator iterator;
    Object lastNext;
  }
  protected void fireDocumentAdded(CorpusEvent e) {
    if (/*!Main.batchMode &&*/ corpusListeners != null) {
      Vector listeners = corpusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CorpusListener) listeners.elementAt(i)).documentAdded(e);
      }
    }
  }
  protected void fireDocumentRemoved(CorpusEvent e) {
    if (/*!Main.batchMode &&*/ corpusListeners != null) {
      Vector listeners = corpusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CorpusListener) listeners.elementAt(i)).documentRemoved(e);
      }
    }
  }///class VerboseIterator
} // class CorpusImpl
