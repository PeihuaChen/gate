/*
	CorpusImpl.java 

	Hamish Cunningham, 11/Feb/2000

	$Id$
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

  /** The name of the corpus */
  protected String name;

  /** The features associated with this corpus. */
  protected FeatureMap features;

} // class CorpusImpl
