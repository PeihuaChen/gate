package debugger.resources;

import debugger.resources.pr.TraceContainer;
import gate.AnnotationSet;
import gate.Gate;
import gate.annotation.AnnotationSetImpl;
import gate.creole.ExecutionException;
import gate.creole.ExecutionInterruptedException;
import gate.creole.SerialAnalyserController;
import gate.event.AnnotationSetEvent;
import gate.event.AnnotationSetListener;
import gate.gui.MainFrame;
import gate.gui.SerialControllerEditor;
import gate.jape.RightHandSide;
import gate.jape.SinglePhaseTransducer;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;

import debugger.ClassRipper;
import debugger.JapeDebugger;
import debugger.gui.GuiFactory;
import debugger.gui.actions.debugging.ExecuteRHSAction;
import debugger.gui.actions.debugging.GoNextBreakpointAction;
import debugger.gui.actions.debugging.RunControllerAction;
import debugger.gui.actions.resources.LrResourceSelectedAction;
import debugger.gui.actions.resources.RuleSelectedAction;
import debugger.resources.pr.PhaseModel;
import debugger.resources.pr.PrModel;
import debugger.resources.pr.RuleModel;

import javax.swing.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin
 */
public class PhaseController implements AnnotationSetListener {
    private static PhaseController ourInstance;
    private static int DebugLevel = 3;
//    private PhaseView phaseView;
    private SPTLock sptLock;
    private boolean isStopAfterRHSExec;
    private AnnotationSet ruleAnnotations;
    private TraceContainer traceContainer = new TraceContainer();

    private SinglePhaseTransducer currentSPT;
    private RightHandSide currentRHS;

    public boolean isStopAfterRHSExec() {
        return isStopAfterRHSExec;
    }

    public void setStopAfterRHSExec(boolean stopAfterRHSExec) {
        isStopAfterRHSExec = stopAfterRHSExec;
    }

    public void continueSPT() {
        synchronized (sptLock) {
            this.sptLock.notifyAll();
        }
    }

//    public static PhaseController getInstance()
//    {
//        if(ourInstance == null)
//        {
////            ourInstance = new Phase
//            return new PhaseController();
//        }
//        return ourInstance;
//    }

