package debugger.gui.debugging.debugviews;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Andrey Shafirin, Oleg Mishenko
 */

class InfoWindow extends JWindow {
    private PrimaryTextPanel panel;

    public InfoWindow(PrimaryTextPanel panel, JFrame owner) {
        super(owner);
        this.panel = panel;
        initialize();
    }

    public void initialize() {
        this.getContentPane().setLayout(new BorderLayout());
        JTextPane textPane = new JTextPane();
        textPane.setBackground(new Color(255, 255, 170));
        String text = "";
        for (Iterator it = panel.getAnnotations().iterator(); it.hasNext();) {
            text = text + it.next() + "\n";
        }
        textPane.setText(text);
        textPane.setEditable(false);
        textPane.setBorder(BorderFactory.createEtchedBorder());
        this.getContentPane().add(new JScrollPane(textPane));
        textPane.setRequestFocusEnabled(true);
        textPane.requestFocus();
        textPane.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                InfoWindow.this.setVisible(false);
            }
        });
        textPane.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                InfoWindow.this.setVisible(false);
            }
        });
        this.setSize(400, 200);
    }
}