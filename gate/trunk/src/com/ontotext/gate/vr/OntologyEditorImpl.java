package com.ontotext.gate.vr;

import gate.gui.*;
import gate.creole.ontology.*;
import gate.creole.*;
import gate.event.*;
import gate.*;
import gate.util.*;

import com.ontotext.gate.vr.dialog.*;
import com.ontotext.gate.ontology.*;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.Component;

/** Implementation of an ontology editor */
public class OntologyEditorImpl
  extends AbstractVisualResource
  implements OntologyEditor, CreoleListener, ObjectModificationListener
{
  /**the size of the editor (x)  */
  public final static int SIZE_X = 500;
  /**the size of the editor (y)  */
  public final static int SIZE_Y = 400;
  /**the position of the editor (x)  */
  public final static int POSITION_X = 300;
  /**the position of the editor (y)  */
  public final static int POSITION_Y = 200;

  /** the main panel */
  private OEMainPanel panel = new OEMainPanel();

  /** flag indicating the mode of the editor. If with onto list then in an extended mode.*/
  boolean withOntoList = true;

  /** the current ontology path */
  private String path;

  /** same as ontoList */
  private Object target;

  private Handle handle;

  /** The name of the ontology */
  private String name;

  /** The ontology currently displayed */
  private Taxonomy ontology;

  /** The list of ontologies */
  private Vector ontoList;

  /** Ontology vs Tree map */
  private Map OvsT = new HashMap();

  public OntologyEditorImpl() {
    panel.setOntologyEditor(this);
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add(panel);
    gate.Gate.addCreoleListener(OntologyEditorImpl.this);
    OntologyImpl.addObjectModificationListener(this);
  }//constructor

  /**Is called when an ontology has been selected from the ontology list
   * @param o the selected ontology */
  public void ontologySelected(Taxonomy o) {
    if ( null != o ) {
      ontology = o;

      if (null == o.getURL()) {
        throw new GateRuntimeException("null URL for ontology "+o.getName());
      }

      boolean saveEnabled = (-1 == o.getURL().getProtocol().indexOf("jar"));
      panel.fileSave.setEnabled(saveEnabled);
      panel.saveItem.setEnabled(saveEnabled);

      String curPath = o.getURL().getPath();
      int index = curPath.lastIndexOf("/");
      if (curPath.length()-1==index)
        curPath = curPath.substring(0,index);
      if ( -1 != index ) {
        path = curPath.substring(0,index);
      }
      panel.oTree = (JTree)OvsT.get(o);
      if ( null == panel.oTree ) {
        panel.buildOntoTree(o);
        OvsT.put(o,panel.oTree);
      } else {
        panel.setOntoTree(panel.oTree);
      }
      panel.oTree.setVisible(true);

    } else {
      panel.oTree.setVisible(false);
    }//else : ontology is null

  }  // ontologySelected(Ontology)


/**Invokes an add sub class dialog in position x,y
 * @param x the x coordinate of the dialog
 * @param y the y coordinate of the dialog*/
 public void addSubClass(int x, int y) {
  AddSubClassDialog dialog = new AddSubClassDialog();
  ClassNode node = (ClassNode)panel.oTree.getLastSelectedPathComponent();
  dialog.setTitle("add a sub class of "+node.toString());
  dialog.setInvokers(this,node);
  if ( 0 == x && 0 == y ) {
    dialog.setLocationRelativeTo(panel.oTree);
  } else {
    dialog.setLocation(x,y);
  }
  dialog.setVisible(true);
} // addSubClass

/**addSubClass given a ClassNode and the resulting info from the dialog
 * @param root the node which is root to the sub class being added
 * @param className the name from the dialog
 * @param classComment the comment from the dialog */
public void addSubClass(ClassNode root, String className, String classComment) {
  Object o = root.getSource();
  if ( o instanceof Taxonomy) {
    Taxonomy onto = (Taxonomy) o;
    TClass clas = onto.createClass(className,classComment);
    clas.setURI(onto.getSourceURI().substring(0,
        onto.getSourceURI().lastIndexOf("#")+1)+className);
    ClassNode subNode = new ClassNode(clas);
    Vector kids = root.children();
    kids.add(subNode);
    root.setChildren(kids);

    panel.oTree.updateUI();

  } // if ontology
  else {
    if ( o instanceof TClass) {
      TClass clas = (TClass) o;
      TClass subClass = clas.getOntology().createClass(className,classComment);
      subClass.setURI(clas.getURI().substring(0,clas.getURI().lastIndexOf("#")+1)
          + className);
      ClassNode subNode = new ClassNode(subClass);

      Vector rChildren = root.children();
      rChildren.add(subNode);
      root.setChildren(rChildren);

      clas.addSubClass(subClass);

      panel.oTree.updateUI();
    } else {
      throw new GateRuntimeException(
        "class node's source is neither TClass, neither Ontology");
    } // neither class neither ontology
  } // else

} // addSubClass


/**Removes the node/class
 * @param node the node to be removed*/
public void removeClass(ClassNode node) {
  Object source = node.getSource();

  if (source instanceof Taxonomy) {

  } else {

    if (source instanceof TClass) {

      TClass clas = (TClass) source;
      clas.getOntology().removeClass(clas);

      if ( panel.oTree.getAnchorSelectionPath() != null ) {
        TreePath path = panel.oTree.getAnchorSelectionPath().getParentPath();
        if (null == path)
          throw new GateRuntimeException("selection path is null (on removing class)");

        ClassNode parentNode = (ClassNode)path.getLastPathComponent();

        Vector kids = parentNode.children();
        kids.remove(node);
        parentNode.setChildren(kids);

        /* the default behaviour is to make all sub nodes
        top nodes.this could be optional on request */
        kids = node.children();
        ClassNode rootNode =
            (ClassNode)panel.oTree.getPathForRow(0).getLastPathComponent();
        kids.addAll(rootNode.children());
        rootNode.setChildren(kids);
      }

      panel.oTree.updateUI();
    } // if oclas

  } // else

} // removeClass

/**Renames a class
 * @param c the class to be renamed
 * @param n the class node associated with the class
 * @param x coords
 * @param y coords */
public void renameClass(TClass c,ClassNode n, int x, int y) {
  if ( null == c )
    throw new GateRuntimeException(
    "ontology class parameter is null while renaming ");
  if ( null == n )
    throw new GateRuntimeException(
    "class node parameter is null while renaming ");

  RenameClassDialog dialog = new RenameClassDialog(this,panel,n,c);

  dialog.nameField.setText(c.getName());
  dialog.commentField.setText(c.getComment());

  if ( 0 == x && 0 == y) {
    dialog.setLocationRelativeTo(panel.oList);
  } else {
    dialog.setLocation(x,y);
  }

  dialog.setTitle("Rename Class "+c);
  dialog.setVisible(true);
}



/** Visualizes the editor */
public void visualize() {
  ontologySelected(ontology);

  setOntologyList( new Vector (
    Gate.getCreoleRegister().getLrInstances(
    "com.ontotext.gate.ontology.DAMLOntology")));

  panel.setVisible(true);

} // visualize()

/**Creates a new ontology
 * @param name the name of the ontology
 * @param sourceURI
 * @param theURL
 * @param comment */
public void createOntology (
  String name, String sourceURI, String theURL, String comment)
  throws ResourceInstantiationException {
  try {
    Taxonomy o = new DAMLOntology();
    o.setComment(comment);

    URL localurl=null;
    try {
      localurl = new URL(theURL);
      o.setURL(localurl);
    } catch (MalformedURLException urle) {
        throw new ResourceInstantiationException(urle);
    } // catch

    o.setName(name);
    o.setSourceURI(sourceURI);

    try {
      Main.getMainFrame().resourceLoaded(
        new gate.event.CreoleEvent(o,gate.event.CreoleEvent.RESOURCE_LOADED));
    } catch (gate.util.GateException ge) {
      throw new GeneralEditorException(
          "\ncannot create new ontology because of:"+
          "\ngate.util.GateException:\n"+
          ge.getMessage()+"\n");
    }

    ontoList.add(o);
    setOntologyList(ontoList);
    setOntology(o);

    panel.oList.setSelectedIndex(ontoList.size()-1);

    panel.oList.updateUI();
    panel.oTree.updateUI();
  }catch (Exception x) {
    if (!(x instanceof ResourceInstantiationException))
      throw new ResourceInstantiationException(x);
  }
} // createOntology()

/** Sets ontology to be loaded in the editor
 * @param o the ontology to be loaded */
public void setOntology(Taxonomy o) {
  ontology = o;
  ontologySelected(o);
} // setOntology();

public Taxonomy getOntology() {
   return ontology;
}

public void setOntologyList(Vector list) {
  ontoList = list;
  target = list;
  panel.setOntologyList(list);
}

public Vector getOntologyList() {
  return ontoList;
}

public void setTarget(Object target) {

  this.target = target;

  if ( target instanceof Vector ) {
    ontoList = (Vector)target;
  } else {
    if (target instanceof Taxonomy) {
      ontology = (Taxonomy) target;

      Vector olist = new Vector(Gate.getCreoleRegister().getLrInstances("com.ontotext.gate.ontology.DAMLOntology"));
      setOntologyList( olist );

      setOntology(ontology);
      panel.setVisible(true);
      panel.listPanel.setVisible(false);
      withOntoList = false;
      panel.withOntoList=false;
      panel.fileNew.setEnabled(false);
      panel.fileOpen.setEnabled(false);
    } else {
      throw new  GateRuntimeException("setTarget should be called with a \n"+
          "java.util.Vector or gate.creole.ontology.Ontology");
    } // else
  } // else

}// setTarget(Object)

  public void setHandle(Handle handle) {
    this.handle = handle;
  }

  public Resource init() throws ResourceInstantiationException {
    panel.setOntologyEditor(this);
    return this;
  }


  public void cleanup() {
    handle = null;
    panel = null;
    target = null;
    ontoList = null;
    ontology = null;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

/**Get Modified Ontologies
 * @return list of the modified ontologies */
public Vector getModifiedOntologies() {
  Vector modified = new Vector();
  for ( int i = 0 ; i<ontoList.size(); i++) {
    Taxonomy o = (Taxonomy)ontoList.get(i);
    if (o.isModified())
      modified.add(o);
  } // for ontologies
  return modified;
} // getModifiedOntologies()


/**Save a list of ontologies.
 * @param list a list of ontologies to be saved*/
public void saveOntologies(Vector list) {
  try {
    if (null != list) {
      for ( int i = 0 ; i < list.size() ; i++) {
        this.saveOntology((Taxonomy) list.get(i));
      } // for list
    } // not null list
  } catch (Exception x) {
    x.printStackTrace(Err.getPrintWriter());
  }

} // saveOntologies

/**close list of ontologies
 * @param list a list of ontologies to be saved*/
public void closeOntologies(Vector list)throws ResourceInstantiationException{
  if ( null != list ) {
    Vector modified = new Vector();
    Vector unmodified = new Vector();
    for ( int i = 0 ; i < list.size(); i++) {
      Taxonomy o = (Taxonomy)list.get(i);
      if ( o.isModified()) {
        modified.add(o);
      } else {
        unmodified.add(o);
      }
    }// for ontologies

    for ( int i = 0 ; i < list.size() ; i++ ) {
      Taxonomy o = (Taxonomy ) list.get(i);
      /** set modified to false and handle saves explicitly */
      o.setModified(false);
      closeOntology(o,0,0);

    }

    fileSave(0,0,modified);
  } // if not null list

} // closeOntologies

/*----------ontologies list popup menu item listeners------------*/

/**save this ontology
 * @param o the ontology to be saved */
public void saveOntology(Taxonomy o) throws ResourceInstantiationException {
  o.store();
}

/** invoke a saveas dialog for this ontology and save it
 *  to the location specified
 *  @param o the ontology to be saved
 *  @param x the x coordinate of the save as dialog
 *  @param y the y coordinate of the save as dialog*/
public void saveAsOntology(Taxonomy o, int x, int y) throws ResourceInstantiationException {
  try {
    JFileChooser chooser = MainFrame.getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

    int result = chooser.showSaveDialog(panel.oList);
    if ( result == JFileChooser.APPROVE_OPTION ) {
      File selected = chooser.getSelectedFile();
      URL url = new URL("file:///"+selected.getAbsolutePath());
      o.setURL(url);
      o.store();
    } // approve
  } catch (Exception e) {
    throw new ResourceInstantiationException(e);
  }
} // saveAsOntology

/**rename an ontology. if the x and y coordinates are not set,
 * then the default position is LocationRelativeTo
 * the ontologies list.
 * @param o the ontology to be renamed
 * @param x the x coordinate of the rename dialog
 * @param y the y coordinate of the rename dialog*/
public void renameOntology(Taxonomy o, int x, int y) {
  if ( null == o )
    throw new GateRuntimeException(" ontology parameter is null while renaming ");
  RenameOntologyDialog dialog = new RenameOntologyDialog(this,o);

  dialog.nameField.setText(o.getName());
  dialog.commentField.setText(o.getComment());

  dialog.setLocationRelativeTo(panel.oList);

  dialog.setTitle("Rename Ontology "+o);
  dialog.setVisible(true);
}// renameOntology

/**delete an ontology. invoke  if the
 * ontology has been changed. currently deleteOntology
 * works as closeOntology. does not delete the file.
 * even asks for saving it if modified.
 * @param o the ontology to be deleted
 * @param x x coordinate of the option pane to be invoked
 * @param y y coordinate of the option pane to be invoked*/
public void deleteOntology(Taxonomy o, int x, int y)
  throws ResourceInstantiationException {
  int index = ontoList.indexOf(o);
  if ( -1 != index ) {
    int option = JOptionPane.NO_OPTION;

    if (o.isModified()) {
      option = AskWannaSave(o,x,y);
    }

    if ( JOptionPane.CANCEL_OPTION != option ) {
      ontoList.remove(o);
      OvsT.remove(o);
      if (index > 0 ) {
        index--;
      }
      panel.setOntologyList(ontoList);
      panel.oTree.setVisible(false);
    } // cancel

    if ( JOptionPane.YES_OPTION == option ) {
      o.store();
    } // yes

    Factory.deleteResource(o);

    Gate.getCreoleRegister().resourceUnloaded(
      new CreoleEvent(o,CreoleEvent.RESOURCE_UNLOADED));
  } // if ontology is in the list
} // deleteOntology

/** edit the URI of an ontology
 * @param o the ontology to be edited
 * @param x  coords of the dialog
 * @param y  coords of the dialog */
public void editURI(Taxonomy o, int x, int y) {
  EditURIDialog dialog = new EditURIDialog(this,o);
  dialog.setLocationRelativeTo(panel.oList);

  dialog.setTitle("Edit URI of Ontology : "+o);
  dialog.setVisible(true);
} // editURI()

/** edit the URI of an ontology class
 * @param c class to be edited
 * @param x  coords of the dialog
 * @param y  coords of the dialog */
public void editClassURI(TClass c, int x, int y){
  EditClassURIDialog dialog = new EditClassURIDialog(this,c);
  dialog.setLocationRelativeTo(panel.oTree);

  dialog.setTitle("Edit URI of class : "+c);
  dialog.setVisible(true);
} // editClassURI()



/**
 * @return all the uris that are available in the editor
 */
public Set getAllURIs() {
  Set result = new HashSet();
  for ( int i = 0 ; i < ontoList.size(); i++ ) {
    String u = ((Taxonomy)ontoList.get(i)).getSourceURI();

    result.add(u);
  }
  return result;
} // getAllURIs()

/**retrieve a set of all the URIs in an ontology
 * @param o the ontology
 * @return set of all the URIs in the ontology
 */
public Set getAllURIs(Taxonomy o) {
  Set result = new HashSet();
  Iterator ci = o.getClasses().iterator();
  while(ci.hasNext()) {
    TClass c = (TClass) ci.next();
    result.add(c.getURI());
  }
  return result;
} // getAllURIs(Ontology)


/**close an ontology. invoke AreYouSureDialog if the
 * ontology has been changed.
 * @param o the ontology to be closed
 * @param x x coordinate of the option pane to be invoked
 * @param y y coordinate of the option pane to be invoked*/
public void closeOntology(Taxonomy o, int x, int y)
  throws ResourceInstantiationException{
  int index = ontoList.indexOf(o);
  if ( -1 != index ) {

    int option = JOptionPane.NO_OPTION;

    if (o.isModified()) {
      option = AskWannaSave(o,x,y);
    }

    if ( JOptionPane.CANCEL_OPTION != option ) {
      ontoList.remove(o);
      OvsT.remove(o);
      if (index > 0 ) {
        index--;
      }

      panel.setOntologyList(ontoList);
      panel.oTree.setVisible(false);
    } // cancel

    if ( JOptionPane.YES_OPTION == option ) {
      o.store();
    } // yes

    Factory.deleteResource(o);

    Gate.getCreoleRegister().resourceUnloaded(
      new CreoleEvent(o,CreoleEvent.RESOURCE_UNLOADED));
  } // if ontology is in the list

} // closeOntology


/*End-------ontologies list popup menu item listeners------------*/

/*------------- menu bar methods --------------*/

/**checks for unsaved ontologies and disposes the main panel*/
public void fileExit() {
  fileSave(0,0,this.getModifiedOntologies());

} // fileExit()


public void fileOpen(int x,int y) throws ResourceInstantiationException {
  try {
    JFileChooser chooser = MainFrame.getFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    int result = chooser.showOpenDialog(panel.oList);
    if ( result == JFileChooser.APPROVE_OPTION ) {
      File selected = chooser.getSelectedFile();
      URL url = new URL("file:///"+selected.getAbsolutePath());

      FeatureMap fm = Factory.newFeatureMap();
      fm.put("URL",url);
      Taxonomy damlo;

      damlo = (Taxonomy)Factory.createResource(
          "com.ontotext.gate.ontology.DAMLOntology",
          fm
        );

      if ( this.ontoList.contains(damlo) ) {
        JOptionPane.showMessageDialog(panel,
          "The newly loaded ontology "+damlo+
        "\nis already in the editor's list of ontologies.\n"+
          ontology.getURL(),
          "Ontology Load",JOptionPane.WARNING_MESSAGE);
      }

      Vector ol = this.getOntologyList();
      ol.add(damlo);
      this.setOntologyList(ol);
    } // approve
  } catch (IOException ioe) {
    throw new ResourceInstantiationException(ioe);
  }
} // fileOpen()

/**
 * invoke a mutiple selection save dialog with a list of ontologies.
 * @param x  coords of the dialog
 * @param y  coords of the dialog
 * @param ontologies the list of ontologies to be optionally saved
 */
public void fileSave(int x, int y, Vector ontologies) {
  if ( null == ontologies)
    throw new GateRuntimeException("null ontologies parameter");

  if (withOntoList) {
    if ( 0 != ontologies.size() ) {
      Vector ontolos = new Vector();
      //filter ontologies that are outside of the jar
      for ( int i = 0; i < ontologies.size() ; i++) {
        Taxonomy o = (Taxonomy)ontologies.get(i);
        if (-1 == o.getURL().getProtocol().indexOf("jar"))
          ontolos.add(o);
      }
      MultipleSelectionDialog dialog = new MultipleSelectionDialog(
        this,
        ontolos,
        "Please select the ontologies to be saved.",
        "Multiple Selection Save Dialog"
      );
      if ( 0 == x && 0 == y ) {
        dialog.setLocationRelativeTo(panel);
      } else {
        dialog.setLocation(x,y);
      }
      dialog.okBtn.addActionListener(new SaveOKListener(dialog));
      dialog.setVisible(true);
    } // if not zero
    else { // check for one ontology
      if(ontology != null) {
        Vector theOne = new Vector(1);
        theOne.add(ontology);
        saveOntologies(theOne);
      } // if
    }
  } else {
    // without ontology list
    if (ontology != null) {
      if (ontology.isModified()) {
        AskWannaSave(ontology,0,0);
      }
    } // o not null
  } //else

} //fileSave();

/**
 * invoke a mutiple selection close dialog with a list of ontologies.
 * @param x  coords of the dialog
 * @param y  coords of the dialog
 * @param ontologies the list of ontologies to be optionally closed
 */
public void fileClose(int x, int y,Vector ontologies) {
  if ( null == ontologies)
    throw new GateRuntimeException("null ontologies parameter");

  if ( 0 != ontologies.size() ) {
    MultipleSelectionDialog dialog = new MultipleSelectionDialog(
      this,
      ontologies,
      "Please select the ontologies to be closed.",
      "Multiple Selection Close Dialog"
    );
    if ( 0 == x && 0 == y ) {
      dialog.setLocationRelativeTo(panel);
    } else {
      dialog.setLocation(x,y);
    }
    dialog.okBtn.addActionListener(new CloseOKListener(dialog));
    dialog.setVisible(true);
  } // if not zero
} // fileClose()

/**inovke a 'new ontology dialog'
 * @param x  coords of the dialog
 * @param y  coords of the dialog */
public void fileNew(int x, int y) {
  NewOntologyDialog dialog = new NewOntologyDialog(this);
  dialog.setLocationRelativeTo(panel);
  dialog.setTitle("New Ontology Dialog");
  dialog.setVisible(true);
} // fileNew()


/**Wanna Save Dialog invocation. currently the x and y parameters
 * are not used since the option pane is by default initialized
 * with position   setLocationRelativeTo(parentComponent)
 * @param o the ontology to be saved or not
 * @param x x coordinate of the WannaSaveDialog to be invoked
 * @param y y coordinate of the WannaSaveDialog to be invoked
 * @return the result of the option pane execution*/
public int AskWannaSave(Taxonomy o, int x, int y) {
  JOptionPane opane = new JOptionPane();

  int option = opane.showConfirmDialog(panel,"The ontology "+o+
    " has been modified\n"+
    "Save the ontology?"
    ,"Wanna Save Option Pane"
    ,JOptionPane.YES_NO_CANCEL_OPTION
    ,JOptionPane.QUESTION_MESSAGE
    );

  return option;
} // AskWannaSave


/*---------implementation of CreoleListener interface--------------*/
  /**Called when a new {@link gate.Resource} has been loaded into the system*/
  public void resourceLoaded(CreoleEvent e) {
    Resource r;

    if ( (r = e.getResource() )instanceof Ontology ) {
      if (null != panel ) {

        Vector olist = new Vector(Gate.getCreoleRegister().getLrInstances(
                "com.ontotext.gate.ontology.DAMLOntology"));
        setOntologyList(olist);
      } // if panel
    } // if ontology
  } // resource loaded

  /**Called when a {@link gate.Resource} has been removed from the system*/
  public void resourceUnloaded(CreoleEvent e) {
    Resource r;
    if ( (r  = e.getResource() )instanceof Taxonomy ) {
      try {
        Taxonomy o = (Taxonomy)r;
        int option = JOptionPane.NO_OPTION;

        if (o.isModified()) {
          option = AskWannaSave(o,0,0);
        }

        if ( JOptionPane.YES_OPTION == option ) {
          o.store();
        } // yes
      } catch (ResourceInstantiationException ex) {
        JOptionPane.showMessageDialog(panel,
          "Close ontology failed.\n"+

          ((Taxonomy)r).getURL()+"\n"+
          "Due to :"+ex.getClass()+":\nMessage:"+ex.getMessage(),
          "Ontology Close Failure",JOptionPane.ERROR_MESSAGE);
      }

      if (null != panel && null != panel.oTree) {
        Vector olist = new Vector(Gate.getCreoleRegister().getLrInstances(
                "com.ontotext.gate.ontology.DAMLOntology"));
        olist.remove(r);
        setOntologyList(olist);

        if (null != ontology && ontology.equals(r)) {
          if ( olist.size() > 0 ) {
            setOntology((Taxonomy)olist.get(0));
          } else {
            setOntology(null);
          }
        } // equals ontology

      } // if
    } // if
  } // resourceUnloaded()

  /**Called when a {@link gate.DataStore} has been opened*/
  public void datastoreOpened(CreoleEvent e){
  }

  /**Called when a {@link gate.DataStore} has been created*/
  public void datastoreCreated(CreoleEvent e){
  }

  /**Called when a {@link gate.DataStore} has been closed*/
  public void datastoreClosed(CreoleEvent e){
  }

  /**
   * Called when the creole register has renamed a resource.1
   */
  public void resourceRenamed(Resource resource, String oldName,
                              String newName){
    if ( resource instanceof Taxonomy ) {
      if (ontology.equals(resource)) {
        ontology.setName(newName);
        Object daRoot = panel.oTree.getModel().getRoot();
        if (daRoot instanceof ClassNode) {
          ((ClassNode)daRoot).rename(newName);
        }
      }
    }

  }
/*---------implementation of CreoleListener interface--------------*/

/*->->->---implementation of ObjectModificationListener interface--------------*/
  public void processGateEvent(GateEvent e) {
  }

  public void objectCreated(ObjectModificationEvent e) {
  }

  public void objectDeleted(ObjectModificationEvent e) {
  }

  public void objectModified(ObjectModificationEvent e) {
    Object source = e.getSource();
    EditableTreeView view = null;
    if ( source instanceof Taxonomy ) {
      if (withOntoList) {
        JTree tree = (JTree)OvsT.get((Taxonomy)source);
        if ( null!= tree ) {
          OvsT.remove(source);
          boolean includeInstances = source instanceof Ontology;
          ClassNode root = ClassNode.createRootNode((Taxonomy)source,
              includeInstances);
          OntoTreeModel model = new OntoTreeModel(root);
          view = new EditableTreeView(model);

          KnowledgeBaseTreeCellRenderer kbTreeCellRenderer =
                                    new KnowledgeBaseTreeCellRenderer();
          view.setCellRenderer(kbTreeCellRenderer);

          /* synchronize the expansion of the old and new trees */
          EditableTreeView.synchronizeTreeExpansion(tree,view);

          OvsT.put((Taxonomy)source,view);
          if (ontology.equals((Taxonomy)source)) {
            view.setMainPanel(panel);
            panel.setOntoTree(view);
          }
        }
      } else {
        if (ontology != null && ontology.equals((Taxonomy)source)) {
          boolean includeInstances = source instanceof Ontology;
          ClassNode root = ClassNode.createRootNode((Taxonomy)source,
              includeInstances);
          OntoTreeModel model = new OntoTreeModel(root);
          view = new EditableTreeView(model);
          KnowledgeBaseTreeCellRenderer kbTreeCellRenderer =
                                    new KnowledgeBaseTreeCellRenderer();
          view.setCellRenderer(kbTreeCellRenderer);

          /* synchronize the expansion of the old and new trees */
          if (panel.oTree != null )
            EditableTreeView.synchronizeTreeExpansion(panel.oTree,view);

          OvsT.put((Taxonomy)source,view);
          view.setMainPanel(panel);
          panel.setOntoTree(view);
        }
      } //without ontoList
    }
  }

/*-<-<-<---implementation of ObjectModificationListener interface--------------*/

  protected class KnowledgeBaseTreeCellRenderer extends DefaultTreeCellRenderer {
    public KnowledgeBaseTreeCellRenderer() {
    }
    public Component getTreeCellRendererComponent(JTree tree,
                                              Object value,
                                              boolean sel,
                                              boolean expanded,
                                              boolean leaf,
                                              int row,
                                              boolean hasFocus){
      super.getTreeCellRendererComponent(tree, value, sel, expanded,
                                         leaf, row, hasFocus);
      if (! (value instanceof ClassNode))
        return this;
      ClassNode theNode = (ClassNode) value;
      if(theNode.getSource() instanceof TClass) {
        setIcon(MainFrame.getIcon("Class.gif"));
      } else if(theNode.getSource() instanceof OInstance) {
        setIcon(MainFrame.getIcon("Instance.gif"));
      }
      return this;
    }
  }


} // class OntologyEditorImpl