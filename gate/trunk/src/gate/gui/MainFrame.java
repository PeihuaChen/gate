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
import java.awt.Window;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.beans.*;

import java.util.*;
import java.io.*;
import java.net.*;

import gate.*;

import gate.creole.*;
import gate.event.*;
import gate.persist.*;
import gate.util.*;
import gate.swing.*;
import guk.im.*;


/**
 * The main Gate GUI frame.
 */
public class MainFrame extends JFrame
                    implements ProgressListener, StatusListener, CreoleListener{

  JMenuBar menuBar;
  JSplitPane mainSplit;
  JSplitPane leftSplit;
  Box southBox;
  JLabel statusBar;
  JProgressBar progressBar;
  XJTabbedPane mainTabbedPane;
  JScrollPane projectTreeScroll;
  JScrollPane lowerScroll;

  JPopupMenu appsPopup;
  JPopupMenu dssPopup;
  JPopupMenu lrsPopup;
  JPopupMenu prsPopup;

  /** used in popups */
  JMenu newLrsPopupMenu;
  JMenu newPrsPopupMenu;

  /** used in menu bar */
  JMenu newLrMenu;
  JMenu newPrMenu;

  JTree resourcesTree;
  JScrollPane resourcesTreeScroll;
  DefaultTreeModel resourcesTreeModel;
  DefaultMutableTreeNode resourcesTreeRoot;
  DefaultMutableTreeNode applicationsRoot;
  DefaultMutableTreeNode languageResourcesRoot;
  DefaultMutableTreeNode processingResourcesRoot;
  DefaultMutableTreeNode datastoresRoot;




  Splash splash;
  LogArea logArea;
  JScrollPane logScroll;
  JToolBar toolbar;
  static JFileChooser fileChooser;

  ApperanceDialog appearanceDialog;
  CartoonMinder animator;
  TabBlinker logBlinker;
  NewResourceDialog newResourceDialog;
  WaitDialog waitDialog;

  NewApplicationAction newApplicationAction;
  NewLRAction newLRAction;
  NewPRAction newPRAction;
  NewDSAction newDSAction;
  OpenDSAction openDSAction;
  HelpAboutAction helpAboutAction;
  NewAnnotDiffAction newAnnotDiffAction = null;
  NewBootStrapAction newBootStrapAction = null;

  /**
   * all the top level containers of this application; needed for changes of
   * look and feel
   */
  Component[] targets;

  /**
   * Holds all the icons used in the Gate GUI indexed by filename.
   * This is needed so we do not need to decode the icon everytime
   * we need it as that would use unecessary CPU time and memory.
   * Access to this data is avaialable through the {@link #getIcon()}
   * method.
   */
  static Map iconByName;

  static public Icon getIcon(String filename){
    Icon result = (Icon)iconByName.get(filename);
    if(result == null){
      try{
        result = new ImageIcon(new URL("gate:/img/" + filename));
        iconByName.put(filename, result);
      }catch(MalformedURLException mue){
        mue.printStackTrace(Err.getPrintWriter());
      }
    }
    return result;
  }


/*
  static public MainFrame getInstance(){
    if(instance == null) instance = new MainFrame();
    return instance;
  }
*/

  static public JFileChooser getFileChooser(){
    return fileChooser;
  }

  protected void select(ResourceHandle handle){
    if(mainTabbedPane.indexOfComponent(handle.getLargeView()) != -1) {
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
    }
  }//protected void select(ResourceHandle handle)

  /**Construct the frame*/
  public MainFrame() {
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

    newApplicationAction = new NewApplicationAction();
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
    try{
      this.setIconImage(Toolkit.getDefaultToolkit().getImage(
            new URL("gate:/img/gateIcon.gif")));
    }catch(MalformedURLException mue){
      mue.printStackTrace(Err.getPrintWriter());
    }

    resourcesTree = new JTree(resourcesTreeModel){
      public void updateUI(){
        super.updateUI();
        setRowHeight(0);
      }
    };

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
    JPanel lowerPane = new JPanel();
    lowerPane.setLayout(new OverlayLayout(lowerPane));

    JPanel animationPane = new JPanel();
    animationPane.setOpaque(false);
    animationPane.setLayout(new BoxLayout(animationPane, BoxLayout.X_AXIS));

    JPanel vBox = new JPanel();
    vBox.setLayout(new BoxLayout(vBox, BoxLayout.Y_AXIS));
    vBox.setOpaque(false);

    JPanel hBox = new JPanel();
    hBox.setLayout(new BoxLayout(hBox, BoxLayout.X_AXIS));
    hBox.setOpaque(false);

    vBox.add(Box.createVerticalGlue());
    vBox.add(animationPane);

    hBox.add(vBox);
    hBox.add(Box.createHorizontalGlue());

    lowerPane.add(hBox);
    lowerPane.add(lowerScroll);

    animator = new CartoonMinder(animationPane);
    Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                               animator);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();

    leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                               resourcesTreeScroll, lowerPane);


    // Create a new logArea and redirect the Out and Err output to it.
    logArea = new LogArea();
    logScroll = new JScrollPane(logArea);
    // Out has been redirected to the logArea
    Out.prln("Gate 2 started at: " + new Date().toString());
    mainTabbedPane = new XJTabbedPane(JTabbedPane.TOP);
    mainTabbedPane.insertTab("Messages",null, logScroll, "Gate log", 0);
    logBlinker = new TabBlinker(mainTabbedPane, logScroll, Color.red);


    mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                               leftSplit, mainTabbedPane);
    this.getContentPane().add(mainSplit, BorderLayout.CENTER);

    southBox = Box.createHorizontalBox();
    statusBar = new JLabel();
    progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
    progressBar.setBorderPainted(false);
    progressBar.setStringPainted(false);
    progressBar.setOrientation(JProgressBar.HORIZONTAL);
    Dimension dim = new Dimension(300, progressBar.getPreferredSize().height);
    progressBar.setMaximumSize(dim);
    progressBar.setMinimumSize(dim);
    progressBar.setPreferredSize(dim);
    Box tempBox = Box.createHorizontalBox();
    southBox.add(statusBar);
    southBox.add(Box.createHorizontalGlue());
    southBox.add(progressBar);

    this.getContentPane().add(southBox, BorderLayout.SOUTH);


    //TOOLBAR
    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    //toolbar.add(new JGateButton(newProjectAction));

    this.getContentPane().add(toolbar, BorderLayout.NORTH);

    //extra stuff
    newResourceDialog = new NewResourceDialog(
      this, "Resource parameters", true
    );
    waitDialog = new WaitDialog(this, "");

    //build the Help->About dialog
    JPanel splashBox = new JPanel();
    splashBox.setLayout(new BoxLayout(splashBox, BoxLayout.Y_AXIS));
    splashBox.setBackground(Color.white);

    JLabel gifLbl = new JLabel(getIcon("gateSplash.gif"));
    Box box = new Box(BoxLayout.X_AXIS);
    box.add(Box.createHorizontalGlue());
    box.add(gifLbl);
    box.add(Box.createHorizontalGlue());
    splashBox.add(box);

    gifLbl = new JLabel(getIcon("gateHeader.gif"));
    box = new Box(BoxLayout.X_AXIS);
    box.add(gifLbl);
    box.add(Box.createHorizontalGlue());
    splashBox.add(box);
    splashBox.add(Box.createVerticalStrut(10));

    JLabel verLbl = new JLabel(
      "<HTML><FONT color=\"blue\">Version <B>"
      + Main.version + "</B></FONT>" +
      ", <FONT color=\"red\">build <B>" + Main.build + "</B></FONT></HTML>"
    );
    box = new Box(BoxLayout.X_AXIS);
    box.add(Box.createHorizontalGlue());
    box.add(verLbl);

    splashBox.add(box);
    splashBox.add(Box.createVerticalStrut(10));

    verLbl = new JLabel(
      "<HTML><B>GATE contributors:</B><BR>" +
      "Hamish Cunningham, Valentin Tablan, Cristian Ursu, " +
      "Kalina Bontcheva,<BR>" +
      "Oana Hamza, Diana Maynard, Yorick Wilks, Robert Gaizauskas,<BR>" +
      "Mark Leisher, Kevin Humphreys."
    );
    box = new Box(BoxLayout.X_AXIS);
    box.add(verLbl);
    box.add(Box.createHorizontalGlue());

    splashBox.add(box);

    JButton okBtn = new JButton("OK");
    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        splash.hide();
      }
    });
    okBtn.setBackground(Color.white);
    box = new Box(BoxLayout.X_AXIS);
    box.add(Box.createHorizontalGlue());
    box.add(okBtn);
    box.add(Box.createHorizontalGlue());

    splashBox.add(Box.createVerticalStrut(10));
    splashBox.add(box);
    splashBox.add(Box.createVerticalStrut(10));
    splash = new Splash(this, splashBox);


    targets = new Component[]{this, newResourceDialog, splash};

    //MENUS
    menuBar = new JMenuBar();


    JMenu fileMenu = new JMenu("File");

    newLrMenu = new JMenu("New language resource");
    fileMenu.add(newLrMenu);
    newPrMenu = new JMenu("New processing resource");
    fileMenu.add(newPrMenu);
    fileMenu.add(new XJMenuItem(newApplicationAction, this));
    fileMenu.addSeparator();
    fileMenu.add(new XJMenuItem(newDSAction, this));
    fileMenu.add(new XJMenuItem(openDSAction, this));
    fileMenu.addSeparator();
    fileMenu.add(new XJMenuItem(new AbstractAction("Exit"){
      {
        putValue(SHORT_DESCRIPTION, "Exits the application");
      }
      public void actionPerformed(ActionEvent evt){
        System.exit(0);
      }
    }, this));
    menuBar.add(fileMenu);



    JMenu optionsMenu = new JMenu("Options");
    appearanceDialog = new ApperanceDialog(this, "Fonts", true, targets);
    optionsMenu.add(new XJMenuItem(new AbstractAction("Fonts"){
      {
        putValue(SHORT_DESCRIPTION, "Set the fonts used in the application");
      }
      public void actionPerformed(ActionEvent evt){
        appearanceDialog.setLocationRelativeTo(MainFrame.this);
        appearanceDialog.show(targets);
      }
    }, this));

    JMenu lnfMenu = new JMenu("Look & Feel");
    ButtonGroup lnfBg = new ButtonGroup();

    UIManager.LookAndFeelInfo[] lnfs = UIManager.getInstalledLookAndFeels();
    class SetLNFAction extends AbstractAction{
      SetLNFAction(UIManager.LookAndFeelInfo info){
        super(info.getName());
        this.info = info;
        putValue(SHORT_DESCRIPTION, "Switch to " + info.getName() +
                                   " look-and-feel");
      }
      public void actionPerformed(ActionEvent evt) {
        try{
          UIManager.setLookAndFeel(info.getClassName());
          for(int i = 0; i< targets.length; i++){
            if(targets[i] instanceof Window){
              SwingUtilities.updateComponentTreeUI(targets[i]);
            }else{
              SwingUtilities.updateComponentTreeUI(SwingUtilities.getRoot(targets[i]));
            }
          }
        }catch(Exception e){
          e.printStackTrace(Err.getPrintWriter());
        }
      }
      UIManager.LookAndFeelInfo info;
    };

    for(int i = 0; i < lnfs.length; i++){
      UIManager.LookAndFeelInfo lnf = lnfs[i];
      try{
        Class lnfClass = Class.forName(lnf.getClassName());
        if(((LookAndFeel)(lnfClass.newInstance())).isSupportedLookAndFeel()){
          JRadioButtonMenuItem item = new JRadioButtonMenuItem(new SetLNFAction(lnf));
          if(lnf.getName().equals(UIManager.getLookAndFeel().getName())){
            item.setSelected(true);
          }
          lnfBg.add(item);
          lnfMenu.add(item);
        }
      }catch(ClassNotFoundException cnfe){
      }catch(IllegalAccessException iae){
      }catch(InstantiationException ie){
      }
    }

    optionsMenu.add(lnfMenu);

    try{
      JMenu imMenu = null;
      Locale locale = new Locale("en", "GB");
      Locale[] availableLocales = new GateIMDescriptor().getAvailableLocales();
      //Locale[] availableLocales = Locale.getAvailableLocales();
      JMenuItem item;
      if(availableLocales != null && availableLocales.length > 1){
        imMenu = new JMenu("Input methods");
        ButtonGroup bg = new ButtonGroup();
        item = new LocaleSelectorMenuItem(this);
        imMenu.add(item);
        item.setSelected(true);
        imMenu.addSeparator();
        bg.add(item);
        for(int i = 0; i < availableLocales.length; i++){
          locale = availableLocales[i];
          item = new LocaleSelectorMenuItem(locale, this);
          imMenu.add(item);
          bg.add(item);
        }
      }
      if(imMenu != null) optionsMenu.add(imMenu);
    }catch(AWTException awte){}

    menuBar.add(optionsMenu);


    JMenu toolsMenu = new JMenu("Tools");
    toolsMenu.add(newAnnotDiffAction);
    toolsMenu.add(newBootStrapAction);
    toolsMenu.add(
      new AbstractAction("Unicode editor", getIcon("unicode.gif")){
      public void actionPerformed(ActionEvent evt){
        new guk.Editor();
      }
    });
    menuBar.add(toolsMenu);

    JMenu helpMenu = new JMenu("Help");
    helpMenu.add(helpAboutAction);
    menuBar.add(helpMenu);

    this.setJMenuBar(menuBar);

    //popups
    appsPopup = new JPopupMenu();
    appsPopup.add(newApplicationAction);

    newLrsPopupMenu = new JMenu("New");
    lrsPopup = new JPopupMenu();
    lrsPopup.add(newLrsPopupMenu);

    newPrsPopupMenu = new JMenu("New");
    prsPopup = new JPopupMenu();
    prsPopup.add(newPrsPopupMenu);

    dssPopup = new JPopupMenu();
    dssPopup.add(newDSAction);
    dssPopup.add(openDSAction);

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
            select(handle);
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

    mainTabbedPane.getModel().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JComponent largeView = (JComponent)mainTabbedPane.getSelectedComponent();
        Enumeration nodesEnum = resourcesTreeRoot.preorderEnumeration();
        boolean done = false;
        DefaultMutableTreeNode node = resourcesTreeRoot;
        while(!done && nodesEnum.hasMoreElements()){
          node = (DefaultMutableTreeNode)nodesEnum.nextElement();
          done = node.getUserObject() instanceof ResourceHandle &&
                 ((ResourceHandle)node.getUserObject()).getLargeView()
                  == largeView;
        }
        if(done){
          select((ResourceHandle)node.getUserObject());
        }else{
          //the selected item is not a resource (maiber the log area?)
          lowerScroll.getViewport().setView(null);
        }
      }
    });

    mainTabbedPane.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)){
          int index = mainTabbedPane.getIndexAt(e.getPoint());
          if(index != -1){
            JComponent view = (JComponent)mainTabbedPane.getComponentAt(index);
            Enumeration nodesEnum = resourcesTreeRoot.preorderEnumeration();
            boolean done = false;
            DefaultMutableTreeNode node = resourcesTreeRoot;
            while(!done && nodesEnum.hasMoreElements()){
              node = (DefaultMutableTreeNode)nodesEnum.nextElement();
              done = node.getUserObject() instanceof ResourceHandle &&
                     ((ResourceHandle)node.getUserObject()).getLargeView()
                      == view;
            }
            if(done){
              ResourceHandle handle = (ResourceHandle)node.getUserObject();
              JPopupMenu popup = handle.getPopup();
              popup.show(mainTabbedPane, e.getX(), e.getY());
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

    newLrMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent e) {
      }
      public void menuDeselected(MenuEvent e) {
      }
      public void menuSelected(MenuEvent e) {
        newLrMenu.removeAll();
        //find out the available types of LRs and repopulate the menu
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
          lrIter = lrNames.iterator();
          while(lrIter.hasNext()){
            ResourceData rData = (ResourceData)resourcesByName.
                                 get(lrIter.next());
            newLrMenu.add(new XJMenuItem(new NewResourceAction(rData),
                                         MainFrame.this));
          }
        }
      }
    });

    newPrMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent e) {
      }
      public void menuDeselected(MenuEvent e) {
      }
      public void menuSelected(MenuEvent e) {
        newPrMenu.removeAll();
        //find out the available types of LRs and repopulate the menu
        CreoleRegister reg = Gate.getCreoleRegister();
        List prTypes = reg.getPublicPrTypes();
        if(prTypes != null && !prTypes.isEmpty()){
          HashMap resourcesByName = new HashMap();
          Iterator prIter = prTypes.iterator();
          while(prIter.hasNext()){
            ResourceData rData = (ResourceData)reg.get(prIter.next());
            resourcesByName.put(rData.getName(), rData);
          }
          List prNames = new ArrayList(resourcesByName.keySet());
          Collections.sort(prNames);
          prIter = prNames.iterator();
          while(prIter.hasNext()){
            ResourceData rData = (ResourceData)resourcesByName.
                                 get(prIter.next());
            newPrMenu.add(new XJMenuItem(new NewResourceAction(rData),
                                         MainFrame.this));
          }
        }
      }
    });

    newLrsPopupMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent e) {
      }
      public void menuDeselected(MenuEvent e) {
      }
      public void menuSelected(MenuEvent e) {
        newLrsPopupMenu.removeAll();
        //find out the available types of LRs and repopulate the menu
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
          lrIter = lrNames.iterator();
          while(lrIter.hasNext()){
            ResourceData rData = (ResourceData)resourcesByName.
                                 get(lrIter.next());
            newLrsPopupMenu.add(new XJMenuItem(new NewResourceAction(rData),
                                         MainFrame.this));
          }
        }
      }
    });

    newPrsPopupMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent e) {
      }
      public void menuDeselected(MenuEvent e) {
      }
      public void menuSelected(MenuEvent e) {
        newPrsPopupMenu.removeAll();
        //find out the available types of LRs and repopulate the menu
        CreoleRegister reg = Gate.getCreoleRegister();
        List prTypes = reg.getPublicPrTypes();
        if(prTypes != null && !prTypes.isEmpty()){
          HashMap resourcesByName = new HashMap();
          Iterator prIter = prTypes.iterator();
          while(prIter.hasNext()){
            ResourceData rData = (ResourceData)reg.get(prIter.next());
            resourcesByName.put(rData.getName(), rData);
          }
          List prNames = new ArrayList(resourcesByName.keySet());
          Collections.sort(prNames);
          prIter = prNames.iterator();
          while(prIter.hasNext()){
            ResourceData rData = (ResourceData)resourcesByName.
                                 get(prIter.next());
            newPrsPopupMenu.add(new XJMenuItem(new NewResourceAction(rData),
                                         MainFrame.this));
          }
        }
      }
    });
  }//protected void initListeners()

  public void progressChanged(int i) {
    //progressBar.setStringPainted(true);
    int oldValue = progressBar.getValue();
    if(oldValue != i){
      if(!animator.isActive()) animator.activate();
      SwingUtilities.invokeLater(new ProgressBarUpdater(i));
    }
  }

  /**
   * Called when the process is finished.
   *
   */
  public void processFinished() {
    //progressBar.setStringPainted(false);
    SwingUtilities.invokeLater(new ProgressBarUpdater(0));
    animator.deactivate();
  }

  public void statusChanged(String text) {
    SwingUtilities.invokeLater(new StatusBarUpdater(text));
  }

  public void resourceLoaded(CreoleEvent e) {
    Resource res = e.getResource();
    if(Gate.isHidden(res)) return;
    DefaultResourceHandle handle = new DefaultResourceHandle(res);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(handle, false);
    if(res instanceof ProcessingResource){
      if(Gate.isApplication(res)){
        handle = new ApplicationHandle((SerialController)res, this, this);
        node = new DefaultMutableTreeNode(handle, false);
        resourcesTreeModel.insertNodeInto(node, applicationsRoot, 0);
      }else{
        resourcesTreeModel.insertNodeInto(node, processingResourcesRoot, 0);
      }
    }else if(res instanceof LanguageResource){
      resourcesTreeModel.insertNodeInto(node, languageResourcesRoot, 0);
    }

    if(handle instanceof DefaultResourceHandle){
      ((DefaultResourceHandle)handle).addProgressListener(MainFrame.this);
      ((DefaultResourceHandle)handle).addStatusListener(MainFrame.this);
    }
  }

  public void resourceUnloaded(CreoleEvent e) {
    Resource res = e.getResource();
    String hidden = (String)res.getFeatures().get("gate.HIDDEN");
    if(hidden != null && hidden.equalsIgnoreCase("true")) return;
    DefaultMutableTreeNode node;
    DefaultMutableTreeNode parent = null;
    if(res instanceof ProcessingResource){
      if(Gate.isApplication(res)){
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
          ResourceHandle handle = (ResourceHandle)node.getUserObject();
          if(mainTabbedPane.indexOfComponent(handle.getLargeView()) != -1){
            mainTabbedPane.remove(handle.getLargeView());
          }
          if(lowerScroll.getViewport().getView() == handle.getSmallView()){
            lowerScroll.getViewport().setView(null);
          }
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
    DataStore ds = e.getDatastore();
    DefaultMutableTreeNode node;
    DefaultMutableTreeNode parent = datastoresRoot;
    if(parent != null){
      Enumeration children = parent.children();
      while(children.hasMoreElements()){
        node = (DefaultMutableTreeNode)children.nextElement();
        if(((DSHandle)node.getUserObject()).getDataStore() == ds){
          resourcesTreeModel.removeNodeFromParent(node);
          DSHandle handle = (DSHandle)node.getUserObject();
          if(mainTabbedPane.indexOfComponent(handle.getLargeView()) != -1){
            mainTabbedPane.remove(handle.getLargeView());
          }
          if(lowerScroll.getViewport().getView() == handle.getSmallView()){
            lowerScroll.getViewport().setView(null);
          }
          return;
        }
      }
    }
  }

  static {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e) {
      throw new gate.util.GateRuntimeException(e.toString());
    }
    if(fileChooser == null){
      fileChooser = new JFileChooser();
      fileChooser.setMultiSelectionEnabled(false);
    }
    iconByName = new HashMap();
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

/*
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
*/

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
      super("Annotation Diff", getIcon("annDiff.gif"));
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
      super("BootStrap Wizard", getIcon("annDiff.gif"));
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
      if(answer == null) return;
      if (answer instanceof String) {
        try{
          FeatureMap features = Factory.newFeatureMap();
          features.put("gate.NAME", answer);
          features.put("gate.APPLICATION", "true");
          SerialController controller =
                (SerialController)Factory.createResource(
                                "gate.creole.SerialController",
                                Factory.newFeatureMap(), features);
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

  class NewResourceAction extends AbstractAction {
    public NewResourceAction(ResourceData rData) {
      super(rData.getName());
      putValue(SHORT_DESCRIPTION,"Create a new " + rData.getName());
      this.rData = rData;
    }

    public void actionPerformed(ActionEvent evt) {
      Runnable runnable = new Runnable(){
        public void run(){
          newResourceDialog.setTitle(
                              "Parameters for the new " + rData.getName());
          newResourceDialog.show(rData);
        }
      };
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable);
      thread.start();
    }

    ResourceData rData;
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
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable);
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
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable);
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
            fileChooser.setDialogTitle("Please create a new empty directory");
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
        setIcon(MainFrame.getIcon("project.gif"));
      } else if(value == applicationsRoot) {
        setIcon(MainFrame.getIcon("applications.gif"));
      } else if(value == languageResourcesRoot) {
        setIcon(MainFrame.getIcon("lrs.gif"));
      } else if(value == processingResourcesRoot) {
        setIcon(MainFrame.getIcon("prs.gif"));
      } else if(value == datastoresRoot) {
        setIcon(MainFrame.getIcon("dss.gif"));
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

  /**
   * During longer operations it is nice to keep the user entertained so
   * (s)he doesn't fall asleep looking at a progress bar that seems have
   * stopped. Also there are some operations that do not support progress
   * reporting so the progress bar would not work at all so we need a way
   * to let the user know that things are happening. We chose for purpose
   * to show the user a small cartoon in the form of an animated gif.
   * This class handles the diplaying and updating of those cartoons.
   */
  class CartoonMinder implements Runnable{

    CartoonMinder(JPanel targetPanel){
      active = false;
      dying = false;
      this.targetPanel = targetPanel;
      imageLabel = new JLabel(getIcon("working.gif"));
      imageLabel.setOpaque(false);
      imageLabel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
    }

    public boolean isActive(){
      boolean res;
      synchronized(lock){
        res = active;
      }
      return res;
    }

    public void activate(){
      //add the label in the panel
      SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          targetPanel.add(imageLabel);
        }
      });
      //wake the dorment thread
      synchronized(lock){
        active = true;
      }
    }

    public void deactivate(){
      //send the thread to sleep
      synchronized(lock){
        active = false;
      }
      //clear the panel
      SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          targetPanel.removeAll();
          targetPanel.paintImmediately(targetPanel.getBounds());
        }
      });
    }

    public void dispose(){
      synchronized(lock){
        dying = true;
      }
    }

    public void run(){
      boolean isDying;
      synchronized(lock){
        isDying = dying;
      }
      while(!isDying){
        boolean isActive;
        synchronized(lock){
          isActive = active;
        }
        if(isActive && targetPanel.isVisible()){
          SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              targetPanel.repaint();
            }
          });
        }
        //sleep
        try{
          Thread.sleep(300);
        }catch(InterruptedException ie){}

        synchronized(lock){
          isDying = dying;
        }
      }//while(!isDying)
    }

    boolean dying;
    boolean active;
    String lock = "lock";
    JPanel targetPanel;
    JLabel imageLabel;
  }

/*
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
*/
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

    public LocaleSelectorMenuItem(JFrame pframe) {
      super("System default  >>" +
            Locale.getDefault().getDisplayName() + "<<");
      this.frame = pframe;
      me = this;
      myLocale = Locale.getDefault();
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