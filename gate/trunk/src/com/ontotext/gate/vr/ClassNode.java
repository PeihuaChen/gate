package com.ontotext.gate.vr;

import java.util.*;


import com.ontotext.gate.vr.*;
import gate.creole.ontology.*;

import gate.util.*;
import gate.creole.gazetteer.*;

import java.awt.datatransfer.*;
import java.io.*;
import java.net.URL;

import javax.swing.tree.*;


/** Represents a single class node from the visualized ontology */
public class ClassNode
    implements IFolder,Transferable,Cloneable, Serializable
{

  /** flavor used for drag and drop */
  final public static DataFlavor CLASS_NODE_FLAVOR =
    new DataFlavor(ClassNode.class, "Class Node");

  static DataFlavor flavors[] = {CLASS_NODE_FLAVOR};

  private String name;
  private Vector children = new Vector();
  private Object source;

  /** create a structure representing the class hierarchy of an ontology
   *  @param includeInstances if true, then instances of the ontology
   *  are also included
   *  @return the root node of the structure
   */
  public static ClassNode createRootNode(Taxonomy o) {
    return createRootNode(o, false);
  }

  public static ClassNode createRootNode(Taxonomy o, boolean includeInstances) {
    if (null == o)
      throw new gate.util.LazyProgrammerException("ontology is null.");

    try {
      ClassNode root = new ClassNode(o);
      Iterator itops = o.getTopClasses().iterator();
      Vector kids = new Vector();
      while (itops.hasNext()) {
        ClassNode node = new ClassNode((TClass)itops.next());
        kids.add(node);
      } // while

      root.source = o;
      root.setChildren(kids);
      Vector parents = kids;
      Vector allKids;
      do {
        allKids = new Vector();
        for ( int i= 0 ; i < parents.size() ; i++ ) {
          ClassNode parent = (ClassNode)parents.get(i);
          kids = new Vector();

          //skip this one if it's an instance
          if(parent.getSource() instanceof OInstance)
            continue;

          TClass ocl = (TClass) parent.getSource();

          //if we include instances, then get them too
          if (includeInstances && (o instanceof Ontology)) {
            Ontology kb = (Ontology) o;
            List instances = kb.getDirectInstances((OClass)ocl);
            if (instances != null && !instances.isEmpty()) {
              Iterator insti = instances.iterator();
              while (insti.hasNext())
                kids.add(new ClassNode( (OInstance) insti.next()));
            }
          }

          if (0 == ocl.getSubClasses(TClass.DIRECT_CLOSURE).size()) {
            if (! kids.isEmpty())
              //add the instances as children, but do not add them for future
              //traversal to allKids
              parent.setChildren(kids);
            continue;
          }  // if 0 children

          Iterator kidsi = ocl.getSubClasses(TClass.DIRECT_CLOSURE).iterator();

          while ( kidsi.hasNext()) {
            kids.add(new ClassNode((TClass)kidsi.next()));
          } // while kidsi
          parent.setChildren(kids);
          allKids.addAll(kids);

        }   // for i
        parents = allKids;
      } while (0 < allKids.size());

      return root;
    } catch (NoSuchClosureTypeException x) {
      throw new GateRuntimeException(x.toString());
    }
  }//createRootNode()

  /** Creates a structure representing the class hierarchy of an ontology
   *  and the gazetteerLists mapped to it.
   *  @param o an ontology
   *  @param mapping mapping definition
   *  @param nameVsNode : this is actually a return value: should be
   *  initialized before passing to this method and afterwards one can find a mapping
   *  of class names vs class nodes there.
   *  @return the root node of the structure
   */
  public static ClassNode createRootNode(Taxonomy o, MappingDefinition mapping, Map nameVsNode) {
    if (null == o || null == nameVsNode || null == mapping)
      throw new gate.util.LazyProgrammerException("mapping, nameVsNode or ontology-o is null.");
    try {
      ClassNode root = new ClassNode(o);
      Iterator itops = o.getTopClasses().iterator();
      Vector kids = new Vector();
      while (itops.hasNext()) {
        ClassNode node = new ClassNode((TClass)itops.next());
        nameVsNode.put(node.toString(),node);
        kids.add(node);
      } // while

      root.source = o;
      root.setChildren(kids);
      Vector parents = kids;
      Vector allKids;
      do {
        allKids = new Vector();
        for ( int i= 0 ; i < parents.size() ; i++ ) {
          ClassNode parent = (ClassNode)parents.get(i);

          TClass ocl = (TClass) parent.getSource();
          if (0 == ocl.getSubClasses(TClass.DIRECT_CLOSURE).size()) {
            continue;
          }  // if 0 children

          Iterator kidsi = ocl.getSubClasses(TClass.DIRECT_CLOSURE).iterator();

          kids = new Vector();
          while ( kidsi.hasNext()) {
            ClassNode cn = new ClassNode((TClass)kidsi.next());
            kids.add(cn);
            nameVsNode.put(cn.toString(),cn);
          } // while kidsi
          parent.setChildren(kids);
          allKids.addAll(kids);

        }   // for i
        parents = allKids;
      } while (0 < allKids.size());

      // display mapping
      Iterator inodes = mapping.iterator();
      MappingNode mn;
      while (inodes.hasNext()) {
        mn = (MappingNode)inodes.next();
        URL turl = null;
        try { turl = new URL(mn.getOntologyID());
        } catch (java.net.MalformedURLException x) {
        }
        if ( null != turl ){
          Taxonomy o2 = null;
          try { o2 = o.getOntology(turl);
          } catch (gate.creole.ResourceInstantiationException x) {
          }
          if ( o2 != null && o2.equals(o) ) {
            ClassNode cmn = new ClassNode(mn);
            ClassNode cn = (ClassNode)nameVsNode.get(mn.getClassID());
            if (null!= cn) {
              cn.children.add(cn.children.size(),cmn);
            }
          }// if from the same ontology
        } // turl != null
      }// while inodes


      return root;
    } catch (NoSuchClosureTypeException x) {
      throw new GateRuntimeException(x.toString());
    }
  }//createRootNode()

  /**Constructs a root class node from an ontology
   * @param o the ontology    */
  public ClassNode(Taxonomy o) {
    name = o.getName();
  }

  /**Constructs a class node given an ontology class
   * @param clas ontology class   */
  public ClassNode(TClass clas) {
    name = clas.getName();
    source = clas;
  }

  /**Constructs a class node given an ontology instance
   * @param instance ontology instance   */
  public ClassNode(OInstance instance) {
    name = instance.getName();
    source = instance;
  }

  /**
   * Constructs a class node given a mapping node
   * @param mapNode mapping node    */
  public ClassNode(MappingNode mapNode) {
    name = mapNode.getList();
    source = mapNode;
  }

  public int getIndexOfChild(Object child) {
    return children.indexOf(child);
  }

  public Iterator getChildren() {
    return children.iterator();
  }

  public void setChildren(Vector chldrn ) {
    children = chldrn;
  }

  public Vector children() {
    return children;
  }

  public String toString() {
    return name;
  }

  public int getChildCount() {
    return children.size();
  }

  public IFolder getChild(int index) {
    return (IFolder)children.get(index);
  }

  public boolean equals(Object o) {
    boolean result = false;
    if (o instanceof ClassNode) {
      ClassNode node = (ClassNode) o;
      result = node.source.equals(this.source);
    }
    return result;
  }

  /**Gets the Source object
   * @return the source object e.g. an gate.creole.TClass
   * or a gate.creole.Ontology   */
  public Object getSource(){
    return source;
  }

  /**Sets the source object
   * @param o the source object to be set   */
  public void setSource(Object o)  {
    source = o;
  }

  /**Renames this class node
   * @param newName the new name of the node   */
  public void rename(String newName) {
    name = newName;
  }

  /**Removes a sub class
   * @param sub the sub class to be removed*/
  public void removeSubNode(ClassNode sub) {
    if ( children.contains(sub) ) {
      children.remove(sub);
      Object source = this.getSource();
      if (source instanceof TClass) {
        TClass c = (TClass) source;
        if (sub.getSource() instanceof TClass)
          c.removeSubClass((TClass)sub.getSource());
        else if (sub.getSource() instanceof OInstance &&
                 c.getOntology() instanceof Ontology)
          ((Ontology)c.getOntology()).removeInstance((OInstance) sub.getSource());
      } else if ( source instanceof Taxonomy ) {
          Taxonomy o = (Taxonomy) source;
          o.removeClass((TClass)sub.getSource());
        } else if (source instanceof OInstance) {
          //cannot remove anything from an instance
          return;
        } else {
          throw new GateRuntimeException(
          "Can not remove a sub node from a classnode.\n"
          +"The source is neither an Ontology neither TClass");
        } // else
    } // if contains
  } // removeSubNode

  /**Adds a sub node
   * @param sub the sub node to be added    */
  public void addSubNode(ClassNode sub) {
    if ( ! children.contains(sub) )  {
      Object source = this.getSource();
      if ( source instanceof TClass) {
        TClass c = (TClass)source;
        if (!(sub.getSource() instanceof TClass) ||
            !(sub.getSource() instanceof OInstance))
          throw new GateRuntimeException(
          "The sub node's source is not an instance of TClass or OInstance");
        if (sub.getSource() instanceof TClass) {
          TClass sc = (TClass) sub.getSource();
          c.addSubClass(sc);
          c.getOntology().addClass(sc);
          children.add(sub);
        }
        if (sub.getSource() instanceof OInstance &&
            c.getOntology() instanceof Ontology){
          OInstance inst = (OInstance) sub.getSource();
          ((Ontology)c.getOntology()).addInstance(inst);
          children.add(sub);
        }

      } else {
        if (source instanceof Taxonomy) {
          Taxonomy o = (Taxonomy) source;
          if (!(sub.getSource() instanceof TClass))
            throw new GateRuntimeException("The sub node's source is not an instance of TClass");
          TClass sc = (TClass)sub.getSource();
          o.addClass(sc);
          children.add(sub);
        } else  {
          throw new GateRuntimeException(
          "cannot add a sub node to something which "
          +"is neither an Ontology neither an TClass");
        } // else
      } // else
    } // if ! contains
  } // addSubNode()

  /*--- Transferable interface implementation ---*/
  public boolean isDataFlavorSupported(DataFlavor df) {
    return df.equals(CLASS_NODE_FLAVOR);
  }

  public Object getTransferData(DataFlavor df)
      throws UnsupportedFlavorException, IOException {
    if (df.equals(CLASS_NODE_FLAVOR)) {
      return this;
    }
    else throw new UnsupportedFlavorException(df);
  }

  public DataFlavor[] getTransferDataFlavors() {
    return flavors;
  }


} // class ClassNode