/*
 *  State.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 11/Apr/2000
 *
 *  $Id$
 */

package gate.fsm;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.jape.*;

/**
 * This class implements a Finite State Machine state.
 *
 */
public class State implements JapeConstants {

  /** Debug flag
   */
  private static final boolean DEBUG = false;

  /**
   * Reports if this state is a final one.
   * Note: A state has an associated action if and only if it is final.
   */
  public boolean isFinal() {
    return action != null;
  }


  /** Sets the action associated to this FINAL state. An action is actually
   * a gate.jape.RightHandSide object.
   * NOTE: only a final state has an associated action so after a call to this
   * method this state will be a final one.
   */
  protected void setAction(RightHandSide rhs) {
    action = rhs;
  }

  /** Sets the value for fileIndex. File index is the index in the jape
   * definition file of the rule that contains as right hand side the action
   * associated to this state. This value is only intended for final states.
   */
  protected void setFileIndex(int i) { fileIndex = i; }

  /** Sets the value for priority. Priority is the priority in the jape
   * definition file of the rule that contains as right hand side the action
   * associated to this state. This value is only intended for final states.
   */
  protected void setPriority(int i) { priority = i; }

  /**
   * Gets the action associated to this state.
   *
   * @return a RightHandSide object
   */
  public RightHandSide getAction() {
    return action;
  }

  /**
   * Attempts to advance from this state usig a set of annotations.
   * @return a set of triplets consisting of:
   * <ul>
   * <li>a List of matched annotations;</li>
   * <li>a List of labels for the matched annotations;</li>
   * <li>the state to advance to</li>
   * </ul>
   */
  public Set attemptMatch(List annotations){
    if(transitions == null) return null;
    //pass all the received annotations through the filters
    Iterator annIter = annotations.iterator();
    while(annIter.hasNext()){
      Annotation ann = (Annotation)annIter.next();
      Map mapForType = (Map)transitions.get(ann.getType());
      if(mapForType != null){
        //check for type-only patterns
        AnnotationHolder holder = (AnnotationHolder)mapForType.get(null);
        if(holder != null){
          holder.setAnnotation(ann);
//Out.prln("Holder filled!");
        }
        //check features
        FeatureMap features = ann.getFeatures();
        if(features != null){
          Set attributesSet = new HashSet(features.keySet());
          attributesSet.retainAll(mapForType.keySet());
          Iterator attrIter = attributesSet.iterator();
          while(attrIter.hasNext()){
            Object attribute = attrIter.next();
            Map mapForAttribute = (Map)mapForType.get(attribute);
            if(mapForAttribute != null){
              Object value = features.get(attribute);
              holder = (AnnotationHolder)mapForAttribute.get(value);
              if(holder != null){
                holder.setAnnotation(ann);
//Out.prln("Holder filled!");
              }
            }//if(mapForAttribute != null)
          }//while(attrIter.hasNext())
        }//if(features != null)
      }//if(mapForType != null)
    }//nextAnnotation: while(annIter.hasNext())

    //collect the results
    Set result = new HashSet();
    Iterator transIter = transitionList.iterator();
    while(transIter.hasNext()){
      Transition trans = (Transition)transIter.next();
      Object[] oneResult = trans.getMatchResult();
      if(oneResult != null) result.add(oneResult);
    }

    //clear all the annotationn holders (temporary data)
    Iterator holderIter = annotationHolders.iterator();
    while(holderIter.hasNext())
      ((AnnotationHolder)holderIter.next()).setAnnotation(null);

    return result;
  }


  /**
   * Returns the index in the definition file of the rule that generated this
   * state.
   * The value for fileIndex is correct only on final states!
   */
  int getFileIndex() { return fileIndex; }

  /**
   * Returns the priority in the definition file of the rule that generated
   * this state.
   * This value is correct only on final states!
   */
  int getPriority() { return priority; }



  /**
   * Adds a new unrestricted transition to this state.
   * @param nextState the new state reacheable from this state via the
   * unrestricted transition.
   */
  public void addLambdaTransition(State nextState){
    if(transitions == null) transitions = new HashMap();
    Set lambdaTransitions = (Set)transitions.get(null);
    if(lambdaTransitions == null){
      lambdaTransitions = new HashSet();
      transitions.put(null, lambdaTransitions);
    }
    lambdaTransitions.add(nextState);
  }//public void addLambdaTransition(State nextState)


  /**
   * Returns the set of states that are linked to his state by a lambda (i.e.
   * unrestricted) transition.
   */
  public Set getLambdaSet(){
    return transitions == null ? null : (Set)transitions.get(null);
  }

  /**
   * Adds a new pattern to this state.
   * These patterns are added while the grammar is first read. In a second phase
   * the FSM graph will be minimised and these patterns will be converted into
   * proper transitions
   * @param pattern the restrictions that need to be satisfied for this
   * transition
   * @param nextState the state this transition leads to
   * @param labels the labels associated to the annotations recognised while
   * advancing over this transition
   */
  void addQuasiTransition(BasicPatternElement pattern, State nextState,
                            List labels){
    if(patterns == null) patterns = new HashSet();
    patterns.add(new Object[]{pattern, nextState, labels});
  }

  Set getQuasiTransitions(){
    return patterns;
  }


