/*
 *	SortedTable.java
 *
 *	Cristian URSU,  30/June/2000
 *
 *	$Id$
 */
package gate.gui;

import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;

public class SortedTable extends JTable{

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
            System.out.println("Initializing width of column "
                                   + i + ". "
                                   + "headerWidth = " + headerWidth
                                   + "; cellWidth = " + cellWidth);
            */
            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            int width = Math.max(headerWidth, cellWidth) + 2;
            if ((column.getMaxWidth() < (width + 1)) && ((width + 1) > 0))
              column.setMaxWidth(width + 1);
            column.setPreferredWidth(width);
        }
  }
}

