/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
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
import gate.Annotation;
import gate.AnnotationSet;
import gate.event.AnnotationEvent;
import gate.event.AnnotationListener;
import gate.swing.XJTable;
import gate.util.GateRuntimeException;
import java.awt.*;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 * A tabular view for a list of annotations.
 * Used as part of the document viewer to display all the annotation currently
 * highlighted.
 */
public class AnnotationListView extends AbstractDocumentView
		implements AnnotationListener{
  public AnnotationListView(){
    tagList = new ArrayList();
    annotationHandlerByTag = new HashMap();
  }

  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#initGUI()
   */
  protected void initGUI() {
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
//    table.addComponentListener(new ComponentAdapter(){
//      public void componentShown(ComponentEvent e){
//        //trigger a resize for the columns
//        table.adjustSizes();
//      }
//    });

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
          statusLabel.setText(
                  Integer.toString(tableModel.getRowCount()) +
                  " Annotations (" +
                  Integer.toString(table.getSelectedRowCount()) +
                  "selected)");
          //blink the selected annotations
          textView.removeAllBlinkingHighlights();
          int[] rows = table.getSelectedRows();
          for(int i = 0; i < rows.length; i++){
            Object tag = tagList.get(table.rowViewToModel(rows[i]));
            AnnotationHandler aHandler = (AnnotationHandler)
              annotationHandlerByTag.get(tag);
            textView.addBlinkingHighlight(aHandler.ann);
          }
        }
    });

    /* Niraj */
    table.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent me) {
        // right click
        if(table.getSelectedRows().length == 0) {
          return;
        }

        if(!table.isRowSelected(table.rowAtPoint(me.getPoint()))) {
          return;
        }

        if(me.getModifiers() == 4) {
          final JPopupMenu popup = new JPopupMenu();
          JButton delete = new JButton("Delete");
          delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              int [] rows = table.getSelectedRows();
              ArrayList handlers = new ArrayList();

              for(int i = 0; i < rows.length; i++){
                Object tag = tagList.get(table.rowViewToModel(rows[i]));
                handlers.add(tag);
              }

              for(int i=0;i<handlers.size();i++) {
                Object tag = handlers.get(i);
                AnnotationHandler aHandler = (AnnotationHandler)
                                             annotationHandlerByTag.get(tag);
                aHandler.set.remove(aHandler.ann);
                removeAnnotation(tag);
              }
              popup.hide();
              popup.setVisible(false);
            }
          });

          popup.add(delete);
          popup.show(table, me.getX(), me.getY());
        }
      }
      public void mouseReleased(MouseEvent me) { }
      public void mouseEntered(MouseEvent me) { }
      public void mouseExited(MouseEvent me) { }
      public void mousePressed(MouseEvent me) { }
    });
    /* End */

  }
  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#registerHooks()
   */
  protected void registerHooks() {
    //this view is a slave only view so it has no hooks to register
  }

  /* (non-Javadoc)
   * @see gate.gui.docview.AbstractDocumentView#unregisterHooks()
   */
  protected void unregisterHooks() {
    //this view is a slave only view so it has no hooks to register
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


  public void addAnnotation(Object tag, Annotation ann, AnnotationSet set){
    AnnotationHandler aHandler = new AnnotationHandler(set, ann);
    Object oldValue = annotationHandlerByTag.put(tag, aHandler);
    if(oldValue == null){
      //new value
      tagList.add(tag);
      int row = tagList.size() -1;
      if(tableModel != null) tableModel.fireTableRowsInserted(row, row);
    }else{
      //update old value
      int row = tagList.indexOf(tag);
      if(tableModel != null) tableModel.fireTableRowsUpdated(row,row);
    }
    //listen for the new annotation's events
    ann.addAnnotationListener(this);
  }

  public void removeAnnotation(Object tag){
    int row = tagList.indexOf(tag);
    if(row >= 0){
      tagList.remove(row);
      AnnotationHandler aHandler = (AnnotationHandler)
      		annotationHandlerByTag.remove(tag);
      if(aHandler != null)aHandler.ann.removeAnnotationListener(this);
      if(tableModel != null) tableModel.fireTableRowsDeleted(row, row);
    }
  }

  /**
   * Adds a batch of annotations in one go. The tags and annotations collections
   * are accessed through their iterators which are expected to return the
   * corresponding tag for the right annotation.
   * This method does not assume it was called from the UI Thread.
   * @param tags a collection of tags
   * @param annotations a collection of annotations
   * @param set the annotation set to which all the annotations belong.
   */
  public void addAnnotations(Collection tags, Collection annotations,
          AnnotationSet set){
    if(tags.size() != annotations.size()) throw new GateRuntimeException(
            "Invalid invocation - different numbers of annotations and tags!");
    Iterator tagIter = tags.iterator();
    Iterator annIter = annotations.iterator();
    while(tagIter.hasNext()){
      Object tag = tagIter.next();
      Annotation ann = (Annotation)annIter.next();
      AnnotationHandler aHandler = new AnnotationHandler(set, ann);
      Object oldValue = annotationHandlerByTag.put(tag, aHandler);
      if(oldValue == null){
        //new value
        tagList.add(tag);
        int row = tagList.size() -1;
      }else{
        //update old value
        int row = tagList.indexOf(tag);
      }
      //listen for the new annotation's events
      ann.addAnnotationListener(this);
    }
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        if(tableModel != null) tableModel.fireTableDataChanged();
      }
    });
  }

  public void removeAnnotations(Collection tags){
    Iterator tagIter = tags.iterator();
    while(tagIter.hasNext()){
      Object tag = tagIter.next();
      int row = tagList.indexOf(tag);
      if(row >= 0){
        tagList.remove(row);
        AnnotationHandler aHandler = (AnnotationHandler)
            annotationHandlerByTag.remove(tag);
        if(aHandler != null)aHandler.ann.removeAnnotationListener(this);
      }
    }
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        if(tableModel != null) tableModel.fireTableDataChanged();
      }
    });
  }

  public void annotationUpdated(AnnotationEvent e){
    //update all occurrences of this annotation
    Annotation ann = (Annotation)e.getSource();
    for(int i = 0; i < tagList.size(); i++){
      Object tag = tagList.get(i);
      if(((AnnotationHandler)annotationHandlerByTag.get(tag)).ann == ann){
        if(tableModel != null)tableModel.fireTableRowsUpdated(i, i);
      }
    }
  }

  class AnnotationTableModel extends AbstractTableModel{
    public int getRowCount(){
      return tagList.size();
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
      AnnotationHandler aHandler = (AnnotationHandler)annotationHandlerByTag.
      	get(tagList.get(row));
      switch(column){
        case TYPE_COL: return aHandler.ann.getType();
        case SET_COL: return aHandler.set.getName();
        case START_COL: return aHandler.ann.getStartNode().getOffset();
        case END_COL: return aHandler.ann.getEndNode().getOffset();
        case FEATURES_COL:
          //sort the features by name
          FeatureMap features = aHandler.ann.getFeatures();
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

  protected static class AnnotationHandler{
    public AnnotationHandler(AnnotationSet set, Annotation ann){
      this.ann = ann;
      this.set = set;
    }
    Annotation ann;
    AnnotationSet set;
  }

  protected XJTable table;
  protected AnnotationTableModel tableModel;
  protected JScrollPane scroller;
  protected Map annotationHandlerByTag;
  protected List tagList;
  protected JPanel mainPanel;
  protected JLabel statusLabel;
  protected TextualDocumentView textView;

  private static final int TYPE_COL = 0;
  private static final int SET_COL = 1;
  private static final int START_COL = 2;
  private static final int END_COL = 3;
  private static final int FEATURES_COL = 4;

}
