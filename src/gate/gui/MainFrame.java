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
import java.beans.*;

import java.util.*;
import java.io.*;
import java.net.*;

import gate.*;
import gate.creole.*;
import gate.event.*;
import gate.persist.*;
import gate.util.*;
import guk.im.*;
import javax.swing.event.*;

/**
 * The main Gate GUI frame. This is a singleton.
 */
public class MainFrame extends JFrame
                    implements ProgressListener, StatusListener, CreoleListener{

//  MainFrame thisMainFrame = null;
  JMenuBar menuBar;
  JSplitPane mainSplit;
  JSplitPane leftSplit;
  Box southBox;
  JLabel statusBar;
  JProgressBar progressBar;
  JTabbedPane mainTabbedPane;
  JScrollPane projectTreeScroll;
  JScrollPane lowerScroll;
  /*To be removed ->*/
/*
  JTree projectTree;
  DefaultTreeModel projectTreeModel;
  DefaultMutableTreeNode projectTreeRoot;
  DefaultMutableTreeNode lowerTreeRoot;
  DefaultMutableTreeNode appRoot;
  DefaultMutableTreeNode lrRoot;
  DefaultMutableTreeNode prRoot;
  DefaultMutableTreeNode dsRoot;
*/
  /* <- To be removed*/
  //new version
  JPopupMenu appsPopup;
  JPopupMenu lrsPopup;
  JPopupMenu prsPopup;
  JPopupMenu dssPopup;

  JTree resourcesTree;
  JScrollPane resourcesTreeScroll;
  DefaultTreeModel resourcesTreeModel;
  DefaultMutableTreeNode resourcesTreeRoot;
  DefaultMutableTreeNode applicationsRoot;
  DefaultMutableTreeNode languageResourcesRoot;
  DefaultMutableTreeNode processingResourcesRoot;
  DefaultMutableTreeNode datastoresRoot;

//  WeakHashMap handleForResourceName;



  Splash splash;
  LogArea logArea;
  JScrollPane logScroll;
//  JComboBox projectCombo;
//  DefaultComboBoxModel projectComboModel;
  JToolBar toolbar;

  JFileChooser fileChooser;
  TabBlinker logBlinker;
//  MainFrame parentFrame;
  NewResourceDialog newResourceDialog;
  WaitDialog waitDialog;

//  List openProjects;
//  ProjectData currentProject;

  NewApplicationAction newApplicationAction;
  //NewProjectAction newProjectAction;
  NewLRAction newLRAction;
  NewPRAction newPRAction;
  NewDSAction newDSAction;
  OpenDSAction openDSAction;
  HelpAboutAction helpAboutAction;
  NewAnnotDiffAction newAnnotDiffAction = null;
  NewBootStrapAction newBootStrapAction = null;


  static private MainFrame instance;

  static public MainFrame getInstance(){
    if(instance == null) instance = new MainFrame();
    return instance;
  }

  /**Construct the frame*/
  private MainFrame() {
//    thisMainFrame = this;
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  protected void initLocalData(){
    resourcesTreeRoot = new DefaultMutableTreeNode("Gate", true);
    applicationsRoot = new DefaultMutableTreeNode("Applications", true);
    languageResourcesRoot = new DefaultMutableTreeNode("Language Resources",
                                                       true);
    processingResourcesRoot = new DefaultMutableTreeNode("Processing Resources",
                                                         true);
    datastoresRoot = new DefaultMutableTreeNode("Data stores", true);
    resourcesTreeRoot.add(applicationsRoot);
    resourcesTreeRoot.add(languageResourcesRoot);
    resourcesTreeRoot.add(processingResourcesRoot);
    resourcesTreeRoot.add(datastoresRoot);
    resourcesTreeModel = new DefaultTreeModel(resourcesTreeRoot, true);

//    handleForResourceName = new WeakHashMap();


/*
    openProjects = new ArrayList();
    openProjects.add(currentProject = new ProjectData(null, this));
*/
    newApplicationAction = new NewApplicationAction();
    //newProjectAction = new NewProjectAction();
    newLRAction = new NewLRAction();
    newPRAction = new NewPRAction();
    newDSAction = new NewDSAction();
    openDSAction = new OpenDSAction();
    helpAboutAction = new HelpAboutAction();
    newAnnotDiffAction = new NewAnnotDiffAction();
    newBootStrapAction = new NewBootStrapAction();
  }

  protected void initGuiComponents(){
    this.getContentPane().setLayout(new BorderLayout());
    this.setSize(new Dimension(800, 600));
    this.setTitle(Main.name + " " + Main.version);


//    resourcesTreeModel = new ResourcesTreeModel(resourcesTreeRoot);
//    resourcesTree = new JTree(resourcesTreeModel);
    resourcesTree = new JTree(resourcesTreeModel);
    resourcesTree.setCellRenderer(new ResourceTreeCellRenderer());
    resourcesTree.setRowHeight(0);
    //expand all nodes
    resourcesTree.expandRow(0);
    resourcesTree.expandRow(1);
    resourcesTree.expandRow(2);
    resourcesTree.expandRow(3);
    resourcesTree.expandRow(4);
    ToolTipManager.sharedInstance().registerComponent(resourcesTree);
    resourcesTreeScroll = new JScrollPane(resourcesTree);

    lowerScroll = new JScrollPane();
    leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                               resourcesTreeScroll, lowerScroll);

    //popups
    appsPopup = new JPopupMenu();
    appsPopup.add(newApplicationAction);

    lrsPopup = new JPopupMenu();
    lrsPopup.add(newLRAction);

    prsPopup = new JPopupMenu();
    prsPopup.add(newPRAction);

    dssPopup = new JPopupMenu();
    dssPopup.add(newDSAction);
    dssPopup.add(openDSAction);
    // <- new version

    // Create a new logArea and redirect the Out and Err output to it.
    logArea = new LogArea();
    logScroll = new JScrollPane(logArea);
    // Out has been redirected to the logArea
    Out.prln("Gate 2 started at: " + new Date().toString());

    mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
    mainTabbedPane.insertTab("Messages",null, logScroll, "Gate log", 0);
    logBlinker = new TabBlinker(mainTabbedPane, logScroll, Color.red);
    mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                               leftSplit, mainTabbedPane);
    this.getContentPane().add(mainSplit, BorderLayout.CENTER);

    southBox = Box.createHorizontalBox();
    statusBar = new JLabel();
    progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
    progressBar.setStringPainted(true);
    Dimension dim = new Dimension(300, progressBar.getPreferredSize().height);
    progressBar.setStringPainted(false);
    progressBar.setMaximumSize(dim);
    progressBar.setMinimumSize(dim);
    progressBar.setPreferredSize(dim);
    southBox.add(statusBar);
    southBox.add(Box.createHorizontalGlue());
    southBox.add(progressBar);
    this.getContentPane().add(southBox, BorderLayout.SOUTH);

    //MENUS
    menuBar = new JMenuBar();

    JMenu fileMenu = new JMenu("File");
    //fileMenu.add(new JGateMenuItem(newProjectAction));
    fileMenu.add(new JGateMenuItem(newLRAction));
    fileMenu.add(new JGateMenuItem(newPRAction));
    fileMenu.add(new JGateMenuItem(newDSAction));
    fileMenu.add(new JGateMenuItem(openDSAction));
    fileMenu.add(new JGateMenuItem(newApplicationAction));
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

    JMenu toolsMenu = new JMenu("Tools");
    toolsMenu.add(newBootStrapAction);
    toolsMenu.add(newAnnotDiffAction);
    menuBar.add(toolsMenu);

    JMenu helpMenu = new JMenu("Help");
    helpMenu.add(helpAboutAction);
    menuBar.add(helpMenu);

    this.setJMenuBar(menuBar);

    //TOOLBAR
    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    //toolbar.add(new JGateButton(newProjectAction));

    this.getContentPane().add(toolbar, BorderLayout.NORTH);

    //extra stuff
    fileChooser = new JFileChooser();
    fileChooser.setMultiSelectionEnabled(false);
    newResourceDialog = new NewResourceDialog(this,
                                              "Resource parameters",
                                              true);
    waitDialog = new WaitDialog(this, "");

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
    Gate.getCreoleRegister().addCreoleListener(this);

    resourcesTree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        //where inside the tree?
        int x = e.getX();
        int y = e.getY();
        TreePath path = resourcesTree.getPathForLocation(x, y);
        JPopupMenu popup = null;
        ResourceHandle handle = null;
        if(path != null){
          Object value = path.getLastPathComponent();
          if(value == resourcesTreeRoot){
          } else if(value == applicationsRoot){
            popup = appsPopup;
          } else if(value == languageResourcesRoot){
            popup = lrsPopup;
          } else if(value == processingResourcesRoot){
            popup = prsPopup;
          } else if(value == datastoresRoot){
            popup = dssPopup;
          }else{
            value = ((DefaultMutableTreeNode)value).getUserObject();
            if(value instanceof ResourceHandle){
              handle = (ResourceHandle)value;
              popup = handle.getPopup();
            }
          }
        }
        if (SwingUtilities.isRightMouseButton(e)) {
          if(popup != null){
            popup.show(resourcesTree, e.getX(), e.getY());
          }
        } else if(SwingUtilities.isLeftMouseButton(e)) {
          if(e.getClickCount() == 2 && handle != null) {
            //double click - show the resource
            if(handle.isShown()) {
              //select
              JComponent largeView = handle.getLargeView();
              if(largeView != null) {
                mainTabbedPane.setSelectedComponent(largeView);
              }
              JComponent smallView = handle.getSmallView();
              if(smallView != null) {
                lowerScroll.getViewport().setView(smallView);
              } else {
                lowerScroll.getViewport().setView(null);
              }
            } else {
              //show
              JComponent largeView = handle.getLargeView();
              if(largeView != null) {
                mainTabbedPane.addTab(handle.getTitle(), handle.getIcon(),
                                      largeView, handle.getTooltipText());
                mainTabbedPane.setSelectedComponent(handle.getLargeView());
              }
              JComponent smallView = handle.getSmallView();
              if(smallView != null) {
                lowerScroll.getViewport().setView(smallView);
              } else {
                lowerScroll.getViewport().setView(null);
              }
              handle.setShown(true);
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

    //blink the messages tab when new information is displayed
    logArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
      public void insertUpdate(javax.swing.event.DocumentEvent e){
        changeOccured();
      }
      public void removeUpdate(javax.swing.event.DocumentEvent e){
        changeOccured();
      }
      public void changedUpdate(javax.swing.event.DocumentEvent e){
        changeOccured();
      }
      protected void changeOccured(){
        logBlinker.startBlinking();
      }
    });

    logArea.addPropertyChangeListener("document", new PropertyChangeListener(){
      public void propertyChange(PropertyChangeEvent evt){
        //add the document listener
        logArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
          public void insertUpdate(javax.swing.event.DocumentEvent e){
            changeOccured();
          }
          public void removeUpdate(javax.swing.event.DocumentEvent e){
            changeOccured();
          }
          public void changedUpdate(javax.swing.event.DocumentEvent e){
            changeOccured();
          }
          protected void changeOccured(){
            logBlinker.startBlinking();
          }
        });
      }
    });
  }

  public void progressChanged(int i) {
    progressBar.setStringPainted(true);
    int oldValue = progressBar.getValue();
    if(oldValue != i){
      SwingUtilities.invokeLater(new ProgressBarUpdater(i));
    }
  }

  /**
   * Called when the process is finished.
   *
   */
  public void processFinished() {
    progressBar.setStringPainted(false);
    SwingUtilities.invokeLater(new ProgressBarUpdater(0));
  }

  public void statusChanged(String text) {
    SwingUtilities.invokeLater(new StatusBarUpdater(text));
  }

  public void resourceLoaded(CreoleEvent e) {
    Resource res = e.getResource();
    String hidden = (String)res.getFeatures().get("gate.hidden");
    if(hidden != null && hidden.equalsIgnoreCase("true")) return;
    DefaultResourceHandle handle = new DefaultResourceHandle(res);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(handle, false);
    if(res instanceof ProcessingResource){
      if(res instanceof SerialController){
        handle = new ApplicationHandle((SerialController)res);
        node = new DefaultMutableTreeNode(handle, false);
        resourcesTreeModel.insertNodeInto(node, applicationsRoot, 0);
      }else{
        resourcesTreeModel.insertNodeInto(node, processingResourcesRoot, 0);
      }
    }else if(res instanceof LanguageResource){
      resourcesTreeModel.insertNodeInto(node, languageResourcesRoot, 0);
    }
  }

  public void resourceUnloaded(CreoleEvent e) {
    Resource res = e.getResource();
    String hidden = (String)res.getFeatures().get("gate.hidden");
    if(hidden != null && hidden.equalsIgnoreCase("true")) return;
    DefaultMutableTreeNode node;
    DefaultMutableTreeNode parent = null;
    if(res instanceof ProcessingResource){
      if(res instanceof Controller){
        parent = applicationsRoot;
      }else{
        parent = processingResourcesRoot;
      }
    }else if(res instanceof LanguageResource){
      parent = languageResourcesRoot;
    }
    if(parent != null){
      Enumeration children = parent.children();
      while(children.hasMoreElements()){
        node = (DefaultMutableTreeNode)children.nextElement();
        if(((ResourceHandle)node.getUserObject()).getResource() == res){
          resourcesTreeModel.removeNodeFromParent(node);
          return;
        }
      }
    }
  }

  /**Called when a {@link gate.Datastore} has been opened*/
  public void datastoreOpened(CreoleEvent e){
    DataStore ds = e.getDatastore();
    if(ds != null){
      //make sure he have a name
      if(ds.getFeatures() == null) {
        FeatureMap features = Factory.newFeatureMap();
        features.put("gate.NAME", ds.getStorageUrl().getFile());
        ds.setFeatures(features);
      } else if(ds.getFeatures().get("gate.NAME") == null){
        ds.getFeatures().put("gate.NAME", ds.getStorageUrl().getFile());
      }
      DSHandle handle = new DSHandle(ds);
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(handle, false);
      resourcesTreeModel.insertNodeInto(node, datastoresRoot, 0);
    }

  }

  /**Called when a {@link gate.Datastore} has been created*/
  public void datastoreCreated(CreoleEvent e){
    datastoreOpened(e);
  }

  /**Called when a {@link gate.Datastore} has been closed*/
  public void datastoreClosed(CreoleEvent e){
  }

  static {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e) {
      throw new gate.util.GateRuntimeException(e.toString());
    }
  }

