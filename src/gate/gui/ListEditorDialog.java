/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 16/10/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;


import gate.*;
import gate.util.*;
import gate.creole.*;

/**
 * A simple editor for List values.
 */
public class ListEditorDialog extends JDialog {

  /**
   * Contructs a new ListEditorDialog.
   * @param owner the component this dialog will be centred on.
   * @param data a list with the initial values. This list will not be changed,
   * its values will be cached and if the user selects the OK option a new list
   * with the updated contents will be returned.
   * @param itemType the type of the elements in the list in the form of a
   * fully qualified class name
   */
  public ListEditorDialog(Component owner, List data, String itemType) {
    this.itemType = itemType == null ? "java.lang.String" : itemType;
    setLocationRelativeTo(owner);
    initLocalData(data);
    initGuiComponents();
    initListeners();
  }

  protected void initLocalData(List data){
    listModel = new DefaultListModel();
    if(data != null){
      Iterator elemIter = data.iterator();
      while(elemIter.hasNext()){
        listModel.addElement(elemIter.next());
      }
    }

    try{
      itemTypeClass = Gate.getClassLoader().loadClass(itemType);
    }catch(ClassNotFoundException cnfe){
      throw new GateRuntimeException(cnfe.toString());
    }

    finiteType = Gate.isGateType(itemType);

    ResourceData rData = (ResourceData)Gate.getCreoleRegister().get(itemType);
    setTitle("List of " + ((rData== null) ? itemType :rData.getName()));

    addAction = new AddAction();
    removeAction = new RemoveAction();
  }

  protected void initGuiComponents(){
    getContentPane().setLayout(new BoxLayout(getContentPane(),
                                             BoxLayout.Y_AXIS));

    //the editor component
    JComponent editComp = null;
    if(finiteType){
      editComp = combo = new JComboBox(new ResourceComboModel());
      combo.setRenderer(new ResourceRenderer());
      if(combo.getModel().getSize() > 0){
        combo.getModel().setSelectedItem(combo.getModel().getElementAt(0));
      }
    }else{
      editComp = textField = new JTextField(20);
    }

    getContentPane().add(editComp);
    getContentPane().add(Box.createVerticalStrut(5));

    //the buttons box
    Box buttonsBox = Box.createHorizontalBox();
    addBtn = new JButton(addAction);
    removeBtn = new JButton(removeAction);
    buttonsBox.add(Box.createHorizontalGlue());
    buttonsBox.add(addBtn);
    buttonsBox.add(Box.createHorizontalStrut(5));
    buttonsBox.add(removeBtn);
    buttonsBox.add(Box.createHorizontalGlue());
    getContentPane().add(buttonsBox);
    getContentPane().add(Box.createVerticalStrut(5));

    //the list component
    Box horBox = Box.createHorizontalBox();
    listComponent = new JList(listModel);
    listComponent.setSelectionMode(ListSelectionModel.
                                   MULTIPLE_INTERVAL_SELECTION);
    listComponent.setCellRenderer(new ResourceRenderer());
    horBox.add(new JScrollPane(listComponent));
    //up down buttons
    Box verBox = Box.createVerticalBox();
    verBox.add(Box.createVerticalGlue());
    moveUpBtn = new JButton(MainFrame.getIcon("moveup.gif"));
    verBox.add(moveUpBtn);
    verBox.add(Box.createVerticalStrut(5));
    moveDownBtn = new JButton(MainFrame.getIcon("movedown.gif"));
    verBox.add(moveDownBtn);
    verBox.add(Box.createVerticalGlue());
    horBox.add(Box.createHorizontalStrut(3));
    horBox.add(verBox);
    horBox.add(Box.createHorizontalStrut(3));
    getContentPane().add(horBox);
    getContentPane().add(Box.createVerticalStrut(5));

    //the bottom buttons
    buttonsBox = Box.createHorizontalBox();
    buttonsBox.add(Box.createHorizontalGlue());
    okButton = new JButton("OK");
    buttonsBox.add(okButton);
    buttonsBox.add(Box.createHorizontalStrut(5));
    cancelButton = new JButton("Cancel");
    buttonsBox.add(cancelButton);
    buttonsBox.add(Box.createHorizontalGlue());
    getContentPane().add(buttonsBox);
  }

