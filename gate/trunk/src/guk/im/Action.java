/*
 * Action.java
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


/** 
 * Defines an action in the FSM of the input method.
 * An action starts from a state and goes into another one adding perhaps something to the composed text.
 * 
 */
public class Action {
  /** 
   * Constructor.
   * 
   * @param nextState the state this action goes to.
   */
  public Action(State nextState){
    this.next = nextState;
    composedText = null;
  }

  /** 
   * Sets the composed text to be added by this action
   * 
   * @param text 
   */
  public void setComposedText(String text){
    composedText = text;
  }

  /** 
   * Gets the composed text added by this action.
   * 
   */
  public String getComposedText(){
    return composedText;
  }

  /** 
   * Gets the state this action leads to.
   * 
   */
  public State getNext(){
    return next;
  }

  /** 
   * The text to be added by this action to the composed text.
   * 
   */
  String composedText;
  /** 
   * The state this action leads to.
   * 
   */
  State next;
}//class Action
