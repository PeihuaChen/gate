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
  
  /** New frame */
  public ShellSlacFrame() {
    super(true);
//    guiRoots.clear();
//    guiRoots.add(this);
    
    initShellSlacLocalData();
    initShellSlacGuiComponents();
  } // ShellSlacFrame

  protected void initShellSlacLocalData(){
    createDefaultApplication();
    createCorpus();
  } // initLocalData

  protected void initShellSlacGuiComponents() {
    super.setJMenuBar(createMenuBar());
    super.setTitle("Shell SLAC " + Main.name + " " + Main.version);
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
      action.putValue(action.NAME, "Create Document");
    
      fileMenu.add(new XJMenuItem(action, this));

      fileMenu.add(new XJMenuItem(new CloseSelectedDocumentAction(), this));
    } // if
    
    fileMenu.add(new XJMenuItem("Store all Documents", "", this));
    fileMenu.add(new XJMenuItem("Load stored Documents", "", this));
    fileMenu.addSeparator();
    
    action = new RunApplicationAction();
    fileMenu.add(new XJMenuItem(action, this));
    
    action = new LoadResourceFromFileAction();
    action.putValue(action.NAME, "Load application");
    fileMenu.add(new XJMenuItem(action, this));
    
    action = new RestoreDefaultApplicationAction();
    fileMenu.add(new XJMenuItem(action, this));

    fileMenu.addSeparator();
    fileMenu.add(new XJMenuItem(new ExitGateAction(), this));
    retMenuBar.add(fileMenu);

    JMenu toolsMenu = new JMenu("Tools");
    createToolsMenuItems(toolsMenu);
    retMenuBar.add(toolsMenu);

    JMenu helpMenu = new JMenu("Help");
    helpMenu.add(new HelpAboutAction());
    retMenuBar.add(helpMenu);

    return retMenuBar;
  } // createMenuBar()

  /** Should check for registered Creole components and populate menu.
   *  <BR> In first version is hardcoded. */  
  private void createToolsMenuItems(JMenu toolsMenu) {
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
    AbstractAction action = new LoadANNIEWithDefaultsAction();
    action.actionPerformed(new ActionEvent(this, 1, "Load ANNIE"));
  } // createDefaultApplication

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
      if(corpus != null) {
        application.setCorpus(corpus);
      } // if
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
  
  class RunApplicationAction extends AbstractAction {
    public RunApplicationAction() {
      super("Run application");
      putValue(SHORT_DESCRIPTION, "Run current application");
    } // RunApplicationAction()

    public void actionPerformed(ActionEvent e) {
      if (application != null) {
        try {
          application.setCorpus(corpus);
          application.execute();
        } catch (ExecutionException ex) {
          ex.printStackTrace();
          throw new GateRuntimeException("Error in execution of application.");
        } // catch
      } // if
/*
      if (application != null && applicationsRoot.getChildCount() > 0) {
        DefaultMutableTreeNode node = 
          (DefaultMutableTreeNode) applicationsRoot.getChildAt(0);
        Object userObject = node.getUserObject();
        if(userObject instanceof NameBearerHandle) {
          NameBearerHandle handle = (NameBearerHandle) userObject;
  Out.println("Handle: "+handle);
  Out.println("Popup: "+handle.getPopup());
          Action act = handle.getPopup().getActionMap().get("Run");
          if (act != null) {
  Out.println("Action is not null");
            act.actionPerformed(null);
          } // if
          else {
  Out.println("Actions: "+handle.getPopup().getActionMap().keys());
          }
        } // if
      }// End if
*/
    } // actionPerformed(ActionEvent e)
  } // class RunApplicationAction extends AbstractAction

  class RestoreDefaultApplicationAction extends AbstractAction {
    public RestoreDefaultApplicationAction() {
      super("Restore default application");
      putValue(SHORT_DESCRIPTION, "Restore default application");
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
  
} // class ShellSlacFrame
