package debugger.gui.actions.resources;

import gate.AnnotationSet;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.awt.*;

import debugger.resources.pr.RuleModel;
import debugger.resources.ResourcesFactory;
import debugger.gui.GuiFactory;

import javax.swing.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishenko
 * */

public class RuleSelectedAction  implements Runnable{
    private static RuleSelectedAction ourInstance;
    private RuleModel ruleModel;

    public synchronized static RuleSelectedAction getInstance() {
        if (ourInstance == null) {
            ourInstance = new RuleSelectedAction();
        }
        return ourInstance;
    }

    private RuleSelectedAction() {
    }

    public void actionPerformed(RuleModel ruleModel) {
        this.ruleModel = ruleModel;
        SwingUtilities.invokeLater(this);
    }

    private int getSlashNAmount(int offset, AnnotationSet as) {
        try {
            String temp = as.getDocument().getContent().getContent(new Long(0), new Long(offset)).toString();
            if (temp.equals("")) return 0;
            StringTokenizer st = new StringTokenizer(temp, "\n");
            if (temp.endsWith("\n")) {
                return st.countTokens();
            } else {
                return st.countTokens() - 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void run() {
        ResourcesFactory.setCurrentRuleModel(ruleModel);
//        DebugController.getInstance().getPrController().setCurrentPR((PRModelImpl) this.currentRuleModel.getParent().getParent());
        int startSelection = 0;
        int endSelection = 0;
        Iterator it = ruleModel.getBindings().keySet().iterator();
        while (it.hasNext()) {
            String s = (String) it.next();
            if (s.startsWith("(")) {
                AnnotationSet as = (AnnotationSet) ruleModel.getBindings().get(s);
                if (as != null) {
                    try {
                        startSelection = ((AnnotationSet) ruleModel.getBindings().get(s)).firstNode().getOffset().intValue();
                        endSelection = ((AnnotationSet) ruleModel.getBindings().get(s)).lastNode().getOffset().intValue();
//                        startSelection = startSelection - getSlashNAmount(startSelection, as);
//                        endSelection = endSelection - getSlashNAmount(endSelection, as);
                        GuiFactory.getDocumentEditor().setTextSelection(Color.cyan, startSelection, endSelection);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    GuiFactory.getDocumentEditor().setTextSelection(Color.cyan, 0, 0);
                }
            }
        }
        GuiFactory.getDebugPanel().getRulePanel().ruleSelected();
//        this.ruleView.ruleSelected();
        GuiFactory.getDebugPanel().getJapeSourcePanel().upgradeTextPane();
        GuiFactory.getDebugPanel().getTraceHistoryPanel().updateRulePanel(ruleModel, null);

//        getJapeSourceView().upgradeTextPane();
    }
}

