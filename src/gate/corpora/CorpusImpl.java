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
    //this is the transient version of corpus, hence return null.
    return null;
  }

  /** Get the features associated with this corpus. */
  public FeatureMap getFeatures() { return features; }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; }

  /** The name of the corpus */
  protected String name;

  /** The features associated with this corpus. */
  protected FeatureMap features;

} // class CorpusImpl
