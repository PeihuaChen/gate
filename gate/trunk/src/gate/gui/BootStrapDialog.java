/*  BootStrapDialog.java
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU 05/03/2001
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

/**
  * This class is used to handle BootStrap wizard with the Gate GUI interface.
  */
public class BootStrapDialog extends JDialog{

  MainFrame mainFrame = null;
  BootStrapDialog thisBootStrapDialog = null;
  BootStrap bootStrapWizard = null;
  // Local data
  String resourceName = null;
  String resourceType = null;
  Map    resourceTypes = null;
  String className = null;
  Set    resourceInterfaces = null;
  String possibleInterfaces = null;
  String pathNewProject = null;

  // GUI components
  JLabel     resourceNameLabel = null;
  JTextField resourceNameTextField = null;

  JLabel     resourceTypesLabel = null;
  JComboBox  resourceTypesComboBox = null;

  JLabel     classNameLabel = null;
  JTextField classNameTextField = null;

  JLabel     interfacesLabel = null;
  JTextField interfacesTextField = null;

  JLabel     chooseFolderLabel = null;
  JTextField chooseFolderTextField = null;
  JButton    chooseFolderButton = null;

  JButton    createResourceButton = null;
  JButton    cancelButton = null;

  JFileChooser fileChooser = null;

  public BootStrapDialog(MainFrame aMainFrame){
    mainFrame = aMainFrame;
    thisBootStrapDialog = this;
    this.setTitle("BootStrap Wizard");
    initLocalData();
    initGuiComponents();
    initListeners();

  }//BootStrapDialog

  private void doCreateResource(){
    resourceName = resourceNameTextField.getText();
    if (resourceName == null || "".equals(resourceName)){
      thisBootStrapDialog.setModal(false);
      JOptionPane.showMessageDialog(mainFrame,
                      "A name for the resource must be provided",
                      "ERROR !",
                      JOptionPane.ERROR_MESSAGE);
      thisBootStrapDialog.setModal(true);
      return;
    }// End if

    className = classNameTextField.getText();
    if (className == null || "".equals(className)){
      thisBootStrapDialog.setModal(false);
      JOptionPane.showMessageDialog(mainFrame,
                      "A name for the implementing class must be provided",
                      "ERROR !",
                      JOptionPane.ERROR_MESSAGE);
      thisBootStrapDialog.setModal(true);
      return;
    }// End if

    pathNewProject = chooseFolderTextField.getText();
    if (pathNewProject == null || "".equals(pathNewProject)){
      thisBootStrapDialog.setModal(false);
      JOptionPane.showMessageDialog(mainFrame,
                      "A path to the creation folder must be provided",
                      "ERROR !",
                      JOptionPane.ERROR_MESSAGE);
      thisBootStrapDialog.setModal(true);
      return;
    }// End if

    resourceType = (String)resourceTypesComboBox.getSelectedItem();
    resourceInterfaces = this.getSelectedInterfaces();

    Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                               new CreateResourceRunner());
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }//doCreateResource();

  /**Initialises the data (the loaded resources)*/
  public void initLocalData(){
    pathNewProject = new String(".");
    resourceTypes = new HashMap();
    resourceTypes.put("LanguageResource","gate.LanguageResource");
    resourceTypes.put("VisualResource","gate.VisualResource");
    resourceTypes.put("ProcessingResource","gate.ProcessingResource");

    possibleInterfaces = (String) resourceTypes.get("LanguageResource");
    if (possibleInterfaces == null)
      possibleInterfaces = new String();
  }// initLocalData

  /**
    * This method initializes the GUI components
    */
  public void initGuiComponents(){

    //Initialise GUI components
    this.getContentPane().setLayout(
        new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS));
    this.setModal(true);
    // init resource name
    resourceNameLabel = new JLabel("Resource name");
    resourceNameLabel.setToolTipText("Here goes the name of the resource" +
                                     " you want to create");
    resourceNameLabel.setOpaque(true);
    resourceNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    resourceNameTextField = new JTextField();
    resourceNameTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
    resourceNameTextField.setColumns(40);
    Dimension dim = new Dimension(
                              resourceNameTextField.getPreferredSize().width,
                              resourceNameTextField.getPreferredSize().height);
    resourceNameTextField.setPreferredSize(dim);
    resourceNameTextField.setMinimumSize(dim);
