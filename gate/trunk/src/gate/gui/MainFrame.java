/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 22/01/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.Component;
import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Font;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import java.util.*;
import java.io.*;
import java.net.*;

import gate.*;
import gate.creole.*;
import gate.persist.*;
import gate.util.*;
import guk.im.*;


public class MainFrame extends JFrame {

  JMenuBar menuBar;
  JSplitPane mainSplit;
  JSplitPane leftSplit;
  Box southBox;
  JLabel statusBar;
  JProgressBar progressBar;
  JTabbedPane mainTabbedPane;
  JScrollPane projectTreeScroll;
  JScrollPane lowerScroll;
  JTree projectTree;
  DefaultTreeModel projectTreeModel;
  DefaultMutableTreeNode projectTreeRoot;
  DefaultMutableTreeNode lowerTreeRoot;
  DefaultMutableTreeNode appRoot;
  DefaultMutableTreeNode lrRoot;
  DefaultMutableTreeNode prRoot;
  DefaultMutableTreeNode dsRoot;

  Splash splash;
  JTextArea logArea;
  JScrollPane logScroll;
  JComboBox projectCombo;
  DefaultComboBoxModel projectComboModel;
  JToolBar toolbar;

  JFileChooser fileChooser;
  MainFrame parentFrame;
  NewResourceDialog newResourceDialog;

  List openProjects;
  ProjectData currentProject;

  NewApplicationAction newApplicationAction;
  NewProjectAction newProjectAction;
  NewLRAction newLRAction;
  NewPRAction newPRAction;
  NewDSAction newDSAction;
  OpenDSAction openDSAction;
  HelpAboutAction helpAboutAction;

  /**Construct the frame*/
  public MainFrame() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  protected void initLocalData(){
    openProjects = new ArrayList();
    newApplicationAction = new NewApplicationAction();
    newProjectAction = new NewProjectAction();
    newLRAction = new NewLRAction();
    newPRAction = new NewPRAction();
    newDSAction = new NewDSAction();
    openDSAction = new OpenDSAction();
    helpAboutAction = new HelpAboutAction();
  }

  protected void initGuiComponents(){
    parentFrame = this;

    this.getContentPane().setLayout(new BorderLayout());
    this.setSize(new Dimension(800, 600));
    this.setTitle(Main.name + " " + Main.version);


    ResourceHandle handle = new ResourceHandle("<No Projects>", null);
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/project.gif")));
    JPopupMenu popup = new JPopupMenu();
    popup.add(newProjectAction);
    handle.setPopup(popup);
    projectTreeRoot = new DefaultMutableTreeNode(handle, true);
    projectTreeModel = new DefaultTreeModel(projectTreeRoot, true);
    projectTree = new JTree(projectTreeModel);
    projectTree.setCellRenderer(new ResourceTreeCellRenderer());
    projectTree.setRowHeight(0);
    ToolTipManager.sharedInstance().registerComponent(projectTree);
    //upperTree.setShowsRootHandles(true);

    projectTreeScroll = new JScrollPane(projectTree);
    lowerScroll = new JScrollPane();
    leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                               projectTreeScroll, lowerScroll);
    Box leftBox = Box.createVerticalBox();
    JPanel projectBox = new JPanel();
    projectBox.setLayout(new BoxLayout(projectBox, BoxLayout.X_AXIS));
    projectBox.add(new JLabel("Project: "));
    projectCombo = new JComboBox();
    projectComboModel = new DefaultComboBoxModel();
    projectCombo.setModel(projectComboModel);
    projectCombo.setEditable(false);
    projectBox.add(projectCombo);
    projectBox.setAlignmentX(Component.LEFT_ALIGNMENT);
    leftBox.add(projectBox);
    leftBox.add(Box.createVerticalStrut(5));
    leftSplit.setAlignmentX(Component.LEFT_ALIGNMENT);
    leftBox.add(leftSplit);


