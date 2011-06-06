package com.ontotext.jape.automaton;

/**
 * This class is needed while building an automaton.
 * 
 * @author petar.mitankin
 * 
 */
public class AutomatonBuildHelp {
	public int transitionsAlloced;
	public int statesAlloced;
	protected int alphabetLength;

	public AutomatonBuildHelp(TripleTransitions tripleTransitions) {
		this.alphabetLength = tripleTransitions.labels.getTransitionsStored();
		transitionsAlloced = tripleTransitions.transitionsStored;
		statesAlloced = tripleTransitions.states.getStored();
	}

	public AutomatonBuildHelp(int alphabetLength) {
		this.alphabetLength = alphabetLength;
		transitionsAlloced = 32;
		statesAlloced = 32;
	}
}
