/*
 *  MutableLexKBWordSenseImpl.java
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
import java.io.Serializable;

public class MutableLexKBWordSenseImpl implements MutableLexKBWordSense, Serializable {
  Word senseWord = null;
  MutableLexKBSynset senseSynset = null;
  int senseNumber = 0;
  int orderInSynset = 0;

  public MutableLexKBWordSenseImpl(Word myWord, MutableLexKBSynset mySynset,
                                   int mySenseNumber, int myOrderInSynset) {
    senseWord = myWord;
    senseSynset = mySynset;
    senseNumber = mySenseNumber;
    orderInSynset = myOrderInSynset;
  }

  public Word getWord() {
    return senseWord;
  }

  public Object getPOS() {
    if (senseSynset == null)
      return null;
    return senseSynset.getPOS();
  }

  public LexKBSynset getSynset() {
    return senseSynset;
  }

  public int getSenseNumber() {
    return senseNumber;
  }

  public int getOrderInSynset() {
    return orderInSynset;
  }

  public void setOrderInSynset(int newIndex) {
    if (senseSynset == null)
      throw new RuntimeException(
          "Cannot set order in synset, as sense has no synset attached!");
    if (senseSynset.setWordSenseIndex(this, newIndex))
      orderInSynset = newIndex;
  }

  public String toString() {
    return senseWord.getLemma() + "_" + senseNumber;
  }//toString

  public void setSenseNumber(int newNumber) {
    senseNumber = newNumber;
  }
}