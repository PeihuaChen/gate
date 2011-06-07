/*
 *  Copyright (c) 2009 - 2011, Valentin Tablan.
 *
 *  SPTBuilder.java
 *  
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Valentin Tablan, 2 Aug 2009
 *
 *  $Id: SPTBuilder.java 71 2009-08-04 06:39:03Z valyt $
 */

package gate.jape.plus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ontotext.jape.pda.FSMPDA;
import com.ontotext.jape.pda.SinglePhaseTransducerPDA;
import com.ontotext.jape.pda.StatePDA;
import com.ontotext.jape.pda.TransitionPDA;

import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;

import gate.creole.ResourceInstantiationException;
import gate.fsm.Transition;
import gate.jape.Constraint;
import gate.jape.JapeConstants;
import gate.jape.RightHandSide;
import gate.jape.Rule;
import gate.jape.constraint.AnnotationFeatureAccessor;
import gate.jape.constraint.ConstraintPredicate;
import gate.jape.constraint.ContainsPredicate;
import gate.jape.constraint.WithinPredicate;
import gate.jape.plus.Predicate.PredicateType;
import gate.jape.plus.SPTBase.MatchMode;

/**
 * An utility class for converting a default JAPE transducer into a JAPE-Plus transducer. 
 */
public class SPTBuilder {

  /**
   * Stores the states for the optimised transducer.
   */
  protected List<SPTBase.State> newStates;
  
  /**
   * Stores the rules in the the transducer.
   */
  protected Rule[] rules;
  
  /**
   * Stores the types of annotations actually used in the grammar.
   */
  protected List<String> annotationTypes;
  
  /**
   * Stores the list of predicates for each annotation type.
   */
  protected Map<String, List<Predicate>> predicatesByType;
  
  /**
   * Holds the mapping between input states (old states, represented through their
   *  ID) and new state (represented as their index in the {@link #newStates} array).
   */
  protected OpenIntIntHashMap oldToNewStates;
  
  public SPTBase buildSPT(SinglePhaseTransducerPDA oldSpt) throws ResourceInstantiationException{
    annotationTypes = new ArrayList<String>();
    predicatesByType = new HashMap<String, List<Predicate>>();
    newStates = new ArrayList<SPTBase.State>();
    oldToNewStates = new OpenIntIntHashMap();

    rules = new Rule[oldSpt.getRules().size()];
    rules = ((List<Rule>) oldSpt.getRules()).toArray(rules);
    
    oldSpt.finish();
//    FSM fsm = new FSM(oldSpt);
    FSMPDA fsm = (FSMPDA) oldSpt.getFSM();
    
    createNewStates(fsm);
    createNewTransitions(fsm);
    optimisePredicates();
    
    SPTBase optimisedTransducer = new SPTBase();
    optimisedTransducer.phaseName = oldSpt.getName();
    optimisedTransducer.arrayOfBindingNames = fsm.getBindingNames();
    //annotation types
    optimisedTransducer.annotationTypes = new String[annotationTypes.size()];
    optimisedTransducer.annotationTypes = annotationTypes.toArray(
            optimisedTransducer.annotationTypes);
    //options
    optimisedTransducer.debugMode = oldSpt.isDebugMode();
    optimisedTransducer.groupMatchingMode = oldSpt.isMatchGroupMode();
    //input types
    Set<String> inputTypes = oldSpt.input;
    optimisedTransducer.inputAnnotationTypes = 
        inputTypes == null || inputTypes.size() == 0 ? 
        null : new String[inputTypes.size()];
    if(optimisedTransducer.inputAnnotationTypes != null){
      optimisedTransducer.inputAnnotationTypes = inputTypes.toArray(
              optimisedTransducer.inputAnnotationTypes);
    }
    //match style
    if(oldSpt.getRuleApplicationStyle() == JapeConstants.ALL_STYLE){
      optimisedTransducer.matchMode = MatchMode.ALL;
    }else if(oldSpt.getRuleApplicationStyle() == JapeConstants.APPELT_STYLE){
      optimisedTransducer.matchMode = MatchMode.APPELT;
    }else if(oldSpt.getRuleApplicationStyle() == JapeConstants.BRILL_STYLE){
      optimisedTransducer.matchMode = MatchMode.BRILL;
    }else if(oldSpt.getRuleApplicationStyle() == JapeConstants.FIRST_STYLE){
      optimisedTransducer.matchMode = MatchMode.FIRST;
    }else if(oldSpt.getRuleApplicationStyle() == JapeConstants.ONCE_STYLE){
      optimisedTransducer.matchMode = MatchMode.ONCE;
    }
    //predicates
    optimisedTransducer.predicatesByType = new Predicate[
        optimisedTransducer.annotationTypes.length][];
    for(int i = 0; i < optimisedTransducer.predicatesByType.length; i++){
      String annType = optimisedTransducer.annotationTypes[i];
      List<Predicate> preds = predicatesByType.get(annType);
      if(preds != null){
        optimisedTransducer.predicatesByType[i] = new Predicate[preds.size()];
        optimisedTransducer.predicatesByType[i] = preds.toArray(
                optimisedTransducer.predicatesByType[i]);
      }else{
        optimisedTransducer.predicatesByType[i] = new Predicate[0];
      }
    }
    // rules
    optimisedTransducer.rules = rules;
    rules = null;
    //states
    optimisedTransducer.states = new SPTBase.State[newStates.size()];
    optimisedTransducer.states = newStates.toArray(optimisedTransducer.states);

    optimisedTransducer.
      setControllerEventBlocksAction(oldSpt.getControllerEventBlocksActionClass());

    //cleanup
    annotationTypes = null;
    newStates = null;
    oldToNewStates = null;
    predicatesByType = null;
    rules = null;
    
    return optimisedTransducer;
  }
  
