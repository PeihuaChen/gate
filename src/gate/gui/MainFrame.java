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
import java.awt.GraphicsEnvironment;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import javax.swing.plaf.FontUIResource;

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
import gate.security.*;
import junit.framework.*;
//import guk.im.*;


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
  JMenu newAppPopupMenu;

  /** used in menu bar */
  JMenu newLrMenu;
  JMenu newPrMenu;
  JMenu newAppMenu;
  JMenu loadANNIEMenu = null;
  JButton stopBtn;
  Action stopAction;

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

  AppearanceDialog appearanceDialog;
  CartoonMinder animator;
  TabBlinker logBlinker;
  NewResourceDialog newResourceDialog;
  WaitDialog waitDialog;

  NewDSAction newDSAction;
  OpenDSAction openDSAction;
  HelpAboutAction helpAboutAction;
  NewAnnotDiffAction newAnnotDiffAction = null;
  NewBootStrapAction newBootStrapAction = null;
//  NewCorpusEvalAction newCorpusEvalAction = null;
  /**
   * all the top level containers of this application; needed for changes of
   * look and feel
   */
  Component[] targets;

  /**
   * Holds all the icons used in the Gate GUI indexed by filename.
   * This is needed so we do not need to decode the icon everytime
   * we need it as that would use unecessary CPU time and memory.
   * Access to this data is avaialable through the {@link #getIcon(String)}
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

  protected void select(Handle handle){
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

//    newApplicationAction = new NewApplicationAction();
    newDSAction = new NewDSAction();
    openDSAction = new OpenDSAction();
    helpAboutAction = new HelpAboutAction();
    newAnnotDiffAction = new NewAnnotDiffAction();
    newBootStrapAction = new NewBootStrapAction();
//    newCorpusEvalAction = new NewCorpusEvalAction();
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
                               animator,
                               "MainFrame1");
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

    mainSplit.setDividerLocation(leftSplit.getPreferredSize().width + 10);
    this.getContentPane().add(mainSplit, BorderLayout.CENTER);

    southBox = Box.createHorizontalBox();
    statusBar = new JLabel();

    UIManager.put("ProgressBar.cellSpacing", new Integer(0));
    progressBar = new JProgressBar(JProgressBar.HORIZONTAL){
      public Dimension getPreferredSize(){
        Dimension pSize = super.getPreferredSize();
        pSize.height = 5;
        return pSize;
      }
    };
    progressBar.setBorder(BorderFactory.createEmptyBorder());
    progressBar.setForeground(new Color(150, 75, 150));
    progressBar.setBorderPainted(false);
    progressBar.setStringPainted(false);
    progressBar.setOrientation(JProgressBar.HORIZONTAL);
    progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));

    Box sbBox = Box.createHorizontalBox();
    sbBox.add(statusBar);
    sbBox.add(new JLabel(" "));
    sbBox.add(Box.createHorizontalGlue());
    Box tempVBox = Box.createVerticalBox();
    tempVBox.add(sbBox);
    tempVBox.add(progressBar);
    stopAction = new StopAction();
    stopAction.setEnabled(false);
    stopBtn = new JButton(stopAction);
//    stopBtn.setBorder(  BorderFactory.createLineBorder(Color.black, 1));BorderFactory.createEtchedBorder()
    stopBtn.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createEmptyBorder(2,3,2,3),
                                    BorderFactory.createLineBorder(Color.black,
                                                                   1)));
    stopBtn.setForeground(Color.red);

    southBox.add(Box.createRigidArea(
                     new Dimension(5, stopBtn.getPreferredSize().height)));
    southBox.add(tempVBox);
    southBox.add(Box.createHorizontalStrut(5));

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
      "<HTML>" +
      "<B>Hamish Cunningham, Valentin Tablan, Cristian Ursu, " +
      "Kalina Bontcheva</B>,<BR>" +
      "Diana Maynard, Marin Dimitrov, Horacio Saggion, Oana Hamza,<BR>" +
      "Robert Gaizauskas, Mark Hepple, Mark Leisher, Kevin Humphreys,<BR>" +
      "Yorick Wilks." +
      "<P><B>JVM version</B>: " + System.getProperty("java.version") +
      " from " + System.getProperty("java.vendor")
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

    newAppMenu = new JMenu("New application");
    fileMenu.add(newAppMenu);

    fileMenu.addSeparator();
    fileMenu.add(new XJMenuItem(new LoadResourceFromFileAction(), this));

    fileMenu.addSeparator();
    fileMenu.add(new XJMenuItem(newDSAction, this));
    fileMenu.add(new XJMenuItem(openDSAction, this));
    fileMenu.addSeparator();
    loadANNIEMenu = new JMenu("Load ANNIE system");
    fileMenu.add(loadANNIEMenu);
    fileMenu.add(new XJMenuItem(new LoadCreoleRepositoryAction(), this));
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
    appearanceDialog = new AppearanceDialog(this, "Fonts", true, targets);
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
    };//class SetLNFAction extends AbstractAction

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
      //if this fails guk is not present
      Class.forName("guk.im.GateIMDescriptor");
      Locale[] availableLocales = new guk.im.GateIMDescriptor().getAvailableLocales();
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
    }catch(Exception e){
      //something happened; most probably guk not present.
      //just drop it, is not vital.
    }

    menuBar.add(optionsMenu);


    JMenu toolsMenu = new JMenu("Tools");
    toolsMenu.add(newAnnotDiffAction);
    toolsMenu.add(newBootStrapAction);
    //temporarily disabled till the evaluation tools are made to run within
    //the GUI
//    toolsMenu.add(newCorpusEvalAction);
    toolsMenu.add(
      new AbstractAction("Unicode editor", getIcon("unicode.gif")){
      public void actionPerformed(ActionEvent evt){
        new guk.Editor();
      }
    });
    menuBar.add(toolsMenu);

    JMenu helpMenu = new JMenu("Help");
    helpMenu.add(new HelpUserGuideAction());
    helpMenu.add(helpAboutAction);
    menuBar.add(helpMenu);

    this.setJMenuBar(menuBar);

    //popups
    newAppPopupMenu = new JMenu("New");
    appsPopup = new JPopupMenu();
    appsPopup.add(newAppPopupMenu);

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
        Handle handle = null;
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
            if(value instanceof Handle){
              handle = (Handle)value;
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

    // Add the keyboard listeners for CTRL+F4 and ALT+F4
    this.addKeyListener(new KeyAdapter() {
      public void keyTyped(KeyEvent e) {
      }

      public void keyPressed(KeyEvent e) {
        // If Ctrl+F4 was pressed then close the active resource
        if (e.isControlDown() && e.getKeyCode()==KeyEvent.VK_F4){
          JComponent resource = (JComponent)
                                        mainTabbedPane.getSelectedComponent();
          if (resource != null){
            Action act = resource.getActionMap().get("Close resource");
            if (act != null)
              act.actionPerformed(null);
          }// End if
        }// End if
        // If CTRL+H was pressed then hide the active view.
        if (e.isControlDown() && e.getKeyCode()==KeyEvent.VK_H){
          JComponent resource = (JComponent)
                                        mainTabbedPane.getSelectedComponent();
          if (resource != null){
            Action act = resource.getActionMap().get("Hide current view");
            if (act != null)
              act.actionPerformed(null);
          }// End if
        }// End if
        // If CTRL+X was pressed then save as XML
        if (e.isControlDown() && e.getKeyCode()==KeyEvent.VK_X){
          JComponent resource = (JComponent)
                                        mainTabbedPane.getSelectedComponent();
          if (resource != null){
            Action act = resource.getActionMap().get("Save As XML");
            if (act != null)
              act.actionPerformed(null);
          }// End if
        }// End if
      }// End keyPressed();

      public void keyReleased(KeyEvent e) {
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
          done = node.getUserObject() instanceof Handle &&
                 ((Handle)node.getUserObject()).getLargeView()
                  == largeView;
        }
        if(done){
          select((Handle)node.getUserObject());
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
              done = node.getUserObject() instanceof Handle &&
                     ((Handle)node.getUserObject()).getLargeView()
                      == view;
            }
            if(done){
              Handle handle = (Handle)node.getUserObject();
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

    // Adding a listener for loading ANNIE with or without defaults
    loadANNIEMenu.addMenuListener(new MenuListener(){
      public void menuCanceled(MenuEvent e){}
      public void menuDeselected(MenuEvent e){}
      public void menuSelected(MenuEvent e){
        loadANNIEMenu.removeAll();
        loadANNIEMenu.add(new LoadANNIEWithDefaultsAction());
        loadANNIEMenu.add(new LoadANNIEWithoutDefaultsAction());
      }// menuSelected();
    });//loadANNIEMenu.addMenuListener(new MenuListener()

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


    newAppMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent e) {
      }
      public void menuDeselected(MenuEvent e) {
      }
      public void menuSelected(MenuEvent e) {
        newAppMenu.removeAll();
        //find out the available types of Controllers and repopulate the menu
        CreoleRegister reg = Gate.getCreoleRegister();
        List controllerTypes = reg.getPublicControllerTypes();
        if(controllerTypes != null && !controllerTypes.isEmpty()){
          HashMap resourcesByName = new HashMap();
          Iterator controllerTypesIter = controllerTypes.iterator();
          while(controllerTypesIter.hasNext()){
            ResourceData rData = (ResourceData)reg.get(controllerTypesIter.next());
            resourcesByName.put(rData.getName(), rData);
          }
          List controllerNames = new ArrayList(resourcesByName.keySet());
          Collections.sort(controllerNames);
          controllerTypesIter = controllerNames.iterator();
          while(controllerTypesIter.hasNext()){
            ResourceData rData = (ResourceData)resourcesByName.
                                 get(controllerTypesIter.next());
            newAppMenu.add(new XJMenuItem(new NewResourceAction(rData),
                                         MainFrame.this));
          }
        }
      }
    });


    newAppPopupMenu.addMenuListener(new MenuListener() {
      public void menuCanceled(MenuEvent e) {
      }
      public void menuDeselected(MenuEvent e) {
      }
      public void menuSelected(MenuEvent e) {
        newAppPopupMenu.removeAll();
        //find out the available types of Controllers and repopulate the menu
        CreoleRegister reg = Gate.getCreoleRegister();
        List controllerTypes = reg.getPublicControllerTypes();
        if(controllerTypes != null && !controllerTypes.isEmpty()){
          HashMap resourcesByName = new HashMap();
          Iterator controllerTypesIter = controllerTypes.iterator();
          while(controllerTypesIter.hasNext()){
            ResourceData rData = (ResourceData)reg.get(controllerTypesIter.next());
            resourcesByName.put(rData.getName(), rData);
          }
          List controllerNames = new ArrayList(resourcesByName.keySet());
          Collections.sort(controllerNames);
          controllerTypesIter = controllerNames.iterator();
          while(controllerTypesIter.hasNext()){
            ResourceData rData = (ResourceData)resourcesByName.
                                 get(controllerTypesIter.next());
            newAppPopupMenu.add(new XJMenuItem(new NewResourceAction(rData),
                                         MainFrame.this));
          }
        }
      }
    });

    gate.util.persistence.PersistenceManager.
              addListener("gate.event.StatusListener", MainFrame.this);
    gate.util.persistence.PersistenceManager.
              addListener("gate.event.ProgressListener", MainFrame.this);

  }//protected void initListeners()

  public void progressChanged(int i) {
    //progressBar.setStringPainted(true);
    int oldValue = progressBar.getValue();
    if((!stopAction.isEnabled()) &&
       (Gate.getExecutable() != null)){
      stopAction.setEnabled(true);
      SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          southBox.add(stopBtn, 0);
        }
      });
    }
    if(!animator.isActive()) animator.activate();
    if(oldValue != i){
      SwingUtilities.invokeLater(new ProgressBarUpdater(i));
    }
  }

  /**
   * Called when the process is finished.
   *
   */
  public void processFinished() {
    //progressBar.setStringPainted(false);
    if(stopAction.isEnabled()){
      stopAction.setEnabled(false);
      SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          southBox.remove(stopBtn);
        }
      });
    }
    SwingUtilities.invokeLater(new ProgressBarUpdater(0));
    animator.deactivate();
  }

  public void statusChanged(String text) {
    SwingUtilities.invokeLater(new StatusBarUpdater(text));
  }

  public void resourceLoaded(CreoleEvent e) {
    Resource res = e.getResource();
    if(Gate.getHiddenAttribute(res.getFeatures())) return;
    NameBearerHandle handle = new NameBearerHandle(res, MainFrame.this);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(handle, false);
    if(res instanceof ProcessingResource){
      resourcesTreeModel.insertNodeInto(node, processingResourcesRoot, 0);
    }else if(res instanceof LanguageResource){
      resourcesTreeModel.insertNodeInto(node, languageResourcesRoot, 0);
    }else if(res instanceof Controller){
      resourcesTreeModel.insertNodeInto(node, applicationsRoot, 0);
    }

    handle.addProgressListener(MainFrame.this);
    handle.addStatusListener(MainFrame.this);

    JPopupMenu popup = handle.getPopup();
    popup.addSeparator();

    // Create a CloseViewAction and a menu item based on it
    CloseViewAction cva = new CloseViewAction(handle);
    XJMenuItem menuItem = new XJMenuItem(cva, this);
    // Add an accelerator ATL+F4 for this action
    menuItem.setAccelerator(KeyStroke.getKeyStroke(
                                      KeyEvent.VK_H, ActionEvent.CTRL_MASK));
    popup.add(menuItem);
    // Put the action command in the component's action map
    if (handle.getLargeView() != null)
      handle.getLargeView().getActionMap().put("Hide current view",cva);

  }// resourceLoaded();

  public void resourceUnloaded(CreoleEvent e) {
    Resource res = e.getResource();
    if(Gate.getHiddenAttribute(res.getFeatures())) return;
    DefaultMutableTreeNode node;
    DefaultMutableTreeNode parent = null;
    if(res instanceof ProcessingResource){
      parent = processingResourcesRoot;
    }else if(res instanceof LanguageResource){
      parent = languageResourcesRoot;
    }else if(res instanceof Controller){
      parent = applicationsRoot;
    }
    if(parent != null){
      Enumeration children = parent.children();
      while(children.hasMoreElements()){
        node = (DefaultMutableTreeNode)children.nextElement();
        if(((NameBearerHandle)node.getUserObject()).getTarget() == res){
          resourcesTreeModel.removeNodeFromParent(node);
          Handle handle = (Handle)node.getUserObject();
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

  /**Called when a {@link gate.DataStore} has been opened*/
  public void datastoreOpened(CreoleEvent e){
    DataStore ds = e.getDatastore();

    ds.setName(ds.getStorageUrl());

    NameBearerHandle handle = new NameBearerHandle(ds, MainFrame.this);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(handle, false);
    resourcesTreeModel.insertNodeInto(node, datastoresRoot, 0);
    handle.addProgressListener(MainFrame.this);
    handle.addStatusListener(MainFrame.this);

    JPopupMenu popup = handle.getPopup();
    popup.addSeparator();
    // Create a CloseViewAction and a menu item based on it
    CloseViewAction cva = new CloseViewAction(handle);
    XJMenuItem menuItem = new XJMenuItem(cva, this);
    // Add an accelerator ATL+F4 for this action
    menuItem.setAccelerator(KeyStroke.getKeyStroke(
                                      KeyEvent.VK_H, ActionEvent.CTRL_MASK));
    popup.add(menuItem);
    // Put the action command in the component's action map
    if (handle.getLargeView() != null)
      handle.getLargeView().getActionMap().put("Hide current view",cva);
  }// datastoreOpened();

  /**Called when a {@link gate.DataStore} has been created*/
  public void datastoreCreated(CreoleEvent e){
    datastoreOpened(e);
  }

  /**Called when a {@link gate.DataStore} has been closed*/
  public void datastoreClosed(CreoleEvent e){
    DataStore ds = e.getDatastore();
    DefaultMutableTreeNode node;
    DefaultMutableTreeNode parent = datastoresRoot;
    if(parent != null){
      Enumeration children = parent.children();
      while(children.hasMoreElements()){
        node = (DefaultMutableTreeNode)children.nextElement();
        if(((NameBearerHandle)node.getUserObject()).
            getTarget() == ds){
          resourcesTreeModel.removeNodeFromParent(node);
          NameBearerHandle handle = (NameBearerHandle)
                                          node.getUserObject();
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
javax.swing.plaf.basic.BasicFileChooserUI uiii;
      fileChooser.setMultiSelectionEnabled(false);
    }
    iconByName = new HashMap();
    //guess the Unicode font for the platform
    String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().
                                             getAvailableFontFamilyNames();
    String unicodeFontName = null;
    for(int i = 0; i < fontNames.length; i++){
      if(fontNames[i].equalsIgnoreCase("Arial Unicode MS")){
        unicodeFontName = fontNames[i];
        break;
      }
      if(fontNames[i].toLowerCase().indexOf("unicode") != -1){
        unicodeFontName = fontNames[i];
      }
    }//for(int i = 0; i < fontNames.length; i++)
    if(unicodeFontName != null){
      FontUIResource font = new FontUIResource(unicodeFontName,
                                               FontUIResource.PLAIN,
                                               12);
      //set font for text components
      String[] keys = AppearanceDialog.textComponentsKeys;
      for(int i = 0; i < keys.length; i++){
        UIManager.put(keys[i], font);
      }

      //set font for menus
      keys = AppearanceDialog.menuKeys;
      for(int i = 0; i < keys.length; i++){
        UIManager.put(keys[i], font);
      }

      //set font for other components
      keys = AppearanceDialog.componentsKeys;
      for(int i = 0; i < keys.length; i++){
        UIManager.put(keys[i], font);
      }
    }//if(unicodeFontName != null)
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
      ds.setName("Unnamed datasource");
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

  /** This class represent an action which brings up the Annot Diff tool*/
  class NewAnnotDiffAction extends AbstractAction {
    public NewAnnotDiffAction() {
      super("Annotation Diff", getIcon("annDiff.gif"));
      putValue(SHORT_DESCRIPTION,"Create a new Annotation Diff Tool");
    }// NewAnnotDiffAction
    public void actionPerformed(ActionEvent e) {
      AnnotDiffDialog annotDiffDialog = new AnnotDiffDialog(MainFrame.this);
      annotDiffDialog.setTitle("Annotation Diff Tool");
      annotDiffDialog.setVisible(true);
    }// actionPerformed();
  }//class NewAnnotDiffAction


  /** This class represent an action which brings up the corpus evaluation tool*/
    //DO NOT DELETE. WILL MAKE RUNNING THE EVAL TOOLS FROM GUI WORK IN NOVEMBER
    //NEEDS PUTTING IN A SEPARATE THREAD!!!!
/*  class NewCorpusEvalAction extends AbstractAction {
    public NewCorpusEvalAction() {
      super("Evaluation Tool");
      putValue(SHORT_DESCRIPTION,"Create a new Evaluation Tool");
    }// newCorpusEvalAction
    public void actionPerformed(ActionEvent e) {
      //NEEDS PUTTING IN A SEPARATE THREAD!!!!

      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle("Please select a directory which contains " +
                             "the documents to be evaluated");
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setMultiSelectionEnabled(false);
      int state = chooser.showDialog(MainFrame.this, "OK");
      File startDir = chooser.getSelectedFile();
      if (state == JFileChooser.CANCEL_OPTION || startDir == null)
        return;

      //first create the tool and set its parameters
      CorpusBenchmarkTool theTool = new CorpusBenchmarkTool();
      theTool.setStartDirectory(startDir);

      Out.prln("Please wait while GATE tools are initialised.");
      //initialise the tool
      theTool.init();
      //and execute it
      theTool.execute();

      Out.prln("Overall average precision: " + theTool.getPrecisionAverage());
      Out.prln("Overall average recall: " + theTool.getRecallAverage());

    }// actionPerformed();
  }//class NewCorpusEvalAction
*/

  /** This class represent an action which loads ANNIE with default params*/
  class LoadANNIEWithDefaultsAction extends AbstractAction {
    public LoadANNIEWithDefaultsAction() {
      super("With defaults");
    }// NewAnnotDiffAction
    public void actionPerformed(ActionEvent e) {
      // Loads ANNIE with defaults
      Runnable runnable = new Runnable(){
        public void run(){
          Map listeners = new HashMap();
          listeners.put("gate.event.ProgressListener", MainFrame.this);
          listeners.put("gate.event.StatusListener", MainFrame.this);
          FeatureMap params = Factory.newFeatureMap();
          try{
            // Load each PR as defined in gate.creole.AnnieConstants.PR_NAMES
            for(int i=0; i<gate.creole.AnnieConstants.PR_NAMES.length; i++){
              Factory.createResource( gate.creole.AnnieConstants.PR_NAMES[i],
                                      params,
                                      listeners);
            }// End for
            // Create an application at the end.
            Factory.createResource("gate.creole.SerialAnalyserController");
          }catch(gate.creole.ResourceInstantiationException ex){
            ex.printStackTrace(Err.getPrintWriter());
          }// End try
        }// run()
      };// End Runnable
      Thread thread = new Thread(runnable, "");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed();
  }//class LoadANNIEWithDefaultsAction

  /** This class represent an action which loads ANNIE without default param*/
  class LoadANNIEWithoutDefaultsAction extends AbstractAction {
    public LoadANNIEWithoutDefaultsAction() {
      super("Without defaults");
    }// NewAnnotDiffAction
    public void actionPerformed(ActionEvent e) {
      //Load ANNIE without defaults
      CreoleRegister reg = Gate.getCreoleRegister();
      // Load each PR as defined in gate.creole.AnnieConstants.PR_NAMES
      for(int i=0; i<gate.creole.AnnieConstants.PR_NAMES.length; i++){
        ResourceData resData = (ResourceData)reg.get(
                                  gate.creole.AnnieConstants.PR_NAMES[i]);
        if (resData != null){
          NewResourceDialog resourceDialog = new NewResourceDialog(
              MainFrame.this, "Resource parameters", true );
          resourceDialog.setTitle(
                            "Parameters for the new " + resData.getName());
          resourceDialog.show(resData);
        }else{
          Err.prln(gate.creole.AnnieConstants.PR_NAMES[i] +
                                        " not found in Creole register");
        }// End if
      }// End for
      try{
        // Create an application at the end.
        Factory.createResource("gate.creole.SerialAnalyserController");
      }catch(gate.creole.ResourceInstantiationException ex){
        ex.printStackTrace(Err.getPrintWriter());
      }// End try
    }// actionPerformed();
  }//class LoadANNIEWithoutDefaultsAction

  class NewBootStrapAction extends AbstractAction {
    public NewBootStrapAction() {
      super("BootStrap Wizard", getIcon("annDiff.gif"));
    }// NewBootStrapAction
    public void actionPerformed(ActionEvent e) {
      BootStrapDialog bootStrapDialog = new BootStrapDialog(MainFrame.this);
      bootStrapDialog.show();
    }// actionPerformed();
  }//class NewBootStrapAction


  class LoadCreoleRepositoryAction extends AbstractAction {
    public LoadCreoleRepositoryAction(){
      super("Load a CREOLE repository");
      putValue(SHORT_DESCRIPTION,"Load a CREOLE repository");
    }

    public void actionPerformed(ActionEvent e) {
      Box messageBox = Box.createHorizontalBox();
      Box leftBox = Box.createVerticalBox();
      JTextField urlTextField = new JTextField(20);
      leftBox.add(new JLabel("Type an URL"));
      leftBox.add(urlTextField);
      messageBox.add(leftBox);

      messageBox.add(Box.createHorizontalStrut(10));
      messageBox.add(new JLabel("or"));
      messageBox.add(Box.createHorizontalStrut(10));

      class URLfromFileAction extends AbstractAction{
        URLfromFileAction(JTextField textField){
          super(null, getIcon("loadFile.gif"));
          putValue(SHORT_DESCRIPTION,"Click to select a directory");
          this.textField = textField;
        }

        public void actionPerformed(ActionEvent e){
          fileChooser.setMultiSelectionEnabled(false);
          fileChooser.setFileSelectionMode(fileChooser.DIRECTORIES_ONLY);
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          int result = fileChooser.showOpenDialog(MainFrame.this);
          if(result == fileChooser.APPROVE_OPTION){
            try{
              textField.setText(fileChooser.getSelectedFile().
                                            toURL().toExternalForm());
            }catch(MalformedURLException mue){
              throw new GateRuntimeException(mue.toString());
            }
          }
        }
        JTextField textField;
      };//class URLfromFileAction extends AbstractAction

      Box rightBox = Box.createVerticalBox();
      rightBox.add(new JLabel("Select a directory"));
      JButton fileBtn = new JButton(new URLfromFileAction(urlTextField));
      rightBox.add(fileBtn);
      messageBox.add(rightBox);


//JOptionPane.showInputDialog(
//                            MainFrame.this,
//                            "Select type of Datastore",
//                            "Gate", JOptionPane.QUESTION_MESSAGE,
//                            null, names,
//                            names[0]);

      int res = JOptionPane.showConfirmDialog(
                            MainFrame.this, messageBox,
                            "Enter an URL to the directory containig the " +
                            "\"creole.xml\" file", JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null);
      if(res == JOptionPane.OK_OPTION){
        try{
          URL creoleURL = new URL(urlTextField.getText());
          Gate.getCreoleRegister().registerDirectories(creoleURL);
        }catch(Exception ex){
          JOptionPane.showMessageDialog(
              MainFrame.this,
              "There was a problem with your selection:\n" +
              ex.toString() ,
              "Gate", JOptionPane.ERROR_MESSAGE);
          ex.printStackTrace(Err.getPrintWriter());
        }
      }
    }
  }//class LoadCreoleRepositoryAction extends AbstractAction


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
      SwingUtilities.invokeLater(runnable);
    }
    ResourceData rData;
  }


  class StopAction extends AbstractAction {
    public StopAction(){
      super(" Stop! ");
      putValue(SHORT_DESCRIPTION,"Stops the current action");
    }

    public void actionPerformed(ActionEvent e) {
      Executable ex = Gate.getExecutable();
      if(ex != null) ex.interrupt();
    }
  }


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
          if(className.equals("gate.persist.SerialDataStore")){
            //get the URL (a file in this case)
            fileChooser.setDialogTitle("Please create a new empty directory");
            fileChooser.setFileSelectionMode(fileChooser.DIRECTORIES_ONLY);
            if(fileChooser.showOpenDialog(MainFrame.this) ==
                                                  fileChooser.APPROVE_OPTION){
              try {
                URL dsURL = fileChooser.getSelectedFile().toURL();
                DataStore ds = Factory.createDataStore(className,
                                                       dsURL.toExternalForm());
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
          } else if(className.equals("gate.persist.OracleDataStore")) {
              JOptionPane.showMessageDialog(
                    MainFrame.this, "Oracle datastores can only be created " +
                                    "by your Oracle administrator!",
                                    "Gate", JOptionPane.ERROR_MESSAGE);
          }  else {

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

  class LoadResourceFromFileAction extends AbstractAction {
    public LoadResourceFromFileAction(){
      super("Restore resource from file");
      putValue(SHORT_DESCRIPTION,"Restores a previously saved resource");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable(){
        public void run(){
          fileChooser.setDialogTitle("Select a file for this resource");
          fileChooser.setFileSelectionMode(fileChooser.FILES_AND_DIRECTORIES);
          if (fileChooser.showOpenDialog(MainFrame.this) ==
                                                fileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            try{
              gate.util.persistence.PersistenceManager.loadObjectFromFile(file);
            }catch(ResourceInstantiationException rie){
              JOptionPane.showMessageDialog(MainFrame.this,
                              "Error!\n"+
                               rie.toString(),
                               "Gate", JOptionPane.ERROR_MESSAGE);
              rie.printStackTrace(Err.getPrintWriter());
              Exception ee = rie.getException();
              while(ee != null){
                Err.prln("==>From:");
                ee.printStackTrace(Err.getPrintWriter());
                if(ee instanceof ResourceInstantiationException)
                  ee = ((ResourceInstantiationException)ee).getException();
                else if(ee instanceof ExecutionException)
                  ee = ((ExecutionException)ee).getException();
                else ee = null;
              }

            }catch(Exception ex){
              JOptionPane.showMessageDialog(MainFrame.this,
                              "Error!\n"+
                               ex.toString(),
                               "Gate", JOptionPane.ERROR_MESSAGE);
              ex.printStackTrace(Err.getPrintWriter());
            }
          }
        }
      };
      Thread thread = new Thread(runnable);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  /**
   * Closes the view associated to a resource.
   * Does not remove the resource from the system, only its view.
   */
  class CloseViewAction extends AbstractAction {
    public CloseViewAction(Handle handle) {
      super("Close this view");
      putValue(SHORT_DESCRIPTION, "Hides this view");
      this.handle = handle;
    }

    public void actionPerformed(ActionEvent e) {
      mainTabbedPane.remove(handle.getLargeView());
      mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);
    }//public void actionPerformed(ActionEvent e)
    Handle handle;
  }//class CloseViewAction

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
                DataStore ds = Factory.openDataStore(className,
                                                     dsURL.toExternalForm());
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
          } else if(className.equals("gate.persist.OracleDataStore")) {
              String storageURL = (String) reg.getConfigData().get("url");
              if (storageURL == null || storageURL.equals(""))
                throw new
                  GateRuntimeException("Oracle URL not configured in gate.xml");
              DataStore ds = null;
              AccessController ac = null;
              try {
                //1. open the oracle datastore
                ds = Factory.openDataStore(className, storageURL);

                //2. login the user
                ac = new AccessControllerImpl();
                Assert.assertNotNull(ac);
                ac.open(storageURL);

                User usr = ac.findUser("kalina");
                Assert.assertNotNull(usr);

                Group grp = (Group)usr.getGroups().get(0);
                Assert.assertNotNull(grp);

                Session usrSession = ac.login("kalina","sesame",grp.getID());
                Assert.assertNotNull(usrSession);
                Assert.assertTrue(ac.isValidSession(usrSession));

              } catch(PersistenceException pe) {
                JOptionPane.showMessageDialog(
                    MainFrame.this, "Datastore open error!\n " +
                                      pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
              } catch(gate.security.SecurityException se) {
                JOptionPane.showMessageDialog(
                    MainFrame.this, "User identification error!\n " +
                                      se.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
                try {
                  if (ac != null)
                    ac.close();
                  if (ds != null)
                    ds.close();
                } catch (gate.persist.PersistenceException ex) {
                  JOptionPane.showMessageDialog(
                      MainFrame.this, "Persistence error!\n " +
                                        se.toString(),
                                        "Gate", JOptionPane.ERROR_MESSAGE);
                }
              }
          }else{
            JOptionPane.showMessageDialog(
                            MainFrame.this,
                            "Support for this type of datastores is not implemenented!\n",
                            "Gate", JOptionPane.ERROR_MESSAGE);
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

  class HelpUserGuideAction extends AbstractAction {
    public HelpUserGuideAction(){
      super("User Guide");
    }

    public void actionPerformed(ActionEvent e) {
      try{
        HelpFrame helpFrame = new HelpFrame();
        helpFrame.setPage(new URL("gate:/userguide.html"));
        helpFrame.setSize(800, 600);
        //center on screen
        Dimension frameSize = helpFrame.getSize();
        Dimension ownerSize = Toolkit.getDefaultToolkit().getScreenSize();
        Point ownerLocation = new Point(0, 0);
        helpFrame.setLocation(
                  ownerLocation.x + (ownerSize.width - frameSize.width) / 2,
                  ownerLocation.y + (ownerSize.height - frameSize.height) / 2);

        helpFrame.setVisible(true);
      }catch(IOException ioe){
        ioe.printStackTrace(Err.getPrintWriter());
      }
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
        setToolTipText("Gate");
      } else if(value == applicationsRoot) {
        setIcon(MainFrame.getIcon("applications.gif"));
        setToolTipText("Gate applications");
      } else if(value == languageResourcesRoot) {
        setIcon(MainFrame.getIcon("lrs.gif"));
        setToolTipText("Language Resources");
      } else if(value == processingResourcesRoot) {
        setIcon(MainFrame.getIcon("prs.gif"));
        setToolTipText("Processing Resources");
      } else if(value == datastoresRoot) {
        setIcon(MainFrame.getIcon("dss.gif"));
        setToolTipText("Gate Datastores");
      }else{
        //not one of the default root nodes
        value = ((DefaultMutableTreeNode)value).getUserObject();
        if(value instanceof Handle) {
          setIcon(((Handle)value).getIcon());
          setText(((Handle)value).getTitle());
          setToolTipText(((Handle)value).getTooltipText());
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
      if(handle != null && handle instanceof Handle){
        setIcon(((Handle)handle).getIcon());
        setText(((Handle)handle).getTitle());
        setToolTipText(((Handle)handle).getTooltipText());
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
          targetPanel.repaint();
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
//              targetPanel.getParent().validate();
//              targetPanel.getParent().repaint();
//              ((JComponent)targetPanel.getParent()).paintImmediately(((JComponent)targetPanel.getParent()).getBounds());
//              targetPanel.doLayout();

//              targetPanel.requestFocus();
              targetPanel.getParent().getParent().invalidate();
              targetPanel.getParent().getParent().repaint();
//              targetPanel.paintImmediately(targetPanel.getBounds());
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
