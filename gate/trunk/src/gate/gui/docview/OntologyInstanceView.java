/**
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Thomas Heitz - 17/12/2009
 *
 *  $Id$
 */

package gate.gui.docview;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.gui.MainFrame;
import gate.swing.XJTable;
import gate.creole.ontology.*;
import gate.util.InvalidOffsetException;
import gate.util.LuckyException;
import gate.util.Out;
import gate.util.Strings;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.text.Collator;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * Document view that shows two tables: one instances and one for properties.
 * The instances table is linked with the OntologyClassView class selection
 * and the properties table is linked with the instances view.
 * <br>
 * Two buttons allow to add a new instance from the text selection in the
 * document or as a new label for the selected instance.
 * <br>
 * You can filter the instances table, delete instances and set properties
 * that are defined in the ontology as object properties.
 */
public class OntologyInstanceView extends AbstractDocumentView {

  public OntologyInstanceView() {

    instanceSet = new HashSet<OInstance>();
    propertiesSet = new HashSet<ObjectProperty>();
    propertiesNotSet = new HashSet<ObjectProperty>();
    classByPropertyMap = new HashMap<String, Set<OClass>>();
  }

  protected void initGUI() {

    // get a pointer to the text view used to display
    // the selected annotations
    Iterator centralViewsIter = owner.getCentralViews().iterator();
    while(textView == null && centralViewsIter.hasNext()){
      DocumentView aView = (DocumentView) centralViewsIter.next();
      if(aView instanceof TextualDocumentView)
        textView = (TextualDocumentView) aView;
    }

    mainPanel = new JPanel(new BorderLayout());

    // filter and buttons at the top
    JPanel filterPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    JLabel filterLabel = new JLabel("Filter: ");
    filterPanel.add(filterLabel, gbc);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    filterPanel.add(filterTextField = new JTextField(20), gbc);
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0;
    filterTextField.setToolTipText("Filter the instance table rows");
    JPanel filterButtonsPanel = new JPanel();
    clearFilterButton = new JButton();
    clearFilterButton.setBorder(BorderFactory.createEmptyBorder());
    filterButtonsPanel.add(clearFilterButton);
    newInstanceButton = new JButton("New Inst.");
    newInstanceButton.setEnabled(false);
    newInstanceButton.setToolTipText("New instance from the selection");
    newInstanceButton.setMnemonic(KeyEvent.VK_N);
    filterButtonsPanel.add(newInstanceButton);
    addLabelButton = new JButton("Add to Selected Inst.");
    addLabelButton.setEnabled(false);
    addLabelButton.setToolTipText(
      "Add label from selection to the selected instance");
    addLabelButton.setMnemonic(KeyEvent.VK_A);
    filterButtonsPanel.add(addLabelButton);
    filterPanel.add(filterButtonsPanel, gbc);

    mainPanel.add(filterPanel, BorderLayout.NORTH);

    // tables at the bottom
    JPanel tablesPanel = new JPanel(new GridLayout(1, 2));
    instanceTable = new XJTable() {
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    DefaultTableModel model = new DefaultTableModel();
    model.addColumn("Instance");
    model.addColumn("Label");
    instanceTable.setModel(model);
    tablesPanel.add(new JScrollPane(instanceTable));
    propertyTable = new XJTable(){
      public boolean isCellEditable(int row, int column) {
        // property values are editable
        return convertColumnIndexToModel(column) == 1;
      }
    };
    model = new DefaultTableModel();
    model.addColumn("Property");
    model.addColumn("Value");
    propertyTable.setModel(model);
    tablesPanel.add(new JScrollPane(propertyTable));

    mainPanel.add(tablesPanel, BorderLayout.CENTER);

    initListeners();
  }

  protected void initListeners() {

    clearFilterButton.setAction(
      new AbstractAction("", MainFrame.getIcon("exit.gif")) {
      { this.putValue(MNEMONIC_KEY, KeyEvent.VK_BACK_SPACE);
        this.putValue(SHORT_DESCRIPTION, "Clear text field"); }
      public void actionPerformed(ActionEvent e) {
        filterTextField.setText("");
        filterTextField.requestFocusInWindow();
      }
    });

    // when an instance is selected, update the property table
    instanceTable.getSelectionModel().addListSelectionListener(
      new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
          if (e.getValueIsAdjusting()) { return; }
          updatePropertyTable();
          addLabelButton.setEnabled(newInstanceButton.isEnabled()
                                 && selectedInstance != null);
        }
      }
    );

