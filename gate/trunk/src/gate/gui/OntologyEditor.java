package gate.gui;

import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.ontology.*;
import gate.event.*;
import gate.gui.ontology.DeleteOntologyResourceAction;
import gate.gui.ontology.DetailsTableCellRenderer;
import gate.gui.ontology.DetailsTableModel;
import gate.gui.ontology.InstanceAction;
import gate.gui.ontology.OntoTreeCellRenderer;
import gate.gui.ontology.OntologyItemComparator;
import gate.gui.ontology.PropertyAction;
import gate.gui.ontology.PropertyDetailsTableCellRenderer;
import gate.gui.ontology.PropertyDetailsTableModel;
import gate.gui.ontology.SubClassAction;
import gate.gui.ontology.SubPropertyAction;
import gate.gui.ontology.TopClassAction;
import gate.gui.ontology.TreeNodeSelectionListener;
import gate.gui.ontology.ValuesSelectionAction;
import gate.swing.XJTable;
import gate.util.GateRuntimeException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

public class OntologyEditor extends AbstractVisualResource
                                                          implements
                                                          ResizableVisualResource,
                                                          OntologyModificationListener {

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.AbstractVisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target) {
    this.taxonomy = (Taxonomy)target;
    if(target instanceof Ontology) {
      this.ontology = (Ontology)target;
      detailsTableModel.setOntology(ontology);
      detailsTableModel.setOntologyMode(true);

      topClassAction.setOntology(ontology);
      subClassAction.setOntology(ontology);
      instanceAction.setOntology(ontology);
      propertyAction.setOntology(ontology);
      subPropertyAction.setOntology(ontology);
      deleteOntoResourceAction.setOntology(ontology);
      ontologyMode = true;
    }
    else {
      ontologyMode = false;
    }
    rebuildModel();
    taxonomy.addOntologyModificationListener(this);
  }

  /**
   * Init method, that creates this object and returns this object as a
   * resource
   */
  public Resource init() throws ResourceInstantiationException {
    super.init();
    initLocalData();
    initGUIComponents();
    initListeners();
    return this;
  }

  /**
   * Initialize the local data
   */
  protected void initLocalData() {
    itemComparator = new OntologyItemComparator();
    listeners = new ArrayList();
  }

  /**
   * Initialize the GUI Components
   */
  protected void initGUIComponents() {
    this.setLayout(new BorderLayout());

    mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    this.add(mainSplit, BorderLayout.CENTER);
    subSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    mainSplit.setLeftComponent(subSplit);

    rootNode = new DefaultMutableTreeNode(null, true);
    treeModel = new DefaultTreeModel(rootNode);
    tree = new JTree(treeModel);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new OntoTreeCellRenderer());
    tree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    JScrollPane scroller = new JScrollPane(tree);
    // enable tooltips for the tree
    ToolTipManager.sharedInstance().registerComponent(tree);
    subSplit.setTopComponent(scroller);
    scroller.setBorder(new TitledBorder("Classes and Instances"));

    // ----------------------------------------------
    propertyRootNode = new DefaultMutableTreeNode(null, true);
    propertyTreeModel = new DefaultTreeModel(propertyRootNode);
    propertyTree = new JTree(propertyTreeModel);
    propertyTree.setRootVisible(false);
    propertyTree.setShowsRootHandles(true);
    propertyTree.setCellRenderer(new OntoTreeCellRenderer());
    propertyTree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    JScrollPane propertyScroller = new JScrollPane(propertyTree);
    // enable tooltips for the tree
    ToolTipManager.sharedInstance().registerComponent(propertyTree);
    subSplit.setBottomComponent(propertyScroller);
    propertyScroller.setBorder(new TitledBorder("Properties"));
    // -----------------------------------------------

    detailsTableModel = new DetailsTableModel();

    // ----------------
    propertyDetailsTableModel = new PropertyDetailsTableModel();
    // ----------------

    detailsTable = new XJTable(detailsTableModel);
    ((XJTable)detailsTable).setSortable(false);
    DetailsTableCellRenderer renderer = new DetailsTableCellRenderer(
            detailsTableModel);
    detailsTable.getColumnModel().getColumn(DetailsTableModel.EXPANDED_COLUMN)
            .setCellRenderer(renderer);
    detailsTable.getColumnModel().getColumn(DetailsTableModel.LABEL_COLUMN)
            .setCellRenderer(renderer);
    detailsTable.setShowGrid(false);
    detailsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    detailsTable.setColumnSelectionAllowed(false);
    detailsTable.setRowSelectionAllowed(true);

    detailsTable.setTableHeader(null);
    detailsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    detailsTableScroller = new JScrollPane(detailsTable);
    detailsTableScroller.getViewport().setOpaque(true);
    detailsTableScroller.getViewport().setBackground(
            detailsTable.getBackground());
    mainSplit.setRightComponent(detailsTableScroller);

    // -------------------
    propertyDetailsTable = new XJTable(propertyDetailsTableModel);
    ((XJTable)propertyDetailsTable).setSortable(false);
    PropertyDetailsTableCellRenderer propertyRenderer = new PropertyDetailsTableCellRenderer(
            propertyDetailsTableModel);
    propertyDetailsTable.getColumnModel().getColumn(
            DetailsTableModel.EXPANDED_COLUMN)
            .setCellRenderer(propertyRenderer);
    propertyDetailsTable.getColumnModel().getColumn(
            DetailsTableModel.LABEL_COLUMN).setCellRenderer(propertyRenderer);
    propertyDetailsTable.setShowGrid(false);
    propertyDetailsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    propertyDetailsTable.setColumnSelectionAllowed(false);
    propertyDetailsTable.setRowSelectionAllowed(true);

    propertyDetailsTable.setTableHeader(null);
    propertyDetailsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    propertyDetailsTableScroller = new JScrollPane(propertyDetailsTable);
    propertyDetailsTableScroller.getViewport().setOpaque(true);
    propertyDetailsTableScroller.getViewport().setBackground(
            propertyDetailsTable.getBackground());

    // --------------------
    toolBar = new JToolBar(JToolBar.HORIZONTAL);
    topClassAction = new TopClassAction("", MainFrame
            .getIcon("ontology-topclass"));
    topClass = new JButton(topClassAction);
    topClass.setToolTipText("Add New Top Class");

    subClassAction = new SubClassAction("", MainFrame
            .getIcon("ontology-subclass"));
    addTreeNodeSelectionListener(subClassAction);
    subClass = new JButton(subClassAction);
    subClass.setToolTipText("Add New Sub Class");

    instanceAction = new InstanceAction("", MainFrame
            .getIcon("ontology-instance"));
    addTreeNodeSelectionListener(instanceAction);
    instance = new JButton(instanceAction);
    instance.setToolTipText("Add New Instance");

    propertyAction = new PropertyAction("", MainFrame
            .getIcon("ontology-property"));
    addTreeNodeSelectionListener(propertyAction);
    property = new JButton(propertyAction);
    property.setToolTipText("Add New Property");

    subPropertyAction = new SubPropertyAction("", MainFrame.getIcon("ontology-subproperty"));
    addTreeNodeSelectionListener(subPropertyAction);
    subProperty = new JButton(subPropertyAction);
    subProperty.setToolTipText("Add New Sub Property");
    
    deleteOntoResourceAction = new DeleteOntologyResourceAction("", MainFrame
            .getIcon("delete"));
    addTreeNodeSelectionListener(deleteOntoResourceAction);
    delete = new JButton(deleteOntoResourceAction);
    delete.setToolTipText("Delete the selected nodes");

    toolBar.setFloatable(false);
    toolBar.add(topClass);
    toolBar.add(subClass);
    toolBar.add(instance);
    toolBar.add(property);
    toolBar.add(subProperty);
    toolBar.add(delete);
    this.add(toolBar, BorderLayout.NORTH);
  }

  protected void initListeners() {
    tree.getSelectionModel().addTreeSelectionListener(
            new TreeSelectionListener() {
              public void valueChanged(TreeSelectionEvent e) {
                int[] selectedRows = tree.getSelectionRows();
                if(selectedRows != null && selectedRows.length > 0) {
                  selectedNodes = new ArrayList();
                  for(int i = 0; i < selectedRows.length; i++) {
                    DefaultMutableTreeNode node1 = (DefaultMutableTreeNode)tree
                            .getPathForRow(selectedRows[i])
                            .getLastPathComponent();
                    selectedNodes.add(node1);
                  }
                  detailsTableModel
                          .setItem(((DefaultMutableTreeNode)selectedNodes
                                  .get(0)).getUserObject());
                  enableDisableToolBarComponents();
                  fireTreeNodeSelectionChanged(selectedNodes);
                  propertyTree.clearSelection();
                }
                mainSplit.setRightComponent(detailsTableScroller);
                mainSplit.updateUI();
              }
            });

    tree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        if(SwingUtilities.isRightMouseButton(me)) {
          if(selectedNodes.size() > 1) return;
          if(((DefaultMutableTreeNode)selectedNodes.get(0)).getUserObject() instanceof OInstance) {
            final JPopupMenu menu = new JPopupMenu();
            final JMenu addProperty = new JMenu("Properties");
            menu.add(addProperty);

            Set props = ontology.getPropertyDefinitions();
            Iterator iter = props.iterator();
            while(iter.hasNext()) {
              final Property p = (Property)iter.next();
              if(p
                      .isValidDomain((OInstance)((DefaultMutableTreeNode)selectedNodes
                              .get(0)).getUserObject())) {
                JMenuItem item = new JMenuItem(p.getName());
                addProperty.add(item);
                item.addActionListener(new ActionListener() {
                  public void actionPerformed(ActionEvent ae) {
                    if(p instanceof ObjectProperty) {
                      Set instances = ontology.getInstances();
                      ArrayList instList = new ArrayList();
                      Iterator instIter = instances.iterator();
                      while(instIter.hasNext()) {
                        OInstance inst = (OInstance)instIter.next();
                        if(p.isValidRange(inst)) {
                          instList.add(inst);
                        }
                      }
                      ValuesSelectionAction vsa = new ValuesSelectionAction();
                      String[] instArray = new String[instList.size()];
                      for(int i = 0; i < instArray.length; i++) {
                        instArray[i] = ((OInstance)instList.get(i)).getName();
                      }
                      vsa.showGUI("Select Values for the " + p.getName(),
                              instArray, new String[0]);
                      String[] selectedValues = vsa.getSelectedValues();
                      for(int i = 0; i < selectedValues.length; i++) {
                        OInstance byName = ontology
                                .getInstanceByName(selectedValues[i]);
                        if(byName == null) continue;
                        ((OInstance)((DefaultMutableTreeNode)selectedNodes
                                .get(0)).getUserObject()).addPropertyValue(p
                                .getName(), byName);
                      }
                    }
                    else {
                      String value = JOptionPane.showInputDialog(null,
                              "Enter Value for property :" + p.getName());
                      ((OInstance)((DefaultMutableTreeNode)selectedNodes.get(0))
                              .getUserObject()).addPropertyValue(p.getName(),
                              value);
                    }
                    TreePath path = tree.getSelectionPath();
                    tree.setSelectionRow(0);
                    tree.setSelectionPath(path);
                  }
                });
              }
            }

            menu.show(tree, me.getX(), me.getY());
            menu.setVisible(true);
          }
        }
      }
    });

    propertyTree.getSelectionModel().addTreeSelectionListener(
            new TreeSelectionListener() {
              public void valueChanged(TreeSelectionEvent e) {
                int[] selectedRows = propertyTree.getSelectionRows();
                if(selectedRows != null && selectedRows.length > 0) {
                  selectedNodes = new ArrayList();
                  for(int i = 0; i < selectedRows.length; i++) {
                    DefaultMutableTreeNode node1 = (DefaultMutableTreeNode)propertyTree
                            .getPathForRow(selectedRows[i])
                            .getLastPathComponent();
                    selectedNodes.add(node1);
                  }
                  propertyDetailsTableModel
                          .setItem(((DefaultMutableTreeNode)selectedNodes
                                  .get(0)).getUserObject());
                  enableDisableToolBarComponents();
                  fireTreeNodeSelectionChanged(selectedNodes);
                  tree.clearSelection();
                }
                mainSplit.setRightComponent(propertyDetailsTableScroller);
                mainSplit.updateUI();
              }
            });

    propertyTree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        if(SwingUtilities.isRightMouseButton(me)) {
          if(selectedNodes.size() > 1) return;

          final JPopupMenu menu = new JPopupMenu();
          final JCheckBoxMenuItem functional = new JCheckBoxMenuItem(
                  "Functional");
          final JCheckBoxMenuItem inverseFunctional = new JCheckBoxMenuItem(
                  "InverseFunctional");
          final JMenuItem sameAs = new JMenuItem("Same As...");

          if(((DefaultMutableTreeNode)selectedNodes.get(0)).getUserObject() instanceof ObjectProperty) {
            functional
                    .setSelected(((ObjectProperty)((DefaultMutableTreeNode)selectedNodes
                            .get(0)).getUserObject()).isFunctional());
            inverseFunctional
                    .setSelected(((ObjectProperty)((DefaultMutableTreeNode)selectedNodes
                            .get(0)).getUserObject()).isInverseFunctional());
            menu.add(functional);
            menu.add(inverseFunctional);
          }

          menu.add(sameAs);

          functional.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              ((ObjectProperty)((DefaultMutableTreeNode)selectedNodes.get(0))
                      .getUserObject()).setFunctional(functional.isSelected());
            }
          });

          inverseFunctional.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              ((ObjectProperty)((DefaultMutableTreeNode)selectedNodes.get(0))
                      .getUserObject()).setInverseFunctional(inverseFunctional
                      .isSelected());
            }
          });

          sameAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              ValuesSelectionAction vsa = new ValuesSelectionAction();
              Set propDefinitions = ontology.getPropertyDefinitions();
              Property prop = (Property)((DefaultMutableTreeNode)selectedNodes
                      .get(0)).getUserObject();

              Class clas = ((DefaultMutableTreeNode)selectedNodes.get(0))
                      .getUserObject().getClass();

              String[] tempArray = new String[propDefinitions.size() - 1];
              Iterator iter = propDefinitions.iterator();
              int counter = 0;
              while(iter.hasNext()) {
                Property p = (Property)iter.next();
                if(p == prop) continue;
                tempArray[counter] = p.getName();
                counter++;
              }

              Set props = prop.getSamePropertyAs();
              String[] selection = new String[props == null ? 0 : props.size()];

              iter = props.iterator();
              counter = 0;
              while(iter.hasNext()) {
                Property p = (Property)iter.next();
                prop.getSamePropertyAs().remove(p);
                p.getSamePropertyAs().remove(prop);
                selection[counter] = p.getName();
                counter++;
              }

              vsa.showGUI("Property " + prop.getName() + " is sameAs:",
                      tempArray, selection);
              String[] sameAsValues = vsa.getSelectedValues();
              ArrayList properties = new ArrayList();
              String cantbeAdded = new String(
                      "Following properties can't be set as SameAs "
                              + prop.getName());
              boolean showMessage = false;
              for(counter = 0; counter < sameAsValues.length; counter++) {
                Property p = ontology
                        .getPropertyDefinitionByName(sameAsValues[counter]);
                if(!p.getClass().isAssignableFrom(clas)) {
                  showMessage = true;
                  cantbeAdded += "\n" + p.getName();
                  continue;
                }
                properties.add(p);
              }

              if(showMessage) JOptionPane.showMessageDialog(null, cantbeAdded);

              for(counter = 0; counter < properties.size(); counter++) {
                Property p1 = (Property)properties.get(counter);
                if(p1 == null) continue;
                p1.setSamePropertyAs(prop);
                prop.setSamePropertyAs(p1);

                for(int j = 0; j < properties.size(); j++) {
                  Property p2 = (Property)properties.get(j);
                  if(p2 == null || counter == j) continue;
                  p1.setSamePropertyAs(p2);
                }
              }
            }
          });

          menu.show(propertyTree, me.getX(), me.getY());
          menu.setVisible(true);
        }
      }
    });

    mainSplit.addComponentListener(new ComponentListener() {
      public void componentHidden(ComponentEvent e) {
      }

      public void componentMoved(ComponentEvent e) {
      }

      public void componentResized(ComponentEvent e) {
        mainSplit.setDividerLocation(0.7);
      }

      public void componentShown(ComponentEvent e) {

      }

    });

    subSplit.addComponentListener(new ComponentListener() {
      public void componentHidden(ComponentEvent e) {
      }

      public void componentMoved(ComponentEvent e) {
      }

      public void componentResized(ComponentEvent e) {
        subSplit.setDividerLocation(0.7);
      }

      public void componentShown(ComponentEvent e) {

      }

    });
  }

  /**
   * Enable-disable toolBar components
   */
  private void enableDisableToolBarComponents() {
    boolean allClasses = true;
    boolean allProperties = true;
    boolean allInstances = true;

    for(int i = 0; i < selectedNodes.size(); i++) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)selectedNodes
              .get(i);
      if(node.getUserObject() instanceof TClass) {
        allProperties = false;
        allInstances = false;
      }
      else if(node.getUserObject() instanceof OInstance) {
        allClasses = false;
        allProperties = false;
      }
      else {
        allInstances = false;
        allClasses = false;
      }
    }

    if(allClasses) {
      topClass.setEnabled(true);
      subClass.setEnabled(true);
      instance.setEnabled(true);
      property.setEnabled(true);
      subProperty.setEnabled(false);
      delete.setEnabled(true);
    }
    else if(allInstances) {
      topClass.setEnabled(true);
      subClass.setEnabled(false);
      instance.setEnabled(false);
      property.setEnabled(false);
      subProperty.setEnabled(false);
      delete.setEnabled(true);
    }
    else if(allProperties) {
      topClass.setEnabled(false);
      subClass.setEnabled(false);
      instance.setEnabled(false);
      property.setEnabled(true);
      subProperty.setEnabled(true);
      delete.setEnabled(true);
    }
    else {
      topClass.setEnabled(false);
      subClass.setEnabled(false);
      instance.setEnabled(false);
      property.setEnabled(false);
      subProperty.setEnabled(false);
      delete.setEnabled(true);
    }
  }

  /**
   * Called when the target of this editor has changed
   */
  protected void rebuildModel() {
    rootNode.removeAllChildren();
    if(ontologyClasses == null)
      ontologyClasses = new ArrayList();
    else ontologyClasses.clear();
    List rootClasses = new ArrayList(taxonomy.getTopClasses());
    Collections.sort(rootClasses, itemComparator);
    addChidrenRec(rootNode, rootClasses, itemComparator);

    propertyRootNode.removeAllChildren();
    List props = new ArrayList(((Ontology)taxonomy).getPropertyDefinitions());
    List subList = new ArrayList();
    for(int i = 0; i < props.size(); i++) {
      Property prop = (Property)props.get(i);
      Set set = prop.getSuperProperties(Property.DIRECT_CLOSURE);
      if(set != null && !set.isEmpty()) {
        continue;
      }
      else {
        subList.add(prop);
      }
    }

    Collections.sort(subList, itemComparator);
    addPropertyChidrenRec(propertyRootNode, subList, itemComparator, "");
    propertyAction.setOntologyClasses(ontologyClasses);
    subPropertyAction.setOntologyClasses(ontologyClasses);

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        treeModel.nodeStructureChanged(rootNode);
        tree.setSelectionInterval(0, 0);
        // expand the root
        tree.expandPath(new TreePath(rootNode));
        // expand the entire tree
        for(int i = 0; i < tree.getRowCount(); i++)
          tree.expandRow(i);

        propertyTreeModel.nodeStructureChanged(propertyRootNode);
        // propertyTree.setSelectionInterval(0,0);
        // expand the root
        propertyTree.expandPath(new TreePath(propertyRootNode));
        // expand the entire tree
        for(int i = 0; i < propertyTree.getRowCount(); i++)
          propertyTree.expandRow(i);

        detailsTableModel.fireTableDataChanged();
        propertyDetailsTableModel.fireTableDataChanged();
      }
    });
  }

  /**
   * Adds the children nodes to a node using values from a list of
   * classes and instances.
   * 
   * @param parent the parent node.
   * @param children the lsit of children objects.
   * @param comparator the Comparator used to sort the children.
   */
  protected void addChidrenRec(DefaultMutableTreeNode parent, List children,
          Comparator comparator) {
    Iterator childIter = children.iterator();
    while(childIter.hasNext()) {
      Object aChild = childIter.next();
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(aChild);
      parent.add(childNode);
      if(aChild instanceof TClass) {
        ontologyClasses.add(aChild);
        childNode.setAllowsChildren(true);
        // add all the subclasses
        TClass aClass = (TClass)aChild;
        List childList = new ArrayList(aClass
                .getSubClasses(TClass.DIRECT_CLOSURE));
        Collections.sort(childList, comparator);
        addChidrenRec(childNode, childList, comparator);

        // add all the instances
        if(ontologyMode) {
          childList = new ArrayList(ontology.getDirectInstances((OClass)aClass));
          Collections.sort(childList, comparator);
          addChidrenRec(childNode, childList, comparator);
        }
      }
      else if(aChild instanceof OInstance) {
        childNode.setAllowsChildren(false);
      }
      else {
        throw new GateRuntimeException("Unknown ontology item: "
                + aChild.getClass().getName() + "!");
      }

    }
  }

  /**
   * Adds the children nodes to a node using values from a list of
   * classes and instances.
   * 
   * @param parent the parent node.
   * @param children the lsit of children objects.
   * @param comparator the Comparator used to sort the children.
   */
  protected void addPropertyChidrenRec(DefaultMutableTreeNode parent,
          List children, Comparator comparator, String space) {
    Iterator childIter = children.iterator();
    while(childIter.hasNext()) {
      Object aChild = childIter.next();
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(aChild);
      parent.add(childNode);
      if(aChild instanceof Property) {
        childNode.setAllowsChildren(true);
        // add all the subclasses
        Property aProperty = (Property)aChild;
        List childList = new ArrayList(aProperty
                .getSubProperties(Property.DIRECT_CLOSURE));
        Collections.sort(childList, comparator);
        addPropertyChidrenRec(childNode, childList, comparator, space + "  ");
      }
      else {
        throw new GateRuntimeException("Unknown ontology item: "
                + aChild.getClass().getName() + "!");
      }
    }
  }

  public void processGateEvent(GateEvent e) {
    // ignore
  }

  public void ontologyModified(OntologyModificationEvent e) {
    if(e.getSource() != ontology) {
      return;
    }
    rebuildModel();
  }

  public void addTreeNodeSelectionListener(TreeNodeSelectionListener listener) {
    this.listeners.add(listener);
  }

  public void removeTreeNodeSelectionListener(TreeNodeSelectionListener listener) {
    this.listeners.remove(listener);
  }

  private void fireTreeNodeSelectionChanged(ArrayList nodes) {
    for(int i = 0; i < listeners.size(); i++) {
      ((TreeNodeSelectionListener)listeners.get(i)).selectionChanged(nodes);
    }
  }

  /**
   * The taxonomy that this editor displays
   */
  protected Taxonomy taxonomy;

  /**
   * If the taxonomy being edited is an ontology (i.e. has instances as
   * well) then this member stores it as well.
   */
  protected Ontology ontology;

  /**
   * Flag that indicates whether the object beiong edited is an
   * ontology.
   */
  protected boolean ontologyMode;

  protected OntologyItemComparator itemComparator;

  /**
   * The tree view.
   */
  protected JTree tree;

  /**
   * The property treeView
   */
  protected JTree propertyTree;

  /**
   * The mode, for the tree.
   */
  protected DefaultTreeModel treeModel;

  /**
   * The property model, for the tree
   */
  protected DefaultTreeModel propertyTreeModel;

  /**
   * The list view used to display item details
   */
  protected JTable detailsTable;

  protected JTable propertyDetailsTable;

  DetailsTableModel detailsTableModel;

  PropertyDetailsTableModel propertyDetailsTableModel;

  /**
   * The main split
   */
  protected JSplitPane mainSplit;

  /**
   * The sub split
   */
  protected JSplitPane subSplit;

  /**
   * The root node of the tree.
   */
  protected DefaultMutableTreeNode rootNode;

  /**
   * The property root node of the tree
   */
  protected DefaultMutableTreeNode propertyRootNode;

  protected JScrollPane detailsTableScroller, propertyDetailsTableScroller;

  /**
   * ToolBar
   */
  protected JToolBar toolBar;

  protected JButton topClass;

  protected JButton subClass;

  protected JButton instance;

  protected JButton property;
  
  protected JButton subProperty;

  protected JButton delete;

  protected ArrayList selectedNodes;

  protected ArrayList ontologyClasses;

  protected TopClassAction topClassAction;

  protected SubClassAction subClassAction;

  protected InstanceAction instanceAction;

  protected PropertyAction propertyAction;
  
  protected SubPropertyAction subPropertyAction;

  protected DeleteOntologyResourceAction deleteOntoResourceAction;

  protected ArrayList listeners;
}
