/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  AnnotatioListView.java
 *
 *  Valentin Tablan, May 25, 2004
 *
 *  $Id$
 */

package gate.gui.docview;

import gate.*;
import gate.creole.*;
import gate.event.AnnotationEvent;
import gate.event.AnnotationListener;
import gate.gui.ResizableVisualResource;
import gate.swing.XJTable;
import gate.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import org.omg.PortableServer.AdapterActivator;

/**
 * A tabular view for a list of annotations.
 * Used as part of the document viewer to display all the annotation currently
 * highlighted.
 */
public class AnnotationListView extends AbstractDocumentView
		implements AnnotationListener{
  public AnnotationListView(){
    annDataList = new ArrayList<AnnotationData>();
  }

  @Override
  public void cleanup() {
    super.cleanup();
    for(AnnotationData aData : annDataList){
      aData.ann.removeAnnotationListener(this);
    }
    annDataList.clear();
    textView = null;
  }

  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#initGUI()
   */
  protected void initGUI() {
    editorsCache = new HashMap();
    tableModel = new AnnotationTableModel();
    table = new XJTable(tableModel);
    table.setAutoResizeMode(XJTable.AUTO_RESIZE_OFF);
    table.setSortable(true);
    table.setSortedColumn(START_COL);
    table.setIntercellSpacing(new Dimension(2, 0));
    scroller = new JScrollPane(table);

    mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.fill= GridBagConstraints.BOTH;
    mainPanel.add(scroller, constraints);

    constraints.gridy = 1;
    constraints.weightx = 0;
    constraints.weighty = 0;
    constraints.fill= GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.WEST;
    statusLabel = new JLabel();
    mainPanel.add(statusLabel, constraints);

    //get a pointer to the text view used to display
    //the selected annotations
    Iterator centralViewsIter = owner.getCentralViews().iterator();
    while(textView == null && centralViewsIter.hasNext()){
      DocumentView aView = (DocumentView)centralViewsIter.next();
      if(aView instanceof TextualDocumentView)
        textView = (TextualDocumentView)aView;
    }

    initListeners();
  }

  public Component getGUI(){
    return mainPanel;
  }
  protected void initListeners(){
    tableModel.addTableModelListener(new TableModelListener(){
      public void tableChanged(TableModelEvent e){
        statusLabel.setText(
                Integer.toString(tableModel.getRowCount()) +
                " Annotations (" +
                Integer.toString(table.getSelectedRowCount()) +
                " selected)");
      }
    });


    table.getSelectionModel().
      addListSelectionListener(new ListSelectionListener(){
        public void valueChanged(ListSelectionEvent e){
          if(!isActive())return;
          statusLabel.setText(
                  Integer.toString(tableModel.getRowCount()) +
                  " Annotations (" +
                  Integer.toString(table.getSelectedRowCount()) +
                  "selected)");
          //blink the selected annotations
          textView.removeAllBlinkingHighlights();
          showHighlights();
        }
    });

    table.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent me) {
        processMouseEvent(me);
      }
      public void mouseReleased(MouseEvent me) {
        processMouseEvent(me);
      }
      public void mouseEntered(MouseEvent me) {
        processMouseEvent(me);
      }
      public void mouseExited(MouseEvent me) { 
        processMouseEvent(me);
      }
      public void mousePressed(MouseEvent me) {
        processMouseEvent(me);
      }
      protected void processMouseEvent(MouseEvent me){
        int viewRow = table.rowAtPoint(me.getPoint());
        final int modelRow = viewRow == -1 ?
                             viewRow : 
                             table.rowViewToModel(viewRow);
        
        // right click
        if(me.isPopupTrigger()) {
          JPopupMenu popup = new JPopupMenu();
          Action deleteAction = new AbstractAction("Delete"){
            public void actionPerformed(ActionEvent evt){
              int[] rows = table.getSelectedRows();
              table.clearSelection();
              if(rows == null || rows.length == 0){
                //no selection -> use row under cursor
                if(modelRow == -1) return;
                rows = new int[]{modelRow};
              }
              for(int i = 0; i < rows.length; i++){
                AnnotationData aData = annDataList.
                    get(table.rowViewToModel(rows[i]));
                aData.set.remove(aData.ann);
              }
            }
          };
          popup.add(deleteAction);
          
          //add the custom edit actions
          if(modelRow != -1){
            AnnotationData aHandler = annDataList.get(modelRow);
            List editorClasses = Gate.getCreoleRegister().
              getAnnotationVRs(aHandler.ann.getType());
            if(editorClasses != null && editorClasses.size() > 0){
              popup.addSeparator();
              Iterator editorIter = editorClasses.iterator();
              while(editorIter.hasNext()){
                String editorClass = (String) editorIter.next();
                AnnotationVisualResource editor = (AnnotationVisualResource)
                  editorsCache.get(editorClass);
                if(editor == null){
                  //create the new type of editor
                  try{
                    editor = (AnnotationVisualResource)
                             Factory.createResource(editorClass);
                    editorsCache.put(editorClass, editor);
                  }catch(ResourceInstantiationException rie){
                    rie.printStackTrace(Err.getPrintWriter());
                  }
                }
                popup.add(new EditAnnotationAction(aHandler.set, 
                        aHandler.ann, editor));
              }
            }
          }
          popup.show(table, me.getX(), me.getY());
        } 
      }
    });
    /* End */

  }
  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#registerHooks()
   */
  protected void registerHooks() {
    //this is called on activation
    showHighlights();
  }

  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#unregisterHooks()
   */
  protected void unregisterHooks() {
    //this is called on de-activation
    //remove highlights
    textView.removeAllBlinkingHighlights();
  }

  /* (non-Javadoc)
   * @see gate.gui.docview.DocumentView#getType()
   */
  public int getType() {
    return HORIZONTAL;
  }
  protected void guiShown(){
    tableModel.fireTableDataChanged();
  }

  protected void showHighlights(){
    int[] rows = table.getSelectedRows();
    AnnotationData aHandler = null;
    for(int i = 0; i < rows.length; i++){
      aHandler = annDataList.get(table.rowViewToModel(rows[i]));
      textView.addBlinkingHighlight(aHandler.ann);
    }    
    //scroll to show the last highlight
    if(aHandler != null && aHandler.ann != null)
        textView.scrollAnnotationToVisible(aHandler.ann);
  }

  /**
   * Adds an annotation to be displayed in the list.
   * @param ann the annotation
   * @param set the set containing the annotation
   * @return a tag that can be used to refer to this annotation for future 
   * operations, e.g. when removing the annotation from display.
   */
  public AnnotationData addAnnotation(Annotation ann, AnnotationSet set){
    AnnotationData aData = new AnnotationData(set, ann);
    annDataList.add(aData);
    int row = annDataList.size() -1;
    if(tableModel != null) tableModel.fireTableRowsInserted(row, row);
    //listen for the new annotation's events
    aData.ann.addAnnotationListener(AnnotationListView.this);
    return aData;
  }

  public void removeAnnotation(AnnotationData tag){
    int row = annDataList.indexOf(tag);
    if(row >= 0){
      //remove from selection, if the table is built
      if(table != null){
        int viewRow = table.rowModelToView(row);
        if(table.isRowSelected(viewRow)){
          table.getSelectionModel().removeIndexInterval(viewRow, viewRow);
        }
      }
      AnnotationData aHandler = annDataList.get(row);
      aHandler.ann.removeAnnotationListener(AnnotationListView.this);
      annDataList.remove(row);
      if(tableModel != null) tableModel.fireTableRowsDeleted(row, row);
    }
  }
  
  public void removeAnnotations(Collection<AnnotationData> tags){
    for(AnnotationData aData : (Collection<AnnotationData>)tags)
      removeAnnotation(aData);
  }

  /**
   * Adds a batch of annotations in one go. 
   * For each annotation, a tag object will be created. The return value is 
   * list that returns the tags in the same order as the collection used
   * for the input annotations.
   * This method does not assume it was called from the UI Thread.
   * @return a collection of tags corresponding to the annotations.
   * @param annotations a collection of annotations
   * @param set the annotation set to which all the annotations belong.
   */
  public List<AnnotationData> addAnnotations(List<Annotation> annotations,
          AnnotationSet set){
    List<AnnotationData> tags = new ArrayList<AnnotationData>();
    for(Annotation ann : annotations) tags.add(new AnnotationData(set, ann));
    annDataList.addAll(tags);
    for(AnnotationData aData : tags) aData.ann.addAnnotationListener(
            AnnotationListView.this);
    if(tableModel != null) tableModel.fireTableDataChanged();
    return tags;
  }


  /**
   * Returns the tags for all the annotations currently displayed
   * @return a list of {@link AnnotationData}.
   */
  public List<AnnotationData> getAllAnnotations(){
    return annDataList;
  }

  public void annotationUpdated(AnnotationEvent e){
    //update all occurrences of this annotation
   // if annotations tab has not been set to visible state
  	// table will be null.
  	if(table == null)	return;
    //save selection
  	int[] selection = table.getSelectedRows();
    Annotation ann = (Annotation)e.getSource();
    if(tableModel != null){
      for(int i = 0; i < annDataList.size(); i++){
        AnnotationData aHandler = annDataList.get(i);
        if(aHandler.ann == ann)tableModel.fireTableRowsUpdated(i, i);
      }
    }
    //restore selection
    table.clearSelection();
    if(selection != null){
      for(int i = 0; i < selection.length; i++){
        table.getSelectionModel().addSelectionInterval(selection[i], 
                selection[i]);
      }
    }
  }

  /**
   * Selects the annotation for the given tag.
   * @param tag the tag of the annotation to be selected.
   */
  public void selectAnnotationForTag(Object tag){
    int modelPosition = annDataList.indexOf(tag);
    
    if(modelPosition != -1){
      int tablePosition = table.rowModelToView(modelPosition);
      table.getSelectionModel().setSelectionInterval(tablePosition, 
              tablePosition);
      table.scrollRectToVisible(table.getCellRect(tablePosition, 0, false));
    }
  }
  
  class AnnotationTableModel extends AbstractTableModel{
    public int getRowCount(){
      return annDataList.size();
    }

    public int getColumnCount(){
      return 5;
    }

    public String getColumnName(int column){
      switch(column){
        case TYPE_COL: return "Type";
        case SET_COL: return "Set";
        case START_COL: return "Start";
        case END_COL: return "End";
        case FEATURES_COL: return "Features";
        default: return "?";
      }
    }

    public Class getColumnClass(int column){
      switch(column){
        case TYPE_COL: return String.class;
        case SET_COL: return String.class;
        case START_COL: return Long.class;
        case END_COL: return Long.class;
        case FEATURES_COL: return String.class;
        default: return String.class;
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return false;
    }

    public Object getValueAt(int row, int column){
      if(row >= annDataList.size()) return null;
      AnnotationData aData = annDataList.get(row);
      switch(column){
        case TYPE_COL: return aData.ann.getType();
        case SET_COL: return aData.set.getName();
        case START_COL: return aData.ann.getStartNode().getOffset();
        case END_COL: return aData.ann.getEndNode().getOffset();
        case FEATURES_COL:
          //sort the features by name
          FeatureMap features = aData.ann.getFeatures();
          List keyList = new ArrayList(features.keySet());
          Collections.sort(keyList);
          StringBuffer strBuf = new StringBuffer("{");
          Iterator keyIter = keyList.iterator();
          boolean first = true;
          while(keyIter.hasNext()){
            Object key = keyIter.next();
            Object value = features.get(key);
            if(first){
              first = false;
            }else{
              strBuf.append(", ");
            }
            strBuf.append(key.toString());
            strBuf.append("=");
            strBuf.append(value == null ? "[null]" : value.toString());
          }
          strBuf.append("}");
          return strBuf.toString();
        default: return "?";
      }
    }

  }

  public static class AnnotationData{
    public AnnotationData(AnnotationSet set, Annotation ann){
      this.ann = ann;
      this.set = set;
    }
    Annotation ann;
    AnnotationSet set;
  }

  protected class EditAnnotationAction extends AbstractAction{
    public EditAnnotationAction(AnnotationSet set, Annotation ann, 
            AnnotationVisualResource editor){
      this.set = set;
      this.ann = ann;
      this.editor = editor;
      ResourceData rData =(ResourceData)Gate.getCreoleRegister().
          get(editor.getClass().getName()); 
      if(rData != null){
        title = rData.getName();
        putValue(NAME, "Edit with " + title);
        putValue(SHORT_DESCRIPTION, rData.getComment());
      }
    }
    
    public void actionPerformed(ActionEvent evt){
      JScrollPane scroller = new JScrollPane((Component)editor); 
      editor.setTarget(set);
      editor.setAnnotation(ann);
      JOptionPane optionPane = new JOptionPane(scroller,
              JOptionPane.QUESTION_MESSAGE, 
              JOptionPane.OK_CANCEL_OPTION, 
              null, new String[]{"OK", "Cancel"});
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      scroller.setMaximumSize(new Dimension((int)(screenSize.width * .75), 
              (int)(screenSize.height * .75)));
      JDialog dialog = optionPane.createDialog(AnnotationListView.this.getGUI(),
              title);
      dialog.setModal(true);
      dialog.setResizable(true);
      dialog.setVisible(true);
      try{
        if(optionPane.getValue().equals("OK")) editor.okAction();
        else editor.cancelAction();
      }catch(GateException ge){
        throw new GateRuntimeException(ge);
      }
    }
    
    String title;
    Annotation ann;
    AnnotationSet set;
    AnnotationVisualResource editor;
  }
  
  protected XJTable table;
  protected AnnotationTableModel tableModel;
  protected JScrollPane scroller;

  /**
   * Stores the {@link AnnotationData} objects representing the annotations
   * displayed by this view.
   */
  protected List<AnnotationData> annDataList;
  
  protected JPanel mainPanel;
  protected JLabel statusLabel;
  protected TextualDocumentView textView;
  /**
   * A map that stores instantiated annotations editors in order to avoid the 
   * delay of building them at each request;
   */
  protected Map editorsCache;

  private static final int TYPE_COL = 0;
  private static final int SET_COL = 1;
  private static final int START_COL = 2;
  private static final int END_COL = 3;
  private static final int FEATURES_COL = 4;

}
