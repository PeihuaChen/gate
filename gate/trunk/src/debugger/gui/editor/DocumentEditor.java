package debugger.gui.editor;

import debugger.JapeDebugger;
import debugger.gui.actions.editor.ShowResultAction;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Copyright (c) Ontos AG (http://www.ontosearch.com).
 * This class is part of JAPE Debugger component for
 * GATE (Copyright (c) "The University of Sheffield" see http://gate.ac.uk/) <br>
 * @author Oleg Mishenko, Andrey Shafirin
 */

public class DocumentEditor extends JComponent {
    private JTextPane textPane;
    private JButton showResultButton;

    public DocumentEditor() {
        initGui();
        initListeners();
    }

    public void setTextSelection(Color color, int startOffset, int endOffset) {
        MutableAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setBackground(attributeSet, new Color(210, 210, 210));
        ((DefaultStyledDocument) textPane.getDocument()).setCharacterAttributes(0, startOffset, attributeSet, false);

        attributeSet = new SimpleAttributeSet();
        StyleConstants.setBackground(attributeSet, Color.white);
        ((DefaultStyledDocument) textPane.getDocument()).setCharacterAttributes(endOffset, textPane.getDocument().getLength() - endOffset, attributeSet, false);

        attributeSet = new SimpleAttributeSet();
        StyleConstants.setBackground(attributeSet, color);
        ((DefaultStyledDocument) textPane.getDocument()).setCharacterAttributes(startOffset, endOffset - startOffset, attributeSet, false);
        try {
            textPane.scrollRectToVisible(textPane.modelToView(startOffset));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void setTextFont(String fontName, int size, int startOffset, int endOffset) {
        MutableAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attributeSet, fontName);
        StyleConstants.setFontSize(attributeSet, size);
        ((DefaultStyledDocument) textPane.getDocument()).setCharacterAttributes(startOffset, endOffset - startOffset, attributeSet, false);
    }

    public JTextPane getTextPane() {
        return textPane;
    }

    private void initGui() {
        textPane = new JTextPane();
        textPane.setEditable(false);
        //this prevents autoscrolling of the scrollbar
        textPane.setCaret(new DefaultCaret() {
            protected void adjustVisibility(Rectangle nloc) {
            }
        });

        this.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        ImageIcon icon = new ImageIcon(JapeDebugger.class.getResource("gui/icons/go.png"));
        icon = new ImageIcon(icon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        showResultButton = new JButton();
        showResultButton.setIcon(icon);
        showResultButton.setPreferredSize(new Dimension(20, 20));
        showResultButton.setEnabled(false);
        buttonPanel.add(showResultButton);
        this.add(buttonPanel, BorderLayout.WEST);
        this.add(new JScrollPane(textPane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
    }

    private void initListeners() {
        showResultButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ShowResultAction.getInstance().actionPerformed(textPane.getSelectionStart(), textPane.getSelectionEnd());
            }
        });
        textPane.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (textPane.getSelectedText() == null) {
                    showResultButton.setEnabled(false);
                } else {
                    showResultButton.setEnabled(true);
                }
            }
        });
        textPane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (textPane.getSelectedText() == null) {
                    showResultButton.setEnabled(false);
                } else {
                    showResultButton.setEnabled(true);
                }
            }

            public void mouseReleased(MouseEvent e) {
                mousePressed(e);
            }
        });
    }
}
