/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  AnnotationEditor.java
 *
 *  Valentin Tablan, Sep 10, 2007
 *
 *  $Id$
 */


package gate.gui.docview;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import gate.Annotation;
import gate.AnnotationSet;
import gate.creole.AnnotationSchema;
import gate.creole.FeatureSchema;

public class AnnotationEditorPane extends JPanel{

  /**
   * The annotation currently being edited.
   */
  protected Annotation annotation;
  
  /**
   * The annotation set containing the currently edited annotation. 
   */
  protected AnnotationSet annSet;
  
  /**
   * The annotation schema for this editor;
   */
  protected AnnotationSchema schema;
  /**
   * The textual view controlling this annotation editor.
   */
  protected TextualDocumentView textView;
  
  /**
   * A Map storing the editor for each feature.
   */
  protected Map<String, FeatureEditor> featureEditors;
  
  /**
   * The maximum number of values to be represented as a buttons flow (as 
   * opposed to a combo-box). 
   */
  private static final int MAX_BUTTONS_FLOW = 6;
  
  protected static enum FeatureType{nominal, bool, text};

  protected static class FeatureEditor{
    
    public FeatureEditor(String featureName, FeatureType featureType, 
            String featureValue){
      this.featureName = featureName;
      this.type = featureType;
      this.value = featureValue;
    }
    
    /**
     * Builds the GUI according to the internally stored values.
     */
    protected void buildGui(){
      //build the empty shell
      gui = new JPanel();
      gui.setAlignmentX(Component.LEFT_ALIGNMENT);
      //set the layout
      LayoutManager layout = null;
      switch(type) {
        case nominal:
          if(values.length <= MAX_BUTTONS_FLOW){
            //use buttons flow
            layout = new FlowLayout(FlowLayout.LEFT);
          }else{
            //use combo-box
            layout = new BoxLayout(gui, BoxLayout.LINE_AXIS);
          }
          break;
        case bool:
        case text:
          layout = new BoxLayout(gui, BoxLayout.LINE_AXIS);
          break;
      }
      gui.setLayout(layout);
//      //add the name label
//      gui.add(new JLabel(featureName));
//      gui.add(Box.createRigidArea(new Dimension(5, 1)));
      //add the editor components
      switch(type) {
        case nominal:
          if(values.length <= MAX_BUTTONS_FLOW){
            //use buttons flow
            buttonGroup = new ButtonGroup();
            for(String aValue : values){
              JToggleButton aButton = new JToggleButton(aValue);
              buttonGroup.add(aButton);
              aButton.setSelected(aValue.equals(value) || 
                      (value == null && aValue.equals(defaultValue)));
              gui.add(aButton);
            }
          }else{
            //use combo-box
            combobox = new JComboBox(values);
            if(value != null) combobox.setSelectedItem(value);
            else if(defaultValue != null) combobox.setSelectedItem(defaultValue);
//            combobox.setMaximumSize(combobox.getPreferredSize());
            gui.add(combobox);
          }
          break;
        case bool:
          checkBox = new JCheckBox();
          if(value != null){
            checkBox.setSelected(Boolean.parseBoolean(value));
          }else if(defaultValue != null){ 
            checkBox.setSelected(Boolean.parseBoolean(defaultValue));
          }
          gui.add(checkBox);
          break;
        case text:
          textField = new JTextField();
          if(value != null){
            textField.setText(value);
          }else if(defaultValue != null){
            textField.setText(defaultValue);
          }
          gui.add(textField);
          break;
      }
      gui.setMaximumSize(gui.getPreferredSize());
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
    
  }
  
  public AnnotationEditorPane(TextualDocumentView textView){
    super();
    this.textView = textView;
  }
  
  protected void initGui(){
    setLayout(new GridBagLayout());    
    GridBagConstraints constraints = new GridBagConstraints();
    featureEditors = new HashMap<String, FeatureEditor>();

    for(String aFeatureName : (Set<String>)schema.getFeatureSchemaSet()){
      FeatureSchema aSchema = schema.getFeatureSchema(aFeatureName);
      String defaultValue = aSchema.getFeatureValue();
      String[] valuesArray = null;
      Set values = aSchema.getPermissibleValues();
      if(values != null && values.size() > 0){
        valuesArray = new String[values.size()];
        int i = 0;
        for(Object aValue : values){
          valuesArray[i++] = aValue.toString();
        }
      }
      FeatureType type;
      if(valuesArray != null && valuesArray.length > 0){
        type = FeatureType.nominal;
      }else{
        //we don't have any permissable values specified
        if(aSchema.getValueClassName().equals(Boolean.class.getCanonicalName())){
          //boolean value
          type = FeatureType.bool;
        }else{
          //plain text value
          type = FeatureType.text;
        }
      }
      Object value = null;
      if(annotation != null){
        value = annotation.getFeatures().get(aFeatureName);
      }else{
        //no annotation yet, use the default
        value = aSchema.getFeatureValue();
      }
      
      FeatureEditor anEditor = new FeatureEditor(aFeatureName, type, 
              value != null? value.toString() : null);
      featureEditors.put(aFeatureName, anEditor);
      
      constraints.gridx = GridBagConstraints.LINE_START;
      constraints.gridy = GridBagConstraints.RELATIVE;
      add(new JLabel(aFeatureName), constraints);
      constraints.gridx = GridBagConstraints.RELATIVE;
      add(anEditor.getGui(), constraints);
      
    }
    
  }
  
  public void setAnnotation(Annotation ann, AnnotationSet set){
    this.annotation = ann;
    this.annSet = set;
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    JFrame aFrame = new JFrame("New Annotation Editor");
//    aFrame.setSize( 800, 600);
    
    Box aBox = Box.createVerticalBox();
    aFrame.getContentPane().add(aBox);
    
    FeatureEditor aFeatEditor = new FeatureEditor("F-nominal-small",
            FeatureType.nominal, "val1");
    aFeatEditor.setValues(new String[]{"val1", "val2", "val3"});
    aBox.add(aFeatEditor.getGui());
    
    aFeatEditor = new FeatureEditor("F-nominal-large",
            FeatureType.nominal, "val1");
    aFeatEditor.setValues(new String[]{"val1", "val2", "val3", "val4", "val5", 
            "val6", "val7", "val8", "val9"});
    aBox.add(aFeatEditor.getGui());
    
    aFeatEditor = new FeatureEditor("F-boolean-true",
            FeatureType.bool, "true");
    aBox.add(aFeatEditor.getGui());    
    
    aFeatEditor = new FeatureEditor("F-boolean-false",
            FeatureType.bool, "false");
    aBox.add(aFeatEditor.getGui());
    
//    aBox.add(Box.createVerticalGlue());
    
    aFrame.pack();
    aFrame.setVisible(true);
  }

  /**
   * @return the schema
   */
  public AnnotationSchema getSchema() {
    return schema;
  }

  /**
   * @param schema the schema to set
   */
  public void setSchema(AnnotationSchema schema) {
    this.schema = schema;
  }

}
