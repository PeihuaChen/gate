/*
 *  LexKBSynset.java
 *
 *  Copyright (c) 1998-2003, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 28/January/2003
 *
 *  $Id$
 */

package gate.lexicon;

import java.util.*;

public interface LexKBSynset {

  /** returns the part-of-speech for this synset*/
  public Object getPOS();

  /** textual description of the synset */
  public String getDefinition();

  /** WordSenses contained in this synset */
  public List getWordSenses();

  /** get specific WordSense according to its order in the synset - most important senses come first  */
  public LexKBWordSense getWordSense(int offset);

  /** Returns the Id of the synset, each synset has a unique Id for
   * connection to the ontology
   */
  public Object getId();


}