/*
 *  Corpus.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 19/Jan/2000
 *
 *  $Id$
 */

package gate;
import java.util.*;
import gate.util.*;

/** Corpora are sets of Document. They are ordered by lexicographic collation
  * on Url. TIPSTER equivalent: Collection.
  */
public interface Corpus extends LanguageResource, SortedSet {

  /** Get the name of the corpus. */
  public String getName();

  /** Set the name of the corpus. */
  public void setName(String name);

} // interface Corpus
