/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Angel Kirilov 26/03/2002
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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.GraphicsEnvironment;

import java.text.*;

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
 * The main Shell SLAC Gate GUI frame.
 */
public class ShellSlacFrame extends MainFrame {

  /** Shell GUI application */
  private SerialAnalyserController application = null;

  /** Shell GUI corpus */
  private Corpus corpus = null;
  
  /** Shell GUI documents DataStore */
  private DataStore dataStore = null;
  
  /** Keep this action for enable/disable the menu item */
  private Action saveAction = null;
  /** Keep this action for enable/disable the menu item */
  private Action runAction = null;
  
  /** New frame */
  public ShellSlacFrame() {
    super(true);
//    guiRoots.clear();
//    guiRoots.add(this);
    
    initShellSlacLocalData();
    initShellSlacGuiComponents();
  } // ShellSlacFrame

  protected void initShellSlacLocalData(){
    createCorpus();
//    createDefaultApplication();
    String applicationURL = 
      System.getProperty(GateConstants.APPLICATION_JAVA_PROPERTY_NAME);
    if(applicationURL != null) {
      createDefaultApplication(applicationURL);
    } 
    else {
      // create default ANNIE
      createDefaultApplication();
    } // if
    
    dataStore = null;
  } // initLocalData

  protected void initShellSlacGuiComponents() {
    super.setJMenuBar(createMenuBar());
  } // initShellSlacGuiComponents()

  /** Create the new Shell SLAC menu */
  private JMenuBar createMenuBar() {
    //MENUS
    JMenuBar retMenuBar = new JMenuBar();

    JMenu fileMenu = new JMenu("File");
    Action action;

    ResourceData rDataDocument = getDocumentResourceData();
    if(rDataDocument != null) {
      action = new NewResourceAction(rDataDocument);
      action.putValue(action.NAME, "New Document");
      action.putValue(action.SHORT_DESCRIPTION,"Create a new document");
    
      fileMenu.add(new XJMenuItem(action, this));

      fileMenu.add(new XJMenuItem(new CloseSelectedDocumentAction(), this));
    } // if
    
    action = new StoreAllDocumentAction();
    action.setEnabled(false);
    saveAction = action;
    fileMenu.add(new XJMenuItem(action, this));
    action = new StoreAllDocumentAsAction();
    fileMenu.add(new XJMenuItem(action, this));
    action = new LoadAllDocumentAction();
    fileMenu.add(new XJMenuItem(action, this));

/*
    fileMenu.addSeparator();
    
    action = new RunApplicationAction();
    if(application == null) {
      action.setEnabled(false);
    } // if
    runAction = action;
    fileMenu.add(new XJMenuItem(action, this));
    
    action = new LoadResourceFromFileAction();
    action.putValue(action.NAME, "Load application");
    fileMenu.add(new XJMenuItem(action, this));
    
    action = new RestoreDefaultApplicationAction();
    fileMenu.add(new XJMenuItem(action, this));

    action = new TestStoreAction();
    fileMenu.add(new XJMenuItem(action, this));
*/

    fileMenu.addSeparator();
    action = new ExitGateAction();
    action.putValue(action.NAME, "Exit");
    fileMenu.add(new XJMenuItem(action, this));
    retMenuBar.add(fileMenu);

    JMenu toolsMenu = new JMenu("Tools");

    action = new RunApplicationAction();
    if(application == null) {
      action.setEnabled(false);
    } // if
    runAction = action;
    toolsMenu.add(new XJMenuItem(action, this));
    toolsMenu.addSeparator();
    
    createToolsMenuItems(toolsMenu);
    retMenuBar.add(toolsMenu);

    JMenu helpMenu = new JMenu("Help");
    helpMenu.add(new HelpAboutSlugAction());
    retMenuBar.add(helpMenu);

    return retMenuBar;
  } // createMenuBar()

