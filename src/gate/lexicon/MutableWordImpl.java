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
import gate.util.Out;

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

  public void removeSenses() {
    //doing back to front, so no need to shift the numbers of the other senses,
    //as they are all getting removed anyway
    for (int i= senseList.size()-1; i >=0; i--) {
      LexKBWordSense theSense = (LexKBWordSense) senseList.get(i);
      removeSense(theSense);
    }
  }

  public void removeSense(LexKBWordSense theSense) {
    if (! (theSense instanceof MutableLexKBWordSense)) {
      Out.prln("Could not remove sense: " + theSense
               + "because it is not mutable");
      return;
    }
    LexKBSynset theSynset = ((MutableLexKBWordSense)theSense).getSynset();
    if (! (theSynset instanceof MutableLexKBSynset)) {
      Out.prln("Could not remove sense: " + theSense
               + "because it is not mutable");
      return;
    }
    ((MutableLexKBSynset)theSynset).removeSense(theSense);

    for (int i=theSense.getSenseNumber() + 1; i < senseList.size(); i++) {
      LexKBWordSense nextSense = (LexKBWordSense) senseList.get(i);
      if (! (nextSense instanceof MutableLexKBWordSense))
        continue;
      //decrease the sense number by 1
      ((MutableLexKBWordSense)nextSense).setSenseNumber(
          nextSense.getSenseNumber()-1);
    }
    senseList.remove(theSense);
  }
}
