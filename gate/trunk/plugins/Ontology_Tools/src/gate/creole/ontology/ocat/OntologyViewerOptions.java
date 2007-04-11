/*
 *  OntologyViewerOptions.java
 *
 *  Niraj Aswani, 12/March/07
 *
 *  $Id: OntologyViewerOptions.html,v 1.0 2007/03/12 16:13:01 niraj Exp $
 */
package gate.creole.ontology.ocat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import gate.*;
import java.util.*;
import gate.event.DocumentEvent;
import gate.event.DocumentListener;

/**
 * Description: This class Provides options window to set the options
 * for Ontology Viewer
 * 
 * @author Niraj Aswani
 * @version 1.0
 */
public class OntologyViewerOptions implements DocumentListener {

  private JPanel optionPanel;

  /**
   * Indicates whether to select all subclasses when a super class is selected or not.
   */
  private JCheckBox cFChBox;

  /**
   * Indicates whether to confirm the deletion of an annotation with user or not.
   */
  private JCheckBox dCChBox;

  /**
   * Indicates whether to be case-sensitive or not when annotating text in the add All option
   */
  private JCheckBox addAllFeatureCaseSensitiveChBox;
  
  /**
   * Default AnnotationSEt or otherAnnotationSets 
   */
  private JRadioButton exSet, defaultSet;

  /**
   * All annotations are listed under this annotationSet comboBox
   */
  private JComboBox annotationSetsNamesCB;

  /**
   * Default AnnotationType, which is Mention and other available annotationtypes
   */
  private JRadioButton exType, defaultType;

  /**
   * All available annotation types, with a capability of adding new, are listed under this annotationTypesComboBox
   */
  private JComboBox annotationTypesCB;

  /**
   * Instance of the main ontologyTreePanel
   */
  private OntologyTreePanel ontologyTreePanel;

  /**
   * Instead of a null value, we specify the defaultAnnotationSetName with some strange string
   */
  public static final String DEFAULT_ANNOTATION_SET = "00#Default#00",
          DEFAULT_ANNOTATION_TYPE = "Mention";

  /**
   * Currently selected annotationSetName
   */
  protected String selectedAnnotationSetName = DEFAULT_ANNOTATION_SET;

  /**
   * Currently selected annotation type
   */
  protected String selectedAnnotationType = DEFAULT_ANNOTATION_TYPE;

  private JOptionPane optionPane;

  /**
   * Constructor
   * 
   * @param ontologyTreePanel
   */
  public OntologyViewerOptions(OntologyTreePanel ontoTree) {
    this.ontologyTreePanel = ontoTree;
    ontoTree.ontoViewer.getDocument().addDocumentListener(this);
    initGUI();
  }

  /**
   * Releases all resources
   */
  public void cleanup() {
    ontologyTreePanel.ontoViewer.getDocument().removeDocumentListener(this);
  }

  /** Returns the currently selected Annotation Set */
  public String getSelectedAnnotationSetName() {
    if(exSet.isEnabled() && exSet.isSelected()) {
      selectedAnnotationSetName = (String)annotationSetsNamesCB.getSelectedItem();
    }
    else if(defaultSet.isEnabled()) {
      selectedAnnotationSetName = DEFAULT_ANNOTATION_SET;
    }
    return selectedAnnotationSetName;
  }

  /**
   * The method disables the graphical selection of selectedAnnotationSetName and
   * will allow user to provide the annotationSetName explicitly
   * 
   * @param annotationSetName
   */
  public void disableAnnotationSetSelection(String annotationSetName) {
    selectedAnnotationSetName = annotationSetName;
    // making sure the selectedAnnotationSetName exists, if not, it will be created
    ontologyTreePanel.ontoViewer.getDocument().getAnnotations(selectedAnnotationSetName);

    exSet.setEnabled(false);
    annotationSetsNamesCB.setEnabled(false);
    defaultSet.setEnabled(false);
  }

  /**
   * This will reenable the graphical support for selecting
   * annotationSetsNamesCB
   * 
   * @param annotationSetName
   */
  public void enabledAnnotationSetSelection() {
    exSet.setEnabled(true);
    annotationSetsNamesCB.setEnabled(true);
    defaultSet.setEnabled(true);
  }

  /** Returns the currently selected Annotation Type */
  public String getSelectedAnnotationType() {
    if(exType.isSelected()) {
      selectedAnnotationType = (String)annotationTypesCB.getSelectedItem();
    }
    else {
      selectedAnnotationType = DEFAULT_ANNOTATION_TYPE;
    }

    return selectedAnnotationType;
  }

