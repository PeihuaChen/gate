/*
 *  SortedTableModel.java
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

/**
  * This abstract class is to be used when a Sorted table model is needed
  */
public abstract class SortedTableModel extends AbstractTableModel {

    /** Debug flag */
    private static final boolean DEBUG = false;

    // Members area
    // A list containing the data displayed into table
    protected List                  m_data;
    // A comparator for data on each column
    protected SortedTableComparator m_comparator;
    // The indice of the column on which the model is sorted
    protected int                   m_sortCol;
    // Sorting type (asc/desc)
    protected boolean               m_sortAsc;
    // How many rows will have this table model
    protected int                   m_rowCount;

    /**  The default constructor initializes the sorting column as being the
      *  first column and sorting type is set to be ascending
      */
    public SortedTableModel() {
      // Sort the first column ascending
      // this is default sorting
      m_sortCol = 0;
      m_sortAsc = true;
    }// SortedTableModel

    /** This method sets the data to be displayed and also sorts the table
      * model using the default values.
      */
    public void setData(Collection data, SortedTableComparator comparator) {
      m_data = new ArrayList();
      m_data.addAll(data);
      m_comparator = comparator;
      sortDefault();
      m_rowCount = m_data.size();
    }// setData

    /** This method sorts the table model data.*/
    private void sortDefault() {
      m_comparator.setSortCol(m_sortCol);
      m_comparator.setSortOrder(m_sortAsc);
      Collections.sort(m_data, m_comparator);
    }// sortDefault

    /** Implemented as required by the AbstaractTableModel*/
    public int getRowCount() {
      return m_rowCount;
    }// getRowCount

    /** This method adds a string to the column being sorted*/
    protected String addSortOrderString(int sortCol) {
      if (sortCol == m_sortCol)
        return "  (" + (m_sortAsc ? "Asc" : "Desc") + ")";
      return "";
    }// addSortOrderString

    /** Gets the objects with max length for each column*/
    public Object[] getMaxValues() {
      int colNumber = getColumnCount();
      Object[] maxValues = new Object[colNumber];

      for (int i = 0; i < colNumber; i++)
        maxValues[i] = getMaxValue(i);

      return maxValues;
    }// getMaxValues

    /** This method must be implemented by each TableSortedModel*/
    abstract public Object getMaxValue(int i);

    /** This class is used to capture the mouse event*/
    class ColumnListener extends MouseAdapter {
      // members area
      protected SortedTable m_table;

      /** Constructor takes a sorted table*/
      public ColumnListener(SortedTable jTable) {
        m_table = jTable;
      }// ColumnListener

      /**This method deals with a mouse click on a SortedTable header */
      public void mouseClicked(MouseEvent e) {

        // Find out what is the selected column index
        TableColumnModel colModel = m_table.getColumnModel();
        int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
        int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

        // If for some reason we get a negative index just simply return
        if (modelIndex < 0)  return;

        // If the column to be sorted is the same with model index then switch
        // the order
        if (m_sortCol == modelIndex) m_sortAsc = !m_sortAsc;
        else m_sortCol = modelIndex;

        // For all the columns set the new column names
        for (int i = 0; i < getColumnCount(); i++){
          TableColumn column = colModel.getColumn(i);
          column.setHeaderValue(getColumnName(column.getModelIndex()));
        }// End for

        m_table.getTableHeader().repaint();
        // Set the selected column inside the comparator
        m_comparator.setSortCol(m_sortCol);

        // Set the order inside comparator
        m_comparator.setSortOrder(m_sortAsc);

        // Sort the rows on selected column
        Collections.sort(m_data, m_comparator);

        // Notify the table that data has changed
        m_table.tableChanged(new TableModelEvent(SortedTableModel.this));

        // Redraw the entire table
        m_table.repaint();
      }// mouseClicked
    }// ColumnListener
} // SortedTableModel

