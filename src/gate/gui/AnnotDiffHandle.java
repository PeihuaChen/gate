/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU 28/01/2001
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

public class AnnotDiffHandle extends ResourceHandle{
  JPopupMenu popup = null;
  SmallView smallView = null;
  AnnotationDiff annotDiff = null;
  MainFrame mainFrame = null;
  AnnotDiffHandle myself = null;

  public AnnotDiffHandle(MainFrame aMainFrame){
    mainFrame = aMainFrame;
    myself = this;
    annotDiff = new AnnotationDiff();
    smallView = new SmallView();
    this.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/lr.gif")));
    popup = new JPopupMenu();
    popup.add(new CloseAction());
    this.setPopup(popup);
  }//AnnotDiffHandle


  public JComponent getLargeView(){
    return annotDiff;
  }// getLargeView()

  public JComponent getSmallView(){
    return smallView;
  }// getSmallView()


  class CloseAction extends AbstractAction{
    public CloseAction(){
      super("Close");
    }// CloseAction

    public void actionPerformed(ActionEvent e){
      mainFrame.remove(myself);
    }// actionPerformed();
  }//CloseAction

  class SmallView extends JPanel{
    // Local data
    Map  documentsMap = null;
    Map  typesMap = null;
    Vector  falsePozTypes = null;

    // GUI components
    JComboBox keyDocComboBox = null;
    JComboBox responseDocComboBox = null;
    JComboBox typesComboBox = null;
    JComboBox falsePozTypeComboBox = null;

    JLabel keyLabel = null;
    JLabel responseLabel = null;
    JLabel typesLabel = null;
    JLabel falsePozLabel = null;

    JButton evalButton = null;

    /**
      * Constructor
      */
    public SmallView(){
      initLocalData();
      initGuiComponents();
      initListeners();
    }// EvaluationEditor

    /** This method is called when addind or removing a document*/
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

    /**Initialises the data (the loaded resources)*/
    public void initLocalData(){
      //Get all available documents and construct the documentsMap
      // DocName, Document pairs
      documentsMap = new HashMap();

      CreoleRegister registry =  Gate.getCreoleRegister();
      ResourceData resourceData = (ResourceData)registry.get("gate.corpora.DocumentImpl");

      if(resourceData != null && !resourceData.getInstantiations().isEmpty()){
        java.util.List instantiations = resourceData.getInstantiations();
        Iterator iter = instantiations.iterator();
        while (iter.hasNext()){
          Resource resource = (Resource) iter.next();
          String docName = (String) resource.getFeatures().get("NAME");
          gate.Document doc = (Document) resource;
          // add it to the Map
          documentsMap.put(docName,doc);
        }// while
      }else documentsMap.put("No docs found",null);

      typesMap = new HashMap();
      // init types map with Type,AnnotationSchema pairs
      typesMap.put("No annot.",null);

      // init falsePozTypes
      falsePozTypes = new Vector();
      falsePozTypes.add("No annot.");

    }// initLocalData

    /**
      * This method initializes the GUI components
      */
    public void initGuiComponents(){

      //Initialise GUI components
      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      // init keyDocComboBox
      Vector comboCont = new Vector(documentsMap.keySet());
      Collections.sort(comboCont);
      keyDocComboBox = new JComboBox(comboCont);
      keyDocComboBox.setEditable(false);
      keyDocComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      Dimension dim = new Dimension(150,keyDocComboBox.getPreferredSize().height);
      keyDocComboBox.setPreferredSize(dim);
      keyDocComboBox.setMaximumSize(dim);
      keyDocComboBox.setMinimumSize(dim);
      keyDocComboBox.setRenderer(new MyCellRenderer(Color.green, Color.black));
      // init its label
      keyLabel = new JLabel("KEY document");
      keyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      keyLabel.setOpaque(true);
      keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD));
      keyLabel.setForeground(Color.black);
      keyLabel.setBackground(Color.green);

      // init responseDocComboBox
      responseDocComboBox = new JComboBox(comboCont);
      responseDocComboBox.setEditable(false);
      responseDocComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      responseDocComboBox.setPreferredSize(dim);
      responseDocComboBox.setMaximumSize(dim);
      responseDocComboBox.setMinimumSize(dim);
      responseDocComboBox.setRenderer(new MyCellRenderer(Color.red, Color.white));
      // init its label
      responseLabel = new JLabel("RESPONSE document");
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
      typesLabel = new JLabel("Annot. type");
      typesLabel.setFont(typesLabel.getFont().deriveFont(Font.BOLD));
      typesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

