package debugger.resources.pr;

import debugger.ClassRipper;
import gate.ProcessingResource;
import gate.jape.Batch;
import gate.jape.MultiPhaseTransducer;
import gate.jape.SinglePhaseTransducer;

import java.util.ArrayList;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin
 */

public class PrModel //extends DefaultMutableTreeNode
{
    private static byte DebugLevel = 3;
    final ProcessingResource processingResource;
    final MultiPhaseTransducer transducer;
    private final PRName prName;
    private ArrayList phases = new ArrayList();

    public PrModel(ProcessingResource pr) {
        this.processingResource = pr;
        this.prName = new PRName(this.processingResource);
        //setUserObject(this.prName);
        this.transducer = getMPTfromPR(processingResource);
        if (null != this.transducer) {
            ArrayList spts = this.transducer.getPhases();
            for (int i = 0; i < spts.size(); i++) {
                SinglePhaseTransducer phaseTransducer = (SinglePhaseTransducer) spts.get(i);
                phases.add(new PhaseModel(phaseTransducer));
            }
            /*for(Iterator phIter = phases.iterator(); phIter.hasNext();)
            {
                SinglePhaseTransducer spt = (SinglePhaseTransducer) phIter.next();
                // add nodes for phases
                this.add(new PhaseModel(spt));
            } */
        }
//        else
//        {
//            this.add(new DefaultMutableTreeNode("This PR isn't subclass of Transducer"));
//        }
    }

    public String getName() {
        return this.prName.toString();
    }

    public MultiPhaseTransducer getTransducer() {
        return this.transducer;
    }

    public ProcessingResource getProcessingResource() {
        return processingResource;
    }

    public ArrayList getPhases() {
        return this.phases;
        //return this.children();
    }

    public PhaseModel getPhase(String phaseName) {
        //Enumeration phaseEnum = this.children();
        for (int i = 0; i < phases.size(); i++) {
            PhaseModel phaseModel = (PhaseModel) phases.get(i);
            if (phaseModel.getName().equals(phaseName)) {
                return phaseModel;
            }
        }
//        while(phases.hasMoreElements())
//        {
//            PhaseModel phase = (PhaseModel) phaseEnum.nextElement();
//            if(phase.getName().equals(phaseName))
//            {
//                return phase;
//            }
//        }
        return null;
    }

    public PhaseModel getPhase(SinglePhaseTransducer spt) {
        if (DebugLevel >= 4) {
            System.out.print("DEBUG [" + this.getClass().getName() + "]: ");
//            System.out.println("getPhase(SinglePhaseTransducer): pr name[" + this.processingResource.getName() + "]" +
//                    " spt name: [" + spt.getName() + "]" +
//                    "] child count = " + this.getChildCount());
        }
        for (int i = 0; i < phases.size(); i++) {
            PhaseModel phaseModel = (PhaseModel) phases.get(i);
            if (phaseModel.containsSPT(spt)) {
                return phaseModel;
            }
        }

//        Enumeration phaseEnum = this.children();
//        while(phaseEnum.hasMoreElements())
//        {
//            Object o = phaseEnum.nextElement();
//            if(o instanceof PhaseModel)
//            {
//                PhaseModel phase = (PhaseModel)o;
//                if(phase.containsSPT(spt))
//                {
//                    return phase;
//                }
//            }
//        }
        return null;
    }

//    public Enumeration children()
//    {
//        List c = new ArrayList(children);
//        Collections.sort(c, new Comparator()
//        {
//            public int compare(Object o1, Object o2)
//            {
//                String name1 = ((PhaseModel) o1).getName();
//                String name2 = ((PhaseModel) o2).getName();
//                return name1.compareToIgnoreCase(name2);
//            }
//        });
//        return new Vector(c).elements();
//    }

    static MultiPhaseTransducer getMPTfromPR(ProcessingResource pr) {
        if (!(pr instanceof gate.creole.Transducer)) {
            return null;
        }
        try {
            Batch batch = (Batch) ClassRipper.getFieldValue(pr, "batch");
            gate.jape.Transducer tr = batch.getTransducer();
            if (DebugLevel >= 4) {
                System.out.print("DEBUG [" + PrModel.class.getName() + "]: ");
                System.out.print("Batch Transducer: ");
                System.out.print("Name = [" + tr.getName() + "]");
                System.out.print("BaseUrl = [" + tr.getBaseURL() + "]");
                System.out.print("Class = [" + tr.getClass() + "]");
                System.out.print("\n");
            }
            if (null != tr && tr instanceof MultiPhaseTransducer) {
                return (MultiPhaseTransducer) tr;
            } else {
                return null;
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof PrModel) {
            return this.processingResource.equals(((PrModel) obj).processingResource);
        }
        return super.equals(obj);
    }

    class PRName {
        private final ProcessingResource pr;

        public PRName(ProcessingResource pr) {
            if (null == pr) {
                throw new RuntimeException("ProcessingResource is null!");
            }
            this.pr = pr;
        }

        public String toString() {
            if (null == this.pr.getName() || this.pr.getName().length() == 0) {
                return "Unnamed Resource";
            } else {
                return this.pr.getName();
            }
        }

        public boolean equals(Object obj) {
            if (null == obj || null == this.pr || null == this.pr.getName()) {
                return false;
            }
            if (obj instanceof String) {
                return this.pr.getName().equals(obj);
            }
            if (obj instanceof PRName) {
                return this.pr.getName().equals(((PRName) obj).pr.getName());
            }
            return false;
        }
    }
}