  /**
   * Generates new states for all the old states in the provided FSM. Stores the newly 
   * created states into the {@link #newStates} list, and populates the mapping 
   * between old state IDs and new state IDs in {@link #oldToNewStates}.
   * @param the {@link FSMPDA} from which the old states are obtained. 
   */
  protected void createNewStates(FSMPDA fsm){
    LinkedList<StatePDA> oldStatesQueue = new LinkedList<StatePDA>();
    oldStatesQueue.add(fsm.getInitialState());
    while(oldStatesQueue.size() > 0){
      StatePDA anOldState = oldStatesQueue.removeFirst();
      if(oldToNewStates.containsKey(anOldState.getIndex())){
        //state already converted
      }else{
        //new state required
        SPTBase.State newState = new SPTBase.State(); 
        newStates.add(newState);
        oldToNewStates.put(anOldState.getIndex(), newStates.size() -1);
        //queue all old states reachable from this state
        for(Transition anOldTransition : anOldState.getTransitions()){
          oldStatesQueue.add((StatePDA) anOldTransition.getTarget());
        }
        //if state is final, set the rule value
        newState.rule = -1;
        if(anOldState.isFinal()){
          RightHandSide rhs = anOldState.getAction();
          for(int i = 0; i < rules.length; i++){
            if(rules[i].getRHS() == rhs){
              newState.rule = i;
              break;
            }
          }
        }
      }
    }
  }
  
  /**
   * Parses the provided FSMPDA and converts the old transitions to new ones. The 
   * {@link #newStates} list and the {@link #oldToNewStates} mapping should 
   * already be populated before this method is called. 
   * @param fsm
   * @throws ResourceInstantiationException 
   */
  protected void createNewTransitions(FSMPDA fsm) 
      throws ResourceInstantiationException{
    LinkedList<StatePDA> oldStatesQueue = new LinkedList<StatePDA>();
    oldStatesQueue.add(fsm.getInitialState());
    IntArrayList visitedOldStates = new IntArrayList();
    while(oldStatesQueue.size() > 0){
      StatePDA anOldState = oldStatesQueue.removeFirst();
      if(visitedOldStates.contains(anOldState.getIndex())){
        //state already processed -> nothing to do
      }else{
        if(!oldToNewStates.containsKey(anOldState.getIndex())){
          throw new ResourceInstantiationException(
                  "State mapping error: " +
                  "old state not associated with a new state!");
        }
        SPTBase.State newState = newStates.get(oldToNewStates.get(
                anOldState.getIndex()));
        //now process all transitions
        List<SPTBase.Transition> newTransitions = 
            new LinkedList<SPTBase.Transition>();
        for(Transition t : anOldState.getTransitions()){
          TransitionPDA anOldTransition = (TransitionPDA) t;
          if(!visitedOldStates.contains(anOldTransition.getTarget().getIndex())){
            oldStatesQueue.add((StatePDA) anOldTransition.getTarget());
          }
          if(!oldToNewStates.containsKey(anOldTransition.getTarget().getIndex())){
            throw new ResourceInstantiationException(
                    "State mapping error: " +
                    "old target state not associated with a new state!");
          }
          int newStateTarget = oldToNewStates.get(anOldTransition.getTarget().getIndex());
          SPTBase.Transition newTransition = new SPTBase.Transition();
          newTransitions.add(newTransition);
          newTransition.nextState = newStateTarget;
          newTransition.type = anOldTransition.getType();
          if(newTransition.type != TransitionPDA.TYPE_CONSTRAINT){
        	  continue;
          }
          Constraint[] oldConstraints = anOldTransition.getConstraints().
              getConstraints();
          List<int[]> newConstraints = new ArrayList<int[]>();
          for(int i = 0; i< oldConstraints.length; i++){
            String annType = oldConstraints[i].getAnnotType();
            int annTypeInt = annotationTypes.indexOf(annType);
            if(annTypeInt < 0){
              annotationTypes.add(annType);
              annTypeInt = annotationTypes.size() -1;
            }
            int[] newConstraint = new int[oldConstraints[i].
                                          getAttributeSeq().size() + 2];
            newConstraints.add(newConstraint);
            newConstraint[0] = annTypeInt;
            newConstraint[1] = oldConstraints[i].isNegated() ? -1 : 0;
            int predId = 2;
            for(ConstraintPredicate oldPredicate : 
                oldConstraints[i].getAttributeSeq()){
              newConstraint[predId++] = convertPredicate(annType, oldPredicate);
            }
          }
          //now save the new constraints
          newTransition.constraints = new int[newConstraints.size()][];
          newTransition.constraints = newConstraints.toArray(
                  newTransition.constraints);
        }
        //convert the transitions list to an array
        newState.transitions = new SPTBase.Transition[newTransitions.size()];
        newState.transitions = newTransitions.toArray(newState.transitions);
        
        //finally, mark the old state as visited.
        visitedOldStates.add(anOldState.getIndex());
      }
    }
  }
  
