/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 15/11/2001
 *
 *  $Id$
 *
 */
package gate.gui;

import gate.*;
import gate.util.*;
import gate.swing.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Frame;
import java.awt.Font;
import java.awt.Component;
import java.awt.font.TextAttribute;
import javax.swing.plaf.FontUIResource;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;

/**
 * The options dialog for Gate.
 */
public class OptionsDialog extends JDialog {
  public OptionsDialog(Frame owner){
    super(owner, "Gate Options", true);
    MainFrame.getGuiRoots().add(this);
  }

  protected void initLocalData(){
    lookAndFeelClassName = Gate.getUserConfig().
                           getString(GateConstants.LOOK_AND_FEEL);

    textComponentsFont = Gate.getUserConfig().
                         getFont(GateConstants.TEXT_COMPONENTS_FONT);

    menusFont = Gate.getUserConfig().
                getFont(GateConstants.MENUS_FONT);

    componentsFont = Gate.getUserConfig().
                     getFont(GateConstants.OTHER_COMPONENTS_FONT);
    dirtyGUI = false;
  }


  protected void initGuiComponents(){
    getContentPane().removeAll();
    mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
    getContentPane().setLayout(new BoxLayout(getContentPane(),
                                             BoxLayout.Y_AXIS));
    getContentPane().add(mainTabbedPane);

    Box appearanceBox = Box.createVerticalBox();
    //the LNF combo
    List supportedLNFs = new ArrayList();
    LNFData currentLNF = null;
    UIManager.LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
    for(int i = 0; i < lnfs.length; i++){
      UIManager.LookAndFeelInfo lnf = lnfs[i];
      try{
        Class lnfClass = Class.forName(lnf.getClassName());
        if(((LookAndFeel)(lnfClass.newInstance())).isSupportedLookAndFeel()){
          if(lnf.getName().equals(UIManager.getLookAndFeel().getName())){
            supportedLNFs.add(currentLNF =
                              new LNFData(lnf.getClassName(), lnf.getName()));
          }else{
            supportedLNFs.add(new LNFData(lnf.getClassName(), lnf.getName()));
          }
        }
      }catch(ClassNotFoundException cnfe){
      }catch(IllegalAccessException iae){
      }catch(InstantiationException ie){
      }
    }
    lnfCombo = new JComboBox(supportedLNFs.toArray());
    lnfCombo.setSelectedItem(currentLNF);

    Box horBox = Box.createHorizontalBox();
    horBox.add(Box.createHorizontalStrut(5));
    horBox.add(new JLabel("Look and feel:"));
    horBox.add(Box.createHorizontalStrut(5));
    horBox.add(lnfCombo);
    horBox.add(Box.createHorizontalStrut(5));
    appearanceBox.add(Box.createVerticalStrut(10));
    appearanceBox.add(horBox);
    appearanceBox.add(Box.createVerticalStrut(10));

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder(" Font options "));

    fontBG = new ButtonGroup();
    textBtn = new JRadioButton("Text components font");
    textBtn.setActionCommand("text");
    fontBG.add(textBtn);
    menuBtn = new JRadioButton("Menu components font");
    menuBtn.setActionCommand("menu");
    fontBG.add(menuBtn);
    otherCompsBtn = new JRadioButton("Other components font");
    otherCompsBtn.setActionCommand("other");
    fontBG.add(otherCompsBtn);
    Box verBox = Box.createVerticalBox();
    verBox.add(Box.createVerticalStrut(5));
    verBox.add(textBtn);
    verBox.add(Box.createVerticalStrut(5));
    verBox.add(menuBtn);
    verBox.add(Box.createVerticalStrut(5));
    verBox.add(otherCompsBtn);
    verBox.add(Box.createVerticalStrut(5));
    verBox.add(Box.createVerticalGlue());
    panel.add(verBox);

    fontChooser = new JFontChooser();
    panel.add(fontChooser);

    appearanceBox.add(panel);

    mainTabbedPane.add("Appearance", appearanceBox);

