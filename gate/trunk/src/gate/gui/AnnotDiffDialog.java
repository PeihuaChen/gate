/*  AnnotDiffDialog.java
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 7/03/2001
 *
 *  $Id$
 *
 */
package gate.gui;

import gate.*;
import gate.annotation.*;
import gate.persist.*;
import gate.util.*;
import gate.creole.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/** This class wraps the {@link gate.annotation.AnnotationDiff} one. It adds the
  * the GUI functionality needed to set up params for AnnotationDiff and also
  * adds the AnnotationDiff as a tool in GATE.
  */
class AnnotDiffDialog extends JFrame {
  // Local data needed in initLocalData() method

  /** A map from documentName 2 GATE document It is used to display names in
    * combo boxes
    */
  Map  documentsMap = null;
  Map  typesMap = null;
  Set  falsePozTypes = null;
  MainFrame mainFrame = null;
  AnnotDiffDialog thisAnnotDiffDialog = null;

  // GUI components used in initGuiComponents()
  JComboBox keyDocComboBox = null;
  JComboBox responseDocComboBox = null;
  JComboBox typesComboBox = null;
  JComboBox falsePozTypeComboBox = null;

  JLabel keyLabel = null;
  JLabel responseLabel = null;
  JLabel typesLabel = null;
  JLabel falsePozLabel = null;

  JLabel weightLabel = null;
  JTextField weightTextField = null;

  JButton evalButton = null;
  AnnotationDiff annotDiff = null;

  /** Constructs an annotDiffDialog object having as parent aMainFrame
    * @param aMainFrame the parent frame for this AnnotDiffDialog. If can be
    * <b>null</b>, meaning no parent.
    */
  public AnnotDiffDialog(MainFrame aMainFrame){
    mainFrame = aMainFrame;
    thisAnnotDiffDialog = this;
    initLocalData();
    initGuiComponents();
    initListeners();
  }//AnnotDiffDialog

