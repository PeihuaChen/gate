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

import java.io.*;

import gate.annotation.*;
import gate.util.*;
import gate.*;
import gate.fsm.*;
import gate.gui.*;
import gate.creole.*;
import gate.event.*;
import java.util.*;

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

  public FSM getFSM(){
    return fsm;
  }

  /** Add a rule. */
  public void addRule(Rule rule) {
    rules.add(rule);
  } // addRule

  /** The values of any option settings given. */
  private java.util.HashMap optionSettings = new java.util.HashMap();

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
  public void finish(){
    // both MPT and SPT have finish called on them by the parser...
    if(finishedAlready) return;
    else finishedAlready = true;

    //each rule has a RHS which has a string for java code
    //those strings need to be compiled now
    Map actionClasses = new HashMap(rules.size());
    for(Iterator i = rules.iterator(); i.hasNext(); ){
      Rule rule = (Rule)i.next();
      rule.finish();
      actionClasses.put(rule.getRHS().getActionClassName(),
                        rule.getRHS().getActionClassString());
    }
    try{
      gate.util.Compiler.loadClasses(actionClasses);
    }catch(Exception e){
      Err.prln("Compile error:\n" + e.getMessage());
//e.printStackTrace();
    }

    //build the finite state machine transition graph
    fsm = new FSM(this);
    //clear the old style data structures
    rules.clear();
    rules = null;
  } // finish

