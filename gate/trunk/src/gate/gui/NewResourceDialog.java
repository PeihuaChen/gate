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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.creole.*;

public class NewResourceDialog extends JDialog {
  public NewResourceDialog(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  protected void initLocalData(){
    params = new ArrayList();
  }

  protected void initGuiComponents(){
    this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),
                                                  BoxLayout.Y_AXIS));
    //name field
    Box nameBox = Box.createHorizontalBox();
    nameBox.add(Box.createHorizontalStrut(5));
    nameBox.add(new JLabel("Name: "));
    nameBox.add(Box.createHorizontalStrut(5));
    nameField = new JTextField(30);
    nameField.setMaximumSize(nameField.getPreferredSize());
    nameBox.add(nameField);
    nameBox.add(Box.createHorizontalStrut(5));
    nameBox.add(Box.createHorizontalGlue());
    this.getContentPane().add(nameBox);
    this.getContentPane().add(Box.createVerticalStrut(5));

    //parameters table
    tableModel = new ParametersTableModel();
    table = new XJTable(tableModel);
    table.setDefaultRenderer(ParameterDisjunction.class,
                             new ParameterDisjunctionRenderer());
    table.setDefaultEditor(ParameterDisjunction.class,
                           new ParameterDisjunctionEditor());
    table.setDefaultRenderer(Boolean.class,
                             new BooleanRenderer());
    table.setIntercellSpacing(new Dimension(10, 10));
    table.setAutoResizeMode(table.AUTO_RESIZE_LAST_COLUMN);
    JScrollPane scroll = new JScrollPane(table);
    this.getContentPane().add(scroll);
    this.getContentPane().add(Box.createVerticalStrut(5));
    this.getContentPane().add(Box.createVerticalGlue());

    //buttons box
    JPanel buttonsBox = new JPanel();
    buttonsBox.setLayout(new BoxLayout(buttonsBox, BoxLayout.X_AXIS));
    //buttonsBox.setAlignmentX(Component.CENTER_ALIGNMENT);
    buttonsBox.add(Box.createHorizontalStrut(10));
    buttonsBox.add(okBtn = new JButton("OK"));
    buttonsBox.add(Box.createHorizontalStrut(10));
    buttonsBox.add(cancelBtn = new JButton("Cancel"));
    buttonsBox.add(Box.createHorizontalStrut(10));
    this.getContentPane().add(buttonsBox);
    this.getContentPane().add(Box.createVerticalStrut(5));
    setSize(400, 300);
  }


  protected void initListeners(){
    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        userCanceled = false;
        String name = nameField.getText();
        if(name == null || name.length() == 0){
          JOptionPane.showMessageDialog(getOwner(),
                                        "Please give a name for the new resource!\n",
                                        "Gate", JOptionPane.ERROR_MESSAGE);
        }else{
          hide();
        }
      }
    });

    cancelBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        userCanceled = true;
        hide();
      }
    });
  }

  ParametersTableModel tableModel;
  XJTable table;
  JComboBox parametersCombo;
  JButton okBtn, cancelBtn;
  JTextField nameField;


  boolean userCanceled;
  ArrayList params;

  public Resource show(ResourceData rData){
    setLocationRelativeTo(getParent());
    /*
    setLocation( (getParent().getWidth() - getWidth())/2,
                 (getParent().getHeight() - getHeight())/2);
*/
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

  int getRowCnt(){
    return params.size();
  }

  //inner classes
  protected class ParametersTableModel extends AbstractTableModel{

    public ParametersTableModel(){
    }

    public void fireTableDataChanged(){
      super.fireTableDataChanged();
    }

    public int getColumnCount(){return 4;}

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0: return ParameterDisjunction.class;
        case 1: return String.class;
        case 2: return Boolean.class;
        case 3: return String.class;
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
      return getRowCnt();
      /*
      if(params == null){
        System.out.println("Null indeed!");
        return 0;
      }else{
        System.out.println("Like hell null!");
        return params.size();
      }
      */
    }

    public Object getValueAt(int rowIndex,
                         int columnIndex){
      ParameterDisjunction pDisj =
                    (ParameterDisjunction)params.get(rowIndex);
      switch(columnIndex){
        case 0: return pDisj;
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
          pDisj.setValue((String)aValue);
          break;
        }
        default:{}
      }
    }
  }///class FeaturesTableModel extends DefaultTableModel

  class BooleanRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column){
      Component comp = super.getTableCellRendererComponent(table,
                                                           "",
                                                           isSelected, hasFocus,
                                                           row, column);
      if(comp instanceof JLabel){
        try{
          JLabel label = (JLabel)comp;
          if(((Boolean)value).booleanValue()){
            label.setIcon(new ImageIcon(getClass().
                          getResource("/gate/resources/img/tick.gif")));
          }else{
            label.setIcon(null);
          }
        }catch(Exception e){}
      }
      return comp;
    }
  }


  class ParameterDisjunctionRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column){
      ParameterDisjunction pDisj = (ParameterDisjunction)value;
      Component comp = super.getTableCellRendererComponent(table,
                                                           pDisj.getName(),
                                                           isSelected, hasFocus,
                                                           row, column);
      if(comp instanceof JLabel){
        try{
          JLabel label = (JLabel)comp;
          label.setToolTipText(pDisj.getComment());
          label.setHorizontalTextPosition(JLabel.LEFT);
          if(pDisj.size() > 1){
            label.setIcon(new ImageIcon(getClass().
                          getResource("/gate/resources/img/down.gif")));

          }else{
            label.setIcon(null);
          }
        }catch(Exception e){}
      }
      return comp;
    }
  }

  class ParameterDisjunctionEditor extends DefaultCellEditor{
    public ParameterDisjunctionEditor(){
      super(new JComboBox());
      combo = (JComboBox)super.getComponent();
    }

    public Component getTableCellEditorComponent(JTable table,
                                             Object value,
                                             boolean isSelected,
                                             int row,
                                             int column){
     ParameterDisjunction pDisj = (ParameterDisjunction)value;

     combo.setModel(new DefaultComboBoxModel(pDisj.getNames()));
     return combo;
    }
    public Object getCellEditorValue(){
      return new Integer(combo.getSelectedIndex());
    }
    JComboBox combo;
  }

  class ParameterDisjunction{
    /**
     * gets a list of {@link gate.creole.Parameter}
     */
    public ParameterDisjunction(List options){
      this.options = options;
      Iterator paramsIter = options.iterator();
      names = new String[options.size()];
      int i = 0;
      while(paramsIter.hasNext()){
        names[i++] = ((Parameter)paramsIter.next()).getComment();
      }
      values = new Object[options.size()];
      setSelectedIndex(0);
    }

    public void setSelectedIndex(int index){
      selectedIndex = index;
      currentParameter = (Parameter)options.get(selectedIndex);
      if(values[selectedIndex] == null){
        try{
          values[selectedIndex] = currentParameter.getDefaultValue();
        }catch(Exception e){
          values[selectedIndex] = "";
        }
      }
      tableModel.fireTableDataChanged();
    }

    public int size(){
      return options.size();
    }

    public Boolean getRequired(){
      return new Boolean(!currentParameter.isOptional());
    }

    public String getName(){
      return currentParameter.getName();
    }

    public String getComment(){
      return currentParameter.getComment();
    }

    public String getType(){
      return currentParameter.getTypeName();
    }

    public String[] getNames(){
      return names;
    }

    public void setValue(String stringValue){
      Object oldValue = values[selectedIndex];
      try{
        values[selectedIndex] = currentParameter.
                                calculateValueFromString(stringValue);
      }catch(Exception e){
        values[selectedIndex] = oldValue;
        JOptionPane.showMessageDialog(getContentPane(),
                                      "Invalid value!\n" +
                                      "Is it the right type?",
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
    public Object getValue(){
      return values[selectedIndex];
    }


    int selectedIndex;
    List options;
    boolean required;
    String typeName;
    String name;
    String[] names;
    Parameter currentParameter;
    Object[] values;
  }
}