/*
 *  CorpusImpl.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 * 
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 * 
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
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
public class CorpusImpl extends TreeSet implements Corpus
{
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** Construction from name */
  public CorpusImpl(String name) {
    this(name, null);
  } // Construction from name

  /** Construction from name and features */
  public CorpusImpl(String name, FeatureMap features) {
    this.name = name;
    this.features = features;
  } // Construction from name and features

  /** Get the name of the corpus. */
  public String getName() { return name; }

  /** Get the data store the document lives in. */
  public DataStore getDataStore() {
    //this is the transient version of corpus, hence return null.
    return null;
  }

  /** Get the features associated with this corpus. */
  public FeatureMap getFeatures() { return features; }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; } 

  /** Get the factory that created this object. */
  public Factory getFactory() {
    throw new LazyProgrammerException();
  } // getFactory()

  /** The name of the corpus */
  protected String name;

  /** The features associated with this corpus. */
  protected FeatureMap features;

} // class CorpusImpl