package debugger.gui.debugging;

import debugger.gui.actions.debugging.ExecuteRHSAction;
import debugger.gui.actions.debugging.GoNextBreakpointAction;

import javax.swing.*;
import java.awt.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishenko
 * */
public class PhasePanel extends JComponent {
    private JToolBar toolBar;
    private JButton exeRHSButton;
    private JButton goToNextBreakButton;
    //private JButton runController;

    public PhasePanel() {
        initGui();
    }

    private void initGui() {
        toolBar = new JToolBar(JToolBar.HORIZONTAL);
        toolBar.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(1, 0, 1, 1);
        this.exeRHSButton = createActionButton(ExecuteRHSAction.getInstance(), null, null, false);
        toolBar.add(this.exeRHSButton, c);

        c.gridx = 1;
        c.insets = new Insets(1, 1, 1, 1);
        this.goToNextBreakButton = createActionButton(GoNextBreakpointAction.getInstance(), null, null, false);
        toolBar.add(goToNextBreakButton, c);

//        c.gridx = 2;
//        this.runController = createActionButton(RunControllerAction.getInstance(), null, null, false);
//        toolBar.add(runController, c);

        c.gridx = 3;
        c.weightx = 1;
        toolBar.add(new JPanel(), c);

        this.setLayout(new BorderLayout());
        this.add(toolBar, BorderLayout.CENTER);
    }


    /**
     * creates button for action. Tends to replace getButton method.
     * @param action button action
     * @param overrideIcon override button icon if needed
     * @param overrideToolTip override button tooltip if needed
     * @param isFocusPainted for traverse call to Button.setFocusPainted
     * @return button
     * */
    private JButton createActionButton(Action action, Icon overrideIcon, String overrideToolTip, boolean isFocusPainted) {
        JButton button = new JButton(action);
        Icon icon = (Icon) ((null != overrideIcon) ? overrideIcon : action.getValue(Action.SMALL_ICON));
        if (icon instanceof ImageIcon) {
            Image iconImage = ((ImageIcon) icon).getImage();
            Image scaledImage = iconImage.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            ImageIcon newIcon = new ImageIcon(scaledImage);
            button.setIcon(newIcon);
        } else {
            button.setIcon(icon);
        }
        if (null != overrideToolTip) button.setToolTipText(overrideToolTip);
        button.setFocusPainted(isFocusPainted);
        return button;
    }
}
