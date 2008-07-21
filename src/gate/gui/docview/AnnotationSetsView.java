/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, Mar 23, 2004
 *
 *  $Id$
 */
package gate.gui.docview;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.text.Highlighter.Highlight;

import gate.*;
import gate.creole.ResourceData;
import gate.creole.ResourceInstantiationException;
import gate.event.*;
import gate.event.DocumentEvent;
import gate.event.DocumentListener;
import gate.gui.*;
import gate.gui.annedit.*;
import gate.gui.annedit.AnnotationEditor;
import gate.gui.annedit.SchemaAnnotationEditor;
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
		                                   AnnotationSetListener, 
		                                   AnnotationEditorOwner{

  
  /* (non-Javadoc)
   * @see gate.gui.annedit.AnnotationEditorOwner#annotationTypeChanged(gate.Annotation, java.lang.String, java.lang.String)
   */
  public void annotationChanged(Annotation ann, AnnotationSet set, 
          String oldType) {
    lastAnnotationType = ann.getType();
    //show new annotation type
    setTypeSelected(set.getName(), ann.getType(), true);
    //select new annotation
//    selectAnnotation(new AnnotationDataImpl(set, ann));
  }
  
  

  /**
   * Queues an an action for selecting the provided annotation
   */
  public void selectAnnotation(final AnnotationData aData) {
    Runnable action = new Runnable(){
      public void run(){
        List<AnnotationData> selAnns = new LinkedList<AnnotationData>();
        selAnns.add(aData);
        owner.setSelectedAnnotations(selAnns);        
      }
    };
    pendingEvents.offer(new PerformActionEvent(action));
    eventMinder.restart();
  }



  /* (non-Javadoc)
   * @see gate.gui.annedit.AnnotationEditorOwner#getNextAnnotation()
   */
  public Annotation getNextAnnotation() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see gate.gui.annedit.AnnotationEditorOwner#getPreviousAnnotation()
   */
  public Annotation getPreviousAnnotation() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see gate.gui.annedit.AnnotationEditorOwner#getTextComponent()
   */
  public JTextComponent getTextComponent() {
    // TODO Auto-generated method stub
    return textPane;
  }

  
  /* (non-Javadoc)
   * @see gate.gui.annedit.AnnotationEditorOwner#getListComponent()
   */
  public AnnotationList getListComponent() {
    return listView;
  }

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
  
  protected void initGUI(){
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
    mainTable.setRowMargin(0);
    mainTable.getColumnModel().setColumnMargin(0);
    SetsTableCellRenderer cellRenderer = new SetsTableCellRenderer();
    mainTable.getColumnModel().getColumn(NAME_COL).setCellRenderer(cellRenderer);
    mainTable.getColumnModel().getColumn(SELECTED_COL).setCellRenderer(cellRenderer);
    SetsTableCellEditor cellEditor = new SetsTableCellEditor();
    mainTable.getColumnModel().getColumn(SELECTED_COL).setCellEditor(cellEditor);
    mainTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mainTable.setColumnSelectionAllowed(false);
    mainTable.setRowSelectionAllowed(true);
    //block autocreation of new columns from now on
    mainTable.setAutoCreateColumnsFromModel(false);
    mainTable.setTableHeader(null);
    mainTable.setShowGrid(false);
    mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    //the background colour seems to change somewhere when using the GTK+ 
    //look and feel on Linux, so we copy the value now and set it 
    Color tableBG = mainTable.getBackground();
    //make a copy of the value (as the reference gets changed somewhere)
    tableBG = new Color(tableBG.getRGB());
    mainTable.setBackground(tableBG);
    
    scroller = new JScrollPane(mainTable);
    scroller.getViewport().setOpaque(true);
    scroller.getViewport().setBackground(tableBG);    
    
    try {
      annotationEditor = createAnnotationEditor(textView, this);
    }
    catch(ResourceInstantiationException e) {
     //this should not really happen
      throw new GateRuntimeException(
              "Could not initialise the annotation editor!", e);
    }
    
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
  
  /**
   * Create the annotation editor (responsible for creating the window
   * used to edit individual annotations).
   * 
   * @param textView
   * @param asView
   * @return
   * @throws ResourceInstantiationException 
   */
  protected gate.gui.annedit.AnnotationEditor createAnnotationEditor(TextualDocumentView textView,
          AnnotationSetsView asView) throws ResourceInstantiationException {
    //find the last VR that implements the AnnotationEditor interface
    List<String> vrTypes = new ArrayList<String>(
            Gate.getCreoleRegister().getPublicVrTypes());
    Collections.reverse(vrTypes);
    for(String aVrType : vrTypes){
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().get(aVrType);
      try{
        Class resClass = rData.getResourceClass();
        if(AnnotationEditor.class.isAssignableFrom(resClass)){
          AnnotationEditor newEditor = (AnnotationEditor) resClass.newInstance();
          newEditor.setOwner(this);
          newEditor.init();
          return newEditor;
        }
      }catch(ClassNotFoundException cnfe){
        //ignore
        Err.prln("Invalid CREOLE data:");
        cnfe.printStackTrace(Err.getPrintWriter());
      }
      catch(InstantiationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch(IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    //if we got this far, we couldn't find an editor
    Err.prln("Could not find any annotation editors. Editing annotations disabled.");
    return null;
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
    textPane.addPropertyChangeListener("highlighter", textChangeListener);
//    textPane.addAncestorListener(textAncestorListener);
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
    textPane.removePropertyChangeListener("highlighter", textChangeListener);
//    textPane.removeAncestorListener(textAncestorListener);
    storeSelectedTypes();
  }
  

  /**
   * Populates the {@link #visibleAnnotationTypes} structure based on the 
   * current selection
   *
   */
  protected void storeSelectedTypes(){
    synchronized(AnnotationSetsView.this) {
//    visibleAnnotationTypes.clear();
      for(SetHandler sHandler:setHandlers){
        for(TypeHandler tHandler: sHandler.typeHandlers){
          if(tHandler.isSelected()){
            visibleAnnotationTypes.add(new TypeSpec(sHandler.set.getName(), 
              tHandler.name));
            tHandler.setSelected(false);
          }
        }
      }
    }
  }
  
  /**
   * Restores the selected types based on the state saved in the 
   * {@link #visibleAnnotationTypes} data structure.
   */
  protected void restoreSelectedTypes(){
    synchronized(AnnotationSetsView.this) {
      for(TypeSpec typeSpec: visibleAnnotationTypes){
        TypeHandler tHandler = getTypeHandler(typeSpec.setName, typeSpec.type);
        tHandler.setSelected(true);
      }
      visibleAnnotationTypes.clear();
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
    textChangeListener = new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getNewValue() != null){
          //we have a new highlighter
          //we need to re-highlight all selected annotations
          for(SetHandler sHandler : setHandlers){
            for(TypeHandler tHandler : sHandler.typeHandlers){
              if(tHandler.isSelected()){
                setTypeSelected(sHandler.set.getName(), tHandler.name, false);
                setTypeSelected(sHandler.set.getName(), tHandler.name, true);
              }
            }
          }
        }
      }
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
  }
  
  public void annotationRemoved(final AnnotationSetEvent e) {
    synchronized(AnnotationSetsView.this) {
      pendingEvents.offer(e);
      eventMinder.restart();
    }
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
  
  public TypeHandler getTypeHandler(String set, String type){
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
        if(tHandler != null){
          tHandler.setSelected(selected);
          int row = tableRows.indexOf(tHandler);
          tableModel.fireTableRowsUpdated(row, row);
          mainTable.getSelectionModel().setSelectionInterval(row, row);
        }else{
          //type handler not created yet
          synchronized(AnnotationSetsView.this) {
            visibleAnnotationTypes.add(new TypeSpec(setName, typeName));  
          }
        }
      }
    });
  }
  
 
  
  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#setSelectedAnnotations(java.util.List)
   */
  @Override
  public void setSelectedAnnotations(List<AnnotationData> selectedAnnots) {
    //for this view, only a single selected annotation makes sense.
    //by convention, we use the first one
    if(selectedAnnots.size() > 0){
      final AnnotationData aData = selectedAnnots.get(0);
      //queue the select action to the events minder
      PerformActionEvent actionEvent = new PerformActionEvent(new Runnable(){
        public void run(){
          //select the annotation for editing, if editing enabled
          if(annotationEditor != null && annotationEditor.isActive()){
            if(annotationEditor.getAnnotationSetCurrentlyEdited() != 
                   aData.getAnnotationSet() ||
               annotationEditor.getAnnotationCurrentlyEdited() != 
                   aData.getAnnotation()){
              annotationEditor.editAnnotation(aData.getAnnotation(),
                      aData.getAnnotationSet());
            }
          }
        }
      });
      synchronized(AnnotationSetsView.this) {
        pendingEvents.offer(actionEvent);
        eventMinder.restart();
      }
    }
  }

  /**
   * Sets a particular annotation as selected. If the list view is visible
   * and active, it makes sure that the same annotation is selected there.
   * If the annotation editor exists and is active, it switches it to this 
   * current annotation.
   * @param ann the annotation
   * @param annSet the parent set
   */
  public void selectAnnotation(final Annotation ann, 
          final AnnotationSet annSet){
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
            setChk.setBackground(isSelected ?
                    		         table.getSelectionBackground() :
                    		         table.getBackground());
            setChk.setEnabled(sHandler.typeHandlers.size() > 0);            
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
  public class SetHandler{
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
      //select the newly created type if previously requested
      TypeSpec typeSpec = new TypeSpec(set.getName(), type);
      synchronized(AnnotationSetsView.this) {
        if(visibleAnnotationTypes.remove(typeSpec)){
          tHandler.setSelected(true);
        }
      }
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
//          if(row >= (setRow + pos + 1)) row--;
        }
        if(typeHandlers.isEmpty()){
          //the set has no more handlers
          setExpanded(false);
          tableModel.fireTableRowsUpdated(setRow, setRow);
        }
        //restore selection if any
        if(row != -1){
          if(mainTable.getRowCount() <= row){
            row = mainTable.getRowCount() -1;
          }
          mainTable.getSelectionModel().setSelectionInterval(row, row);        }
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
  
  public class TypeHandler{
    TypeHandler (SetHandler setHandler, String name){
      this.setHandler = setHandler;
      this.name = name;
      colour = getColor(name);
      hghltTagsForAnnId = new HashMap<Integer, Object>();
      annListTagsForAnn = new HashMap();
      changeColourAction = new ChangeColourAction();
      annotationCount = 0;
    }
    
    /**
     * @return the colour
     */
    public Color getColour() {
      return colour;
    }

    public void setColour(Color colour){
      if(this.colour.equals(colour)) return;
      this.colour = colour;
      saveColor(name, colour);
      if(isSelected()){
        //redraw the highlights
        //hide highlights
        textView.removeHighlights(hghltTagsForAnnId.values());
        hghltTagsForAnnId.clear();
        //show highlights
        List<Annotation> annots = new ArrayList<Annotation>(
                setHandler.set.get(name));
        List<AnnotationData>aDataList = new ArrayList<AnnotationData>();
        for(Annotation ann : annots){
          aDataList.add(new AnnotationDataImpl(setHandler.set, ann));
        }
        List tags = textView.addHighlights(aDataList, TypeHandler.this.colour);
        for(int i = 0; i < annots.size(); i++){
          hghltTagsForAnnId.put(annots.get(i).getId(), tags.get(i));
        }
      }
      //update the table display
      int row = tableRows.indexOf(this);
      if(row >= 0) tableModel.fireTableRowsUpdated(row, row);
    }
    
    public void setSelected(boolean selected){
      if(this.selected == selected) return;
      this.selected = selected;
      final List<Annotation> annots = new ArrayList<Annotation>(setHandler.set.get(name));
      if(selected){
        //make sure set is expanded
        setHandler.setExpanded(true);
        //add to the list view
        annListTagsForAnn.clear();
        List<AnnotationData> listTags = 
            listView.addAnnotations(annots, setHandler.set);
        for(AnnotationData aData: listTags)
          annListTagsForAnn.put(aData.getAnnotation().getId(), aData);
        //show highlights
        hghltTagsForAnnId.clear();
//        List tags = textView.addHighlights(annots, setHandler.set, colour);
        List tags = textView.addHighlights(listTags, colour);
        for(int i = 0; i < annots.size(); i++){
          hghltTagsForAnnId.put(annots.get(i).getId(), tags.get(i));
        }        
      }else{
        //hide highlights
        try{
          listView.removeAnnotations(annListTagsForAnn.values());
          textView.removeHighlights(hghltTagsForAnnId.values());
        }finally{
          hghltTagsForAnnId.clear();
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
        if(!hghltTagsForAnnId.containsKey(ann.getId())) 
            hghltTagsForAnnId.put(ann.getId(), 
                    textView.addHighlight(
                            new AnnotationDataImpl(setHandler.set, ann),
                            colour));
        if(!annListTagsForAnn.containsKey(ann.getId())) 
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
        Object tag = hghltTagsForAnnId.remove(ann.getId());
        if(tag != null) textView.removeHighlight(tag);
        AnnotationData listTag = annListTagsForAnn.remove(ann.getId());
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
      List tags = new ArrayList(hghltTagsForAnnId.size());
      List<Annotation> annots = new ArrayList<Annotation>(hghltTagsForAnnId.size());
      Iterator annIter = hghltTagsForAnnId.keySet().iterator();
      while(annIter.hasNext()){
        Annotation ann = setHandler.set.get((Integer)annIter.next());
        int annStart = ann.getStartNode().getOffset().intValue();
        int annEnd = ann.getEndNode().getOffset().intValue();
        if((annStart <= start && start <= annEnd) ||
           (start <= annStart && annStart <= end)){
          if(!hghltTagsForAnnId.containsKey(ann.getId())){
            System.out.println("Error!!!");
          }
          tags.add(hghltTagsForAnnId.get(ann.getId()));
          annots.add(ann);
        }
      }
      for(int i = 0; i < tags.size(); i++){
        Object tag = tags.get(i);
        Annotation ann = annots.get(i);
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
    Map<Integer, Object> hghltTagsForAnnId;

    /**
     * Map from annotation ID (which is immutable) to AnnotationListView tag
     */
    Map<Integer, AnnotationData> annListTagsForAnn;
    
    String name;
    SetHandler setHandler;
    Color colour;
    int annotationCount;
  }
  
  /**
   * A class storing the identifying information for an annotation type (i.e.
   * the set name and the type).
   * @author Valentin Tablan (valyt)
   *
   */
  private static class TypeSpec{
    private String setName;
    
    private String type;

    public TypeSpec(String setName, String type) {
      super();
      this.setName = setName;
      this.type = type;
    }

    @Override
    public int hashCode() {
      final int PRIME = 31;
      int result = 1;
      result = PRIME * result + ((setName == null) ? 0 : setName.hashCode());
      result = PRIME * result + ((type == null) ? 0 : type.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) return true;
      if(obj == null) return false;
      if(getClass() != obj.getClass()) return false;
      final TypeSpec other = (TypeSpec)obj;
      if(setName == null) {
        if(other.setName != null) return false;
      }
      else if(!setName.equals(other.setName)) return false;
      if(type == null) {
        if(other.type != null) return false;
      }
      else if(!type.equals(other.type)) return false;
      return true;
    }
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
      //check the text location is real
      int textLocation = textPane.viewToModel(e.getPoint());
      try {
        Rectangle viewLocation = textPane.modelToView(textLocation);
        //expand the rectangle a bit
        int error = 10;
        viewLocation = new Rectangle(viewLocation.x - error, 
                                     viewLocation.y - error,
                                     viewLocation.width + 2*error, 
                                     viewLocation.height + 2*error);
        if(viewLocation.contains(e.getPoint())){
          mouseStoppedMovingAction.setTextLocation(textLocation);
        }else{
          mouseStoppedMovingAction.setTextLocation(-1);
        }
      }
      catch(BadLocationException e1) {
        //this should not happen, as the text location comes from the text view 
        throw new LuckyException(e1);
      }finally{
        mouseMovementTimer.restart();
      }
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
      if(annotationEditor == null) return;
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
          //select the annotation set in the tree view and expand it
          //to avoid the next annotation to be always in the default set
          if (tableRows.get(row) instanceof SetHandler) {
            ((SetHandler)tableRows.get(row)).setExpanded(true);
            mainTable.getSelectionModel().setSelectionInterval(row, row);
          }
	        //make sure new annotation is visible
	        setTypeSelected(set.getName(), ann.getType(), true);
	        //edit the new annotation
	        new EditAnnotationAction(new AnnotationDataImpl(set, ann)).
	          actionPerformed(null);
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

          if (document.getSourceUrl() != null) {
            String fileName = document.getSourceUrl().getPath();
            fileName = fileName.replaceAll("%20", " "); // spaces in URL
            fileName = fileName.replaceAll("\\.[^. ]{1,5}$", ".xml");
            if (!fileName.endsWith(".xml")) { fileName += ".xml"; }
            File file = new File(fileName);
            fileChooser.setSelectedFile(file);
            fileChooser.ensureFileIsVisible(file);
          }

          int res = fileChooser.showSaveDialog(owner);
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
  
  /**
   * A fake GATE Event used to wrap a {@link Runnable} value. This is used for
   * queueing actions to the document UI update timer.  
   */
  private class PerformActionEvent extends GateEvent{
    public PerformActionEvent(Runnable runnable){
      super(AnnotationSetsView.this, 0);
      this.runnable = runnable;
    }
    
    private Runnable runnable;

    /**
     * @return the runnable
     */
    public Runnable getRunnable() {
      return runnable;
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
          }else if(event instanceof PerformActionEvent){
            ((PerformActionEvent)event).getRunnable().run();
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
        storeSelectedTypes();
        //release all resources
        for(SetHandler sHandler : setHandlers){
          sHandler.typeHandlers.clear();
          sHandler.typeHandlersByType.clear();
          sHandler.set.removeAnnotationSetListener(AnnotationSetsView.this);
        }
        setHandlers.clear();
        tableRows.clear();
        listView.removeAnnotations(listView.getAllAnnotations());
//        textView.removeAllBlinkingHighlights();
        //rebuild the UI
        populateUI();
        
        //restore the selection
        restoreSelectedTypes();
        tableModel.fireTableDataChanged();
      }catch(Throwable t){
        //something happened, we need to give up
        uiDirty = true;
//        t.printStackTrace();        
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
      if(annotationEditor == null) return;
      //this action either creates a new annotation or starts editing an 
      //existing one. In either case we need first to make sure that the current
      //annotation is finished editing.
      if(!annotationEditor.editingFinished()) return;
      if(textLocation == -1) return;
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
              annotsAtPoint.add(new AnnotationDataImpl(sHandler.set, ann));
            }
          }
        }
        if(annotsAtPoint.size() > 0){
          if(annotsAtPoint.size() > 1){
            JPopupMenu popup = new JPopupMenu();
            Iterator annIter = annotsAtPoint.iterator();
            while(annIter.hasNext()){
              AnnotationData aHandler = (AnnotationData)annIter.next();
              popup.add(new HighlightMenuItem(
                      new EditAnnotationAction(aHandler),
                      aHandler.getAnnotation().getStartNode().getOffset().intValue(),
                      aHandler.getAnnotation().getEndNode().getOffset().intValue(),
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
            new EditAnnotationAction((AnnotationData)annotsAtPoint.get(0)).
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
    public EditAnnotationAction(AnnotationData aData){
      super(aData.getAnnotation().getType() + " [" + 
              (aData.getAnnotationSet().getName() == null ? "  " : 
                aData.getAnnotationSet().getName()) +
              "]");
      putValue(SHORT_DESCRIPTION, aData.getAnnotation().getFeatures().toString());
      this.aData = aData;
    }
    
    public void actionPerformed(ActionEvent evt){
      if(annotationEditor == null) return;
      //if the editor is done with the current annotation, we can move to the 
      //next one
      if(annotationEditor.editingFinished()){
        //set the annotation as selected
        selectAnnotation(aData);
        //show the annotation editor
        annotationEditor.editAnnotation(aData.getAnnotation(), 
                aData.getAnnotationSet());
      }
    }
    
    private AnnotationData aData;
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
        //restore selection
        if(mainTable.getRowCount() <= row){
          row = mainTable.getRowCount() -1;
        }
        mainTable.getSelectionModel().setSelectionInterval(row, row);
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
  gate.gui.annedit.AnnotationEditor annotationEditor;
  NewAnnotationSetAction newSetAction;
  
  /**
   * The listener for mouse and mouse motion events in the text view.
   */
  protected TextMouseListener textMouseListener;
  
  /**
   * Listener for property changes on the text pane.
   */
  protected PropertyChangeListener textChangeListener;
  
  /**
   * Stores the list of visible annotation types when the view is inactivated 
   * so that the selection can be restored when the view is made active again.
   * The values are String[2] pairs of form <set name, type>.
   */
  protected List<TypeSpec> visibleAnnotationTypes;
  
  protected Timer mouseMovementTimer;
  /**
   * Timer used to handle events coming from the document
   */
  protected Timer eventMinder;
  
  protected Queue<GateEvent> pendingEvents;
  
  private static final int MOUSE_MOVEMENT_TIMER_DELAY = 500;
  protected MouseStoppedMovingAction mouseStoppedMovingAction;
  
  protected String lastAnnotationType = "_New_";
  
  protected List actions;
  
  protected ColorGenerator colourGenerator;
  private static final int NAME_COL = 1;
  private static final int SELECTED_COL = 0;
  
  private static final int EVENTS_HANDLE_DELAY = 300;
  
}
