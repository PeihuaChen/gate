/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  XJTable.java
 *
 *  Valentin Tablan, 25-Jun-2004
 *
 *  $Id$
 */

package gate.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import gate.swing.XJTable.SortingModel.ValueHolder;
import gate.util.ObjectComparator;

/**
 * A &quot;smarter&quot; JTable. Features include:
 * <ul>
 * <li>sorting the table using the values from a column as keys</li>
 * <li>updating the widths of the columns so they accommodate the contents to
 * their preferred sizes.</li>
 * <li>sizing the rows according to the preferred sizes of the renderers</li>
 * <li>ability to hide columns</li>
 * </ul>
 * It uses a custom made model that stands between the table model set by the
 * user and the GUI component. This middle model is responsible for sorting the
 * rows.
 */
public class XJTable extends JTable{
  
  public XJTable(){
    super();
  }
  
  public XJTable(TableModel model){
    super();
    setModel(model);
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
    columnData = new ArrayList<ColumnData>(dataModel.getColumnCount());
    for(int i = 0; i < dataModel.getColumnCount(); i++)
      columnData.add(new ColumnData(i));
  }
  
  
  public void setTableHeader(JTableHeader newTableHeader) {
    //first remove the old listener from the old header
    JTableHeader oldHeader = getTableHeader();
    if(oldHeader != null && headerMouseListener != null)
      oldHeader.removeMouseListener(headerMouseListener);
    // set the new header
    super.setTableHeader(newTableHeader);
    //add the mouse listener to the new header
    if(headerMouseListener == null) headerMouseListener = 
      new HeaderMouseListener();
    if(newTableHeader != null) 
      newTableHeader.addMouseListener(headerMouseListener);
  }
  
  public Dimension getPreferredScrollableViewportSize(){
    return getPreferredSize();
  }
  
  protected void calculatePreferredSize(){
    //start with all columns at default size
    int colCount = getColumnModel().getColumnCount();
    for(int col = 0; col < colCount; col++){
      TableColumn tCol = getColumnModel().getColumn(col);
      TableCellRenderer headerRenderer = tCol.getHeaderRenderer();
      if(headerRenderer == null){
        //no header renderer provided -> use default implementation
        JTableHeader tHeader = getTableHeader();
        if(tHeader == null){
          tHeader = new JTableHeader();
        }
        headerRenderer = tHeader.getDefaultRenderer();
        tCol.setHeaderRenderer(headerRenderer);
      }
      if(headerRenderer != null){
        Component c = headerRenderer.getTableCellRendererComponent(
                XJTable.this, tCol.getHeaderValue(), false, false, 0, 0);
        tCol.setMinWidth(c.getMinimumSize().width);
        tCol.setPreferredWidth(c.getPreferredSize().width);
      }else{
        tCol.setMinWidth(1);
        tCol.setPreferredWidth(1);          
      }
    }
    //start with all rows of size 1
    for(int row = 0; row < getRowCount(); row++)
      setRowHeight(row, 1);
    for(int row = 0; row < getRowCount(); row++)
      for(int column = 0; column < getColumnCount(); column ++){
        prepareRenderer(getCellRenderer(row, column), row, column);
      }
    componentSizedProperly = true;
  }
  @Override
  /**
   * Overridden so that the preferred size can be calculated properly
   */
  public Dimension getPreferredSize() {
    //the first time the component is sized, calculate the actual preferred size
    if(!componentSizedProperly){
      calculatePreferredSize();
    }
    return super.getPreferredSize();
  }

  /**
   * Overridden to ignore requests for this table to track the width of its
   * containing viewport in cases where the viewport is narrower than the
   * minimum size of the table.  Where the viewport is at least as wide as
   * the minimum size of the table, we will allow the table to resize with
   * the viewport, but when it gets too small we stop tracking, which allows
   * the horizontal scrollbar to appear.
   */
  @Override
  public boolean getScrollableTracksViewportWidth() {
    if(super.getScrollableTracksViewportWidth()) {
      Container parent = this.getParent();
      if(parent instanceof JViewport) {
        // only track the viewport width if it is big enough.
        return ((JViewport)parent).getExtentSize().width
                    >= this.getMinimumSize().width;
      }
      else {
        return true;
      }
    }
    else { // super.getScrollableTracksViewportWidth() == false
      return false;
    }
  }