    Box advancedBox = Box.createVerticalBox();
    saveOptionsChk = new JCheckBox(
        "Save options on exit",
        Gate.getUserConfig().getBoolean(GateConstants.SAVE_OPTIONS_ON_EXIT).
        booleanValue());

    saveSessionChk = new JCheckBox(
        "Save session on exit",
        Gate.getUserConfig().getBoolean(GateConstants.SAVE_SESSION_ON_EXIT).
        booleanValue());
    advancedBox.add(Box.createVerticalStrut(10));
    advancedBox.add(saveOptionsChk);
    advancedBox.add(Box.createVerticalStrut(10));
    advancedBox.add(saveSessionChk);
    advancedBox.add(Box.createVerticalStrut(10));
    mainTabbedPane.add("Advanced", advancedBox);

    Box buttonsBox = Box.createHorizontalBox();
    buttonsBox.add(Box.createHorizontalGlue());
    buttonsBox.add(okButton = new JButton(new OKAction()));
    buttonsBox.add(Box.createHorizontalStrut(10));
    buttonsBox.add(cancelButton = new JButton("Cancel"));
    buttonsBox.add(Box.createHorizontalGlue());

    getContentPane().add(Box.createVerticalStrut(10));
    getContentPane().add(buttonsBox);
    getContentPane().add(Box.createVerticalStrut(10));
  }

  protected void initListeners(){
    lnfCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(!lookAndFeelClassName.equals(
           ((LNFData)lnfCombo.getSelectedItem()).className)
          ){
          dirtyGUI = true;
          lookAndFeelClassName = ((LNFData)lnfCombo.getSelectedItem()).
                                 className;
        }
      }
    });

    fontChooser.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        if(e.getPropertyName().equals("fontValue")){
          String selectedFont = fontBG.getSelection().getActionCommand();
          if(selectedFont.equals("text")){
            textComponentsFont = (Font)e.getNewValue();
            dirtyGUI = true;
          }else if(selectedFont.equals("menu")){
            menusFont = (Font)e.getNewValue();
            dirtyGUI = true;
          }else if(selectedFont.equals("other")){
            componentsFont = (Font)e.getNewValue();
            dirtyGUI = true;
          }
        }
      }
    });

    textBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(textBtn.isSelected()) selectedFontChanged();
        selectedFontBtn = "text";
        fontChooser.setFontValue(textComponentsFont);
      }
    });

    menuBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(menuBtn.isSelected()) selectedFontChanged();
        selectedFontBtn = "menu";
        fontChooser.setFontValue(menusFont);
      }
    });

    otherCompsBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(otherCompsBtn.isSelected()) selectedFontChanged();
        selectedFontBtn = "other";
        fontChooser.setFontValue(componentsFont);
      }
    });

    cancelButton.setAction(new AbstractAction("Cancel"){
      public void actionPerformed(ActionEvent evt){
        hide();
      }
    });
    textBtn.setSelected(true);
  }

  public void dispose(){
    MainFrame.getGuiRoots().remove(this);
    super.dispose();
  }

  protected void selectedFontChanged(){
    if(selectedFontBtn != null){
      //save the old font
      if(selectedFontBtn.equals("text")){
        textComponentsFont = fontChooser.getFontValue();
      }else if(selectedFontBtn.equals("menu")){
        menusFont = fontChooser.getFontValue();
      }else if(selectedFontBtn.equals("other")){
        componentsFont = fontChooser.getFontValue();
      }
    }
  }

  public void show(){
    initLocalData();
    initGuiComponents();
    textBtn.setSelected(true);
    fontChooser.setFontValue(textComponentsFont);
    initListeners();
    pack();
    setLocationRelativeTo(getOwner());
    super.show();
  }

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
        OptionsDialog dialog = new OptionsDialog(frame);
        dialog.pack();
        dialog.show();
      }
    });
    frame.getContentPane().add(btn);
    frame.pack();
    frame.setVisible(true);
    System.out.println("Font: " + UIManager.getFont("Button.font"));
  }// main


  protected static void setUIDefaults(Object[] keys, Object value) {
    for(int i = 0; i < keys.length; i++){
      UIManager.put(keys[i], value);
    }
  }// setUIDefaults(Object[] keys, Object value)

  /**
   * Updates the Swing defaults table with the provided font to be used for the
   * text components
   */
  public static void setTextComponentsFont(Font font){
    setUIDefaults(textComponentsKeys, new FontUIResource(font));
    Gate.getUserConfig().put(GateConstants.TEXT_COMPONENTS_FONT, font);
  }

  /**
   * Updates the Swing defaults table with the provided font to be used for the
   * menu components
   */
  public static void setMenuComponentsFont(Font font){
    setUIDefaults(menuKeys, new FontUIResource(font));
    Gate.getUserConfig().put(GateConstants.MENUS_FONT, font);
  }

  /**
   * Updates the Swing defaults table with the provided font to be used for
   * various compoents that neither text or menu components
   */
  public static void setComponentsFont(Font font){
    setUIDefaults(componentsKeys, new FontUIResource(font));
    Gate.getUserConfig().put(GateConstants.OTHER_COMPONENTS_FONT, font);
  }

  class OKAction extends AbstractAction{
    OKAction(){
      super("OK");
    }

    public void actionPerformed(ActionEvent evt) {
      OptionsMap userConfig = Gate.getUserConfig();
      if(dirtyGUI){
        setMenuComponentsFont(menusFont);
        setComponentsFont(componentsFont);
        setTextComponentsFont(textComponentsFont);
        userConfig.put(GateConstants.LOOK_AND_FEEL, lookAndFeelClassName);
        try{
          UIManager.setLookAndFeel(lookAndFeelClassName);
          Iterator rootsIter = MainFrame.getGuiRoots().iterator();
          while(rootsIter.hasNext()){
            SwingUtilities.updateComponentTreeUI((Component)rootsIter.next());
          }
        }catch(Exception e){}
      }

      userConfig.put(GateConstants.SAVE_OPTIONS_ON_EXIT,
                     new Boolean(saveOptionsChk.isSelected()));
      userConfig.put(GateConstants.SAVE_SESSION_ON_EXIT,
                     new Boolean(saveSessionChk.isSelected()));
      hide();
    }// void actionPerformed(ActionEvent evt)
  }

  protected static class LNFData{
    public LNFData(String className, String name){
      this.className = className;
      this.name = name;
    }

    public String toString(){
      return name;
    }

    String className;
    String name;
  }


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

  /**
   * The main tabbed pane
   */
  JTabbedPane mainTabbedPane;

  /**
   * The OK button. The action for this button is an {@link OKAction}
   */
  JButton okButton;

  /**
   * The Cancel button: hides the dialog without doing anything
   */
  JButton cancelButton;

  /**
   * Radio button used to set the font for text components
   */
  JRadioButton textBtn;

  /**
   * which text is currently being edited; values are: "text", "menu", "other"
   */
  String selectedFontBtn = null;

  /**
   * Radio button used to set the font for menu components
   */
  JRadioButton menuBtn;

  /**
   * Radio button used to set the font for other components
   */
  JRadioButton otherCompsBtn;

  /**
   * Button group for the font setting radio buttons
   */
  ButtonGroup fontBG;

  /**
   * The font chooser used for selecting fonts
   */
  JFontChooser fontChooser;

  /**
   * The "Save Options on close" checkbox
   */
  JCheckBox saveOptionsChk;

  /**
   * The "Save Session on close" checkbox
   */
  JCheckBox saveSessionChk;
  /**
   * The name of the look and feel class
   */
  String lookAndFeelClassName;

  /**
   * The font to be used for the menus; cached value for the one in the user
   * config map.
   */
  Font menusFont;

  /**
   * The font to be used for text components; cached value for the one in the
   * user config map.
   */
  Font textComponentsFont;

  /**
   * The font to be used for GUI components; cached value for the one in the
   * user config map.
   */
  Font componentsFont;

  /**
   * This flag becomes true when an GUI related option has been changed
   */
  boolean dirtyGUI;

  /**
   * The combobox for the look and feel selection
   */
  JComboBox lnfCombo;
}