/*
 *  MutableWordImpl.java
 *
 *  Copyright (c) 1998-2003, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 21/February/2003
 *
 *  $Id$
 */

package gate.lexicon;

import java.util.*;
import java.io.Serializable;

public class MutableWordImpl implements MutableWord, Serializable {
  private String lemma = "";
  private List senseList = new ArrayList();

  public MutableWordImpl(String newLemma) {
    this.lemma = newLemma;
  }

  public LexKBWordSense addSense(MutableLexKBSynset wordSynset) {
    return addSense(senseList.size(), wordSynset);
  }

  public LexKBWordSense addSense(int index, MutableLexKBSynset wordSynset) {
    MutableLexKBSynset newSynset = wordSynset;
    if (newSynset == null)
      throw new RuntimeException("A valid synset must be provided!");
    MutableLexKBWordSense newSense = new MutableLexKBWordSenseImpl(
        this, newSynset, index, newSynset.getWordSenses().size());
    senseList.add(index, newSense);
    newSynset.addWordSense(newSense);
    return newSense;
  }

  public List getWordSenses() {
    return senseList;
  }

  public String getLemma() {
    return lemma;
  }

  public int getSenseCount() {
    return senseList.size();
  }

}