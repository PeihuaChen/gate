package debugger.resources.pr;

import gate.Gate;
import gate.ProcessingResource;
import gate.jape.SinglePhaseTransducer;

import java.util.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Vladimir Karasev
 */

public class PRRoot {
    private ArrayList prs;

    public PRRoot() {
        if (null == Gate.getCreoleRegister()) {
            prs = new ArrayList();
        } else {
            prs = new ArrayList();
            List prInstances;
            prInstances = Gate.getCreoleRegister().getPublicPrInstances();
            List list = new ArrayList(prInstances);
            Collections.sort(list, new Comparator() {
                public int compare(Object o1, Object o2) {
                    String name1 = ((ProcessingResource) o1).getName();
                    String name2 = ((ProcessingResource) o2).getName();
                    return name1.compareToIgnoreCase(name2);
                }
            });
            for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                ProcessingResource pr = (ProcessingResource) iterator.next();
                prs.add(new PrModel(pr));
            }
        }
    }

    public ArrayList getPRs() {
        return prs;
    }

    public PhaseModel getPhase(SinglePhaseTransducer spt) {
        for (int i = 0; i < prs.size(); i++) {
            PrModel prModel = (PrModel) prs.get(i);
            if (prModel.getPhase(spt) != null) {
                return prModel.getPhase(spt);
            }
        }
        return null;
    }

}
