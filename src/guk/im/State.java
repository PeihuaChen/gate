/*
 * State.java
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
 * Valentin Tablan, October 2000
 *
 * $Id$
 */
package guk.im;

import java.util.*;

/**
 * A state of the {@link LocaleHandler} FSM.
 *
 */
public class State{

  /**
   * Creates a new state
   *
   * @param isFinal
   */
  public State(boolean isFinal ){
    this.finalState = isFinal;
  }

  /**
   * Default constructor; creates a non final state
   *
   */
  public State(){
    this.finalState = false;
  }

  /**
   * Adds anew action to this state.
   *
   * @param key
   * @param action
   */
  public Action addAction(Key key, Action action){
    return (Action)transitionFunction.put(key, action);
  }

  /**
   * Gets the action this state will activate for a given {@link Key}
   *
   * @param key
   */
  public Action getNext(Key key){
    return (Action)transitionFunction.get(key);
  }

  /**
   * Is this state final?
   *
   */
  public boolean isFinal(){
    return finalState;
  }

  /**
   * Has this state any actions?
   *
   */
  public boolean hasNext(){
    return !transitionFunction.isEmpty();
  }

  /**
   * Sets the final attribute.
   *
   * @param pFinal
   */
  public void setFinal(boolean pFinal){
    finalState = pFinal;
  }
  //maps from Key to Action
  /**
   * The transition function for this state.
   *
   */
  Map transitionFunction = new HashMap();

  /**
   * Is this state final?
   *
   */
  boolean finalState;
}//class State
