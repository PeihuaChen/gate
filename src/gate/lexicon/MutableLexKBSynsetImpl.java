/*
 *  MutableLexKBSynsetImpl.java
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
import java.io.Serializable;

public class MutableLexKBSynsetImpl implements MutableLexKBSynset, Serializable {

  private Object POS;
  private String definition;
  private Long id;
  private static long nextID = 1;
  private List senses = new ArrayList();

  public MutableLexKBSynsetImpl() {
    id = new Long(nextID);
    nextID++;
  }

  public void setPOS(Object newPOS) {
    POS = newPOS;
  }

  public void setDefinition(String newDefinition) {
    definition = newDefinition;
  }

  public boolean addWordSense(LexKBWordSense newWordSense) {
    return addWordSense(newWordSense, senses.size());
  }

  public boolean addWordSense(LexKBWordSense newWordSense, int offset) {
    if (offset > senses.size() )
      return false;
    senses.add(offset, newWordSense);
    return true;
  }

  public boolean setWordSenseIndex(LexKBWordSense wordSense, int newOffset) {
    if (newOffset > senses.size())
      return false;
    senses.set(newOffset, wordSense);
    return true;
  }

  public Object getId(){
    return id;
  }

  /** returns the part-of-speech for this synset*/
  public Object getPOS(){
    return POS;
  }

  /** textual description of the synset */
  public String getDefinition(){
    return definition;
  }

  /** WordSenses contained in this synset */
  public List getWordSenses(){
    return senses;

  }

  /** get specific WordSense according to its order in the synset - most important senses come first  */
  public LexKBWordSense getWordSense(int offset){
    return (LexKBWordSense) senses.get(offset);
  }

  public String toString() {
    StringBuffer theString = new StringBuffer();
    theString.append("[");
    for (int i = 0; i < senses.size(); i++) {
      LexKBWordSense sense = (LexKBWordSense) senses.get(i);
      theString.append(sense.toString() + ";");
    }//for
    theString.append("]");
    return theString.toString();
  }//toString

  public void removeSenses(){
    senses.clear();
  }

  public void removeSense(LexKBWordSense theSense){
    senses.remove(theSense);
  }

}