  protected int convertPredicate(String annotationType, 
          ConstraintPredicate oldPredicate) throws ResourceInstantiationException{
    Predicate newPredicate = new Predicate();
    newPredicate.annotationAccessor = oldPredicate.getAccessor();
    String operator = oldPredicate.getOperator();
    if(operator == ConstraintPredicate.EQUAL){
      newPredicate.type = PredicateType.EQ;
    }else if(operator == ConstraintPredicate.GREATER){
      newPredicate.type = PredicateType.GT;
    }else if(operator == ConstraintPredicate.GREATER_OR_EQUAL){
      newPredicate.type = PredicateType.GE;
    }else if(operator == ConstraintPredicate.LESSER){
      newPredicate.type = PredicateType.LT;
    }else if(operator == ConstraintPredicate.LESSER_OR_EQUAL){
      newPredicate.type = PredicateType.LE;
    }else if(operator == ConstraintPredicate.NOT_EQUAL){
      newPredicate.type = PredicateType.NOT_EQ;
    }else if(operator == ConstraintPredicate.NOT_REGEXP_FIND){
      newPredicate.type = PredicateType.REGEX_NOT_FIND;
    }else if(operator == ConstraintPredicate.NOT_REGEXP_MATCH){
      newPredicate.type = PredicateType.REGEX_NOT_MATCH;
    }else if(operator == ConstraintPredicate.REGEXP_FIND){
      newPredicate.type = PredicateType.REGEX_FIND;
    }else if(operator == ConstraintPredicate.REGEXP_MATCH){
      newPredicate.type = PredicateType.REGEX_MATCH;
    }else if(operator == ContainsPredicate.OPERATOR){
      newPredicate.type = PredicateType.CONTAINS;
    }else if(operator == WithinPredicate.OPERATOR){
      newPredicate.type = PredicateType.WITHIN;
    }else{
      throw new ResourceInstantiationException(
              "Constraint predicates with operator \"" + operator +
              "\" are not supported in JAPE-Plus!");
    }
    if(newPredicate.type == PredicateType.CONTAINS) {
      String containedAnnType = null;
      List<Integer> containedPredicates = new LinkedList<Integer>();
      // convert the value
      ContainsPredicate contPredicate = (ContainsPredicate)oldPredicate;
      Object value = oldPredicate.getValue();
      if(value == null) {
        // just annotation type
        containedAnnType = contPredicate.getAnnotType();
      } else  if(value instanceof String) {
        // a simple annotation type
        containedAnnType = (String)value;
      } else if (value instanceof Constraint) {
        Constraint constraint = (Constraint)value;
        containedAnnType = constraint.getAnnotType();
        for(ConstraintPredicate pred : constraint.getAttributeSeq()) {
          containedPredicates.add(convertPredicate(containedAnnType, pred));
        }
      }
      int[] newPredValue = new int[2 + containedPredicates.size()];
      newPredValue[0] = annotationTypes.indexOf(containedAnnType);
      if(newPredValue[0] == -1) {
        annotationTypes.add(containedAnnType);
        newPredValue[0] = annotationTypes.size() -1;
      }
      // contains predicates are always positive
      newPredValue[1] = 1;
      int predIdx = 2;
      for(Integer predId : containedPredicates) {
        newPredValue[predIdx++] = predId;
      }
      newPredicate.featureValue = newPredValue;
    } else if(newPredicate.type == PredicateType.WITHIN) {
      String containedAnnType = null;
      List<Integer> containedPredicates = new LinkedList<Integer>();
      // convert the value
      WithinPredicate contPredicate = (WithinPredicate)oldPredicate;
      Object value = oldPredicate.getValue();
      if(value == null) {
        // just annotation type
        containedAnnType = contPredicate.getAnnotType();
      } else  if(value instanceof String) {
        // a simple annotation type
        containedAnnType = (String)value;
      } else if (value instanceof Constraint) {
        Constraint constraint = (Constraint)value;
        containedAnnType = constraint.getAnnotType();
        for(ConstraintPredicate pred : constraint.getAttributeSeq()) {
          containedPredicates.add(convertPredicate(containedAnnType, pred));
        }
      }
      int[] newPredValue = new int[2 + containedPredicates.size()];
      newPredValue[0] = annotationTypes.indexOf(containedAnnType);
      if(newPredValue[0] == -1) {
        annotationTypes.add(containedAnnType);
        newPredValue[0] = annotationTypes.size() -1;
      }
      // contains predicates are always positive
      newPredValue[1] = 1;
      int predIdx = 2;
      for(Integer predId : containedPredicates) {
        newPredValue[predIdx++] = predId;
      }
      newPredicate.featureValue = newPredValue;
    } else {
      newPredicate.featureValue = oldPredicate.getValue();
    }
    //now see if this is a new predicate or not
    List<Predicate> predsOfType = predicatesByType.get(annotationType);
    if(predsOfType == null){
      predsOfType = new ArrayList<Predicate>();
      predicatesByType.put(annotationType, predsOfType);
    }
    for(int i = 0; i < predsOfType.size(); i++){
      if(predsOfType.get(i).equals(newPredicate)){
        return i;
      }
    }
    //we have a new predicate
    newPredicate.alsoFalse = new int[0];
    newPredicate.alsoTrue = new int[0];
    newPredicate.converselyFalse = new int[0];
    newPredicate.converselyTrue = new int[0];
    predsOfType.add(newPredicate);
    return predsOfType.size() -1; 
  }
  
