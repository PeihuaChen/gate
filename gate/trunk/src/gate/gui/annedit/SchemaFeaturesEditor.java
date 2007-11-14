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
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import gate.Factory;
import gate.FeatureMap;
import gate.creole.*;
import gate.event.FeatureMapListener;
import gate.gui.MainFrame;
import gate.swing.JChoice;

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
     * Type for boolean features.
     */
    bool, 
    
    /**
     * Type for free text features.
     */
    text};

  protected class FeatureEditor{
    
    /**
     * Constructor for nominal features
     * @param featureName
     * @param values
     * @param defaultValue
     */
    public FeatureEditor(String featureName, String[] values, 
            String defaultValue){
      this.featureName = featureName;
      this.type = FeatureType.nominal;
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
      //prepare the action listener
      sharedActionListener = new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          Object newValue = null;
          if(e.getSource() == checkbox){
            newValue = new Boolean(checkbox.isSelected());
          }else if(e.getSource() == textField){
            newValue = textField.getText();
          }else if(e.getSource() == jchoice){
            newValue = jchoice.getSelectedItem();
          }else if(e.getSource() == SchemaFeaturesEditor.this){
            //synthetic event
            newValue = getValue();
          }
          
          if(featureMap != null && e.getSource() != SchemaFeaturesEditor.this){
            if(newValue != null){
              featureMap.put(featureName, newValue);
            }else{
              featureMap.remove(featureName);
            }
          }
          
          
          //if the change makes this feature map non schema-compliant,
          //highlight this feature editor
          if(required && newValue == null){
            if(getGui().getBorder() != highlightBorder){ 
              getGui().setBorder(highlightBorder);
            }
          }else{
            if(getGui().getBorder() != defaultBorder){
              getGui().setBorder(defaultBorder);
            }
          }
        }
      };
      //build the empty shell
      gui = new JPanel();
      gui.setAlignmentX(Component.LEFT_ALIGNMENT);
      gui.setLayout(new BoxLayout(gui, BoxLayout.Y_AXIS));
      switch(type) {
        case nominal:
          //use JChoice
          jchoice = new JChoice(values);
          jchoice.setDefaultButtonMargin(new Insets(0, 2, 0, 2));
          jchoice.setMaximumFastChoices(20);
          jchoice.setMaximumWidth(300);
          jchoice.setSelectedItem(value);
          jchoice.addActionListener(sharedActionListener);
          gui.add(jchoice);
          break;
        case bool:
          gui.setLayout(new BoxLayout(gui, BoxLayout.LINE_AXIS));
          checkbox = new JCheckBox();
          checkbox.addActionListener(sharedActionListener);
          if(defaultValue != null){ 
            checkbox.setSelected(Boolean.parseBoolean(defaultValue));
          }
          gui.add(checkbox);
          break;
        case text:
          gui.setLayout(new BoxLayout(gui, BoxLayout.LINE_AXIS));
          textField = new JTextField(20);
          if(value != null){
            textField.setText(value);
          }else if(defaultValue != null){
            textField.setText(defaultValue);
          }
          textField.addActionListener(sharedActionListener);
          textField.getDocument().addDocumentListener(new DocumentListener(){
            public void changedUpdate(DocumentEvent e) {
            }
            public void insertUpdate(DocumentEvent e) {
              sharedActionListener.actionPerformed(
                      new ActionEvent(textField, ActionEvent.ACTION_PERFORMED, 
                              null));
            }
            public void removeUpdate(DocumentEvent e) {
              sharedActionListener.actionPerformed(
                      new ActionEvent(textField, ActionEvent.ACTION_PERFORMED, 
                              null));
            }
          });
          gui.add(textField);          
          break;
      }
      
      defaultBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
      highlightBorder = BorderFactory.createLineBorder(Color.RED, 2);
      gui.setBorder(defaultBorder);
    }
    
    protected JTextField textField;
    protected JCheckBox checkbox;
    protected JChoice jchoice;
    
    protected Border defaultBorder;
    
    protected Border highlightBorder;
    
    
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
     * Is this feature required
     */
    protected boolean required;
    
    /**
     * The action listener that acts upon UI actions on nay of the widgets.
     */
    protected ActionListener sharedActionListener;
    
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
    public void setValue(String value) {
      switch(type){
        case nominal:
          jchoice.setSelectedItem(value);
          break;
        case bool:
          checkbox.setSelected(value != null && Boolean.parseBoolean(value));
          break;
        case text:
          textField.setText(value);
          break;
      }
      //call the action listener to update the border
      sharedActionListener.actionPerformed(
              new ActionEvent(SchemaFeaturesEditor.this, 
              ActionEvent.ACTION_PERFORMED, ""));
    }

    public Object getValue(){
      switch(type){
        case nominal:
          return jchoice.getSelectedItem();
        case bool:
          return new Boolean(checkbox.isSelected());
        case text:
          return textField.getText();
        default:
          return null;
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

    /**
     * @return the required
     */
    public boolean isRequired() {
      return required;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
      this.required = required;
    }

  }
  
  public SchemaFeaturesEditor(AnnotationSchema schema){
    this.schema = schema;
    featureSchemas = new HashMap<String, FeatureSchema>();
    if(schema != null && schema.getFeatureSchemaSet() != null){
      for(FeatureSchema aFSchema : schema.getFeatureSchemaSet()){
        featureSchemas.put(aFSchema.getFeatureName(), aFSchema);
      }
    }
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
        if(defaultValue != null && defaultValue.length() == 0) 
          defaultValue = null;
        String[] valuesArray = null;
        Set values = aFeatureSchema.getPermittedValues();
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
                  defaultValue);
        }else{
          //we don't have any permitted set of values specified
          if(aFeatureSchema.getFeatureValueClass().equals(Boolean.class)){
            //boolean value
            anEditor = new FeatureEditor(aFeatureName, 
                    Boolean.parseBoolean(defaultValue));
          }else{
            //plain text value
            anEditor = new FeatureEditor(aFeatureName, defaultValue);
          }
        }
        anEditor.setRequired(aFeatureSchema.isRequired());
        featureEditors.put(aFeatureName, anEditor);
      }
    }
    //add the feature editors in the alphabetical order
    for(String featureName : featureEditors.keySet()){
      FeatureEditor featureEditor = featureEditors.get(featureName);
      constraints.gridy = gridy++;
      JLabel nameLabel = new JLabel(
              "<html>" + featureName + 
              (featureEditor.isRequired() ? "<b><font color='red'>*</font></b>: " : ": ") +
              "</html>");
      add(nameLabel, constraints);
      constraints.weightx = 1;
      add(featureEditor.getGui(), constraints);
      constraints.weightx = 0;
//      //add a horizontal spacer
//      constraints.weightx = 1;
//      add(Box.createHorizontalGlue(), constraints);
//      constraints.weightx = 0;
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
    this.featureMap = featureMap;
    featureMapUpdated();
  }
  
  /* (non-Javadoc)
   * @see gate.event.FeatureMapListener#featureMapUpdated()
   */
  public void featureMapUpdated() {
    //the underlying F-map was changed
    // 1) remove all non schema compliant values
    if(featureMap != null){
      for(Object aFeatureName : new HashSet<Object>(featureMap.keySet())){
        //first check if the feature is allowed
        if(featureSchemas.keySet().contains(aFeatureName)){
          FeatureSchema fSchema = featureSchemas.get(aFeatureName);
          Object aFeatureValue = featureMap.get(aFeatureName);
          //check if the value is permitted
          if(fSchema.getFeatureValueClass().equals(Boolean.class) ||
             fSchema.getFeatureValueClass().equals(Integer.class) ||
             fSchema.getFeatureValueClass().equals(Short.class) ||
             fSchema.getFeatureValueClass().equals(Byte.class) ||
             fSchema.getFeatureValueClass().equals(Float.class) ||
             fSchema.getFeatureValueClass().equals(Double.class)){
            //just check the right type
            if(!fSchema.getFeatureValueClass().isAssignableFrom(
                    aFeatureValue.getClass())){
              //invalid value type
              featureMap.remove(aFeatureName);
            }
          }else if(fSchema.getFeatureValueClass().equals(String.class)){
            if(fSchema.getPermittedValues() != null &&
                    !fSchema.getPermittedValues().contains(aFeatureValue)){
                   //invalid value
                   featureMap.remove(aFeatureName);
                 }
          }
        }else{
          //feature not permitted
          featureMap.remove(aFeatureName);
        }
      }
    }
    // 2) then update all the displays
    for(String featureName : featureEditors.keySet()){
//      FeatureSchema fSchema = featureSchemas.get(featureName);
      FeatureEditor aFeatureEditor = featureEditors.get(featureName);
      Object featureValue = featureMap == null ? 
              null : featureMap.get(featureName);
      if(featureValue == null){
        //we don't have a value from the featureMap
        //use the default
        featureValue = aFeatureEditor.getDefaultValue();
        //if we still don't have a value, use the last used value
//        if(featureValue == null ||
//           ( featureValue instanceof String && 
//             ((String)featureValue).length() == 0 
//           ) ){
//          featureValue = aFeatureEditor.getValue();
//        }
        if(featureValue != null && featureMap != null){
          //we managed to find a relevant value -> save it in the feature map
          featureMap.put(featureName, featureValue);
        }
      }
      aFeatureEditor.setValue((String)featureValue);
    }
  }
  
  
  /**
   * The feature schema for this editor
   */
  protected AnnotationSchema schema;
  
  /**
   * Stored the individual feature schemas, indexed by name. 
   */
  protected Map<String, FeatureSchema> featureSchemas;
  
  /**
   * The feature map currently being edited.
   */
  protected FeatureMap featureMap;
  

  /**
   * A Map storing the editor for each feature.
   */
  protected Map<String, FeatureEditor> featureEditors;
}
