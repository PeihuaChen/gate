package debugger.gui.actions.debugging;

import debugger.resources.ResourcesFactory;
import debugger.JapeDebugger;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin
 */
public class ExecuteRHSAction extends AbstractAction {
    private static ExecuteRHSAction ourInstance;

    public synchronized static ExecuteRHSAction getInstance() {
        if (ourInstance == null) {
            ourInstance = new ExecuteRHSAction();
        }
        return ourInstance;
    }

    /**
     * Constructs action to continue execution SPT with particular image and tooltip.
     * */
    private ExecuteRHSAction() {
        super();
        putValue(Action.SHORT_DESCRIPTION, new String("Execute RHS"));
        putValue(Action.SMALL_ICON, new ImageIcon(JapeDebugger.class.getResource("gui/icons/exeRHS.png")));
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent aEvt) {
        setEnabled(false);
        ResourcesFactory.getPhaseController().setStopAfterRHSExec(true);
        ResourcesFactory.getPhaseController().continueSPT();
    }
}

