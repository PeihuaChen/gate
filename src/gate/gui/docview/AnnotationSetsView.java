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
import java.util.List;

import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.*;
import javax.swing.event.MouseInputListener;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.*;
import javax.swing.text.BadLocationException;

import gate.Annotation;
import gate.AnnotationSet;
import gate.event.AnnotationSetEvent;
import gate.event.AnnotationSetListener;
import gate.event.DocumentEvent;
import gate.event.DocumentListener;
import gate.gui.MainFrame;
import gate.swing.ColorGenerator;
import gate.swing.XJTable;
import gate.util.GateRuntimeException;

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
    textPane = (JEditorPane)((JScrollPane)textView.getGUI())
            .getViewport().getView();
    
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
    mainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mainTable.setColumnSelectionAllowed(false);
    mainTable.setRowSelectionAllowed(true);
    
    mainTable.setTableHeader(null);
    mainTable.setShowGrid(false);
    mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    scroller = new JScrollPane(mainTable);
    scroller.getViewport().setOpaque(true);
    scroller.getViewport().setBackground(mainTable.getBackground());
    
    annotationEditor = new AnnotationEditor(textView, this);
    initListeners();
  }
  
  /**
   * This method will be called whenever the view becomes active. Implementers 
   * should use this to add hooks (such as mouse listeners) to the other views
   * as required by their functionality. 
   */
  protected void registerHooks(){
    textPane.addMouseListener(textMouseListener);
    textPane.addMouseMotionListener(textMouseListener);
    textPane.addCaretListener(textCaretListener);
    textPane.addAncestorListener(textAncestorListener);
  }

  /**
   * This method will be called whenever this view becomes inactive. 
   * Implementers should use it to unregister whatever hooks they registered
   * in {@link #registerHooks()}.
   *
   */
  protected void unregisterHooks(){
    textPane.removeMouseListener(textMouseListener);
    textPane.removeMouseMotionListener(textMouseListener);
    textPane.removeCaretListener(textCaretListener);
    textPane.removeAncestorListener(textAncestorListener);
  }
  
  
  protected void initListeners(){
    document.addDocumentListener(this);
    textMouseListener = new TextMouseListener();
    textCaretListener = new TextCaretListener();
    textAncestorListener = new AncestorListener(){
      public void ancestorAdded(AncestorEvent event){
        if(wasShowing) annotationEditor.show(false);
        wasShowing = false;
      }
      
      public void ancestorRemoved(AncestorEvent event){
        if(annotationEditor.isShowing()){
          wasShowing = true;
          annotationEditor.hide();
        }
      }
      
      public void ancestorMoved(AncestorEvent event){
        
      }
      private boolean wasShowing = false; 
    };
    
    mainTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent e){
          int selectedRow = mainTable.getSelectedRow();
          if(selectedRow >= 0){
	          while(!(tableRows.get(selectedRow) instanceof SetHandler)) 
	            selectedRow --;
	          mainTable.getSelectionModel().setSelectionInterval(selectedRow, 
	                  selectedRow);
          }
        }
    });
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
    if(previousHandler.isExpanded()){
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
      if(sHandler.isExpanded())
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
  
  public void setTypeSelected(final String setName, 
                              final String typeName, 
                              final boolean selected){
    
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        TypeHandler tHandler = getTypeHandler(setName, typeName);
        tHandler.setSelected(selected);
        int row = tableRows.indexOf(tHandler);
        tableModel.fireTableRowsUpdated(row, row);
      }
    });
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
            return new Boolean(((SetHandler)value).isExpanded());
          if(value instanceof TypeHandler) 
            return new Boolean(((TypeHandler)value).isSelected());
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
      setLabel.setOpaque(true);
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
//      typeChk.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

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
      setChk.setOpaque(true);
      
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
            setLabel.setBackground(isSelected ?
                                   table.getSelectionBackground() :
                                   table.getBackground());
            return setLabel;
          case SELECTED_COL:
            setChk.setSelected(sHandler.isExpanded());
            setChk.setEnabled(sHandler.typeHandlers.size() > 0);
            setChk.setBackground(isSelected ?
                    		         table.getSelectionBackground() :
                    		         table.getBackground());
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
            typeChk.setSelected(tHandler.isSelected());
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
      setChk.setOpaque(true);
      setChk.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          fireEditingStopped();
        }
      });
      typeChk = new JCheckBox();
      typeChk.setOpaque(false);
