/*
 *  FSM.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 29/Mar/2000
 *
 *  $Id$
 */

package gate.fsm;

import java.util.*;

import gate.jape.*;
import gate.util.*;
/**
  * This class implements a standard Finite State Machine.
  * It is used for both deterministic and non-deterministic machines.
  */
public class FSM implements JapeConstants {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /**
    * Builds a standalone FSM starting from a single phase transducer.
    * @param spt the single phase transducer to be used for building this FSM.
    */
  public FSM(SinglePhaseTransducer spt){
    initialState = new State();
    Iterator rulesEnum = spt.getRules().iterator();
    Rule currentRule;

    while(rulesEnum.hasNext()){
      currentRule = (Rule) rulesEnum.next();
      FSM ruleFSM = new FSM(currentRule);
      initialState.addLambdaTransition(ruleFSM.getInitialState());
    }
    minimise();
  }

  /**
    * Builds a FSM starting from a rule. This FSM is actually a part of a larger
    * one (usually the one that is built based on the single phase transducer
    * that contains the rule).
    * @param owner the larger FSM that wil own all the states in the new FSM
    * built by this constructor.
    * @param rule the rule to be used for the building process.
    */
  public FSM(Rule rule) {
    initialState = new State();
    LeftHandSide lhs = rule.getLHS();
    PatternElement[][] constraints =
                       lhs.getConstraintGroup().getPatternElementDisjunction();
    // the rectangular array constraints is a disjunction of sequences of
    // constraints = [[PE]:[PE]...[PE] ||
    //                [PE]:[PE]...[PE] ||
    //                ...
    //                [PE]:[PE]...[PE] ]

    //The current and the next state for the current ROW.
    State currentRowState, nextRowState;
    State finalState = new State();
    PatternElement currentPattern;

    for(int i = 0; i < constraints.length; i++){
      // for each row we have to create a sequence of states that will accept
      // the sequence of annotations described by the restrictions on that row.
      // The final state of such a sequence will always be a final state which
      // will have associated the right hand side of the rule used for this
      // constructor.

      // For each row we will start from the initial state.
      currentRowState = initialState;
      for(int j=0; j < constraints[i].length; j++) {

        // parse the sequence of constraints:
        // For each basic pattern element add a new state and link it to the
        // currentRowState.
        // The case of kleene operators has to be considered!
        currentPattern = constraints[i][j];
        State insulator = new State();
        currentRowState.addLambdaTransition(insulator);
        currentRowState = insulator;
        if(currentPattern instanceof BasicPatternElement) {
          //the easy case
          nextRowState = new State();

          currentRowState.addQuasiTransition(
                                        (BasicPatternElement)currentPattern,
                                        nextRowState, null);
          currentRowState = nextRowState;
        } else if(currentPattern instanceof ComplexPatternElement) {

          // the current pattern is a complex pattern element
          // ..it will probaly be converted into a sequence of states itself.
          currentRowState =  convertComplexPE(
                              currentRowState,
                              (ComplexPatternElement)currentPattern,
                              null);
        } else {
          // we got an unknown kind of pattern
          throw new RuntimeException("Strange looking pattern:"+currentPattern);
        }

      } // for j

      //link the end of the current row to the final state using
      //an empty transition.
      currentRowState.addLambdaTransition(finalState);
    } // for i
    finalState.setAction(rule.getRHS());
    finalState.setFileIndex(rule.getPosition());
    finalState.setPriority(rule.getPriority());
  }

  /**
    * Gets the initial state of this FSM
    * @return an object of type gate.fsm.State representing the initial state.
    */
  public State getInitialState() {
    return initialState;
  } // getInitialState

