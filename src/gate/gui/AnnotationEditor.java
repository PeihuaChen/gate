/*
 *  AnnotationEditor.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 13/11/2000
 *
 *  $Id$
 *
 */
package gate.gui;

import gate.*;

import javax.swing.*;
import java.beans.*;
import java.util.*;

public class AnnotationEditor extends JPanel implements VisualResource {

  public AnnotationEditor() {
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Annotation Editor Test");
    AnnotationEditor editor = new AnnotationEditor();
    editor.init();
    frame.getContentPane().add(editor.getGUI());
    frame.pack();
    frame.setVisible(true);
  }

  public Resource init(){
    //listen for our own properties change events
    this.addPropertyChangeListener("document", new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
      }
    });
    this.addPropertyChangeListener("visibleAnnotationSets",
                                   new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
      }
    });
    this.addPropertyChangeListener("annotationSchemas",
                                   new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
      }
    });
    //initialise GUI components
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    textPane = new JTextPane();
    textScroll = new JScrollPane(textPane);

    annotationsTableSorter = new TableSorter();
    annotationsTable = new JTable(annotationsTableSorter);
    annotationsTableSorter.addMouseListenerToHeaderInTable(annotationsTable);
    tableScroll = new JScrollPane(annotationsTable);

    leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                               textScroll, tableScroll);

    this.add(leftSplit);

    annotationSetsList = new JList();
    listScroll = new JScrollPane(annotationSetsList);
    buttonsBox = Box.createVerticalBox();
    buttonsScroll = new JScrollPane(buttonsBox);

    rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                listScroll, buttonsScroll);
    this.add(rightSplit);

    //data initialisation
    if(document != null){
      //set the text
      textPane.setText(document.getContent().toString());
      if(visibleAnnotationSets == null){
        visibleAnnotationSets = document.getNamedAnnotationSets().keySet();
        //add the key for the default annotation set
        visibleAnnotationSets.add("");
      }
      Iterator vasIter = visibleAnnotationSets.iterator();
      String asName;
      vAS = new HashSet();
      while(vasIter.hasNext()){
        asName = (String)vasIter.next();
        if(asName.equals("")) vAS.add(document.getAnnotations());
        else vAS.add(document.getAnnotations(asName));
      }

    }//if(document != null)

    return this;
  }

  public JComponent getGUI(){
    return this;
  }

  //no doc required: javadoc will copy it from the interface
  public FeatureMap getFeatures(){
    return features;
  }

  public void setFeatures(FeatureMap features){
    this.features = features;
  }

  public void setVisibleAnnotationSets(java.util.Set newVisibleAnnotationSets) {
    java.util.Set  oldVisibleAnnotationSets = visibleAnnotationSets;
    visibleAnnotationSets = newVisibleAnnotationSets;
    propertyChangeListeners.firePropertyChange("visibleAnnotationSets",
                                               oldVisibleAnnotationSets,
                                               newVisibleAnnotationSets);
  }

  public java.util.Set getVisibleAnnotationSets() {
    return visibleAnnotationSets;
  }

  public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
    super.removePropertyChangeListener(l);
    propertyChangeListeners.removePropertyChangeListener(l);
  }

  public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
    super.addPropertyChangeListener(l);
    propertyChangeListeners.addPropertyChangeListener(l);
  }

  public void setDocument(gate.Document newDocument) {
    gate.Document  oldDocument = document;
    document = newDocument;
    propertyChangeListeners.firePropertyChange("document", oldDocument,
                                               newDocument);
  }

  public gate.Document getDocument() {
    return document;
  }

  public void setAnnotationSchemas(java.util.Set newAnnotationSchemas) {
    java.util.Set  oldAnnotationSchemas = annotationSchemas;
    annotationSchemas = newAnnotationSchemas;
    propertyChangeListeners.firePropertyChange("annotationSchemas",
                                               oldAnnotationSchemas,
                                               newAnnotationSchemas);
  }

  public java.util.Set getAnnotationSchemas() {
    return annotationSchemas;
  }

  //properties
  protected FeatureMap features;
  //set of String
  /**
   * The names of the annotation sets that should be visible.
   */
  private java.util.Set visibleAnnotationSets;
  private transient PropertyChangeSupport propertyChangeListeners =
                                          new PropertyChangeSupport(this);
  private gate.Document document;
  private java.util.Set annotationSchemas;

  //GUI components
  JTextPane textPane;
  JScrollPane textScroll;
  TableSorter annotationsTableSorter;
  JTable annotationsTable;
  JScrollPane tableScroll;
  JSplitPane leftSplit;

  JList annotationSetsList;
  JScrollPane listScroll;
  Box buttonsBox;
  JScrollPane buttonsScroll;
  JSplitPane rightSplit;

  //misc members
  /**
   * A set containg the actual annotation sets that should be visible.
   */
  protected Set vAS;

}