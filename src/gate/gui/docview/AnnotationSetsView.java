/*
 * Created on Mar 23, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package gate.gui.docview;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;

import gate.*;
import gate.event.*;
import gate.event.DocumentEvent;
import gate.event.DocumentListener;
import gate.gui.*;
import gate.swing.ColorGenerator;
import gate.swing.XJTable;
import gate.util.*;

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
    setHandlers = new ArrayList<SetHandler>();
    tableRows = new ArrayList();
    visibleAnnotationTypes = new ArrayList();
    colourGenerator = new ColorGenerator();
    actions = new ArrayList();
    actions.add(new SavePreserveFormatAction());
    pendingEvents = new LinkedList<GateEvent>();
    eventMinder = new Timer(EVENTS_HANDLE_DELAY, 
            new HandleDocumentEventsAction());
    eventMinder.setRepeats(true);
    eventMinder.setCoalesce(true);    
  }
  
  public List getActions() {
    return actions;
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
    textPane = (JTextArea)((JScrollPane)textView.getGUI())
            .getViewport().getView();
    
    //get a pointer to the list view
    Iterator horizontalViewsIter = owner.getHorizontalViews().iterator();
    while(listView == null && horizontalViewsIter.hasNext()){
      DocumentView aView = (DocumentView)horizontalViewsIter.next();
      if(aView instanceof AnnotationListView) 
        listView = (AnnotationListView)aView;
    }
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

    populateUI();
    tableModel.fireTableDataChanged();
    
    
    eventMinder.start();    
    initListeners();
  }
  
  protected void populateUI(){
    setHandlers.add(new SetHandler(document.getAnnotations()));
    List setNames = document.getNamedAnnotationSets() == null ?
            new ArrayList() :
            new ArrayList(document.getNamedAnnotationSets().keySet());
    Collections.sort(setNames);
    Iterator setsIter = setNames.iterator();
    while(setsIter.hasNext()){
      setHandlers.add(new SetHandler(document.
              getAnnotations((String)setsIter.next())));
    }
    tableRows.addAll(setHandlers);
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
   * Enables or disables creation of the new annotation set.
   */
  public void setNewAnnSetCreationEnabled(boolean b) {
		newSetAction.setEnabled(b);
		newSetNameTextField.setEnabled(b);
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
    restoreSelectedTypes();
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
    storeSelectedTypes();
  }
  

  /**
   * Populates the {@link #visibleAnnotationTypes} structure based on the 
   * current selection
   *
   */
  protected void storeSelectedTypes(){
    visibleAnnotationTypes.clear();
    for(SetHandler sHandler:setHandlers){
      for(TypeHandler tHandler: sHandler.typeHandlers){
        if(tHandler.isSelected()){
          visibleAnnotationTypes.add(new String[]{sHandler.set.getName(), 
            tHandler.name});
          tHandler.setSelected(false);
        }
      }
    }
  }
  
  /**
   * Restores the selected types based on the state saved in the 
   * {@link #visibleAnnotationTypes} data structure.
   */
  protected void restoreSelectedTypes(){
    for(String[] typeSpec: visibleAnnotationTypes){
      TypeHandler tHandler = getTypeHandler(typeSpec[0], typeSpec[1]);
      tHandler.setSelected(true);
    }
  }

  protected void initListeners(){
    document.addDocumentListener(this);
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
    for(SetHandler sHandler : setHandlers){
      sHandler.set.removeAnnotationSetListener(AnnotationSetsView.this);
    }    
    eventMinder.stop();
    synchronized(this) {
      pendingEvents.clear();  
    }
    super.cleanup();
    document = null;
  }
  
  public void annotationSetAdded(final DocumentEvent e) {
    synchronized(AnnotationSetsView.this) {
      pendingEvents.offer(e);
      eventMinder.restart();
    }
    
//    Runnable runner = new Runnable(){
//      public void run(){
//        String newSetName = e.getAnnotationSetName();
//        SetHandler sHandler = new SetHandler(document.getAnnotations(newSetName));
//        //find the right location for the new set
//        //this is a named set and the first one is always the default one
//        int i = 1;
//        for(;
//            i < setHandlers.size() && 
//            ((SetHandler)setHandlers.get(i)).set.
//            	getName().compareTo(newSetName) <= 0;
//            i++);
//        setHandlers.add(i, sHandler);
//        //update the tableRows list
//        SetHandler previousHandler = (SetHandler)setHandlers.get(i -1);
//        //find the index for the previous handler - which is guaranteed to exist
//        int j = 0;
//        for(;
//        	tableRows.get(j) != previousHandler;
//          j++);
//        if(previousHandler.isExpanded()){
//          j += previousHandler.typeHandlers.size();
//        }
//        j++;
//        tableRows.add(j, sHandler);
//        //update the table view
//        tableModel.fireTableRowsInserted(j, j);
//      }
//    };
//    SwingUtilities.invokeLater(runner);
  }//public void annotationSetAdded(DocumentEvent e) 
  
  public void annotationSetRemoved(final DocumentEvent e) {
    synchronized(AnnotationSetsView.this) {
      pendingEvents.offer(e);
      eventMinder.restart();
    }
    
//    Runnable runner = new Runnable(){
//      public void run(){
//        String setName = e.getAnnotationSetName();
//        //find the handler and remove it from the list of handlers
//    //    Iterator shIter = setHandlers.iterator();
//        SetHandler sHandler = getSetHandler(setName);
//        if(sHandler != null){
//          //remove highlights if any
//          Iterator typeIter = sHandler.typeHandlers.iterator();
//          while(typeIter.hasNext()){
//            TypeHandler tHandler = (TypeHandler)typeIter.next();
//            tHandler.setSelected(false);
//          }
//          setHandlers.remove(sHandler);
//          //remove the set from the table
//          int row = tableRows.indexOf(sHandler);
//          tableRows.remove(row);
//          int removed = 1;
//          //remove the type rows as well
//          if(sHandler.isExpanded())
//            for(int i = 0; i < sHandler.typeHandlers.size(); i++){ 
//              tableRows.remove(row);
//              removed++;
//            }
//          tableModel.fireTableRowsDeleted(row, row + removed -1);
//          sHandler.cleanup();
//        }
//      }
//    };
//    SwingUtilities.invokeLater(runner);
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
  
  
  public void annotationAdded(final AnnotationSetEvent e) {
    synchronized(AnnotationSetsView.this) {
      pendingEvents.offer(e);
      eventMinder.restart();
    }
//    
//    Runnable runner = new Runnable(){
//      public void run(){
//        AnnotationSet set = (AnnotationSet)e.getSource();
//        Annotation ann = e.getAnnotation();
//        TypeHandler tHandler = getTypeHandler(set.getName(), ann.getType());
//        if(tHandler == null){
//          //new type for this set
//          SetHandler sHandler = getSetHandler(set.getName());
//          tHandler = sHandler.newType(ann.getType());
//        }
//        tHandler.annotationAdded(ann);        
//      }
//    };
//    SwingUtilities.invokeLater(runner);
  }
  
  public void annotationRemoved(final AnnotationSetEvent e) {
    synchronized(AnnotationSetsView.this) {
      pendingEvents.offer(e);
      eventMinder.restart();
    }
    
//    //we need to find out if this was the last annotation of its kind
//    //this needs to be done from this thread to avoid concurrent modifications
//    String annType = e.getAnnotation().getType();
//    Set<String> remainingTypes = ((AnnotationSet)e.getSource()).getAllTypes();
//    final boolean lastOfItsType = !remainingTypes.contains(annType);
//    Runnable runner = new Runnable(){
//      public void run(){
//        AnnotationSet set = (AnnotationSet)e.getSource();
//        Annotation ann = e.getAnnotation();
//        TypeHandler tHandler = getTypeHandler(set.getName(), ann.getType());
//        tHandler.annotationRemoved(ann, lastOfItsType);
//      }
//    };
//    SwingUtilities.invokeLater(runner);
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
        mainTable.getSelectionModel().setSelectionInterval(row, row);
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
      setChk.setSelectedIcon(MainFrame.getIcon("expanded"));
      setChk.setIcon(MainFrame.getIcon("closed"));
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
      setChk.setSelectedIcon(MainFrame.getIcon("expanded"));
      setChk.setIcon(MainFrame.getIcon("closed"));
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
      typeHandlers = new ArrayList<TypeHandler>();
      typeHandlersByType = new HashMap();
      List typeNames = new ArrayList(set.getAllTypes());
      Collections.sort(typeNames);
      Iterator typIter = typeNames.iterator();
      while(typIter.hasNext()){
        String name = (String)typIter.next();
        TypeHandler tHandler = new TypeHandler(this, name);
        tHandler.annotationCount = set.get(name).size();
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
      //preserve table selection
      int row = mainTable.getSelectedRow();
      int setRow = tableRows.indexOf(this);
      if(typeHandlers.size() == 1) 
        tableModel.fireTableRowsUpdated(setRow, setRow);
      if(expanded){
        tableRows.add(setRow + pos + 1, tHandler);
        tableModel.fireTableRowsInserted(setRow + pos + 1,
              setRow + pos + 1);
      }
      //restore selection if any
      if(row != -1) mainTable.getSelectionModel().setSelectionInterval(row, row);
      return tHandler;
    }
    
    public void removeType(TypeHandler tHandler){
      int setRow = tableRows.indexOf(this);
      int pos = typeHandlers.indexOf(tHandler);
      if(setRow != -1 && pos != -1){
        typeHandlers.remove(pos);
        typeHandlersByType.remove(tHandler.name);
        //preserve table selection
        int row = mainTable.getSelectedRow();
        if(expanded){
          tableRows.remove(setRow + pos + 1);
          tableModel.fireTableRowsDeleted(setRow + pos + 1, setRow + pos + 1);
          if(row >= (setRow + pos + 1)) row--;
        }
        if(typeHandlers.isEmpty()){
          //the set has no more handlers
          setExpanded(false);
          tableModel.fireTableRowsUpdated(setRow, setRow);
        }
        //restore selection if any
        if(row != -1) mainTable.getSelectionModel().setSelectionInterval(row, row);
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
    List<TypeHandler> typeHandlers;
    Map typeHandlersByType;
    private boolean expanded = false;
  }
  
  protected class TypeHandler{
    TypeHandler (SetHandler setHandler, String name){
      this.setHandler = setHandler;
      this.name = name;
      colour = getColor(name);
      hghltTagsForAnn = new HashMap();
      annListTagsForAnn = new HashMap();
      changeColourAction = new ChangeColourAction();
      annotationCount = 0;
    }
    
    public void setColour(Color colour){
      if(this.colour.equals(colour)) return;
      this.colour = colour;
      saveColor(name, colour);
      if(isSelected()){
        //redraw the highlights
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
      //update the table display
      int row = tableRows.indexOf(this);
      if(row >= 0) tableModel.fireTableRowsUpdated(row, row);
    }
    
    public void setSelected(boolean selected){
      if(this.selected == selected) return;
      this.selected = selected;
      final List<Annotation> annots = new ArrayList(setHandler.set.get(name));
      if(selected){
        //make sure set is expanded
        setHandler.setExpanded(true);
      	//show highlights
        hghltTagsForAnn.clear();
        List tags = textView.addHighlights(annots, setHandler.set, colour);
        for(int i = 0; i < annots.size(); i++){
          hghltTagsForAnn.put(((Annotation)annots.get(i)).getId(), tags.get(i));
        }
        //add to the list view
        annListTagsForAnn.clear();
        List<AnnotationListView.AnnotationData> listTags = 
            listView.addAnnotations(annots, setHandler.set);
        for(AnnotationListView.AnnotationData aData: listTags)
          annListTagsForAnn.put(aData.ann.getId(), aData);
      }else{
        //hide highlights
        try{
          textView.removeHighlights(hghltTagsForAnn.values());
          listView.removeAnnotations(annListTagsForAnn.values());
        }finally{
          hghltTagsForAnn.clear();
          annListTagsForAnn.clear();
        }
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
    public void annotationAdded(final Annotation ann){
      annotationCount++;
      if(selected){
        //add new highlight
        hghltTagsForAnn.put(ann.getId(), 
                textView.addHighlight(ann, setHandler.set, colour));
        annListTagsForAnn.put(ann.getId(), 
                listView.addAnnotation(ann, setHandler.set));
      }
    }
    
    /**
     * Notifies this type handler that an annotation has been removed
     * @param ann the removed annotation
     */
    public void annotationRemoved(Annotation ann){
      annotationCount--;
      if(selected){
        //single annotation removal
        Object tag = hghltTagsForAnn.remove(ann.getId());
        if(tag != null) textView.removeHighlight(tag);
        AnnotationListView.AnnotationData listTag = 
            annListTagsForAnn.remove(ann.getId());
        if(tag != null) listView.removeAnnotation(listTag);
      }
      //if this was the last annotation of this type then the handler is no
      //longer required
      if(annotationCount == 0){
        setHandler.removeType(TypeHandler.this);
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
    /**
     * Map from annotation ID (which is immutable) to highlight tag
     */
    Map hghltTagsForAnn;

    /**
     * Map from annotation ID (which is immutable) to AnnotationListView tag
     */
    Map<Integer, AnnotationListView.AnnotationData> annListTagsForAnn;
    
    String name;
    SetHandler setHandler;
    Color colour;
    int annotationCount;
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
      //do not create annotations while dragging
      mouseMovementTimer.stop();
    }
    
    public void mouseMoved(MouseEvent e){
      //this triggers select annotation leading to edit annotation or new 
      //annotation actions
      //ignore movement if CTRL pressed or dragging
      int modEx = e.getModifiersEx();
      if((modEx & MouseEvent.CTRL_DOWN_MASK) != 0){
        mouseMovementTimer.stop();
        return;
      }
      if((modEx & MouseEvent.BUTTON1_DOWN_MASK) != 0){
        mouseMovementTimer.stop();
        return;
      }
      mouseStoppedMovingAction.setTextLocation(textPane.viewToModel(e.getPoint()));
      mouseMovementTimer.restart();
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
        AnnotationSet set = document.getAnnotations(name);
        //select the newly added set
        Iterator rowsIter = tableRows.iterator();
        int row = -1;
        for(int i = 0; i < tableRows.size() && row < 0; i++){
          if(tableRows.get(i) instanceof SetHandler &&
             ((SetHandler)tableRows.get(i)).set == set) row = i;
        }
        if(row >= 0) mainTable.getSelectionModel().setSelectionInterval(row, row);
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
  
  protected class SavePreserveFormatAction extends AbstractAction{
    public SavePreserveFormatAction(){
      super("Save preserving document format");
    }
    
    public void actionPerformed(ActionEvent evt){
      Runnable runableAction = new Runnable(){
        public void run(){
          JFileChooser fileChooser = MainFrame.getFileChooser();
          File selectedFile = null;

          fileChooser.setMultiSelectionEnabled(false);
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          fileChooser.setDialogTitle("Select document to save ...");
          fileChooser.setSelectedFiles(null);

          int res = fileChooser.showDialog(owner, "Save");
          if(res == JFileChooser.APPROVE_OPTION){
            selectedFile = fileChooser.getSelectedFile();
            fileChooser.setCurrentDirectory(fileChooser.getCurrentDirectory());
            if(selectedFile == null) return;
            StatusListener sListener = (StatusListener)MainFrame.getListeners().
              get("gate.event.StatusListener");
            if (sListener != null) 
              sListener.statusChanged("Please wait while dumping annotations"+
              "in the original format to " + selectedFile.toString() + " ...");
            // This method construct a set with all annotations that need to be
            // dupmped as Xml. If the set is null then only the original markups
            // are dumped.
            Set annotationsToDump = new HashSet();
            Iterator setIter = setHandlers.iterator();
            while(setIter.hasNext()){
              SetHandler sHandler = (SetHandler)setIter.next();
              Iterator typeIter = sHandler.typeHandlers.iterator();
              while(typeIter.hasNext()){
                TypeHandler tHandler = (TypeHandler)typeIter.next();
                if(tHandler.isSelected()){
                  annotationsToDump.addAll(sHandler.set.get(tHandler.name));
                }
              }
            }
            
            try{
              // Prepare to write into the xmlFile using the original encoding
              String encoding = ((TextualDocument)document).getEncoding();

              OutputStreamWriter writer = new OutputStreamWriter(
                                            new FileOutputStream(selectedFile),
                                            encoding);

              //determine if the features need to be saved first
              Boolean featuresSaved =
                  Gate.getUserConfig().getBoolean(
                    GateConstants.SAVE_FEATURES_WHEN_PRESERVING_FORMAT);
              boolean saveFeatures = true;
              if (featuresSaved != null)
                saveFeatures = featuresSaved.booleanValue();
              // Write with the toXml() method
              writer.write(
                document.toXml(annotationsToDump, saveFeatures));
              writer.flush();
              writer.close();
            } catch (Exception ex){
              ex.printStackTrace(Out.getPrintWriter());
            }// End try
            if (sListener != null)
              sListener.statusChanged("Finished dumping into the "+
              "file : " + selectedFile.toString());
          }// End if
        }// End run()
      };// End Runnable
      Thread thread = new Thread(runableAction, "");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }
  
  protected class HandleDocumentEventsAction extends AbstractAction{

    public void actionPerformed(ActionEvent ev) {
      //see if we need to try again to rebuild from scratch
      if(uiDirty){
        //a previous call to rebuild has failed; try again
        rebuildDisplay();
        return;
      }
      //process the individual events
      synchronized(AnnotationSetsView.this) {
        //if too many individual events, then rebuild UI from scratch as it's 
        //faster.
        if(pendingEvents.size() > MAX_EVENTS){
          rebuildDisplay();
          return;
        }
        while(!pendingEvents.isEmpty()){
          GateEvent event = pendingEvents.remove();
          if(event instanceof DocumentEvent){
            DocumentEvent e = (DocumentEvent)event;
            if(event.getType() == DocumentEvent.ANNOTATION_SET_ADDED){
                String newSetName = e.getAnnotationSetName();
                SetHandler sHandler = new SetHandler(document.getAnnotations(newSetName));
                //find the right location for the new set
                //this is a named set and the first one is always the default one
                int i = 0;
                if(newSetName != null){
                  for(i = 1;
                      i < setHandlers.size() && 
                      ((SetHandler)setHandlers.get(i)).set.
                      getName().compareTo(newSetName) <= 0;
                      i++);
                }
                setHandlers.add(i, sHandler);
                //update the tableRows list
                int j = 0;
                if(i > 0){
                  SetHandler previousHandler = (SetHandler)setHandlers.get(i -1);
                  //find the index for the previous handler - which is guaranteed to exist
                  for(; tableRows.get(j) != previousHandler; j++);
                  if(previousHandler.isExpanded()){
                    j += previousHandler.typeHandlers.size();
                  }
                  j++;
                }
                tableRows.add(j, sHandler);
                //update the table view
                tableModel.fireTableRowsInserted(j, j);
            }else if(event.getType() == DocumentEvent.ANNOTATION_SET_REMOVED){
              String setName = e.getAnnotationSetName();
              //find the handler and remove it from the list of handlers
              SetHandler sHandler = getSetHandler(setName);
              if(sHandler != null){
                sHandler.set.removeAnnotationSetListener(AnnotationSetsView.this);
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
            }else{
              //some other kind of event we don't care about
            }
          }else if(event instanceof AnnotationSetEvent){
            AnnotationSetEvent e = (AnnotationSetEvent)event;
            AnnotationSet set = (AnnotationSet)e.getSource();
            Annotation ann = e.getAnnotation();
            if(event.getType() == AnnotationSetEvent.ANNOTATION_ADDED){
              TypeHandler tHandler = getTypeHandler(set.getName(), ann.getType());
              if(tHandler == null){
                //new type for this set
                SetHandler sHandler = getSetHandler(set.getName());
                tHandler = sHandler.newType(ann.getType());
              }
              tHandler.annotationAdded(ann);        
            }else if(event.getType() == AnnotationSetEvent.ANNOTATION_REMOVED){
              TypeHandler tHandler = getTypeHandler(set.getName(), ann.getType());
              if(tHandler != null) tHandler.annotationRemoved(ann);
            }else{
              //some other kind of event we don't care about
            }
          }else{
            //unknown type of event -> ignore
          }
        }
      }
    }
    
    /**
     * This method is used to update the display by reading the associated
     * document when it is considered that doing so would be cheaper than 
     * acting on the events queued
     */
    protected void rebuildDisplay(){
      //if there is a process still running, we may get concurrent modification 
      //exceptions, in which case we should give up and try again later.
      //this method will always run from the UI thread, so no synchronisation 
      //is necessary
      uiDirty = false;
      try{
        synchronized(AnnotationSetsView.this) {
          //ignore all pending events, as we're rebuilding from scratch
          pendingEvents.clear();
        }
        //store selection state
        Set<String> selectedAnnotationTypes = new HashSet<String>();
        for(SetHandler sHandler : setHandlers){
          for(TypeHandler tHandler : sHandler.typeHandlers){
            if(tHandler.isSelected()){
              selectedAnnotationTypes.add(sHandler.set.getName() + 
                      ":" + tHandler.name);
              //hide highlights, annotations listed
              tHandler.setSelected(false);
            }
//            sHandler.removeType(tHandler);                      
          }
          sHandler.typeHandlers.clear();
          sHandler.typeHandlersByType.clear();
          sHandler.set.removeAnnotationSetListener(AnnotationSetsView.this);
        }
        setHandlers.clear();
        tableRows.clear();
        listView.removeAnnotations(listView.getAllAnnotations());
        textView.removeAllBlinkingHighlights();
        //rebuild the UI
        populateUI();
        
        //restore the selection
        for(SetHandler sHandler : setHandlers){
          for(TypeHandler tHandler : sHandler.typeHandlers){
            if(selectedAnnotationTypes.remove(sHandler.set.getName() + 
                    ":" + tHandler.name)) {
              tHandler.setSelected(true);
            }
          }
        }
        tableModel.fireTableDataChanged();
      }catch(Throwable t){
        //something happened, we need to give up
        uiDirty = true;
t.printStackTrace();        
      }
    }
    
    boolean uiDirty = false;
    /**
     * Maximum number of events to treat individually. If we have more pending
     * events than this value, the UI will be rebuilt from scratch
     */
    private static final int MAX_EVENTS = 300;
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
          Iterator annIter = sHandler.set.get(new Long(textLocation - 1),
                                              new Long(textLocation + 1)).iterator();
          while(annIter.hasNext()){
            Annotation ann = (Annotation)annIter.next();
            TypeHandler tHandler = sHandler.getTypeHandler(ann.getType()); 
            if(sHandler != null && tHandler.isSelected()){
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
      
      //select the annotation being edited in the tabular view
      if(listView != null && listView.isActive() &&
              listView.getGUI().isVisible()){
        TypeHandler tHandler = getTypeHandler(aHandler.set.getName(), 
                aHandler.ann.getType());
        if(tHandler != null){
          Object tag = tHandler.annListTagsForAnn.get(aHandler.ann.getId());
          listView.selectAnnotationForTag(tag);
        }
      }
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
  
  List<SetHandler> setHandlers;
  List tableRows; 
  XJTable mainTable;
  SetsTableModel tableModel;
  JScrollPane scroller;
  JPanel mainPanel;
  JTextField newSetNameTextField;
  
  TextualDocumentView textView;
  AnnotationListView listView;
  JTextArea textPane;
  AnnotationEditor annotationEditor;
  NewAnnotationSetAction newSetAction;
  
  /**
   * The listener for mouse and mouse motion events in the text view.
   */
  protected TextMouseListener textMouseListener;
  
  /**
   * Stores the list of visible annotation types when the view is inactivated 
   * so that the selection can be restored when the view is made active again.
   * The values are String[2] pairs of form <set name, type>.
   */
  protected List<String[]> visibleAnnotationTypes;
  
  protected Timer mouseMovementTimer;
  /**
   * Timer used to handle events coming from the document
   */
  protected Timer eventMinder;
  
  protected Queue<GateEvent> pendingEvents;
  
  private static final int MOUSE_MOVEMENT_TIMER_DELAY = 500;
  protected AncestorListener textAncestorListener; 
  protected MouseStoppedMovingAction mouseStoppedMovingAction;
  
  protected String lastAnnotationType = "_New_";
  
  protected List actions;
  
  protected ColorGenerator colourGenerator;
  private static final int NAME_COL = 1;
  private static final int SELECTED_COL = 0;
  
  private static final int EVENTS_HANDLE_DELAY = 300;
  
}
