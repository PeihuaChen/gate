/*
 *  MutableLexKBSynset.java
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

public interface MutableLexKBSynset extends LexKBSynset {

  /** sets the part-of-speech for this synset*/
  public void setPOS(Object newPOS);

  /** textual description of the synset */
  public void setDefinition(String newDefinition);

  /** add a new word sense at the end of the synset*/
  public boolean addWordSense(LexKBWordSense newWordSense);

  /** add a new word sense at a given position */
  public boolean addWordSense(LexKBWordSense newWordSense, int offset);

  /** change the offset of an existing word sense */
  public boolean setWordSenseIndex(LexKBWordSense wordSense, int newOffset);
}