/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Angel Kirilov 18/04/2002
 *
 *  $Id$
 *
 */
package gate.gui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.*;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.ui.ProjectToolBar;

import gate.*;
import gate.creole.AbstractVisualResource;
import gate.creole.ProtegeProjectName;
import gate.creole.ontology.Taxonomy;

/**
 *  This class wrap the Protege application to show it as VR in GATE
 */
public class ProtegeWrapper extends AbstractVisualResource {
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** File name as string will be VR target for now */
  private ProtegeProjectName projectFileName = null;

  /** Should have JRootPane to show Protege in it */
  private JRootPane protegeRootPane = null;

  protected Handle myHandle;

  public ProtegeWrapper() {
    projectFileName = null;
  }

  public Resource init(){
    initLocalData();
    initGuiComponents();
    initListeners();
    return this;
  }

  private void initLocalData() {
    projectFileName = null;
  } // initLocalData()

  private void initGuiComponents() {
    setLayout(new BorderLayout());
    protegeRootPane = new JRootPane();
  } // initGuiComponents()

  /** Find and remove the Protege toolbar */
  private void removeToolbar(JRootPane rootPane) {
    Container pane = rootPane.getContentPane();

    Component components[] = pane.getComponents();
    for(int i=0; i<components.length; ++i) {
      if(components[i] instanceof ProjectToolBar) {
        pane.remove((ProjectToolBar) components[i]);
        pane.add(new JLabel(), BorderLayout.SOUTH, i);
        break;
      } // if
    } // for
  } // removeToolbar(JRootPane rootPane)

  private void initListeners() {
  } // initListeners()

  public void setHandle(Handle handle){
    myHandle = handle;
  }

  /** Refresh OntoEditor if any on LargeView tab pane */
  public void refreshOntoeditor(Taxonomy o) {
    if(myHandle == null || myHandle.getLargeView() == null) return;

    JComponent comp = myHandle.getLargeView();
    if(comp instanceof JTabbedPane) {
      JTabbedPane tabPane = (JTabbedPane) comp;
      Component aView;

      for(int i=0; i<tabPane.getTabCount(); ++i) {
        aView = tabPane.getComponentAt(i);
        if(aView instanceof com.ontotext.gate.vr.OntologyEditorImpl) {
          ((com.ontotext.gate.vr.OntologyEditorImpl) aView).setOntology(o);
        }
      } // for
    } // if
  } // refreshOntoeditor()

  public void setTarget(Object target){
    if(target == null){
      // if projectFileName is null Protege will create a new project
      projectFileName = null;
    }
    else {
      if(!(target instanceof ProtegeProjectName)){
        throw new IllegalArgumentException(
          "The Protege wrapper can only display Protege projects!\n" +
          "The provided resource is not a Protege project but a: " +
          target.getClass().toString() + "!");
      } // if

      projectFileName = (ProtegeProjectName) target;
      String fileName = null;

      if(projectFileName != null) {
        URL projectURL = projectFileName.getProjectName();
        if(projectURL != null) {
          fileName = projectURL.getFile();
        }
        if(fileName != null && fileName.trim().length() == 0) {
          fileName = null;
        }
      }

      JFrame frame = new JFrame();
      frame.getContentPane().add(protegeRootPane);

      ProjectManager.getProjectManager().setRootPane(protegeRootPane);
      if(DEBUG) {
        System.out.println("Load Protege project: "+fileName);
      }
      ProjectManager.getProjectManager().loadProject(fileName);

      protegeRootPane.setJMenuBar(null);
      removeToolbar(protegeRootPane);

      JScrollPane scroll = new JScrollPane();
      add(scroll, BorderLayout.CENTER);
      scroll.getViewport().add(protegeRootPane);

      // set KnowledgeBase object
      Project prj = null;
      KnowledgeBase knBase = null;

      prj = ProjectManager.getProjectManager().getCurrentProject();
      if(projectFileName != null && prj != null) {
        knBase = prj.getKnowledgeBase();
        projectFileName.setKnowledgeBase(knBase);
        projectFileName.setViewResource(this);
// Some debug information about KnowledgeBase instance
System.out.println("KnBase name: "+knBase.getName());
System.out.println("KnBase root cls: "+knBase.getRootClses());
System.out.println("KnBase cls count: "+knBase.getClsCount());
      } // if

    } // if
  } // setTarget(Object target)

//------------------------------------------------------------------------------
// Main method for test purposes

  /** Test code*/
  public static void main(String[] args) {

    try {
      Gate.setLocalWebServer(false);
      Gate.setNetConnected(false);
      Gate.init();

      JFrame frame = new JFrame("Protege Wrapper Test");
      frame.setSize(800, 500);

      frame.addWindowListener(new WindowAdapter(){
        public void windowClosing(WindowEvent e){
          System.exit(0);
        }
      });

      FeatureMap params = Factory.newFeatureMap();
      params.put("projectName",
        "");
      ProtegeProjectName prjName = (ProtegeProjectName) Factory.createResource(
                            "gate.creole.ProtegeProjectName", params);

      params.clear();

      ProtegeWrapper protege;

      protege = (ProtegeWrapper)Factory.createResource(
                          "gate.gui.ProtegeWrapper", params);

      frame.getContentPane().add(protege);
      frame.pack();
      frame.setVisible(true);
      protege.setTarget(prjName);

    } catch (Exception ex) {
      ex.printStackTrace();
    }

  } // public static void main(String[] args)
} // class ProtegeWrapper