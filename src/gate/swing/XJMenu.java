/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 20 Feb 2003
 *
 *  $Id$
 */

package gate.swing;

import gate.event.StatusListener;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;

/**
 * A modified version of JMenu that uses {@link MenuLayout} as its layout.
 * Adds also a description and a StatusListener as parameters.
 * The description is used in the statusListener and as a tooltip.
 */
public class XJMenu extends JMenu {
  public XJMenu(){
    super();
    getPopupMenu().setLayout(new MenuLayout());
  }

  public XJMenu(Action a){
    super(a);
    getPopupMenu().setLayout(new MenuLayout());
  }

  public XJMenu(Action a, StatusListener listener){
    super(a);
    this.description = (String)a.getValue(Action.SHORT_DESCRIPTION);
    this.listener = listener;
    initListeners();
    getPopupMenu().setLayout(new MenuLayout());
  }

  public XJMenu(String s){
    super(s);
    getPopupMenu().setLayout(new MenuLayout());
  }

  public XJMenu(String s, String description, StatusListener listener){
    super(s);
    this.description = description;
    this.listener = listener;
    initListeners();
    getPopupMenu().setLayout(new MenuLayout());
    setToolTipText(description);
  }

  public XJMenu(String s, boolean b){
    super(s, b);
    getPopupMenu().setLayout(new MenuLayout());
  }

  /**
   * Force separators to be the same width as the JPopupMenu.
   * This is because the MenuLayout make separators invisible contrary
   * to the default JMenu layout manager.
   * @param aFlag true if the popupmenu is visible
   */
  public void setPopupMenuVisible(boolean aFlag) {
    super.setPopupMenuVisible(aFlag);
    for (Component component : getMenuComponents()) {
      if (component instanceof JSeparator) {
        JSeparator separator = (JSeparator) component;
        // use the popupmenu width to set the separators width
        separator.setPreferredSize(new Dimension((int)getPopupMenu()
          .getLayout().preferredLayoutSize(getPopupMenu()).getWidth()
          - getPopupMenu().getInsets().left - getPopupMenu().getInsets().right,
          separator.getHeight()));
      }
    }
    getPopupMenu().revalidate();
  }

  protected void initListeners(){
    this.addMouseListener(new MouseAdapter() {
      public void mouseExited(MouseEvent e) {
        // clear the status
        listener.statusChanged("");
      }
    });
    this.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        // display the menu description in the status
        listener.statusChanged(description);
      }
    });
    this.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent e) {
        // do nothing
      }
      public void menuDeselected(MenuEvent e) {
        // clear the status
        listener.statusChanged("");
      }
      public void menuSelected(MenuEvent e) {
        // do nothing
      }
    });
  }

  private StatusListener listener;
  private String description;
}