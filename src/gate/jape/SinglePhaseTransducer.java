/*
 *  SinglePhaseTransducer.java - transducer class
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 24/07/98
 *
 *  $Id$
 */


package gate.jape;

import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.io.*;
import com.objectspace.jgl.*;

import gate.annotation.*;
import gate.util.*;
import gate.*;
import gate.fsm.*;
import gate.gui.*;

/**
  * Represents a complete CPSL grammar, with a phase name, options and
  * rule set (accessible by name and by sequence).
  * Implements a transduce method taking a Document as input.
  * Constructs from String or File.
  */
public class SinglePhaseTransducer
extends Transducer implements JapeConstants, java.io.Serializable
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction from name. */
  public SinglePhaseTransducer(String name) {
    this.name = name;
    rules = new PrioritisedRuleList();
    finishedAlready = false;
  } // Construction from name

  /** Type of rule application (constants defined in JapeConstants). */
  private int ruleApplicationStyle = BRILL_STYLE;

  /** Set the type of rule application (types defined in JapeConstants). */
  public void setRuleApplicationStyle(int style) {
    ruleApplicationStyle = style;
  }

  /** The list of rules in this transducer. Ordered by priority and
    * addition sequence (which will be file position if they come from
    * a file).
    */
  private PrioritisedRuleList rules;

  FSM fsm;

  /** Add a rule. */
  public void addRule(Rule rule) {
    rules.add(rule);
  } // addRule

  /** The values of any option settings given. */
  private HashMap optionSettings = new HashMap();

  /** Add an option setting. If this option is set already, the new
    * value overwrites the previous one.
    */
  public void setOption(String name, String setting) {
    optionSettings.put(name, setting);
  } // setOption

  /** Get the value for a particular option. */
  public String getOption(String name) {
    return (String) optionSettings.get(name);
  } // getOption

  /** Whether the finish method has been called or not. */
  private boolean finishedAlready;

  /** Finish: replace dynamic data structures with Java arrays; called
    * after parsing.
    */
  public void finish() {
    // both MPT and SPT have finish called on them by the parser...
    if(finishedAlready)
      return;
    else
      finishedAlready = true;

    for(ForwardIterator i = rules.start(); ! i.atEnd(); i.advance())
      ((Rule) i.get()).finish();
    //build the finite state machine transition graph
    fsm = new FSM(this);
    //convert it to deterministic
    fsm.eliminateVoidTransitions();
  } // finish


  /**
    * Transduce a document using the annotation set provided and the current
    * rule application style.
    */
  public void transduce1(Document doc, AnnotationSet annotationSet)
                                                          throws JapeException {
    fireProgressChangedEvent(0);

    //the input annotations will be read from this set
    AnnotationSet annotations = null;

    //select only the annotations of types specified in the input list
    //Out.println("Input:" + input);
    if(input.isEmpty()) annotations = annotationSet;
    else{
      Iterator typesIter = input.iterator();
      AnnotationSet ofOneType = null;
      while(typesIter.hasNext()){
        ofOneType = annotationSet.get((String)typesIter.next());
        if(ofOneType != null){
    //Out.println("Adding " + ofOneType.getAllTypes());
          if(annotations == null) annotations = ofOneType;
          else annotations.addAll(ofOneType);
        }
      }
    }
    if(annotations == null) annotations = new AnnotationSetImpl(doc);
    //Out.println("Actual input types: " + annotations.getAllTypes() + "\n"+
    //         "Actual input values: " + annotations + "\n===================");
    //INITIALISATION Should we move this someplace else?
    //build the finite state machine transition graph
    FSM fsm = new FSM(this);

    //convert it to deterministic
    fsm.eliminateVoidTransitions();


    //define data structures
    //FSM instances that haven't blocked yet
    java.util.LinkedList activeFSMInstances = new java.util.LinkedList();

    // FSM instances that have reached a final state
    // This is a sorted set and the contained objects are sorted by the length
    // of the document content covered by the matched annotations
    java.util.SortedSet acceptingFSMInstances = new java.util.TreeSet();
    FSMInstance currentFSM;

    // startNode: the node from the current matching attepmt starts.
    // initially startNode = leftMost node
    gate.Node startNode = annotations.firstNode();

    // if there are no annotations return
    if(startNode == null) return;

    // The last node: where the parsing will stop
    gate.Node lastNode = annotations.lastNode();
    int oldStartNodeOff = 0;
    int lastNodeOff = lastNode.getOffset().intValue();
    int startNodeOff;
    //the big while for the actual parsing
    while(startNode != lastNode){
      //System.out.println("Offset: " + startNode.getOffset());
      //while there are more annotations to parse
      //create initial active FSM instance starting parsing from new startNode
      //currentFSM = FSMInstance.getNewInstance(
      currentFSM = new FSMInstance(
                  fsm,
                  fsm.getInitialState(),//fresh start
                  startNode,//the matching starts form the current startNode
                  startNode,//current position in AG is the start position
                  new java.util.HashMap()//no bindings yet!
                  );
      // at this point ActiveFSMInstances should always be empty!
      activeFSMInstances.addLast(currentFSM);
        while(!activeFSMInstances.isEmpty()){
        // while there are some "alive" FSM instances
        // take the first active FSM instance
        currentFSM = (FSMInstance)activeFSMInstances.removeFirst();
        // process the current FSM instance
        if(currentFSM.getFSMPosition().isFinal()){
          // if the current FSM is in a final state
          acceptingFSMInstances.add(currentFSM.clone());
          //  Out.println("==========================\n" +
          //                     "New Accepting FSM:\n" + currentFSM +
          //                     "\n==========================");
        }

        // this will (should) be optimised
        State fsmPosition = currentFSM.getFSMPosition();
        // System.out.println("Current FSM from:" +
        //                 currentFSM.getStartAGPosition().getOffset() +
        //                 " to " + currentFSM.getAGPosition().getOffset());
        java.util.Set transitions = fsmPosition.getTransitions();
        java.util.Iterator transIter = transitions.iterator();
        while(transIter.hasNext()){
          // System.out.print("..");

          //for each transition, check if it is possible and "DO IT!"
          Transition currentTrans = (Transition)transIter.next();
          //holds all the matched annotations. In case of success all these
          //annotations will be added to the bindings Map for the new
          //FSMInstance
          //...using LinkedList instead of HashSet because Annotation does not
          //implement hashCode()

          //get an empty annotation set.
          AnnotationSet matchedAnnots = new AnnotationSetImpl(doc);

          //maps String to gate.FeatureMap
          java.util.Map constraintsByType = new java.util.HashMap();
          Constraint[] currentConstraints =
                       currentTrans.getConstraints().getConstraints();
          String annType;
          FeatureMap newAttributes, oldAttributes;

          for(int i=0; i < currentConstraints.length; i++){
            annType = currentConstraints[i].getAnnotType();
            newAttributes = currentConstraints[i].getAttributeSeq();
            oldAttributes = (FeatureMap)constraintsByType.get(annType);
            if(newAttributes == null){
              if(oldAttributes == null){
                //no constraints about this type.
                constraintsByType.put(annType, Factory.newFeatureMap());
              }
            } else {
              //newAttributes != null
              if(oldAttributes != null) newAttributes.putAll(oldAttributes);
              constraintsByType.put(annType, newAttributes);
            }
          }//for(int i=0; i < currentConstraints.length; i++)
          //try to match all the constraints

          boolean success = true;
          java.util.Iterator typesIter = constraintsByType.keySet().iterator();
          AnnotationSet matchedHere = null;
          Long offset;

          while(success && typesIter.hasNext()) {
            //System.out.print("++");
            //do a query for each annotation type
            annType = (String)typesIter.next();
            newAttributes = (FeatureMap)constraintsByType.get(annType);
            offset = currentFSM.getAGPosition().getOffset();
            matchedHere = annotations.get(annType,
                                          newAttributes,
                                          offset);
            if(matchedHere == null || matchedHere.isEmpty()) success = false;
            else{
              // we have some matched annotations of the current type
              // let's add them to the list of matched annotations
              matchedAnnots.addAll(matchedHere);
            }
          } // while(success && typesIter.hasNext())
          if(success){
            // System.out.println("Success!");
            // create a new FSMInstance, make it advance in AG and in FSM,
            // take care of its bindings data structure and
            // add it to the list of active FSMs.
            FSMInstance newFSMI = (FSMInstance)currentFSM.clone();
            newFSMI.setAGPosition(matchedAnnots.lastNode());
            newFSMI.setFSMPosition(currentTrans.getTarget());
            // do the bindings

            // all the annotations matched here will be added to the sets
            // corresponding to the labels in this list in case of succesful
            // matching
            java.util.Iterator labelsIter =
                                          currentTrans.getBindings().iterator();
            AnnotationSet oldSet, newSet;
            String label;
            java.util.Map binds = newFSMI.getBindings();
            while(labelsIter.hasNext()){
              // for each label add the set of matched annotations to the set of
              // annotations currently bound to that name
              label = (String)labelsIter.next();
              oldSet = (AnnotationSet)binds.get(label);
              if(oldSet != null){
                newSet = new AnnotationSetImpl(oldSet);
                newSet.addAll(matchedAnnots);
              }else{
                newSet = new AnnotationSetImpl(matchedAnnots);
              }
              binds.put(label, newSet);
            } // while(labelsIter.hasNext())
            activeFSMInstances.addLast(newFSMI);
          }
        } // while(transIter.hasNext())
       // return currentFSM to the rightful owner :)
       // FSMInstance.returnInstance(currentFSM);
       } // while(!activeFSMInstances.isEmpty())

       //FIRE THE RULE
      if(acceptingFSMInstances.isEmpty()){
        // System.out.println("\nNo match...");
        // advance to next relevant node in the Annotation Graph
        startNode = annotations.nextNode(startNode);

        // System.out.println("111111111");

        // check to see if there are any annotations starting here
        AnnotationSet annSet = annotations.get(startNode.getOffset());

        // System.out.println("22222222");

        if(annSet == null || annSet.isEmpty()){
          // System.out.println("No way to advance... Bail!");
          // no more starting annotations beyond this point
          startNode = lastNode;
        } else {
          // System.out.print("Advancing...");
          // advance to the next node that has starting annotations
          startNode = ((Annotation)annSet.iterator().next()).getStartNode();
          // System.out.println("done");
        }

      /*
        AnnotationSet res = annotations.get(startNode.getOffset());
        if(!res.isEmpty())
          startNode = ((Annotation)res.iterator().next()).getStartNode();
        else startNode = lastNode;
      */
      } else if(ruleApplicationStyle == BRILL_STYLE) {
        // fire the rules corresponding to all accepting FSM instances
        java.util.Iterator accFSMs = acceptingFSMInstances.iterator();
        FSMInstance currentAcceptor;
        RightHandSide currentRHS;
        long lastAGPosition = startNode.getOffset().longValue();
        //  Out.println("XXXXXXXXXXXXXXXXXXXX All the accepting FSMs are:");

        while(accFSMs.hasNext()){
          currentAcceptor = (FSMInstance) accFSMs.next();
          //  Out.println("==========================\n" +
                  //                     currentAcceptor +
                  //                     "\n==========================");

          currentRHS = currentAcceptor.getFSMPosition().getAction();
          currentRHS.transduce(doc, annotations, currentAcceptor.getBindings());

          long currentAGPosition =
               currentAcceptor.getAGPosition().getOffset().longValue();
          if(lastAGPosition <= currentAGPosition){
            startNode = currentAcceptor.getAGPosition();
            lastAGPosition = currentAGPosition;
          }
        }
      // Out.println("XXXXXXXXXXXXXXXXXXXX");
      } else if(ruleApplicationStyle == APPELT_STYLE) {
        // AcceptingFSMInstances is an ordered structure:
        // just execute the longest (last) rule.
        FSMInstance currentAcceptor =
                                    (FSMInstance)acceptingFSMInstances.last();
        RightHandSide currentRHS = currentAcceptor.getFSMPosition().getAction();
        currentRHS.transduce(doc, annotations, currentAcceptor.getBindings());
        //advance in AG
        startNode = currentAcceptor.getAGPosition();

      } else throw new RuntimeException("Unknown rule application style!");
      //release all the accepting instances as they have done their job
      /*
        Iterator acceptors = acceptingFSMInstances.iterator();
        while(acceptors.hasNext())
        FSMInstance.returnInstance((FSMInstance)acceptors.next());
      */
      acceptingFSMInstances.clear();
      startNodeOff = startNode.getOffset().intValue();

      if(startNodeOff - oldStartNodeOff > 1024){
        fireProgressChangedEvent(100 * startNodeOff / lastNodeOff);
        oldStartNodeOff = startNodeOff;
      }
    } // while(startNode != lastNode)
    // FSMInstance.clearInstances();
    fireProcessFinishedEvent();
  } // transduce

  /**
    * Transduce a document using the annotation set provided and the current
    * rule application style.
    */
  public void transduce(Document doc, AnnotationSet inputAS,
                        AnnotationSet outputAS) throws JapeException {
    fireProgressChangedEvent(0);

    //the input annotations will be read from this set
    AnnotationSet annotations = null;

    //select only the annotations of types specified in the input list
    //Out.println("Input:" + input);
    if(input.isEmpty()) annotations = inputAS;
    else{
      annotations = new AnnotationSetImpl(doc);
      Iterator typesIter = input.iterator();
      AnnotationSet ofOneType = null;
      while(typesIter.hasNext()){
        ofOneType = inputAS.get((String)typesIter.next());
        if(ofOneType != null){
    //Out.println("Adding " + ofOneType.getAllTypes());
          annotations.addAll(ofOneType);
        }
      }
    }
    if(annotations == null) annotations = new AnnotationSetImpl(doc);

    //define data structures
    //FSM instances that haven't blocked yet
    java.util.LinkedList activeFSMInstances = new java.util.LinkedList();

    // FSM instances that have reached a final state
    // This is a sorted set and the contained objects are sorted by the length
    // of the document content covered by the matched annotations
    java.util.SortedSet acceptingFSMInstances = new java.util.TreeSet();
    FSMInstance currentFSM;

    // startNode: the node from the current matching attepmt starts.
    // initially startNode = leftMost node
    gate.Node startNode = annotations.firstNode();

    // if there are no annotations return
    if(startNode == null) return;

    // The last node: where the parsing will stop
    gate.Node lastNode = annotations.lastNode();
    int oldStartNodeOff = 0;
    int lastNodeOff = lastNode.getOffset().intValue();
    int startNodeOff;
    //the big while for the actual parsing
    while(startNode != lastNode){
      //while there are more annotations to parse
      //create initial active FSM instance starting parsing from new startNode
      //currentFSM = FSMInstance.getNewInstance(
      currentFSM = new FSMInstance(
                  fsm,
                  fsm.getInitialState(),//fresh start
                  startNode,//the matching starts form the current startNode
                  startNode,//current position in AG is the start position
                  new java.util.HashMap()//no bindings yet!
                  );
      // at this point ActiveFSMInstances should always be empty!
      activeFSMInstances.addLast(currentFSM);
      while(!activeFSMInstances.isEmpty()){
        // while there are some "alive" FSM instances
        // take the first active FSM instance
        currentFSM = (FSMInstance)activeFSMInstances.removeFirst();
        // process the current FSM instance
        if(currentFSM.getFSMPosition().isFinal()){
          // if the current FSM is in a final state
          acceptingFSMInstances.add(currentFSM.clone());
        }
        //all the annotations that start from the current node.
        AnnotationSet paths = annotations.get(
                                currentFSM.getAGPosition().getOffset());
        if(paths != null){
          Iterator pathsIter = paths.iterator();
          Annotation onePath;
          State currentState = currentFSM.getFSMPosition();
          Iterator transitionsIter;
          //foreach possible annotation
          while(pathsIter.hasNext()){
            onePath = (Annotation)pathsIter.next();
            transitionsIter = currentState.getTransitions().iterator();
            Transition currentTransition;
            Constraint[] currentConstraints;
            transitionsWhile:
            while(transitionsIter.hasNext()){
              currentTransition = (Transition)transitionsIter.next();
              //check if the current transition can use the curent annotation (path)
              currentConstraints =
                           currentTransition.getConstraints().getConstraints();
              String annType;
              FeatureMap features = Factory.newFeatureMap();
              //we assume that all annotations in a contraint are of the same type
              for(int i = 0; i<currentConstraints.length; i++){
                annType = currentConstraints[i].getAnnotType();
                //if wrong type try next transition
                if(!annType.equals(onePath.getType()))continue transitionsWhile;
                features.putAll(currentConstraints[i].getAttributeSeq());
              }
              if(onePath.getFeatures().entrySet().containsAll(features.entrySet())){
                //we have a match
  //System.out.println("Match!");
                //create a new FSMInstance, advance it over the current annotation
                //take care of the bindings  and add it to ActiveFSM
                FSMInstance newFSMI = (FSMInstance)currentFSM.clone();
                newFSMI.setAGPosition(onePath.getEndNode());
                newFSMI.setFSMPosition(currentTransition.getTarget());
                //bindings
                java.util.Map binds = newFSMI.getBindings();
                java.util.Iterator labelsIter =
                                   currentTransition.getBindings().iterator();
                String oneLabel;
                AnnotationSet boundAnnots, newSet;
                while(labelsIter.hasNext()){
                  oneLabel = (String)labelsIter.next();
                  boundAnnots = (AnnotationSet)binds.get(oneLabel);
                  if(boundAnnots != null){
                    newSet = new AnnotationSetImpl(boundAnnots);
                  }else{
                    newSet = new AnnotationSetImpl(doc);
                  }
                  newSet.add(onePath);
                  binds.put(oneLabel, newSet);
                }//while(labelsIter.hasNext())
                activeFSMInstances.addLast(newFSMI);
              }//if match
            }//while(transitionsIter.hasNext())
          }//while(pathsIter.hasNext())
        }//if(paths != null)
      }//while(!activeFSMInstances.isEmpty())

      //FIRE THE RULE
      if(acceptingFSMInstances.isEmpty()){
//System.out.println("No acceptor");
        //no rule to fire just advance to next relevant node in the
        //Annotation Graph
        startNode = annotations.nextNode(startNode);
        // check to see if there are any annotations starting here
        AnnotationSet annSet = annotations.get(startNode.getOffset());

        if(annSet == null || annSet.isEmpty()){
          // no more starting annotations beyond this point
          startNode = lastNode;
        } else {
          // advance to the next node that has starting annotations
          startNode = ((Annotation)annSet.iterator().next()).getStartNode();
        }
      } else if(ruleApplicationStyle == BRILL_STYLE) {
      //System.out.println("Brill acceptor");
        // fire the rules corresponding to all accepting FSM instances
        java.util.Iterator accFSMs = acceptingFSMInstances.iterator();
        FSMInstance currentAcceptor;
        RightHandSide currentRHS;
        long lastAGPosition = startNode.getOffset().longValue();
        //  Out.println("XXXXXXXXXXXXXXXXXXXX All the accepting FSMs are:");

        while(accFSMs.hasNext()){
          currentAcceptor = (FSMInstance) accFSMs.next();
          //  Out.println("==========================\n" +
                  //                     currentAcceptor +
                  //                     "\n==========================");

          currentRHS = currentAcceptor.getFSMPosition().getAction();
          currentRHS.transduce(doc, outputAS, currentAcceptor.getBindings());

          long currentAGPosition =
               currentAcceptor.getAGPosition().getOffset().longValue();
          if(lastAGPosition <= currentAGPosition){
            startNode = currentAcceptor.getAGPosition();
            lastAGPosition = currentAGPosition;
          }
        }
      // Out.println("XXXXXXXXXXXXXXXXXXXX");
      } else if(ruleApplicationStyle == APPELT_STYLE) {
//System.out.println("Appelt acceptor");
        // AcceptingFSMInstances is an ordered structure:
        // just execute the longest (last) rule.

        FSMInstance currentAcceptor =
                                    (FSMInstance)acceptingFSMInstances.last();
        RightHandSide currentRHS = currentAcceptor.getFSMPosition().getAction();
        currentRHS.transduce(doc, outputAS, currentAcceptor.getBindings());
        //advance in AG
        startNode = currentAcceptor.getAGPosition();

      } else throw new RuntimeException("Unknown rule application style!");
      //release all the accepting instances as they have done their job
      /*
        Iterator acceptors = acceptingFSMInstances.iterator();
        while(acceptors.hasNext())
        FSMInstance.returnInstance((FSMInstance)acceptors.next());
      */
      acceptingFSMInstances.clear();
      startNodeOff = startNode.getOffset().intValue();

      if(startNodeOff - oldStartNodeOff > 1024){
        fireProgressChangedEvent(100 * startNodeOff / lastNodeOff);
        oldStartNodeOff = startNodeOff;
      }
    } // while(startNode != lastNode)
    // FSMInstance.clearInstances();
    fireProcessFinishedEvent();
  } // transduce


  //###############end modified versions

  /** Clean up (delete action class files, for e.g.). */
  public void cleanUp() {

    for(DListIterator i = rules.begin(); ! i.atEnd(); i.advance())
      ((Rule) i.get()).cleanUp();

  } // cleanUp

  /** A string representation of this object. */
  public String toString() {
    return toString("");
  } // toString()

  /** A string representation of this object. */
  public String toString(String pad) {
    String newline = Strings.getNl();
    String newPad = Strings.addPadding(pad, INDENT_PADDING);

    StringBuffer buf =
      new StringBuffer(pad + "SPT: name(" + name + "); ruleApplicationStyle(");

    switch(ruleApplicationStyle) {
      case APPELT_STYLE: buf.append("APPELT_STYLE); "); break;
      case BRILL_STYLE:  buf.append("BRILL_STYLE); ");  break;
      default: break;
    }

    buf.append("rules(" + newline);
    Enumeration rulesIterator = rules.elements();
    while(rulesIterator.hasMoreElements())
      buf.append(((Rule) rulesIterator.nextElement()).toString(newPad) + " ");

    buf.append(newline + pad + ")." + newline);

    return buf.toString();
  } // toString(pad)

  //needed by fsm
  public PrioritisedRuleList getRules() {
    return rules;
  }

  /**
    * Adds a new type of input annotations used by this transducer.
    * If the list of input types is empty this transducer will parse all the
    * annotations in the document otherwise the types not found in the input
    * list will be completely ignored! To be used with caution!
    */
  public void addInput(String ident) {
    input.add(ident);
  }

  /**
    * Defines the types of input annotations that this transducer reads. If this
    * set is empty the transducer will read all the annotations otherwise it
    * will only "see" the annotations of types found in this list ignoring all
    * other types of annotations.
    */
  java.util.Set input = new java.util.HashSet();

  /*
  private void writeObject(ObjectOutputStream oos) throws IOException {
    Out.prln("writing spt");
    oos.defaultWriteObject();
    Out.prln("finished writing spt");
  } // writeObject
  */


} // class SinglePhaseTransducer
