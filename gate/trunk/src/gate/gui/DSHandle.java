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
import gate.event.*;

public class DSHandle extends DefaultResourceHandle implements DatastoreListener{

  public DSHandle(DataStore datastore) {
    super(datastore);
    super.setIcon(MainFrame.getIcon("ds.gif"));
    this.datastore = datastore;
    tooltipText = "Type : Gate datastore";
    initLocalData();
    initGuiComponents();
    initListeners();
  }//public DSHandle(DataStore datastore)

  protected void initLocalData(){
  }

  public DataStore getDataStore(){
    return datastore;
  }

  protected void initGuiComponents(){
    treeRoot = new DefaultMutableTreeNode(
                 datastore.getName(), true);
    treeModel = new DefaultTreeModel(treeRoot, true);
    tree = new JTree(treeModel);
    tree.setExpandsSelectedPaths(true);
    smallView = tree;
    tree.expandPath(new TreePath(treeRoot));
    try {
      Iterator lrTypesIter = datastore.getLrTypes().iterator();
      CreoleRegister cReg = Gate.getCreoleRegister();
      while(lrTypesIter.hasNext()){
        String type = (String)lrTypesIter.next();
        ResourceData rData = (ResourceData)cReg.get(type);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                                                              rData.getName());
        treeModel.insertNodeInto(node, treeRoot, treeRoot.getChildCount());
        tree.expandPath(new TreePath(new Object[]{treeRoot, node}));
        Iterator lrIDsIter = datastore.getLrIds(type).iterator();
        while(lrIDsIter.hasNext()){
          String id = (String)lrIDsIter.next();
          DSEntry entry = new DSEntry(datastore.getLrName(id), id, type);
          DefaultMutableTreeNode lrNode =
            new DefaultMutableTreeNode(entry, false);
          treeModel.insertNodeInto(lrNode, node, node.getChildCount());
          node.add(lrNode);
        }
      }
    } catch(PersistenceException pe) {
      throw new GateRuntimeException(pe.toString());
    }

    popup = new JPopupMenu();
    popup.add(new CloseAction());
  }//protected void initGuiComponents()

  protected void initListeners(){
    datastore.addDatastoreListener(this);
    tree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)){
          //where inside the tree?
          TreePath path = tree.getPathForLocation(e.getX(), e.getY());
          if(path != null){
            Object value = ((DefaultMutableTreeNode)
                                  path.getLastPathComponent()).getUserObject();
            if(value instanceof DSEntry){
              JPopupMenu popup = ((DSEntry)value).getPopup();
              popup.show(tree, e.getX(), e.getY());
            }
          }
        }//if
      }//public void mouseClicked(MouseEvent e)
    });
  }//protected void initListeners()

