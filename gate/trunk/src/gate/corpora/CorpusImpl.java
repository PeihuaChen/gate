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
public class CorpusImpl // implements Corpus
{
  /** Map document source URLs to Documents. */
  TreeMap docsByURL;

  /** The features associated with this corpus. */
  FeatureMap features;

  /** Get the features associated with this corpus. */
  public FeatureMap getFeatures() { return features; }

} // class CorpusImpl