//dam: was
//  private void addAnnotationsByOffset(Map map, SortedSet keys, Set annotations){
  private void addAnnotationsByOffset(/*Map map,*/ SimpleSortedSet keys, Set annotations){
    Iterator annIter = annotations.iterator();
    while(annIter.hasNext()){
      Annotation ann = (Annotation)annIter.next();
      //ignore empty annotations
      long offset = ann.getStartNode().getOffset().longValue();
      if(offset == ann.getEndNode().getOffset().longValue())
        continue;
//dam: was
/*
//      Long offset = ann.getStartNode().getOffset();

      List annsAtThisOffset = null;
      if(keys.add(offset)){
        annsAtThisOffset = new LinkedList();
        map.put(offset, annsAtThisOffset);
      }else{
        annsAtThisOffset = (List)map.get(offset);
      }
      annsAtThisOffset.add(ann);
*/
//dam: end
      keys.add(offset, ann);
    }
  }//private void addAnnotationsByOffset()


  /**
    * Transduce a document using the annotation set provided and the current
    * rule application style.
    */
  public void transduce(Document doc, AnnotationSet inputAS,
                        AnnotationSet outputAS) throws JapeException,
                                                       ExecutionException {
    interrupted = false;
    fireProgressChanged(0);

    //the input annotations will be read from this map
    //maps offset to list of annotations

//dam was
/*
    Map annotationsByOffset = new HashMap();

    SortedSet offsets = new TreeSet();
*/
//dam: now
    SimpleSortedSet offsets = new SimpleSortedSet();
    SimpleSortedSet annotationsByOffset = offsets;
//dam: end

    //select only the annotations of types specified in the input list
//    Out.println("Input:" + input);
    if(input.isEmpty())
    {
//dam: was
//        addAnnotationsByOffset(annotationsByOffset, offsets, inputAS);
//dam: now
        addAnnotationsByOffset(offsets, inputAS);
//dam: end
    } else {
      Iterator typesIter = input.iterator();
      AnnotationSet ofOneType = null;
      while(typesIter.hasNext()){
        ofOneType = inputAS.get((String)typesIter.next());
        if(ofOneType != null){
//dam: was
//        addAnnotationsByOffset(annotationsByOffset, offsets, ofOneType);
//dam: now
          addAnnotationsByOffset(offsets, ofOneType);
//dam: end
        }
      }
    }

    if(annotationsByOffset.isEmpty()){
      fireProcessFinished();
      return;
    }

    annotationsByOffset.sort();
    //define data structures
    //FSM instances that haven't blocked yet
//    java.util.LinkedList activeFSMInstances = new java.util.LinkedList();
    java.util.ArrayList activeFSMInstances = new java.util.ArrayList();

    // FSM instances that have reached a final state
    // This is a sorted set and the contained objects are sorted by the length
    // of the document content covered by the matched annotations
//dam: was ArrayList has faster add and remove methods then LinkedList
//    java.util.LinkedList acceptingFSMInstances = new LinkedList();
//dam: now
    java.util.ArrayList acceptingFSMInstances = new ArrayList();
//dam: end
    FSMInstance currentFSM;


    //find the first node of the document
    Node startNode = ((Annotation)
                      ((ArrayList)annotationsByOffset.
                             get(offsets.first())).get(0)).
                      getStartNode();

    //used to calculate the percentage of processing done
    long lastNodeOff = doc.getContent().size().longValue();

    //the offset of the node where the matching currently starts
    //the value -1 marks no more annotations to parse
    long startNodeOff = startNode.getOffset().longValue();

    //used to decide when to fire progress events
    long oldStartNodeOff = 0;

    //the big while for the actual parsing
    while(startNodeOff != -1){
//Out.prln();
//Out.pr("Start: " + startNodeOff);
      //while there are more annotations to parse
      //create initial active FSM instance starting parsing from new startNode
      //currentFSM = FSMInstance.getNewInstance(
      currentFSM = new FSMInstance(
                  fsm,
                  fsm.getInitialState(),//fresh start
                  startNode,//the matching starts form the current startNode
                  startNode,//current position in AG is the start position
                  new java.util.HashMap(),//no bindings yet!
                  doc
                  );

      // at this point ActiveFSMInstances should always be empty!
      activeFSMInstances.clear();
      acceptingFSMInstances.clear();
//dam: was used LinkedList
//      activeFSMInstances.addLast(currentFSM);
//dam: now used ArrayList
      activeFSMInstances.add(currentFSM);
//dam: end

      //far each active FSM Instance, try to advance
      whileloop2:
      while(!activeFSMInstances.isEmpty()){
        if(interrupted) throw new ExecutionInterruptedException(
          "The execution of the \"" + getName() +
          "\" Jape transducer has been abruptly interrupted!");

//Out.pr(" <" + acceptingFSMInstances.size() + "/" +
//              activeFSMInstances.size() +">");
        // take the first active FSM instance
        currentFSM = (FSMInstance)activeFSMInstances.remove(0);

        // process the current FSM instance
        if(currentFSM.getFSMPosition().isFinal()){
          //the current FSM is in a final state
//dam: was LinkedList
//          acceptingFSMInstances.addLast(currentFSM.clone());
//dam: now
          acceptingFSMInstances.add(currentFSM.clone());
//dam: end
//          //if we are in APPELT mode clear all the accepting instances
//          //apart from the longest one
//          if(ruleApplicationStyle == APPELT_STYLE &&
//             acceptingFSMInstances.size() > 1){
//            Object longestAcceptor = acceptingFSMInstances.last();
//            acceptingFSMInstances.clear();
//            acceptingFSMInstances.add(longestAcceptor);
//          }
          //if we're only looking for the shortest stop here
          if(ruleApplicationStyle == FIRST_STYLE) break whileloop2;
        }

        //get all the annotations that start where the current FSM finishes
//<<< DAM: was using SortedSet
//        SortedSet offsetsTailSet = offsets.tailSet(
//=== DAM: now
        SimpleSortedSet offsetsTailSet = offsets.tailSet(
//>>> DAM: end
                                    currentFSM.getAGPosition().getOffset().longValue());
        ArrayList paths; //was linkedList

//<<< DAM: SortedSet speedup
/*
        if(offsetsTailSet.isEmpty()){
          paths = new ArrayList();
        }else{
          paths = (List)annotationsByOffset.get(offsetsTailSet.first());
        }
*/
//=== DAM: now
        long theFirst = offsetsTailSet.first();
        if(theFirst <0)
          continue;

          paths = (ArrayList)annotationsByOffset.get(theFirst);
//        }
//System.out.println("Paths: " + paths + "\n^localInputIndex: " + localInputIndex);
//>>> DAM: end

//        if(!paths.isEmpty()){
        if(paths.isEmpty()) continue;
          Iterator pathsIter = paths.iterator();
          Annotation onePath;
          State currentState = currentFSM.getFSMPosition();
          Iterator transitionsIter;
//DAM: doit without intermediate FetureMap
//        FeatureMap features = null;//Factory.newFeatureMap();
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
//DAM: introduce index of the constaint to process
              int currentConstraintsindex = -1;
              //we assume that all annotations in a contraint are of the same type
              for(int i = 0; i<currentConstraints.length; i++){
                annType = currentConstraints[i].getAnnotType();
                //if wrong type try next transition
                if(!annType.equals(onePath.getType()))continue transitionsWhile;
//DAM: doit without intermediate FetureMap
//                features.clear();
//                features.putAll(currentConstraints[i].getAttributeSeq());
                currentConstraintsindex = i;
                break;
              }
// >>> was
//              if(onePath.getFeatures().entrySet().containsAll(features.entrySet())){
// >>> NASO, FeatArray optimization
              if(onePath.getFeatures().subsumes(
//dam: was
//                features
//dam: now
                currentConstraints[currentConstraintsindex].getAttributeSeq()
//dam: end
                )){
// >>> end NASO
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
                  if(boundAnnots != null)
                    newSet = new AnnotationSetImpl((AnnotationSet)boundAnnots);
                  else
                    newSet = new AnnotationSetImpl(doc);
                  newSet.add(onePath);
                  binds.put(oneLabel, newSet);

                }//while(labelsIter.hasNext())
                activeFSMInstances.add(newFSMI);
//Out.pr("^(" + newFSMI.getStartAGPosition().getOffset() +
//                               "->" + newFSMI.getAGPosition().getOffset() + ")");
              }//if match
            }//while(transitionsIter.hasNext())
          }//while(pathsIter.hasNext())
