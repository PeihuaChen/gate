/*  ApplicationViewer.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 25/01/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import gate.creole.*;
import gate.*;
import gate.swing.*;
import gate.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import java.text.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.IOException;
import java.net.URL;
import gate.event.*;

public class ApplicationViewer extends AbstractVisualResource
                               implements CreoleListener {

  public ApplicationViewer() {
  }
/*
  public ApplicationViewer(Controller controller) {
    if(controller instanceof SerialController){
      this.controller = (SerialController)controller;
      this.handle = handle;
      this.popup = handle.getPopup();
      init();
    }else{
      throw new UnsupportedOperationException(
        "Editing of controllers implemented only for serial controllers!");
    }
  }
*/
  public void setController(SerialController controller){
    this.controller = controller;
  }

  public void setHandle(ResourceHandle handle){
    this.handle = handle;
  }

  public Resource init(){
    initLocalData();
    initGuiComponents();
    initListeners();
    return this;
  }

  protected void initLocalData(){
    paramsForPR = new HashMap();
    addActionForPR = new HashMap();
    removeActionForPR = new HashMap();
    runAction = new RunAction();
/*
    Iterator prIter = project.getPRList().iterator();
    while(prIter.hasNext()){
      ProcessingResource pr = (ProcessingResource)prIter.next();
      AddPRAction addAction = new AddPRAction(pr);
      RemovePRAction remAction = new RemovePRAction(pr);
      remAction.setEnabled(false);
      addActionForPR.put(pr, addAction);
      removeActionForPR.put(pr, remAction);
    }
*/
  }

  protected void initGuiComponents(){
    this.setLayout(new BorderLayout());
    Box mainBox = Box.createHorizontalBox();

    mainTTModel = new PRsAndParamsTTModel(controller);
    mainTreeTable = new JTreeTable(mainTTModel);
    mainTreeTable.getTree().setCellRenderer(new CustomTreeCellRenderer());
    mainTreeTable.getTree().setRootVisible(false);
//    mainTreeTable.getTree().setEditable(true);
    mainTreeTable.getTree().setShowsRootHandles(true);
    mainTreeTable.getTree().setCellEditor(new ParameterDisjunctionEditor());
    mainTreeTable.setIntercellSpacing(new Dimension(5,0));
    mainTreeTable.setDefaultRenderer(Object.class, new ParameterValueRenderer());
    mainTreeTable.setDefaultEditor(Object.class, new ParameterValueEditor());

    ToolTipManager.sharedInstance().registerComponent(mainTreeTable.getTree());
    ToolTipManager.sharedInstance().registerComponent(mainTreeTable);
    JScrollPane scroller = new JScrollPane(mainTreeTable);
    scroller.setBorder(BorderFactory.createTitledBorder("Used components"));

    mainBox.add(scroller);

    Box buttonsBox = Box.createVerticalBox();
    addModuleBtn = new JButton("Add component",
                               new ImageIcon(
                                 ApplicationViewer.class.getResource(
                                 "/gate/resources/img/left2.gif"))
                               );
    addModuleBtn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    removeModuleBtn = new JButton("Remove component",
                                  new ImageIcon(
                                    ApplicationViewer.class.getResource(
                                    "/gate/resources/img/right2.gif"))
                                  );
    removeModuleBtn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    buttonsBox.add(Box.createVerticalStrut(30));
    buttonsBox.add(addModuleBtn);
    buttonsBox.add(Box.createVerticalStrut(5));
    buttonsBox.add(removeModuleBtn);
    buttonsBox.add(Box.createVerticalGlue());
    upBtn = new JButton("Move up",
                new ImageIcon(
                    ApplicationViewer.class.getResource(
                    "/gate/resources/img/up.gif")));
    downBtn = new JButton("Move down",
              new ImageIcon(
                  ApplicationViewer.class.getResource(
                  "/gate/resources/img/down.gif")));
    Box horBox = Box.createHorizontalBox();
    Box verBox = Box.createVerticalBox();
    verBox.add(upBtn);
    verBox.add(downBtn);
    horBox.add(verBox);
    horBox.add(Box.createHorizontalGlue());
    buttonsBox.add(horBox);

    mainBox.add(buttonsBox);

    modulesTableModel = new ModulesTableModel();
    modulesTable = new XJTable(modulesTableModel);
    modulesTable.setSortable(true);
    modulesTable.setSortedColumn(0);
    modulesTable.setSelectionMode(
                                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    scroller = new JScrollPane(modulesTable);
    scroller.setBorder(BorderFactory.createTitledBorder("Available components"));

    mainBox.add(scroller);

    this.add(mainBox, BorderLayout.CENTER);

    popup = new JPopupMenu();
    popup.add(new RunAction());
    addMenu = new JMenu("Add");
    removeMenu = new JMenu("Remove");
    popup.add(addMenu);
    popup.add(removeMenu);
  }

  protected void initListeners(){
    Gate.getCreoleRegister().addCreoleListener(this);
    this.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)){
          if(handle != null && handle.getPopup()!= null)
            handle.getPopup().show(ApplicationViewer.this, e.getX(), e.getY());
        }
      }
    });

    addModuleBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rows[] = modulesTable.getSelectedRows();
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              ApplicationViewer.this,
              "Please select some components from the list of available components!\n" ,
              "Gate", JOptionPane.ERROR_MESSAGE);
        }else{
          List actions = new ArrayList();
          for(int i = 0; i < rows.length; i++){
            Action act =(Action)
                       addActionForPR.get(modulesTable.getValueAt(rows[i], -1));
            if(act != null) actions.add(act);
          }
          Iterator actIter = actions.iterator();
          while(actIter.hasNext()){
            ((Action)actIter.next()).actionPerformed(null);
          }
        }
      }
    });

    removeModuleBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rows[] = mainTreeTable.getSelectedRows();
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              ApplicationViewer.this,
              "Please select some components to be removed from the list of used components!\n" ,
              "Gate", JOptionPane.ERROR_MESSAGE);
        } else {
          List actions = new ArrayList();
          List nodes = new ArrayList();
          for(int i = 0; i < rows.length; i++){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                          mainTreeTable.getTree().
                                          getPathForRow(rows[i]).
                                          getLastPathComponent();
            Object value = ((DefaultMutableTreeNode)node).getUserObject();
            if(value instanceof ProcessingResource && controller.contains(value)){
              Action act = (Action)removeActionForPR.get(value);
              if(act != null){
                actions.add(act);
                nodes.add(node);
              }
            } else {
              JOptionPane.showMessageDialog(
                  ApplicationViewer.this,
                  "Only processing resources can be removed!\n" +
                  "(Processing resources are the nodes from the first level of the tree)" ,
                  "Gate", JOptionPane.ERROR_MESSAGE);
            }
          }
          Iterator actIter = actions.iterator();
          while(actIter.hasNext()){
            ((Action)actIter.next()).actionPerformed(null);
          }
          Iterator nodesIter = nodes.iterator();
          while(nodesIter.hasNext()){
            mainTTModel.removeNodeFromParent(
                        (DefaultMutableTreeNode)nodesIter.next());
          }
        }
      }
    });

    upBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = mainTreeTable.getTree().getSelectionPaths();
        if(paths == null || paths.length == 0){
          JOptionPane.showMessageDialog(
              ApplicationViewer.this,
              "Please select some components to be moved from the list of used components!\n" ,
              "Gate", JOptionPane.ERROR_MESSAGE);
        }else{
          for(int i = 0; i < paths.length; i++){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                          paths[i].getLastPathComponent();
            Object value = node.getUserObject();
            if(value instanceof ProcessingResource &&
               controller.contains(value)){
              int index = controller.indexOf(value);
              //move the module up
              if(index > 0){
                controller.remove(index);
                index--;
                controller.add(index, value);
                DefaultMutableTreeNode parent =
                      (DefaultMutableTreeNode)node.getParent();
                boolean expanded = mainTreeTable.getTree().isExpanded(paths[i]);
                mainTTModel.removeNodeFromParent(node);
                mainTTModel.insertNodeInto(node, parent, index);
                if(expanded){
                  mainTreeTable.expandPath(paths[i]);
                }
              }
            }else{
              JOptionPane.showMessageDialog(
                  ApplicationViewer.this,
                  "Only processing resources can be moved!\n" +
                  "(Processing resources are the nodes from the first level of the tree)" ,
                  "Gate", JOptionPane.ERROR_MESSAGE);
            }
          }
          final TreePath[] finalPaths = paths;
          SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              mainTreeTable.getTree().setSelectionPaths(finalPaths);
            }
          });
        }
      }//public void actionPerformed(ActionEvent e)
    });

    downBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        TreePath[] paths = mainTreeTable.getTree().getSelectionPaths();
        if(paths == null || paths.length == 0){
          JOptionPane.showMessageDialog(
              ApplicationViewer.this,
              "Please select some components to be moved from the list of used components!\n" ,
              "Gate", JOptionPane.ERROR_MESSAGE);
        }else{
          for(int i = paths.length -1; i >= 0; i--){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                          paths[i].getLastPathComponent();
            Object value = node.getUserObject();
            if(value instanceof ProcessingResource && controller.contains(value)){
              int index = controller.indexOf(value);
              //move the module down
              if(index < controller.size() - 1){
                controller.remove(index);
                index++;
                controller.add(index, value);
                DefaultMutableTreeNode parent =
                      (DefaultMutableTreeNode)node.getParent();
                boolean expanded = mainTreeTable.getTree().isExpanded(paths[i]);
                mainTTModel.removeNodeFromParent(node);
                mainTTModel.insertNodeInto(node, parent, index);
                if(expanded){
                  mainTreeTable.expandPath(paths[i]);
                }
              }
            }else{
              JOptionPane.showMessageDialog(
                  ApplicationViewer.this,
                  "Only processing resources can be moved!\n" +
                  "(Processing resources are the nodes from the first level of the tree)" ,
                  "Gate", JOptionPane.ERROR_MESSAGE);
            }
          }//for(int i = 0; i < rows.length; i++)
          final TreePath[] finalPaths = paths;
          SwingUtilities.invokeLater(new Runnable(){
            public void run(){
              mainTreeTable.getTree().setSelectionPaths(finalPaths);
            }
          });
        }
      }//public void actionPerformed(ActionEvent e)
    });

    mainTreeTable.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        ApplicationViewer.this.validate();
      }

      public void componentShown(ComponentEvent e) {
        ApplicationViewer.this.validate();
      }
    });

    modulesTable.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        ApplicationViewer.this.validate();
      }

      public void componentShown(ComponentEvent e) {
        ApplicationViewer.this.validate();
      }
    });
  }//protected void initListeners()

  protected void updateActions(){
    Iterator prIter = Gate.getCreoleRegister().getPrInstances().iterator();
    while(prIter.hasNext()){
      ProcessingResource pr = (ProcessingResource)prIter.next();
      if(pr == controller ||
         ( pr.getFeatures().get("gate.HIDDEN") != null &&
           ((String)pr.getFeatures().
                       get("gate.HIDDEN")).equalsIgnoreCase("true"))
        ){
        //ignore this resource
      }else{
        if(!addActionForPR.containsKey(pr)){
          AddPRAction addAction = new AddPRAction(pr);
          RemovePRAction remAction = new RemovePRAction(pr);
          remAction.setEnabled(false);
          addActionForPR.put(pr, addAction);
          removeActionForPR.put(pr, remAction);
        }
      }
    }
  }//protected void updateActions()

  public MenuElement[] getPopupElements(){
    updateActions();
    popup.removeAll();
    popup.add(runAction);
    addMenu = new JMenu("Add");
    removeMenu = new JMenu("Remove");
    popup.add(addMenu);
    popup.add(removeMenu);
    Iterator addActionsIter = addActionForPR.values().iterator();
    while(addActionsIter.hasNext()){
      addMenu.add((Action)addActionsIter.next());
    }

    Iterator remActionsIter = removeActionForPR.values().iterator();
    while(remActionsIter.hasNext()){
      removeMenu.add((Action)remActionsIter.next());
    }
    return popup.getSubElements();
  }

  protected String getResourceName(Resource res){
    ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                         get(res.getClass().getName());
    if(rData != null) return rData.getName();
    else return res.getClass().getName();
  }

  class PRsAndParamsTTModel extends DefaultTreeModel implements TreeTableModel{
    PRsAndParamsTTModel(SerialController aController){
      super(new DefaultMutableTreeNode(aController, true), true);
    }

    public int getColumnCount(){
      return 3;
    }

    public String getColumnName(int column){
      switch(column){
        case 0: return "Name";
        case 1: return "Type";
        case 2: return "Parameter Value";
        default: return "?";
      }
    }

    public Class getColumnClass(int column){
      switch(column){
        case 1: return String.class;
        case 2: return Object.class;
        default: return Object.class;
      }
    }

    public Object getValueAt(Object node, int column){
      if(node == root){
        return null;
      }else{
        node = ((DefaultMutableTreeNode)node).getUserObject();
        if (node instanceof ProcessingResource){
          ProcessingResource pr = (ProcessingResource)node;
          if(column == 1) return getResourceName(pr);
          else return null;
        }else if (node instanceof ParameterDisjunction){
          ParameterDisjunction pd = (ParameterDisjunction)node;
          switch(column){
            case 0: return pd;
            case 1: {
              String paramType = pd.getType();
              if(paramType.startsWith("gate.")){
                ResourceData rData = (ResourceData)
                                     Gate.getCreoleRegister().get(paramType);
                if(rData != null) paramType = rData.getName();
              }
              return paramType;
            }case 2: return pd.getValue();
            default: return null;
          }
        }
      }
      return null;
    }

    public boolean isCellEditable(Object node, int column){
      node = ((DefaultMutableTreeNode)node).getUserObject();
      if(column == 2) return node instanceof ParameterDisjunction;
      if(column == 0){
        return node instanceof ParameterDisjunction &&
               ((ParameterDisjunction)node).size() > 1 ;
      }
      return false;
    }

    public void setValueAt(Object aValue, Object node, int column){
      node = ((DefaultMutableTreeNode)node).getUserObject();
      switch(column){
        case 0:{
          if(node instanceof ParameterDisjunction && aValue instanceof Integer){
            ((ParameterDisjunction)node).
              setSelectedIndex(((Integer)aValue).intValue());
          }
        }case 2:{
          if(node instanceof ParameterDisjunction){
            ((ParameterDisjunction)node).setValue(aValue);
          }
          break;
        }
      }//switch(column)
    }
/*
    public Object getChild(Object parent, int index){
      if(parent == root){
        SerialController sc = (SerialController)parent;
        return sc.get(index);
      }else{
        parent = ((DefaultMutableTreeNode)parent).getUserObject();
        if (parent instanceof ProcessingResource){
          ProcessingResource pr = (ProcessingResource)parent;
          List paramsList = (List)paramsForPR.get(pr);
          return (ParameterDisjunction)paramsList.get(index);
        }else return null;
      }
    }

    public int getChildCount(Object parent){
      if(parent == root){
        SerialController sc = (SerialController)parent;
        return sc.size();
      }else{
        parent = ((DefaultMutableTreeNode)parent).getUserObject();
        if (parent instanceof ProcessingResource){
          ProcessingResource pr = (ProcessingResource)parent;
          List paramsList = (List)paramsForPR.get(pr);
          return paramsList==null ? 0 : paramsList.size();
        }else  return 0;
      }
    }

*/
    public void dataChanged(){
      fireTreeStructureChanged(this, new Object[]{getRoot()}, null, null);
    }

  }//class PRsAndParamsTTModel extends AbstractTreeTableModel

  class CustomTreeCellRenderer extends DefaultTreeCellRenderer{
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus){

      String text = "";
      String tipText = null;
      String iconName = null;
      value = ((DefaultMutableTreeNode)value).getUserObject();
      if (value instanceof ProcessingResource){
        ProcessingResource pr = (ProcessingResource)value;
        text = (String)pr.getFeatures().get("gate.NAME");
        ResourceData rData = (ResourceData)
                   Gate.getCreoleRegister().get(pr.getClass().getName());
        tipText = rData.getComment();
        iconName = rData.getIcon();
        if(iconName == null) iconName = "pr.gif";
      } else if (value instanceof ParameterDisjunction) {
        iconName = "param.gif";
        ParameterDisjunction pd = (ParameterDisjunction)value;
        text =  pd.getName();
        if(pd.size() > 1) text+=" [more...]";
        if(pd.getType().startsWith("gate.")){
          ResourceData rData = (ResourceData)
                               Gate.getCreoleRegister().get(pd.getType());
          if(rData != null) tipText = rData.getComment();
        }
      }
      //prepare the renderer
      Component comp = super.getTreeCellRendererComponent(tree, text, sel,
                                                          expanded, leaf,
                                                          row, hasFocus);
      setToolTipText(tipText);
      setIcon(getIcon(iconName));
      return this;
    }//public Component getTreeCellRendererComponent

    private Icon getIcon(String name){
      Icon res = (Icon)iconForName.get(name);
      if(res == null){
        try{
          res = new ImageIcon(new URL("gate:/img/" + name));
          iconForName.put(name, res);
        }catch(java.net.MalformedURLException mue){
          mue.printStackTrace(Err.getPrintWriter());
        }
      }
      return res;
    }

    private Map iconForName = new HashMap();
  }//class CustomTreeCellRenderer extends DefaultTreeCellRenderer

  class ModulesTableModel extends AbstractTableModel{
    public int getRowCount(){
      return
        Gate.getCreoleRegister().getPrInstances().size() - controller.size() -1;
    }

    public int getColumnCount(){
      return 2;
    }

    public String getColumnName(int columnIndex){
      switch(columnIndex){
        case 0: return "Name";
        case 1: return "Type";
        default: return "?";
      }
    }

    public Class getColumnClass(int columnIndex){
      return String.class;
    }

    public boolean isCellEditable(int rowIndex,  int columnIndex){
      return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex){
      //find the right PR
      List allPrs = Gate.getCreoleRegister().getPrInstances();
      allPrs.remove(controller);
      Iterator allPRsIter = allPrs.iterator();
      int index = -1;
      ProcessingResource pr =null;
      while(allPRsIter.hasNext() && index < rowIndex){
        pr = (ProcessingResource)allPRsIter.next();
        if(!controller.contains(pr))  index ++;
      }
      if(index == rowIndex && pr != null){
        switch(columnIndex){
          case -1: return pr;
          case 0: return pr.getFeatures().get("gate.NAME");
          case 1: return getResourceName(pr);
        }
      }
      return null;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
    }

  }//class ModulesTableModel extends AbstractTableModel

  class AddPRAction extends AbstractAction{
    AddPRAction(ProcessingResource aPR){
      super((String)aPR.getFeatures().get("gate.NAME"));
      this.pr = aPR;
    }

    public void actionPerformed(ActionEvent e){
      controller.add(pr);
      DefaultMutableTreeNode root = (DefaultMutableTreeNode)mainTTModel.
                                     getRoot();
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(pr, true);
      mainTTModel.insertNodeInto(node, root, root.getChildCount());
      mainTreeTable.expandPath(
            new TreePath(new Object[]{mainTTModel.getRoot(), node}));

      ResourceData rData = (ResourceData)
                          Gate.getCreoleRegister().get(pr.getClass().getName());
      List params = rData.getParameterList().getRuntimeParameters();
      Iterator paramsIter = params.iterator();
      List parameterDisjunctions = new ArrayList();
      while(paramsIter.hasNext()){
        ParameterDisjunction pDisj = new ParameterDisjunction(
                                          (List)paramsIter.next());
        parameterDisjunctions.add(pDisj);
        DefaultMutableTreeNode paramNode = new DefaultMutableTreeNode(pDisj);
        mainTTModel.insertNodeInto(paramNode, node, node.getChildCount());
        mainTreeTable.expandPath(new TreePath(new Object[]{
                                     mainTTModel.getRoot(), node, paramNode}));
      }
      paramsForPR.put(pr, parameterDisjunctions);

      modulesTableModel.fireTableDataChanged();
      this.setEnabled(false);
      ((Action)removeActionForPR.get(pr)).setEnabled(true);
    }//public void actionPerformed(ActionEvent e)
    ProcessingResource pr;
  }//class AddPRAction extends AbstractAction


  class RemovePRAction extends AbstractAction{
    RemovePRAction(ProcessingResource pr){
      super((String)pr.getFeatures().get("gate.NAME"));
      this.pr = pr;
    }

    public void actionPerformed(ActionEvent e){
      controller.remove(pr);
      paramsForPR.remove(pr);
      modulesTableModel.fireTableDataChanged();
      this.setEnabled(false);
      ((Action)addActionForPR.get(pr)).setEnabled(true);
    }
    ProcessingResource pr;
  }//class RemovePRAction extends AbstractAction

  class RunAction extends AbstractAction{
    RunAction(){
      super("Run");
    }
    public void actionPerformed(ActionEvent e){
      Runnable runnable = new Runnable(){
        public void run(){
          //stop edits
          if(mainTreeTable.getEditingColumn() != -1 &&
            mainTreeTable.getEditingRow() != -1){
            mainTreeTable.editingStopped(
                new ChangeEvent(mainTreeTable.getCellEditor(
                                mainTreeTable.getEditingRow(),
                                mainTreeTable.getEditingColumn())));
          }
          long startTime = System.currentTimeMillis();
          fireStatusChanged("Running " +
                            controller.getFeatures().get("gate.NAME"));
          fireProgressChanged(0);
          MainFrame.getInstance().showWaitDialog();
          Iterator prsIter = controller.iterator();
          while(prsIter.hasNext()){
            ProcessingResource pr = (ProcessingResource)prsIter.next();
            FeatureMap params = Factory.newFeatureMap();
            List someParams = (List)paramsForPR.get(pr);
            Iterator paramsIter = someParams.iterator();
            while(paramsIter.hasNext()){
              ParameterDisjunction pDisj =
                                        (ParameterDisjunction)paramsIter.next();
              if(pDisj.getValue() != null){
                params.put(pDisj.getName(), pDisj.getValue());
              }
            }
            try{
    //System.out.println("PR:" + pr.getFeatures().get("gate.NAME") + "\n" + params);
              Factory.setResourceParameters(pr, params);
            }catch(ResourceInstantiationException ie){
              JOptionPane.showMessageDialog(ApplicationViewer.this,
                                            "Could not set parameters for " +
                                            pr.getFeatures().get("gate.NAME") +
                                            ":\n" + ie.toString(),
                                            "Gate", JOptionPane.ERROR_MESSAGE);
              return;
            }
          }
          //run the thing
          prsIter = controller.iterator();
          int i = 0;
          while(prsIter.hasNext()){
            ProcessingResource pr = (ProcessingResource)prsIter.next();
            fireStatusChanged("Running " + pr.getFeatures().get("gate.NAME"));
            pr.run();
            try {
              pr.check();
            } catch(ExecutionException ee) {
              JOptionPane.showMessageDialog(ApplicationViewer.this,
                                            "Execution error:\n " +
                                            ee.toString(),
                                            "Gate", JOptionPane.ERROR_MESSAGE);
            }
            i++;
            fireProgressChanged(i / controller.size());
          }
          long endTime = System.currentTimeMillis();
          fireProcessFinished();
          fireStatusChanged(controller.getFeatures().get("gate.NAME") +
                            " run in " +
                            NumberFormat.getInstance().format(
                            (double)(endTime - startTime) / 1000) + " seconds");
          MainFrame.getInstance().hideWaitDialog();
        }
      };
      Thread thread = new Thread(runnable);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
  }

  class ParameterDisjunction{
    /**
     * gets a list of {@link gate.creole.Parameter}
     */
    public ParameterDisjunction(List options){
      this.options = options;
      Iterator paramsIter = options.iterator();
      names = new String[options.size()];
      int i = 0;
      while(paramsIter.hasNext()){
        names[i++] = ((Parameter)paramsIter.next()).getName();
      }
      values = new Object[options.size()];
      setSelectedIndex(0);
    }

    public void setSelectedIndex(int index){
      selectedIndex = index;
      currentParameter = (Parameter)options.get(selectedIndex);
      typeName = currentParameter.getTypeName();
      if(values[selectedIndex] == null){
        try {
          values[selectedIndex] = currentParameter.getDefaultValue();
        } catch(Exception e) {
          values[selectedIndex] = "";
        }
      }
//      tableModel.fireTableDataChanged();
    }

    public int size() {
      return options.size();
    }

    public Boolean getRequired() {
      return new Boolean(!currentParameter.isOptional());
    }

    public String getName() {
      return currentParameter.getName();
    }

    public String getComment(){
      return currentParameter.getComment();
    }

    public String getType(){
      return currentParameter.getTypeName();
    }

    public String[] getNames(){
      return names;
    }

    public void setValue(Object value){
      Object oldValue = values[selectedIndex];
      if(value instanceof String){
        if(typeName.equals("java.lang.String")){
          values[selectedIndex] = value;
        }else{
          try{
            values[selectedIndex] = currentParameter.
                                    calculateValueFromString((String)value);
          }catch(Exception e){
            values[selectedIndex] = oldValue;
            JOptionPane.showMessageDialog(ApplicationViewer.this,
                                          "Invalid value!\n" +
                                          "Is it the right type?",
                                          "Gate", JOptionPane.ERROR_MESSAGE);
          }
        }
      }else{
        values[selectedIndex] = value;
      }
    }
    public Object getValue(){
      if(values[selectedIndex] != null) {
        return values[selectedIndex];
      } else {
        //no value set; use the most currently used one of the given type
        if(getType().startsWith("gate.")){
          Stack instances = ((ResourceData)
                              Gate.getCreoleRegister().get(getType())).
                                  getInstantiations();
          if(instances != null && !instances.isEmpty()) return instances.peek();
          else return null;
        } else {
          return null;
        }
      }
    }


    int selectedIndex;
    List options;
    boolean required;
    String typeName;
    String name;
    String[] names;
    Parameter currentParameter;
    Object[] values;
  }//class ParameterDisjunction

  class ParameterDisjunctionEditor extends DefaultCellEditor{
    public ParameterDisjunctionEditor(){
      super(new JComboBox());
      combo = (JComboBox)super.getComponent();
    }

    public Component getTreeCellEditorComponent(JTree tree,
                                                Object value,
                                                boolean isSelected,
                                                boolean expanded,
                                                boolean leaf,
                                                int row){
     ParameterDisjunction pDisj = (ParameterDisjunction)
                                  ((DefaultMutableTreeNode)value).
                                  getUserObject();
     combo.setModel(new DefaultComboBoxModel(pDisj.getNames()));
     combo.setSelectedItem(pDisj.getName());
     return combo;
    }
    public Object getCellEditorValue(){
      return new Integer(combo.getSelectedIndex());
    }
    JComboBox combo;
  }

  class ParameterValueRenderer extends ObjectRenderer {
    public Component getTableCellRendererComponent(JTable table,
                                               Object value,
                                               boolean isSelected,
                                               boolean hasFocus,
                                               int row,
                                               int column){
      //value = ((DefaultMutableTreeNode)value).getUserObject();
      if(value instanceof FeatureBearer){
        String name = (String)
                        ((FeatureBearer)value).getFeatures().get("gate.NAME");
        if(name != null){
          return super.getTableCellRendererComponent(table, name, isSelected,
                                                     hasFocus, row, column);
        }
      }
      return super.getTableCellRendererComponent(table, value, isSelected,
                                                 hasFocus, row, column);
    }
  }

  class ParameterValueEditor extends AbstractCellEditor
                             implements TableCellEditor{
    ParameterValueEditor(){
      combo = new JComboBox();
      textField = new JTextField();
      button = new JButton(new ImageIcon(getClass().getResource(
                               "/gate/resources/img/loadFile.gif")));
      button.setToolTipText("Set from file...");
      button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JFileChooser fileChooser = MainFrame.getInstance().fileChooser;
          int res = fileChooser.showOpenDialog(ApplicationViewer.this);
          if(res == fileChooser.APPROVE_OPTION){
            try {
              textField.setText(fileChooser.getSelectedFile().
                                toURL().toExternalForm());
            } catch(IOException ioe){}
          }
        }
      });
      textButtonBox = Box.createHorizontalBox();
      textButtonBox.add(textField, button);
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column){

      type = ((ParameterDisjunction)
              ((DefaultMutableTreeNode)
              mainTreeTable.getTree().getPathForRow(row).getLastPathComponent())
              .getUserObject()).getType();
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().get(type);
      if(rData != null){
        //Gate type
        combo.setModel(new DefaultComboBoxModel(
                                          rData.getInstantiations().toArray()));
        combo.setSelectedItem(value);
        combo.setRenderer(new ComboRenderer());
        comboUsed = true;
        return combo;
      }else{
        if(value != null) textField.setText(value.toString());
        comboUsed = false;
        if(type.equals("java.net.URL")){
          return textButtonBox;
        }else return textField;
      }
    }//getTableCellEditorComponent

    public Object getCellEditorValue(){
      if(comboUsed) return combo.getSelectedItem();
      else return textField.getText();
    }//public Object getCellEditorValue()

    class ComboRenderer extends DefaultListCellRenderer{
      public Component getListCellRendererComponent(JList list,
                                                    Object value,
                                                    int index,
                                                    boolean isSelected,
                                                    boolean cellHasFocus){
        if(value instanceof FeatureBearer){
          String name = (String)(
                          (FeatureBearer)value).getFeatures().get("gate.NAME");
          if(name != null){
            return super.getListCellRendererComponent(list, name, index,
                                                      isSelected, cellHasFocus);
          }
        }
        return super.getListCellRendererComponent(list, value, index,
                                                   isSelected, cellHasFocus);
      }
    }

    String type;
    JComboBox combo;
    JTextField textField;
    boolean comboUsed;
    JButton button;
    Box textButtonBox;
  }
