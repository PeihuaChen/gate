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

public class SortedTable extends JTable{

  // members area
  SortedTableModel m_model = null;

  // constructors
  public SortedTable(){}
  public void setTableModel(SortedTableModel model){
    m_model = model;
    setModel(model);
    InitHeader();
  }
  private void InitHeader(){
    JTableHeader header = getTableHeader();
    header.setUpdateTableInRealTime(true);
    header.addMouseListener(m_model.new ColumnListener(this));
    header.setReorderingAllowed(true);
  }
}

