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

public class ResourceViewer extends AbstractVisualResource {

  public ResourceViewer() {
    initLocalData();
    initGuiComponents();
    initListeners();
  }


  protected void initLocalData(){
    features = new ArrayList();
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
    features.clear();
    features.addAll(resource.getFeatures().entrySet());
    tableModel.fireTableDataChanged();
  }

  public gate.Resource getResource() {
    return resource;
  }

  XJTable table;
  FeaturesTableModel tableModel;
  private gate.Resource resource;
  List features;

  class FeaturesTableModel extends AbstractTableModel{
    public int getColumnCount(){return 2;}
    public int getRowCount(){
      return features.size();
    }

    public String getColumnName(int columnIndex){
      switch(columnIndex){
         case 0: return "Feature";
        case 1: return "Value";
        default: return "?";
      }
    }//public String getColumnName(int columnIndex)

    public Class getColumnClass(int columnIndex){
      return String.class;
    }

    public boolean isCellEditable(int rowIndex,
                              int columnIndex){
      return false;
    }

    public Object getValueAt(int rowIndex,
                         int columnIndex){
      Map.Entry entry = (Map.Entry)features.get(rowIndex);
      switch(columnIndex){
        case 0:{
          return entry.getKey().toString();
        }
        case 1:{
          return entry.getValue().toString();
        }
        default:{
          return null;
        }
      }
    }

    public void setValueAt(Object aValue,
                       int rowIndex,
                       int columnIndex){}
  }///class FeaturesTableModel extends DefaultTableModel
}