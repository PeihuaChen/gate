/*
 *	SortedTableModel.java
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
 *	Cristian URSU,  29/June/2000
 *
 *	$Id$
 */
package gate.gui;

import javax.swing.table.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;

public abstract class SortedTableModel extends AbstractTableModel{
    /**
      *  This field is "final static" because it brings in
      *  the advantage of dead code elimination
      *  When DEBUG is set on false the code that it guardes will be eliminated
      *  by the compiler. This will spead up the progam a little bit.
      */
    private static final boolean DEBUG = false;

    // members area
    protected List                  m_data;
    protected SortedTableComparator m_comparator;
    protected int                   m_sortCol;
    protected boolean               m_sortAsc;
    protected int                   m_rowCount;

    public SortedTableModel(){
      // sort the first column ascending
      // this is default sorting
      m_sortCol = 0;
      m_sortAsc = true;
    }

    public void setData(Collection data, SortedTableComparator comparator){
      m_data = new ArrayList();
      m_data.addAll(data);
      m_comparator = comparator;
      sortDefault();
      m_rowCount = m_data.size();
    }

    private void sortDefault(){
      m_comparator.setSortCol(m_sortCol);
      m_comparator.setSortOrder(m_sortAsc);
      Collections.sort(m_data, m_comparator);
    }

    public int getRowCount(){
      return m_rowCount;
    }

    protected String addSortOrderString(int sortCol){
      if (sortCol == m_sortCol) return "  (" + (m_sortAsc ? "Asc" : "Desc") + ")";
      return "";
    }

    class ColumnListener extends MouseAdapter {
      // members area
      protected SortedTable m_table;

      public ColumnListener(SortedTable jTable){
        m_table = jTable;
      }

      public void mouseClicked(MouseEvent e){
        // find out what is the selected column index
        TableColumnModel colModel = m_table.getColumnModel();
        int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
        int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

        // if for some reason we get a negative index just simply return
        if (modelIndex < 0)  return;
        // if the column to be sorted is the same with model index then switch the order
        if (m_sortCol == modelIndex) m_sortAsc = !m_sortAsc;
        else m_sortCol = modelIndex;
        // for all the columns set the new column names
        for (int i = 0; i < getColumnCount(); i++){
          TableColumn column = colModel.getColumn(i);
          column.setHeaderValue(getColumnName(column.getModelIndex()));
        }
        m_table.getTableHeader().repaint();
        // set the selected column inside the comparator
        m_comparator.setSortCol(m_sortCol);
        // set the order inside comparator
        m_comparator.setSortOrder(m_sortAsc);
        // sort the rows on selected column
        Collections.sort(m_data, m_comparator);
        // notify the table that data has changed
        m_table.tableChanged(new TableModelEvent(SortedTableModel.this));
        // redraw the entire table
        m_table.repaint();
      }
    }
    
    public Object[] getMaxValues(){
      int colNumber = getColumnCount();
      Object[] maxValues = new Object[colNumber];

      for (int i = 0; i < colNumber; i++)
        maxValues[i] = getMaxValue(i);

      return maxValues;
    }

    abstract public Object getMaxValue(int i);

}

