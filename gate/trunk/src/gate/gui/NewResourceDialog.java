/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 23/01/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.Frame;
import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.table.*;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.creole.*;

public class NewResourceDialog extends JDialog {
  public NewResourceDialog(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    outerClass = this;
    params = new ArrayList();
System.out.println("0" + params);
    tableModel = new ParametersTableModel();
    table = new XJTable(tableModel);
    this.getContentPane().add(table, BorderLayout.CENTER);
  }

  ParametersTableModel tableModel;
  JTable table;

  ArrayList params;
  NewResourceDialog outerClass;

  public Resource show(ResourceData rData){
    ParameterList pList = rData.getParameterList();
    Iterator parIter = pList.getInitimeParameters().iterator();
    while(parIter.hasNext()){
      params.add(new ParameterDisjunction((List)parIter.next()));
    }
    tableModel.fireTableDataChanged();
    super.show();
    return null;
  }

  //inner classes
  class ParametersTableModel extends DefaultTableModel{
    public int getColumnCount(){return 4;}

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0: return String.class;
        case 1: return String.class;
        case 2: return Boolean.class;
        case 3: return Object.class;
        default: return Object.class;
      }
    }

    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0: return "Name";
        case 1: return "Type";
        case 2: return "Required";
        case 3: return "Value";
        default: return "?";
      }
    }//public String getColumnName(int columnIndex)

    public boolean isCellEditable(int rowIndex,
                              int columnIndex){
        if(columnIndex == 3) return true;
        if(columnIndex == 1 || columnIndex == 2) return false;
        ParameterDisjunction pDisj =
                      (ParameterDisjunction)params.get(rowIndex);
        return pDisj.size() > 1;
    }

    public int getRowCount(){
      /*return outerClass.params.size();*/
      if(params == null){
        System.out.println("Null indeed!");
        return 0;
      }else{
        System.out.println("Like hell null!");
        return params.size();
      }
    }

    public Object getValueAt(int rowIndex,
                         int columnIndex){
      ParameterDisjunction pDisj =
                    (ParameterDisjunction)params.get(rowIndex);
      switch(columnIndex){
        case 0: return pDisj.getName();
        case 1: return pDisj.getType();
        case 2: return pDisj.getRequired();
        case 3: return pDisj.getValue();
        default: return "?";
      }
    }

    public void setValueAt(Object aValue,
                       int rowIndex,
                       int columnIndex){
      ParameterDisjunction pDisj =
                    (ParameterDisjunction)params.get(rowIndex);
      switch(columnIndex){
        case 0:{
          pDisj.setName((String) aValue);
          break;
        }
        case 1:{
          break;
        }
        case 2:{
          break;
        }
        case 3:{
          pDisj.setValue(aValue);
          break;
        }
        default:{}
      }
    }

  }///class FeaturesTableModel extends DefaultTableModel

  class ParameterDisjunction{
    public ParameterDisjunction(List options){
      this.options = options;
      selectedIndex = 0;
    }

    public void setSelectedIndex(int index){
      selectedIndex = index;
      Parameter par = (Parameter)options.get(selectedIndex);
      required = !par.isOptional();
      typeName = par.getTypeName();
      name = par.getName();
      try{
        value = par.getDefaultValue();
      }catch(ParameterException pe){
        throw new GateRuntimeException(pe.toString());
      }
    }

    public int size(){
      return options.size();
    }

    public Boolean getRequired(){return new Boolean(required);}

    public void setValue(Object aValue){
      value = aValue;
    }
    public Object getValue(){return value;}

    public String getType(){return typeName;}

    public String getName(){return name;}
    public void setName(String name){
      //NOP
    }

    int selectedIndex;
    List options;
    boolean required;
    Object value;
    String typeName;
    String name;
  }
}