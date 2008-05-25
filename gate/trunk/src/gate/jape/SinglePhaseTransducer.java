/*
 *  SinglePhaseTransducer.java - transducer class
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
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

import org.apache.log4j.Logger;

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
  protected static final Logger log = Logger
          .getLogger(SinglePhaseTransducer.class);

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
  private Map<String, String> optionSettings = new HashMap<String, String>();

  /**
   * Add an option setting. If this option is set already, the new value
   * overwrites the previous one.
   */
  public void setOption(String name, String setting) {
    optionSettings.put(name, setting);
  } // setOption

  /** Get the value for a particular option. */
  public String getOption(String name) {
    return optionSettings.get(name);
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
    log.debug("Start: " + name);
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
  @SuppressWarnings("unchecked")
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
      if(ruleApplicationStyle == FIRST_STYLE) return true;
    }

    // get all the annotations that start where the current FSM finishes
    SimpleSortedSet offsetsTailSet = offsets.tailSet(currentFSM.getAGPosition()
            .getOffset().longValue());
    List<Annotation> paths;
    long theFirst = offsetsTailSet.first();
    if(theFirst < 0) return false;

    paths = (List)annotationsByOffset.get(theFirst);

    if(paths.isEmpty()) return false;

    // get the transitions for the current state of the FSM
    State currentState = currentFSM.getFSMPosition();
    Iterator transitionsIter = currentState.getTransitions().iterator();

    // for each transition, keep the set of annotations starting at
    // current node (the "paths") that match each constraint of the
    // transition.
    transitionsWhile: while(transitionsIter.hasNext()) {
      Transition currentTransition = (Transition)transitionsIter.next();

      // There will only be multiple constraints if this transition is over
      // a written constraint that has the "and" operator (comma) and the
      // parts referr to different annotation types.  For example -
      // {A,B} would result in 2 constraints in the array, while
      // {A.foo=="val", A.bar=="val"} would only be a single constraint.
      Constraint[] currentConstraints = currentTransition.getConstraints()
              .getConstraints();

      boolean hasPositiveConstraint = false;
      Map<Constraint, Collection<Annotation>> matchingMap =
        new LinkedHashMap<Constraint, Collection<Annotation>>();

      // check all negated constraints first.  If any annotation matches any
      // negated constraint, then the transition fails.
      for(Constraint c : currentConstraints) {
        if (!c.isNegated()) {
          hasPositiveConstraint = true;
          continue;
        }
        List<Annotation> matchList = c.matches(paths, ontology, doc);
        if (!matchList.isEmpty())
            continue transitionsWhile;
      }

      // Now check all non-negated constraints.  At least one annotation must
      // match each constraint.
      if (hasPositiveConstraint){
        for(Constraint c : currentConstraints) {
          if (c.isNegated()) continue;
          List<Annotation> matchList = c.matches(paths, ontology, doc);
          //if no annotations matched, then the transition fails.
          if (matchList.isEmpty())
            continue transitionsWhile;
          else
            matchingMap.put(c, matchList);
        }
      } //end if hasPositiveConstraint
      else {
        // There are no non-negated constraints.  Since the negated constraints
        // did not fail, this means that all of the current annotations
        // are potentially valid.  Add the whole set to the matchingMap.
        // Use the first negated constraint for the debug trace since any will do.
        matchingMap.put(currentConstraints[0], paths);
      }

      // We have a match if every positive constraint is met by at least one annot.
      // Given the sets Sx of the annotations that match constraint x,
      // compute all tuples (A1, A2, ..., An) where Ax comes from the
      // set Sx and n is the number of constraints
      List<List<Annotation>> matchLists = new ArrayList<List<Annotation>>();
      for(Map.Entry<Constraint,Collection<Annotation>> entry : matchingMap.entrySet()) {
        //seeing the constaint is useful when debugging
        @SuppressWarnings("unused")
        Constraint c = entry.getKey();
        Collection<Annotation> matchList = entry.getValue();
        if (matchList instanceof List)
          matchLists.add((List<Annotation>)matchList);
        else
          matchLists.add(new ArrayList<Annotation>(matchList));
      }
      List<List<Annotation>> combinations = combine(matchLists, matchLists.size(), new LinkedList());

      // Create a new FSM for every tuple of annot
      for(List<Annotation> tuple : combinations) {

        // Find longest annotation and use that to mark the start of the
        // new FSM
        Annotation matchingAnnot = getRightMostAnnotation(tuple);

        // figure out which constaint this came from so can include it in
        // debug trace
        Constraint matchingConstraint = null;
        for(Map.Entry<Constraint,Collection<Annotation>> entry : matchingMap.entrySet()) {
          if (entry.getValue().contains(matchingAnnot)) {
            matchingConstraint = entry.getKey();
            break;
          }
        }

        // we have a match. create a new FSMInstance, advance it over the current
        // annotation take care of the bindings and add it to ActiveFSM
        FSMInstance newFSMI = (FSMInstance)currentFSM.clone();
        newFSMI.setAGPosition(matchingAnnot.getEndNode());
        newFSMI.setFSMPosition(currentTransition.getTarget());

        // by Shafirin Andrey start (according to Vladimir Karasev)
        if(gate.Gate.isEnableJapeDebug()) {
          if(null != phaseController) {
            currRuleTrace = rulesTrace.getStateContainer(currentFSM
                    .getFSMPosition());
            if(currRuleTrace == null) {
              currRuleTrace = new RuleTrace(newFSMI.getFSMPosition(), doc);
              currRuleTrace.addAnnotation(matchingAnnot);
              currRuleTrace.putPattern(matchingAnnot, matchingConstraint);
              rulesTrace.add(currRuleTrace);
            }
            else {
              currRuleTrace.addState(newFSMI.getFSMPosition());
              currRuleTrace.addAnnotation(matchingAnnot);
              currRuleTrace.putPattern(matchingAnnot, matchingConstraint);
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

          for(Annotation annot : tuple) {
            newSet.add(annot);
          }

          binds.put(oneLabel, newSet);
        }// while(labelsIter.hasNext())
        activeFSMInstances.add(newFSMI);
      } // iter over matching combinations

    }// while(transitionsIter.hasNext())
    return false;
  }

  /**
   * Return the annotation with the right-most end node
   * @param annots
   * @return
   */
  protected Annotation getRightMostAnnotation(Collection<Annotation> annots) {
    long maxOffset = -1;
    Annotation retVal = null;
    for(Annotation annot : annots) {
      Long curOffset = annot.getEndNode().getOffset();
      if(curOffset > maxOffset) {
        maxOffset = curOffset;
        retVal = annot;
      }
    }

    return retVal;
  }

  /**
   * Computes all tuples (x1, x2, ..., xn) resulting from the linear
   * combination of the elements of n lists, where x1 comes from the 1st
   * list, x2 comes from the second, etc. This method works recursively.
   * The first call should have those parameters:
   *
   * @param sourceLists an array of n lists whose elements will be
   *          combined
   * @param maxTupleSize the number of elements per tuple
   * @param incompleteTuple an empty list
   */

  private static List<List<Annotation>> combine(List<List<Annotation>> sourceLists,
          int maxTupleSize, List<Annotation> incompleteTuple) {

    List<List<Annotation>> newTupleList = new LinkedList<List<Annotation>>();

    if(incompleteTuple.size() == maxTupleSize) {
      newTupleList.add(incompleteTuple);
    }
    else {
      List<Annotation> currentSourceList = sourceLists.get(incompleteTuple.size());
      // use for loop instead of ListIterator to increase speed
      // (critical here)
      for(int i = 0; i < currentSourceList.size(); i++) {
        List<Annotation> augmentedTuple = (List<Annotation>)((LinkedList<Annotation>)incompleteTuple).clone();
        augmentedTuple.add(currentSourceList.get(i));
        newTupleList.addAll(combine(sourceLists, maxTupleSize, augmentedTuple));
      }
    }

    return newTupleList;
  }

  /**
   * Fire the rule that matched.
   *
   * @return true if processing should keep going, false otherwise.
   */

  protected boolean fireRule(List<FSMInstance> acceptingFSMInstances,
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
      Iterator<FSMInstance> accFSMIter = acceptingFSMInstances.iterator();
      FSMInstance currentAcceptor;
      RightHandSide currentRHS;
      lastAGPosition = startNode.getOffset().longValue();

      while(accFSMIter.hasNext()) {
        currentAcceptor = accFSMIter.next();
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
      Iterator<FSMInstance> accFSMIter = acceptingFSMInstances.iterator();
      FSMInstance currentAcceptor = accFSMIter.next();
      if(isDebugMode()) {
        // see if we have any conflicts
        Iterator<FSMInstance> accIter = acceptingFSMInstances.iterator();
        FSMInstance anAcceptor;
        List<FSMInstance> conflicts = new ArrayList<FSMInstance>();
        while(accIter.hasNext()) {
          anAcceptor = accIter.next();
          if(anAcceptor.equals(currentAcceptor)) {
            conflicts.add(anAcceptor);
          }
          else {
            break;
          }
        }
        if(conflicts.size() > 1) {
          log.info("Conflicts found during matching:"
                  + "\n================================");
          accIter = conflicts.iterator();
          int i = 0;
          while(accIter.hasNext()) {
            if (log.isInfoEnabled())
              log.info(i++ + ") " + accIter.next().toString());
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
        // log.debug("Jape grammar in MULTI application style.");
        // ~bp: check for other matching fsm instances with same length,
        // priority and rule index : if such execute them also.
        String currentAcceptorString = null;
        multiModeWhile: while(accFSMIter.hasNext()) {
          FSMInstance rivalAcceptor = accFSMIter.next();
          // get rivals that match the same document segment
          // makes use of the semantic difference between the compareTo
          // and equals methods on FSMInstance
          if(rivalAcceptor.compareTo(currentAcceptor) == 0) {
            // gets the rivals that are NOT COMPLETELY IDENTICAL with
            // the current acceptor.
            if(!rivalAcceptor.equals(currentAcceptor)) {
              //depends on the debug option in the transducer
              if(isDebugMode()) {
                if(currentAcceptorString == null) {
                  // first rival
                  currentAcceptorString = currentAcceptor.toString();
                  if (log.isInfoEnabled()) {
                    log.info("~Jape Grammar Transducer : "
                            + "\nConcurrent Patterns by length,priority and index (all transduced):");
                    log.info(currentAcceptorString);
                    log.info("bindings : " + currentAcceptor.getBindings());
                    log.info("Rivals Follow: ");
                  }
                }
                if (log.isInfoEnabled()) {
                  log.info(rivalAcceptor);
                  log.info("bindings : " + rivalAcceptor.getBindings());
                }
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
    SimpleSortedSet offsetsTailSet = offsets.tailSet(lastAGPosition);
    long theFirst = offsetsTailSet.first();
    if(theFirst < 0) {
      // no more input, phew! :)
      startNodeOff = -1;
      fireProcessFinished();
    } else {
      long nextKey = theFirst;
      startNode = ((Annotation)((ArrayList)annotationsByOffset.get(nextKey))
              .get(0)). // nextKey
              getStartNode();
      startNodeOff = startNode.getOffset().longValue();

      // eliminate the possibility for infinite looping
      if(oldStartNodeOff == startNodeOff) {
        // we are about to step twice in the same place, ...skip ahead
        lastAGPosition = startNodeOff + 1;
        offsetsTailSet = offsets.tailSet(lastAGPosition);
        theFirst = offsetsTailSet.first();
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

  /**
   * Checks if this Phase has the annotation type as input. This is the
   * case if either no input annotation types were specified, in which case
   * all annotation types will be used, or if the annotation type was
   * specified.
   *
   * @param ident the type of an annotation to be checked
   * @return true if the annotation type will be used in this phase
   */
  public boolean hasInput(String ident) {
    return input.isEmpty() || input.contains(ident);
  }

  /**
   * Check if there is a restriction on the input annotation types
   * for this SPT, i.e. if there were annotation types specified for
   * the "Input:" declaration of this phase.
   *
   * @return true if only certain annotation types are considered in this
   *   phase, false if all are considered.
   */
  public boolean isInputRestricted() {
    return !input.isEmpty();
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