  /** Initialize the GUI */
  private void initGUI() {
    cFChBox = new JCheckBox("Disable Child Feature");
    dCChBox = new JCheckBox("Enable confirm deletion");
    addAllFeatureCaseSensitiveChBox = new JCheckBox("Case Sensitive \"Annotate All\" Feature");
    addAllFeatureCaseSensitiveChBox.setSelected(true);
    
    annotationSetsNamesCB = new JComboBox();
    annotationTypesCB = new JComboBox();

    // lets find out all the annotations
    Document document = ontologyTreePanel.ontoViewer.getDocument();

    Map annotSetMap = document.getNamedAnnotationSets();
    if(annotSetMap != null) {
      java.util.List setNames = new ArrayList(annotSetMap.keySet());
      if(setNames != null) {
        Collections.sort(setNames);
        Iterator setsIter = setNames.iterator();
        while(setsIter.hasNext()) {
          String setName = (String)setsIter.next();
          annotationSetsNamesCB.addItem(setName);
          ontologyTreePanel.ontoViewer.getDocument().getAnnotations(setName)
                  .addAnnotationSetListener(ontologyTreePanel.ontoViewer);
        }
      }
    }
    annotationSetsNamesCB.setEnabled(false);
    annotationSetsNamesCB.setEditable(true);
    annotationSetsNamesCB.addActionListener(new OntologyViewerOptionsActions());

    Set types = document.getAnnotations().getAllTypes();
    if(types != null) {
      Iterator iter = types.iterator();
      while(iter.hasNext()) {
        annotationTypesCB.addItem((String)iter.next());
      }
    }

    annotationTypesCB.setEnabled(false);
    annotationTypesCB.setEditable(true);
    annotationTypesCB.addActionListener(new OntologyViewerOptionsActions());

    optionPanel = new JPanel();
    optionPanel.setLayout(new GridLayout(9, 1));
    optionPanel.add(cFChBox);
    optionPanel.add(dCChBox);
    optionPanel.add(addAllFeatureCaseSensitiveChBox);

    optionPanel.add(new JLabel("Annotation Set : "));
    defaultSet = new JRadioButton();
    defaultSet.setSelected(true);
    defaultSet.addActionListener(new OntologyViewerOptionsActions());
    exSet = new JRadioButton();
    exSet.addActionListener(new OntologyViewerOptionsActions());

    ButtonGroup group = new ButtonGroup();
    group.add(defaultSet);
    group.add(exSet);

    JPanel temp3 = new JPanel();
    temp3.setLayout(new FlowLayout(FlowLayout.LEFT));
    temp3.add(defaultSet);
    temp3.add(new JLabel("Default Annotation Set"));

    JPanel temp1 = new JPanel();
    temp1.setLayout(new FlowLayout(FlowLayout.LEFT));
    temp1.add(exSet);
    temp1.add(annotationSetsNamesCB);

    optionPanel.add(temp3);
    optionPanel.add(temp1);

    optionPanel.add(new JLabel("Annotation Type : "));
    defaultType = new JRadioButton();
    defaultType.setSelected(true);
    defaultType.addActionListener(new OntologyViewerOptionsActions());
    exType = new JRadioButton();
    exType.addActionListener(new OntologyViewerOptionsActions());

    ButtonGroup group1 = new ButtonGroup();
    group1.add(defaultType);
    group1.add(exType);

    JPanel temp4 = new JPanel();
    temp4.setLayout(new FlowLayout(FlowLayout.LEFT));
    temp4.add(defaultType);
    temp4.add(new JLabel("Mention"));

    JPanel temp5 = new JPanel();
    temp5.setLayout(new FlowLayout(FlowLayout.LEFT));
    temp5.add(exType);
    temp5.add(annotationTypesCB);

    optionPanel.add(temp4);
    optionPanel.add(temp5);

    optionPane = new JOptionPane();

  }

  /**
   * Returns the panel for ontoOption Panel
   * 
   * @return
   */
  public Component getGUI() {
    JPanel myPanel = new JPanel();
    myPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    myPanel.add(optionPanel);
    return myPanel;
  }

  /**
   * Inner class that implements the actions for various options
   * @author Niraj Aswani
   * @version 1.0
   */
  private class OntologyViewerOptionsActions extends AbstractAction {

    /**
	 * Serial version ID
	 */
	private static final long serialVersionUID = 3906926759864643636L;

