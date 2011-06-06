/*
 *  FSMInstancePDA.java
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
import gate.annotation.AnnotationSetImpl;
import gate.fsm.FSMInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FSMInstancePDA extends FSMInstance {

	private Document document;

	public FSMInstancePDA(FSMPDA supportGraph, StatePDA FSMPosition,
			Node startNode, Node AGPosition,
			HashMap<String, AnnotationSet> bindings, Document document) {
		super(supportGraph, FSMPosition, startNode, AGPosition, bindings,
				document);
		this.bindingStack = new ArrayList[8];
		this.document = document;
	}

	public Object clone() {
		ArrayList<Annotation>[] tmp = bindingStack;
		bindingStack = null;
		FSMInstancePDA clone = (FSMInstancePDA) super.clone();
		bindingStack = tmp;
		clone.bindingStack = new ArrayList[bindingStack.length];
		for (int i = 0; i < bindingStackStored; i++) {
			if (bindingStack[i] != null) {
				clone.bindingStack[i] = (ArrayList<Annotation>) bindingStack[i]
						.clone();
			}
		}
		return clone;
	}

	/**
	 * A stack of bindings. It maps a number i to bindingStack[i]. A string
	 * label L corresponds to each i. This label L becomes clear when a
	 * transition '):L' (of type closing-round-bracket) is consumed. When L
	 * becomes clear, we add the pair <L, bindingStack[i]> into the hash map
	 * bindings.
	 */
	private ArrayList<Annotation>[] bindingStack;

	/**
	 * The number of the annotation sets stored in the stack. If
	 * bindingStackStored > 0, then the top set of the stack is
	 * bindingStack[bindingStackStored - 1].
	 */
	private int bindingStackStored;

	/**
	 * Pushes a new empty annotation set in the binding stack. This method is
	 * invoked for each opening-round-bracket transition that is consumed during
	 * the traversal.
	 */
	public void pushNewEmptyBindingSet() {
		if (bindingStack.length == bindingStackStored) {
			bindingStack = Arrays.copyOf(bindingStack, 2 * bindingStackStored);
		}
		bindingStack[bindingStackStored] = null;
		bindingStackStored++;
	}

	/**
	 * Pops annotation set from the binding stack and puts it in the hash map
	 * bindings. This method is invoked when a closing-round-bracket transition
	 * '):label' is consumed during the traversal.
	 */
	public void popBindingSet(String label) {
		// Here bindingStackStored is always > 0.
		bindingStackStored--;
		ArrayList<Annotation> annotList = bindingStack[bindingStackStored];
		if (annotList != null && !annotList.isEmpty()) {
			AnnotationSet annotSet = getBindings().get(label);
			if (annotSet == null) {
				annotSet = new AnnotationSetImpl(document);
			}
			for (Annotation a : annotList) {
				annotSet.add(a);
			}
			getBindings().put(label, annotSet);
		}
	}

	/**
	 * Adds all annotations from tuple to each annotation set stored in the
	 * binding stack.
	 */
	public void bindAnnotations(Annotation[] tuple) {
		int j;
		for (int i = 0; i < bindingStackStored; i++) {
			for (j = 0; j < tuple.length; j++) {
				if (bindingStack[i] == null) {
					bindingStack[i] = new ArrayList<Annotation>();
				}
				bindingStack[i].add(tuple[j]);
			}
		}
	}

}
