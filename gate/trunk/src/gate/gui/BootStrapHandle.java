/*  BootStrapHandle.java
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
public class BootStrapHandle extends ResourceHandle{
  JPopupMenu popup = null;
  SmallView smallView = null;
  JPanel largeView = null;
  MainFrame mainFrame = null;
  BootStrapHandle thisBootStrapHandle = null;
  BootStrap bootStrapWizard = null;

  public BootStrapHandle(MainFrame aMainFrame){
    mainFrame = aMainFrame;
    thisBootStrapHandle = this;

    smallView = new SmallView();
    this.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/annDiff.gif")));
    popup = new JPopupMenu();
    popup.add(new CloseAction());
    this.setPopup(popup);
  }//AnnotDiffHandle


  public JComponent getLargeView(){
    return largeView;
  }// getLargeView()

  public JComponent getSmallView(){
    return smallView;
  }// getSmallView()


  private void doCreateResource(){
    Thread thread = new Thread(new CreateResourceRunner());
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }//doCreateResource();

  class CloseAction extends AbstractAction{
    public CloseAction(){
      super("Close");
    }// CloseAction

    public void actionPerformed(ActionEvent e){
      mainFrame.remove(thisBootStrapHandle);
    }// actionPerformed();
  }//CloseAction

  /**
    * This class is the configuration window for BootStraper
    */
  class SmallView extends JPanel{
    // Local data
    String resourceName = null;
    String resourceType = null;
    Set    resourceTypes = null;
    String className = null;
    Set    resourceInterfaces = null;
    Set    possibleInterfaces = null;
    String pathNewProject = null;

    // GUI components
    JLabel     resourceNameLabel = null;
    JTextField resourceNameTextField = null;

    JLabel     resourceTypesLabel = null;
    JComboBox  resourceTypesComboBox = null;

    JLabel     classNameLabel = null;
    JTextField classNameTextField = null;

    JLabel     interfacesLabel = null;
    JList      interfacesList     = null;

    JLabel     chooseFolderLabel = null;
    JButton    chooseFolderButton = null;

    JButton    createResourceButton = null;

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
    }//updateData()

    /**Initialises the data (the loaded resources)*/
    public void initLocalData(){
      pathNewProject = new String(".");
      resourceTypes = new HashSet();
      possibleInterfaces = new HashSet();
    }// initLocalData

    /**
      * This method initializes the GUI components
      */
    public void initGuiComponents(){

      //Initialise GUI components
      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      // init resource name
      resourceNameLabel = new JLabel("Resource name");
      resourceNameLabel.setToolTipText("Here goes the name of the resource" +
                                       " you want to create");
      resourceNameLabel.setOpaque(true);
      resourceNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      resourceNameTextField = new JTextField();
      resourceNameTextField.setAlignmentX(Component.LEFT_ALIGNMENT);

      // init resourceTypesComboBox
      resourceTypesLabel = new JLabel("Resource type");
      resourceTypesLabel.setToolTipText("Select the type of the resource !");
      resourceTypesLabel.setOpaque(true);
      resourceTypesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      Vector comboCont = new Vector(resourceTypes);
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

      // init interfaces
      interfacesLabel = new JLabel("Interfaces implemented");
      interfacesLabel.setToolTipText("Selecte the interfaces implemented");
      interfacesLabel.setOpaque(true);
      interfacesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      interfacesList = new JList(new Vector(possibleInterfaces));
      interfacesList.setAlignmentX(Component.LEFT_ALIGNMENT);

      // init choose Folder
      chooseFolderLabel = new JLabel("Create in ...");
      chooseFolderLabel.setOpaque(true);
      chooseFolderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      chooseFolderLabel.setToolTipText("Select the name of the folder where" +
                                    " you want the resource to be created.");
      chooseFolderButton = new JButton("...");
      chooseFolderButton.setAlignmentX(Component.LEFT_ALIGNMENT);

      // init createresource
      createResourceButton = new JButton("Create");
      createResourceButton.setAlignmentX(Component.LEFT_ALIGNMENT);

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
      currentBox.add(interfacesList);
      mainBox.add(currentBox);

      mainBox.add(Box.createRigidArea(new Dimension(0,10)));

      currentBox = new Box(BoxLayout.Y_AXIS);
      currentBox.add(chooseFolderLabel);
      currentBox.add(chooseFolderButton);
      mainBox.add(currentBox);

      mainBox.add(Box.createRigidArea(new Dimension(0,20)));

      mainBox.add(createResourceButton);

      // Add a space
      this.add(Box.createRigidArea(new Dimension(0,5)));
      this.add(mainBox);
      this.add(Box.createRigidArea(new Dimension(0,5)));
    }//initGuiComponents

    /**
      * This one initializes the listeners fot the GUI components
      */
    public void initListeners(){

     createResourceButton.addActionListener(new java.awt.event.ActionListener(){
        public void actionPerformed(ActionEvent e){
          thisBootStrapHandle.doCreateResource();
        }//actionPerformed
     });

     chooseFolderButton.addActionListener(new java.awt.event.ActionListener(){
        public void actionPerformed(ActionEvent e){
          // choose folder code
        }//actionPerformed
     });

    }//initListeners


    /** It returns the resource name the user enters*/
    public String getSelectedResoureName(){
      return resourceName;
    }//getSelectedResoureName

    /** It returns the selected resource type*/
    public String getSelectedResourceType(){
      return resourceType;
    }//getSelectedResourceType

    /** It returns the name of the class that implements the resource*/
    public String getSelectedClassName(){
      return className;
    }//getSelectedClassName

    /** It returns the interfaces the resource implements*/
    public Set getSelectedInterfaces(){
      return resourceInterfaces;
    }//getSelectedInterfaces
  }// SmallView

  /**Class used to run an annot. diff in a new thread*/
  class CreateResourceRunner implements Runnable{

    public CreateResourceRunner(){
    }// CreateResourceRunner()

    public void run(){
/*      try{
      } catch (ResourceInstantiationException e){
        JOptionPane.showMessageDialog(mainFrame,
                     e.getMessage() + "\n Resource creation stopped !",
                     "Resource creation error !",
                     JOptionPane.ERROR_MESSAGE);
      }
*/
    }// run();
  }//CreateResourceRunner

}//BootStrapHandle