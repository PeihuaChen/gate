package debugger.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * Main debugger frame.
 * @author Oleg Mishchenko, Andrey Shafirin
 */
public class MainFrame extends JFrame
{
    private JComponent mainPanel;

    public MainFrame()
    {
        super("Jape Debugger [build $build_number$] (Ontos AG)");
        initGui();
    }

    private void initGui()
    {
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(getMainPanel(), BorderLayout.CENTER);
    }

    public JComponent getMainPanel()
    {
        if(mainPanel == null)
        {
            mainPanel = new MainPanel();
        }
        return mainPanel;
    }
}