  /**
    * Receives a state to start from and a complex pattern element.
    * Parses the complex pattern element and creates all the necessary states
    * and transitions for accepting annotations described by the given PE.
    * @param state the state to start from
    * @param cpe the pattern to be recognized
    * @param label the bindings name for all the annotation accepted along
    * the way this is actually a list of Strings. It is necessary to use
    * a list becuase of the reccursive definition of ComplexPatternElement.
    * @return the final state reached after accepting a sequence of annotations
    * as described in the pattern
    */
  private State convertComplexPE(State startState,
                                ComplexPatternElement cpe, List labels){
    /* The labels for this Complex Pattern Element.
    they will contain all the labels from above and the local label*/
    List localLabels = null;
    if(cpe.getBindingName() != null || labels != null){
      localLabels = new ArrayList();
      if(labels != null) localLabels.addAll(labels);
      if(cpe.getBindingName() != null)localLabels.add(cpe.getBindingName());
    }

    PatternElement[][] constraints =
                       cpe.getConstraintGroup().getPatternElementDisjunction();

    // the rectangular array constraints is a disjunction of sequences of
    // constraints = [[PE]:[PE]...[PE] ||
    //                [PE]:[PE]...[PE] ||
    //                ...
    //                [PE]:[PE]...[PE] ]

    //The current and the next state for the current ROW.
    State currentRowState, nextRowState, endState = new State();
    PatternElement currentPattern;

    for(int i = 0; i < constraints.length; i++) {
      // for each row we have to create a sequence of states that will accept
      // the sequence of annotations described by the restrictions on that row.
      // The final state of such a sequence will always be a finale state which
      // will have associated the right hand side of the rule used for this
      // constructor.

      //For each row we will start from the initial state.
      currentRowState = startState;
      for(int j=0; j < (constraints[i]).length; j++) {

        //parse the sequence of constraints:
        //For each basic pattern element add a new state and link it to the
        //currentRowState.
        //The case of kleene operators has to be considered!
        State insulator = new State();
        currentRowState.addLambdaTransition(insulator);
        currentRowState = insulator;
        currentPattern = constraints[i][j];
        if(currentPattern instanceof BasicPatternElement) {

          //the easy case
          nextRowState = new State();
          currentRowState.addQuasiTransition(
                                        (BasicPatternElement)currentPattern,
                                        nextRowState,
                                        localLabels);
          currentRowState = nextRowState;
        } else if(currentPattern instanceof ComplexPatternElement) {

          // the current pattern is a complex pattern element
          // ..it will probaly be converted into a sequence of states itself.
          currentRowState =  convertComplexPE(
                              currentRowState,
                              (ComplexPatternElement)currentPattern,
                              localLabels);
        } else {

          //we got an unknown kind of pattern
          throw new RuntimeException("Strange looking pattern:"+currentPattern);
        }

      } // for j
        // link the end of the current row to the general end state using
        // an empty transition.
        currentRowState.addLambdaTransition(endState);
    } // for i

    // let's take care of the kleene operator
    int kleeneOp = cpe.getKleeneOp();
    switch (kleeneOp){
      case NO_KLEENE_OP:{
        break;
      }
      case KLEENE_QUERY:{
        //allow to skip everything via a null transition
        startState.addLambdaTransition(endState);
        break;
      }
      case KLEENE_PLUS:{

        // allow to return to startState
        endState.addLambdaTransition(startState);
        break;
      }
      case KLEENE_STAR:{

        // allow to skip everything via a null transition
        startState.addLambdaTransition(endState);

        // allow to return to startState
        endState.addLambdaTransition(startState);
        break;
      }
      default:{
        throw new RuntimeException("Unknown Kleene operator"+kleeneOp);
      }
    } // switch (cpe.getKleeneOp())
    return endState;
  } // convertComplexPE

//  /**
//    * Add a new state to the set of states belonging to this FSM.
//    * @param state the new state to be added
//    */
//  protected void addState(State state) {
//    allStates.add(state);
//  } // addState

//  /**
//    * Converts this FSM from a non-deterministic to a deterministic one by
//    * eliminating all the unrestricted transitions.
//    */
//  public void eliminateVoidTransitions() {
//
//    Map newStates = new HashMap();
//    Set dStates = new HashSet();
//
//    LinkedList unmarkedDStates = new LinkedList();
//    Set currentDState = new HashSet();
//
//    currentDState.add(initialState);
//    currentDState = lambdaClosure(currentDState);
//    dStates.add(currentDState);
//    unmarkedDStates.add(currentDState);
//
//    // create a new state that will take the place the set of states
//    // in currentDState
//    initialState = new State();
//    newStates.put(currentDState, initialState);
//
//    // find out if the new state is a final one
//    Iterator innerStatesIter = currentDState.iterator();
//    RightHandSide action = null;
//
//    while(innerStatesIter.hasNext()){
//      State currentInnerState = (State)innerStatesIter.next();
//      if(currentInnerState.isFinal()){
//        if(action != null){
//          Err.prln("JAPE: Ambiguous rules found while minimising the FSM:\n" +
//                   action.toString() + " vs " +
//                   currentInnerState.getAction().toString());
//        }
//        action = (RightHandSide)currentInnerState.getAction();
//        initialState.setAction(action);
//        initialState.setFileIndex(currentInnerState.getFileIndex());
//        initialState.setPriority(currentInnerState.getPriority());
//        break;
//      }
//    }
//
//    while(!unmarkedDStates.isEmpty()) {
//      currentDState = (AbstractSet)unmarkedDStates.removeFirst();
//      Iterator insideStatesIter = currentDState.iterator();
//
//      while(insideStatesIter.hasNext()) {
//        State innerState = (State)insideStatesIter.next();
//        Iterator transIter = innerState.getTransitions().iterator();
//
//        while(transIter.hasNext()) {
//          Transition currentTrans = (Transition)transIter.next();
//
//          if(currentTrans.getConstraints() !=null) {
//            State target = currentTrans.getTarget();
//            Set newDState = new HashSet();
//            newDState.add(target);
//            newDState = lambdaClosure(newDState);
//
//            if(!dStates.contains(newDState)) {
//              dStates.add(newDState);
//              unmarkedDStates.add(newDState);
//              State newState = new State();
//              newStates.put(newDState, newState);
//
//              //find out if the new state is a final one
//              innerStatesIter = newDState.iterator();
//              while(innerStatesIter.hasNext()) {
//                State currentInnerState = (State)innerStatesIter.next();
//
//                if(currentInnerState.isFinal()) {
//                  newState.setAction(
//                          (RightHandSide)currentInnerState.getAction());
//                  newState.setFileIndex(currentInnerState.getFileIndex());
//                  newState.setPriority(currentInnerState.getPriority());
//                  break;
//                }
//              }
//            }// if(!dStates.contains(newDState))
//
//            State currentState = (State)newStates.get(currentDState);
//            State newState = (State)newStates.get(newDState);
//            currentState.addTransition(new Transition(
//                                        currentTrans.getConstraints(),
//                                        newState,
//                                        currentTrans.getBindings()));
//          }// if(currentTrans.getConstraints() !=null)
//
//        }// while(transIter.hasNext())
//
//      }// while(insideStatesIter.hasNext())
//
//    }// while(!unmarkedDstates.isEmpty())
//
//    /*
//    //find final states
//    Iterator allDStatesIter = dStates.iterator();
//    while(allDStatesIter.hasNext()){
//      currentDState = (AbstractSet) allDStatesIter.next();
//      Iterator innerStatesIter = currentDState.iterator();
//      while(innerStatesIter.hasNext()){
//        State currentInnerState = (State) innerStatesIter.next();
//        if(currentInnerState.isFinal()){
//          State newState = (State)newStates.get(currentDState);
//
//          newState.setAction(currentInnerState.getAction());
//          break;
//        }
//      }
//
//    }
//    */
////    allStates = newStates.values();
//  }//eliminateVoidTransitions

