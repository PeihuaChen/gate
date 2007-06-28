package debugger.resources.pr;

import debugger.resources.ResourcesFactory;
import gate.Annotation;
import gate.AnnotationSet;
import gate.jape.RightHandSide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin
 */

public class RuleModel //extends DefaultMutableTreeNode
{
    private HashMap bindings;
    /** Reference to {@link gate.jape.RightHandSide} of this rule */
    private final RightHandSide rhs;
    /** Represents this rule name */
    private final RuleName ruleName;
    /** Annotation history for this rule */
    private final RuleAnnotationHistory annotationHistory;
    /** Designates if this rule marked as breakpoint */
    private boolean stopOnMatch = false;

    /** Constructs RuleModel from given {@link gate.jape.RightHandSide}
     *  @param rhs {@link gate.jape.RightHandSide} to construct this RuleModel. */
    public RuleModel(RightHandSide rhs) {
        this.rhs = rhs;
        this.annotationHistory = new RuleAnnotationHistory();
        this.ruleName = new RuleName(this.rhs);
        //setUserObject(ruleName);
        //setAllowsChildren(false);
    }

    /** @return String representation of this rule name based on {@link #ruleName}.
     * Simply {@link debugger.resources.pr.RuleModel.RuleName#toString()}
     * */
    public String getName() {
        return ruleName.toString();
    }

    /** @return {@link #rhs} for this rule */
    public RightHandSide getRHS() {
        return rhs;
    }

    /** @return {@link #annotationHistory} for this rule */
    public RuleAnnotationHistory getAnnotationHistory() {
        return annotationHistory;
    }

    /** @return true if this rule marked as breakpoint */
    public boolean isStopOnMatch() {
        return stopOnMatch;
    }

    /** @param stopOnMatch set true to set breakpoint for this rule */
    public void setStopOnMatch(boolean stopOnMatch) {
        this.stopOnMatch = stopOnMatch;
    }

    public boolean equals(Object obj) {
        if (obj instanceof RuleModel) {
            return this.rhs.equals(((RuleModel) obj).rhs);
        }
        return super.equals(obj);
    }

    public void setBindings(HashMap bindings) {
        this.bindings = bindings;
    }

    public HashMap getBindings() {
        return bindings;
    }

    class RuleName {
        private final RightHandSide rhs;

        public RuleName(RightHandSide rhs) {
            this.rhs = rhs;
        }

        /** @return String representation of this rule name */
        public String toString() {
            if (null == rhs) {
                return "RHS is null!";
            }
            if (null == this.rhs.getRuleName() || this.rhs.getRuleName().length() == 0) {
                return "Unnamed Rule";
            }
            return this.rhs.getRuleName();
        }

        /**
         * Compares this RuleName to another RuleName or String.
         * Assumes null != null.
         * */
        public boolean equals(Object obj) {
            if (null == obj || null == rhs || null == rhs.getRuleName()) {
                return false;
            }
            if (obj instanceof String) {
                return rhs.getRuleName().equals(obj);
            }
            if (obj instanceof RuleName) {
                return rhs.getRuleName().equals(((RuleName) obj).rhs.getRuleName());
            }
            return false;
        }
    }

    public ArrayList getMatshedRuleTable() {
        ArrayList tm = new ArrayList();
        HashMap hm = getBindings();
        if (null == hm) return tm;
        Iterator iter = hm.keySet().iterator();
        while (iter.hasNext()) {
            String label = (String) iter.next();
            if (label.startsWith("(")) {
                StringTokenizer st = new StringTokenizer(label, "\n");
                while (st.hasMoreTokens()) {
                    tm.add(st.nextToken());
                }
            }
        }
        return tm;
    }

