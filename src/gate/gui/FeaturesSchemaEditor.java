/*
 * Copyright (c) 1998-2005, The University of Sheffield.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import gate.*;
import gate.creole.*;
import gate.creole.metadata.*;
import gate.event.FeatureMapListener;
import gate.swing.XJTable;
import gate.util.*;

/**
 */
@CreoleResource(name = "Features", guiType = GuiType.SMALL,
    resourceDisplayed = "gate.util.FeatureBearer")
public class FeaturesSchemaEditor extends AbstractVisualResource
        implements ResizableVisualResource, FeatureMapListener{
  public FeaturesSchemaEditor(){
//    setBackground(UIManager.getDefaults().getColor("Table.background"));
    instance = this;
  }

  public void setTargetFeatures(FeatureMap features){
    if(targetFeatures != null) targetFeatures.removeFeatureMapListener(this);
    this.targetFeatures = features;
    populate();
    if(targetFeatures != null) targetFeatures.addFeatureMapListener(this);
  }
  
  
  @Override
  public void cleanup() {
    super.cleanup();
    if(targetFeatures != null){
      targetFeatures.removeFeatureMapListener(this);
      targetFeatures = null;
    }
    target = null;
    schema = null;
  }

  /* (non-Javadoc)
   * @see gate.VisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target){
    this.target = (FeatureBearer)target;
    setTargetFeatures(this.target.getFeatures());
  }
  
  public void setSchema(AnnotationSchema schema){
    this.schema = schema;
    featuresModel.fireTableRowsUpdated(0, featureList.size() - 1);
  }
    
  public XJTable getTable(){
    return mainTable;
  }

  /* (non-Javadoc)
   * @see gate.event.FeatureMapListener#featureMapUpdated()
   * Called each time targetFeatures is changed.
   */
  public void featureMapUpdated(){
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        populate();    
      }
    });
  }
  
  
  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    featureList = new ArrayList<Feature>();
    emptyFeature = new Feature("", null);
    featureList.add(emptyFeature);
    initGUI();
    return this;
  }//init()
  
  protected void initGUI(){
    featuresModel = new FeaturesTableModel();
    mainTable = new XJTable();
    mainTable.setModel(featuresModel);
    mainTable.setTableHeader(null);
    mainTable.setSortable(false);
    mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    mainTable.setShowGrid(false);
    mainTable.setBackground(getBackground());
    mainTable.setIntercellSpacing(new Dimension(2,2));
    mainTable.setTabSkipUneditableCell(true);
    mainTable.setEditCellAsSoonAsFocus(true);
    featureEditorRenderer = new FeatureEditorRenderer();
    mainTable.getColumnModel().getColumn(ICON_COL).
        setCellRenderer(featureEditorRenderer);
    mainTable.getColumnModel().getColumn(NAME_COL).
        setCellRenderer(featureEditorRenderer);
    mainTable.getColumnModel().getColumn(NAME_COL).
        setCellEditor(featureEditorRenderer);
    mainTable.getColumnModel().getColumn(VALUE_COL).
        setCellRenderer(featureEditorRenderer);
    mainTable.getColumnModel().getColumn(VALUE_COL).
        setCellEditor(featureEditorRenderer);
    mainTable.getColumnModel().getColumn(DELETE_COL).
        setCellRenderer(featureEditorRenderer);
    mainTable.getColumnModel().getColumn(DELETE_COL).
      setCellEditor(featureEditorRenderer);
    
    //the background colour seems to change somewhere when using the GTK+ 
    //look and feel on Linux, so we copy the value now and set it 
    Color tableBG = mainTable.getBackground();
    //make a copy of the value (as the reference gets changed somewhere)
    tableBG = new Color(tableBG.getRGB());
    mainTable.setBackground(tableBG);

    // allow Tab key to select the next cell in the table
    mainTable.setSurrendersFocusOnKeystroke(true);

    // remove (shift) control tab as traversal keys
    Set<AWTKeyStroke> keySet = new HashSet<AWTKeyStroke>(
      mainTable.getFocusTraversalKeys(
      KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
    keySet.remove(KeyStroke.getKeyStroke("control TAB"));
    mainTable.setFocusTraversalKeys(
      KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keySet);
    keySet = new HashSet<AWTKeyStroke>(
      mainTable.getFocusTraversalKeys(
      KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
    keySet.remove(KeyStroke.getKeyStroke("shift control TAB"));
    mainTable.setFocusTraversalKeys(
      KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, keySet);

    // add (shift) control tab to go the container of this component
    keySet.clear();
    keySet.add(KeyStroke.getKeyStroke("control TAB"));
    mainTable.setFocusTraversalKeys(
      KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, keySet);
    keySet.clear();
    keySet.add(KeyStroke.getKeyStroke("shift control TAB"));
    mainTable.setFocusTraversalKeys(
      KeyboardFocusManager.DOWN_CYCLE_TRAVERSAL_KEYS, keySet);

    scroller = new JScrollPane(mainTable);
    scroller.getViewport().setOpaque(true);
    scroller.getViewport().setBackground(tableBG);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(scroller);
  }
  
  /**
   * Called internally whenever the data represented changes.
   * Get feature names from targetFeatures and schema then sort them
   * and add them to featureList.
   * Fire a table data changed event for the feature table whith featureList
   * used as data model.
   */
  protected void populate(){
    featureList.clear();
    //get all the existing features
    Set fNames = new HashSet();
    
    if(targetFeatures != null){
      //add all the schema features
      fNames.addAll(targetFeatures.keySet());
      if(schema != null && schema.getFeatureSchemaSet() != null){
        for(FeatureSchema featureSchema : schema.getFeatureSchemaSet()) {
          //        if(featureSchema.isRequired())
          fNames.add(featureSchema.getFeatureName());
        }
      }
      List featureNames = new ArrayList(fNames);
      Collections.sort(featureNames);
      for(Object featureName : featureNames) {
        String name = (String) featureName;
        Object value = targetFeatures.get(name);
        featureList.add(new Feature(name, value));
      }
    }
    if (!featureList.contains(emptyFeature)) {
      featureList.add(emptyFeature);
    }
    featuresModel.fireTableDataChanged();
    mainTable.setSize(mainTable.getPreferredScrollableViewportSize());
  }

  FeatureMap targetFeatures;
  FeatureBearer target;
  Feature emptyFeature;
  AnnotationSchema schema;
  FeaturesTableModel featuresModel;
  List<Feature> featureList;
  FeatureEditorRenderer featureEditorRenderer;
  XJTable mainTable;
  JScrollPane scroller;
  FeaturesSchemaEditor instance;
  
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
      return fSchema == null || fSchema.getPermittedValues() == null||
             fSchema.getPermittedValues().contains(value);
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
      Feature feature = featureList.get(row);
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
      Feature feature = featureList.get(rowIndex);
      if (feature == null) { return; }
      if(targetFeatures == null){
        targetFeatures = Factory.newFeatureMap();
        target.setFeatures(targetFeatures);
        setTargetFeatures(targetFeatures);
      }
      switch(columnIndex){
        case VALUE_COL:
          if (feature.value != null
           && feature.value.equals(aValue)) { return; }
          feature.value = aValue;
          if(feature.name != null && feature.name.length() > 0){
            targetFeatures.removeFeatureMapListener(instance);
            targetFeatures.put(feature.name, aValue);
            targetFeatures.addFeatureMapListener(instance);
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                // edit the last row that is empty
                mainTable.editCellAt(mainTable.getRowCount() - 1, NAME_COL);
              }
            });
          }
          break;
        case NAME_COL:
          if (feature.name.equals(aValue)) {
            return;
          }
          targetFeatures.remove(feature.name);
          feature.name = (String)aValue;
          targetFeatures.put(feature.name, feature.value);
          if(feature == emptyFeature) emptyFeature = new Feature("", null);
          populate();
          int newRow;
          for (newRow = 0; newRow < mainTable.getRowCount(); newRow++) {
            if (mainTable.getValueAt(newRow, NAME_COL).equals(feature.name)) {
              break; // find the previously selected row in the new table
            }
          }
          final int newRowF = newRow;
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              // edit the cell containing the value associated with this name
              mainTable.editCellAt(newRowF, VALUE_COL);
            }
          });
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


  protected class FeatureEditorRenderer extends DefaultCellEditor
                                        implements TableCellRenderer {
    public FeatureEditorRenderer(){
      super(new JComboBox());
      defaultComparator = new ObjectComparator();
      editorCombo = (JComboBox)editorComponent;
      editorCombo.setModel(new DefaultComboBoxModel());
      editorCombo.setBackground(mainTable.getBackground());
      editorCombo.setEditable(true);
      editorCombo.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          stopCellEditing();
        }
      });
      
      rendererCombo = new JComboBox();
      rendererCombo.setModel(new DefaultComboBoxModel());
      rendererCombo.setBackground(mainTable.getBackground());
      rendererCombo.setEditable(true);
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
      requiredIconLabel.setIcon(MainFrame.getIcon("r"));
      requiredIconLabel.setOpaque(false);
      requiredIconLabel.setToolTipText("Required feature");
      
      optionalIconLabel = new JLabel(){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                                          Object oldValue,
                                          Object newValue){}
        
      };
      optionalIconLabel.setIcon(MainFrame.getIcon("o"));
      optionalIconLabel.setOpaque(false);
      optionalIconLabel.setToolTipText("Optional feature");

      nonSchemaIconLabel = new JLabel(MainFrame.getIcon("c")){
        public void repaint(long tm, int x, int y, int width, int height){}
        public void repaint(Rectangle r){}
        public void validate(){}
        public void revalidate(){}
        protected void firePropertyChange(String propertyName,
                													Object oldValue,
                													Object newValue){}
        
      };
      nonSchemaIconLabel.setToolTipText("Custom feature");
      nonSchemaIconLabel.setOpaque(false);
      
      deleteButton = new JButton(MainFrame.getIcon("delete"));
      deleteButton.setMargin(new Insets(0,0,0,0));
