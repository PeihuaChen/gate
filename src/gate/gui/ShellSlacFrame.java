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
import java.awt.Container;
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

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Shell GUI application */
  private CorpusController application = null;

  /** Shell GUI corpus */
  private Corpus corpus = null;
  private Corpus oneDocCorpus = null;

  /** Shell GUI documents DataStore */
  private DataStore dataStore = null;

  /** Keep this action for enable/disable the menu item */
  private Action saveAction = null;
  /** Keep this action for enable/disable the menu item */
  private Action runOneAction = null;
  private Action runAction = null;

  /** Default corpus resource name */
  public static final String DEFAULT_SLUG_CORPUS_NAME = "SLUG Corpus";
  public static final String ONE_DOC_SLUG_CORPUS_NAME = "SLUG One Doc Corpus";

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

    } // if
    
    // Open All... action - open multiple files from directory
    corpusFiller = new CorpusFillerComponent();
    action = new PopulateCorpusAction();
    action.putValue(action.NAME, "New Documents...");
    action.putValue(action.SHORT_DESCRIPTION,"Create multiple documents");
    fileMenu.add(new XJMenuItem(action, this));

    fileMenu.add(new XJMenuItem(new CloseSelectedDocumentAction(), this));
    fileMenu.add(new XJMenuItem(new CloseAllDocumentAction(), this));

    fileMenu.addSeparator();

    action = new ImportDocumentAction();
    fileMenu.add(new XJMenuItem(action, this));

    JMenu exportMenu = new JMenu("Export");
    action = new ExportDocumentAction();
    exportMenu.add(new XJMenuItem(action, this));
    action = new ExportDocumentInlineAction();
    exportMenu.add(new XJMenuItem(action, this));
    fileMenu.add(exportMenu);
    
    JMenu exportAllMenu = new JMenu("Export All");
    action = new ExportAllDocumentAction();
    exportAllMenu.add(new XJMenuItem(action, this));
    action = new ExportAllDocumentInlineAction();
    exportAllMenu.add(new XJMenuItem(action, this));
    fileMenu.add(exportAllMenu);

