/*
 * FSMState.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan, 11/07/2000
 *
 * $Id$
 */

package gate.creole.gazetteer;

import java.util.*;

import gate.creole.tokeniser.*;
import gate.util.*;

/** Implements a state of the deterministic finite state machine of the
  * gazetter.
  */
class FSMState {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Constructs a new FSMState object and adds it to the list of
    * states of the {@link DefaultGazetteer} provided as owner.
    * @param owner a {@link DefaultGazetteer} object
    */
  public FSMState(DefaultGazetteer owner) {
    myIndex = index++;
    owner.fsmStates.add(this);
  }

  /**Adds a new value to the transition function
    */
  void put(Character chr, FSMState state) {
    transitionFunction.put(chr,state);
  }

  /** This method is used to access the transition function of this state.
    */
  FSMState next(Character chr) {//UnicodeType type){
    return (FSMState)transitionFunction.get(chr);
  }

  /** Returns a GML (Graph Modelling Language) representation of the edges
    * emerging from this state.
    */
  String getEdgesGML() {
    String res = "";
    Iterator charsIter = transitionFunction.keySet().iterator();
    Character currentChar;
    FSMState nextState;

    while(charsIter.hasNext()){
      currentChar = (Character)charsIter.next();
      nextState = next(currentChar);
      res += "edge [ source " + myIndex +
      " target " + nextState.getIndex() +
      " label \"'" + currentChar + "'\" ]\n";
    }
    return res;
  }

  /** Checks whether this state is a final one */
  boolean isFinal() { return !lookupSet.isEmpty(); }

  /** Returns a set of {@link Lookup} objects describing the types of lookups
    * the phrase for which this state is the final one belongs to
    */
  Set getLookupSet(){return lookupSet;}

  /** Adds a new looup description to this state's lookup descriptions set */
  void addLookup(Lookup lookup) {
    lookupSet.add(lookup);
  } // addLookup

  /** Removes a looup description from this state's lookup descriptions set */
  void removeLookup(Lookup lookup) {
    lookupSet.remove(lookup);
  } // removeLookup

  /** Returns the unique ID of this state. */
  int getIndex(){ return myIndex; }


  /** The transition function of this state.
    */
  Map transitionFunction = new HashMap();

  Set lookupSet = new HashSet();

  int myIndex;

  static int index;

  static{
    index = 0;
  }

} // class FSMState