package gate.gui;

import gate.creole.*;
import gate.*;
import gate.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.IOException;

public class ApplicationViewer extends AbstractVisualResource {

  public ApplicationViewer(Controller controller, ApplicationHandle handle) {
    if(controller instanceof SerialController){
      this.controller = (SerialController)controller;
      this.handle = handle;
      this.project = handle.project;
      this.popup = handle.popup;
      initLocalData();
      initGuiComponents();
      initListeners();
    }else{
      throw new UnsupportedOperationException(
        "Editing of controllers implemented only for serial controllers!");
    }
  }

  protected void initLocalData(){
    paramsForPR = new HashMap();
    addActionForPR = new HashMap();
    removeActionForPR = new HashMap();
    Iterator prIter = project.getPRList().iterator();
    while(prIter.hasNext()){
      ProcessingResource pr = (ProcessingResource)
                              ((PRHandle)prIter.next()).getResource();
      AddPRAction addAction = new AddPRAction(pr);
      RemovePRAction remAction = new RemovePRAction(pr);
      remAction.setEnabled(false);
      addActionForPR.put(pr, addAction);
      removeActionForPR.put(pr, remAction);
    }
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
    addModuleBtn = new JButton("",
                               new ImageIcon(
                                 ApplicationViewer.class.getResource(
                                 "/gate/resources/img/left2.gif"))
                               );
    addModuleBtn.setAlignmentX(JComponent.CENTER_ALIGNMENT);
    removeModuleBtn = new JButton("",
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
    upBtn = new JButton(
                new ImageIcon(
                    ApplicationViewer.class.getResource(
                    "/gate/resources/img/up.gif")));
    downBtn = new JButton(
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
    modulesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    scroller = new JScrollPane(modulesTable);
    scroller.setBorder(BorderFactory.createTitledBorder("Available components"));

    mainBox.add(scroller);

    this.add(mainBox, BorderLayout.CENTER);

    popup.add(new RunAction());
    addMenu = new JMenu("Add");
    removeMenu = new JMenu("Remove");
    updateActions();
    popup.add(addMenu);
    popup.add(removeMenu);
  }

  protected void initListeners(){
    this.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)){
          popup.show(ApplicationViewer.this, e.getX(), e.getY());
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
        }else{
          List actions = new ArrayList();
          for(int i = 0; i < rows.length; i++){
            Object node = mainTreeTable.getTree().getPathForRow(rows[i]).getLastPathComponent();
            if(node instanceof ProcessingResource && controller.contains(node)){
              Action act = (Action)removeActionForPR.get(node);
              if(act != null) actions.add(act);
            }else{
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
        }
      }
    });

    upBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean changed= false;
        int rows[] = mainTreeTable.getSelectedRows();
        List selectedComponents = new ArrayList();
        Arrays.sort(rows);
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              ApplicationViewer.this,
              "Please select some components to be moved from the list of used components!\n" ,
              "Gate", JOptionPane.ERROR_MESSAGE);
        }else{
          for(int i = 0; i < rows.length; i++){
            TreePath path = mainTreeTable.getTree().getPathForRow(rows[i]);
            Object node = path.getLastPathComponent();
            if(node instanceof ProcessingResource && controller.contains(node)){
              int index = controller.indexOf(node);
              //move the module up
              if(index > 0){
                controller.remove(index);
                index--;
                controller.add(index, node);
                changed = true;
              }
              selectedComponents.add(node);
            }else{
              JOptionPane.showMessageDialog(
                  ApplicationViewer.this,
                  "Only processing resources can be moved!\n" +
                  "(Processing resources are the nodes from the first level of the tree)" ,
                  "Gate", JOptionPane.ERROR_MESSAGE);
            }
          }
          if(changed){
            mainTTModel.dataChanged();
            //restore the selection
            mainTreeTable.clearSelection();
            Iterator selIter = selectedComponents.iterator();
            while(selIter.hasNext()){
              int row = mainTreeTable.getTree().getRowForPath(new TreePath(
                          new Object[]{controller, selIter.next()}));
              mainTreeTable.addRowSelectionInterval(row, row);
            }
          }
        }
      }//public void actionPerformed(ActionEvent e)
    });

    downBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rows[] = mainTreeTable.getSelectedRows();
        boolean changed = false;
        List selectedComponents = new ArrayList();
        Arrays.sort(rows);
        if(rows == null || rows.length == 0){
          JOptionPane.showMessageDialog(
              ApplicationViewer.this,
              "Please select some components to be moved from the list of used components!\n" ,
              "Gate", JOptionPane.ERROR_MESSAGE);
        }else{
          for(int i = rows.length -1; i >= 0; i--){
            Object node = mainTreeTable.getTree().getPathForRow(rows[i]).getLastPathComponent();
            if(node instanceof ProcessingResource && controller.contains(node)){
              int index = controller.indexOf(node);
              //move the module down
              if(index < controller.size() - 1){
                controller.remove(index);
                index++;
                controller.add(index, node);
                changed = true;
              }
              selectedComponents.add(node);
            }else{
              JOptionPane.showMessageDialog(
                  ApplicationViewer.this,
                  "Only processing resources can be moved!\n" +
                  "(Processing resources are the nodes from the first level of the tree)" ,
                  "Gate", JOptionPane.ERROR_MESSAGE);
            }
          }//for(int i = 0; i < rows.length; i++)
          if(changed){
            mainTTModel.dataChanged();
           //restore the selection
            mainTreeTable.clearSelection();
            Iterator selIter = selectedComponents.iterator();
            while(selIter.hasNext()){
              int row = mainTreeTable.getTree().getRowForPath(new TreePath(
                          new Object[]{controller, selIter.next()}));
              mainTreeTable.addRowSelectionInterval(row, row);
            }
          }
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
    Iterator prIter = project.getPRList().iterator();
    while(prIter.hasNext()){
      ProcessingResource pr = (ProcessingResource)
                              ((PRHandle)prIter.next()).getResource();
      if(!addActionForPR.containsKey(pr)){
        AddPRAction addAction = new AddPRAction(pr);
        RemovePRAction remAction = new RemovePRAction(pr);
        remAction.setEnabled(false);
        addActionForPR.put(pr, addAction);
        removeActionForPR.put(pr, remAction);
      }
    }
    addMenu.removeAll();
    removeMenu.removeAll();
    Iterator addActionsIter = addActionForPR.values().iterator();
    while(addActionsIter.hasNext()){
      addMenu.add((Action)addActionsIter.next());
    }

    Iterator remActionsIter = removeActionForPR.values().iterator();
    while(remActionsIter.hasNext()){
      removeMenu.add((Action)remActionsIter.next());
    }
  }

  public JPopupMenu getPopup(){
    updateActions();
    return popup;
  }

  protected String getResourceName(Resource res){
    ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                         get(res.getClass().getName());
    if(rData != null) return rData.getName();
    else return res.getClass().getName();
  }

  class PRsAndParamsTTModel extends AbstractTreeTableModel{
    PRsAndParamsTTModel(SerialController aController){
      super(aController);
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
      if(node instanceof SerialController){
        return null;
      }else if (node instanceof ProcessingResource){
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
      return null;
    }

    public boolean isCellEditable(Object node, int column){
      if(column == 2) return node instanceof ParameterDisjunction;
      if(column == 0){
        return node instanceof ParameterDisjunction &&
               ((ParameterDisjunction)node).size() > 1 ;
      }
      return false;
    }

    public void setValueAt(Object aValue, Object node, int column){
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

    public Object getChild(Object parent, int index){
      if(parent instanceof SerialController){
        SerialController sc = (SerialController)parent;
        return sc.get(index);
      }else if (parent instanceof ProcessingResource){
        ProcessingResource pr = (ProcessingResource)parent;
        List paramsList = (List)paramsForPR.get(pr);
        return (ParameterDisjunction)paramsList.get(index);
      }else return null;
    }

    public int getChildCount(Object parent){
      if(parent instanceof SerialController){
        SerialController sc = (SerialController)parent;
        return sc.size();
      }else if (parent instanceof ProcessingResource){
        ProcessingResource pr = (ProcessingResource)parent;
        List paramsList = (List)paramsForPR.get(pr);
        return paramsList==null ? 0 : paramsList.size();
      }else  return 0;
    }

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
      if (value instanceof ProcessingResource){
        ProcessingResource pr = (ProcessingResource)value;
        text = (String)pr.getFeatures().get("NAME");
        tipText = ((ResourceData)
                   Gate.getCreoleRegister().get(pr.getClass().getName())
                   ).getComment();
        Iterator prIter = project.getPRList().iterator();
        boolean done = false;
        while(!done && prIter.hasNext()){
          PRHandle handle = (PRHandle)prIter.next();
          if(handle.getResource() == pr){
            done = true;
            Icon icon = handle.getSmallIcon();
            setOpenIcon(icon);
            setClosedIcon(icon);
            setLeafIcon(icon);
          }
        }
      }else if (value instanceof ParameterDisjunction){
        Icon icon = new ImageIcon(getClass().
                                  getResource("/gate/resources/img/param.gif"));
        setOpenIcon(icon);
        setClosedIcon(icon);
        setLeafIcon(icon);

        ParameterDisjunction pd = (ParameterDisjunction)value;
        text =  pd.getName();
        if(pd.size() > 1) text+=" [more...]";
        if(pd.getType().startsWith("gate.")){
          ResourceData rData = (ResourceData)
                               Gate.getCreoleRegister().get(pd.getType());
          if(rData != null) tipText = rData.getComment();
        }
      }
      setToolTipText(tipText);
      //prepare the renderer
      Component comp = super.getTreeCellRendererComponent(tree, text, sel,
                                                          expanded, leaf,
                                                          row, hasFocus);
      return this;
    }//public Component getTreeCellRendererComponent
  }//class CustomTreeCellRenderer extends DefaultTreeCellRenderer

  class ModulesTableModel extends AbstractTableModel{
    public int getRowCount(){
      return project.getPRList().size() - controller.size();
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
      Iterator allPRsIter = project.getPRList().iterator();
      int index = -1;
      ProcessingResource pr =null;
      while(allPRsIter.hasNext() && index < rowIndex){
        pr = (ProcessingResource)((PRHandle)allPRsIter.next()).getResource();
        if(!controller.contains(pr))  index ++;
      }
      if(index == rowIndex && pr != null){
        switch(columnIndex){
          case -1: return pr;
          case 0: return pr.getFeatures().get("NAME");
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
      super((String)aPR.getFeatures().get("NAME"));
      this.pr = aPR;
    }

    public void actionPerformed(ActionEvent e){
      controller.add(pr);
      ResourceData rData = (ResourceData)
                           Gate.getCreoleRegister().get(pr.getClass().getName());
      List params = rData.getParameterList().getRuntimeParameters();
      Iterator paramsIter = params.iterator();
      List parameterDisjunctions = new ArrayList();
      while(paramsIter.hasNext()){
        parameterDisjunctions.add(
          new ParameterDisjunction((List)paramsIter.next())
        );
      }
      paramsForPR.put(pr, parameterDisjunctions);
      mainTTModel.dataChanged();
      modulesTableModel.fireTableDataChanged();
      this.setEnabled(false);
      ((Action)removeActionForPR.get(pr)).setEnabled(true);
    }//public void actionPerformed(ActionEvent e)
    ProcessingResource pr;
  }//class AddPRAction extends AbstractAction


  class RemovePRAction extends AbstractAction{
    RemovePRAction(ProcessingResource pr){
      super((String)pr.getFeatures().get("NAME"));
      this.pr = pr;
    }

    public void actionPerformed(ActionEvent e){
      controller.remove(pr);
      paramsForPR.remove(pr);
      mainTTModel.dataChanged();
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
      Iterator prsIter = controller.iterator();
      while(prsIter.hasNext()){
        ProcessingResource pr = (ProcessingResource)prsIter.next();
        FeatureMap params = Factory.newFeatureMap();
        List someParams = (List)paramsForPR.get(pr);
        Iterator paramsIter = someParams.iterator();
        while(paramsIter.hasNext()){
          ParameterDisjunction pDisj = (ParameterDisjunction)paramsIter.next();
          if(pDisj.getValue() != null){
            params.put(pDisj.getName(), pDisj.getValue());
          }
        }
        try{
//System.out.println("PR:" + pr.getFeatures().get("NAME") + "\n" + params);
          Factory.setResourceParameters(pr, params);
        }catch(java.beans.IntrospectionException ie){
          JOptionPane.showMessageDialog(ApplicationViewer.this,
                                        "Could not set parameters for " +
                                        pr.getFeatures().get("NAME") + ":\n" +
                                        ie.toString(),
                                        "Gate", JOptionPane.ERROR_MESSAGE);
          return;
        }catch(java.lang.reflect.InvocationTargetException ite){
          JOptionPane.showMessageDialog(ApplicationViewer.this,
                                        "Could not set parameters for " +
                                        pr.getFeatures().get("NAME") + ":\n" +
                                        ite.toString(),
                                        "Gate", JOptionPane.ERROR_MESSAGE);
          return;
        }catch(IllegalAccessException iae){
          JOptionPane.showMessageDialog(ApplicationViewer.this,
                                        "Could not set parameters for " +
                                        pr.getFeatures().get("NAME") + ":\n" +
                                        iae.toString(),
                                        "Gate", JOptionPane.ERROR_MESSAGE);
          return;
        }catch(GateException ge){
          JOptionPane.showMessageDialog(ApplicationViewer.this,
                                        "Could not set parameters for " +
                                        pr.getFeatures().get("NAME") + ":\n" +
                                        ge.toString(),
                                        "Gate", JOptionPane.ERROR_MESSAGE);
          return;
        }
      }
      controller.run();
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
        try{
          values[selectedIndex] = currentParameter.getDefaultValue();
        }catch(Exception e){
          values[selectedIndex] = "";
        }
      }
//      tableModel.fireTableDataChanged();
    }

    public int size(){
      return options.size();
    }

    public Boolean getRequired(){
      return new Boolean(!currentParameter.isOptional());
    }

    public String getName(){
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
      }else{
        //no value set; use the most currently used one of the given type
        if(getType().startsWith("gate.")){
          Stack instances = ((ResourceData)Gate.getCreoleRegister().get(getType())).
                 getInstantiations();
          if(instances != null && !instances.isEmpty()) return instances.peek();
          else return null;
        }else{
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
     ParameterDisjunction pDisj = (ParameterDisjunction)value;
     combo.setModel(new DefaultComboBoxModel(pDisj.getNames()));
     combo.setSelectedItem(pDisj.getName());
     return combo;
    }
    public Object getCellEditorValue(){
      return new Integer(combo.getSelectedIndex());
    }
    JComboBox combo;
  }

  class ParameterValueRenderer extends DefaultTableCellRenderer{
    public Component getTableCellRendererComponent(JTable table,
                                               Object value,
                                               boolean isSelected,
                                               boolean hasFocus,
                                               int row,
                                               int column){
      if(value instanceof FeatureBearer){
        String name = (String)((FeatureBearer)value).getFeatures().get("NAME");
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
          JFileChooser fileChooser = handle.project.frame.fileChooser;
          int res = fileChooser.showOpenDialog(ApplicationViewer.this);
          if(res == fileChooser.APPROVE_OPTION){
            try{
              textField.setText(fileChooser.getSelectedFile().
                                toURL().toExternalForm());
            }catch(IOException ioe){}
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
      type = ((ParameterDisjunction)mainTreeTable.getTree().getPathForRow(row).
              getLastPathComponent()).getType();
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().get(type);
      if(rData != null){
        //Gate type
        combo.setModel(new DefaultComboBoxModel(rData.getInstantiations().toArray()));
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
          String name = (String)((FeatureBearer)value).getFeatures().get("NAME");
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
  ProjectData project;
  ApplicationHandle handle;
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

  /**
   * maps from ProcessingResource to List of ParameterDisjunction
   */
  Map paramsForPR;
  /**
   * Maps from pr to AddPRAction
   */
  Map addActionForPR;
  Map removeActionForPR;

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
        name = (String)res.getFeatures().get("NAME");
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
        String prName = (String)handle.resource.getFeatures().get("NAME");
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
                               getFeatures().get("NAME");
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