/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import gate.Gate;
import gate.GateConstants;
import gate.swing.JFontChooser;
import gate.util.GateRuntimeException;
import gate.util.OptionsMap;

/**
 * The options dialog for Gate.
 */
public class OptionsDialog extends JDialog {
  public OptionsDialog(Frame owner){
    super(owner, "GATE Options", true);
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
    Box vBox;
    Box hBox;

    /*******************
     * Appearance pane *
     *******************/

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

    JPanel appearanceBox = new JPanel();
    appearanceBox.setLayout(new BoxLayout(appearanceBox, BoxLayout.Y_AXIS));
    appearanceBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    appearanceBox.add(Box.createVerticalStrut(5));

    vBox = Box.createVerticalBox();
    vBox.setBackground(getContentPane().getBackground());
    vBox.setBorder(BorderFactory.createTitledBorder(" Look and Feel "));
    vBox.add(Box.createVerticalStrut(5));
      hBox = Box.createHorizontalBox();
      hBox.add(Box.createHorizontalStrut(5));
      hBox.add(lnfCombo);
      hBox.add(Box.createHorizontalStrut(5));
    vBox.add(hBox);
    vBox.add(Box.createVerticalStrut(5));
    appearanceBox.add(vBox);

    appearanceBox.add(Box.createVerticalStrut(5));

    hBox = Box.createHorizontalBox();
    hBox.setBorder(BorderFactory.createTitledBorder(" Font options "));
    hBox.add(Box.createHorizontalStrut(5));
      vBox = Box.createVerticalBox();
      vBox.add(textBtn);
      vBox.add(Box.createVerticalStrut(5));
      vBox.add(menuBtn);
      vBox.add(Box.createVerticalStrut(5));
      vBox.add(otherCompsBtn);
      vBox.add(Box.createVerticalStrut(5));
      vBox.add(Box.createVerticalGlue());
    hBox.add(Box.createHorizontalStrut(5));
    hBox.add(vBox);
    fontChooser = new JFontChooser();
    hBox.add(fontChooser);
    hBox.add(Box.createHorizontalStrut(5));

    appearanceBox.add(hBox);

    mainTabbedPane.add("Appearance", appearanceBox);

    /*****************
     * Advanced pane *
     *****************/

    saveOptionsChk = new JCheckBox(
        "Save options on exit",
        Gate.getUserConfig().getBoolean(GateConstants.SAVE_OPTIONS_ON_EXIT).
        booleanValue());

    saveSessionChk = new JCheckBox(
        "Save session on exit",
        Gate.getUserConfig().getBoolean(GateConstants.SAVE_SESSION_ON_EXIT).
        booleanValue());

    includeFeaturesOnPreserveFormatChk = new JCheckBox(
      "Include annotation features for \"Save preserving format\"",
      Gate.getUserConfig().
      getBoolean(GateConstants.SAVE_FEATURES_WHEN_PRESERVING_FORMAT).
      booleanValue());

    addSpaceOnMarkupUnpackChk = new JCheckBox(
      "Add space on markup unpack if needed",
      true);

    if ( (Gate.getUserConfig().
       get(GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME) != null)
      &&
      !Gate.getUserConfig().
        getBoolean(GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME).
          booleanValue()
      )
      addSpaceOnMarkupUnpackChk.setSelected(false);

    ButtonGroup bGroup = new ButtonGroup();
    doceditInsertAppendChk = new JCheckBox("Append (default)");
    bGroup.add(doceditInsertAppendChk);
    doceditInsertPrependChk = new JCheckBox("Prepend");
    bGroup.add(doceditInsertPrependChk);
    doceditInsertPrependChk.setSelected(Gate.getUserConfig().
        getBoolean(GateConstants.DOCEDIT_INSERT_PREPEND).booleanValue());
    doceditInsertAppendChk.setSelected(Gate.getUserConfig().
        getBoolean(GateConstants.DOCEDIT_INSERT_APPEND).booleanValue());
    //if none set then set the default one
    if(!(doceditInsertAppendChk.isSelected()||
         doceditInsertPrependChk.isSelected()))
      doceditInsertAppendChk.setSelected(true);

    browserComboBox = new JComboBox(new String[] {"Java", "Firefox",
        "Internet Explorer", "Safari", "Custom"});
    browserComboBox.setPrototypeDisplayValue("Internet Explorer");
    browserCommandLineTextField = new JTextField(15);
    String commandLine =
      Gate.getUserConfig().getString(GateConstants.HELP_BROWSER_COMMAND_LINE);
    if(commandLine == null || commandLine.trim().length() == 0) {
      browserComboBox.setSelectedItem("Java");
      browserCommandLineTextField.setEnabled(false);
    }
    else if(commandLine.contains("firefox")) {
      browserComboBox.setSelectedItem("Firefox");
      browserCommandLineTextField.setText(commandLine);
    }
    else if(commandLine.contains("iexplore")) {
      browserComboBox.setSelectedItem("Internet Explorer");
      browserCommandLineTextField.setText(commandLine);
    }
    else if(commandLine.contains("Safari")) {
      browserComboBox.setSelectedItem("Safari");
      browserCommandLineTextField.setText(commandLine);
    }
    else {
      browserComboBox.setSelectedItem("Custom");
      browserCommandLineTextField.setText(commandLine);
    }

    JPanel advancedBox =  new JPanel();
    advancedBox.setLayout(new BoxLayout(advancedBox, BoxLayout.Y_AXIS));
    advancedBox.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    advancedBox.add(Box.createVerticalStrut(5));

    hBox = Box.createHorizontalBox();
    hBox.setBorder(BorderFactory.createTitledBorder(" Advanced features "));
    hBox.add(Box.createHorizontalStrut(5));
      vBox = Box.createVerticalBox();
      vBox.add(includeFeaturesOnPreserveFormatChk);
      vBox.add(Box.createVerticalStrut(5));
      vBox.add(addSpaceOnMarkupUnpackChk);
      vBox.add(Box.createVerticalStrut(5));
    hBox.add(vBox);
    hBox.add(Box.createHorizontalStrut(5));
    hBox.add(Box.createHorizontalGlue());
    advancedBox.add(hBox);

    advancedBox.add(Box.createVerticalStrut(5));

    hBox = Box.createHorizontalBox();
    hBox.setBorder(BorderFactory.createTitledBorder(" Session persistence "));
    hBox.add(Box.createHorizontalStrut(5));
      vBox = Box.createVerticalBox();
      vBox.add(saveOptionsChk);
      vBox.add(Box.createVerticalStrut(5));
      vBox.add(saveSessionChk);
      vBox.add(Box.createVerticalStrut(5));
    hBox.add(vBox);
    hBox.add(Box.createHorizontalStrut(5));
    hBox.add(Box.createHorizontalGlue());
    advancedBox.add(hBox);

    advancedBox.add(Box.createVerticalStrut(5));

    hBox = Box.createHorizontalBox();
    hBox.setBorder(BorderFactory.createTitledBorder(
            " Document editor insert behaviour "));
    hBox.add(Box.createHorizontalStrut(5));
      vBox = Box.createVerticalBox();
      vBox.add(doceditInsertAppendChk);
      vBox.add(Box.createVerticalStrut(5));
      vBox.add(doceditInsertPrependChk);
      vBox.add(Box.createVerticalStrut(5));
    hBox.add(vBox);
    hBox.add(Box.createHorizontalStrut(5));
    hBox.add(Box.createHorizontalGlue());
    advancedBox.add(hBox);

    advancedBox.add(Box.createVerticalStrut(5));

    hBox = Box.createHorizontalBox();
    hBox.setBorder(BorderFactory.createTitledBorder(
      " Browser used to display help "));
    hBox.add(Box.createHorizontalStrut(5));
      vBox = Box.createVerticalBox();
      vBox.add(browserComboBox);
      vBox.add(Box.createVerticalStrut(5));
      vBox.add(browserCommandLineTextField);
      vBox.add(Box.createVerticalStrut(5));
    hBox.add(vBox);
    hBox.add(Box.createHorizontalStrut(5));
    advancedBox.add(hBox);

    mainTabbedPane.add("Advanced", advancedBox);

    /******************
     * Dialog buttons *
     ******************/

    Box buttonsBox = Box.createHorizontalBox();
    buttonsBox.add(okButton = new JButton(new OKAction()));
    buttonsBox.add(Box.createHorizontalStrut(10));
    buttonsBox.add(cancelButton = new JButton("Cancel"));

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
        setVisible(false);
      }
    });
    textBtn.setSelected(true);

    browserComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(browserComboBox.getSelectedItem() == null) {
          return;
        }
        String item = (String)browserComboBox.getSelectedItem();
        browserCommandLineTextField.setEnabled(!item.equals("Java"));
        if(item.equals("Java")) {
          browserCommandLineTextField.setText("");
        }
        else if(item.equals("Firefox")) {
          browserCommandLineTextField.setText("firefox %file");
        }
        else if(item.equals("Internet Explorer")) {
          browserCommandLineTextField.setText("@start /b iexplore.exe %file");
        }
        else if(item.equals("Safari")) {
          browserCommandLineTextField
                  .setText("open -a /Applications/Safari.app %file");
        }
      }
    });
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

  public void showDialog(){
    initLocalData();
    initGuiComponents();
    initListeners();
    textBtn.doClick();
    
    pack();
    setLocationRelativeTo(getOwner());
    setVisible(true);
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
        dialog.showDialog();
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
        }catch(Exception e){
          throw new GateRuntimeException(
                  "Error while setting the look and feel", e);
        }
        Iterator rootsIter = MainFrame.getGuiRoots().iterator();
        while(rootsIter.hasNext()){
          try{
            SwingUtilities.updateComponentTreeUI((Component)rootsIter.next());
          }catch(Exception e){
            throw new GateRuntimeException(
                    "Error while updating the graphical interface", e);
          }            
        }
      }

      userConfig.put(GateConstants.SAVE_OPTIONS_ON_EXIT,
                     new Boolean(saveOptionsChk.isSelected()));
      userConfig.put(GateConstants.SAVE_SESSION_ON_EXIT,
                     new Boolean(saveSessionChk.isSelected()));
      userConfig.put(GateConstants.SAVE_FEATURES_WHEN_PRESERVING_FORMAT,
                     new Boolean(includeFeaturesOnPreserveFormatChk.
                                 isSelected()));
      userConfig.put(GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME,
                     new Boolean(addSpaceOnMarkupUnpackChk.
                                 isSelected()));
      userConfig.put(GateConstants.DOCEDIT_INSERT_APPEND,
                     new Boolean(doceditInsertAppendChk.isSelected()));
      userConfig.put(GateConstants.DOCEDIT_INSERT_PREPEND,
                     new Boolean(doceditInsertPrependChk.isSelected()));
      userConfig.put(GateConstants.HELP_BROWSER_COMMAND_LINE,
                     browserCommandLineTextField.getText());
      setVisible(false);
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
                                          "TextField.font",
                                          "TitledBorder.font",
                                          "ToggleButton.font",
                                          "ToolBar.font",
                                          "ToolTip.font",
                                          "Tree.font",
                                          "Viewport.font"};

  public static String[] textComponentsKeys =
                             new String[]{"EditorPane.font",
                                          "TextArea.font",
                                          "TextPane.font"};

  /**
   * The main tabbed pane
   */
  protected JTabbedPane mainTabbedPane;

  /**
   * The OK button. The action for this button is an {@link OKAction}
   */
  protected JButton okButton;

  /**
   * The Cancel button: hides the dialog without doing anything
   */
  protected JButton cancelButton;

  /**
   * Radio button used to set the font for text components
   */
  protected JRadioButton textBtn;

  /**
   * which text is currently being edited; values are: "text", "menu", "other"
   */
  protected String selectedFontBtn = null;

  /**
   * Radio button used to set the font for menu components
   */
  protected JRadioButton menuBtn;

  /**
   * Radio button used to set the font for other components
   */
  protected JRadioButton otherCompsBtn;

  /**
   * Button group for the font setting radio buttons
   */
  protected ButtonGroup fontBG;

  /**
   * The font chooser used for selecting fonts
   */
  protected JFontChooser fontChooser;

  /**
   * The "Save Options on close" checkbox
   */
  protected JCheckBox saveOptionsChk;

  /**
   * The "Save Session on close" checkbox
   */
  protected JCheckBox saveSessionChk;

  /**
   * The "Include Annotation Features in Save Preserving Format" checkbox
   */
  protected JCheckBox includeFeaturesOnPreserveFormatChk;

  /**
   * The "Add extra space markup unpack if needed" checkbox
   */
  protected JCheckBox addSpaceOnMarkupUnpackChk;

  /** The Docedit append checkbox */
  protected JCheckBox doceditInsertAppendChk;

  /** The Docedit prepend checkbox */
  protected JCheckBox doceditInsertPrependChk;

  /**
   * The name of the look and feel class
   */
  protected String lookAndFeelClassName;

  /**
   * The font to be used for the menus; cached value for the one in the user
   * config map.
   */
  protected Font menusFont;

  /**
   * The font to be used for text components; cached value for the one in the
   * user config map.
   */
  protected Font textComponentsFont;

  /**
   * The font to be used for GUI components; cached value for the one in the
   * user config map.
   */
  protected Font componentsFont;

  /**
   * This flag becomes true when an GUI related option has been changed
   */
  protected boolean dirtyGUI;

  /**
   * The combobox for the look and feel selection
   */
  protected JComboBox lnfCombo;

  /**
   * List of browsers. Update the browserCommandLineTextField.
   */
  protected JComboBox browserComboBox;

  /**
   * Browser command line.
   */
  protected JTextField browserCommandLineTextField;
}
