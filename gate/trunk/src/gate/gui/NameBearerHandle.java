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
import java.awt.event.*;
import java.text.NumberFormat;
import java.io.*;
import javax.swing.filechooser.FileFilter;

import gate.*;
import gate.util.*;
import gate.swing.*;
import gate.creole.*;
import gate.persist.*;
import gate.event.*;

/**
 * Class used to store the GUI information about an open entity (resource,
 * controller, datatstore).
 * Such information will include icon to be used for tree components,
 * popup menu for right click events, large and small views, etc.
 */
public class NameBearerHandle implements Handle,
                                         StatusListener,
                                         ProgressListener {

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
        tooltipText = "Type : " + rData.getName();
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

      popup.addSeparator();
      popup.add(new XJMenuItem(new DumpToFileAction(), sListenerProxy));
    }

    //Language Resources
    if(target instanceof LanguageResource) {
      popup.addSeparator();
      popup.add(new XJMenuItem(new SaveAction(), sListenerProxy));
      popup.add(new XJMenuItem(new SaveToAction(), sListenerProxy));
      if(target instanceof gate.corpora.DocumentImpl){
        XJMenuItem saveAsXmlItem =
                         new XJMenuItem(new SaveAsXmlAction(), sListenerProxy);
        saveAsXmlItem.setAccelerator(KeyStroke.getKeyStroke(
                                        KeyEvent.VK_X, ActionEvent.CTRL_MASK));

        popup.add(saveAsXmlItem);
      }//End if
    }//if(resource instanceof LanguageResource)

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
        statusListeners.clear();
        progressListeners.clear();
        //delete the viewers
        if(largeView instanceof VisualResource){
          Factory.deleteResource((VisualResource)largeView);
        }else if(largeView instanceof JTabbedPane){
          Component[] comps = ((JTabbedPane)largeView).getComponents();
          for(int i = 0; i < comps.length; i++){
            if(comps[i] instanceof VisualResource)
              Factory.deleteResource((VisualResource)comps[i]);
          }
        }
        if(smallView instanceof VisualResource){
          Factory.deleteResource((VisualResource)smallView);
        }else if(smallView instanceof JTabbedPane){
          Component[] comps = ((JTabbedPane)smallView).getComponents();
          for(int i = 0; i < comps.length; i++){
            if(comps[i] instanceof VisualResource)
              Factory.deleteResource((VisualResource)comps[i]);
          }
        }
      }
    }//public void actionPerformed(ActionEvent e)
  }//class CloseAction

  class SaveAsXmlAction extends AbstractAction {
    public SaveAsXmlAction(){
      super("Save As Xml...");
      putValue(SHORT_DESCRIPTION, "Saves this resource in XML");
    }// SaveAsXmlAction()

    public void actionPerformed(ActionEvent e) {

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
        try{
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
        }
      }// End if
    }// actionPerformed()
  }// SaveAsXmlAction

  class SaveAction extends AbstractAction {
    public SaveAction(){
      super("Save");
      putValue(SHORT_DESCRIPTION, "Save back to the datastore");
    }
    public void actionPerformed(ActionEvent e){
      DataStore ds = ((LanguageResource)target).getDataStore();
      if(ds != null){
        try {
          ((LanguageResource)
                    target).getDataStore().sync((LanguageResource)target);
        } catch(PersistenceException pe) {
          JOptionPane.showMessageDialog(getLargeView(),
                                        "Save failed!\n " +
                                        pe.toString(),
                                        "Gate", JOptionPane.ERROR_MESSAGE);
        }
      } else {
        JOptionPane.showMessageDialog(getLargeView(),
                        "This resource has not been loaded from a datastore.\n"+
                         "Please use the \"Save to\" option!\n",
                         "Gate", JOptionPane.ERROR_MESSAGE);

      }
    }//public void actionPerformed(ActionEvent e)
  }//class SaveAction

  class DumpToFileAction extends AbstractAction {
    public DumpToFileAction(){
      super("Save to file");
      putValue(SHORT_DESCRIPTION, "Save resource into a");
    }

    public void actionPerformed(ActionEvent ae){
      JFileChooser fileChooser = MainFrame.getFileChooser();

      fileChooser.setDialogTitle("Select a file for this resource");
      fileChooser.setFileSelectionMode(fileChooser.FILES_AND_DIRECTORIES);
      if (fileChooser.showSaveDialog(largeView) ==
                                            fileChooser.APPROVE_OPTION){
        File file = fileChooser.getSelectedFile();
        try{
          PersistentResourceData.saveResourceToFile((Resource)target, file);
        }catch(Exception e){
          JOptionPane.showMessageDialog(getLargeView(),
                          "Error!\n"+
                           e.toString(),
                           "Gate", JOptionPane.ERROR_MESSAGE);
          e.printStackTrace(Err.getPrintWriter());
        }
      }
    }

  }


  class SaveToAction extends AbstractAction {
    public SaveToAction(){
      super("Save to...");
      putValue(SHORT_DESCRIPTION, "Save this resource to a new datastore");
    }

    public void actionPerformed(ActionEvent e) {
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
            ds.sync((LanguageResource)target);
          }else{
            //TODO: change the null for SecurityInfo
            LanguageResource lr = ds.adopt((LanguageResource)target,null);
            ds.sync(lr);
          }
        }
      } catch(PersistenceException pe) {
        JOptionPane.showMessageDialog(getLargeView(),
                                      "Save failed!\n " +
                                      pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }catch(gate.security.SecurityException se) {
        JOptionPane.showMessageDialog(getLargeView(),
                                      "Save failed!\n " +
                                      se.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
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
}//class DefaultResourceHandle
