/*
 *  AnnotationSetTableModel.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 11/07/2000
 *
 *  $Id$
 */

package gate.gui;

import gate.*;
  /** This class implements a SortedTableModel*/
  public class AnnotationSetTableModel extends gate.gui.SortedTableModel {

    /** Debug flag */
    private static final boolean DEBUG = false;

    /** Constructor uses inside a AnnotationSetComparator (implemenetd based on
      * a SortedTableComparator )
      */
    public AnnotationSetTableModel(Document doc, AnnotationSet as) {
      setData (as, new AnnotationSetComparator());
      document = doc;
    }// AnnotationSetTableModel

    /** As requested by TableModel Interface*/
    public int getColumnCount() {
      return 4;
    } // getColumnCount

    /** Get the column class*/
    public Class getColumnClass(int column) {
      return new String("0").getClass();
    } // getColumnClass

    /** gets the column name. As requested by TableModel interface.*/
    public String getColumnName(int column) {
      switch(column){
        case 0:{
          return "Start";// + addSortOrderString(0);
        }
        case 1:{
          return "End";// + addSortOrderString(1);
        }
        case 2:{
          return "Type";// + addSortOrderString(2);
        }
        case 3:{
          return "Features";// + addSortOrderString(3);
        }
      }
      return null;
    } // getColumnName

    /** This has to be implemented if the table has editable cells*/
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    } // isCellEditable

    /** As requested by TableModel interface*/
    public Object getValueAt(int row, int column) {
      gate.Annotation currentAnn = (gate.Annotation) m_data.get(row);
      switch(column){
        case 0:{
          return currentAnn.getStartNode().getOffset();
        }
        case 1:{
          return currentAnn.getEndNode().getOffset();
        }
        case 2:{
          return currentAnn.getType();
        }
        case 3:{
          return currentAnn.getFeatures();
        }
      }
      return null;
    } // getValueAt

    public Object getMaxValue(int column) {
      String maxValue = null;
      int maxValueLength = 0;

      switch(column){
        case 0:
                 for (int i = 0 ; i < getRowCount(); i++){
                   String strValue =((gate.Annotation)
                          m_data.get(i)).getStartNode().getOffset().toString();
                   int length = strValue.length();
                   if (length > maxValueLength){
                      maxValueLength = length;
                      maxValue = strValue;
                   }
                 }
                 return maxValue;

        case 1:
                 for (int i = 0 ; i < getRowCount(); i++){
                   String strValue = ((gate.Annotation)
                            m_data.get(i)).getEndNode().getOffset().toString();
                   int length = strValue.length();
                   if (length > maxValueLength){
                      maxValueLength = length;
                      maxValue = strValue;
                   }
                 }
                 return maxValue;

        case 2:
                 for (int i = 0 ; i < getRowCount(); i++){
                   String strValue = ((gate.Annotation)m_data.get(i)).getType();
                   int length = strValue.length();
                   if (length > maxValueLength){
                      maxValueLength = length;
                      maxValue = strValue;
                   }
                 }
                 return maxValue;

        case 3:
                 for (int i = 0 ; i < getRowCount(); i++){
                   String strValue = ((gate.Annotation)
                                        m_data.get(i)).getFeatures().toString();
                   int length = strValue.length();
                   if (length > maxValueLength){
                      maxValueLength = length;
                      maxValue = strValue;
                   }
                 }
                 return maxValue;
     }
     return null;
    } // getMaxValue

    class AnnotationSetComparator extends gate.gui.SortedTableComparator {

      public AnnotationSetComparator(){
      }

      public int compare(Object o1, Object o2) {
        if ( !(o1 instanceof gate.Annotation) ||
             !(o2 instanceof gate.Annotation)) return 0;

        gate.Annotation a1 = (gate.Annotation) o1;
        gate.Annotation a2 = (gate.Annotation) o2;
        int result = 0;

        switch(this.getSortCol()) {
          case 0: // Start
          {
            Long l1 = a1.getStartNode().getOffset();
            Long l2 = a2.getStartNode().getOffset();
            result = l1.compareTo(l2);
          }break;
          case 1: // End
          {
            Long l1 = a1.getEndNode().getOffset();
            Long l2 = a2.getEndNode().getOffset();
            result  = l1.compareTo(l2);
          }break;
          case 2: // Type
          {
            String s1 = a1.getType();
            String s2 = a2.getType();
            result = s1.compareTo(s2);
          }break;
          case 3: // Features
          {
            String fm1 = a1.getFeatures().toString();
            String fm2 = a2.getFeatures().toString();
            result = fm1.compareTo(fm2);
          }break;
        }// switch

        if (!this.getSortOrder()) result = -result;
        return result;
      }//compare

    } // class AnnotationSetComparator
  Document document;
} // AnnotationSetTableModel
