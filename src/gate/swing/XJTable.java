/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  XXJTable.java
 *
 *  Valentin Tablan, 25-Jun-2004
 *
 *  $Id$
 */

package gate.swing;

import java.awt.Dimension;
import java.awt.event.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * A &quot;smarter&quot; JTable. Feaures include:
 * <ul>
 * <li>sorting the table using the values from a column as keys</li>
 * <li>updating the widths of the columns so they accommodate the contents to
 * their preferred sizes.
 * </ul>
 * It uses a custom made model that stands between the table model set by the
 * user and the gui component. This middle model is responsible for sorting the
 * rows.
 */
public class XJTable extends JTable{
  
  public XJTable(){
    super();
  }
  
  public XJTable(TableModel model){
    super(model);
  }

  /**
   * Overrides some of the defaults as defined in JTable
   */
  protected void initializeLocalVars() {
    super.initializeLocalVars();
    setAutoResizeMode(AUTO_RESIZE_OFF);
  }
  
  public void setModel(TableModel dataModel) {
    sortingModel = new SortingModel(dataModel);
    super.setModel(sortingModel);
    newColumns();
  }
  
  /**
   * Called when the columns have changed.
   */
  protected void newColumns(){
    columnData = new ArrayList(dataModel.getColumnCount());
    for(int i = 0; i < dataModel.getColumnCount(); i++)
      columnData.add(new ColumnData(i));
  }
  
  /**
   * This is called whenever the UI is initialised or changed
   */
  public void updateUI() {
    super.updateUI();
    getTableHeader().addMouseListener(new HeaderMouseListener());
    fixColumnSizes();
  }
  
  public Dimension getPreferredSize(){
    int width = 0;
    for(int i = 0; i < getColumnModel().getColumnCount(); i++)
      width += getColumnModel().getColumn(i).getPreferredWidth();
    int height = 0;
    for(int i = 0; i < getRowCount(); i++)
      height += getRowHeight(i);
    return new Dimension(width, height);
  }
  
  public Dimension getPreferredScrollableViewportSize(){
    return getPreferredSize();
  }
  
  /**
   * Used for setting the initial sizing of the columns
   *
   */
  public void fixColumnSizes(){
    Iterator colIter = columnData.iterator();
    while(colIter.hasNext()){
      ((ColumnData)colIter.next()).adjustColumnWidth();
    }
  }
  
  /**
   * Sets the custom comparator to be used for a particular column. Columns that
   * don't have a custom comparator will be sorted using the natural order.
   * @param column the column index.
   * @param comparator the comparator to be used.
   */
  public void setComparator(int column, Comparator comparator){
    ((ColumnData)columnData.get(column)).comparator = comparator;
  }
    
  /**
   * @return Returns the sortable.
   */
  public boolean isSortable(){
    return sortable;
  }
  /**
   * @param sortable The sortable to set.
   */
  public void setSortable(boolean sortable){
    this.sortable = sortable;
  }
  /**
   * @return Returns the sortColumn.
   */
  public int getSortedColumn(){
    return sortedColumn;
  }
  /**
   * @param sortColumn The sortColumn to set.
   */
  public void setSortedColumn(int sortColumn){
    this.sortedColumn = sortColumn;
  }
  
  /**
   * Get the row in the table for a row in the model.
   */
  public int getTableRow(int modelRow){
    return sortingModel.sourceToTarget(modelRow);
  }

