/*
 *  NLGLexWordImpl.java
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

import java.io.Serializable;

public class NLGLexWordImpl extends MutableWordImpl implements Serializable {

  static final long serialVersionUID = -3076810814718212187L;

  public NLGLexWordImpl(String lemma) {
    super(lemma);
  }
  public LexKBWordSense addSense(int index, MutableLexKBSynset wordSynset) {
    MutableLexKBSynset newSynset = wordSynset;
    if (newSynset == null)
      throw new RuntimeException("A valid synset must be provided!");
    NLGLexWordSense newSense = new NLGLexWordSenseImpl(
        this, newSynset, index, newSynset.getWordSenses().size());
    getWordSenses().add(index, newSense);
    newSynset.addWordSense(newSense);
    return newSense;
  }


}