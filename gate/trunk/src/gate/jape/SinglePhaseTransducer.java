/*
 *  SinglePhaseTransducer.java - transducer class
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
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
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
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
  } // finish


  /**
  * Transduce a document using the annotation set provided and the current
  * rule application style.
  */
  public void transduce(Document doc, AnnotationSet annotationSet) throws JapeException {
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

//Out.println("Actual input:" + annotations.getAllTypes());
    //INITIALISATION Should we move this someplace else?
    //build the finite state machine transition graph
    FSM fsm = new FSM(this);
    //convert it to deterministic
    fsm.eliminateVoidTransitions();


    //define data structures
    //FSM instances that haven't blocked yet
    java.util.LinkedList activeFSMInstances = new java.util.LinkedList();
    //FSM instances that have reached a final state
    //This is a sorted set and the contained objects are sorted by the length
    //of the document content covered by the matched annotations
    java.util.SortedSet acceptingFSMInstances = new java.util.TreeSet();
    FSMInstance currentFSM;
    //startNode: the node from the current matching attepmt starts.
    //initially startNode = leftMost node
    gate.Node startNode = annotations.firstNode();
    //if there are no annotations return
    if(startNode == null) return;
    //The last node: where the parsing will stop
    gate.Node lastNode = annotations.lastNode();
    int oldStartNodeOff = 0;
    int lastNodeOff = lastNode.getOffset().intValue();
    int startNodeOff;
    //the big while for the actual parsing
    while(startNode != lastNode){
      //while there are more annotations to parse
      //create initial active FSM instance starting parsing from new startNode
//      currentFSM = FSMInstance.getNewInstance(
      currentFSM = new FSMInstance(
                  fsm,
                  fsm.getInitialState(),//fresh start
                  startNode,//the matching starts form the current startNode
                  startNode,//current position in AG is the start position
                  new java.util.HashMap()//no bindings yet!
                  );
      //at this point ActiveFSMInstances should always be empty!
      activeFSMInstances.addLast(currentFSM);
        while(!activeFSMInstances.isEmpty()){
        //while there are some "alive" FSM instances
        //take the first active FSM instance
        currentFSM = (FSMInstance)activeFSMInstances.removeFirst();
        //process the current FSM instance
        if(currentFSM.getFSMPosition().isFinal()){
          //if the current FSM is in a final state
          acceptingFSMInstances.add(currentFSM.clone());
//  Out.println("==========================\n" +
//                     "New Accepting FSM:\n" + currentFSM +
//                     "\n==========================");
        }

        //this will (should) be optimised
        State fsmPosition = currentFSM.getFSMPosition();
        java.util.Set transitions = fsmPosition.getTransitions();
        java.util.Iterator transIter = transitions.iterator();
        while(transIter.hasNext()){
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
                constraintsByType.put(annType, Transients.newFeatureMap());
              }
            }else{
              //newAttributes != null
              if(oldAttributes != null) newAttributes.putAll(oldAttributes);
              constraintsByType.put(annType, newAttributes);
            }
          }//for(int i=0; i < currentConstraints.length; i++)
          //try to match all the constraints
          boolean success = true;
          java.util.Iterator typesIter = constraintsByType.keySet().iterator();
          AnnotationSet matchedHere;
          Long offset;
          while(success && typesIter.hasNext()){
            //do a query for each annotation type
            annType = (String)typesIter.next();
            newAttributes = (FeatureMap)constraintsByType.get(annType);
            offset = currentFSM.getAGPosition().getOffset();
            matchedHere = annotations.get(annType,
                                          newAttributes,
                                          offset);
            if(matchedHere == null || matchedHere.isEmpty()) success = false;
            else{
              //we have some matched annotations of the current type
              //let's add them to the list of matched annotations
              matchedAnnots.addAll(matchedHere);
            }
          }//while(success && typesIter.hasNext())
          if(success){
            //create a new FSMInstance, make it advance in AG and in FSM,
            //take care of its bindings data structure and
            //add it to the list of active FSMs.
            FSMInstance newFSMI = (FSMInstance)currentFSM.clone();
            newFSMI.setAGPosition(matchedAnnots.lastNode());
            newFSMI.setFSMPosition(currentTrans.getTarget());
            //do the bindings

            //all the annotations matched here will be added to the sets
            //corresponding to the labels in this list in case of succesful
            //matching
            java.util.Iterator labelsIter =
                                          currentTrans.getBindings().iterator();
            AnnotationSet oldSet, newSet;
            String label;
            java.util.Map binds = newFSMI.getBindings();
            while(labelsIter.hasNext()){
              //for each label add the set of matched annotations to the set of
              //annotations currently bound to that name
              label = (String)labelsIter.next();
              oldSet = (AnnotationSet)binds.get(label);
              if(oldSet != null){
                newSet = new AnnotationSetImpl(oldSet);
                newSet.addAll(matchedAnnots);
              }else{
                newSet = new AnnotationSetImpl(matchedAnnots);
              }
              binds.put(label, newSet);
            }//while(labelsIter.hasNext())
            activeFSMInstances.addLast(newFSMI);
          }
        }//while(transIter.hasNext())
       //return currentFSM to the rightful owner :)
