package com.ontotext.gate.vr;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;


import com.ontotext.gate.vr.*;


import java.awt.event.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.dnd.peer.*;

import java.io.*;
import java.util.*;
import java.util.List;

import gate.creole.ontology.*;
import gate.util.GateRuntimeException;
import gate.util.Out;

/**Extends {@link javax.swing.JTree} in order to provide
 * means for editing the hierarchy of an ontology
 * borislav popov */
public class EditableTreeView extends JTree
  implements  TreeSelectionListener,DragGestureListener,DropTargetListener,
              DragSourceListener
{

  /** Stores the selected node info */
  protected TreePath selectedTreePath = null;

  /** The currently selected node*/
  protected ClassNode selectedNode = null;


  private JPopupMenu m_popUpMenu;

  private JMenuItem add_item;
  private JMenuItem rename_item;
  private JMenuItem editURI_item;
  private JMenuItem remove_item;
  private JMenuItem view_properties_item;

  private OEMainPanel mainPanel;

  /** dragSource needed for Drag'n'Drop */
  private DragSource dragSource = null;
  /** dragSourceContext needed for Drag'n'Drop */
  private DragSourceContext dragSourceContext = null;
  /** transferable obejct needed for Drag'n'Drop */
  private Transferable transferable = null;


  public EditableTreeView(OntoTreeModel model) {
      super(model);
      init();
  }

  /**
   * Synchronizes the expansion of the given trees.
   * @param orig the original tree
   * @param mirror the tree to mimic the expansion of the original
   */
  public static void synchronizeTreeExpansion(JTree orig, JTree mirror) {
    /*create a Set of expanded node names*/
    /*below will :
      iterate all nodes of the tree
      accumulate the path for each node as an arraylist
      check for each passed node whether the treepath is expanded
      and if expanded add it to the expanded list as a string.
    */
    Set expanded = new HashSet();
    TreeModel model =  orig.getModel();
//    ArrayList expPaths = new ArrayList();

    ArrayList remains = new ArrayList();
    ArrayList remainPaths = new ArrayList();

    remains.add(model.getRoot());
    ArrayList rootPath = new ArrayList();
    rootPath.add(model.getRoot());
    remainPaths.add(rootPath);

    while (remains.size() > 0 ) {
      Object node = remains.get(0);
      int cc = model.getChildCount(node);
      ArrayList parentPath = (ArrayList)remainPaths.get(0);
      for ( int c = 0 ; c < cc ; c++) {
        Object child = model.getChild(node,c);
        remains.add(child);
        ArrayList pp = new ArrayList(parentPath);
        pp.add(child);
        remainPaths.add(pp);
      }
      TreePath tp = new TreePath(parentPath.toArray());
      if (orig.isExpanded(tp)) {
        expanded.add(node.toString());
      }
      remains.remove(0);
      remainPaths.remove(0);
    } // while nodes remain

    /*expand the mirror tree according to the expanded nodes set*/
    /*
      iterate all the nodes and keep their paths
      if a node is found as a string then expand it
    */

    remains = new ArrayList();
    remainPaths = new ArrayList();

    model = mirror.getModel();
    remains.add(model.getRoot());
    rootPath = new ArrayList();
    rootPath.add(model.getRoot());
    remainPaths.add(rootPath);

    while (remains.size() > 0 ) {
      Object node = remains.get(0);
      int cc = model.getChildCount(node);
      ArrayList parentPath = (ArrayList)remainPaths.get(0);
      for ( int c = 0 ; c < cc ; c++) {
        Object child = model.getChild(node,c);
        remains.add(child);
        ArrayList pp = new ArrayList(parentPath);
        pp.add(child);
        remainPaths.add(pp);
      }

      if (expanded.contains(node.toString()) ) {
        TreePath tp = new TreePath(parentPath.toArray());
        mirror.expandPath(tp);
      }
      remains.remove(0);
      remainPaths.remove(0);
    } // while nodes remain

  } // synchronizeTreeExpansion(JTree,JTree)

  /**
   * Sets the main panel of this tree view.
   * should be called anytime a tree is created.
   * @param panel the main panel
   */
  public void setMainPanel(OEMainPanel panel) {
    mainPanel = panel;
  }

  /**Gets the main panel of this tree view
   * @return the main panel   */
  public OEMainPanel getmainPanel(){
     return mainPanel;
  }

  public void setModel(OntoTreeModel model){
      if( model != null)
        super.setModel(model);
      else
        super.setModel(null);
  }

  /** Initializes the tree view.*/
  private void init(){
    getSelectionModel().setSelectionMode(
    TreeSelectionModel.SINGLE_TREE_SELECTION);
    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    renderer.setLeafIcon(renderer.getDefaultClosedIcon());
    this.setCellRenderer(renderer);
    addMouseListener(new MyMouseAdapter(this));
    m_popUpMenu= new  JPopupMenu();

    add_item = new JMenuItem("Add sub class");
    add_item.addActionListener(new AddSubActListener());

    rename_item = new JMenuItem("Rename class");
    rename_item.addActionListener(new RenameListener());

    remove_item = new JMenuItem("Remove class");
    remove_item.addActionListener(new RemoveActListener());

    editURI_item =  new JMenuItem("Edit URI");
    editURI_item.addActionListener(new EditURIListener());

    view_properties_item = new JMenuItem("View Properties");
    view_properties_item.addActionListener(new ViewPropertiesListener());

    m_popUpMenu.add(add_item);
    m_popUpMenu.add(rename_item);
    m_popUpMenu.add(editURI_item);
    m_popUpMenu.add(remove_item);
    m_popUpMenu.add(view_properties_item);

    /* ------- DnD --------- */
    /* in order to keep track of selecteNode and selectedPAth*/
    addTreeSelectionListener(this);

    dragSource = DragSource.getDefaultDragSource() ;


    DragGestureRecognizer dgr =
      dragSource.createDefaultDragGestureRecognizer(
        this,  //DragSource
        DnDConstants.ACTION_MOVE, //specifies valid actions
        this                              //DragGestureListener
      );

    /* Eliminates right mouse clicks as valid actions - useful especially
     * if you implement a JPopupMenu for the JTree*/
    dgr.setSourceActions(dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK);

    /* First argument:  Component to associate the target with
     * Second argument: DropTargetListener */
    DropTarget dropTarget = new DropTarget(this, this);

    putClientProperty("JTree.lineStyle", "Angled");

 } // init

  /*---------------drag and drop--------------*/

  public void dragGestureRecognized(DragGestureEvent event) {
    //Get the selected node
    ClassNode dragNode = this.getSelectedNode();

    if (dragNode != null) {
      //Get the Transferable Object
      transferable = (Transferable) dragNode;


      //Select the appropriate cursor;
      Cursor cursor = DragSource.DefaultMoveNoDrop;

      //begin the drag
      dragSource.startDrag(event, cursor, transferable, this);
    }

  } // dragGestureRecognized()

  public void drop(DropTargetDropEvent e) {

    Transferable tr = e.getTransferable();

    //flavor not supported, reject drop
    if (!tr.isDataFlavorSupported( ClassNode.CLASS_NODE_FLAVOR)) {
      e.rejectDrop();
    }

    //get new parent node
    Point loc = e.getLocation();
    TreePath destinationPath = getPathForLocation(loc.x, loc.y);

    final String msg = testDropTarget(destinationPath, selectedTreePath);
    if (msg != null) {
      e.rejectDrop();

      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JOptionPane.showMessageDialog(
               mainPanel, msg, "Error Dialog", JOptionPane.ERROR_MESSAGE
          );
        }
      });
    } // drop


    ClassNode newParent =
      (ClassNode) destinationPath.getLastPathComponent();

    /*get old parent node*/
    TreePath temPath;
    if (null == (temPath = selectedTreePath.getParentPath())) {
      throw new GateRuntimeException (
        "The node being dragged has no parent." );
    }
    ClassNode oldParent = (ClassNode) temPath.getLastPathComponent();

    int action = e.getDropAction();

    try {
      oldParent.removeSubNode((ClassNode)transferable);
      newParent.addSubNode((ClassNode)transferable);
      e.acceptDrop (DnDConstants.ACTION_MOVE);
    }
    catch (java.lang.IllegalStateException ils) {
      e.rejectDrop();
    }

    e.getDropTargetContext().dropComplete(true);


    this.setExpandedState(temPath,true);
    this.setExpandedState(destinationPath,true);

    this.updateUI();


  } // drop

  public void dragExit(DragSourceEvent dsde) {
    dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
  }

  public void dragExit(DropTargetEvent e) {
  }


  public void dragEnter(DragSourceDragEvent dsde) {
    dragSourceContext = dsde.getDragSourceContext();
  }

  public void dragEnter(DropTargetDragEvent e) {

  }

  public void dropActionChanged(DragSourceDragEvent e) {
    dragSourceContext = e.getDragSourceContext();
  }

  public void dropActionChanged(DropTargetDragEvent e) {
    e.acceptDrag(DnDConstants.ACTION_MOVE);
  }

  public void dragOver(DragSourceDragEvent e) {
    dragSourceContext = e.getDragSourceContext();
  }

  public void dragOver(DropTargetDragEvent e) {
    //set cursor location. Needed in setCursor method
    Point cursorLocationBis = e.getLocation();
    TreePath destinationPath =
      getPathForLocation(cursorLocationBis.x, cursorLocationBis.y);


    // if destination path is okay accept drop...
    if (testDropTarget(destinationPath, selectedTreePath) == null){

        if ( null != dragSourceContext )
          dragSourceContext.setCursor(DragSource.DefaultMoveDrop);

        e.acceptDrag(DnDConstants.ACTION_MOVE);
    }
    // ...otherwise reject drop
    else {

        if ( null != dragSourceContext )
          dragSourceContext.setCursor(DragSource.DefaultMoveNoDrop);

        e.rejectDrag() ;
    } // else
  } // dragOver()


  public void dragDropEnd(DragSourceDropEvent e) {
  } // dragDropEnd()


  /** Convenience method to test whether drop location is valid
  @param destination The destination path
  @param dropper The path for the node to be dropped
  @return null if no problems, otherwise an explanation
  */
  private String testDropTarget(TreePath destination, TreePath dropper) {
    //Test 1.
    boolean destinationPathIsNull = destination == null;
    if (destinationPathIsNull)
      return "Invalid drop location.";

    //Test 2.
    ClassNode node = (ClassNode) destination.getLastPathComponent();

    if (destination.equals(dropper))
      return "Destination cannot be same as source";

    //Test 3.
    if ( dropper.isDescendant(destination))
       return "Destination node cannot be a descendant.";

    //Test 4.
    if ( dropper.getParentPath().equals(destination))
       return "Destination node cannot be a parent.";

    return null;
  } //testDropTarget()

  /** sets selected node */
  public void valueChanged(TreeSelectionEvent evt) {
    selectedTreePath = evt.getNewLeadSelectionPath();
    if (selectedTreePath == null) {
      selectedNode = null;
      return;
    }
    selectedNode =
      (ClassNode)selectedTreePath.getLastPathComponent();
  } // valueChanged()

  /** Returns the selected node */
  public ClassNode getSelectedNode() {
    return selectedNode;
  }




  /*END---------------drag and drop--------------*/


  class MyMouseAdapter extends MouseAdapter{
      private EditableTreeView view;

      public MyMouseAdapter(EditableTreeView view){
          this.view=view;
      }

      public void mouseClicked(MouseEvent e){
          TreePath path=view.getSelectionPath();
          javax.swing.JTree tree = new javax.swing.JTree();
          //IOntoFolder node =null;
          IFolder node =null;
          if(SwingUtilities.isRightMouseButton(e)){
            if( path != null){
              //node = (IOntoFolder)path.getLastPathComponent();
              node = (IFolder)path.getLastPathComponent();
              ClassNode cnode = (ClassNode) node;
              if (cnode.getSource() instanceof Ontology) {
                rename_item.setEnabled(false);
                remove_item.setEnabled(false);
              }
              else {
                rename_item.setEnabled(true);
                remove_item.setEnabled(true);
              }

              m_popUpMenu.show(view,e.getX(),e.getY());
            }
          }
      }
  } //class MyMouseAdapter

  /*Action Listener of the add pop up menu item */
  class AddSubActListener implements ActionListener{
    public void actionPerformed(ActionEvent e) {
      JMenuItem item = (JMenuItem)e.getSource();
      JPopupMenu popup = (JPopupMenu)item.getParent();
      EditableTreeView tree = (EditableTreeView)popup.getInvoker();
      OEMainPanel panel = tree.getmainPanel();

      if (null == panel) {
        throw new GateRuntimeException(
        "the main panel of the editor is not reachable\n "+
        "upon add sub class from the popup");
      }// if null

      OntologyEditor oe = panel.getOntologyEditor();

      if (null == oe) {
        throw new GateRuntimeException(
        "the ontology editor of the main panel is not reachable\n "+
        "upon add sub class from the popup");
      }// if null

      oe.addSubClass((int)EditableTreeView.this.getLocationOnScreen().getX()+50,
        (int)EditableTreeView.this.getLocationOnScreen().getY()+50);

    } // actionPerformed();
  } // class AddSubActListener

  /*Action Listener of the remove pop up menu item */
  class RemoveActListener implements ActionListener{
    public void actionPerformed(ActionEvent e) {
      JMenuItem item = (JMenuItem)e.getSource();
      JPopupMenu popup = (JPopupMenu)item.getParent();
      EditableTreeView tree = (EditableTreeView)popup.getInvoker();
      ClassNode node = (ClassNode)tree.getLastSelectedPathComponent();
      OEMainPanel panel = tree.getmainPanel();

      if (null == panel) {
        throw new GateRuntimeException(
        "the main panel of the editor is not reachable\n "+
        "upon add sub class from the popup");
      }// if null

      OntologyEditor oe = panel.getOntologyEditor();

      if (null == oe) {
        throw new GateRuntimeException(
        "the ontology editor of the main panel is not reachable\n "+
        "upon add sub class from the popup");
      }// if null

      oe.removeClass(node);

    } // actionPerformed()
  } // class RemoveActListener

  /*Action Listener of the edit URI pop up menu item */
  class EditURIListener implements ActionListener{
    public void actionPerformed(ActionEvent e) {
      JMenuItem item = (JMenuItem)e.getSource();
      JPopupMenu popup = (JPopupMenu)item.getParent();
      EditableTreeView tree = (EditableTreeView)popup.getInvoker();
      ClassNode node = (ClassNode)tree.getLastSelectedPathComponent();
      OEMainPanel panel = tree.getmainPanel();

      if (null == panel) {
        throw new GateRuntimeException(
        "the main panel of the editor is not reachable\n "+
        "upon rename class from the popup");
      }// if null

      OntologyEditor oe = panel.getOntologyEditor();

      if (null == oe) {
        throw new GateRuntimeException(
        "the ontology editor of the main panel is not reachable\n "+
        "upon rename class from the popup");
      }// if null

      Object obj = node.getSource();
      if ( null == obj ) {
        throw new GateRuntimeException(
          "the class/ontology is null; in EditURIListener");
      }
      if (obj instanceof OClass) {
        oe.editClassURI((OClass)obj,0,0);
      }
      if (obj instanceof Ontology) {
        oe.editURI((Ontology)obj,0,0);
      }
    } // actionPerformed()
  } // class RemoveActListener

  /*Action Listener of the rename pop up menu item */
  class RenameListener implements ActionListener{
    public void actionPerformed(ActionEvent e) {
      JMenuItem item = (JMenuItem)e.getSource();
      JPopupMenu popup = (JPopupMenu)item.getParent();
      EditableTreeView tree = (EditableTreeView)popup.getInvoker();
      ClassNode node = (ClassNode)tree.getLastSelectedPathComponent();
      OEMainPanel panel = tree.getmainPanel();

      if (null == panel) {
        throw new GateRuntimeException(
        "the main panel of the editor is not reachable\n "+
        "upon rename class from the popup");
      }// if null

      OntologyEditor oe = panel.getOntologyEditor();

      if (null == oe) {
        throw new GateRuntimeException(
        "the ontology editor of the main panel is not reachable\n "+
        "upon rename class from the popup");
      }// if null

      Object obj = node.getSource();
      if ( null == obj ) {
        throw new GateRuntimeException(
          "the class/ontology is null; in EditURIListener");
      }
      if (obj instanceof OClass) {
        OClass rc = (OClass)obj;
        oe.renameClass(rc,node,0,0);
      }
      if (obj instanceof Ontology) {
        Ontology ro = (Ontology) obj;
        oe.renameOntology(ro,0,0);
      }
      EditableTreeView.this.updateUI();
    } // actionPerformed()

  } // class RemoveActListener

  /**Listener for choosing [view Properties] from the popup menu */
  private class ViewPropertiesListener implements ActionListener{
    public void actionPerformed(ActionEvent e) {
      JMenuItem item = (JMenuItem)e.getSource();
      JPopupMenu popup = (JPopupMenu)item.getParent();
      EditableTreeView tree = (EditableTreeView)popup.getInvoker();
      ClassNode node = (ClassNode)tree.getLastSelectedPathComponent();
      OEMainPanel panel = tree.getmainPanel();

      if (null == panel) {
        throw new GateRuntimeException(
        "the main panel of the editor is not reachable\n "+
        "upon rename class from the popup");
      }// if null

      OntologyEditor oe = panel.getOntologyEditor();

      if (null == oe) {
        throw new GateRuntimeException(
        "the ontology editor of the main panel is not reachable\n "+
        "upon rename class from the popup");
      }// if null

      Object obj = node.getSource();
      if ( null == obj ) {
        throw new GateRuntimeException(
          "the class/ontology is null; in EditURIListener");
      }
      if (obj instanceof OClass) {
        OClass theClass = (OClass) obj;
        if (theClass.getProperties() == null)
          return;
        Out.println("Properties for class " + theClass.getName());
        Iterator ip = theClass.getProperties().iterator();
        while (ip.hasNext())
          Out.println( ip.next().toString());

      } else if (obj instanceof OInstance) {
        OInstance theInstance = (OInstance) obj;
        OClass instClass = theInstance.getOClass();
        Set props = instClass.getProperties();
        if(props != null && !props.isEmpty()){
          Iterator iter = props.iterator();
          while (iter.hasNext()) {
            gate.creole.ontology.Property prop = (Property) iter.next();
            //iterate over the values
            List values = theInstance.getPropertyValues(prop.getName());
            if(values != null){
              Iterator valIter = values.iterator();
              while(valIter.hasNext()){
                Out.println("[" + prop.getName() + "=" +
                        valIter.next().toString() 
                     + "]");
                
              }
            }
             
          }
        }         
      }

    } // actionPerformed
  } //class ViewPropertiesListener

} // class EditableTreeView