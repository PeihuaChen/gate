/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva (based on code from Valentin Tablan) 31/10/2001
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
import java.text.NumberFormat;

import gate.event.*;
import java.beans.*;

public class OracleDatastoreViewer extends JTree
                                   implements VisualResource,
                                              DatastoreListener {

  public OracleDatastoreViewer() {
  }


  public void cleanup(){
  }

  /** Accessor for features. */
  public FeatureMap getFeatures(){
    return features;
  }//getFeatures()

  /** Mutator for features*/
  public void setFeatures(FeatureMap features){
    this.features = features;
  }// setFeatures()

  //Parameters utility methods
  /**
   * Gets the value of a parameter of this resource.
   * @param paramaterName the name of the parameter
   * @return the current value of the parameter
   */
  public Object getParameterValue(String paramaterName)
                throws ResourceInstantiationException{
    return AbstractResource.getParameterValue(this, paramaterName);
  }

  /**
   * Sets the value for a specified parameter.
   *
   * @param paramaterName the name for the parameteer
   * @param parameterValue the value the parameter will receive
   */
  public void setParameterValue(String paramaterName, Object parameterValue)
              throws ResourceInstantiationException{
    // get the beaninfo for the resource bean, excluding data about Object
    BeanInfo resBeanInf = null;
    try {
      resBeanInf = Introspector.getBeanInfo(this.getClass(), Object.class);
    } catch(Exception e) {
      throw new ResourceInstantiationException(
        "Couldn't get bean info for resource " + this.getClass().getName()
        + Strings.getNl() + "Introspector exception was: " + e
      );
    }
    AbstractResource.setParameterValue(this, resBeanInf, paramaterName, parameterValue);
  }

  /**
   * Sets the values for more parameters in one step.
   *
   * @param parameters a feature map that has paramete names as keys and
   * parameter values as values.
   */
  public void setParameterValues(FeatureMap parameters)
              throws ResourceInstantiationException{
    AbstractResource.setParameterValues(this, parameters);
  }

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    return this;
  }//init()

  public void clear(){
  }

  public void setTarget(Object target){
    if(target instanceof DataStore){
      datastore = (DataStore)target;
      initLocalData();
      initGuiComponents();
      initListeners();
    }else{
      throw new IllegalArgumentException(
        "SerialDatastoreViewers can only be used with GATE serial datastores!\n" +
        target.getClass().toString() + " is not a GATE serial datastore!");
    }
  }


  public void setHandle(Handle handle){
    if(handle instanceof NameBearerHandle){
      myHandle = (NameBearerHandle)handle;
    }
  }

  protected void fireProgressChanged(int e) {
    myHandle.fireProgressChanged(e);
  }//protected void fireProgressChanged(int e)

  protected void fireProcessFinished() {
    myHandle.fireProcessFinished();
  }//protected void fireProcessFinished()

  protected void fireStatusChanged(String e) {
    myHandle.fireStatusChanged(e);
  }

  protected void initLocalData(){
  }

  protected void initGuiComponents(){
    treeRoot = new DefaultMutableTreeNode(
                 datastore.getName(), true);
    treeModel = new DefaultTreeModel(treeRoot, true);
    setModel(treeModel);
    setExpandsSelectedPaths(true);
    expandPath(new TreePath(treeRoot));
    try {
      Iterator lrTypesIter = datastore.getLrTypes().iterator();
      CreoleRegister cReg = Gate.getCreoleRegister();
      while(lrTypesIter.hasNext()){
        String type = (String)lrTypesIter.next();
        ResourceData rData = (ResourceData)cReg.get(type);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                                                              rData.getName());
        treeModel.insertNodeInto(node, treeRoot, treeRoot.getChildCount());
        expandPath(new TreePath(new Object[]{treeRoot, node}));
        Iterator lrIDsIter = datastore.getLrIds(type).iterator();
        while(lrIDsIter.hasNext()){
          Object id = (Object)lrIDsIter.next();
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

  }//protected void initGuiComponents()

  protected void initListeners(){
    datastore.addDatastoreListener(this);
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        //where inside the tree?
        TreePath path = getPathForLocation(e.getX(), e.getY());
        Object value = null;
        if(path != null) value = ((DefaultMutableTreeNode)
                                  path.getLastPathComponent()).getUserObject();

        if(SwingUtilities.isRightMouseButton(e)){
          //right click
          if(value != null && value instanceof DSEntry){
            JPopupMenu popup = ((DSEntry)value).getPopup();
            popup.show(OracleDatastoreViewer.this, e.getX(), e.getY());
          }
        }else if(SwingUtilities.isLeftMouseButton(e) &&
                 e.getClickCount() == 2){
          //double click -> just load the resource
          if(value != null && value instanceof DSEntry){
            new LoadAction((DSEntry)value).actionPerformed(null);
          }
        }
      }//public void mouseClicked(MouseEvent e)
    });
  }//protected void initListeners()


  class LoadAction extends AbstractAction {
    LoadAction(DSEntry entry){
      super("Load");
      this.entry = entry;
    }

    public void actionPerformed(ActionEvent e){
      Runnable runnable = new Runnable(){
        public void run(){
          try{
            long start = System.currentTimeMillis();
            fireStatusChanged("Loading " + entry.name);
            fireProgressChanged(0);
            FeatureMap params = Factory.newFeatureMap();
            params.put(DataStore.DATASTORE_FEATURE_NAME, datastore);
            params.put(DataStore.LR_ID_FEATURE_NAME, entry.id);
            FeatureMap features = Factory.newFeatureMap();
            Resource res = Factory.createResource(entry.type, params, features,
                                                  null, entry.name);
            //project.frame.resourcesTreeModel.treeChanged();
            fireProgressChanged(0);
            fireProcessFinished();
            long end = System.currentTimeMillis();
            fireStatusChanged(entry.name + " loaded in " +
                              NumberFormat.getInstance().format(
                              (double)(end - start) / 1000) + " seconds");
          } catch(ResourceInstantiationException rie){
            JOptionPane.showMessageDialog(OracleDatastoreViewer.this,
                                          "Error!\n" + rie.toString(),
                                          "Gate", JOptionPane.ERROR_MESSAGE);
            rie.printStackTrace(Err.getPrintWriter());
            fireProgressChanged(0);
            fireProcessFinished();
          }
        }
      };//runnable
      Thread thread = new Thread(Thread.currentThread().getThreadGroup(),
                                 runnable,
                                 "Loader from DS");
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
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
        JOptionPane.showMessageDialog(OracleDatastoreViewer.this,
                                      "Error!\n" + pe.toString(),
                                      "Gate", JOptionPane.ERROR_MESSAGE);
        pe.printStackTrace(Err.getPrintWriter());
      }
    }// public void actionPerformed(ActionEvent e)
    DSEntry entry;
  }// class DeleteAction


  class DSEntry {
    DSEntry(String name, Object id, String type){
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
    Object id;
    JPopupMenu popup;
  }// class DSEntry

  DefaultMutableTreeNode treeRoot;
  DefaultTreeModel treeModel;
  DataStore datastore;
  NameBearerHandle myHandle;
  protected FeatureMap features;

  private transient Vector progressListeners;
  private transient Vector statusListeners;
  public void resourceAdopted(DatastoreEvent e) {
    //do nothing; SerialDataStore does actually nothing on adopt()
    //we'll have to listen for RESOURE_WROTE events
  }

  public void resourceDeleted(DatastoreEvent e) {
    Object resID = e.getResourceID();
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
    Object resID = e.getResourceID();
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
    expandPath(new TreePath(new Object[]{parent, node}));

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