//       FSMInstance.returnInstance(currentFSM);
       }//while(!activeFSMInstances.isEmpty())

       //FIRE THE RULE
      if(acceptingFSMInstances.isEmpty()){
        //advance to next relevant node in the Annotation Graph
        startNode = annotations.nextNode(startNode);
      }else if(ruleApplicationStyle == BRILL_STYLE){
        //fire the rules corresponding to all accepting FSM instances
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
      }else if(ruleApplicationStyle == APPELT_STYLE){
        //AcceptingFSMInstances is an ordered structure:
        //just execute the longest (last) rule.
        FSMInstance currentAcceptor =
                                    (FSMInstance)acceptingFSMInstances.last();
        RightHandSide currentRHS = currentAcceptor.getFSMPosition().getAction();
        currentRHS.transduce(doc, annotations, currentAcceptor.getBindings());
        //advance in AG
        startNode = currentAcceptor.getAGPosition();

      }else throw new RuntimeException("Unknown rule application style!");
      //release all the accepting instances as they have done their job
//      Iterator acceptors = acceptingFSMInstances.iterator();
//      while(acceptors.hasNext())
//        FSMInstance.returnInstance((FSMInstance)acceptors.next());
      acceptingFSMInstances.clear();
      startNodeOff = startNode.getOffset().intValue();
      if(startNodeOff - oldStartNodeOff > 1024){
        fireProgressChangedEvent(100 * startNodeOff / lastNodeOff);
        oldStartNodeOff = startNodeOff;
      }
    }//while(startNode != lastNode)
//    FSMInstance.clearInstances();
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
  public PrioritisedRuleList getRules(){
    return rules;
  }

  /**
  *Adds a new type of input annotations used by this transducer.
  *If the list of input types is empty this transducer will parse all the
  *annotations in the document otherwise the types not found in the input list
  *will be completely ignored! To be used with caution!
  */
  public void addInput(String ident){
    input.add(ident);
  }

  /**
  *Defines the types of input annotations that this transducer reads. If this
  *set is empty the transducer will read all the annotations otherwise it will
  *only "see" the annotations of types found in this list ignoring all other
  *types of annotations.
  */
  java.util.Set input = new java.util.HashSet();
  
} // class SinglePhaseTransducer



