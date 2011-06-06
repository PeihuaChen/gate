/*
 *  SinglePhaseTransducerPDA.java
 *
 *  Copyright (c) 2010-2011, Ontotext (www.ontotext.com).
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *
 *  $Id$
 */
package com.ontotext.jape.pda;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Node;
import gate.creole.ExecutionException;
import gate.creole.ExecutionInterruptedException;
import gate.fsm.FSMInstance;
import gate.jape.Constraint;
import gate.jape.JapeException;
import gate.jape.RightHandSide;
import gate.jape.SinglePhaseTransducer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SinglePhaseTransducerPDA extends SinglePhaseTransducer {
	private static final long serialVersionUID = 4481388366641529213L;

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

	private static final class FSMMatcherResult {
		public FSMMatcherResult(List<FSMInstancePDA> activeFSMInstances,
				List<FSMInstancePDA> acceptingFSMInstances) {
			this.activeFSMInstances = activeFSMInstances;
			this.acceptingFSMInstances = acceptingFSMInstances;
		}

		private List<FSMInstancePDA> acceptingFSMInstances;
		private List<FSMInstancePDA> activeFSMInstances;
	}

	public SinglePhaseTransducerPDA(String name) {
		super(name);
	}

	protected FSMPDA createFSM() {
		return new FSMPDA(this);
	}

	private void addAnnotationsByOffset(SimpleSet keys, Set annotations) {
		Iterator annIter = annotations.iterator();
		while (annIter.hasNext()) {
			Annotation ann = (Annotation) annIter.next();
			// ignore empty annotations
			long offset = ann.getStartNode().getOffset().longValue();
			if (offset == ann.getEndNode().getOffset().longValue())
				continue;
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
			keys.add(ann);
		}
	}// private void addAnnotationsByOffset()

	/**
	 * Transduce a document using the annotation set provided and the current
	 * rule application style.
	 */
	public void transduce(Document doc, AnnotationSet inputAS,
			AnnotationSet outputAS) throws JapeException, ExecutionException {
		interrupted = false;
		log.debug("Start: " + name);
		fireProgressChanged(0);

		// the input annotations will be read from this map
		SimpleSet annotationsByOffset = new SimpleSet(doc.getContent().size()
				.intValue());

		// select only the annotations of types specified in the input list
		if (input.isEmpty()) {
			addAnnotationsByOffset(annotationsByOffset, inputAS);
		} else {
			Iterator typesIter = input.iterator();
			AnnotationSet ofOneType = null;
			while (typesIter.hasNext()) {
				ofOneType = inputAS.get((String) typesIter.next());
				if (ofOneType != null) {
					addAnnotationsByOffset(annotationsByOffset, ofOneType);
				}
			}
		}

		if (annotationsByOffset.isEmpty()) {
			fireProcessFinished();
			return;
		}

		annotationsByOffset.finish();
		// define data structures
		// FSM instances that haven't blocked yet
		if (activeFSMInstances == null) {
			activeFSMInstances = new LinkedList<FSMInstance>();
		} else {
			activeFSMInstances.clear();
		}

		// FSM instances that have reached a final state
		// This is a list and the contained objects are sorted by the length
		// of the document content covered by the matched annotations
		List<FSMInstancePDA> acceptingFSMInstances = new LinkedList<FSMInstancePDA>();

		// find the first node of the document
		Node startNode = annotationsByOffset
				.get(annotationsByOffset.firstStartOffsetAfter(0)).get(0)
				.getStartNode();

		// used to calculate the percentage of processing done
		long lastNodeOff = doc.getContent().size().longValue();

		// the offset of the node where the matching currently starts
		// the value -1 marks no more annotations to parse
		long startNodeOff = startNode.getOffset().longValue();

		// The structure that fireRule() will update
		SearchState state = new SearchState(startNode, startNodeOff, 0);

		// the big while for the actual parsing
		while (state.startNodeOff != -1) {
			// while there are more annotations to parse
			// create initial active FSM instance starting parsing from new
			// startNode
			// currentFSM = FSMInstance.getNewInstance(
			FSMInstancePDA firstCurrentFSM = new FSMInstancePDA((FSMPDA) fsm,
					(StatePDA) ((FSMPDA) fsm).getInitialState(),// fresh start
					state.startNode,// the matching starts form the current
									// startNode
					state.startNode,// current position in AG is the start
									// position
					new java.util.HashMap<String, AnnotationSet>(),// no
																	// bindings
																	// yet!
					doc);

			// at this point ActiveFSMInstances should always be empty!
			activeFSMInstances.clear();
			acceptingFSMInstances.clear();
			activeFSMInstances.add(firstCurrentFSM);

			// far each active FSM Instance, try to advance
			// while(!finished){
			activeFSMWhile: while (!activeFSMInstances.isEmpty()) {
				if (interrupted)
					throw new ExecutionInterruptedException(
							"The execution of the \""
									+ getName()
									+ "\" Jape transducer has been abruptly interrupted!");

				// take the first active FSM instance
				FSMInstancePDA currentFSM = (FSMInstancePDA) activeFSMInstances
						.remove(0);
				// process the current FSM instance
				// if(currentFSM.getFSMPosition().isFinal()) {
				// // the current FSM is in a final state
				// acceptingFSMInstances.add((FSMInstance)currentFSM.clone());
				// // if we're only looking for the shortest stop here
				// if(ruleApplicationStyle == FIRST_STYLE) break;
				// }

				FSMMatcherResult result = attemptAdvance(currentFSM,
						annotationsByOffset, doc, inputAS);
				if (result != null) {
					if (result.acceptingFSMInstances != null
							&& !result.acceptingFSMInstances.isEmpty()) {
						acceptingFSMInstances
								.addAll(result.acceptingFSMInstances);
						if (ruleApplicationStyle == FIRST_STYLE
								|| ruleApplicationStyle == ONCE_STYLE)
							break activeFSMWhile;
					}

					if (result.activeFSMInstances != null
							&& !result.activeFSMInstances.isEmpty()) {
						activeFSMInstances.addAll(result.activeFSMInstances);
					}
				}
			}
			boolean keepGoing = fireRule(acceptingFSMInstances, state,
					lastNodeOff, inputAS, outputAS, doc, annotationsByOffset);
			if (!keepGoing)
				return;

		}// while(state.startNodeOff != -1)

		fireProcessFinished();
	} // transduce

	/**
	 * A flag used to indicate when advancing the current instance requires the
	 * creation of a clone (i.e. when there are more than 1 ways to advance).
	 */
	private boolean newInstanceRequired;

	/**
	 * A clone to be used for creating new states. The actual current instance
	 * cannot be used itself, as it may change.
	 */
	private FSMInstancePDA currentClone;

	/**
	 * A list of the new active FSM instances.
	 */
	private List<FSMInstancePDA> newActiveInstances;

	/**
	 * Generates a new active FSM instance if it is needed. Adds each new FSM
	 * instance that is created in the list of the new active FSM instances.
	 */
	private FSMInstancePDA getNewFSMInstance(FSMInstancePDA currentInstance) {
		FSMInstancePDA newFSMI;

		if (newInstanceRequired) {
			// we need to create a clone
			newFSMI = (FSMInstancePDA) currentClone.clone();
		} else {
			// we're advancing the current instance
			newFSMI = currentInstance;
			// next time, we'll have to create a new one
			newInstanceRequired = true;
		}
		if (newActiveInstances == null) {
			newActiveInstances = new ArrayList<FSMInstancePDA>();
		}
		newActiveInstances.add(newFSMI);
		return newFSMI;
	}

	/**
	 * Try to advance the activeFSMInstances.
	 * 
	 * @return a list of newly created FSMInstances
	 */
	@SuppressWarnings("unchecked")
	private FSMMatcherResult attemptAdvance(FSMInstancePDA currentInstance,
			SimpleSet annotationsByOffset, Document document,
			AnnotationSet inputAS) {
		newActiveInstances = null;
		List<FSMInstancePDA> newAcceptingInstances = null;

		// Attempt advancing the current instance.
		// While doing that, generate new active FSM instances and
		// new accepting FSM instances, as required
		// create a clone to be used for creating new states
		// the actual current instance cannot be used itself, as it may change
		currentClone = (FSMInstancePDA) currentInstance.clone();

		// process the current FSM instance
		if (currentInstance.getFSMPosition().isFinal()) {
			// the current FSM is in a final state
			newAcceptingInstances = new ArrayList<FSMInstancePDA>();
			newAcceptingInstances.add((FSMInstancePDA) currentClone);
			// if we're only looking for the shortest stop here
			if (ruleApplicationStyle == FIRST_STYLE
					|| ruleApplicationStyle == ONCE_STYLE) {
				return new FSMMatcherResult(newActiveInstances,
						newAcceptingInstances);
			}
		}

		// get all the annotations that start where the current FSM finishes
		int theFirst = annotationsByOffset
				.firstStartOffsetAfter(currentInstance.getAGPosition()
						.getOffset().intValue());
		List<Annotation> paths = (theFirst >= 0) ? (List) annotationsByOffset
				.get(theFirst) : null;

		// get the transitions for the current state of the FSM
		StatePDA currentState = (StatePDA) currentClone.getFSMPosition();
		Iterator transitionsIter = currentState.getTransitions().iterator();

		newInstanceRequired = false;

		// for each transition, keep the set of annotations starting at
		// current node (the "paths") that match each constraint of the
		// transition.
		transitionsWhile: while (transitionsIter.hasNext()) {
			TransitionPDA currentTransition = (TransitionPDA) transitionsIter
					.next();
			int tranistionType = currentTransition.getType();

			FSMInstancePDA newFSMI;

			if (tranistionType == TransitionPDA.TYPE_OPENING_ROUND_BRACKET) {
				// opening-round-bracket transition
				newFSMI = getNewFSMInstance(currentInstance);
				newFSMI.pushNewEmptyBindingSet();
				newFSMI.setFSMPosition(currentTransition.getTarget());
				// we do not advance the AGPosition, since opening-round-bracket
				// transitions are
				// treated like epsilon transitions
				continue;
			}

			if (tranistionType != TransitionPDA.TYPE_CONSTRAINT) {
				// closing-round-bracket transition
				newFSMI = getNewFSMInstance(currentInstance);
				newFSMI.popBindingSet(((FSMPDA) (newFSMI.getSupportGraph()))
						.getBindingNames()[tranistionType]);
				newFSMI.setFSMPosition(currentTransition.getTarget());
				// we do not advance the AGPosition, since closing-round-bracket
				// transitions are
				// treated like epsilon transitions
				continue;
			}

			// constrained transition

			if (paths == null || paths.isEmpty()) {
				continue;
			}

			// There will only be multiple constraints if this transition is
			// over
			// a written constraint that has the "and" operator (comma) and
			// the
			// parts referr to different annotation types. For example -
			// {A,B} would result in 2 constraints in the array, while
			// {A.foo=="val", A.bar=="val"} would only be a single
			// constraint.
			Constraint[] currentConstraints = currentTransition
					.getConstraints().getConstraints();

			boolean hasPositiveConstraint = false;
			List<List<Annotation>> matchLists = new ArrayList<List<Annotation>>();

			// Check all constraints.
			// If any annotation matches any negated constraint, then the
			// transition fails.
			// At least one annotation must match each non-negated constraint.
			for (int i = 0; i < currentConstraints.length; i++) {
				Constraint c = currentConstraints[i];
				List<Annotation> matchList = c
						.matches(paths, ontology, inputAS);
				if (c.isNegated()) {
					if (!matchList.isEmpty())
						continue transitionsWhile;
				} else {
					// if no annotations matched, then the transition fails.
					if (matchList.isEmpty())
						continue transitionsWhile;
					hasPositiveConstraint = true;
					matchLists.add(matchList);
				}
			}
			if (!hasPositiveConstraint) {
				// There are no non-negated constraints. Since the negated
				// constraints
				// did not fail, this means that all of the current
				// annotations
				// are potentially valid. Add the whole set of annotations.
				matchLists.add(paths);
			}

			// We have a match if every positive constraint is met by at
			// least one annot.
			// Given the sets Sx of the annotations that match constraint x,
			// compute all tuples (A1, A2, ..., An) where Ax comes from the
			// set Sx and n is the number of constraints
			Annotation[][] tuples = product(matchLists);

			// Create a new FSM for every tuple of annot
			for (Annotation[] tuple : tuples) {
				// Find longest annotation and use that to mark the start of
				// the
				// new FSM
				Annotation matchingAnnot = getRightMostAnnotation(tuple);

				// we have a match.
				// create a new FSMInstance, advance it over
				// the current
				// annotation take care of the bindings and add it to
				// ActiveFSM
				newFSMI = getNewFSMInstance(currentInstance);
				newFSMI.setAGPosition(matchingAnnot.getEndNode());
				newFSMI.setFSMPosition(currentTransition.getTarget());
				newFSMI.bindAnnotations(tuple);
			} // iter over matching tuples
		}// while(transitionsIter.hasNext())

		return new FSMMatcherResult(newActiveInstances, newAcceptingInstances);
	}

	/**
	 * Return the annotation with the right-most end node
	 * 
	 * @param annots
	 * @return
	 */
	protected Annotation getRightMostAnnotation(Annotation[] annots) {
		long maxOffset = -1;
		Annotation retVal = null;
		for (Annotation annot : annots) {
			Long curOffset = annot.getEndNode().getOffset();
			if (curOffset > maxOffset) {
				maxOffset = curOffset;
				retVal = annot;
			}
		}

		return retVal;
	}

	/**
	 * Computes the cartesian product of the input lists, i.e. computes all
	 * tuples (x1, x2, ..., xn), where x1 comes from the 1st list, x2 comes from
	 * the second, etc. Parameters:
	 * 
	 * @param sourceLists
	 *            a list of n lists
	 */
	private static Annotation[][] product(List<List<Annotation>> sourceLists) {
		int n = sourceLists.size();
		Annotation[][] lists = new Annotation[n][];
		int i = 0, numberOfTuples = 1, j;
		for (List<Annotation> list : sourceLists) {
			j = list.size();
			numberOfTuples *= j;
			lists[i] = new Annotation[j];
			j = 0;
			for (Annotation annot : list) {
				lists[i][j] = annot;
				j++;
			}
			i++;
		}
		int[] positions = new int[n];
		Annotation result[][] = new Annotation[numberOfTuples][n];
		numberOfTuples = 0;
		while (true) {
			for (i = 0; i < n; i++) {
				result[numberOfTuples][i] = lists[i][positions[i]];
			}
			numberOfTuples++;
			for (i = n - 1; i >= 0; i--) {
				positions[i]++;
				if (positions[i] == lists[i].length) {
					positions[i] = 0;
				} else {
					break;
				}
			}
			if (i == -1) {
				break;
			}
		}
		return result;
	}

	/**
	 * Fire the rule that matched.
	 * 
	 * @return true if processing should keep going, false otherwise.
	 * @throws ExecutionInterruptedException
	 */
	protected boolean fireRule(List<FSMInstancePDA> acceptingFSMInstances,
			SearchState state, long lastNodeOff, AnnotationSet inputAS,
			AnnotationSet outputAS, Document doc, SimpleSet annotationsByOffset)
			throws JapeException, ExecutionException {

		Node startNode = state.startNode;
		long startNodeOff = state.startNodeOff;
		long oldStartNodeOff = state.oldStartNodeOff;

		// FIRE THE RULE
		long lastAGPosition = -1;
		if (acceptingFSMInstances.isEmpty()) {
			// no rule to fire, advance to the next input offset
			lastAGPosition = startNodeOff + 1;
		} else if (ruleApplicationStyle == BRILL_STYLE
				|| ruleApplicationStyle == ALL_STYLE) {
			// fire the rules corresponding to all accepting FSM instances
			Iterator<FSMInstancePDA> accFSMIter = acceptingFSMInstances
					.iterator();
			FSMInstancePDA currentAcceptor;
			RightHandSide currentRHS;
			lastAGPosition = startNode.getOffset().longValue();

			while (accFSMIter.hasNext()) {
				currentAcceptor = (FSMInstancePDA) accFSMIter.next();
				currentRHS = currentAcceptor.getFSMPosition().getAction();

				currentRHS.transduce(doc, currentAcceptor.getBindings(),
						inputAS, outputAS, ontology, actionContext);

				if (ruleApplicationStyle == BRILL_STYLE) {
					// find the maximal next position
					long currentAGPosition = currentAcceptor.getAGPosition()
							.getOffset().longValue();
					if (currentAGPosition > lastAGPosition)
						lastAGPosition = currentAGPosition;
				}
			}
			if (ruleApplicationStyle == ALL_STYLE) {
				// simply advance to next offset
				lastAGPosition = lastAGPosition + 1;
			}

		} else if (ruleApplicationStyle == APPELT_STYLE
				|| ruleApplicationStyle == FIRST_STYLE
				|| ruleApplicationStyle == ONCE_STYLE) {
			// AcceptingFSMInstances is an ordered structure:
			// just execute the longest (last) rule
			Iterator<FSMInstancePDA> accFSMIter = acceptingFSMInstances
					.iterator();
			FSMInstancePDA currentAcceptor = (FSMInstancePDA) accFSMIter.next();
			FSMInstancePDA anAcceptor;
			while (accFSMIter.hasNext()) {
				anAcceptor = (FSMInstancePDA) accFSMIter.next();
				if (anAcceptor.compareTo(currentAcceptor) > 0) {
					currentAcceptor = anAcceptor;
				}
			}

			if (isDebugMode()) {
				// see if we have any conflicts
				accFSMIter = acceptingFSMInstances.iterator();
				List<FSMInstancePDA> conflicts = new ArrayList<FSMInstancePDA>();
				conflicts.add(currentAcceptor);
				while (accFSMIter.hasNext()) {
					anAcceptor = (FSMInstancePDA) accFSMIter.next();
					if (anAcceptor.compareTo(currentAcceptor) == 0) {
						if (!anAcceptor.equals(currentAcceptor)) {
							conflicts.add(anAcceptor);
						}
					}
				}
				if (conflicts.size() > 1) {
					log.info("Conflicts found during matching:"
							+ "\n================================");
					accFSMIter = conflicts.iterator();
					int i = 0;
					while (accFSMIter.hasNext()) {
						if (log.isInfoEnabled())
							log.info(i++ + ") " + accFSMIter.next().toString());
					}
				}
			}

			RightHandSide currentRHS = currentAcceptor.getFSMPosition()
					.getAction();
			currentRHS.transduce(doc, currentAcceptor.getBindings(), inputAS,
					outputAS, ontology, actionContext);

			// if in matchGroup mode check other possible patterns in this
			// span
			if (isMatchGroupMode()) {
				// log.debug("Jape grammar in MULTI application style.");
				// ~bp: check for other matching fsm instances with same length,
				// priority and rule index : if such execute them also.
				String currentAcceptorString = null;
				accFSMIter = acceptingFSMInstances.iterator();

				while (accFSMIter.hasNext()) {
					FSMInstancePDA rivalAcceptor = (FSMInstancePDA) accFSMIter
							.next();
					// get rivals that match the same document segment
					// makes use of the semantic difference between the
					// compareTo
					// and equals methods on FSMInstance
					if (rivalAcceptor.compareTo(currentAcceptor) == 0) {
						// gets the rivals that are NOT COMPLETELY IDENTICAL
						// with
						// the current acceptor.
						if (!rivalAcceptor.equals(currentAcceptor)) {
							currentRHS = rivalAcceptor.getFSMPosition()
									.getAction();
							currentRHS.transduce(doc,
									rivalAcceptor.getBindings(), inputAS,
									outputAS, ontology, actionContext);
						} // equal rival
					}
				} // while there are fsm instances
			} // matchGroupMode

			// if in ONCE mode stop after first match
			if (ruleApplicationStyle == ONCE_STYLE) {
				state.startNodeOff = startNodeOff;
				return false;
			}

			// advance in AG
			lastAGPosition = currentAcceptor.getAGPosition().getOffset()
					.longValue();
		} else
			throw new RuntimeException("Unknown rule application style!");

		// advance on input
		long theFirst = annotationsByOffset
				.firstStartOffsetAfter((int) lastAGPosition);
		if (theFirst < 0) {
			// no more input, phew! :)
			startNodeOff = -1;
			fireProcessFinished();
		} else {
			long nextKey = theFirst;
			startNode = annotationsByOffset.get((int) nextKey).get(0)
					.getStartNode();
			startNodeOff = startNode.getOffset().longValue();

			// eliminate the possibility for infinite looping
			if (oldStartNodeOff == startNodeOff) {
				// we are about to step twice in the same place, ...skip ahead
				lastAGPosition = startNodeOff + 1;
				theFirst = annotationsByOffset
						.firstStartOffsetAfter((int) lastAGPosition);
				if (theFirst < 0) {
					// no more input, phew! :)
					startNodeOff = -1;
					fireProcessFinished();
				} else {
					nextKey = theFirst;
					startNode = annotationsByOffset.get((int) theFirst).get(0)
							.getStartNode();
					startNodeOff = startNode.getOffset().longValue();
				}
			}// if(oldStartNodeOff == startNodeOff)
				// fire the progress event
			if (startNodeOff - oldStartNodeOff > 256) {
				if (isInterrupted())
					throw new ExecutionInterruptedException(
							"The execution of the \""
									+ getName()
									+ "\" Jape transducer has been abruptly interrupted!");

				fireProgressChanged((int) (100 * startNodeOff / lastNodeOff));
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
}
