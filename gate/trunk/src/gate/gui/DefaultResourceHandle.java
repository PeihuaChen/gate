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
import java.awt.event.*;
import java.text.NumberFormat;
import java.io.*;

import gate.*;
import gate.util.*;
import gate.swing.*;
import gate.creole.*;
import gate.persist.*;
import gate.event.*;

/**
 * Class used to store the information about an open resource.
 * Such information will include icon to be used for tree components,
 * popup menu for right click events, etc.
 */
public class DefaultResourceHandle implements ResourceHandle {

  public DefaultResourceHandle(FeatureBearer res) {
    this.resource = res;
    rData = (ResourceData)Gate.getCreoleRegister().
                                            get(resource.getClass().getName());
    if(rData != null){
      String iconName = rData.getIcon();
      if(iconName == null){
        if(resource instanceof LanguageResource) iconName = "lr.gif";
        else if(resource instanceof ProcessingResource) iconName = "pr.gif";
      }
      this.icon = MainFrame.getIcon(iconName);
      tooltipText = "Type : " + rData.getName();
    } else {
      this.icon = MainFrame.getIcon("lr.gif");
    }

    popup = null;
    title = (String)resource.getName();
    buildViews();
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

  public Resource getResource() {
    if(resource instanceof Resource) return (Resource)resource;
    else return null;
  }

  public FeatureBearer getFeatureBearer() {
    return resource;
  }

  private void addAllViews(){
    /* Fancy discovery code goes here
    ...
    ...
    ...
    */

    /* Not so fancy hardcoded views build */
    popup = new JPopupMenu();
    popup.add(new CloseAction());
    if(resource instanceof ProcessingResource &&
       !Gate.getApplicationAttribute(resource.getFeatures())){
      popup.addSeparator();
      popup.add(new ReloadAction());
    }

    //Language Resources
    if(resource instanceof LanguageResource) {
      popup.addSeparator();
      popup.add(new SaveAction());
      popup.add(new SaveToAction());
      if(resource instanceof gate.corpora.DocumentImpl) {
        popup.add(new SaveAsXmlAction());
        try{
          FeatureMap params = Factory.newFeatureMap();
          params.put("document", resource);
          largeView.add("Annotations",
                 (JComponent)Factory.createResource("gate.gui.AnnotationEditor",
                                                      params)
                  );
        }catch(ResourceInstantiationException rie){
          rie.printStackTrace(Err.getPrintWriter());
        }
      }//else if(resource instanceof OtherKindOfLanguageResource){}
    }else if(resource instanceof ProcessingResource){
      if(resource instanceof SerialController){

      }//else if(resource instanceof OtherKindOfProcessingResource){}
      //catch all unknown PR's
    }

    FeaturesEditor fEdt = new FeaturesEditor();
    fEdt.setFeatureBearer(resource);
    largeView.add("Features", fEdt);
    smallView = null;
  }

  protected void buildViews() {
    //build the large views
    fireStatusChanged("Building views...");
    largeView = new JTabbedPane(JTabbedPane.BOTTOM);
    addAllViews();
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
  FeatureBearer resource;
  ResourceData rData;
  Icon icon;
  JComponent smallView;
  JComponent largeView;

  File currentDir = null;
  private transient Vector progressListeners;
  private transient Vector statusListeners;

  class CloseAction extends AbstractAction {
    public CloseAction() {
      super("Close");
    }

    public void actionPerformed(ActionEvent e) {
      if(resource instanceof Resource){
        Factory.deleteResource((Resource)resource);
      }
    }//public void actionPerformed(ActionEvent e)
  }//class CloseAction

  class SaveAsXmlAction extends AbstractAction {
    public SaveAsXmlAction(){
      super("Save As Xml...");
    }// SaveAsXmlAction()

    public void actionPerformed(ActionEvent e) {

      JFileChooser fileChooser = MainFrame.getFileChooser();
      File selectedFile = null;

      ExtensionFileFilter filter = new ExtensionFileFilter();
      filter.addExtension("xml");
      filter.addExtension("gml");


      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.setDialogTitle("Select document to save ...");
      fileChooser.setSelectedFiles(null);
      fileChooser.setFileFilter(filter);

      int res = (getLargeView() != null) ? fileChooser.showDialog(getLargeView(), "Save"):
                  (getSmallView() != null) ? fileChooser.showDialog(getSmallView(), "Save") :
                                             fileChooser.showDialog(null, "Save");
      if(res == JFileChooser.APPROVE_OPTION){
        selectedFile = fileChooser.getSelectedFile();
        currentDir = fileChooser.getCurrentDirectory();
        if(selectedFile == null) return;
        try{
          // Prepare to write into the xmlFile using UTF-8 encoding
          OutputStreamWriter writer = new OutputStreamWriter(
                          new FileOutputStream(selectedFile),"UTF-8");
 //         OutputStreamWriter writer = new OutputStreamWriter(
 //                                       new FileOutputStream(selectedFile));

          // Write (test the toXml() method)
          // This Action is added only when a gate.Document is created.
          // So, is for sure that the resource is a gate.Document
          writer.write(((gate.Document)resource).toXml());
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
    }
    public void actionPerformed(ActionEvent e){
      DataStore ds = ((LanguageResource)resource).getDataStore();
      if(ds != null){
        try {
          ((LanguageResource)
                    resource).getDataStore().sync((LanguageResource)resource);
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

  class SaveToAction extends AbstractAction {
    public SaveToAction(){
      super("Save to...");
    }

    public void actionPerformed(ActionEvent e) {
      try {
        DataStoreRegister dsReg = Gate.getDataStoreRegister();
        Map dsByName =new HashMap();
        Iterator dsIter = dsReg.iterator();
        while(dsIter.hasNext()){
          DataStore oneDS = (DataStore)dsIter.next();
          String name;
          if(oneDS.getFeatures() != null &&
             (name = (String)oneDS.getName()) != null){
          } else {
            name  = oneDS.getStorageUrl().getFile();
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
          DataStore ds = (DataStore)dsByName.get(answer);
          if (ds == null){
            Err.prln("The datastore does not exists. Saving procedure" +
                              " has FAILED! This should never happen again!");
            return;
          }// End if
          DataStore ownDS = ((LanguageResource)resource).getDataStore();
          if(ds == ownDS){
            ds.sync((LanguageResource)resource);
          }else{
            ds.adopt((LanguageResource)resource);
            ds.sync((LanguageResource)resource);
          }
        }
      } catch(PersistenceException pe) {
        JOptionPane.showMessageDialog(getLargeView(),
                                      "Save failed!\n " +
                                      pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
  }//class SaveToAction extends AbstractAction

  class ReloadAction extends AbstractAction {
    ReloadAction() {
      super("Reinitialise");
    }

    public void actionPerformed(ActionEvent e) {
      Runnable runnable = new Runnable(){
        public void run(){
          if(!(resource instanceof ProcessingResource)) return;
          try{
            long startTime = System.currentTimeMillis();
            fireStatusChanged("Reinitialising " +
                               resource.getName());
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

            ProcessingResource res = (ProcessingResource)resource;
            try{
              Factory.setResourceListeners(res, listeners);
            }catch (Exception e){
              e.printStackTrace(Err.getPrintWriter());
            }
            //show the progress indicator
            fireProgressChanged(0);
            //the actual reinitialisation
            res.reInit();
            try{
              Factory.removeResourceListeners(res, listeners);
            }catch (Exception e){
              e.printStackTrace(Err.getPrintWriter());
            }
            long endTime = System.currentTimeMillis();
            fireStatusChanged(resource.getName() +
                              " reinitialised in " +
                              NumberFormat.getInstance().format(
                              (double)(endTime - startTime) / 1000) + " seconds");
            fireProcessFinished();
          }catch(ResourceInstantiationException rie){
            fireStatusChanged("reinitialisation failed");
            JOptionPane.showMessageDialog(getLargeView(),
                                          "Reload failed!\n " +
                                          rie.toString(),
                                          "Gate", JOptionPane.ERROR_MESSAGE);
          }
        }//public void run()
      };
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }//public void actionPerformed(ActionEvent e)

  }//class ReloadAction

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
  }////class ReloadAction extends AbstractAction
}//class DefaultResourceHandle