  /** This method is called when adding or removing a document*/
  public void updateData(){
    documentsMap = null;
    typesMap = null;
    falsePozTypes = null;
    this.removeAll();

    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        initLocalData();
        initGuiComponents();
        initListeners();
      }
    });
  }//updateData()

  /** Initialises the data needed to set up {@link gate.annotation.AnnotationDiff}
    * GUI components will be build using this data.
    */
  public void initLocalData(){
    annotDiff = new AnnotationDiff();
    // Get all available documents and construct the documentsMap
    // (docName, gate.Document) pairs
    documentsMap = new HashMap();

    CreoleRegister registry =  Gate.getCreoleRegister();
    ResourceData resourceData =
                        (ResourceData)registry.get("gate.corpora.DocumentImpl");
    if(resourceData != null && !resourceData.getInstantiations().isEmpty()){
      java.util.List instantiations = resourceData.getInstantiations();
      Iterator iter = instantiations.iterator();
      while (iter.hasNext()){
        Resource resource = (Resource) iter.next();
        String docName = resource.getName ();
        gate.Document doc = (Document) resource;
        // add it to the Map
        documentsMap.put(docName,doc);
      }// while
    }else documentsMap.put("No docs found",null);

    typesMap = new TreeMap();
    // init types map with Type,AnnotationSchema pairs
    typesMap.put("No annot.",null);

    // init falsePozTypes
    falsePozTypes = new TreeSet();
    falsePozTypes.add("No annot.");
  }// initLocalData

  /**
    * This method initializes the GUI components. Data is loaded from localData
    * fields.
    */
  public void initGuiComponents(){

    //Initialise GUI components
    //this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.getContentPane().setLayout(new BorderLayout());
    // init keyDocComboBox
    Set comboCont = new TreeSet(documentsMap.keySet());
    keyDocComboBox = new JComboBox(comboCont.toArray());
    keyDocComboBox.setSelectedIndex(0);
    keyDocComboBox.setEditable(false);
    keyDocComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    Dimension dim = new Dimension(150,keyDocComboBox.getPreferredSize().height);
    keyDocComboBox.setPreferredSize(dim);
    keyDocComboBox.setMaximumSize(dim);
    keyDocComboBox.setMinimumSize(dim);
    keyDocComboBox.setRenderer(new MyCellRenderer(Color.green, Color.black));
    // init its label
    keyLabel = new JLabel("Select the KEY doc");
    keyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    keyLabel.setOpaque(true);
    keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD));
    keyLabel.setForeground(Color.black);
    keyLabel.setBackground(Color.green);

    // init responseDocComboBox
    responseDocComboBox = new JComboBox(comboCont.toArray());
    responseDocComboBox.setSelectedIndex(0);
    responseDocComboBox.setEditable(false);
    responseDocComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    responseDocComboBox.setPreferredSize(dim);
    responseDocComboBox.setMaximumSize(dim);
    responseDocComboBox.setMinimumSize(dim);
    responseDocComboBox.setRenderer(new MyCellRenderer(Color.red, Color.white));
    // init its label
    responseLabel = new JLabel("Select the RESPONSE doc");
    responseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    responseLabel.setOpaque(true);
    responseLabel.setFont(responseLabel.getFont().deriveFont(Font.BOLD));
    responseLabel.setBackground(Color.red);
    responseLabel.setForeground(Color.white);

    // init typesComboBox
    Vector typesCont = new Vector(typesMap.keySet());
    Collections.sort(typesCont);
    typesComboBox = new JComboBox(typesCont);
    typesComboBox.setEditable(false);
    typesComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    // init its label
    typesLabel = new JLabel("Select annot. type");
    typesLabel.setFont(typesLabel.getFont().deriveFont(Font.BOLD));
    typesLabel.setOpaque(true);
    typesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    // init falsePozTypeComboBox
    falsePozTypeComboBox = new JComboBox(falsePozTypes.toArray());
    falsePozTypeComboBox.setEditable(false);
    falsePozTypeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    // init its label
    falsePozLabel = new JLabel("Select annot. type for FalsePoz");
    falsePozLabel.setFont(falsePozLabel.getFont().deriveFont(Font.BOLD));
    falsePozLabel.setOpaque(true);
    falsePozLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    // init weightTextField
    weightTextField = new JTextField(
                              (new Double(AnnotationDiff.weight)).toString());
    weightTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
    // init its label
    weightLabel = new JLabel("Weight for F-Measure");
    weightLabel.setFont(falsePozLabel.getFont().deriveFont(Font.BOLD));
    weightLabel.setOpaque(true);
    weightLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    // evaluate button
    evalButton = new JButton("Evaluate");
    evalButton.setFont(evalButton.getFont().deriveFont(Font.BOLD));

    // Put all those components at their place
    Box northBox = new Box(BoxLayout.X_AXIS);
    // Arange Key Document components
    Box currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(keyLabel);
    currentBox.add(keyDocComboBox);
    northBox.add(currentBox);

    northBox.add(Box.createRigidArea(new Dimension(10,0)));

    // Arange Response Document components
    currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(responseLabel);
    currentBox.add(responseDocComboBox);
    northBox.add(currentBox);

    northBox.add(Box.createRigidArea(new Dimension(10,0)));

    // Arange annotation types components
    currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(typesLabel);
    currentBox.add(typesComboBox);
    northBox.add(currentBox);

    northBox.add(Box.createRigidArea(new Dimension(10,0)));

    // Arange F-Measure weight components
    currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(weightLabel);
    currentBox.add(weightTextField);
    northBox.add(currentBox);

    northBox.add(Box.createRigidArea(new Dimension(10,0)));

    // Arange false poz components
    currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(falsePozLabel);
    currentBox.add(falsePozTypeComboBox);
    northBox.add(currentBox);

    northBox.add(Box.createRigidArea(new Dimension(10,0)));
    northBox.add(evalButton);

    initAnnotTypes();
    this.getContentPane().add(northBox,BorderLayout.NORTH);
    this.getContentPane().add(annotDiff,BorderLayout.CENTER);
    this.pack();
  }//initGuiComponents

  /** This method is called when the user want to close the tool. See
    * initListeners() method for more details
    */
  void this_windowClosing(WindowEvent e){
    this.setVisible(false);
  }//this_windowClosing();

  /** This method starts AnnotationDiff tool in a separate thread.*/
  private void doDiff(){
    try{
      Double d = new Double(thisAnnotDiffDialog.getCurrentWeight());
      AnnotationDiff.weight = d.doubleValue();
    }catch (NumberFormatException e){
        JOptionPane.showMessageDialog(thisAnnotDiffDialog,
                     "The weight for F-Measure should be a double !",
                     "Annotation Diff initialization error !",
                     JOptionPane.ERROR_MESSAGE);
        return;
    }// End try
    Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                               new DiffRunner());
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }//doDiff();

  /**This one initializes the listeners fot the GUI components */
  public void initListeners(){

    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    this.addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        this_windowClosing(e);
      }// windowClosing();
    });// addWindowListener();

    evalButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
         thisAnnotDiffDialog.doDiff();
      }// actionPerformed();
    });//addActionListener();

    keyDocComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        initAnnotTypes();
      }// actionPerformed();
    });//addActionListener();

    responseDocComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        initAnnotTypes();
      }// actionPerformed();
    });//addActionListener();
  }//initListeners();

  /** Reads the selected keyDocument + the selected responseDocument, does the
    * intersection of the two annot sets and fill the two combo boxes called
    * typesComboBox and falsePozTypeComboBox.
    */
  private void initAnnotTypes(){
    gate.Document keyDocument = null;
    gate.Document responseDocument = null;

    keyDocument = (gate.Document) documentsMap.get(
                                 (String)keyDocComboBox.getSelectedItem());
    responseDocument = (gate.Document) documentsMap.get(
                                 (String)responseDocComboBox.getSelectedItem());

    typesMap.clear();
    falsePozTypes.clear();

    if (keyDocument == null || responseDocument == null){
      // init types map with Type,AnnotationSchema pairs
      typesMap.put("No annot.",null);
      ComboBoxModel cbm = new DefaultComboBoxModel(typesMap.keySet().toArray());
      typesComboBox.setModel(cbm);

      // init falsePozTypes
      falsePozTypes.add("No annot.");
      cbm = new DefaultComboBoxModel(falsePozTypes.toArray());
      falsePozTypeComboBox.setModel(cbm);
      return;
    }//End if

    // Do intersection for annotation types...
    // First add to the keySet all annotations from default set
    Set keySet = new HashSet(keyDocument.getAnnotations().getAllTypes());
    // Now add all the annotation from the named sets
    Map namedAnnotSets = keyDocument.getNamedAnnotationSets();
    if (namedAnnotSets != null){
      Iterator iter = namedAnnotSets.values().iterator();
      while (iter.hasNext()){
          AnnotationSet annotSet = (AnnotationSet) iter.next();
          keySet.addAll(annotSet.getAllTypes());
      }// End while
    }// End if
    // Do the same thing for the responseSet too
    Set responseSet = new HashSet(
                              responseDocument.getAnnotations().getAllTypes());
    // Add all the annotation from the named sets
    namedAnnotSets = responseDocument.getNamedAnnotationSets();
    if (namedAnnotSets != null){
      Iterator iter = namedAnnotSets.values().iterator();
      while (iter.hasNext()){
        AnnotationSet annotSet = (AnnotationSet) iter.next();
        responseSet.addAll(annotSet.getAllTypes());
      }// End while
    }// End if

    keySet.retainAll(responseSet);
    Set intersectSet = keySet;

    Iterator iter = intersectSet.iterator();
    while (iter.hasNext()){
      String annotName = (String) iter.next();

      AnnotationSchema schema = new AnnotationSchema();
      schema.setAnnotationName(annotName);

      typesMap.put(annotName,schema);
    }// while

    if (typesMap.isEmpty())
      typesMap.put("No annot.",null);

    DefaultComboBoxModel cbm = new DefaultComboBoxModel(
                                              typesMap.keySet().toArray());
    typesComboBox.setModel(cbm);

    // Init falsePozTypes
    falsePozTypes.addAll(responseSet);
    if (falsePozTypes.isEmpty())
      falsePozTypes.add("No annot.");
    cbm = new DefaultComboBoxModel(falsePozTypes.toArray());
    falsePozTypeComboBox.setModel(cbm);
  }//initAnnotTypes();

  /** It returns the selected KEY gate.Document*/
  public gate.Document getSelectedKeyDocument(){
    return (gate.Document) documentsMap.get(
                                (String)keyDocComboBox.getSelectedItem());
  }//getSelectedKeyDocument

  /** It returns the selected RESPONSE gate.Document*/
  public gate.Document getSelectedResponseDocument(){
    return (gate.Document) documentsMap.get(
                                (String)responseDocComboBox.getSelectedItem());
  }//getSelectedResponseDocument

  /** It returns the selected SCHEMA  */
  public AnnotationSchema getSelectedSchema(){
    return (AnnotationSchema) typesMap.get(
                                (String)typesComboBox.getSelectedItem());
  }//getSelectedSchema

  /** It returns the current weight  */
  public String getCurrentWeight(){
    return weightTextField.getText();
  }//getCurrentWeight

  /** It returns the selected Annotation to calculate the False Pozitive  */
  public String getSelectedFalsePozAnnot(){
    return (String) falsePozTypeComboBox.getSelectedItem();
  }//getSelectedFalsePozAnnot

  /**  Inner class that adds a tool tip to the combo boxes with key and response
    *  documents. The tool tip represents the full path of the documents.
    */
  class MyCellRenderer extends JLabel implements ListCellRenderer {

     Color background = null;
     Color foreground = null;
     /** Constructs a renderer*/
     public MyCellRenderer(Color aBackground, Color aForeground){
         setOpaque(true);
         background = aBackground;
         foreground = aForeground;
     }// MyCellRenderer();

     /** This method is overridden in order to implement the needed behaviour*/
     public Component getListCellRendererComponent(
                                                   JList list,
                                                   Object value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus){
         // should be done only once...
         ToolTipManager.sharedInstance().registerComponent(list);
         setText(value.toString());
         setBackground(isSelected ? background : Color.white);
         setForeground(isSelected ? foreground : Color.black);
         if (isSelected)
             list.setToolTipText(value.toString());
         return this;
     }// getListCellRendererComponent()
  }//MyCellRenderer

  /**Inner class used to run an annot. diff in a new thread*/
  class DiffRunner implements Runnable{
    /** Constructor */
    public DiffRunner(){}// DiffRuner
    /** This method is overridden in order to implement the needed behaviour*/
    public void run(){
      annotDiff.setKeyDocument(thisAnnotDiffDialog.getSelectedKeyDocument());
      annotDiff.setResponseDocument(thisAnnotDiffDialog.getSelectedResponseDocument());
      annotDiff.setAnnotationSchema(thisAnnotDiffDialog.getSelectedSchema());
      String falsePozAnnot = thisAnnotDiffDialog.getSelectedFalsePozAnnot();
      if ("No annot.".equals(falsePozAnnot))
            falsePozAnnot = null;
      annotDiff.setAnnotationTypeForFalsePositive(falsePozAnnot);
      try{
        annotDiff.init();
      } catch (ResourceInstantiationException e){
        JOptionPane.showMessageDialog(thisAnnotDiffDialog,
                     e.getMessage() + "\n Annotation diff stopped !",
                     "Annotation Diff initialization error !",
                     JOptionPane.ERROR_MESSAGE);
      }finally {
        SwingUtilities.invokeLater(new Runnable(){
          public void run(){
            doLayout();
            pack();
          }// run
        });//invokeLater
      }// End try
    }// run();
  }//DiffRunner
}//AnnotDiffDialog