  /** Should check for registered Creole components and populate menu.
   *  <BR> In first version is hardcoded. */  
  private void createToolsMenuItems(JMenu toolsMenu) {
    toolsMenu.add(new NewAnnotDiffAction());
    toolsMenu.add(
      new AbstractAction("Unicode editor", getIcon("unicode.gif")){
      public void actionPerformed(ActionEvent evt){
        new guk.Editor();
      }
    });

    /*add the ontology editor to the tools menu ontotext.bp */
    toolsMenu.add(newOntologyEditorAction);
  } // createToolsMenuItems()
  
  /** Find ResourceData for "Create Document" menu item. */
  private ResourceData getDocumentResourceData() {
    ResourceData result = null;
    
    CreoleRegister reg = Gate.getCreoleRegister();
    List lrTypes = reg.getPublicLrTypes();
    
    if(lrTypes != null && !lrTypes.isEmpty()){
      Iterator lrIter = lrTypes.iterator();
      while(lrIter.hasNext()){
        ResourceData rData = (ResourceData)reg.get(lrIter.next());
        if("gate.corpora.DocumentImpl".equalsIgnoreCase(rData.getClassName())) {
          result = rData;
          break;
        } // if    
      } // while
    } // if
    
    return result;
  } // getDocumentResourceData()
  
  /** Here default ANNIE is created. Could be changed. */
  private void createDefaultApplication() {
    // Loads ANNIE with defaults

//    SwingUtilities.invokeLater(new Runnable() {
//      public void run(){
        Runnable loadAction = new ANNIERunnable(ShellSlacFrame.this);
        Thread thread = new Thread(loadAction, "");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
//      }
//    });
  } // createDefaultApplication

  /** Load serialized application from file. */
  private void createDefaultApplication(String url) {
    ApplicationLoadRun run = new ApplicationLoadRun(url);
    SwingUtilities.invokeLater(run);
  } // createDefaultApplication

    
  public class ApplicationLoadRun implements Runnable {
    private String appURL;
    public ApplicationLoadRun(String url) {
      appURL = url;
    }
    
    public void run(){
      File file = new File(appURL);  
      
      if( file.exists() ) 
        try {
          gate.util.persistence.PersistenceManager.loadObjectFromFile(file);
        } catch (PersistenceException pex) {
          pex.printStackTrace();
        } catch (ResourceInstantiationException riex) {
          riex.printStackTrace();
        } catch (IOException ioex) {
          ioex.printStackTrace();
        } // catch
    } // run
  } // class ApplicationLoadRun implements Runnable 

  public class ANNIERunnable implements Runnable {
    MainFrame parentFrame;
    ANNIERunnable(MainFrame parent) {
      parentFrame = parent;
    }
    
    public void run(){
      AbstractAction action = new LoadANNIEWithDefaultsAction();
      action.actionPerformed(new ActionEvent(parentFrame, 1, "Load ANNIE"));
    }
  } // ANNIERunnable
  
  /** Create corpus for application */
  private void createCorpus() {
    try {
      Factory.newCorpus("Shell SLAC Corpus");
    } catch (ResourceInstantiationException ex) {
      ex.printStackTrace();
      throw new GateRuntimeException("Error in creating build in corpus.");
    } // catch
  } // createCorpus()
  
  /** Override base class method */
  public void resourceLoaded(CreoleEvent e) {
    super.resourceLoaded(e);

    Resource res = e.getResource();

    if(res instanceof SerialAnalyserController) {
      if(application != null) {
        // remove old application
        Factory.deleteResource(application);
      } // if
      application = (SerialAnalyserController) res;
      
      runAction.setEnabled(true);
      if(corpus != null) 
        application.setCorpus(corpus);
    } // if
    
    if(res instanceof Corpus) {
      corpus = (Corpus) res;
      if(application != null)
        application.setCorpus(corpus);
    } // if

    if(res instanceof Document) {
      Document doc = (Document) res;
      corpus.add(doc);
      showDocument(doc);
    } // if
  }// resourceLoaded();
  