/*
    action = new StoreAllDocumentAction();
    action.setEnabled(false);
    saveAction = action;
    fileMenu.add(new XJMenuItem(action, this));
    action = new StoreAllDocumentAsAction();
    fileMenu.add(new XJMenuItem(action, this));
    action = new LoadAllDocumentAction();
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

//    action = new ExitGateAction();
    // define exit action without save of session
    action = new AbstractAction () {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
        System.exit(0);
      }
    };
    action.putValue(action.NAME, "Exit");
    fileMenu.add(new XJMenuItem(action, this));
    retMenuBar.add(fileMenu);

    JMenu analyseMenu = new JMenu("Analyse");

    action = new RunApplicationOneDocumentAction();
    if(application == null) {
      action.setEnabled(false);
    } // if
    runOneAction = action;
    analyseMenu.add(new XJMenuItem(action, this));
    retMenuBar.add(analyseMenu);

    action = new RunApplicationAction();
    if(application == null) {
      action.setEnabled(false);
    } // if
    runAction = action;
    analyseMenu.add(new XJMenuItem(action, this));
    retMenuBar.add(analyseMenu);

    JMenu toolsMenu = new JMenu("Tools");
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

    if (System.getProperty("gate.slug.gazetteer") != null)
      toolsMenu.add(newGazetteerEditorAction);

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
    Runnable loadAction = new ANNIERunnable(ShellSlacFrame.this);

    Thread thread = new Thread(loadAction, "");
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  } // createDefaultApplication

  /** Load serialized application from file. */
  private void createDefaultApplication(String url) {
    ApplicationLoadRun run = new ApplicationLoadRun(url, this);
    Thread thread = new Thread(run, "");
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  } // createDefaultApplication

  /** Create corpus for application */
  private void createCorpus() {
    try {
      Factory.newCorpus(DEFAULT_SLUG_CORPUS_NAME);
      Factory.newCorpus(ONE_DOC_SLUG_CORPUS_NAME);
    } catch (ResourceInstantiationException ex) {
      ex.printStackTrace();
      throw new GateRuntimeException("Error in creating build in corpus.");
    } // catch
  } // createCorpus()

  /** Override base class method */
  public void resourceLoaded(CreoleEvent e) {
    super.resourceLoaded(e);

    Resource res = e.getResource();

    if(res instanceof CorpusController) {
      if(application != null) {
        // remove old application
        Factory.deleteResource(application);
      } // if
      application = (CorpusController) res;

      runOneAction.setEnabled(true);
      runAction.setEnabled(true);
      if(corpus != null)
        application.setCorpus(corpus);
    } // if

    if(res instanceof Corpus) {
      Corpus resCorpus = (Corpus) res;

      if(DEFAULT_SLUG_CORPUS_NAME.equals(resCorpus.getName())) {
        corpus = resCorpus;
        if(application != null)
          application.setCorpus(corpus);
      } // if

      if(ONE_DOC_SLUG_CORPUS_NAME.equals(resCorpus.getName())) {
        oneDocCorpus = resCorpus;
      } // if
    } // if

    if(res instanceof Document) {
      Document doc = (Document) res;
      corpus.add(doc);
      showDocument(doc);
    } // if
  }// resourceLoaded();

  /** Find in resource tree and show the document */
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

  /** Return handle to selected tab resource */
  private Handle getSelectedResource() {
    JComponent largeView = (JComponent)
                                mainTabbedPane.getSelectedComponent();

    Handle result = null;
    Enumeration nodesEnum = resourcesTreeRoot.preorderEnumeration();
    boolean done = false;
    DefaultMutableTreeNode node = resourcesTreeRoot;
    while(!done && nodesEnum.hasMoreElements()){
      node = (DefaultMutableTreeNode)nodesEnum.nextElement();
      done = node.getUserObject() instanceof Handle &&
             ((Handle)node.getUserObject()).getLargeView()
              == largeView;
    }
    if(done)
      result = (Handle)node.getUserObject();

    return result;
  } // getSelectedResource()

  /** Export All store of documents from SLUG corpus */
  private void saveDocuments(File targetDir) {
    if(corpus == null || corpus.size() == 0) return;

    Document doc;
    String target = targetDir.getPath();
    URL fileURL;
    String fileName = null;
    int index;

    MainFrame.lockGUI("Export all documents...");

    target = target+File.separatorChar;
    for(int i=0; i<corpus.size(); ++i) {
      doc = (Document) corpus.get(i);
      fileURL = doc.getSourceUrl();
      if(fileURL != null)
        fileName = fileURL.toString();
        index = fileName.lastIndexOf('/');
        if(index != -1) {
          fileName = fileName.substring(index+1, fileName.length());
        }
      else
        fileName = "content_txt";

      // create full file name
      fileName = target + fileName+".xml";
      try{

        // Prepare to write into the xmlFile using UTF-8 encoding
        OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(new File(fileName)),"UTF-8");

        // Write (test the toXml() method)
        // This Action is added only when a gate.Document is created.
        // So, is for sure that the resource is a gate.Document
        writer.write(doc.toXml());
        writer.flush();
        writer.close();
      } catch (Exception ex){
        ex.printStackTrace(Out.getPrintWriter());
      } finally{
        MainFrame.unlockGUI();
      } // finally
    } // for

    MainFrame.unlockGUI();
  } // saveDocuments(File targetDir)

