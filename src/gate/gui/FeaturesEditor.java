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
import gate.util.*;
import gate.swing.*;
import gate.creole.*;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;

import java.util.*;

import gate.creole.AbstractVisualResource;

public class FeaturesEditor extends AbstractVisualResource{

  public FeaturesEditor() {
    initLocalData();
    initGuiComponents();
    initListeners();
  }// FeaturesEditor()

  protected void initLocalData(){
    features = Factory.newFeatureMap();
  }

  protected void initGuiComponents(){
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    tableModel = new FeaturesTableModel();
    table = new XJTable(tableModel);
    table.setIntercellSpacing(new Dimension(5,5));
    table.setDefaultRenderer(String.class, new ObjectRenderer());
    table.setDefaultRenderer(Object.class, new ObjectRenderer());

    DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
    editor.setClickCountToStart(0);
    table.setDefaultEditor(String.class, editor);
    table.setDefaultEditor(Object.class, editor);

    JScrollPane scroll = new JScrollPane(table);
    this.add(scroll, BorderLayout.CENTER);
    this.add(Box.createVerticalStrut(5));

    Box box = Box.createHorizontalBox();
    newFeatureField = new JTextField(10);
    newFeatureField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                                 newFeatureField.
                                                 getPreferredSize().height));

    Box vBox = Box.createVerticalBox();
    vBox.add(new JLabel("New feature name"));
    vBox.add(newFeatureField);
    box.add(vBox);
    box.add(Box.createHorizontalStrut(5));

    newValueField = new JTextField(10);
    newValueField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                                 newValueField.
                                                 getPreferredSize().height));

    vBox = Box.createVerticalBox();
    vBox.add(new JLabel("New feature value"));
    vBox.add(newValueField);
    box.add(vBox);
    box.add(Box.createHorizontalStrut(5));

    addNewBtn = new JButton("Add feature");
    box.add(addNewBtn);

    this.add(box);
    this.add(Box.createVerticalGlue());

  }// protected void initGuiComponents()

  protected void initListeners(){
    addNewBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String name = newFeatureField.getText();
        String value = newValueField.getText();
        if(name != null){
          features.put(name, value);
          tableModel.fireTableDataChanged();
          newFeatureField.setText("");
          newValueField.setText("");
        }
      }
    });
  }

  public void setFeatureBearer(FeatureBearer newResource) {
    resource = newResource;
    features = resource.getFeatures();
    tableModel.fireTableDataChanged();
  }// public void setFeatureBearer(FeatureBearer newResource)

  public void setTarget(Object target) {
    if(target instanceof FeatureBearer){
      setFeatureBearer((FeatureBearer)target);
    }else{
      throw new IllegalArgumentException(
        "FeatureEditors can only be used with FeatureBearer!\n" +
        target.getClass().toString() + " is not a FeatureBearer!");
    }
  }//public void setResource(Resource resource)

  public void setHandle(ResourceHandle handle){
    //NOP
  }


  public FeatureBearer getFeatureBearer() {
    return resource;
  }

  XJTable table;
  FeaturesTableModel tableModel;
  private FeatureBearer resource;
  FeatureMap features;
  JTextField newFeatureField;
  JTextField newValueField;

  JButton addNewBtn;

  class FeaturesTableModel extends AbstractTableModel{
    public int getColumnCount(){
      return 2;
    }

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
      switch(columnIndex){
        case 0: return String.class;
        case 1: return Object.class;
        default: return Object.class;
      }
    }

    public boolean isCellEditable(int rowIndex,
                              int columnIndex){
      return rowIndex == features.size()
             ||
             ((!((String)table.getModel().getValueAt(rowIndex, 0)).
              startsWith("gate."))
             );
    }// public boolean isCellEditable

    public Object getValueAt(int rowIndex,
                         int columnIndex){
      List keys = new ArrayList(features.keySet());
      Collections.sort(keys);
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
    }// public Object getValueAt

    public void setValueAt(Object aValue,
                       int rowIndex,
                       int columnIndex){

      if(columnIndex == 0) {
        //the name of the feature changed
        //if the name is null or empty the feature will be deleted
        String oldName = (String)getValueAt(rowIndex, 0);
        Object oldValue = features.remove(oldName);
        if(aValue != null && !aValue.equals("")){
          features.put(aValue, oldValue);
        }
      } else {
        //the value of a feature changed
        features.put(getValueAt(rowIndex, 0), aValue);
      }
      fireTableDataChanged();
    }// public void setValueAt

  }///class FeaturesTableModel extends DefaultTableModel
/*
  class FeaturesTableRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column){

      super.getTableCellRendererComponent(table, value, false, hasFocus,
                                          row, column);
      setEnabled(table.isCellEditable(row, column));
      return this;
    }

  }// class FeaturesTableRenderer
*/
}// class FeaturesEditor