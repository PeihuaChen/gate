package debugger.gui.actions.resources;

import debugger.gui.GuiFactory;
import debugger.resources.ResourcesFactory;
import debugger.resources.lr.LrModel;

import javax.swing.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Oleg Mishenko
 * */
public class LrResourceSelectedAction implements Runnable{
    private static LrResourceSelectedAction ourInstance;
    private LrModel lrModel;

    public synchronized static LrResourceSelectedAction getInstance() {
        if (ourInstance == null) {
            ourInstance = new LrResourceSelectedAction();
        }
        return ourInstance;
    }

    private LrResourceSelectedAction() {
    }

    public void actionPerformed(LrModel lrModel) {
        this.lrModel = lrModel;
        SwingUtilities.invokeLater(this);
    }
    public void run()
    {
        ResourcesFactory.setCurrentLrModel(lrModel);

        GuiFactory.getDocumentEditor().getTextPane().setText(
                (lrModel == null ? "" : lrModel.getText()));
        //set border with document name
        GuiFactory.getDocumentEditor().setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                (lrModel == null ? "No document selected" : lrModel.getName())));
        GuiFactory.getDocumentEditor().revalidate();

    }
}