  /**
   * Handles translations between an indexed data source and a permutation of 
   * itself (like the translations between the rows in sorted table and the
   * rows in the actual unsorted model).  
   */
  protected class SortingModel extends AbstractTableModel 
      implements TableModelListener{
    
    public SortingModel(TableModel sourceModel){
      init(sourceModel);
    }
    
    protected void init(TableModel sourceModel){
      if(this.sourceModel != null) 
        this.sourceModel.removeTableModelListener(this);
      this.sourceModel = sourceModel;
      //start with the identity order
      int size = sourceModel.getRowCount();
      sourceToTarget = new int[size];
      targetToSource = new int[size];
      for(int i = 0; i < size; i++) {
        sourceToTarget[i] = i;
        targetToSource[i] = i;
      }
      sourceModel.addTableModelListener(this);
      if(isSortable()) setSortedColumn(0);
    }
    
    /**
     * This gets events from the source model and forwards them to the UI
     */
    public void tableChanged(TableModelEvent e){
      int type = e.getType();
      int firstRow = e.getFirstRow();
      int lastRow = e.getLastRow();
      int column = e.getColumn();
      
      //now deal with the changes in the data
      //we have no way to "repair" the sorting on data updates so we will need
      //to rebuild the order every time
      
      switch(type){
        case TableModelEvent.UPDATE:
          if(firstRow == TableModelEvent.HEADER_ROW){
            //complete structure change -> reallocate the data
            init(sourceModel);
            fireTableChanged(new TableModelEvent(this,  
                    firstRow, lastRow, column, type));
            if(isSortable()) sort();
            newColumns();
            fixColumnSizes();
          }else if(lastRow == Integer.MAX_VALUE){
            //all data changed (including the number of rows)
            init(sourceModel);
            fireTableChanged(new TableModelEvent(this,  
                    firstRow, lastRow, column, type));
            if(isSortable()) sort();
            fixColumnSizes();
          }else{
            //the rows should have normal values
            //if the sortedColumn is not affected we don't care
            if(column == sortedColumn || column == TableModelEvent.ALL_COLUMNS){
              if(isSortable()) sort();
            }else{
              fireTableChanged(new TableModelEvent(this,  
                      sourceToTarget(firstRow), 
                      sourceToTarget(lastRow), column, type));
              
            }
            if(column == TableModelEvent.ALL_COLUMNS){
              fixColumnSizes();
            }else{
              ((ColumnData)columnData.get(column)).adjustColumnWidth();
            }
          }
          break;
        case TableModelEvent.INSERT:
          //rows were inserted -> we need to rebuild
          init(sourceModel);
          if(firstRow == lastRow){  
            fireTableChanged(new TableModelEvent(this,  
                    sourceToTarget(firstRow), 
                    sourceToTarget(lastRow), column, type));
          }else{
            //the real rows are not in sequence
            fireTableDataChanged();
          }
          if(isSortable()) sort();
          if(column == TableModelEvent.ALL_COLUMNS) fixColumnSizes();
          else ((ColumnData)columnData.get(column)).adjustColumnWidth();
          break;
        case TableModelEvent.DELETE:
          //rows were deleted -> we need to rebuild
          init(sourceModel);
          if(firstRow == lastRow){  
            fireTableChanged(new TableModelEvent(this,  
                    sourceToTarget(firstRow), 
                    sourceToTarget(lastRow), column, type));
          }else{
            //the real rows are not in sequence
            fireTableDataChanged();
          }
          if(isSortable()) sort();
      }
    }
    
    public int getRowCount(){
      return sourceModel.getRowCount();
    }
    
    public int getColumnCount(){
      return sourceModel.getColumnCount();
    }
    
    public String getColumnName(int columnIndex){
      return sourceModel.getColumnName(columnIndex);
    }
    public Class getColumnClass(int columnIndex){
      return sourceModel.getColumnClass(columnIndex);
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex){
      return sourceModel.isCellEditable(targetToSource(rowIndex),
              columnIndex);
    }
    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
      sourceModel.setValueAt(aValue, targetToSource(rowIndex), 
              columnIndex);
    }
    public Object getValueAt(int row, int column){
      return sourceModel.getValueAt(targetToSource(row), column);
    }
    
