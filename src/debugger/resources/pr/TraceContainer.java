package debugger.resources.pr;

import debugger.resources.ResourcesFactory;
import gate.Annotation;
import gate.AnnotationSet;
import gate.annotation.AnnotationSetImpl;
import gate.fsm.State;
import gate.jape.RightHandSide;
import gate.jape.SinglePhaseTransducer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Vladimir Karasev
 */

public class TraceContainer extends ArrayList {
    private HashMap phaseCut = new HashMap();

    public RuleTrace getStateContainer(State state) {
        RuleTrace result = null;
        ArrayList helpList = new ArrayList();
        for (Iterator iter = iterator(); iter.hasNext();) {
            RuleTrace rt = (RuleTrace) iter.next();
            if (rt.containsState(state)) {
                helpList.add(rt);
            }
        }
        if (helpList.isEmpty()) return result;
        result = (RuleTrace) helpList.remove(0);
        for (Iterator iterator = helpList.iterator(); iterator.hasNext();) {
            RuleTrace ruleTrace = (RuleTrace) iterator.next();
            if (ruleTrace.getAnnotations().lastNode().getOffset().longValue() > result.getAnnotations().lastNode().getOffset().longValue())
                result = ruleTrace;
        }
        return result;
    }

    public TraceContainer getTraceByOffset(Long start, Long end) {
        TraceContainer result = new TraceContainer();
        result.phaseCut.putAll(this.phaseCut);
//       for(Iterator iterator = result.phaseCut.keySet().iterator(); iterator.hasNext();)
//       {
//           PhaseModel phaseModel = (PhaseModel) iterator.next();
//           if(null != ((AnnotationSet)result.phaseCut.get(phaseModel)).get("GraduateNoun"))
//           {
//               System.out.println("phaseModel = " + phaseModel.getName());
//               System.out.println("start = " + start);
//           }
//
//       }
        for (Iterator iterator = this.iterator(); iterator.hasNext();) {
            RuleTrace ruleTrace = (RuleTrace) iterator.next();
            if (ruleTrace.getAnnotations().firstNode().getOffset().longValue() >= start.longValue())
                if (ruleTrace.getAnnotations().firstNode().getOffset().longValue() < end.longValue())
                    result.add(ruleTrace);
        }
        return result;
    }
//   public TraceContainer getTraceByPhase(String phaseName)
//   {
//      TraceContainer result = new TraceContainer();
//      for (Iterator iterator = this.iterator(); iterator.hasNext();)
//      {
//         RuleTrace ruleTrace = (RuleTrace) iterator.next();
//         if(ruleTrace.getPhaseName().equals(phaseName))
//            result.add(ruleTrace);
//      }
//      return result;
//   }
//
//   public TraceContainer getTraceByRule(String ruleName)
//   {
//      TraceContainer result = new TraceContainer();
//      for (Iterator iterator = this.iterator(); iterator.hasNext();)
//      {
//         RuleTrace ruleTrace = (RuleTrace) iterator.next();
//         if(ruleTrace.getRuleName().equals(ruleName))
//            result.add(ruleTrace);
//      }
//      return result;
//   }
    public TraceContainer getTraceByRuleModel(RuleModel ruleModel) {
        TraceContainer result = new TraceContainer();
        Iterator iter = this.iterator();
        while (iter.hasNext()) {
            RuleTrace ruleTrace = (RuleTrace) iter.next();
            if (ruleTrace.getRuleModel() == ruleModel)
                result.add(ruleTrace);
        }
        result.phaseCut.putAll(this.phaseCut);
        return result;
    }

    public AnnotationSet getPhaseCut(PhaseModel pm) {
        //System.out.println("phaseCut.size() = " + phaseCut.size());
        return (AnnotationSet) phaseCut.get(pm);
    }

