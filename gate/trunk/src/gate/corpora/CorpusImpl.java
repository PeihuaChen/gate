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

/** Corpora are sets of Document. They are ordered by lexicographic collation
  * on Url.
  */
public class CorpusImpl extends TreeSet implements Corpus {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public CorpusImpl() {
  } // Construction

  /** The data store this LR lives in. */
  protected transient DataStore dataStore;

  /** Initialise this resource, and return it. */
  public Resource init() {
    return this;
  } // init()

  /** Get the name of the corpus. */
  public String getName() { return name; }

  /** Set the name of the corpus. */
  public void setName(String name) { this.name = name; }

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

  /* two corpus are equal if they have the same documents
   * the same features and the same name
   */
  public boolean equals(Object other) {

    if (!super.equals(other)) return false;

    Corpus corpus;
    if (!(other instanceof CorpusImpl)) return false;
    else corpus = (Corpus)other;

    // verify the name
    if ((name == null)^(corpus.getName() == null)) return false;
    if ((name != null)&& (!name.equals(corpus.getName()))) return false;

    // verify the features
    if ((features == null) ^ (corpus.getFeatures() == null)) return false;
    if ((features != null)&&(!features.equals(corpus.getFeatures())))return false;

    return true;
  }

  public int hashCode() {
    int hash = 0;
    int docHash = 0;
    Iterator iter = this.iterator();
    while (iter.hasNext()) {
      Document currentDoc = (Document)iter.next();
      docHash = (currentDoc == null ? 0 : currentDoc.hashCode());
      hash += docHash;
    }
    int nameHash = (name == null ? 0 : name.hashCode());
    int featureHash = (features == null ? 0 : features.hashCode());

    return hash ^ featureHash ^ nameHash;
  } // hashCode

  /** The name of the corpus */
  protected String name;

  /** The features associated with this corpus. */
  protected FeatureMap features;

} // class CorpusImpl
