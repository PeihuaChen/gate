/*
 * DFSMState.java
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

import gate.util.*;

/** Implements a state of the deterministic finite state machine of the
  * tokeniser.
  * It differs from {@link FSMState FSMState} by the definition of the
  * transition function which in this case maps character types to other states
  * as oposed to the transition function from FSMState which maps character
  * types to sets of states, hence the nondeterministic character.
  * {@see FSMState FSMState}
  */
class DFSMState { //extends FSMState{

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Constructs a new DFSMState object and adds it to the list of deterministic
    * states of the {@link DefaultTokeniser DefaultTokeniser} provided as owner.
    * @param owner a {@link DefaultTokeniser DefaultTokeniser} object
    */
  public DFSMState(DefaultTokeniser owner){
    myIndex = index++;
    owner.dfsmStates.add(this);
  }

  /** Adds a new mapping in the transition function of this state
    * @param type the UnicodeType for this mapping
    * @state the next state of the FSM Machine when a character of type type
    * is read from the input.
    */
  void put(UnicodeType type, DFSMState state){
    put(type.type, state);
  } // put(UnicodeType type, DFSMState state)

  /** Adds a new mapping using the actual index in the internal array.
    * This method is for internal use only. Use {@link #put(UnicodeType}
    * instead.
    */
  void put(int index, DFSMState state){
    transitionFunction[index] = state;
  } // put(int index, DFSMState state)

  /** This method is used to access the transition function of this state.
    * @param type the Unicode type identifier as the corresponding static value
    * on {@link java.lang.Character}
    */
  DFSMState next(int type){//UnicodeType type){
    return transitionFunction[type];
  } // next

  /** Returns a GML (Graph Modelling Language) representation of the edges
    * emerging from this state
    */
  String getEdgesGML(){
    String res = "";
    Set nextSet;
    Iterator nextSetIter;
    DFSMState nextState;

    for(int i = 0; i< transitionFunction.length; i++){
      nextState = transitionFunction[i];
      if(null != nextState){
        res += "edge [ source " + myIndex +
        " target " + nextState.getIndex() +
        " label \"";
        res += DefaultTokeniser.typeMnemonics[i];
        res += "\" ]\n";
      }
    };
    return res;
  } // getEdgesGML

  /** Builds the token description for the token that will be generated when
    * this <b>final</b> state will be reached and the action associated with it
    * will be fired.
    * See also {@link #setRhs(String)}.
    */
  void buildTokenDesc() throws TokeniserException{
    String ignorables = " \t\f";
    String token = null,
           type = null,
           attribute = null,
           value = null,
           prefix = null,
           read ="";
    LinkedList attributes = new LinkedList(),
               values = new LinkedList();
    StringTokenizer mainSt =
      new StringTokenizer(rhs, ignorables + "\\\";=", true);

    int descIndex = 0;
    //phase means:
    //0 == looking for type;
    //1 == looking for attribute;
    //2 == looking for value;
    //3 == write the attr/value pair
    int phase = 0;

    while(mainSt.hasMoreTokens()) {
      token = DefaultTokeniser.skipIgnoreTokens(mainSt);

      if(token.equals("\\")){
        if(null == prefix) prefix = mainSt.nextToken();
        else prefix += mainSt.nextToken();
        continue;
      } else if(null != prefix) {
        read += prefix;
        prefix = null;
      }

      if(token.equals("\"")){

        read = mainSt.nextToken("\"");
        if(read.equals("\"")) read = "";
        else {
          //delete the remaining enclosing quote and restore the delimiters
          mainSt.nextToken(ignorables + "\\\";=");
        }

      } else if(token.equals("=")) {

        if(phase == 1){
          attribute = read;
          read = "";
          phase = 2;
        }else throw new TokeniserException("Invalid attribute format: " +
                                           read);
      } else if(token.equals(";")) {
        if(phase == 0){
          type = read;
          read = "";
          //Out.print("Type: " + type);
          attributes.addLast(type);
          values.addLast("");
          phase = 1;
        } else if(phase == 2) {
          value = read;
          read = "";
          phase = 3;
        } else throw new TokeniserException("Invalid value format: " +
                                           read);
      } else read += token;

      if(phase == 3) {
        // Out.print("; " + attribute + "=" + value);
        attributes.addLast(attribute);
        values.addLast(value);
        phase = 1;
      }
    }
    //Out.println();
    if(attributes.size() < 1)
      throw new InvalidRuleException("Invalid right hand side " + rhs);
    tokenDesc = new String[attributes.size()][2];

    for(int i = 0; i < attributes.size(); i++) {
      tokenDesc[i][0] = (String)attributes.get(i);
      tokenDesc[i][1] = (String)values.get(i);
    }

    // for(int i = 0; i < attributes.size(); i++){
    //    Out.println(tokenDesc[i][0] + "=" +
    //                  tokenDesc[i][1]);
    // }
  } // buildTokenDesc

  /** Sets the right hand side associated with this state. The RHS is
    * represented as a string value that will be parsed by the
    * {@link #buildTokenDesc()} method being converted in a table of strings
    * with 2 columns and as many lines as necessary.
    * @param rhs the RHS string
    */
  void setRhs(String rhs) { this.rhs = rhs; }

  /** Returns the RHS string*/
  String getRhs(){return rhs;}

  /** Checks whether this state is a final one*/
  boolean isFinal() { return (null != rhs); }

  /** Returns the unique ID of this state.*/
  int getIndex() { return myIndex; }

  /** Returns the token description associated with this state. This description
    * is built by {@link #buildTokenDesc()} method and consists of a table of
    * strings having two columns.
    * The first line of the table contains the annotation type on the first
    * position and nothing on the second.
    * Each line after the first one contains a attribute on the first position
    * and its associated value on the second.
    */
  String[][] getTokenDesc() {
    return tokenDesc;
  }

  /** A table of strings describing an annotation.
    * The first line of the table contains the annotation type on the first
    * position and nothing on the second.
    * Each line after the first one contains a attribute on the first position
    * and its associated value on the second.
    */
  String[][] tokenDesc;

  /** The transition function of this state.
    */
  DFSMState[] transitionFunction = new DFSMState[DefaultTokeniser.maxTypeId];

  /** The string of the RHS of the rule from which the token
    * description is built
    */
  String rhs;

  /** The unique index of this state*/
  int myIndex;

  /** Used to generate unique indices for all the objects of this class*/
  static int index;

  static {
    index = 0;
  }

} // class DFSMState