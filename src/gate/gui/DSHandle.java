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
import gate.creole.*;
import gate.util.*;
import gate.persist.*;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.tree.*;

import java.util.*;

public class DSHandle extends ResourceHandle {

  public DSHandle(DataStore datastore, ProjectData project) {
    super((String)datastore.getFeatures().get("NAME"), project);
    super.setSmallIcon(new ImageIcon(getClass().
                           getResource("/gate/resources/img/ds.gif")));
    this.datastore = datastore;
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  protected void initLocalData(){
  }

  protected void initGuiComponents(){
    treeRoot = new DefaultMutableTreeNode(datastore.getFeatures().get("NAME"),
                                          true);
    try{
      Iterator lrTypesIter = datastore.getLrTypes().iterator();
      CreoleRegister cReg = Gate.getCreoleRegister();
      while(lrTypesIter.hasNext()){
        String type = (String)lrTypesIter.next();
        ResourceData rData = (ResourceData)cReg.get(type);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(rData.getName());
        Iterator lrIDsIter = datastore.getLrIds(type).iterator();
        while(lrIDsIter.hasNext()){
          DefaultMutableTreeNode lrNode =
            new DefaultMutableTreeNode(datastore.
                                         getLrName((String)lrIDsIter.next()),
                                       false);
          node.add(lrNode);
        }
        treeRoot.add(node);
      }
    }catch(PersistenceException pe){
      throw new GateRuntimeException(pe.toString());
    }
    treeModel = new DefaultTreeModel(treeRoot, true);
    tree = new JTree(treeModel);
    smallView = tree;

    popup = new JPopupMenu();
    popup.add(new RefreshAction());

  }

  protected void initListeners(){
  }

  class RefreshAction extends AbstractAction{
    public RefreshAction(){
      super("Refresh");
    }

    public void actionPerformed(ActionEvent e){
      SwingUtilities.invokeLater(new Runnable(){
        public void run(){
          treeRoot.removeAllChildren();
          try{
            Iterator lrTypesIter = datastore.getLrTypes().iterator();
            CreoleRegister cReg = Gate.getCreoleRegister();
            while(lrTypesIter.hasNext()){
              String type = (String)lrTypesIter.next();
              ResourceData rData = (ResourceData)cReg.get(type);
              DefaultMutableTreeNode node = new DefaultMutableTreeNode(rData.getName());
              Iterator lrIDsIter = datastore.getLrIds(type).iterator();
              while(lrIDsIter.hasNext()){
                DefaultMutableTreeNode lrNode =
                  new DefaultMutableTreeNode(datastore.
                                               getLrName((String)lrIDsIter.next()),
                                             false);
                node.add(lrNode);
              }
              treeRoot.add(node);
            }
          }catch(PersistenceException pe){
            throw new GateRuntimeException(pe.toString());
          }
          treeModel.reload();
        }
      });
    }
  }

  class CloseAction extends AbstractAction{
    public CloseAction(){
      super("Close");
    }

    public void actionPerformed(ActionEvent e){
      try{
        datastore.close();
        project.remove(myself);
      }catch(PersistenceException pe){
        JOptionPane.showMessageDialog(project.frame,
                                      "Error!\n" + pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
  }


  JTree tree;
  DefaultMutableTreeNode treeRoot;
  DefaultTreeModel treeModel;
  DataStore datastore;
}