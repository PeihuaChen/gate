/*  XJMenuItem.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 02/04/2001
 *
 *  $Id$
 *
 */

package gate.swing;

import javax.swing.JMenuItem;
import javax.swing.*;
import java.awt.event.*;

import gate.event.*;

public class XJMenuItem extends JMenuItem {

  public XJMenuItem(Icon icon, String description, StatusListener listener){
    super(icon);
    this.description = description;
    this.listener = listener;
    initListeners();
  }// public XJMenuItem(Icon icon, String description, StatusListener listener)

  public XJMenuItem(String text, String description, StatusListener listener){
    super(text);
    this.description = description;
    this.listener = listener;
    initListeners();
  }// XJMenuItem(String text, String description, StatusListener listener)

  public XJMenuItem(Action a, StatusListener listener){
    super(a);
    this.description = (String)a.getValue(a.SHORT_DESCRIPTION);
    this.listener = listener;
    initListeners();
  }// XJMenuItem(Action a, StatusListener listener)

  public XJMenuItem(String text, Icon icon,
                    String description, StatusListener listener){
    super(text, icon);
    this.description = description;
    this.listener = listener;
    initListeners();
  }// XJMenuItem

  public XJMenuItem(String text, int mnemonic,
                    String description, StatusListener listener){
    super(text, mnemonic);
    this.description = description;
    this.listener = listener;
    initListeners();
  }

  protected void initListeners(){
    this.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
        listener.statusChanged(description);
      }

      public void mouseExited(MouseEvent e) {
        listener.statusChanged("");
      }
    });
  }// void initListeners()

  private StatusListener listener;
  String description;
}// class XJMenuItem extends JMenuItem