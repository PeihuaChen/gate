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

import gate.*;

import javax.swing.*;
import java.awt.BorderLayout;
import javax.swing.table.*;

import java.util.*;

import gate.creole.AbstractVisualResource;

public class FeaturesEditor extends AbstractVisualResource {

  public FeaturesEditor() {
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  protected void initLocalData(){
    features = Factory.newFeatureMap();
  }

  protected void initGuiComponents(){
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    tableModel = new FeaturesTableModel();
    table = new XJTable(tableModel);
    JScrollPane scroll = new JScrollPane(table);
    this.add(scroll, BorderLayout.CENTER);
  }

  protected void initListeners(){
  }

  public void setResource(gate.Resource newResource) {
    resource = newResource;
    features = resource.getFeatures();
    tableModel.fireTableDataChanged();
  }

  public gate.Resource getResource() {
    return resource;
  }

  XJTable table;
  FeaturesTableModel tableModel;
  private gate.Resource resource;
  FeatureMap features;

  class FeaturesTableModel extends AbstractTableModel{
    public int getColumnCount(){
      return 2;
    }
    public int getRowCount(){
      return features.size() + 1;
    }

    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0: return "Feature";
        case 1: return "Value";
        default: return "?";
      }
    }//public String getColumnName(int columnIndex)

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0: return String.class;
        case 1: return Object.class;
        default: return Object.class;
      }
    }

    public boolean isCellEditable(int rowIndex,
                              int columnIndex){
      return columnIndex == 1 || rowIndex == features.size();
    }

    public Object getValueAt(int rowIndex,
                         int columnIndex){
      List keys = new ArrayList(features.keySet());
      Collections.sort(keys);
      if(rowIndex == keys.size()){
        switch(columnIndex){
          case 0:{
            return newKey;
          }
          case 1:{
            return newValue;
          }
          default:{
            return null;
          }
        }
      }
      Object key = keys.get(rowIndex);
      switch(columnIndex){
        case 0:{
          return key;
        }
        case 1:{
          return features.get(key).toString();
        }
        default:{
          return null;
        }
      }
    }

    public void setValueAt(Object aValue,
                       int rowIndex,
                       int columnIndex){
      if(rowIndex == features.size()){
        if(columnIndex == 0){
          newKey = (String)aValue;
          if(newKey.equals("")) newKey = null;
        }
        else if(columnIndex == 1){
          newValue = aValue;
          if(newValue.equals("")) newValue = null;
        }

        if(newKey != null && newValue != null){
          features.put(newKey, newValue);
          newKey = null;
          newValue = null;
        }
      }else{
        //the column here is 2
        if(aValue.equals("")){
          //remove feature
          features.remove(getValueAt(rowIndex, 0));
        }else{
          //change feature
          features.put(getValueAt(rowIndex, 0), aValue);
        }
      }
    }

    String newKey;
    Object newValue;
  }///class FeaturesTableModel extends DefaultTableModel
}