// $Log$
// Revision 1.17  2000/10/10 15:36:37  oana
// Changed System.out in Out and System.err in Err;
// Added the DEBUG variable seted on false;
// Added in the header the licence;
//
// Revision 1.16  2000/09/12 13:40:52  valyt
// Fixed a bug in Jape (the input specification didn't work properly)
//
// Revision 1.15  2000/09/10 18:30:26  valyt
// Added support for:
// 	rules priority
// 	input specification
// in Jape
//
// Revision 1.14  2000/07/19 20:37:37  valyt
// Changed the Files.getResourceAsStream() method in order to break the tests :)
//
// now it doesn't only load gate resources but the full path of the resource must be specified
//
// Revision 1.13  2000/07/12 11:40:19  valyt
// *** empty log message ***
//
// Revision 1.12  2000/07/04 14:37:39  valyt
// Added some support for Jape-ing in a different annotations et than the default one;
// Changed the L&F for the JapeGUI to the System default
//
// Revision 1.11  2000/07/03 21:00:59  valyt
// Added StatusBar and ProgressBar support for tokenisation & Jape transduction
// (it looks great :) )
//
// Revision 1.10  2000/06/26 14:45:50  valyt
// Fixed the haunting bug in Jape: it works OK now
// Reversed Jape to using the Java object management instead of our custom made object pooling:
// aparently it works faster if the VM deletes and creates new objects than when we try to re-use them. I imagine it is a 1.3 issue: the hotspot improves object creation/deletion times.
//
// Revision 1.9  2000/06/22 13:50:28  valyt
// Changed TestJdk to accommodate linux
//
// Revision 1.8  2000/05/24 10:22:23  valyt
// Added Jape GUI
//
// Revision 1.7  2000/05/17 19:56:14  valyt
// Killed some bugs in Jape.
// It looks like it's working
// (so I think it's dangerous to test it anymore :) )
//
// Revision 1.6  2000/05/17 17:08:49  valyt
// First working (?) version of jape.
//
// Revision 1.5  2000/05/12 14:14:16  valyt
// Done some work on jape....almost done :)
//
// Revision 1.4  2000/05/08 14:14:36  valyt
// Moved the ORACLE tests to derwent
//
// Revision 1.3  2000/05/05 12:51:12  valyt
// Got rid of deprecation warnings
//
// Revision 1.2  2000/04/14 18:02:46  valyt
// Added some gate.fsm classes
// added some accessor function in old jape classes
//
// Revision 1.1  2000/02/23 13:46:11  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:02  hamish
// added gate2
//
// Revision 1.16  1998/11/13 13:17:18  hamish
// merged in the doc length bug fix
//
// Revision 1.15  1998/11/12 17:47:28  kalina
// A bug fixed, wasn't restoring the document length
//
// Revision 1.14  1998/11/05 13:36:30  kalina
// moved to use array of JdmAttributes for selectNextAnnotation instead of a sequence
//
// Revision 1.13  1998/11/01 23:18:45  hamish
// use new instead of clear on containers
//
// Revision 1.12  1998/11/01 21:21:41  hamish
// use Java arrays in transduction where possible
//
// Revision 1.11  1998/10/30 15:31:08  kalina
// Made small changes to make compile under 1.2 and 1.1.x
//
// Revision 1.10  1998/10/29 12:13:06  hamish
// reorganised appelt transduction to be more efficient
// and accurate in the way it resets rules
//
// Revision 1.9  1998/10/01 16:06:39  hamish
// new appelt transduction style, replacing buggy version
//
// Revision 1.8  1998/09/26 09:19:20  hamish
// added cloning of PE macros
//
// Revision 1.7  1998/09/18 13:36:02  hamish
// made Transducer a class
//
// Revision 1.6  1998/09/17 10:24:04  hamish
// added options support, and Appelt-style rule application
//
// Revision 1.5  1998/08/19 20:21:44  hamish
// new RHS assignment expression stuff added
//
// Revision 1.4  1998/08/18 12:43:08  hamish
// fixed SPT bug, not advancing newPosition
//
// Revision 1.3  1998/08/12 15:39:44  hamish
// added padding toString methods
//
// Revision 1.2  1998/08/10 14:16:41  hamish
// fixed consumeblock bug and added batch.java
//
// Revision 1.1  1998/08/07 16:18:46  hamish
// parser pretty complete, with backend link done
//