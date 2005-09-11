package gate.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.tree.*;
import com.ontotext.gate.ontology.TaxonomyImpl;
import gate.Resource;
import gate.creole.AbstractVisualResource;
import gate.creole.ResourceInstantiationException;
import gate.creole.ontology.NoSuchClosureTypeException;
import gate.creole.ontology.OClass;
import gate.creole.ontology.OInstance;
import gate.creole.ontology.Ontology;
import gate.creole.ontology.TClass;
import gate.creole.ontology.Taxonomy;
import gate.event.GateEvent;
import gate.event.ObjectModificationEvent;
import gate.event.ObjectModificationListener;
import gate.util.GateRuntimeException;
import gate.util.LuckyException;

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
//    tree.setCellRenderer(new DefaultTreeCellRenderer());
    tree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
    JScrollPane scroller = new JScrollPane(tree);
    
    mainSplit.setTopComponent(scroller);
    
  }
  
  protected void initListeners(){
    
  }
  
  /**
   * Called when the target of this editor has changed
   */
  protected void rebuildModel(){
    rootNode.removeAllChildren();
    Comparator comparator = new EntityComparator();
    List rootClasses = new ArrayList(taxonomy.getTopClasses());
    Collections.sort(rootClasses, comparator);
    
    addChidrenRec(rootNode, rootClasses, comparator);
    
    SwingUtilities.invokeLater(new Runnable(){
      public void run(){
        treeModel.reload();
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
        try{
          List childList = 
            new ArrayList(aClass.getSubClasses(TClass.DIRECT_CLOSURE));
          Collections.sort(childList, comparator);
          addChidrenRec(childNode, childList, comparator);
        }catch(NoSuchClosureTypeException nsce){
          throw new GateRuntimeException(nsce);
        }

        //add all the instances
        if(ontologyMode){
          List childList = 
            new ArrayList(ontology.getDirectInstances((OClass)aClass));
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
  
  
  protected static class EntityComparator implements Comparator{

    public int compare(Object o1, Object o2){
      if(o1 instanceof TClass && o2 instanceof TClass)
        return ((TClass)o1).getName().compareTo(((TClass)o2).getName());
      else if(o1 instanceof OInstance && o2 instanceof OInstance)
        return ((OInstance)o1).getName().compareTo(((OInstance)o2).getName());
      else return 0;
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
        }else if(nodeObject instanceof OInstance){
          icon = MainFrame.getIcon("Instance.gif");
          itemName = ((OInstance)nodeObject).getName();
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
  
  /**
   * The tree view.
   */
  protected JTree tree;
  
  /**
   * The mode, for the tree.
   */
  protected DefaultTreeModel treeModel;
  
  /**
   * The main split
   */
  protected JSplitPane mainSplit;
  
  /**
   * The root node of the tree.
   */
  protected DefaultMutableTreeNode rootNode;

}
