/*
 * Copyright (c) 1998-2004, The University of Sheffield.
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free software,
 * licenced under the GNU Library General Public License, Version 2, June 1991
 * (in the distribution as file licence.html, and also available at
 * http://gate.ac.uk/gate/licence.html).
 * 
 * FeaturesSchemaEditor.java
 * 
 * Valentin Tablan, May 18, 2004
 * 
 * $Id$
 */
package gate.gui;

import java.awt.*;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import gate.FeatureMap;
import gate.creole.AnnotationSchema;
import gate.creole.FeatureSchema;
import gate.swing.XJTable;
import gate.util.GateRuntimeException;

/**
 */
public class FeaturesSchemaEditor extends XJTable{
  public FeaturesSchemaEditor(){
    featureList = new ArrayList();
    emptyFeature = new Feature("", null);
    featuresModel = new FeaturesTableModel();
    setModel(featuresModel);
    setTableHeader(null);
    setSortable(false);
    setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
//    setIntercellSpacing(new Dimension(2,2));
    featureEditorRenderer = new FeatureEditorRenderer();
    getColumnModel().getColumn(ICON_COL).setCellRenderer(featureEditorRenderer);
    getColumnModel().getColumn(NAME_COL).setCellRenderer(featureEditorRenderer);
    getColumnModel().getColumn(NAME_COL).setCellEditor(featureEditorRenderer);
    getColumnModel().getColumn(VALUE_COL).setCellRenderer(featureEditorRenderer);
    getColumnModel().getColumn(VALUE_COL).setCellEditor(featureEditorRenderer);
    getColumnModel().getColumn(DELETE_COL).setCellRenderer(featureEditorRenderer);
    getColumnModel().getColumn(DELETE_COL).setCellEditor(featureEditorRenderer);
  }
  public void setFeatures(FeatureMap features){
    this.features = features;
    populate();
  }
  
  public void setSchema(AnnotationSchema schema){
    this.schema = schema;
    featuresModel.fireTableRowsUpdated(0, featureList.size() - 1);
  }
  
  public boolean getScrollableTracksViewportWidth(){
    return true;
  }
  
  public boolean getScrollableTracksViewportHeight(){
    return false;
  }
  /**
   * Called internally whenever the data represented changes.
   *  
   */
  protected void populate(){
    featureList.clear();
    //get all the exisitng features 
    Set fNames = new HashSet(features.keySet());
    //add all the schema features
    if(schema != null && schema.getFeatureSchemaSet() != null){
      Iterator fSchemaIter = schema.getFeatureSchemaSet().iterator();
      while(fSchemaIter.hasNext()){
        FeatureSchema fSchema = (FeatureSchema)fSchemaIter.next();
//        if(fSchema.isRequired()) 
          fNames.add(fSchema.getFeatureName());
      }
    }
    List featureNames = new ArrayList(fNames);
    Collections.sort(featureNames);
    Iterator namIter = featureNames.iterator();
    while(namIter.hasNext()){
      String name = (String)namIter.next();
      Object value = features.get(name);
      featureList.add(new Feature(name, value));
    }
    featureList.add(emptyFeature);
    featuresModel.fireTableDataChanged();
//    setSize(getPreferredScrollableViewportSize());
  }

  FeatureMap features;
  Feature emptyFeature;
  AnnotationSchema schema;
  FeaturesTableModel featuresModel;
  List featureList;
  FeatureEditorRenderer featureEditorRenderer;
  
  private static final int COLUMNS = 4;
  private static final int ICON_COL = 0;
  private static final int NAME_COL = 1;
  private static final int VALUE_COL = 2;
  private static final int DELETE_COL = 3;
  
  private static final Color REQUIRED_WRONG = Color.RED;
  private static final Color OPTIONAL_WRONG = Color.ORANGE;

  protected class Feature{
    String name;
    Object value;

    public Feature(String name, Object value){
      this.name = name;
      this.value = value;
    }
    boolean isSchemaFeature(){
      return schema != null && schema.getFeatureSchema(name) != null;
    }
    boolean isCorrect(){
      if(schema == null) return true;
      FeatureSchema fSchema = schema.getFeatureSchema(name);
      return fSchema == null || fSchema.getPermissibleValues() == null||
             fSchema.getPermissibleValues().contains(value);
    }
    boolean isRequired(){
      if(schema == null) return false;
      FeatureSchema fSchema = schema.getFeatureSchema(name);
      return fSchema != null && fSchema.isRequired();
    }
    Object getDefaultValue(){
      if(schema == null) return null;
      FeatureSchema fSchema = schema.getFeatureSchema(name);
      return fSchema == null ? null : fSchema.getFeatureValue();
    }
  }
  
  
  protected class FeaturesTableModel extends AbstractTableModel{
    public int getRowCount(){
      return featureList.size();
    }
    
    public int getColumnCount(){
      return COLUMNS;
    }
    
    public Object getValueAt(int row, int column){
      Feature feature = (Feature)featureList.get(row);
      switch(column){
        case NAME_COL:
          return feature.name;
        case VALUE_COL:
          return feature.value;
        default:
          return null;
      }
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex){
      return columnIndex == VALUE_COL || columnIndex == NAME_COL || 
             columnIndex == DELETE_COL;
    }
    
    public void setValueAt(Object aValue, int rowIndex,  int columnIndex){
      Feature feature = (Feature)featureList.get(rowIndex);
      switch(columnIndex){
        case VALUE_COL:
          feature.value = aValue;
          if(feature.name != null && feature.name.length() > 0){
            features.put(feature.name, aValue);
            fireTableRowsUpdated(rowIndex, rowIndex);
          }
          break;
        case NAME_COL:
          features.remove(feature.name);
          feature.name = (String)aValue;
          features.put(feature.name, feature.value);
          if(feature == emptyFeature) emptyFeature = new Feature("", null);
          populate();
          break;
        case DELETE_COL:
          //nothing
          break;
        default:
          throw new GateRuntimeException("Non editable cell!");
      }
      
    }
    