//------------------------------------------------------------------------------
//  Inner classes section

  /** Run the current application SLAC */
  class RunApplicationAction extends AbstractAction {
    public RunApplicationAction() {
      super("Analyse All", getIcon("menu_controller.gif"));
      putValue(SHORT_DESCRIPTION, "Run the application to process documents");
    } // RunApplicationAction()

    public void actionPerformed(ActionEvent e) {
      if (application != null && corpus != null && corpus.size() > 0) {
        application.setCorpus(corpus);
        SerialControllerEditor editor = new SerialControllerEditor();
        editor.setTarget(application);
        editor.runAction.actionPerformed(null);
      } // if
    } // actionPerformed(ActionEvent e)
  } // class RunApplicationAction extends AbstractAction

  /** Run the current application SLAC on current document */
  class RunApplicationOneDocumentAction extends AbstractAction {
    public RunApplicationOneDocumentAction() {
      super("Analyse", getIcon("menu_controller.gif"));
      putValue(SHORT_DESCRIPTION,
          "Run the application to process current document");
    } // RunApplicationOneDocumentAction()

    public void actionPerformed(ActionEvent e) {
      if (application != null) {
        Handle handle = getSelectedResource();
        if(handle == null) return;
        Object target = handle.getTarget();
        if(target == null) return;

        if(target instanceof Document) {
          Document doc = (Document) target;
          oneDocCorpus.clear();
          oneDocCorpus.add(doc);

          application.setCorpus(oneDocCorpus);

          SerialControllerEditor editor = new SerialControllerEditor();
          editor.setTarget(application);
          editor.runAction.actionPerformed(null);
        } // if - Document
      } // if
    } // actionPerformed(ActionEvent e)
  } // class RunApplicationOneDocumentAction extends AbstractAction

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

  class CloseAllDocumentAction extends AbstractAction {
    public CloseAllDocumentAction() {
      super("Close All");
      putValue(SHORT_DESCRIPTION, "Closes all documents");
    } // CloseAllDocumentAction()

    public void actionPerformed(ActionEvent e) {
      JComponent resource;
      for(int i=mainTabbedPane.getTabCount()-1; i>0; --i) {

        resource = (JComponent) mainTabbedPane.getComponentAt(i);
        if (resource != null){
          Action act = resource.getActionMap().get("Close resource");
          if (act != null)
            act.actionPerformed(null);
        }// End if
      } // for
    } // actionPerformed(ActionEvent e)
  } // class CloseAllDocumentAction extends AbstractAction

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
          application = (CorpusController) object;

          System.out.println("Loading completed in " +
            NumberFormat.getInstance().format(
            (double)(endTime - startTime) / 1000) + " seconds");

        } catch (Exception ex) {
          ex.printStackTrace();
        } // catch
      } // if
    } // actionPerformed(ActionEvent e)
  } // class TestStoreAction extends AbstractAction

  /** Import document action */
  class ImportDocumentAction extends AbstractAction {
    public ImportDocumentAction() {
      super("Import");
      putValue(SHORT_DESCRIPTION, "Open a document in XML format");
    } // ImportDocumentAction()

    public void actionPerformed(ActionEvent e) {
      fileChooser.setDialogTitle("Select file to Import from");
      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

      int res = fileChooser.showOpenDialog(ShellSlacFrame.this);
      if(res == fileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();

        String str = "";
        char chArr[] = new char[1024];

        try {
          FileReader reader = new FileReader(file);
          int readedChars = reader.read(chArr);
          reader.close();
          str = new String(chArr, 0, readedChars);
        } catch (Exception ex) {
          // do nothing - some error, so we shouldn't read file anyway
        } // catch

        boolean isGateXmlDocument = false;
        // Detect whether or not is a GateXmlDocument
        if(str.indexOf("<GateDocument") != -1  ||
           str.indexOf(" GateDocument") != -1)
          isGateXmlDocument = true;

        if(isGateXmlDocument) {
          Runnable run = new ImportRunnable(file);
          Thread thread = new Thread(run, "");
          thread.setPriority(Thread.MIN_PRIORITY);
          thread.start();
        }
        else {
          JOptionPane.showMessageDialog(ShellSlacFrame.this,
              "The import file '"+file.getAbsolutePath()+"'\n"
              +"is not a SLUG document.",
              "Import error",
              JOptionPane.WARNING_MESSAGE);
        } // if
      } // if
    } // actionPerformed(ActionEvent e)
  } // class ImportDocumentAction extends AbstractAction

  /** Object to run ExportAll in a new Thread */
  private class ImportRunnable implements Runnable {
    File file;
    ImportRunnable(File targetFile) {
      file = targetFile;
    } // ImportRunnable(File targetDirectory)

    public void run() {
      if(file != null) {
        MainFrame.lockGUI("Import file...");
        try {
          Factory.newDocument(file.toURL());
        } catch (MalformedURLException mex) {
          mex.printStackTrace();
        } catch (ResourceInstantiationException rex) {
          rex.printStackTrace();
        } finally {
          MainFrame.unlockGUI();
        } // finally
      } // if
    } // run()
  } // ImportRunnable

  /** Export current document action */
  class ExportDocumentInlineAction extends AbstractAction {
    public ExportDocumentInlineAction() {
      super("with inline markup");
      putValue(SHORT_DESCRIPTION, "Save the selected document in XML format"
            +" with inline markup");
    } // ExportDocumentInlineAction()

    public void actionPerformed(ActionEvent e) {
      JComponent resource = (JComponent)
                                  mainTabbedPane.getSelectedComponent();
      if (resource == null) return;
      Component c;
      Document doc = null;

      for(int i=0; i<resource.getComponentCount(); ++i) {
        c = resource.getComponent(i);
        if(c instanceof DocumentEditor) {
          doc = ((DocumentEditor) c).getDocument();
        } // if
      } // for

      if(doc != null) {
        JFileChooser fileChooser = MainFrame.getFileChooser();
        File selectedFile = null;

        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Select document to save ...");
        fileChooser.setSelectedFiles(null);

        int res = fileChooser.showDialog(ShellSlacFrame.this, "Save");
        if(res == JFileChooser.APPROVE_OPTION){
          selectedFile = fileChooser.getSelectedFile();
          fileChooser.setCurrentDirectory(fileChooser.getCurrentDirectory());

          // store document with annotations Document.toXML()
          Runnable run = new ExportInline(doc, selectedFile);
          Thread thread = new Thread(run, "");
          thread.setPriority(Thread.MIN_PRIORITY);
          thread.start();
        } // if
      }// End if
    } // actionPerformed(ActionEvent e)
  } // class ExportDocumentInlineAction extends AbstractAction

  /** New thread object for export inline */
  private class ExportInline implements Runnable {
    File targetFile;
    Document document;

    ExportInline(Document doc, File target) {
      targetFile = target;
      document = doc;
    } // ExportAllRunnable(File targetDirectory)

    protected Set getTypes(String types) {
      Set set = new HashSet();
      StringTokenizer tokenizer = new StringTokenizer(types, ";");

      while(tokenizer.hasMoreTokens()) {
        set.add(tokenizer.nextToken());
      } // while
      
      return set;
    }
    
    public void run() {
      if(document == null || targetFile == null) return;
      MainFrame.lockGUI("Store document with inline markup...");
      try{
        AnnotationSet annotationsToDump = null;
        annotationsToDump = document.getAnnotations();
        // check for restriction from Java property
        String enumaratedAnnTypes =
          System.getProperty(GateConstants.ANNOT_TYPE_TO_EXPORT);

        if(enumaratedAnnTypes != null) {
          Set typesSet = getTypes(enumaratedAnnTypes);
          annotationsToDump = annotationsToDump.get(typesSet);
        } // if
        
        // Prepare to write into the xmlFile using the original encoding
        String encoding = ((gate.TextualDocument)document).getEncoding();
        if(encoding == null || encoding.length() == 0)
          encoding = System.getProperty("file.encoding");
        if(encoding == null || encoding.length() == 0) encoding = "UTF-8";

        OutputStreamWriter writer = new OutputStreamWriter(
                                      new FileOutputStream(targetFile),
                                      encoding);

        //determine if the features need to be saved first
        Boolean featuresSaved =
            Gate.getUserConfig().getBoolean(
              GateConstants.SAVE_FEATURES_WHEN_PRESERVING_FORMAT);
        boolean saveFeatures = true;
        if (featuresSaved != null)
          saveFeatures = featuresSaved.booleanValue();

        // Write with the toXml() method
        String toXml = document.toXml(annotationsToDump, saveFeatures);

        // check for plain text feature and add root XML tag <GATE>
        String mimeType = (String) document.getFeatures().get("MimeType");
        if("text/plain".equalsIgnoreCase(mimeType)) {
          toXml = "<GATE>\n"+ toXml + "\n</GATE>";
        } // if
        
        writer.write(toXml);
        writer.flush();
        writer.close();
      } catch (Exception ex){
        ex.printStackTrace(Out.getPrintWriter());
      }// End try
      
      MainFrame.unlockGUI();
    } // run()
  } // ExportInline

  /** Export current document action */
  class ExportDocumentAction extends AbstractAction {
    public ExportDocumentAction() {
      super("in GATE format");
      putValue(SHORT_DESCRIPTION, "Save the selected document in XML format");
    } // ExportDocumentAction()

    public void actionPerformed(ActionEvent e) {
      JComponent resource = (JComponent)
                                  mainTabbedPane.getSelectedComponent();
      if (resource != null){
        Action act = resource.getActionMap().get("Save As XML");
        if (act != null)
          act.actionPerformed(null);
      }// End if
    } // actionPerformed(ActionEvent e)
  } // class ExportDocumentAction extends AbstractAction

  /** Export All menu action */
  class ExportAllDocumentAction extends AbstractAction {
    public ExportAllDocumentAction() {
      super("in GATE format");
      putValue(SHORT_DESCRIPTION, "Save all documents in XML format");
    } // ExportAllDocumentAction()

    public void actionPerformed(ActionEvent e) {
      fileChooser.setDialogTitle("Select Export directory");
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      int res = fileChooser.showOpenDialog(ShellSlacFrame.this);
      if(res == fileChooser.APPROVE_OPTION) {
        File directory = fileChooser.getSelectedFile();
        if(directory != null && directory.isDirectory()) {
          Runnable run = new ExportAllRunnable(directory);
          Thread thread = new Thread(run, "");
          thread.setPriority(Thread.MIN_PRIORITY);
          thread.start();
        } // if
      } // if
    } // actionPerformed(ActionEvent e)
  } // class ExportAllDocumentAction extends AbstractAction

  /** Export All Inline menu action */
  class ExportAllDocumentInlineAction extends AbstractAction {
    public ExportAllDocumentInlineAction() {
      super("with inline markup");
      putValue(SHORT_DESCRIPTION, "Save all documents in XML format"
            +" with inline markup");
    } // ExportAllDocumentInlineAction()

    public void actionPerformed(ActionEvent e) {
      fileChooser.setDialogTitle("Select Export directory");
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

      int res = fileChooser.showOpenDialog(ShellSlacFrame.this);
      if(res == fileChooser.APPROVE_OPTION) {
        File directory = fileChooser.getSelectedFile();
        if(directory != null && directory.isDirectory()) {
          Document currentDoc;
          String fileName;
          URL fileURL;
          Runnable run;
          Thread thread;
          
          for(int i=0; i<corpus.size(); ++i) {
            currentDoc = (Document) corpus.get(i);

            fileURL = currentDoc.getSourceUrl();
            fileName = null;
            if(fileURL != null){
              fileName = fileURL.getFile();
              fileName = Files.getLastPathComponent(fileName);
            } // if
            if(fileName == null || fileName.length() == 0){
              fileName = currentDoc.getName();
            } // if
            if(fileName.length() == 0) {
              fileName = "gate_result"+i;
            } // if      
            // create full file name
            fileName = fileName+".gate";
            
            // run export
            run = new ExportInline(currentDoc, new File(directory, fileName));
            thread = new Thread(run, "");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
          } // for
        } // if
      } // if
    } // actionPerformed(ActionEvent e)
  } // class ExportAllDocumentInlineAction extends AbstractAction

  /** Object to run ExportAll in a new Thread */
  private class ExportAllRunnable implements Runnable {
    File directory;
    ExportAllRunnable(File targetDirectory) {
      directory = targetDirectory;
    } // ExportAllRunnable(File targetDirectory)

    public void run() {
      saveDocuments(directory);
    } // run()
  } // ExportAllRunnable

  /** Load application from file */
  private class ApplicationLoadRun implements Runnable {
    private String appURL;
    private MainFrame appFrame;
    public ApplicationLoadRun(String url, MainFrame frame) {
      appURL = url;
      appFrame = frame;
    }

    public void run(){
      File file = new File(appURL);
      boolean appLoaded = false;

      appFrame.lockGUI("Application from '"+appURL+"' is being loaded...");
      if( file.exists() ) {
        try {
          gate.util.persistence.PersistenceManager.loadObjectFromFile(file);
          appLoaded = true;
        } catch (PersistenceException pex) {
          pex.printStackTrace();
        } catch (ResourceInstantiationException riex) {
          riex.printStackTrace();
        } catch (IOException ioex) {
          ioex.printStackTrace();
        } // catch
      } // if
      appFrame.unlockGUI();

      if(!appLoaded) {
        // file do not exist. Show a message
        JOptionPane.showMessageDialog(ShellSlacFrame.this,
            "The application file '"+appURL+"'\n"
            +"from parameter -Dgate.slug.app\n"
            +"is missing or corrupted."
            +"Create default application.",
            "Load application error",
            JOptionPane.WARNING_MESSAGE);

        createDefaultApplication();
      } // if
    } // run
  } // class ApplicationLoadRun implements Runnable

  /** Create default ANNIE */
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

  class AboutPaneDialog extends JDialog {
    public AboutPaneDialog(Frame frame, String title, boolean modal) {
      super(frame, title, modal);
    } // AboutPaneDialog

    public boolean setURL(URL url) {
      boolean success = false;
      // try to show in JEditorPane
      try {
        Container pane = getContentPane();

        JScrollPane scroll = new JScrollPane();
        JEditorPane editor = new JEditorPane(url);
        editor.setEditable(false);
        scroll.getViewport().add(editor);
        pane.add(scroll, BorderLayout.CENTER);

        JButton ok = new JButton("Close");
        ok.addActionListener( new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AboutPaneDialog.this.hide();
          }
        });
        pane.add(ok, BorderLayout.SOUTH);
        success = true;
      } catch (Exception ex) {
        if(DEBUG) {
          ex.printStackTrace();
        }
      } // catch
      return success;
    } // setURL
  } // class AboutPaneDialog

  /** Dummy Help About dialog */
  class HelpAboutSlugAction extends AbstractAction {
    public HelpAboutSlugAction() {
      super("About");
    } // HelpAboutSlugAction()

    public void actionPerformed(ActionEvent e) {

      // Set about box content from Java properties
      String aboutText = "Slug application.";
      String aboutURL =
        System.getProperty(GateConstants.ABOUT_URL_JAVA_PROPERTY_NAME);

      boolean canShowInPane = false;

      if(aboutURL != null) {
        try {
          URL url = new URL(aboutURL);

          AboutPaneDialog dlg =
            new AboutPaneDialog(ShellSlacFrame.this,
                                "Slug application about", true);
          canShowInPane = dlg.setURL(url);
          if(canShowInPane) {
            dlg.setSize(300, 200);
            dlg.setLocationRelativeTo(ShellSlacFrame.this);
            dlg.show();
          } // if
          else {
            BufferedReader reader = new BufferedReader(
              new InputStreamReader(url.openStream()));
            String line = "";
            StringBuffer content = new StringBuffer();
            do {
              content.append(line);
              line = reader.readLine();
            } while (line != null);

            if(content.length() != 0) {
              aboutText = content.toString();
            } // if
          } // if

        } catch (Exception ex) {
          // do nothing on exception
          // application just stay with a dummy text in about box
          if(DEBUG) {
            ex.printStackTrace();
          }
        } // catch
      } // if


      if(!canShowInPane) JOptionPane.showMessageDialog(ShellSlacFrame.this,
          aboutText,
          "Slug application about",
          JOptionPane.INFORMATION_MESSAGE);
    } // actionPerformed(ActionEvent e)
  } // class HelpAboutSlugAction extends AbstractAction

  
  /**
   * Component used to select the options for corpus populating
   */
  CorpusFillerComponent corpusFiller;

  class PopulateCorpusAction extends AbstractAction {
    PopulateCorpusAction() {
      super("New Documents...");
    } // PopulateCorpusAction()

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable(){
        public void run(){
          if(corpus == null || corpusFiller == null) return;
          corpusFiller.setExtensions(new ArrayList());
          corpusFiller.setEncoding("");
          boolean answer = OkCancelDialog.showDialog(
                                  ShellSlacFrame.this,
                                  corpusFiller,
                                  "Select a directory and allowed extensions");
          if(answer){
            URL url = null;
            try{
              url = new URL(corpusFiller.getUrlString());
              java.util.List extensions = corpusFiller.getExtensions();
              ExtensionFileFilter filter = null;
              if(extensions == null || extensions.isEmpty()) filter = null;
              else{
                filter = new ExtensionFileFilter();
                Iterator extIter = corpusFiller.getExtensions().iterator();
                while(extIter.hasNext()){
                  filter.addExtension((String)extIter.next());
                }
              }
              corpus.populate(url, filter,
                                corpusFiller.getEncoding(),
                                corpusFiller.isRecurseDirectories());
            }catch(MalformedURLException mue){
              JOptionPane.showMessageDialog(ShellSlacFrame.this,
                                            "Invalid URL!\n " +
                                            "See \"Messages\" tab for details!",
                                            "Gate", JOptionPane.ERROR_MESSAGE);
              mue.printStackTrace(Err.getPrintWriter());
            }catch(IOException ioe){
              JOptionPane.showMessageDialog(ShellSlacFrame.this,
                                            "I/O error!\n " +
                                            "See \"Messages\" tab for details!",
                                            "Gate", JOptionPane.ERROR_MESSAGE);
              ioe.printStackTrace(Err.getPrintWriter());
            }catch(ResourceInstantiationException rie){
              JOptionPane.showMessageDialog(ShellSlacFrame.this,
                                            "Could not create document!\n " +
                                            "See \"Messages\" tab for details!",
                                            "Gate", JOptionPane.ERROR_MESSAGE);
              rie.printStackTrace(Err.getPrintWriter());
            }
          }
        }
      };
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    } // actionPerformed(ActionEvent e)
  } // class PopulateCorpusAction extends AbstractAction
  
} // class ShellSlacFrame
