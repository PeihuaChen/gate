/*
 *  MutableWord.java
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

public interface MutableWord extends Word {

  /** Add a new sense to this word*/
  public LexKBWordSense addSense(MutableLexKBSynset wordSynset);

  /** Add a new sense to this word at the given index*/
  public LexKBWordSense addSense(int index, MutableLexKBSynset wordSynset);

}