    logArea = new JTextArea("Gate 2 started at: " + new Date().toString());
    logScroll = new JScrollPane(logArea);
    mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
    mainTabbedPane.insertTab("Messages",null, logScroll, "Gate log", 0);
    mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                               leftBox, mainTabbedPane);
    this.getContentPane().add(mainSplit, BorderLayout.CENTER);

    southBox = Box.createHorizontalBox();
    statusBar = new JLabel();
    progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
    southBox.add(statusBar);
    southBox.add(progressBar);
    this.getContentPane().add(southBox, BorderLayout.SOUTH);

    //MENUS
    menuBar = new JMenuBar();

    JMenu fileMenu = new JMenu("File");
    fileMenu.add(newProjectAction);
    fileMenu.add(newLRAction);
    fileMenu.add(newPRAction);
    fileMenu.add(newDSAction);
    fileMenu.add(openDSAction);
    fileMenu.add(newApplicationAction);
    menuBar.add(fileMenu);

    JMenu editMenu = new JMenu("Edit");
    try{
      JMenu imMenu = null;
      Locale locale = new Locale("en", "GB");
      Locale[] availableLocales = new GateIMDescriptor().getAvailableLocales();
      JMenuItem item;
      if(availableLocales != null && availableLocales.length > 1){
        imMenu = new JMenu("Input methods");
        ButtonGroup bg = new ButtonGroup();
        for(int i = 0; i < availableLocales.length; i++){
          locale = availableLocales[i];
          item = new LocaleSelectorMenuItem(locale, this);
          imMenu.add(item);
          bg.add(item);
        }
      }
      if(imMenu != null) editMenu.add(imMenu);
    }catch(AWTException awte){}

    menuBar.add(editMenu);

    JMenu helpMenu = new JMenu("Help");
    helpMenu.add(helpAboutAction);
    menuBar.add(helpMenu);

    this.setJMenuBar(menuBar);

    //TOOLBAR
    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    toolbar.add(newProjectAction);

    this.getContentPane().add(toolbar, BorderLayout.NORTH);

    //extra stuff
    fileChooser = new JFileChooser();
    fileChooser.setMultiSelectionEnabled(false);
    newResourceDialog = new NewResourceDialog(parentFrame,
                                              "Resource parameters",
                                              true);

    //build the splash
    JPanel splashBox = new JPanel();
    splashBox.setLayout(new BoxLayout(splashBox, BoxLayout.Y_AXIS));
    splashBox.setBackground(Color.white);
    JLabel gifLbl = new JLabel(new ImageIcon(MainFrame.class.getResource(
        "/gate/resources/img/gateSplash.gif")));
    gifLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
    splashBox.add(gifLbl);

    JLabel verLbl = new JLabel("<HTML>Version <B>" + Main.version + "</B>" +
                                ", build <B>" + Main.build + "</B></HTML>");
    splashBox.add(verLbl);

    JButton okBtn = new JButton("OK");
    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        splash.hide();
      }
    });
    okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
    okBtn.setBackground(Color.white);
    splashBox.add(Box.createVerticalStrut(10));
    splashBox.add(okBtn);
    splashBox.add(Box.createVerticalStrut(10));
    splash = new Splash(this, splashBox);
  }

  protected void initListeners(){
    projectCombo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setCurrentProject((ProjectData)projectCombo.getSelectedItem());
      }
    });

    projectTree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        //where inside the tree?
        int x = e.getX();
        int y = e.getY();
        TreePath path = projectTree.getPathForLocation(x, y);
        ResourceHandle handle = null;
        if(path != null){
          DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.
                                       getLastPathComponent();
          handle = (ResourceHandle)node.getUserObject();
        }
        if(handle != null){
          if(SwingUtilities.isRightMouseButton(e)){
            JPopupMenu popup = handle.getPopup();
            if(popup != null){
              popup.show(projectTree, e.getX(), e.getY());
            }
          }else if(SwingUtilities.isLeftMouseButton(e)){
            if(e.getClickCount() == 2){
              //double click - show the resource
              if(handle.isShown()){
                //select
                JComponent largeView = handle.getLargeView();
                if(largeView != null){
                  mainTabbedPane.setSelectedComponent(largeView);
                }
                JComponent smallView = handle.getSmallView();
                if(smallView != null){
                  lowerScroll.getViewport().setView(smallView);
                }else{
                  lowerScroll.getViewport().setView(null);
                }
              }else{
                //show
                JComponent largeView = handle.getLargeView();
                if(largeView != null){
                  mainTabbedPane.addTab(handle.getTitle(), handle.getSmallIcon(),
                                        largeView, handle.getTooltipText());
                  mainTabbedPane.setSelectedComponent(handle.getLargeView());
                }
                JComponent smallView = handle.getSmallView();
                if(smallView != null){
                  lowerScroll.getViewport().setView(smallView);
                }else{
                  lowerScroll.getViewport().setView(null);
                }
                handle.setShown(true);
              }
            }
          }
        }
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }
    });

    this.addComponentListener(new ComponentAdapter() {
      public void componentShown(ComponentEvent e) {
        leftSplit.setDividerLocation(0.7);
      }
    });

  }

  static{
    try{
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }catch(Exception e){
      throw new gate.util.GateRuntimeException(e.toString());
    }
  }

  void remove(ResourceHandle handle){
    DefaultMutableTreeNode parent = null;
    if(handle instanceof ApplicationHandle){
      parent = appRoot;
    }else if(handle instanceof LRHandle){
      parent = lrRoot;
    }else if(handle instanceof PRHandle){
      parent = prRoot;
    }
    DefaultMutableTreeNode node = null;
    if(parent != null) node = (DefaultMutableTreeNode)parent.getFirstChild();
    while(node != null && node.getUserObject() != handle){
      node = (DefaultMutableTreeNode)node.getFirstChild();
    }
    if(node != null){
      node.removeFromParent();
      projectTreeModel.nodeStructureChanged(parent);
    }
    JComponent view = handle.getLargeView();
    if(view != null) mainTabbedPane.remove(view);
    view = handle.getSmallView();
    if(view == lowerScroll.getViewport().getView())
      lowerScroll.getViewport().removeAll();
  }

  /**File | Exit action performed*/
  public void jMenuFileExit_actionPerformed(ActionEvent e) {
    System.exit(0);
  }

  /**Overridden so we can exit when window is closed*/
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      jMenuFileExit_actionPerformed(null);
    }
  }



  protected void addProject(ProjectData pData){
    openProjects.add(pData);
    projectComboModel.addElement(pData);
    projectComboModel.setSelectedItem(pData);
  }

  /**
   * Makes the necessary GUI adjustements when a new project becomes current.
   */
  protected void setCurrentProject(ProjectData project){
    if(currentProject == project) return;
    currentProject = project;

    if(!openProjects.contains(project)) openProjects.add(project);

    ResourceHandle handle = new ResourceHandle(project.toString(), currentProject);
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/project.gif")));
    projectTreeRoot.setUserObject(handle);

    projectTreeRoot.removeAllChildren();
    mainTabbedPane.removeAll();
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        mainTabbedPane.insertTab("Messages",null, logScroll, "Gate log", 0);
      }
    });


    handle = new ResourceHandle("Applications", currentProject);
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/applications.gif")));
    appRoot = new DefaultMutableTreeNode(handle, true);
    JPopupMenu popup = new JPopupMenu();
    popup.add(newApplicationAction);
    handle.setPopup(popup);
    projectTreeRoot.add(appRoot);
    Iterator resIter = currentProject.getApplicationsList().iterator();
    while(resIter.hasNext()){
      handle = (ResourceHandle)resIter.next();
      appRoot.add(new DefaultMutableTreeNode(handle));
      if(handle.isShown() && handle.getLargeView() != null){
        mainTabbedPane.addTab(handle.getTitle(), handle.getLargeView());
      }
    }


    handle = new ResourceHandle("Language Resources", currentProject);
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/lrs.gif")));
    lrRoot = new DefaultMutableTreeNode(handle, true);
    popup = new JPopupMenu();
    popup.add(newLRAction);
    handle.setPopup(popup);
    projectTreeRoot.add(lrRoot);
    resIter = currentProject.getLRList().iterator();
    while(resIter.hasNext()){
      handle = (ResourceHandle)resIter.next();
      lrRoot.add(new DefaultMutableTreeNode(handle));
      if(handle.isShown() && handle.getLargeView() != null){
        mainTabbedPane.addTab(handle.getTitle(), handle.getLargeView());
      }
    }

    handle = new ResourceHandle("Processing Resources", currentProject);
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/prs.gif")));
    prRoot = new DefaultMutableTreeNode(handle, true);
    popup = new JPopupMenu();
    popup.add(newPRAction);
    handle.setPopup(popup);
    projectTreeRoot.add(prRoot);
    resIter = currentProject.getPRList().iterator();
    while(resIter.hasNext()){
      handle = (ResourceHandle)resIter.next();
      prRoot.add(new DefaultMutableTreeNode(handle));
      if(handle.isShown() && handle.getLargeView() != null){
        mainTabbedPane.addTab(handle.getTitle(), handle.getLargeView());
      }
    }

    handle = new ResourceHandle("Data Stores", currentProject);
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/dss.gif")));
    popup = new JPopupMenu();
    popup.add(newDSAction);
    popup.add(openDSAction);
    handle.setPopup(popup);
    dsRoot = new DefaultMutableTreeNode(handle, true);
    projectTreeRoot.add(dsRoot);
    DataStoreRegister dsr = Gate.getDataStoreRegister();
    Iterator dsIter = dsr.iterator();
    while(dsIter.hasNext()){
      DataStore ds = (DataStore)dsIter.next();
      //make sure he have a name
      if(ds.getFeatures() == null){
        FeatureMap features = Factory.newFeatureMap();
        features.put("NAME", "Unnamed datasource");
        ds.setFeatures(features);
      }else if(ds.getFeatures().get("NAME") == null){
        ds.getFeatures().put("NAME", "Unnamed datasource");
      }
      handle = new DSHandle(ds, currentProject);
      dsRoot.add(new DefaultMutableTreeNode(handle));
    }

    projectTreeModel.nodeStructureChanged(projectTreeRoot);


    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                  projectTreeRoot.getFirstChild();
    while(node != null){
      projectTree.expandPath(
                  new TreePath(projectTreeModel.getPathToRoot(node)));
      node = node.getNextSibling();
    }


  }

  class NewProjectAction extends AbstractAction{
    public NewProjectAction(){
      super("New Project", new ImageIcon(MainFrame.class.getResource("/gate/resources/img/newProject.gif")));

    }
    public void actionPerformed(ActionEvent e){
      fileChooser.setDialogTitle("Select new project file");
      fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);
      if(fileChooser.showOpenDialog(parentFrame) == fileChooser.APPROVE_OPTION){
        ProjectData pData = new ProjectData(fileChooser.getSelectedFile(), parentFrame);
        addProject(pData);
      }
    }
  }

  class NewApplicationAction extends AbstractAction{
    public NewApplicationAction(){
      super("New Application");
    }
    public void actionPerformed(ActionEvent e){
      Object answer = JOptionPane.showInputDialog(
                        parentFrame,
                        "Please provide a name for the new application:",
                        "Gate",
                        JOptionPane.QUESTION_MESSAGE);
      if(answer instanceof String){
        try{
          SerialController controller =
                (SerialController)Factory.createResource(
                                "gate.creole.SerialController",
                                Factory.newFeatureMap());
          FeatureMap fm = controller.getFeatures();
          if(fm == null){
            controller.setFeatures(fm = Factory.newFeatureMap());
          }
          fm.put("NAME", answer);

          ApplicationHandle handle = new ApplicationHandle(controller,
                                                           currentProject);
          appRoot.add(new DefaultMutableTreeNode(handle));
          projectTreeModel.nodeStructureChanged(appRoot);
          projectTree.expandPath(new TreePath(projectTreeModel.getPathToRoot(appRoot)));
          currentProject.addApplication(handle);
        }catch(ResourceInstantiationException rie){
          JOptionPane.showMessageDialog(parentFrame,
                                        "Could not create application!\n" +
                                         rie.toString(),
                                        "Gate", JOptionPane.ERROR_MESSAGE);
        }
      }else{
        JOptionPane.showMessageDialog(parentFrame,
                                      "Unrecognised input!",
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  class NewLRAction extends AbstractAction{
    public NewLRAction(){
      super("Create language resource");
    }

    public void actionPerformed(ActionEvent e){
      CreoleRegister reg = Gate.getCreoleRegister();
      Set lrTypes = reg.getLrTypes();
      if(lrTypes != null && !lrTypes.isEmpty()){
        HashMap resourcesByName = new HashMap();
        Iterator lrIter = lrTypes.iterator();
        while(lrIter.hasNext()){
          ResourceData rData = (ResourceData)reg.get(lrIter.next());
          resourcesByName.put(rData.getName(), rData);
        }
        List lrNames = new ArrayList(resourcesByName.keySet());
        Collections.sort(lrNames);
        Object answer = JOptionPane.showInputDialog(
                            parentFrame,
                            "Select type of Language resource",
                            "Gate", JOptionPane.QUESTION_MESSAGE,
                            null, lrNames.toArray(),
                            lrNames.get(0));
        if(answer != null){
          ResourceData rData = (ResourceData)resourcesByName.get(answer);
          newResourceDialog.setTitle("Parameters for the new " + rData.getName());
          LanguageResource res = (LanguageResource)newResourceDialog.show(rData);
          if(res != null){
            LRHandle handle = new LRHandle(res, currentProject);
            handle.setTooltipText("<html><b>Type:</b> " +
                                  rData.getName() + "</html>");
            lrRoot.add(new DefaultMutableTreeNode(handle, false));
            projectTreeModel.nodeStructureChanged(lrRoot);
            projectTree.expandPath(new TreePath(projectTreeModel.getPathToRoot(lrRoot)));
            currentProject.addLR(handle);
          }

        }
      }else{
        //no lr types
        JOptionPane.showMessageDialog(parentFrame,
                                      "Could not find any registered types " +
                                      "of resources...\n" +
                                      "Check your Gate installation!",
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
  }//class NewLRAction extends AbstractAction


  class NewPRAction extends AbstractAction{
    public NewPRAction(){
      super("Create processing resource");
    }

    public void actionPerformed(ActionEvent e){
      CreoleRegister reg = Gate.getCreoleRegister();
      Set prTypes = reg.getPrTypes();
      if(prTypes != null && !prTypes.isEmpty()){
        HashMap resourcesByName = new HashMap();
        Iterator lrIter = prTypes.iterator();
        while(lrIter.hasNext()){
          ResourceData rData = (ResourceData)reg.get(lrIter.next());
          resourcesByName.put(rData.getName(), rData);
        }
        List prNames = new ArrayList(resourcesByName.keySet());
        Collections.sort(prNames);
        Object answer = JOptionPane.showInputDialog(
                            parentFrame,
                            "Select type of Language resource",
                            "Gate", JOptionPane.QUESTION_MESSAGE,
                            null, prNames.toArray(),
                            prNames.get(0));
        if(answer != null){
          ResourceData rData = (ResourceData)resourcesByName.get(answer);
          newResourceDialog.setTitle("Parameters for the new " + rData.getName());
          ProcessingResource res = (ProcessingResource)newResourceDialog.show(rData);
          if(res != null){
            PRHandle handle = new PRHandle(res, currentProject);
            handle.setTooltipText("<html><b>Type:</b> " +
                                  rData.getName() + "</html>");
            prRoot.add(new DefaultMutableTreeNode(handle, false));
            projectTreeModel.nodeStructureChanged(prRoot);
            projectTree.expandPath(new TreePath(projectTreeModel.getPathToRoot(prRoot)));
            currentProject.addPR(handle);
          }
        }
      }else{
        //no lr types
        JOptionPane.showMessageDialog(parentFrame,
                                      "Could not find any registered types " +
                                      "of resources...\n" +
                                      "Check your Gate installation!",
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
  }//class NewPRAction extends AbstractAction

  class NewDSAction extends AbstractAction{
    public NewDSAction(){
      super("Create datastore");
    }

    public void actionPerformed(ActionEvent e){
      DataStoreRegister reg = Gate.getDataStoreRegister();
      Map dsTypes = reg.getDataStoreClassNames();
      HashMap dsTypeByName = new HashMap();
      Iterator dsTypesIter = dsTypes.entrySet().iterator();
      while(dsTypesIter.hasNext()){
        Map.Entry entry = (Map.Entry)dsTypesIter.next();
        dsTypeByName.put(entry.getValue(), entry.getKey());
      }

      if(!dsTypeByName.isEmpty()){
        Object[] names = dsTypeByName.keySet().toArray();
        Object answer = JOptionPane.showInputDialog(
                            parentFrame,
                            "Select type of Datastore",
                            "Gate", JOptionPane.QUESTION_MESSAGE,
                            null, names,
                            names[0]);
        if(answer != null){
          String className = (String)dsTypeByName.get(answer);
          if(className.indexOf("SerialDataStore") != -1){
            //get the URL (a file in this case)
            fileChooser.setDialogTitle("Select a new directory");
            fileChooser.setFileSelectionMode(fileChooser.DIRECTORIES_ONLY);
            if(fileChooser.showOpenDialog(parentFrame) == fileChooser.APPROVE_OPTION){
              try{
                URL dsURL = fileChooser.getSelectedFile().toURL();
                DataStore ds = Factory.createDataStore(className, dsURL);
                if(ds != null){
                  //make sure he have a name
                  if(ds.getFeatures() == null){
                    FeatureMap features = Factory.newFeatureMap();
                    features.put("NAME", dsURL.getFile());
                    ds.setFeatures(features);
                  }else if(ds.getFeatures().get("NAME") == null){
                    ds.getFeatures().put("NAME", dsURL.getFile());
                  }
                  DSHandle handle = new DSHandle(ds, currentProject);
                  dsRoot.add(new DefaultMutableTreeNode(handle, false));
                  projectTreeModel.nodeStructureChanged(dsRoot);
                }
              }catch(MalformedURLException mue){
                JOptionPane.showMessageDialog(
                    parentFrame, "Invalid location for the datastore\n " +
                                      mue.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
              }catch(PersistenceException pe){
                JOptionPane.showMessageDialog(
                    parentFrame, "Datastore creation error!\n " +
                                      pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
              }
            }
          }else{
            throw new UnsupportedOperationException("Unimplemented option!\n"+
                                                    "Use a serial datastore");
          }
        }
      }else{
        //no ds types
        JOptionPane.showMessageDialog(parentFrame,
                                      "Could not find any registered types " +
                                      "of datastores...\n" +
                                      "Check your Gate installation!",
                                      "Gate", JOptionPane.ERROR_MESSAGE);

      }
    }
  }//class NewDSAction extends AbstractAction


  class OpenDSAction extends AbstractAction{
    public OpenDSAction(){
      super("Open datastore");
    }

    public void actionPerformed(ActionEvent e){
      DataStoreRegister reg = Gate.getDataStoreRegister();
      Map dsTypes = reg.getDataStoreClassNames();
      HashMap dsTypeByName = new HashMap();
      Iterator dsTypesIter = dsTypes.entrySet().iterator();
      while(dsTypesIter.hasNext()){
        Map.Entry entry = (Map.Entry)dsTypesIter.next();
        dsTypeByName.put(entry.getValue(), entry.getKey());
      }

      if(!dsTypeByName.isEmpty()){
        Object[] names = dsTypeByName.keySet().toArray();
        Object answer = JOptionPane.showInputDialog(
                            parentFrame,
                            "Select type of Datastore",
                            "Gate", JOptionPane.QUESTION_MESSAGE,
                            null, names,
                            names[0]);
        if(answer != null){
          String className = (String)dsTypeByName.get(answer);
          if(className.indexOf("SerialDataStore") != -1){
            //get the URL (a file in this case)
            fileChooser.setDialogTitle("Select the datastore directory");
            fileChooser.setFileSelectionMode(fileChooser.DIRECTORIES_ONLY);
            if(fileChooser.showOpenDialog(parentFrame) == fileChooser.APPROVE_OPTION){
              try{
                URL dsURL = fileChooser.getSelectedFile().toURL();
                DataStore ds = Factory.openDataStore(className, dsURL);
                if(ds != null){
                  //make sure he have a name
                  if(ds.getFeatures() == null){
                    FeatureMap features = Factory.newFeatureMap();
                    features.put("NAME", dsURL.getFile());
                    ds.setFeatures(features);
                  }else if(ds.getFeatures().get("NAME") == null){
                    ds.getFeatures().put("NAME", dsURL.getFile());
                  }
                  DSHandle handle = new DSHandle(ds, currentProject);
                  dsRoot.add(new DefaultMutableTreeNode(handle));
                  projectTreeModel.nodeStructureChanged(dsRoot);
                }
              }catch(MalformedURLException mue){
                JOptionPane.showMessageDialog(
                    parentFrame, "Invalid location for the datastore\n " +
                                      mue.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
              }catch(PersistenceException pe){
                JOptionPane.showMessageDialog(
                    parentFrame, "Datastore opening error!\n " +
                                      pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
              }
            }
          }else{
            throw new UnsupportedOperationException("Unimplemented option!\n"+
                                                    "Use a serial datastore");
          }
        }
      }else{
        //no ds types
        JOptionPane.showMessageDialog(parentFrame,
                                      "Could not find any registered types " +
                                      "of datastores...\n" +
                                      "Check your Gate installation!",
                                      "Gate", JOptionPane.ERROR_MESSAGE);

      }
    }
  }//class OpenDSAction extends AbstractAction

  class HelpAboutAction extends AbstractAction{
    public HelpAboutAction(){
      super("About");
    }

    public void actionPerformed(ActionEvent e){
      splash.show();
    }
  }

  protected class ResourceTreeCellRenderer extends DefaultTreeCellRenderer{
    public ResourceTreeCellRenderer(){
      setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
    }
    public Component getTreeCellRendererComponent(JTree tree,
                                              Object value,
                                              boolean sel,
                                              boolean expanded,
                                              boolean leaf,
                                              int row,
                                              boolean hasFocus){
      super.getTreeCellRendererComponent(tree, value, selected, expanded,
                                         leaf, row, hasFocus);
      Object handle = ((DefaultMutableTreeNode)value).getUserObject();
      if(handle != null && handle instanceof ResourceHandle){
        setIcon(((ResourceHandle)handle).getSmallIcon());
        setText(((ResourceHandle)handle).getTitle());
        setToolTipText(((ResourceHandle)handle).getTooltipText());
      }
      return this;
    }
  }

  class LocaleSelectorMenuItem extends JRadioButtonMenuItem{
    public LocaleSelectorMenuItem(Locale locale, JFrame pframe){
      super(locale.getDisplayName());
      this.frame = pframe;
      me = this;
      myLocale = locale;
      this.addActionListener(new ActionListener()  {
        public void actionPerformed(ActionEvent e) {
          me.setSelected(frame.getInputContext().selectInputMethod(myLocale));
        }
      });
    }
    Locale myLocale;
    JRadioButtonMenuItem me;
    JFrame frame;
  }
}