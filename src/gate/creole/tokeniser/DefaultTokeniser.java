/**
 * Title:        GATE<p>
 * Description:  <p>
 * Copyright:    Copyright (c) 1999<p>
 * Company:      Univ Sheffield<p>
 * @author Hamish, Kalina, Christian, Valentin
 * @version
 */
package gate.creole.tokeniser;

import java.util.*;
import java.io.*;

import gate.util.*;

public class DefaultTokeniser {

  public DefaultTokeniser(String rulesFile) throws FileNotFoundException,
                                                   IOException,
                                                   GateException {
    this(new FileReader(rulesFile));
  }

  public DefaultTokeniser(InputStream rulesIOStr) throws FileNotFoundException,
                                                   IOException,
                                                   GateException {
    this(new InputStreamReader(rulesIOStr));
  }

  public DefaultTokeniser(Reader rulesRdr) throws IOException,
                                                  GateException {
    initialState = new FSMState();
    BufferedReader rulesReader = new BufferedReader(rulesRdr);
    String line = rulesReader.readLine();
    String toParse = "";
    while (line != null){
      if(line.endsWith("\\")){
        toParse += line.substring(0,line.length()-1);
      }else{
        toParse += line;
        parseRule(toParse);
        toParse = "";
      }
      line = rulesReader.readLine();
    }
    eliminateVoidTransitions();
  }


  void parseRule(String line)throws GateException{
    //ignore comments
    if(line.startsWith("#")) return;
    if(line.startsWith("//")) return;
    StringTokenizer st = new StringTokenizer(line, "()+*\" \t\f>", true);
    FSMState finalState = parseLHS(initialState, st, LHStoRHS);
    String rhs = "";
    while(st.hasMoreTokens()) rhs += st.nextToken();
    finalState.setRhs(rhs);
  }

  FSMState parseLHS(FSMState startState, StringTokenizer st, String until)
       throws GateException{
    FSMState currentState = startState;
    String token;
    token = skipIgnoreTokens(st);
    if(null == token) return currentState;
    while(!token.equals(until)){
      if(token.equals("(")){//(..)
        FSMState newState = parseLHS(currentState, st,")");
        //treat the operators
        token = skipIgnoreTokens(st);
        if(null == token) throw
                          new GateException("Tokeniser rule ended too soon!");
        if(token.equals("+")){
          newState.put(null,currentState);
          token = skipIgnoreTokens(st);
          if(null == token) throw
                            new GateException("Tokeniser rule ended too soon!");
        }else if(token.equals("*")){
          currentState.put(null,newState);
          newState.put(null,currentState);
          token = skipIgnoreTokens(st);
          if(null == token) throw
                            new GateException("Tokeniser rule ended too soon!");
        }
        currentState = newState;
        newState = new FSMState();
        currentState.put(null,newState);
        currentState = newState;
      }else if(token.equals("\"")){//".."
        String sType = parseQuotedString(st, "\"");
System.out.println(sType);
        FSMState newState = new FSMState();
        UnicodeType uType = (UnicodeType)characterTypes.get(sType);
        if(null == uType) throw
                  new InvalidRuleException("Invalid type: \"" + sType + "\"");
        currentState.put(uType ,newState);
        //treat the operators
        token = skipIgnoreTokens(st);
        if(null == token) throw
                          new GateException("Tokeniser rule ended too soon!");
        if(token.equals("+")){
          newState.put(null,currentState);
          token = skipIgnoreTokens(st);
          if(null == token) throw
                            new GateException("Tokeniser rule ended too soon!");
        }else if(token.equals("*")){
          currentState.put(null,newState);
          newState.put(null,currentState);
          token = skipIgnoreTokens(st);
          if(null == token) throw
                            new GateException("Tokeniser rule ended too soon!");
        }
        currentState = newState;
        newState = new FSMState();
        currentState.put(null,newState);
        currentState = newState;
      }else{// a type with no quotes
        String sType = token;
System.out.println(sType);
        FSMState newState = new FSMState();
        UnicodeType uType = (UnicodeType)characterTypes.get(sType);
        if(null == uType) throw
                  new InvalidRuleException("Invalid type: \"" + sType + "\"");
        currentState.put(uType ,newState);
        //treat the operators
        token = skipIgnoreTokens(st);
        if(token.equals("+")){
          newState.put(null,currentState);
          token = skipIgnoreTokens(st);
          if(null == token) throw
                            new GateException("Tokeniser rule ended too soon!");
        }else if(token.equals("*")){
          currentState.put(null,newState);
          newState.put(null,currentState);
          token = skipIgnoreTokens(st);
          if(null == token) throw
                            new GateException("Tokeniser rule ended too soon!");
        }
        currentState = newState;
        newState = new FSMState();
        currentState.put(null,newState);
        currentState = newState;
      }

    }
    return currentState;
  }