      // init falsePozTypeComboBox
      Collections.sort(falsePozTypes);
      falsePozTypeComboBox = new JComboBox(falsePozTypes);
      falsePozTypeComboBox.setEditable(false);
      falsePozTypeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
      // init its label
      falsePozLabel = new JLabel("Annot. type for FalsePoz");
      falsePozLabel.setFont(falsePozLabel.getFont().deriveFont(Font.BOLD));
      falsePozLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

      // evaluate button
      evalButton = new JButton("Do diff");
      evalButton.setFont(evalButton.getFont().deriveFont(Font.BOLD));

      // Put all those components at their place
  //    JPanel mainBox = new JPanel();
  //    mainBox.setLayout(new FlowLayout(BoxLayout.X_AXIS));
      Box mainBox = new Box(BoxLayout.Y_AXIS);

      Box currentBox = new Box(BoxLayout.Y_AXIS);
      currentBox.add(keyLabel);
      currentBox.add(keyDocComboBox);
      mainBox.add(currentBox);

      mainBox.add(Box.createRigidArea(new Dimension(0,10)));

      currentBox = new Box(BoxLayout.Y_AXIS);
      currentBox.add(responseLabel);
      currentBox.add(responseDocComboBox);
      mainBox.add(currentBox);

      mainBox.add(Box.createRigidArea(new Dimension(0,10)));

      currentBox = new Box(BoxLayout.Y_AXIS);
      currentBox.add(typesLabel);
      currentBox.add(typesComboBox);
      mainBox.add(currentBox);

      mainBox.add(Box.createRigidArea(new Dimension(0,10)));

      currentBox = new Box(BoxLayout.Y_AXIS);
      currentBox.add(falsePozLabel);
      currentBox.add(falsePozTypeComboBox);
      mainBox.add(currentBox);

      mainBox.add(Box.createRigidArea(new Dimension(0,20)));

      mainBox.add(evalButton);

      // Add a space
      this.add(Box.createRigidArea(new Dimension(0,5)));
      this.add(mainBox);
      this.add(Box.createRigidArea(new Dimension(0,5)));
    }//initGuiComponents

    /**
      * This one initializes the listeners fot the GUI components
      */
    public void initListeners(){

      evalButton.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(ActionEvent e) {
        }
      });

      keyDocComboBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          initAnnotTypes();
        }
      });

      responseDocComboBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          initAnnotTypes();
        }
      });
    }//initListeners

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
        cbm = new DefaultComboBoxModel(falsePozTypes);
        falsePozTypeComboBox.setModel(cbm);
        return;
      }// if

      // Do intersection for annotation types...
      Set keySet = new HashSet(keyDocument.getAnnotations().getAllTypes());
      Set responseSet = new HashSet(
                                responseDocument.getAnnotations().getAllTypes());

      keySet.retainAll(responseSet);
      Set intersectSet = keySet;

      Iterator iter = intersectSet.iterator();
      while (iter.hasNext()){
        String annotName = (String) iter.next();

        AnnotationSchema schema = new AnnotationSchema();
        schema.setAnnotationName(annotName);

        typesMap.put(annotName,schema);
      }// while

      typesMap.put("No annot.",null);
      DefaultComboBoxModel cbm = new DefaultComboBoxModel(
                                                typesMap.keySet().toArray());
      typesComboBox.setModel(cbm);

      // Init falsePozTypes
      falsePozTypes.addAll(responseSet);
      falsePozTypes.add("No annot.");
      cbm = new DefaultComboBoxModel(falsePozTypes);
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

    /** It returns the selected Annotation to calculate the False Pozitive  */
    public String getSelectedFalsePozAnnot(){
      return (String) falsePozTypeComboBox.getSelectedItem();
    }//getSelectedFalsePozAnnot

    class MyCellRenderer extends JLabel implements ListCellRenderer {

       Color background = null;
       Color foreground = null;

       public MyCellRenderer(Color aBackground, Color aForeground) {
           setOpaque(true);
           background = aBackground;
           foreground = aForeground;
       }
       public Component getListCellRendererComponent(
           JList list,
           Object value,
           int index,
           boolean isSelected,
           boolean cellHasFocus)
       {
           // should be done only once...
           ToolTipManager.sharedInstance().registerComponent(list);
           setText(value.toString());
           setBackground(isSelected ? background : Color.white);
           setForeground(isSelected ? foreground : Color.black);
           if (isSelected)
               list.setToolTipText(value.toString());
           return this;
       }
    }//MyCellRenderer

  }// SmallView

}//AnnotDiffHandle