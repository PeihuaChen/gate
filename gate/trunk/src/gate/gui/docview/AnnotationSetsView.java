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
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.event.MouseInputListener;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.*;
import javax.swing.text.BadLocationException;

import gate.*;
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
import gate.util.InvalidOffsetException;

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
    mainTable = new XJTable();
    tableModel = new SetsTableModel();
    ((XJTable)mainTable).setSortable(false);
    mainTable.setModel(tableModel);
//    mainTable.setRowMargin(0);
//    mainTable.getColumnModel().setColumnMargin(0);
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
    
    mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    
    constraints.gridy = 0;
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridwidth = 2;
    constraints.weighty = 1;
    constraints.weightx = 1;
    constraints.fill = GridBagConstraints.BOTH;
    mainPanel.add(scroller, constraints);
    
    constraints.gridy = 1;
    constraints.gridwidth = 1;
    constraints.weighty = 0;
    newSetNameTextField = new JTextField();
    mainPanel.add(newSetNameTextField, constraints);
    constraints.weightx = 0;
    newSetAction = new NewAnnotationSetAction();
    mainPanel.add(new JButton(newSetAction), constraints);
    initListeners();
  }
  
  public Component getGUI(){
    return mainPanel;
  }

  protected Color getColor(String annotationType){
    Preferences prefRoot = Preferences.userNodeForPackage(getClass());
    int rgba = prefRoot.getInt(annotationType, -1);
    Color colour;
    if(rgba == -1){
      //initialise and save
      float components[] = colourGenerator.getNextColor().getComponents(null);
      colour = new Color(components[0],
                         components[1],
                         components[2],
                         0.5f);
      int rgb = colour.getRGB();
      int alpha = colour.getAlpha();
      rgba = rgb | (alpha << 24);
      prefRoot.putInt(annotationType, rgba);
    }else{
      colour = new Color(rgba, true);
    }
    return colour;
  }
  
  protected void saveColor(String annotationType, Color colour){
    Preferences prefRoot = Preferences.userNodeForPackage(getClass());
    int rgb = colour.getRGB();
    int alpha = colour.getAlpha();
    int rgba = rgb | (alpha << 24);
    prefRoot.putInt(annotationType, rgba);
  }
  
  /**
   * This method will be called whenever the view becomes active. Implementers 
   * should use this to add hooks (such as mouse listeners) to the other views
   * as required by their functionality. 
   */
  protected void registerHooks(){
    textPane.addMouseListener(textMouseListener);
    textPane.addMouseMotionListener(textMouseListener);
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
    textPane.removeAncestorListener(textAncestorListener);
  }
  
  
  protected void initListeners(){
    document.addDocumentListener(this);
    mainTable.addComponentListener(new ComponentAdapter(){
      public void componentResized(ComponentEvent e){
        //trigger a resize for the columns
        mainTable.adjustSizes();
//        tableModel.fireTableRowsUpdated(0, 0);
      }
    });
    
    mainTable.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent evt){
        int row =  mainTable.rowAtPoint(evt.getPoint());
        int column = mainTable.columnAtPoint(evt.getPoint());
        if(row >= 0 && column == NAME_COL){
          Object handler = tableRows.get(row);
          if(handler instanceof TypeHandler){
            TypeHandler tHandler = (TypeHandler)handler;
            if(evt.getClickCount() >= 2){
              //double click
              tHandler.changeColourAction.actionPerformed(null);
            }
          }
        }
      }
      public void mousePressed(MouseEvent evt){
        int row =  mainTable.rowAtPoint(evt.getPoint());
        int column = mainTable.columnAtPoint(evt.getPoint());
        if(row >= 0 && column == NAME_COL){
          Object handler = tableRows.get(row);
          if(handler instanceof TypeHandler){
            TypeHandler tHandler = (TypeHandler)handler;
            if(evt.isPopupTrigger()){
              //show popup
              JPopupMenu popup = new JPopupMenu();
              popup.add(tHandler.changeColourAction);
              popup.show(mainTable, evt.getX(), evt.getY());
            }
          }
        }
      }
      
      public void mouseReleased(MouseEvent evt){
        int row =  mainTable.rowAtPoint(evt.getPoint());
        int column = mainTable.columnAtPoint(evt.getPoint());
        if(row >= 0 && column == NAME_COL){
          Object handler = tableRows.get(row);
          if(handler instanceof TypeHandler){
            TypeHandler tHandler = (TypeHandler)handler;
            if(evt.isPopupTrigger()){
              //show popup
              JPopupMenu popup = new JPopupMenu();
              popup.add(tHandler.changeColourAction);
              popup.show(mainTable, evt.getX(), evt.getY());
            }
          }
        }
      }
    });
    
    
    mouseStoppedMovingAction = new MouseStoppedMovingAction();
    mouseMovementTimer = new javax.swing.Timer(MOUSE_MOVEMENT_TIMER_DELAY, 
            mouseStoppedMovingAction);
    mouseMovementTimer.setRepeats(false);
    textMouseListener = new TextMouseListener();
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
    
    mainTable.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteAll");
    mainTable.getActionMap().put("deleteAll", 
            new DeleteSelectedAnnotationGroupAction());
    newSetNameTextField.getInputMap().put(
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "newSet");
    newSetNameTextField.getActionMap().put("newSet", newSetAction);
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
      j += previousHandler.typeHandlers.size();
    }
    j++;
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
      //remove highlights if any
      Iterator typeIter = sHandler.typeHandlers.iterator();
      while(typeIter.hasNext()){
        TypeHandler tHandler = (TypeHandler)typeIter.next();
        tHandler.setSelected(false);
      }
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
      sHandler.cleanup();
    }
  }//public void annotationSetRemoved(DocumentEvent e) 
  
  /**Called when the content of the document has changed through an edit 
   * operation.
   */
  public void contentEdited(DocumentEvent e){
    //go through all the type handlers and propagate the event
    Iterator setIter = setHandlers.iterator();
    while(setIter.hasNext()){
      SetHandler sHandler = (SetHandler)setIter.next();
      Iterator typeIter = sHandler.typeHandlers.iterator();
      while(typeIter.hasNext()){
        TypeHandler tHandler = (TypeHandler)typeIter.next();
        if(tHandler.isSelected()) 
          tHandler.repairHighlights(e.getEditStart().intValue(), 
                  e.getEditEnd().intValue());
      }
    }
  }
  
  
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
  
  /**
   * Sets the last annotation type created (which will be used as a default
   * for creating new annotations).
   * @param annType the type of annotation.
   */
  void setLastAnnotationType(String annType){
    this.lastAnnotationType = annType;
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
      typeChk.setOpaque(true);
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
      
      normalBorder = BorderFactory.createLineBorder(
              mainTable.getBackground(), 2);
      selectedBorder = BorderFactory.createLineBorder(
              mainTable.getSelectionBackground(), 2);
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
            typeLabel.setBorder(isSelected ? selectedBorder : normalBorder);
            return typeLabel;
          case SELECTED_COL:
            typeChk.setBackground(isSelected ?
       		         table.getSelectionBackground() :
        		       table.getBackground());
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
    protected Border selectedBorder;
    protected Border normalBorder;
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
      colour = getColor(name);
      hghltTagsForAnn = new HashMap();
      changeColourAction = new ChangeColourAction();
    }
    
    public void setColour(Color colour){
      if(this.colour.equals(colour)) return;
      this.colour = colour;
      saveColor(name, colour);
      if(isSelected()){
        //redraw the highlights
        Runnable runnable = new Runnable(){
          public void run(){
            //hide highlights
            textView.removeHighlights(hghltTagsForAnn.values());
            hghltTagsForAnn.clear();
            //show highlights
            List annots = new ArrayList(setHandler.set.get(name));
            List tags = textView.addHighlights(annots, setHandler.set, 
                    TypeHandler.this.colour);
            for(int i = 0; i < annots.size(); i++){
              hghltTagsForAnn.put(((Annotation)annots.get(i)).getId(), tags.get(i));
            }
          }
        };
        Thread thread = new Thread(runnable);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
      }
      //update the table display
      SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          int row = tableRows.indexOf(this);
          if(row >= 0) tableModel.fireTableRowsUpdated(row, row);
        }
      });
    }
    
    public void setSelected(boolean selected){
      if(this.selected == selected) return;
      this.selected = selected;
      final List annots = new ArrayList(setHandler.set.get(name));
      if(selected){
        //make sure set is expanded
        setHandler.setExpanded(true);
      	//show highlights
        hghltTagsForAnn.clear();
        Iterator annIter = annots.iterator();
        //we're doing a lot of operations so let's get out of the UI thread
        Runnable runnable = new Runnable(){
          public void run(){
            //do all operations in one go
            List tags = textView.addHighlights(annots, setHandler.set, colour);
            for(int i = 0; i < annots.size(); i++){
              hghltTagsForAnn.put(((Annotation)annots.get(i)).getId(), tags.get(i));
            }
          }
        };
        Thread thread = new Thread(runnable);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
      }else{
      	//hide highlights
        Runnable runnable = new Runnable(){
          public void run(){
            //do all operations in one go
            textView.removeHighlights(hghltTagsForAnn.values());
            hghltTagsForAnn.clear();
          }
        };
        Thread thread = new Thread(runnable);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
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
      if(selected) hghltTagsForAnn.put(ann.getId(), 
              textView.addHighlight(ann, setHandler.set, colour));
    }
    
    /**
     * Notifies this type handler that an annotation has been removed
     * @param ann the removed annotation
     */
    public void annotationRemoved(Annotation ann){
      if(selected){
        Object tag = hghltTagsForAnn.remove(ann.getId());
        textView.removeHighlight(tag);
      }
      //if this was the last annotation of this type then the handler is no
      //longer required
      Set remainingAnns = setHandler.set.get(name); 
      if(remainingAnns == null || remainingAnns.isEmpty()){
        setHandler.removeType(this);
      }
    }
    
    protected void repairHighlights(int start, int end){
      //map from tag to annotation
      List tags = new ArrayList(hghltTagsForAnn.size());
      List annots = new ArrayList(hghltTagsForAnn.size());
      Iterator annIter = hghltTagsForAnn.keySet().iterator();
      while(annIter.hasNext()){
        Annotation ann = setHandler.set.get((Integer)annIter.next());
        int annStart = ann.getStartNode().getOffset().intValue();
        int annEnd = ann.getEndNode().getOffset().intValue();
        if((annStart <= start && start <= annEnd) ||
           (start <= annStart && annStart <= end)){
          if(!hghltTagsForAnn.containsKey(ann.getId())){
            System.out.println("Error!!!");
          }
          tags.add(hghltTagsForAnn.get(ann.getId()));
          annots.add(ann);
        }
      }
      for(int i = 0; i < tags.size(); i++){
        Object tag = tags.get(i);
        Annotation ann = (Annotation)annots.get(i);
        try{
          textView.moveHighlight(tag, 
                  ann.getStartNode().getOffset().intValue(), 
                  ann.getEndNode().getOffset().intValue());
        }catch(BadLocationException ble){
          //this should never happen as the offsets come from an annotation
        }
      }
    }
    
    
    protected class ChangeColourAction extends AbstractAction{
      public ChangeColourAction(){
        super("Change colour");
      }
      
      public void actionPerformed(ActionEvent evt){
        Color col = JColorChooser.showDialog(mainTable, 
                "Select colour for \"" + name + "\"",
                colour);
        if(col != null){
          Color colAlpha = new Color(col.getRed(), col.getGreen(),
                  col.getBlue(), 128);
          setColour(colAlpha);
        }
      }
    }
    
    ChangeColourAction changeColourAction;
    boolean selected;
    //Map from annotation ID (which is imuttable) to tag
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
    public void mouseDragged(MouseEvent e){
      mouseStoppedMovingAction.setTextLocation(textPane.viewToModel(e.getPoint()));
      mouseMovementTimer.restart();
    }
    
    public void mouseMoved(MouseEvent e){
      //this triggers select annotation leading to edit annotation or new 
      //annotation actions
      mouseStoppedMovingAction.setTextLocation(textPane.viewToModel(e.getPoint()));
      mouseMovementTimer.restart();
    }
    
    public void mouseClicked(MouseEvent e){
      //this is required so we can trigger new annotation when selecting text 
      //by double/triple clicking
      mouseStoppedMovingAction.setTextLocation(textPane.viewToModel(e.getPoint()));
      mouseMovementTimer.restart();
    }
    
    public void mousePressed(MouseEvent e){
      
    }
    public void mouseReleased(MouseEvent e){
      
    }
    
    public void mouseEntered(MouseEvent e){
      
    }
    
    public void mouseExited(MouseEvent e){
      mouseMovementTimer.stop();
    }
  }//protected class TextMouseListener implements MouseInputListener
  
    
  protected class NewAnnotationSetAction extends AbstractAction{
    public NewAnnotationSetAction(){
      super("New");
      putValue(SHORT_DESCRIPTION, "Creates a new annotation set");
    }
    
    public void actionPerformed(ActionEvent evt){
      String name = newSetNameTextField.getText();
      newSetNameTextField.setText("");
      if(name != null && name.length() > 0){
        document.getAnnotations(name);
      }
    }
  }

  protected class NewAnnotationAction extends AbstractAction{
    public void actionPerformed(ActionEvent evt){
      int start = textPane.getSelectionStart();
      int end = textPane.getSelectionEnd();
      if(start != end){
        textPane.setSelectionStart(start);
        textPane.setSelectionEnd(start);
        //create a new annotation
        //find the selected set
        int row = mainTable.getSelectedRow();
        //select the default annotation set if none selected
        if(row < 0) row = 0;
        //find the set handler
        while(!(tableRows.get(row) instanceof SetHandler)) row --;
        AnnotationSet set = ((SetHandler)tableRows.get(row)).set;
        try{
	        Integer annId =  set.add(new Long(start), new Long(end), 
	                lastAnnotationType, Factory.newFeatureMap());
	        Annotation ann = set.get(annId);
	        //make sure new annotaion is visible
	        setTypeSelected(set.getName(), ann.getType(), true);
	        //show the editor
	        annotationEditor.setAnnotation(ann, set);
	        annotationEditor.show(true);
        }catch(InvalidOffsetException ioe){
          //this should never happen
          throw new GateRuntimeException(ioe);
        }
      }
    }
  }
  
  /**
   * Used to select an annotation for editing.
   *
   */
  protected class MouseStoppedMovingAction extends AbstractAction{
    
    public void actionPerformed(ActionEvent evt){
      //first check for selection hovering
      //if inside selection, add new annotation.
      if(textPane.getSelectionStart() <= textLocation &&
         textPane.getSelectionEnd() >= textLocation){
        new NewAnnotationAction().actionPerformed(evt);
      }else{
        //now check for annotations at location
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
      putValue(SHORT_DESCRIPTION, aHandler.ann.getFeatures().toString());
      this.aHandler = aHandler;
    }
    
    public void actionPerformed(ActionEvent evt){
      annotationEditor.setAnnotation(aHandler.ann, aHandler.set);
      annotationEditor.show(true);
    }
    
    AnnotationHandler aHandler;
  }
  
  protected class DeleteSelectedAnnotationGroupAction extends AbstractAction{
    public DeleteSelectedAnnotationGroupAction(){
    }
    public void actionPerformed(ActionEvent evt){
      int row = mainTable.getSelectedRow();
      if(row >= 0){
        Object handler = tableRows.get(row);
        if(handler instanceof TypeHandler){
          TypeHandler tHandler = (TypeHandler)handler;
          AnnotationSet set = tHandler.setHandler.set;
          AnnotationSet toDeleteAS = set.get(tHandler.name);
          if(toDeleteAS != null){
            List toDelete = new ArrayList(toDeleteAS);
            set.removeAll(toDelete);
          }
        }else if(handler instanceof SetHandler){
          SetHandler sHandler = (SetHandler)handler;
          if(sHandler.set == document.getAnnotations()){
            //the default annotation set - clear
            sHandler.set.clear();
          }else{
            document.removeAnnotationSet(sHandler.set.getName());
          }
        }
      }
    }
  }  
  
  List setHandlers;
  List tableRows; 
  XJTable mainTable;
  SetsTableModel tableModel;
  JScrollPane scroller;
  JPanel mainPanel;
  JTextField newSetNameTextField;
  
  TextualDocumentView textView;
  JEditorPane textPane;
  AnnotationEditor annotationEditor;
  NewAnnotationSetAction newSetAction;
  
  /**
   * The listener for mouse and mouse motion events in the text view.
   */
  protected TextMouseListener textMouseListener;
  
  protected javax.swing.Timer mouseMovementTimer;
  private static final int MOUSE_MOVEMENT_TIMER_DELAY = 500;
  protected AncestorListener textAncestorListener; 
  protected MouseStoppedMovingAction mouseStoppedMovingAction;
  
  protected String lastAnnotationType = "_New_";
  
  protected ColorGenerator colourGenerator;
  private static final int NAME_COL = 1;
  private static final int SELECTED_COL = 0;
  
}