  String parseQuotedString(StringTokenizer st, String until) throws GateException{
    String token;
    if(st.hasMoreElements()) token = st.nextToken();
    else return null;
    String type = "";
    while(!token.equals(until)){
      type += token;
      if(st.hasMoreElements())token = st.nextToken();
      else throw new GateException("Tokeniser rule ended too soon!");
    }
    return type;
  }

  String skipIgnoreTokens(StringTokenizer st){
    Iterator ignorables;
    boolean ignorableFound = false;
    String currentToken;
    while(true){
      if(st.hasMoreTokens()){
        currentToken = st.nextToken();
        ignorables = ignoreTokens.iterator();
        ignorableFound = false;
        while(!ignorableFound && ignorables.hasNext()){
          if(currentToken.equals((String)ignorables.next()))
            ignorableFound = true;
        }
        if(!ignorableFound) return currentToken;
      }else return null;
    }
  }

  void parseRHS(String rhs){
  }
  /*
  * Computes the lambda-closure (aka epsilon closure) of the given set of
  * states, that is the set of states that are accessible from any of the
  *states in the given set using only unrestricted transitions.
  *@return a set containing all the states accessible from this state via
  *transitions that bear no restrictions.
  */
  private AbstractSet lambdaClosure(Set s){
    //the stack/queue used by the algorithm
    LinkedList list = new LinkedList(s);
    //the set to be returned
    AbstractSet lambdaClosure = new HashSet(s);
    FSMState top;
    FSMState currentState;
    Set nextStates;
    Iterator statesIter;
    while(!list.isEmpty()){
      top = (FSMState)list.removeFirst();
      nextStates = top.nextSet(null);
      if(null != nextStates){
        statesIter = nextStates.iterator();
        while(statesIter.hasNext()){
          currentState = (FSMState)statesIter.next();
          if(!lambdaClosure.contains(currentState)){
            lambdaClosure.add(currentState);
            list.addFirst(currentState);
          }//if(!lambdaClosure.contains(currentState))
        }//while(statesIter.hasNext())
      }//if(null != nextStates)
    }
    return lambdaClosure;
  }

  /**
  * Converts the FSM from a non-deterministic to a deterministic one by
  * eliminating all the unrestricted transitions.
  */
  void eliminateVoidTransitions(){
    Map newStates = new HashMap();
    Set sdStates = new HashSet();
    LinkedList unmarkedDStates = new LinkedList();
    DFSMState dCurrentState = new DFSMState();
    Set sdCurrentState = new HashSet();
    sdCurrentState.add(initialState);
    sdCurrentState = lambdaClosure(sdCurrentState);
    newStates.put(sdCurrentState, dCurrentState);
    sdStates.add(sdCurrentState);
    //find out if the new state is a final one
    Iterator innerStatesIter = sdCurrentState.iterator();
    String rhs;
    FSMState currentInnerState;
    while(innerStatesIter.hasNext()){
      currentInnerState = (FSMState)innerStatesIter.next();
      if(currentInnerState.isFinal()){
        rhs = currentInnerState.getRhs();
        if(dCurrentState.rhs == null || rhs.length() > dCurrentState.rhs.length())
          dCurrentState.rhs = rhs;
      }
    }
    unmarkedDStates.addFirst(sdCurrentState);
    dInitialState = dCurrentState;
    Set nextSet;
    while(!unmarkedDStates.isEmpty()){
//System.out.println("\n\n=====================" + unmarkedDStates.size());
      sdCurrentState = (Set)unmarkedDStates.removeFirst();
      for(int type = Byte.MIN_VALUE; type <= Byte.MAX_VALUE; type++){
//System.out.print(type);
        nextSet = new HashSet();
        innerStatesIter = sdCurrentState.iterator();
        while(innerStatesIter.hasNext()){
          currentInnerState = (FSMState)innerStatesIter.next();
          Set tempSet = currentInnerState.nextSet((byte)type);
          if(null != tempSet) nextSet.addAll(tempSet);
        }//while(innerStatesIter.hasNext())
        if(!nextSet.isEmpty()){
          nextSet = lambdaClosure(nextSet);
          if(!sdStates.contains(nextSet)){
            sdStates.add(nextSet);
            unmarkedDStates.add(nextSet);
            newStates.put(nextSet, new DFSMState());
          }
          ((DFSMState)newStates.get(sdCurrentState)).put(type,(DFSMState)newStates.get(nextSet));
        }//if(!nextSet.isEmpty())
      }//for(byte type = 0; type < 256; type++)
    }//while(!unmarkedDStates.isEmpty())
  }


  public String getFSMgml(){
    return null;
  }

  public String getDFSMgml(){
    return null;
  }
  /** A state of the finite state machine that is the actual tokeniser
    */
  class FSMState{
    public FSMState(){
    }

    void put(UnicodeType type, FSMState state){
      if(null == type) put(128,state);
      else put(type.type , state);
    }

    Set nextSet(UnicodeType type){
      if(null == type) return transitionFunction[256];
      else return transitionFunction[type.type +128];
    }

    Set nextSet(byte type){
      return transitionFunction[type + 128];
    }