//    resourceNameTextField.setMaximumSize(dim);

    // init resourceTypesComboBox
    resourceTypesLabel = new JLabel("Resource type");
    resourceTypesLabel.setToolTipText("Select the type of the resource !");
    resourceTypesLabel.setOpaque(true);
    resourceTypesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    Vector comboCont = new Vector(resourceTypes.keySet());
    Collections.sort(comboCont);
    resourceTypesComboBox = new JComboBox(comboCont);
    resourceTypesComboBox.setEditable(false);
    resourceTypesComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

    // init class name
    classNameLabel = new JLabel("Implementing class name");
    classNameLabel.setToolTipText("The name of the class that " +
                                  "impements this resource");
    classNameLabel.setOpaque(true);
    classNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    classNameTextField = new JTextField();
    classNameTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
    classNameTextField.setColumns(40);
    dim = new Dimension(classNameTextField.getPreferredSize().width,
                        classNameTextField.getPreferredSize().height);

    classNameTextField.setPreferredSize(dim);
    classNameTextField.setMinimumSize(dim);
//    classNameTextField.setMaximumSize(dim);

    // init interfaces
    interfacesLabel = new JLabel("Interfaces implemented use ");
    interfacesLabel.setToolTipText("Write the interfaces implemented separated by comma");
    interfacesLabel.setOpaque(true);
    interfacesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    interfacesTextField = new JTextField(possibleInterfaces);
    interfacesTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
    interfacesTextField.setColumns(40);
    dim = new Dimension(interfacesTextField.getPreferredSize().width,
                        interfacesTextField.getPreferredSize().height);

    interfacesTextField.setPreferredSize(dim);
    interfacesTextField.setMinimumSize(dim);
//    interfacesTextField.setMaximumSize(dim);

    // init choose Folder
    chooseFolderLabel = new JLabel("Create in folder ...");
    chooseFolderLabel.setOpaque(true);
    chooseFolderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    chooseFolderLabel.setToolTipText("Select the name of the folder where" +
                                  " you want the resource to be created.");
    chooseFolderButton = new JButton("...");
    chooseFolderButton.setAlignmentX(Component.LEFT_ALIGNMENT);
    chooseFolderTextField = new JTextField();
    chooseFolderTextField.setAlignmentX(Component.LEFT_ALIGNMENT);
    chooseFolderTextField.setColumns(35);
    dim = new Dimension(chooseFolderTextField.getPreferredSize().width,
                        chooseFolderTextField.getPreferredSize().height);

    chooseFolderTextField.setPreferredSize(dim);
    chooseFolderTextField.setMinimumSize(dim);
