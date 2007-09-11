/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  AbstractDocumentView.java
 *
 *  Valentin Tablan, Sep 11, 2007
 *
 *  $Id$
 */
package gate.gui.annedit;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.*;

import javax.swing.*;

import gate.Factory;
import gate.FeatureMap;
import gate.creole.*;
import gate.event.FeatureMapListener;

/**
 * A GUI component for editing a feature map based on a feature schema object.
 */
public class SchemaFeaturesEditor extends JPanel implements FeatureMapListener{

  protected static enum FeatureType{
    /**
     * Type for features that have a range of possible values 
     */
    nominal, 
    
    /**
     * Type for features that have a large range of possible values.
     * This means it's not practical to show the values as a flow of buttons.
     */
    nominal_large, 
    
    /**
     * Type for boolean features.
     */
    bool, 
    
    /**
     * Type for free text features.
     */
    text};

  protected static class FeatureEditor{
    
    /**
     * Constructor for nominal features
     * @param featureName
     * @param values
     * @param defaultValue
     */
    public FeatureEditor(String featureName, String[] values, 
            String defaultValue){
      this.featureName = featureName;
      this.type = values.length <= MAX_BUTTONS_FLOW ? FeatureType.nominal :
        FeatureType.nominal_large;
      this.values = values;
      this.defaultValue = defaultValue;
      buildGui();
    }
    
    /**
     * Constructor for boolean features
     * @param featureName
     * @param defaultValue
     */
    public FeatureEditor(String featureName, boolean defaultValue){
      this.featureName = featureName;
      this.type = FeatureType.bool;
      this.defaultValue = defaultValue ? "true" : "false";
      this.values = null;
      buildGui();
    }
    
    /**
     * Constructor for plain text features
     * @param featureName
     * @param defaultValue
     */
    public FeatureEditor(String featureName, String defaultValue){
      this.featureName = featureName;
      this.type = FeatureType.text;
      this.defaultValue = defaultValue;
      this.values = null;
      buildGui();
    }
    
