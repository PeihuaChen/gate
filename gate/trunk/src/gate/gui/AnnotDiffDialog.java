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
  Map keyAnnotationSetMap = null;
  Map responseAnnotationSetMap = null;
  Map  typesMap = null;
  Set  falsePozTypes = null;
  MainFrame mainFrame = null;
  AnnotDiffDialog thisAnnotDiffDialog = null;

  // GUI components used in initGuiComponents()
  JComboBox keyDocComboBox = null;
  JComboBox responseDocComboBox = null;
  JComboBox typesComboBox = null;
  JComboBox falsePozTypeComboBox = null;
  JComboBox keyDocAnnotSetComboBox = null;
  JComboBox responseDocAnnotSetComboBox = null;
  JComboBox responseDocAnnotSetFalsePozComboBox = null;

  JLabel keyLabel = null;
  JLabel responseLabel = null;
  JLabel typesLabel = null;
  JLabel falsePozLabel = null;
  JLabel keyDocAnnotSetLabel = null;
  JLabel responseDocAnnotSetLabel = null;
  JLabel responseDocAnnotSetFalsePozLabel = null;

  JLabel weightLabel = null;
  JTextField weightTextField = null;

  JButton evalButton = null;
  AnnotationDiff annotDiff = null;

  protected JSplitPane jSplit;

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

    keyAnnotationSetMap = new TreeMap();
    responseAnnotationSetMap = new TreeMap();

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

    // init keyDocAnnotSetComboBox
    Set comboAsCont = new TreeSet(keyAnnotationSetMap.keySet());
    keyDocAnnotSetComboBox = new JComboBox(comboAsCont.toArray());
    keyDocAnnotSetComboBox.setEditable(false);
    keyDocAnnotSetComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    // init its label
    keyDocAnnotSetLabel = new JLabel("Select the KEY annotation set");
    keyDocAnnotSetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    keyDocAnnotSetLabel.setOpaque(true);
    keyDocAnnotSetLabel.setFont(
                  keyDocAnnotSetLabel.getFont().deriveFont(Font.BOLD));
    keyDocAnnotSetLabel.setForeground(Color.black);
    keyDocAnnotSetLabel.setBackground(Color.green);

    // init responseDocComboBox
    responseDocComboBox = new JComboBox(comboCont.toArray());
    responseDocComboBox.setSelectedIndex(0);
    responseDocComboBox.setEditable(false);
    responseDocComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    responseDocComboBox.setPreferredSize(dim);
    responseDocComboBox.setMaximumSize(dim);
    responseDocComboBox.setMinimumSize(dim);
    responseDocComboBox.setRenderer(new MyCellRenderer(Color.red, Color.black));
    // init its label
    responseLabel = new JLabel("Select the RESPONSE doc");
    responseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    responseLabel.setOpaque(true);
    responseLabel.setFont(responseLabel.getFont().deriveFont(Font.BOLD));
    responseLabel.setBackground(Color.red);
    responseLabel.setForeground(Color.black);

    // init responseDocAnnotSetComboBox
    comboAsCont = new TreeSet(responseAnnotationSetMap.keySet());
    responseDocAnnotSetComboBox = new JComboBox(comboAsCont.toArray());
    responseDocAnnotSetComboBox.setEditable(false);
    responseDocAnnotSetComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    // init its label
    responseDocAnnotSetLabel = new JLabel("Select the RESPONSE annot set");
    responseDocAnnotSetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    responseDocAnnotSetLabel.setOpaque(true);
    responseDocAnnotSetLabel.setFont(
                  responseDocAnnotSetLabel.getFont().deriveFont(Font.BOLD));
    responseDocAnnotSetLabel.setForeground(Color.black);
    responseDocAnnotSetLabel.setBackground(Color.red);

    // init responseDocAnnotSetFalsePozComboBox
    // This combo is used in calculating False Poz
    comboAsCont = new TreeSet(responseAnnotationSetMap.keySet());
    responseDocAnnotSetFalsePozComboBox = new JComboBox(comboAsCont.toArray());
    responseDocAnnotSetFalsePozComboBox.setEditable(false);
    responseDocAnnotSetFalsePozComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    // init its label
    responseDocAnnotSetFalsePozLabel = new JLabel("Select the RESPONSE annot set");
    responseDocAnnotSetFalsePozLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    responseDocAnnotSetFalsePozLabel.setOpaque(true);
    responseDocAnnotSetFalsePozLabel.setFont(
              responseDocAnnotSetFalsePozLabel.getFont().deriveFont(Font.BOLD));
    responseDocAnnotSetFalsePozLabel.setForeground(Color.black);
    responseDocAnnotSetFalsePozLabel.setBackground(Color.red);


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

    // Set initial dimmension for weightTextField
    Dimension d = new Dimension(weightLabel.getPreferredSize().width,
                                    weightTextField.getPreferredSize().height);
    weightTextField.setMinimumSize(d);
    weightTextField.setMaximumSize(d);
    weightTextField.setPreferredSize(d);

    // evaluate button
    evalButton = new JButton("Evaluate");
    evalButton.setFont(evalButton.getFont().deriveFont(Font.BOLD));

    // ***************************************************************
    // Put all those components at their place
    // ***************************************************************
    Box northBox = new Box(BoxLayout.X_AXIS);
    // Arange Key Document components
    Box currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(keyLabel);
    currentBox.add(keyDocComboBox);
    currentBox.add(Box.createVerticalStrut(10));
    currentBox.add(responseLabel);
    currentBox.add(responseDocComboBox);
    northBox.add(currentBox);

    northBox.add(Box.createRigidArea(new Dimension(10,0)));

    // Arange annotation set components
    currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(keyDocAnnotSetLabel);
    currentBox.add(keyDocAnnotSetComboBox);
    currentBox.add(Box.createVerticalStrut(10));
    currentBox.add(responseDocAnnotSetLabel);
    currentBox.add(responseDocAnnotSetComboBox);
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
    currentBox.add(Box.createVerticalStrut(10));
    currentBox.add(responseDocAnnotSetFalsePozLabel);
    currentBox.add(responseDocAnnotSetFalsePozComboBox);
    northBox.add(currentBox);

    northBox.add(Box.createRigidArea(new Dimension(10,0)));
    northBox.add(evalButton);

    initKeyAnnotSetNames();
    initResponseAnnotSetNames();
    initAnnotTypes();
    initAnnotTypesFalsePoz();

    this.getContentPane().setLayout(new BoxLayout(this.getContentPane(),
                                                            BoxLayout.Y_AXIS));
    Dimension maxDimm = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension newDim = new Dimension(2*maxDimm.width/3, 2*maxDimm.height/3);
    annotDiff.setMaximumSize(newDim);
    annotDiff.setMinimumSize(newDim);
    annotDiff.setPreferredSize(newDim);
    jSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                               new JScrollPane(northBox),
                               new JScrollPane(annotDiff));
    jSplit.setOneTouchExpandable(true);
    jSplit.setOpaque(true);
    jSplit.setAlignmentY(Component.TOP_ALIGNMENT);
    this.getContentPane().add(jSplit);
    this.pack();
    ////////////////////////////////
    // Center it on screen
    ///////////////////////////////
    Dimension ownerSize;
    Point ownerLocation;
    if(getOwner() == null){
      ownerSize = Toolkit.getDefaultToolkit().getScreenSize();
      ownerLocation = new Point(0, 0);
    }else{
      ownerSize = getOwner().getSize();
      ownerLocation = getOwner().getLocation();
      if(ownerSize.height == 0 ||
         ownerSize.width == 0 ||
         !getOwner().isVisible()){
        ownerSize = Toolkit.getDefaultToolkit().getScreenSize();
        ownerLocation = new Point(0, 0);
      }
    }
    //Center the window
    Dimension frameSize = getSize();
    if (frameSize.height > ownerSize.height)
      frameSize.height = ownerSize.height;
    if (frameSize.width > ownerSize.width)
      frameSize.width = ownerSize.width;
    setLocation(ownerLocation.x + (ownerSize.width - frameSize.width) / 2,
                ownerLocation.y + (ownerSize.height - frameSize.height) / 2);

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
                               new DiffRunner(),
                               "AnnotDiffDialog1");
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
        initKeyAnnotSetNames();
        initAnnotTypes();
      }// actionPerformed();
    });//addActionListener();

    responseDocComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        initResponseAnnotSetNames();
        initAnnotTypes();
      }// actionPerformed();
    });//addActionListener();

    keyDocAnnotSetComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        initAnnotTypes();
      }// actionPerformed();
    });//addActionListener();

    responseDocAnnotSetComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        initAnnotTypes();
      }// actionPerformed();
    });//addActionListener();

    responseDocAnnotSetFalsePozComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        initAnnotTypesFalsePoz();
      }// actionPerformed();
    });//addActionListener();

  }//initListeners();

  private void initAnnotTypesFalsePoz(){
    gate.Document responseDocument = null;
    responseDocument = (gate.Document) documentsMap.get(
                                 (String)responseDocComboBox.getSelectedItem());
    falsePozTypes.clear();
    if (responseDocument == null){
      // init falsePozTypes
      falsePozTypes.add("No annot.");
      DefaultComboBoxModel cbm = new DefaultComboBoxModel(falsePozTypes.toArray());
      falsePozTypeComboBox.setModel(cbm);
      return;
    }//End if

    // Fill the responseSet
    Set responseSet = null;
    if (responseDocAnnotSetFalsePozComboBox.getSelectedItem() == null ||
        responseAnnotationSetMap.get(
                   responseDocAnnotSetFalsePozComboBox.getSelectedItem())==null)
        responseSet = new HashSet(
                              responseDocument.getAnnotations().getAllTypes());
    else{
      // Get all types of annotation from the selected responseAnnotationSet
      AnnotationSet as = (AnnotationSet) responseAnnotationSetMap.get(
                         responseDocAnnotSetFalsePozComboBox.getSelectedItem());
      responseSet = new HashSet(as.getAllTypes());
    }// End if

    // Init falsePozTypes
    falsePozTypes.addAll(responseSet);
    if (falsePozTypes.isEmpty())
      falsePozTypes.add("No annot.");
    DefaultComboBoxModel cbm = new DefaultComboBoxModel(falsePozTypes.toArray());
    falsePozTypeComboBox.setModel(cbm);
  }//initAnnotTypesFalsePoz();

  /** Reads the selected keyDocument + the selected responseDocument and
    * also reads the selected annotation sets from Key and response and
    * intersects the annotation types. The result is the typesComboBox which
    * is filled with the intersected types.
    */
  private void initAnnotTypes(){
    gate.Document keyDocument = null;
    gate.Document responseDocument = null;

    keyDocument = (gate.Document) documentsMap.get(
                                 (String)keyDocComboBox.getSelectedItem());
    responseDocument = (gate.Document) documentsMap.get(
                                 (String)responseDocComboBox.getSelectedItem());

    typesMap.clear();

    if (keyDocument == null || responseDocument == null){
      // init types map with Type,AnnotationSchema pairs
      typesMap.put("No annot.",null);
      ComboBoxModel cbm = new DefaultComboBoxModel(typesMap.keySet().toArray());
      typesComboBox.setModel(cbm);
      return;
    }//End if

    // Do intersection for annotation types...
    // First fill the keySet;
    Set keySet = null;
    if (keyDocAnnotSetComboBox.getSelectedItem()== null ||
        keyAnnotationSetMap.get(keyDocAnnotSetComboBox.getSelectedItem())==null)
      // First add to the keySet all annotations from default set
      keySet = new HashSet(keyDocument.getAnnotations().getAllTypes());
    else{
      // Get all types of annotation from the selected keyAnnotationSet
      AnnotationSet as = (AnnotationSet) keyAnnotationSetMap.get(
                                  keyDocAnnotSetComboBox.getSelectedItem());
      keySet = new HashSet(as.getAllTypes());
    }// End if

    // Do the same thing for the responseSet
    // Fill the responseSet
    Set responseSet = null;
    if (responseDocAnnotSetComboBox.getSelectedItem() == null ||
        responseAnnotationSetMap.get(
                          responseDocAnnotSetComboBox.getSelectedItem())==null)
        responseSet = new HashSet(
                              responseDocument.getAnnotations().getAllTypes());
    else{
      // Get all types of annotation from the selected responseAnnotationSet
      AnnotationSet as = (AnnotationSet) responseAnnotationSetMap.get(
                                 responseDocAnnotSetComboBox.getSelectedItem());
      responseSet = new HashSet(as.getAllTypes());
    }// End if

    // DO intersection between keySet & responseSet
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
  }//initAnnotTypes();

  /** Reads the selected keyDocument + the selected responseDocument and fill
    * the two combo boxes called keyDocAnnotSetComboBox and
    * responseDocAnnotSetComboBox.
    */
  private void initKeyAnnotSetNames(){
    gate.Document keyDocument = null;
    keyDocument = (gate.Document) documentsMap.get(
                                 (String)keyDocComboBox.getSelectedItem());
    keyAnnotationSetMap.clear();

    if (keyDocument != null){
      Map namedAnnotSets = keyDocument.getNamedAnnotationSets();
      if (namedAnnotSets != null)
        keyAnnotationSetMap.putAll(namedAnnotSets);
      keyAnnotationSetMap.put("Default set",null);
      DefaultComboBoxModel cbm = new DefaultComboBoxModel(
                                        keyAnnotationSetMap.keySet().toArray());
      keyDocAnnotSetComboBox.setModel(cbm);
    }// End if
  }//initKeyAnnotSetNames();

  /** Reads the selected responseDocument and fill
    * the combo box called responseDocAnnotSetFalsePozComboBox as well as
    * responseDocAnnotSetComboBox.
    */
  private void initResponseAnnotSetNames(){
    gate.Document responseDocument = null;
    responseDocument = (gate.Document) documentsMap.get(
                                 (String)responseDocComboBox.getSelectedItem());
    responseAnnotationSetMap.clear();

    if (responseDocument != null){
      Map namedAnnotSets = responseDocument.getNamedAnnotationSets();
      if (namedAnnotSets != null)
        responseAnnotationSetMap.putAll(namedAnnotSets);
      responseAnnotationSetMap.put("Default set",null);
      DefaultComboBoxModel cbm = new DefaultComboBoxModel(
                                  responseAnnotationSetMap.keySet().toArray());
      responseDocAnnotSetComboBox.setModel(cbm);
      cbm = new DefaultComboBoxModel(
                                  responseAnnotationSetMap.keySet().toArray());
      responseDocAnnotSetFalsePozComboBox.setModel(cbm);
    }// End if
  }//initResponseAnnotSetNames();

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

  /** It returns the selected key AnnotationSet name. It returns null for the
    * default annotation set.
    */
  public String getSelectedKeyAnnotationSetName(){
   String asName = (String) keyDocAnnotSetComboBox.getSelectedItem();
   if ("Default set".equals(asName)) return null;
   else return asName;
  }//getSelectedKeyAnnotationSetName()

  /** It returns the selected response AnnotationSet name.It returns null for the
    * default annotation set.
    */
  public String getSelectedResponseAnnotationSetName(){
   String asName = (String) responseDocAnnotSetComboBox.getSelectedItem();
   if ("Default set".equals(asName)) return null;
   else return asName;
  }//getSelectedResponseAnnotationSetName()

  /** It returns the selected response AnnotationSet name for False Poz.
    * It returns null for the default annotation set.
    */
  public String getSelectedResponseAnnotationSetNameFalsePoz(){
   String asName = (String) responseDocAnnotSetFalsePozComboBox.getSelectedItem();
   if ("Default set".equals(asName)) return null;
   else return asName;
  }//getSelectedResponseAnnotationSetNameFalsePoz()

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
      annotDiff.setResponseDocument(
                          thisAnnotDiffDialog.getSelectedResponseDocument());
      annotDiff.setAnnotationSchema(thisAnnotDiffDialog.getSelectedSchema());
      annotDiff.setKeyAnnotationSetName(
                      thisAnnotDiffDialog.getSelectedKeyAnnotationSetName());
      annotDiff.setResponseAnnotationSetName(
                  thisAnnotDiffDialog.getSelectedResponseAnnotationSetName());
      annotDiff.setResponseAnnotationSetNameFalsePoz(
            thisAnnotDiffDialog.getSelectedResponseAnnotationSetNameFalsePoz());

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
