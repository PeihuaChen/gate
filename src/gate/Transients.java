/*
 *	Transients.java
 *
 *	Hamish Cunningham, 18/Feb/2000
 *
 *	$Id$
 */

package gate;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.corpora.*;
import gate.util.*;
import gate.annotation.*;

/** Provides static methods for the creation of transient Language
  * Resources. These are non-persistent; to save them a DataStore
  * must adopt them.
  */
public class Transients extends Factory
{
  /** Create a new LanguageResource. */
  public static LanguageResource newLR(Class LRClass, Object[] args) {
    throw new LazyProgrammerException();
  } // newLR(LRClass, args)

  /** Create a new Corpus. */
  public static Corpus newCorpus(String name) {
    return new CorpusImpl(name);
  } // newCorpus

  /** Create a new Document from a URL. */
  public static Document newDocument(URL u) throws IOException {
    return new DocumentImpl(u);
  } // newDocument(URL)

  /** Create a new Document from a String. */
  public static Document newDocument(String s) throws IOException {
    return new DocumentImpl(s);
  } // newDocument(String)

  /** Create a new FeatureMap. */
  public static FeatureMap newFeatureMap() {
    return new SimpleFeatureMapImpl();
  } // newFeatureMap
} // class Transients
