/**
 * Class description.
 * Creation date: Mar 25, 2003
 * Creation time: 11:08:04 AM
 * author: Vladimir Karasev
 */
package debugger.resources.pr;

import debugger.resources.ResourcesFactory;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.annotation.AnnotationSetImpl;
import gate.fsm.State;
import gate.fsm.Transition;
import gate.jape.Constraint;
import gate.jape.RightHandSide;

import java.util.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Vladimir Karasev
 */

public class RuleTrace {
    private boolean finished;
    private RuleModel ruleModel;
    private AnnotationSet annotations;
    private HashMap patterns;
    private State lastState;
    private static int maxId = 0;
    //TODO: make id private
    public int id;
    private RuleModel overrided;

    public RuleTrace(State state, Document document) {
        this.finished = false;
        this.ruleModel = findModel(state);
        this.overrided = null;
        this.annotations = new AnnotationSetImpl(document);
        this.lastState = null;
        this.addState(state);
        this.patterns = new HashMap();
        this.id = maxId++;
    }

    public void addState(State state) {
        if (state.isFinal()) finished = true;
        lastState = state;
    }

    public boolean containsState(State state) {
        return (state == lastState);
    }

    public AnnotationSet getAnnotations() {
        return annotations;
    }

    public void setAnnotations(AnnotationSet annotations) {
        this.annotations = annotations;
    }

    public void addAnnotation(Annotation annotation) {
        this.annotations.add(annotation);
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    private RuleModel findModel(State state) {
        RightHandSide rhs = null;
        if (state.isFinal()) rhs = state.getAction();
        ArrayList wave = new ArrayList();
        wave.add(state);
        while (rhs == null) {
            ArrayList newWave = new ArrayList();
            for (int i = 0; i < wave.size(); i++) {
                State oldState = (State) wave.get(i);
                Iterator<Transition> iter = oldState.getTransitions().iterator();
                while (iter.hasNext()) {
                    Transition transition = iter.next();
                    State newState = transition.getTarget();
                    if (newState.isFinal()) {
                        rhs = newState.getAction();
                        break;
                    }
                    newWave.add(newState);
                }
            }
            wave = newWave;
        }
        for (int i = 0; i < ResourcesFactory.getPrRoot().getPRs().size(); i++) {
            PrModel prModel = (PrModel) ResourcesFactory.getPrRoot().getPRs().get(i);
            for (int j = 0; j < prModel.getPhases().size(); j++) {
                PhaseModel phaseModel = (PhaseModel) prModel.getPhases().get(j);
                for (int k = 0; k < phaseModel.getRules().size(); k++) {
                    RuleModel ruleModel = (RuleModel) phaseModel.getRules().get(k);
                    if (ruleModel.getRHS() == rhs) {
                        return ruleModel;
                    }
                }
            }
        }
        return null;
    }

    public void putPattern(Annotation ann, Constraint constraint) {
        patterns.put(ann, constraint);
    }

    public Constraint getPattern(Annotation ann) {
        return (Constraint) patterns.get(ann);
    }

    public void setOverrided(RuleModel ruleModel) {
        this.overrided = ruleModel;
    }

    public RuleModel getRuleModel() {
        return ruleModel;
    }

    public RuleModel getOverrided() {
        return overrided;
    }

    public String toString() {
        String result = "RuleTrace:";
        result += annotations.firstNode().getOffset().intValue() + "-"
                + annotations.lastNode().getOffset().intValue();

        result += "(";
        for (Iterator iterator = annotations.iterator(); iterator.hasNext();) {
            Annotation annotation = (Annotation) iterator.next();
            result += annotation.toString();
        }
        result += ")";
        return result;
    }
}
