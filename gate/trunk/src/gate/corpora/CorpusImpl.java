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

/** Corpora are sets of Document. They are ordered by lexicographic collation
  * on Url.
  */
public class CorpusImpl extends TreeSet implements Corpus
{
  
  /** Get the data store the document lives in. */
  public DataStore getDataStore() { throw new LazyProgrammerException(); }

  /** Get the features associated with this corpus. */
  public FeatureMap getFeatures() { return features; }

  /** The size of this corpus */
  public int size() { return docsByURL.size(); }

  /** Get an iterator for the members. */
  public Iterator iterator() { return docsByURL.values().iterator(); }

  /** Map document source URLs to Documents. */
  TreeMap docsByURL;

  /** The features associated with this corpus. */
  FeatureMap features;

} // class CorpusImpl