  protected void initListeners(){
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        userCancelled = false;
        hide();
      }
    });

    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        userCancelled = true;
        hide();
      }
    });


    moveUpBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rows[] = listComponent.getSelectedIndices();
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              ListEditorDialog.this,
              "Please select some items to be moved ",
              "Gate", JOptionPane.ERROR_MESSAGE);
        }else{
          //we need to make sure the rows are sorted
          Arrays.sort(rows);
          //get the list of items
          for(int i = 0; i < rows.length; i++){
            int row = rows[i];
            if(row > 0){
              //move it up
              Object value = listModel.remove(row);
              listModel.add(row - 1, value);
            }
          }
          //restore selection
          for(int i = 0; i < rows.length; i++){
            int newRow = -1;
            if(rows[i] > 0) newRow = rows[i] - 1;
            else newRow = rows[i];
            listComponent.addSelectionInterval(newRow, newRow);
          }
        }

      }//public void actionPerformed(ActionEvent e)
    });


    moveDownBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rows[] = listComponent.getSelectedIndices();
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              ListEditorDialog.this,
              "Please select some items to be moved ",
              "Gate", JOptionPane.ERROR_MESSAGE);
        } else {
          //we need to make sure the rows are sorted
          Arrays.sort(rows);
          //get the list of items
          for(int i = rows.length - 1; i >= 0; i--){
            int row = rows[i];
            if(row < listModel.size() -1){
              //move it down
              Object value = listModel.remove(row);
              listModel.add(row + 1, value);
            }
          }
          //restore selection
          for(int i = 0; i < rows.length; i++){
            int newRow = -1;
            if(rows[i] < listModel.size() - 1) newRow = rows[i] + 1;
            else newRow = rows[i];
            listComponent.addSelectionInterval(newRow, newRow);
          }
        }

      }//public void actionPerformed(ActionEvent e)
    });

  }

  /**
   * Make this dialog visible allowing the editing of the list.
   * If the user selects the <b>OK</b> option a new list with the updated
   * contents will be returned; it the <b>Cancel</b> option is selected this
   * method return <tt>null</tt>.
   */
  public List showDialog(){
    pack();
    userCancelled = true;
    setModal(true);
    super.show();
    return userCancelled ? null : Arrays.asList(listModel.toArray());
  }

  /**
   * test code
   */
  public static void main(String[] args){
    try{
      Gate.init();
    }catch(Exception e){
      e.printStackTrace();
    }
    JFrame frame = new JFrame("Foo frame");

    ListEditorDialog dialog = new ListEditorDialog(frame,
                                                   new ArrayList(),
                                                   "java.lang.Integer");

    frame.setSize(300, 300);
    frame.setVisible(true);
    System.out.println(dialog.showDialog());
  }

  /**
   * Adds an element to the list from the editing component located at the top
   * of this dialog.
   */
  protected class AddAction extends AbstractAction{
    AddAction(){
      super("Add");
      putValue(SHORT_DESCRIPTION, "Add the edited value to the list");
    }
    public void actionPerformed(ActionEvent e){
      if(finiteType){
        listModel.addElement(combo.getSelectedItem());
      }else{
        Object value = null;
        //convert the value to the proper type
        String stringValue = textField.getText();
        if(stringValue == null || stringValue.length() == 0) stringValue = null;

        if(itemTypeClass.isAssignableFrom(String.class)){
          //no conversion necessary
          value = stringValue;
        }else{
          //try conversion
          try{
            value = itemTypeClass.getConstructor(new Class[]{String.class}).
                                  newInstance( new Object[]{stringValue} );
          }catch(Exception ex){
            JOptionPane.showMessageDialog(
                ListEditorDialog.this,
                "Invalid value!\nIs it the right type?",
                "Gate", JOptionPane.ERROR_MESSAGE);
            return;
          }
        }
        listModel.addElement(value);
        textField.setText("");
      }
    }
  }

  /**
   * Removes the selected element(s) from the list
   */
  protected class RemoveAction extends AbstractAction{
    RemoveAction(){
      super("Remove");
      putValue(SHORT_DESCRIPTION, "Remove the selected value(s) from the list");
    }

    public void actionPerformed(ActionEvent e){
      int[] indices = listComponent.getSelectedIndices();
      Arrays.sort(indices);
      for(int i = indices.length -1; i >= 0; i--){
        listModel.remove(indices[i]);
      }
    }
  }


  /**
   * A model for a combobox containing the loaded corpora in the system
   */
  protected class ResourceComboModel extends AbstractListModel
                                  implements ComboBoxModel{

    public int getSize(){
      //get all corpora regardless of their actual type
      java.util.List loadedResources = null;
      try{
        loadedResources = Gate.getCreoleRegister().
                               getAllInstances(itemType);
      }catch(GateException ge){
        ge.printStackTrace(Err.getPrintWriter());
      }

      return loadedResources == null ? 0 : loadedResources.size();
    }

    public Object getElementAt(int index){
      //get all corpora regardless of their actual type
      java.util.List loadedResources = null;
      try{
        loadedResources = Gate.getCreoleRegister().
                               getAllInstances(itemType);
      }catch(GateException ge){
        ge.printStackTrace(Err.getPrintWriter());
      }
      return loadedResources == null? null : loadedResources.get(index);
    }

    public void setSelectedItem(Object anItem){
      if(anItem == null) selectedItem = null;
      else selectedItem = anItem;
    }

    public Object getSelectedItem(){
      return selectedItem;
    }

    void fireDataChanged(){
      fireContentsChanged(this, 0, getSize());
    }

    Object selectedItem = null;
  }

  /**
   * The type of the elements in the list
   */
  String itemType;

  /**
   * The Class for the elements in the list
   */
  Class itemTypeClass;

  /**
   * The GUI compoenent used to display the list
   */
  JList listComponent;

  /**
   * Comobox used to select among values for GATE types
   */
  JComboBox combo;

  /**
   * Text field used to input new arbitrary values
   */
  JTextField textField;

  /**
   * Used to remove the selected element in the list;
   */
  JButton removeBtn;

  /**
   * Used to add a new value to the list
   */
  JButton addBtn;

  /**
   * Moves up one or more items in the list
   */
  JButton moveUpBtn;

  /**
   * Moves down one or more items in the list
   */
  JButton moveDownBtn;

  /**
   * The model used by the {@link listComponent}
   */
  DefaultListModel listModel;

  /**
   * Does the item type have a finite range (i.e. should we use the combo)?
   */
  boolean finiteType;

  /**
   * An action that adds the item being edited to the list
   */
  Action addAction;

  /**
   * An action that removes the item(s) currently selected from the list
   */
  Action removeAction;

  /**
   * The OK button for this dialog
   */
  JButton okButton;

  /**
   * The cancel button for this dialog
   */
  JButton cancelButton;

  /**
   * Did the user press the cancel button?
   */
  boolean userCancelled;
}