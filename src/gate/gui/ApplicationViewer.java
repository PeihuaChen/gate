package gate.gui;

import gate.creole.*;
import gate.*;
import gate.util.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.Component;
import java.awt.Dimension;

import java.util.*;


public class ApplicationViewer extends AbstractVisualResource {

  public ApplicationViewer(Controller controller, ProjectData project) {
    if(controller instanceof SerialController){
      this.controller = (SerialController)controller;
      this.project = project;
      initLocalData();
      initGuiComponents();
      initListeners();
    }else{
      throw new UnsupportedOperationException(
        "Editing of controllers implemented only for serial controllers!");
    }
  }

  protected void initLocalData(){
  }

  protected void initGuiComponents(){
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    tableModel = new PRListTableModel();
    table = new XJTable(tableModel);
    table.setDefaultRenderer(ProcessingResource.class, new PRRenderer());
    table.setDefaultEditor(ProcessingResource.class, new PREditor());
    table.setIntercellSpacing(new Dimension(10, 10));
    table.setSortable(false);
    JScrollPane scroll = new JScrollPane(table);
    this.add(scroll);
  }

  protected void initListeners(){
  }


  XJTable table;
  PRListTableModel tableModel;

  SerialController controller;
  ProjectData project;


  class PRListTableModel extends AbstractTableModel{
    public int getRowCount(){
      return controller.size() + 1;
    }

    public int getColumnCount(){
      return 2;
    }

    public String getColumnName(int columnIndex){
      switch (columnIndex){
        case 0: return "Processing resource";
        case 1: return "Type";
        default: return "?";
      }
    }

    public Class getColumnClass(int columnIndex){
      switch (columnIndex){
        case 0: return ProcessingResource.class;
        case 1: return String.class;
        default: return Object.class;
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return columnIndex == 0;// && rowIndex == controller.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex){
      if(rowIndex >= controller.size()){
        switch (columnIndex){
          case 0: return null;
          case 1: return "- no type -";
          default: return Object.class;
        }
      }
      switch (columnIndex){
        case 0: return controller.get(rowIndex);
        case 1: return ((ProcessingResource)controller.get(rowIndex)).getClass().toString();
        default: return Object.class;
      }
    }

    public void setValueAt(Object aValue,
                       int rowIndex,
                       int columnIndex){
      if(columnIndex == 0){
        if(rowIndex >= controller.size()){
          if(aValue != null) controller.add(aValue);
        }else{
          if(aValue != null) controller.set(rowIndex, aValue);
          else controller.remove(rowIndex);
        }
      }
    }

  }//class PRListTableModel extends AbstractTableModel

  class PRRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column){

      String name = null;
      if(value != null){
        ProcessingResource res = (ProcessingResource) value;
        name = (String)res.getFeatures().get("NAME");
        if(name == null){
          name = "No name: " + res.getClass().toString();
        }
      }else{
        name = "< Add new... >";
      }
      return super.getTableCellRendererComponent(table, name, isSelected,
                                                 hasFocus, 0, 0);
    }
  }

  class PREditor extends DefaultCellEditor{
    public PREditor(){
      super(new JComboBox());
      combo = (JComboBox)getComponent();
      prsByName = new TreeMap();
    }

    public Component getTableCellEditorComponent(JTable table,
                                             Object value,
                                             boolean isSelected,
                                             int row,
                                             int column){

      prsByName.clear();
      Iterator prsIter = project.getPRList().iterator();
      while(prsIter.hasNext()){
        PRHandle handle = (PRHandle)prsIter.next();
        ProcessingResource pr = (ProcessingResource)handle.resource;
        String prName = (String)handle.resource.getFeatures().get("NAME");
        if(prName == null){
          prName = "No name: " + pr.getClass().toString();
        }
        prsByName.put(prName, pr);
      }//while(prsIter.hasNext())
      if(prsByName.isEmpty()) return null;
      prsByName.put("< Delete! >", null);
      combo.setModel(new DefaultComboBoxModel(prsByName.keySet().toArray()));
      return super.getTableCellEditorComponent(table, value, isSelected,
                                               row, column);
    }

    public Object getCellEditorValue(){
      ProcessingResource res = (ProcessingResource)prsByName.get(combo.getSelectedItem());
/*
      controller.add(res);
      tableModel.fireTableDataChanged();
*/
      return res;
    }

    JComboBox combo;
    Map prsByName;
  }//class PREditor extends DefaultCellEditor
}