    /**
     * Builds the GUI according to the internally stored values.
     */
    protected void buildGui(){
      //build the empty shell
      gui = new JPanel();
      gui.setAlignmentX(Component.LEFT_ALIGNMENT);
      switch(type) {
        case nominal:
          gui.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
          //use buttons flow
          buttonGroup = new ButtonGroup();
          for(String aValue : values){
            JToggleButton aButton = new JToggleButton(aValue);
            buttonGroup.add(aButton);
            aButton.setSelected(aValue.equals(value) || 
                    (value == null && aValue.equals(defaultValue)));
            gui.add(aButton);
          }
          break;
        case nominal_large:
          gui.setLayout(new BoxLayout(gui, BoxLayout.LINE_AXIS));
          //use combo-box
          combobox = new JComboBox(values);
          if(value != null) combobox.setSelectedItem(value);
          else if(defaultValue != null) combobox.setSelectedItem(defaultValue);
          //restrict the height: comboboxes look horible when extended
          //verticaly
          combobox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 
                  combobox.getPreferredSize().height));
          gui.add(combobox);
          break;
        case bool:
          gui.setLayout(new BoxLayout(gui, BoxLayout.LINE_AXIS));
          checkBox = new JCheckBox();
          if(defaultValue != null){ 
            checkBox.setSelected(Boolean.parseBoolean(defaultValue));
          }
          gui.add(checkBox);
          break;
        case text:
          gui.setLayout(new BoxLayout(gui, BoxLayout.LINE_AXIS));
          textField = new JTextField();
          if(value != null){
            textField.setText(value);
          }else if(defaultValue != null){
            textField.setText(defaultValue);
          }
          gui.add(textField);          
          break;
      }
    }
    
    protected JTextField textField;
    protected ButtonGroup buttonGroup;
    protected JCheckBox checkBox;
    protected JComboBox combobox;
    
    
    /**
     * The type of the feature.
     */
    protected FeatureType type;
    
    /**
     * The name of the feature
     */
    protected String featureName;
    
    /**
     * 
     * The GUI used for editing the feature.
     */
    protected JComponent gui;
    
    /**
     * Permitted values for nominal features. 
     */
    protected String[] values;
    
    /**
     * Default value as string.
     */
    protected String defaultValue;
    
    /**
     * The value of the feature
     */
    protected String value;
    
    /**
     * @return the type
     */
    public FeatureType getType() {
      return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(FeatureType type) {
      this.type = type;
    }
    /**
     * @return the values
     */
    public String[] getValues() {
      return values;
    }
    /**
     * @param values the values to set
     */
    public void setValues(String[] values) {
      this.values = values;
    }
    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
      return defaultValue;
    }
    
    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
    }
    
    /**
     * Sets the value for this feature
     * @param value
     */
    /**
     * @param value
     */
    public void setValue(Object value) {
      switch(type){
        case nominal:
          Enumeration<AbstractButton> btnEnum = buttonGroup.getElements();
          while(btnEnum.hasMoreElements()){
            AbstractButton aBtn = btnEnum.nextElement();
            if(aBtn.getText().equals(value.toString())){
              aBtn.setSelected(true);
              break;
            }
          }
          break;
        case nominal_large:
          combobox.setSelectedItem(value.toString());
          break;
        case bool:
          checkBox.setSelected(Boolean.parseBoolean(value.toString()));
          break;
        case text:
          textField.setText(value.toString());
          break;
      }
    }

    
    /**
     * @return the featureName
     */
    public String getFeatureName() {
      return featureName;
    }
    /**
     * @param featureName the featureName to set
     */
    public void setFeatureName(String featureName) {
      this.featureName = featureName;
    }
    
    /**
     * @return the gui
     */
    public JComponent getGui() {
      if(gui == null) buildGui();
      return gui;
    }
    /**
     * The maximum number of values to be represented as a buttons flow (as 
     * opposed to a combo-box). 
     */
    private static final int MAX_BUTTONS_FLOW = 10;

  }
  
  public SchemaFeaturesEditor(AnnotationSchema schema){
    this.schema = schema;
    initGui();
  }
  
  public static void main(String[] args){
    try {
      JFrame aFrame = new JFrame("New Annotation Editor");

      AnnotationSchema aSchema = new AnnotationSchema();
      aSchema.setXmlFileUrl(new File("/home/valyt/tmp/bug/schema.xml").toURI().toURL());
      aSchema.init();
      
      final SchemaFeaturesEditor fsEditor = new SchemaFeaturesEditor(aSchema);
      
      aFrame.getContentPane().add(fsEditor, BorderLayout.CENTER);
      aFrame.pack();
      aFrame.setVisible(true);
      
      JToolBar tBar = new JToolBar();
      tBar.add(new AbstractAction("New Values!"){
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
          FeatureMap fMap = Factory.newFeatureMap();
          
          fMap.put("boolean-false", new Boolean(true));
          fMap.put("boolean-true", new Boolean(false));
          fMap.put("nominal-long", "val10");
          fMap.put("nominal-short", "val6");
          fMap.put("free-text", "New text!");
          fsEditor.editFeatureMap(fMap);
          
        }
      });
      
      tBar.add(new AbstractAction("Null Values!"){
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
          fsEditor.editFeatureMap(null);
          
        }
      });
      aFrame.getContentPane().add(tBar, BorderLayout.NORTH);
      
      
    }
    catch(HeadlessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(ResourceInstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
      
  }
  
  protected void initGui(){
    setLayout(new GridBagLayout());   
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(2,2,2,2);
    constraints.weightx = 0;
    constraints.weighty = 0;
    int gridy = 0;
    constraints.gridx = GridBagConstraints.RELATIVE;

    
    //build the feature editors
    featureEditors = new TreeMap<String, FeatureEditor>();
    Set<FeatureSchema> fsSet = schema.getFeatureSchemaSet();
    if(fsSet != null){
      for(FeatureSchema aFeatureSchema : fsSet){
        String aFeatureName = aFeatureSchema.getFeatureName();
        String defaultValue = aFeatureSchema.getFeatureValue();
        String[] valuesArray = null;
        Set values = aFeatureSchema.getPermissibleValues();
        if(values != null && values.size() > 0){
          valuesArray = new String[values.size()];
          int i = 0;
          for(Object aValue : values){
            valuesArray[i++] = aValue.toString();
          }
          Arrays.sort(valuesArray);
        }
        //build the right editor for the current feature
        FeatureEditor anEditor;
        if(valuesArray != null && valuesArray.length > 0){
          //we have a set of allowed values -> nominal feature
          anEditor = new FeatureEditor(aFeatureName, valuesArray, 
                  aFeatureSchema.getFeatureValue());
        }else{
          //we don't have any permitted set of values specified
          if(aFeatureSchema.getValueClassName().equals(Boolean.class.getCanonicalName())){
            //boolean value
            anEditor = new FeatureEditor(aFeatureName, 
                    Boolean.parseBoolean(aFeatureSchema.getFeatureValue()));
          }else{
            //plain text value
            anEditor = new FeatureEditor(aFeatureName, 
                    aFeatureSchema.getFeatureValue());
          }
        }
        featureEditors.put(aFeatureName, anEditor);
      }
    }
    //add the feature editors in the alphabetical order
    for(String featureName : featureEditors.keySet()){
      FeatureEditor featureEditor = featureEditors.get(featureName);
      constraints.gridy = gridy++;
      JLabel nameLabel = new JLabel(featureName);
      add(nameLabel, constraints);
      add(featureEditor.getGui(), constraints);
      //add a horizontal spacer
      constraints.weightx = 1;
      add(Box.createHorizontalGlue(), constraints);
      constraints.weightx = 0;
    }
    //add a vertical spacer
    constraints.weighty = 1;
    constraints.gridy = gridy++;
    constraints.gridx = GridBagConstraints.LINE_START;
    add(Box.createVerticalGlue(), constraints);
  }
  
  /**
   * Method called to initiate editing of a new feature map.
   * @param featureMap
   */
  public void editFeatureMap(FeatureMap featureMap){
    if(this.featureMap != null && this.featureMap != featureMap){
      this.featureMap.removeFeatureMapListener(this);
    }
    this.featureMap = featureMap;
    if(this.featureMap != null){
      this.featureMap.addFeatureMapListener(this);
    }
    featureMapUpdated();
  }
  
  
  /* (non-Javadoc)
   * @see gate.event.FeatureMapListener#featureMapUpdated()
   */
  public void featureMapUpdated() {
    //the underlying F-map was changed
    //update all the displays
    for(String featureName : featureEditors.keySet()){
      FeatureEditor aFeatureEditor = featureEditors.get(featureName);
      if(featureMap == null){
        //use default value
        aFeatureEditor.setValue(aFeatureEditor.getDefaultValue());
      }else{
        //use actual value
        aFeatureEditor.setValue(featureMap.get(featureName));
      }
    }
  }
  
  /**
   * The feature schema for this editor
   */
  protected AnnotationSchema schema;
  
  /**
   * The feature map currently being edited.
   */
  protected FeatureMap featureMap;
  

  /**
   * A Map storing the editor for each feature.
   */
  protected Map<String, FeatureEditor> featureEditors;
}