    /**
     * Sorts the table using the values in the specified column and sorting order.
     * @param sortedColumn the column used for sorting the data.
     * @param ascending the sorting order.
     */
    public void sort(){
      List sourceData = new ArrayList(sourceModel.getRowCount());
      //get the data in the source order
      for(int i = 0; i < sourceModel.getRowCount(); i++){
        sourceData.add(sourceModel.getValueAt(i, sortedColumn));
      }
      //get an appropriate comparator
      Comparator comparator = ((ColumnData)columnData.
              get(sortedColumn)).comparator;
      if(comparator == null){
        //use the default comparator
        if(defaultComparator == null) 
          defaultComparator = new DefaultComparator();
        comparator = defaultComparator;
      }
      for(int i = 0; i < sourceData.size() - 1; i++){
        for(int j = i + 1; j < sourceData.size(); j++){
          Object o1 = sourceData.get(targetToSource(i));
          Object o2 = sourceData.get(targetToSource(j));
          boolean swap = ascending ?
                  (comparator.compare(o1, o2) > 0) :
                  (comparator.compare(o1, o2) < 0);
          if(swap){
            int aux = targetToSource[i];
            targetToSource[i] = targetToSource[j];
            targetToSource[j] = aux;
            
            sourceToTarget[targetToSource[i]] = i;
            sourceToTarget[targetToSource[j]] = j;
          }
        }
      }
      
      fireTableRowsUpdated(0, sourceData.size() -1);
    }

    
//    /**
//     * Reinitialises the internal data structures with the given sorting order.
//     * @param order the new sorting order.
//     */
//    public void init(int[] order){
//      sourceToTarget = new int[order.length];
//      System.arraycopy(order, 0, sourceToTarget, 
//              0, order.length);
//      buildTargetToSourceIndex();
//    }
    
    /**
     * Converts an index from the source coordinates to the target ones.
     * Used to propagate events from the data source (table model) to the view. 
     * @param index the index in the source coordinates.
     * @return the corresponding index in the target coordinates.
     */
    public int sourceToTarget(int index){
      return sourceToTarget[index];
    }

    /**
     * Converts an index from the target coordinates to the source ones. 
     * @param index the index in the target coordinates.
     * Used to propagate events from the view (e.g. editing) to the source
     * data source (table model).
     * @return the corresponding index in the source coordinates.
     */
    public int targetToSource(int index){
      return targetToSource[index];
    }
    
    /**
     * Builds the reverse index based on the new sorting order.
     */
    protected void buildTargetToSourceIndex(){
      targetToSource = new int[sourceToTarget.length];
      for(int i = 0; i < sourceToTarget.length; i++)
        targetToSource[sourceToTarget[i]] = i;
    }
    
    /**
     * The direct index
     */
    protected int[] sourceToTarget;
    
    /**
     * The reverse index.
     */
    protected int[] targetToSource;
    