    public void putPhaseCut(SinglePhaseTransducer spt, AnnotationSet annotations) {
        //by Shafirin Andrey
        // don't do anything without JapeDebugger running
        // was an error: resources loaded after MorphHashGazetteer not visible
        // because of to early init of PrRoot while running internal MorphHashGazetteer's
        // tokenizer during parsing MorphHashGazetteer's word list
        //if(null == JapeDebugger.getMainFrame()) return;

        AnnotationSet storedAnnotations = null;
        boolean isSPTFound = false;
        //clone annotations
        storedAnnotations = new AnnotationSetImpl(annotations.getDocument());
        for (Iterator iterator = annotations.iterator(); iterator.hasNext();) {
            Annotation annotation = (Annotation) iterator.next();
            storedAnnotations.add(annotation);
        }
        for (int i = 0; i < ResourcesFactory.getPrRoot().getPRs().size(); i++) {
            PrModel prModel = (PrModel) ResourcesFactory.getPrRoot().getPRs().get(i);
            for (int j = 0; j < prModel.getPhases().size(); j++) {
                PhaseModel phaseModel = (PhaseModel) prModel.getPhases().get(j);
                if (phaseModel.getSPT() == spt) {
                    if (this.phaseCut.containsKey(phaseModel)) {
                        ((AnnotationSet) this.phaseCut.get(phaseModel)).addAll(storedAnnotations);
                    } else
                        this.phaseCut.put(phaseModel, storedAnnotations);
                    isSPTFound = true;
                }
            }
        }
//      if(!isSPTFound)
//         System.out.println("spt " + spt.getName()+ " not found!");

    }

    public void addAll(TraceContainer tc) {
        super.addAll(tc);
//       for(Iterator iterator = tc.phaseCut.keySet().iterator(); iterator.hasNext();)
//       {
//           PhaseModel phaseModel = (PhaseModel) iterator.next();
//           if(null != ((AnnotationSet)tc.phaseCut.get(phaseModel)).get("GraduateNoun"))
//           {
//               System.out.println("-------in addAll Transit-------------------------------");
//               System.out.println("phaseModel = " + phaseModel.getName());
//           }
//
//       }
        for (Iterator iterator = tc.phaseCut.keySet().iterator(); iterator.hasNext();) {
            PhaseModel phaseModel = (PhaseModel) iterator.next();
            if (this.phaseCut.containsKey(phaseModel)) {
                if (phaseModel.getName().equalsIgnoreCase("postprocess")) {
                    this.phaseCut.clear();
                    super.clear();
                    super.addAll(tc);
                    this.phaseCut.put(phaseModel, tc.phaseCut.get(phaseModel));
                } else
                    ((AnnotationSet) this.phaseCut.get(phaseModel)).addAll((AnnotationSet) tc.phaseCut.get(phaseModel));
//            System.out.println("Merge annotations:"+tc.getPhaseCut(phaseModel).getDocument().getContent().toString());
            } else
                this.phaseCut.put(phaseModel, tc.phaseCut.get(phaseModel));
        }
//       for(Iterator iterator = this.phaseCut.keySet().iterator(); iterator.hasNext();)
//       {
//           PhaseModel phaseModel = (PhaseModel) iterator.next();
//           if(null != ((AnnotationSet)this.phaseCut.get(phaseModel)).get("GraduateNoun"))
//           {
//               System.out.println("-------in addAll in--------------------------------");
//               System.out.println("phaseModel = " + phaseModel.getName());
//           }
//
//       }

    }

    public void leaveLast(RightHandSide currentRHS) {
        Iterator iter = iterator();
        RuleTrace last = null;
        while (iter.hasNext()) {
            RuleTrace ruleTrace = (RuleTrace) iter.next();
            if (ruleTrace.getRuleModel().getRHS() == currentRHS) {
                last = ruleTrace;
            }
        }
        iter = iterator();
        while (iter.hasNext()) {
            RuleTrace ruleTrace = (RuleTrace) iter.next();
            if (ruleTrace != last)
                if (ruleTrace.isFinished())
                    ruleTrace.setOverrided(last.getRuleModel());
        }
    }
}
