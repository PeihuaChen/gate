package debugger.gui.resources;

import gate.*;
import gate.jape.SinglePhaseTransducer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.*;

import debugger.resources.pr.PrModel;
import debugger.resources.lr.LrModel;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin
 */
public class ResourceTreeModel extends DefaultTreeModel {
    private static int DebugLevel = 4;
    private DefaultMutableTreeNode prResources;
    private DefaultMutableTreeNode lrResources;

    /**
     * Creates a new instance of ResourceModel with newRoot set
     * to the root of this model.
     */
    private ResourceTreeModel(TreeNode newRoot) {
        super(newRoot);
    }

    /** Creates a new default instance of ResourceModel. */
    public ResourceTreeModel() {
        super(createNewNode("Resources"));
        prResources = createNewNode("Processing Resources");
        ((DefaultMutableTreeNode) getRoot()).add(prResources);
        lrResources = createNewNode("Language Resources");
        ((DefaultMutableTreeNode) getRoot()).add(lrResources);
//        Gate.addCreoleListener(DebugController.getInstance().getResourceController());
    }

    public static DefaultMutableTreeNode createNewNode(String name) {
        if (null == name) {
            name = "Undefined name";
        }
        return new DefaultMutableTreeNode(new String(name));
    }

    public Enumeration getPRResources() {
        return prResources.children();
    }

    public Enumeration getLrResources() {
        return lrResources.children();
    }

    public PrModel getPRResource(ProcessingResource pr) {
        Enumeration prEnum = prResources.children();
        while (prEnum.hasMoreElements()) {
            PrModel prModel = (PrModel) prEnum.nextElement();
            if (prModel.getProcessingResource().equals(pr)) {
                return prModel;
            }
        }
        return null;
    }

    public PrModel getPRResource(String phaseName) {
        Enumeration prEnum = prResources.children();
        while (prEnum.hasMoreElements()) {
            PrModel pr = (PrModel) prEnum.nextElement();
            if (pr.getName().equals(phaseName)) {
                return pr;
            }
        }
        return null;
    }

    public PrModel getPRResource(SinglePhaseTransducer spt) {
        if (DebugLevel >= 5) {
            System.out.print("DEBUG [" + this.getClass().getName() + "]: ");
            System.out.println("getPrResource(SinglePhaseTransducer): SPT name [" + spt.getName() + "] child count = " + prResources.getChildCount());
        }
        Enumeration prEnum = prResources.children();
        while (prEnum.hasMoreElements()) {
            PrModel pr = (PrModel) prEnum.nextElement();
            if (null != pr.getPhase(spt)) {
                return pr;
            }
        }
        return null;
    }

    public LrModel getLRResource(LanguageResource lr) {
        Enumeration lrEnum = lrResources.children();
        while (lrEnum.hasMoreElements()) {
            LrModel model = (LrModel) lrEnum.nextElement();
            if (model.getLr().equals(lr)) {
                return model;
            }
        }
        return null;
    }

    public DefaultMutableTreeNode getPRResourcesNode() {
        return prResources;
    }

    public DefaultMutableTreeNode getLRResourcesNode() {
        return lrResources;
    }

    public void reloadProcessingResource(ProcessingResource resource) {
//        this.prResources.remove(this.getPRResource(resource));
//        this.prResources.add(new PrModel(resource));
        this.reload();
    }
}

