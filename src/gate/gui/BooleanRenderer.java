/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 23/01/2001
 *
 *  $Id$
 *
 */
package gate.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A {@link javax.swing.table.TableCellRenderer} used for Booleans
 */
public class BooleanRenderer extends DefaultTableCellRenderer {
  public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int column){
    Component comp = super.getTableCellRendererComponent(table,
                                                         "",
                                                         isSelected, hasFocus,
                                                         row, column);
    if(value instanceof Boolean &&
       value != null &&
       ((Boolean)value).booleanValue()){
      setIcon(MainFrame.getIcon("tick.gif"));
//      setIcon(MainFrame.getIcon((isSelected) ? "tick_white.gif" : "tick.gif"));
    } else {
      setIcon(null);
    }

    return this;
  }//public Component getTableCellRendererComponent
}//class BooleanRenderer extends DefaultTableCellRenderer