// dam: reverse the paths.isEmpty check
//        }//if(paths != null)
// dam
      }//while(!activeFSMInstances.isEmpty())


      //FIRE THE RULE
//dam: use long
//      Long lastAGPosition = null;
//dam: now
      long lastAGPosition = -1;
//dam: end
      if(acceptingFSMInstances.isEmpty()){
        //no rule to fire, advance to the next input offset
        lastAGPosition = startNodeOff + 1;
      } else if(ruleApplicationStyle == BRILL_STYLE) {
      //System.out.println("Brill acceptor");
        // fire the rules corresponding to all accepting FSM instances
        java.util.Iterator accFSMs = acceptingFSMInstances.iterator();
        FSMInstance currentAcceptor;
        RightHandSide currentRHS;
        lastAGPosition = startNode.getOffset().longValue();

        while(accFSMs.hasNext()){
          currentAcceptor = (FSMInstance) accFSMs.next();

          currentRHS = currentAcceptor.getFSMPosition().getAction();
          currentRHS.transduce(doc, currentAcceptor.getBindings(),
                               inputAS, outputAS, ontology);
//dam: use long
//          Long currentAGPosition = currentAcceptor.getAGPosition().getOffset();
//dam: now
          long currentAGPosition = currentAcceptor.getAGPosition().getOffset().longValue();
//dam: end
          if(currentAGPosition > lastAGPosition)
            lastAGPosition = currentAGPosition;
        }

      } else if(ruleApplicationStyle == APPELT_STYLE ||
                ruleApplicationStyle == FIRST_STYLE ||
                ruleApplicationStyle == ONCE_STYLE) {

//System.out.println("Appelt acceptor");
        // AcceptingFSMInstances is an ordered structure:
        // just execute the longest (last) rule

        Collections.sort(acceptingFSMInstances, Collections.reverseOrder());

        FSMInstance currentAcceptor =(FSMInstance)acceptingFSMInstances.get(0);
        if(isDebugMode()){
          //see if we have any conflicts
          Iterator accIter = acceptingFSMInstances.iterator();
          FSMInstance anAcceptor;
          List conflicts = new ArrayList();
          while(accIter.hasNext()){
            anAcceptor = (FSMInstance)accIter.next();
            if(anAcceptor.equals(currentAcceptor)){
              conflicts.add(anAcceptor);
            }else{
              break;
            }
          }
          if(conflicts.size() > 1){
            Out.prln("\nConflicts found during matching:" +
                     "\n================================");
            accIter = conflicts.iterator();
            int i = 0;
            while(accIter.hasNext()){
              Out.prln(i++ + ") " + accIter.next().toString());
            }
          }
        }

        RightHandSide currentRHS = currentAcceptor.getFSMPosition().getAction();
        currentRHS.transduce(doc, currentAcceptor.getBindings(),
                             inputAS, outputAS, ontology);

        //if in ONCE mode stop after first match
        if(ruleApplicationStyle == ONCE_STYLE) return;

        //advance in AG
        lastAGPosition = currentAcceptor.getAGPosition().getOffset().longValue();
      }