  /**
    * Converts this FSM from a non-deterministic to a deterministic one by
    * eliminating all the unrestricted transitions.
    */
  private void minimise() {
    //initialise data structures
    quasiStatesByState = new HashMap();
    detStateByQuasiState = new HashMap();
    unmarkedQuasiStates = new ArrayList();

    //create the quasi-state for the initial state
    getQuasiState(initialState);

    //start the algorithm
    while(!unmarkedQuasiStates.isEmpty()){
      //get an unprocessed quasi-state
      Set currentQuasiState = (Set)unmarkedQuasiStates.remove(0);
      //get the deterministic state for the current quasi-state
      State currentDetState = (State)detStateByQuasiState.
                              get(currentQuasiState);

      //process the quasi-state
      Iterator innerStatesIter = currentQuasiState.iterator();
      while(innerStatesIter.hasNext()){
        State currentInnerState = (State)innerStatesIter.next();
        //check for finality
        if(currentInnerState.isFinal()){
          currentDetState.setAction(currentInnerState.getAction());
          currentDetState.setFileIndex(currentInnerState.getFileIndex());
          currentDetState.setPriority(currentInnerState.getPriority());
        }

        //add the transitions for the new deterministic state
        Set quasiTransitions = currentInnerState.getQuasiTransitions();
        if(quasiTransitions != null){
          Iterator patternsIter = quasiTransitions.iterator();
          while(patternsIter.hasNext()){
            Object[] patternData = (Object[])patternsIter.next();
            currentDetState.addTransition(
                (BasicPatternElement)patternData[0],
                (State)detStateByQuasiState.
                       get(getQuasiState((State)patternData[1])),
                (List)patternData[2]);
          }
        }
      }//while(innerStatesIter.hasNext())

    }//while(!unmarkedQuasiStates.isEmpty())
    initialState = (State)detStateByQuasiState.get(
                          quasiStatesByState.get(initialState));


  }//minimise


