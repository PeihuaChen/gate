package debugger.gui.debugging;

import javax.swing.*;
import java.awt.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishenko
 * */

public class MainDebugPanel extends JPanel {
    private JTabbedPane tabbedPane;
    private JapeSourcePanel japeSourcePanel;
    private RulePanel rulePanel;
    private PhasePanel phasePanel;
    private TraceHistoryPanel traceHistoryPanel;

    public MainDebugPanel() {
        initGui();
    }

    private void initGui() {
        this.setLayout(new BorderLayout());
        this.add(getPhasePanel(), BorderLayout.NORTH);
        this.add(getTabbedPane(), BorderLayout.CENTER);
    }

    private JTabbedPane getTabbedPane() {
        if (tabbedPane == null) {
            tabbedPane = new JTabbedPane();
//long i = System.currentTimeMillis();
//System.out.println("Initializing Jape Rule...");
            tabbedPane.addTab("Trace History", getTraceHistoryPanel());
            tabbedPane.addTab("Jape Rule", getRulePanel());
//long j = System.currentTimeMillis();
//System.out.println("Time: " + (j - i));
//i = System.currentTimeMillis();
//System.out.println("Initializing Jape Source...");
            tabbedPane.addTab("Jape Source", getJapeSourcePanel());
//j = System.currentTimeMillis();
//System.out.println("Time: " + (j - i));
//i = System.currentTimeMillis();
//System.out.println("Initializing Jape Debugging...");
//j = System.currentTimeMillis();
//System.out.println("Time: " + (j - i));
        }
        return tabbedPane;
    }

    public JapeSourcePanel getJapeSourcePanel() {
        if (japeSourcePanel == null) {
            japeSourcePanel = new JapeSourcePanel();
        }
        return japeSourcePanel;
    }

    public RulePanel getRulePanel() {
        if (rulePanel == null) {
            rulePanel = new RulePanel();
        }
        return rulePanel;
    }

    public PhasePanel getPhasePanel() {
        if (phasePanel == null) {
            phasePanel = new PhasePanel();
        }
        return phasePanel;
    }

    public TraceHistoryPanel getTraceHistoryPanel() {
        if (traceHistoryPanel == null) {
            traceHistoryPanel = new TraceHistoryPanel();
        }
        return traceHistoryPanel;
    }

    public void selectJapeRulePanel() {
        tabbedPane.setSelectedComponent(getRulePanel());
    }

    public void selectTraceHisoryPanel() {
        tabbedPane.setSelectedComponent(getTraceHistoryPanel());
    }

    public void selectJapeSourcePanel() {
        tabbedPane.setSelectedComponent(getJapeSourcePanel());
    }
}