//      else if(ruleApplicationStyle == FIRST_STYLE) {
//        // AcceptingFSMInstances is an ordered structure:
//        // just execute the shortest (first) rule
//
//        FSMInstance currentAcceptor =(FSMInstance)acceptingFSMInstances.first();
//        RightHandSide currentRHS = currentAcceptor.getFSMPosition().getAction();
//        currentRHS.transduce(doc, outputAS, currentAcceptor.getBindings());
//        //advance in AG
//        long lastAGPosition = currentAcceptor.getAGPosition().
//                              getOffset().longValue();
//        //advance the index on input
//        while(inputIndex < annotations.size() &&
//              ((Annotation)annotations.get(inputIndex)).
//              getStartNode().getOffset().longValue() < lastAGPosition){
//          inputIndex++;
//        }
//      }
      else throw new RuntimeException("Unknown rule application style!");


      //advance on input
//      SortedSet OffsetsTailSet = offsets.tailSet(lastAGPosition);
      SimpleSortedSet OffsetsTailSet = offsets.tailSet(lastAGPosition);
//<<< DAM: isEmpty speedup
/*
      if(OffsetsTailSet.isEmpty()){
*/
//=== DAM: now
        long theFirst = OffsetsTailSet.first();
      if( theFirst < 0){
//>>> DAM: end
        //no more input, phew! :)
        startNodeOff = -1;
        fireProcessFinished();
      }else{
//<<< DAM: use long
/*
        Long nextKey = (Long)OffsetsTailSet.first();
*/
//=== DAM: now
        long nextKey = theFirst;
//>>> DAM: end
        startNode = ((Annotation)
                      ((ArrayList)annotationsByOffset.get(nextKey)).get(0)). //nextKey
                    getStartNode();
        startNodeOff = startNode.getOffset().longValue();

        //eliminate the possibility for infinite looping
        if(oldStartNodeOff == startNodeOff){
//Out.prln("");
//Out.pr("SKIP " + startNodeOff);
          //we are about to step twice in the same place, ...skip ahead
          lastAGPosition = startNodeOff + 1;
          OffsetsTailSet = offsets.tailSet(lastAGPosition);
//<<< DAM: isEmpty speedup
/*
          if(OffsetsTailSet.isEmpty()){
*/
//=== DAM: now
          theFirst = OffsetsTailSet.first();
          if(theFirst < 0){
//>>> DAM: end
            //no more input, phew! :)
            startNodeOff = -1;
            fireProcessFinished();
          }else{
//<<< DAM: use long
//            nextKey = (Long)OffsetsTailSet.first();
//=== DAM: now
            nextKey = theFirst;
//>>> DAM: end
            startNode = ((Annotation)
                          ((List)annotationsByOffset.get(theFirst)).get(0)).
                        getStartNode();
            startNodeOff =startNode.getOffset().longValue();
          }
//Out.prln(" ->" + startNodeOff);
        }//if(oldStartNodeOff == startNodeOff)


        //fire the progress event
        if(startNodeOff - oldStartNodeOff > 256){
          if(isInterrupted()) throw new ExecutionInterruptedException(
            "The execution of the \"" + getName() +
            "\" Jape transducer has been abruptly interrupted!");

          fireProgressChanged((int)(100 * startNodeOff / lastNodeOff));
          oldStartNodeOff = startNodeOff;
        }
      }
    }//while(startNodeOff != -1)
    fireProcessFinished();
  } // transduce


  /** Clean up (delete action class files, for e.g.). */
  public void cleanUp() {
//    for(DListIterator i = rules.begin(); ! i.atEnd(); i.advance())
//      ((Rule) i.get()).cleanUp();
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
    Iterator rulesIterator = rules.iterator();
    while(rulesIterator.hasNext())
      buf.append(((Rule) rulesIterator.next()).toString(newPad) + " ");

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
  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }
  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null ? new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }

  /**
    * Defines the types of input annotations that this transducer reads. If this
    * set is empty the transducer will read all the annotations otherwise it
    * will only "see" the annotations of types found in this list ignoring all
    * other types of annotations.
    */
  java.util.Set input = new java.util.HashSet();
  private transient Vector progressListeners;

  protected void fireProgressChanged(int e) {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).progressChanged(e);
      }
    }
  }
  protected void fireProcessFinished() {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).processFinished();
      }
    }
  }
  public int getRuleApplicationStyle() {
    return ruleApplicationStyle;
  }

  /*
  private void writeObject(ObjectOutputStream oos) throws IOException {
    Out.prln("writing spt");
    oos.defaultWriteObject();
    Out.prln("finished writing spt");
  } // writeObject
  */


} // class SinglePhaseTransducer

