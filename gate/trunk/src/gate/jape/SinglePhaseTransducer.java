/*
	SinglePhaseTransducer.java - transducer class

	Hamish Cunningham, 24/07/98

	$Id$
*/


package gate.jape;

import java.util.Enumeration;
import com.objectspace.jgl.*;

import gate.annotation.*;
import gate.util.*;
import gate.*;
import gate.fsm.*;

/**
  * Represents a complete CPSL grammar, with a phase name, options and 
  * rule set (accessible by name and by sequence).
  * Implements a transduce method taking a Document as input.
  * Constructs from String or File.
  */
public class SinglePhaseTransducer
extends Transducer implements JapeConstants, java.io.Serializable
{
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
  * Transduce a document using the default annotation set and the current
  * rule application style.
  */
  public void transduce(Document doc) throws JapeException {
   //INITIALISATION

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
    gate.Node startNode = doc.getAnnotations().firstNode();
    //The last node: where the parsing will stop
    gate.Node lastNode = doc.getAnnotations().lastNode();

    //the big while for the actual parsing
    while(startNode != lastNode){
      //while there are more annotations to parse
      //create initial active FSM instance starting parsing from new startNode
      currentFSM = new FSMInstance(
                  fsm,
                  fsm.getInitialState(),//fresh start
                  startNode,//the matching starts form the current startNode
                  startNode,//current position in AG is the start position
                  new java.util.HashMap()//no bindings yet!
                  );
      //at this point ActiveFSMInstances should be always empty!
      activeFSMInstances.addLast(currentFSM);
        while(!activeFSMInstances.isEmpty()){
        //while there are some "alive" FSM instances
        //take the first active FSM instance
        currentFSM = (FSMInstance)activeFSMInstances.removeFirst();
        //process the current FSM instance
        if(currentFSM.getFSMPosition().isFinal()){
          //if the current FSM is in a final state
          acceptingFSMInstances.add(currentFSM.clone());
//  System.out.println("==========================\n" +
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
            matchedHere = doc.getAnnotations().get(annType,
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
  //System.out.println("No of active FSMs: " + activeFSMInstances.size());
       }//while(!activeFSMInstances.isEmpty())

       //FIRE THE RULE
      if(acceptingFSMInstances.isEmpty()){
        //advance to next relevant node in the Annotation Graph
        startNode = doc.getAnnotations().nextNode(startNode);
      }else if(ruleApplicationStyle == BRILL_STYLE){
        //fire the rules corresponding to all accepting FSM instances
        java.util.Iterator accFSMs = acceptingFSMInstances.iterator();
        FSMInstance currentAcceptor;
        RightHandSide currentRHS;
        long lastAGPosition = startNode.getOffset().longValue();
//  System.out.println("XXXXXXXXXXXXXXXXXXXX All the accepting FSMs are:");
        while(accFSMs.hasNext()){
          currentAcceptor = (FSMInstance) accFSMs.next();
//  System.out.println("==========================\n" +
//                     currentAcceptor +
//                     "\n==========================");

          currentRHS = currentAcceptor.getFSMPosition().getAction();
          currentRHS.transduce(doc,currentAcceptor.getBindings());

          long currentAGPosition =
               currentAcceptor.getAGPosition().getOffset().longValue();
          if(lastAGPosition <= currentAGPosition){
            startNode = currentAcceptor.getAGPosition();
            lastAGPosition = currentAGPosition;
          }
        }
//  System.out.println("XXXXXXXXXXXXXXXXXXXX");
        acceptingFSMInstances.clear();
      }else if(ruleApplicationStyle == APPELT_STYLE){
        //AcceptingFSMInstances is an ordered structure:
        //just execute the first rule.
        FSMInstance currentAcceptor =
                                    (FSMInstance)acceptingFSMInstances.first();
        RightHandSide currentRHS = currentAcceptor.getFSMPosition().getAction();
        currentRHS.transduce(doc,currentAcceptor.getBindings());
        //advance in AG
        startNode = currentAcceptor.getAGPosition();
      }else throw new RuntimeException("Unknown rule application style!");
    }//while(startNode != lastNode)

  } // transduce


//###############end modified versions

  /** Transduce a document. Defers to other methods dependent on
    * the current rule application style.
    */
  public void transduce_(Document doc) throws JapeException {

    if(ruleApplicationStyle == BRILL_STYLE)
      transduceBrillStyle(doc);
    else if(ruleApplicationStyle == APPELT_STYLE)
      transduceAppeltStyle(doc);

  } // transduce

  /** Transduce a document. Rule application is Brill style (i.e. we
    * try to find all possible applications of all rules).
    */
  protected void transduceBrillStyle(Document doc) throws JapeException {
    int finalPosition = doc.getContent().size().intValue();

    Enumeration rulesIterator = rules.elements();
    while(rulesIterator.hasMoreElements()) {
      int position = 0;
      MutableInteger newPosition = new MutableInteger();
      newPosition.value = 0;
      Rule rule = (Rule) rulesIterator.nextElement();

      while(position <= finalPosition) {

	      if(rule.matches(doc, position, newPosition)) {
	        rule.transduce(doc);
        }
	      position = newPosition.value;

      } // while position not final

    } // while there are more rules

  } // transduceBrillStyle

  /** Transduce a document. Rule application is Appelt style (i.e. we
    * apply only a single rule in any position, based on length/priority).
    */
  protected void transduceAppeltStyle(Document doc) throws JapeException {

    PrioritisedRuleList candidates = new PrioritisedRuleList();// matched rules
    int pos = 0; // position in the document byte stream
    int end = doc.getContent().size().intValue(); // the end of the byte stream

    while(pos < end) {
      int smallestPending = Integer.MAX_VALUE; // next pending rule left offset

      // Set each rule to its next match and collect candidates for this pos.
      // Rules will be in one of five states during this loop:
      // 1. the last match failed, and there are no more annotations
      //    of required types in the document, so the rule is finished
      // 2. the rule is pending, but at a position that has been jumped
      //    over by another rule firing. reset and shift to state 3.
      // 3. not pending, not finished. this is the initial state; get
      //    the next match (shift to state 1, 4 or 5)
      // 4. pending at the current position: this is a candidate for firing
      // 5. pending at some future point (the smallest of these is recorded
      //    so we can advance position to there)
      // After this loop we have zero or more candidates for firing and
      // zero or more pending. The fired rule is reset by the firing process;
      // pending rules that are no longer valid after a firing are in state 2.
      // and get reset in this loop. So rule state is consistent for the
      // duration of this document; all rules get reset at the end of the
      // document.
      for(ForwardIterator i = rules.start(); ! i.atEnd(); i.advance()) {
        Rule rule = (Rule) i.get();
        //System.out.println("trying rule " + rule.getName());

        // 1. rule has no more matches in this document: ignore
        if(rule.finished())
          continue;

        int pendingAt = rule.pending(); // rule pending status or offset

        // 2. it was pending but got jumped by other rule firing: shift to 3.
        if(pendingAt != -1 && pendingAt < pos) {
          rule.reset(); // implies any candidate will be reset before next try,
          pendingAt = -1; // as best gets to transduce and others end up here
        }

        // 3. it isn't pending: get the next match (shift to state 1, 4 or 5)
        if(pendingAt == -1)
          pendingAt = rule.getNextMatch(doc, pos, end);

        // 4. it's a valid match at this position, so add to candidates
        if(pendingAt == pos)
          candidates.add(rule, rule.getEndPosition()-rule.getStartPosition());

        // 5. pending in the future; record pending point if it's the smallest
        else if(pendingAt != -1)
          smallestPending = Math.min(smallestPending, pendingAt);
      } // for each rule

      // fire the best rule
      if(candidates.size() > 0) {
        Rule bestRule = (Rule) candidates.at(0); // first candidate is best
        candidates = new PrioritisedRuleList();  // forget the other candidates
        pos = bestRule.getEndPosition(); // advance to end of this rule
        bestRule.transduce(doc); // do the transduction (resets rule)
        //System.out.println("applied rule " + bestRule.getName());
      }

      // no match, and none pending so give up
      else if(smallestPending == Integer.MAX_VALUE)
        break;

      // no rules matched here but some are pending: advance to the leftmost
      else
        pos = Math.max(smallestPending, pos + 1);

    }   // while pos < end

    // reset all rules; some may be finished, some pending
    for(ForwardIterator i = rules.start(); ! i.atEnd(); i.advance())
      ((Rule) i.get()).reset();



  /************* BUGGY: ***********
    int position = 0;
    int finalPosition = doc.getByteSequence().length();
    PrioritisedRuleList candidateRules = new PrioritisedRuleList();

    while(position <= finalPosition) {
      int leftmostFailurePosition = Integer.MAX_VALUE;

      Enumeration rulesIterator = rules.elements();
      while(rulesIterator.hasMoreElements()) {
        Rule rule = (Rule) rulesIterator.nextElement();
        MutableInteger newPosition = new MutableInteger();
        newPosition.value = 0;
        if(rule.matches(doc, position, newPosition)) {
          //Debug.pr(
          //  this, "matched rule: " + Debug.getNl() + rule.toString("  ")
          //);
          candidateRules.add(
            rule, newPosition.value - rule.getStartPosition()
          );

        }
        else {
          //Debug.pr("rule " + rule.getName() + " failed, newPos = " +
          //         newPosition.value + Debug.getNl());
          if(leftmostFailurePosition > newPosition.value)
            leftmostFailurePosition = newPosition.value;
        }
      } // while there are more rules

      if(candidateRules.size() > 0) {
        DListIterator i = candidateRules.begin();
        Rule bestRule = (Rule) i.get();
        //Debug.pr(
        //  this, "bestRule: " + bestRule.getName() + Debug.getNl()
        //);
        position = bestRule.getEndPosition();
        //Debug.pr("position = " + position);
        bestRule.transduce(doc);

        // reset the rules that weren't applied
        for(i.advance(); ! i.atEnd(); i.advance()) {
          //Debug.pr(
          //  this,
          //  "reseting rule: " + ((Rule) i.get()).getName() + Debug.getNl()
          //);
          ((Rule) i.get()).reset();
        }
      } else { // no rules matched
        position = leftmostFailurePosition;
        //Debug.pr(this,
        //         "no rules matched, position = " + position + Debug.getNl());
      }

      // clear the candidates list
      candidateRules.clear();
    } // while position <= final
******************/
  } // transduceAppeltStyle

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

} // class SinglePhaseTransducer



// $Log$
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
