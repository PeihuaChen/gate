/*
 * Created on Mar 23, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package gate.gui.docview;

import java.awt.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import gate.AnnotationSet;
import gate.gui.MainFrame;
import gate.swing.ColorGenerator;
import gate.swing.XJTable;

/**
 * @author valyt
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AnnotationSetsView extends AbstractDocumentView {

  
  public AnnotationSetsView(){
    setHandlers = new ArrayList();
    tableRows = new ArrayList();
    colourGenerator = new ColorGenerator();
  }
  
  /* (non-Javadoc)
   * @see gate.gui.docview.DocumentView#getGUI()
   */
  public Component getGUI() {
    return scroller;
  }
  /* (non-Javadoc)
   * @see gate.gui.docview.DocumentView#getType()
   */
  public int getType() {
    return VERTICAL;
  }
  
  protected void initGUI() {
    setHandlers.add(new SetHandler(document.getAnnotations()));
    List setNames = new ArrayList(document.getNamedAnnotationSets().keySet());
    Collections.sort(setNames);
    Iterator setsIter = setNames.iterator();
    while(setsIter.hasNext()){
      setHandlers.add(new SetHandler(document.
              getAnnotations((String)setsIter.next())));
    }
    tableRows.addAll(setHandlers);
    mainTable = new XJTable(tableModel = new SetsTableModel());
    ((XJTable)mainTable).setSortable(false);
    mainTable.setRowMargin(2);
    mainTable.getColumnModel().setColumnMargin(0);
    SetsTableCellRenderer cellRenderer = new SetsTableCellRenderer();
    mainTable.getColumnModel().getColumn(NAME_COL).setCellRenderer(cellRenderer);
    mainTable.getColumnModel().getColumn(SELECTED_COL).setCellRenderer(cellRenderer);
    SetsTableCellEditor cellEditor = new SetsTableCellEditor();
    mainTable.getColumnModel().getColumn(SELECTED_COL).setCellEditor(cellEditor);
    
    mainTable.setTableHeader(null);
    mainTable.setShowVerticalLines(false);
    mainTable.setShowHorizontalLines(false);
    mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    scroller = new JScrollPane(mainTable);
    scroller.getViewport().setOpaque(true);
    scroller.getViewport().setBackground(mainTable.getBackground());
    
    initListeners();
  }
  
  protected void initListeners(){
  }
    
	
  protected class SetsTableModel extends AbstractTableModel{
    public int getRowCount(){
      return tableRows.size();
//      //we have at least one row per set
//      int rows = setHandlers.size();
//      //expanded sets add rows
//      for(int i =0; i < setHandlers.size(); i++){
//        SetHandler sHandler = (SetHandler)setHandlers.get(i);
//        rows += sHandler.expanded ? sHandler.set.getAllTypes().size() : 0;
//      }
//      return rows;
    }
    
    public int getColumnCount(){
      return 2;
    }
    
    public Object getValueAt(int row, int column){
      Object value = tableRows.get(row);
      switch(column){
        case NAME_COL:
          return value;
        case SELECTED_COL:
          if(value instanceof SetHandler)
            return new Boolean(((SetHandler)value).expanded);
          if(value instanceof TypeHandler) 
            return new Boolean(((TypeHandler)value).selected);
        default:
          return null;
      }
//      
//      int currentRow = 0;
//      Iterator handlerIter = setHandlers.iterator();
//      SetHandler sHandler = (SetHandler)handlerIter.next();
//      
//      while(currentRow < row){
//        if(sHandler.expanded){
//          if(sHandler.typeHandlers.size() + currentRow >= row){
//            //we want a row in current set
//             return sHandler.typeHandlers.get(row - currentRow);
//          }else{
//            currentRow += sHandler.typeHandlers.size();
//            sHandler = (SetHandler)handlerIter.next();
//          }
//        }else{
//          //just go to next handler
//          currentRow++;
//          sHandler = (SetHandler)handlerIter.next();
//        }
//        if(currentRow == row) return sHandler;
//      }
//      if(currentRow == row) return sHandler;
//System.out.println("BUG! row: " + row + " col: " + column);      
//      return null;
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex){
      Object value = tableRows.get(rowIndex);
      switch(columnIndex){
        case NAME_COL: return false;
        case SELECTED_COL:
          if(value instanceof SetHandler)
            return ((SetHandler)value).typeHandlers.size() > 0;
          if(value instanceof TypeHandler) return true; 
      }
      return columnIndex == SELECTED_COL;
    }
    
    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
      Object receiver = tableRows.get(rowIndex);
      switch(columnIndex){
        case SELECTED_COL:
          if(receiver instanceof SetHandler){
            ((SetHandler)receiver).setExpanded(((Boolean)aValue).booleanValue());
          }else if(receiver instanceof TypeHandler){
            ((TypeHandler)receiver).setSelected(((Boolean)aValue).booleanValue());
          }
          
          break;
        default:
          break;
      }
    }
  }//public Object getValueAt(int row, int column)
  
  protected class SetsTableCellRenderer implements TableCellRenderer{
    public SetsTableCellRenderer(){
      typeLabel = new JLabel(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                													Object oldValue,
                													Object newValue){}
      };
      typeLabel.setOpaque(true);
      typeLabel.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createMatteBorder(0, 0, 0, 5,
                      												mainTable.getBackground()),
              BorderFactory.createEmptyBorder(0, 5, 0, 5)));
