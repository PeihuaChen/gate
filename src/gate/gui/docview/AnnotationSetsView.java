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

import gate.Annotation;
import gate.AnnotationSet;
import gate.event.AnnotationSetEvent;
import gate.event.AnnotationSetListener;
import gate.event.DocumentEvent;
import gate.event.DocumentListener;
import gate.gui.MainFrame;
import gate.swing.ColorGenerator;
import gate.swing.XJTable;

/**
 * @author valyt
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AnnotationSetsView extends AbstractDocumentView 
		                        implements DocumentListener,
		                                   AnnotationSetListener{

  
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
    //get a pointer to the textual view used for highlights
    Iterator centralViewsIter = owner.getCentralViews().iterator();
    while(textView == null && centralViewsIter.hasNext()){
      DocumentView aView = (DocumentView)centralViewsIter.next();
      if(aView instanceof TextualDocumentView) 
        textView = (TextualDocumentView)aView;
    }
    
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
    document.addDocumentListener(this);
  }
    
	
  /* (non-Javadoc)
   * @see gate.Resource#cleanup()
   */
  public void cleanup() {
    document.removeDocumentListener(this);
    super.cleanup();
  }
  public void annotationSetAdded(DocumentEvent e) {
    String newSetName = e.getAnnotationSetName();
    SetHandler sHandler = new SetHandler(document.getAnnotations(newSetName));
    //find the right location for the new set
    //this is a named set and the first one is always the default one
    int i = 1;
    for(;
        i < setHandlers.size() && 
        ((SetHandler)setHandlers.get(i)).set.
        	getName().compareTo(newSetName) <= 0;
        i++);
    setHandlers.add(i, sHandler);
    //update the tableRows list
    SetHandler previousHandler = (SetHandler)setHandlers.get(i -1);
    //find the index for the previous handler - which is guaranteed to exist
    int j = 0;
    for(;
    	tableRows.get(j) != previousHandler;
        j++);
    if(previousHandler.expanded){
      j+=previousHandler.typeHandlers.size();
    }else{
      j++;
    }
    tableRows.add(j, sHandler);
    //update the table view
    tableModel.fireTableRowsInserted(j, j);
  }//public void annotationSetAdded(DocumentEvent e) 
  
  public void annotationSetRemoved(DocumentEvent e) {
    String setName = e.getAnnotationSetName();
    //find the handler and remove it from the list of handlers
//    Iterator shIter = setHandlers.iterator();
    SetHandler sHandler = getSetHandler(setName);
    if(sHandler != null){
      setHandlers.remove(sHandler);
      //remove the set from the table
      int row = tableRows.indexOf(sHandler);
      tableRows.remove(row);
      int removed = 1;
      //remove the type rows as well
      if(sHandler.expanded)
        for(int i = 0; i < sHandler.typeHandlers.size(); i++){ 
          tableRows.remove(row);
          removed++;
        }
      tableModel.fireTableRowsDeleted(row, row + removed -1);
      //remove highlights if any
      Iterator typeIter = sHandler.typeHandlers.iterator();
      while(typeIter.hasNext()){
        TypeHandler tHandler = (TypeHandler)typeIter.next();
        tHandler.setSelected(false);
      }
      sHandler.cleanup();
    }
  }//public void annotationSetRemoved(DocumentEvent e) 
  
  public void annotationAdded(AnnotationSetEvent e) {
    AnnotationSet set = (AnnotationSet)e.getSource();
    Annotation ann = e.getAnnotation();
    TypeHandler tHandler = getTypeHandler(set.getName(), ann.getType());
    if(tHandler == null){
      //new type for this set
      SetHandler sHandler = getSetHandler(set.getName());
      tHandler = sHandler.newType(ann.getType());
    }
    tHandler.annotationAdded(ann);
  }
  
  public void annotationRemoved(AnnotationSetEvent e) {
    AnnotationSet set = (AnnotationSet)e.getSource();
    Annotation ann = e.getAnnotation();
    TypeHandler tHandler = getTypeHandler(set.getName(), ann.getType());
    tHandler.annotationRemoved(ann);
  }
  
  protected SetHandler getSetHandler(String name){
    Iterator shIter = setHandlers.iterator();
    while(shIter.hasNext()){
      SetHandler sHandler = (SetHandler)shIter.next();
      if(name == null){
        if(sHandler.set.getName() == null) return sHandler;
      }else{
        if(name.equals(sHandler.set.getName())) return sHandler;
      }
    }
    return null;
  }
  
  protected TypeHandler getTypeHandler(String set, String type){
    SetHandler sHandler = getSetHandler(set);
    TypeHandler tHandler = null;
    Iterator typeIter = sHandler.typeHandlers.iterator();
    while(tHandler == null && typeIter.hasNext()){
      TypeHandler aHandler = (TypeHandler)typeIter.next();
      if(aHandler.name.equals(type)) tHandler = aHandler;
    }
    return tHandler;
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
              BorderFactory.createMatteBorder(0, 5, 0, 0,
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
      typeChk.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

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
      typeChk.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
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
      set.addAnnotationSetListener(AnnotationSetsView.this);
    }
    
    public void cleanup(){
      set.removeAnnotationSetListener(AnnotationSetsView.this);
      typeHandlers.clear();
    }
    /**
     * Notifies this set handler that anew type of annotations has been created
     * @param type the new type of annotations
     * @return the new TypeHandler created as a result
     */
    public TypeHandler newType(String type){
      //create a new TypeHandler
      TypeHandler tHandler = new TypeHandler(this, type);
      //add it to the list at the right position
      int pos = 0;
      for(;
          pos < typeHandlers.size() &&
          ((TypeHandler)typeHandlers.get(pos)).name.compareTo(type) <= 0;
          pos++);
      typeHandlers.add(pos, tHandler);
      int setRow = tableRows.indexOf(this);
      if(typeHandlers.size() == 1) 
        tableModel.fireTableRowsUpdated(setRow, setRow);
      if(expanded) tableModel.fireTableRowsInserted(setRow + pos + 1,
              setRow + pos + 1);
      return tHandler;
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
      float components[] = colourGenerator.getNextColor().getComponents(null);
      colour = new Color(components[0],
                         components[1],
                         components[2],
                         0.5f);
      hghltTagsForAnn = new HashMap();
    }
    
    public void setSelected(boolean selected){
      if(this.selected == selected) return;
      this.selected = selected;
      if(selected){
      	//show highlights
        hghltTagsForAnn.clear();
        Iterator annIter = setHandler.set.get(name).iterator();
        while(annIter.hasNext()){
          Annotation ann = (Annotation)annIter.next();
          hghltTagsForAnn.put(ann, textView.addHighlight(ann, colour));
        }
      }else{
      	//hide highlights
      	Iterator tagIter = hghltTagsForAnn.values().iterator();
      	while(tagIter.hasNext()){
      	  textView.removeHighlight(tagIter.next());
      	}
      	hghltTagsForAnn.clear();
      }
    }
    
    /**
     * Notifies this type handler that a new annotation was created of the 
     * right type
     * @param ann
     */
    public void annotationAdded(Annotation ann){
      //if selected, add new highlight
      if(selected) hghltTagsForAnn.put(ann, textView.addHighlight(ann, colour));
    }
    
    /**
     * Notifies this type handler that an annotation has been removed
     * @param ann the removed annotation
     */
    public void annotationRemoved(Annotation ann){
      if(selected){
        Object tag = hghltTagsForAnn.remove(ann);
        textView.removeHighlight(tag);
      }
      //if this was the last annotation of this type then the handler is no
      //longer required
      Set remainingAnns = setHandler.set.get(name); 
      if(remainingAnns == null || remainingAnns.isEmpty()){
        int setRow = tableRows.indexOf(setHandler);
        int pos = setHandler.typeHandlers.indexOf(this);
        setHandler.typeHandlers.remove(pos);
        if(setHandler.expanded){
          tableRows.remove(setRow + pos + 1);
          tableModel.fireTableRowsDeleted(setRow + pos + 1, setRow + pos + 1);
        }
        if(setHandler.typeHandlers.isEmpty()){
          //the set has no more handlers
          setHandler.expanded = false;
          tableModel.fireTableRowsUpdated(setRow, setRow);
        }
      }
    }
    
    boolean selected;
    Map hghltTagsForAnn;
    String name;
    SetHandler setHandler;
    Color colour;
  }
  
  List setHandlers;
  List tableRows; 
  JTable mainTable;
  SetsTableModel tableModel;
  JScrollPane scroller;
  TextualDocumentView textView;
  
  protected ColorGenerator colourGenerator;
  private static final int NAME_COL = 1;
  private static final int SELECTED_COL = 0;
  
}