//      deleteButton.setBorderPainted(false);
//      deleteButton.setContentAreaFilled(false);
//      deleteButton.setOpaque(false);
      deleteButton.setToolTipText("Delete");
      deleteButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent evt){
          int row = mainTable.getEditingRow();
          if(row < 0) return;
          Feature feature = featureList.get(row);
          if(feature == emptyFeature){
            feature.value = null;
          }else{
            featureList.remove(row);
            targetFeatures.remove(feature.name);
            featuresModel.fireTableRowsDeleted(row, row);
            mainTable.setSize(mainTable.getPreferredScrollableViewportSize());
          }
        }
      });
    }    
		
  	public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column){
      Feature feature = featureList.get(row);
      switch(column){
        case ICON_COL: 
          return feature.isSchemaFeature() ? 
                 (feature.isRequired() ? 
                         requiredIconLabel : 
                         optionalIconLabel) :
                         nonSchemaIconLabel;  
        case NAME_COL:
          rendererCombo.setPreferredSize(null);
          prepareCombo(rendererCombo, row, column);
//          Dimension dim = rendererCombo.getPreferredSize();
//          rendererCombo.setPreferredSize(new Dimension(dim.width + 5, dim.height));
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
      Feature feature = featureList.get(row);
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
          combo.getEditor().getEditorComponent().setBackground(FeaturesSchemaEditor.this.getBackground());
          combo.setSelectedItem(feature.name);
          break;
        case VALUE_COL:
          List fValues = new ArrayList();
          if(feature.isSchemaFeature()){
            Set permValues = schema.getFeatureSchema(feature.name).
              getPermittedValues();
            if(permValues != null) fValues.addAll(permValues);
          }
          if(!fValues.contains(feature.value)) fValues.add(feature.value);
          Collections.sort(fValues, defaultComparator);
          for(Iterator valIter = fValues.iterator(); 
              valIter.hasNext(); 
              comboModel.addElement(valIter.next()));
          combo.getEditor().getEditorComponent().setBackground(feature.isCorrect() ?
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
    ObjectComparator defaultComparator;
  }
}
