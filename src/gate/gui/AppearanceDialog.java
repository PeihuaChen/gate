/*  ApperanceDialog.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 12/04/2001
 *
 *  $Id$
 *
 */
package gate.gui;

import javax.swing.JDialog;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import gate.swing.*;

public class AppearanceDialog extends JDialog {

  public AppearanceDialog(Frame owner, String title, boolean modal,
                         Component[] targets) {
    super(owner, title, modal);
    this.targets = targets;
    init();
  }// ApperanceDialog

  public AppearanceDialog(Dialog owner, String title, boolean modal,
                         Component[] targets) {
    super(owner, title, modal);
    this.targets = targets;
    init();
  }//ApperanceDialog

  protected void init() {
    initLocalData();
    initGuiComponents();
    initListeners();
    bGroup.setSelected(menusRBtn.getModel(), true);
    cancelBtn.getAction().actionPerformed(null);
  }

  protected void initLocalData() {
    oldMenusFont = menusFont = UIManager.getFont("Menu.font");
    oldComponentsFont = componentsFont = UIManager.getFont("Button.font");
    oldTextComponentsFont = textComponentsFont =
                            UIManager.getFont("TextPane.font");
  }// initLocalData()

  protected void initGuiComponents() {
    getContentPane().setLayout(new BoxLayout(getContentPane(),
                                             BoxLayout.Y_AXIS));
    //add the radio buttons
    Box box = Box.createHorizontalBox();
    Box tempBox = Box.createVerticalBox();
    bGroup = new ButtonGroup();
    menusRBtn = new JRadioButton("Menus", false);
    menusRBtn.setActionCommand("menus");
    bGroup.add(menusRBtn);
    tempBox.add(menusRBtn);
    componentsRBtn = new JRadioButton("Components", false);
    componentsRBtn.setActionCommand("components");
    bGroup.add(componentsRBtn);
    tempBox.add(componentsRBtn);
    textComponentsRBtn = new JRadioButton("Text components", false);
    textComponentsRBtn.setActionCommand("text components");
    bGroup.add(textComponentsRBtn);
    tempBox.add(textComponentsRBtn);
    box.add(tempBox);
    box.add(Box.createHorizontalGlue());
    getContentPane().add(box);

    //add the font chooser
    fontChooser = new JFontChooser();
    getContentPane().add(fontChooser);

    //add the buttons
    box = Box.createHorizontalBox();
    okBtn = new JButton(new OKAction());
    box.add(okBtn);
    cancelBtn = new JButton(new CancelAction());
    box.add(cancelBtn);
    applyBtn = new JButton(new ApplyAction());
    box.add(applyBtn);
    getContentPane().add(box);

    setResizable(false);

  }// initGuiComponents()

