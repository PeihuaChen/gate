/*
 *  OntologyEditor.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OntologyEditor.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.ontology.*;
import gate.event.*;
import gate.gui.*;
import gate.swing.XJTable;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

/**
 * The GUI for the Ontology Editor
 * @author niraj
 *
 */
public class OntologyEditor extends AbstractVisualResource
                                                          implements
                                                          ResizableVisualResource,
                                                          OntologyModificationListener {
  private static final long serialVersionUID = 3257847701214345265L;

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.AbstractVisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target) {
    this.ontology = (Ontology)target;
    detailsTableModel.setOntology(ontology);
    topClassAction.setOntology(ontology);
    subClassAction.setOntology(ontology);
    instanceAction.setOntology(ontology);
    rdfPropertyAction.setOntology(ontology);
    annotationPropertyAction.setOntology(ontology);
    datatypePropertyAction.setOntology(ontology);
    objectPropertyAction.setOntology(ontology);
    symmetricPropertyAction.setOntology(ontology);
    transitivePropertyAction.setOntology(ontology);
    deleteOntoResourceAction.setOntology(ontology);
    ontology.removeOntologyModificationListener(this);
    rebuildModel();
    ontology.addOntologyModificationListener(this);
  }

  /**
   * Init method, that creates this object and returns this object as a resource
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
    listeners = new ArrayList<TreeNodeSelectionListener>();
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
    scroller = new JScrollPane(tree);
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
    propertyScroller = new JScrollPane(propertyTree);
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
    rdfPropertyAction = new RDFPropertyAction("", MainFrame
            .getIcon("ontology-rdf-property"));
    addTreeNodeSelectionListener(rdfPropertyAction);
    rdfProperty = new JButton(rdfPropertyAction);
    rdfProperty.setToolTipText("Add New RDF Property");
    annotationPropertyAction = new AnnotationPropertyAction("", MainFrame
            .getIcon("ontology-annotation-property"));
    addTreeNodeSelectionListener(annotationPropertyAction);
    annotationProperty = new JButton(annotationPropertyAction);
    annotationProperty.setToolTipText("Add New Annotation Property");
    datatypePropertyAction = new DatatypePropertyAction("", MainFrame
            .getIcon("ontology-datatype-property"));
    addTreeNodeSelectionListener(datatypePropertyAction);
    datatypeProperty = new JButton(datatypePropertyAction);
    datatypeProperty.setToolTipText("Add New Datatype Property");
    objectPropertyAction = new ObjectPropertyAction("", MainFrame
            .getIcon("ontology-object-property"));
    addTreeNodeSelectionListener(objectPropertyAction);
    objectProperty = new JButton(objectPropertyAction);
    objectProperty.setToolTipText("Add New Object Property");
    symmetricPropertyAction = new SymmetricPropertyAction("", MainFrame
            .getIcon("ontology-symmetric-property"));
    addTreeNodeSelectionListener(symmetricPropertyAction);
    symmetricProperty = new JButton(symmetricPropertyAction);
    symmetricProperty.setToolTipText("Add New Symmetric Property");
    transitivePropertyAction = new TransitivePropertyAction("", MainFrame
            .getIcon("ontology-transitive-property"));
    addTreeNodeSelectionListener(transitivePropertyAction);
    transitiveProperty = new JButton(transitivePropertyAction);
    transitiveProperty.setToolTipText("Add New Transitive Property");
    deleteOntoResourceAction = new DeleteOntologyResourceAction("", MainFrame
            .getIcon("delete"));
    addTreeNodeSelectionListener(deleteOntoResourceAction);
    delete = new JButton(deleteOntoResourceAction);
    delete.setToolTipText("Delete the selected nodes");
    toolBar.setFloatable(false);
    toolBar.add(topClass);
    toolBar.add(subClass);
    toolBar.add(instance);
    toolBar.add(rdfProperty);
    toolBar.add(annotationProperty);
    toolBar.add(datatypeProperty);
    toolBar.add(objectProperty);
    toolBar.add(symmetricProperty);
    toolBar.add(transitiveProperty);
    toolBar.add(delete);
    this.add(toolBar, BorderLayout.NORTH);
  }

  /**
   * Initializes various listeners
   */
  protected void initListeners() {
    tree.getSelectionModel().addTreeSelectionListener(
            new TreeSelectionListener() {
              public void valueChanged(TreeSelectionEvent e) {
                int[] selectedRows = tree.getSelectionRows();
                if(selectedRows != null && selectedRows.length > 0) {
                  selectedNodes = new ArrayList<DefaultMutableTreeNode>();
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
    propertyTree.getSelectionModel().addTreeSelectionListener(
            new TreeSelectionListener() {
              public void valueChanged(TreeSelectionEvent e) {
                int[] selectedRows = propertyTree.getSelectionRows();
                if(selectedRows != null && selectedRows.length > 0) {
                  selectedNodes = new ArrayList<DefaultMutableTreeNode>();
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
    tree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        if(SwingUtilities.isRightMouseButton(me)) {
          if(selectedNodes.size() > 1) return;
          final JPopupMenu menu = new JPopupMenu();
          final JMenu addProperty = new JMenu("Properties");
          final OResource candidate = (OResource)((DefaultMutableTreeNode)selectedNodes
                  .get(0)).getUserObject();
          menu.add(addProperty);
          final JMenuItem sameAs = new JMenuItem(candidate instanceof OClass
                  ? "Equivalent Class"
                  : "Same As Instance");
          menu.add(sameAs);
          if(candidate instanceof OClass) {
            sameAs.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ae) {
                Set<OClass> oclasses = ontology.getOClasses(false);
                ArrayList<OClass> classList = new ArrayList<OClass>();
                Iterator<OClass> classIter = oclasses.iterator();
                while(classIter.hasNext()) {
                  OClass aClass = classIter.next();
                  classList.add(aClass);
                }
                classList.remove(candidate);
                ValuesSelectionAction vsa = new ValuesSelectionAction();
                String[] classArray = new String[classList.size()];
                for(int i = 0; i < classArray.length; i++) {
                  classArray[i] = classList.get(i).getURI().toString();
                }
                vsa.showGUI(candidate.getName() + " is equivalent to :",
                        classArray, new String[0]);
                String[] selectedValues = vsa.getSelectedValues();
                for(int i = 0; i < selectedValues.length; i++) {
                  OClass byName = (OClass)ontology
                          .getOResourceFromMap(selectedValues[i]);
                  if(byName == null) continue;
                  ((OClass)candidate).setEquivalentClassAs(byName);
                }
                TreePath path = tree.getSelectionPath();
                tree.setSelectionRow(0);
                tree.setSelectionPath(path);
                return;
              }
            });
          }
          if(candidate instanceof OInstance) {
            sameAs.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ae) {
                Set<OInstance> instances = ontology.getOInstances();
                ArrayList<OInstance> instancesList = new ArrayList<OInstance>();
                Iterator<OInstance> instancesIter = instances.iterator();
                while(instancesIter.hasNext()) {
                  OInstance instance = instancesIter.next();
                  instancesList.add(instance);
                }
                instancesList.remove(candidate);
                ValuesSelectionAction vsa = new ValuesSelectionAction();
                String[] instancesArray = new String[instancesList.size()];
                for(int i = 0; i < instancesArray.length; i++) {
                  instancesArray[i] = instancesList.get(i).getURI().toString();
                }
                vsa.showGUI(candidate.getName() + " is same As :",
                        instancesArray, new String[0]);
                String[] selectedValues = vsa.getSelectedValues();
                for(int i = 0; i < selectedValues.length; i++) {
                  OInstance byName = (OInstance)ontology
                          .getOResourceFromMap(selectedValues[i]);
                  if(byName == null) continue;
                  ((OInstance)candidate).setSameInstanceAs(byName);
                }
                TreePath path = tree.getSelectionPath();
                tree.setSelectionRow(0);
                tree.setSelectionPath(path);
                return;
              }
            });
          }
          Set<RDFProperty> props = ontology.getPropertyDefinitions();
          Iterator<RDFProperty> iter = props.iterator();
          while(iter.hasNext()) {
            final RDFProperty p = iter.next();
            if(p instanceof AnnotationProperty) {
              JMenuItem item = new JMenuItem(p.getName());
              addProperty.add(item);
              item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                  String value = JOptionPane.showInputDialog(null,
                          "Enter Value for property :" + p.getName());
                  candidate.addAnnotationPropertyValue((AnnotationProperty)p,
                          new Literal(value));
                  TreePath path = tree.getSelectionPath();
                  tree.setSelectionRow(0);
                  tree.setSelectionPath(path);
                  return;
                }
              });
              continue;
            }
            if(candidate instanceof OInstance && p instanceof DatatypeProperty
                    && p.isValidDomain(candidate)) {
              JMenuItem item = new JMenuItem(p.getName());
              addProperty.add(item);
              item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                  String value = JOptionPane.showInputDialog(null,
                          "Datatype : "
                                  + ((DatatypeProperty)p).getDataType()
                                          .getXmlSchemaURI().toString(),
                          "Enter Value for property :" + p.getName());
                  boolean validValue = ((DatatypeProperty)p).getDataType()
                          .isValidValue(value);
                  if(!validValue) {
                    JOptionPane.showMessageDialog(null, "Incompatible value : "
                            + value);
                    return;
                  }
                  try {
                    ((OInstance)candidate).addDatatypePropertyValue(
                            (DatatypeProperty)p, new Literal(value,
                                    ((DatatypeProperty)p).getDataType()));
                  } catch(InvalidValueException ive) {
                    JOptionPane.showMessageDialog(null, "Incompatible value");
                    return;
                  }
                  TreePath path = tree.getSelectionPath();
                  tree.setSelectionRow(0);
                  tree.setSelectionPath(path);
                  return;
                }
              });
              continue;
            }
            if(candidate instanceof OInstance && p instanceof ObjectProperty
                    && p.isValidDomain(candidate)) {
              JMenuItem item = new JMenuItem(p.getName());
              addProperty.add(item);
              item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                  Set<OInstance> instances = ontology.getOInstances();
                  ArrayList<OInstance> instList = new ArrayList<OInstance>();
                  Iterator<OInstance> instIter = instances.iterator();
                  while(instIter.hasNext()) {
                    OInstance inst = instIter.next();
                    if(p.isValidRange(inst)) {
                      instList.add(inst);
                    }
                  }
                  ValuesSelectionAction vsa = new ValuesSelectionAction();
                  String[] instArray = new String[instList.size()];
                  for(int i = 0; i < instArray.length; i++) {
                    instArray[i] = instList.get(i).getURI().toString();
                  }
                  vsa.showGUI("Select Values for the " + p.getName(),
                          instArray, new String[0]);
                  String[] selectedValues = vsa.getSelectedValues();
                  for(int i = 0; i < selectedValues.length; i++) {
                    OInstance byName = (OInstance)ontology
                            .getOResourceFromMap(selectedValues[i]);
                    if(byName == null) continue;
                    try {
                      ((OInstance)candidate).addObjectPropertyValue(
                              (ObjectProperty)p, byName);
                    } catch(InvalidValueException ive) {
                      JOptionPane.showMessageDialog(null, "Incompatible value");
                      return;
                    }
                  }
                  TreePath path = tree.getSelectionPath();
                  tree.setSelectionRow(0);
                  tree.setSelectionPath(path);
                  return;
                }
              });
              continue;
            }
          }
          menu.show(tree, me.getX(), me.getY());
          menu.setVisible(true);
        }
      }
    });
    propertyTree.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        if(SwingUtilities.isRightMouseButton(me)) {
          if(selectedNodes.size() > 1) return;
          final JPopupMenu menu = new JPopupMenu();
          final OResource candidate = (OResource)((DefaultMutableTreeNode)selectedNodes
                  .get(0)).getUserObject();
          final JMenuItem sameAs = new JMenuItem("Equivalent Property");
          final JCheckBoxMenuItem functional = new JCheckBoxMenuItem(
                  "Functional");
          final JCheckBoxMenuItem inverseFunctional = new JCheckBoxMenuItem(
                  "InverseFunctional");
          menu.add(sameAs);
          if(candidate instanceof AnnotationProperty) { return; }
          final Set<RDFProperty> props = new HashSet<RDFProperty>();
          if(candidate instanceof ObjectProperty) {
            props.addAll(ontology.getObjectProperties());
            props.addAll(ontology.getSymmetricProperties());
            props.addAll(ontology.getTransitiveProperties());
            functional.setSelected(((ObjectProperty)candidate).isFunctional());
            inverseFunctional.setSelected(((ObjectProperty)candidate)
                    .isInverseFunctional());
            functional.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ae) {
                ((ObjectProperty)candidate).setFunctional(functional
                        .isSelected());
              }
            });
            inverseFunctional.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent ae) {
                ((ObjectProperty)candidate)
                        .setInverseFunctional(inverseFunctional.isSelected());
              }
            });
            menu.add(functional);
            menu.add(inverseFunctional);
          } else if(candidate instanceof DatatypeProperty) {
            props.addAll(ontology.getDatatypeProperties());
          } else {
            props.addAll(ontology.getRDFProperties());
          }
          sameAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
              props.remove(candidate);
              Iterator<RDFProperty> iter = props.iterator();
              ValuesSelectionAction vsa = new ValuesSelectionAction();
              String[] propArray = new String[props.size()];
              for(int i = 0; i < propArray.length; i++) {
                propArray[i] = iter.next().getURI().toString();
              }
              vsa.showGUI(candidate.getName() + " is equivalent to :",
                      propArray, new String[0]);
              String[] selectedValues = vsa.getSelectedValues();
              for(int i = 0; i < selectedValues.length; i++) {
                RDFProperty byName = (RDFProperty)ontology
                        .getOResourceFromMap(selectedValues[i]);
                if(byName == null) continue;
                ((RDFProperty)candidate).setEquivalentPropertyAs(byName);
              }
              TreePath path = propertyTree.getSelectionPath();
              propertyTree.setSelectionRow(0);
              propertyTree.setSelectionPath(path);
              return;
            }
          });
          menu.show(propertyTree, me.getX(), me.getY());
          menu.setVisible(true);
        }
      }
    });
  }

  protected void expandNode(JTree tree) {
    for(int i = 0; i < tree.getRowCount(); i++) {
      tree.expandRow(i);
    }
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
      if(node.getUserObject() instanceof OClass) {
        allProperties = false;
        allInstances = false;
      } else if(node.getUserObject() instanceof OInstance) {
        allClasses = false;
        allProperties = false;
      } else {
        allInstances = false;
        allClasses = false;
      }
    }
    if(allClasses) {
      topClass.setEnabled(true);
      subClass.setEnabled(true);
      instance.setEnabled(true);
      rdfProperty.setEnabled(true);
      annotationProperty.setEnabled(true);
      datatypeProperty.setEnabled(true);
      objectProperty.setEnabled(true);
      symmetricProperty.setEnabled(true);
      transitiveProperty.setEnabled(true);
      delete.setEnabled(true);
    } else if(allInstances) {
      topClass.setEnabled(true);
      subClass.setEnabled(false);
      instance.setEnabled(false);
      rdfProperty.setEnabled(true);
      annotationProperty.setEnabled(true);
      datatypeProperty.setEnabled(false);
      objectProperty.setEnabled(false);
      symmetricProperty.setEnabled(false);
      transitiveProperty.setEnabled(false);
      delete.setEnabled(true);
    } else if(allProperties) {
      topClass.setEnabled(false);
      subClass.setEnabled(false);
      instance.setEnabled(false);
      rdfProperty.setEnabled(true);
      annotationProperty.setEnabled(true);
      datatypeProperty.setEnabled(true);
      objectProperty.setEnabled(true);
      symmetricProperty.setEnabled(true);
      transitiveProperty.setEnabled(true);
      delete.setEnabled(true);
    } else {
      topClass.setEnabled(false);
      subClass.setEnabled(false);
      instance.setEnabled(false);
      rdfProperty.setEnabled(true);
      annotationProperty.setEnabled(true);
      datatypeProperty.setEnabled(false);
      objectProperty.setEnabled(false);
      symmetricProperty.setEnabled(false);
      transitiveProperty.setEnabled(false);
      delete.setEnabled(true);
    }
  }

  /**
   * Called when the target of this editor has changed
   */
  protected void rebuildModel() {
    rootNode.removeAllChildren();
    propertyRootNode.removeAllChildren();
    if(ontologyClassesURIs == null)
      ontologyClassesURIs = new ArrayList<String>();
    else ontologyClassesURIs.clear();
    uri2TreeNodesListMap = new HashMap<String, ArrayList<DefaultMutableTreeNode>>();
    reverseMap = new HashMap<DefaultMutableTreeNode, URI>();
    List<OResource> rootClasses = new ArrayList<OResource>(ontology
            .getOClasses(true));
    Collections.sort(rootClasses, itemComparator);
    addChidrenRec(rootNode, rootClasses, itemComparator);
    List<RDFProperty> props = new ArrayList<RDFProperty>(ontology
            .getPropertyDefinitions());
    List<RDFProperty> subList = new ArrayList<RDFProperty>();
    for(int i = 0; i < props.size(); i++) {
      RDFProperty prop = props.get(i);
      if(prop instanceof AnnotationProperty) {
        subList.add(prop);
        continue;
      }
      Set<RDFProperty> set = prop.getSuperProperties(OConstants.DIRECT_CLOSURE);
      if(set != null && !set.isEmpty()) {
        continue;
      } else {
        subList.add(prop);
      }
    }
    Collections.sort(subList, itemComparator);
    addPropertyChidrenRec(propertyRootNode, subList, itemComparator);
    rdfPropertyAction.setResoucesList(ontology.getAllResources());
    datatypePropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
    objectPropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
    symmetricPropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
    transitivePropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
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
        // expand the root
        propertyTree.expandPath(new TreePath(propertyRootNode));
        // expand the entire tree
        for(int i = 0; i < propertyTree.getRowCount(); i++)
          propertyTree.expandRow(i);
        // detailsTableModel.fireTableDataChanged();
        // propertyDetailsTableModel.fireTableDataChanged();
      }
    });
  }

  /**
   * Adds the children nodes to a node using values from a list of classes and
   * instances.
   * 
   * @param parent
   *          the parent node.
   * @param children
   *          the lsit of children objects.
   * @param comparator
   *          the Comparator used to sort the children.
   */
  protected void addChidrenRec(DefaultMutableTreeNode parent,
          List<OResource> children, Comparator comparator) {
    Iterator<OResource> childIter = children.iterator();
    while(childIter.hasNext()) {
      OResource aChild = childIter.next();
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(aChild);
      parent.add(childNode);
      // we maintain a map of ontology resources and their representing tree
      // nodes
      ArrayList<DefaultMutableTreeNode> list = uri2TreeNodesListMap.get(aChild
              .getURI().toString());
      if(list == null) {
        list = new ArrayList<DefaultMutableTreeNode>();
        uri2TreeNodesListMap.put(aChild.getURI().toString(), list);
      }
      list.add(childNode);
      reverseMap.put(childNode, aChild.getURI());
      if(aChild instanceof OClass) {
        if(!ontologyClassesURIs.contains(aChild.getURI().toString()))
          ontologyClassesURIs.add(aChild.getURI().toString());
        childNode.setAllowsChildren(true);
        // add all the subclasses
        OClass aClass = (OClass)aChild;
        List<OResource> childList = new ArrayList<OResource>(aClass
                .getSubClasses(OClass.DIRECT_CLOSURE));
        Collections.sort(childList, comparator);
        addChidrenRec(childNode, childList, comparator);
        childList = new ArrayList<OResource>(ontology.getOInstances(aClass,
                OConstants.DIRECT_CLOSURE));
        Collections.sort(childList, comparator);
        addChidrenRec(childNode, childList, comparator);
      } else if(aChild instanceof OInstance) {
        childNode.setAllowsChildren(false);
      }
    }
  }

  /**
   * Adds the children nodes to a node using values from a list of classes and
   * instances.
   * 
   * @param parent
   *          the parent node.
   * @param children
   *          the lsit of children objects.
   * @param comparator
   *          the Comparator used to sort the children.
   */
  protected void addPropertyChidrenRec(DefaultMutableTreeNode parent,
          List<RDFProperty> children, Comparator comparator) {
    Iterator<RDFProperty> childIter = children.iterator();
    while(childIter.hasNext()) {
      RDFProperty aChild = childIter.next();
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(aChild);
      parent.add(childNode);
      // we maintain a map of ontology resources and their representing tree
      // nodes
      ArrayList<DefaultMutableTreeNode> list = uri2TreeNodesListMap.get(aChild
              .getURI().toString());
      if(list == null) {
        list = new ArrayList<DefaultMutableTreeNode>();
        uri2TreeNodesListMap.put(aChild.getURI().toString(), list);
      }
      list.add(childNode);
      reverseMap.put(childNode, aChild.getURI());
      if(aChild instanceof AnnotationProperty) {
        childNode.setAllowsChildren(false);
      } else {
        childNode.setAllowsChildren(true);
        // add all the sub properties
        List<RDFProperty> childList = new ArrayList<RDFProperty>(aChild
                .getSubProperties(OConstants.DIRECT_CLOSURE));
        Collections.sort(childList, comparator);
        addPropertyChidrenRec(childNode, childList, comparator);
      }
    }
  }

  public void processGateEvent(GateEvent e) {
    // ignore
  }

  /**
   * Should be invoked when a class is added
   * 
   * @param e
   */
  protected void classIsAdded(OClass aClass) {
    // we first obtain its superClasses
    Set<OClass> superClasses = aClass.getSuperClasses(OClass.DIRECT_CLOSURE);
    List<OResource> list = new ArrayList<OResource>();
    list.add(aClass);
    if(superClasses == null || superClasses.isEmpty()) {
      // this is a root node
      addChidrenRec(rootNode, list, itemComparator);
      treeModel.nodeStructureChanged(rootNode);
    } else {
      Iterator<OClass> iter = superClasses.iterator();
      while(iter.hasNext()) {
        ArrayList<DefaultMutableTreeNode> superNodeList = uri2TreeNodesListMap
                .get(iter.next().getURI().toString());
        for(int i = 0; i < superNodeList.size(); i++) {
          DefaultMutableTreeNode node = superNodeList.get(i);
          addChidrenRec(node, list, itemComparator);
          treeModel.nodeStructureChanged(node);
        }
      }
    }
  }

  /**
   * should be invoked when a property is added
   * 
   * @param e
   */
  protected void propertyIsAdded(RDFProperty p) {
    // we first obtain its superProperty
    if(p instanceof AnnotationProperty) {
      List<RDFProperty> list = new ArrayList<RDFProperty>();
      list.add(p);
      addPropertyChidrenRec(propertyRootNode, list, itemComparator);
      propertyTreeModel.nodeStructureChanged(propertyRootNode);
      return;
    }
    Set<RDFProperty> superProperties = p
            .getSuperProperties(OConstants.DIRECT_CLOSURE);
    List<RDFProperty> list = new ArrayList<RDFProperty>();
    list.add(p);
    if(superProperties == null || superProperties.isEmpty()) {
      // this is a root node
      addPropertyChidrenRec(propertyRootNode, list, itemComparator);
      propertyTreeModel.nodeStructureChanged(propertyRootNode);
    } else {
      Iterator<RDFProperty> iter = superProperties.iterator();
      while(iter.hasNext()) {
        ArrayList<DefaultMutableTreeNode> superNodeList = uri2TreeNodesListMap
                .get(iter.next().getURI().toString());
        for(int i = 0; i < superNodeList.size(); i++) {
          DefaultMutableTreeNode node = superNodeList.get(i);
          addPropertyChidrenRec(node, list, itemComparator);
          propertyTreeModel.nodeStructureChanged(node);
        }
      }
    }
  }

  /**
   * Should be invoked when an instance is added
   * 
   * @param e
   */
  protected void instanceIsAdded(OInstance anInstance) {
    ArrayList<DefaultMutableTreeNode> newList = uri2TreeNodesListMap
            .get(anInstance.getURI().toString());
    if(newList != null) {
      for(int i = 0; i < newList.size(); i++) {
        DefaultMutableTreeNode node = newList.get(i);
        removeFromMap(treeModel, node);
      }
    }
    Set<OClass> superClasses = anInstance
            .getOClasses(OConstants.DIRECT_CLOSURE);
    List<OResource> list = new ArrayList<OResource>();
    list.add(anInstance);
    Iterator<OClass> iter = superClasses.iterator();
    while(iter.hasNext()) {
      OClass aClass = iter.next();
      ArrayList<DefaultMutableTreeNode> superNodeList = uri2TreeNodesListMap
              .get(aClass.getURI().toString());
      for(int i = 0; i < superNodeList.size(); i++) {
        DefaultMutableTreeNode node = superNodeList.get(i);
        addChidrenRec(node, list, itemComparator);
        treeModel.nodeStructureChanged(node);
      }
    }
  }

  private void removeFromMap(DefaultTreeModel model, DefaultMutableTreeNode node) {
    if(!node.isLeaf()) {
      Enumeration enumeration = node.children();
      ArrayList children = new ArrayList();
      while(enumeration.hasMoreElements()) {
        children.add(enumeration.nextElement());
      }
      for(int i = 0; i < children.size(); i++) {
        removeFromMap(model, (DefaultMutableTreeNode)children.get(i));
      }
    }
    URI rURI = reverseMap.get(node);
    reverseMap.remove(node);
    ArrayList<DefaultMutableTreeNode> list = uri2TreeNodesListMap.get(rURI
            .toString());
    list.remove(node);
    if(list.isEmpty()) uri2TreeNodesListMap.remove(rURI.toString());
    
    model.removeNodeFromParent(node);
  }

  /**
   * Should be invoked when a super property is added or deleted
   * 
   * @param e
   */
  protected void superPropertyAffected(RDFProperty p) {
    ArrayList<DefaultMutableTreeNode> nodesList = uri2TreeNodesListMap.get(p
            .getURI().toString());
    // we don't know which super property is added or removed
    // so we remove all nodes and add them again
    for(int i = 0; i < nodesList.size(); i++) {
      DefaultMutableTreeNode node = nodesList.get(i);
      removeFromMap(propertyTreeModel, node);
      propertyTreeModel.nodeStructureChanged(node.getParent());
    }
    List<RDFProperty> list = new ArrayList<RDFProperty>();
    list.add(p);
    // and now add them again
    Set<RDFProperty> superProperties = p
            .getSuperProperties(OConstants.DIRECT_CLOSURE);
    if(superProperties != null) {
      Iterator<RDFProperty> iter = superProperties.iterator();
      while(iter.hasNext()) {
        RDFProperty superProperty = iter.next();
        ArrayList<DefaultMutableTreeNode> superNodesList = uri2TreeNodesListMap
                .get(superProperty.getURI().toString());
        if(superNodesList != null) {
          for(int i = 0; i < superNodesList.size(); i++) {
            DefaultMutableTreeNode superNode = superNodesList.get(i);
            addPropertyChidrenRec(superNode, list, itemComparator);
            propertyTreeModel.nodeStructureChanged(superNode);
          }
        }
      }
    }
  }

  /**
   * Should be invoked when a sub property is added
   * 
   * @param e
   */
  protected void subPropertyIsAdded(RDFProperty p) {
    ArrayList<DefaultMutableTreeNode> nodesList = uri2TreeNodesListMap.get(p
            .getURI().toString());
    // p is a property where the subProperty is added
    // the property which is added as a subProperty might not have any
    // super RDFProperty before
    // so we first remove it from the propertyTree
    Set<RDFProperty> props = p.getSubProperties(OConstants.DIRECT_CLOSURE);
    Iterator<RDFProperty> iter = props.iterator();
    while(iter.hasNext()) {
      RDFProperty subP = iter.next();
      ArrayList<DefaultMutableTreeNode> subNodesList = uri2TreeNodesListMap
              .get(subP.getURI().toString());
      if(subNodesList != null) {
        for(int i = 0; i < subNodesList.size(); i++) {
          DefaultMutableTreeNode node = subNodesList.get(i);
          removeFromMap(propertyTreeModel, node);
          propertyTreeModel.nodeStructureChanged(node.getParent());
        }
      }
      if(subNodesList != null && nodesList != null) {
        // and each of this node needs to be added again
        for(int i = 0; i < nodesList.size(); i++) {
          DefaultMutableTreeNode superNode = nodesList.get(i);
          List<RDFProperty> list = new ArrayList<RDFProperty>();
          list.add(subP);
          addPropertyChidrenRec(superNode, list, itemComparator);
          propertyTreeModel.nodeStructureChanged(superNode);
        }
      }
    }
  }

  /**
   * Should be invoked when a sub property is deleted
   */
  protected void subPropertyIsDeleted(RDFProperty p) {
    ArrayList<DefaultMutableTreeNode> nodeList = uri2TreeNodesListMap.get(p
            .getURI().toString());
    if(nodeList == null || nodeList.isEmpty()) {
      // this is already deleted
      return;
    }
    // p is a property where the subProperty is deleted
    // we don't know which property is deleted
    // so we remove the property p from the tree and add it again
    for(int i = 0; i < nodeList.size(); i++) {
      DefaultMutableTreeNode node = nodeList.get(i);
      removeFromMap(propertyTreeModel, node);
      propertyTreeModel.nodeStructureChanged(node.getParent());
    }
    // now we need to add it again
    Set<RDFProperty> superProperties = p
            .getSuperProperties(OConstants.DIRECT_CLOSURE);
    List<RDFProperty> list = new ArrayList<RDFProperty>();
    list.add(p);
    if(superProperties != null) {
      Iterator<RDFProperty> iter = superProperties.iterator();
      while(iter.hasNext()) {
        RDFProperty superP = (RDFProperty)iter.next();
        nodeList = uri2TreeNodesListMap.get(superP.getURI().toString());
        for(int i = 0; i < nodeList.size(); i++) {
          DefaultMutableTreeNode superNode = nodeList.get(i);
          addPropertyChidrenRec(superNode, list, itemComparator);
          propertyTreeModel.nodeStructureChanged(superNode);
        }
      }
    } else {
      addPropertyChidrenRec(propertyRootNode, list, itemComparator);
      propertyTreeModel.nodeStructureChanged(propertyRootNode);
    }
  }

  /**
   * Should be invoked when a sub class is Added
   * 
   * @param e
   */
  protected void subClassIsAdded(OClass c) {
    ArrayList<DefaultMutableTreeNode> nodesList = uri2TreeNodesListMap.get(c
            .getURI().toString());
    // c is a class where the subClass is added
    // the class which is added as a subClass might not have any
    // super Class before
    // so we first remove it from the tree
    Set<OClass> classes = c.getSubClasses(OClass.DIRECT_CLOSURE);
    Iterator<OClass> iter = classes.iterator();
    while(iter.hasNext()) {
      OClass subC = iter.next();
      ArrayList<DefaultMutableTreeNode> subNodesList = uri2TreeNodesListMap
              .get(subC.getURI().toString());
      if(subNodesList != null) {
        for(int i = 0; i < subNodesList.size(); i++) {
          DefaultMutableTreeNode node = subNodesList.get(i);
          removeFromMap(treeModel, node);
          treeModel.nodeStructureChanged(node.getParent());
        }
      }
      if(subNodesList != null && nodesList != null) {
        // and each of this node needs to be added again
        List<OResource> list = new ArrayList<OResource>();
        list.add(subC);
        for(int i = 0; i < nodesList.size(); i++) {
          DefaultMutableTreeNode superNode = nodesList.get(i);
          addChidrenRec(superNode, list, itemComparator);
          treeModel.nodeStructureChanged(superNode);
        }
      }
    }
  }

  /**
   * Should be invoked when a sub class is deleted
   * 
   * @param e
   */
  protected void subClassIsDeleted(OClass c) {
    ArrayList<DefaultMutableTreeNode> nodeList = uri2TreeNodesListMap.get(c
            .getURI().toString());
    if(nodeList == null || nodeList.isEmpty()) {
      // this is already deleted
      return;
    }
    // c is a class where the subClass is deleted
    // we don't know which class is deleted
    // so we remove the class c from the tree and add it again
    for(int i = 0; i < nodeList.size(); i++) {
      DefaultMutableTreeNode node = nodeList.get(i);
      removeFromMap(treeModel, node);
      treeModel.nodeStructureChanged(node.getParent());
    }
    // now we need to add it again
    Set<OClass> superClasses = c.getSuperClasses(OClass.DIRECT_CLOSURE);
    List<OResource> list = new ArrayList<OResource>();
    list.add(c);
    if(superClasses != null && !superClasses.isEmpty()) {
      Iterator<OClass> iter = superClasses.iterator();
      while(iter.hasNext()) {
        OClass superC = iter.next();
        nodeList = uri2TreeNodesListMap.get(superC.getURI().toString());
        for(int i = 0; i < nodeList.size(); i++) {
          DefaultMutableTreeNode superNode = nodeList.get(i);
          addChidrenRec(superNode, list, itemComparator);
          treeModel.nodeStructureChanged(superNode);
        }
      }
    } else {
      addChidrenRec(rootNode, list, itemComparator);
      treeModel.nodeStructureChanged(rootNode);
    }
  }

  /**
   * Should be invoked when a super class is added or deleted
   * 
   * @param e
   */
  protected void superClassAffected(OClass c) {
    ArrayList<DefaultMutableTreeNode> nodesList = uri2TreeNodesListMap.get(c
            .getURI().toString());
    if(nodesList == null || nodesList.isEmpty()) return;
    // we don't know which super clss is added or removed
    // so we remove all nodes and add them again
    for(int i = 0; i < nodesList.size(); i++) {
      DefaultMutableTreeNode node = nodesList.get(i);
      removeFromMap(treeModel, node);
      treeModel.nodeStructureChanged(node.getParent());
    }
    List<OResource> list = new ArrayList<OResource>();
    list.add(c);
    // and now add them again
    Set<OClass> superClasses = c.getSuperClasses(OConstants.DIRECT_CLOSURE);
    if(superClasses != null) {
      Iterator<OClass> iter = superClasses.iterator();
      while(iter.hasNext()) {
        OClass superClass = iter.next();
        ArrayList<DefaultMutableTreeNode> superNodesList = uri2TreeNodesListMap
                .get(superClass.getURI().toString());
        if(superNodesList != null) {
          for(int i = 0; i < superNodesList.size(); i++) {
            DefaultMutableTreeNode superNode = superNodesList.get(i);
            addChidrenRec(superNode, list, itemComparator);
            treeModel.nodeStructureChanged(superNode);
          }
        }
      }
    }
  }

  public void resourcesRemoved(Ontology ontology, final String[] resources) {
    if(this.ontology != ontology) { return; }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // first hide the tree
        scroller.getViewport().setView(new JLabel("PLease wait, updating..."));
        propertyScroller.getViewport().setView(
                new JLabel("Please wait, updating..."));
        // now update the tree
        // we can use a normal thread here
        Runnable treeUpdater = new Runnable() {
          public void run() {
            // this is not in the swing thread
            // update the tree...
            ontologyClassesURIs.removeAll(Arrays.asList(resources));
            for(int i = 0; i < resources.length; i++) {
              ArrayList<DefaultMutableTreeNode> nodeList = uri2TreeNodesListMap
                      .get(resources[i]);
              if(nodeList != null) {
                for(int j = 0; j < nodeList.size(); j++) {
                  DefaultMutableTreeNode node = nodeList.get(j);
                  DefaultTreeModel modelToUse = node.getUserObject() instanceof RDFProperty
                          ? propertyTreeModel
                          : treeModel;
                  removeFromMap(modelToUse, node);
                  modelToUse.nodeStructureChanged(node.getParent());
                }
              }
            }
            // now we need to show back the tree
            // go back to the swing thread
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                // show the tree again
                scroller.getViewport().setView(tree);
                propertyScroller.getViewport().setView(propertyTree);
                // ask the tree to refresh
                tree.invalidate();
                propertyTree.invalidate();
              }
            });
          }
        };
        treeUpdater.run();
      }
    });
  }

  public void resourceAdded(Ontology ontology, OResource resource) {
    if(this.ontology != ontology) { return; }
    boolean isItTree = true;
    TreePath path = tree.getSelectionPath();
    if(path == null) {
      isItTree = false;
      path = propertyTree.getSelectionPath();
    }
    if(resource instanceof OClass) {
      classIsAdded((OClass)resource);
      expandNode(tree);
    } else if(resource instanceof RDFProperty) {
      propertyIsAdded((RDFProperty)resource);
      expandNode(propertyTree);
    } else if(resource instanceof OInstance) {
      instanceIsAdded((OInstance)resource);
      expandNode(tree);
    }
    rdfPropertyAction.setResoucesList(ontology.getAllResources());
    datatypePropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
    objectPropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
    symmetricPropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
    transitivePropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
    if(isItTree) {
      tree.setSelectionPath(path);
    } else {
      propertyTree.setSelectionPath(path);
    }
    return;
  }

  /**
   * This method is invoked from ontology whenever it is modified
   */
  public void ontologyModified(Ontology ontology, OResource resource,
          int eventType) {
    if(this.ontology != ontology) { return; }
    boolean isItTree = true;
    TreePath path = tree.getSelectionPath();
    if(path == null) {
      isItTree = false;
      path = propertyTree.getSelectionPath();
    }
    switch(eventType){
      case OConstants.SUB_PROPERTY_ADDED_EVENT:
        subPropertyIsAdded((RDFProperty)resource);
        break;
      case OConstants.SUB_PROPERTY_REMOVED_EVENT:
        subPropertyIsDeleted((RDFProperty)resource);
        break;
      case OConstants.SUPER_PROPERTY_ADDED_EVENT:
      case OConstants.SUPER_PROPERTY_REMOVED_EVENT:
        superPropertyAffected((RDFProperty)resource);
        break;
      case OConstants.SUPER_CLASS_ADDED_EVENT:
      case OConstants.SUPER_CLASS_REMOVED_EVENT:
        superClassAffected((OClass)resource);
        break;
      case OConstants.SUB_CLASS_ADDED_EVENT:
        subClassIsAdded((OClass)resource);
        break;
      case OConstants.SUB_CLASS_REMOVED_EVENT:
        subClassIsDeleted((OClass)resource);
        break;
    }
    switch(eventType){
      case OConstants.SUB_PROPERTY_ADDED_EVENT:
      case OConstants.SUB_PROPERTY_REMOVED_EVENT:
      case OConstants.SUPER_PROPERTY_ADDED_EVENT:
      case OConstants.SUPER_PROPERTY_REMOVED_EVENT:
        expandNode(propertyTree);
        break;
      default:
        expandNode(tree);
        break;
    }
    rdfPropertyAction.setResoucesList(ontology.getAllResources());
    datatypePropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
    objectPropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
    symmetricPropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
    transitivePropertyAction.setOntologyClassesURIs(ontologyClassesURIs);
    if(isItTree) {
      tree.setSelectionPath(path);
    } else {
      propertyTree.setSelectionPath(path);
    }
  }

  public void addTreeNodeSelectionListener(TreeNodeSelectionListener listener) {
    this.listeners.add(listener);
  }

  public void removeTreeNodeSelectionListener(TreeNodeSelectionListener listener) {
    this.listeners.remove(listener);
  }

  private void fireTreeNodeSelectionChanged(
          ArrayList<DefaultMutableTreeNode> nodes) {
    for(int i = 0; i < listeners.size(); i++) {
      listeners.get(i).selectionChanged(nodes);
    }
  }

  /**
   * the ontology instance
   */
  protected Ontology ontology;

  /**
   * Ontology Item Comparator
   */
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

  protected JButton rdfProperty;

  protected JButton annotationProperty;

  protected JButton datatypeProperty;

  protected JButton objectProperty;

  protected JButton symmetricProperty;

  protected JButton transitiveProperty;

  protected JButton delete;

  protected ArrayList<DefaultMutableTreeNode> selectedNodes;

  protected ArrayList<String> ontologyClassesURIs;

  protected TopClassAction topClassAction;

  protected SubClassAction subClassAction;

  protected InstanceAction instanceAction;

  protected RDFPropertyAction rdfPropertyAction;

  protected AnnotationPropertyAction annotationPropertyAction;

  protected DatatypePropertyAction datatypePropertyAction;

  protected ObjectPropertyAction objectPropertyAction;

  protected SymmetricPropertyAction symmetricPropertyAction;

  protected TransitivePropertyAction transitivePropertyAction;

  protected DeleteOntologyResourceAction deleteOntoResourceAction;

  protected ArrayList<TreeNodeSelectionListener> listeners;

  protected HashMap<String, ArrayList<DefaultMutableTreeNode>> uri2TreeNodesListMap;

  protected HashMap<DefaultMutableTreeNode, URI> reverseMap;

  JScrollPane propertyScroller, scroller;
}