  /**
   * Returns the quasi-state containig the provided nondeterministic state.
   * If it doesn't exist yet it will be created and then returned.
   */
  private Set getQuasiState(State state){
    Set quasiState = (Set)quasiStatesByState.get(state);
    if(quasiState == null){
      //create the new quasi-state
      quasiState = new HashSet(){
        public int hashCode(){
Out.prln("HashCode called");
          return hashcode;
        }
        int hashcode = (int) (Math.random() * Integer.MAX_VALUE);
      };
      quasiState.add(state);
      quasiState = lambdaClosure(quasiState);
//Out.prln("new quasistate of size: " + quasiState.size());
      Iterator innerStatesIter = quasiState.iterator();
      while(innerStatesIter.hasNext()){
        quasiStatesByState.put(innerStatesIter.next(), quasiState);
      }
      //create the new deterministic state for the quasi-state
      State newState = new State();
      detStateByQuasiState.put(quasiState, newState);
      unmarkedQuasiStates.add(quasiState);
    }
    return quasiState;
  }//private Set getSetForState(State state)

  /*
    * Computes the lambda-closure (aka epsilon closure) of the given set of
    * states, that is the set of states that are accessible from any of the
    * states in the given set using only unrestricted transitions.
    * @return a set containing all the states accessible from this state via
    * transitions that bear no restrictions.
    */
  private Set lambdaClosure(Set s) {
    // the stack used by the algorithm
    List stack = new LinkedList(s);

    // the set to be returned
    Set lambdaClosure = new HashSet(s);
    State top;
    State currentState;
    while(!stack.isEmpty()){
      top = (State)stack.remove(0);
      Set lambdaSet = top.getLambdaSet();
      if(lambdaSet != null){
//Out.prln("Lamnbda set of size: " + lambdaSet.size());
        Iterator statesIter = lambdaSet.iterator();
        while(statesIter.hasNext()){
          State state = (State)statesIter.next();
          if(!lambdaClosure.contains(state)){
            lambdaClosure.add(state);
            stack.add(state);
          }
        }
      }
    }
//Out.prln("Lambda closure of size: " + lambdaClosure.size());
    return lambdaClosure;
  } // lambdaClosure


  /**
    * The initial state of this FSM.
    */
  private State initialState;

  /**
   * Used by the minimisation algorithm.
   * Maps from a (nondeterministic) state to the set-state containing it
   */
  private Map quasiStatesByState;

  /**
   * Maps from a quasi-state to the actual state created for it
   */
  Map detStateByQuasiState;


  /**
   * Used by the minimisation algorithm.
   * A list of deterministic quasi-states (sets of nondeterministic states)
   * that haven't yet been processed
   */
  private List unmarkedQuasiStates;

} // FSM
