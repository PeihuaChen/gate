/*
	Corpus.java 

	Hamish Cunningham, 19/Jan/2000

	$Id$
*/

package gate;
import java.util.*;
import gate.util.*;

/** Corpora are sets of Document. They are ordered by lexicographic collation
  * on Url. TIPSTER equivalent: Collection.
  */
public interface Corpus extends LanguageResource, SortedSet
{
  /** Get the name of the corpus. */
  public String getName();

  /**Get a document by id*/
//  public Document getDocument(long id);

} // interface Corpus