  /**
   * Adds a new transition to this state
   * @param pattern the restrictions that need to be satisfied for this
   * transition
   * @param nextState the state this transition leads to
   * @param labels the labels associated to the annotations recognised while
   * advancing over this transition
   */
  public void addTransition(BasicPatternElement pattern, State nextState,
                            List labels){
    Transition transition = (Transition)transitionsByNextState.get(nextState);
    if(transition == null){
      //this is the first time we see this state
      transition = new Transition(nextState);
      //we need to make a copy
      transition.labels = (labels == null) ? null : new ArrayList(labels);
    }
    if(transitions == null) transitions = new HashMap();
    //add the new constraints to the transitions structure
    Constraint[] constraints = pattern.getConstraints();
    for(int i = 0; i < constraints.length; i++){
      String type = constraints[i].getAnnotType();
      FeatureMap attributes = constraints[i].getAttributeSeq();
      Map mapForType = (Map)transitions.get(type);
      if(mapForType == null){
        mapForType = new HashMap();
        transitions.put(type, mapForType);
      }
      if(attributes == null || attributes.isEmpty()){
        //the only restriction is the annotation type
        AnnotationHolder holder = (AnnotationHolder)mapForType.get(null);
        if(holder == null){
          holder = newAnnotationHolder();
          mapForType.put(null, holder);
        }
        transition.matchedAnnotations.add(holder);
      }else{
        Iterator attrIter = attributes.keySet().iterator();
        while(attrIter.hasNext()){
          AnnotationHolder holder = newAnnotationHolder();
          Object attribute = attrIter.next();
          Object value = attributes.get(attribute);
          Map mapForAttribute = (Map)mapForType.get(attribute);
          if(mapForAttribute == null){
            mapForAttribute = new HashMap();
            mapForType.put(attribute, mapForAttribute);
          }
          mapForAttribute.put(value, holder);
          transition.matchedAnnotations.add(holder);
        }//while(attrIter.hasNext())
      }//else attributes == null
    }//for constraints
  }//public void addTransition()

  /**
   * Stores the transition according to the state they lead to.
   */
  protected Map transitionsByNextState = new HashMap();


  /**
   * Stores triplets made of <BasicPatternElement, State, Labels> which will be
   * converted into proper transitions during the minimisation algorithm.
   * Only nondeterministic states will have this structure.
   */
  private Set patterns;

  /**
   * The transitions for this state. This tructure is a map from String (i.e.
   * the annotation type) to Map to Map (annotation type -> attribute name
   * -> attribute value -> {@link AnnotationHolder}.
   *
   * A special case is when the key is <tt>null</tt>: the value for such a key
   * is a List of states: the states that can be reached from this state via
   * null transitions (no constraints). This is used to represent
   * nondeterministic automata.
   */
  protected HashMap transitions = null;

  /**
   * Holds all the objects of type {@link Transition} belonging to this state
   */
  protected List transitionList = new ArrayList();

  /**
   * The right hand side associated to the rule for which this state recognizes
   * the lhs.
   */
  protected RightHandSide action = null;

  /**
   * The index in the definition file of the rule that was used for creating
   * this state.
   * NOTE: this member is consistent only for FINAL STATES!
   */
  protected int fileIndex = 0;

  /**
   * The priority of the rule from which this state derived.
   *
   */
  protected int priority = -1;


  /**
   * A very simple structure capable a
   * of holding an Annotation object.
   */
  private class AnnotationHolder{

    void setAnnotation(Annotation ann){
      this.annotation = ann;
    }

    Annotation getAnnotation(){
      return annotation;
    }
    Annotation annotation;
  }// class AnnotationHolder

  /**
   * Factory method. Creates a new annotation holder and adds it to the list of
   * annotation holders for this state ({@link annotationHolders}).
   */
  protected AnnotationHolder newAnnotationHolder(){
    AnnotationHolder holder = new AnnotationHolder();
    annotationHolders.add(holder);
    return holder;
  }

  /**
   * This class models a transition from this to another one.
   * It holds a list of annotation holders which all need to be filled in order
   * for this transition to take place. It also holds a pointer to the state
   * this transition leads to.
   */
  class Transition {

    Transition (State targetState){
if(targetState == null){
try{
  throw new Exception("foo");
}catch(Exception e){
  e.printStackTrace();
}
}
      this.nextState = targetState;
      matchedAnnotations = new ArrayList();
      transitionsByNextState.put(targetState, this);
      transitionList.add(this);
    }//constructor

    /**
     * Checks to see whether this transition was satisfied during the last
     * match attempt and if so returns a triplet of:
     *
     */
    Object[] getMatchResult(){
      Iterator holdersIter = matchedAnnotations.iterator();
      List annotations = new ArrayList();
      while(holdersIter.hasNext()){
        AnnotationHolder holder = (AnnotationHolder)holdersIter.next();
        if(holder.getAnnotation() == null) return null;
        else annotations.add(holder.getAnnotation());
      }
      Object[] result = new Object[]{annotations, labels, nextState};
      return result;
    }

    /**
     * The labels that need to be associated with the annotations matched in
     * order to advance to the next state.
     */
    List labels;

    /**
     * A list of {@link AnnotationHolder} objects. If they contain non null
     * values then a match has suceeded.
     * The values in this list are from the annotationHolders list.
     *
     * When all the holders in this list contain annotations (i.e. non null
     * values) then this transition is satisfied and the FSM can advance to the
     * state stored in {@link nextState}.
     */
    List matchedAnnotations;

    /**
     * The state to which this transition leads in case of a successful match.
     */
    State nextState;
  }//class Tranzitie

  /**
   * A list of all the annotation holders used by the Target objects. The
   * elements of this lis are is emptied at the begining of each match atttempt.
   */
  List annotationHolders = new ArrayList();

} // State
