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
public class CorpusImpl extends AbstractLanguageResource implements Corpus {

  /** Debug flag */
  private static final boolean DEBUG = false;

  protected TreeSet corpusSet = null;

  /** Construction */
  public CorpusImpl() {
    corpusSet = new TreeSet();
  } // Construction

  public CorpusImpl(String name) {
    corpusSet = new TreeSet();
    setName(name);
  } // Construction

  public void cleanup(){
  }

  /** Initialise this resource, and return it. */
  public Resource init() {
    return this;
  } // init()

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

  public Object last() {
    return corpusSet.last();
  }

  public Object first() {
    return corpusSet.first();
  }

  public SortedSet tailSet(Object fromElement){
    return corpusSet.tailSet(fromElement);
  }

  public SortedSet headSet(Object fromElement){
    return corpusSet.headSet(fromElement);
  }

  public SortedSet subSet(Object fromElement, Object toElement){
    return corpusSet.subSet(fromElement, toElement);
  }

  public Comparator comparator() {
    return corpusSet.comparator();
  }

  public boolean removeAll(Collection c) {
    return corpusSet.removeAll(c);
  }

  public boolean retainAll(Collection c) {
    return corpusSet.retainAll(c);
  }

  public boolean containsAll(Collection c) {
    return corpusSet.containsAll(c);
  }

  public Object[] toArray() {
    return corpusSet.toArray();
  }

  public Object[] toArray(Object[] a) {
    return corpusSet.toArray(a);
  }

  public boolean contains(Object o) {
    return corpusSet.contains(o);
  }

  public boolean isEmpty() {
    return corpusSet.isEmpty();
  }

  public int size() {
    return corpusSet.size();
  }

  /* Two corpus are equal if they have the same documents
   * the same features and the same name
   */
  public boolean equals(Object other) {

//    if (!corpusSet.equals(other)) return false;

    Corpus corpus;
    if (!(other instanceof CorpusImpl)) return false;
    else corpus = (Corpus)other;

    if (! corpusSet.containsAll(corpus))
      return false;

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
    return new VerboseIterator(corpusSet.iterator());
  }

  /** Clears all documents in that corpus. Does not clear everything else though
   *  like features, name, etc.
   */
  public void clear() {
    corpusSet.clear();
  }

  /**
   * Overridden so it can check the input and notify the listeners of the
   * addition.
   */
  public boolean add(Object o) {
    if(o instanceof Document){
      boolean res = corpusSet.add(o);
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
      boolean res = corpusSet.remove(o);
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
    this.name = name;
  }

  /** Returns the name of this resource*/
  public String getName(){
    return name;
  }

  protected String name;

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
  }//class VerboseIterator


  //Parameters utility methods
  /**
   * Gets the value of a parameter of this resource.
   * @param paramaterName the name of the parameter
   * @return the current value of the parameter
   */
  public Object getParameterValue(String paramaterName)
                throws ResourceInstantiationException{
    return AbstractResource.getParameterValue(this, paramaterName);
  }

  /**
   * Sets the value for a specified parameter.
   *
   * @param paramaterName the name for the parameteer
   * @param parameterValue the value the parameter will receive
   */
  public void setParameterValue(String paramaterName, Object parameterValue)
              throws ResourceInstantiationException{
    AbstractResource.setParameterValue(this, paramaterName, parameterValue);
  }

  /**
   * Sets the values for more parameters in one step.
   *
   * @param parameters a feature map that has paramete names as keys and
   * parameter values as values.
   */
  public void setParameterValues(FeatureMap parameters)
              throws ResourceInstantiationException{
    AbstractResource.setParameterValues(this, parameters);
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
  }
} // class CorpusImpl