//    chooseFolderTextField.setMaximumSize(dim);

    // init createresource
    createResourceButton = new JButton("Finish");
    // init cancel
    cancelButton = new JButton("Cancel");
    // Put all those components at their place
    Box mainBox = new Box(BoxLayout.Y_AXIS);

    Box currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(resourceNameLabel);
    currentBox.add(resourceNameTextField);
    mainBox.add(currentBox);

    mainBox.add(Box.createRigidArea(new Dimension(0,10)));

    currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(resourceTypesLabel);
    currentBox.add(resourceTypesComboBox);
    mainBox.add(currentBox);

    mainBox.add(Box.createRigidArea(new Dimension(0,10)));

    currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(classNameLabel);
    currentBox.add(classNameTextField);
    mainBox.add(currentBox);

    mainBox.add(Box.createRigidArea(new Dimension(0,10)));

    currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(interfacesLabel);
    currentBox.add(interfacesTextField);
    mainBox.add(currentBox);

    mainBox.add(Box.createRigidArea(new Dimension(0,10)));

    currentBox = new Box(BoxLayout.Y_AXIS);
    currentBox.add(chooseFolderLabel);
    JPanel tmpBox = new JPanel();
    tmpBox.setLayout(new BoxLayout(tmpBox,BoxLayout.X_AXIS));
    tmpBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    tmpBox.add(chooseFolderTextField);
    tmpBox.add(chooseFolderButton);
    currentBox.add(tmpBox);
    mainBox.add(currentBox);

    mainBox.add(Box.createRigidArea(new Dimension(0,20)));

    tmpBox = new JPanel();
    tmpBox.setLayout(new BoxLayout(tmpBox,BoxLayout.X_AXIS));
    tmpBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    tmpBox.add(Box.createRigidArea(new Dimension(90,0)));
    tmpBox.add(createResourceButton);
    tmpBox.add(Box.createRigidArea(new Dimension(20,0)));
    tmpBox.add(cancelButton);
    mainBox.add(tmpBox);

    // Add a space
    this.getContentPane().add(Box.createRigidArea(new Dimension(0,5)));
    this.getContentPane().add(mainBox);
    this.getContentPane().add(Box.createRigidArea(new Dimension(0,5)));

    this.pack();
    fileChooser = new JFileChooser();
  }//initGuiComponents

  /**
    * This one initializes the listeners fot the GUI components
    */
  public void initListeners(){

   createResourceButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        doCreateResource();
      }//actionPerformed
   });

   cancelButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        thisBootStrapDialog.hide();
      }//actionPerformed
   });

   resourceTypesComboBox.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        String selectedItem =(String) resourceTypesComboBox.getSelectedItem();
        possibleInterfaces = (String)resourceTypes.get(selectedItem);
        interfacesTextField.setText(possibleInterfaces);
      }// actionPerformed();
   });

   chooseFolderButton.addActionListener(new java.awt.event.ActionListener(){
      public void actionPerformed(ActionEvent e){
        // choose folder code
        fileChooser.setDialogTitle("Select the path for this resource");
        fileChooser.setFileSelectionMode(fileChooser.DIRECTORIES_ONLY);
        if(fileChooser.showOpenDialog(mainFrame) == fileChooser.APPROVE_OPTION){
          pathNewProject = fileChooser.getSelectedFile().toString();
          fileChooser.setCurrentDirectory(fileChooser.getCurrentDirectory());
        }// End if
        chooseFolderTextField.setText(pathNewProject);

      }//actionPerformed
   });

  }//initListeners


  /** It returns the interfaces the resource implements*/
  public Set getSelectedInterfaces(){
    String interfaces = interfacesTextField.getText();
    resourceInterfaces = new HashSet();
    if (interfaces == null || "".equals(interfaces))
        return resourceInterfaces;
    StringTokenizer tokenizer = new StringTokenizer(interfaces,",");
    while (tokenizer.hasMoreElements()){
      String token = tokenizer.nextToken();
      resourceInterfaces.add(token);
    }// end While
    return resourceInterfaces;
  }//getSelectedInterfaces

  /**Class used to run an annot. diff in a new thread*/
  class CreateResourceRunner implements Runnable{

    public CreateResourceRunner(){
    }// CreateResourceRunner()

    public void run(){


      try{
        bootStrapWizard = new BootStrap();
        bootStrapWizard.createResource(resourceName,
                                       resourceType,
                                       className,
                                       resourceInterfaces,
                                       pathNewProject);
        thisBootStrapDialog.hide();
        JOptionPane.showMessageDialog(mainFrame,
                                    "Creation succeeded !",
                                    "DONE !",
                                    JOptionPane.DEFAULT_OPTION);
      }catch (Exception e){
        thisBootStrapDialog.setModal(false);
        e.printStackTrace(Err.getPrintWriter());
        JOptionPane.showMessageDialog(mainFrame,
                     e.getMessage() + "\n Resource creation stopped !",
                     "BootStrap error !",
                     JOptionPane.ERROR_MESSAGE);
        thisBootStrapDialog.setModal(true);
      }
    }// run();
  }//CreateResourceRunner

}//BootStrapDialog