    void put(int index, FSMState state){
      if(null == transitionFunction[index +128])
        transitionFunction[index +128] = new HashSet();
      transitionFunction[index +128].add(state);
    }

    void setRhs(String rhs){this.rhs = rhs;}
    String getRhs(){return rhs;}
    boolean isFinal() {return (null != rhs);}
    Set[] transitionFunction = new Set[257];
    String rhs;
  }//class FSMState

  class DFSMState{
    public DFSMState(){
    }

    void put(UnicodeType type, DFSMState state){
      put(type.type, state);
    }

    DFSMState next(UnicodeType type){
      return transitionFunction[type.type +128];
    }

    void put(int index, DFSMState state){
      transitionFunction[index +128] =state;
    }

    void setRhs(String rhs){this.rhs = rhs;}
    String getRhs(){return rhs;}
    boolean isFinal() {return (null != rhs);}
    DFSMState[] transitionFunction = new DFSMState[256];
    String rhs;
  }//class FSMState

  static public void main(String[] args){
    try{
      DefaultTokeniser dt = new DefaultTokeniser("d:/tmp/eg.tok");
    }catch(Exception ex){ex.printStackTrace(System.err);}
  }

  FSMState initialState;
  DFSMState dInitialState;
  static Map characterTypes;
  static String LHStoRHS = ">";
  static Set ignoreTokens;
  static{
    characterTypes = new HashMap();
    characterTypes.put("UPPERCASE_LETTER",
                       new UnicodeType(Character.UPPERCASE_LETTER));
    characterTypes.put("LOWERCASE_LETTER",
                       new UnicodeType(Character.LOWERCASE_LETTER));
    characterTypes.put("TITLECASE_LETTER",
                       new UnicodeType(Character.TITLECASE_LETTER));
    characterTypes.put("MODIFIER_LETTER",
                       new UnicodeType(Character.MODIFIER_LETTER));
    characterTypes.put("OTHER_LETTER",
                       new UnicodeType(Character.OTHER_LETTER));
    characterTypes.put("NON_SPACING_MARK",
                       new UnicodeType(Character.NON_SPACING_MARK));
    characterTypes.put("ENCLOSING_MARK",
                       new UnicodeType(Character.ENCLOSING_MARK));
    characterTypes.put("COMBINING_SPACING_MARK",
                       new UnicodeType(Character.COMBINING_SPACING_MARK));
    characterTypes.put("DECIMAL_DIGIT_NUMBER",
                       new UnicodeType(Character.DECIMAL_DIGIT_NUMBER));
    characterTypes.put("LETTER_NUMBER",
                       new UnicodeType(Character.LETTER_NUMBER));
    characterTypes.put("OTHER_NUMBER",
                       new UnicodeType(Character.OTHER_NUMBER));
    characterTypes.put("SPACE_SEPARATOR",
                       new UnicodeType(Character.SPACE_SEPARATOR));
    characterTypes.put("LINE_SEPARATOR",
                       new UnicodeType(Character.LINE_SEPARATOR));
    characterTypes.put("PARAGRAPH_SEPARATOR",
                       new UnicodeType(Character.PARAGRAPH_SEPARATOR));
    characterTypes.put("CONTROL",
                       new UnicodeType(Character.CONTROL));
    characterTypes.put("FORMAT",
                       new UnicodeType(Character.FORMAT));
    characterTypes.put("SURROGATE",
                       new UnicodeType(Character.SURROGATE));
    characterTypes.put("DASH_PUNCTUATION",
                       new UnicodeType(Character.DASH_PUNCTUATION));
    characterTypes.put("START_PUNCTUATION",
                       new UnicodeType(Character.START_PUNCTUATION));
    characterTypes.put("END_PUNCTUATION",
                       new UnicodeType(Character.END_PUNCTUATION));
    characterTypes.put("CONNECTOR_PUNCTUATION",
                       new UnicodeType(Character.CONNECTOR_PUNCTUATION));
    characterTypes.put("OTHER_PUNCTUATION",
                       new UnicodeType(Character.OTHER_PUNCTUATION));
    characterTypes.put("MATH_SYMBOL",
                       new UnicodeType(Character.MATH_SYMBOL));
    characterTypes.put("CURRENCY_SYMBOL",
                       new UnicodeType(Character.CURRENCY_SYMBOL));
    characterTypes.put("MODIFIER_SYMBOL",
                       new UnicodeType(Character.MODIFIER_SYMBOL));
    characterTypes.put("OTHER_SYMBOL",
                       new UnicodeType(Character.OTHER_SYMBOL));

    ignoreTokens = new HashSet();
    ignoreTokens.add(" ");
    ignoreTokens.add("\t");
    ignoreTokens.add("\f");
  }//static initializer


}//class DefaultTokeniser

/** Used as an object wrapper that holds an Unicode type (the byte value of
  * the static member of java.lang.Character).
  */
class UnicodeType{
  byte type;
  UnicodeType(byte type){ this.type = type;}
}//class UnicodeType