  protected void optimisePredicates(){
    for(List<Predicate> preds : predicatesByType.values()){
      IntArrayList[] alsoTrue = new IntArrayList[preds.size()];
      IntArrayList[] alsoFalse = new IntArrayList[preds.size()];
      IntArrayList[] convTrue = new IntArrayList[preds.size()];
      IntArrayList[] convFalse = new IntArrayList[preds.size()];
      for(int i = 0; i < preds.size(); i++){
        alsoTrue[i] = new IntArrayList(preds.size());
        alsoFalse[i] = new IntArrayList(preds.size());
        convTrue[i] = new IntArrayList(preds.size());
        convFalse[i] = new IntArrayList(preds.size());
      }
      for(int i = 0; i < preds.size() -1; i++){
        Predicate one = preds.get(i);
        for(int j = i +1; j < preds.size(); j++){
          Predicate other = preds.get(j);
          switch(one.type){
            case EQ:
              switch(other.type){
                case EQ:
                  if(one.annotationAccessor.equals(other.annotationAccessor)){
                    if(one.featureValue.equals(other.featureValue)){
                      alsoTrue[i].add(j);
                      alsoTrue[j].add(i);
                    }else{
                      convFalse[i].add(j);
                      convFalse[j].add(i);
                    }
                  }
                  break;
                default:
              }
              break;
            default:
          }
        }//for j
      }//for i
      for(int i = 0; i< preds.size(); i++){
        Predicate pred = preds.get(i);
        pred.alsoTrue = Arrays.copyOfRange(alsoTrue[i].elements(), 0, 
                alsoTrue[i].size());
        pred.alsoFalse = Arrays.copyOfRange(alsoFalse[i].elements(), 0, 
                alsoFalse[i].size()); 
        pred.converselyTrue = Arrays.copyOfRange(convTrue[i].elements(), 0, 
                convTrue[i].size()); 
        pred.converselyFalse = Arrays.copyOfRange(convFalse[i].elements(), 0, 
                convFalse[i].size()); 
      }
    }//for preds
  }
}