    protected TableModel sourceModel;
  }
  
  protected class HeaderMouseListener extends MouseAdapter{
    public HeaderMouseListener(){
    }
    
    public void mouseClicked(MouseEvent e){
      process(e);
    }
    
    public void mousePressed(MouseEvent e){
      process(e);
    }
    
    public void mouseReleased(MouseEvent e){
      process(e);
    }
    
    protected void process(MouseEvent e){
      int viewColumn = columnModel.getColumnIndexAtX(e.getX());
      final int column = convertColumnIndexToModel(viewColumn);
      ColumnData cData = (ColumnData)columnData.get(column);
      if((e.getID() == MouseEvent.MOUSE_PRESSED || 
          e.getID() == MouseEvent.MOUSE_RELEASED) && 
         e.isPopupTrigger()){
        //show pop-up
        cData.popup.show(e.getComponent(), e.getX(), e.getY());
      }else if(e.getID() == MouseEvent.MOUSE_CLICKED &&
               e.getButton() == MouseEvent.BUTTON1){
        //normal click 
        if(e.getClickCount() >= 2){
          //double click -> resize
          if(singleClickTimer != null){
            singleClickTimer.stop();
            singleClickTimer = null;
          }
          cData.adjustColumnWidth();
        }else {
          //possible single click -> resort
          singleClickTimer = new Timer(CLICK_DELAY, new ActionListener(){
            public void actionPerformed(ActionEvent evt){
              //this is the action to be done for single click.
              if(sortable && column != -1) {
                ascending = (column == sortedColumn) ? !ascending : true;
                sortedColumn = column;
                sortingModel.sort();
              }
            }
          });
          singleClickTimer.setRepeats(false);
          singleClickTimer.start();
        }
      }
    }
    /**
     * How long should we wait for a second click until deciding the it's 
     * actually a single click.
     */
    private static final int CLICK_DELAY = 300;
    protected Timer singleClickTimer;
  }
  
  protected class ColumnData{
    public ColumnData(int column){
      this.column = column;
      popup = new JPopupMenu();
      hideMenuItem = new JCheckBoxMenuItem("Hide", false);
      popup.add(hideMenuItem);
      autoSizeMenuItem = new JCheckBoxMenuItem("Autosize", true);
//      popup.add(autoSizeMenuItem);
      hidden = false;
      initListeners();
    }
    
    protected void initListeners(){
      hideMenuItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          TableColumn tCol = getColumnModel().getColumn(column);
          if(hideMenuItem.isSelected()){
            //hide column
            colWidth = tCol.getWidth();
            colPreferredWidth = tCol.getPreferredWidth();
            colMaxWidth = tCol.getMaxWidth();
            tCol.setPreferredWidth(HIDDEN_WIDTH);
            tCol.setMaxWidth(HIDDEN_WIDTH);
            tCol.setWidth(HIDDEN_WIDTH);
          }else{
            //show column
            tCol.setMaxWidth(colMaxWidth);
            tCol.setPreferredWidth(colPreferredWidth);
            tCol.setWidth(colWidth);
          }
        }
      });
      
      autoSizeMenuItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          if(autoSizeMenuItem.isSelected()){
            //adjust the size for this column
            adjustColumnWidth();
          }
        }
      });
      
    }
    
    public boolean isHidden(){
      return hideMenuItem.isSelected();
    }
    
    public void adjustColumnWidth(){
      int viewColumn = convertColumnIndexToView(column);
      TableColumn tCol = getColumnModel().getColumn(column);
      Dimension dim;
      int width;
      TableCellRenderer renderer;
      //compute the sizes
      if(getTableHeader() != null){
        renderer = tCol.getHeaderRenderer();
        if(renderer == null) renderer = getTableHeader().getDefaultRenderer();
        width = renderer.getTableCellRendererComponent(XJTable.this, 
                tCol.getHeaderValue(), true, true ,0 , viewColumn).
                getPreferredSize().width;
      }else{
        width = 0;
      }
      renderer = tCol.getCellRenderer();
      if(renderer == null) renderer = getDefaultRenderer(getColumnClass(column));
      for(int row = 0; row < getRowCount(); row ++){
        if(renderer != null){
          dim = renderer. getTableCellRendererComponent(XJTable.this, 
                  getValueAt(row, column), false, false, row, viewColumn).
                  getPreferredSize();
          width = Math.max(width, dim.width);
          int height = dim.height;
          if((height + getRowMargin()) > getRowHeight(row)){
            setRowHeight(row, height + getRowMargin());
           }          
        }
      }

      int marginWidth = getColumnModel().getColumnMargin(); 
      if(marginWidth > 0) width += marginWidth; 
      tCol.setPreferredWidth(width);
    }
    
    JCheckBoxMenuItem autoSizeMenuItem;
    JCheckBoxMenuItem hideMenuItem;
    JPopupMenu popup;
    int column;
    boolean hidden;
    int colPreferredWidth;
    int colMaxWidth;
    int colWidth;
    Comparator comparator;
    private static final int HIDDEN_WIDTH = 5;
  }
  
  /**
   * This is used as the default comparator for a column when a custom was
   * not provided.
   */
  protected class DefaultComparator implements Comparator{
    
    public int compare(Object o1, Object o2){
      // If both values are null, return 0.
      if (o1 == null && o2 == null) {
        return 0;
      } else if (o1 == null) { // Define null less than everything.
        return -1;
      } else if (o2 == null) {
        return 1;
      }
      int result;
      if(o1 instanceof Comparable){
        try {
          result = ((Comparable)o1).compareTo(o2);
        } catch(ClassCastException cce) {
          String s1 = o1.toString();
          String s2 = o2.toString();
          result = s1.compareTo(s2);
        }
      } else {
        String s1 = o1.toString();
        String s2 = o2.toString();
        result = s1.compareTo(s2);
      }
      
      return result;
    }
  }
  protected SortingModel sortingModel;
  protected DefaultComparator defaultComparator;
  
  /**
   * The column currently being sorted.
   */
  protected int sortedColumn = -1;
  
  /**
   * is the current sort order ascending (or descending)?
   */
  protected boolean ascending = true;
  /**
   * Should this table be sortable.
   */
  protected boolean sortable = true;
  
  /**
   * A list of {@link ColumnData} objects.
   */
  protected List columnData;
}
