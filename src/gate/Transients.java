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

/** Provides static methods for the creation of transient Language
  * Resources. These are non-persistent; to save them a DataStore
  * must adopt them.
  */
public class Transients
{
  /** Create a new LanguageResource. */
  public static LanguageResource newLR(Class LRClass, Object[] args) {
    throw new LazyProgrammerException();
  } // newLR(LRClass, args)

  /** Create a new Corpus. */
  public static Corpus newCorpus(String name) {
    return new CorpusImpl(name);
  } // newCorpus

  /** Create a new Document. */
  public static Document newDocument(URL u) throws IOException {
    return new DocumentImpl(u);
  } // newDocument

} // class Transients
