/*
 *  AnnotationDiffTableModel.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 11/Feb/2000
 *
 *  Cristian URSU, 07/Nov/2000
 *
 *  $Id$
 */

package gate.annotation;

import java.util.*;

import gate.*;
import gate.gui.*;
import gate.util.*;


  /** This class implements a SortedTableModel*/
  public class AnnotationDiffTableModel extends gate.gui.SortedTableModel{

    /** Debug flag */
    private static final boolean DEBUG = false;

    private SortedTableComparator comparator = null;

    /** Constructor uses inside a AnnotationSetComparator (implemenetd based on
      * a SortedTableComparator )
      */
    public AnnotationDiffTableModel(){
      comparator = new AnnotationDiffComparator();
    }// AnnotationSetTableModel

    public void setData(Collection data){
      super.setData(data,comparator);
    }
    /** As requested by TableModel Interface*/
    public int getColumnCount() {
      return 9;
    } // getColumnCount

    /** Get the column class*/
    public Class getColumnClass(int column) {
      return new String("").getClass();
    } // getColumnClass

    /** gets the column name. As requested by TableModel interface.*/
    public String getColumnName(int column) {
      switch(column){
        case 0:return "Start(key)";
        case 1:return "End(key)";// + addSortOrderString(1);
        case 2:return "Type(key)";
        case 3:return "Features(key)";
        case 4:return "   ";
        case 5:return "Start(resp)";
        case 6:return "End(resp)";
        case 7:return "Type(resp)";
        case 8:return "Features(resp)";
      }// switch
      return null;
    } // getColumnName

    /** This has to be implemented if the table has editable cells*/
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    } // isCellEditable

    /** As requested by TableModel interface*/
    public Object getValueAt(int row, int column) {

      AnnotationDiff.DiffSetElement diffElement = null;
      diffElement = (AnnotationDiff.DiffSetElement) m_data.get(row);
      if (diffElement == null)
        return null;

      switch(column){
        case 0:{
          if (diffElement.getLeftAnnotation() == null)
            return "";
          else
            return diffElement.getLeftAnnotation().getStartNode().getOffset();
        }
        case 1:{
          if (diffElement.getLeftAnnotation() == null)
            return "";
          else
            return diffElement.getLeftAnnotation().getEndNode().getOffset();
        }
        case 2:{
          if (diffElement.getLeftAnnotation() == null)
            return "";
          else
            return diffElement.getLeftAnnotation().getType();
        }
        case 3:{
          if (diffElement.getLeftAnnotation() == null)
            return "";
          else
            return diffElement.getLeftAnnotation().getFeatures();
        }
        case 4: return "   ";
        case 5:{
          if (diffElement.getRightAnnotation() == null)
            return "";
          else
            return diffElement.getRightAnnotation().getStartNode().getOffset();
        }
        case 6:{
          if (diffElement.getRightAnnotation() == null)
            return "";
          else
            return diffElement.getRightAnnotation().getEndNode().getOffset();
        }
        case 7:{
          if (diffElement.getRightAnnotation() == null)
            return "";
          else
            return diffElement.getRightAnnotation().getType();
        }
        case 8:{
          if (diffElement.getRightAnnotation() == null)
            return "";
          else
            return diffElement.getRightAnnotation().getFeatures();
        }
      }// End Switch
      return null;
    } // getValueAt

    /** Returns the max value*/
    public Object getMaxValue(int column){
      String maxValue = "";
      int maxValueLength = 0;

      switch(column){
        case 0:
                 for (int i = 0 ; i < getRowCount(); i++){
                   AnnotationDiff.DiffSetElement diffElement = null;
                   diffElement = (AnnotationDiff.DiffSetElement) m_data.get(i);
                   if ( diffElement != null &&
                        diffElement.getLeftAnnotation() != null ){
                      String strValue =
          diffElement.getLeftAnnotation().getStartNode().getOffset().toString();
                      int length = strValue.length();
                      if (length > maxValueLength){
                        maxValueLength = length;
                        maxValue = strValue;
                      }// end If
                   }// end If
                 }// End for
                 return maxValue;

        case 1:
                 for (int i = 0 ; i < getRowCount(); i++){
                   AnnotationDiff.DiffSetElement diffElement = null;
                   diffElement = (AnnotationDiff.DiffSetElement) m_data.get(i);
                   if ( diffElement != null &&
                        diffElement.getLeftAnnotation() != null ){
                      String strValue =
          diffElement.getLeftAnnotation().getEndNode().getOffset().toString();
                      int length = strValue.length();
                      if (length > maxValueLength){
                        maxValueLength = length;
                        maxValue = strValue;
                      }// end If
                   }// end If
                 }// End for
                 return maxValue;

        case 2:
                 for (int i = 0 ; i < getRowCount(); i++){
                   AnnotationDiff.DiffSetElement diffElement = null;
                   diffElement = (AnnotationDiff.DiffSetElement) m_data.get(i);
                   if ( diffElement != null &&
                        diffElement.getLeftAnnotation() != null ){
                      String strValue =
                        diffElement.getLeftAnnotation().getType();
                      int length = strValue.length();
                      if (length > maxValueLength){
                        maxValueLength = length;
                        maxValue = strValue;
                      }// end If
                   }// end If
                 }// End for
                 return maxValue;

        case 3:
                 for (int i = 0 ; i < getRowCount(); i++){
                   AnnotationDiff.DiffSetElement diffElement = null;
                   diffElement = (AnnotationDiff.DiffSetElement) m_data.get(i);
                   if ( diffElement != null &&
                        diffElement.getLeftAnnotation() != null ){
                      String strValue =
                      diffElement.getLeftAnnotation().getFeatures().toString();
                      int length = strValue.length();
                      if (length > maxValueLength){
                        maxValueLength = length;
                        maxValue = strValue;
                      }// end If
                   }// end If
                 }// End for
                 return maxValue;

        case 4: return "   ";

        case 5:
                 for (int i = 0 ; i < getRowCount(); i++){
                   AnnotationDiff.DiffSetElement diffElement = null;
                   diffElement = (AnnotationDiff.DiffSetElement) m_data.get(i);
                   if ( diffElement != null &&
                        diffElement.getRightAnnotation() != null ){
                      String strValue =
          diffElement.getRightAnnotation().getStartNode().getOffset().toString();
                      int length = strValue.length();
                      if (length > maxValueLength){
                        maxValueLength = length;
                        maxValue = strValue;
                      }// end If
                   }// end If
                 }// End for
                 return maxValue;

        case 6:
                 for (int i = 0 ; i < getRowCount(); i++){
                   AnnotationDiff.DiffSetElement diffElement = null;
                   diffElement = (AnnotationDiff.DiffSetElement) m_data.get(i);
                   if ( diffElement != null &&
                        diffElement.getRightAnnotation() != null ){
                      String strValue =
          diffElement.getRightAnnotation().getEndNode().getOffset().toString();
                      int length = strValue.length();
                      if (length > maxValueLength){
                        maxValueLength = length;
                        maxValue = strValue;
                      }// end If
                   }// end If
                 }// End for
                 return maxValue;
        case 7:
                 for (int i = 0 ; i < getRowCount(); i++){
                   AnnotationDiff.DiffSetElement diffElement = null;
                   diffElement = (AnnotationDiff.DiffSetElement) m_data.get(i);
                   if ( diffElement != null &&
                        diffElement.getRightAnnotation() != null ){
                      String strValue =
                             diffElement.getRightAnnotation().getType();
                      int length = strValue.length();
                      if (length > maxValueLength){
                        maxValueLength = length;
                        maxValue = strValue;
                      }// end If
                   }// end If
                 }// End for
                 return maxValue;
        case 8:
                 for (int i = 0 ; i < getRowCount(); i++){
                   AnnotationDiff.DiffSetElement diffElement = null;
                   diffElement = (AnnotationDiff.DiffSetElement) m_data.get(i);
                   if ( diffElement != null &&
                        diffElement.getRightAnnotation() != null ){
                      String strValue =
                      diffElement.getRightAnnotation().getFeatures().toString();
                      int length = strValue.length();
                      if (length > maxValueLength){
                        maxValueLength = length;
                        maxValue = strValue;
                      }// end If
                   }// end If
                 }// End for
                 return maxValue;

     }// end switch()
     return null;
    } // getMaxValue

    class AnnotationDiffComparator extends gate.gui.SortedTableComparator{
      /** Constructor*/
      public AnnotationDiffComparator(){}

      /** This method compares TWO DiffSetElements.
        * @return -1 (o1 < o2)
        * @return 0  (o1 == o2)
        * @return 1  (o1 > o2)
        */
      public int compare(Object o1, Object o2) {
        if ( !(o1 instanceof AnnotationDiff.DiffSetElement) ||
             !(o2 instanceof AnnotationDiff.DiffSetElement)
           ) return 0;

        AnnotationDiff.DiffSetElement d1 = (AnnotationDiff.DiffSetElement) o1;
        AnnotationDiff.DiffSetElement d2 = (AnnotationDiff.DiffSetElement) o2;

        if (d1 == null || d2 == null)
          return 0;

        int result = 0;

        switch(this.getSortCol()) {
          // Start - LeftAnnotation
          case 0:{
            if( d1.getLeftAnnotation() == null &&
                d2.getLeftAnnotation() == null
              ) return 0;
            if( d1.getLeftAnnotation() == null &&
                d2.getLeftAnnotation() != null
              ) return -1;
            if( d1.getLeftAnnotation() != null &&
                d2.getLeftAnnotation() == null
              ) return 1;
            Long l1 = d1.getLeftAnnotation().getStartNode().getOffset();
            Long l2 = d2.getLeftAnnotation().getStartNode().getOffset();
            result = l1.compareTo(l2);
          }break;
          // End - LeftAnnotation
          case 1:
          {
            if( d1.getLeftAnnotation() == null &&
                d2.getLeftAnnotation() == null
              ) return 0;
            if( d1.getLeftAnnotation() == null &&
                d2.getLeftAnnotation() != null
              ) return -1;
            if( d1.getLeftAnnotation() != null &&
                d2.getLeftAnnotation() == null
              ) return 1;
            Long l1 = d1.getLeftAnnotation().getEndNode().getOffset();
            Long l2 = d2.getLeftAnnotation().getEndNode().getOffset();
            result = l1.compareTo(l2);
          }break;
           // Type - LeftAnnotation
          case 2:{
            if( d1.getLeftAnnotation() == null &&
                d2.getLeftAnnotation() == null
              ) return 0;
            if( d1.getLeftAnnotation() == null &&
                d2.getLeftAnnotation() != null
              ) return -1;
            if( d1.getLeftAnnotation() != null &&
                d2.getLeftAnnotation() == null
              ) return 1;
            String s1 = d1.getLeftAnnotation().getType();
            String s2 = d2.getLeftAnnotation().getType();
            result = s1.compareTo(s2);
          }break;

          // Features - LeftAnnotation
          case 3:{
            if( d1.getLeftAnnotation() == null &&
                d2.getLeftAnnotation() == null
              ) return 0;
            if( d1.getLeftAnnotation() == null &&
                d2.getLeftAnnotation() != null
              ) return -1;
            if( d1.getLeftAnnotation() != null &&
                d2.getLeftAnnotation() == null
              ) return 1;
            String s1 = d1.getLeftAnnotation().getFeatures().toString();
            String s2 = d2.getLeftAnnotation().getFeatures().toString();
            result = s1.compareTo(s2);
          }break;

          // Blank column
          case 4: return 0;

          // Start - RightAnnotation
          case 5:{
            if( d1.getRightAnnotation() == null &&
                d2.getRightAnnotation() == null
              ) return 0;
            if( d1.getRightAnnotation() == null &&
                d2.getRightAnnotation() != null
              ) return -1;
            if( d1.getRightAnnotation() != null &&
                d2.getRightAnnotation() == null
              ) return 1;
            Long l1 = d1.getRightAnnotation().getStartNode().getOffset();
            Long l2 = d2.getRightAnnotation().getStartNode().getOffset();
            result = l1.compareTo(l2);
          }break;

          // End - RightAnnotation
          case 6:{
            if( d1.getRightAnnotation() == null &&
                d2.getRightAnnotation() == null
              ) return 0;
            if( d1.getRightAnnotation() == null &&
                d2.getRightAnnotation() != null
              ) return -1;
            if( d1.getRightAnnotation() != null &&
                d2.getRightAnnotation() == null
              ) return 1;
            Long l1 = d1.getRightAnnotation().getEndNode().getOffset();
            Long l2 = d2.getRightAnnotation().getEndNode().getOffset();
            result = l1.compareTo(l2);
          }break;
          // Type - RightAnnotation
          case 7:{
            if( d1.getRightAnnotation() == null &&
                d2.getRightAnnotation() == null
              ) return 0;
            if( d1.getRightAnnotation() == null &&
                d2.getRightAnnotation() != null
              ) return -1;
            if( d1.getRightAnnotation() != null &&
                d2.getRightAnnotation() == null
              ) return 1;
            String s1 = d1.getRightAnnotation().getType();
            String s2 = d2.getRightAnnotation().getType();
            result = s1.compareTo(s2);
          }break;
          // Feature - RightAnnotation
          case 8:{
            if( d1.getRightAnnotation() == null &&
                d2.getRightAnnotation() == null
              ) return 0;
            if( d1.getRightAnnotation() == null &&
                d2.getRightAnnotation() != null
              ) return -1;
            if( d1.getRightAnnotation() != null &&
                d2.getRightAnnotation() == null
              ) return 1;
            String s1 = d1.getRightAnnotation().getFeatures().toString();
            String s2 = d2.getRightAnnotation().getFeatures().toString();
            result = s1.compareTo(s2);
          }break;
        }// switch

        if (!this.getSortOrder())
          result = -result;
        return result;
      }//compare

    } // class AnnotationSetComparator
  Document document;
} // AnnotationSetTableModel
