/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 03/10/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.util.*;
import java.net.URL;
import java.io.IOException;
import java.text.*;

import gate.*;
import gate.util.*;
import gate.swing.*;
import gate.creole.*;

/**
 * Allows the editing of a set of parameters for a resource. It needs a pointer
 * to the resource and a list of the parameter names for the parameters that
 * should be displayed. The list of the parameters is actually a list of lists
 * of strings representing parameter disjunctions.
 */
public class ResourceParametersEditor extends XJTable{

  public ResourceParametersEditor(){
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  /**
   * Initialises this GUI component.
   * @param the resource for which the parameters need to be set.
   * @param paramaters a list of lists of {@link Parameter} representing
   * parameter disjunctions.
   */
  public void init(Resource resource, List parameters){
    this.resource = resource;
    if(parameters != null){
      parameterDisjunctions = new ArrayList(parameters.size());
      for(int i = 0; i < parameters.size(); i++){
        parameterDisjunctions.add(
                            new ParameterDisjunction(resource,
                                                     (List)parameters.get(i)));
      }
    }else{
      parameterDisjunctions = null;
    }
    tableModel.fireTableDataChanged();
    adjustSizes();
  }

  protected void initLocalData(){
    resource = null;
    parameterDisjunctions = null;
  }// protected void initLocalData()

  protected void initGuiComponents(){
    setModel(tableModel = new ParametersTableModel());

    getColumnModel().getColumn(0).
                     setCellRenderer(new ParameterDisjunctionRenderer());

    getColumnModel().getColumn(1).
                     setCellRenderer(new DefaultTableCellRenderer());

    getColumnModel().getColumn(2).
                     setCellRenderer(new BooleanRenderer());

    getColumnModel().getColumn(3).
                     setCellRenderer(new ParameterValueRenderer());


    getColumnModel().getColumn(0).
                     setCellEditor(new ParameterDisjunctionEditor());

    getColumnModel().getColumn(3).
                     setCellEditor(new ParameterValueEditor());

    setIntercellSpacing(new Dimension(5, 5));
  }// protected void initGuiComponents()


  protected void initListeners(){
  }

  /**
   * Should this GUI comonent allow editing?
   */

  /**
   * Sets the parameters for the resource to their new values as resulted
   * from the user's edits.
   */
  public void setParameters() throws ResourceInstantiationException{
    //stop current edits
    if(getEditingColumn() != -1 && getEditingRow() != -1){
      editingStopped(new ChangeEvent(getCellEditor(getEditingRow(),
                                                   getEditingColumn())));
    }
    //set the parameters
    for(int i = 0; i < parameterDisjunctions.size(); i++){
      ParameterDisjunction pDisj = (ParameterDisjunction)
                                   parameterDisjunctions.get(i);
      resource.setParameterValue(pDisj.getName(), pDisj.getValue());
    }
  }

  /**
   * Does this GUI component allow editing?
   */

  public Resource getResource() {
    return resource;
  }

  /**
   * Gets the current values for the parameters.
   * @return a {@link FeatureMap} conatining the curent values for the curently
   * selected parameters in each disjunction.
   */
  public FeatureMap getParameterValues(){
    //stop current edits
    if(getEditingColumn() != -1 && getEditingRow() != -1){
      editingStopped(new ChangeEvent(getCellEditor(getEditingRow(),
                                                   getEditingColumn())));
    }
    //get the parameters
    FeatureMap values = Factory.newFeatureMap();
    if(parameterDisjunctions != null){
      for(int i = 0; i < parameterDisjunctions.size(); i++){
        ParameterDisjunction pDisj = (ParameterDisjunction)
                                     parameterDisjunctions.get(i);
        values.put(pDisj.getName(), pDisj.getValue());
      }
    }
    return values;
  }

  ParametersTableModel tableModel;
  Resource resource;



  /**
   * A list of {@link ParameterDisjunction}
   */
  protected List parameterDisjunctions;

  //inner classes
  protected class ParametersTableModel extends AbstractTableModel{

    public int getColumnCount(){return 4;}

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0: return ParameterDisjunction.class;
        case 1: return String.class;
        case 2: return Boolean.class;
        case 3: return Object.class;
        default: return Object.class;
      }
    }// public Class getColumnClass(int columnIndex)

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
                              int columnIndex) {
      switch(columnIndex){
        case 0: return ((ParameterDisjunction)
                        parameterDisjunctions.get(rowIndex)).size() > 1;
        case 1: return false;
        case 2: return false;
        case 3: return true;
        default: return false;
      }
    }// public boolean isCellEditable

    public int getRowCount(){
      return (parameterDisjunctions == null) ? 0 : parameterDisjunctions.size();
    }

    public Object getValueAt(int rowIndex,
                         int columnIndex) {
      ParameterDisjunction pDisj = (ParameterDisjunction)
                                   parameterDisjunctions.get(rowIndex);
      switch(columnIndex){
        case 0: return pDisj;
        case 1: return pDisj.getType();
        case 2: return pDisj.isRequired();
        case 3: return pDisj.getValue();
        default: return "?";
      }
    }// public Object getValueAt

    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
      ParameterDisjunction pDisj = (ParameterDisjunction)
                                   parameterDisjunctions.get(rowIndex);
      switch(columnIndex){
        case 0:{
          pDisj.setSelectedIndex(((Integer)aValue).intValue());
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
    }// public void setValueAt
  }///class FeaturesTableModel extends DefaultTableModel

  class ParameterDisjunctionRenderer extends DefaultTableCellRenderer {
    public ParameterDisjunctionRenderer(){
      combo = new JComboBox();
      class CustomRenderer extends JLabel implements ListCellRenderer {
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus){

          setText(text);
          setIcon(MainFrame.getIcon(iconName));
          return this;
        }
      };
      combo.setRenderer(new CustomRenderer());
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
      ParameterDisjunction pDisj = (ParameterDisjunction)value;
      text = pDisj.getName();
      String type = pDisj.getType();
      iconName = "param.gif";
      if(Gate.getCreoleRegister().containsKey(type)){
        ResourceData rData = (ResourceData)Gate.getCreoleRegister().get(type);
        if(rData != null) iconName = rData.getIcon();
      }
      if(pDisj.size() > 1){
        combo.setModel(new DefaultComboBoxModel(new Object[]{text}));
        return combo;
      }
      //prepare the renderer
      Component comp = super.getTableCellRendererComponent(table,
                                                           text,
                                                           isSelected, hasFocus,
                                                           row, column);
      setIcon(MainFrame.getIcon(iconName));
      return this;
    }// public Component getTableCellRendererComponent

    //combobox used for OR parameters
    JComboBox combo;
    String iconName;
    String text;
  }//class ParameterDisjunctionRenderer


  /**
   * A renderer that displays a File Open button next to a text field.
   * Used for setting URLs from files.
   */
  class ParameterValueRenderer extends ObjectRenderer {
    ParameterValueRenderer() {
      button = new JButton(MainFrame.getIcon("loadFile.gif"));
      button.setToolTipText("Set from file...");
      textButtonBox = new JPanel();
      textButtonBox.setLayout(new BoxLayout(textButtonBox, BoxLayout.X_AXIS));
      textButtonBox.setOpaque(false);

      combo = new JComboBox();
      combo.setRenderer(new ResourceRenderer());
    }// CustomObjectRenderer()

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {

      String type = ((ParameterDisjunction)table.getValueAt(row, 0)).getType();
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().get(type);

      if(rData != null){
        //Gate type
        combo.setModel(new DefaultComboBoxModel(new Object[]{value}));
        return combo;
      }else{
        //non Gate type -> we'll use the text field
        String text = (value == null) ?
                      "                                        " +
                      "                                        " :
                      value.toString();
        //prepare the renderer
        super.getTableCellRendererComponent(table, text, isSelected,
                                              hasFocus, row, column);

        if(type.equals("java.net.URL")){
          textButtonBox.removeAll();
          textButtonBox.add(this);
          this.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                            getPreferredSize().height));
          textButtonBox.add(Box.createHorizontalStrut(5));
          textButtonBox.add(button);
          return textButtonBox;
        }else return this;
      }
    }// public Component getTableCellRendererComponent

    JButton button;
    JComboBox combo;
    JPanel textButtonBox;
  }//class ObjectRenderer extends DefaultTableCellRenderer

  class ParameterDisjunctionEditor extends DefaultCellEditor{
    public ParameterDisjunctionEditor(){
      super(new JComboBox());
      combo = (JComboBox)super.getComponent();
      class CustomRenderer extends JLabel implements ListCellRenderer {
        public CustomRenderer(){
          setOpaque(true);
        }
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus){
          if (isSelected) {
              setBackground(list.getSelectionBackground());
              setForeground(list.getSelectionForeground());
          }
          else {
              setBackground(list.getBackground());
              setForeground(list.getForeground());
          }

          setFont(list.getFont());

          setText((String)value);

          String iconName = "param.gif";
          Parameter[] params = pDisj.getParameters();
          for(int i = 0; i < params.length; i++){
            Parameter param = (Parameter)params[i];
            if(param.getName().equals(value)){
              String type = param.getTypeName();
              if(Gate.getCreoleRegister().containsKey(type)){
                ResourceData rData = (ResourceData)
                                     Gate.getCreoleRegister().get(type);
                if(rData != null) iconName = rData.getIcon();
              }
              break;
            }//if(params[i].getName().equals(value))
          }//for(int i = 0; params.length; i++)

          setIcon(MainFrame.getIcon(iconName));
          return this;
        }
      };//class CustomRenderer extends JLabel implements ListCellRenderer
      combo.setRenderer(new CustomRenderer());
      combo.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          stopCellEditing();
        }
      });
    }// public ParameterDisjunctionEditor()

    public Component getTableCellEditorComponent(JTable table,
                                             Object value,
                                             boolean isSelected,
                                             int row,
                                             int column){
     pDisj = (ParameterDisjunction)value;
     combo.setModel(new DefaultComboBoxModel(pDisj.getNames()));
     return combo;
    }// public Component getTableCellEditorComponent

    public Object getCellEditorValue(){
      return new Integer(combo.getSelectedIndex());
    }

    public boolean stopCellEditing(){
      combo.hidePopup();
      return super.stopCellEditing();
    }

    JComboBox combo;
    ParameterDisjunction pDisj;
  }// class ParameterDisjunctionEditor extends DefaultCellEditor

  class ParameterValueEditor extends AbstractCellEditor
                             implements TableCellEditor{
    ParameterValueEditor(){
      combo = new JComboBox();
      combo.setRenderer(new ResourceRenderer());

      textField = new JTextField();

      button = new JButton(new ImageIcon(getClass().getResource(
                               "/gate/resources/img/loadFile.gif")));
      button.setToolTipText("Set from file...");
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JFileChooser fileChooser = MainFrame.getFileChooser();
          int res = fileChooser.showOpenDialog(ResourceParametersEditor.this);
          if(res == fileChooser.APPROVE_OPTION){
            try {
              textField.setText(fileChooser.getSelectedFile().
                                toURL().toExternalForm());
            } catch(IOException ioe){}
          }
        }
      });
      textButtonBox = new JPanel();
      textButtonBox.setLayout(new BoxLayout(textButtonBox, BoxLayout.X_AXIS));
      textButtonBox.setOpaque(false);
    }//ParameterValueEditor()

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column){

      String type = ((ParameterDisjunction)table.getValueAt(row, 0)).getType();
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().get(type);

      if(rData != null){
        //Gate type
        comboUsed = true;
        combo.setModel(new DefaultComboBoxModel(
                                          rData.getInstantiations().toArray()));
        combo.setSelectedItem(value);
        return combo;
      }else{
        //non Gate type
        comboUsed = false;
        textField.setText((value == null) ? "" : value.toString());
        if(type.equals("java.net.URL")){
          textButtonBox.removeAll();
          textButtonBox.add(textField);
          textButtonBox.add(Box.createHorizontalStrut(5));
          textButtonBox.add(button);
          return textButtonBox;
        }else return textField;
      }
    }//getTableCellEditorComponent

    public Object getCellEditorValue(){
      if(comboUsed) return combo.getSelectedItem();
      else return ((textField.getText().equals("")) ? null :
                                                      textField.getText());
    }//public Object getCellEditorValue()


    public boolean stopCellEditing(){
      if(comboUsed) combo.hidePopup();
      return super.stopCellEditing();
    }

    JComboBox combo;
    JTextField textField;
    boolean comboUsed;
    JButton button;
    JPanel textButtonBox;
  }////class ParameterValueEditor



}//class NewResourceDialog