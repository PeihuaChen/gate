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
import java.util.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
    mainTable = new XJTable(new SetsTableModel());
    ((XJTable)mainTable).setSortable(false);
    SetsTableCellRenderer cellRenderer = new SetsTableCellRenderer();
    mainTable.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
    mainTable.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
    mainTable.setTableHeader(null);
    mainTable.setShowVerticalLines(false);
//    mainTable.setMinimumSize(mainTable.getPreferredSize());
    mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    scroller = new JScrollPane(mainTable);
//    scroller.setMinimumSize(mainTable.getPreferredSize());
  }
    
	
  protected class SetsTableModel extends AbstractTableModel{
    public int getRowCount(){
      //we have at least one row per set
      int rows = setHandlers.size();
      //expanded sets add rows
      for(int i =0; i < setHandlers.size(); i++){
        SetHandler sHandler = (SetHandler)setHandlers.get(i);
        rows += sHandler.expanded ? sHandler.set.getAllTypes().size() : 0;
      }
      return rows;
    }
    
    public int getColumnCount(){
      return 2;
    }
    
    public Object getValueAt(int row, int column){
      int currentRow = 0;
      Iterator handlerIter = setHandlers.iterator();
      SetHandler sHandler = (SetHandler)handlerIter.next();
      
      while(currentRow < row){
        if(sHandler.expanded){
          if(sHandler.typeHandlers.size() + currentRow >= row){
            //we want a row in current set
             return sHandler.typeHandlers.get(row - currentRow);
          }else{
            currentRow += sHandler.typeHandlers.size();
            sHandler = (SetHandler)handlerIter.next();
          }
        }else{
          //just go to next handler
          currentRow++;
          sHandler = (SetHandler)handlerIter.next();
        }
        if(currentRow == row) return sHandler;
      }
      if(currentRow == row) return sHandler;
System.out.println("BUG! row: " + row + " col: " + column);      
      return null;
    }
  }
  
  protected class SetsTableCellRenderer implements TableCellRenderer{
    public SetsTableCellRenderer(){
      label = new JLabel(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                													Object oldValue,
                													Object newValue){}
      };
      normalFont = label.getFont();
      boldFont = normalFont.deriveFont(Font.BOLD);
      button = new JToggleButton(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                													Object oldValue,
                													Object newValue){}
      };
//      button.setSelectedIcon(MainFrame.getIcon("expanded.gif"));
//      button.setIcon(MainFrame.getIcon("closed.gif"));
      button.setMaximumSize(button.getMinimumSize());
    }
    public Component getTableCellRendererComponent(JTable table,
																		               Object value,
																			             boolean isSelected,
																			             boolean hasFocus,
																			             int row,
																			             int column){
      
      if(value instanceof SetHandler){
        SetHandler sHandler = (SetHandler)value;
        switch(column){
          case NAME_COL:
            label.setText(sHandler.set.getName());
            label.setFont(boldFont);
            label.setOpaque(false);
            label.setIcon(null);
            return label;
          case SELECTED_COL:
            label.setText(null);
            label.setIcon(sHandler.expanded ? 
                          MainFrame.getIcon("expanded.gif") :
                          MainFrame.getIcon("closed.gif"));         
            button.setSelected(sHandler.expanded);
            return label;
          default:
            label.setText("?");
          	return label;
        }
      }else if(value instanceof TypeHandler){
        TypeHandler tHandler = (TypeHandler)value;
        switch(column){
          case NAME_COL:
            label.setText(tHandler.name);
            label.setFont(boldFont);
            label.setOpaque(false);
            label.setIcon(null);
            return label;
          case SELECTED_COL:
            button.setSelected(tHandler.selected);
            return button;
          default:
            label.setText("?");
          	return label;
        }
      }else{
        label.setText("?");
      	return label;
        //bugcheck!
      }
    }
    protected JLabel label;
    protected JToggleButton button;
    protected Font normalFont;
    protected Font boldFont;
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
    
    boolean selected;
    String name;
    SetHandler setHandler;
    Color colour;
  }
  
  List setHandlers;
  JTable mainTable;
  JScrollPane scroller;
  
  protected ColorGenerator colourGenerator;
  private static final int NAME_COL = 0;
  private static final int SELECTED_COL = 1;
  
}