	public void actionPerformed(ActionEvent ae) {

      if(ae.getSource() == exSet) {
        annotationSetsNamesCB.setEnabled(true);
        if(annotationSetsNamesCB.getSelectedItem() == null
                && annotationSetsNamesCB.getItemCount() > 0) {
          annotationSetsNamesCB.setSelectedIndex(0);
          return;
        }
        else if(annotationSetsNamesCB.getItemCount() == 0) {
          ontologyTreePanel.ontoTreeListener.removeHighlights();
          return;
        }
        else {
          annotationSetsNamesCB.setSelectedIndex(annotationSetsNamesCB.getSelectedIndex());
          return;
        }
      }
      else if(ae.getSource() == annotationSetsNamesCB) {

        // see if user has entered a new Item
        String item = (String)annotationSetsNamesCB.getSelectedItem();

        // we need to change the annotationTypesCB values as well
        annotationTypesCB.removeAllItems();
        Set types = ontologyTreePanel.ontoViewer.getDocument().getAnnotations(
                (String)item).getAllTypes();
        if(types != null) {
          Iterator iter = types.iterator();
          while(iter.hasNext()) {
            annotationTypesCB.addItem((String)iter.next());
          }
        }

        annotationTypesCB.updateUI();
        if(defaultType.isSelected()) {
          ontologyTreePanel.ontoTreeListener.refreshHighlights();
        }
        else {
          if(annotationTypesCB.getSelectedItem() == null
                  && annotationTypesCB.getItemCount() > 0) {
            annotationTypesCB.setSelectedIndex(0);
            return;
          }
          else if(annotationTypesCB.getItemCount() == 0) {
            ontologyTreePanel.ontoTreeListener.removeHighlights();
            return;
          }
          else {
            annotationTypesCB
                    .setSelectedIndex(annotationTypesCB.getSelectedIndex());
            return;
          }
        }
      }
      else if(ae.getSource() == defaultSet) {

        annotationSetsNamesCB.setEnabled(false);

        // we need to change the annotationTypesCB values as well
        annotationTypesCB.removeAllItems();
        Set types = ontologyTreePanel.ontoViewer.getDocument().getAnnotations()
                .getAllTypes();

        if(types != null) {
          Iterator iter = types.iterator();
          while(iter.hasNext()) {
            annotationTypesCB.addItem((String)iter.next());
          }
        }
        annotationTypesCB.updateUI();

        if(defaultType.isSelected()) {
          ontologyTreePanel.ontoTreeListener.refreshHighlights();
        }
        else {
          if(annotationTypesCB.getSelectedItem() == null
                  && annotationTypesCB.getItemCount() > 0) {
            annotationTypesCB.setSelectedIndex(0);
            return;
          }
          else if(annotationTypesCB.getItemCount() == 0) {
            ontologyTreePanel.ontoTreeListener.removeHighlights();
            return;
          }
          else {
            annotationTypesCB
                    .setSelectedIndex(annotationTypesCB.getSelectedIndex());
            return;
          }
        }
      }
      else if(ae.getSource() == exType) {

        annotationTypesCB.setEnabled(exType.isSelected());
        if(annotationTypesCB.getSelectedItem() == null
                && annotationTypesCB.getItemCount() > 0) {
          annotationTypesCB.setSelectedIndex(0);
          return;
        }
        else if(annotationTypesCB.getItemCount() == 0) {
          ontologyTreePanel.ontoTreeListener.removeHighlights();
          return;
        }
        else {
          annotationTypesCB.setSelectedIndex(annotationTypesCB.getSelectedIndex());
          return;
        }

      }
      else if(ae.getSource() == defaultType) {

        annotationTypesCB.setEnabled(false);
        ontologyTreePanel.ontoTreeListener.refreshHighlights();
        return;

      }
      else if(ae.getSource() == annotationTypesCB) {

        // see if user has entered a new Item
        String item = (String)annotationTypesCB.getSelectedItem();
        if(item == null) {
          if(annotationTypesCB.getItemCount() > 0) {
            annotationTypesCB.setSelectedIndex(0);
            return;
          }
          return;
        }

        for(int i = 0; i < annotationTypesCB.getItemCount(); i++) {
          if(item.equals((String)annotationTypesCB.getItemAt(i))) {
            annotationTypesCB.setSelectedIndex(i);
            ontologyTreePanel.ontoTreeListener.refreshHighlights();
            return;
          }
        }

        // here means a new item is added
        annotationTypesCB.addItem(item);
        // annotationTypesCB.setSelectedItem(item);
        ontologyTreePanel.ontoTreeListener.refreshHighlights();
        return;
      }
    }
  }

  /** Returns if Child Feature is set to ON/OFF */
  public boolean getChildFeature() {
    return cFChBox.isSelected();
  }

  /** Returns if Child Feature is set to ON/OFF */
  public boolean getDeleteConfirmation() {
    return dCChBox.isSelected();
  }

  /**
   * Returns if case sensitive option is set to ON/OFF
   * @return
   */
  public boolean isAddAllOptionCaseSensitive() {
    return addAllFeatureCaseSensitiveChBox.isSelected();
  }
  
  // DocumentListener Methods
  public void annotationSetAdded(DocumentEvent de) {
    // we need to update our annotationSetsNamesCB List
    String getSelected = (String)annotationSetsNamesCB.getSelectedItem();
    annotationSetsNamesCB.addItem(de.getAnnotationSetName());
    ontologyTreePanel.ontoViewer.getDocument().getAnnotations(
            de.getAnnotationSetName()).addAnnotationSetListener(
            ontologyTreePanel.ontoViewer);
    ;
    annotationSetsNamesCB.setSelectedItem(getSelected);
  }

  public void contentEdited(DocumentEvent de) {
    // ignore
  }

  /**
   * This methods implements the actions when any selectedAnnotationSetName is
   * removed from
   * 
   * @param de
   */
  public void annotationSetRemoved(DocumentEvent de) {
    String getSelected = (String)annotationSetsNamesCB.getSelectedItem();
    annotationSetsNamesCB.removeItem(de.getAnnotationSetName());
    // Note: still removing the hook (listener) is remaining and we need
    // to
    // sort it out
  }
}
