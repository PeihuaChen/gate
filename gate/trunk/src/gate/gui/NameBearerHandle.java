/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
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

import javax.swing.*;
import java.util.*;
import java.net.*;
import java.awt.Component;
import java.awt.Window;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.event.*;
import java.text.NumberFormat;
import java.io.*;
import javax.swing.filechooser.FileFilter;

import gate.*;
import gate.util.*;
import gate.swing.*;
import gate.creole.*;
import gate.creole.ir.*;
import gate.persist.*;
import gate.event.*;
import gate.security.*;
import gate.security.SecurityException;

/**
 * Class used to store the GUI information about an open entity (resource,
 * controller, datastore).
 * Such information will include icon to be used for tree components,
 * popup menu for right click events, large and small views, etc.
 */
public class NameBearerHandle implements Handle,
                                         StatusListener,
                                         ProgressListener, CreoleListener {

  public NameBearerHandle(NameBearer target, Window window) {
    this.target = target;
    this.window = window;
    sListenerProxy = new ProxyStatusListener();
    String iconName = null;
    if(target instanceof Resource){
      rData = (ResourceData)Gate.getCreoleRegister().
                                              get(target.getClass().getName());
      if(rData != null){
        iconName = rData.getIcon();
        if(iconName == null){
          if(target instanceof LanguageResource) iconName = "lr.gif";
          else if(target instanceof ProcessingResource) iconName = "pr.gif";
          else if(target instanceof Controller) iconName = "controller.gif";
        }
        tooltipText = "<HTML> <b>" + rData.getComment() + "</b><br>(<i>" +
                      rData.getClassName() + "</i>)</HTML>";
      } else {
        this.icon = MainFrame.getIcon("lr.gif");
      }
    }else if(target instanceof DataStore){
      iconName = ((DataStore)target).getIconName();
      tooltipText = ((DataStore)target).getComment();
    }

    popup = null;
    title = (String)target.getName();
    this.icon = MainFrame.getIcon(iconName);

    Gate.getCreoleRegister().addCreoleListener(this);
    buildViews();
    // Add the CTRL +F4 key & action combination to the resource
    JComponent largeView = this.getLargeView();
    if (largeView != null){
      largeView.getActionMap().put("Close resource",
                        new CloseAction());
      if (target instanceof gate.corpora.DocumentImpl){
        largeView.getActionMap().put("Save As XML", new SaveAsXmlAction());
      }// End if
    }// End if
  }//public DefaultResourceHandle(FeatureBearer res)

  public Icon getIcon(){
    return icon;
  }

  public void setIcon(Icon icon){
    this.icon = icon;
  }

  public String getTitle(){
    return title;
  }

  public void setTitle(String newTitle){
    this.title = newTitle;
  }

  /**
   * Returns a GUI component to be used as a small viewer/editor, e.g. below
   * the main tree in the Gate GUI for the selected resource
   */
  public JComponent getSmallView() {
    return smallView;
  }

  /**
   * Returns the large view for this resource. This view will go into the main
   * display area.
   */
  public JComponent getLargeView() {
    return largeView;
  }

  public JPopupMenu getPopup() {
    return popup;
  }

  public void setPopup(JPopupMenu popup) {
    this.popup = popup;
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

  public Action getCloseAction(){
    return new CloseAction();
  }

  /** Fill Protege save, save as and save in format actions */
  private void fillProtegeActions(JPopupMenu popup) {
    Action action;

    popup.addSeparator();

    action = new edu.stanford.smi.protege.action.SaveProject();
    action.putValue(action.NAME, "Save Protege");
    action.putValue(action.SHORT_DESCRIPTION, "Save protege project");
    // Add Save Protege action
    popup.add(action);

    action = new edu.stanford.smi.protege.action.SaveAsProject();
    action.putValue(action.NAME, "Save Protege As...");
    action.putValue(action.SHORT_DESCRIPTION, "Save protege project as");
    // Add Save as... Protege action
    popup.add(action);

    action = new edu.stanford.smi.protege.action.ChangeProjectStorageFormat();
    // Add Save in format... Protege action
    popup.add(action);

    popup.addSeparator();
    action = new edu.stanford.smi.protege.action.BuildProject();
    // Add Import... Protege action
    popup.add(action);
  } // fillProtegeActions(gate.gui.ProtegeWrapper protege)

  protected void buildViews() {
    //build the popup
    popup = new JPopupMenu();
    XJMenuItem closeItem = new XJMenuItem(new CloseAction(), sListenerProxy);
    closeItem.setAccelerator(KeyStroke.getKeyStroke(
                                KeyEvent.VK_F4, ActionEvent.CTRL_MASK));
    popup.add(closeItem);

    if(target instanceof ProcessingResource){
      popup.addSeparator();
      popup.add(new XJMenuItem(new ReloadAction(), sListenerProxy));
    }else if(target instanceof LanguageResource) {
      //Language Resources
      popup.addSeparator();
      popup.add(new XJMenuItem(new SaveAction(), sListenerProxy));
      popup.add(new XJMenuItem(new SaveToAction(), sListenerProxy));
      if(target instanceof gate.corpora.DocumentImpl){
        XJMenuItem saveAsXmlItem =
                         new XJMenuItem(new SaveAsXmlAction(), sListenerProxy);
        saveAsXmlItem.setAccelerator(KeyStroke.getKeyStroke(
                                        KeyEvent.VK_X, ActionEvent.CTRL_MASK));

        popup.add(saveAsXmlItem);
        XJMenuItem savePreserveFormatItem =
                         new XJMenuItem(new DumpPreserveFormatAction(),
                                        sListenerProxy);
        popup.add(savePreserveFormatItem);
      }else if(target instanceof Corpus){
        popup.addSeparator();
        corpusFiller = new CorpusFillerComponent();
        popup.add(new XJMenuItem(new PopulateCorpusAction(), sListenerProxy));
        popup.addSeparator();
        popup.add(new XJMenuItem(new SaveCorpusAsXmlAction(), sListenerProxy));
        if (target instanceof IndexedCorpus){
          popup.addSeparator();
          popup.add(new XJMenuItem(new CreateIndexAction(), sListenerProxy));
          popup.add(new XJMenuItem(new OptimizeIndexAction(), sListenerProxy));
          popup.add(new XJMenuItem(new DeleteIndexAction(), sListenerProxy));
        }
      }
      if (target instanceof gate.creole.ProtegeProjectName){
        fillProtegeActions(popup);
      }// End if
    }else if(target instanceof Controller){
      //Applications
      popup.addSeparator();
      popup.add(new XJMenuItem(new DumpToFileAction(), sListenerProxy));
    }

    fireStatusChanged("Building views...");

    //build the large views
    List largeViewNames = Gate.getCreoleRegister().
                          getLargeVRsForResource(target.getClass().getName());
    if(largeViewNames != null && !largeViewNames.isEmpty()){
      largeView = new JTabbedPane(JTabbedPane.BOTTOM);
      Iterator classNameIter = largeViewNames.iterator();
      while(classNameIter.hasNext()){
        try{
          String className = (String)classNameIter.next();
          ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                                  get(className);
          FeatureMap params = Factory.newFeatureMap();
          FeatureMap features = Factory.newFeatureMap();
          Gate.setHiddenAttribute(features, true);
          VisualResource view = (VisualResource)
                                Factory.createResource(className,
                                                       params,
                                                       features);
          view.setTarget(target);
          view.setHandle(this);
          ((JTabbedPane)largeView).add((Component)view, rData.getName());
        }catch(ResourceInstantiationException rie){
          rie.printStackTrace(Err.getPrintWriter());
        }
      }
      if(largeViewNames.size() == 1){
        largeView = (JComponent)((JTabbedPane)largeView).getComponentAt(0);
      }else{
        ((JTabbedPane)largeView).setSelectedIndex(0);
      }
    }

    //build the small views
    List smallViewNames = Gate.getCreoleRegister().
                          getSmallVRsForResource(target.getClass().getName());
    if(smallViewNames != null && !smallViewNames.isEmpty()){
      smallView = new JTabbedPane(JTabbedPane.BOTTOM);
      Iterator classNameIter = smallViewNames.iterator();
      while(classNameIter.hasNext()){
        try{
          String className = (String)classNameIter.next();
          ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                                  get(className);
          FeatureMap params = Factory.newFeatureMap();
          FeatureMap features = Factory.newFeatureMap();
          Gate.setHiddenAttribute(features, true);
          VisualResource view = (VisualResource)
                                Factory.createResource(className,
                                                       params,
                                                       features);
          view.setTarget(target);
          view.setHandle(this);
          ((JTabbedPane)smallView).add((Component)view, rData.getName());
        }catch(ResourceInstantiationException rie){
          rie.printStackTrace(Err.getPrintWriter());
        }
      }
      if(smallViewNames.size() == 1){
        smallView = (JComponent)((JTabbedPane)smallView).getComponentAt(0);
      }else{
        ((JTabbedPane)smallView).setSelectedIndex(0);
      }
    }
    fireStatusChanged("Views built!");
  }//protected void buildViews

  public String toString(){ return title;}

  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }//public synchronized void removeProgressListener(ProgressListener l)

  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null ? new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }//public synchronized void addProgressListener(ProgressListener l)

  JPopupMenu popup;
  String title;
  String tooltipText;
  NameBearer target;
  /**
   * The top level GUI component this hadle belongs to.
   */
  Window window;
  ResourceData rData;
  Icon icon;
  JComponent smallView;
  JComponent largeView;

  /**
   * Component used to select the options for corpus populating
   */
  CorpusFillerComponent corpusFiller;

  StatusListener sListenerProxy;