  protected void initListeners() {
    fontChooser.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        pack();
     }
    });

    fontChooser.addPropertyChangeListener("fontValue",
                                          new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        String selectedButton = bGroup.getSelection().getActionCommand();
        if(selectedButton.equals("menus")){
          menusFont = fontChooser.getFontValue();
        } else if(selectedButton.equals("components")){
          componentsFont = fontChooser.getFontValue();
        } else if(selectedButton.equals("text components")){
          textComponentsFont = fontChooser.getFontValue();
        }
      }// propertyChange(PropertyChangeEvent e)
    });

    menusRBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fontChooser.setFontValue(menusFont);
      }// public void actionPerformed(ActionEvent e)
    });

    componentsRBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fontChooser.setFontValue(componentsFont);
      }// public void actionPerformed(ActionEvent e)
    });

    textComponentsRBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fontChooser.setFontValue(textComponentsFont);
      }// public void actionPerformed(ActionEvent e)
    });
  }// initListeners()

  public void show(Component[] targets) {
    this.targets = targets;
    oldMenusFont = menusFont = UIManager.getFont("Menu.font");
    oldComponentsFont = componentsFont = UIManager.getFont("Button.font");
    oldTextComponentsFont = textComponentsFont =
                            UIManager.getFont("TextPane.font");
    super.show();
  }// show(Component[] targets)


  protected void setUIDefaults(Object[] keys, Object value) {
    for(int i = 0; i < keys.length; i++){
      UIManager.put(keys[i], value);
    }
  }// setUIDefaults(Object[] keys, Object value)


  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e){
      e.printStackTrace();
    }

    JFrame frame = new JFrame("Foo frame");
    final AppearanceDialog apperanceDialog1 = new AppearanceDialog(frame,
                                                           "Font appearance",
                                                           true,
                                                           new Component[]{frame});
    apperanceDialog1.pack();

    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    JButton btn = new JButton("Show dialog");
    btn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        apperanceDialog1.show();
      }
    });

    frame.getContentPane().add(btn);
    frame.setSize(new Dimension(300, 300));
    frame.setVisible(true);
  }// public static void main(String[] args)

  JRadioButton menusRBtn;
  JRadioButton componentsRBtn;
  JRadioButton textComponentsRBtn;
  JFontChooser fontChooser;

  JButton okBtn;
  JButton applyBtn;
  JButton cancelBtn;
  ButtonGroup bGroup;

  Font menusFont;
  Font componentsFont;
  Font textComponentsFont;

  Font oldMenusFont;
  Font oldComponentsFont;
  Font oldTextComponentsFont;

  Component[] targets;

  public static String[] menuKeys = new String[]{"CheckBoxMenuItem.acceleratorFont",
                                          "CheckBoxMenuItem.font",
                                          "Menu.acceleratorFont",
                                          "Menu.font",
                                          "MenuBar.font",
                                          "MenuItem.acceleratorFont",
                                          "MenuItem.font",
                                          "RadioButtonMenuItem.acceleratorFont",
                                          "RadioButtonMenuItem.font"};

  public static String[] componentsKeys =
                             new String[]{"Button.font",
                                          "CheckBox.font",
                                          "ColorChooser.font",
                                          "ComboBox.font",
                                          "InternalFrame.titleFont",
                                          "Label.font",
                                          "List.font",
                                          "OptionPane.font",
                                          "Panel.font",
                                          "PasswordField.font",
                                          "PopupMenu.font",
                                          "ProgressBar.font",
                                          "RadioButton.font",
                                          "ScrollPane.font",
                                          "TabbedPane.font",
                                          "Table.font",
                                          "TableHeader.font",
                                          "TitledBorder.font",
                                          "ToggleButton.font",
                                          "ToolBar.font",
                                          "ToolTip.font",
                                          "Tree.font",
                                          "Viewport.font"};

  public static String[] textComponentsKeys =
                             new String[]{"EditorPane.font",
                                          "TextArea.font",
                                          "TextField.font",
                                          "TextPane.font"};

  class ApplyAction extends AbstractAction{
    ApplyAction(){
      super("Apply");
    }

    public void actionPerformed(ActionEvent evt) {
      setUIDefaults(menuKeys, new FontUIResource(menusFont));
      setUIDefaults(componentsKeys, new FontUIResource(componentsFont));
      setUIDefaults(textComponentsKeys, new FontUIResource(textComponentsFont));
      SwingUtilities.updateComponentTreeUI(AppearanceDialog.this);
      for(int i = 0; i< targets.length; i++){
        if(targets[i] instanceof Window) {
          SwingUtilities.updateComponentTreeUI(targets[i]);
        } else {
          SwingUtilities.updateComponentTreeUI(
            SwingUtilities.getRoot(targets[i])
          );
        }
      }
    }// void actionPerformed(ActionEvent evt)
  }

  class OKAction extends AbstractAction {
    OKAction(){
      super("OK");
    }

    public void actionPerformed(ActionEvent evt){
      applyBtn.getAction().actionPerformed(evt);
      hide();
    }
  }// class OKAction extends AbstractAction

  class CancelAction extends AbstractAction {
    CancelAction(){
      super("Cancel");
    }

    public void actionPerformed(ActionEvent evt){
      setUIDefaults(menuKeys, new FontUIResource(oldMenusFont));
      setUIDefaults(componentsKeys, new FontUIResource(oldComponentsFont));
      setUIDefaults(textComponentsKeys, new FontUIResource(oldTextComponentsFont));
      SwingUtilities.updateComponentTreeUI(
                                  SwingUtilities.getRoot(AppearanceDialog.this));
      for(int i = 0; i< targets.length; i++){
        if(targets[i] instanceof Window){
          SwingUtilities.updateComponentTreeUI(targets[i]);
        } else {
          SwingUtilities.updateComponentTreeUI(
            SwingUtilities.getRoot(targets[i])
          );
        }
      }
      hide();
    }// void actionPerformed(ActionEvent evt)
  }// class CancelAction extends AbstractAction

}// class ApperanceDialog