package debugger.resources.pr;

import debugger.ClassRipper;
import debugger.resources.ResourcesFactory;
import gate.fsm.FSM;
import gate.jape.RightHandSide;
import gate.jape.SinglePhaseTransducer;

import java.util.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin
 */

public class PhaseModel //extends DefaultMutableTreeNode
{
    final SinglePhaseTransducer transducer;
    private final SPTName transducerName;

    public ArrayList getRules() {
        return rules;
    }

    ArrayList rules = new ArrayList();

    public PhaseModel(SinglePhaseTransducer transducer) {
        this.transducer = transducer;
        this.transducerName = new SPTName(this.transducer);
        //setUserObject(transducerName);
        //    for (Iterator rulesItr = getPhaseRules(this.transducer).iterator(); rulesItr.hasNext();) {
        rules = getPhaseRules(transducer);
        //this.add((RuleModel) rulesItr.next());
        //    }
        this.transducer.setPhaseController(ResourcesFactory.getPhaseController());
    }

    public String getName() {
        return this.transducerName.toString();
    }

    ArrayList getPhaseRules(SinglePhaseTransducer spt) {
        ArrayList phaseRules = new ArrayList();
        FSM fsm = spt.getFSM();
        try {
            Collection allStates = (Collection) ClassRipper.getFieldValue(fsm, "allStates");
            synchronized (phaseRules) {
                for (Iterator allStatesItr = allStates.iterator(); allStatesItr.hasNext();) {
                    gate.fsm.State st = (gate.fsm.State) allStatesItr.next();
                    if (!st.isFinal()) {
                        continue; // processing just final states
                    }
                    RightHandSide rhs = st.getAction();
                    RuleModel newRule = new RuleModel(rhs);
                    HashMap hm = new HashMap();
                    hm.put(fsm.ruleHash.get(newRule.getName()), null);
                    newRule.setBindings(hm);
                    if (!phaseRules.contains(newRule)) {
                        phaseRules.add(newRule);
                    }
                }
                Collections.sort(phaseRules, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        String name1 = ((RuleModel) o1).getName();
                        String name2 = ((RuleModel) o2).getName();
                        return name1.compareToIgnoreCase(name2);
                    }
                });
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return phaseRules;
    }

    public boolean equals(Object obj) {
        if (obj instanceof PhaseModel) {
            return this.transducer.equals(((PhaseModel) obj).transducer);
        }
        return super.equals(obj);
    }

    class SPTName {
        private SinglePhaseTransducer tr;

        public SPTName(SinglePhaseTransducer transducer) {
            if (null == transducer) {
                throw new RuntimeException("SinglePhaseTransducer is null!");
            }
            this.tr = transducer;
        }

        public String toString() {
            if (null == this.tr.getName() || this.tr.getName().length() == 0) {
                return "Unnamed Transducer";
            } else {
                return this.tr.getName();
            }
        }

        public boolean equals(Object obj) {
            if (null == obj || null == tr || null == tr.getName()) {
                return false;
            }
            if (obj instanceof String) {
                return tr.getName().equals(obj);
            }
            if (obj instanceof SPTName) {
                return tr.getName().equals(((SPTName) obj).tr.getName());
            }
            return false;
        }
    }

    public RuleModel getRule(String name) {
        for (int i = 0; i < rules.size(); i++) {
            RuleModel ruleModel = (RuleModel) rules.get(i);
            if (ruleModel.getName().equals(name)) {
                return ruleModel;
            }

        }
//        Enumeration ruleEnum = this.children();
//        while (ruleEnum.hasMoreElements()) {
//            RuleModel rule = (RuleModel) ruleEnum.nextElement();
//            if (rule.getName().equals(name)) {
//                return rule;
//            }
//        }
        return null;
    }

    public RuleModel getRule(RightHandSide rhs) {
        for (int i = 0; i < rules.size(); i++) {
            RuleModel ruleModel = (RuleModel) rules.get(i);
            if (ruleModel.getRHS().equals(rhs)) {
                return ruleModel;
            }
        }
//        Enumeration ruleEnum = this.children();
//        while (ruleEnum.hasMoreElements()) {
//            RuleModel rule = (RuleModel) ruleEnum.nextElement();
//            if (rule.getRHS().equals(rhs)) {
//                return rule;
//            }
//        }
        return null;
    }

    public boolean containsSPT(SinglePhaseTransducer spt) {
        return this.transducer.equals(spt);
    }

    public SinglePhaseTransducer getSPT() {
        return this.transducer;
    }

    public String getControl() {
        return this.transducer.getOption("control");
    }

    public Set getInput() {
        return getSPT().input;
    }

}