    public String getColumnName(int column){
      switch(column){
        case NAME_COL:
          return "Name";
        case VALUE_COL:
          return "Value";
        case DELETE_COL:
          return "";
        default:
          return null;
      }
    }
  }
  
  
  protected class FeatureEditorRenderer extends DefaultCellEditor implements TableCellRenderer{
    public FeatureEditorRenderer(){
      super(new JComboBox());
      editorCombo = (JComboBox)editorComponent;
      editorCombo.setModel(new DefaultComboBoxModel());
      editorCombo.setBackground(FeaturesSchemaEditor.this.getBackground());
      editorCombo.setEditable(true);
      editorCombo.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          stopCellEditing();
        }
      });
      
      rendererCombo = new JComboBox();
      rendererCombo.setModel(new DefaultComboBoxModel());
      rendererCombo.setBackground(FeaturesSchemaEditor.this.getBackground());
      rendererCombo.setEditable(false);
      rendererCombo.setOpaque(false);

      
      requiredIconLabel = new JLabel(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                													Object oldValue,
                													Object newValue){}
        
      };
      requiredIconLabel.setIcon(MainFrame.getIcon("r.gif"));
      requiredIconLabel.setOpaque(false);
      requiredIconLabel.setToolTipText("Required");
      
      optionalIconLabel = new JLabel(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                                          Object oldValue,
                                          Object newValue){}
        
      };
      optionalIconLabel.setIcon(MainFrame.getIcon("o.gif"));
      optionalIconLabel.setOpaque(false);
      optionalIconLabel.setToolTipText("Optional");

      nonSchemaIconLabel = new JLabel(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                													Object oldValue,
                													Object newValue){}
        
      };
      nonSchemaIconLabel.setOpaque(false);
      
      deleteButton = new JButton(MainFrame.getIcon("delete.gif"));
      deleteButton.setMargin(new Insets(0,0,0,0));
      deleteButton.setBorderPainted(false);
      deleteButton.setContentAreaFilled(false);
      deleteButton.setOpaque(false);
      deleteButton.setToolTipText("Delete");
      deleteButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          int row = getEditingRow();
          if(row < 0) return;
          Feature feature = (Feature)featureList.get(row);
          if(feature == emptyFeature){
            feature.value = null;
            featuresModel.fireTableRowsUpdated(row, row);
          }else{
            featureList.remove(row);
            features.remove(feature.name);
            populate();
          }
        }
      });
    }    
		
  	public Component getTableCellRendererComponent(JTable table, Object value,
  	        									   boolean isSelected, boolean hasFocus, int row, int column){
      Feature feature = (Feature)featureList.get(row);
      switch(column){
        case ICON_COL: 
          return feature.isSchemaFeature() ? 
                 (feature.isRequired() ? 
                         requiredIconLabel : 
                         optionalIconLabel) :
                 nonSchemaIconLabel;  
        case NAME_COL:
          prepareCombo(rendererCombo, row, column);
          return rendererCombo;
        case VALUE_COL:
          prepareCombo(rendererCombo, row, column);
          return rendererCombo;
        case DELETE_COL: return deleteButton;  
        default: return null;
      }
    }
  
    public Component getTableCellEditorComponent(JTable table,  Object value, 
            boolean isSelected, int row, int column){
      switch(column){
        case NAME_COL:
          prepareCombo(editorCombo, row, column);
          return editorCombo;
        case VALUE_COL:
          prepareCombo(editorCombo, row, column);
          return editorCombo;
        case DELETE_COL: return deleteButton;  
        default: return null;
      }

    }
  
    protected void prepareCombo(JComboBox combo, int row, int column){
      Feature feature = (Feature)featureList.get(row);
      DefaultComboBoxModel comboModel = (DefaultComboBoxModel)combo.getModel(); 
      comboModel.removeAllElements();
      switch(column){
        case NAME_COL:
          List fNames = new ArrayList();
          if(schema != null && schema.getFeatureSchemaSet() != null){
            Iterator fSchemaIter = schema.getFeatureSchemaSet().iterator();
            while(fSchemaIter.hasNext())
              fNames.add(((FeatureSchema)fSchemaIter.next()).getFeatureName());
          }
          if(!fNames.contains(feature.name))fNames.add(feature.name);
          Collections.sort(fNames);
          for(Iterator nameIter = fNames.iterator(); 
              nameIter.hasNext(); 
              comboModel.addElement(nameIter.next()));
          combo.setBackground(FeaturesSchemaEditor.this.getBackground());
          combo.setSelectedItem(feature.name);
          break;
        case VALUE_COL:
          List fValues = new ArrayList();
          if(feature.isSchemaFeature()){
            Set permValues = schema.getFeatureSchema(feature.name).
              getPermissibleValues();
            if(permValues != null) fValues.addAll(permValues);
          }
          if(!fValues.contains(feature.value)) fValues.add(feature.value);
          for(Iterator valIter = fValues.iterator(); 
              valIter.hasNext(); 
              comboModel.addElement(valIter.next()));
          combo.setBackground(feature.isCorrect() ?
                  FeaturesSchemaEditor.this.getBackground() :
                  (feature.isRequired() ? REQUIRED_WRONG : OPTIONAL_WRONG));
          combo.setSelectedItem(feature.value);
          break;
        default: ;
      }
      
    }

    JLabel requiredIconLabel;
    JLabel optionalIconLabel;
    JLabel nonSchemaIconLabel;
    JComboBox editorCombo;
    JComboBox rendererCombo;
    JButton deleteButton;
  }
}