package gate.gui;

import gate.creole.*;
import gate.*;

import javax.swing.*;
import javax.swing.table.*;

import java.util.*;


public class ApplicationViewer extends AbstractVisualResource {

  public ApplicationViewer(Controller controller) {
    if(controller instanceof SerialController){
      this.controller = (SerialController)controller;
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
    table = new JTable(tableModel);
    JScrollPane scroll = new JScrollPane(table);
    this.add(scroll);
  }

  protected void initListeners(){
  }


  JTable table;
  TableModel tableModel;
  SerialController controller;

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
        case 0: return String.class;
        case 1: return String.class;
        default: return Object.class;
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex){
      if(rowIndex >= controller.size()) return null;
      switch (columnIndex){
        case 0: return controller.get(rowIndex).toString();
        case 1: return ((ProcessingResource)controller.get(rowIndex)).getClass().toString();
        default: return Object.class;
      }
    }

    public void setValueAt(Object aValue,
                       int rowIndex,
                       int columnIndex){
    }
  }
}