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

import gate.*;
import gate.creole.*;
import gate.util.*;


public class MainFrame extends JFrame {

  JPanel contentPane;
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
  }

  protected void initGuiComponents(){
    parentFrame = this;

    this.getContentPane().setLayout(new BorderLayout());
    this.setSize(new Dimension(800, 600));
    this.setTitle("Gate 2.0");


    ResourceHandle handle = new ResourceHandle(null, "<No Projects>");
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/project.gif")));
    projectTreeRoot = new DefaultMutableTreeNode(handle, true);
    projectTreeModel = new DefaultTreeModel(projectTreeRoot, true);
    projectTree = new JTree(projectTreeModel);
    projectTree.setCellRenderer(new ResourceTreeCellRenderer());
    projectTree.setRowHeight(0);
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
    mainTabbedPane.add(logScroll, "Messages");
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
        if(SwingUtilities.isRightMouseButton(e)){
          //where inside the tree?
          int x = e.getX();
          int y = e.getY();
          TreePath path = projectTree.getPathForLocation(x, y);
          if(path != null){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.
                                         getLastPathComponent();
            ResourceHandle handle = (ResourceHandle)node.getUserObject();
            JPopupMenu popup = handle.getPopup();
            if(popup != null){
              popup.show(projectTree, e.getX(), e.getY());
            }
          }
        }//if(SwingUtilities.isRightMouseButton(e))
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

    ResourceHandle handle = new ResourceHandle(null, project.toString());
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/project.gif")));
    projectTreeRoot.setUserObject(handle);

    projectTreeRoot.removeAllChildren();

    handle = new ResourceHandle(null, "Applications");
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/applications.gif")));
    appRoot = new DefaultMutableTreeNode(handle, true);
    JPopupMenu popup = new JPopupMenu();
    popup.add(newApplicationAction);
    handle.setPopup(popup);
    projectTreeRoot.add(appRoot);


    handle = new ResourceHandle(null, "Language Resources");
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/lrs.gif")));
    lrRoot = new DefaultMutableTreeNode(handle, true);
    popup = new JPopupMenu();
    popup.add(newLRAction);
    handle.setPopup(popup);

    projectTreeRoot.add(lrRoot);

    handle = new ResourceHandle(null, "Processing Resources");
    handle.setSmallIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/prs.gif")));
    prRoot = new DefaultMutableTreeNode(handle, true);
    projectTreeRoot.add(prRoot);

    handle = new ResourceHandle(null, "Data Stores");
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
                            lrTypes.toArray()[0]);
        if(answer != null){
          ResourceData rData = (ResourceData)resourcesByName.get(answer);
          LanguageResource res = (LanguageResource)newResourceDialog.show(rData);
          if(res != null){
            LRHandle handle = new LRHandle(res, rData.getName());
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
      }
      return this;
    }
  }
}