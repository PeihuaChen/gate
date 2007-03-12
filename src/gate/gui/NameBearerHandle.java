/*
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 23/01/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import gate.*;
import gate.corpora.DocumentStaxUtils;
import gate.creole.*;
import gate.creole.ir.*;
import gate.event.*;
import gate.persist.PersistenceException;
import gate.security.*;
import gate.security.SecurityException;
import gate.swing.XJMenuItem;
import gate.swing.XJPopupMenu;
import gate.util.*;

/**
 * Class used to store the GUI information about an open entity
 * (resource, controller, datastore). Such information will include icon
 * to be used for tree components, popup menu for right click events,
 * large and small views, etc.
 */
public class NameBearerHandle implements Handle, StatusListener,
                             ProgressListener, CreoleListener {

  public NameBearerHandle(NameBearer target, Window window) {
    this.target = target;
    this.window = window;
    actionPublishers = new ArrayList();

    sListenerProxy = new ProxyStatusListener();
    String iconName = null;
    if(target instanceof Resource) {
      rData = (ResourceData)Gate.getCreoleRegister().get(
              target.getClass().getName());
      if(rData != null) {
        iconName = rData.getIcon();
        if(iconName == null) {
          if(target instanceof LanguageResource)
            iconName = "lr";
          else if(target instanceof ProcessingResource)
            iconName = "pr";
          else if(target instanceof Controller) iconName = "application";
        }
        if(target instanceof Controller && target.getName().startsWith("ANNIE"))
          iconName = "annie-application";
        tooltipText = "<HTML> <b>" + rData.getComment() + "</b><br>(<i>"
                + rData.getClassName() + "</i>)</HTML>";
      }
      else {
        iconName = "lr";
      }
    }
    else if(target instanceof DataStore) {
      iconName = ((DataStore)target).getIconName();
      tooltipText = ((DataStore)target).getComment();
    }

    title = (String)target.getName();
    this.icon = MainFrame.getIcon(iconName);

    Gate.getCreoleRegister().addCreoleListener(this);

    if(target instanceof ActionsPublisher) actionPublishers.add(target);

    buildStaticPopupItems();

    viewsBuilt = false;
  }// public DefaultResourceHandle(FeatureBearer res)

  public Icon getIcon() {
    return icon;
  }

  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String newTitle) {
    this.title = newTitle;
  }

  /**
   * Returns <tt>true</tt> if the views have already been built for
   * this handle.
   * 
   * @return a <tt>boolean</tt> value.
   */
  public boolean viewsBuilt() {
    return viewsBuilt;
  }

  /**
   * Returns a GUI component to be used as a small viewer/editor, e.g.
   * below the main tree in the Gate GUI for the selected resource
   */
  public JComponent getSmallView() {
    if(!viewsBuilt) buildViews();
    return smallView;
  }

  /**
   * Returns the large view for this resource. This view will go into
   * the main display area.
   */
  public JComponent getLargeView() {
    if(!viewsBuilt) buildViews();
    return largeView;
  }

  public JPopupMenu getPopup() {
    JPopupMenu popup = new XJPopupMenu();
    // first add the static items
    Iterator itemIter = staticPopupItems.iterator();
    while(itemIter.hasNext()) {
      JMenuItem anItem = (JMenuItem)itemIter.next();
      if(anItem == null)
        popup.addSeparator();
      else popup.add(anItem);
    }

    // next add the dynamic list from the target and its editors
    Iterator publishersIter = actionPublishers.iterator();
    while(publishersIter.hasNext()) {
      ActionsPublisher aPublisher = (ActionsPublisher)publishersIter.next();
      if(aPublisher.getActions() != null) {
        Iterator actionIter = aPublisher.getActions().iterator();
        while(actionIter.hasNext()) {
          Action anAction = (Action)actionIter.next();
          if(anAction == null)
            popup.addSeparator();
          else {
            popup.add(new XJMenuItem(anAction, sListenerProxy));
          }
        }
      }
    }

    return popup;
  }

  public String getTooltipText() {
    return tooltipText;
  }

  public void setTooltipText(String text) {
    this.tooltipText = text;
  }

  public Object getTarget() {
    return target;
  }

  public Action getCloseAction() {
    return new CloseAction();
  }

  /** Fill HMM Save and Save As... actions */
  private void fillHMMActions(List popupItems) {
    Action action;

    com.ontotext.gate.hmm.agent.AlternativeHMMAgent hmmPR = (com.ontotext.gate.hmm.agent.AlternativeHMMAgent)target;

    popupItems.add(null);
    action = new com.ontotext.gate.hmm.agent.SaveAction(hmmPR);
    action.putValue(Action.SHORT_DESCRIPTION,
            "Save trained HMM model into PR URL file");
    // Add Save trained HMM model action
    popupItems.add(new XJMenuItem(action, sListenerProxy));

    action = new com.ontotext.gate.hmm.agent.SaveAsAction(hmmPR);
    action.putValue(Action.SHORT_DESCRIPTION,
            "Save trained HMM model into new file");
    // Add Save As... trained HMM model action
    popupItems.add(new XJMenuItem(action, sListenerProxy));
  } // fillHMMActions(gate.gui.ProtegeWrapper protege)

  // protected JPopupMenu buildPopup(){
  // //build the popup
  // JPopupMenu popup = new JPopupMenu();
  // XJMenuItem closeItem = new XJMenuItem(new CloseAction(),
  // sListenerProxy);
  // closeItem.setAccelerator(KeyStroke.getKeyStroke(
  // KeyEvent.VK_F4, ActionEvent.CTRL_MASK));
  // popup.add(closeItem);
  //
  // if(target instanceof ProcessingResource){
  // popup.addSeparator();
  // popup.add(new XJMenuItem(new ReloadAction(), sListenerProxy));
  // if(target instanceof gate.ml.DataCollector){
  // popup.add(new DumpArffAction());
  // }
  // if(target instanceof
  // com.ontotext.gate.hmm.agent.AlternativeHMMAgent) {
  // fillHMMActions(popup);
  // } // if
  // }else if(target instanceof LanguageResource) {
  // //Language Resources
  // popup.addSeparator();
  // popup.add(new XJMenuItem(new SaveAction(), sListenerProxy));
  // popup.add(new XJMenuItem(new SaveToAction(), sListenerProxy));
  // if(target instanceof gate.TextualDocument){
  // XJMenuItem saveAsXmlItem =
  // new XJMenuItem(new SaveAsXmlAction(), sListenerProxy);
  // saveAsXmlItem.setAccelerator(KeyStroke.getKeyStroke(
  // KeyEvent.VK_X, ActionEvent.CTRL_MASK));
  //
  // popup.add(saveAsXmlItem);
  // XJMenuItem savePreserveFormatItem =
  // new XJMenuItem(new DumpPreserveFormatAction(),
  // sListenerProxy);
  // popup.add(savePreserveFormatItem);
  // }else if(target instanceof Corpus){
  // popup.addSeparator();
  // corpusFiller = new CorpusFillerComponent();
  // popup.add(new XJMenuItem(new PopulateCorpusAction(),
  // sListenerProxy));
  // popup.addSeparator();
  // popup.add(new XJMenuItem(new SaveCorpusAsXmlAction(false),
  // sListenerProxy));
  // popup.add(new XJMenuItem(new SaveCorpusAsXmlAction(true),
  // sListenerProxy));
  // if (target instanceof IndexedCorpus){
  // popup.addSeparator();
  // popup.add(new XJMenuItem(new CreateIndexAction(), sListenerProxy));
  // popup.add(new XJMenuItem(new OptimizeIndexAction(),
  // sListenerProxy));
  // popup.add(new XJMenuItem(new DeleteIndexAction(), sListenerProxy));
  // }
  // }
  // if (target instanceof gate.creole.ProtegeProjectName){
  // fillProtegeActions(popup);
  // }// End if
  // }else if(target instanceof Controller){
  // //Applications
  // popup.addSeparator();
  // popup.add(new XJMenuItem(new DumpToFileAction(), sListenerProxy));
  // }
  //
  // //add the custom actions from the resource if any are provided
  // if(target instanceof ActionsPublisher){
  // Iterator actionsIter =
  // ((ActionsPublisher)target).getActions().iterator();
  // while(actionsIter.hasNext()){
  // Action anAction = (Action)actionsIter.next();
  // if(anAction == null) popup.addSeparator();
  // else{
  // if(window instanceof StatusListener)
  // popup.add(new XJMenuItem(anAction, (StatusListener)window));
  // else popup.add(anAction);
  // }
  // }
  // }
  // return popup;
  // }

  protected void buildStaticPopupItems() {
    // build the static part of the popup
    staticPopupItems = new ArrayList();

    XJMenuItem closeItem = new XJMenuItem(new CloseAction(), sListenerProxy);
    closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
            ActionEvent.CTRL_MASK));
    staticPopupItems.add(closeItem);

    if(target instanceof ProcessingResource) {
      staticPopupItems.add(null);
      staticPopupItems.add(new XJMenuItem(new ReloadAction(), sListenerProxy));
      if(target instanceof com.ontotext.gate.hmm.agent.AlternativeHMMAgent) {
        fillHMMActions(staticPopupItems);
      } // if
    }
    else if(target instanceof LanguageResource) {
      // Language Resources
      staticPopupItems.add(null);
      staticPopupItems.add(new XJMenuItem(new SaveAction(), sListenerProxy));
      staticPopupItems.add(new XJMenuItem(new SaveToAction(), sListenerProxy));
      if(target instanceof gate.TextualDocument) {
        XJMenuItem saveAsXmlItem = new XJMenuItem(new SaveAsXmlAction(),
                sListenerProxy);
        saveAsXmlItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                ActionEvent.CTRL_MASK));

        staticPopupItems.add(saveAsXmlItem);
      }
      else if(target instanceof Corpus) {
        staticPopupItems.add(null);
        corpusFiller = new CorpusFillerComponent();
        staticPopupItems.add(new XJMenuItem(new PopulateCorpusAction(),
                sListenerProxy));
        staticPopupItems.add(null);
        staticPopupItems.add(new XJMenuItem(new SaveCorpusAsXmlAction(false),
                sListenerProxy));
        // staticPopupItems.add(new XJMenuItem(new
        // SaveCorpusAsXmlAction(true), sListenerProxy));
        if(target instanceof IndexedCorpus) {
          staticPopupItems.add(null);
          staticPopupItems.add(new XJMenuItem(new CreateIndexAction(),
                  sListenerProxy));
          staticPopupItems.add(new XJMenuItem(new OptimizeIndexAction(),
                  sListenerProxy));
          staticPopupItems.add(new XJMenuItem(new DeleteIndexAction(),
                  sListenerProxy));
        }
      }
      
      if(target instanceof Document) {
        staticPopupItems.add(null);
        staticPopupItems.add(new XJMenuItem(new CreateCorpusForDocAction(),
                sListenerProxy));
      }
      
    }
    else if(target instanceof Controller) {
      // Applications
      staticPopupItems.add(null);
      staticPopupItems.add(new XJMenuItem(new DumpToFileAction(),
              sListenerProxy));
    }
  }

  protected void buildViews() {
    viewsBuilt = true;
    fireStatusChanged("Building views...");

    // build the large views
    List largeViewNames = Gate.getCreoleRegister().getLargeVRsForResource(
            target.getClass().getName());
    if(largeViewNames != null && !largeViewNames.isEmpty()) {
      largeView = new JTabbedPane(JTabbedPane.BOTTOM);
      Iterator classNameIter = largeViewNames.iterator();
      while(classNameIter.hasNext()) {
        try {
          String className = (String)classNameIter.next();
          ResourceData rData = (ResourceData)Gate.getCreoleRegister().get(
                  className);
          FeatureMap params = Factory.newFeatureMap();
          FeatureMap features = Factory.newFeatureMap();
          Gate.setHiddenAttribute(features, true);
          VisualResource view = (VisualResource)Factory.createResource(
                  className, params, features);
          view.setTarget(target);
          view.setHandle(this);
          ((JTabbedPane)largeView).add((Component)view, rData.getName());
          // if view provide actions, add it to the list of action
          // puiblishers
          if(view instanceof ActionsPublisher) actionPublishers.add(view);
        }
        catch(ResourceInstantiationException rie) {
          rie.printStackTrace(Err.getPrintWriter());
        }
      }
      if(largeViewNames.size() == 1) {
        largeView = (JComponent)((JTabbedPane)largeView).getComponentAt(0);
      }
      else {
        ((JTabbedPane)largeView).setSelectedIndex(0);
      }
    }

    // build the small views
    List smallViewNames = Gate.getCreoleRegister().getSmallVRsForResource(
            target.getClass().getName());
    if(smallViewNames != null && !smallViewNames.isEmpty()) {
      smallView = new JTabbedPane(JTabbedPane.BOTTOM);
      Iterator classNameIter = smallViewNames.iterator();
      while(classNameIter.hasNext()) {
        try {
          String className = (String)classNameIter.next();
          ResourceData rData = (ResourceData)Gate.getCreoleRegister().get(
                  className);
          FeatureMap params = Factory.newFeatureMap();
          FeatureMap features = Factory.newFeatureMap();
          Gate.setHiddenAttribute(features, true);
          VisualResource view = (VisualResource)Factory.createResource(
                  className, params, features);
          view.setTarget(target);
          view.setHandle(this);
          ((JTabbedPane)smallView).add((Component)view, rData.getName());
          if(view instanceof ActionsPublisher) actionPublishers.add(view);
        }
        catch(ResourceInstantiationException rie) {
          rie.printStackTrace(Err.getPrintWriter());
        }
      }
      if(smallViewNames.size() == 1) {
        smallView = (JComponent)((JTabbedPane)smallView).getComponentAt(0);
      }
      else {
        ((JTabbedPane)smallView).setSelectedIndex(0);
      }
    }
    fireStatusChanged("Views built!");

    // Add the CTRL +F4 key & action combination to the resource
    JComponent largeView = this.getLargeView();
    if(largeView != null) {
      largeView.getActionMap().put("Close resource", new CloseAction());
      if(target instanceof gate.TextualDocument) {
        largeView.getActionMap().put("Save As XML", new SaveAsXmlAction());
      }// End if
    }// End if
  }// protected void buildViews

  public String toString() {
    return title;
  }

  public synchronized void removeProgressListener(ProgressListener l) {
    if(progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector)progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }// public synchronized void removeProgressListener(ProgressListener
    // l)

  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null
            ? new Vector(2)
            : (Vector)progressListeners.clone();
    if(!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }// public synchronized void addProgressListener(ProgressListener l)

  String title;

  String tooltipText;

  NameBearer target;

  /**
   * Stores all the action providers for this resource. They will be
   * questioned when the getPopup() method is called.
   */
  protected List actionPublishers;

  /**
   * A list of menu items that constitute the static part of the popup.
   * Null values are used for separators.
   */
  protected List staticPopupItems;

  /**
   * The top level GUI component this hadle belongs to.
   */
  Window window;

  ResourceData rData;

  Icon icon;

  JComponent smallView;

  JComponent largeView;

  protected boolean viewsBuilt = false;

  /**
   * Component used to select the options for corpus populating
   */
  CorpusFillerComponent corpusFiller;

  StatusListener sListenerProxy;

  // File currentDir = null;
  private transient Vector progressListeners;

  private transient Vector statusListeners;

  class CloseAction extends AbstractAction {
    public CloseAction() {
      super("Close");
      putValue(SHORT_DESCRIPTION, "Removes this resource from the system");
    }

    public void actionPerformed(ActionEvent e) {
      if(target instanceof Resource) {
        Factory.deleteResource((Resource)target);
      }
      else if(target instanceof DataStore) {
        try {
          ((DataStore)target).close();
        }
        catch(PersistenceException pe) {
          JOptionPane.showMessageDialog(largeView != null
                  ? largeView
                  : smallView, "Error!\n" + pe.toString(), "GATE",
                  JOptionPane.ERROR_MESSAGE);
        }
      }

      statusListeners.clear();
      progressListeners.clear();
      // //delete the viewers
      // if(largeView instanceof VisualResource){
      // Factory.deleteResource((VisualResource)largeView);
      // }else if(largeView instanceof JTabbedPane){
      // Component[] comps = ((JTabbedPane)largeView).getComponents();
      // for(int i = 0; i < comps.length; i++){
      // if(comps[i] instanceof VisualResource)
      // Factory.deleteResource((VisualResource)comps[i]);
      // }
      // }
      // if(smallView instanceof VisualResource){
      // Factory.deleteResource((VisualResource)smallView);
      // }else if(smallView instanceof JTabbedPane){
      // Component[] comps = ((JTabbedPane)smallView).getComponents();
      // for(int i = 0; i < comps.length; i++){
      // if(comps[i] instanceof VisualResource)
      // Factory.deleteResource((VisualResource)comps[i]);
      // }
      // }
      //
    }// public void actionPerformed(ActionEvent e)
  }// class CloseAction

  /**
   * Used to save a document as XML
   */
  class SaveAsXmlAction extends AbstractAction {
    public SaveAsXmlAction() {
      super("Save As Xml...");
      putValue(SHORT_DESCRIPTION, "Saves this resource in XML");
    }// SaveAsXmlAction()

    public void actionPerformed(ActionEvent e) {
      Runnable runableAction = new Runnable() {
        public void run() {
          JFileChooser fileChooser = MainFrame.getFileChooser();
          File selectedFile = null;

          List filters = Arrays.asList(fileChooser.getChoosableFileFilters());
          Iterator filtersIter = filters.iterator();
          FileFilter filter = null;
          if(filtersIter.hasNext()) {
            filter = (FileFilter)filtersIter.next();
            while(filtersIter.hasNext()
                    && filter.getDescription().indexOf("XML") == -1) {
              filter = (FileFilter)filtersIter.next();
            }
          }
          if(filter == null || filter.getDescription().indexOf("XML") == -1) {
            // no suitable filter found, create a new one
            ExtensionFileFilter xmlFilter = new ExtensionFileFilter();
            xmlFilter.setDescription("XML files");
            xmlFilter.addExtension("xml");
            xmlFilter.addExtension("gml");
            fileChooser.addChoosableFileFilter(xmlFilter);
            filter = xmlFilter;
          }
          fileChooser.setFileFilter(filter);

          fileChooser.setMultiSelectionEnabled(false);
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          fileChooser.setDialogTitle("Select document to save ...");
          fileChooser.setSelectedFiles(null);

          int res = (getLargeView() != null) ? fileChooser.showDialog(
                  getLargeView(), "Save") : (getSmallView() != null)
                  ? fileChooser.showDialog(getSmallView(), "Save")
                  : fileChooser.showDialog(null, "Save");
          if(res == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            File currentDir = fileChooser.getCurrentDirectory();
            if(selectedFile == null) return;
            long start = System.currentTimeMillis();
            NameBearerHandle.this.statusChanged("Saving as XML to "
                    + selectedFile.toString() + "...");
            try {
              MainFrame.lockGUI("Saving...");
              // Prepare to write into the xmlFile using the original
              // encoding
              // //////////////////////////////
              // String encoding =
              // ((gate.TextualDocument)target).getEncoding();

              // OutputStreamWriter writer = new OutputStreamWriter(
              // new FileOutputStream(selectedFile),
              // encoding);

              // Write (test the toXml() method)
              // This Action is added only when a gate.Document is
              // created.
              // So, is for sure that the resource is a gate.Document
              // writer.write(((gate.Document)target).toXml());
              // writer.flush();
              // writer.close();
              
              // write directly to the file using StAX
              DocumentStaxUtils.writeDocument((gate.Document)target,
                      selectedFile);
            }
            catch(Exception ex) {
              ex.printStackTrace(Out.getPrintWriter());
            }
            finally {
              MainFrame.unlockGUI();
            }
            long time = System.currentTimeMillis() - start;
            NameBearerHandle.this.statusChanged("Finished saving as xml into "
                    + " the file: " + selectedFile.toString() + " in "
                    + ((double)time) / 1000 + " s");
          }// End if
        }// End run()
      };// End Runnable
      Thread thread = new Thread(runableAction, "");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed()
  }// SaveAsXmlAction

  /**
   * Saves a corpus as a set of xml files in a directory.
   */
  class SaveCorpusAsXmlAction extends AbstractAction {
    private boolean preserveFormat;

    public SaveCorpusAsXmlAction(boolean preserveFormat) {
      super("Save As Xml...");
      putValue(SHORT_DESCRIPTION, "Saves this corpus in XML");
      this.preserveFormat = preserveFormat;

      if(preserveFormat) {
        putValue(NAME, "Save As Xml preserve format...");
        putValue(SHORT_DESCRIPTION, "Saves this corpus in XML preserve format");
      } // if
    }// SaveAsXmlAction()

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          if(preserveFormat) System.out.println("Preserve option set!");
          try {
            // we need a directory
            JFileChooser filer = MainFrame.getFileChooser();
            filer
                    .setDialogTitle("Select the directory that will contain the corpus");
            filer.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            filer.setFileFilter(filer.getAcceptAllFileFilter());

            if(filer.showDialog(getLargeView() != null
                    ? getLargeView()
                    : getSmallView(), "Select") == JFileChooser.APPROVE_OPTION) {

              File dir = filer.getSelectedFile();
              // create the top directory if needed
              if(!dir.exists()) {
                if(!dir.mkdirs()) {
                  JOptionPane.showMessageDialog(largeView != null
                          ? largeView
                          : smallView, "Could not create top directory!",
                          "GATE", JOptionPane.ERROR_MESSAGE);
                  return;
                }
              }

              MainFrame.lockGUI("Saving...");

              // iterate through all the docs and save each of them as
              // xml
              Corpus corpus = (Corpus)target;
              Iterator docIter = corpus.iterator();
              boolean overwriteAll = false;
              int docCnt = corpus.size();
              int currentDocIndex = 0;
              while(docIter.hasNext()) {
                boolean docWasLoaded = corpus.isDocumentLoaded(currentDocIndex);
                Document currentDoc = (Document)docIter.next();
                URL sourceURL = currentDoc.getSourceUrl();
                String fileName = null;
                if(sourceURL != null) {
                  fileName = sourceURL.getFile();
                  fileName = Files.getLastPathComponent(fileName);
                }
                if(fileName == null || fileName.length() == 0) {
                  fileName = currentDoc.getName();
                }
                // makes sure that the filename does not contain any
                // forbidden character
                fileName = fileName.replaceAll("[\\/:\\*\\?\"<>|]", "_");

                if(!fileName.toLowerCase().endsWith(".xml"))
                  fileName += ".xml";
                File docFile = null;
                boolean nameOK = false;
                do {
                  docFile = new File(dir, fileName);
                  if(docFile.exists() && !overwriteAll) {
                    // ask the user if we can ovewrite the file
                    Object[] options = new Object[] {"Yes", "All", "No",
                        "Cancel"};
                    MainFrame.unlockGUI();
                    int answer = JOptionPane.showOptionDialog(largeView != null
                            ? largeView
                            : smallView, "File " + docFile.getName()
                            + " already exists!\n" + "Overwrite?", "GATE",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.WARNING_MESSAGE, null, options,
                            options[2]);
                    MainFrame.lockGUI("Saving...");
                    switch(answer) {
                      case 0: {
                        nameOK = true;
                        break;
                      }
                      case 1: {
                        nameOK = true;
                        overwriteAll = true;
                        break;
                      }
                      case 2: {
                        // user said NO, allow them to provide an
                        // alternative name;
                        MainFrame.unlockGUI();
                        fileName = (String)JOptionPane.showInputDialog(
                                largeView != null ? largeView : smallView,
                                "Please provide an alternative file name",
                                "GATE", JOptionPane.QUESTION_MESSAGE, null,
                                null, fileName);
                        if(fileName == null) {
                          fireProcessFinished();
                          return;
                        }
                        MainFrame.lockGUI("Saving");
                        break;
                      }
                      case 3: {
                        // user gave up; return
                        fireProcessFinished();
                        return;
                      }
                    }

                  }
                  else {
                    nameOK = true;
                  }
                } while(!nameOK);
                // save the file
                try {
                  String content = "";
                  // check for preserve format flag
                  if(preserveFormat) {
                    Set annotationsToDump = null;
                    // Find the shown document editor.
                    // If none, just dump the original markup
                    // annotations,
                    // i.e., leave the annotationsToDump null
                    if(largeView instanceof JTabbedPane) {
                      Component shownComponent = ((JTabbedPane)largeView)
                              .getSelectedComponent();
                      if(shownComponent instanceof DocumentEditor) {
                        // so we only get annotations for dumping
                        // if they are shown in the table of the
                        // document editor,
                        // which is currently in front of the user
                        annotationsToDump = ((DocumentEditor)shownComponent)
                                .getDisplayedAnnotations();
                      }// if we have a document editor
                    }// if tabbed pane

                    // determine if the features need to be saved first
                    Boolean featuresSaved = Gate.getUserConfig().getBoolean(
                            GateConstants.SAVE_FEATURES_WHEN_PRESERVING_FORMAT);
                    boolean saveFeatures = true;
                    if(featuresSaved != null)
                      saveFeatures = featuresSaved.booleanValue();

                    // Write with the toXml() method
                    content = currentDoc.toXml(annotationsToDump, saveFeatures);

                    // Prepare to write into the xmlFile using the
                    // original encoding
                    String encoding = ((gate.TextualDocument)currentDoc)
                            .getEncoding();

                    OutputStreamWriter writer = new OutputStreamWriter(
                            new FileOutputStream(docFile), encoding);

                    writer.write(content);
                    writer.flush();
                    writer.close();
                  }
                  else {
                    // for GATE XML format, use the direct StAX writer
                    DocumentStaxUtils.writeDocument(currentDoc, docFile);
                  } // if
                }
                catch(Exception ioe) {
                  MainFrame.unlockGUI();
                  JOptionPane.showMessageDialog(largeView != null
                          ? largeView
                          : smallView, "Could not create write file:"
                          + ioe.toString(), "GATE", JOptionPane.ERROR_MESSAGE);
                  ioe.printStackTrace(Err.getPrintWriter());
                  return;
                }

                fireStatusChanged(currentDoc.getName() + " saved");
                // close the doc if it wasn't already loaded
                if(!docWasLoaded) {
                  corpus.unloadDocument(currentDoc);
                  Factory.deleteResource(currentDoc);
                }

                fireProgressChanged(100 * currentDocIndex++ / docCnt);
              }// while(docIter.hasNext())
              fireStatusChanged("Corpus saved");
              fireProcessFinished();
            }// select directory
          }
          finally {
            MainFrame.unlockGUI();
          }
        }// public void run(){
      };// Runnable runnable = new Runnable()
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
              runnable, "Corpus XML dumper");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();

    }// public void actionPerformed(ActionEvent e)
  }// class SaveCorpusAsXmlAction extends AbstractAction

  /**
   * Saves a corpus as a set of xml files in a directory.
   */
  class ReloadClassAction extends AbstractAction {
    public ReloadClassAction() {
      super("Reload resource class");
      putValue(SHORT_DESCRIPTION, "Reloads the java class for this resource");
    }// SaveAsXmlAction()

    public void actionPerformed(ActionEvent e) {
      int answer = JOptionPane.showOptionDialog(largeView != null
              ? largeView
              : smallView, "This is an advanced option!\n"
              + "You should not use this unless your name is Hamish.\n"
              + "Are you sure you want to do this?", "GATE",
              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
              null, null);
      if(answer == JOptionPane.OK_OPTION) {
        try {
          String className = target.getClass().getName();
          Gate.getClassLoader().reloadClass(className);
          fireStatusChanged("Class " + className + " reloaded!");
        }
        catch(Exception ex) {
          JOptionPane.showMessageDialog(largeView != null
                  ? largeView
                  : smallView, "Look what you've done: \n" + ex.toString()
                  + "\nI told you not to do it...", "GATE",
                  JOptionPane.ERROR_MESSAGE);
          ex.printStackTrace(Err.getPrintWriter());
        }
      }
    }
  }

  class SaveAction extends AbstractAction {
    public SaveAction() {
      super("Save");
      putValue(SHORT_DESCRIPTION, "Save back to the datastore");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          DataStore ds = ((LanguageResource)target).getDataStore();
          if(ds != null) {
            try {
              MainFrame.lockGUI("Saving "
                      + ((LanguageResource)target).getName());
              StatusListener sListener = (StatusListener)gate.gui.MainFrame
                      .getListeners().get("gate.event.StatusListener");
              if(sListener != null)
                sListener.statusChanged("Saving: "
                        + ((LanguageResource)target).getName());
              double timeBefore = System.currentTimeMillis();
              ((LanguageResource)target).getDataStore().sync(
                      (LanguageResource)target);
              double timeAfter = System.currentTimeMillis();
              if(sListener != null)
                sListener.statusChanged(((LanguageResource)target).getName()
                        + " saved in "
                        + NumberFormat.getInstance().format(
                                (timeAfter - timeBefore) / 1000) + " seconds");
            }
            catch(PersistenceException pe) {
              MainFrame.unlockGUI();
              JOptionPane.showMessageDialog(getLargeView(), "Save failed!\n "
                      + pe.toString(), "GATE", JOptionPane.ERROR_MESSAGE);
            }
            catch(SecurityException se) {
              MainFrame.unlockGUI();
              JOptionPane.showMessageDialog(getLargeView(), "Save failed!\n "
                      + se.toString(), "GATE", JOptionPane.ERROR_MESSAGE);
            }
            finally {
              MainFrame.unlockGUI();
            }
          }
          else {
            JOptionPane.showMessageDialog(getLargeView(),
                    "This resource has not been loaded from a datastore.\n"
                            + "Please use the \"Save to\" option!\n", "GATE",
                    JOptionPane.ERROR_MESSAGE);

          }
        }
      };
      new Thread(runnable).start();
    }// public void actionPerformed(ActionEvent e)
  }// class SaveAction

  class DumpToFileAction extends AbstractAction {
    public DumpToFileAction() {
      super("Save application state");
      putValue(SHORT_DESCRIPTION,
              "Saves the data needed to recreate this application");
    }

    public void actionPerformed(ActionEvent ae) {
      JFileChooser fileChooser = MainFrame.getFileChooser();

      fileChooser.setDialogTitle("Select a file for this resource");
      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      if(fileChooser.showSaveDialog(largeView) == JFileChooser.APPROVE_OPTION) {
        final File file = fileChooser.getSelectedFile();
        Runnable runnable = new Runnable() {
          public void run() {
            try {
              gate.util.persistence.PersistenceManager.saveObjectToFile(
                      (Resource)target, file);
            }
            catch(Exception e) {
              JOptionPane.showMessageDialog(getLargeView(), "Error!\n"
                      + e.toString(), "GATE", JOptionPane.ERROR_MESSAGE);
              e.printStackTrace(Err.getPrintWriter());
            }
          }
        };
        Thread thread = new Thread(runnable);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
      }
    }

  }

  class SaveToAction extends AbstractAction {
    public SaveToAction() {
      super("Save to...");
      putValue(SHORT_DESCRIPTION, "Save this resource to a datastore");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          try {
            DataStoreRegister dsReg = Gate.getDataStoreRegister();
            Map dsByName = new HashMap();
            Iterator dsIter = dsReg.iterator();
            while(dsIter.hasNext()) {
              DataStore oneDS = (DataStore)dsIter.next();
              String name;
              if((name = (String)oneDS.getName()) != null) {
              }
              else {
                name = oneDS.getStorageUrl();
                try {
                  URL tempURL = new URL(name);
                  name = tempURL.getFile();
                }
                catch(java.net.MalformedURLException ex) {
                  throw new GateRuntimeException();
                }
              }
              dsByName.put(name, oneDS);
            }
            List dsNames = new ArrayList(dsByName.keySet());
            if(dsNames.isEmpty()) {
              JOptionPane.showMessageDialog(getLargeView(),
                      "There are no open datastores!\n "
                              + "Please open a datastore first!", "GATE",
                      JOptionPane.ERROR_MESSAGE);

            }
            else {
              Object answer = JOptionPane.showInputDialog(getLargeView(),
                      "Select the datastore", "GATE",
                      JOptionPane.QUESTION_MESSAGE, null, dsNames.toArray(),
                      dsNames.get(0));
              if(answer == null) return;
              DataStore ds = (DataStore)dsByName.get(answer);
              if(ds == null) {
                Err.prln("The datastore does not exists. Saving procedure"
                        + " has FAILED! This should never happen again!");
                return;
              }// End if
              DataStore ownDS = ((LanguageResource)target).getDataStore();
              if(ds == ownDS) {
                MainFrame.lockGUI("Saving "
                        + ((LanguageResource)target).getName());

                StatusListener sListener = (StatusListener)gate.gui.MainFrame
                        .getListeners().get("gate.event.StatusListener");
                if(sListener != null)
                  sListener.statusChanged("Saving: "
                          + ((LanguageResource)target).getName());
                double timeBefore = System.currentTimeMillis();
                ds.sync((LanguageResource)target);
                double timeAfter = System.currentTimeMillis();
                if(sListener != null)
                  sListener
                          .statusChanged(((LanguageResource)target).getName()
                                  + " saved in "
                                  + NumberFormat.getInstance().format(
                                          (timeAfter - timeBefore) / 1000)
                                  + " seconds");
              }
              else {
                FeatureMap securityData = (FeatureMap)DataStoreRegister
                        .getSecurityData(ds);
                SecurityInfo si = null;
                // check whether the datastore supports security data
                // serial ones do not for example
                if(securityData != null) {
                  // first get the type of access from the user
                  if(!AccessRightsDialog.showDialog(window)) return;
                  int accessType = AccessRightsDialog.getSelectedMode();
                  if(accessType < 0) return;
                  si = new SecurityInfo(accessType, (User)securityData
                          .get("user"), (Group)securityData.get("group"));
                }// if security info
                StatusListener sListener = (StatusListener)gate.gui.MainFrame
                        .getListeners().get("gate.event.StatusListener");
                MainFrame.lockGUI("Saving "
                        + ((LanguageResource)target).getName());

                if(sListener != null)
                  sListener.statusChanged("Saving: "
                          + ((LanguageResource)target).getName());
                double timeBefore = System.currentTimeMillis();
                LanguageResource lr = ds.adopt((LanguageResource)target, si);
                ds.sync(lr);
                double timeAfter = System.currentTimeMillis();
                if(sListener != null)
                  sListener
                          .statusChanged(((LanguageResource)target).getName()
                                  + " saved in "
                                  + NumberFormat.getInstance().format(
                                          (timeAfter - timeBefore) / 1000)
                                  + " seconds");

                // check whether the new LR is different from the
                // transient one and
                // if so, unload the transient LR, so the user realises
                // it is no longer valid. Don't do this in the adopt()
                // code itself
                // because the batch code might wish to keep the
                // transient
                // resource for some purpose.
                if(lr != target) {
                  Factory.deleteResource((LanguageResource)target);
                }
              }
            }
          }
          catch(PersistenceException pe) {
            MainFrame.unlockGUI();
            JOptionPane.showMessageDialog(getLargeView(), "Save failed!\n "
                    + pe.toString(), "GATE", JOptionPane.ERROR_MESSAGE);
          }
          catch(gate.security.SecurityException se) {
            MainFrame.unlockGUI();
            JOptionPane.showMessageDialog(getLargeView(), "Save failed!\n "
                    + se.toString(), "GATE", JOptionPane.ERROR_MESSAGE);
          }
          finally {
            MainFrame.unlockGUI();
          }

        }
      };
      new Thread(runnable).start();
    }
  }// class SaveToAction extends AbstractAction

  class ReloadAction extends AbstractAction {
    ReloadAction() {
      super("Reinitialise");
      putValue(SHORT_DESCRIPTION, "Reloads this resource");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          if(!(target instanceof ProcessingResource)) return;
          try {
            long startTime = System.currentTimeMillis();
            fireStatusChanged("Reinitialising " + target.getName());
            Map listeners = new HashMap();
            StatusListener sListener = new StatusListener() {
              public void statusChanged(String text) {
                fireStatusChanged(text);
              }
            };
            listeners.put("gate.event.StatusListener", sListener);

            ProgressListener pListener = new ProgressListener() {
              public void progressChanged(int value) {
                fireProgressChanged(value);
              }

              public void processFinished() {
                fireProcessFinished();
              }
            };
            listeners.put("gate.event.ProgressListener", pListener);

            ProcessingResource res = (ProcessingResource)target;
            try {
              AbstractResource.setResourceListeners(res, listeners);
            }
            catch(Exception e) {
              e.printStackTrace(Err.getPrintWriter());
            }
            // show the progress indicator
            fireProgressChanged(0);
            // the actual reinitialisation
            res.reInit();
            try {
              AbstractResource.removeResourceListeners(res, listeners);
            }
            catch(Exception e) {
              e.printStackTrace(Err.getPrintWriter());
            }
            long endTime = System.currentTimeMillis();
            fireStatusChanged(target.getName()
                    + " reinitialised in "
                    + NumberFormat.getInstance().format(
                            (double)(endTime - startTime) / 1000) + " seconds");
            fireProcessFinished();
          }
          catch(ResourceInstantiationException rie) {
            fireStatusChanged("reinitialisation failed");
            rie.printStackTrace(Err.getPrintWriter());
            JOptionPane.showMessageDialog(getLargeView(), "Reload failed!\n "
                    + "See \"Messages\" tab for details!", "GATE",
                    JOptionPane.ERROR_MESSAGE);
            fireProcessFinished();
          }
        }// public void run()
      };
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
              runnable, "DefaultResourceHandle1");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// public void actionPerformed(ActionEvent e)

  }// class ReloadAction

  class PopulateCorpusAction extends AbstractAction {
    PopulateCorpusAction() {
      super("Populate");
      putValue(SHORT_DESCRIPTION,
              "Fills this corpus with documents from a directory");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable() {
        public void run() {
          corpusFiller.setExtensions(new ArrayList());
          corpusFiller.setEncoding("");
          boolean answer = OkCancelDialog.showDialog(getLargeView(),
                  corpusFiller, "Select a directory and allowed extensions");
          if(answer) {
            long startTime = System.currentTimeMillis();
            URL url = null;
            try {
              url = new URL(corpusFiller.getUrlString());
              java.util.List extensions = corpusFiller.getExtensions();
              ExtensionFileFilter filter = null;
              if(extensions == null || extensions.isEmpty())
                filter = null;
              else {
                filter = new ExtensionFileFilter();
                Iterator extIter = corpusFiller.getExtensions().iterator();
                while(extIter.hasNext()) {
                  filter.addExtension((String)extIter.next());
                }
              }
              ((Corpus)target).populate(url, filter,
                      corpusFiller.getEncoding(), corpusFiller
                              .isRecurseDirectories());
              long endTime = System.currentTimeMillis();
              fireStatusChanged("Corpus populated in "
                      + NumberFormat.getInstance().format(
                              (double)(endTime - startTime) / 1000)
                      + " seconds!");

            }
            catch(MalformedURLException mue) {
              JOptionPane.showMessageDialog(getLargeView(), "Invalid URL!\n "
                      + "See \"Messages\" tab for details!", "GATE",
                      JOptionPane.ERROR_MESSAGE);
              mue.printStackTrace(Err.getPrintWriter());
            }
            catch(IOException ioe) {
              JOptionPane.showMessageDialog(getLargeView(), "I/O error!\n "
                      + "See \"Messages\" tab for details!", "GATE",
                      JOptionPane.ERROR_MESSAGE);
              ioe.printStackTrace(Err.getPrintWriter());
            }
            catch(ResourceInstantiationException rie) {
              JOptionPane.showMessageDialog(getLargeView(),
                      "Could not create document!\n "
                              + "See \"Messages\" tab for details!", "GATE",
                      JOptionPane.ERROR_MESSAGE);
              rie.printStackTrace(Err.getPrintWriter());
            }
          }
        }
      };
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
              runnable);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  class CreateIndexAction1 extends AbstractAction {
    CreateIndexAction1() {
      super("Create Index");
      putValue(SHORT_DESCRIPTION, "Create index with documents from a corpus");
    }

    public void actionPerformed(ActionEvent e) {
      CreateIndexDialog cid = null;
      if(getWindow() instanceof Frame) {
        cid = new CreateIndexDialog((Frame)getWindow(), (IndexedCorpus)target);
      }
      if(getWindow() instanceof Dialog) {
        cid = new CreateIndexDialog((Dialog)getWindow(), (IndexedCorpus)target);
      }
      cid.setVisible(true);
    }
  }

  class CreateIndexAction extends AbstractAction {
    CreateIndexAction() {
      super("Index corpus");
      putValue(SHORT_DESCRIPTION, "Create index with documents from the corpus");
      createIndexGui = new CreateIndexGUI();
    }

    public void actionPerformed(ActionEvent e) {
      boolean ok = OkCancelDialog.showDialog(largeView, createIndexGui,
              "Index \"" + target.getName() + "\" corpus");
      if(ok) {
        DefaultIndexDefinition did = new DefaultIndexDefinition();
        IREngine engine = createIndexGui.getIREngine();
        did.setIrEngineClassName(engine.getClass().getName());

        did.setIndexLocation(createIndexGui.getIndexLocation().toString());

        // add the content if wanted
        if(createIndexGui.getUseDocumentContent()) {
          did.addIndexField(new IndexField("body", new DocumentContentReader(),
                  false));
        }
        // add all the features
        Iterator featIter = createIndexGui.getFeaturesList().iterator();
        while(featIter.hasNext()) {
          String featureName = (String)featIter.next();
          did.addIndexField(new IndexField(featureName, new FeatureReader(
                  featureName), false));
        }

        ((IndexedCorpus)target).setIndexDefinition(did);

        Thread thread = new Thread(new Runnable() {
          public void run() {
            try {
              fireProgressChanged(1);
              fireStatusChanged("Indexing corpus...");
              long start = System.currentTimeMillis();
              ((IndexedCorpus)target).getIndexManager().deleteIndex();
              fireProgressChanged(10);
              ((IndexedCorpus)target).getIndexManager().createIndex();
              fireProgressChanged(100);
              fireProcessFinished();
              fireStatusChanged("Corpus indexed in "
                      + NumberFormat
                              .getInstance()
                              .format(
                                      (double)(System.currentTimeMillis() - start) / 1000)
                      + " seconds");
            }
            catch(IndexException ie) {
              JOptionPane.showMessageDialog(getLargeView() != null
                      ? getLargeView()
                      : getSmallView(), "Could not create index!\n "
                      + "See \"Messages\" tab for details!", "GATE",
                      JOptionPane.ERROR_MESSAGE);
              ie.printStackTrace(Err.getPrintWriter());
            }
            finally {
              fireProcessFinished();
            }
          }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
      }
    }

    CreateIndexGUI createIndexGui;
  }

  class OptimizeIndexAction extends AbstractAction {
    OptimizeIndexAction() {
      super("Optimize Index");
      putValue(SHORT_DESCRIPTION, "Optimize existing index");
    }

    public boolean isEnabled() {
      return ((IndexedCorpus)target).getIndexDefinition() != null;
    }

    public void actionPerformed(ActionEvent e) {
      IndexedCorpus ic = (IndexedCorpus)target;
      Thread thread = new Thread(new Runnable() {
        public void run() {
          try {
            fireProgressChanged(1);
            fireStatusChanged("Optimising index...");
            long start = System.currentTimeMillis();
            ((IndexedCorpus)target).getIndexManager().optimizeIndex();
            fireStatusChanged("Index optimised in "
                    + NumberFormat
                            .getInstance()
                            .format(
                                    (double)(System.currentTimeMillis() - start) / 1000)
                    + " seconds");
            fireProcessFinished();
          }
          catch(IndexException ie) {
            JOptionPane.showMessageDialog(getLargeView() != null
                    ? getLargeView()
                    : getSmallView(), "Errors during optimisation!", "GATE",
                    JOptionPane.PLAIN_MESSAGE);
            ie.printStackTrace(Err.getPrintWriter());
          }
          finally {
            fireProcessFinished();
          }
        }
      });
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  class DeleteIndexAction extends AbstractAction {
    DeleteIndexAction() {
      super("Delete Index");
      putValue(SHORT_DESCRIPTION, "Delete existing index");
    }

    public boolean isEnabled() {
      return ((IndexedCorpus)target).getIndexDefinition() != null;
    }

    public void actionPerformed(ActionEvent e) {
      int answer = JOptionPane.showOptionDialog(getLargeView() != null
              ? getLargeView()
              : getSmallView(), "Do you want to delete index?", "Gate",
              JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
              null, null);
      if(answer == JOptionPane.YES_OPTION) {
        try {
          IndexedCorpus ic = (IndexedCorpus)target;
          if(ic.getIndexManager() != null) {
            ic.getIndexManager().deleteIndex();
            ic.getFeatures().remove(
                    GateConstants.CORPUS_INDEX_DEFINITION_FEATURE_KEY);
          }
          else {
            JOptionPane.showMessageDialog(getLargeView() != null
                    ? getLargeView()
                    : getSmallView(), "There is no index to delete!", "GATE",
                    JOptionPane.PLAIN_MESSAGE);
          }
        }
        catch(gate.creole.ir.IndexException ie) {
          ie.printStackTrace();
        }
      }
    }
  }
  
  class CreateCorpusForDocAction extends AbstractAction {
    public CreateCorpusForDocAction() {
      super("New corpus with this document");
    }
    
    public void actionPerformed(ActionEvent e) {
      try {
        Corpus corpus = Factory.newCorpus("Corpus for " + target.getName());
        corpus.add(target);
      }
      catch(ResourceInstantiationException rie) {
        Err.println("Exception creating corpus");
        rie.printStackTrace(Err.getPrintWriter());
      }
    }
  }

  /**
   * Releases the memory, removes the listeners, cleans up. Will get
   * called when the target resource is unloaded from the system
   */
  public void cleanup() {
    // delete all the VRs that were created
    if(largeView != null) {
      if(largeView instanceof VisualResource) {
        // we only had a view so no tabbed pane was used
        Factory.deleteResource((VisualResource)largeView);
      }
      else {
        Component vrs[] = ((JTabbedPane)largeView).getComponents();
        for(int i = 0; i < vrs.length; i++) {
          if(vrs[i] instanceof VisualResource) {
            Factory.deleteResource((VisualResource)vrs[i]);
          }
        }
      }
    }

    if(smallView != null) {
      if(smallView instanceof VisualResource) {
        // we only had a view so no tabbed pane was used
        Factory.deleteResource((VisualResource)smallView);
      }
      else {
        Component vrs[] = ((JTabbedPane)smallView).getComponents();
        for(int i = 0; i < vrs.length; i++) {
          if(vrs[i] instanceof VisualResource) {
            Factory.deleteResource((VisualResource)vrs[i]);
          }
        }
      }
    }

    Gate.getCreoleRegister().removeCreoleListener(this);
    target = null;
  }

  class ProxyStatusListener implements StatusListener {
    public void statusChanged(String text) {
      fireStatusChanged(text);
    }
  }

  protected void fireProgressChanged(int e) {
    if(progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for(int i = 0; i < count; i++) {
        ((ProgressListener)listeners.elementAt(i)).progressChanged(e);
      }
    }
  }// protected void fireProgressChanged(int e)

  protected void fireProcessFinished() {
    if(progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for(int i = 0; i < count; i++) {
        ((ProgressListener)listeners.elementAt(i)).processFinished();
      }
    }
  }// protected void fireProcessFinished()

  public synchronized void removeStatusListener(StatusListener l) {
    if(statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector)statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }// public synchronized void removeStatusListener(StatusListener l)

  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null
            ? new Vector(2)
            : (Vector)statusListeners.clone();
    if(!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }// public synchronized void addStatusListener(StatusListener l)

  protected void fireStatusChanged(String e) {
    if(statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for(int i = 0; i < count; i++) {
        ((StatusListener)listeners.elementAt(i)).statusChanged(e);
      }
    }
  }

  public void statusChanged(String e) {
    fireStatusChanged(e);
  }

  public void progressChanged(int e) {
    fireProgressChanged(e);
  }

  public void processFinished() {
    fireProcessFinished();
  }

  public Window getWindow() {
    return window;
  }

  public void resourceLoaded(CreoleEvent e) {
  }

  public void resourceUnloaded(CreoleEvent e) {
  }

  public void resourceRenamed(Resource resource, String oldName, String newName) {
    if(target == resource) title = target.getName();
  }

  public void datastoreOpened(CreoleEvent e) {
  }

  public void datastoreCreated(CreoleEvent e) {
  }

  public void datastoreClosed(CreoleEvent e) {
    if(getTarget() == e.getDatastore()) cleanup();
  }
}// class DefaultResourceHandle