  protected void showDocument(Document doc) {
    // should find NameBearerHandle for document and call 
    Handle handle = null;
    Enumeration nodesEnum = resourcesTreeRoot.preorderEnumeration();
    boolean done = false;
    DefaultMutableTreeNode node = resourcesTreeRoot;
    Object obj;
    
    while(!done && nodesEnum.hasMoreElements()){
      node = (DefaultMutableTreeNode)nodesEnum.nextElement();
      obj = node.getUserObject();
      if(obj instanceof Handle) {
        handle = (Handle)obj;
        obj = handle.getTarget();
        done = obj instanceof Document
          && doc == (Document)obj;
      } // if
    } // while
    
    if(done){
      select(handle);
    } // if
  } // showDocument(Document doc)

  /** Called when a {@link gate.DataStore} has been opened.
   *  Save corpus on datastore open. */
  public void datastoreOpened(CreoleEvent e){
    super.datastoreOpened(e);
    if(corpus == null) return; 

    DataStore ds = e.getDatastore();
    try {
      if(dataStore != null) {
        // close old datastore if any
        dataStore.close();
      } // if
      // put documents in datastore
      saveAction.setEnabled(false);

      LanguageResource persCorpus = ds.adopt(corpus, null);
      ds.sync(persCorpus);
      // change corpus with the new persistent corpus
      Factory.deleteResource((LanguageResource)corpus);
      corpus = (Corpus) persCorpus;
      if(application != null) application.setCorpus(corpus);

      dataStore = ds;
      saveAction.setEnabled(true);
    } catch (PersistenceException pex) {
      pex.printStackTrace();
    } catch (gate.security.SecurityException sex) {
      sex.printStackTrace();
    } // catch
  } // datastoreOpened(CreoleEvent e)

//------------------------------------------------------------------------------
//  Inner classes section
  
  /** Run the current application SLAC */
  class RunApplicationAction extends AbstractAction {
    public RunApplicationAction() {
      super("Process Documents", getIcon("menu_controller.gif"));
      putValue(SHORT_DESCRIPTION, "Run the application to process documents");
    } // RunApplicationAction()

    public void actionPerformed(ActionEvent e) {
      if (application != null) {
        SerialControllerEditor editor = new SerialControllerEditor();
        editor.setTarget(application);
        editor.runAction.actionPerformed(null);
      } // if
    } // actionPerformed(ActionEvent e)
  } // class RunApplicationAction extends AbstractAction

  class RestoreDefaultApplicationAction extends AbstractAction {
    public RestoreDefaultApplicationAction() {
      super("Create ANNIE application");
      putValue(SHORT_DESCRIPTION, "Create default ANNIE application");
    } // RestoreDefaultApplicationAction()

    public void actionPerformed(ActionEvent e) {
      createDefaultApplication();
    } // actionPerformed(ActionEvent e)
  } // class RestoreDefaultApplicationAction extends AbstractAction

  class CloseSelectedDocumentAction extends AbstractAction {
    public CloseSelectedDocumentAction() {
      super("Close Document");
      putValue(SHORT_DESCRIPTION, "Closes the selected document");
    } // CloseSelectedDocumentAction()

    public void actionPerformed(ActionEvent e) {
      JComponent resource = (JComponent)
                                  mainTabbedPane.getSelectedComponent();
      if (resource != null){
        Action act = resource.getActionMap().get("Close resource");
        if (act != null)
          act.actionPerformed(null);
      }// End if
    } // actionPerformed(ActionEvent e)
  } // class CloseSelectedDocumentAction extends AbstractAction

  class StoreAllDocumentAsAction extends AbstractAction {
    public StoreAllDocumentAsAction() {
      super("Store all Documents As...");
      putValue(SHORT_DESCRIPTION,
        "Store all opened in the application documents in new directory");
    } // StoreAllDocumentAsAction()

    public void actionPerformed(ActionEvent e) {
      createSerialDataStore();
    } // actionPerformed(ActionEvent e)
  } // class StoreAllDocumentAction extends AbstractAction

  class StoreAllDocumentAction extends AbstractAction {
    public StoreAllDocumentAction() {
      super("Store all Documents");
      putValue(SHORT_DESCRIPTION,"Store all opened in the application documents");
    } // StoreAllDocumentAction()