    // when typing a character in the instance table, use it for filtering
    instanceTable.addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent e) {
        if (e.getKeyChar() != KeyEvent.VK_TAB
         && e.getKeyChar() != KeyEvent.VK_SPACE
         && e.getKeyChar() != KeyEvent.VK_BACK_SPACE
         && e.getKeyChar() != KeyEvent.VK_DELETE) {
          filterTextField.requestFocusInWindow();
          filterTextField.setText(String.valueOf(e.getKeyChar()));
        }
      }
    });

    // context menu to delete instances
    instanceTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        processMouseEvent(evt);
      }
      public void mousePressed(MouseEvent evt) {
        JTable table = (JTable) evt.getSource();
        int row =  table.rowAtPoint(evt.getPoint());
        if (evt.isPopupTrigger()
        && !table.isRowSelected(row)) {
          // if right click outside the selection then reset selection
          table.getSelectionModel().setSelectionInterval(row, row);
        }
        processMouseEvent(evt);
      }
      public void mouseReleased(MouseEvent evt) {
        processMouseEvent(evt);
      }
      protected void processMouseEvent(MouseEvent evt) {
        final JTable table = (JTable) evt.getSource();
        int row = table.rowAtPoint(evt.getPoint());
        if (row >= 0) {
          if (evt.isPopupTrigger()) {
            // context menu
            JPopupMenu popup = new JPopupMenu();
            if (table.getSelectedRowCount() > 0) {
              popup.add(new AbstractAction(table.getSelectedRowCount() > 1 ?
                "Delete instances" : "Delete instance") {
                public void actionPerformed(ActionEvent e) {
                  for (OInstance oInstance : instanceSet) {
                    for (int selectedRow : table.getSelectedRows()) {
                      if (oInstance.getName().equals(
                          table.getModel().getValueAt(selectedRow, 0))) {
                        selectedOntology.removeOInstance(oInstance);
                        // find annotations related to this instance
                        AnnotationSet annotationSet =
                          document.getAnnotations(classView.getSelectedSet());
                        for (Annotation annotation :
                          annotationSet.get("Mention")) {
                          if (annotation.getFeatures().containsKey(ONTOLOGY)
                          && annotation.getFeatures().get(ONTOLOGY)
                            .equals(selectedOntology.getOntologyURI())
                          && annotation.getFeatures().containsKey(CLASS)
                          && annotation.getFeatures().get(CLASS)
                            .equals(selectedClass.getONodeID().toString())
                          && annotation.getFeatures().containsKey(INSTANCE)
                          && annotation.getFeatures().get(INSTANCE)
                            .equals(oInstance.getONodeID().toString())) {
                            // delete the annotation
                            annotationSet.remove(annotation);
                          }
                        }
                      }
                    }
                  }
                  classView.setClassSelected(selectedClass, false);
                  classView.setClassSelected(selectedClass, true);
                  updateInstanceTable(selectedClass);
                }
              });
            }
            if (popup.getComponentCount() > 0) {
              popup.show(table, evt.getX(), evt.getY());
            }
          }
        }
      }
    });

    // context menu to delete properties
    propertyTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        processMouseEvent(evt);
      }
      public void mousePressed(MouseEvent evt) {
        JTable table = (JTable) evt.getSource();
        int row =  table.rowAtPoint(evt.getPoint());
        if (evt.isPopupTrigger()
        && !table.isRowSelected(row)) {
          // if right click outside the selection then reset selection
          table.getSelectionModel().setSelectionInterval(row, row);
        }
        processMouseEvent(evt);
      }
      public void mouseReleased(MouseEvent evt) {
        processMouseEvent(evt);
      }
      protected void processMouseEvent(MouseEvent evt) {
        final JTable table = (JTable) evt.getSource();
        int row = table.rowAtPoint(evt.getPoint());
        if (row >= 0) {
          if (evt.isPopupTrigger()) {
            // context menu
            JPopupMenu popup = new JPopupMenu();
            if (table.getSelectedRowCount() > 0) {
              popup.add(new AbstractAction(table.getSelectedRowCount() > 1 ?
                "Delete properties" : "Delete property") {
                public void actionPerformed(ActionEvent e) {
                  for (ObjectProperty objectProperty : propertiesSet) {
                    for (int selectedRow : table.getSelectedRows()) {
                      // find the property that matches the first column value
                      if (objectProperty.getName().equals(
                          table.getModel().getValueAt(selectedRow, 0))) {
                        for (OInstance oInstance : selectedInstance
                            .getObjectPropertyValues(objectProperty)) {
                          String value = oInstance.getONodeID()
                            .getResourceName();
                          // find the value that matches the second column value
                          if (value.equals(table.getModel()
                              .getValueAt(selectedRow, 1))) {
                            // delete the property
                            selectedInstance.removeObjectPropertyValue(
                              objectProperty, oInstance);
                            break;
                          }
                        }
                      }
                    }
                  }
                  updatePropertyTable();
                }
              });
            }
            if (popup.getComponentCount() > 0) {
              popup.show(table, evt.getX(), evt.getY());
            }
          }
        }
      }
    });

    // show only the rows containing the text from filterTextField
    filterTextField.getDocument().addDocumentListener(new DocumentListener() {
      private Timer timer = new Timer("Instance view table rows filter", true);
      private TimerTask timerTask;
      public void changedUpdate(DocumentEvent e) { /* do nothing */ }
      public void insertUpdate(DocumentEvent e) { update(); }
      public void removeUpdate(DocumentEvent e) { update(); }
      private void update() {
        if (timerTask != null) { timerTask.cancel(); }
        Date timeToRun = new Date(System.currentTimeMillis() + 300);
        timerTask = new TimerTask() { public void run() {
          updateInstanceTable(selectedClass);
        }};
        // add a delay
        timer.schedule(timerTask, timeToRun);
      }
    });

    // Up/Down key events in filterTextField are transferred to the table
    filterTextField.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP
         || e.getKeyCode() == KeyEvent.VK_DOWN
         || e.getKeyCode() == KeyEvent.VK_PAGE_UP
         || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
          instanceTable.dispatchEvent(e);
        }
      }
    });
  }

  protected void registerHooks() { /* do nothing */ }

  protected void unregisterHooks() { /* do nothing */ }

  public Component getGUI() {
    return mainPanel;
  }

  public int getType() {
    return HORIZONTAL;
  }

  public void setClassView(OntologyClassView classView) {
    this.classView = classView;
  }

  /**
   * Update the instance table for the class and ontology selected.
   * @param selectedClass class selected
   */
  public void updateInstanceTable(OClass selectedClass) {
    this.selectedClass = selectedClass;
    instanceSet.clear();
    final DefaultTableModel tableModel = new DefaultTableModel();
    tableModel.addColumn("Instance");
    tableModel.addColumn("Label");
    if (selectedClass != null) {
      selectedOntology = selectedClass.getOntology();
      Set<OInstance> instances = selectedOntology.getOInstances(
        selectedClass, OConstants.Closure.TRANSITIVE_CLOSURE);
      String filter = filterTextField.getText()
        .trim().toLowerCase(Locale.ENGLISH);
      for (OInstance instance : instances) {
        Set<AnnotationProperty> properties =
          instance.getSetAnnotationProperties();
        for (AnnotationProperty property : properties) {
          if (property.getName().equals("label")) {
            instanceSet.add(instance);
            List<Literal> values =
              instance.getAnnotationPropertyValues(property);
            Set<String> labels = new HashSet<String>();
            boolean matchFilter = false;
            for (Literal value : values) {
              labels.add(value.getValue());
              if (value.getValue().toLowerCase().indexOf(filter) != -1) {
                matchFilter = true;
              }
            }
            if (matchFilter) {
              tableModel.addRow(new Object[]{instance.getName(),
                Strings.toString(labels)});
            } else {
              instanceSet.remove(instance);
            }
          }
        }
      }
    }
    SwingUtilities.invokeLater(new Runnable() { public void run() {
      instanceTable.setModel(tableModel);
      if (instanceTable.getRowCount() > 0) {
        instanceTable.setRowSelectionInterval(0, 0);
      }
    }});
  }

  public void updatePropertyTable() {
    selectedInstance = null;
    final DefaultTableModel tableModel = new DefaultTableModel();
    tableModel.addColumn("Property");
    tableModel.addColumn("Value");
    if (instanceTable.getSelectedRow() != -1) {
      String selectedValue = (String) instanceTable.getValueAt(
        instanceTable.getSelectedRow(),
        instanceTable.convertColumnIndexToView(0));
      for (OInstance instance : instanceSet) {
        if (instance.getName().equals(selectedValue)) {
          // found the instance matching the name in the table
          selectedInstance = instance;
          propertiesSet.clear();
          propertiesNotSet.clear();
          // get all object properties that can be set for this instance
          Set<OClass> classes =
            instance.getOClasses(OConstants.Closure.DIRECT_CLOSURE);
          for (OClass oClass : classes) {
            for (RDFProperty property :
                 oClass.getPropertiesWithResourceAsDomain()) {
              if (property instanceof ObjectProperty) {
                propertiesNotSet.add((ObjectProperty) property);
                Set<String> ranges = new HashSet<String>();
                Set<OClass> rangeClasses = new HashSet<OClass>();
                for (OResource range :
                    ((ObjectProperty) property).getRange()) {
                  ranges.add(range.getName());
                  rangeClasses.add((OClass) range);
                }
                if (ranges.isEmpty()) {
                  ranges.add("All classes");
                }
                classByPropertyMap.put(property.getName(), rangeClasses);
                tableModel.addRow(new Object[]{property.getName(),
                  Strings.toString(ranges)});
              }
            }
          }
          // get all set object properties and values for this instance
          for (ObjectProperty objectProperty :
               instance.getSetObjectProperties()) {
            propertiesSet.add(objectProperty);
            for (OInstance oInstance :
                 instance.getObjectPropertyValues(objectProperty)) {
                  tableModel.addRow(new Object[]{objectProperty.getName(),
                    oInstance.getONodeID().getResourceName()});
            }
          }
          break;
        }
      }
    }
    SwingUtilities.invokeLater(new Runnable() { public void run() {
      propertyTable.setModel(tableModel);
      propertyTable.getColumnModel().getColumn(1)
        .setCellEditor(new PropertyValueCellEditor());
    }});
  }

  /**
   * Create a new annotation and instance from a text selection.
   * Use the text selected as the instance property label.
   *
   * @param selectedSet name of the selected annotation set
   * @param selectedText selection
   * @param start selection start offset
   * @param end selection end offset
   */
  protected void addSelectionToFilter(final String selectedSet,
      final String selectedText, final int start, final int end) {
    newInstanceButton.setAction(
      new AbstractAction(newInstanceButton.getText()) {
      { this.putValue(MNEMONIC_KEY, KeyEvent.VK_N);
        this.putValue(SHORT_DESCRIPTION, newInstanceButton.getToolTipText()); }
      public void actionPerformed(ActionEvent e) {
        createFromSelection(selectedSet, selectedText, start, end, true);
        filterTextField.setText("");
      }
    });
    newInstanceButton.setEnabled(true);
    addLabelButton.setAction(
      new AbstractAction(addLabelButton.getText()) {
      { this.putValue(MNEMONIC_KEY, KeyEvent.VK_A);
        this.putValue(SHORT_DESCRIPTION, addLabelButton.getToolTipText()); }
      public void actionPerformed(ActionEvent e) {
        createFromSelection(selectedSet, selectedText, start, end, false);
        filterTextField.setText("");
      }
    });
    filterTextField.setText(selectedText);
    addLabelButton.setEnabled(selectedInstance != null);
  }

  /**
   * Create a new annotation and instance or label from a text selection.
   * Use the text selected as the instance property label.
   *
   * @param selectedSet name of the selected annotation set
   * @param selectedText selection
   * @param start selection start offset
   * @param end selection end offset
   * @param newInstance true if it will create a new instance otherwise
   * it will add a new label to the selected instance
   */
  protected void createFromSelection(String selectedSet, String selectedText,
                                     int start, int end, boolean newInstance) {
    newInstanceButton.setEnabled(false);
    addLabelButton.setEnabled(false);
    AnnotationProperty annotationProperty;
    RDFProperty property = selectedOntology.getProperty(
      selectedOntology.createOURIForName("label"));
    if (property == null) {
      // create a property 'label' if it doesn't exist
      annotationProperty = selectedOntology.addAnnotationProperty(
        selectedOntology.createOURIForName("label"));
    } else if (property instanceof AnnotationProperty) {
      // get the existing property 'label'
      annotationProperty = (AnnotationProperty) property;
    } else {
      Out.prln("There is already a property 'label' " +
        "that is not an annotation property!");
      return;
    }
    OInstance instance = selectedInstance;
    if (newInstance) {
      OURI instanceOURI = selectedOntology.createOURIForName(selectedText);
      for (int i = 0; selectedOntology.containsOInstance(instanceOURI)
          && i < Integer.MAX_VALUE; i++) {
        // instance name already existing so suffix with a number
        instanceOURI = selectedOntology.createOURIForName(selectedText+'_'+i);
      }
      // create a new instance from the text selected
      instance = selectedOntology.addOInstance(instanceOURI, selectedClass);
    }
    // add a property 'label' with the selected text as value
    instance.addAnnotationPropertyValue(annotationProperty,
      new Literal(selectedText));
    AnnotationSet set = document.getAnnotations(selectedSet);
    Integer id;
    try {
      features = Factory.newFeatureMap();
      features.put(ONTOLOGY, selectedOntology.getOntologyURI());
      features.put(CLASS, selectedClass.getONodeID().toString());
      features.put(INSTANCE, instance.getONodeID().toString());
      // create a new annotation from the text selected
      id = set.add((long) start, (long) end, "Mention", features);
    } catch(InvalidOffsetException e) {
      throw new LuckyException(e);
    }
    classView.setClassSelected(selectedClass, false);
    classView.setClassSelected(selectedClass, true);
//    classView.selectInstance(set, set.get(id), selectedClass);
    updateInstanceTable(selectedClass);
  }

  protected class PropertyValueCellEditor extends AbstractCellEditor
      implements TableCellEditor, ActionListener {
    private JComboBox valueComboBox;
    private Collator comparator;
    private String oldValue;
    private Map<String, OInstance> nameInstanceMap;

    private PropertyValueCellEditor() {
      valueComboBox = new JComboBox();
      valueComboBox.setMaximumRowCount(10);
      valueComboBox.addActionListener(this);
      comparator = Collator.getInstance();
      comparator.setStrength(java.text.Collator.TERTIARY);
      nameInstanceMap = new HashMap<String, OInstance>();
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {
      oldValue = (String) value;
      TreeSet<String> ts = new TreeSet<String>(comparator);
      Set<OClass> classes = classByPropertyMap.get((String)
        propertyTable.getModel().getValueAt(row, 0));
      if (classes.isEmpty()) { classes = selectedOntology.getOClasses(false); }
      for (OClass oClass : classes) {
        // get all the classes that belong to the property domain
        Set<OInstance> instances = selectedOntology.getOInstances(
          oClass, OConstants.Closure.TRANSITIVE_CLOSURE);
        for (OInstance instance : instances) {
          // for each class add their instance names
          ts.add(instance.getName());
          nameInstanceMap.put(instance.getName(), instance);
        }
      }
      DefaultComboBoxModel dcbm = new DefaultComboBoxModel(ts.toArray());
      valueComboBox.setModel(dcbm);
      valueComboBox.setSelectedItem(propertyTable.getValueAt(row, column));
      return valueComboBox;
    }

    public Object getCellEditorValue() {
      return valueComboBox.getSelectedItem();
    }

    protected void fireEditingStopped() {
      String newValue = (String) getCellEditorValue();
      if (newValue == null) {
        fireEditingCanceled();
        return;
      }
      super.fireEditingStopped();
      String selectedProperty = (String) propertyTable.getModel().getValueAt(
        propertyTable.getSelectedRow(), 0);
      // search the object property to set
      for (ObjectProperty objectProperty : propertiesSet) {
        // verify that the property value correspond
        if (objectProperty.getName().equals(selectedProperty)) {
          for (OInstance oInstance :
               selectedInstance.getObjectPropertyValues(objectProperty)) {
            String value = oInstance.getONodeID().getResourceName();
            if (value.equals(oldValue)) {
              // property already existing, remove it first
              selectedInstance.removeObjectPropertyValue(
                objectProperty, oInstance);
                try {
                  // set the new value for the selected object property
                  selectedInstance.addObjectPropertyValue(
                    objectProperty, nameInstanceMap.get(newValue));
                } catch (InvalidValueException e) {
                  e.printStackTrace();
                }
              updatePropertyTable();
              return;
            }
          }
        }
      }
      for (ObjectProperty objectProperty : propertiesNotSet) {
        if (objectProperty.getName().equals(selectedProperty)) {
          try {
            // set the new value for the selected object property
              selectedInstance.addObjectPropertyValue(
                objectProperty, nameInstanceMap.get(newValue));
          } catch (InvalidValueException e) {
            e.printStackTrace();
          }
          updatePropertyTable();
          return;
        }
      }
    }

    // TODO: itemlistener may be better
    public void actionPerformed(ActionEvent e) {
      if (getCellEditorValue() == null) {
        fireEditingCanceled();
      } else {
        fireEditingStopped();
      }
    }
  }

  // external resources
  protected Ontology selectedOntology;
  protected TextualDocumentView textView;
  protected OntologyClassView classView;

  // UI components
  protected JPanel mainPanel;
  protected JTextField filterTextField;
  protected JButton clearFilterButton;
  protected JButton newInstanceButton;
  protected JButton addLabelButton;
  protected XJTable instanceTable;
  protected XJTable propertyTable;

  // local objects
  protected OClass selectedClass;
  protected OInstance selectedInstance;
  protected Set<OInstance> instanceSet;
  protected Set<ObjectProperty> propertiesSet;
  protected Set<ObjectProperty> propertiesNotSet;
  protected Map<String, Set<OClass>> classByPropertyMap;
  protected static final String ONTOLOGY =
    gate.creole.ANNIEConstants.LOOKUP_ONTOLOGY_FEATURE_NAME;
  protected static final String CLASS =
    gate.creole.ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME;
  protected static final String INSTANCE =
    gate.creole.ANNIEConstants.LOOKUP_INSTANCE_FEATURE_NAME;
}
