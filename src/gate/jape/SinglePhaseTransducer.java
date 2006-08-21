/*
 *  SinglePhaseTransducer.java - transducer class
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
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

import java.util.*;

import gate.*;
import gate.annotation.AnnotationSetImpl;
import gate.creole.ExecutionException;
import gate.creole.ExecutionInterruptedException;
import gate.event.ProgressListener;
import gate.fsm.*;
import gate.util.*;

// by Shafirin Andrey start
import debugger.resources.pr.TraceContainer;
import debugger.resources.pr.RuleTrace;
import debugger.resources.SPTLock;
import debugger.resources.PhaseController;

// by Shafirin Andrey end

/**
 * Represents a complete CPSL grammar, with a phase name, options and
 * rule set (accessible by name and by sequence). Implements a transduce
 * method taking a Document as input. Constructs from String or File.
 */
public class SinglePhaseTransducer extends Transducer implements JapeConstants,
                                                     java.io.Serializable {
  /*
   * A structure to pass information to/from the fireRule() method.
   * Since Java won't let us return multiple values, we stuff them into
   * a 'state' object that fireRule() can update.
   */
  protected static class SearchState {
    Node startNode;

    long startNodeOff;

    long oldStartNodeOff;

    SearchState(Node startNode, long startNodeOff, long oldStartNodeOff) {
      this.startNode = startNode;
      this.startNodeOff = startNodeOff;
      this.oldStartNodeOff = oldStartNodeOff;
    }
  }

  /** Debug flag */
  private static final boolean DEBUG = false;

  // by Shafirin Andrey start
  PhaseController phaseController = null;

  TraceContainer rulesTrace = null;

  RuleTrace currRuleTrace = null;

  public PhaseController getPhaseController() {
    return phaseController;
  }

  public void setPhaseController(PhaseController phaseController) {
    this.phaseController = phaseController;
  }

  // by Shafirin Andrey end

  /** Construction from name. */
  public SinglePhaseTransducer(String name) {
    this.name = name;
    rules = new PrioritisedRuleList();
    finishedAlready = false;
  } // Construction from name

  /** Type of rule application (constants defined in JapeConstants). */
  protected int ruleApplicationStyle = BRILL_STYLE;

  /** Set the type of rule application (types defined in JapeConstants). */
  public void setRuleApplicationStyle(int style) {
    ruleApplicationStyle = style;
  }

  /**
   * The list of rules in this transducer. Ordered by priority and
   * addition sequence (which will be file position if they come from a
   * file).
   */
  protected PrioritisedRuleList rules;

  protected FSM fsm;

  public FSM getFSM() {
    return fsm;
  }

  /** Add a rule. */
  public void addRule(Rule rule) {
    rules.add(rule);
  } // addRule

  /** The values of any option settings given. */
  private java.util.HashMap optionSettings = new java.util.HashMap();

  /**
   * Add an option setting. If this option is set already, the new value
   * overwrites the previous one.
   */
  public void setOption(String name, String setting) {
    optionSettings.put(name, setting);
  } // setOption

  /** Get the value for a particular option. */
  public String getOption(String name) {
    return (String)optionSettings.get(name);
  } // getOption

  /** Whether the finish method has been called or not. */
  protected boolean finishedAlready;

  /**
   * Finish: replace dynamic data structures with Java arrays; called
   * after parsing.
   */
  public void finish() {
    // both MPT and SPT have finish called on them by the parser...
    if(finishedAlready) return;
    finishedAlready = true;

    // each rule has a RHS which has a string for java code
    // those strings need to be compiled now
    Map actionClasses = new HashMap(rules.size());
    for(Iterator i = rules.iterator(); i.hasNext();) {
      Rule rule = (Rule)i.next();
      rule.finish();
      actionClasses.put(rule.getRHS().getActionClassName(), rule.getRHS()
              .getActionClassString());
    }
    try {
      gate.util.Javac.loadClasses(actionClasses);
    }
    catch(Exception e) {
      throw new GateRuntimeException(e);
    }

    // build the finite state machine transition graph
    fsm = createFSM();
    // clear the old style data structures
    rules.clear();
    rules = null;
  } // finish

  protected FSM createFSM() {
    return new FSM(this);
  }

  // dam: was
  // private void addAnnotationsByOffset(Map map, SortedSet keys, Set
  // annotations){
  private void addAnnotationsByOffset(/* Map map, */SimpleSortedSet keys,
          Set annotations) {
    Iterator annIter = annotations.iterator();
    while(annIter.hasNext()) {
      Annotation ann = (Annotation)annIter.next();
      // ignore empty annotations
      long offset = ann.getStartNode().getOffset().longValue();
      if(offset == ann.getEndNode().getOffset().longValue()) continue;
      // dam: was
      /*
       * // Long offset = ann.getStartNode().getOffset();
       * 
       * List annsAtThisOffset = null; if(keys.add(offset)){
       * annsAtThisOffset = new LinkedList(); map.put(offset,
       * annsAtThisOffset); }else{ annsAtThisOffset =
       * (List)map.get(offset); } annsAtThisOffset.add(ann);
       */
      // dam: end
      keys.add(offset, ann);
    }
  }// private void addAnnotationsByOffset()

  /**
   * Transduce a document using the annotation set provided and the
   * current rule application style.
   */
  public void transduce(Document doc, AnnotationSet inputAS,
          AnnotationSet outputAS) throws JapeException, ExecutionException {
    interrupted = false;
    fireProgressChanged(0);

    // the input annotations will be read from this map
    // maps offset to list of annotations
    SimpleSortedSet offsets = new SimpleSortedSet();
    SimpleSortedSet annotationsByOffset = offsets;

    // select only the annotations of types specified in the input list
    if(input.isEmpty()) {
      addAnnotationsByOffset(offsets, inputAS);
    }
    else {
      Iterator typesIter = input.iterator();
      AnnotationSet ofOneType = null;
      while(typesIter.hasNext()) {
        ofOneType = inputAS.get((String)typesIter.next());
        if(ofOneType != null) {
          addAnnotationsByOffset(offsets, ofOneType);
        }
      }
    }

    if(annotationsByOffset.isEmpty()) {
      fireProcessFinished();
      return;
    }

    annotationsByOffset.sort();
    // define data structures
    // FSM instances that haven't blocked yet
    java.util.ArrayList activeFSMInstances = new java.util.ArrayList();

    // FSM instances that have reached a final state
    // This is a list and the contained objects are sorted by the length
    // of the document content covered by the matched annotations
    java.util.ArrayList acceptingFSMInstances = new ArrayList();
    FSMInstance currentFSM;

    // find the first node of the document
    Node startNode = ((Annotation)((ArrayList)annotationsByOffset.get(offsets
            .first())).get(0)).getStartNode();

    // used to calculate the percentage of processing done
    long lastNodeOff = doc.getContent().size().longValue();

    // the offset of the node where the matching currently starts
    // the value -1 marks no more annotations to parse
    long startNodeOff = startNode.getOffset().longValue();

    // The structure that fireRule() will update
    SearchState state = new SearchState(startNode, startNodeOff, 0);

    // by Shafirin Andrey start (according to Vladimir Karasev)
    if(gate.Gate.isEnableJapeDebug()) {
      // by Shafirin Andrey --> if (null != phaseController) {
      if(null != phaseController) {
        rulesTrace = new TraceContainer();
        rulesTrace.putPhaseCut(this, inputAS);
      }
    }
    // by Shafirin Andrey end

    // the big while for the actual parsing
    while(state.startNodeOff != -1) {
      // while there are more annotations to parse
      // create initial active FSM instance starting parsing from new
      // startNode
      // currentFSM = FSMInstance.getNewInstance(
      currentFSM = new FSMInstance(fsm, fsm.getInitialState(),// fresh
                                                              // start
              state.startNode,// the matching starts form the current
                              // startNode
              state.startNode,// current position in AG is the start
                              // position
              new java.util.HashMap(),// no bindings yet!
              doc);

      // at this point ActiveFSMInstances should always be empty!
      activeFSMInstances.clear();
      acceptingFSMInstances.clear();
      activeFSMInstances.add(currentFSM);

      // far each active FSM Instance, try to advance
      while(!activeFSMInstances.isEmpty()) {

        boolean isFinal = attemptAdvance(activeFSMInstances,
                acceptingFSMInstances, offsets, annotationsByOffset, doc);

        // if we're only looking for the shortest stop here
        if(isFinal && ruleApplicationStyle == FIRST_STYLE) break;
      }

      boolean keepGoing = fireRule(acceptingFSMInstances, state, lastNodeOff,
              offsets, inputAS, outputAS, doc, annotationsByOffset);
      if(!keepGoing) return;

    }// while(state.startNodeOff != -1)
    fireProcessFinished();
  } // transduce

  /**
   * Try to advance the activeFSMInstances.
   * 
   * @return true if we ended in a 'final' state, false otherwise.
   */

  protected boolean attemptAdvance(java.util.ArrayList activeFSMInstances,
          java.util.ArrayList acceptingFSMInstances, SimpleSortedSet offsets,
          SimpleSortedSet annotationsByOffset, Document doc)
          throws ExecutionInterruptedException {

    if(interrupted)
      throw new ExecutionInterruptedException("The execution of the \""
              + getName() + "\" Jape transducer has been abruptly interrupted!");

    // take the first active FSM instance
    FSMInstance currentFSM = (FSMInstance)activeFSMInstances.remove(0);

    // process the current FSM instance
    if(currentFSM.getFSMPosition().isFinal()) {
      // the current FSM is in a final state
      acceptingFSMInstances.add(currentFSM.clone());

      // if we're only looking for the shortest stop here
      if(ruleApplicationStyle == FIRST_STYLE) return true; // Ended in
                                                            // a final
                                                            // state
    }

    // get all the annotations that start where the current FSM finishes
    SimpleSortedSet offsetsTailSet = offsets.tailSet(currentFSM.getAGPosition()
            .getOffset().longValue());
    ArrayList paths;
    long theFirst = offsetsTailSet.first();
    if(theFirst < 0) return false;

    paths = (ArrayList)annotationsByOffset.get(theFirst);

    if(paths.isEmpty()) return false;

    Iterator pathsIter = paths.iterator();
    Annotation onePath;
    State currentState = currentFSM.getFSMPosition();
    Iterator transitionsIter;
    // foreach possible annotation
    while(pathsIter.hasNext()) {
      onePath = (Annotation)pathsIter.next();
      transitionsIter = currentState.getTransitions().iterator();
      Transition currentTransition;
      Constraint[] currentConstraints;
      transitionsWhile: while(transitionsIter.hasNext()) {
        currentTransition = (Transition)transitionsIter.next();
        // check if the current transition can use the current
        // annotation (path)
        currentConstraints = currentTransition.getConstraints()
                .getConstraints();

        // We initially only check the first constraint for a match. If
        // it
        // does match, we go on to check the others with the other
        // annotations
        // that exist at this point in the doc.
        if(!onePath.getType().equals(currentConstraints[0].getAnnotType())
                || !onePath.getFeatures().subsumes(ontology,
                        currentConstraints[0].getAttributeSeq())) {
          continue transitionsWhile;
        }

        Constraint oneConstraint = currentConstraints[0];

        // The first constraint matches. Now check the other
        // constraints,
        // which are of a different type.
        if(currentConstraints.length > 1) {
          /*
           * TODO - if there are multiple constraints matching, which
           * matched constraint do we bind to? For the moment, bind to
           * the longest one. To be completely correct, we should
           * actually fire off a new FSMInstance for each potential
           * bind.
           */

          int maxEnding = onePath.getEndNode().getOffset().intValue();

          otherConstraints: for(int i = 1; i < currentConstraints.length; i++) {
            Constraint c = currentConstraints[i];

            // Look for a path that matches c
            for(Iterator j = paths.iterator(); j.hasNext();) {
              Annotation a = (Annotation)j.next();
              if(a.getType().equals(c.getAnnotType())
                      && a.getFeatures()
                              .subsumes(ontology, c.getAttributeSeq())) {

                if(a.getEndNode().getOffset().intValue() > maxEnding) {
                  onePath = a;
                  oneConstraint = c;
                  maxEnding = a.getEndNode().getOffset().intValue();
                }
                continue otherConstraints;
              }
            }
            // No match found for c, go to the next transition
            continue transitionsWhile;
          }
        }

        // we have a match
        // create a new FSMInstance, advance it over the current
        // annotation
        // take care of the bindings and add it to ActiveFSM
        FSMInstance newFSMI = (FSMInstance)currentFSM.clone();
        newFSMI.setAGPosition(onePath.getEndNode());
        newFSMI.setFSMPosition(currentTransition.getTarget());

        // by Shafirin Andrey start (according to Vladimir Karasev)
        if(gate.Gate.isEnableJapeDebug()) {
          if(null != phaseController) {
            currRuleTrace = rulesTrace.getStateContainer(currentFSM
                    .getFSMPosition());
            if(currRuleTrace == null) {
              currRuleTrace = new RuleTrace(newFSMI.getFSMPosition(), doc);
              currRuleTrace.addAnnotation(onePath);
              currRuleTrace
                      .putPattern(onePath, oneConstraint.getAttributeSeq());
              rulesTrace.add(currRuleTrace);
            }
            else {
              currRuleTrace.addState(newFSMI.getFSMPosition());
              currRuleTrace.addAnnotation(onePath);
              currRuleTrace
                      .putPattern(onePath, oneConstraint.getAttributeSeq());
            }
          }
        }
        // by Shafirin Andrey end

        // bindings
        java.util.Map binds = newFSMI.getBindings();
        java.util.Iterator labelsIter = currentTransition.getBindings()
                .iterator();
        String oneLabel;
        AnnotationSet boundAnnots, newSet;
        while(labelsIter.hasNext()) {
          oneLabel = (String)labelsIter.next();
          boundAnnots = (AnnotationSet)binds.get(oneLabel);
          if(boundAnnots != null)
            newSet = new AnnotationSetImpl(boundAnnots);
          else newSet = new AnnotationSetImpl(doc);
          newSet.add(onePath);
          binds.put(oneLabel, newSet);

        }// while(labelsIter.hasNext())
        activeFSMInstances.add(newFSMI);
      }// while(transitionsIter.hasNext())
    }// while(pathsIter.hasNext())

    return false;
  } // attemptAdvance

  /**
   * Fire the rule that matched.
   * 
   * @return true if processing should keep going, false otherwise.
   */

  protected boolean fireRule(java.util.ArrayList acceptingFSMInstances,
          SearchState state, long lastNodeOff, SimpleSortedSet offsets,
          AnnotationSet inputAS, AnnotationSet outputAS, Document doc,
          SimpleSortedSet annotationsByOffset) throws JapeException,
          ExecutionException {

    Node startNode = state.startNode;
    long startNodeOff = state.startNodeOff;
    long oldStartNodeOff = state.oldStartNodeOff;

    // FIRE THE RULE
    long lastAGPosition = -1;
    if(acceptingFSMInstances.isEmpty()) {
      // no rule to fire, advance to the next input offset
      lastAGPosition = startNodeOff + 1;
    }
    else if(ruleApplicationStyle == BRILL_STYLE
            || ruleApplicationStyle == ALL_STYLE) {
      // fire the rules corresponding to all accepting FSM instances
      java.util.Iterator accFSMIter = acceptingFSMInstances.iterator();
      FSMInstance currentAcceptor;
      RightHandSide currentRHS;
      lastAGPosition = startNode.getOffset().longValue();

      while(accFSMIter.hasNext()) {
        currentAcceptor = (FSMInstance)accFSMIter.next();
        currentRHS = currentAcceptor.getFSMPosition().getAction();

        // by Shafirin Andrey start
        // debugger callback
        if(gate.Gate.isEnableJapeDebug()) {
          if(null != phaseController) {
            SPTLock lock = new SPTLock();
            phaseController.TraceTransit(rulesTrace);
            rulesTrace = new TraceContainer();
            phaseController.RuleMatched(lock, this, currentRHS, doc,
                    currentAcceptor.getBindings(), inputAS, outputAS);
          }
        }
        // by Shafirin Andrey end

        currentRHS.transduce(doc, currentAcceptor.getBindings(), inputAS,
                outputAS, ontology);

        // by Shafirin Andrey start
        // debugger callback
        if(gate.Gate.isEnableJapeDebug()) {
          if(null != phaseController) {
            SPTLock lock = new SPTLock();
            phaseController.RuleFinished(lock, this, currentRHS, doc,
                    currentAcceptor.getBindings(), inputAS, outputAS);
          }
        }
        // by Shafirin Andrey end
        if(ruleApplicationStyle == BRILL_STYLE) {
          // find the maximal next position
          long currentAGPosition = currentAcceptor.getAGPosition().getOffset()
                  .longValue();
          if(currentAGPosition > lastAGPosition)
            lastAGPosition = currentAGPosition;
        }
      }
      if(ruleApplicationStyle == ALL_STYLE) {
        // simply advance to next offset
        lastAGPosition = lastAGPosition + 1;
      }

    }
    else if(ruleApplicationStyle == APPELT_STYLE
            || ruleApplicationStyle == FIRST_STYLE
            || ruleApplicationStyle == ONCE_STYLE) {

      // AcceptingFSMInstances is an ordered structure:
      // just execute the longest (last) rule
      Collections.sort(acceptingFSMInstances, Collections.reverseOrder());
      Iterator accFSMIter = acceptingFSMInstances.iterator();
      FSMInstance currentAcceptor = (FSMInstance)accFSMIter.next();
      if(isDebugMode()) {
        // see if we have any conflicts
        Iterator accIter = acceptingFSMInstances.iterator();
        FSMInstance anAcceptor;
        List conflicts = new ArrayList();
        while(accIter.hasNext()) {
          anAcceptor = (FSMInstance)accIter.next();
          if(anAcceptor.equals(currentAcceptor)) {
            conflicts.add(anAcceptor);
          }
          else {
            break;
          }
        }
        if(conflicts.size() > 1) {
          Out.prln("\nConflicts found during matching:"
                  + "\n================================");
          accIter = conflicts.iterator();
          int i = 0;
          while(accIter.hasNext()) {
            Out.prln(i++ + ") " + accIter.next().toString());
          }
        }
      }
      RightHandSide currentRHS = currentAcceptor.getFSMPosition().getAction();

      // by Shafirin Andrey start
      // debugger callback
      if(gate.Gate.isEnableJapeDebug()) {
        if(null != phaseController) {
          SPTLock lock = new SPTLock();
          rulesTrace.leaveLast(currentRHS);
          phaseController.TraceTransit(rulesTrace);
          rulesTrace = new TraceContainer();
          phaseController.RuleMatched(lock, this, currentRHS, doc,
                  currentAcceptor.getBindings(), inputAS, outputAS);
        }
      }
      // by Shafirin Andrey end

      currentRHS.transduce(doc, currentAcceptor.getBindings(), inputAS,
              outputAS, ontology);

      // by Shafirin Andrey start
      // debugger callback
      if(gate.Gate.isEnableJapeDebug()) {
        if(null != phaseController) {
          SPTLock lock = new SPTLock();
          phaseController.RuleFinished(lock, this, currentRHS, doc,
                  currentAcceptor.getBindings(), inputAS, outputAS);
        }
      }
      // by Shafirin Andrey end

      // if in matchGroup mode check other possible patterns in this
      // span
      if(isMatchGroupMode()) {
        // Out.prln("Jape grammar in MULTI application style.");
        // ~bp: check for other matching fsm instances with same length,
        // priority and rule index : if such execute them also.
        String currentAcceptorString = null;
        multiModeWhile: while(accFSMIter.hasNext()) {
          FSMInstance rivalAcceptor = (FSMInstance)accFSMIter.next();
          // get rivals that match the same document segment
          // makes use of the semantic difference between the compareTo
          // and
          // equals methods on FSMInstance
          if(rivalAcceptor.compareTo(currentAcceptor) == 0) {
            // gets the rivals that are NOT COMPLETELY IDENTICAL with
            // the
            // current acceptor.
            if(!rivalAcceptor.equals(currentAcceptor)) {
              if(isDebugMode()) { /*
                                   * depends on the debug option in the
                                   * transducer
                                   */
                if(currentAcceptorString == null) {
                  // first rival
                  currentAcceptorString = currentAcceptor.toString();
                  Out
                          .prln("~Jape Grammar Transducer : "
                                  + "\nConcurrent Patterns by length,priority and index (all transduced):");
                  Out.prln(currentAcceptorString);
                  Out.prln("bindings : " + currentAcceptor.getBindings());
                  Out.prln("Rivals Follow: ");
                }
                Out.prln(rivalAcceptor);
                Out.prln("bindings : " + rivalAcceptor.getBindings());
              }// DEBUG
              currentRHS = rivalAcceptor.getFSMPosition().getAction();

              // by Shafirin Andrey start
              // debugger callback
              if(gate.Gate.isEnableJapeDebug()) {
                if(null != phaseController) {
                  SPTLock lock = new SPTLock();
                  rulesTrace.leaveLast(currentRHS);
                  phaseController.TraceTransit(rulesTrace);
                  rulesTrace = new TraceContainer();
                  phaseController.RuleMatched(lock, this, currentRHS, doc,
                          rivalAcceptor.getBindings(), inputAS, outputAS);
                }
              }
              // by Shafirin Andrey end

              currentRHS.transduce(doc, rivalAcceptor.getBindings(), inputAS,
                      outputAS, ontology);

              // by Shafirin Andrey start
              // debugger callback
              if(gate.Gate.isEnableJapeDebug()) {
                if(null != phaseController) {
                  SPTLock lock = new SPTLock();
                  phaseController.RuleFinished(lock, this, currentRHS, doc,
                          rivalAcceptor.getBindings(), inputAS, outputAS);
                }
              }
              // by Shafirin Andrey end
            } // equal rival
          }
          else {
            // if rival is not equal this means that there are no
            // further
            // equal rivals (since the list is sorted)
            break multiModeWhile;
          }
        } // while there are fsm instances
      } // matchGroupMode

      // if in ONCE mode stop after first match
      if(ruleApplicationStyle == ONCE_STYLE) {
        state.startNodeOff = startNodeOff;
        return false;
      }

      // advance in AG
      lastAGPosition = currentAcceptor.getAGPosition().getOffset().longValue();
    }
    else throw new RuntimeException("Unknown rule application style!");

    // advance on input
    SimpleSortedSet OffsetsTailSet = offsets.tailSet(lastAGPosition);
    long theFirst = OffsetsTailSet.first();
    if(theFirst < 0) {
      // no more input, phew! :)
      startNodeOff = -1;
      fireProcessFinished();
    }
    else {
      long nextKey = theFirst;
      startNode = ((Annotation)((ArrayList)annotationsByOffset.get(nextKey))
              .get(0)). // nextKey
              getStartNode();
      startNodeOff = startNode.getOffset().longValue();

      // eliminate the possibility for infinite looping
      if(oldStartNodeOff == startNodeOff) {
        // we are about to step twice in the same place, ...skip ahead
        lastAGPosition = startNodeOff + 1;
        OffsetsTailSet = offsets.tailSet(lastAGPosition);
        theFirst = OffsetsTailSet.first();
        if(theFirst < 0) {
          // no more input, phew! :)
          startNodeOff = -1;
          fireProcessFinished();
        }
        else {
          nextKey = theFirst;
          startNode = ((Annotation)((List)annotationsByOffset.get(theFirst))
                  .get(0)).getStartNode();
          startNodeOff = startNode.getOffset().longValue();
        }
      }// if(oldStartNodeOff == startNodeOff)
      // fire the progress event
      if(startNodeOff - oldStartNodeOff > 256) {
        if(isInterrupted())
          throw new ExecutionInterruptedException("The execution of the \""
                  + getName()
                  + "\" Jape transducer has been abruptly interrupted!");

        fireProgressChanged((int)(100 * startNodeOff / lastNodeOff));
        oldStartNodeOff = startNodeOff;
      }
    }
    // by Shafirin Andrey start (according to Vladimir Karasev)
    // if(gate.Gate.isEnableJapeDebug()) {
    // if (null != phaseController) {
    // phaseController.TraceTransit(rulesTrace);
    // }
    // }
    // by Shafirin Andrey end

    state.oldStartNodeOff = oldStartNodeOff;
    state.startNodeOff = startNodeOff;
    state.startNode = startNode;
    return true;
  } // fireRule

  /** Clean up (delete action class files, for e.g.). */
  public void cleanUp() {
    // for(DListIterator i = rules.begin(); ! i.atEnd(); i.advance())
    // ((Rule) i.get()).cleanUp();
  } // cleanUp

  /** A string representation of this object. */
  public String toString() {
    return toString("");
  } // toString()

  /** A string representation of this object. */
  public String toString(String pad) {
    String newline = Strings.getNl();
    String newPad = Strings.addPadding(pad, INDENT_PADDING);

    StringBuffer buf = new StringBuffer(pad + "SPT: name(" + name
            + "); ruleApplicationStyle(");

    switch(ruleApplicationStyle) {
      case APPELT_STYLE:
        buf.append("APPELT_STYLE); ");
        break;
      case BRILL_STYLE:
        buf.append("BRILL_STYLE); ");
        break;
      default:
        break;
    }

    buf.append("rules(" + newline);
    Iterator rulesIterator = rules.iterator();
    while(rulesIterator.hasNext())
      buf.append(((Rule)rulesIterator.next()).toString(newPad) + " ");

    buf.append(newline + pad + ")." + newline);

    return buf.toString();
  } // toString(pad)

  // needed by fsm
  public PrioritisedRuleList getRules() {
    return rules;
  }

  /**
   * Adds a new type of input annotations used by this transducer. If
   * the list of input types is empty this transducer will parse all the
   * annotations in the document otherwise the types not found in the
   * input list will be completely ignored! To be used with caution!
   */
  public void addInput(String ident) {
    input.add(ident);
  }

  public synchronized void removeProgressListener(ProgressListener l) {
    if(progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector)progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }

  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null
            ? new Vector(2)
            : (Vector)progressListeners.clone();
    if(!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }

  /**
   * Defines the types of input annotations that this transducer reads.
   * If this set is empty the transducer will read all the annotations
   * otherwise it will only "see" the annotations of types found in this
   * list ignoring all other types of annotations.
   */
  // by Shafirin Andrey start (modifier changed to public)
  public java.util.Set input = new java.util.HashSet();

  // java.util.Set input = new java.util.HashSet();
  // by Shafirin Andrey end
  private transient Vector progressListeners;

  protected void fireProgressChanged(int e) {
    if(progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for(int i = 0; i < count; i++) {
        ((ProgressListener)listeners.elementAt(i)).progressChanged(e);
      }
    }
  }

  protected void fireProcessFinished() {
    if(progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for(int i = 0; i < count; i++) {
        ((ProgressListener)listeners.elementAt(i)).processFinished();
      }
    }
  }

  public int getRuleApplicationStyle() {
    return ruleApplicationStyle;
  }

  /*
   * private void writeObject(ObjectOutputStream oos) throws IOException {
   * Out.prln("writing spt"); oos.defaultWriteObject();
   * Out.prln("finished writing spt"); } // writeObject
   */

} // class SinglePhaseTransducer