//      typeLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

      
      setLabel = new JLabel(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                													Object oldValue,
                													Object newValue){}
      };
      setLabel.setOpaque(false);
      setLabel.setFont(setLabel.getFont().deriveFont(Font.BOLD));
      setLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
      

      typeChk = new JCheckBox(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                													Object oldValue,
                													Object newValue){}
      };
      typeChk.setOpaque(false);

      setChk = new JCheckBox(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                													Object oldValue,
                													Object newValue){}
      };
      setChk.setSelectedIcon(MainFrame.getIcon("expanded.gif"));
      setChk.setIcon(MainFrame.getIcon("closed.gif"));
      setChk.setMaximumSize(setChk.getMinimumSize());
      setChk.setOpaque(false);
      
    }
    public Component getTableCellRendererComponent(JTable table,
																		               Object value,
																			             boolean isSelected,
																			             boolean hasFocus,
																			             int row,
																			             int column){
      
      value = tableRows.get(row);
      if(value instanceof SetHandler){
        SetHandler sHandler = (SetHandler)value;
        switch(column){
          case NAME_COL:
            setLabel.setText(sHandler.set.getName());
            return setLabel;
          case SELECTED_COL:
            setChk.setSelected(sHandler.expanded);
            setChk.setEnabled(sHandler.typeHandlers.size() > 0);
            return setChk;
        }
      }else if(value instanceof TypeHandler){
        TypeHandler tHandler = (TypeHandler)value;
        switch(column){
          case NAME_COL:
            typeLabel.setBackground(tHandler.colour);
            typeLabel.setText(tHandler.name);
            return typeLabel;
          case SELECTED_COL:
//            typeChk.setBackground(tHandler.colour);
            typeChk.setSelected(tHandler.selected);
            return typeChk;
        }
      }
      typeLabel.setText("?");
    	return typeLabel;
      //bugcheck!
    }
    
    protected JLabel typeLabel;
    protected JLabel setLabel;
    protected JCheckBox setChk;
    protected JCheckBox typeChk;
  }
  
  protected class SetsTableCellEditor extends AbstractCellEditor
                                      implements TableCellEditor{
    public SetsTableCellEditor(){
      setChk = new JCheckBox();
      setChk.setSelectedIcon(MainFrame.getIcon("expanded.gif"));
      setChk.setIcon(MainFrame.getIcon("closed.gif"));
//      setChk.setMaximumSize(setChk.getMinimumSize());
      setChk.setOpaque(false);
      setChk.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          fireEditingStopped();
        }
      });
      typeChk = new JCheckBox();
      typeChk.setOpaque(false);
      typeChk.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          fireEditingStopped();
        }
      });
    }
    
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column){
      value = tableRows.get(row);
      if(value instanceof SetHandler){
        SetHandler sHandler = (SetHandler)value;
        switch(column){
          case NAME_COL: return null;
          case SELECTED_COL:
            setChk.setSelected(sHandler.expanded);
            setChk.setEnabled(sHandler.typeHandlers.size() > 0);
            currentChk = setChk;
            return setChk;
        }
      }else if(value instanceof TypeHandler){
        TypeHandler tHandler = (TypeHandler)value;
        switch(column){
          case NAME_COL: return null;
          case SELECTED_COL:
//            typeChk.setBackground(tHandler.colour);
            typeChk.setSelected(tHandler.selected);
            currentChk = typeChk;
            return typeChk;
        }
      }
      return null;
    }
    
    public boolean stopCellEditing(){
      return true;
    }
    
    public Object getCellEditorValue(){
      return new Boolean(currentChk.isSelected());
    }
    
    public boolean shouldSelectCell(EventObject anEvent){
      return false;
    }
    
    public boolean isCellEditable(EventObject anEvent){
      return true;
    }
    
    JCheckBox currentChk;
    JCheckBox setChk;
    JCheckBox typeChk;
  }
  
  
  /**
   * Stores the data related to an annotation set
   */
  protected class SetHandler{
    SetHandler(AnnotationSet set){
      this.set = set;
      typeHandlers = new ArrayList();
      List typeNames = new ArrayList(set.getAllTypes());
      Collections.sort(typeNames);
      Iterator typIter = typeNames.iterator();
      while(typIter.hasNext()){
        String name = (String)typIter.next();
        typeHandlers.add(new TypeHandler(this, name));
      }
    }
    
    public void setExpanded(boolean expanded){
      if(this.expanded == expanded) return;
      this.expanded = expanded;
      int myPosition = tableRows.indexOf(this);
      if(expanded){
        //expand
        tableRows.addAll(myPosition + 1, typeHandlers);
        tableModel.fireTableRowsInserted(myPosition + 1, 
                 												 myPosition + 1 + typeHandlers.size());
      }else{
        //collapse
        for(int i = 0; i < typeHandlers.size(); i++){
          tableRows.remove(myPosition + 1);
        }
        tableModel.fireTableRowsDeleted(myPosition + 1, 
								                        myPosition + 1 + typeHandlers.size());
      }
    }
    
    AnnotationSet set;
    List typeHandlers;
    boolean expanded = false;
  }
  
  protected class TypeHandler{
    TypeHandler (SetHandler setHandler, String name){
      this.setHandler = setHandler;
      this.name = name;
      colour = colourGenerator.getNextColor();
    }
    
    public void setSelected(boolean selected){
      this.selected = selected;
    }
    
    boolean selected;
    String name;
    SetHandler setHandler;
    Color colour;
  }
  
  List setHandlers;
  List tableRows; 
  JTable mainTable;
  SetsTableModel tableModel;
  JScrollPane scroller;
  
  protected ColorGenerator colourGenerator;
  private static final int NAME_COL = 1;
  private static final int SELECTED_COL = 0;
  
}