/*
class SimpleSortedSet {

    static final int INCREMENT = 1023;
    int[] theArray = new int[INCREMENT];
    Object[] theObject = new Object[INCREMENT];
    int tsindex = 0;
    int size = 0;
    public static int avesize = 0;
    public static int maxsize = 0;
    public static int avecount = 0;
    public SimpleSortedSet()
    {
        avecount++;
        java.util.Arrays.fill(theArray, Integer.MAX_VALUE);
    }

    public Object get(int elValue)
    {
        int index = java.util.Arrays.binarySearch(theArray, elValue);
        if (index >=0)
            return theObject[index];
        return null;
    }

    public boolean add(int elValue, Object o)
    {
        int index = java.util.Arrays.binarySearch(theArray, elValue);
        if (index >=0)
        {
            ((ArrayList)theObject[index]).add(o);
            return false;
        }
        if (size == theArray.length)
        {
            int[] temp = new int[theArray.length + INCREMENT];
            Object[] tempO = new Object[theArray.length + INCREMENT];
            System.arraycopy(theArray, 0, temp, 0, theArray.length);
            System.arraycopy(theObject, 0, tempO, 0, theArray.length);
            java.util.Arrays.fill(temp, theArray.length, temp.length , Integer.MAX_VALUE);
            theArray = temp;
            theObject = tempO;
        }
        index = ~index;
        System.arraycopy(theArray, index, theArray, index+1, size - index );
        System.arraycopy(theObject, index, theObject, index+1, size - index );
        theArray[index] = elValue;
        theObject[index] = new ArrayList();
        ((ArrayList)theObject[index]).add(o);
        size++;
        return true;
    }
    public int first()
    {
        if (tsindex >= size) return -1;
        return theArray[tsindex];
    }

    public Object getFirst()
    {
        if (tsindex >= size) return null;
        return theObject[tsindex];
    }

    public SimpleSortedSet tailSet(int elValue)
    {
        if (tsindex < theArray.length && elValue != theArray[tsindex])
        {
            if (tsindex<(size-1) && elValue > theArray[tsindex] &&
                elValue <= theArray[tsindex+1])
                {
                    tsindex++;
                   return this;
                }
            int index = java.util.Arrays.binarySearch(theArray, elValue);
            if (index < 0)
                index = ~index;
            tsindex = index;
        }
        return this;
    }

    public boolean isEmpty()
    {
        return size ==0;
    }
};
*/