    public void actionPerformed(ActionEvent e) {
      if(dataStore != null) {
        try {
          dataStore.sync(corpus);
        } catch (PersistenceException pex) {
          pex.printStackTrace();
        } catch (gate.security.SecurityException sex) {
          sex.printStackTrace();
        } // catch
      } // if
    } // actionPerformed(ActionEvent e)
  } // class StoreAllDocumentAction extends AbstractAction

  class LoadAllDocumentAction extends AbstractAction {
    public LoadAllDocumentAction() {
      super("Load all Documents");
      putValue(SHORT_DESCRIPTION,"Load documents from storage");
    } // StoreAllDocumentAction()

    public void actionPerformed(ActionEvent e) {
      if(dataStore != null) {
        // on close all resources will be closed too
        try {
          dataStore.close();
        } catch (PersistenceException pex) {
          pex.printStackTrace();
        } // catch
        dataStore = null;
      } // if

      // should open a datastore
      dataStore = openSerialDataStore();
      
      if(dataStore != null) {
        // load from datastore
        List corporaIDList = null;
        List docIDList = null;
        String docID = "";
        FeatureMap features;
        Document doc;

        try {
          corporaIDList = dataStore.getLrIds("gate.corpora.CorpusImpl");
          docIDList = dataStore.getLrIds("gate.corpora.DocumentImpl");
        } catch (PersistenceException pex) {
          pex.printStackTrace();
        } // catch
        
        features = Factory.newFeatureMap();
        features.put(DataStore.LR_ID_FEATURE_NAME, docID);
        features.put(DataStore.DATASTORE_FEATURE_NAME, dataStore);

        for(int i=0; i < docIDList.size(); ++i) {
          docID = (String) docIDList.get(i);
          // read the document back
          features.put(DataStore.LR_ID_FEATURE_NAME, docID);
          doc = null;
          try {
            doc = (Document) 
              Factory.createResource("gate.corpora.DocumentImpl", features);
          } catch (gate.creole.ResourceInstantiationException rex) {
            rex.printStackTrace();
          } // catch

          if(doc != null) corpus.add(doc);
        } // for
      } // if
      
    } // actionPerformed(ActionEvent e)
  } // class LoadAllDocumentAction extends AbstractAction
  
  class TestStoreAction extends AbstractAction {
    public TestStoreAction() {
      super("Test Store application");
      putValue(SHORT_DESCRIPTION,"Store the application");
    } // TestStoreAction()

    public void actionPerformed(ActionEvent e) {
      if(application != null) {
        // load/store test
        try {
          File file = new File("D:/temp/tempapplication.tmp");
          ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
          long startTime = System.currentTimeMillis();
          oos.writeObject(application);
          long endTime = System.currentTimeMillis();
  
          System.out.println("Storing completed in " +
            NumberFormat.getInstance().format(
            (double)(endTime - startTime) / 1000) + " seconds");
  
          ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
          Object object;
          startTime = System.currentTimeMillis();
          object = ois.readObject();
          endTime = System.currentTimeMillis();
          application = (SerialAnalyserController) object;
  
          System.out.println("Loading completed in " +
            NumberFormat.getInstance().format(
            (double)(endTime - startTime) / 1000) + " seconds");
  
        } catch (Exception ex) {
          ex.printStackTrace();
        } // catch
      } // if
    } // actionPerformed(ActionEvent e)
  } // class TestStoreAction extends AbstractAction

  class HelpAboutSlugAction extends AbstractAction {
    public HelpAboutSlugAction() {
      super("About");
    } // HelpAboutSlugAction()

    public void actionPerformed(ActionEvent e) {
      JOptionPane.showMessageDialog(ShellSlacFrame.this, 
          "Slug application",
          "Slug application about", 
          JOptionPane.INFORMATION_MESSAGE);
    } // actionPerformed(ActionEvent e)
  } // class HelpAboutSlugAction extends AbstractAction

} // class ShellSlacFrame