/*
  XJTable prsTable;
  XJTable paramsTable;
  PRListTableModel prsTableModel;
  PRParametersTableModel paramsTableModel;
*/

  SerialController controller;
  ResourceHandle handle;
  JTreeTable mainTreeTable;
  PRsAndParamsTTModel mainTTModel;
  JPopupMenu popup;
  JMenu addMenu;
  JMenu removeMenu;
  XJTable modulesTable;
  ModulesTableModel modulesTableModel;
  JButton addModuleBtn;
  JButton removeModuleBtn;
  JButton upBtn;
  JButton downBtn;
  Action runAction;

  /**
   * maps from ProcessingResource to List of ParameterDisjunction
   */
  Map paramsForPR;
  /**
   * Maps from pr to AddPRAction
   */
  Map addActionForPR;
  Map removeActionForPR;
  private transient Vector statusListeners;
  private transient Vector progressListeners;
  public void resourceLoaded(CreoleEvent e) {
    if(e.getResource() instanceof ProcessingResource){
      updateActions();
      modulesTableModel.fireTableDataChanged();
    }else{
      mainTreeTable.repaint();
    }
  }

  public void resourceUnloaded(CreoleEvent e) {
    Resource res = e.getResource();
    if(res.getFeatures().get("gate.HIDDEN") == null ||
       ((String)res.getFeatures().get("gate.HIDDEN")).equalsIgnoreCase("true"))
    return;
    updateActions();
    modulesTableModel.fireTableDataChanged();
  }

  public void datastoreOpened(CreoleEvent e) {
  }
  public void datastoreCreated(CreoleEvent e) {
  }
  public void datastoreClosed(CreoleEvent e) {
  }
  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }
  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null ? new Vector(2) : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }
  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
  }
  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }
  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null ? new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }
  protected void fireProgressChanged(int e) {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).progressChanged(e);
      }
    }
  }
  protected void fireProcessFinished() {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).processFinished();
      }
    }
  }

