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
import java.io.*;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.persist.*;

/**
 * Class used to store the information about an open resource.
 * Such information will include icon to be used for tree components,
 * popup menu for right click events, etc.
 */
class DefaultResourceHandle implements ResourceHandle{

  public DefaultResourceHandle(FeatureBearer res){
    this.resource = res;
    rData = (ResourceData)Gate.getCreoleRegister().
                                            get(resource.getClass().getName());
    if(rData != null){
      String iconName = rData.getIcon();
      if(iconName == null){
        if(resource instanceof LanguageResource) iconName = "lr.gif";
        else if(resource instanceof ProcessingResource) iconName = "pr.gif";
      }
      try{
        this.icon = new ImageIcon(new URL("gate:/img/" + iconName));
      }catch(MalformedURLException mue){
        mue.printStackTrace(Err.getPrintWriter());
      }
    }else{
      try{
        this.icon = new ImageIcon(new URL("gate:/img/lr.gif"));
      }catch(MalformedURLException mue){
        mue.printStackTrace(Err.getPrintWriter());
      }
    }

    popup = null;
    title = (String)resource.getFeatures().get("gate.NAME");
    buildViews();
  }

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
  public JComponent getSmallView(){
    return smallView;
  }

  /**
   * Returns the large view for this resource. This view will go into the main
   * display area.
   */
  public JComponent getLargeView(){
    return largeView;
  }

  public JPopupMenu getPopup(){
    return popup;
  }

  public void setPopup(JPopupMenu popup){
    this.popup = popup;
  }

  public String getTooltipText(){
    return tooltipText;
  }

  public void setTooltipText(String text){
    this.tooltipText = text;
  }

  public Resource getResource(){
    if(resource instanceof Resource) return (Resource)resource;
    else return null;
  }

  public FeatureBearer getFeatureBearer(){
    return resource;
  }

  protected void buildViews(){
    //build the large views
    JTabbedPane view = new JTabbedPane(JTabbedPane.BOTTOM);

    /* Fancy discovery code goes here
    ...
    ...
    ...
    */

    /* Not so fancy hardcoded views build */
    popup = new JPopupMenu();
    popup.add(new CloseAction());
    //Language Resources
    if(resource instanceof LanguageResource){
      popup.add(new SaveAction());
      popup.add(new SaveToAction());
      if(resource instanceof gate.corpora.DocumentImpl){
        popup.add(new SaveAsXmlAction());
        try{
          FeatureMap params = Factory.newFeatureMap();
          params.put("document", resource);
          view.add("Annotations",
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
    view.add("Features", fEdt);
    largeView = view;
    smallView = null;
  }

  public String toString(){ return title;}

  JPopupMenu popup;
  String title;
  String tooltipText;
  FeatureBearer resource;
  ResourceData rData;
  Icon icon;
  JComponent smallView;
  JComponent largeView;

  File currentDir = null;

  class CloseAction extends AbstractAction{
    public CloseAction(){
      super("Close");
    }

    public void actionPerformed(ActionEvent e){
      if(resource instanceof Resource){
        Factory.deleteResource((Resource)resource);
      }
    }
  }

  class SaveAsXmlAction extends AbstractAction{
    public SaveAsXmlAction(){
      super("Save As Xml...");
    }// SaveAsXmlAction()

    public void actionPerformed(ActionEvent e){

      JFileChooser fileChooser = MainFrame.getInstance().fileChooser;
      File selectedFile = null;

      ExtensionFileFilter filter = new ExtensionFileFilter();
      filter.addExtension("xml");
      filter.addExtension("gml");


      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.setDialogTitle("Select document to save ...");
      fileChooser.setSelectedFiles(null);
      fileChooser.setFileFilter(filter);
      int res = fileChooser.showDialog(MainFrame.getInstance(), "Save");
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

  class SaveAction extends AbstractAction{
    public SaveAction(){
      super("Save");
    }
    public void actionPerformed(ActionEvent e){
      DataStore ds = ((LanguageResource)resource).getDataStore();
      if(ds != null){
        try{
          ((LanguageResource)
                    resource).getDataStore().sync((LanguageResource)resource);
        }catch(PersistenceException pe){
          JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                        "Save failed!\n " +
                                        pe.toString(),
                                        "Gate", JOptionPane.ERROR_MESSAGE);
        }
      }else{
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
                        "This resource has not been loaded from a datastore.\n"+
                         "Please use the \"Save to\" option!\n",
                         "Gate", JOptionPane.ERROR_MESSAGE);

      }
    }
  }

  class SaveToAction extends AbstractAction{
    public SaveToAction(){
      super("Save to...");
    }

    public void actionPerformed(ActionEvent e){
      try{
        DataStoreRegister dsReg = Gate.getDataStoreRegister();
        Map dsByName =new HashMap();
        Iterator dsIter = dsReg.iterator();
        while(dsIter.hasNext()){
          DataStore oneDS = (DataStore)dsIter.next();
          String name;
          if(oneDS.getFeatures() != null &&
             (name = (String)oneDS.getFeatures().get("gate.NAME")) != null){
          }else{
            name  = oneDS.getStorageUrl().getFile();
          }
          dsByName.put(name, oneDS);
        }
        List dsNames = new ArrayList(dsByName.keySet());
        if(dsNames.isEmpty()){
          JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                        "There are no open datastores!\n " +
                                        "Please open a datastore first!",
                                        "Gate", JOptionPane.ERROR_MESSAGE);

        }else{
          Object answer = JOptionPane.showInputDialog(
                              MainFrame.getInstance(),
                              "Select the datastore",
                              "Gate", JOptionPane.QUESTION_MESSAGE,
                              null, dsNames.toArray(),
                              dsNames.get(0));
          DataStore ds = (DataStore)dsByName.get(answer);
          DataStore ownDS = ((LanguageResource)resource).getDataStore();
          if(ds == ownDS){
            ds.sync((LanguageResource)resource);
          }else{
            ds.adopt((LanguageResource)resource);
            ds.sync((LanguageResource)resource);
          }
        }
      }catch(PersistenceException pe){
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                      "Save failed!\n " +
                                      pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}
