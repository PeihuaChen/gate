/*  SchemaAnnotationEditor.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  12/July/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.util.*;
import java.lang.reflect.*;

import gate.*;
import gate.annotation.*;
import gate.util.*;
import gate.creole.*;

/** This class is a viewer which adds/edits features on a GATE annotation.
  * This viewer is {@link gate.creole.AnnotationSchema} driven.
  */
public class SchemaAnnotationEditor extends AbstractVisualResource
                                    implements AnnotationVisualResource{

  /** Default constructor */
  public SchemaAnnotationEditor(){}

  // Methods required by AnnotationVisualResource

  /**
    * Called by the GUI when this viewer/editor has to initialise itself for a
    * specific annotation or text span.
    * @param target the object which will always be a {@link gate.AnnotationSet}
    */
  public void setTarget(Object target){
    currentAnnotSet = (AnnotationSet) target;
  }// setTarget();

  /**
    * Used when the viewer/editor has to display/edit an existing annotation
    * @param ann the annotation to be displayed or edited. If ann is null then
    * the method simply returns
    */
  public void setAnnotation(Annotation ann){
    // If ann is null, then simply return.
    if (ann == null) return;

    currentAnnot = ann;
    currentStartOffset = currentAnnot.getStartNode().getOffset();
    currentEndOffset = currentAnnot.getEndNode().getOffset();
    currentAnnotFeaturesMap = Factory.newFeatureMap();
    currentAnnotFeaturesMap.putAll(currentAnnot.getFeatures());
    currentAnnotSchema = null;
    CreoleRegister creoleReg = Gate.getCreoleRegister();
    List currentAnnotationSchemaList =
                      creoleReg.getLrInstances("gate.creole.AnnotationSchema");
    // If there is no Annotation schema loaded the editor can only do nothing
    if (currentAnnotationSchemaList.isEmpty()) return;
    name2annotSchemaMap = new HashMap();
    Iterator annotSchemaIter = currentAnnotationSchemaList.iterator();
    // currentAnnotationSchemaList is not empty
    currentAnnotSchema = (AnnotationSchema) currentAnnotationSchemaList.get(0);
    while (annotSchemaIter.hasNext()){
      AnnotationSchema annotSch = (AnnotationSchema)annotSchemaIter.next();
      name2annotSchemaMap.put(annotSch.getAnnotationName(),annotSch);
      if (currentAnnot.getType().equals(annotSch.getAnnotationName()))
        currentAnnotSchema = annotSch;
    }// End while

    initLocalData();
    buildGuiComponents();
    initListeners();
  }// setAnnotation();

  /**
    * Used when the viewer has to create new annotations.
    * @param startOffset the start offset of the span covered by the new
    * annotation(s). If is <b>null</b> the method will simply return.
    * @param endOffset the end offset of the span covered by the new
    * annotation(s). If is <b>null</b> the method will simply return.
    */
  public void setSpan(Long startOffset, Long endOffset){
    // If one of them is null, then simply return.
    if (startOffset == null || endOffset == null) return;
    currentStartOffset = startOffset;
    currentEndOffset = endOffset;
    currentAnnot = null;
    currentAnnotFeaturesMap = null;
    currentAnnotSchema = null;
    CreoleRegister creoleReg = Gate.getCreoleRegister();
    List currentAnnotationSchemaList =
                      creoleReg.getLrInstances("gate.creole.AnnotationSchema");
    // If there is no Annotation schema loaded the editor can only do nothing
    if (currentAnnotationSchemaList.isEmpty()) return;
    name2annotSchemaMap = new HashMap();
    Iterator annotSchemaIter = currentAnnotationSchemaList.iterator();
    // currentAnnotationSchemaList is not empty
    currentAnnotSchema = (AnnotationSchema) currentAnnotationSchemaList.get(0);
    while (annotSchemaIter.hasNext()){
      AnnotationSchema annotSch = (AnnotationSchema)annotSchemaIter.next();
      name2annotSchemaMap.put(annotSch.getAnnotationName(),annotSch);
    }// End while

    initLocalData();
    buildGuiComponents();
    initListeners();
  }// setSpan();

  /**
   * Called by the GUI when the user has pressed the "OK" button. This should
   * trigger the saving of the newly created annotation(s)
   */
  public void okAction() throws GateException{
    // Construct the response featutre
    Iterator iter = tableModel.data.iterator();
    while (iter.hasNext()){
      RowData rd = (RowData) iter.next();
      responseMap.put(rd.getFeatureSchema().getFeatureName(), rd.getValue());
    }// End while
    if (currentAnnot == null){
      currentAnnotSet.add( currentStartOffset,
                           currentEndOffset,
                           currentAnnotSchema.getAnnotationName(),
                           responseMap);
    }else{
      if (currentAnnot.getType().equals(currentAnnotSchema.getAnnotationName())){
        currentAnnot.setFeatures(responseMap);
      }else{
        currentAnnotSet.remove(currentAnnot);
        currentAnnotSet.add( currentStartOffset,
                             currentEndOffset,
                             currentAnnotSchema.getAnnotationName(),
                             responseMap);
      }// End if
    }// End if
  }//okAction();

  /**
    * Checks whether this viewer/editor can handle a specific annotation type.
    * @param annotationType represents the annotation type being questioned.If
    * it is <b>null</b> then the method will return false.
    * @return true if the SchemaAnnotationEditor can handle the annotationType
    * or false otherwise.
    */
  public boolean canDisplayAnnotationType(String annotationType){
    // Returns true only if the there is an AnnotationSchema with the same type
    // as annotationType.
    if (annotationType == null) return false;
    CreoleRegister creoleReg = Gate.getCreoleRegister();
    List currentAnnotationSchemaList =
                      creoleReg.getLrInstances("gate.creole.AnnotationSchema");
    if (currentAnnotationSchemaList.isEmpty()) return false;
    Iterator iter = currentAnnotationSchemaList.iterator();
    while (iter.hasNext()){
      AnnotationSchema annotSchema = (AnnotationSchema) iter.next();
      if (annotationType.equals(annotSchema.getAnnotationName())) return true;
    }// End while
    return false;
  }// canDisplayAnnotationType();

  // The Schema Editor functionality

  // Local data

  /** The annotation schema present into the system*/
  List currentAnnotationSchemaList = null;
  /** The curent annotation set used by the editor*/
  AnnotationSet currentAnnotSet = null;
  /** The curent annotation used by the editor*/
  Annotation currentAnnot = null;
  /** The start offset of the span covered by the currentAnnot*/
  Long currentStartOffset = null;
  /** The end offset of the span covered by the currentAnnot*/
  Long currentEndOffset = null;
  /** This is the currentAnnotSchema being used by the editor*/
  AnnotationSchema currentAnnotSchema = null;
  /** The current FeatureMap used by the editor*/
  FeatureMap currentAnnotFeaturesMap = null;
  /** This field is returned when a featureMap was editted or created*/
  FeatureMap responseMap = null;
  /** This field is the table model used to represent features*/
  FeaturesTableModel tableModel = null;
  /** A map from feature name to its FeatureSchema definition*/
  Map name2featureSchemaMap = null;
  /** A map from annotation type to its AnnotationSchema definition*/
  Map name2annotSchemaMap = null;
  /** A list model used to represent the features not assigned to an annot*/
  DefaultListModel listModel = null;

  // Gui Components
  JTable  featuresTable = null;
  JScrollPane featuresTableScroll = null;
  JScrollPane featuresListScroll = null;
  JButton removeFeatButton = null;
  JButton addFeatButton = null;
  JList   featureSchemaList = null;
  JComboBox annotSchemaComboBox = null;
  FeaturesEditor featuresEditor;

  /** Init local data*/
  protected void initLocalData(){
    // Create the response feature Map
    responseMap = Factory.newFeatureMap();

    if (currentAnnotFeaturesMap == null)
      currentAnnotFeaturesMap = Factory.newFeatureMap();

    name2featureSchemaMap = new HashMap();
    // Construct a set of feature names from feature schema
    Map fSNames2FSMap = new HashMap();

    listModel = new DefaultListModel();
    // Init name2featureSchemaMap
    // If the feature map provided was null, then we are in the creation mode
    Set featuresSch = currentAnnotSchema.getFeatureSchemaSet();
    if (featuresSch != null){
      Iterator iter = featuresSch.iterator();
      while (iter.hasNext()){
        FeatureSchema fs = (FeatureSchema) iter.next();
        // If the currentAnnotFeaturesMap doesn't contain the feature
        // from FeatureSchema then
        // add the featureSchema to the list
        if (fs != null){
          fSNames2FSMap.put(fs.getFeatureName(),fs);
          if( !currentAnnotFeaturesMap.containsKey(fs.getFeatureName())){
              name2featureSchemaMap.put(fs.getFeatureName(),fs);
              listModel.addElement(fs.getFeatureName());
          }// end if
        }// end if
      }// end while
      featureSchemaList.setVisibleRowCount(featuresSch.size());
    }// end if

    // Init the table model
    Set tableData = new HashSet();
    Iterator iterator = currentAnnotFeaturesMap.keySet().iterator();
    while (iterator.hasNext()){
      String key = (String) iterator.next();
      // If in currentAnnotFeaturesMap there is a key contained into
      // fSNames2FSMap then
      // add this feature to the table model together with its corresponding
      // FeatureSchema
      if (fSNames2FSMap.keySet().contains(key)){
        // Add it to the table model
        Object value = currentAnnotFeaturesMap.get(key);
        tableData.add(new RowData(value,(FeatureSchema)fSNames2FSMap.get(key)));
      } else
        // Add it to the responseFeatureMap
        // It might be a feature detected by the nameMatcher module, etc.
        // Those features must be preserved.
        responseMap.put(key,currentAnnotFeaturesMap.get(key));
    }// end while
    tableModel = new FeaturesTableModel(tableData);
  }// initLocalData();

  /** This method creates the GUI components and paces them into the layout*/
  protected void buildGuiComponents(){
    this.setLayout(new BoxLayout( this,BoxLayout.Y_AXIS));
    // Create the annotationSchema JComboBox box
    Box annotSchBox = Box.createVerticalBox();
    annotSchBox.add(Box.createVerticalStrut(5));
    annotSchemaComboBox = new JComboBox(name2annotSchemaMap.keySet().toArray());
    annotSchemaComboBox.setSelectedItem(currentAnnotSchema.getAnnotationName());
    annotSchBox.add(new JLabel("Select annotation schema"));
    annotSchBox.add(annotSchemaComboBox);

    //create the main box
    Box componentsBox = Box.createHorizontalBox();
    componentsBox.add(Box.createHorizontalStrut(5));
    // add the feature table
    featuresTable = new JTable();
    featuresTable.setSelectionMode(
                  ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    featuresTable.setModel(new FeaturesTableModel(new HashSet()));
    featuresTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    featuresEditor = new FeaturesEditor();
    featuresTable.setDefaultEditor(java.lang.Object.class, featuresEditor);
    featuresTableScroll = new JScrollPane(featuresTable);

    Box box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(new JLabel("Current features"));
    box.add(Box.createVerticalStrut(10));
    box.add(featuresTableScroll);
    box.add(Box.createVerticalStrut(5));

    componentsBox.add(box);
    componentsBox.add(Box.createHorizontalStrut(10));

    // add the remove put buttons
    Box buttBox = Box.createVerticalBox();
    removeFeatButton = new JButton(">>");
    addFeatButton = new JButton("<<");

    buttBox.add(addFeatButton);
    buttBox.add(Box.createVerticalStrut(10));
    buttBox.add(removeFeatButton);

    componentsBox.add(buttBox);

    componentsBox.add(Box.createHorizontalStrut(10));

    // add the Feature Schema list
    featureSchemaList = new JList();
    featureSchemaList.setSelectionMode(
                  ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    featuresListScroll = new JScrollPane(featureSchemaList);

    box = Box.createVerticalBox();
    box.add(Box.createVerticalStrut(5));
    box.add(new JLabel("Possible features"));
    box.add(Box.createVerticalStrut(10));
    box.add(featuresListScroll);
    box.add(Box.createVerticalStrut(5));

    componentsBox.add(box);
    componentsBox.add(Box.createHorizontalStrut(5));

    this.add(componentsBox);
    this.add(Box.createVerticalStrut(5));
    this.add(annotSchBox);
  }//buildGuiComponents();

  /** Init GUI components with values taken from local data*/
  protected void initGuiComponents(){
    featuresTable.setModel(tableModel);
    featureSchemaList.setModel(listModel);
  }//initGuiComponents()

  /** Init all the listeners*/
  protected void initListeners(){

    annotSchemaComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        currentAnnotSchema = (AnnotationSchema) name2annotSchemaMap.get(
                                 (String)annotSchemaComboBox.getSelectedItem());
        initLocalData();
        initGuiComponents();
      }// actionPerformed();
    });//addActionListener();

    // -> button
    removeFeatButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doRemoveFeatures();
      }//actionPerformed();
    });// addActionListener();

    // <- button
    addFeatButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doAddFeatures();
      }// actionPerformed();
    });// addActionListener();
  }//initListeners()

  /** This method remove a feature from the table and adds it to the list*/
  private void doRemoveFeatures(){
    int[] selectedRows = featuresTable.getSelectedRows();

    if (selectedRows.length <= 0) return;

    for (int i = (selectedRows.length - 1); i > -1 ; i--)
      doRemoveFeature(selectedRows[i]);

    tableModel.fireTableDataChanged();
  }// doRemoveFeatures();

  /** This removes the feature @ rowIndex*/
  private void doRemoveFeature(int rowIndex){
    RowData rd =  (RowData) tableModel.data.get(rowIndex);

    name2featureSchemaMap.put(rd.getFeatureSchema().getFeatureName(),
                                                      rd.getFeatureSchema());

    listModel.addElement(rd.getFeatureSchema().getFeatureName());
    tableModel.data.remove(rowIndex);
  }// doRemoveFeature();

  /** This method adds features from the list to the table*/
  private void doAddFeatures(){
    Object[] selectedFeaturesName = featureSchemaList.getSelectedValues();
    for (int i = 0 ; i < selectedFeaturesName.length; i ++){
      doAddFeature((String) selectedFeaturesName[i]);
    }// end for
    tableModel.fireTableDataChanged();
  }//doAddFeatures();

  /** This method adds a feature from the list to the table*/
  private void doAddFeature(String aFeatureName){
      FeatureSchema fs=(FeatureSchema) name2featureSchemaMap.get(aFeatureName);

      // Remove the feature schema from the list
      name2featureSchemaMap.remove(aFeatureName);
      listModel.removeElement(aFeatureName);

      Object value = null;
      if (fs.isDefault() || fs.isFixed())
        value = fs.getFeatureValue();
      if (value == null && fs.isEnumeration()){
        Iterator iter = fs.getPermissibleValues().iterator();
        if (iter.hasNext()) value = iter.next();
      }
      tableModel.data.add(new RowData(value,fs));
  }// doAddFeature();

  // Inner classes

  // TABLE MODEL
  protected class FeaturesTableModel extends AbstractTableModel{

    ArrayList data = null;

    public FeaturesTableModel(Set aData){
      data = new ArrayList(aData);
    }// FeaturesTableModel

    public void fireTableDataChanged(){
      super.fireTableDataChanged();
    }// fireTableDataChanged();

    public int getColumnCount(){return 3;}

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case 0: return String.class;
        case 1: return Object.class;
        case 2: return String.class;
        default: return Object.class;
      }
    }//getColumnClass()

    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0: return "Name";
        case 1: return "Value";
        case 2: return "Type";
        default: return "?";
      }
    }//public String getColumnName(int columnIndex)

    public boolean isCellEditable( int rowIndex,
                                   int columnIndex){


        if(columnIndex == 1){
          RowData rd = (RowData) data.get(rowIndex);
          FeatureSchema fs = rd.getFeatureSchema();
          if (fs.isFixed() || fs.isProhibited()) return false;
          else return true;
        }// end if
        if(columnIndex == 0 || columnIndex == 2) return false;
        return false;
    }//isCellEditable

    public int getRowCount(){
      return data.size();
    }//getRowCount()

    /** Returns the value at row,column from Table Model */
    public Object getValueAt( int rowIndex,
                              int columnIndex){

      RowData rd = (RowData) data.get(rowIndex);

      switch(columnIndex){
        case 0: return rd.getFeatureSchema().getFeatureName();
        case 1: return (rd.getValue() == null)? new String(""): rd.getValue();
        case 2: {
                  // Show only the last substring. For example, for
                  // java.lang.Integer -> Integer
                  String type = rd.getFeatureSchema().getValueClassName();
                  if(type == null)
                      return new String("");
                  else{
                    int start = type.lastIndexOf(".");
                    if ((start > -1) && (start < type.length()))
                      return type.substring(start+1,type.length());
                    else return type;
                  }// End if
                }

        default: return "?";
      }// End Switch
    }//getValueAt

    /** Set the value from the Cell Editor into the table model*/
    public void setValueAt( Object aValue,
                            int rowIndex,
                            int columnIndex){

      if (data == null || data.isEmpty()) return;
      RowData rd = (RowData) data.get(rowIndex);
      switch(columnIndex){
        case 0:{break;}
        case 1:{
          // Try to perform type conversion
          String className = null;
          String aValueClassName = null;
          // Need to create an object belonging to class "className" based on
          // the string object "aValue"
          if (aValue == null){
            rd.setValue("?");
            return;
          }// End if
          // Get the class name the final object must belong
          className = rd.getFeatureSchema().getValueClassName();
          // Get the class name that aValue object belongs to.
          aValueClassName = aValue.getClass().toString();
          // If there is no class to convert to, let the aValue object as it is
          // and return.
          if (className == null){
              rd.setValue(aValue);
              return;
          }// End if

          // If both classes are the same, then return. There is nothing to
          // convert to
          if (className.equals(aValueClassName)){
            rd.setValue(aValue);
            return;
          }// End if
          // If the class "aValue" object belongs to is not String then return.
          // This method tries to convert a string to various other types.
          if (!"class java.lang.String".equals(aValueClassName)){
            rd.setValue(aValue);
            return;
          }// End if

          // The aValue object belonging to java.lang.String needs to be
          // converted into onother object belonging to "className"
          Class  classObj = null;
          try{
            // Create a class object from className
            classObj = Class.forName(className);
          }catch (ClassNotFoundException cnfex){
            rd.setValue(aValue);
            return;
          }// End catch
          // Get its list of constructors
          Constructor[] constrArray = classObj.getConstructors();
          if (constrArray == null){
            rd.setValue(aValue);
            return;
          }// End if

          // Search for the constructo which takes only one String parameter
          boolean found = false;
          Constructor constructor = null;
          for (int i=0; i<constrArray.length; i++){
            constructor = constrArray[i];
            if ( constructor.getParameterTypes().length == 1 &&
                 "class java.lang.String".equals(
                                constructor.getParameterTypes()[0].toString())
               ){
                  found = true;
                  break;
            }// End if
          }// End for

          if (!found){
            rd.setValue(aValue);
            return;
          }// End if
          try{
            // Try to create an object with this constructor
            Object[] paramsArray = new Object[1];
            paramsArray[0] = aValue;
            Object newValueObject = constructor.newInstance(paramsArray);

            rd.setValue(newValueObject);

          } catch (Exception e){
            rd.setValue("");
          }// End catch

//          rd.setValue(aValue);
          break;
        }// End case
        case 2:{break;}
        case 3:{break;}
        default:{}
      }// End switch
    }// setValueAt();

  }///class FeaturesTableModel extends DefaultTableModel

  /** Internal class used in the inner FeaturesTableModel class*/
  class RowData {
    private Object value = null;
    private FeatureSchema featSchema = null;

    /** Constructor*/
    RowData(Object aValue, FeatureSchema aFeatureSchema){
      value = aValue;
      featSchema = aFeatureSchema;
    }//RowData

    public void setValue(Object aValue){
      value = aValue;
    }// setValue();

    public Object getValue(){
      return value;
    }//getValue()

    public void setFeatureSchema(FeatureSchema aFeatureSchema){
      featSchema = aFeatureSchema;
    }// setFeatureSchema();

    public FeatureSchema getFeatureSchema(){
      return featSchema;
    }//getFeatureSchema()

  }// RowData

  // The EDITOR RENDERER
  /** This inner class deals with the feature type being eddited. What it does
    * is to decide what GUI component to use (JComboBox, JTextField or JLabel)
    */
  class FeaturesEditor extends AbstractCellEditor  implements TableCellEditor{
    // Fields
    JComboBox cb = null;
    JTextField tf = null;
    /** Constructor*/
    public FeaturesEditor(){}
    /** The method overridden in order to implement behaviour*/
    public Component getTableCellEditorComponent( JTable table,
                                                  Object value,
                                                  boolean isSelected,
                                                  int row,
                                                  int column){
       RowData rd = (RowData) tableModel.data.get(row);
       if (rd.getFeatureSchema().isEnumeration()){
          cb = new JComboBox(rd.getFeatureSchema().
                                            getPermissibleValues().toArray());
          cb.setSelectedItem(value);
          tf = null;
          return cb;
       }// End if
       if ( rd.getFeatureSchema().isDefault() ||
            rd.getFeatureSchema().isOptional() ||
            rd.getFeatureSchema().isRequired() ){

            tf = new JTextField(value.toString());
            cb = null;
            return tf;
       }// End iff
       return new JLabel(value.toString());
    }//getTableCellEditorComponent
    /** @return the object representing the value stored @ that cell*/
    public Object getCellEditorValue(){
      if (cb != null ) return cb.getSelectedItem();
      if (tf != null ) return tf.getText();
      return new String("");
    }//getCellEditorValue
  }//FeaturesEditor inner class

}// End class SchemaAnnotationEditor