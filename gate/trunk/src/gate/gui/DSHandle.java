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

public class DSHandle extends DefaultResourceHandle {

  public DSHandle(DataStore datastore) {
    super(datastore);
    super.setIcon(new ImageIcon(getClass().
                           getResource("/gate/resources/img/ds.gif")));
    this.datastore = datastore;
    initLocalData();
    initGuiComponents();
    initListeners();
  }

  protected void initLocalData(){
  }

  public DataStore getDataStore(){
    return datastore;
  }

  protected void initGuiComponents(){
    treeRoot = new DefaultMutableTreeNode(datastore.getFeatures().get("gate.NAME"),
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
          String id = (String)lrIDsIter.next();
          DSEntry entry = new DSEntry(datastore.getLrName(id), id, type);
          DefaultMutableTreeNode lrNode =
            new DefaultMutableTreeNode(entry, false);
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
    popup.add(new CloseAction());
    popup.add(new RefreshAction());
  }

  protected void initListeners(){

    tree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)){
          //where inside the tree?
          TreePath path = tree.getPathForLocation(e.getX(), e.getY());
          if(path != null){
            Object value = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
            if(value instanceof DSEntry){
              JPopupMenu popup = ((DSEntry)value).getPopup();
              popup.show(tree, e.getX(), e.getY());
            }
          }
        }
      }
    });
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
                String id = (String)lrIDsIter.next();
                DSEntry entry = new DSEntry(datastore.getLrName(id), id, type);
                DefaultMutableTreeNode lrNode =
                  new DefaultMutableTreeNode(entry, false);
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
      }catch(PersistenceException pe){
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                      "Error!\n" + pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  class LoadAction extends AbstractAction{
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
        features.put("gate.NAME", entry.name);
        Resource res = Factory.createResource(entry.type, params, features);
        datastore.getLr(entry.type, entry.id);
        //project.frame.resourcesTreeModel.treeChanged();
      }catch(gate.persist.PersistenceException pe){
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                      "Error!\n" + pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }catch(ResourceInstantiationException rie){
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                      "Error!\n" + rie.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
    DSEntry entry;
  }

  class DeleteAction extends AbstractAction{
    DeleteAction(DSEntry entry){
      super("Delete");
      this.entry = entry;
    }

    public void actionPerformed(ActionEvent e){
      try{
        datastore.delete(entry.type, entry.id);
        //project.frame.resourcesTreeModel.treeChanged();
      }catch(gate.persist.PersistenceException pe){
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
                                      "Error!\n" + pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
      }
    }
    DSEntry entry;
  }


  class DSEntry{
    DSEntry(String name, String id, String type){
      this.name = name;
      this.type = type;
      this.id = id;
      popup = new JPopupMenu();
      popup.add(new LoadAction(this));
      popup.add(new DeleteAction(this));
    }

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
  }

  JTree tree;
  DefaultMutableTreeNode treeRoot;
  DefaultTreeModel treeModel;
  DataStore datastore;
}