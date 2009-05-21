/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
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
import gate.gui.annedit.*;
import gate.swing.XJTable;
import gate.util.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;

/**
 * A tabular view for a list of annotations.
  * Used as part of the document viewer to display all the annotation currently
  * highlighted.
  */
 public class AnnotationListView extends AbstractDocumentView
     implements AnnotationListener, AnnotationList, AnnotationEditorOwner{

   public AnnotationListView(){
     annDataList = new ArrayList<AnnotationData>();
   }


   /**
    *  (non-Javadoc)
    * @see gate.gui.docview.AnnotationList#getAnnotationAtRow(int)
    */
   public AnnotationData getAnnotationAtRow(int row) {
     return annDataList == null ? null : annDataList.get(
             table.rowViewToModel(row));
   }


   /* (non-Javadoc)
    * @see gate.gui.docview.AnnotationList#getSelectionModel()
    */
   public ListSelectionModel getSelectionModel() {
     return table == null ? null : table.getSelectionModel();
   }


   @Override
   public void cleanup() {
     super.cleanup();
     for(AnnotationData aData : annDataList){
       aData.getAnnotation().removeAnnotationListener(this);
     }
     annDataList.clear();
     textView = null;
   }

   /* (non-Javadoc)
    * @see gate.gui.docview.AbstractDocumentView#initGUI()
    */
   protected void initGUI() {
     editorsCache = new HashMap<String, AnnotationVisualResource>();
     tableModel = new AnnotationTableModel();
     table = new XJTable(tableModel);
     table.setAutoResizeMode(XJTable.AUTO_RESIZE_OFF);
     table.setSortable(true);
     table.setSortedColumn(START_COL);
     table.setIntercellSpacing(new Dimension(2, 0));
     table.setEnableHidingColumns(true);
     //the background colour seems to change somewhere when using the GTK+
     //look and feel on Linux, so we copy the value now and set it
     Color tableBG = table.getBackground();
     //make a copy of the value (as the reference gets changed somewhere)
     tableBG = new Color(tableBG.getRGB());
     table.setBackground(tableBG);

     scroller = new JScrollPane(table);
     scroller.getViewport().setOpaque(true);
     scroller.getViewport().setBackground(tableBG);

     mainPanel = new JPanel();
     mainPanel.setLayout(new GridBagLayout());
     GridBagConstraints constraints = new GridBagConstraints();

     constraints.gridx = 0;
     constraints.gridwidth = 4;
     constraints.gridy = 0;
     constraints.weightx = 1;
     constraints.weighty = 1;
     constraints.fill= GridBagConstraints.BOTH;
     mainPanel.add(scroller, constraints);

     constraints.gridx = GridBagConstraints.RELATIVE;
     constraints.gridwidth = 1;
     constraints.gridy = 1;
     constraints.weightx = 0;
     constraints.weighty = 0;
     constraints.fill= GridBagConstraints.NONE;
     constraints.anchor = GridBagConstraints.WEST;
     statusLabel = new JLabel();
     mainPanel.add(statusLabel, constraints);
     constraints.fill= GridBagConstraints.HORIZONTAL;
     constraints.anchor = GridBagConstraints.EAST;
     mainPanel.add(Box.createHorizontalStrut(10), constraints);
     mainPanel.add(new JLabel("Select: "), constraints);
     filterTextField = new JTextField(20);
     filterTextField.setToolTipText("Select the rows containing this text.");
     mainPanel.add(filterTextField, constraints);

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
                      " selected)");
              //update the list of selected annotations globally
              synchronized(this) {
                if(localSelectionUpdating) return;
              }
              int[] viewRows = table.getSelectedRows();
              List<AnnotationData> selAnns = new ArrayList<AnnotationData>();
              for(int i = 0; i < viewRows.length; i++){
                int modelRow = table.rowViewToModel(viewRows[i]);
                if(modelRow >= 0){
                  selAnns.add(annDataList.get(modelRow));
                }
              }
              owner.setSelectedAnnotations(selAnns);
              //blink the selected annotations
    //          textView.removeAllBlinkingHighlights();
    //          showHighlights();

              if(table.getSelectedRowCount() >= 1){
                int modelRow = table.rowViewToModel(
                  table.getSelectionModel().getLeadSelectionIndex());
                AnnotationData aHandler = annDataList.get(modelRow);
                //scroll to show the last highlight
                if(aHandler != null && aHandler.getAnnotation() != null)
                  textView.scrollAnnotationToVisible(aHandler.getAnnotation());
              }
            }
        });

        table.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent me) {
            processMouseEvent(me);
          }
          public void mouseReleased(MouseEvent me) {
            processMouseEvent(me);
          }
          public void mousePressed(MouseEvent me) {
            int row = table.rowAtPoint(me.getPoint());
            if(me.isPopupTrigger()
            && !table.isRowSelected(row)) {
              // if right click outside the selection then reset selection
              table.getSelectionModel().setSelectionInterval(row, row);
            }
            processMouseEvent(me);
          }
          protected void processMouseEvent(MouseEvent me){
            int viewRow = table.rowAtPoint(me.getPoint());
            final int modelRow = viewRow == -1 ?
                                 viewRow :
                                 table.rowViewToModel(viewRow);

            // popup menu
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
                    aData.getAnnotationSet().remove(aData.getAnnotation());
                  }
                }
              };
              popup.add(deleteAction);

              //add the custom edit actions
              if(modelRow != -1){
                AnnotationData aHandler = annDataList.get(modelRow);
                //add the specific editors
                List<String> specificEditorClasses = Gate.getCreoleRegister().
                  getAnnotationVRs(aHandler.getAnnotation().getType());
                if(specificEditorClasses != null &&
                   specificEditorClasses.size() > 0){
                  popup.addSeparator();
                  Iterator<String> editorIter = specificEditorClasses.iterator();
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
                    popup.add(new EditAnnotationAction(aHandler.getAnnotationSet(),
                            aHandler.getAnnotation(), editor));
                  }
                }
                //add generic editors
                List<String> genericEditorClasses = Gate.getCreoleRegister().
                  getAnnotationVRs();
                if(genericEditorClasses != null &&
                   genericEditorClasses.size() > 0){
                  popup.addSeparator();
                  Iterator<String> genEditorIter = genericEditorClasses.iterator();
                  while(genEditorIter.hasNext()){
                    String editorClass = (String) genEditorIter.next();
                    if(specificEditorClasses.contains(editorClass)) continue;
                    AnnotationVisualResource editor = (AnnotationVisualResource)
                      editorsCache.get(editorClass);
                    if(editor == null){
                      //create the new type of editor
                      try{
                        ResourceData resData = Gate.getCreoleRegister().get(editorClass);
                        Class<?> resClass = resData.getResourceClass();
                        if(OwnedAnnotationEditor.class.isAssignableFrom(resClass)) {
                          OwnedAnnotationEditor newEditor = (OwnedAnnotationEditor)resClass
                                  .newInstance();
                          newEditor.setOwner(AnnotationListView.this);
                          newEditor.init();
                          editor = newEditor;
                        }else{
                          editor = (AnnotationVisualResource)
                                   Factory.createResource(editorClass);
                        }
                        editorsCache.put(editorClass, editor);
                      }catch(Exception rie){
                        rie.printStackTrace(Err.getPrintWriter());
                      }
                    }
                    popup.add(new EditAnnotationAction(aHandler.getAnnotationSet(),
                            aHandler.getAnnotation(), editor));
                  }
                }
              }
              popup.show(table, me.getX(), me.getY());
            }
          }
        });

        table.addAncestorListener(new AncestorListener() {
          public void ancestorAdded(AncestorEvent event) {
            // force the table to be sorted when the view is shown
            tableModel.fireTableDataChanged();
          }
          public void ancestorMoved(AncestorEvent event) {
          }
          public void ancestorRemoved(AncestorEvent event) {
          }
        });

        // select all the rows containing the text from filterTextField
        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
          private Timer timer = new Timer();
          public void changedUpdate(DocumentEvent e) {
          }
          public void insertUpdate(DocumentEvent e) {
            timer.cancel();
            // one second delay
            Date timeToRun = new Date(System.currentTimeMillis() + 1000);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                  selectRows();
                }
              }, timeToRun);
          }
          public void removeUpdate(DocumentEvent e) {
            timer.cancel();
            // one second delay
            Date timeToRun = new Date(System.currentTimeMillis() + 1000);
            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                  selectRows();
                }
              }, timeToRun);
          }
          private void selectRows() {
            table.clearSelection();
            if (filterTextField.getText().trim().length() < 2
             || table.getRowCount() == 0) {
              return;
            }
            // block upward events
            synchronized(this) { localSelectionUpdating = true; }
            for (int row = 0; row < table.getRowCount(); row++) {
              for (int col = 0; col < table.getColumnCount(); col++) {
                if (table.getValueAt(row, col) != null
                 && table.getValueAt(row, col).toString()
                    .contains(filterTextField.getText().trim())) {
                  table.addRowSelectionInterval(row, row);
                  break;
                }
              }
            }
            synchronized(this) { localSelectionUpdating = false; }
            // update the highlights in the document
            if (table.isCellSelected(0,0)) {
              table.addRowSelectionInterval(0, 0);
            } else {
              table.removeRowSelectionInterval(0, 0);
            }
          }
        });
      }
   /* (non-Javadoc)
       * @see gate.gui.docview.AbstractDocumentView#registerHooks()
       */
      protected void registerHooks() {
        //this is called on activation
    //    showHighlights();
      }

   /* (non-Javadoc)
    * @see gate.gui.docview.AbstractDocumentView#unregisterHooks()
    */
   protected void unregisterHooks() {
     //this is called on de-activation
     //remove highlights
 //    textView.removeAllBlinkingHighlights();
   }

   /* (non-Javadoc)
    * @see gate.gui.docview.DocumentView#getType()
    */
   public int getType() {
     return HORIZONTAL;
   }

 //  protected void showHighlights(){
 //    int[] viewRows = table.getSelectedRows();
 //    AnnotationData aHandler = null;
 //    for(int i = 0; i < viewRows.length; i++){
 //      int modelRow = table.rowViewToModel(viewRows[i]);
 //      if(modelRow >= 0){
 //        aHandler = annDataList.get(modelRow);
 //        textView.addBlinkingHighlight(aHandler.getAnnotation());
 //      }
 //    }
 //  }


   /**
    * Adds an annotation to be displayed in the list.
    * @param ann the annotation
    * @param set the set containing the annotation
    * @return a tag that can be used to refer to this annotation for future
    * operations, e.g. when removing the annotation from display.
    */
   public AnnotationDataImpl addAnnotation(Annotation ann, AnnotationSet set){
     AnnotationDataImpl aData = new AnnotationDataImpl(set, ann);
     annDataList.add(aData);
     int row = annDataList.size() -1;
     if(tableModel != null) tableModel.fireTableRowsInserted(row, row);
     //listen for the new annotation's events
     aData.getAnnotation().addAnnotationListener(AnnotationListView.this);
     return aData;
   }

   public void removeAnnotation(AnnotationData tag){
     int row = annDataList.indexOf(tag);
     if(row >= 0){
       AnnotationData aHandler = annDataList.get(row);
       //remove from selection, if the table is built
       List<AnnotationData> selAnns = owner.getSelectedAnnotations();
       if(selAnns.remove(tag)){
         owner.setSelectedAnnotations(selAnns);
       }
 //      if(table != null){
 //        int viewRow = table.rowModelToView(row);
 //        if(table.isRowSelected(viewRow)){
 //          table.getSelectionModel().removeIndexInterval(viewRow, viewRow);
 //          //remove the blinking highlight
 //          textView.removeBlinkingHighlight(aHandler.getAnnotation());
 //        }
 //      }
       aHandler.getAnnotation().removeAnnotationListener(AnnotationListView.this);
       annDataList.remove(row);
       if(tableModel != null) tableModel.fireTableRowsDeleted(row, row);
     }
   }

   public void removeAnnotations(Collection<AnnotationData> tags){
     //cache the selected annotations
     final List<AnnotationData> selectedAnns = new ArrayList<AnnotationData>();
     if(table != null){
       int[] selRows = table.getSelectedRows();
       if(selRows != null && selRows.length > 0){
         for(int viewRow : selRows){
           int modelRow = table.rowViewToModel(viewRow);
           AnnotationData aData = annDataList.get(modelRow);
           //only save it if it's not to be removed
           if(!tags.contains(aData)) selectedAnns.add(aData);
         }
       }
       //to speed-up things, first remove all blinking highlights
       table.getSelectionModel().clearSelection();
     }
     //now do the actual removal
     for(AnnotationData aData : tags) removeAnnotation(aData);
     //restore the selection, if necessary
     if(selectedAnns.size() > 0){
       //this needs to happen after the table has caught up with all the changes
       //hence we need to queue it to the GUI thread
       SwingUtilities.invokeLater(new Runnable(){
         public void run(){
           for(AnnotationData aData : selectedAnns){
             int modelRow = annDataList.indexOf(aData);
             if(modelRow != -1){
               int viewRow = table.rowModelToView(modelRow);
               table.getSelectionModel().addSelectionInterval(viewRow, viewRow);
             }
           }
         }
       });
     }
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
     for(Annotation ann : annotations) tags.add(new AnnotationDataImpl(set, ann));
     annDataList.addAll(tags);
     for(AnnotationData aData : tags) aData.getAnnotation().addAnnotationListener(
             AnnotationListView.this);
     if(tableModel != null) tableModel.fireTableDataChanged();
     return tags;
   }


   /**
    * Returns the tags for all the annotations currently displayed
    * @return a list of {@link AnnotationDataImpl}.
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
         if(aHandler.getAnnotation() == ann)tableModel.fireTableRowsUpdated(i, i);
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


   /* (non-Javadoc)
    * @see gate.gui.docview.AbstractDocumentView#setSelectedAnnotations(java.util.List)
    */
   @Override
   public void setSelectedAnnotations(List<AnnotationData> selectedAnnots) {
     //if the list of selected annotations differs from the current selection,
     //update the selection.
     //otherwise do nothing (to break infinite looping)

     //first get the local list of selected annotations
     int[] viewRows = table.getSelectedRows();
     AnnotationData aHandler = null;
     List<AnnotationData> localSelAnns = new ArrayList<AnnotationData>();
     for(int i = 0; i < viewRows.length; i++){
       int modelRow = table.rowViewToModel(viewRows[i]);
       if(modelRow >= 0){
         localSelAnns.add(annDataList.get(modelRow));
       }
     }
     //now compare with the new value
     if(localSelAnns.size() == selectedAnnots.size()){
       //same size, we need to actually compare contents
       localSelAnns.removeAll(selectedAnnots);
       if(localSelAnns.isEmpty()){
         //lists are the same -> exit!
         return;
       }
     }
     //if we got this far, the selection lists were different
     try{
       //block upward events
       synchronized(this) {
         localSelectionUpdating = true;
       }
       //update the local selection
       table.getSelectionModel().clearSelection();
       for(AnnotationData aData : selectedAnnots){
         int modelRow = annDataList.indexOf(aData);
         if(modelRow != -1){
           int viewRow = table.rowModelToView(modelRow);
           table.getSelectionModel().addSelectionInterval(viewRow, viewRow);
           table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
         }
       }
     }finally{
       //re-enable upward events
       synchronized(this) {
         localSelectionUpdating = false;
       }
     }
   }


   /**
    * Selects the annotation for the given tag.
    * @param tag the tag of the annotation to be selected.
    */
   public void selectAnnotationForTag(Object tag){
     int modelPosition = annDataList.indexOf(tag);
     table.getSelectionModel().clearSelection();
     if(modelPosition != -1){
       int tablePosition = table.rowModelToView(modelPosition);
       table.getSelectionModel().setSelectionInterval(tablePosition,
               tablePosition);
       table.scrollRectToVisible(table.getCellRect(tablePosition, 0, false));
     }
   }



   /* (non-Javadoc)
    * @see gate.gui.annedit.AnnotationEditorOwner#annotationChanged(gate.Annotation, gate.AnnotationSet, java.lang.String)
    */
   public void annotationChanged(Annotation ann, AnnotationSet set,
           String oldType) {
     //do nothing
   }

   /* (non-Javadoc)
    * @see gate.gui.annedit.AnnotationEditorOwner#getNextAnnotation()
    */
   public Annotation getNextAnnotation() {
     return null;
   }


   /* (non-Javadoc)
    * @see gate.gui.annedit.AnnotationEditorOwner#getPreviousAnnotation()
    */
   public Annotation getPreviousAnnotation() {
     return null;
   }


   /* (non-Javadoc)
    * @see gate.gui.annedit.AnnotationEditorOwner#getTextComponent()
    */
   public JTextComponent getTextComponent() {
     return (JTextArea)((JScrollPane)textView.getGUI()).getViewport().getView();
   }

   /* (non-Javadoc)
    * @see gate.gui.annedit.AnnotationEditorOwner#selectAnnotation(gate.gui.annedit.AnnotationData)
    */
   public void selectAnnotation(AnnotationData data) {
   }

   /* (non-Javadoc)
    * @see gate.gui.docview.AnnotationList#getRowForAnnotation(gate.gui.annedit.AnnotationData)
    */
   public int getRowForAnnotation(AnnotationData data) {
     return annDataList.indexOf(data);
   }

  class AnnotationTableModel extends AbstractTableModel{
       public int getRowCount(){
         return annDataList.size();
       }

       public int getColumnCount(){
         return 6;
       }

       public String getColumnName(int column){
         switch(column){
           case TYPE_COL: return "Type";
           case SET_COL: return "Set";
           case START_COL: return "Start";
           case END_COL: return "End";
           case ID_COL: return "Id";
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
           case ID_COL: return Integer.class;
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
           case TYPE_COL: return aData.getAnnotation().getType();
           case SET_COL: return aData.getAnnotationSet().getName();
           case START_COL: return aData.getAnnotation().getStartNode().getOffset();
           case END_COL: return aData.getAnnotation().getEndNode().getOffset();
           case ID_COL: return aData.getAnnotation().getId();
           case FEATURES_COL:
             //sort the features by name
             FeatureMap features = aData.getAnnotation().getFeatures();
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
   //      editor.setTarget(set);
   //      editor.setAnnotation(ann);
         if(editor instanceof OwnedAnnotationEditor){
           //we need to unpin the editor so that it actually calculates the
           //position
           ((OwnedAnnotationEditor)editor).setPinnedMode(false);
           ((OwnedAnnotationEditor)editor).placeDialog(
                   ann.getStartNode().getOffset().intValue(),
                   ann.getEndNode().getOffset().intValue());
           //now we need to [pin it so that it does not disappear automatically
           ((OwnedAnnotationEditor)editor).setPinnedMode(true);
           editor.editAnnotation(ann, set);
         }else{
           editor.editAnnotation(ann, set);
           JScrollPane scroller = new JScrollPane((Component)editor);
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

     /**
      * Flag used to mark the fact that the table selection is currently being
      * updated, to synchronise it with the global selection.
      * This is used to block update events being sent to the owner, while the
      * current selection is adjusted.
      */
     protected boolean localSelectionUpdating = false;

     protected JPanel mainPanel;
     protected JLabel statusLabel;
     protected JTextField filterTextField;
     protected TextualDocumentView textView;
     /**
      * A map that stores instantiated annotations editors in order to avoid the
      * delay of building them at each request;
      */
     protected Map<String, AnnotationVisualResource> editorsCache;

     private static final int TYPE_COL = 0;
     private static final int SET_COL = 1;
     private static final int START_COL = 2;
     private static final int END_COL = 3;
     private static final int ID_COL = 4;
     private static final int FEATURES_COL = 5;

   }
