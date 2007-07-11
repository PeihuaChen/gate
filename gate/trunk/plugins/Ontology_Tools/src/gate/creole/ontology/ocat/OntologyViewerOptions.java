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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import gate.*;
import java.util.*;
import gate.event.DocumentEvent;
import gate.event.DocumentListener;
import gate.gui.MainFrame;
import gate.util.GateRuntimeException;

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
   * Indicates whether to select all subclasses when a super class is
   * selected or not.
   */
  private JCheckBox childFeatureCB;

  /**
   * Indicates whether to confirm the deletion of an annotation with
   * user or not.
   */
  private JCheckBox deleteConfirmationCB;

  /**
   * Indicates whether to be case-sensitive or not when annotating text
   * in the add All option
   */
  private JCheckBox addAllFeatureCaseSensitiveCB;

  /**
   * Indicates whether to use the provided ontology class filter file or
   * not. If yes, it disables all the classes mentioned in the filter
   * file from the ocat tree.
   */
  private JCheckBox useOClassFilterFileCB;

  /**
   * Filter File URL
   */
  private URL filterFileURL;

  /**
   * Text box to display the path of the selected filter file.
   */
  private JTextField oClassFilterFilePathTF;

  /**
   * Button that allows selecting the filter file.
   */
  private JButton browseFilterFileButton;

  /**
   * Button that allows saving the filter file.
   */
   private JButton browseNewFilterFileButton;

  
  /**
   * Default AnnotationSEt or otherAnnotationSets
   */
  private JRadioButton otherAS, defaultAS;

  /**
   * All annotations are listed under this annotationSet comboBox
   */
  private JComboBox annotationSetsNamesCB;

  /**
   * Default AnnotationType, which is Mention and other available
   * annotationtypes
   */
  private JRadioButton otherAT, mentionAT;

  /**
   * All available annotation types, with a capability of adding new,
   * are listed under this annotationTypesComboBox
   */
  private JComboBox annotationTypesCB;

  /**
   * Instance of the main ontologyTreePanel
   */
  private OntologyTreePanel ontologyTreePanel;

  /**
   * List of ontology classes to be filtered out.
   */
  protected HashSet<String> ontologyClassesToFilterOut;

  /**
   * Instead of a null value, we specify the defaultAnnotationSetName
   * with some strange string
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

  private JOptionPane optionPane = null;

  private boolean readFilterFile = false;

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
    if(otherAS.isEnabled() && otherAS.isSelected()) {
      selectedAnnotationSetName = (String)annotationSetsNamesCB
              .getSelectedItem();
    }
    else if(defaultAS.isEnabled()) {
      selectedAnnotationSetName = DEFAULT_ANNOTATION_SET;
    }
    return selectedAnnotationSetName;
  }

  /**
   * The method disables the graphical selection of
   * selectedAnnotationSetName and will allow user to provide the
   * annotationSetName explicitly
   * 
   * @param annotationSetName
   */
  public void disableAnnotationSetSelection(String annotationSetName) {
    selectedAnnotationSetName = annotationSetName;
    // making sure the selectedAnnotationSetName exists, if not, it will
    // be created
    ontologyTreePanel.ontoViewer.getDocument().getAnnotations(
            selectedAnnotationSetName);

    otherAS.setEnabled(false);
    annotationSetsNamesCB.setEnabled(false);
    defaultAS.setEnabled(false);
  }

  /**
   * This will reenable the graphical support for selecting
   * annotationSetsNamesCB
   * 
   * @param annotationSetName
   */
  public void enabledAnnotationSetSelection() {
    otherAS.setEnabled(true);
    annotationSetsNamesCB.setEnabled(true);
    defaultAS.setEnabled(true);
  }

  /** Returns the currently selected Annotation Type */
  public String getSelectedAnnotationType() {
    if(otherAT.isSelected()) {
      selectedAnnotationType = (String)annotationTypesCB.getSelectedItem();
    }
    else {
      selectedAnnotationType = DEFAULT_ANNOTATION_TYPE;
    }

    return selectedAnnotationType;
  }

  /** Initialize the GUI */
  private void initGUI() {
    ontologyClassesToFilterOut = new HashSet<String>();
    childFeatureCB = new JCheckBox("Disable Child Feature");
    deleteConfirmationCB = new JCheckBox("Enable confirm deletion");
    addAllFeatureCaseSensitiveCB = new JCheckBox(
            "Case Sensitive \"Annotate All\" Feature");
    addAllFeatureCaseSensitiveCB.setSelected(true);

    useOClassFilterFileCB = new JCheckBox("Filter");
    useOClassFilterFileCB.addActionListener(new OntologyViewerOptionsActions());
    oClassFilterFilePathTF = new JTextField(15);
    browseFilterFileButton = new JButton("Browse");
    browseFilterFileButton
            .addActionListener(new OntologyViewerOptionsActions());
    browseNewFilterFileButton = new JButton("Save");
    browseNewFilterFileButton.addActionListener(new OntologyViewerOptionsActions());
    
    JPanel temp6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    temp6.add(useOClassFilterFileCB);
    temp6.add(oClassFilterFilePathTF);
    temp6.add(browseFilterFileButton);
    temp6.add(browseNewFilterFileButton);

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
    optionPanel.setLayout(new GridLayout(10, 1));
    optionPanel.add(childFeatureCB);
    optionPanel.add(deleteConfirmationCB);
    optionPanel.add(addAllFeatureCaseSensitiveCB);
    optionPanel.add(temp6);

    optionPanel.add(new JLabel("Annotation Set : "));
    defaultAS = new JRadioButton();
    defaultAS.setSelected(true);
    defaultAS.addActionListener(new OntologyViewerOptionsActions());
    otherAS = new JRadioButton();
    otherAS.addActionListener(new OntologyViewerOptionsActions());

    ButtonGroup group = new ButtonGroup();
    group.add(defaultAS);
    group.add(otherAS);

    JPanel temp3 = new JPanel();
    temp3.setLayout(new FlowLayout(FlowLayout.LEFT));
    temp3.add(defaultAS);
    temp3.add(new JLabel("Default Annotation Set"));

    JPanel temp1 = new JPanel();
    temp1.setLayout(new FlowLayout(FlowLayout.LEFT));
    temp1.add(otherAS);
    temp1.add(annotationSetsNamesCB);

    optionPanel.add(temp3);
    optionPanel.add(temp1);

    optionPanel.add(new JLabel("Annotation Type : "));
    mentionAT = new JRadioButton();
    mentionAT.setSelected(true);
    mentionAT.addActionListener(new OntologyViewerOptionsActions());
    otherAT = new JRadioButton();
    otherAT.addActionListener(new OntologyViewerOptionsActions());

    ButtonGroup group1 = new ButtonGroup();
    group1.add(mentionAT);
    group1.add(otherAT);

    JPanel temp4 = new JPanel();
    temp4.setLayout(new FlowLayout(FlowLayout.LEFT));
    temp4.add(mentionAT);
    temp4.add(new JLabel("Mention"));

    JPanel temp5 = new JPanel();
    temp5.setLayout(new FlowLayout(FlowLayout.LEFT));
    temp5.add(otherAT);
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
   * 
   * @author Niraj Aswani
   * @version 1.0
   */
  private class OntologyViewerOptionsActions extends AbstractAction {

    /**
     * Serial version ID
     */
    private static final long serialVersionUID = 3906926759864643636L;

    public void actionPerformed(ActionEvent ae) {

      if(ae.getSource() == otherAS) {
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
          annotationSetsNamesCB.setSelectedIndex(annotationSetsNamesCB
                  .getSelectedIndex());
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
        if(mentionAT.isSelected()) {
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
            annotationTypesCB.setSelectedIndex(annotationTypesCB
                    .getSelectedIndex());
            return;
          }
        }
      }
      else if(ae.getSource() == defaultAS) {

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

        if(mentionAT.isSelected()) {
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
            annotationTypesCB.setSelectedIndex(annotationTypesCB
                    .getSelectedIndex());
            return;
          }
        }
      }
      else if(ae.getSource() == otherAT) {

        annotationTypesCB.setEnabled(otherAT.isSelected());
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
          annotationTypesCB.setSelectedIndex(annotationTypesCB
                  .getSelectedIndex());
          return;
        }

      }
      else if(ae.getSource() == mentionAT) {

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
      else if(ae.getSource() == browseFilterFileButton) {
        // open the file dialogue
        JFileChooser fileChooser = MainFrame.getFileChooser();
        int answer = fileChooser.showOpenDialog(MainFrame.getInstance());
        if(answer == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fileChooser.getSelectedFile();
          if(selectedFile == null) {
            return;
          }
          else {
            try {
              String newURL = selectedFile.toURI().toURL().toString();
              if(!newURL.equalsIgnoreCase(oClassFilterFilePathTF.getText()
                      .trim())) {
                readFilterFile = true;
              }
              else {
                readFilterFile = false;
              }

              oClassFilterFilePathTF.setText(newURL);
              filterFileURL = selectedFile.toURI().toURL();
              if(isFilterOn()) {
                updateOClassFilter();
              }
            }
            catch(MalformedURLException me) {
              JOptionPane.showMessageDialog(MainFrame.getInstance(),
                      "Invalid URL");
              return;
            }
          }
        }
      } else if(ae.getSource() == browseNewFilterFileButton){
        // open the file dialogue
        JFileChooser fileChooser = MainFrame.getFileChooser();
        int answer = fileChooser.showSaveDialog(MainFrame.getInstance());
        if(answer == JFileChooser.APPROVE_OPTION) {
          File selectedFile = fileChooser.getSelectedFile();
          if(selectedFile == null) {
            return;
          }
          else {
            try {
              
              BufferedWriter bw = new BufferedWriter(new FileWriter(selectedFile));
              for(String s : ontologyClassesToFilterOut) {
                bw.write(s);
                bw.newLine();
              }
              bw.flush();
              bw.close();
            }
            catch(IOException ioe) {
              JOptionPane.showMessageDialog(MainFrame.getInstance(),
                      ioe.getMessage());
              throw new GateRuntimeException(ioe);
            }
          }
        }
        
      }
      else if(ae.getSource() == useOClassFilterFileCB) {
        updateOClassFilter();
      }
    }
  }

  public boolean isFilterOn() {
    return useOClassFilterFileCB.isSelected();
  }

  private void updateOClassFilter() {
    try {
      if(filterFileURL == null || !readFilterFile) return;
      ontologyClassesToFilterOut.clear();
      BufferedReader br = new BufferedReader(new InputStreamReader(
              filterFileURL.openStream()));
      String line = br.readLine();
      while(line != null) {
        ontologyClassesToFilterOut.add(line.trim());
        line = br.readLine();
      }
      br.close();
      ontologyTreePanel.ontoTreeListener.refreshHighlights();
      return;
    }
    catch(IOException ioe) {
      throw new GateRuntimeException(ioe);
    }
  }

  /**
   * Use this method to switch on and off the filter.
   * 
   * @param onOff
   */
  public void setFilterOn(boolean onOff) {
    if(useOClassFilterFileCB.isSelected() != onOff) {
      useOClassFilterFileCB.setSelected(onOff);
    }
    updateOClassFilter();
  }

  /** Returns if Child Feature is set to ON/OFF */
  public boolean isChildFeatureDisabled() {
    return childFeatureCB.isSelected();
  }

  /** Returns if Child Feature is set to ON/OFF */
  public boolean getDeleteConfirmation() {
    return deleteConfirmationCB.isSelected();
  }

  /**
   * Returns if case sensitive option is set to ON/OFF
   * 
   * @return
   */
  public boolean isAddAllOptionCaseSensitive() {
    return addAllFeatureCaseSensitiveCB.isSelected();
  }

  public void addToFilter(HashSet<String> list) {
    if(ontologyClassesToFilterOut == null)
      ontologyClassesToFilterOut = new HashSet<String>();
    ontologyClassesToFilterOut.addAll(list);
    ontologyTreePanel.ontoTreeListener.refreshHighlights();
  }

  public void removeFromFilter(HashSet<String> list) {
    if(ontologyClassesToFilterOut == null)
      ontologyClassesToFilterOut = new HashSet<String>();
    ontologyClassesToFilterOut.removeAll(list);
    ontologyTreePanel.ontoTreeListener.refreshHighlights();
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
   * This methods implements the actions when any
   * selectedAnnotationSetName is removed from
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

  /**
   * Gets the URL of the filter file being used.
   * 
   * @return
   */
  public URL getFilterFileURL() {
    return filterFileURL;
  }

  /**
   * Sets the filter file to be used.
   * 
   * @param filterFileURL
   */
  public void setFilterFileURL(URL filterFileURL) {
    this.filterFileURL = filterFileURL;
    if(isFilterOn()) {
      updateOClassFilter();
    }
  }
}