    public void RuleMatched(SPTLock sptLock, SinglePhaseTransducer spt, RightHandSide rhs,
                            gate.Document document, HashMap bindings,
                            AnnotationSet inputAS, AnnotationSet outputAS) {
        if (DebugLevel >= 5) {
            System.out.print("DEBUG [" + this.getClass().getName() + "]: ");
            System.out.println("RuleMatched(..) rhs phaseName/ruleName: [" + rhs.getPhaseName() + "/" + rhs.getRuleName() + "]");
        }
        // save lock to be able to continue SPT execution later
        this.sptLock = sptLock;
        // looking up for rule
        PhaseModel phase = ResourcesFactory.getPrRoot().getPhase(spt);
        if (phase == null) return;

        RuleModel rule = phase.getRule(rhs.getRuleName());
        if (null != rule) {
            if (rule.isStopOnMatch()) {
                if (DebugLevel >= 4) {
                    System.out.print("DEBUG [" + this.getClass().getName() + "]: ");
                    System.out.println("rule match break point catched! rhs phaseName/ruleName: [" + rhs.getPhaseName() + "/" + rhs.getRuleName() + "]");
                }
                rule.setBindings(bindings);
                LrResourceSelectedAction.getInstance().actionPerformed(
                        ResourcesFactory.getLrRoot().getDocumentModel(document));
                //ResourcesFactory.setCurrentRuleModel(rule);
                RuleSelectedAction.getInstance().actionPerformed(rule);
                SwingUtilities.invokeLater(new Runnable(){
                    public void run()
                    {
                        ExecuteRHSAction.getInstance().setEnabled(true);
                        GoNextBreakpointAction.getInstance().setEnabled(true);
                        // Andrey Shafirn
                        //RunControllerAction.getInstance().setEnabled(true);
                        GuiFactory.getDebugPanel().selectJapeRulePanel();
                        JapeDebugger.getMainFrame().requestFocus();
                    }
                });

                MainFrame.unlockGUI();
                document.getAnnotations().addAnnotationSetListener(this);
                this.ruleAnnotations = new AnnotationSetImpl(document);
                this.currentSPT = spt;
                this.currentRHS = rhs;
                try {
                    synchronized (sptLock) {
                        sptLock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void RuleFinished(SPTLock sptLock, SinglePhaseTransducer spt,
                             RightHandSide rhs, gate.Document document, HashMap bindings,
                             AnnotationSet inputAS, AnnotationSet outputAS) {
        if (DebugLevel >= 5) {
            System.out.print("DEBUG [" + this.getClass().getName() + "]: ");
            System.out.println("RuleFinished(..) rhs phaseName/ruleName: [" + rhs.getPhaseName() + "/" + rhs.getRuleName() + "]");
        }
        // save lock to be able to continue SPT execution later
        this.sptLock = sptLock;
        // looking up for rule
        PhaseModel phase = ResourcesFactory.getPrRoot().getPhase(spt);
        if (phase == null) return;

        RuleModel rule = phase.getRule(rhs.getRuleName());
        if (null != rule) {
            if (DebugLevel >= 4) {
                System.out.print("DEBUG [" + this.getClass().getName() + "]: ");
                System.out.println("RHS executed! rhs phaseName/ruleName: [" + rhs.getPhaseName() + "/" + rhs.getRuleName() + "]");
            }
            if (rule.isStopOnMatch()) {
//                DebugController.getInstance().getRuleController().addCreatedAnnotations(spt, rhs, this.ruleAnnotations.get());
                RuleModel rm = rule;//DebugController.getInstance().getResourceController().getResourceModel().getPRResource(spt).getPhase(spt).getRule(rhs);
                rm.getAnnotationHistory().addAnnotationSet(this.ruleAnnotations.get());
                document.getAnnotations().removeAnnotationSetListener(this);
            }
            if (this.isStopAfterRHSExec) {
                if (this.currentSPT != spt || this.currentRHS != rhs) {
                    throw new RuntimeException("Rule Finished called for another rule!!!");
                }
//                rule.setSPT(spt);
                rule.setBindings(bindings);//???
                LrResourceSelectedAction.getInstance().actionPerformed(
                        ResourcesFactory.getLrRoot().getDocumentModel(document));
                //ResourcesFactory.setCurrentRuleModel(rule);
                RuleSelectedAction.getInstance().actionPerformed(rule);
                SwingUtilities.invokeLater(new Runnable(){
                    public void run()
                    {
                        ExecuteRHSAction.getInstance().setEnabled(false);
                        GoNextBreakpointAction.getInstance().setEnabled(true);
                        // Andrey Shafirn
                        //RunControllerAction.getInstance().setEnabled(true);
                        GuiFactory.getDebugPanel().selectJapeRulePanel();
                        JapeDebugger.getMainFrame().requestFocus();
                    }
                });


                MainFrame.unlockGUI();
                try {
                    synchronized (sptLock) {
                        sptLock.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**Called when a new {@link gate.Annotation} has been added*/
    public void annotationAdded
            (AnnotationSetEvent
            e) {
        this.ruleAnnotations.add(e.getAnnotation());
    }

    /**Called when an {@link gate.Annotation} has been removed*/
    public void annotationRemoved
            (AnnotationSetEvent
            e) {
        // TODO: probably we'll need to add AnnotationSetListener to this annotation
        // to remove references to annotations removed by another rules later
        this.ruleAnnotations.remove(e.getAnnotation());
    }

    public void runControllerButtonActionPerformed(ActionEvent ae) {
        for (Iterator it = Gate.getCreoleRegister().getVrInstances().iterator(); it.hasNext();) {
            Object current = it.next();
            if (current instanceof SerialControllerEditor) {
                SerialAnalyserController controller = null;
                try {
                    controller = (SerialAnalyserController) ClassRipper.getFieldValue(current, "controller");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (controller != null) {
                    final SerialAnalyserController controller1 = controller;
                    Runnable runnable = new Runnable() {
                        public void run() {
                            try {
                                Gate.setExecutable(controller1);
                                controller1.execute();
                            } catch (ExecutionInterruptedException eie) {
                                eie.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (Exception ee) {
                                ee.printStackTrace();
                            } finally {
                                Gate.setExecutable(null);
                            }
                        }
                    };
                    Thread thread = new Thread(Thread.currentThread().getThreadGroup(), runnable, "ApplicationViewer1");
                    thread.setPriority(Thread.MIN_PRIORITY);
                    thread.start();
                }
                break;
            }
        }
    }

    public void TraceTransit(TraceContainer traceContainer) {
        this.traceContainer.addAll(traceContainer);
        //System.out.println("count in Phase " + this.traceContainer.size());
    }

    public TraceContainer getRuleTrace() {
        return this.traceContainer;
    }

    public SinglePhaseTransducer getSPT() {
//        return DebugController.getInstance().getRuleController().getSPT();
        return null;
    }

}