    public ArrayList getMatchedText() {
        ArrayList alCPE = getMatshedRuleTable();
        ArrayList result = new ArrayList();
        HashMap bindHashMap = getBindings();
        for (int i = 0; i < alCPE.size(); i++) {
            String currStr = ((String) alCPE.get(i)).trim();
            Iterator bindIter = bindHashMap.keySet().iterator();
            String currText = "";
            while (bindIter.hasNext()) {
                String s = (String) bindIter.next();
                if (s.startsWith("{")) {
                    if (s.equals(currStr)) {
                        //currText = s;
                        AnnotationSet as = (AnnotationSet) bindHashMap.get(s);
                        try {
                            Annotation rightAnn = neededAnnotation(as, s);
                            if (null != rightAnn)
                                currText = as.getDocument().getContent().getContent(rightAnn.getStartNode().getOffset(), rightAnn.getEndNode().getOffset()).toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
            result.add(currText);
        }
        return result;
    }

    private Annotation neededAnnotation(AnnotationSet annSet, String target) {

        ArrayList containedFeatures = new ArrayList();
        StringTokenizer st = new StringTokenizer(target, "==");
        if (st.countTokens() == 1) {
        } else {
            for (String currToken = st.nextToken(); st.hasMoreTokens(); currToken = st.nextToken()) {
                int point = currToken.indexOf(".");
                if (point != -1) {
                    if (!currToken.substring(0, point).endsWith("Lookup"))
                        if (!currToken.substring(0, point).endsWith("Token")) {
                            currToken = currToken.substring(point + 1);
                            containedFeatures.add(currToken);
                        }
                }
            }
        }
        boolean isEqual = true;
        Iterator<Annotation> iterSet = annSet.iterator();
        while (iterSet.hasNext()) {
            Annotation ann = iterSet.next();
            String annType = ann.getType();
            if (target.indexOf(annType) != -1) {
                gate.FeatureMap fm = ann.getFeatures();
                for (int i = 0; i < containedFeatures.size(); i++) {
                    String s = (String) containedFeatures.get(i);
                    if (!fm.containsKey(s)) {
                        isEqual = false;
                        break;
                    }
                }
                Iterator iter = fm.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    if (target.indexOf(annType + "." + key) != -1) {
                        if (target.indexOf(annType + "." + key + "==\"" + fm.get(key) + "\"") == -1) {
                            isEqual = false;
                        }
                    }
                }
                if (isEqual)
                    return ann;
                else
                    isEqual = true;
            }
        }
        return null;
    }

    public ArrayList getMatchedAnnotations() {
        ArrayList alCPE = getMatshedRuleTable();
        ArrayList result = new ArrayList();
        HashMap bindHashMap = getBindings();
        for (int i = 0; i < alCPE.size(); i++) {
            String currStr = ((String) alCPE.get(i)).trim();
            Iterator bindIter = bindHashMap.keySet().iterator();
            Annotation rightAnn = null;
            while (bindIter.hasNext()) {
                String s = (String) bindIter.next();
                if (s.startsWith("{")) {
                    if (s.equals(currStr)) {
                        AnnotationSet as = (AnnotationSet) bindHashMap.get(s);
                        try {
                            rightAnn = neededAnnotation(as, s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
            result.add(rightAnn);
        }
        return result;
    }

    public PhaseModel getParentPhase() {
        ArrayList prs = ResourcesFactory.getPrRoot().getPRs();
        for (int i = 0; i < prs.size(); i++) {
            PrModel prModel = (PrModel) prs.get(i);
            ArrayList phases = prModel.getPhases();
            for (int j = 0; j < phases.size(); j++) {
                PhaseModel phaseModel = (PhaseModel) phases.get(j);
                ArrayList rules = phaseModel.getRules();
                for (int k = 0; k < rules.size(); k++) {
                    RuleModel ruleModel = (RuleModel) rules.get(k);
                    if (ruleModel == this) return phaseModel;
                }
            }

        }
        return null;

    }

    public String getRuleText() {
        String result = "";
        String all = ResourcesFactory.getCurrentJapeText();
        if (all == null) return "";
        all = all.substring(all.toLowerCase().indexOf("rule"));
        int commentMLIndex = 0;
        int commentSLIndex = 0;
        int startIndex = 0;
        int endIndex = 0;
        while (result.equals("")) {
            endIndex = all.indexOf(" " + getName() + "\n");
            if (endIndex == -1) endIndex = all.indexOf(" " + getName() + " ");
            if (endIndex == -1) endIndex = all.indexOf("\t" + getName() + " ");
            if (endIndex == -1) endIndex = all.indexOf("\t" + getName() + "\n");

            if (endIndex == -1) {
                return "Unknoun text";
            }
            //procesing multiline comments
            commentMLIndex = all.substring(0, endIndex).lastIndexOf("/*");
            if ((commentMLIndex != -1) && (all.substring(commentMLIndex, endIndex).indexOf("*/") == -1)) {
                all = all.substring(endIndex + 2);
                continue;
            }
            //procesing singleline comments
            commentSLIndex = all.substring(0, endIndex).lastIndexOf("//");
            if ((commentSLIndex != -1) && (all.substring(commentSLIndex, endIndex).indexOf("\n") == -1)) {
                all = all.substring(endIndex + 2);
                continue;
            }
            startIndex = all.substring(0, endIndex).toLowerCase().lastIndexOf("rule");
            endIndex = all.substring(startIndex + 1).toLowerCase().indexOf("Rule") + startIndex + 1;
            if (startIndex == endIndex) endIndex = all.length();
            //System.out.println("Rule "+getName()+"("+startIndex+","+endIndex+") is:\n "+result);
            result = all.substring(startIndex, endIndex);
        }
        return result;
    }
}
