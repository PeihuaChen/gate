/*
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

package gate.gui;

import java.awt.Component;
import javax.swing.table.*;
import javax.swing.*;
import javax.swing.border.*;

public class ObjectRenderer extends DefaultTableCellRenderer{
  public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column){
      //prepare the renderer
      super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                                          row, column);

      if(table.isCellEditable(row, column))
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      return this;
  }//public Component getTableCellRendererComponent

    JPanel textButtonBox;
}//class ObjectRenderer extends DefaultTableCellRenderer