//      typeChk.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
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
            setChk.setSelected(sHandler.isExpanded());
            setChk.setEnabled(sHandler.typeHandlers.size() > 0);
            setChk.setBackground(isSelected ?
       		         	             table.getSelectionBackground() :
       		         	             table.getBackground());
            currentChk = setChk;
            return setChk;
        }
      }else if(value instanceof TypeHandler){
        TypeHandler tHandler = (TypeHandler)value;
        switch(column){
          case NAME_COL: return null;
          case SELECTED_COL:
//            typeChk.setBackground(tHandler.colour);
            typeChk.setSelected(tHandler.isSelected());
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
      return true;
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
      typeHandlersByType = new HashMap();
      List typeNames = new ArrayList(set.getAllTypes());
      Collections.sort(typeNames);
      Iterator typIter = typeNames.iterator();
      while(typIter.hasNext()){
        String name = (String)typIter.next();
        TypeHandler tHandler = new TypeHandler(this, name); 
        typeHandlers.add(tHandler);
        typeHandlersByType.put(name, tHandler);
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
      typeHandlersByType.put(type, tHandler);
      int setRow = tableRows.indexOf(this);
      if(typeHandlers.size() == 1) 
        tableModel.fireTableRowsUpdated(setRow, setRow);
      if(expanded){
        tableRows.add(setRow + pos + 1, tHandler);
        tableModel.fireTableRowsInserted(setRow + pos + 1,
              setRow + pos + 1);
      }
      return tHandler;
    }
    
    public void removeType(TypeHandler tHandler){
      int setRow = tableRows.indexOf(this);
      int pos = typeHandlers.indexOf(tHandler);
      typeHandlers.remove(pos);
      typeHandlersByType.remove(tHandler.name);
      if(expanded){
        tableRows.remove(setRow + pos + 1);
        tableModel.fireTableRowsDeleted(setRow + pos + 1, setRow + pos + 1);
      }
      if(typeHandlers.isEmpty()){
        //the set has no more handlers
        setExpanded(false);
        tableModel.fireTableRowsUpdated(setRow, setRow);
      }
    }
    
    public void removeType(String type){
      removeType((TypeHandler)typeHandlersByType.get(type));
    }

    public TypeHandler getTypeHandler(String type){
      return (TypeHandler)typeHandlersByType.get(type);
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
      tableModel.fireTableRowsUpdated(myPosition, myPosition);
    }
    
    public boolean isExpanded(){
      return expanded;
    }
    
    
    AnnotationSet set;
    List typeHandlers;
    Map typeHandlersByType;
    private boolean expanded = false;
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
      //update the table display
      int row = tableRows.indexOf(this);
      tableModel.fireTableRowsUpdated(row, row);
    }
    
    public boolean isSelected(){
      return selected;
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
        setHandler.removeType(this);
      }
    }
    
    boolean selected;
    Map hghltTagsForAnn;
    String name;
    SetHandler setHandler;
    Color colour;
  }
  
  protected static class AnnotationHandler{
    public AnnotationHandler(AnnotationSet set, Annotation ann){
      this.ann = ann;
      this.set = set;
    }
    Annotation ann;
    AnnotationSet set;
  }
  
  /**
   * A mouse listener used for events in the text view. 
   */
  protected class TextMouseListener implements MouseInputListener{
    public TextMouseListener(){
      selectAction = new SelectAnnotationAction();
      timer = new javax.swing.Timer(DELAY, selectAction);
      timer.setRepeats(false);
    }
    
    public void mouseDragged(MouseEvent e){
      
    }
    public void mouseMoved(MouseEvent e){
      selectAction.setTextLocation(textPane.viewToModel(e.getPoint()));
      timer.restart();
      //get highlighted annotations
//      //first check if there is any highlight at the location
//      Highlighter.Highlight highlights[] = textPane.
//      	getHighlighter().getHighlights();
//      int i = 0;
//      for(;
//          i < highlights.length &&
//          (highlights[i].getStartOffset() > textPosition ||
//           highlights[i].getEndOffset() < textPosition);
//          i++);
//      if(highlights[i].getStartOffset() < textPosition &&
//         textPosition < highlights[i].getEndOffset()){
//        //there is a highlight going through the current point
      //find the highlighted annotation[s]
    }
    
    public void mouseClicked(MouseEvent e){
      
    }
    
    public void mousePressed(MouseEvent e){
      
    }
    public void mouseReleased(MouseEvent e){
      
    }
    
    public void mouseEntered(MouseEvent e){
      
    }
    
    public void mouseExited(MouseEvent e){
      timer.stop();
      
    }
    
    javax.swing.Timer timer;
    SelectAnnotationAction selectAction;
    public static final int DELAY = 300;
    
  }//protected class TextMouseListener implements MouseInputListener
  
  
  protected class TextCaretListener implements CaretListener{
    public TextCaretListener(){
      caretListenerTimer = new javax.swing.Timer(TIMER_DELAY,
      		new NewAnnotationAction());
      caretListenerTimer.setRepeats(false);
    }
    
    public void caretUpdate(CaretEvent e){
      caretListenerTimer.stop();
      int dot = e.getDot();
      int mark = e.getMark();
      if(dot == mark) return;
      caretListenerTimer.restart();
    }
    
    javax.swing.Timer caretListenerTimer;
    private static final int TIMER_DELAY = 500;
  }
  
  protected class NewAnnotationAction extends AbstractAction{
    public void actionPerformed(ActionEvent evt){
      JOptionPane.showMessageDialog(textPane, "New Annotation?");
    }
  }
  /**
   * Used to select an annotation for editing.
   *
   */
  protected class SelectAnnotationAction extends AbstractAction{
    public SelectAnnotationAction(){
      super("Edit annotation");
    }
    
    public void actionPerformed(ActionEvent evt){
      List annotsAtPoint = new ArrayList();
      Iterator shIter = setHandlers.iterator();
      while(shIter.hasNext()){
        SetHandler sHandler = (SetHandler)shIter.next();
        Iterator annIter = sHandler.set.get(new Long(textLocation),
                                            new Long(textLocation)).iterator();
        while(annIter.hasNext()){
          Annotation ann = (Annotation)annIter.next();
          if(sHandler.getTypeHandler(ann.getType()).isSelected()){
            annotsAtPoint.add(new AnnotationHandler(sHandler.set, ann));
          }
        }
      }
      
      if(annotsAtPoint.size() > 0){
        if(annotsAtPoint.size() > 1){
	        JPopupMenu popup = new JPopupMenu();
	        Iterator annIter = annotsAtPoint.iterator();
	        while(annIter.hasNext()){
	          AnnotationHandler aHandler = (AnnotationHandler)annIter.next();
	          popup.add(new HighlightMenuItem(
	                  new EditAnnotationAction(aHandler),
	                  aHandler.ann.getStartNode().getOffset().intValue(),
	                  aHandler.ann.getEndNode().getOffset().intValue(),
	                  popup));
	        }
	        try{
		        Rectangle rect =  textPane.modelToView(textLocation);
		        popup.show(textPane, rect.x + 10, rect.y);
	        }catch(BadLocationException ble){
	          throw new GateRuntimeException(ble);
	        }
	      }else{
	        //only one annotation: start the editing directly
	        new EditAnnotationAction((AnnotationHandler)annotsAtPoint.get(0)).
	        	actionPerformed(null);
	      }
      }
    }
    
    public void setTextLocation(int textLocation){
      this.textLocation = textLocation;
    }
    int textLocation;
  }//protected class SelectAnnotationAction extends AbstractAction{
  
  
  /**
   * The popup menu items used to select annotations
   * Apart from the normal {@link javax.swing.JMenuItem} behaviour, this menu
   * item also highlights the annotation which it would select if pressed.
   */
  protected class HighlightMenuItem extends JMenuItem {
    public HighlightMenuItem(Action action, int startOffset, int endOffset, 
            JPopupMenu popup) {
      super(action);
      this.start = startOffset;
      this.end = endOffset;
      this.addMouseListener(new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
          showHighlight();
        }

        public void mouseExited(MouseEvent e) {
          removeHighlight();
        }
      });
      popup.addPopupMenuListener(new PopupMenuListener(){
        public void popupMenuWillBecomeVisible(PopupMenuEvent e){
          
        }
        public void popupMenuCanceled(PopupMenuEvent e){
          removeHighlight();
        }
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e){
          removeHighlight();
        }
        
        
      });
    }
    
    protected void showHighlight(){
      try {
        highlight = textPane.getHighlighter().addHighlight(start, end,
                                        DefaultHighlighter.DefaultPainter);
      }catch(BadLocationException ble){
        throw new GateRuntimeException(ble.toString());
      }

    }
    
    protected void removeHighlight(){
      if(highlight != null){
        textPane.getHighlighter().removeHighlight(highlight);
        highlight = null;
      }
      
    }

    int start;
    int end;
    Action action;
    Object highlight;
  }
  
  
  
  protected class EditAnnotationAction extends AbstractAction{
    public EditAnnotationAction(AnnotationHandler aHandler){
      super(aHandler.ann.getType() + " [" + 
              (aHandler.set.getName() == null ? "  " : 
                aHandler.set.getName()) +
              "]");
      
      this.aHandler = aHandler;
    }
    
    public void actionPerformed(ActionEvent evt){
      annotationEditor.setAnnotation(aHandler.ann, aHandler.set);
      annotationEditor.show(true);
    }
    
    AnnotationHandler aHandler;
  }
  
  
  
  List setHandlers;
  List tableRows; 
  JTable mainTable;
  SetsTableModel tableModel;
  JScrollPane scroller;
  TextualDocumentView textView;
  JEditorPane textPane;
  AnnotationEditor annotationEditor;
  
  /**
   * The listener for mouse and mouse motion events in the text view.
   */
  protected TextMouseListener textMouseListener;
  
  protected TextCaretListener textCaretListener; 
  
  protected AncestorListener textAncestorListener; 
  
  
  protected ColorGenerator colourGenerator;
  private static final int NAME_COL = 1;
  private static final int SELECTED_COL = 0;
  
}