/*
  void remove(CustomResourceHandle handle){
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
      node = (DefaultMutableTreeNode)node.getNextSibling();
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
*/
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

/*
  protected void addProject(ProjectData pData) {
    openProjects.add(pData);
    projectComboModel.addElement(pData);
    projectComboModel.setSelectedItem(pData);
  }
*/
  /**
   * Makes the necessary GUI adjustements when a new project becomes current.
   */
/*
  protected void setCurrentProject(ProjectData project){
    if(currentProject == project) return;
    currentProject = project;

    if(!openProjects.contains(project)) openProjects.add(project);

    CustomResourceHandle handle =
                  new CustomResourceHandle(project.toString(), currentProject);
    handle.setIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/project.gif")));
    projectTreeRoot.setUserObject(handle);

    projectTreeRoot.removeAllChildren();
    mainTabbedPane.removeAll();
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        mainTabbedPane.insertTab("Messages",null, logScroll, "Gate log", 0);
      }
    });


    handle = new CustomResourceHandle("Applications", currentProject);
    handle.setIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/applications.gif")));
    appRoot = new DefaultMutableTreeNode(handle, true);
    JPopupMenu popup = new JPopupMenu();
    popup.add(newApplicationAction);
    handle.setPopup(popup);
    projectTreeRoot.add(appRoot);
    Iterator resIter = currentProject.getApplicationsList().iterator();
    while(resIter.hasNext()){
      handle = (CustomResourceHandle)resIter.next();
      appRoot.add(new DefaultMutableTreeNode(handle));
      if(handle.isShown() && handle.getLargeView() != null){
        mainTabbedPane.addTab(handle.getTitle(), handle.getLargeView());
      }
    }


    handle = new CustomResourceHandle("Language Resources", currentProject);
    handle.setIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/lrs.gif")));
    lrRoot = new DefaultMutableTreeNode(handle, true);
    popup = new JPopupMenu();
    popup.add(newLRAction);
    handle.setPopup(popup);
    projectTreeRoot.add(lrRoot);
    resIter = currentProject.getLRList().iterator();
    while(resIter.hasNext()){
      handle = (CustomResourceHandle)resIter.next();
      lrRoot.add(new DefaultMutableTreeNode(handle));
      if(handle.isShown() && handle.getLargeView() != null){
        mainTabbedPane.addTab(handle.getTitle(), handle.getLargeView());
      }
    }

    handle = new CustomResourceHandle("Processing Resources", currentProject);
    handle.setIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/prs.gif")));
    prRoot = new DefaultMutableTreeNode(handle, true);
    popup = new JPopupMenu();
    popup.add(newPRAction);
    handle.setPopup(popup);
    projectTreeRoot.add(prRoot);
    resIter = currentProject.getPRList().iterator();
    while(resIter.hasNext()){
      handle = (CustomResourceHandle)resIter.next();
      prRoot.add(new DefaultMutableTreeNode(handle));
      if(handle.isShown() && handle.getLargeView() != null){
        mainTabbedPane.addTab(handle.getTitle(), handle.getLargeView());
      }
    }

    handle = new CustomResourceHandle("Data Stores", currentProject);
    handle.setIcon(new ImageIcon(
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
        features.put("gate.NAME", "Unnamed datasource");
        ds.setFeatures(features);
      }else if(ds.getFeatures().get("gate.NAME") == null){
        ds.getFeatures().put("gate.NAME", "Unnamed datasource");
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
  }//protected void setCurrentProject(ProjectData project)
*/

  synchronized void showWaitDialog() {
    Point location = getLocationOnScreen();
    location.translate(10,
              getHeight() - waitDialog.getHeight() - southBox.getHeight() - 10);
    waitDialog.setLocation(location);
    waitDialog.showDialog(new Component[]{});
  }

  synchronized void  hideWaitDialog() {
    waitDialog.goAway();
  }