/*
 * class SimpleSortedSet {
 * 
 * static final int INCREMENT = 1023; int[] theArray = new
 * int[INCREMENT]; Object[] theObject = new Object[INCREMENT]; int
 * tsindex = 0; int size = 0; public static int avesize = 0; public
 * static int maxsize = 0; public static int avecount = 0; public
 * SimpleSortedSet() { avecount++; java.util.Arrays.fill(theArray,
 * Integer.MAX_VALUE); }
 * 
 * public Object get(int elValue) { int index =
 * java.util.Arrays.binarySearch(theArray, elValue); if (index >=0)
 * return theObject[index]; return null; }
 * 
 * public boolean add(int elValue, Object o) { int index =
 * java.util.Arrays.binarySearch(theArray, elValue); if (index >=0) {
 * ((ArrayList)theObject[index]).add(o); return false; } if (size ==
 * theArray.length) { int[] temp = new int[theArray.length + INCREMENT];
 * Object[] tempO = new Object[theArray.length + INCREMENT];
 * System.arraycopy(theArray, 0, temp, 0, theArray.length);
 * System.arraycopy(theObject, 0, tempO, 0, theArray.length);
 * java.util.Arrays.fill(temp, theArray.length, temp.length ,
 * Integer.MAX_VALUE); theArray = temp; theObject = tempO; } index =
 * ~index; System.arraycopy(theArray, index, theArray, index+1, size -
 * index ); System.arraycopy(theObject, index, theObject, index+1, size -
 * index ); theArray[index] = elValue; theObject[index] = new
 * ArrayList(); ((ArrayList)theObject[index]).add(o); size++; return
 * true; } public int first() { if (tsindex >= size) return -1; return
 * theArray[tsindex]; }
 * 
 * public Object getFirst() { if (tsindex >= size) return null; return
 * theObject[tsindex]; }
 * 
 * public SimpleSortedSet tailSet(int elValue) { if (tsindex <
 * theArray.length && elValue != theArray[tsindex]) { if (tsindex<(size-1) &&
 * elValue > theArray[tsindex] && elValue <= theArray[tsindex+1]) {
 * tsindex++; return this; } int index =
 * java.util.Arrays.binarySearch(theArray, elValue); if (index < 0)
 * index = ~index; tsindex = index; } return this; }
 * 
 * public boolean isEmpty() { return size ==0; } };
 */
