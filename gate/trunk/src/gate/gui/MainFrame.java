/*
 *  Copyright (c) 1998-2007, The University of Sheffield.
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

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import junit.framework.Assert;

import com.ontotext.gate.vr.Gaze;

import gate.*;
import gate.creole.*;
import gate.creole.annic.Constants;
import gate.event.*;
import gate.persist.PersistenceException;
import gate.security.*;
import gate.swing.*;
import gate.util.*;

/**
 * The main Gate GUI frame.
 */
public class MainFrame extends JFrame implements ProgressListener,
                                     StatusListener, CreoleListener {

  protected JMenuBar menuBar;

  protected JSplitPane mainSplit;

  protected JSplitPane leftSplit;

  protected JLabel statusBar;

  protected JProgressBar progressBar;

  protected XJTabbedPane mainTabbedPane;

  protected JScrollPane projectTreeScroll;

  protected JScrollPane lowerScroll;

  /**
   * Popup used for right click actions on the Applications node.
   */
  protected JPopupMenu appsPopup;

  /**
   * Popup used for right click actions on the Datastores node.
   */
  protected JPopupMenu dssPopup;

  /**
   * Popup used for right click actions on the LRs node.
   */
  protected JPopupMenu lrsPopup;

  /**
   * Popup used for right click actions on the PRs node.
   */
  protected JPopupMenu prsPopup;

  protected JCheckBoxMenuItem verboseModeItem;

  protected JTree resourcesTree;

  protected JScrollPane resourcesTreeScroll;

  protected DefaultTreeModel resourcesTreeModel;

  protected DefaultMutableTreeNode resourcesTreeRoot;

  protected DefaultMutableTreeNode applicationsRoot;

  protected DefaultMutableTreeNode languageResourcesRoot;

  protected DefaultMutableTreeNode processingResourcesRoot;

  protected DefaultMutableTreeNode datastoresRoot;

  protected Splash splash;

  protected PluginManagerUI pluginManager;

  protected LogArea logArea;

  protected JScrollPane logScroll;

  protected JToolBar toolbar;

  static JFileChooser fileChooser;

  static private MainFrame instance;

  protected AppearanceDialog appearanceDialog;

  protected OptionsDialog optionsDialog;

  protected CartoonMinder animator;

  protected TabHighlighter logHighlighter;

  protected NewResourceDialog newResourceDialog;

  protected WaitDialog waitDialog;

  protected HelpFrame helpFrame;
  
  protected JCheckBox toggleToolTipsCheckBoxMenuItem;

  /**
   * Holds all the icons used in the Gate GUI indexed by filename. This is
   * needed so we do not need to decode the icon everytime we need it as that
   * would use unecessary CPU time and memory. Access to this data is avaialable
   * through the {@link #getIcon(String)} method.
   */
  protected static Map iconByName = new HashMap();

  /**
   * A Map which holds listeners that are singletons (e.g. the status listener
   * that updates the status bar on the main frame or the progress listener that
   * updates the progress bar on the main frame). The keys used are the class
   * names of the listener interface and the values are the actual listeners
   * (e.g "gate.event.StatusListener" -> this).
   */
  private static java.util.Map listeners = new HashMap();

  protected static java.util.Collection guiRoots = new ArrayList();

  /**
   * Extensions for icon files to be tried in this order.
   */
  protected static final String[] ICON_EXTENSIONS = {"", ".png", ".gif"};

  private static JDialog guiLock = null;

  /**
   * Contains the last directories selected when using resources.
   */
  static private Preferences prefs =
    Preferences.userNodeForPackage(MainFrame.class);
  
  /**
   * Name of the current resource class used by the file chooser.
   */
  private static String currentResourceClassName;

  static public Icon getIcon(String baseName) {
    Icon result = (Icon)iconByName.get(baseName);
    for(int i = 0; i < ICON_EXTENSIONS.length && result == null; i++) {
      String extension = ICON_EXTENSIONS[i];
      String fileName = baseName + extension;
      URL iconURL = null;
      // if the ICON is an absolute path starting with '/', then just
      // load
      // it from that path. If it does not start with '/', treat it as
      // relative to gate/resources/img for backwards compatibility
      if(fileName.charAt(0) == '/') {
        iconURL = Files.getResource(fileName);
      }
      else {
        iconURL = Files.getGateResource("/img/" + fileName);
      }
      if(iconURL != null) {
        result = new ImageIcon(iconURL);
        iconByName.put(baseName, result);
      }
    }
    return result;
  }

  static public MainFrame getInstance() {
    if(instance == null) instance = new MainFrame();
    return instance;
  }

  /**
   * Get the file chooser.
   */
  static public JFileChooser getFileChooser() {
    return fileChooser;
  }

  /**
   * Gets the original system output stream, which was later redirected to the
   * messages pane.
   * 
   * @return a {@link PrintStream} value.
   */
  public PrintStream getOriginalOut() {
    return logArea.getOriginalOut();
  }

  /**
   * Gets the original system error output stream, which was later redirected to
   * the messages pane.
   * 
   * @return a {@link PrintStream} value.
   */
  public PrintStream getOriginalErr() {
    return logArea.getOriginalErr();
  }

  /**
   * Selects a resource if loaded in the system and not invisible.
   * 
   * @param res
   *          the resource to be selected.
   */
  public void select(Resource res) {
    // first find the handle for the resource
    Handle handle = null;
    // go through all the nodes
    Enumeration nodesEnum = resourcesTreeRoot.breadthFirstEnumeration();
    while(nodesEnum.hasMoreElements() && handle == null) {
      Object node = nodesEnum.nextElement();
      if(node instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode dmtNode = (DefaultMutableTreeNode)node;
        if(dmtNode.getUserObject() instanceof Handle) {
          if(((Handle)dmtNode.getUserObject()).getTarget() == res) {
            handle = (Handle)dmtNode.getUserObject();
          }
        }
      }
    }

    // now select the handle if found
    if(handle != null) select(handle);
  }

  protected void select(Handle handle) {
    if(handle.viewsBuilt()
      && mainTabbedPane.indexOfComponent(handle.getLargeView()) != -1) {
      // select
      JComponent largeView = handle.getLargeView();
      if(largeView != null) {
        mainTabbedPane.setSelectedComponent(largeView);
      }
    }
    else {
      // show
      JComponent largeView = handle.getLargeView();
      if(largeView != null) {
        mainTabbedPane.addTab(handle.getTitle(), handle.getIcon(), largeView,
          handle.getTooltipText());
        mainTabbedPane.setSelectedComponent(handle.getLargeView());
      }
    }
    // show the small view
    JComponent smallView = handle.getSmallView();
    if(smallView != null) {
      lowerScroll.getViewport().setView(smallView);
    }
    else {
      lowerScroll.getViewport().setView(null);
    }
  }// protected void select(ResourceHandle handle)

  public MainFrame() {
    this(null);
  }

  public MainFrame(GraphicsConfiguration gc) {
    this(false, gc);
  } // MainFrame

  /** Construct the frame */
  public MainFrame(boolean isShellSlacGIU, GraphicsConfiguration gc) {
    super(gc);
    instance = this;
    guiRoots.add(this);
    if(fileChooser == null) {
      fileChooser = new GateFileChooser();
      fileChooser.setMultiSelectionEnabled(false);
      guiRoots.add(fileChooser);

      // the JFileChooser seems to size itself better once it's been
      // added to a
      // top level container such as a dialog.
      JDialog dialog = new JDialog(this, "", true);
      java.awt.Container contentPane = dialog.getContentPane();
      contentPane.setLayout(new BorderLayout());
      contentPane.add(fileChooser, BorderLayout.CENTER);
      dialog.pack();
      dialog.getContentPane().removeAll();
      dialog.dispose();
      dialog = null;
    }
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    initLocalData(isShellSlacGIU);
    initGuiComponents(isShellSlacGIU);
    initListeners(isShellSlacGIU);
  } // MainFrame(boolean simple)

  protected void initLocalData(boolean isShellSlacGIU) {
    resourcesTreeRoot = new DefaultMutableTreeNode("GATE", true);
    applicationsRoot = new DefaultMutableTreeNode("Applications", true);
    if(isShellSlacGIU) {
      languageResourcesRoot = new DefaultMutableTreeNode("Documents", true);
    }
    else {
      languageResourcesRoot =
        new DefaultMutableTreeNode("Language Resources", true);
    } // if
    processingResourcesRoot =
      new DefaultMutableTreeNode("Processing Resources", true);
    datastoresRoot = new DefaultMutableTreeNode("Data stores", true);
    resourcesTreeRoot.add(applicationsRoot);
    resourcesTreeRoot.add(languageResourcesRoot);
    resourcesTreeRoot.add(processingResourcesRoot);
    resourcesTreeRoot.add(datastoresRoot);

    resourcesTreeModel = new ResourcesTreeModel(resourcesTreeRoot, true);
  }

  protected void initGuiComponents(boolean isShellSlacGUI) {
    this.getContentPane().setLayout(new BorderLayout());

    Integer width = Gate.getUserConfig().getInt(GateConstants.MAIN_FRAME_WIDTH);
    Integer height =
      Gate.getUserConfig().getInt(GateConstants.MAIN_FRAME_HEIGHT);
    this.setSize(new Dimension(width == null ? 800 : width.intValue(),
      height == null ? 600 : height.intValue()));

    this.setIconImage(Toolkit.getDefaultToolkit().getImage(
      Files.getGateResource("/img/gate-icon.png")));
    resourcesTree = new ResourcesTree();
    resourcesTree.setModel(resourcesTreeModel);
    resourcesTree.setRowHeight(0);

    resourcesTree.setEditable(true);
    ResourcesTreeCellRenderer treeCellRenderer =
      new ResourcesTreeCellRenderer();
    resourcesTree.setCellRenderer(treeCellRenderer);
    resourcesTree.setCellEditor(new ResourcesTreeCellEditor(resourcesTree,
      treeCellRenderer));

    resourcesTree.setRowHeight(0);
    // expand all nodes
    resourcesTree.expandRow(0);
    resourcesTree.expandRow(1);
    resourcesTree.expandRow(2);
    resourcesTree.expandRow(3);
    resourcesTree.expandRow(4);
    resourcesTree.getSelectionModel().setSelectionMode(
      TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    resourcesTree.setEnabled(true);
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
    Thread thread =
      new Thread(Thread.currentThread().getThreadGroup(), animator,
        "MainFrame1");
    thread.setDaemon(true);
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();

    leftSplit =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT, resourcesTreeScroll, lowerPane);

    leftSplit.setResizeWeight((double)0.7);

    // Create a new logArea and redirect the Out and Err output to it.
    logArea = new LogArea();
    logScroll = new JScrollPane(logArea);
    // Out has been redirected to the logArea

    Out.prln("GATE " + Main.version + " build " + Main.build + " started at: "
      + new Date().toString());
    mainTabbedPane = new XJTabbedPane(JTabbedPane.TOP);
    mainTabbedPane.insertTab("Messages", null, logScroll, "GATE log", 0);

    logHighlighter = new TabHighlighter(mainTabbedPane, logScroll, Color.red);

    mainSplit =
      new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplit, mainTabbedPane);

    mainSplit.setDividerLocation(leftSplit.getPreferredSize().width + 10);
    this.getContentPane().add(mainSplit, BorderLayout.CENTER);

    // status and progress bars
    statusBar = new JLabel(" ");
    statusBar.setPreferredSize(new Dimension(200,
      statusBar.getPreferredSize().height));

    UIManager.put("ProgressBar.cellSpacing", new Integer(0));
    progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
    // progressBar.setBorder(BorderFactory.createEmptyBorder());
    progressBar.setForeground(new Color(150, 75, 150));
    // progressBar.setBorderPainted(false);
    progressBar.setStringPainted(false);
    progressBar.setOrientation(JProgressBar.HORIZONTAL);

    JPanel southBox = new JPanel();
    southBox.setLayout(new GridLayout(1, 2));
    southBox.setBorder(null);

    Box tempHBox = Box.createHorizontalBox();
    tempHBox.add(Box.createHorizontalStrut(5));
    tempHBox.add(statusBar);
    southBox.add(tempHBox);
    tempHBox = Box.createHorizontalBox();
    tempHBox.add(progressBar);
    tempHBox.add(Box.createHorizontalStrut(5));
    southBox.add(tempHBox);

    this.getContentPane().add(southBox, BorderLayout.SOUTH);
    progressBar.setVisible(false);

    // extra stuff
    newResourceDialog =
      new NewResourceDialog(this, "Resource parameters", true);
    waitDialog = new WaitDialog(this, "");

    // build the Help->About dialog
    JPanel splashBox = new JPanel();
    splashBox.setBackground(Color.WHITE);

    splashBox.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1;
    constraints.weighty = 0;
    constraints.insets = new Insets(2, 2, 2, 2);
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.BOTH;
    // constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;

    JLabel gifLbl = new JLabel(getIcon("splash"));
    splashBox.add(gifLbl, constraints);

    constraints.gridy = 2;
    constraints.gridwidth = 2;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    String splashHtml;
    try {
      splashHtml = Files.getGateResourceAsString("splash.html");
    }
    catch(IOException e1) {
      splashHtml = "GATE";
      Err.prln("couldn't get splash.html resource: " + e1);
    }
    JLabel htmlLbl = new JLabel(splashHtml);
    htmlLbl.setHorizontalAlignment(SwingConstants.CENTER);
    splashBox.add(htmlLbl, constraints);

    constraints.gridy = 3;
    htmlLbl =
      new JLabel("<HTML><FONT color=\"blue\">Version <B>" + Main.version
        + "</B></FONT>" + ", <FONT color=\"red\">build <B>" + Main.build
        + "</B></FONT>" + "<P><B>JVM version</B>: "
        + System.getProperty("java.version") + " from "
        + System.getProperty("java.vendor") + "</HTML>");
    constraints.fill = GridBagConstraints.HORIZONTAL;
    splashBox.add(htmlLbl, constraints);

    constraints.gridy = 4;
    constraints.gridwidth = 2;
    constraints.fill = GridBagConstraints.NONE;
    JButton okBtn = new JButton("OK");
    okBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        splash.setVisible(false);
      }
    });
    okBtn.setBackground(Color.white);
    splashBox.add(okBtn, constraints);
    splash = new Splash(this, splashBox);

    // MENUS
    menuBar = new JMenuBar();

    JMenu fileMenu = new XJMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);

    LiveMenu newAPPMenu = new LiveMenu(LiveMenu.APP);
    newAPPMenu.setText("New application");
    newAPPMenu.setIcon(getIcon("applications"));
    fileMenu.add(newAPPMenu);

    LiveMenu newLRMenu = new LiveMenu(LiveMenu.LR);
    newLRMenu.setText("New language resource");
    newLRMenu.setIcon(getIcon("lrs"));
    fileMenu.add(newLRMenu);

    LiveMenu newPRMenu = new LiveMenu(LiveMenu.PR);
    newPRMenu.setText("New processing resource");
    newPRMenu.setIcon(getIcon("prs"));
    fileMenu.add(newPRMenu);

    JMenu dsMenu = new JMenu("Datastores");
    dsMenu.setIcon(getIcon("datastores"));
    dsMenu.add(new XJMenuItem(new NewDSAction(), this));
    dsMenu.add(new XJMenuItem(new OpenDSAction(), this));
    fileMenu.add(dsMenu);

    fileMenu.addSeparator();
    fileMenu.add(new XJMenuItem(new LoadResourceFromFileAction(), this));

    fileMenu.addSeparator();
    JMenu loadANNIEMenu = new JMenu("Load ANNIE system");
    loadANNIEMenu.setIcon(getIcon("annie-application"));
    loadANNIEMenu.add(new XJMenuItem(new LoadANNIEWithDefaultsAction(), this));
    loadANNIEMenu
      .add(new XJMenuItem(new LoadANNIEWithoutDefaultsAction(), this));
    fileMenu.add(loadANNIEMenu);

    // fileMenu.add(new XJMenuItem(new LoadCreoleRepositoryAction(),
    // this));

    fileMenu.add(new XJMenuItem(new ManagePluginsAction(), this));
    fileMenu.addSeparator();

    fileMenu.add(new XJMenuItem(new ExitGateAction(), this));
    menuBar.add(fileMenu);

    JMenu optionsMenu = new JMenu("Options");
    optionsMenu.setMnemonic(KeyEvent.VK_O);

    optionsDialog = new OptionsDialog(MainFrame.this);
    optionsMenu.add(new XJMenuItem(new AbstractAction("Configuration") {
      {
        putValue(SHORT_DESCRIPTION, "Edit gate options");
      }

      public void actionPerformed(ActionEvent evt) {
        optionsDialog.showDialog();
        optionsDialog.dispose();
      }
    }, this));

    JMenu imMenu = null;
    List installedLocales = new ArrayList();
    try {
      // if this fails guk is not present
      Class.forName("guk.im.GateIMDescriptor");
      // add the Gate input methods
      installedLocales.addAll(Arrays.asList(new guk.im.GateIMDescriptor()
        .getAvailableLocales()));
    }
    catch(Exception e) {
      // something happened; most probably guk not present.
      // just drop it, is not vital.
    }
    try {
      // add the MPI IMs
      // if this fails mpi IM is not present
      Class.forName("mpi.alt.java.awt.im.spi.lookup.LookupDescriptor");

      installedLocales.addAll(Arrays
        .asList(new mpi.alt.java.awt.im.spi.lookup.LookupDescriptor()
          .getAvailableLocales()));
    }
    catch(Exception e) {
      // something happened; most probably MPI not present.
      // just drop it, is not vital.
    }

    Collections.sort(installedLocales, new Comparator() {
      public int compare(Object o1, Object o2) {
        return ((Locale)o1).getDisplayName().compareTo(
          ((Locale)o2).getDisplayName());
      }
    });
    JMenuItem item;
    if(!installedLocales.isEmpty()) {
      imMenu = new XJMenu("Input methods");
      ButtonGroup bg = new ButtonGroup();
      item = new LocaleSelectorMenuItem();
      imMenu.add(item);
      item.setSelected(true);
      imMenu.addSeparator();
      bg.add(item);
      for(int i = 0; i < installedLocales.size(); i++) {
        Locale locale = (Locale)installedLocales.get(i);
        item = new LocaleSelectorMenuItem(locale);
        imMenu.add(item);
        bg.add(item);
      }
    }
    if(imMenu != null) optionsMenu.add(imMenu);

    menuBar.add(optionsMenu);

    JMenu toolsMenu = new XJMenu("Tools");
    toolsMenu.setMnemonic(KeyEvent.VK_T);
    toolsMenu.add(new NewAnnotDiffAction());
    // toolsMenu.add(newCorpusAnnotDiffAction);
    toolsMenu.add(new NewBootStrapAction());
    // temporarily disabled till the evaluation tools are made to run
    // within
    // the GUI
    JMenu corpusEvalMenu = new JMenu("Corpus Benchmark Tools");
    corpusEvalMenu.setIcon(getIcon("corpus-benchmark"));
    toolsMenu.add(corpusEvalMenu);
    corpusEvalMenu.add(new NewCorpusEvalAction());
    corpusEvalMenu.addSeparator();
    corpusEvalMenu.add(new GenerateStoredCorpusEvalAction());
    corpusEvalMenu.addSeparator();
    corpusEvalMenu.add(new StoredMarkedCorpusEvalAction());
    corpusEvalMenu.add(new CleanMarkedCorpusEvalAction());
    corpusEvalMenu.addSeparator();
    verboseModeItem =
      new JCheckBoxMenuItem(new VerboseModeCorpusEvalToolAction());
    corpusEvalMenu.add(verboseModeItem);
    // JCheckBoxMenuItem datastoreModeItem =
    // new JCheckBoxMenuItem(datastoreModeCorpusEvalToolAction);
    // corpusEvalMenu.add(datastoreModeItem);
    toolsMenu.add(new AbstractAction("Unicode editor", getIcon("unicode")) {
      public void actionPerformed(ActionEvent evt) {
        new guk.Editor();
      }
    });

    /*
     * add the ontology editor to the tools menu ontotext.bp
     */
    // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
    // Removed as it is now obsolete.
    // toolsMenu.add(new NewOntologyEditorAction());
    if(Gate.isEnableJapeDebug()) {
      // by Shafirin Andrey start
      toolsMenu
        .add(new AbstractAction("JAPE Debugger", getIcon("application")) {
          public void actionPerformed(ActionEvent evt) {
            System.out.println("Creating Jape Debugger");
            new debugger.JapeDebugger();
          }
        });
      // by Shafirin Andrey end
    }

    menuBar.add(toolsMenu);

    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_H);
    helpMenu.add(new HelpUserGuideAction());
    helpMenu.add(new HelpUserGuideInContextAction());
    toggleToolTipsCheckBoxMenuItem =
      new JCheckBox(new ToggleToolTipsAction());
    toggleToolTipsCheckBoxMenuItem.setSelected(true);
    helpMenu.add(toggleToolTipsCheckBoxMenuItem);
    helpMenu.add(new HelpAboutAction());
    menuBar.add(helpMenu);

    this.setJMenuBar(menuBar);

    // popups
    appsPopup = new XJPopupMenu();
    LiveMenu appsMenu = new LiveMenu(LiveMenu.APP);
    appsMenu.setText("New");
    appsPopup.add(appsMenu);
    appsPopup.addSeparator();
    appsPopup.add(new XJMenuItem(new LoadResourceFromFileAction(), this));
    guiRoots.add(appsMenu);
    guiRoots.add(appsPopup);

    lrsPopup = new XJPopupMenu();
    LiveMenu lrsMenu = new LiveMenu(LiveMenu.LR);
    lrsMenu.setText("New");
    lrsPopup.add(lrsMenu);
    guiRoots.add(lrsPopup);
    guiRoots.add(lrsMenu);

    prsPopup = new XJPopupMenu();
    LiveMenu prsMenu = new LiveMenu(LiveMenu.PR);
    prsMenu.setText("New");
    prsPopup.add(prsMenu);
    guiRoots.add(prsMenu);
    guiRoots.add(prsPopup);

    dssPopup = new XJPopupMenu();
    dssPopup.add(new NewDSAction());
    dssPopup.add(new OpenDSAction());
    guiRoots.add(dssPopup);

    // TOOLBAR
    toolbar = new JToolBar(JToolBar.HORIZONTAL);
    toolbar.setFloatable(false);
    toolbar.add(new LoadResourceFromFileAction());
    JMenuBar smallMenuBar = new JMenuBar();
    smallMenuBar.setBorderPainted(false);
    smallMenuBar.setAlignmentY(JComponent.CENTER_ALIGNMENT);
    JMenu annieMenu = new JMenu();
    annieMenu.setIcon(getIcon("annie-application"));
    annieMenu.setToolTipText("Load ANNIE System");
    annieMenu.add(new LoadANNIEWithDefaultsAction());
    annieMenu.add(new LoadANNIEWithoutDefaultsAction());
    smallMenuBar.add(annieMenu);
    toolbar.add(smallMenuBar);
    toolbar.addSeparator();
    smallMenuBar = new JMenuBar();
    smallMenuBar.setBorderPainted(false);
    smallMenuBar.setAlignmentY(JComponent.CENTER_ALIGNMENT);
    LiveMenu tbNewLRMenu = new LiveMenu(LiveMenu.LR);
    tbNewLRMenu.setToolTipText("New Language Resource");
    tbNewLRMenu.setIcon(getIcon("lrs"));
    smallMenuBar.add(tbNewLRMenu);
    toolbar.add(smallMenuBar);

    smallMenuBar = new JMenuBar();
    smallMenuBar.setBorderPainted(false);
    smallMenuBar.setAlignmentY(JComponent.CENTER_ALIGNMENT);
    LiveMenu tbNewPRMenu = new LiveMenu(LiveMenu.PR);
    tbNewPRMenu.setToolTipText("New Processing Resource");
    tbNewPRMenu.setIcon(getIcon("prs"));
    smallMenuBar.add(tbNewPRMenu);
    toolbar.add(smallMenuBar);

    smallMenuBar = new JMenuBar();
    smallMenuBar.setBorderPainted(false);
    smallMenuBar.setAlignmentY(JComponent.CENTER_ALIGNMENT);
    LiveMenu tbNewAppMenu = new LiveMenu(LiveMenu.APP);
    tbNewAppMenu.setToolTipText("New Application");
    tbNewAppMenu.setIcon(getIcon("applications"));
    smallMenuBar.add(tbNewAppMenu);
    toolbar.add(smallMenuBar);

    smallMenuBar = new JMenuBar();
    smallMenuBar.setBorderPainted(false);
    smallMenuBar.setAlignmentY(JComponent.CENTER_ALIGNMENT);
    JMenu tbDsMenu = new JMenu();
    tbDsMenu.setToolTipText("Datastores");
    tbDsMenu.setIcon(getIcon("datastores"));
    tbDsMenu.add(new NewDSAction());
    tbDsMenu.add(new OpenDSAction());
    smallMenuBar.add(tbDsMenu);
    toolbar.add(smallMenuBar);

    toolbar.addSeparator();
    toolbar.add(new ManagePluginsAction());
    toolbar.addSeparator();
    toolbar.add(new NewAnnotDiffAction());

    toolbar.add(Box.createGlue());
    this.getContentPane().add(toolbar, BorderLayout.NORTH);
  }

  protected void initListeners(boolean isShellSlacGIU) {
    Gate.getCreoleRegister().addCreoleListener(this);

    resourcesTree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        // where inside the tree?
        int x = e.getX();
        int y = e.getY();
        TreePath path = resourcesTree.getPathForLocation(x, y);
        JPopupMenu popup = null;
        Handle handle = null;
        if(path != null) {
          Object value = path.getLastPathComponent();
          if(value == resourcesTreeRoot) {
          }
          else if(value == applicationsRoot) {
            popup = appsPopup;
          }
          else if(value == languageResourcesRoot) {
            popup = lrsPopup;
          }
          else if(value == processingResourcesRoot) {
            popup = prsPopup;
          }
          else if(value == datastoresRoot) {
            popup = dssPopup;
          }
          else {
            value = ((DefaultMutableTreeNode)value).getUserObject();
            if(value instanceof Handle) {
              handle = (Handle)value;
              currentResourceClassName =
                handle.getTarget().getClass().getName();
              popup = handle.getPopup();
            }
          }
        }
        if(SwingUtilities.isRightMouseButton(e)) {
          if(resourcesTree.getSelectionCount() > 1) {
            // multiple selection in tree-> show a popup for delete all
            popup = new XJPopupMenu();
            popup.add(new XJMenuItem(new CloseSelectedResourcesAction(),
              MainFrame.this));
            popup.show(resourcesTree, e.getX(), e.getY());
          }
          else if(popup != null) {
            if(handle != null) {
              // // Create a CloseViewAction and a menu item based on it
              // CloseViewAction cva = new CloseViewAction(handle);
              // XJMenuItem menuItem = new XJMenuItem(cva,
              // MainFrame.this);
              // popup.insert(menuItem, 1);

              // add a rename action
              popup.insert(new JPopupMenu.Separator(), 2);
              popup.insert(new XJMenuItem(new RenameResourceAction(path),
                MainFrame.this), 3);

              // add a help action
              popup.insert(new XJMenuItem(new HelpOnItemTreeAction(
                handle.getTarget().getClass().getName()), MainFrame.this), 4);

              // Put the action command in the component's action map
              // if (handle.getLargeView() != null){
              // handle.getLargeView().getActionMap().
              // put("Hide current view",cva);
              // }
            }

            popup.show(resourcesTree, e.getX(), e.getY());
          }
        }
        else if(SwingUtilities.isLeftMouseButton(e)) {
          if(e.getClickCount() == 2 && handle != null) {
            // double click - show the resource
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
        if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_F4) {
          JComponent resource =
            (JComponent)mainTabbedPane.getSelectedComponent();
          if(resource != null) {
            Action act = resource.getActionMap().get("Close resource");
            if(act != null) act.actionPerformed(null);
          }// End if
        }// End if
        // If CTRL+H was pressed then hide the active view.
        if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_H) {
          JComponent resource =
            (JComponent)mainTabbedPane.getSelectedComponent();
          if(resource != null) {
            Action act = resource.getActionMap().get("Hide current view");
            if(act != null) act.actionPerformed(null);
          }// End if
        }// End if
        // If CTRL+X was pressed then save as XML
        if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_X) {
          JComponent resource =
            (JComponent)mainTabbedPane.getSelectedComponent();
          if(resource != null) {
            Action act = resource.getActionMap().get("Save As XML");
            if(act != null) act.actionPerformed(null);
          }// End if
        }// End if
      }// End keyPressed();

      public void keyReleased(KeyEvent e) {
      }
    });

    mainTabbedPane.getModel().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        // use this to synchronise the selection in the tabbed pane with
        // the one
        // in the resources tree
        JComponent largeView =
          (JComponent)mainTabbedPane.getSelectedComponent();
        Enumeration nodesEnum = resourcesTreeRoot.preorderEnumeration();
        boolean done = false;
        DefaultMutableTreeNode node = resourcesTreeRoot;
        while(!done && nodesEnum.hasMoreElements()) {
          node = (DefaultMutableTreeNode)nodesEnum.nextElement();
          done =
            node.getUserObject() instanceof Handle
              && ((Handle)node.getUserObject()).viewsBuilt()
              && ((Handle)node.getUserObject()).getLargeView() == largeView;
        }
        if(done) {
          Handle handle = (Handle)node.getUserObject();
          TreePath nodePath = new TreePath(node.getPath());
          resourcesTree.setSelectionPath(nodePath);
          resourcesTree.scrollPathToVisible(nodePath);
          lowerScroll.getViewport().setView(handle.getSmallView());
        }
        else {
          // the selected item is not a resource (maybe the log area?)
          lowerScroll.getViewport().setView(null);
        }
      }
    });

    mainTabbedPane.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)) {
          int index = mainTabbedPane.getIndexAt(e.getPoint());
          if(index != -1) {
            JComponent view = (JComponent)mainTabbedPane.getComponentAt(index);
            Enumeration nodesEnum = resourcesTreeRoot.preorderEnumeration();
            boolean done = false;
            DefaultMutableTreeNode node = resourcesTreeRoot;
            while(!done && nodesEnum.hasMoreElements()) {
              node = (DefaultMutableTreeNode)nodesEnum.nextElement();
              done =
                node.getUserObject() instanceof Handle
                  && ((Handle)node.getUserObject()).viewsBuilt()
                  && ((Handle)node.getUserObject()).getLargeView() == view;
            }
            if(done) {
              Handle handle = (Handle)node.getUserObject();
              JPopupMenu popup = handle.getPopup();
              // Create a CloseViewAction and a menu item based on it
              CloseViewAction cva = new CloseViewAction(handle);
              XJMenuItem menuItem = new XJMenuItem(cva, MainFrame.this);
              popup.insert(menuItem, 1);
              popup.insert(new JPopupMenu.Separator(), 2);

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

    addComponentListener(new ComponentAdapter() {
      public void componentHidden(ComponentEvent e) {

      }

      public void componentMoved(ComponentEvent e) {
      }

      public void componentResized(ComponentEvent e) {
      }

      public void componentShown(ComponentEvent e) {
        leftSplit.setDividerLocation((double)0.7);
      }
    });

    if(isShellSlacGIU) {
      mainSplit.setDividerSize(0);
      mainSplit.getTopComponent().setVisible(false);
      mainSplit.getTopComponent().addComponentListener(new ComponentAdapter() {
        public void componentHidden(ComponentEvent e) {
        }

        public void componentMoved(ComponentEvent e) {
          mainSplit.setDividerLocation(0);
        }

        public void componentResized(ComponentEvent e) {
          mainSplit.setDividerLocation(0);
        }

        public void componentShown(ComponentEvent e) {
          mainSplit.setDividerLocation(0);
        }
      });
    } // if

    // blink the messages tab when new information is displayed
    logArea.getDocument().addDocumentListener(
      new javax.swing.event.DocumentListener() {
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured();
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
          changeOccured();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
        }

        protected void changeOccured() {
          logHighlighter.highlight();
        }
      });

    logArea.addPropertyChangeListener("document", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        // add the document listener
        logArea.getDocument().addDocumentListener(
          new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
              changeOccured();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
              changeOccured();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
              changeOccured();
            }

            protected void changeOccured() {
              logHighlighter.highlight();
            }
          });
      }
    });

    listeners.put("gate.event.StatusListener", MainFrame.this);
    listeners.put("gate.event.ProgressListener", MainFrame.this);
    if(System.getProperty("mrj.version") != null) {
      // mac-specific initialisation
      initMacListeners();
    }
  }// protected void initListeners()

  /**
   * Set up the handlers to support the Macintosh Application menu. This makes
   * the About, Quit and Preferences menu items map to their equivalents in
   * GATE. If an exception occurs during this process we print a warning.
   */
  protected void initMacListeners() {
    // What this method effectively does is:
    // 
    // com.apple.eawt.Application app = Application.getApplication();
    // app.addApplicationListener(new ApplicationAdapter() {
    // public void handleAbout(ApplicationEvent e) {
    // e.setHandled(true);
    // new HelpAboutAction().actionPerformed(null);
    // }
    // public void handleQuit(ApplicationEvent e) {
    // e.setHandled(false);
    // new ExitGateAction().actionPerformed(null);
    // }
    // public void handlePreferences(ApplicationEvent e) {
    // e.setHandled(true);
    // optionsDialog.show();
    // }
    // });
    // 
    // app.setEnabledPreferencesMenu(true);
    //
    // except that it does it all by reflection so as not to
    // compile-time
    // depend on Apple classes.
    try {
      // load the Apple classes
      final Class eawtApplicationClass =
        Gate.getClassLoader().loadClass("com.apple.eawt.Application");
      final Class eawtApplicationListenerInterface =
        Gate.getClassLoader().loadClass("com.apple.eawt.ApplicationListener");
      final Class eawtApplicationEventClass =
        Gate.getClassLoader().loadClass("com.apple.eawt.ApplicationEvent");

      // method used in the InvocationHandler
      final Method appEventSetHandledMethod =
        eawtApplicationEventClass.getMethod("setHandled",
          new Class[]{boolean.class});

      // Invocation handler used to process Apple application events
      InvocationHandler handler = new InvocationHandler() {
        private Action aboutAction = new HelpAboutAction();

        private Action exitAction = new ExitGateAction();

        public Object invoke(Object proxy, Method method, Object[] args)
          throws Throwable {
          Object appEvent = args[0];
          if("handleAbout".equals(method.getName())) {
            appEventSetHandledMethod.invoke(appEvent,
              new Object[]{Boolean.TRUE});
            aboutAction.actionPerformed(null);
          }
          else if("handleQuit".equals(method.getName())) {
            appEventSetHandledMethod.invoke(appEvent,
              new Object[]{Boolean.FALSE});
            exitAction.actionPerformed(null);
          }
          else if("handlePreferences".equals(method.getName())) {
            appEventSetHandledMethod.invoke(appEvent,
              new Object[]{Boolean.TRUE});
            optionsDialog.showDialog();
          }

          return null;
        }
      };

      // Create an ApplicationListener proxy instance
      Object applicationListenerObject =
        Proxy.newProxyInstance(Gate.getClassLoader(),
          new Class[]{eawtApplicationListenerInterface}, handler);

      // get hold of the Application object
      Method getApplicationMethod =
        eawtApplicationClass.getMethod("getApplication", new Class[0]);
      Object applicationObject =
        getApplicationMethod.invoke(null, new Object[0]);

      // enable the preferences menu item
      Method setEnabledPreferencesMenuMethod =
        eawtApplicationClass.getMethod("setEnabledPreferencesMenu",
          new Class[]{boolean.class});
      setEnabledPreferencesMenuMethod.invoke(applicationObject,
        new Object[]{Boolean.TRUE});

      // Register our proxy instance as an ApplicationListener
      Method addApplicationListenerMethod =
        eawtApplicationClass.getMethod("addApplicationListener",
          new Class[]{eawtApplicationListenerInterface});
      addApplicationListenerMethod.invoke(applicationObject,
        new Object[]{applicationListenerObject});
    }
    catch(Throwable t) {
      // oh well, we tried
      System.out.println("Warning: there was a problem setting up the Mac "
        + "application\nmenu.  Your options/session will not be saved if "
        + "you exit\nwith command-Q, use \"File/Exit GATE\" instead");
    }
  }

  public void progressChanged(int i) {
    // progressBar.setStringPainted(true);
    int oldValue = progressBar.getValue();
    // if((!stopAction.isEnabled()) &&
    // (Gate.getExecutable() != null)){
    // stopAction.setEnabled(true);
    // SwingUtilities.invokeLater(new Runnable(){
    // public void run(){
    // southBox.add(stopBtn, 0);
    // }
    // });
    // }
    if(!animator.isActive()) animator.activate();
    if(oldValue != i) {
      SwingUtilities.invokeLater(new ProgressBarUpdater(i));
    }
  }

  /**
   * Called when the process is finished.
   * 
   */
  public void processFinished() {
    // progressBar.setStringPainted(false);
    // if(stopAction.isEnabled()){
    // stopAction.setEnabled(false);
    // SwingUtilities.invokeLater(new Runnable(){
    // public void run(){
    // southBox.remove(stopBtn);
    // }
    // });
    // }
    SwingUtilities.invokeLater(new ProgressBarUpdater(0));
    animator.deactivate();
  }

  public void statusChanged(String text) {
    SwingUtilities.invokeLater(new StatusBarUpdater(text));
  }

  public void resourceLoaded(CreoleEvent e) {
    Resource res = e.getResource();
    if(Gate.getHiddenAttribute(res.getFeatures())
      || res instanceof VisualResource) return;
    NameBearerHandle handle = new NameBearerHandle(res, MainFrame.this);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(handle, false);
    if(res instanceof ProcessingResource) {
      resourcesTreeModel.insertNodeInto(node, processingResourcesRoot, 0);
    }
    else if(res instanceof LanguageResource) {
      resourcesTreeModel.insertNodeInto(node, languageResourcesRoot, 0);
    }
    else if(res instanceof Controller) {
      resourcesTreeModel.insertNodeInto(node, applicationsRoot, 0);
    }

    handle.addProgressListener(MainFrame.this);
    handle.addStatusListener(MainFrame.this);

    // JPopupMenu popup = handle.getPopup();
    //
    // // Create a CloseViewAction and a menu item based on it
    // CloseViewAction cva = new CloseViewAction(handle);
    // XJMenuItem menuItem = new XJMenuItem(cva, this);
    // // Add an accelerator ATL+F4 for this action
    // menuItem.setAccelerator(KeyStroke.getKeyStroke(
    // KeyEvent.VK_H, ActionEvent.CTRL_MASK));
    // popup.insert(menuItem, 1);
    // popup.insert(new JPopupMenu.Separator(), 2);
    //
    // popup.insert(new XJMenuItem(
    // new RenameResourceAction(
    // new TreePath(resourcesTreeModel.getPathToRoot(node))),
    // MainFrame.this) , 3);
    //
    // // Put the action command in the component's action map
    // if (handle.getLargeView() != null)
    // handle.getLargeView().getActionMap().put("Hide current
    // view",cva);
    //
  }// resourceLoaded();

  public void resourceUnloaded(CreoleEvent e) {
    final Resource res = e.getResource();
    if(Gate.getHiddenAttribute(res.getFeatures())) return;
    Runnable runner = new Runnable() {
      public void run() {
        DefaultMutableTreeNode node;
        DefaultMutableTreeNode parent = null;
        if(res instanceof ProcessingResource) {
          parent = processingResourcesRoot;
        }
        else if(res instanceof LanguageResource) {
          parent = languageResourcesRoot;
        }
        else if(res instanceof Controller) {
          parent = applicationsRoot;
        }
        if(parent != null) {
          Enumeration children = parent.children();
          while(children.hasMoreElements()) {
            node = (DefaultMutableTreeNode)children.nextElement();
            if(((NameBearerHandle)node.getUserObject()).getTarget() == res) {
              resourcesTreeModel.removeNodeFromParent(node);
              Handle handle = (Handle)node.getUserObject();
              if(handle.viewsBuilt()) {
                if(mainTabbedPane.indexOfComponent(handle.getLargeView()) != -1)
                  mainTabbedPane.remove(handle.getLargeView());
                if(lowerScroll.getViewport().getView() == handle.getSmallView())
                  lowerScroll.getViewport().setView(null);
              }
              handle.cleanup();
              return;
            }
          }
        }
      }
    };
    SwingUtilities.invokeLater(runner);
  }

  /** Called when a {@link gate.DataStore} has been opened */
  public void datastoreOpened(CreoleEvent e) {
    DataStore ds = e.getDatastore();

    ds.setName(ds.getStorageUrl());

    NameBearerHandle handle = new NameBearerHandle(ds, MainFrame.this);
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(handle, false);
    resourcesTreeModel.insertNodeInto(node, datastoresRoot, 0);
    handle.addProgressListener(MainFrame.this);
    handle.addStatusListener(MainFrame.this);

    // JPopupMenu popup = handle.getPopup();
    // popup.addSeparator();
    // // Create a CloseViewAction and a menu item based on it
    // CloseViewAction cva = new CloseViewAction(handle);
    // XJMenuItem menuItem = new XJMenuItem(cva, this);
    // // Add an accelerator ATL+F4 for this action
    // menuItem.setAccelerator(KeyStroke.getKeyStroke(
    // KeyEvent.VK_H, ActionEvent.CTRL_MASK));
    // popup.add(menuItem);
    // // Put the action command in the component's action map
    // if (handle.getLargeView() != null)
    // handle.getLargeView().getActionMap().put("Hide current
    // view",cva);
  }// datastoreOpened();

  /** Called when a {@link gate.DataStore} has been created */
  public void datastoreCreated(CreoleEvent e) {
    datastoreOpened(e);
  }

  /** Called when a {@link gate.DataStore} has been closed */
  public void datastoreClosed(CreoleEvent e) {
    DataStore ds = e.getDatastore();
    DefaultMutableTreeNode node;
    DefaultMutableTreeNode parent = datastoresRoot;
    if(parent != null) {
      Enumeration children = parent.children();
      while(children.hasMoreElements()) {
        node = (DefaultMutableTreeNode)children.nextElement();
        if(((NameBearerHandle)node.getUserObject()).getTarget() == ds) {
          resourcesTreeModel.removeNodeFromParent(node);
          NameBearerHandle handle = (NameBearerHandle)node.getUserObject();

          if(handle.viewsBuilt()
            && mainTabbedPane.indexOfComponent(handle.getLargeView()) != -1) {
            mainTabbedPane.remove(handle.getLargeView());
          }
          if(lowerScroll.getViewport().getView() == handle.getSmallView()) {
            lowerScroll.getViewport().setView(null);
          }
          return;
        }
      }
    }
  }

  public void resourceRenamed(Resource resource, String oldName, String newName) {
    for(int i = 0; i < mainTabbedPane.getTabCount(); i++) {
      if(mainTabbedPane.getTitleAt(i).equals(oldName)) {
        mainTabbedPane.setTitleAt(i, newName);

        return;
      }
    }
  }

  /**
   * Overridden so we can exit when window is closed
   */
  protected void processWindowEvent(WindowEvent e) {
    if(e.getID() == WindowEvent.WINDOW_CLOSING) {
      new ExitGateAction().actionPerformed(null);
    }
    super.processWindowEvent(e);
  }// processWindowEvent(WindowEvent e)

  /**
   * Returns the listeners map, a map that holds all the listeners that are
   * singletons (e.g. the status listener that updates the status bar on the
   * main frame or the progress listener that updates the progress bar on the
   * main frame). The keys used are the class names of the listener interface
   * and the values are the actual listeners (e.g "gate.event.StatusListener" ->
   * this). The returned map is the actual data member used to store the
   * listeners so any changes in this map will be visible to everyone.
   */
  public static java.util.Map getListeners() {
    return listeners;
  }

  public static java.util.Collection getGuiRoots() {
    return guiRoots;
  }

  /**
   * This method will lock all input to the gui by means of a modal dialog. If
   * Gate is not currently running in GUI mode this call will be ignored. A call
   * to this method while the GUI is locked will cause the GUI to be unlocked
   * and then locked again with the new message. If a message is provided it
   * will show in the dialog.
   * 
   * @param message
   *          the message to be displayed while the GUI is locked
   */
  public synchronized static void lockGUI(final String message) {
    // check whether GUI is up
    if(getGuiRoots() == null || getGuiRoots().isEmpty()) return;
    // if the GUI is locked unlock it so we can show the new message
    unlockGUI();

    // build the dialog contents
    Object[] options = new Object[]{new JButton(new StopAction())};
    JOptionPane pane =
      new JOptionPane(message, JOptionPane.WARNING_MESSAGE,
        JOptionPane.DEFAULT_OPTION, null, options, null);

    // build the dialog
    Component parentComp = (Component)((ArrayList)getGuiRoots()).get(0);
    JDialog dialog;
    Window parentWindow;
    if(parentComp instanceof Window)
      parentWindow = (Window)parentComp;
    else parentWindow = SwingUtilities.getWindowAncestor(parentComp);
    if(parentWindow instanceof Frame) {
      dialog = new JDialog((Frame)parentWindow, "Please wait", true) {
        protected void processWindowEvent(WindowEvent e) {
          if(e.getID() == WindowEvent.WINDOW_CLOSING) {
            getToolkit().beep();
          }
        }
      };
    }
    else if(parentWindow instanceof Dialog) {
      dialog = new JDialog((Dialog)parentWindow, "Please wait", true) {
        protected void processWindowEvent(WindowEvent e) {
          if(e.getID() == WindowEvent.WINDOW_CLOSING) {
            getToolkit().beep();
          }
        }
      };
    }
    else {
      dialog = new JDialog(JOptionPane.getRootFrame(), "Please wait", true) {
        protected void processWindowEvent(WindowEvent e) {
          if(e.getID() == WindowEvent.WINDOW_CLOSING) {
            getToolkit().beep();
          }
        }
      };
    }
    dialog.getContentPane().setLayout(new BorderLayout());
    dialog.getContentPane().add(pane, BorderLayout.CENTER);
    dialog.pack();
    dialog.setLocationRelativeTo(parentComp);
    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    guiLock = dialog;

    // this call needs to return so we'll show the dialog from a
    // different thread
    // the Swing thread sounds good for that
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        guiLock.setVisible(true);
      }
    });

    // this call should not return until the dialog is up to ensure
    // proper
    // sequentiality for lock - unlock calls
    while(!guiLock.isShowing()) {
      try {
        Thread.sleep(100);
      }
      catch(InterruptedException ie) {
      }
    }
  }

  public synchronized static void unlockGUI() {
    // check whether GUI is up
    if(getGuiRoots() == null || getGuiRoots().isEmpty()) return;

    if(guiLock != null) {
      guiLock.setVisible(false);
      // completely dispose the dialog (causes it to disappear even if
      // displayed on a non-visible virtual display on Linux)
      // fix for bug 1369096
      // (http://sourceforge.net/tracker/index.php?func=detail&aid=1369096&group_id=143829&atid=756796)
      guiLock.dispose();
    }
    guiLock = null;
  }

  /** Flag to protect Frame title to be changed */
  private boolean titleChangable = false;

  public void setTitleChangable(boolean isChangable) {
    titleChangable = isChangable;
  } // setTitleChangable(boolean isChangable)

  /** Override to avoid Protege to change Frame title */
  public synchronized void setTitle(String title) {
    if(titleChangable) {
      super.setTitle(title);
    } // if
  } // setTitle(String title)

  /** Method is used in NewDSAction */
  protected DataStore createSerialDataStore() {
    DataStore ds = null;

    // get the URL (a file in this case)
    fileChooser.setDialogTitle("Please create a new empty directory");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    currentResourceClassName = "gate.persist.SerialDataStore";
    if(fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
      try {
        URL dsURL = fileChooser.getSelectedFile().toURI().toURL();
        ds =
          Factory.createDataStore("gate.persist.SerialDataStore", dsURL
            .toExternalForm());
      }
      catch(MalformedURLException mue) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Invalid location for the datastore\n " + mue.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      }
      catch(PersistenceException pe) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Datastore creation error!\n " + pe.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      } // catch
    } // if

    return ds;
  } // createSerialDataStore()

  /** Method is used in OpenDSAction */
  protected DataStore openSerialDataStore() {
    DataStore ds = null;

    // get the URL (a file in this case)
    fileChooser.setDialogTitle("Select the datastore directory");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    currentResourceClassName = "gate.persist.SerialDataStore";
    if(fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
      try {
        URL dsURL = fileChooser.getSelectedFile().toURI().toURL();
        ds =
          Factory.openDataStore("gate.persist.SerialDataStore", dsURL
            .toExternalForm());
      }
      catch(MalformedURLException mue) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Invalid location for the datastore\n " + mue.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      }
      catch(PersistenceException pe) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Datastore opening error!\n " + pe.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      } // catch
    } // if

    return ds;
  } // openSerialDataStore()

  /** Method is used in ....OpenDSAction */
  protected DataStore openDocServiceDataStore() {
    DataStore ds = null;
    try {
      String DSLocation =
        JOptionPane.showInputDialog(MainFrame.this,
          "Enter document service URL",
          "http://localhost:8080/docservice/services/docservice");
      // System.out.println("DEBUG: MainFrame.openDocServiceDataStore()
      // DSLocation='" + DSLocation + "'");
      ds =
        Factory.openDataStore("gleam.docservice.gate.DocServiceDataStore",
          DSLocation);
    }
    catch(Exception pe) {
      pe.printStackTrace();
      JOptionPane.showMessageDialog(MainFrame.this,
        "Datastore opening error!\n " + pe.toString(), "GATE",
        JOptionPane.ERROR_MESSAGE);
    }
    return ds;
  } // openWSDataStore()

  /*
   * synchronized void showWaitDialog() { Point location =
   * getLocationOnScreen(); location.translate(10, getHeight() -
   * waitDialog.getHeight() - southBox.getHeight() - 10);
   * waitDialog.setLocation(location); waitDialog.showDialog(new Component[]{}); }
   * 
   * synchronized void hideWaitDialog() { waitDialog.goAway(); }
   */

  /*
   * class NewProjectAction extends AbstractAction { public NewProjectAction(){
   * super("New Project", new ImageIcon(MainFrame.class.getResource(
   * "/gate/resources/img/newProject"))); putValue(SHORT_DESCRIPTION,"Create a
   * new project"); } public void actionPerformed(ActionEvent e){
   * fileChooser.setDialogTitle("Select new project file");
   * fileChooser.setFileSelectionMode(fileChooser.FILES_ONLY);
   * if(fileChooser.showOpenDialog(parentFrame) == fileChooser.APPROVE_OPTION){
   * ProjectData pData = new ProjectData(fileChooser.getSelectedFile(),
   * parentFrame); addProject(pData); } } }
   */

  /** This class represent an action which brings up the Annot Diff tool */
  class NewAnnotDiffAction extends AbstractAction {
    public NewAnnotDiffAction() {
      super("Annotation Diff", getIcon("annotation-diff"));
      putValue(SHORT_DESCRIPTION, "Open a new Annotation Diff window");
    }// NewAnnotDiffAction

    public void actionPerformed(ActionEvent e) {
      // AnnotDiffDialog annotDiffDialog = new
      // AnnotDiffDialog(MainFrame.this);
      // annotDiffDialog.setTitle("Annotation Diff Tool");
      // annotDiffDialog.setVisible(true);
      AnnotationDiffGUI frame = new AnnotationDiffGUI("Annotation Diff Tool");
      frame.pack();
      try {
        frame.setIconImage(((ImageIcon)getIcon("annotation-diff")).getImage());
      }
      catch(Exception ex) {
        // ignore exceptions here - this is only for aesthetic reasons
      }
      frame.setLocationRelativeTo(MainFrame.this);
      frame.setVisible(true);
    }// actionPerformed();
  }// class NewAnnotDiffAction

  /**
   * This class represent an action which brings up the corpus evaluation tool
   */
  class NewCorpusEvalAction extends AbstractAction {
    public NewCorpusEvalAction() {
      super("Default mode");
      putValue(SHORT_DESCRIPTION, "Run the Benchmark Tool in its default mode");
      putValue(SMALL_ICON, getIcon("corpus-benchmark"));
    }// newCorpusEvalAction

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          JFileChooser chooser = MainFrame.getFileChooser();
          chooser.setDialogTitle("Please select a directory which contains "
            + "the documents to be evaluated");
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          chooser.setMultiSelectionEnabled(false);
          currentResourceClassName = CorpusBenchmarkTool.class.toString();
          int state = chooser.showOpenDialog(MainFrame.this);
          File startDir = chooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || startDir == null) return;

          chooser
            .setDialogTitle("Please select the application that you want to run");
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          currentResourceClassName =
            CorpusBenchmarkTool.class.toString()+".application";
          state = chooser.showOpenDialog(MainFrame.this);
          File testApp = chooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || startDir == null) return;

          // first create the tool and set its parameters
          CorpusBenchmarkTool theTool = new CorpusBenchmarkTool();
          theTool.setStartDirectory(startDir);
          theTool.setApplicationFile(testApp);
          theTool.setVerboseMode(verboseModeItem.isSelected());

          Out.prln("Please wait while GATE tools are initialised.");
          // initialise the tool
          theTool.init();
          // and execute it
          theTool.execute();
          theTool.printStatistics();

          Out.prln("<BR>Overall average precision: "
            + theTool.getPrecisionAverage());
          Out.prln("<BR>Overall average recall: " + theTool.getRecallAverage());
          Out.prln("<BR>Overall average fMeasure : "
            + theTool.getFMeasureAverage());
          Out.prln("<BR>Finished!");
          theTool.unloadPRs();
        }
      };
      Thread thread =
        new Thread(Thread.currentThread().getThreadGroup(), runnable,
          "Eval thread");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed();
  }// class NewCorpusEvalAction

  /**
   * This class represent an action which brings up the corpus evaluation tool
   */
  class StoredMarkedCorpusEvalAction extends AbstractAction {
    public StoredMarkedCorpusEvalAction() {
      super("Human marked against stored processing results");
      putValue(SHORT_DESCRIPTION, "Run the Benchmark Tool -stored_clean");
      putValue(SMALL_ICON, getIcon("corpus-benchmark"));
    }// newCorpusEvalAction

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          JFileChooser chooser = MainFrame.getFileChooser();
          chooser.setDialogTitle("Please select a directory which contains "
            + "the documents to be evaluated");
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          chooser.setMultiSelectionEnabled(false);
          currentResourceClassName = CorpusBenchmarkTool.class.toString();
          int state = chooser.showOpenDialog(MainFrame.this);
          File startDir = chooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || startDir == null) return;

          // first create the tool and set its parameters
          CorpusBenchmarkTool theTool = new CorpusBenchmarkTool();
          theTool.setStartDirectory(startDir);
          theTool.setMarkedStored(true);
          theTool.setVerboseMode(verboseModeItem.isSelected());
          // theTool.setMarkedDS(
          // MainFrame.this.datastoreModeCorpusEvalToolAction.isDatastoreMode());

          Out
            .prln("Evaluating human-marked documents against pre-stored results.");
          // initialise the tool
          theTool.init();
          // and execute it
          theTool.execute();
          theTool.printStatistics();

          Out.prln("<BR>Overall average precision: "
            + theTool.getPrecisionAverage());
          Out.prln("<BR>Overall average recall: " + theTool.getRecallAverage());
          Out.prln("<BR>Overall average fMeasure : "
            + theTool.getFMeasureAverage());
          Out.prln("<BR>Finished!");
          theTool.unloadPRs();
        }
      };
      Thread thread =
        new Thread(Thread.currentThread().getThreadGroup(), runnable,
          "Eval thread");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed();
  }// class StoredMarkedCorpusEvalActionpusEvalAction

  /**
   * This class represent an action which brings up the corpus evaluation tool
   */
  class CleanMarkedCorpusEvalAction extends AbstractAction {
    public CleanMarkedCorpusEvalAction() {
      super("Human marked against current processing results");
      putValue(SHORT_DESCRIPTION, "Run the Benchmark Tool -marked_clean");
      putValue(SMALL_ICON, getIcon("corpus-benchmark"));
    }// newCorpusEvalAction

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          JFileChooser chooser = MainFrame.getFileChooser();
          chooser.setDialogTitle("Please select a directory which contains "
            + "the documents to be evaluated");
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          chooser.setMultiSelectionEnabled(false);
          currentResourceClassName = CorpusBenchmarkTool.class.toString();
          int state = chooser.showOpenDialog(MainFrame.this);
          File startDir = chooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || startDir == null) return;

          chooser
            .setDialogTitle("Please select the application that you want to run");
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          currentResourceClassName =
            CorpusBenchmarkTool.class.toString()+".application";
          state = chooser.showOpenDialog(MainFrame.this);
          File testApp = chooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || startDir == null) return;

          // first create the tool and set its parameters
          CorpusBenchmarkTool theTool = new CorpusBenchmarkTool();
          theTool.setStartDirectory(startDir);
          theTool.setApplicationFile(testApp);
          theTool.setMarkedClean(true);
          theTool.setVerboseMode(verboseModeItem.isSelected());

          Out
            .prln("Evaluating human-marked documents against current processing results.");
          // initialise the tool
          theTool.init();
          // and execute it
          theTool.execute();
          theTool.printStatistics();

          Out.prln("Overall average precision: "
            + theTool.getPrecisionAverage());
          Out.prln("Overall average recall: " + theTool.getRecallAverage());
          Out
            .prln("Overall average fMeasure : " + theTool.getFMeasureAverage());
          Out.prln("Finished!");
          theTool.unloadPRs();
        }
      };
      Thread thread =
        new Thread(Thread.currentThread().getThreadGroup(), runnable,
          "Eval thread");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed();
  }// class CleanMarkedCorpusEvalActionpusEvalAction

  /**
   * This class represent an action which brings up the corpus evaluation tool
   */
  class GenerateStoredCorpusEvalAction extends AbstractAction {
    public GenerateStoredCorpusEvalAction() {
      super("Store corpus for future evaluation");
      putValue(SHORT_DESCRIPTION, "Run the Benchmark Tool -generate");
      putValue(SMALL_ICON, getIcon("corpus-benchmark"));
    }// newCorpusEvalAction

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          JFileChooser chooser = MainFrame.getFileChooser();
          chooser.setDialogTitle("Please select a directory which contains "
            + "the documents to be evaluated");
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          chooser.setMultiSelectionEnabled(false);
          currentResourceClassName = CorpusBenchmarkTool.class.toString();
          int state = chooser.showOpenDialog(MainFrame.this);
          File startDir = chooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || startDir == null) return;

          chooser
            .setDialogTitle("Please select the application that you want to run");
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          currentResourceClassName =
            CorpusBenchmarkTool.class.toString()+".application";
          state = chooser.showOpenDialog(MainFrame.this);
          File testApp = chooser.getSelectedFile();
          if(state == JFileChooser.CANCEL_OPTION || startDir == null) return;

          // first create the tool and set its parameters
          CorpusBenchmarkTool theTool = new CorpusBenchmarkTool();
          theTool.setStartDirectory(startDir);
          theTool.setApplicationFile(testApp);
          theTool.setGenerateMode(true);

          Out.prln("Processing and storing documents for future evaluation.");
          // initialise the tool
          theTool.init();
          // and execute it
          theTool.execute();
          theTool.unloadPRs();
          Out.prln("Finished!");
        }
      };
      Thread thread =
        new Thread(Thread.currentThread().getThreadGroup(), runnable,
          "Eval thread");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed();
  }// class GenerateStoredCorpusEvalAction

  /**
   * This class represent an action which brings up the corpus evaluation tool
   */
  class VerboseModeCorpusEvalToolAction extends AbstractAction {
    public VerboseModeCorpusEvalToolAction() {
      super("Verbose mode");
      putValue(SHORT_DESCRIPTION, "Run the Benchmark Tool in verbose mode");
    }// VerboseModeCorpusEvalToolAction

    public boolean isVerboseMode() {
      return verboseMode;
    }

    public void actionPerformed(ActionEvent e) {
      if(!(e.getSource() instanceof JCheckBoxMenuItem)) return;
      verboseMode = ((JCheckBoxMenuItem)e.getSource()).getState();
    }// actionPerformed();

    protected boolean verboseMode = false;
  }// class f

  /**
   * This class represent an action which brings up the corpus evaluation tool
   */
  /*
   * class DatastoreModeCorpusEvalToolAction extends AbstractAction { public
   * DatastoreModeCorpusEvalToolAction() { super("Use a datastore for human
   * annotated texts"); putValue(SHORT_DESCRIPTION,"Use a datastore for the
   * human annotated texts"); }// DatastoreModeCorpusEvalToolAction
   * 
   * public boolean isDatastoreMode() {return datastoreMode;}
   * 
   * public void actionPerformed(ActionEvent e) { if (! (e.getSource()
   * instanceof JCheckBoxMenuItem)) return; datastoreMode =
   * ((JCheckBoxMenuItem)e.getSource()).getState(); }// actionPerformed();
   * protected boolean datastoreMode = false; }//class
   * DatastoreModeCorpusEvalToolListener
   */

  /**
   * This class represent an action which loads ANNIE with default params
   */
  class LoadANNIEWithDefaultsAction extends AbstractAction implements
                                                          ANNIEConstants {
    public LoadANNIEWithDefaultsAction() {
      super("With defaults");
      putValue(SHORT_DESCRIPTION, "Load ANNIE system using defaults");
      putValue(SMALL_ICON, getIcon("annie-application"));
    }// NewAnnotDiffAction

    public void actionPerformed(ActionEvent e) {
      // Loads ANNIE with defaults
      Runnable runnable = new Runnable() {
        public void run() {
          long startTime = System.currentTimeMillis();
          FeatureMap params = Factory.newFeatureMap();
          try {
            // lock the gui
            lockGUI("ANNIE is being loaded...");
            // Create a serial analyser
            SerialAnalyserController sac =
              (SerialAnalyserController)Factory.createResource(
                "gate.creole.SerialAnalyserController",
                Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE_"
                  + Gate.genSym());
            // Load each PR as defined in
            // gate.creole.ANNIEConstants.PR_NAMES
            for(int i = 0; i < PR_NAMES.length; i++) {
              ProcessingResource pr =
                (ProcessingResource)Factory.createResource(PR_NAMES[i], params);
              // Add the PR to the sac
              sac.add(pr);
            }// End for

            long endTime = System.currentTimeMillis();
            statusChanged("ANNIE loaded in "
              + NumberFormat.getInstance().format(
                (double)(endTime - startTime) / 1000) + " seconds");
          }
          catch(gate.creole.ResourceInstantiationException ex) {
            ex.printStackTrace(Err.getPrintWriter());
          }
          finally {
            unlockGUI();
          }
        }// run()
      };// End Runnable
      Thread thread = new Thread(runnable, "");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed();
  }// class LoadANNIEWithDefaultsAction

  /**
   * This class represent an action which loads ANNIE with default params
   */
  class LoadANNIEWithoutDefaultsAction extends AbstractAction implements
                                                             ANNIEConstants {
    public LoadANNIEWithoutDefaultsAction() {
      super("Without defaults");
      putValue(SHORT_DESCRIPTION, "Load ANNIE system without defaults");
      putValue(SMALL_ICON, getIcon("annie-application"));
    }// NewAnnotDiffAction

    public void actionPerformed(ActionEvent e) {
      // Loads ANNIE with defaults
      Runnable runnable = new Runnable() {
        public void run() {
          FeatureMap params = Factory.newFeatureMap();
          try {
            // Create a serial analyser
            SerialAnalyserController sac =
              (SerialAnalyserController)Factory.createResource(
                "gate.creole.SerialAnalyserController",
                Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE_"
                  + Gate.genSym());
            // NewResourceDialog resourceDialog = new NewResourceDialog(
            // MainFrame.this, "Resource parameters", true );
            // Load each PR as defined in
            // gate.creole.ANNIEConstants.PR_NAMES
            for(int i = 0; i < PR_NAMES.length; i++) {
              // get the params for the Current PR
              ResourceData resData =
                (ResourceData)Gate.getCreoleRegister().get(PR_NAMES[i]);
              currentResourceClassName = resData.getClassName();
              if(newResourceDialog.show(resData, "Parameters for the new "
                + resData.getName())) {
                sac.add((ProcessingResource)Factory.createResource(PR_NAMES[i],
                  newResourceDialog.getSelectedParameters()));
              }
              else {
                // the user got bored and aborted the operation
                statusChanged("Loading cancelled! Removing traces...");
                Iterator loadedPRsIter = new ArrayList(sac.getPRs()).iterator();
                while(loadedPRsIter.hasNext()) {
                  Factory.deleteResource((ProcessingResource)loadedPRsIter
                    .next());
                }
                Factory.deleteResource(sac);
                statusChanged("Loading cancelled!");
                return;
              }
            }// End for
            statusChanged("ANNIE loaded!");
          }
          catch(gate.creole.ResourceInstantiationException ex) {
            ex.printStackTrace(Err.getPrintWriter());
          }// End try
        }// run()
      };// End Runnable
      SwingUtilities.invokeLater(runnable);
      // Thread thread = new Thread(runnable, "");
      // thread.setPriority(Thread.MIN_PRIORITY);
      // thread.start();
    }// actionPerformed();
  }// class LoadANNIEWithoutDefaultsAction

  /**
   * This class represent an action which loads ANNIE without default param
   */
  class LoadANNIEWithoutDefaultsAction1 extends AbstractAction implements
                                                              ANNIEConstants {
    public LoadANNIEWithoutDefaultsAction1() {
      super("Without defaults");
    }// NewAnnotDiffAction

    public void actionPerformed(ActionEvent e) {
      // Load ANNIE without defaults
      CreoleRegister reg = Gate.getCreoleRegister();
      // Load each PR as defined in gate.creole.ANNIEConstants.PR_NAMES
      for(int i = 0; i < PR_NAMES.length; i++) {
        ResourceData resData = (ResourceData)reg.get(PR_NAMES[i]);
        if(resData != null) {
          NewResourceDialog resourceDialog =
            new NewResourceDialog(MainFrame.this, "Resource parameters", true);
          resourceDialog
            .setTitle("Parameters for the new " + resData.getName());
          currentResourceClassName = resData.getClassName();
          resourceDialog.show(resData);
        }
        else {
          Err.prln(PR_NAMES[i] + " not found in Creole register");
        }// End if
      }// End for
      try {
        // Create an application at the end.
        Factory.createResource("gate.creole.SerialAnalyserController", Factory
          .newFeatureMap(), Factory.newFeatureMap(), "ANNIE_" + Gate.genSym());
      }
      catch(gate.creole.ResourceInstantiationException ex) {
        ex.printStackTrace(Err.getPrintWriter());
      }// End try
    }// actionPerformed();
  }// class LoadANNIEWithoutDefaultsAction

  class NewBootStrapAction extends AbstractAction {
    public NewBootStrapAction() {
      super("BootStrap Wizard", getIcon("application"));
    }// NewBootStrapAction

    public void actionPerformed(ActionEvent e) {
      BootStrapDialog bootStrapDialog = new BootStrapDialog(MainFrame.this);
      bootStrapDialog.setVisible(true);
    }// actionPerformed();
  }// class NewBootStrapAction

  class ManagePluginsAction extends AbstractAction {
    public ManagePluginsAction() {
      super("Manage CREOLE plugins");
      putValue(SHORT_DESCRIPTION, "Manage CREOLE plugins");
      putValue(SMALL_ICON, getIcon("creole-plugins"));
    }

    public void actionPerformed(ActionEvent e) {
      if(pluginManager == null) {
        pluginManager = new PluginManagerUI(MainFrame.this);
        // pluginManager.setLocationRelativeTo(MainFrame.this);
        pluginManager.setModal(true);
        getGuiRoots().add(pluginManager);
        pluginManager.pack();
        // size the window so that it doesn't go off-screen
        Dimension screenSize = /* Toolkit.getDefaultToolkit().getScreenSize(); */
        getGraphicsConfiguration().getBounds().getSize();
        Dimension dialogSize = pluginManager.getPreferredSize();
        int width =
          dialogSize.width > screenSize.width
            ? screenSize.width * 3 / 4
            : dialogSize.width;
        int height =
          dialogSize.height > screenSize.height
            ? screenSize.height * 3 / 4
            : dialogSize.height;
        pluginManager.setSize(width, height);
        pluginManager.validate();
        // center the window on screen
        int x = (screenSize.width - width) / 2;
        int y = (screenSize.height - height) / 2;
        pluginManager.setLocation(x, y);
      }
      currentResourceClassName = "gate.PluginManager";
      pluginManager.setVisible(true);
      // free resources after the dialog is hidden
      pluginManager.dispose();
    }
  }

  class LoadCreoleRepositoryAction extends AbstractAction {
    public LoadCreoleRepositoryAction() {
      super("Load a CREOLE repository");
      putValue(SHORT_DESCRIPTION, "Load a CREOLE repository");
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

      class URLfromFileAction extends AbstractAction {
        URLfromFileAction(JTextField textField) {
          super(null, getIcon("open-file"));
          putValue(SHORT_DESCRIPTION, "Click to select a directory");
          this.textField = textField;
        }

        public void actionPerformed(ActionEvent e) {
          fileChooser.setMultiSelectionEnabled(false);
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          currentResourceClassName = "gate.CreoleRegister";
          int result = fileChooser.showOpenDialog(MainFrame.this);
          if(result == JFileChooser.APPROVE_OPTION) {
            try {
              textField.setText(fileChooser.getSelectedFile().toURI().toURL()
                .toExternalForm());
            }
            catch(MalformedURLException mue) {
              throw new GateRuntimeException(mue.toString());
            }
          }
        }

        JTextField textField;
      }
      ;// class URLfromFileAction extends AbstractAction

      Box rightBox = Box.createVerticalBox();
      rightBox.add(new JLabel("Select a directory"));
      JButton fileBtn = new JButton(new URLfromFileAction(urlTextField));
      rightBox.add(fileBtn);
      messageBox.add(rightBox);

      // JOptionPane.showInputDialog(
      // MainFrame.this,
      // "Select type of Datastore",
      // "Gate", JOptionPane.QUESTION_MESSAGE,
      // null, names,
      // names[0]);

      int res =
        JOptionPane.showConfirmDialog(MainFrame.this, messageBox,
          "Enter an URL to the directory containig the "
            + "\"creole.xml\" file", JOptionPane.OK_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE, null);
      if(res == JOptionPane.OK_OPTION) {
        try {
          URL creoleURL = new URL(urlTextField.getText());
          Gate.getCreoleRegister().registerDirectories(creoleURL);
        }
        catch(Exception ex) {
          JOptionPane.showMessageDialog(MainFrame.this,
            "There was a problem with your selection:\n" + ex.toString(),
            "GATE", JOptionPane.ERROR_MESSAGE);
          ex.printStackTrace(Err.getPrintWriter());
        }
      }
    }
  }// class LoadCreoleRepositoryAction extends AbstractAction

  class NewResourceAction extends AbstractAction {
    /** Used for creation of resource menu item and creation dialog */
    ResourceData rData;

    public NewResourceAction(ResourceData rData) {
      super(rData.getName());
      putValue(SHORT_DESCRIPTION, rData.getComment());
      this.rData = rData;
      putValue(SMALL_ICON, getIcon(rData.getIcon()));
    } // NewResourceAction(ResourceData rData)

    public void actionPerformed(ActionEvent evt) {
      Runnable runnable = new Runnable() {
        public void run() {
          newResourceDialog.setTitle("Parameters for the new "
            + rData.getName());
          currentResourceClassName = rData.getClassName();
          newResourceDialog.show(rData);
        }
      };
      SwingUtilities.invokeLater(runnable);
    } // actionPerformed
  } // class NewResourceAction extends AbstractAction

  static class StopAction extends AbstractAction {
    public StopAction() {
      super(" Stop! ");
      putValue(SHORT_DESCRIPTION, "Stops the current action");
    }

    public boolean isEnabled() {
      return Gate.getExecutable() != null;
    }

    public void actionPerformed(ActionEvent e) {
      Executable ex = Gate.getExecutable();
      if(ex != null) ex.interrupt();
    }
  }

  /** Method is used in NewDSAction */
  protected DataStore createSearchableDataStore() {
    try {
      JPanel mainPanel = new JPanel(new GridBagLayout());

      final JTextField dsLocation = new JTextField("", 20);
      final JTextField indexLocation = new JTextField("", 20);

      JTextField btat = new JTextField("Token", 20);
      btat.setToolTipText("Examples: Token, AnnotationSetName.Token, "
        + Constants.DEFAULT_ANNOTATION_SET_NAME + ".Token");
      JCheckBox createTokensAutomatically =
        new JCheckBox("Create Tokens Automatically");
      createTokensAutomatically.setSelected(true);
      JTextField iuat = new JTextField("", 20);
      iuat.setToolTipText("Examples: Sentence, AnnotationSetName.Sentence, "
        + Constants.DEFAULT_ANNOTATION_SET_NAME + ".Sentence");

      final List<String> inputASList = new ArrayList<String>();
      inputASList.add("Key");
      final JTextField inputAS = new JTextField("", 20);
      inputAS.setText("Key");
      inputAS.setEditable(false);
      JButton editInputAS = new JButton(MainFrame.getIcon("edit-list"));
      editInputAS.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          ListEditorDialog listEditor =
            new ListEditorDialog(getInstance(), inputASList, "java.lang.String");
          List result = listEditor.showDialog();
          if(result != null) {
            inputASList.clear();
            inputASList.addAll(result);
            if(inputASList.size() > 0) {
              String text =
                inputASList.get(0) == null
                  ? Constants.DEFAULT_ANNOTATION_SET_NAME
                  : inputASList.get(0).toString();
              for(int j = 1; j < inputASList.size(); j++) {
                text +=
                  ";"
                    + (inputASList.get(j) == null
                      ? Constants.DEFAULT_ANNOTATION_SET_NAME
                      : inputASList.get(j).toString());
              }
              inputAS.setText(text);
            }
            else {
              inputAS.setText("");
            }
          }
        }
      });

      JComboBox asie = new JComboBox(new String[]{"include", "exclude"});
      inputAS.setToolTipText("Leave blank for indexing all annotation sets");

      final List<String> fteList = new ArrayList<String>();
      fteList.add("SpaceToken");
      fteList.add("Split");
      final JTextField fte = new JTextField("", 20);
      fte.setText("SpaceToken;Split");
      fte.setEditable(false);
      JButton editFTE = new JButton(MainFrame.getIcon("edit-list"));
      editFTE.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          ListEditorDialog listEditor =
            new ListEditorDialog(getInstance(), fteList, "java.lang.String");
          List result = listEditor.showDialog();
          if(result != null) {
            fteList.clear();
            fteList.addAll(result);
            if(fteList.size() > 0) {
              String text =
                fteList.get(0) == null
                  ? Constants.DEFAULT_ANNOTATION_SET_NAME
                  : fteList.get(0).toString();
              for(int j = 1; j < fteList.size(); j++) {
                text +=
                  ";"
                    + (fteList.get(j) == null
                      ? Constants.DEFAULT_ANNOTATION_SET_NAME
                      : fteList.get(j).toString());
              }
              fte.setText(text);
            }
            else {
              fte.setText("");
            }
          }
        }
      });

      JComboBox ftie = new JComboBox(new String[]{"include", "exclude"});
      ftie.setSelectedIndex(1);
      fte.setToolTipText("Leave blank for inclusion of all features");

      JButton dsBrowse = new JButton(MainFrame.getIcon("open-file"));
      JButton indexBrowse = new JButton(MainFrame.getIcon("open-file"));
      dsBrowse.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          // first we need to ask for a new empty directory
          fileChooser
            .setDialogTitle("Please create a new empty directory for datastore");
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          currentResourceClassName = "gate.DataStore.data";
          if(fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
            try {
              dsLocation.setText(fileChooser.getSelectedFile().toURI().toURL()
                .toExternalForm());
            }
            catch(Exception e) {
              dsLocation.setText("");
            }
          }
        }
      });

      indexBrowse.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
          // first we need to ask for a new empty directory
          fileChooser
            .setDialogTitle("Please create a new empty directory for datastore");
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          currentResourceClassName = "gate.DataStore.index";
          if(fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
            try {
              indexLocation.setText(fileChooser.getSelectedFile().toURI()
                .toURL().toExternalForm());
            }
            catch(Exception e) {
              indexLocation.setText("");
            }
          }
        }
      });

      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 0;
      constraints.gridwidth = 3;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Datastore URL:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 0;
      constraints.gridwidth = 5;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(dsLocation, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 0;
      constraints.gridwidth = 1;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(dsBrowse, constraints);
      dsBrowse.setBorderPainted(false);
      dsBrowse.setContentAreaFilled(false);

      // second row
      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 1;
      constraints.gridwidth = 3;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Index Location:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 1;
      constraints.gridwidth = 5;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(indexLocation, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 1;
      constraints.gridwidth = 1;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(indexBrowse, constraints);
      indexBrowse.setBorderPainted(false);
      indexBrowse.setContentAreaFilled(false);

      // third row row
      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 2;
      constraints.gridwidth = 2;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Annotation Sets:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 2;
      constraints.gridwidth = 1;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(asie, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 2;
      constraints.gridwidth = 5;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(inputAS, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 2;
      constraints.gridwidth = 1;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(editInputAS, constraints);
      editInputAS.setBorderPainted(false);
      editInputAS.setContentAreaFilled(false);

      // fourth row row
      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 3;
      constraints.gridwidth = 3;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Base Token Type:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 3;
      constraints.gridwidth = 5;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(btat, constraints);

      // fifth row
      constraints = new GridBagConstraints();
      constraints.gridx = 4;
      constraints.gridy = 4;
      constraints.gridwidth = 5;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(createTokensAutomatically, constraints);

      // sixth row
      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 5;
      constraints.gridwidth = 3;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Index Unit Type:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 5;
      constraints.gridwidth = 5;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(iuat, constraints);

      // seventh row
      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 6;
      constraints.gridwidth = 2;
      constraints.anchor = GridBagConstraints.WEST;
      constraints.fill = GridBagConstraints.NONE;
      constraints.insets = new Insets(0, 0, 0, 5);
      mainPanel.add(new JLabel("Features:"), constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 6;
      constraints.gridwidth = 1;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(ftie, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 6;
      constraints.gridwidth = 5;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      constraints.insets = new Insets(0, 0, 0, 10);
      mainPanel.add(fte, constraints);

      constraints = new GridBagConstraints();
      constraints.gridx = GridBagConstraints.RELATIVE;
      constraints.gridy = 6;
      constraints.gridwidth = 1;
      constraints.anchor = GridBagConstraints.NORTHWEST;
      mainPanel.add(editFTE, constraints);
      editFTE.setBorderPainted(false);
      editFTE.setContentAreaFilled(false);

      int returnValue =
        JOptionPane.showOptionDialog(MainFrame.getInstance(), mainPanel,
          "SearchableDataStore", JOptionPane.PLAIN_MESSAGE,
          JOptionPane.OK_CANCEL_OPTION, MainFrame.getIcon("empty"),
          new String[]{"OK", "Cancel"}, "OK");
      if(returnValue == JOptionPane.OK_OPTION) {

        DataStore ds =
          Factory.createDataStore("gate.persist.LuceneDataStoreImpl",
            dsLocation.getText());

        // we need to set Indexer
        Class[] consParam = new Class[1];
        consParam[0] = URL.class;
        Constructor constructor =
          Class.forName("gate.creole.annic.lucene.LuceneIndexer", true,
            Gate.getClassLoader()).getConstructor(consParam);
        Object indexer =
          constructor.newInstance(new URL(indexLocation.getText()));

        Map parameters = new HashMap();
        parameters.put(Constants.INDEX_LOCATION_URL, new URL(indexLocation
          .getText()));
        parameters.put(Constants.BASE_TOKEN_ANNOTATION_TYPE, btat.getText());
        parameters.put(Constants.INDEX_UNIT_ANNOTATION_TYPE, iuat.getText());
        parameters.put(Constants.CREATE_TOKENS_AUTOMATICALLY, new Boolean(
          createTokensAutomatically.isSelected()));

        if(inputAS.getText().trim().length() > 0) {
          ArrayList<String> inputASList1 = new ArrayList<String>();
          String[] inputASArray = inputAS.getText().trim().split(";");
          if(inputASArray != null && inputASArray.length > 0) {
            for(int k = 0; k < inputASArray.length; k++) {
              inputASList1.add(inputASArray[k]);
            }
          }
          if(asie.getSelectedIndex() == 0) {
            // user has provided values for inclusion
            parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_INCLUDE,
              inputASList1);
            parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_EXCLUDE,
              new ArrayList<String>());
          }
          else {
            // user has provided values for exclusion
            parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_EXCLUDE,
              inputASList1);
            parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_INCLUDE,
              new ArrayList<String>());
          }
        }
        else {
          parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_EXCLUDE,
            new ArrayList<String>());
          parameters.put(Constants.ANNOTATION_SETS_NAMES_TO_INCLUDE,
            new ArrayList<String>());
        }

        if(fte.getText().trim().length() > 0) {
          ArrayList<String> fteList1 = new ArrayList<String>();
          String[] inputASArray = fte.getText().trim().split(";");
          if(inputASArray != null && inputASArray.length > 0) {
            for(int k = 0; k < inputASArray.length; k++) {
              fteList1.add(inputASArray[k]);
            }
          }
          if(ftie.getSelectedIndex() == 0) {
            // user has provided values for inclusion
            parameters.put(Constants.FEATURES_TO_INCLUDE, fteList1);
            parameters.put(Constants.FEATURES_TO_EXCLUDE,
              new ArrayList<String>());
          }
          else {
            // user has provided values for exclusion
            parameters.put(Constants.FEATURES_TO_EXCLUDE, fteList1);
            parameters.put(Constants.FEATURES_TO_INCLUDE,
              new ArrayList<String>());
          }
        }
        else {
          parameters
            .put(Constants.FEATURES_TO_EXCLUDE, new ArrayList<String>());
          parameters
            .put(Constants.FEATURES_TO_INCLUDE, new ArrayList<String>());
        }

        Class[] params = new Class[2];
        params[0] =
          Class.forName("gate.creole.annic.Indexer", true, Gate
            .getClassLoader());
        params[1] = Map.class;
        Method indexerMethod = ds.getClass().getMethod("setIndexer", params);
        indexerMethod.invoke(ds, indexer, parameters);

        // Class[] searchConsParams = new Class[0];
        Constructor searcherConst =
          Class.forName("gate.creole.annic.lucene.LuceneSearcher", true,
            Gate.getClassLoader()).getConstructor();
        Object searcher = searcherConst.newInstance();
        Class[] searchParams = new Class[1];
        searchParams[0] =
          Class.forName("gate.creole.annic.Searcher", true, Gate
            .getClassLoader());
        Method searcherMethod =
          ds.getClass().getMethod("setSearcher", searchParams);
        searcherMethod.invoke(ds, searcher);
        return ds;
      }
      return null;
    }
    catch(Exception e) {
      throw new GateRuntimeException(e);
    }
  } // createSearchableDataStore()

  /** Method is used in OpenDSAction */
  protected DataStore openSearchableDataStore() {
    DataStore ds = null;

    // get the URL (a file in this case)
    fileChooser.setDialogTitle("Select the datastore directory");
    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    currentResourceClassName = "gate.DataStore.data";
    if(fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
      try {
        URL dsURL = fileChooser.getSelectedFile().toURI().toURL();
        ds =
          Factory.openDataStore("gate.persist.LuceneDataStoreImpl", dsURL
            .toExternalForm());
      }
      catch(MalformedURLException mue) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Invalid location for the datastore\n " + mue.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      }
      catch(PersistenceException pe) {
        JOptionPane.showMessageDialog(MainFrame.this,
          "Datastore opening error!\n " + pe.toString(), "GATE",
          JOptionPane.ERROR_MESSAGE);
      } // catch
    } // if

    return ds;
  } // openSerialDataStore()

  class NewDSAction extends AbstractAction {
    public NewDSAction() {
      super("Create datastore");
      putValue(SHORT_DESCRIPTION, "Create a new Datastore");
      putValue(SMALL_ICON, getIcon("datastore"));
    }

    public void actionPerformed(ActionEvent e) {
      DataStoreRegister reg = Gate.getDataStoreRegister();
      Map dsTypes = DataStoreRegister.getDataStoreClassNames();
      HashMap dsTypeByName = new HashMap();
      Iterator dsTypesIter = dsTypes.entrySet().iterator();
      while(dsTypesIter.hasNext()) {
        Map.Entry entry = (Map.Entry)dsTypesIter.next();
        dsTypeByName.put(entry.getValue(), entry.getKey());
      }

      if(!dsTypeByName.isEmpty()) {
        Object[] names = dsTypeByName.keySet().toArray();
        Object answer =
          JOptionPane.showInputDialog(MainFrame.this,
            "Select type of Datastore", "GATE", JOptionPane.QUESTION_MESSAGE,
            null, names, names[0]);
        if(answer != null) {
          String className = (String)dsTypeByName.get(answer);
          if(className.equals("gate.persist.SerialDataStore")) {
            createSerialDataStore();
          }
          else if(className.equals("gate.persist.LuceneDataStoreImpl")) {
            createSearchableDataStore();
          }
          else if(className.equals("gate.persist.OracleDataStore")) {
            JOptionPane.showMessageDialog(MainFrame.this,
              "Oracle datastores can only be created "
                + "by your Oracle administrator!", "GATE",
              JOptionPane.ERROR_MESSAGE);
          }
          else {

            throw new UnsupportedOperationException("Unimplemented option!\n"
              + "Use a serial datastore");
          }
        }
      }
      else {
        // no ds types
        JOptionPane.showMessageDialog(MainFrame.this,
          "Could not find any registered types " + "of datastores...\n"
            + "Check your GATE installation!", "GATE",
          JOptionPane.ERROR_MESSAGE);

      }
    }
  }// class NewDSAction extends AbstractAction

  class LoadResourceFromFileAction extends AbstractAction {
    public LoadResourceFromFileAction() {
      super("Restore application from file");
      putValue(SHORT_DESCRIPTION, "Restores a previously saved application");
      putValue(SMALL_ICON, getIcon("open-application"));
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          fileChooser.setDialogTitle("Select a file for this resource");
          fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
          fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
          currentResourceClassName = "gate.ApplicationRestore";
          if(fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
              gate.util.persistence.PersistenceManager.loadObjectFromFile(file);
            }
            catch(ResourceInstantiationException rie) {
              processFinished();
              JOptionPane.showMessageDialog(MainFrame.this, "Error!\n"
                + rie.toString(), "GATE", JOptionPane.ERROR_MESSAGE);
              rie.printStackTrace(Err.getPrintWriter());
            }
            catch(Exception ex) {
              processFinished();
              JOptionPane.showMessageDialog(MainFrame.this, "Error!\n"
                + ex.toString(), "GATE", JOptionPane.ERROR_MESSAGE);
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
   * Closes the view associated to a resource. Does not remove the resource from
   * the system, only its view.
   */
  class CloseViewAction extends AbstractAction {
    public CloseViewAction(Handle handle) {
      super("Hide this view");
      putValue(SHORT_DESCRIPTION, "Hides this view");
      this.handle = handle;
    }

    public void actionPerformed(ActionEvent e) {
      mainTabbedPane.remove(handle.getLargeView());
      mainTabbedPane.setSelectedIndex(mainTabbedPane.getTabCount() - 1);
      // remove all GUI resources used by this handle
      handle.removeViews();
    }// public void actionPerformed(ActionEvent e)

    Handle handle;
  }// class CloseViewAction

  class RenameResourceAction extends AbstractAction {
    RenameResourceAction(TreePath path) {
      super("Rename");
      putValue(SHORT_DESCRIPTION, "Renames the resource");
      this.path = path;
    }

    public void actionPerformed(ActionEvent e) {
      resourcesTree.startEditingAtPath(path);
    }

    TreePath path;
  }

  class CloseSelectedResourcesAction extends AbstractAction {
    public CloseSelectedResourcesAction() {
      super("Close all");
      putValue(SHORT_DESCRIPTION, "Closes the selected resources");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runner = new Runnable() {
        public void run() {
          TreePath[] paths = resourcesTree.getSelectionPaths();
          for(int i = 0; i < paths.length; i++) {
            Object userObject =
              ((DefaultMutableTreeNode)paths[i].getLastPathComponent())
                .getUserObject();
            if(userObject instanceof NameBearerHandle) {
              ((NameBearerHandle)userObject).getCloseAction().actionPerformed(
                null);
            }
          }
        }
      };
      Thread thread = new Thread(runner);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  /**
   * Closes the view associated to a resource. Does not remove the resource from
   * the system, only its view.
   */
  class ExitGateAction extends AbstractAction {
    public ExitGateAction() {
      super("Exit GATE");
      putValue(SHORT_DESCRIPTION, "Closes the application");
      putValue(SMALL_ICON, getIcon("exit"));
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          // save the options
          OptionsMap userConfig = Gate.getUserConfig();
          if(userConfig.getBoolean(GateConstants.SAVE_OPTIONS_ON_EXIT)
            .booleanValue()) {
            // save the window size
            Integer width = new Integer(MainFrame.this.getWidth());
            Integer height = new Integer(MainFrame.this.getHeight());
            userConfig.put(GateConstants.MAIN_FRAME_WIDTH, width);
            userConfig.put(GateConstants.MAIN_FRAME_HEIGHT, height);
            try {
              Gate.writeUserConfig();
            }
            catch(GateException ge) {
              logArea.getOriginalErr().println("Failed to save config data:");
              ge.printStackTrace(logArea.getOriginalErr());
            }
          }
          else {
            // don't save options on close
            // save the option not to save the options
            OptionsMap originalUserConfig = Gate.getOriginalUserConfig();
            originalUserConfig.put(GateConstants.SAVE_OPTIONS_ON_EXIT,
              new Boolean(false));
            userConfig.clear();
            userConfig.putAll(originalUserConfig);
            try {
              Gate.writeUserConfig();
            }
            catch(GateException ge) {
              logArea.getOriginalErr().println("Failed to save config data:");
              ge.printStackTrace(logArea.getOriginalErr());
            }
          }

          // save the session;
          File sessionFile = Gate.getUserSessionFile();
          if(userConfig.getBoolean(GateConstants.SAVE_SESSION_ON_EXIT)
            .booleanValue()) {
            // save all the open applications
            try {
              ArrayList appList =
                new ArrayList(Gate.getCreoleRegister().getAllInstances(
                  "gate.Controller"));
              // remove all hidden instances
              Iterator appIter = appList.iterator();
              while(appIter.hasNext())
                if(Gate.getHiddenAttribute(((Controller)appIter.next())
                  .getFeatures())) appIter.remove();

              gate.util.persistence.PersistenceManager.saveObjectToFile(
                appList, sessionFile);
            }
            catch(Exception ex) {
              logArea.getOriginalErr().println("Failed to save session data:");
              ex.printStackTrace(logArea.getOriginalErr());
            }
          }
          else {
            // we don't want to save the session
            if(sessionFile.exists()) sessionFile.delete();
          }
          // restore out and err streams as we're about to hide the
          // windows
          System.setErr(logArea.getOriginalErr());
          System.setOut(logArea.getOriginalOut());
          // now we need to dispose all GUI roots
          List<Window> roots = new ArrayList<Window>(getGuiRoots());
          while(!roots.isEmpty()) {
            Object aRoot = roots.remove(0);
            if(aRoot instanceof Window) {
              Window window = (Window)aRoot;
              roots.addAll(Arrays.asList(window.getOwnedWindows()));
              window.setVisible(false);
              window.dispose();
            }
          }

          // only hidden when closed
          helpFrame.dispose();

          // trying to release all resources occupied by all
          try {
            List<Resource> resources =
              Gate.getCreoleRegister().getAllInstances(
                gate.Resource.class.getName());

            // we need to call the clean up method for each of these
            // resources
            for(Resource aResource : resources) {
              try {
                // System.out.print("Cleaning up :" + aResource.getName());
                aResource.cleanup();
                // System.out.println(" Done!");
              }
              catch(Throwable e) {
                // this may throw somekind of exception
                // but we just ignore it as anyway we are closing
                // everything
                System.err.println(" Some problem cleaning up the resource "
                  + e.getMessage());
              }
            }

            // close all the opened datastores
            if(Gate.getDataStoreRegister() != null) {
              Set dataStores = new HashSet(Gate.getDataStoreRegister());
              for(Object aDs : dataStores) {
                try {
                  if(aDs instanceof DataStore) {
                    ((DataStore)aDs).close();
                  }
                }
                catch(Throwable e) {
                  System.err.println("Some problem in closing the datastore"
                    + e.getMessage());
                }
              }
            }

          }
          catch(GateException exception) {
            // we just ignore this
          }

        }// run
      };// Runnable
      Thread thread =
        new Thread(Thread.currentThread().getThreadGroup(), runnable,
          "Shutdown thread");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  class OpenDSAction extends AbstractAction {
    public OpenDSAction() {
      super("Open datastore");
      putValue(SHORT_DESCRIPTION, "Open a datastore");
      putValue(SMALL_ICON, getIcon("datastore"));
    }

    public void actionPerformed(ActionEvent e) {
      DataStoreRegister reg = Gate.getDataStoreRegister();
      Map dsTypes = DataStoreRegister.getDataStoreClassNames();
      HashMap dsTypeByName = new HashMap();
      Iterator dsTypesIter = dsTypes.entrySet().iterator();
      while(dsTypesIter.hasNext()) {
        Map.Entry entry = (Map.Entry)dsTypesIter.next();
        dsTypeByName.put(entry.getValue(), entry.getKey());
      }

      if(!dsTypeByName.isEmpty()) {
        Object[] names = dsTypeByName.keySet().toArray();
        Object answer =
          JOptionPane.showInputDialog(MainFrame.this,
            "Select type of Datastore", "GATE", JOptionPane.QUESTION_MESSAGE,
            null, names, names[0]);
        if(answer != null) {
          String className = (String)dsTypeByName.get(answer);
          if(className.indexOf("SerialDataStore") != -1) {
            openSerialDataStore();
          }
          else if(className.indexOf("LuceneDataStoreImpl") != -1) {
            openSearchableDataStore();
          }
          else if(className.indexOf("DocServiceDataStore") != -1) {
            openDocServiceDataStore();
          }
          else if(className.equals("gate.persist.OracleDataStore")
            || className.equals("gate.persist.PostgresDataStore")) {
            List dbPaths = new ArrayList();
            Iterator keyIter =
              DataStoreRegister.getConfigData().keySet().iterator();
            while(keyIter.hasNext()) {
              String keyName = (String)keyIter.next();
              if(keyName.startsWith("url"))
                dbPaths.add(DataStoreRegister.getConfigData().get(keyName));
            }
            if(dbPaths.isEmpty())
              throw new GateRuntimeException(
                "JDBC URL not configured in gate.xml");
            // by default make it the first
            String storageURL = (String)dbPaths.get(0);
            if(dbPaths.size() > 1) {
              Object[] paths = dbPaths.toArray();
              answer =
                JOptionPane.showInputDialog(MainFrame.this,
                  "Select a database", "GATE", JOptionPane.QUESTION_MESSAGE,
                  null, paths, paths[0]);
              if(answer != null)
                storageURL = (String)answer;
              else return;
            }
            DataStore ds = null;
            AccessController ac = null;
            try {
              // 1. login the user
              // ac = new AccessControllerImpl(storageURL);
              ac = Factory.createAccessController(storageURL);
              Assert.assertNotNull(ac);
              ac.open();

              Session mySession = null;
              User usr = null;
              Group grp = null;
              try {
                String userName = "";
                String userPass = "";
                String group = "";

                JPanel listPanel = new JPanel();
                listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.X_AXIS));

                JPanel panel1 = new JPanel();
                panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
                panel1.add(new JLabel("User name: "));
                panel1.add(new JLabel("Password: "));
                panel1.add(new JLabel("Group: "));

                JPanel panel2 = new JPanel();
                panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
                JTextField usrField = new JTextField(30);
                panel2.add(usrField);
                JPasswordField pwdField = new JPasswordField(30);
                panel2.add(pwdField);
                JComboBox grpField = new JComboBox(ac.listGroups().toArray());
                grpField.setSelectedIndex(0);
                panel2.add(grpField);

                listPanel.add(panel1);
                listPanel.add(Box.createHorizontalStrut(20));
                listPanel.add(panel2);

                if(OkCancelDialog.showDialog(MainFrame.this.getContentPane(),
                  listPanel, "Please enter login details")) {

                  userName = usrField.getText();
                  userPass = new String(pwdField.getPassword());
                  group = (String)grpField.getSelectedItem();

                  if(userName.equals("") || userPass.equals("")
                    || group.equals("")) {
                    JOptionPane
                      .showMessageDialog(
                        MainFrame.this,
                        "You must provide non-empty user name, password and group!",
                        "Login error", JOptionPane.ERROR_MESSAGE);
                    return;
                  }
                }
                else if(OkCancelDialog.userHasPressedCancel) { return; }

                grp = ac.findGroup(group);
                usr = ac.findUser(userName);
                mySession = ac.login(userName, userPass, grp.getID());

                // save here the user name, pass and group in
                // local gate.xml

              }
              catch(gate.security.SecurityException ex) {
                JOptionPane.showMessageDialog(MainFrame.this, ex.getMessage(),
                  "Login error", JOptionPane.ERROR_MESSAGE);
                ac.close();
                return;
              }

              if(!ac.isValidSession(mySession)) {
                JOptionPane.showMessageDialog(MainFrame.this,
                  "Incorrect session obtained. "
                    + "Probably there is a problem with the database!",
                  "Login error", JOptionPane.ERROR_MESSAGE);
                ac.close();
                return;
              }

              // 2. open the oracle datastore
              ds = Factory.openDataStore(className, storageURL);
              // set the session, so all get/adopt/etc work
              ds.setSession(mySession);

              // 3. add the security data for this datastore
              // this saves the user and group information, so it
              // can
              // be used later when resources are created with
              // certain rights
              FeatureMap securityData = Factory.newFeatureMap();
              securityData.put("user", usr);
              securityData.put("group", grp);
              DataStoreRegister.addSecurityData(ds, securityData);
            }
            catch(PersistenceException pe) {
              JOptionPane.showMessageDialog(MainFrame.this,
                "Datastore open error!\n " + pe.toString(), "GATE",
                JOptionPane.ERROR_MESSAGE);
            }
            catch(gate.security.SecurityException se) {
              JOptionPane.showMessageDialog(MainFrame.this,
                "User identification error!\n " + se.toString(), "GATE",
                JOptionPane.ERROR_MESSAGE);
              try {
                if(ac != null) ac.close();
                if(ds != null) ds.close();
              }
              catch(gate.persist.PersistenceException ex) {
                JOptionPane.showMessageDialog(MainFrame.this,
                  "Persistence error!\n " + ex.toString(), "GATE",
                  JOptionPane.ERROR_MESSAGE);
              }
            }

          }
          else {
            JOptionPane.showMessageDialog(MainFrame.this,
              "Support for this type of datastores is not implemenented!\n",
              "GATE", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
      else {
        // no ds types
        JOptionPane.showMessageDialog(MainFrame.this,
          "Could not find any registered types " + "of datastores...\n"
            + "Check your GATE installation!", "GATE",
          JOptionPane.ERROR_MESSAGE);

      }
    }
  }// class OpenDSAction extends AbstractAction

  /**
   * A menu that self populates based on CREOLE register data before being
   * shown. Used for creating new resources of all possible types.
   */
  class LiveMenu extends XJMenu {
    public LiveMenu(int type) {
      super();
      this.type = type;
      init();
    }

    protected void init() {
      addMenuListener(new MenuListener() {
        public void menuCanceled(MenuEvent e) {
        }

        public void menuDeselected(MenuEvent e) {
        }

        public void menuSelected(MenuEvent e) {
          removeAll();
          // find out the available types of LRs and repopulate the menu
          CreoleRegister reg = Gate.getCreoleRegister();
          List resTypes;
          switch(type){
            case LR:
              resTypes = reg.getPublicLrTypes();
              break;
            case PR:
              resTypes = reg.getPublicPrTypes();
              break;
            case APP:
              resTypes = reg.getPublicControllerTypes();
              break;
            default:
              throw new GateRuntimeException("Unknown LiveMenu type: " + type);
          }

          if(resTypes != null && !resTypes.isEmpty()) {
            HashMap resourcesByName = new HashMap();
            Iterator resIter = resTypes.iterator();
            while(resIter.hasNext()) {
              ResourceData rData = (ResourceData)reg.get(resIter.next());
              resourcesByName.put(rData.getName(), rData);
            }
            List resNames = new ArrayList(resourcesByName.keySet());
            Collections.sort(resNames);
            resIter = resNames.iterator();
            while(resIter.hasNext()) {
              ResourceData rData =
                (ResourceData)resourcesByName.get(resIter.next());
              add(new XJMenuItem(new NewResourceAction(rData), MainFrame.this));
            }
          }
        }
      });
    }

    protected int type;

    /**
     * Switch for using LR data.
     */
    public static final int LR = 1;

    /**
     * Switch for using PR data.
     */
    public static final int PR = 2;

    /**
     * Switch for using Controller data.
     */
    public static final int APP = 3;
  }

  /**
   * Overrides default JTree behaviour for tooltips.
   */
  class ResourcesTree extends JTree {

    public ResourcesTree() {
      myToolTip = new ResourceToolTip();
    }

    /**
     * Overrides <code>JTree</code>'s <code>getToolTipText</code> method in
     * order to allow custom tips to be used.
     * 
     * @param event
     *          the <code>MouseEvent</code> that initiated the
     *          <code>ToolTip</code> display
     * @return a string containing the tooltip or <code>null</code> if
     *         <code>event</code> is null
     */
    public String getToolTipText(MouseEvent event) {
      String res = super.getToolTipText(event);
      if(event != null) {
        Point p = event.getPoint();
        int selRow = getRowForLocation(p.x, p.y);
        if(selRow != -1) {
          TreePath path = getPathForRow(selRow);
          Object lastPath = path.getLastPathComponent();
          Object value = ((DefaultMutableTreeNode)lastPath).getUserObject();
          myToolTip.setValue(value);
        }
      }
      // if we always return the same text, the tooltip manager thinks
      // the
      // size and location don't need changing.
      return res;
    }

    public JToolTip createToolTip() {
      return myToolTip;
    }

    ResourceToolTip myToolTip;
  }

  /**
   * Implementation of a custom tool tip to be used for showing extended
   * information about CREOLE resources.
   * 
   */
  class ResourceToolTip extends JToolTip {
    public ResourceToolTip() {
      this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      tipComponent = new JPanel();
      tipComponent.setOpaque(false);
      tipComponent.setLayout(new BoxLayout(tipComponent, BoxLayout.X_AXIS));
      tipComponent.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

      iconLabel = new JLabel(getIcon("annie-application"));
      iconLabel.setText(null);
      iconLabel.setOpaque(false);
      tipComponent.add(iconLabel);

      textLabel = new JLabel();
      textLabel.setOpaque(false);
      tipComponent.add(Box.createHorizontalStrut(10));
      tipComponent.add(textLabel);

      add(tipComponent);
    }

    /**
     * Label used for the icon
     */
    JLabel iconLabel;

    /**
     * Label used for the text
     */
    JLabel textLabel;

    /**
     * The actual component displaying the tooltip.
     */
    JPanel tipComponent;

    public void setTipText(String tipText) {
      // textLabel.setText(tipText);
    }

    /**
     * Sets the value to be displayed
     * 
     * @param value
     */
    public void setValue(Object value) {
      if(value != null) {
        if(value instanceof String) {
          textLabel.setText((String)value);
          iconLabel.setIcon(null);
        }
        else if(value instanceof NameBearerHandle) {
          NameBearerHandle handle = (NameBearerHandle)value;
          textLabel.setText(handle.getTooltipText());
          iconLabel.setIcon(handle.getIcon());
        }
        else {
          textLabel.setText(null);
          iconLabel.setIcon(null);
        }
      }
    }

    public Dimension getPreferredSize() {
      Dimension d = tipComponent.getPreferredSize();
      Insets ins = getInsets();
      return new Dimension(d.width + ins.left + ins.right, d.height + ins.top
        + ins.bottom);
    }

  }

  class HelpAboutAction extends AbstractAction {
    public HelpAboutAction() {
      super("About");
    }

    public void actionPerformed(ActionEvent e) {
      splash.showSplash();
    }
  }

  class HelpUserGuideAction extends AbstractAction {
    public HelpUserGuideAction() {
      super("User Guide Contents");
      putValue(SHORT_DESCRIPTION, "This option needs an internet connection");
      putValue(ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent e) {
      showHelpFrame("http://www.gate.ac.uk/sale/tao/splitli1.html");
    }
  }

  private void showHelpFrame(String urlString) {
    final URL url;
    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      e.printStackTrace();
      return;
    }
    Runnable runnable = new Runnable() {
      public void run() {
        if (helpFrame == null) {
          helpFrame = new HelpFrame();
          helpFrame.setSize(800, 600);
          helpFrame.setDefaultCloseOperation(HIDE_ON_CLOSE);
          // center on screen
          Dimension frameSize = helpFrame.getSize();
          Dimension ownerSize = Toolkit.getDefaultToolkit().getScreenSize();
          Point ownerLocation = new Point(0, 0);
          helpFrame.setLocation(ownerLocation.x
             + (ownerSize.width - frameSize.width) / 2, ownerLocation.y
             + (ownerSize.height - frameSize.height) / 2);
        }
        try {
          helpFrame.setPage(url);
        } catch (IOException e) {
          e.printStackTrace();
          return;
        }
        helpFrame.setVisible(false);
        helpFrame.setVisible(true);
      }
    };
    Thread thread = new Thread(runnable);
    thread.start();
  }

  class HelpUserGuideInContextAction extends AbstractAction {
    public HelpUserGuideInContextAction() {
      super("Contextual User Guide");
      putValue(SHORT_DESCRIPTION, "This option needs an internet connection");
      putValue(ACCELERATOR_KEY,
        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
    }

    public void actionPerformed(ActionEvent e) {
      String tabToolTip = mainTabbedPane.getToolTipTextAt(
        mainTabbedPane.getSelectedIndex());
      showHelpFrame(getHelpUrlStringForRessourceClassOrComment(tabToolTip));
    }
  }

  private String getHelpUrlStringForRessourceClassOrComment(String classOrComment) {
    String url = "http://gate.ac.uk/sale/tao/split";
    if (classOrComment.contains("gate.creole.SerialAnalyserController")
     || classOrComment.contains("gate.creole.SerialController")) {
      url += "ch3.html#sec:howto:apps";
    } else if (classOrComment.contains("gate.creole.ConditionalSerialAnalyserController")
            || classOrComment.contains("gate.creole.ConditionalSerialController")) {
      url += "ch3.html#sec:howto:cond";
    } else if (classOrComment.contains("gate.creole.RealtimeCorpusController")) {
      url += "ch4.html#sec:applications";
    } else if (classOrComment.contains("gate.corpora.DocumentImpl")) {
      url += "ch3.html#sec:howto:edit";
    } else if (classOrComment.contains("gate.corpora.CorpusImpl")) {
      url += "ch3.html#sec:howto:loadlr";
    } else if (classOrComment.contains("gate.creole.AnnotationSchema")) {
      url += "ch6.html#sec:schemas";
    } else if (classOrComment.contains("gate.creole.ontology.owlim.OWLIMOntologyLR")) {
      url += "ch10.html#sec:ontologies:lr";
    } else if (classOrComment.contains("gate.creole.orthomatcher.OrthoMatcher")) {
      url += "ch8.html#sec:annie:orthomatcher";
    } else if (classOrComment.contains("gate.creole.ANNIETransducer")) {
      url += "ch7.html#chap:jape";
    } else if (classOrComment.contains("gate.creole.POSTagger")) {
      url += "ch8.html#sec:tagger";
    } else if (classOrComment.contains("gate.creole.splitter.SentenceSplitter")) {
      url += "ch8.html#sec:splitter";
    } else if (classOrComment.contains("gate.creole.tokeniser.DefaultTokeniser")) {
      url += "ch8.html#sec:tokeniser";
    } else if (classOrComment.contains("gate.creole.annotdelete.AnnotationDeletePR")) {
      url += "ch9.html#sec:misc-creole:reset";
    } else if (classOrComment.contains("gate.creole.gazetteer.DefaultGazetteer")) {
      url += "ch8.html#sec:gazetteer";
    } else if (classOrComment.contains("gate.creole.splitter.RegexSentenceSplitter")) {
      url += "ch8.html#sec:regex-splitter";
    } else if (classOrComment.contains("gate.creole.gazetteer.OntoGazetteerImpl")) {
      url += "ch5.html#sect:ontogaz";
    } else if (classOrComment.contains("com.ontotext.gate.gazetteer.HashGazetteer")) {
      url += "ch5.html#sect:gaze";
    } else if (classOrComment.contains("gate.creole.Transducer")) {
      url += "ch7.html#chap:jape";
    } else if (classOrComment.contains("gate.creole.annotransfer.AnnotationSetTransfer")) {
      url += "ch9.html#sec:misc-creole:ast";
    } else if (classOrComment.contains("gate.creole.tokeniser.SimpleTokeniser")) {
      url += "ch8.html#sec:tokeniser";
    } else if (classOrComment.contains("gate.compound.impl")) {
      url += "ch12.html#chapt:alignment";
    } else if (classOrComment.contains("gate.merger.AnnotationMergingMain")) {
      url += "ch9.html#sec:misc-creole:merging";
    } else if (classOrComment.contains("gate.creole.coref.Coreferencer")) {
      url += "ch8.html#sec:annie:pronom-coref";
    } else if (classOrComment.contains("gate.creole.coref.NominalCoref")) {
      url += "ch8.html#sec:annie:pronom-coref";
    } else if (classOrComment.contains("gate.creole.GazetteerListsCollector")) {
      url += "ch9.html#sec:misc-creole:listscollector";
    } else if (classOrComment.contains("gate.creole.morph.Morph")) {
      url += "ch9.html#sec:misc-creole:morpher";
    } else if (classOrComment.contains("gate.creole.dumpingPR.DumpingPR")) {
      url += "ch9.html#sec:misc-creole:flexexport";
    } else if (classOrComment.contains("gate.creole.VPChunker")) {
      url += "ch9.html#sec:misc-creole:npchunker";
    } else if (classOrComment.contains("gate.creole.ml.MachineLearningPR")) {
      url += "ch11.html#chapt:mlapi";
    } else if (classOrComment.contains("GATE serial datastore")
     || classOrComment.contains("gate.persist.LuceneDataStoreImpl")) {
      url += "ch9.html#sec:misc-creole:annic";
    } else if (classOrComment.contains("gate.persist.SerialDataStore")) {
      url += "ch3.html#sec:howto:datastores";
    } else if (classOrComment.contains("GATE log")) {
      url += "ch3.html#sec:howto:guistart";
    } else {
      url += "li1.html";
    }
    return url;
  }
  
  class HelpOnItemTreeAction extends AbstractAction {
    HelpOnItemTreeAction(String className) {
      super("Help");
      putValue(SHORT_DESCRIPTION, "Help on this resource");
      this.className = className;
    }

    public void actionPerformed(ActionEvent e) {
      showHelpFrame(getHelpUrlStringForRessourceClassOrComment(className));
    }

    String className;
  }

  class ToggleToolTipsAction extends AbstractAction {
    public ToggleToolTipsAction() {
      super("Show tooltips");
      putValue(SHORT_DESCRIPTION,
        "Show or hide the help balloon under the cursor.");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          javax.swing.ToolTipManager toolTipManager;
          toolTipManager = ToolTipManager.sharedInstance();
          if (toggleToolTipsCheckBoxMenuItem.isSelected()) {
            toolTipManager.setEnabled(true);
          } else {
            toolTipManager.setEnabled(false);
          }
        }
      };
      Thread thread = new Thread(runnable);
      thread.start();
    }
  }


    protected class ResourcesTreeCellRenderer extends DefaultTreeCellRenderer {
    public ResourcesTreeCellRenderer() {
      setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
        hasFocus);
      if(value == resourcesTreeRoot) {
        setIcon(MainFrame.getIcon("root"));
        setToolTipText("GATE");
      }
      else if(value == applicationsRoot) {
        setIcon(MainFrame.getIcon("applications"));
        setToolTipText("GATE applications");
      }
      else if(value == languageResourcesRoot) {
        setIcon(MainFrame.getIcon("lrs"));
        setToolTipText("Language Resources");
      }
      else if(value == processingResourcesRoot) {
        setIcon(MainFrame.getIcon("prs"));
        setToolTipText("Processing Resources");
      }
      else if(value == datastoresRoot) {
        setIcon(MainFrame.getIcon("datastores"));
        setToolTipText("GATE Datastores");
      }
      else {
        // not one of the default root nodes
        value = ((DefaultMutableTreeNode)value).getUserObject();
        if(value instanceof Handle) {
          setIcon(((Handle)value).getIcon());
          setText(((Handle)value).getTitle());
          setToolTipText(((Handle)value).getTooltipText());
        }
      }
      return this;
    }

    public Component getTreeCellRendererComponent1(JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
      super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf,
        row, hasFocus);
      Object handle = ((DefaultMutableTreeNode)value).getUserObject();
      if(handle != null && handle instanceof Handle) {
        setIcon(((Handle)handle).getIcon());
        setText(((Handle)handle).getTitle());
        setToolTipText(((Handle)handle).getTooltipText());
      }
      return this;
    }

  }

  protected class ResourcesTreeCellEditor extends DefaultTreeCellEditor {
    ResourcesTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
      super(tree, renderer);
    }

    /**
     * This is the original implementation from the super class with some
     * changes (i.e. shorter timer: 500 ms instead of 1200)
     */
    protected void startEditingTimer() {
      if(timer == null) {
        timer = new javax.swing.Timer(500, this);
        timer.setRepeats(false);
      }
      timer.start();
    }

    /**
     * This is the original implementation from the super class with some
     * changes (i.e. correct discovery of icon)
     */
    public Component getTreeCellEditorComponent(JTree tree, Object value,
      boolean isSelected, boolean expanded, boolean leaf, int row) {
      Component retValue =
        super.getTreeCellEditorComponent(tree, value, isSelected, expanded,
          leaf, row);
      // lets find the actual icon
      if(renderer != null) {
        renderer.getTreeCellRendererComponent(tree, value, isSelected,
          expanded, leaf, row, false);
        editingIcon = renderer.getIcon();
      }
      return retValue;
    }
  }// protected class ResourcesTreeCellEditor extends
  // DefaultTreeCellEditor {

  protected class ResourcesTreeModel extends DefaultTreeModel {
    ResourcesTreeModel(TreeNode root, boolean asksAllowChildren) {
      super(root, asksAllowChildren);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {
      DefaultMutableTreeNode aNode =
        (DefaultMutableTreeNode)path.getLastPathComponent();
      Object userObject = aNode.getUserObject();
      if(userObject instanceof Handle) {
        Object target = ((Handle)userObject).getTarget();
        if(target instanceof Resource) {
          Gate.getCreoleRegister().setResourceName((Resource)target,
            (String)newValue);
        }
      }
      nodeChanged(aNode);
    }
  }

  /**
   * Extends {@link JFileChooser} to make sure the shared {@link MainFrame}
   * instance is used as a parent.
   */
  public static class GateFileChooser extends JFileChooser {
    /**
     * Overridden to make sure the shared MainFrame instance is used as a parent
     * when no parent is specified
     */
    public int showDialog(Component parent, String approveButtonText)
      throws HeadlessException {
      if (this.getDialogType() != JFileChooser.SAVE_DIALOG) {
        setSelectedFileInFileChooser();
      }
      if(parent == null) {
        return super.showDialog(getInstance(), approveButtonText);
      }
      else {
        return super.showDialog(parent, approveButtonText);
      }
    }

    /**
     * If possible, set the last directory/file used
     * by the resource as the current directory/file.
     */
    private void setSelectedFileInFileChooser() {
      if (currentResourceClassName != null) {
      String resourcePath = currentResourceClassName.replaceAll("\\.", "/");
      String lastUsedPath = null;
      try {
        if(prefs.nodeExists("filechooserlocations/" + resourcePath)) {
          Preferences node =
            prefs.node("filechooserlocations/" + resourcePath);
          lastUsedPath = node.get("location", null);
        }
      } catch (BackingStoreException e) {
        e.printStackTrace();
      }
      if (lastUsedPath != null && lastUsedPath.length() > 0) {
        File file = new File(lastUsedPath);
        if (file.exists()) {
            fileChooser.setSelectedFile(file);
            fileChooser.ensureFileIsVisible(file);
        }
      }
      }
    }

    @Override
    public void approveSelection() {
      // Save the location of the file chooser for the current resource.
      if (currentResourceClassName == null) { return; }
      String resourcePath =
        currentResourceClassName.replaceAll("\\.", "/");
      Preferences node = null;
      node = prefs.node("filechooserlocations/" + resourcePath);
      try {
        node.put("location", fileChooser.getSelectedFile().getCanonicalPath());
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
      try {
        prefs.flush();
      } catch (BackingStoreException be) {
        be.printStackTrace();
        return;
      }
      super.approveSelection();
    }

  }

  class ProgressBarUpdater implements Runnable {
    ProgressBarUpdater(int newValue) {
      value = newValue;
    }

    public void run() {
      if(value == 0)
        progressBar.setVisible(false);
      else progressBar.setVisible(true);
      progressBar.setValue(value);
    }

    int value;
  }

  class StatusBarUpdater implements Runnable {
    StatusBarUpdater(String text) {
      this.text = text;
    }

    public void run() {
      statusBar.setText(text);
    }

    String text;
  }

  /**
   * During longer operations it is nice to keep the user entertained so (s)he
   * doesn't fall asleep looking at a progress bar that seems have stopped. Also
   * there are some operations that do not support progress reporting so the
   * progress bar would not work at all so we need a way to let the user know
   * that things are happening. We chose for purpose to show the user a small
   * cartoon in the form of an animated gif. This class handles the diplaying
   * and updating of those cartoons.
   */
  class CartoonMinder implements Runnable {

    CartoonMinder(JPanel targetPanel) {
      active = false;
      dying = false;
      this.targetPanel = targetPanel;
      imageLabel = new JLabel(getIcon("working"));
      imageLabel.setOpaque(false);
      imageLabel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
    }

    public boolean isActive() {
      boolean res;
      synchronized(lock) {
        res = active;
      }
      return res;
    }

    public void activate() {
      // add the label in the panel
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          targetPanel.add(imageLabel);
        }
      });
      // wake the dorment thread
      synchronized(lock) {
        active = true;
      }
    }

    public void deactivate() {
      // send the thread to sleep
      synchronized(lock) {
        active = false;
      }
      // clear the panel
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          targetPanel.removeAll();
          targetPanel.repaint();
        }
      });
    }

    public void dispose() {
      synchronized(lock) {
        dying = true;
      }
    }

    public void run() {
      boolean isDying;
      synchronized(lock) {
        isDying = dying;
      }
      while(!isDying) {
        boolean isActive;
        synchronized(lock) {
          isActive = active;
        }
        if(isActive && targetPanel.isVisible()) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              // targetPanel.getParent().validate();
              // targetPanel.getParent().repaint();
              // ((JComponent)targetPanel.getParent()).paintImmediately(((JComponent)targetPanel.getParent()).getBounds());
              // targetPanel.doLayout();

              // targetPanel.requestFocus();
              targetPanel.getParent().getParent().invalidate();
              targetPanel.getParent().getParent().repaint();
              // targetPanel.paintImmediately(targetPanel.getBounds());
            }
          });
        }
        // sleep
        try {
          Thread.sleep(300);
        }
        catch(InterruptedException ie) {
        }

        synchronized(lock) {
          isDying = dying;
        }
      }// while(!isDying)
    }

    boolean dying;

    boolean active;

    String lock = "lock";

    JPanel targetPanel;

    JLabel imageLabel;
  }

  /*
   * class JGateMenuItem extends JMenuItem { JGateMenuItem(javax.swing.Action
   * a){ super(a); this.addMouseListener(new MouseAdapter() { public void
   * mouseEntered(MouseEvent e) { oldText = statusBar.getText();
   * statusChanged((String)getAction().
   * getValue(javax.swing.Action.SHORT_DESCRIPTION)); }
   * 
   * public void mouseExited(MouseEvent e) { statusChanged(oldText); } }); }
   * String oldText; }
   * 
   * class JGateButton extends JButton { JGateButton(javax.swing.Action a){
   * super(a); this.addMouseListener(new MouseAdapter() { public void
   * mouseEntered(MouseEvent e) { oldText = statusBar.getText();
   * statusChanged((String)getAction().
   * getValue(javax.swing.Action.SHORT_DESCRIPTION)); }
   * 
   * public void mouseExited(MouseEvent e) { statusChanged(oldText); } }); }
   * String oldText; }
   */
  class LocaleSelectorMenuItem extends JRadioButtonMenuItem {
    public LocaleSelectorMenuItem(Locale locale) {
      super(locale.getDisplayName());
      me = this;
      myLocale = locale;
      this.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Iterator rootIter = MainFrame.getGuiRoots().iterator();
          while(rootIter.hasNext()) {
            Object aRoot = rootIter.next();
            if(aRoot instanceof Window) {
              me.setSelected(((Window)aRoot).getInputContext()
                .selectInputMethod(myLocale));
            }
          }
        }
      });
    }

    public LocaleSelectorMenuItem() {
      super("System default  >>" + Locale.getDefault().getDisplayName() + "<<");
      me = this;
      myLocale = Locale.getDefault();
      this.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Iterator rootIter = MainFrame.getGuiRoots().iterator();
          while(rootIter.hasNext()) {
            Object aRoot = rootIter.next();
            if(aRoot instanceof Window) {
              me.setSelected(((Window)aRoot).getInputContext()
                .selectInputMethod(myLocale));
            }
          }
        }
      });
    }

    Locale myLocale;

    JRadioButtonMenuItem me;
  }// //class LocaleSelectorMenuItem extends JRadioButtonMenuItem

  /**
   * This class represent an action which brings up the Gazetteer Editor tool
   */
  class NewGazetteerEditorAction extends AbstractAction {
    public NewGazetteerEditorAction() {
      super("Gazetteer Editor", getIcon("gazetteer"));
      putValue(SHORT_DESCRIPTION, "Start the Gazetteer Editor");
    }

    public void actionPerformed(ActionEvent e) {
      com.ontotext.gate.vr.Gaze editor = new com.ontotext.gate.vr.Gaze();
      try {
        JFrame frame = new JFrame();
        editor.init();
        frame.setTitle("Gazetteer Editor");
        frame.getContentPane().add(editor);

        Set gazetteers =
          new HashSet(Gate.getCreoleRegister().getLrInstances(
            "gate.creole.gazetteer.DefaultGazetteer"));
        if(gazetteers == null || gazetteers.isEmpty()) return;
        Iterator iter = gazetteers.iterator();
        while(iter.hasNext()) {
          gate.creole.gazetteer.Gazetteer gaz =
            (gate.creole.gazetteer.Gazetteer)iter.next();
          if(gaz.getListsURL().toString().endsWith(
            System.getProperty("gate.slug.gazetteer"))) editor.setTarget(gaz);
        }

        frame.setSize(Gaze.SIZE_X, Gaze.SIZE_Y);
        frame.setLocation(Gaze.POSITION_X, Gaze.POSITION_Y);
        frame.setVisible(true);
        editor.setVisible(true);
      }
      catch(ResourceInstantiationException ex) {
        ex.printStackTrace(Err.getPrintWriter());
      }
    }// actionPerformed();
  }// class NewOntologyEditorAction

} // class MainFrame