/*
  class NewProjectAction extends AbstractAction {
    public NewProjectAction(){
      super("New Project", new ImageIcon(MainFrame.class.getResource(
                                        "/gate/resources/img/newProject.gif")));
      putValue(SHORT_DESCRIPTION,"Create a new project");
    }
    public void actionPerformed(ActionEvent e){
      fileChooser.setDialogTitle("Select new project file");
      fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);
      if(fileChooser.showOpenDialog(parentFrame) == fileChooser.APPROVE_OPTION){
        ProjectData pData = new ProjectData(fileChooser.getSelectedFile(),
                                                                  parentFrame);
        addProject(pData);
      }
    }
  }
*/

  class NewAnnotDiffAction extends AbstractAction {
    public NewAnnotDiffAction() {
      super("Annotation Diff",
      new ImageIcon(MainFrame.class.getResource(
                                          "/gate/resources/img/annDiff.gif")));
      putValue(SHORT_DESCRIPTION,"Create a new Annotation Diff Tool");
    }// NewAnnotDiffAction
    public void actionPerformed(ActionEvent e) {
/*      AnnotDiffHandle handle = new AnnotDiffHandle(thisMainFrame);
      handle.setTooltipText("<html><b>Tool:</b> " +
                            "Annotation diff" + "</html>");
      handle.setTitle("Annotation Diff");

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
*/
      AnnotDiffDialog annotDiffDialog = new AnnotDiffDialog(MainFrame.this);
      annotDiffDialog.setTitle("Annotation Diff Tool");
      annotDiffDialog.setVisible(true);
    }// actionPerformed();
  }//class NewAnnotDiffAction

  class NewBootStrapAction extends AbstractAction {
    public NewBootStrapAction() {
      super("BootStrap Wizard",
      new ImageIcon(MainFrame.class.getResource(
                                          "/gate/resources/img/annDiff.gif")));
    }// NewBootStrapAction
    public void actionPerformed(ActionEvent e) {
      BootStrapDialog bootStrapDialog = new BootStrapDialog(MainFrame.this);
      bootStrapDialog.show();
    }// actionPerformed();
  }//class NewBootStrapAction

  class NewApplicationAction extends AbstractAction {
    public NewApplicationAction() {
      super("New Application");
      putValue(SHORT_DESCRIPTION,"Create a new Application");
    }
    public void actionPerformed(ActionEvent e) {
      Object answer = JOptionPane.showInputDialog(
                        MainFrame.this,
                        "Please provide a name for the new application:",
                        "Gate",
                        JOptionPane.QUESTION_MESSAGE);
      if (answer instanceof String) {
        try{
          FeatureMap features = Factory.newFeatureMap();
          features.put("gate.NAME", answer);
          SerialController controller =
                (SerialController)Factory.createResource(
                                "gate.creole.SerialController",
                                Factory.newFeatureMap(), features);
/*
          ApplicationHandle handle = new ApplicationHandle(controller,
                                                           currentProject);
          currentProject.addApplication(handle);
          //resourcesTreeModel.treeChanged();
          resourcesTree.expandPath(new TreePath(
                                    new Object[]{resourcesTreeRoot,
                                                 applicationsRoot}));
*/
        } catch(ResourceInstantiationException rie){
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Could not create application!\n" +
                                         rie.toString(),
                                        "Gate", JOptionPane.ERROR_MESSAGE);
        }
      } else{
        JOptionPane.showMessageDialog(MainFrame.this,
                                      "Unrecognised input!",
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }

  }

  class NewLRAction extends AbstractAction {
    public NewLRAction() {
      super("Create language resource");
      putValue(SHORT_DESCRIPTION,"Create a new language resource");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run(){
          CreoleRegister reg = Gate.getCreoleRegister();
          List lrTypes = reg.getPublicLrTypes();
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
                                MainFrame.this,
                                "Select type of Language resource",
                                "Gate", JOptionPane.QUESTION_MESSAGE,
                                null, lrNames.toArray(),
                                lrNames.get(0));
            if(answer != null){
              ResourceData rData = (ResourceData)resourcesByName.get(answer);
              newResourceDialog.setTitle(
                                  "Parameters for the new " + rData.getName());
              LanguageResource res = (LanguageResource)
                                                  newResourceDialog.show(rData);
/*
              if(res != null){
                LRHandle handle = new LRHandle(res, currentProject);
                handle.setTooltipText("<html><b>Type:</b> " +
                                      rData.getName() + "</html>");
                handleForResourceName.put(res.getFeatures().get("gate.NAME"), handle);
                //lrRoot.add(new DefaultMutableTreeNode(handle, false));
                //projectTreeModel.nodeStructureChanged(lrRoot);
                resourcesTree.expandPath(new TreePath(
                            new Object[]{resourcesTreeRoot,
                              languageResourcesRoot}));
                //currentProject.addLR(handle);
                statusChanged(res.getFeatures().get("gate.NAME") + " loaded!");
              }
*/
            }
          }else{
            //no lr types
            JOptionPane.showMessageDialog(MainFrame.this,
                                      "Could not find any registered types " +
                                      "of resources...\n" +
                                      "Check your Gate installation!",
                                      "Gate", JOptionPane.ERROR_MESSAGE);
          }
        }//public void run()
      };
      Thread thread = new Thread(runnable);
      thread.setPriority(thread.MIN_PRIORITY);
      thread.start();
    }
  }//class NewLRAction extends AbstractAction


  class NewPRAction extends AbstractAction {
    public NewPRAction() {
      super("Create processing resource");
      putValue(SHORT_DESCRIPTION,"Create a new processing resource");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          CreoleRegister reg = Gate.getCreoleRegister();
          List prTypes = reg.getPublicPrTypes();
          if(prTypes != null && !prTypes.isEmpty()){
            HashMap resourcesByName = new HashMap();
            Iterator lrIter = prTypes.iterator();
            while(lrIter.hasNext()) {
              ResourceData rData = (ResourceData)reg.get(lrIter.next());
              resourcesByName.put(rData.getName(), rData);
            }
            List prNames = new ArrayList(resourcesByName.keySet());
            Collections.sort(prNames);
            Object answer = JOptionPane.showInputDialog(
                                MainFrame.this,
                                "Select type of Processing resource",
                                "Gate", JOptionPane.QUESTION_MESSAGE,
                                null, prNames.toArray(),
                                prNames.get(0));
            if(answer != null){
              ResourceData rData = (ResourceData)resourcesByName.get(answer);
              newResourceDialog.setTitle(
                                  "Parameters for the new " + rData.getName());
              ProcessingResource res = (ProcessingResource)
                                                  newResourceDialog.show(rData);
/*
              if(res != null){
                PRHandle handle = new PRHandle(res, currentProject);
                handle.setTooltipText("<html><b>Type:</b> " +
                                      rData.getName() + "</html>");
                handleForResourceName.put(res.getFeatures().get("gate.NAME"), handle);
                //prRoot.add(new DefaultMutableTreeNode(handle, false));
                //projectTreeModel.nodeStructureChanged(prRoot);
                resourcesTree.expandPath(new TreePath(new Object[]{
                                resourcesTreeRoot, processingResourcesRoot}));
                //currentProject.addPR(handle);
                statusChanged(res.getFeatures().get("gate.NAME") + " loaded!");
              }
*/
            }
          } else {
            //no lr types
            JOptionPane.showMessageDialog(MainFrame.this,
                                        "Could not find any registered types " +
                                        "of resources...\n" +
                                        "Check your Gate installation!",
                                        "Gate", JOptionPane.ERROR_MESSAGE);
          }
        }//public void run()
      };
      Thread thread = new Thread(runnable);
      thread.setPriority(thread.MIN_PRIORITY);
      thread.start();
    }
  }//class NewPRAction extends AbstractAction

  class NewDSAction extends AbstractAction {
    public NewDSAction(){
      super("Create datastore");
      putValue(SHORT_DESCRIPTION,"Create a new Datastore");
    }

    public void actionPerformed(ActionEvent e) {
      DataStoreRegister reg = Gate.getDataStoreRegister();
      Map dsTypes = reg.getDataStoreClassNames();
      HashMap dsTypeByName = new HashMap();
      Iterator dsTypesIter = dsTypes.entrySet().iterator();
      while(dsTypesIter.hasNext()){
        Map.Entry entry = (Map.Entry)dsTypesIter.next();
        dsTypeByName.put(entry.getValue(), entry.getKey());
      }

      if(!dsTypeByName.isEmpty()) {
        Object[] names = dsTypeByName.keySet().toArray();
        Object answer = JOptionPane.showInputDialog(
                            MainFrame.this,
                            "Select type of Datastore",
                            "Gate", JOptionPane.QUESTION_MESSAGE,
                            null, names,
                            names[0]);
        if(answer != null) {
          String className = (String)dsTypeByName.get(answer);
          if(className.indexOf("SerialDataStore") != -1){
            //get the URL (a file in this case)
            fileChooser.setDialogTitle("Select a new directory");
            fileChooser.setFileSelectionMode(fileChooser.DIRECTORIES_ONLY);
            if(fileChooser.showOpenDialog(MainFrame.this) ==
                                                  fileChooser.APPROVE_OPTION){
              try {
                URL dsURL = fileChooser.getSelectedFile().toURL();
                DataStore ds = Factory.createDataStore(className, dsURL);
/*
                if(ds != null){
                  //make sure he have a name
                  if(ds.getFeatures() == null){
                    FeatureMap features = Factory.newFeatureMap();
                    features.put("gate.NAME", dsURL.getFile());
                    ds.setFeatures(features);
                  } else if(ds.getFeatures().get("gate.NAME") == null) {
                    ds.getFeatures().put("gate.NAME", dsURL.getFile());
                  }
                  DSHandle handle = new DSHandle(ds, currentProject);
                  handleForResourceName.put(ds.getFeatures().get("gate.NAME"),
                                                                        handle);
                  //resourcesTreeModel.treeChanged();
                  //dsRoot.add(new DefaultMutableTreeNode(handle, false));
                  //projectTreeModel.nodeStructureChanged(dsRoot);
                }
*/
              } catch(MalformedURLException mue) {
                JOptionPane.showMessageDialog(
                    MainFrame.this, "Invalid location for the datastore\n " +
                                      mue.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
              } catch(PersistenceException pe) {
                JOptionPane.showMessageDialog(
                    MainFrame.this, "Datastore creation error!\n " +
                                      pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
              }
            }
          } else {
            throw new UnsupportedOperationException("Unimplemented option!\n"+
                                                    "Use a serial datastore");
          }
        }
      } else {
        //no ds types
        JOptionPane.showMessageDialog(MainFrame.this,
                                      "Could not find any registered types " +
                                      "of datastores...\n" +
                                      "Check your Gate installation!",
                                      "Gate", JOptionPane.ERROR_MESSAGE);

      }
    }
  }//class NewDSAction extends AbstractAction


  class OpenDSAction extends AbstractAction {
    public OpenDSAction() {
      super("Open datastore");
      putValue(SHORT_DESCRIPTION,"Open a datastore");
    }

    public void actionPerformed(ActionEvent e) {
      DataStoreRegister reg = Gate.getDataStoreRegister();
      Map dsTypes = reg.getDataStoreClassNames();
      HashMap dsTypeByName = new HashMap();
      Iterator dsTypesIter = dsTypes.entrySet().iterator();
      while(dsTypesIter.hasNext()){
        Map.Entry entry = (Map.Entry)dsTypesIter.next();
        dsTypeByName.put(entry.getValue(), entry.getKey());
      }

      if(!dsTypeByName.isEmpty()) {
        Object[] names = dsTypeByName.keySet().toArray();
        Object answer = JOptionPane.showInputDialog(
                            MainFrame.this,
                            "Select type of Datastore",
                            "Gate", JOptionPane.QUESTION_MESSAGE,
                            null, names,
                            names[0]);
        if(answer != null) {
          String className = (String)dsTypeByName.get(answer);
          if(className.indexOf("SerialDataStore") != -1){
            //get the URL (a file in this case)
            fileChooser.setDialogTitle("Select the datastore directory");
            fileChooser.setFileSelectionMode(fileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showOpenDialog(MainFrame.this) ==
                                                  fileChooser.APPROVE_OPTION){
              try {
                URL dsURL = fileChooser.getSelectedFile().toURL();
                DataStore ds = Factory.openDataStore(className, dsURL);
              } catch(MalformedURLException mue) {
                JOptionPane.showMessageDialog(
                    MainFrame.this, "Invalid location for the datastore\n " +
                                      mue.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
              } catch(PersistenceException pe) {
                JOptionPane.showMessageDialog(
                    MainFrame.this, "Datastore opening error!\n " +
                                      pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
              }
            }
          }else{
            throw new UnsupportedOperationException("Unimplemented option!\n"+
                                                    "Use a serial datastore");
          }
        }
      } else {
        //no ds types
        JOptionPane.showMessageDialog(MainFrame.this,
                                      "Could not find any registered types " +
                                      "of datastores...\n" +
                                      "Check your Gate installation!",
                                      "Gate", JOptionPane.ERROR_MESSAGE);

      }
    }
  }//class OpenDSAction extends AbstractAction

  class HelpAboutAction extends AbstractAction {
    public HelpAboutAction(){
      super("About");
    }

    public void actionPerformed(ActionEvent e) {
      splash.show();
    }
  }

  protected class ResourceTreeCellRenderer extends DefaultTreeCellRenderer {
    public ResourceTreeCellRenderer() {
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

      if(value == resourcesTreeRoot) {
        setIcon(new ImageIcon(getClass().getResource(
                                          "/gate/resources/img/project.gif")));
      } else if(value == applicationsRoot) {
        setIcon(new ImageIcon(getClass().getResource(
                                    "/gate/resources/img/applications.gif")));
      } else if(value == languageResourcesRoot) {
        setIcon(new ImageIcon(getClass().getResource(
                                              "/gate/resources/img/lrs.gif")));
      } else if(value == processingResourcesRoot) {
        setIcon(new ImageIcon(getClass().getResource(
                                              "/gate/resources/img/prs.gif")));
      } else if(value == datastoresRoot) {
        setIcon(new ImageIcon(getClass().getResource(
                                              "/gate/resources/img/dss.gif")));
      }else{
        //not one of the default root nodes
        value = ((DefaultMutableTreeNode)value).getUserObject();
        if(value instanceof ResourceHandle) {
            setIcon(((ResourceHandle)value).getIcon());
            setText(((ResourceHandle)value).getTitle());
            setToolTipText(((ResourceHandle)value).getTooltipText());
        }
      }
      return this;
    }

    public Component getTreeCellRendererComponent1(JTree tree,
                                              Object value,
                                              boolean sel,
                                              boolean expanded,
                                              boolean leaf,
                                              int row,
                                              boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, selected, expanded,
                                         leaf, row, hasFocus);
      Object handle = ((DefaultMutableTreeNode)value).getUserObject();
      if(handle != null && handle instanceof ResourceHandle){
        setIcon(((ResourceHandle)handle).getIcon());
        setText(((ResourceHandle)handle).getTitle());
        setToolTipText(((ResourceHandle)handle).getTooltipText());
      }
      return this;
    }
  }


  /**
   * Model for the tree representing the resources loaded in the system
   */
/*
  class ResourcesTreeModel extends DefaultTreeModel {
    ResourcesTreeModel(TreeNode root){
      super(root);
    }

    public Object getRoot(){
      return resourcesTreeRoot;
    }

    public Object getChild(Object parent,
                       int index){
      return getChildren(parent).get(index);
    }

    public int getChildCount(Object parent){
      return getChildren(parent).size();
    }

    public boolean isLeaf(Object node){
      return getChildren(node).isEmpty();
    }

    public int getIndexOfChild(Object parent,
                           Object child){
      return getChildren(parent).indexOf(child);
    }

    protected List getChildren(Object parent) {
      List result = new ArrayList();
      if(parent == resourcesTreeRoot){
        result.add(applicationsRoot);
        result.add(languageResourcesRoot);
        result.add(processingResourcesRoot);
        result.add(datastoresRoot);
      } else if(parent == applicationsRoot) {
//        result.addAll(currentProject.getApplicationsList());
      } else if(parent == languageResourcesRoot) {
        result.addAll(Gate.getCreoleRegister().getLrInstances());
      } else if(parent == processingResourcesRoot) {
        result.addAll(Gate.getCreoleRegister().getPrInstances());
      } else if(parent == datastoresRoot) {
        result.addAll(Gate.getDataStoreRegister());
      }
      ListIterator iter = result.listIterator();
      while(iter.hasNext()) {
        Object value = iter.next();
        ResourceData rData = (ResourceData)
                      Gate.getCreoleRegister().get(value.getClass().getName());
        if(rData != null && rData.isPrivate()) iter.remove();
      }
      return result;
    }

    public synchronized void removeTreeModelListener(TreeModelListener l) {
      if (treeModelListeners != null && treeModelListeners.contains(l)) {
        Vector v = (Vector) treeModelListeners.clone();
        v.removeElement(l);
        treeModelListeners = v;
      }
    }

    public synchronized void addTreeModelListener(TreeModelListener l) {
      Vector v = treeModelListeners ==
                    null ? new Vector(2) : (Vector) treeModelListeners.clone();
      if (!v.contains(l)) {
        v.addElement(l);
        treeModelListeners = v;
      }
    }

    void treeChanged(){
      SwingUtilities.invokeLater(new Runnable(){
        public void run() {
          fireTreeStructureChanged(new TreeModelEvent(
                                        this,new Object[]{resourcesTreeRoot}));
        }
      });
    }

    public void valueForPathChanged(TreePath path,
                                Object newValue){
      fireTreeNodesChanged(new TreeModelEvent(this,path));
    }

    protected void fireTreeNodesChanged(TreeModelEvent e) {
      if (treeModelListeners != null) {
        Vector listeners = treeModelListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
          ((TreeModelListener) listeners.elementAt(i)).treeNodesChanged(e);
        }
      }
    }

    protected void fireTreeNodesInserted(TreeModelEvent e) {
      if (treeModelListeners != null) {
        Vector listeners = treeModelListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
          ((TreeModelListener) listeners.elementAt(i)).treeNodesInserted(e);
        }
      }
    }

    protected void fireTreeNodesRemoved(TreeModelEvent e) {
      if (treeModelListeners != null) {
        Vector listeners = treeModelListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
          ((TreeModelListener) listeners.elementAt(i)).treeNodesRemoved(e);
        }
      }
    }

    protected void fireTreeStructureChanged(TreeModelEvent e) {
      if (treeModelListeners != null) {
        Vector listeners = treeModelListeners;
        int count = listeners.size();
        for (int i = 0; i < count; i++) {
          ((TreeModelListener) listeners.elementAt(i)).treeStructureChanged(e);
        }
      }
    }

    private transient Vector treeModelListeners;
  }
*/

  class ProgressBarUpdater implements Runnable{
    ProgressBarUpdater(int newValue){
      value = newValue;
    }
    public void run(){
      progressBar.setValue(value);
//      progressBar.paintImmediately(progressBar.getBounds());
    }

    int value;
  }

  class StatusBarUpdater implements Runnable {
    StatusBarUpdater(String text){
      this.text = text;
    }
    public void run(){
      statusBar.setText(text);
    }
    String text;
  }

  class JGateMenuItem extends JMenuItem {
    JGateMenuItem(javax.swing.Action a){
      super(a);
      this.addMouseListener(new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
          oldText = statusBar.getText();
          statusChanged((String)getAction().
                        getValue(javax.swing.Action.SHORT_DESCRIPTION));
        }

        public void mouseExited(MouseEvent e) {
          statusChanged(oldText);
        }
      });
    }
    String oldText;
  }

  class JGateButton extends JButton {
    JGateButton(javax.swing.Action a){
      super(a);
      this.addMouseListener(new MouseAdapter() {
        public void mouseEntered(MouseEvent e) {
          oldText = statusBar.getText();
          statusChanged((String)getAction().
                        getValue(javax.swing.Action.SHORT_DESCRIPTION));
        }

        public void mouseExited(MouseEvent e) {
          statusChanged(oldText);
        }
      });
    }
    String oldText;
  }

  class LocaleSelectorMenuItem extends JRadioButtonMenuItem {
    public LocaleSelectorMenuItem(Locale locale, JFrame pframe) {
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
  }//class LocaleSelectorMenuItem extends JRadioButtonMenuItem

}