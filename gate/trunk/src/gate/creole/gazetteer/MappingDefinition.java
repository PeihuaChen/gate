/*
 * MappingDefinition.java
 *
 * Copyright (c) 2002, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * borislav popov 02/2002
 *
 */
package gate.creole.gazetteer;


import java.net.*;
import java.util.*;
import java.io.*;

import gate.creole.ResourceInstantiationException;

/** represents a mapping definition which maps gazetteer lists to ontology classes */
public class MappingDefinition extends gate.creole.AbstractLanguageResource
                              implements List {

  private final static String ENCODING = "UTF-8";

  private List nodes = new ArrayList();
  private URL url;
  /** a set of the lists */
  private Set lists = new HashSet();

  /** a mapping between a list and a node */
  private Map nodesByList = new HashMap();


  /** create new mapping definition */
  public MappingDefinition() {
  }

  /** get the url
   *  @return the url */
  public URL getURL() {
    return url;
  }

  /** set the url
   *  @param aUrl the url */
  public void setURL(URL aUrl) {
    url = aUrl;
  }

  /**load the mapping definition from the url specified
   * @throws ResourceInstantiationException
   */
  public void load() throws ResourceInstantiationException,InvalidFormatException {
    if (null == url) {
      throw new ResourceInstantiationException("URL not set (null).");
    }
    try {
      BufferedReader mapReader =
      new BufferedReader(new InputStreamReader((url).openStream(), ENCODING));

      String line;
      MappingNode node;
      while (null != (line = mapReader.readLine())) {
        node = new MappingNode(line);
        this.add(node);
      } //while

      mapReader.close();

    } catch (InvalidFormatException ife){
      throw new InvalidFormatException(url,"on load");
    } catch (IOException ioe) {
      throw new ResourceInstantiationException(ioe);
    }


  } // load();

  /**
   * store the mapping definition to the specified url
   * @throws ResourceInstantiationException
   */
  public void store()throws ResourceInstantiationException{
    if (null == url) {
      throw new ResourceInstantiationException("URL not set (null).");
    }
    try {
    File fileo = new File(url.getFile());
    fileo.delete();
    BufferedWriter mapWriter = new BufferedWriter(new FileWriter(fileo));
    for (int index = 0 ; index < nodes.size() ; index++) {
      mapWriter.write(nodes.get(index).toString());
      mapWriter.newLine();
    }
    mapWriter.close();
    } catch (IOException ioe) {
      throw new ResourceInstantiationException(ioe);
    }
  } //store();

  /**
   * @return a set of the lists
   */
  public Set getLists() {
    return new HashSet(lists);
  }

  /**
   * get node by list
   * @param list
   * @return the mapping node that matches the list
   */
  public MappingNode getNodeByList(String list) {
    return (MappingNode)nodesByList.get(list);
  }

  /*---implementation of interface java.util.List---*/

  public int size() {
    return nodes.size();
  }

  public boolean isEmpty() {
    return nodes.isEmpty();
  }

  public boolean contains(Object o) {
    return nodes.contains(o);
  }

  public Iterator iterator() {
    return new SafeIterator();
  }

  public Object[] toArray() {
    return nodes.toArray();
  }

  public Object[] toArray(Object[] a) {
    return nodes.toArray(a);
  }

  /**
   * adds a new node, only if its list is new and uniquely mapped to this node.
   * @param o a node
   * @return true if the list of node is not already mapped with another node.
   */
  public boolean add(Object o) {
    boolean result = false;
    if (o instanceof MappingNode) {
      String list = ((MappingNode)o).getList();
      if (!nodesByList.containsKey(list)) {
        result = nodes.add(o);
        nodesByList.put(list,o);
        lists.add(list);
      } // if unique
    } // if a linear node
    return result;
  } // add()

  /**
   * adds a new node at the specified position, only if its list is new and uniquely mapped to this node.
   * @param o a node
   * @param index position in the list
   */
  public void add(int index,Object o) {
    if (o instanceof MappingNode) {
      String list = ((MappingNode)o).getList();
      if (!nodesByList.containsKey(list)) {
        nodes.add(index,o);
        nodesByList.put(list,o);
        lists.add(list);
      } // if unique
    } // if a linear node
  } // add()

  public Object set(int index, Object o) {
    throw new UnsupportedOperationException("this method has not been implemented");
  }

  public Object get(int index){
    return nodes.get(index);
  }

  public boolean remove(Object o) {
    boolean result = false;
    if (o instanceof MappingNode) {
      result = nodes.remove(o);
      String list = ((MappingNode)o).getList();
      lists.remove(list);
      nodesByList.remove(list);
    } // if linear node
    return result;
  }// remove

  public Object remove(int index) {
    Object result = null;
    result = nodes.remove(index);
    if (null!=result) {
      String list = ((MappingNode)result).getList();
      lists.remove(list);
      nodesByList.remove(list);
    }
    return result;
  }

  public boolean containsAll(Collection c) {
    return nodes.containsAll(c);
  }

  public boolean addAll(Collection c) {
    boolean result = false;
    Iterator iter = c.iterator();
    Object o;
    while (iter.hasNext()) {
      o = iter.next();
      if (o instanceof MappingNode)  {
        result |= add(o);
      } // instance of MappingNode
    } // while
    return result;
  } // addAll(Collection)

  public boolean addAll(int index,Collection c) {
    int size = nodes.size();
    Iterator iter = c.iterator();
    Object o;
    while (iter.hasNext()) {
      o = iter.next();
      if (o instanceof MappingNode)  {
        add(index++, o);
      } // instance of MappingNode
    } // while
    return (size!=nodes.size());
  }//addAll(int,Collection)

  public boolean removeAll(Collection c) {
    boolean result = false;
    Iterator iter = c.iterator();
    Object o;
    while (iter.hasNext()) {
      o = iter.next();
      result |= remove(o);
    }
    return result;
  }// removeAll()


  public boolean retainAll(Collection c) {
    int aprioriSize = nodes.size();
    List scrap = new ArrayList();

    MappingNode node;
    for (int index = 0; index < nodes.size(); index++) {
      node = (MappingNode) nodes.get(index);
      if (c.contains(node)) {
        scrap.add(node);
      }
    } //for

    removeAll(scrap);

    return (aprioriSize != nodes.size());
  }


  public void clear() {
    nodes.clear();
    lists.clear();
    nodesByList.clear();
  }

  public boolean equals(Object o) {
    boolean result = false;
    if ( o instanceof MappingDefinition ) {
      MappingDefinition def = (MappingDefinition) o;
      result &= nodes.equals(def.nodes);
      result &= lists.equals(def.lists);
      result &= nodesByList.equals(def.lists);
    }// if
    return result;
  } // equals()

  public List subList(int i1, int i2) {
    return nodes.subList(i1,i2);
  }

  public ListIterator listIterator(int index) {
    throw new UnsupportedOperationException("this method is not implemented");
  }
  public ListIterator listIterator() {
    throw new UnsupportedOperationException("this method is not implemented");
  }

  public int lastIndexOf(Object o) {
    return nodes.lastIndexOf(o);
  }

  public int indexOf(Object o) {
    return nodes.indexOf(o);
  }

 /*---end of implementation of interface java.util.List---*/

 /*-----------internal classes -------------*/

  /**provides means for safe iteration over
   * the entries of the Mapping Definition  */
  private class SafeIterator implements Iterator {
    private int index = 0;
    private boolean removeCalled = false;

    public boolean hasNext() {
      return (index < nodes.size());
    }

    public Object next() {
      removeCalled = false;
      return nodes.get(index++);
    }

    public void remove() {
      if (!removeCalled && index > 0  ) {
        index--;
        MappingDefinition.this.remove(nodes.get(index));
      }// if possible remove
      removeCalled = true;
    } // remove


  } // class SafeIterator

} // class MappingDefinition