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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Frame;
import java.awt.Font;
import java.awt.font.TextAttribute;
import javax.swing.plaf.FontUIResource;
import java.util.*;

/**
 * The options dialog for Gate.
 */
public class OptionsDialog extends JDialog {
  public OptionsDialog(Frame owner){
    super(owner, "Gate Options", true);
  }

  protected void initLocalData(){
  }

  protected void initGuiComponents(){
  }

  protected void initListeners(){
  }




  protected static void setUIDefaults(Object[] keys, Object value) {
    for(int i = 0; i < keys.length; i++){
      UIManager.put(keys[i], value);
    }
  }// setUIDefaults(Object[] keys, Object value)

  /**
   * Updates the Swing defaults table with the provided font to be used for the
   * text components
   */
  public static void setTextComponentsFont(Font textComponentsFont){
    setUIDefaults(textComponentsKeys, new FontUIResource(textComponentsFont));
    Gate.getUserConfig().put(GateConstants.TEXT_COMPONENTS_FONT,
                             textComponentsFont);
  }

  /**
   * Updates the Swing defaults table with the provided font to be used for the
   * menu components
   */
  public static void setMenuComponentsFont(Font menuComponentsFont){
    setUIDefaults(menuKeys, new FontUIResource(menuComponentsFont));
    Gate.getUserConfig().put(GateConstants.MENUS_FONT,
                             menuComponentsFont);
  }

  /**
   * Updates the Swing defaults table with the provided font to be used for
   * various compoents that neither text or menu components
   */
  public static void setComponentsFont(Font componentsFont){
    setUIDefaults(componentsKeys, new FontUIResource(componentsFont));
    Gate.getUserConfig().put(GateConstants.OTHER_COMPONENTS_FONT,
                             componentsFont);
  }

  class OKAction extends AbstractAction{
    OKAction(){
      super("OK");
    }

    public void actionPerformed(ActionEvent evt) {
      if(dirtyGUI){
        setMenuComponentsFont(menusFont);
        setComponentsFont(componentsFont);
        setTextComponentsFont(textComponentsFont);
      }

//      SwingUtilities.updateComponentTreeUI(AppearanceDialog.this);
//      for(int i = 0; i< targets.length; i++){
//        if(targets[i] instanceof Window) {
//          SwingUtilities.updateComponentTreeUI(targets[i]);
//        } else {
//          SwingUtilities.updateComponentTreeUI(
//            SwingUtilities.getRoot(targets[i])
//          );
//        }
//      }
    }// void actionPerformed(ActionEvent evt)
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
}