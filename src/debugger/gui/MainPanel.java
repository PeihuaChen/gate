package debugger.gui;

import debugger.gui.resources.CreoleListenerImpl;
import debugger.resources.ResourcesFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishenko, Vladimir Karasev
 */

public class MainPanel extends JComponent {
    public MainPanel() {
        initGui();
    }

    private void initGui() {
        JSplitPane rightLeftSplitPane;
        JSplitPane upperLowerSplitPane;
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(4, 4, 4, 4);

        JPanel panel = new JPanel(new BorderLayout());
        JButton refreshButton = new JButton("Refresh resources");
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ResourcesFactory.updateRoots();
                GuiFactory.getResourceView().refresh();
            }
        });
        panel.add(refreshButton, BorderLayout.NORTH);
        panel.add(GuiFactory.getResourceView(), BorderLayout.CENTER);

        rightLeftSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, GuiFactory.getDebugPanel());
//        rightLeftSplitPane.setDividerSize(4);
        rightLeftSplitPane.setDividerLocation(250);
        this.add(rightLeftSplitPane, c);

        upperLowerSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightLeftSplitPane, GuiFactory.getDocumentEditor());
        upperLowerSplitPane.setDividerLocation(500);
//        upperLowerSplitPane.setDividerSize(4);
        this.add(upperLowerSplitPane, c);
    }
}
