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

import gate.*;
import gate.persist.*;
import gate.util.*;

import javax.swing.*;
import java.awt.event.*;

import java.util.*;
import java.io.*;

public class LRHandle extends CustomResourceHandle {

  File currentDir = null;

  public LRHandle(LanguageResource res, ProjectData project) {
    super(res, project);
    setIcon(new ImageIcon(
           getClass().getResource("/gate/resources/img/lr.gif")));
    popup = new JPopupMenu();
    popup.add(new CloseAction());
    popup.add(new SaveAction());
    popup.add(new SaveToAction());
    JTabbedPane view = (JTabbedPane)super.getLargeView();
    if(resource instanceof gate.Document){
      AnnotationEditor annView = new AnnotationEditor();
      annView.setDocument((Document)resource);
      view.add("Annotations", annView);
      view.setSelectedComponent(annView);
      popup.add(new SaveAsXmlAction());
    }
  }


  class CloseAction extends AbstractAction{
    public CloseAction(){
      super("Close");
    }

    public void actionPerformed(ActionEvent e){
      project.remove(myself);
      Factory.deleteResource(resource);
    }
  }

  class SaveAsXmlAction extends AbstractAction{
    public SaveAsXmlAction(){
      super("Save As Xml...");
    }// SaveAsXmlAction()

    public void actionPerformed(ActionEvent e){

      JFileChooser fileChooser = null;
      File selectedFile = null;

      ExtensionFileFilter filter = new ExtensionFileFilter();
      filter.addExtension("xml");
      filter.addExtension("gml");

      if (currentDir == null)
        fileChooser = new JFileChooser();
      else
        fileChooser = new JFileChooser(currentDir);

      fileChooser.setMultiSelectionEnabled(false);
      fileChooser.setDialogTitle("Select document to save ...");
      fileChooser.setSelectedFiles(null);
      fileChooser.setFileFilter(filter);
      int res = fileChooser.showDialog(project.frame, "Save");
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
          ex.printStackTrace(System.out);
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
          JOptionPane.showMessageDialog(project.frame,
                                        "Save failed!\n " +
                                        pe.toString(),
                                        "Gate", JOptionPane.ERROR_MESSAGE);
        }
      }else{
        JOptionPane.showMessageDialog(project.frame,
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
             (name = (String)oneDS.getFeatures().get("NAME")) != null){
          }else{
            name  = oneDS.getStorageUrl().getFile();
          }
          dsByName.put(name, oneDS);
        }
        List dsNames = new ArrayList(dsByName.keySet());
        if(dsNames.isEmpty()){
          JOptionPane.showMessageDialog(project.frame,
                                        "There are no open datastores!\n " +
                                        "Please open a datastore first!",
                                        "Gate", JOptionPane.ERROR_MESSAGE);

        }else{
          Object answer = JOptionPane.showInputDialog(
                              project.frame,
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
        JOptionPane.showMessageDialog(project.frame,
                                      "Save failed!\n " +
                                      pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}