/*
  class RefreshAction extends AbstractAction {
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
              DefaultMutableTreeNode node = new DefaultMutableTreeNode
                                                              (rData.getName());
              Iterator lrIDsIter = datastore.getLrIds(type).iterator();
              while(lrIDsIter.hasNext()){
                String id = (String)lrIDsIter.next();
                DSEntry entry = new DSEntry(datastore.getLrName(id), id, type);
                DefaultMutableTreeNode lrNode =
                  new DefaultMutableTreeNode(entry, false);
                node.add(lrNode);
              }
              treeRoot.add(node);
            }
          } catch(PersistenceException pe){
            throw new GateRuntimeException(pe.toString());
          }
          treeModel.reload();
        }//public void run
      });
    }// public void actionPerformed(ActionEvent e)
  }//class RefreshAction
*/
  class CloseAction extends AbstractAction {
    public CloseAction(){
      super("Close");
    }

    public void actionPerformed(ActionEvent e){
      try{
        datastore.close();
      } catch(PersistenceException pe){
        JOptionPane.showMessageDialog(getLargeView(),
                                      "Error!\n" + pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }//public void actionPerformed(ActionEvent e)
  }//class CloseAction

  class LoadAction extends AbstractAction {
    LoadAction(DSEntry entry){
      super("Load");
      this.entry = entry;
    }

    public void actionPerformed(ActionEvent e){
      try{
        FeatureMap params = Factory.newFeatureMap();
        params.put("DataStore", datastore);
        params.put("DataStoreInstanceId", entry.id);
        FeatureMap features = Factory.newFeatureMap();
        Gate.setName(features, entry.name);
        Resource res = Factory.createResource(entry.type, params, features);
        datastore.getLr(entry.type, entry.id);
        //project.frame.resourcesTreeModel.treeChanged();
      }catch(gate.persist.PersistenceException pe){
        JOptionPane.showMessageDialog(getLargeView(),
                                      "Error!\n" + pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
        pe.printStackTrace(Err.getPrintWriter());
      } catch(ResourceInstantiationException rie){
        JOptionPane.showMessageDialog(getLargeView(),
                                      "Error!\n" + rie.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
        rie.printStackTrace(Err.getPrintWriter());
      }
    }// public void actionPerformed(ActionEvent e)
    DSEntry entry;
  }//class LoadAction extends AbstractAction

  class DeleteAction extends AbstractAction {
    DeleteAction(DSEntry entry){
      super("Delete");
      this.entry = entry;
    }

    public void actionPerformed(ActionEvent e){
      try{
        datastore.delete(entry.type, entry.id);
        //project.frame.resourcesTreeModel.treeChanged();
      }catch(gate.persist.PersistenceException pe){
        JOptionPane.showMessageDialog(getLargeView(),
                                      "Error!\n" + pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }// public void actionPerformed(ActionEvent e)
    DSEntry entry;
  }// class DeleteAction


  class DSEntry {
    DSEntry(String name, String id, String type){
      this.name = name;
      this.type = type;
      this.id = id;
      popup = new JPopupMenu();
      popup.add(new LoadAction(this));
      popup.add(new DeleteAction(this));
    }// DSEntry

    public String toString(){
      return name;
    }

    public JPopupMenu getPopup(){
      return popup;
    }

    String name;
    String type;
    String id;
    JPopupMenu popup;
  }// class DSEntry

  JTree tree;
  DefaultMutableTreeNode treeRoot;
  DefaultTreeModel treeModel;
  DataStore datastore;
  public void resourceAdopted(DatastoreEvent e) {
    //do nothing; SerialDataStore does actually nothing on adopt()
    //we'll have to listen for RESOURE_WROTE events
  }

  public void resourceDeleted(DatastoreEvent e) {
    String resID = e.getResourceID();
    DefaultMutableTreeNode node = null;
    Enumeration nodesEnum = treeRoot.depthFirstEnumeration();
    boolean found = false;
    while(nodesEnum.hasMoreElements() && !found){
      node = (DefaultMutableTreeNode)nodesEnum.nextElement();
      Object userObject = node.getUserObject();
      found = userObject instanceof DSEntry &&
              ((DSEntry)userObject).id.equals(resID);
    }
    if(found){
      DefaultMutableTreeNode parent = (DefaultMutableTreeNode)node.getParent();
      treeModel.removeNodeFromParent(node);
      if(parent.getChildCount() == 0) treeModel.removeNodeFromParent(parent);
    }
  }

  public void resourceWritten(DatastoreEvent e) {
    Resource res = e.getResource();
    String resID = e.getResourceID();
    String resType = ((ResourceData)Gate.getCreoleRegister().
                      get(res.getClass().getName())).getName();
    DefaultMutableTreeNode parent = treeRoot;
    DefaultMutableTreeNode node = null;
    //first look for the type node
    Enumeration childrenEnum = parent.children();
    boolean found = false;
    while(childrenEnum.hasMoreElements() && !found){
      node = (DefaultMutableTreeNode)childrenEnum.nextElement();
      found = node.getUserObject().equals(resType);
    }
    if(!found){
      //exhausted the children without finding the node -> new type
      node = new DefaultMutableTreeNode(resType);
      treeModel.insertNodeInto(node, parent, parent.getChildCount());
    }
    tree.expandPath(new TreePath(new Object[]{parent, node}));

    //now look for the resource node
    parent = node;
    childrenEnum = parent.children();
    found = false;
    while(childrenEnum.hasMoreElements() && !found){
      node = (DefaultMutableTreeNode)childrenEnum.nextElement();
      found = ((DSEntry)node.getUserObject()).id.equals(resID);
    }
    if(!found){
      //exhausted the children without finding the node -> new resource
      try{
        DSEntry entry = new DSEntry(datastore.getLrName(resID), resID,
                                    res.getClass().getName());
        node = new DefaultMutableTreeNode(entry, false);
        treeModel.insertNodeInto(node, parent, parent.getChildCount());
      }catch(PersistenceException pe){
        pe.printStackTrace(Err.getPrintWriter());
      }
    }
  }//public void resourceWritten(DatastoreEvent e)
}//public class DSHandle