/*  AnnotationEditDialog.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  26/Jan/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.util.*;

import gate.*;
import gate.annotation.*;
import gate.util.*;
import gate.creole.*;

public class AnnotationEditDialog extends JDialog {

  // Local data
  AnnotationSchema annotSchema = null;
  FeaturesTableModel tableModel = null;
  Map name2featureSchemaMap = null;

  // Gui Components
  JTable  featuresTable = null;
  JButton removeFeatButton = null;
  JButton addFeatButton = null;
  JList   featureSchemaList = null;
  JButton okButton = null;
  JButton cancelButton = null;

  /** Constructs an AnnotationEditDialog
    * @param aFram the parent frame of this dialog
    * @param anAnnotationSchema object from which this dialog configures
    * @param aModal (wheter or not this dialog is modal)
    */
  public AnnotationEditDialog( Frame aFrame,
                               AnnotationSchema anAnnotationSchema,
                               boolean aModal) {

    super(aFrame, anAnnotationSchema.getAnnotationName(), aModal);
    annotSchema = anAnnotationSchema;
    this.setLocationRelativeTo(aFrame);

    initLocalData();
    initGuiComponents();
    initListeners();
  }//AnnotationEditDialog

  /** Init local data*/
  protected void initLocalData(){

    // Init name2featureSchemaMap
    Set featuresSch = annotSchema.getFeatureSchemaSet();
    if (featuresSch != null){
      Iterator iter = featuresSch.iterator();
      while (iter.hasNext()){
        FeatureSchema fs = (FeatureSchema) iter.next();
        name2featureSchemaMap.put(fs.getFeatureName(),fs);
      }// end while
    }// end if

  }// initLocalData();

  /** Init GUI components*/
  protected void initGuiComponents(){
    this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),
                                                  BoxLayout.Y_AXIS));
    //name field
    Box componentsBox = Box.createHorizontalBox();
    Box cancelOkBox = Box.createHorizontalBox();

    componentsBox.add(Box.createHorizontalStrut(5));
    // add the feature table

    componentsBox.add(Box.createHorizontalStrut(10));

    // add the remove put buttons

    componentsBox.add(Box.createHorizontalStrut(10));

    // add the Feature Schema list

    componentsBox.add(Box.createHorizontalStrut(5));

    // Add the buttons

    this.getContentPane().add(componentsBox);
    this.getContentPane().add(Box.createVerticalStrut(5));
    this.getContentPane().add(cancelOkBox);
    this.getContentPane().add(Box.createVerticalStrut(5));

    setSize(400, 300);
  }//initGuiComponents()

  /** Init all the listeners*/
  protected void initListeners(){

  }//initListeners()

  /** This method displays the AnnotationEditDialog in edit mode*/
  public FeatureMap show(FeatureMap aFeatMap, AnnotationSchema anAnnotSchema){
    return null;
  }// show()

  /** This method displays the AnnotationEditDialog in creating mode*/
  public FeatureMap  show(AnnotationSchema anAnnotSchema){
    return null;
  }// show()

/*
  public Resource show(ResourceData rData){
    nameField.setText("");
    ParameterList pList = rData.getParameterList();
//System.out.println(pList.getInitimeParameters());
    Iterator parIter = pList.getInitimeParameters().iterator();
    params.clear();
    while(parIter.hasNext()){
      params.add(new ParameterDisjunction((List)parIter.next()));
    }
    tableModel.fireTableDataChanged();
    pack();
    super.show();
    if(userCanceled) return null;
    else{
      //create the new resource
      FeatureMap params = Factory.newFeatureMap();
      for(int i=0; i< tableModel.getRowCount(); i++){
        ParameterDisjunction pDisj = (ParameterDisjunction)
                                     tableModel.getValueAt(i,0);
        if(pDisj.getValue() != null){
          params.put(pDisj.getName(), pDisj.getValue());
        }
      }
      Resource res;
      try{
        res = Factory.createResource(rData.getClassName(), params);
        res.getFeatures().put("NAME", nameField.getText());
      }catch(ResourceInstantiationException rie){
        JOptionPane.showMessageDialog(getOwner(),
                                      "Resource could not be created!\n" +
                                      rie.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
        res = null;
      }

      return res;
    }
  }
*/

  //inner classes

  protected class FeaturesTableModel extends AbstractTableModel{

    public FeaturesTableModel(){
    }// FeaturesTableModel

    public void fireTableDataChanged(){
      super.fireTableDataChanged();
    }

    public int getColumnCount(){return 3;}
/*
    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0: return ParameterDisjunction.class;
        case 1: return String.class;
        case 2: return Boolean.class;
        case 3: return String.class;
        default: return Object.class;
      }
    }//getColumnClass()
*/
    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0: return "Name";
        case 1: return "Value";
        case 2: return "Type";
        default: return "?";
      }
    }//public String getColumnName(int columnIndex)

    public boolean isCellEditable( int rowIndex,
                                   int columnIndex){

        if(columnIndex == 1) return true;
        if(columnIndex == 0 || columnIndex == 2) return false;
//        ParameterDisjunction pDisj =
//                      (ParameterDisjunction)params.get(rowIndex);
//        return pDisj.size() > 1;
  return true;
    }//isCellEditable

    public int getRowCount(){
    return 0;
    }//getRowCount()

    public Object getValueAt( int rowIndex,
                              int columnIndex){
/*
      ParameterDisjunction pDisj =
                    (ParameterDisjunction)params.get(rowIndex);
      switch(columnIndex){
        case 0: return pDisj;
        case 1: return pDisj.getType();
        case 2: return pDisj.getRequired();
        default: return "?";
      }
*/
  return new String();
    }//getValueAt

    public void setValueAt( Object aValue,
                            int rowIndex,
                            int columnIndex){

//      ParameterDisjunction pDisj =
//                    (ParameterDisjunction)params.get(rowIndex);
      switch(columnIndex){
        case 0:{

          break;
        }
        case 1:{
          // adaug in table model a randul i, valoarea citita
          // Mai intiai se face conversia la tipul dorit
          break;
        }
        case 2:{
          break;
        }
        case 3:{
//          pDisj.setValue((String)aValue);
          break;
        }
        default:{}
      }
    }
  }///class FeaturesTableModel extends DefaultTableModel

  // The EDITOR RENDERER

  class FeaturesEditor extends DefaultCellEditor{
    public FeaturesEditor(){
      super(new JComboBox());
      combo = (JComboBox)super.getComponent();
    }

    public Component getTableCellEditorComponent(JTable table,
                                             Object value,
                                             boolean isSelected,
                                             int row,
                                             int column){
//     ParameterDisjunction pDisj = (ParameterDisjunction)value;

//     combo.setModel(new DefaultComboBoxModel(pDisj.getNames()));
     return combo;
    }//getTableCellEditorComponent

    public Object getCellEditorValue(){
      return new Integer(combo.getSelectedIndex());
    }
    JComboBox combo;
  }//FeaturesEditor

}//AnnotationEditDialog