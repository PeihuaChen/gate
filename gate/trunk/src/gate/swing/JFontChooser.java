/*  JFontChooser.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 06/04/2001
 *
 *  $Id$
 *
 */

package gate.swing;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.Dialog;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import javax.swing.plaf.FontUIResource;
import java.beans.*;

public class JFontChooser extends JPanel {

  public JFontChooser(){
    this(UIManager.getFont("Button.font"));
  }

  public JFontChooser(Font initialFont){
    initLocalData();
    initGuiComponents();
    initListeners();
    setFontValue(initialFont);
  }// public JFontChooser(Font initialFont)

  public static Font showDialog(Component parent, String title,
                                Font initialfont){

    Window windowParent;
    if(parent instanceof Window) windowParent = (Window)parent;
    else windowParent = SwingUtilities.getWindowAncestor(parent);
    if(windowParent == null) throw new IllegalArgumentException(
      "The supplied parent component has no window ancestor");
    final JDialog dialog;
    if(windowParent instanceof Frame) dialog = new JDialog((Frame)windowParent,
                                                           title, true);
    else dialog = new JDialog((Dialog)windowParent, title, true);

    dialog.getContentPane().setLayout(new BoxLayout(dialog.getContentPane(),
                                                    BoxLayout.Y_AXIS));

    final JFontChooser fontChooser = new JFontChooser(initialfont);
    dialog.getContentPane().add(fontChooser);

    JButton okBtn = new JButton("OK");
    JButton cancelBtn = new JButton("Cancel");
    JPanel buttonsBox = new JPanel();
    buttonsBox.setLayout(new BoxLayout(buttonsBox, BoxLayout.X_AXIS));
    buttonsBox.add(Box.createHorizontalGlue());
    buttonsBox.add(okBtn);
    buttonsBox.add(Box.createHorizontalStrut(30));
    buttonsBox.add(cancelBtn);
    buttonsBox.add(Box.createHorizontalGlue());
    dialog.getContentPane().add(buttonsBox);
    dialog.pack();
    fontChooser.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        dialog.pack();
      }
    });
    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.hide();
      }
    });

    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.hide();
        fontChooser.setFontValue(null);
      }
    });

    dialog.show();

    return fontChooser.getFontValue();
  }// showDialog

  protected void initLocalData() {
  }

  protected void initGuiComponents() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    familyCombo = new JComboBox(
                        GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getAvailableFontFamilyNames()
                      );
    familyCombo.setSelectedItem(UIManager.getFont("Label.font").getFamily());

    sizeCombo = new JComboBox(new String[]{"6", "8", "10", "12", "14", "16",
                                              "18", "20", "22", "24", "26"});
    sizeCombo.setSelectedItem(new Integer(
                        UIManager.getFont("Label.font").getSize()).toString());

    italicChk = new JCheckBox("<html><i>Italic</i></html>", false);
    boldChk = new JCheckBox("<html><i=b>Bold</b></html>", false);

    JPanel fontBox = new JPanel();
    fontBox.setLayout(new BoxLayout(fontBox, BoxLayout.X_AXIS));
    fontBox.add(familyCombo);
    fontBox.add(sizeCombo);
    fontBox.setBorder(BorderFactory.createTitledBorder("Font"));
    add(fontBox);
    add(Box.createVerticalStrut(10));

    JPanel effectsBox = new JPanel();
    effectsBox.setLayout(new BoxLayout(effectsBox, BoxLayout.X_AXIS));
    effectsBox.add(italicChk);
    effectsBox.add(boldChk);
    effectsBox.setBorder(BorderFactory.createTitledBorder("Effects"));
    add(effectsBox);
    add(Box.createVerticalStrut(10));

    sampleTextArea = new JTextArea("Type your sample here...");
    JPanel samplePanel = new JPanel();
    samplePanel.setLayout(new BoxLayout(samplePanel, BoxLayout.X_AXIS));
    //samplePanel.add(new JScrollPane(sampleTextArea));
    samplePanel.add(sampleTextArea);
    samplePanel.setBorder(BorderFactory.createTitledBorder("Sample"));
    add(samplePanel);
    add(Box.createVerticalStrut(10));
  }// initGuiComponents()

  protected void initListeners(){
    //listen for our own properties change events
    this.addPropertyChangeListener("fontValue",
                                   new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt){
        Font newFont = (Font)evt.getNewValue();
        if(newFont == null) return;
        familyCombo.setSelectedItem(newFont.getName());
        sizeCombo.setSelectedItem(Integer.toString(newFont.getSize()));
        boldChk.setSelected(newFont.isBold());
        italicChk.setSelected(newFont.isItalic());
        sampleTextArea.setFont(newFont);
      }
    });

    familyCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateFont();
      }
    });

    sizeCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateFont();
      }
    });

    boldChk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateFont();
      }
    });

    italicChk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateFont();
      }
    });
  }// initListeners()

  private void updateFont(){
    String family = (String)familyCombo.getSelectedItem();
    int size = Integer.parseInt((String)sizeCombo.getSelectedItem());
    int style = Font.PLAIN;
    if(boldChk.isSelected()) style = Font.BOLD;
    if(italicChk.isSelected()){
      if(style == Font.PLAIN) style = Font.ITALIC;
      else style |= Font.ITALIC;
    }
    Font newFont = new Font(family, style, size);
    setFontValue(newFont);
  }//updateFont()

  public static void main(String args[]){
    try{
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }catch(Exception e){
      e.printStackTrace();
    }
    final JFrame frame = new JFrame("Foo frame");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    JButton btn = new JButton("Show dialog");
    btn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.out.println(showDialog(frame, "Fonter",
                                      UIManager.getFont("Button.font")));
      }
    });
    frame.getContentPane().add(btn);
    frame.setSize(new Dimension(300, 300));
    frame.setVisible(true);
    System.out.println("Font: " + UIManager.getFont("Button.font"));
    showDialog(frame, "Fonter", UIManager.getFont("Button.font"));
  }// main

  public void setFontValue(java.awt.Font newFontValue) {
    java.awt.Font  oldFontValue = fontValue;
    fontValue = newFontValue;
    propertyChangeListeners.firePropertyChange(
                                      "fontValue", oldFontValue, newFontValue);
  }

  public java.awt.Font getFontValue() {
    return fontValue;
  }

  public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
    super.removePropertyChangeListener(l);
    propertyChangeListeners.removePropertyChangeListener(l);
  }

  public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
    super.addPropertyChangeListener(l);
    propertyChangeListeners.addPropertyChangeListener(l);
  }

  public synchronized void addPropertyChangeListener(String propertyName,
                                                     PropertyChangeListener l) {
    super.addPropertyChangeListener(propertyName, l);
    propertyChangeListeners.addPropertyChangeListener(propertyName, l);
  }

  JComboBox familyCombo;
  JCheckBox italicChk;
  JCheckBox boldChk;
  JComboBox sizeCombo;
  JTextArea sampleTextArea;
  private java.awt.Font fontValue;
  private transient PropertyChangeSupport propertyChangeListeners =
                    new PropertyChangeSupport(this);
}// class JFontChooser extends JPanel