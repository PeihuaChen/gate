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

public interface LexicalKnowledgeBase extends Lexicon {

  /** returns the lexicon version */
  public String getVersion();

  /** returns all synsets for a specific POS */
  public Iterator getSynsets(Object pos);

  /** returns list of WordSense-s for specific lemma */
  public List lookupWord(String lemma);

  /** returns list of WordSense-s for specific lemma of the specified POS */
  public List lookupWord(String lemma, Object pos);

  public Object[] getPOSTypes();

}