/*
  class PRListTableModel extends AbstractTableModel{
    public int getRowCount(){
      return controller.size() + 1;
    }

    public int getColumnCount(){
      return 2;
    }

    public String getColumnName(int columnIndex){
      switch (columnIndex){
        case 0: return "Processing resource";
        case 1: return "Type";
        default: return "?";
      }
    }

    public Class getColumnClass(int columnIndex){
      switch (columnIndex){
        case 0: return ProcessingResource.class;
        case 1: return String.class;
        default: return Object.class;
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return columnIndex == 0;// && rowIndex == controller.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex){
      if(rowIndex >= controller.size()){
        switch (columnIndex){
          case 0: return null;
          case 1: return "- no type -";
          default: return Object.class;
        }
      }
      switch (columnIndex){
        case 0: return controller.get(rowIndex);
        case 1: return ((ProcessingResource)controller.get(rowIndex)).getClass().toString();
        default: return Object.class;
      }
    }

    public void setValueAt(Object aValue,
                       int rowIndex,
                       int columnIndex){
      if(columnIndex == 0){
        if(rowIndex >= controller.size()){
          if(aValue != null) controller.add(aValue);
        }else{
          if(aValue != null) controller.set(rowIndex, aValue);
          else controller.remove(rowIndex);
        }
      }
    }

  }//class PRListTableModel extends AbstractTableModel
*/
/*
  class PRRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column){

      String name = null;
      if(value != null){
        ProcessingResource res = (ProcessingResource) value;
        name = (String)res.getFeatures().get("gate.NAME");
        if(name == null){
          name = "No name: " + res.getClass().toString();
        }
      }else{
        name = "< Add new... >";
      }
      return super.getTableCellRendererComponent(table, name, isSelected,
                                                 hasFocus, 0, 0);
    }
  }
*/
/*
  class PREditor extends DefaultCellEditor{
    public PREditor(){
      super(new JComboBox());
      combo = (JComboBox)getComponent();
      prsByName = new TreeMap();
      setClickCountToStart(2);
    }

    public Component getTableCellEditorComponent(JTable table,
                                             Object value,
                                             boolean isSelected,
                                             int row,
                                             int column){

      prsByName.clear();
      Iterator prsIter = project.getPRList().iterator();
      while(prsIter.hasNext()){
        PRHandle handle = (PRHandle)prsIter.next();
        ProcessingResource pr = (ProcessingResource)handle.resource;
        String prName = (String)handle.resource.getFeatures().get("gate.NAME");
        if(prName == null){
          prName = "No name: " + pr.getClass().toString();
        }
        prsByName.put(prName, pr);
      }//while(prsIter.hasNext())
      if(prsByName.isEmpty()) return null;
      prsByName.put("< Delete! >", null);
      combo.setModel(new DefaultComboBoxModel(prsByName.keySet().toArray()));
      if(value != null){
        //select the current value
        try{
          String currentName = (String)((ProcessingResource)value).
                               getFeatures().get("gate.NAME");
          if(prsByName.containsKey(currentName)){
            combo.setSelectedItem(currentName);
          }
        }catch(Exception e){}
      }else{
        combo.setSelectedItem("< Delete! >");
      }
      return super.getTableCellEditorComponent(table, value, isSelected,
                                               row, column);
    }

    public Object getCellEditorValue(){
      if(prsByName == null || combo.getSelectedItem() == null) return null;
      ProcessingResource res = (ProcessingResource)prsByName.get(combo.getSelectedItem());
      return res;
    }

    JComboBox combo;
    Map prsByName;
  }//class PREditor extends DefaultCellEditor
*/
/*
  class PRParametersTableModel extends AbstractTableModel{
    public int getRowCount(){
      return controller.size();
    }

    public int getColumnCount(){
      return 4;
    }

    public String getColumnName(int columnIndex){
      switch (columnIndex){
        case 0: return "Processing resource";
        case 1: return "Parameter name";
        case 2: return "Type";
        case 3: return "Value";
        default: return "?";
      }
    }

    public Class getColumnClass(int columnIndex){
      switch (columnIndex){
        case 0: return ProcessingResource.class;
        case 1: return String.class;
        case 2: return String.class;
        case 3: return String.class;
        default: return Object.class;
      }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex){
      return columnIndex == 3;
    }

    public Object getValueAt(int rowIndex, int columnIndex){
      return null;
    }

    public void setValueAt(Object aValue,
                       int rowIndex,
                       int columnIndex){
    }
  }//class PRParametersTableModel
*/
}