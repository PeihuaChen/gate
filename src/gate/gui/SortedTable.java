/*
 *	SortedTable.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *  
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *  
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *  
 *	Cristian URSU,  30/June/2000
 *
 *	$Id$
 */
package gate.gui;

import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;

import gate.util.*;

public class SortedTable extends JTable{
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  // members area
  SortedTableModel m_model = null;

  // constructors
  public SortedTable(){}
  public void setTableModel(SortedTableModel model){
    m_model = model;
    setModel(model);
    InitHeader();
    initColumnSizes(JTable.AUTO_RESIZE_OFF);
  }
  private void InitHeader(){
    JTableHeader header = getTableHeader();
    header.setUpdateTableInRealTime(true);
    header.addMouseListener(m_model.new ColumnListener(this));
    header.setReorderingAllowed(true);
  }

  private void initColumnSizes(int autoResize){
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longValues = m_model.getMaxValues();

        this.setAutoResizeMode(autoResize);
        for (int i = 0; i < m_model.getColumnCount(); i++) {
            column = this.getColumnModel().getColumn(i);
            /*
            comp = column.getCellRenderer().
                             getTableCellRendererComponent(
                                 null, column.getHeaderValue(),
                                 false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;
            */
            //*
            comp = this.getDefaultRenderer(m_model.getColumnClass(i)).
                             getTableCellRendererComponent(
                                 this, longValues[i],
                                 false, false, 0, i);

            cellWidth = comp.getPreferredSize().width;
            //*/
            /*
            Out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            */
            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            int width = Math.max(40, cellWidth) + 10;
            if ((column.getMaxWidth() < (width + 1)) && ((width + 1) > 0))
              column.setMaxWidth(width + 1);
            column.setPreferredWidth(width);
        }
  }

}
