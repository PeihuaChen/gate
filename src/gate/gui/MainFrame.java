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
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import java.util.*;
import java.io.*;
import java.net.URL;

import gate.*;
import gate.creole.*;
import gate.util.*;


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


  JTextArea logArea;
  JScrollPane logScroll;
  JComboBox projectCombo;
  DefaultComboBoxModel projectComboModel;
  JToolBar toolbar;

  JFileChooser fileChooser;
  JFrame parentFrame;
  NewResourceDialog newResourceDialog;

  List openProjects;
  ProjectData currentProject;

  NewApplicationAction newApplicationAction;
  NewProjectAction newProjectAction;
  NewLRAction newLRAction;
  NewPRAction newPRAction;

  /**Construct the frame*/
  public MainFrame() {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    initLocalData();
    initGuiComponents();
    initListeners();
  }
  /**Component initialization*/
  private void jbInit() throws Exception  {
    /*
    //setIconImage(Toolkit.getDefaultToolkit().createImage(MainFrame.class.getResource("[Your Icon]")));
    contentPane = (JPanel) this.getContentPane();
    contentPane.setLayout(borderLayout1);
    this.setSize(new Dimension(800, 600));
    this.setTitle("Gate 2.0");

    //menus
    jMenuFile.setText("File");
    jMenuFileExit.setText("Exit");
    jMenuFileExit.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        jMenuFileExit_actionPerformed(e);
      }
    });
    jMenuHelp.setText("Help");
    jMenuHelpAbout.setText("About");
    jMenuHelpAbout.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent e) {
        jMenuHelpAbout_actionPerformed(e);
      }
    });
    jMenuFile.add(jMenuFileExit);
    jMenuHelp.add(jMenuHelpAbout);
    jMenuBar1.add(jMenuFile);
    jMenuBar1.add(jMenuHelp);
    this.setJMenuBar(jMenuBar1);

    leftSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
    leftSplit.add(upperTreeScroll, JSplitPane.TOP);
    upperTreeScroll.getViewport().add(upperTree, null);
    leftSplit.add(lowerTreeScroll, JSplitPane.BOTTOM);
    lowerTreeScroll.getViewport().add(lowerTree, null);
    mainSplit.add(leftSplit, JSplitPane.LEFT);

    logArea.setText("Gate 2 started at " + new Date());
    rightTabbedPane.add(logArea, "Messages");
    mainSplit.add(rightTabbedPane, JSplitPane.RIGHT);
    contentPane.add(mainSplit, BorderLayout.CENTER);

    statusBar.setText(" ");
    southBox = Box.createHorizontalBox();
    southBox.add(statusBar, null);
    southBox.add(progressBar, null);
    contentPane.add(southBox, BorderLayout.SOUTH);
    */
  }

  protected void initLocalData(){
    openProjects = new ArrayList();
    newApplicationAction = new NewApplicationAction();
    newProjectAction = new NewProjectAction();
    newLRAction = new NewLRAction();
    newPRAction = new NewPRAction();
  }

  protected void initGuiComponents(){
    parentFrame = this;

    this.getContentPane().setLayout(new BorderLayout());
    this.setSize(new Dimension(800, 600));
    this.setTitle("Gate 2.0");


    ResourceHandle handle = new ResourceHandle("<No Projects>");
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/project.gif")));
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
    menuBar.add(fileMenu);

    JMenu helpMenu = new JMenu("Help");
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
                mainTabbedPane.setSelectedComponent(handle.getLargeView());
              }else{
                //show
                JComponent largeView = handle.getLargeView();
                if(largeView != null){
                  mainTabbedPane.addTab(handle.getTitle(),null, largeView,
                                        handle.getTooltipText());
                  mainTabbedPane.setSelectedComponent(handle.getLargeView());
                }
                JComponent smallView = handle.getSmallView();
                if(smallView != null){
                  lowerScroll.getViewport().setView(smallView);
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

  /**File | Exit action performed*/
  public void jMenuFileExit_actionPerformed(ActionEvent e) {
    System.exit(0);
  }
  /**Help | About action performed*/
  public void jMenuHelpAbout_actionPerformed(ActionEvent e) {
    MainFrame_AboutBox dlg = new MainFrame_AboutBox(this);
    Dimension dlgSize = dlg.getPreferredSize();
    Dimension frmSize = getSize();
    Point loc = getLocation();
    dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
    dlg.setModal(true);
    dlg.show();
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

    ResourceHandle handle = new ResourceHandle(project.toString());
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


    handle = new ResourceHandle("Applications");
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


    handle = new ResourceHandle("Language Resources");
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

    handle = new ResourceHandle("Processing Resources");
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

    handle = new ResourceHandle("Data Stores");
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/dss.gif")));
    dsRoot = new DefaultMutableTreeNode(handle, true);
    projectTreeRoot.add(dsRoot);
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
      super("New Project");
    }
    public void actionPerformed(ActionEvent e){
      fileChooser.setDialogTitle("Select new project file");
      if(fileChooser.showOpenDialog(parentFrame) == fileChooser.APPROVE_OPTION){
        ProjectData pData = new ProjectData(fileChooser.getSelectedFile());
        addProject(pData);
      }
    }
  }

  class NewApplicationAction extends AbstractAction{
    public NewApplicationAction(){
      super("New");
    }
    public void actionPerformed(ActionEvent e){
      Object answer = JOptionPane.showInputDialog(
                        parentFrame,
                        "Please provide a name for the new application:",
                        "Gate",
                        JOptionPane.QUESTION_MESSAGE);
      if(answer instanceof String){
        ApplicationHandle handle = new ApplicationHandle((String)answer);
        appRoot.add(new DefaultMutableTreeNode(handle));
        projectTreeModel.nodeStructureChanged(appRoot);
        projectTree.expandPath(new TreePath(projectTreeModel.getPathToRoot(appRoot)));

        currentProject.addApplication(handle);
      }else{
        JOptionPane.showMessageDialog(parentFrame,
                                      "Unrecognised input!",
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  class NewLRAction extends AbstractAction{
    public NewLRAction(){
      super("Create new");
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
            LRHandle handle = new LRHandle(res);
            handle.setTooltipText("<html><b>Type:</b> " +
                                  rData.getName() + "</html>");
            lrRoot.add(new DefaultMutableTreeNode(handle));
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
      super("Create new");
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
            PRHandle handle = new PRHandle(res);
            handle.setTooltipText("<html><b>Type:</b> " +
                                  rData.getName() + "</html>");
            prRoot.add(new DefaultMutableTreeNode(handle));
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
}