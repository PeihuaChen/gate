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
import java.awt.Insets;
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
import gate.event.*;

/**
 * Allows the editing of a set of parameters for a resource. It needs a pointer
 * to the resource and a list of the parameter names for the parameters that
 * should be displayed. The list of the parameters is actually a list of lists
 * of strings representing parameter disjunctions.
 */
public class ResourceParametersEditor extends XJTable implements CreoleListener{

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
    Gate.getCreoleRegister().addCreoleListener(this);
    addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        if(e.getKeyCode() == e.VK_ENTER){
          if(getEditingColumn() == -1 && getEditingRow() == -1){
            getParent().dispatchEvent(e);
          }
        }
      }

      public void keyPressed(KeyEvent e) {
      }

      public void keyReleased(KeyEvent e) {
      }
    });
  }

  /**
   * Disable key handling for most keys by JTable when not editing.
   */
  protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                                      int condition, boolean pressed) {
    int keyCode = e.getKeyCode();
    if(isEditing() ||
       keyCode == KeyEvent.VK_UP ||
       keyCode == KeyEvent.VK_DOWN ||
       keyCode == KeyEvent.VK_LEFT ||
       keyCode == KeyEvent.VK_RIGHT ||
       keyCode == KeyEvent.VK_TAB) return super.processKeyBinding(ks, e,
                                                                  condition,
                                                                  pressed);
    return false;
  }

  /**
   * Should this GUI comonent allow editing?
   */

  /**
   * Sets the parameters for the resource to their new values as resulted
   * from the user's edits.
   */
  public void setParameters() throws ResourceInstantiationException{
    if(resource == null || parameterDisjunctions == null) return;
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

  public void resourceLoaded(CreoleEvent e) {
    repaint();
  }
  public void resourceUnloaded(CreoleEvent e) {
    repaint();
  }
  public void datastoreOpened(CreoleEvent e) {
  }
  public void datastoreCreated(CreoleEvent e) {
  }
  public void datastoreClosed(CreoleEvent e) {
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
      if(Gate.isGateType(type)){
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
      fileButton = new JButton(MainFrame.getIcon("loadFile.gif"));
      fileButton.setToolTipText("Set from file...");
      listButton = new JButton(MainFrame.getIcon("editList.gif"));
      listButton.setToolTipText("Edit the list");
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

      if(Gate.isGateType(type)){
        //Gate type
        combo.setModel(new DefaultComboBoxModel(new Object[]{value == null ?
                                                             "<none>" :
                                                             value }));

        return combo;
      }else{
        Class typeClass = null;
        try{
          typeClass = Class.forName(type);
        }catch(ClassNotFoundException cnfe){
        }
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
          textButtonBox.add(fileButton);
          return textButtonBox;
        }else if(typeClass != null &&
                 List.class.isAssignableFrom(typeClass)){
          //List value
          setText(textForList((List)value));
          textButtonBox.removeAll();
          textButtonBox.add(this);
          this.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                            getPreferredSize().height));
          textButtonBox.add(Box.createHorizontalStrut(5));
          textButtonBox.add(listButton);
          return textButtonBox;
        }else return this;
      }
    }// public Component getTableCellRendererComponent

    /**
     * Gets a string representation for a list value
     */
    protected String textForList(List list){
      if(list == null) return "[]";
      StringBuffer res = new StringBuffer("[");
      Iterator elemIter = list.iterator();
      while(elemIter.hasNext()){
        Object elem = elemIter.next();
        res.append( ((elem instanceof NameBearer) ?
                    ((NameBearer)elem).getName() :
                    elem.toString()) + ", ");
      }
      res.delete(res.length() - 2, res.length() - 1);
      res.append("]");
      return res.toString();
    }

    JButton fileButton;
    JButton listButton;
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
      combo.setEditable(false);

      textField = new JTextField();

      fileChooser = MainFrame.getFileChooser();
      fileButton = new JButton(MainFrame.getIcon("loadFile.gif"));
      fileButton.setToolTipText("Set from file...");
      fileButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
          fileChooser.setDialogTitle("Select a file");
          int res = fileChooser.showOpenDialog(ResourceParametersEditor.this);
          if(res == fileChooser.APPROVE_OPTION){
            try {
              textField.setText(fileChooser.getSelectedFile().
                                toURL().toExternalForm());
            } catch(IOException ioe){}
            fireEditingStopped();
          }else{
            fireEditingCanceled();
          }
        }
      });

      listButton = new JButton(MainFrame.getIcon("editList.gif"));
      listButton.setToolTipText("Edit the list");
      listButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          List returnedList = listEditor.showDialog();
          if(returnedList != null){
            listValue = returnedList;
            fireEditingStopped();
          }else{
            fireEditingCanceled();
          }
        }
      });

      textButtonBox = new JPanel();
      textButtonBox.setLayout(new BoxLayout(textButtonBox, BoxLayout.X_AXIS));
      textButtonBox.setOpaque(false);
      label = new JLabel(){
        public boolean isFocusTraversable(){
          return true;
        }
      };
      label.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      label.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          Boolean value = new Boolean(label.getText());
          value = new Boolean(!value.booleanValue());
          label.setText(value.toString());
        }
      });
      label.addKeyListener(new KeyAdapter() {
        public void keyTyped(KeyEvent e) {
          Boolean value = new Boolean(label.getText());
          value = new Boolean(!value.booleanValue());
          label.setText(value.toString());
        }
      });
    }//ParameterValueEditor()

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column){
      comboUsed = false;
      listUsed = false;
      ParameterDisjunction pDisj = (ParameterDisjunction)
                                   table.getValueAt(row, 0);
      type = pDisj.getType();
//      ResourceData rData = (ResourceData)Gate.getCreoleRegister().get(type);

      if(Gate.isGateType(type)){
        //Gate type
        comboUsed = true;
        ArrayList values = new ArrayList();
        try{
          values.addAll(Gate.getCreoleRegister().
                        getAllInstances(type));
        }catch(GateException ge){
          ge.printStackTrace(Err.getPrintWriter());
        }
        values.add(0, "<none>");
        combo.setModel(new DefaultComboBoxModel(values.toArray()));
        combo.setSelectedItem(value == null ? "<none>" : value);
        return combo;
      }else{
        //non Gate type
        Class typeClass = null;
        try{
          typeClass = Class.forName(type);
        }catch(ClassNotFoundException cnfe){
        }

        textField.setText((value == null) ? "" : value.toString());
        if(type.equals("java.net.URL")){
          //clean up all filters
          fileChooser.resetChoosableFileFilters();
          fileChooser.setAcceptAllFileFilterUsed(true);
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          Parameter param = pDisj.getParameter();
          Set sufixes = param.getSuffixes();
          if(sufixes != null){
            ExtensionFileFilter fileFilter = new ExtensionFileFilter();
            Iterator sufIter = sufixes.iterator();
            while(sufIter.hasNext()){
              fileFilter.addExtension((String)sufIter.next());
            }
            fileFilter.setDescription("Known file types " + sufixes.toString());
            fileChooser.addChoosableFileFilter(fileFilter);
            fileChooser.setFileFilter(fileFilter);

//fileChooser.doLayout();
          }

          textField.setEditable(true);
          textButtonBox.removeAll();
          textButtonBox.add(textField);
          textButtonBox.add(Box.createHorizontalStrut(5));
          textButtonBox.add(fileButton);
          return textButtonBox;
        }else if(type.equals("java.lang.Boolean")){
          label.setText(value.toString());
          return label;
        }else if(typeClass != null &&
                      List.class.isAssignableFrom(typeClass)){
          //List value
          listUsed = true;
          Parameter param = pDisj.getParameter();
          Set sufixes = param.getSuffixes();

          listValue = (List)value;
          listEditor = new ListEditorDialog(ResourceParametersEditor.this,
                                            (List)value,
                                            param.getItemClassName());

          textField.setEditable(false);
          textField.setText(textForList((List)value));
          textButtonBox.removeAll();
          textButtonBox.add(textField);
          textButtonBox.add(Box.createHorizontalStrut(5));
          textButtonBox.add(listButton);
          return textButtonBox;
        }else{
          textField.setEditable(true);
          return textField;
        }
      }
    }//getTableCellEditorComponent

    /**
     * Gets a string representation for a list value
     */
    protected String textForList(List list){
      if(list == null) return "[]";
      StringBuffer res = new StringBuffer("[");
      Iterator elemIter = list.iterator();
      while(elemIter.hasNext()){
        Object elem = elemIter.next();
        res.append( ((elem instanceof NameBearer) ?
                    ((NameBearer)elem).getName() :
                    elem.toString()) + ", ");
      }
      res.delete(res.length() - 2, res.length() - 1);
      res.append("]");
      return res.toString();
    }

    public Object getCellEditorValue(){
      if(comboUsed){
        Object value = combo.getSelectedItem();
         return value == "<none>" ? null : value;
      }
      else if(listUsed){
        return listValue;
      }else{
        if(type.equals("java.lang.Boolean")){
          //get the value from the label
          return new Boolean(label.getText());
        }else{
          //get the value from the text field
          return ((textField.getText().equals("")) ? null :
                                                     textField.getText());
        }
      }
    }//public Object getCellEditorValue()

    /**
     * The type of the value currently being edited
     */
    String type;

    /**
     * Combobox use as editor for Gate objects (chooses between instances)
     */
    JComboBox combo;

    /**
     * Editor used for boolean values
     */
    JLabel label;

    /**
     * Generic editor for all types that are not treated special
     */
    JTextField textField;

    /**
     * A pointer to the filechooser from MainFrame;
     */
    JFileChooser fileChooser;

    ListEditorDialog listEditor = null;
    List listValue;

    boolean comboUsed;
    boolean listUsed;
    JButton fileButton;
    JButton listButton;
    JPanel textButtonBox;
  }//class ParameterValueEditor



}//class NewResourceDialog