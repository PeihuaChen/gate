/*  XJTabbedPane.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 04/04/2001
 *
 *  $Id$
 *
 */

package gate.swing;

import javax.swing.JTabbedPane;

import java.awt.*;

/**
 * An extended version of {@link javax.swing.JTabbedPane}.
 */
public class XJTabbedPane extends JTabbedPane {

  public XJTabbedPane(int tabPlacement){
    super(tabPlacement);
  }

  /**
   * Gets the tab index for a given location
   */
  public int getIndexAt(Point p){
    for(int i = 0; i < getTabCount(); i++){
      if(getBoundsAt(i).contains(p)) return i;
    }
    return -1;
  }// int getIndexAt(Point p)
}// class XJTabbedPane extends JTabbedPane