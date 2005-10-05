package gate.gui;

import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.ontology.*;
import gate.event.*;
import gate.swing.XJTable;
import gate.util.Err;
import gate.util.GateRuntimeException;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.*;
import com.ontotext.gate.ontology.TaxonomyImpl;

public class OntologyEditor extends AbstractVisualResource 
                            implements ResizableVisualResource, 
                                       ObjectModificationListener{

  /* (non-Javadoc)
   * @see gate.creole.AbstractVisualResource#setTarget(java.lang.Object)
   */
  public void setTarget(Object target){
    this.taxonomy = (Taxonomy)target;
    if(target instanceof Ontology){
      this.ontology = (Ontology)target;
      ontologyMode = true;
    }else{
      ontologyMode = false;
    }
    rebuildModel();
    if(taxonomy instanceof TaxonomyImpl){
      ((TaxonomyImpl)taxonomy).addObjectModificationListener(this);
    }
  }
  
  public Resource init() throws ResourceInstantiationException{
    super.init();
    initLocalData();
    initGUIComponents();
    initListeners();
    return this;
  }

  
  protected void initLocalData(){
    itemComparator = new OntologyItemComparator();
  }
  
  protected void initGUIComponents(){
    this.setLayout(new BorderLayout());
    
    mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    this.add(mainSplit, BorderLayout.CENTER);
    
    
    rootNode = new DefaultMutableTreeNode(null, true);
    treeModel = new DefaultTreeModel(rootNode);
    tree = new JTree(treeModel);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setCellRenderer(new OntoTreeCellRenderer());
    tree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
    JScrollPane scroller = new JScrollPane(tree);
    //enable tooltips for the tree
    ToolTipManager.sharedInstance().registerComponent(tree);
    mainSplit.setLeftComponent(scroller);
    
    detailsTableModel = new DetailsTableModel();
    detailsTable = new XJTable(detailsTableModel);
    ((XJTable)detailsTable).setSortable(false);
    DetailsTableCellRenderer renderer = new DetailsTableCellRenderer();
    detailsTable.getColumnModel().getColumn(DetailsTableModel.EXPANDED_COLUMN).
      setCellRenderer(renderer);
    detailsTable.getColumnModel().getColumn(DetailsTableModel.LABEL_COLUMN).
      setCellRenderer(renderer);
    detailsTable.setShowGrid(false);
    detailsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    detailsTable.setColumnSelectionAllowed(false);
    detailsTable.setRowSelectionAllowed(true);
    
    detailsTable.setTableHeader(null);
    detailsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    
    scroller = new JScrollPane(detailsTable);
    scroller.getViewport().setOpaque(true);
    scroller.getViewport().setBackground(detailsTable.getBackground());
    
    mainSplit.setRightComponent(scroller);
    
  }
  
  protected void initListeners(){
    tree.getSelectionModel().addTreeSelectionListener(
      new TreeSelectionListener(){
        public void valueChanged(TreeSelectionEvent e){
          int[] selectedRows = tree.getSelectionRows();
          if(selectedRows != null && selectedRows.length > 0){
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getPathForRow(selectedRows[0]).
                getLastPathComponent();
            detailsTableModel.setItem(node.getUserObject());
          }
        }
    });
    
    mainSplit.addComponentListener(new ComponentListener(){
      public void componentHidden(ComponentEvent e){
      }

      public void componentMoved(ComponentEvent e){
      }

      public void componentResized(ComponentEvent e){
        mainSplit.setDividerLocation(0.7); 
      }

      public void componentShown(ComponentEvent e){
        
      }
      
    });
  }
  
  /**
   * Called when the target of this editor has changed
   */
  protected void rebuildModel(){
    rootNode.removeAllChildren();
    List rootClasses = new ArrayList(taxonomy.getTopClasses());
    Collections.sort(rootClasses, itemComparator);
    
    addChidrenRec(rootNode, rootClasses, itemComparator);
    
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        treeModel.nodeStructureChanged(rootNode);
        tree.setSelectionInterval(0, 0);
        //expand the root
        tree.expandPath(new TreePath(rootNode));
        //expand the entire tree
        for(int i = 0; i < tree.getRowCount(); i++) tree.expandRow(i);
      }
    });
  }
  
  /**
   * Adds the children nodes to a node using values from a list of classes and 
   * instances.
   * @param parent the parent node.
   * @param children the lsit of children objects.
   * @param comparator the Comparator used to sort the children.
   */
  protected void addChidrenRec(DefaultMutableTreeNode parent, List children,
          Comparator comparator){
    Iterator childIter = children.iterator();
    while(childIter.hasNext()){
      Object aChild = childIter.next();
      DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(aChild);
      parent.add(childNode);
      if(aChild instanceof TClass){
        childNode.setAllowsChildren(true);
        //add all the subclasses
        TClass aClass = (TClass)aChild;
        List childList = 
          new ArrayList(aClass.getSubClasses(TClass.DIRECT_CLOSURE));
        Collections.sort(childList, comparator);
        addChidrenRec(childNode, childList, comparator);

        //add all the instances
        if(ontologyMode){
          childList = new ArrayList(ontology.getDirectInstances((OClass)aClass));
          Collections.sort(childList, comparator);
          addChidrenRec(childNode, childList, comparator);
        }
      }else if(aChild instanceof OInstance){
        childNode.setAllowsChildren(false);
      }else{
        throw new GateRuntimeException("Unknown ontology item: " + 
                aChild.getClass().getName() + "!");
      }
      
    }
  }
  
  public void processGateEvent(GateEvent e){
    //ignore
  }

  public void objectCreated(ObjectModificationEvent e){
    //ignore
  }

  public void objectDeleted(ObjectModificationEvent e){
    //ignore
  }

  public void objectModified(ObjectModificationEvent e){
//System.out.println("Ontology updated");   
    rebuildModel(); 
  }
  
  
  protected static class OntologyItemComparator implements Comparator{

    public int compare(Object o1, Object o2){
      if(o1 == null) return o2 == null ? 0 : -1;
      if(o2 == null) return o1 == null ? 0 : 1;
      if(o1 instanceof OntologyResource && o2 instanceof OntologyResource) {
        String s1 = ((OntologyResource)o1).getName();
        String s2 = ((OntologyResource)o2).getName();
        if(s1 == null) return s2 == null ? 0 : -1;
        if(s2 == null) return s1 == null ? 0 : 1;
        return s1.compareTo(s2);
      }else return 0;
    }
  }
  
  protected static class OntoTreeCellRenderer extends DefaultTreeCellRenderer{
    public Component getTreeCellRendererComponent(JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus){
      
        
      if(value != null && value instanceof DefaultMutableTreeNode){
        Icon icon = null;
        String itemName = null;
        Object nodeObject = ((DefaultMutableTreeNode)value).getUserObject();
        if(nodeObject instanceof TClass){
          icon = MainFrame.getIcon("Class.gif");
          itemName = ((TClass)nodeObject).getName();
          setToolTipText(((TClass)nodeObject).getURI());
        }else if(nodeObject instanceof OInstance){
          icon = MainFrame.getIcon("Instance.gif");
          itemName = ((OInstance)nodeObject).getName();
          setToolTipText(((OInstance)nodeObject).getURI());
        }
        if(icon != null){
          if(expanded) setOpenIcon(icon);
          else setClosedIcon(icon);
          if(leaf) setLeafIcon(icon);
        }
        super.getTreeCellRendererComponent(tree, 
                itemName, sel, expanded, leaf, row, 
                hasFocus);
      }else{
        super.getTreeCellRendererComponent(tree, 
                value, sel, expanded, leaf, row, 
                hasFocus);
      }
      return this;
    }

  }
  
  /**
   * A model for the list object displaying the item details.
   */
  protected class DetailsTableModel extends AbstractTableModel{

    public DetailsTableModel(){
      directSuperClasses = new DetailsGroup("Direct Super Classes", true, null);
      allSuperClasses = new DetailsGroup("All Super Classes", true, null);
      directSubClasses = new DetailsGroup("Direct Sub Classes", true, null);
      allSubClasses = new DetailsGroup("All Sub Classes", true, null);
      instances = new DetailsGroup("Instances", true, null);
      properties = new DetailsGroup("Properties", true, null);
      directTypes = new DetailsGroup("Direct Types", true, null);
      allTypes = new DetailsGroup("All Types", true, null);
      detailGroups = new DetailsGroup[]{};

    }
    
    
    public int getColumnCount(){
      return COLUMN_COUNT;
    }

    public int getRowCount(){
      int size = detailGroups.length;
      for(int i = 0; i < detailGroups.length; i++)
        if(detailGroups[i].isExpanded()) size += detailGroups[i].getSize();
      return size;
    }

    
    public String getColumnName(int column){
      switch(column){
        case EXPANDED_COLUMN : return "";
        case LABEL_COLUMN: return "";
        default: return "";
      }
    }

    public Class getColumnClass(int columnIndex){
      switch(columnIndex){
        case EXPANDED_COLUMN : return Boolean.class;
        case LABEL_COLUMN: return Object.class;
        default: return Object.class;
      }
    }


    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int rowIndex, int columnIndex){
      Object value = getItemForRow(rowIndex);
      return columnIndex == EXPANDED_COLUMN && value instanceof DetailsGroup;
    }


    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex){
      Object oldValue = getItemForRow(rowIndex);
      if(columnIndex == EXPANDED_COLUMN && oldValue instanceof DetailsGroup){
        //set the expanded state
        DetailsGroup aGroup = (DetailsGroup)oldValue;
        aGroup.setExpanded(((Boolean)aValue).booleanValue());
      }
      fireTableDataChanged();
    }


    protected Object getItemForRow(int rowIndex){
      int currentIndex = 0;
      int groupIndex = 0;
      while(currentIndex <= rowIndex){
        if(currentIndex == rowIndex) {
          //the item is a DetailsGroup
          return detailGroups[groupIndex];
        }
        //find the increment required to point to the next group
        int increment = 1 + 
            (detailGroups[groupIndex].isExpanded() ? 
             detailGroups[groupIndex].getSize() : 0);
        if(currentIndex + increment > rowIndex){
          //the value is from the current group
          return detailGroups[groupIndex].getValueAt(rowIndex - currentIndex -1);
        }else{
          currentIndex += increment;
          groupIndex++;
        }
      }
      return null;
    }
    
    public Object getValueAt(int rowIndex, int columnIndex){
      Object value = getItemForRow(rowIndex);
      switch(columnIndex){
        case EXPANDED_COLUMN:
          return value instanceof DetailsGroup ?
                 new Boolean(((DetailsGroup)value).isExpanded()) :
                 null;
        case LABEL_COLUMN:
          return value;
        default:
          return null;
      }
    }

    
    /**
     * Used to set the current ontology item for which the details are shown.
     * @param item the item to be displayed.
     */
    public void setItem(Object item){
      if(item instanceof TClass){
        detailGroups = ontologyMode ? 
          new DetailsGroup[]{
                directSuperClasses, allSuperClasses, 
                directSubClasses, allSubClasses,
                properties, instances} :
          new DetailsGroup[]{
                directSuperClasses, allSuperClasses, 
                directSubClasses, allSubClasses};

        //displaying a class
        TClass aClass = (TClass)item;
        //set the direct superClasses
        Set classes = aClass. getSuperClasses(TClass.DIRECT_CLOSURE);
        directSuperClasses.getValues().clear();
        if(classes != null){
          directSuperClasses.getValues().addAll(classes);
          Collections.sort(directSuperClasses.getValues(), itemComparator);
        }

        //set the direct superClasses
        classes = aClass. getSuperClasses(TClass.TRANSITIVE_CLOSURE);
        allSuperClasses.getValues().clear();
        if(classes != null){
          allSuperClasses.getValues().addAll(classes);
          Collections.sort(allSuperClasses.getValues(), itemComparator);
        }
        
        //set the subclasses
        classes = aClass. getSubClasses(TClass.DIRECT_CLOSURE);
        directSubClasses.getValues().clear();
        if(classes != null){
          directSubClasses.getValues().addAll(classes);
          Collections.sort(directSubClasses.getValues(), itemComparator);
        }

        //set the subclasses
        classes = aClass. getSubClasses(TClass.TRANSITIVE_CLOSURE);
        allSubClasses.getValues().clear();
        if(classes != null){
          allSubClasses.getValues().addAll(classes);
          Collections.sort(allSubClasses.getValues(), itemComparator);
        }
        
        if(ontologyMode) {
          //set the properties
          properties.getValues().clear();
          Iterator propIter = new HashSet(ontology.getPropertyDefinitions())
              .iterator();
          //create a local instance to check for properties 
          OInstanceImpl aFakeInstance = new OInstanceImpl("", "", 
                  (OClass)aClass, ontology);
          
          while(propIter.hasNext()) {
            Property prop = (Property)propIter.next();
            if(prop.isValidDomain(aFakeInstance))
              properties.getValues().add(prop);
//            if(mightPropertyApplyToClass(prop, (OClass)aClass)) {
//              properties.getValues().add(prop);
//            }
          }
          Collections.sort(properties.getValues(), itemComparator);
          
          //set the instances
          if(ontologyMode){
            Set instanceSet = ontology.getDirectInstances((OClass)aClass);
            instances.getValues().clear();
            if(instanceSet != null){
              instances.getValues().addAll(instanceSet);
              Collections.sort(instances.getValues(), itemComparator);
            }
          }
        }        
      }else if(item instanceof OInstance){
        //displaying an instance
        OInstance anInstance = (OInstance)item;
        detailGroups = new DetailsGroup[]{directTypes, allTypes, properties};
        
        //set the direct types
        Set classes = anInstance.getOClasses();
        directTypes.getValues().clear();
        if(classes != null){
          directTypes.getValues().addAll(classes);
          Collections.sort(directTypes.getValues(), itemComparator);
        }

        //set all superClasses
        Set allClasses = new HashSet();
        classes = anInstance.getOClasses();
        allClasses.addAll(classes);
        for(Iterator classIter = classes.iterator(); classIter.hasNext();) {
          OClass aClass = (OClass)classIter.next();
          allClasses.addAll(
                  aClass.getSuperClasses(OntologyConstants.TRANSITIVE_CLOSURE));
        }
        allTypes.getValues().clear();
        if(allClasses != null){
          allTypes.getValues().addAll(allClasses);
          Collections.sort(allTypes.getValues(), itemComparator);
        }
        
        properties.getValues().clear();
        Set propNames = anInstance.getSetPropertiesNames();
        if(propNames != null){
          Iterator propIter = propNames.iterator();
          while(propIter.hasNext()){
            String propertyName = (String)propIter.next();
            List propValues = anInstance.getPropertyValues(propertyName);
            if(propValues != null){
              Iterator propValIter = propValues.iterator();
              while(propValIter.hasNext()){
                StringBuffer propText = new StringBuffer(propertyName);
                propText.append("(");
                Object propValue = propValIter.next();
                if(propValue != null)
                  propText.append(propValue instanceof OInstance ?
                          ((OInstance)propValue).getName() :
                           propValue.toString());
                propText.append(")");
                properties.getValues().add(propText.toString());
              }
            }
          }
          Collections.sort(properties.getValues());
        }
      }
      
      fireTableDataChanged();
    }
    
    
    /**
     * Checks whether a property might apply to an instance of the provided
     * class. This is indicative only as the correct decision can only be taken
     * based on the actual instances (in case the property requires domain values
     * to be members of several classes).
     * @param instance the instance
     * @return <tt>true</tt> if the property is valid for the instance.
     */
    protected boolean mightPropertyApplyToClass(Property property, OClass aClass) {
      Set domainClasses = new HashSet(property.getDomain());
      Iterator superPropIter = property.
        getSuperProperties(OntologyConstants.TRANSITIVE_CLOSURE).iterator();
      while(superPropIter.hasNext()) {
        Property aProp = (Property)superPropIter.next();
        if(aProp == null)Err.prln("Null superProp for " + property.getName());
        if(aProp.getDomain() == null) Err.prln("Null domain for " + aProp.getName());
        else domainClasses.addAll(aProp.getDomain());
      }
      
      return domainClasses.contains(aClass);
    }

    protected DetailsGroup directSuperClasses;
    protected DetailsGroup allSuperClasses;
    protected DetailsGroup directSubClasses;
    protected DetailsGroup allSubClasses;
    protected DetailsGroup instances;
    protected DetailsGroup properties;
    protected DetailsGroup directTypes;
    protected DetailsGroup allTypes;
    protected DetailsGroup[] detailGroups;
    
    public static final int COLUMN_COUNT = 2;
    public static final int EXPANDED_COLUMN = 0;
    public static final int LABEL_COLUMN = 1;
  }
  
  
  protected class DetailsTableCellRenderer extends DefaultTableCellRenderer{
    
    public Component getTableCellRendererComponent(JTable table, 
            Object value, boolean isSelected, boolean hasFocus, 
            int row, int column){
      //prepare the renderer
      Component res = super.getTableCellRendererComponent(table, "", 
              isSelected,hasFocus, row, column);

      //set the text and icon
      if(column == DetailsTableModel.EXPANDED_COLUMN){
        setText(null);
        if(value == null) setIcon(null);
        else{
          Object actualValue = detailsTableModel.
              getValueAt(row, DetailsTableModel.LABEL_COLUMN);
          setIcon(MainFrame.getIcon(
                ((Boolean)value).booleanValue() ?
                "expanded.gif" :
                "closed.gif"));
          setEnabled(((DetailsGroup)actualValue).getSize() > 0);
        }
      }else if(column == DetailsTableModel.LABEL_COLUMN){
        if(value instanceof DetailsGroup){
          DetailsGroup aGroup = (DetailsGroup)value;
          setIcon(null);
          setFont(getFont().deriveFont(Font.BOLD));
          setText(aGroup.getName());
          setEnabled(aGroup.getSize() > 0);
        }else if(value instanceof TClass){
          TClass aClass = (TClass)value;
          setIcon(MainFrame.getIcon("Class.gif"));
          setFont(getFont().deriveFont(Font.PLAIN));
          setText(aClass.getName());
          setToolTipText(aClass.getURI());
          setEnabled(true);
        }else if(value instanceof OInstance){
          OInstance anInstance = (OInstance)value;
          setIcon(MainFrame.getIcon("Instance.gif"));
          setFont(getFont().deriveFont(Font.PLAIN));
          setText(anInstance.getName());
          setToolTipText(anInstance.getURI());
          setEnabled(true);
        }else if(value instanceof Property){
          ObjectProperty aProperty = (ObjectProperty)value;
          setIcon(MainFrame.getIcon("param.gif"));
          setFont(getFont().deriveFont(Font.PLAIN));
          String text = aProperty.getName() + " -> ";
          Set range = aProperty.getRange();
          text += range.toString();
          setText(text);
          setToolTipText("<HTML><b>Object Property</b><br>" +
                  aProperty.getURI() + "</html>");
          setEnabled(true);
        }else{
          setIcon(null);
          setFont(getFont().deriveFont(Font.PLAIN));
          setText(value.toString());
          setEnabled(true);
        }
      }
      
      return res;
    }

  }
  /**
   * An object that holds one type of details (i.e. the super classes, or the 
   * properties) of an ontology item (class or instance).
   * @author Valentin Tablan
   */
  protected static class DetailsGroup{
    public DetailsGroup(String name, boolean expanded, Collection values){
      this.name = name;
      this.expanded = expanded;
      this.values = values == null ? new ArrayList() : new ArrayList(values);
    }
    
    public String getName(){
     return name; 
    }
    
    /**
     * @return Returns the expanded.
     */
    public boolean isExpanded(){
      return expanded;
    }
    /**
     * @param expanded The expanded to set.
     */
    public void setExpanded(boolean expanded){
      this.expanded = expanded;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name){
      this.name = name;
    }
    
    public int getSize(){
      return values.size();
    }
    
    public Object getValueAt(int index){
      return values.get(index);
    }

    /**
     * @return Returns the values.
     */
    public List getValues(){
      return values;
    }

    /**
     * @param values The values to set.
     */
    public void setValues(List values){
      this.values = values;
    }
        
    boolean expanded;
    String name;
    List values;
  }
  /**
   * The taxonomy that this editor displays
   */
  protected Taxonomy taxonomy;
  
  /**
   * If the taxonomy being edited is an ontology (i.e. has instances as well)
   * then this member stores it as well.
   */
  protected Ontology ontology;
  
  /**
   * Flag that indicates whether the object beiong edited is an ontology.
   */
  protected boolean ontologyMode;
  
  
  protected OntologyItemComparator itemComparator;
  /**
   * The tree view.
   */
  protected JTree tree;
  
  /**
   * The mode, for the tree.
   */
  protected DefaultTreeModel treeModel;
  
  
  /**
   * The list view used to display item details
   */
  protected JTable detailsTable;
  
  DetailsTableModel detailsTableModel;
  
  /**
   * The main split
   */
  protected JSplitPane mainSplit;
  
  /**
   * The root node of the tree.
   */
  protected DefaultMutableTreeNode rootNode;

}
