/*
 *  WordImpl.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 17/May/2002
 *
 *  $Id$
 */

package gate.wordnet;

import java.util.List;
import java.util.ArrayList;

import junit.framework.*;
import net.didion.jwnl.dictionary.*;
//import net.didion.jwnl.data.*;
import net.didion.jwnl.*;
import gate.util.*;


public class WordSenseImpl implements WordSense {

  private Word word;
  private Synset  synset;
  private int senseNumber;
  private int orderInSynset;
  private boolean isSemcor;
  private List lexRelations;

  public WordSenseImpl(Word _word,
                      Synset _synset,
                      int _senseNumber,
                      int _orderInSynset,
                      boolean _isSemcor) {
    this.word = _word;
    this.synset = _synset;
    this.senseNumber = _senseNumber;
    this.orderInSynset = _orderInSynset;
    this.isSemcor = _isSemcor;

  }

  public Word getWord() {
    return this.word;
  }

  public int getPOS() {
    return this.synset.getPOS();
  }

  public Synset getSynset() {
    return this.synset;
  }

  public int getSenseNumber() {
    return this.senseNumber;
  }

  public int getOrderInSynset() {
    return this.orderInSynset;
  }

  public boolean isSemcor() {
    return this.isSemcor;
  }

  public List getLexicalRealtions() {
    throw new MethodNotImplementedException();
  }

  public List getLexicalRealtions(int type) {
    throw new MethodNotImplementedException();
  }

}