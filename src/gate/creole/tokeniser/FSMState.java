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
 * Valentin Tablan, 27/06/2000
 *
 * $Id$
 */

package gate.creole.tokeniser;

import java.util.*;
  /** A state of the finite state machine that is the kernel tokeniser
    */
  class FSMState{
    /**
      *  This field is "final static" because it brings in
      *  the advantage of dead code elimination
      *  When DEBUG is set on false the code that it guardes will be eliminated
      *  by the compiler. This will spead up the progam a little bit.
      */
    private static final boolean DEBUG = false;

    /**Creates a new FSMState belonging to a specified tokeniser
      *@param owner the tokeniser that contains this new state
      */
    public FSMState(DefaultTokeniser owner){
      myIndex = index++;
      owner.fsmStates.add(this);
    }

    /**Returns the value of the transition function of this state for a given
      *Unicode type.
      *As this state can belong to a non-deterministic automaton, the result
      *will be a set.
      */
    Set nextSet(UnicodeType type){
      if(null == type) return transitionFunction[DefaultTokeniser.maxTypeId];
      else return transitionFunction[type.type];
    }

    /**Returns the value of the transition function of this state for a given
      *Unicode type specified using the internal ids used by the tokeniser.
      *As this state can belong to a non-deterministic automaton, the result
      *will be a set.
      */
    Set nextSet(int type){
      return transitionFunction[type];
    }

    /**Adds a new transition to the transition function of this state
      *@param type the restriction for the new transition; if <code>null</code>
      * this transition will be unrestricted.
      *@param state the vaule of the transition function for the given type
      */
    void put(UnicodeType type, FSMState state){
      if(null == type) put(DefaultTokeniser.maxTypeId, state);
      else put(type.type, state);
    }

    /**Adds a new transition to the transition function of this state
      *@param index the internal index of the Unicode type representing the
      *restriction for the new transition;
      *@param state the vaule of the transition function for the given type
      */
    void put(int index, FSMState state){
      if(null == transitionFunction[index])
        transitionFunction[index] = new HashSet();
      transitionFunction[index].add(state);
    }

    /**Sets the RHS string value*/
    void setRhs(String rhs){this.rhs = rhs;}

    /**Gets the RHS string value*/
    String getRhs(){return rhs;}

    /**Checks whether this state is a final one*/
    boolean isFinal() {return (null != rhs);}

    /**Gets the unique id of this state*/
    int getIndex(){return myIndex;}

    /**Returns a GML representation of all the edges emerging from this state*/
    String getEdgesGML(){
      String res = "";
      Set nextSet;
      Iterator nextSetIter;
      FSMState nextState;
      for(int i = 0; i <= DefaultTokeniser.maxTypeId; i++){
        nextSet = transitionFunction[i];
        if(null != nextSet){
          nextSetIter = nextSet.iterator();
          while(nextSetIter.hasNext()){
            nextState = (FSMState)nextSetIter.next();
            res += "edge [ source " + myIndex +
            " target " + nextState.getIndex() +
            " label \"";
            if(i == DefaultTokeniser.maxTypeId) res += "[]";
            else res += DefaultTokeniser.typeMnemonics[i];
            res += "\" ]\n";
          }//while(nextSetIter.hasNext())
        }
      };
      return res;
    }

    /**The transition function of this state. It's an array mapping from int
      *(the ids used internally by the tokeniser for the Unicode types) to sets
      *of states.
      */
    Set[] transitionFunction = new Set[DefaultTokeniser.maxTypeId + 1];

    /**The RHS string value from which the annotation associated to final states
      *is constructed.
      */
    String rhs;

    /**the unique index of this state*/
    int myIndex;

    /**used for generating unique ids*/
    static int index;

    static{
      index = 0;
    }
  }//class FSMState
