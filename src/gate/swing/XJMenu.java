/*
 *  Copyright (c) 1998-2003, The University of Sheffield.
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

import javax.swing.*;

/**
 * A modified version of JMenu that uses {@link MenuLayout} as its layout.
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

  public XJMenu(String s){
    super(s);
    getPopupMenu().setLayout(new MenuLayout());
  }

  public XJMenu(String s, boolean b){
    super(s, b);
    getPopupMenu().setLayout(new MenuLayout());
  }
}