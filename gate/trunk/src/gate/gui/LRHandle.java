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

public class LRHandle extends ResourceHandle {

  public LRHandle(LanguageResource res, ProjectData project) {
    super(res, project);
    setSmallIcon(new ImageIcon(
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
    }
  }


  class CloseAction extends AbstractAction{
    public CloseAction(){
      super("Close");
    }

    public void actionPerformed(ActionEvent e){
      project.remove(myself);
    }
  }

  class SaveAction extends AbstractAction{
    public SaveAction(){
      super("Save");
    }
    public void actionPerformed(ActionEvent e){
      DataStore ds = ((LanguageResource)resource).getDataStore();
      if(ds != null){
        try{
          ((LanguageResource)resource).getDataStore().sync((LanguageResource)resource);
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