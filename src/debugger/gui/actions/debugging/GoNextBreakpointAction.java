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
public class GoNextBreakpointAction extends AbstractAction {
    private static GoNextBreakpointAction ourInstance;

    public synchronized static GoNextBreakpointAction getInstance() {
        if (ourInstance == null) {
            ourInstance = new GoNextBreakpointAction();
        }
        return ourInstance;
    }

    private GoNextBreakpointAction() {
        super();
        putValue(Action.SHORT_DESCRIPTION, new String("Go to next breakpoint"));
        putValue(Action.SMALL_ICON, new ImageIcon(JapeDebugger.class.getResource("gui/icons/go.png")));
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent aEvt) {
        this.setEnabled(false);
        ResourcesFactory.getPhaseController().setStopAfterRHSExec(false);
        ResourcesFactory.getPhaseController().continueSPT();
    }
}