//  File currentDir = null;
  private transient Vector progressListeners;
  private transient Vector statusListeners;

  class CloseAction extends AbstractAction {
    public CloseAction() {
      super("Close");
      putValue(SHORT_DESCRIPTION, "Removes this resource from the system");
    }

    public void actionPerformed(ActionEvent e){
      if(target instanceof Resource){
        Factory.deleteResource((Resource)target);
      }else if(target instanceof DataStore){
        try{
          ((DataStore)target).close();
        } catch(PersistenceException pe){
          JOptionPane.showMessageDialog(largeView != null ?
                                                     largeView : smallView,
                                        "Error!\n" + pe.toString(),
                                        "Gate", JOptionPane.ERROR_MESSAGE);
        }
      }

      statusListeners.clear();
      progressListeners.clear();
//      //delete the viewers
//      if(largeView instanceof VisualResource){
//        Factory.deleteResource((VisualResource)largeView);
//      }else if(largeView instanceof JTabbedPane){
//        Component[] comps = ((JTabbedPane)largeView).getComponents();
//        for(int i = 0; i < comps.length; i++){
//          if(comps[i] instanceof VisualResource)
//            Factory.deleteResource((VisualResource)comps[i]);
//        }
//      }
//      if(smallView instanceof VisualResource){
//        Factory.deleteResource((VisualResource)smallView);
//      }else if(smallView instanceof JTabbedPane){
//        Component[] comps = ((JTabbedPane)smallView).getComponents();
//        for(int i = 0; i < comps.length; i++){
//          if(comps[i] instanceof VisualResource)
//            Factory.deleteResource((VisualResource)comps[i]);
//        }
//      }
//
    }//public void actionPerformed(ActionEvent e)
  }//class CloseAction

  /**
   * Used to save a document as XML
   */
  class SaveAsXmlAction extends AbstractAction {
    public SaveAsXmlAction(){
      super("Save As Xml...");
      putValue(SHORT_DESCRIPTION, "Saves this resource in XML");
    }// SaveAsXmlAction()

    public void actionPerformed(ActionEvent e) {
      Runnable runableAction = new Runnable(){
        public void run(){
          JFileChooser fileChooser = MainFrame.getFileChooser();
          File selectedFile = null;

          List filters = Arrays.asList(fileChooser.getChoosableFileFilters());
          Iterator filtersIter = filters.iterator();
          FileFilter filter = null;
          if(filtersIter.hasNext()){
            filter = (FileFilter)filtersIter.next();
            while(filtersIter.hasNext() &&
                  filter.getDescription().indexOf("XML") == -1){
              filter = (FileFilter)filtersIter.next();
            }
          }
          if(filter == null || filter.getDescription().indexOf("XML") == -1){
            //no suitable filter found, create a new one
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

          int res = (getLargeView() != null) ?
                                  fileChooser.showDialog(getLargeView(), "Save"):
                    (getSmallView() != null) ?
                                  fileChooser.showDialog(getSmallView(), "Save") :
                                              fileChooser.showDialog(null, "Save");
          if(res == JFileChooser.APPROVE_OPTION){
            selectedFile = fileChooser.getSelectedFile();
            File currentDir = fileChooser.getCurrentDirectory();
            if(selectedFile == null) return;
            long start = System.currentTimeMillis();
            NameBearerHandle.this.statusChanged("Saving as XML to " +
             selectedFile.toString() + "...");
            try{
              MainFrame.lockGUI("Saving...");
              // Prepare to write into the xmlFile using UTF-8 encoding
              OutputStreamWriter writer = new OutputStreamWriter(
                              new FileOutputStream(selectedFile),"UTF-8");

              // Write (test the toXml() method)
              // This Action is added only when a gate.Document is created.
              // So, is for sure that the resource is a gate.Document
              writer.write(((gate.Document)target).toXml());
              writer.flush();
              writer.close();
            } catch (Exception ex){
              ex.printStackTrace(Out.getPrintWriter());
            }finally{
              MainFrame.unlockGUI();
            }
            long time = System.currentTimeMillis() - start;
            NameBearerHandle.this.statusChanged("Finished saving as xml into "+
             " the file: " + selectedFile.toString() +
             " in " + ((double)time) / 1000 + " s");
          }// End if
        }// End run()
      };// End Runnable
      Thread thread = new Thread(runableAction, "");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }// actionPerformed()
  }// SaveAsXmlAction

  /**
   * The action that is fired when the user wants to dump annotations
   * preserving the original document format.
   */
  protected class DumpPreserveFormatAction extends AbstractAction{
//    private Set annotationsToDump = null;

    public DumpPreserveFormatAction(){
      super("Save preserving document format");
    }


    /** This method takes care of how the dumping is done*/
    public void actionPerformed(ActionEvent e){
      Runnable runableAction = new Runnable(){
        public void run(){
          JFileChooser fileChooser = MainFrame.getFileChooser();
          File selectedFile = null;

          fileChooser.setMultiSelectionEnabled(false);
          fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          fileChooser.setDialogTitle("Select document to save ...");
          fileChooser.setSelectedFiles(null);

          int res = (getLargeView() != null) ?
                                  fileChooser.showDialog(getLargeView(), "Save"):
                    (getSmallView() != null) ?
                                  fileChooser.showDialog(getSmallView(), "Save") :
                                              fileChooser.showDialog(null, "Save");
          if(res == JFileChooser.APPROVE_OPTION){
            selectedFile = fileChooser.getSelectedFile();
            fileChooser.setCurrentDirectory(fileChooser.getCurrentDirectory());
            if(selectedFile == null) return;
            if (NameBearerHandle.this!= null)
              NameBearerHandle.this.statusChanged("Please wait while dumping annotations"+
              "in the original format to " + selectedFile.toString() + " ...");
            // This method construct a set with all annotations that need to be
            // dupmped as Xml. If the set is null then only the original markups
            // are dumped.
            Set annotationsToDump = null;
            //find the shown document editor. If none, just dump the original
            //markup annotations, i.e., leave the annotationsToDump null
            if (largeView instanceof JTabbedPane) {
              Component shownComponent =
                ((JTabbedPane) largeView).getSelectedComponent();
              if (shownComponent instanceof DocumentEditor) {
                //so we only get annotations for dumping if they are shown in the
                //table of the document editor, which is currently in front
                //of the user
                annotationsToDump =
                  ((DocumentEditor) shownComponent).getDisplayedAnnotations();
              }//if we have a document editor
            }//if tabbed pane
            try{
              // Prepare to write into the xmlFile using UTF-8 encoding
              OutputStreamWriter writer = new OutputStreamWriter(
                                    new FileOutputStream(selectedFile),"UTF-8");

              //determine if the features need to be saved first
              Boolean featuresSaved =
                  Gate.getUserConfig().getBoolean(
                    GateConstants.SAVE_FEATURES_WHEN_PRESERVING_FORMAT);
              boolean saveFeatures = true;
              if (featuresSaved != null)
                saveFeatures = featuresSaved.booleanValue();
              // Write with the toXml() method
              writer.write(
                ((gate.Document)target).toXml(annotationsToDump, saveFeatures));
              writer.flush();
              writer.close();
            } catch (Exception ex){
              ex.printStackTrace(Out.getPrintWriter());
            }// End try
            if (NameBearerHandle.this!= null)
              NameBearerHandle.this.statusChanged("Finished dumping into the "+
              "file : " + selectedFile.toString());
          }// End if
        }// End run()
      };// End Runnable
      Thread thread = new Thread(runableAction, "");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }//public void actionPerformed(ActionEvent e)

  }//class DumpPreserveFormatAction


  /**
   * Saves a corpus as a set of xml files in a directory.
   */
  class SaveCorpusAsXmlAction extends AbstractAction {
    public SaveCorpusAsXmlAction(){
      super("Save As Xml...");
      putValue(SHORT_DESCRIPTION, "Saves this corpus in XML");
    }// SaveAsXmlAction()

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable(){
        public void run(){
          try{
            //we need a directory
            JFileChooser filer = MainFrame.getFileChooser();
            filer.setDialogTitle(
                "Select the directory that will contain the corpus");
            filer.setFileSelectionMode(filer.DIRECTORIES_ONLY);
            filer.setFileFilter(filer.getAcceptAllFileFilter());

            if (filer.showDialog(getLargeView() != null ?
                                     getLargeView() :
                                     getSmallView(),
                                     "Select") == filer.APPROVE_OPTION){

              File dir = filer.getSelectedFile();
              //create the top directory if needed
              if(!dir.exists()){
                if(!dir.mkdirs()){
                  JOptionPane.showMessageDialog(
                    largeView != null ?largeView : smallView,
                    "Could not create top directory!",
                    "Gate", JOptionPane.ERROR_MESSAGE);
                  return;
                }
              }

              MainFrame.lockGUI("Saving...");

              //iterate through all the docs and save each of them as xml
              Corpus corpus = (Corpus)target;
              Iterator docIter = corpus.iterator();
              boolean overwriteAll = false;
              int docCnt = corpus.size();
              int currentDocIndex = 0;
              while(docIter.hasNext()){
                Document currentDoc = (Document)docIter.next();
                URL sourceURL = currentDoc.getSourceUrl();
                String fileName = null;
                if(sourceURL != null){
                  fileName = sourceURL.getFile();
                  fileName = Files.getLastPathComponent(fileName);
                }
                if(fileName == null || fileName.length() == 0){
                  fileName = currentDoc.getName();
                }
                if(!fileName.toLowerCase().endsWith(".xml")) fileName += ".xml";
                File docFile = null;
                boolean nameOK = false;
                do{
                  docFile = new File(dir, fileName);
                  if(docFile.exists() && !overwriteAll){
                    //ask the user if we can ovewrite the file
                    Object[] options = new Object[] {"Yes", "All",
                                                     "No", "Cancel"};
                    MainFrame.unlockGUI();
                    int answer = JOptionPane.showOptionDialog(
                      largeView != null ? largeView : smallView,
                      "File " + docFile.getName() + " already exists!\n" +
                      "Overwrite?" ,
                      "Gate", JOptionPane.DEFAULT_OPTION,
                      JOptionPane.WARNING_MESSAGE, null, options, options[2]);
                    MainFrame.lockGUI("Saving...");
                    switch(answer){
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
                        //user said NO, allow them to provide an alternative name;
                        MainFrame.unlockGUI();
                        fileName = (String)JOptionPane.showInputDialog(
                            largeView != null ? largeView : smallView,
                            "Please provide an alternative file name",
                            "Gate", JOptionPane.QUESTION_MESSAGE,
                            null, null, fileName);
                        if(fileName == null){
                          fireProcessFinished();
                          return;
                        }
                        MainFrame.lockGUI("Saving");
                        break;
                      }
                      case 3: {
                        //user gave up; return
                        fireProcessFinished();
                        return;
                      }
                    }

                  }else{
                    nameOK = true;
                  }
                }while(!nameOK);
                //save the file
                try{
                  OutputStreamWriter writer = new OutputStreamWriter(
                                new FileOutputStream(docFile),"UTF-8");
                  writer.write(currentDoc.toXml());
                  writer.flush();
                  writer.close();
                }catch(IOException ioe){
                  MainFrame.unlockGUI();
                  JOptionPane.showMessageDialog(
                    largeView != null ? largeView : smallView,
                    "Could not create write file:" +
                    ioe.toString(),
                    "Gate", JOptionPane.ERROR_MESSAGE);
                  ioe.printStackTrace(Err.getPrintWriter());
                  return;
                }

                fireStatusChanged(currentDoc.getName() + " saved");
                fireProgressChanged(100 * currentDocIndex++ / docCnt);
              }//while(docIter.hasNext())
              fireStatusChanged("Corpus saved");
              fireProcessFinished();
            }//select directory
          }finally{
            MainFrame.unlockGUI();
          }
        }//public void run(){
      };//Runnable runnable = new Runnable()
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable, "Corpus XML dumper");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();

    }//public void actionPerformed(ActionEvent e)
  }//class SaveCorpusAsXmlAction extends AbstractAction

  /**
   * Saves a corpus as a set of xml files in a directory.
   */
  class ReloadClassAction extends AbstractAction {
    public ReloadClassAction(){
      super("Reload resource class");
      putValue(SHORT_DESCRIPTION, "Reloads the java class for this resource");
    }// SaveAsXmlAction()

    public void actionPerformed(ActionEvent e) {
      int answer = JOptionPane.showOptionDialog(
                largeView != null ? largeView : smallView,
                "This is an advanced option!\n" +
                "You should not use this unless your name is Hamish.\n" +
                "Are you sure you want to do this?" ,
                "Gate", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE, null, null, null);
      if(answer == JOptionPane.OK_OPTION){
        try{
          String className = target.getClass().getName();
          Gate.getClassLoader().reloadClass(className);
          fireStatusChanged("Class " + className + " reloaded!");
        }catch(Exception ex){
          JOptionPane.showMessageDialog(largeView != null ?
                                        largeView : smallView,
                                        "Look what you've done: \n" +
                                        ex.toString() +
                                        "\nI told you not to do it...",
                                        "Gate", JOptionPane.ERROR_MESSAGE);
          ex.printStackTrace(Err.getPrintWriter());
        }
      }
    }
  }

  class SaveAction extends AbstractAction {
    public SaveAction(){
      super("Save");
      putValue(SHORT_DESCRIPTION, "Save back to the datastore");
    }
    public void actionPerformed(ActionEvent e){
      Runnable runnable = new Runnable(){
        public void run(){
          DataStore ds = ((LanguageResource)target).getDataStore();
          if(ds != null){
            try {
              MainFrame.lockGUI("Saving " + ((LanguageResource)target).getName());
              StatusListener sListener = (StatusListener)
                                         gate.gui.MainFrame.getListeners().
                                         get("gate.event.StatusListener");
              if(sListener != null) sListener.statusChanged(
                "Saving: " + ((LanguageResource)target).getName());
              double timeBefore = System.currentTimeMillis();
              ((LanguageResource)
                        target).getDataStore().sync((LanguageResource)target);
              double timeAfter = System.currentTimeMillis();
              if(sListener != null) sListener.statusChanged(
                ((LanguageResource)target).getName() + " saved in " +
                NumberFormat.getInstance().format((timeAfter-timeBefore)/1000)
                + " seconds");
            } catch(PersistenceException pe) {
              MainFrame.unlockGUI();
              JOptionPane.showMessageDialog(getLargeView(),
                                            "Save failed!\n " +
                                            pe.toString(),
                                            "Gate", JOptionPane.ERROR_MESSAGE);
            } catch(SecurityException se) {
              MainFrame.unlockGUI();
              JOptionPane.showMessageDialog(getLargeView(),
                                            "Save failed!\n " +
                                            se.toString(),
                                            "Gate", JOptionPane.ERROR_MESSAGE);
            }finally{
              MainFrame.unlockGUI();
            }
          } else {
            JOptionPane.showMessageDialog(getLargeView(),
                            "This resource has not been loaded from a datastore.\n"+
                             "Please use the \"Save to\" option!\n",
                             "Gate", JOptionPane.ERROR_MESSAGE);

          }
        }
      };
      new Thread(runnable).start();
    }//public void actionPerformed(ActionEvent e)
  }//class SaveAction

  class DumpToFileAction extends AbstractAction {
    public DumpToFileAction(){
      super("Save application state");
      putValue(SHORT_DESCRIPTION,
               "Saves the data needed to recreate this application");
    }

    public void actionPerformed(ActionEvent ae){
      JFileChooser fileChooser = MainFrame.getFileChooser();

      fileChooser.setDialogTitle("Select a file for this resource");
      fileChooser.setFileSelectionMode(fileChooser.FILES_AND_DIRECTORIES);
      if (fileChooser.showSaveDialog(largeView) ==
                                            fileChooser.APPROVE_OPTION){
        final File file = fileChooser.getSelectedFile();
          Runnable runnable = new Runnable(){
            public void run(){
              try{
                gate.util.persistence.PersistenceManager.
                                      saveObjectToFile((Resource)target, file);
              }catch(Exception e){
                JOptionPane.showMessageDialog(getLargeView(),
                                "Error!\n"+
                                 e.toString(),
                                 "Gate", JOptionPane.ERROR_MESSAGE);
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
    public SaveToAction(){
      super("Save to...");
      putValue(SHORT_DESCRIPTION, "Save this resource to a datastore");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable(){
        public void run(){
          try {
            DataStoreRegister dsReg = Gate.getDataStoreRegister();
            Map dsByName =new HashMap();
            Iterator dsIter = dsReg.iterator();
            while(dsIter.hasNext()){
              DataStore oneDS = (DataStore)dsIter.next();
              String name;
              if((name = (String)oneDS.getName()) != null){
              } else {
                name  = oneDS.getStorageUrl();
                try {
                  URL tempURL = new URL(name);
                  name = tempURL.getFile();
                } catch (java.net.MalformedURLException ex) {
                  throw new GateRuntimeException(
                            );
                }
              }
              dsByName.put(name, oneDS);
            }
            List dsNames = new ArrayList(dsByName.keySet());
            if(dsNames.isEmpty()){
              JOptionPane.showMessageDialog(getLargeView(),
                                            "There are no open datastores!\n " +
                                            "Please open a datastore first!",
                                            "Gate", JOptionPane.ERROR_MESSAGE);

            } else {
              Object answer = JOptionPane.showInputDialog(
                                  getLargeView(),
                                  "Select the datastore",
                                  "Gate", JOptionPane.QUESTION_MESSAGE,
                                  null, dsNames.toArray(),
                                  dsNames.get(0));
              if(answer == null) return;
              DataStore ds = (DataStore)dsByName.get(answer);
              if (ds == null){
                Err.prln("The datastore does not exists. Saving procedure" +
                                  " has FAILED! This should never happen again!");
                return;
              }// End if
              DataStore ownDS = ((LanguageResource)target).getDataStore();
              if(ds == ownDS){
                MainFrame.lockGUI("Saving " + ((LanguageResource)target).getName());

                StatusListener sListener = (StatusListener)
                                           gate.gui.MainFrame.getListeners().
                                           get("gate.event.StatusListener");
                if(sListener != null) sListener.statusChanged(
                  "Saving: " + ((LanguageResource)target).getName());
                double timeBefore = System.currentTimeMillis();
                ds.sync((LanguageResource)target);
                double timeAfter = System.currentTimeMillis();
                if(sListener != null) sListener.statusChanged(
                  ((LanguageResource)target).getName() + " saved in " +
                  NumberFormat.getInstance().format((timeAfter-timeBefore)/1000)
                  + " seconds");
              }else{
                FeatureMap securityData = (FeatureMap)
                             Gate.getDataStoreRegister().getSecurityData(ds);
                SecurityInfo si = null;
                //check whether the datastore supports security data
                //serial ones do not for example
                if (securityData != null) {
                  //first get the type of access from the user
                  if(!AccessRightsDialog.showDialog(window))
                    return;
                  int accessType = AccessRightsDialog.getSelectedMode();
                  if(accessType < 0)
                    return;
                  si = new SecurityInfo(accessType,
                                        (User) securityData.get("user"),
                                        (Group) securityData.get("group"));
                }//if security info
                StatusListener sListener = (StatusListener)
                                           gate.gui.MainFrame.getListeners().
                                           get("gate.event.StatusListener");
                MainFrame.lockGUI("Saving " + ((LanguageResource)target).getName());

                if(sListener != null) sListener.statusChanged(
                  "Saving: " + ((LanguageResource)target).getName());
                double timeBefore = System.currentTimeMillis();
                LanguageResource lr = ds.adopt((LanguageResource)target,si);
                ds.sync(lr);
                double timeAfter = System.currentTimeMillis();
                if(sListener != null) sListener.statusChanged(
                  ((LanguageResource)target).getName() + " saved in " +
                  NumberFormat.getInstance().format((timeAfter-timeBefore)/1000)
                  + " seconds");

                //check whether the new LR is different from the transient one and
                //if so, unload the transient LR, so the user realises
                //it is no longer valid. Don't do this in the adopt() code itself
                //because the batch code might wish to keep the transient
                //resource for some purpose.
                if (lr != target) {
                  Factory.deleteResource((LanguageResource)target);
                }
              }
            }
          } catch(PersistenceException pe) {
            MainFrame.unlockGUI();
            JOptionPane.showMessageDialog(getLargeView(),
                                          "Save failed!\n " +
                                          pe.toString(),
                                          "Gate", JOptionPane.ERROR_MESSAGE);
          }catch(gate.security.SecurityException se) {
            MainFrame.unlockGUI();
            JOptionPane.showMessageDialog(getLargeView(),
                                          "Save failed!\n " +
                                          se.toString(),
                                          "Gate", JOptionPane.ERROR_MESSAGE);
          }finally{
            MainFrame.unlockGUI();
          }

        }
      };
      new Thread(runnable).start();
    }
  }//class SaveToAction extends AbstractAction

  class ReloadAction extends AbstractAction {
    ReloadAction() {
      super("Reinitialise");
      putValue(SHORT_DESCRIPTION, "Reloads this resource");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable(){
        public void run(){
          if(!(target instanceof ProcessingResource)) return;
          try{
            long startTime = System.currentTimeMillis();
            fireStatusChanged("Reinitialising " +
                               target.getName());
            Map listeners = new HashMap();
            StatusListener sListener = new StatusListener(){
                                        public void statusChanged(String text){
                                          fireStatusChanged(text);
                                        }
                                       };
            listeners.put("gate.event.StatusListener", sListener);

            ProgressListener pListener =
                new ProgressListener(){
                  public void progressChanged(int value){
                    fireProgressChanged(value);
                  }
                  public void processFinished(){
                    fireProcessFinished();
                  }
                };
            listeners.put("gate.event.ProgressListener", pListener);

            ProcessingResource res = (ProcessingResource)target;
            try{
              AbstractResource.setResourceListeners(res, listeners);
            }catch (Exception e){
              e.printStackTrace(Err.getPrintWriter());
            }
            //show the progress indicator
            fireProgressChanged(0);
            //the actual reinitialisation
            res.reInit();
            try{
              AbstractResource.removeResourceListeners(res, listeners);
            }catch (Exception e){
              e.printStackTrace(Err.getPrintWriter());
            }
            long endTime = System.currentTimeMillis();
            fireStatusChanged(target.getName() +
                              " reinitialised in " +
                              NumberFormat.getInstance().format(
                              (double)(endTime - startTime) / 1000) + " seconds");
            fireProcessFinished();
          }catch(ResourceInstantiationException rie){
            fireStatusChanged("reinitialisation failed");
            rie.printStackTrace(Err.getPrintWriter());
            JOptionPane.showMessageDialog(getLargeView(),
                                          "Reload failed!\n " +
                                          "See \"Messages\" tab for details!",
                                          "Gate", JOptionPane.ERROR_MESSAGE);
            fireProcessFinished();
          }
        }//public void run()
      };
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable,
                                 "DefaultResourceHandle1");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }//public void actionPerformed(ActionEvent e)

  }//class ReloadAction

  class PopulateCorpusAction extends AbstractAction {
    PopulateCorpusAction() {
      super("Populate");
      putValue(SHORT_DESCRIPTION,
               "Fills this corpus with documents from a directory");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable(){
        public void run(){
          corpusFiller.setExtensions(new ArrayList());
          corpusFiller.setEncoding("");
          boolean answer = OkCancelDialog.showDialog(
                                  getLargeView(),
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
              ((Corpus)target).populate(url, filter,
                                        corpusFiller.getEncoding(),
                                        corpusFiller.isRecurseDirectories());
              fireStatusChanged("Corpus populated!");

            }catch(MalformedURLException mue){
              JOptionPane.showMessageDialog(getLargeView(),
                                            "Invalid URL!\n " +
                                            "See \"Messages\" tab for details!",
                                            "Gate", JOptionPane.ERROR_MESSAGE);
              mue.printStackTrace(Err.getPrintWriter());
            }catch(IOException ioe){
              JOptionPane.showMessageDialog(getLargeView(),
                                            "I/O error!\n " +
                                            "See \"Messages\" tab for details!",
                                            "Gate", JOptionPane.ERROR_MESSAGE);
              ioe.printStackTrace(Err.getPrintWriter());
            }catch(ResourceInstantiationException rie){
              JOptionPane.showMessageDialog(getLargeView(),
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
    }
  }

  class CreateIndexAction extends AbstractAction {
    CreateIndexAction() {
      super("Create Index");
      putValue(SHORT_DESCRIPTION,
               "Create index with documents from a corpus");
    }

    public void actionPerformed(ActionEvent e) {
      CreateIndexDialog cid = null;
      if (getWindow() instanceof Frame){
        cid = new CreateIndexDialog((Frame) getWindow(), (IndexedCorpus) target);
      }
      if (getWindow() instanceof Dialog){
        cid = new CreateIndexDialog((Dialog) getWindow(), (IndexedCorpus) target);
      }
      cid.show();
    }
  }

  class OptimizeIndexAction extends AbstractAction {
    OptimizeIndexAction() {
      super("Optimize Index");
      putValue(SHORT_DESCRIPTION,
               "Optimize existing index");
    }

    public void actionPerformed(ActionEvent e) {
      try {
        IndexedCorpus ic = (IndexedCorpus) target;
        if (ic.getIndexManager() != null){
          ic.getIndexManager().optimizeIndex();
        } else {
          JOptionPane.showMessageDialog(getLargeView() != null ?
                                     getLargeView() :
                                     getSmallView(),
                                     "There are not existing index!",
                                     "Index", JOptionPane.PLAIN_MESSAGE);
        }
      } catch (gate.creole.ir.IndexException ie) {
        ie.printStackTrace();
      }
    }
  }

  class DeleteIndexAction extends AbstractAction {
    DeleteIndexAction() {
      super("Delete Index");
      putValue(SHORT_DESCRIPTION,
               "Delete existing index");
    }

    public void actionPerformed(ActionEvent e) {
      int answer = JOptionPane.showOptionDialog(getLargeView() != null ?
                                     getLargeView() :
                                     getSmallView(), "Do you want to delete index?", "Index",
                                     JOptionPane.YES_NO_OPTION,
                                     JOptionPane.QUESTION_MESSAGE, null, null, null);
      if (answer == JOptionPane.YES_OPTION) {
        try {
          IndexedCorpus ic = (IndexedCorpus) target;
          if (ic.getIndexManager() != null){
            ic.getIndexManager().deleteIndex();
          } else {
            JOptionPane.showMessageDialog(getLargeView() != null ?
                                     getLargeView() :
                                     getSmallView(),
                                     "There are not existing index!",
                                     "Index", JOptionPane.PLAIN_MESSAGE);
          }
        } catch (gate.creole.ir.IndexException ie) {
          ie.printStackTrace();
        }
      }
    }
  }

  /**
   * Releases the memory, removes the listeners, cleans up.
   * Will get called when the target resource is unloaded from the system
   */
  protected void cleanup(){
    //delete all the VRs that were created
    if(largeView != null){
      if(largeView instanceof VisualResource){
        //we only had a view so no tabbed pane was used
        Factory.deleteResource((VisualResource)largeView);
      }else{
        Component vrs[] = ((JTabbedPane)largeView).getComponents();
        for(int i = 0; i < vrs.length; i++){
          if(vrs[i] instanceof VisualResource){
            Factory.deleteResource((VisualResource)vrs[i]);
          }
        }
      }
    }

    if(smallView != null){
      if(smallView instanceof VisualResource){
        //we only had a view so no tabbed pane was used
        Factory.deleteResource((VisualResource)smallView);
      }else{
        Component vrs[] = ((JTabbedPane)smallView).getComponents();
        for(int i = 0; i < vrs.length; i++){
          if(vrs[i] instanceof VisualResource){
            Factory.deleteResource((VisualResource)vrs[i]);
          }
        }
      }
    }

    Gate.getCreoleRegister().removeCreoleListener(this);
    popup = null;
    target = null;
  }

  class ProxyStatusListener implements StatusListener{
    public void statusChanged(String text){
      fireStatusChanged(text);
    }
  }

  protected void fireProgressChanged(int e) {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).progressChanged(e);
      }
    }
  }//protected void fireProgressChanged(int e)

  protected void fireProcessFinished() {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).processFinished();
      }
    }
  }//protected void fireProcessFinished()

  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }//public synchronized void removeStatusListener(StatusListener l)

  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null ? new Vector(2) : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }//public synchronized void addStatusListener(StatusListener l)

  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
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
    if(getTarget() == e.getResource()) cleanup();

  }

  public void resourceRenamed(Resource resource, String oldName,
                              String newName){
    if(target == resource) title = target.getName();
  }

  public void datastoreOpened(CreoleEvent e) {
  }

  public void datastoreCreated(CreoleEvent e) {
  }

  public void datastoreClosed(CreoleEvent e) {
    if(getTarget() == e.getDatastore()) cleanup();
  }
}//class DefaultResourceHandle
