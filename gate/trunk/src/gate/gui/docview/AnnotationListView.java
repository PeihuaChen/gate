/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
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
import java.awt.Component;
import java.util.*;
import java.util.List;
import java.util.Map;
import javax.swing.JScrollPane;
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
//    table.setAutoResizeMode(XJTable.AUTO_RESIZE_LAST_COLUMN);
    table.setSortable(true);
    table.setSortedColumn(START_COL);
    scroller = new JScrollPane(table);
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
   * @see gate.gui.docview.DocumentView#getGUI()
   */
  public Component getGUI() {
    return scroller;
  }
  
  /* (non-Javadoc)
   * @see gate.gui.docview.DocumentView#getType()
   */
  public int getType() {
    return HORIZONTAL;
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

  XJTable table;
  AnnotationTableModel tableModel;
  JScrollPane scroller;
  Map annotationHandlerByTag;
  List tagList; 
  private static final int TYPE_COL = 0;
  private static final int SET_COL = 1;
  private static final int START_COL = 2;
  private static final int END_COL = 3;
  private static final int FEATURES_COL = 4;
  
}
