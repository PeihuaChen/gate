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

/**
 */
public class FeaturesSchemaEditor extends XJTable{
  public FeaturesSchemaEditor(){
    featureList = new ArrayList();
    featuresModel = new FeaturesTableModel();
    setModel(featuresModel);
    //    setTableHeader(null);
    setSortable(false);
    setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
    featureRenderer = new FeatureRenderer();
    getColumnModel().getColumn(ICON_COL).setCellRenderer(featureRenderer);
    getColumnModel().getColumn(NAME_COL).setCellRenderer(featureRenderer);
    getColumnModel().getColumn(VALUE_COL).setCellRenderer(featureRenderer);
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
    return true;
  }
  /**
   * Called internally whenever the data represented changes.
   *  
   */
  protected void populate(){
    featureList.clear();
    List featureNames = new ArrayList(features.keySet());
    Collections.sort(featureNames);
    Iterator namIter = featureNames.iterator();
    while(namIter.hasNext()){
      String name = (String)namIter.next();
      Object value = features.get(name);
      featureList.add(new Feature(name, value));
    }
    featuresModel.fireTableDataChanged();
  }

  FeatureMap features;
  AnnotationSchema schema;
  FeaturesTableModel featuresModel;
  List featureList;
  FeatureRenderer featureRenderer;
  
  private static final int COLUMNS = 4;
  private static final int ICON_COL = 0;
  private static final int NAME_COL = 1;
  private static final int VALUE_COL = 2;
  private static final int DELETE_COL = 3;

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
      return fSchema == null || fSchema.getPermissibleValues().contains(value);
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
    public String getColumnName(int column){
      switch(column){
        case NAME_COL:
          return "Name";
        case VALUE_COL:
          return "Value";
        case DELETE_COL:
          return "!";
        default:
          return null;
      }
    }
  }
  
  
  protected class FeatureRenderer implements TableCellRenderer{
    public FeatureRenderer(){
      schemaIconLabel = new JLabel(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                													Object oldValue,
                													Object newValue){}
        
      };
      schemaIconLabel.setIcon(MainFrame.getIcon("s.gif"));
      schemaIconLabel.setOpaque(false);
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
      nameLabel = new JLabel(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                													Object oldValue,
                													Object newValue){}
        
      };
      nameLabel.setOpaque(false);
      valueTextField = new JTextField(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
      };
      valueTextField.setBackground(UIManager.getLookAndFeelDefaults().
              getColor("ToolTip.background"));
      valueTextField.setBorder(null);
      
    }    
		
		public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column){
      Feature feature = (Feature)featureList.get(row);
      switch(column){
        case ICON_COL:
          return feature.isSchemaFeature()
                  ? schemaIconLabel
                  : nonSchemaIconLabel;
        case NAME_COL:
          nameLabel.setText(feature.name);
          return nameLabel;
        case VALUE_COL:
          valueTextField.setText(feature.value.toString());
          return valueTextField;
        default: return null;
      }
    }

    JLabel schemaIconLabel;
    JLabel nonSchemaIconLabel;
    JLabel nameLabel;
    JTextField valueTextField;
    JButton deleteButton;
  }
}