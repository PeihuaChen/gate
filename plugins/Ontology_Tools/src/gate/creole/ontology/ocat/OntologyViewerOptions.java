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

  private JScrollPane scroller;
  
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
   * selected text as annotation property?
   * user or not.
   */
  private JCheckBox selectedTextAsPropertyValue;
  
  private JTextField propertyName;
  
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
  private JRadioButton classesToHideRB;

  /**
   * Filter File URL
   */
  private URL classesToHideFileURL;

  /**
   * Text box to display the path of the selected filter file.
   */
  private JTextField classesToHideFilePathTF;

  /**
   * Button that allows selecting the filter file.
   */
  private JButton browseClassesToHideFileButton;

  /**
   * Button that allows saving the filter file.
   */
  private JButton saveClassesToHideFileButton;

  
  /**
   * Indicates whether to use the provided ontology class filter file or
   * not. If yes, it disables all the classes mentioned in the filter
   * file from the ocat tree.
   */
  private JRadioButton classesToShowRB;

  private JRadioButton disableFilteringRB;
  
  /**
   * Filter File URL
   */
  private URL classesToShowFileURL;

  /**
   * Text box to display the path of the selected filter file.
   */
  private JTextField classesToShowFilePathTF;

  /**
   * Button that allows selecting the filter file.
   */
  private JButton browseClassesToShowFileButton;

  /**
   * Button that allows saving the filter file.
   */
  private JButton saveClassesToShowFileButton;
  
  
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
  protected HashSet<String> classesToHide;

  /**
   * List of ontology classes to be filtered out.
   */
  protected HashSet<String> classesToShow;
  
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

  
  private boolean readClassesToHideFile = false;
  private boolean readClassesToShowFile = false;
  
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
    classesToHide = new HashSet<String>();
    classesToShow = new HashSet<String>();
    childFeatureCB = new JCheckBox("Disable Child Feature");
    selectedTextAsPropertyValue = new JCheckBox("Selected Text As Property Value?");
    propertyName = new JTextField("alias",15);
    
    deleteConfirmationCB = new JCheckBox("Enable confirm deletion");
    addAllFeatureCaseSensitiveCB = new JCheckBox(
            "Case Sensitive \"Annotate All\" Feature");
    addAllFeatureCaseSensitiveCB.setSelected(true);

    classesToHideRB = new JRadioButton("Classes to ommit");
    classesToHideRB.addActionListener(new OntologyViewerOptionsActions());
    classesToHideFilePathTF = new JTextField(15);
    browseClassesToHideFileButton = new JButton("Browse");
    browseClassesToHideFileButton
            .addActionListener(new OntologyViewerOptionsActions());
    saveClassesToHideFileButton = new JButton("Save");
    saveClassesToHideFileButton
            .addActionListener(new OntologyViewerOptionsActions());

    JPanel temp6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    temp6.add(new JLabel("    File:"));
    temp6.add(classesToHideFilePathTF);
    temp6.add(browseClassesToHideFileButton);
    temp6.add(saveClassesToHideFileButton);

    classesToShowRB = new JRadioButton("Classes to show");
    classesToShowRB.addActionListener(new OntologyViewerOptionsActions());
    classesToShowFilePathTF = new JTextField(15);
    browseClassesToShowFileButton = new JButton("Browse");
    browseClassesToShowFileButton
            .addActionListener(new OntologyViewerOptionsActions());
    saveClassesToShowFileButton = new JButton("Save");
    saveClassesToShowFileButton
            .addActionListener(new OntologyViewerOptionsActions());

    JPanel temp8 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    temp8.add(new JLabel("    File:"));
    temp8.add(classesToShowFilePathTF);
    temp8.add(browseClassesToShowFileButton);
    temp8.add(saveClassesToShowFileButton);

    disableFilteringRB = new JRadioButton("Disable Filtering");
    
    ButtonGroup bg8 = new ButtonGroup();
    bg8.add(classesToShowRB);
    bg8.add(classesToHideRB);
    bg8.add(disableFilteringRB);
    disableFilteringRB.setSelected(true);
    
    JPanel temp7 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    temp7.add(new JLabel("    Annotation Property : "));
    temp7.add(propertyName);
    
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
    optionPanel.setLayout(new GridLayout(16, 1));
    optionPanel.add(childFeatureCB);
    optionPanel.add(deleteConfirmationCB);
    optionPanel.add(addAllFeatureCaseSensitiveCB);
    optionPanel.add(disableFilteringRB);
    optionPanel.add(classesToHideRB);
    optionPanel.add(temp6);
    optionPanel.add(classesToShowRB);
    optionPanel.add(temp8);
    optionPanel.add(selectedTextAsPropertyValue);
    optionPanel.add(temp7);
    
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
    scroller = new JScrollPane(optionPanel);
  }

  /**
   * Returns the panel for ontoOption Panel
   * 
   * @return
   */
  public Component getGUI() {
    return scroller;
//    JPanel myPanel = new JPanel();
//    myPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
//    myPanel.add(scroller);
//    return myPanel;
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
      else if(ae.getSource() == browseClassesToHideFileButton) {
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
              if(!newURL.equalsIgnoreCase(classesToHideFilePathTF.getText()
                      .trim())) {
                readClassesToHideFile = true;
              }
              else {
                readClassesToHideFile = false;
              }

              classesToHideFilePathTF.setText(newURL);
              classesToHideFileURL = selectedFile.toURI().toURL();
              if(isClassesToHideFilterOn()) {
                updateClassesToHide();
              }
            }
            catch(MalformedURLException me) {
              JOptionPane.showMessageDialog(MainFrame.getInstance(),
                      "Invalid URL");
              return;
            }
          }
        }
      }
      else if(ae.getSource() == saveClassesToHideFileButton) {
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

              BufferedWriter bw = new BufferedWriter(new FileWriter(
                      selectedFile));
              for(String s : classesToHide) {
                bw.write(s);
                bw.newLine();
              }
              bw.flush();
              bw.close();
            }
            catch(IOException ioe) {
              JOptionPane.showMessageDialog(MainFrame.getInstance(), ioe
                      .getMessage());
              throw new GateRuntimeException(ioe);
            }
          }
        }

      }
      else if(ae.getSource() == classesToHideRB) {
        updateClassesToHide();
      }
      else if(ae.getSource() == browseClassesToShowFileButton) {
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
              if(!newURL.equalsIgnoreCase(classesToShowFilePathTF.getText()
                      .trim())) {
                readClassesToShowFile = true;
              }
              else {
                readClassesToShowFile = false;
              }

              classesToShowFilePathTF.setText(newURL);
              classesToShowFileURL = selectedFile.toURI().toURL();
              if(isClassesToShowFilterOn()) {
                updateClassesToShow();
              }
            }
            catch(MalformedURLException me) {
              JOptionPane.showMessageDialog(MainFrame.getInstance(),
                      "Invalid URL");
              return;
            }
          }
        }
      }
      else if(ae.getSource() == saveClassesToShowFileButton) {
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

              BufferedWriter bw = new BufferedWriter(new FileWriter(
                      selectedFile));
              for(String s : classesToShow) {
                bw.write(s);
                bw.newLine();
              }
              bw.flush();
              bw.close();
            }
            catch(IOException ioe) {
              JOptionPane.showMessageDialog(MainFrame.getInstance(), ioe
                      .getMessage());
              throw new GateRuntimeException(ioe);
            }
          }
        }

      }
      else if(ae.getSource() == classesToShowRB) {
        updateClassesToShow();
      }
    }
  }

  public boolean isClassesToHideFilterOn() {
    return classesToHideRB.isSelected();
  }
  
  public boolean isClassesToShowFilterOn() {
    return classesToShowRB.isSelected();
  }

  
  private void updateClassesToHide() {
    try {
      if(classesToHideFileURL == null || !readClassesToHideFile) return;
      classesToHide.clear();
      BufferedReader br = new BufferedReader(new InputStreamReader(
              classesToHideFileURL.openStream()));
      String line = br.readLine();
      while(line != null) {
        classesToHide.add(line.trim());
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

  private void updateClassesToShow() {
    try {
      if(classesToShowFileURL == null || !readClassesToShowFile) return;
      classesToShow.clear();
      BufferedReader br = new BufferedReader(new InputStreamReader(
              classesToShowFileURL.openStream()));
      String line = br.readLine();
      while(line != null) {
        classesToShow.add(line.trim());
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
  public void setClassesToHideOn(boolean onOff) {
    if(classesToHideRB.isSelected() != onOff) {
      classesToHideRB.setSelected(onOff);
    }
    updateClassesToHide();
  }

  /**
   * Use this method to switch on and off the filter.
   * 
   * @param onOff
   */
  public void setClassesToShowOn(boolean onOff) {
    if(classesToShowRB.isSelected() != onOff) {
      classesToShowRB.setSelected(onOff);
    }
    updateClassesToShow();
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

  public void addToClassesToHide(HashSet<String> list) {
    if(classesToHide == null)
      classesToHide = new HashSet<String>();
    classesToHide.addAll(list);
    ontologyTreePanel.ontoTreeListener.refreshHighlights();
  }

  public void removeFromClassesToHide(HashSet<String> list) {
    if(classesToHide == null)
      classesToHide = new HashSet<String>();
    classesToHide.removeAll(list);
    ontologyTreePanel.ontoTreeListener.refreshHighlights();
  }

  public void addToClassesToShow(HashSet<String> list) {
    if(classesToShow == null)
      classesToShow = new HashSet<String>();
    classesToShow.addAll(list);
    ontologyTreePanel.ontoTreeListener.refreshHighlights();
  }

  public void removeFromClassesToShow(HashSet<String> list) {
    if(classesToShow == null)
      classesToShow = new HashSet<String>();
    classesToShow.removeAll(list);
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
    //String getSelected = (String)annotationSetsNamesCB.getSelectedItem();
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
  public URL getClassesToHideFileURL() {
    return classesToHideFileURL;
  }

  /**
   * Sets the filter file to be used.
   * 
   * @param classesToHideFileURL
   */
  public void setClassesToHideFileURL(URL filterFileURL) {
    this.classesToHideFileURL = filterFileURL;
    if(isClassesToHideFilterOn()) {
      updateClassesToHide();
    }
  }

  /**
   * Gets the URL of the filter file being used.
   * 
   * @return
   */
  public URL getClassesToShowFileURL() {
    return classesToShowFileURL;
  }

  /**
   * Sets the filter file to be used.
   * 
   * @param classesToHideFileURL
   */
  public void setClassesToShowFileURL(URL filterFileURL) {
    this.classesToShowFileURL = filterFileURL;
    if(isClassesToShowFilterOn()) {
      updateClassesToShow();
    }
  }
  
  
  /**
   * Gets a set of ontology classes disabled in the OCAT.
   * @return
   */
  public HashSet<String> getClassesToHide() {
    return classesToHide;
  }

  /**
   * Gets a set of ontology classes disabled in the OCAT.
   * @return
   */
  public HashSet<String> getClassesToShow() {
    return classesToShow;
  }  
  
  public String getPropertyName() {
    return selectedTextAsPropertyValue.isSelected() ? propertyName.getText().trim() : null; 
  }
  
  
  /**
   * This method should be called to specify the ontology classes that
   * should be disabled from the ocat.
   * 
   * @param classesToHide
   */
  public void setClassesToHide(
          HashSet<String> ontologyClassesToFilterOut) {
    // ok here we need to create a temporary file and add these classes
    // in it
    if(ontologyClassesToFilterOut == null) {
      ontologyClassesToFilterOut = new HashSet<String>();
    }

    try {
      File newFile = File.createTempFile("classesToHide", "tmp");
      BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
      for(String aClassName : ontologyClassesToFilterOut) {
        bw.write(aClassName);
        bw.newLine();
      }
      bw.flush();
      bw.close();
      readClassesToHideFile = true;
      classesToHideFilePathTF.setText(newFile.toURI().toURL().toString());
      classesToHideFileURL = newFile.toURI().toURL();
      setClassesToHideOn(true);
    }
    catch(IOException ioe) {
      throw new GateRuntimeException(
              "Not able to save the classes in a temporary file", ioe);
    }
  }

  /**
   * This method should be called to specify the ontology classes that
   * should be disabled from the ocat.
   * 
   * @param classesToHide
   */
  public void setClassesToShow(
          HashSet<String> ontologyClassesToShow) {
    // ok here we need to create a temporary file and add these classes
    // in it
    if(ontologyClassesToShow == null) {
      ontologyClassesToShow = new HashSet<String>();
    }

    try {
      File newFile = File.createTempFile("classesToShow", "tmp");
      BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
      for(String aClassName : ontologyClassesToShow) {
        bw.write(aClassName);
        bw.newLine();
      }
      bw.flush();
      bw.close();
      readClassesToShowFile = true;
      classesToShowFilePathTF.setText(newFile.toURI().toURL().toString());
      classesToShowFileURL = newFile.toURI().toURL();
      setClassesToShowOn(true);
    }
    catch(IOException ioe) {
      throw new GateRuntimeException(
              "Not able to save the classes in a temporary file", ioe);
    }
  }
  
  
  public boolean shouldShow(String aResourceName) {
    if(disableFilteringRB.isSelected())
      return true;
    
    if(classesToHideRB.isSelected() && classesToHide != null)
      return !classesToHide.contains(aResourceName);
    
    else if(classesToShow != null)
      return classesToShow.contains(aResourceName);
    
    return true;
  }

}
