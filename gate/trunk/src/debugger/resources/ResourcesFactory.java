package debugger.resources;

import debugger.gui.GuiFactory;
import debugger.gui.actions.editor.ShowRuleInfoAction;
import debugger.gui.resources.ResourceTree;
import debugger.resources.lr.LRRoot;
import debugger.resources.lr.LrModel;
import debugger.resources.pr.PRRoot;
import debugger.resources.pr.RuleModel;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishchenko
 */

public class ResourcesFactory {
    private static LrModel currentLrModel;
    private static RuleModel currentRuleModel;
    private static PRRoot prRoot;
    private static LRRoot lrRoot;
    private static PhaseController phaseController;
    //private static File currentJapeFile;
    private static JapeFile currentJapeFile;
    private static String currentJapeText;

    public static PhaseController getPhaseController() {
        if (phaseController == null) {
            phaseController = new PhaseController();
        }
        return phaseController;
    }

    public static PRRoot getPrRoot() {
        if (null == prRoot) {
            prRoot = new PRRoot();
        }
        return prRoot;
    }

    public static void updateRoots() {
        prRoot = null;
        lrRoot = null;
    }

    public static LRRoot getLrRoot() {
        if (null == lrRoot) {
            lrRoot = new LRRoot();
        }
        return lrRoot;
    }

    public static RuleModel getCurrentRuleModel() {
        return currentRuleModel;
    }

    public static void setCurrentRuleModel(RuleModel currentRuleModel) {
        ResourcesFactory.currentRuleModel = currentRuleModel;
        // hilight selection in tree
        ResourceTree.PRTreeNode prtNode = GuiFactory.getResourceView().getNode(currentRuleModel);
        TreeNode[] tn = prtNode.getPath();
        TreePath tp = new TreePath(tn);
        GuiFactory.getResourceView().getTree().setSelectionPath(tp);
        GuiFactory.getResourceView().getTree().revalidate();
        GuiFactory.getResourceView().getTree().repaint();
        ShowRuleInfoAction.getInstance().actionPerformed(currentRuleModel);
    }

    public static void setCurrentLrModel(LrModel currentLrModel) {
        ResourcesFactory.currentLrModel = currentLrModel;
    }

    public static LrModel getCurrentLrModel() {
        return currentLrModel;
    }

    public static JapeFile getCurrentJapeFile() {
        return currentJapeFile;
    }

    public static String getCurrentJapeText() {
        return currentJapeText;
    }

    public static void setCurrentJapeText(String currentJapeText) {
        ResourcesFactory.currentJapeText = currentJapeText;
    }

    public static void setCurrentJapeFile(JapeFile currentJapeFile) {
        ResourcesFactory.currentJapeFile = currentJapeFile;
    }
}