  private boolean componentSizedProperly = false;
  @Override
  /**
   * Overridden to capture the preferred size while painting the cells
   */  
  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    Component cellComponent =  super.prepareRenderer(renderer, row, column);
    TableColumn tColumn = getColumnModel().getColumn(column);
    column = convertColumnIndexToModel(column);
    ColumnData cData = columnData.get(column);
    Dimension spacing = getIntercellSpacing();
    
    if(cData.isHidden()){
      //do nothing - the sizes are already fixed
      tColumn.setMinWidth(ColumnData.HIDDEN_WIDTH + spacing.width);
      tColumn.setPreferredWidth(ColumnData.HIDDEN_WIDTH + spacing.width);
    }else{
      //fix the minimum size
      Dimension dim = cellComponent.getMinimumSize();
      if(tColumn.getMinWidth() < (dim.width + spacing.width))
              tColumn.setMinWidth(dim.width + spacing.width);
      //now fix the preferred size
      dim = cellComponent.getPreferredSize();
      if(tColumn.getPreferredWidth() < (dim.width + spacing.width))
        tColumn.setPreferredWidth(dim.width + spacing.width);
    }
    //now fix the row height
    Dimension dim = cellComponent.getPreferredSize();
    if(getRowHeight(row) < (dim.height + spacing.height)) 
            setRowHeight(row, dim.height + spacing.height);
    return cellComponent;
  }

  
  /**
   * Converts a row number from the model co-ordinates system to the view's. 
   * @param modelRow the row number in the model
   * @return the corresponding row number in the view. 
   */
  public int rowModelToView(int modelRow){
    return sortingModel.sourceToTarget(modelRow);
  }

  /**
   * @return Returns the ascending.
   */
  public boolean isAscending() {
    return ascending;
  }
  
  /**
   * Gets the hidden state for a column
   * @param columnIndex the column
   * @return the hidden state
   */
  public boolean isColumnHidden(int columnIndex){
    return columnData.get(columnIndex).isHidden();
  }
  
  /**
   * @param ascending The ascending to set.
   */
  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }
  /**
   * Converts a row number from the view co-ordinates system to the model's. 
   * @param viewRow the row number in the view.
   * @return the corresponding row number in the model. 
   */
  public int rowViewToModel(int viewRow){
    return sortingModel.targetToSource(viewRow);
  }
  
  /**
   * Sets the custom comparator to be used for a particular column. Columns that
   * don't have a custom comparator will be sorted using the natural order.
   * @param column the column index.
   * @param comparator the comparator to be used.
   */
  public void setComparator(int column, Comparator comparator){
    columnData.get(column).comparator = comparator;
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
      compWrapper = new ValueHolderComparator();
      init(sourceModel);
    }
    
    protected class ValueHolder{
      private Object value;
      private int index;
      public ValueHolder(Object value, int index) {
        super();
        this.value = value;
        this.index = index;
      }
    }
    
    protected class ValueHolderComparator implements Comparator<ValueHolder>{
      private Comparator comparator;
      
      protected Comparator getComparator() {
        return comparator;
      }

      protected void setComparator(Comparator comparator) {
        this.comparator = comparator;
      }

      public int compare(ValueHolder o1, ValueHolder o2) {
        // TODO Auto-generated method stub
        return ascending ? comparator.compare(o1.value, o2.value) :
          comparator.compare(o1.value, o2.value) * -1;
      }
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
      if(isSortable() && sortedColumn == -1) setSortedColumn(0);
      componentSizedProperly = false;
    }
    
    /**
     * This gets events from the source model and forwards them to the UI
     */
    public void tableChanged(TableModelEvent e){
      int type = e.getType();
      int firstRow = e.getFirstRow();
      int lastRow = e.getLastRow();
      int column = e.getColumn();
      
      //deal with the changes in the data
      //we have no way to "repair" the sorting on data updates so we will need
      //to rebuild the order every time
      
      switch(type){
        case TableModelEvent.UPDATE:
          if(firstRow == TableModelEvent.HEADER_ROW){
            //complete structure change -> reallocate the data
            init(sourceModel);
            newColumns();
            fireTableStructureChanged();
            if(isSortable()) sort();
          }else if(lastRow == Integer.MAX_VALUE){
            //all data changed (including the number of rows)
            init(sourceModel);
            if(isSortable()) sort();
            else fireTableDataChanged();
          }else{
            //the rows should have normal values
            //if the sortedColumn is not affected we don't care
            if(isSortable() &&
               (column == sortedColumn || 
                column == TableModelEvent.ALL_COLUMNS)){
                //re-sorting will also fire the event upwards
                sort();
            }else{
              fireTableChanged(new TableModelEvent(this,  
                      sourceToTarget(firstRow), 
                      sourceToTarget(lastRow), column, type));
              
            }
          }
          break;
        case TableModelEvent.INSERT:
          //rows were inserted -> we need to rebuild
          init(sourceModel);
          if(firstRow != TableModelEvent.HEADER_ROW && firstRow == lastRow){
            //single row insertion
            if(isSortable()) sort();
            else fireTableChanged(new TableModelEvent(this,  
                    sourceToTarget(firstRow), 
                    sourceToTarget(lastRow), column, type));
          }else{
            //the real rows are not in sequence
            if(isSortable()) sort();
            else fireTableDataChanged();
          }
          break;
        case TableModelEvent.DELETE:
          //rows were deleted -> we need to rebuild
          init(sourceModel);
          if(isSortable()) sort();
          else fireTableDataChanged();
      }
    }
    
    public int getRowCount(){
      return sourceToTarget.length;
//      return sourceModel.getRowCount();
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
      try{
        return sourceModel.getValueAt(targetToSource(row), column);
      }catch(IndexOutOfBoundsException iobe){
        //this can occur because of multithreaded access -> some threads empties 
        //the data while some other thread tries to update the display.
        //this error is safe to ignore as the GUI will get updated by another 
        //event once the change to the data has been effected
        return null;
      }
    }
    
    /**
     * Sorts the table using the values in the specified column and sorting order.
     * @param sortedColumn the column used for sorting the data.
     * @param ascending the sorting order.
     */
    public void sort(){
      try {
        //save the selection
        final int[] rows = getSelectedRows();
        //convert to model co-ordinates
        for(int i = 0; i < rows.length; i++) {
          rows[i] = rowViewToModel(rows[i]);
        }
        clearSelection();
        //create a list of ValueHolder objects for the source data that needs
        //sorting
        List<ValueHolder> sourceData = 
            new ArrayList<ValueHolder>(sourceModel.getRowCount());
        //get the data in the source order
        for(int i = 0; i < sourceModel.getRowCount(); i++){
          Object value = sourceModel.getValueAt(i, sortedColumn);
          sourceData.add(new ValueHolder(value, i));
        }
        
        //get an appropriate comparator
        Comparator comparator = columnData.get(sortedColumn).comparator;
        if(comparator == null){
          //use the default comparator
          if(defaultComparator == null) defaultComparator = new ObjectComparator();
          comparator = defaultComparator;
        }
        compWrapper.setComparator(comparator);
//          for(int i = 0; i < sourceData.size() - 1; i++){
//            for(int j = i + 1; j < sourceData.size(); j++){
//              Object o1 = sourceData.get(targetToSource(i));
//              Object o2 = sourceData.get(targetToSource(j));
//              boolean swap = ascending ?
//                      (comparator.compare(o1, o2) > 0) :
//                      (comparator.compare(o1, o2) < 0);
//              if(swap){
//                int aux = targetToSource[i];
//                targetToSource[i] = targetToSource[j];
//                targetToSource[j] = aux;
//                
//                sourceToTarget[targetToSource[i]] = i;
//                sourceToTarget[targetToSource[j]] = j;
//              }
//            }
//          }
          //perform the actual sorting
          Collections.sort(sourceData, compWrapper);
          //extract the new order
          for(int i = 0; i < sourceData.size(); i++){
            int targetIndex = i;
            int sourceIndex = sourceData.get(i).index;
            sourceToTarget[sourceIndex] = targetIndex;
            targetToSource[targetIndex] = sourceIndex;
          }
          sourceData.clear();
          fireTableDataChanged();
          //restore selection
          //convert to model co-ordinates
          for(int i = 0; i < rows.length; i++){
            rows[i] = rowModelToView(rows[i]);
            getSelectionModel().addSelectionInterval(rows[i], rows[i]);
          }
      }catch(ArrayIndexOutOfBoundsException aioob) {
        //this can happen when update events get behind
        //just ignore - we'll get another event later to cause the sorting
      }
    }
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
    
    protected ValueHolderComparator compWrapper;
  }
  
  protected class HeaderMouseListener extends MouseAdapter{
    public void mouseClicked(MouseEvent e){
      process(e);
    }
    
    public void mousePressed(MouseEvent e){
      process(e);
    }

    protected void process(MouseEvent e){
      int viewColumn = columnModel.getColumnIndexAtX(e.getX());
      if(viewColumn != -1){
        int column = convertColumnIndexToModel(viewColumn);
        ColumnData cData = columnData.get(column);
        if(e.isPopupTrigger()){
          //show pop-up
          cData.popup.show(e.getComponent(), e.getX(), e.getY());
        }else if(e.getID() == MouseEvent.MOUSE_CLICKED &&
               e.getButton() == MouseEvent.BUTTON1){
          //normal click -> re-sort
          if(sortable && column != -1) {
            ascending = (column == sortedColumn) ? !ascending : true;
            sortedColumn = column;
            sortingModel.sort();
          }
        }
      }
    }
  }
  
  protected class ColumnData{
    public ColumnData(int column){
      this.column = column;
      popup = new JPopupMenu();
      hideMenuItem = new JCheckBoxMenuItem("Hide", false);
      popup.add(hideMenuItem);
      hidden = false;
      initListeners();
    }
    
    protected void initListeners(){
      hideMenuItem.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          TableColumn tCol = getColumnModel().getColumn(column);
          if(hideMenuItem.isSelected()){
            //hide column
            //the state has already changed to hidden, the sizes will change
            //accordingly automatically
          }else{
            //show column
            if(tCol.getHeaderRenderer() == null){
              TableCellRenderer defaultRenderer = getDefaultRenderer(Object.class);
              if(defaultRenderer != null){
                Component c = defaultRenderer.getTableCellRendererComponent(
                        XJTable.this, tCol.getHeaderValue(), false, false, 0, 0);
                tCol.setMinWidth(c.getMinimumSize().width);
                tCol.setPreferredWidth(c.getPreferredSize().width);
              }else{
                tCol.setMinWidth(1);
                tCol.setPreferredWidth(1);          
              }
            }else{
              tCol.sizeWidthToFit();
            }
          }
        }
      });
    }
    
    public boolean isHidden(){
      return hideMenuItem.isSelected();
    }

    JCheckBoxMenuItem autoSizeMenuItem;
    JCheckBoxMenuItem hideMenuItem;
    JPopupMenu popup;
    int column;
    int columnWidth;
    boolean hidden;
    Comparator comparator;
    private static final int HIDDEN_WIDTH = 10;
  }
  
  protected SortingModel sortingModel;
  protected ObjectComparator defaultComparator;
  
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
  protected List<ColumnData> columnData;
  
  protected HeaderMouseListener headerMouseListener;
}
