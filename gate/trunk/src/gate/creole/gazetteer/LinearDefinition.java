/*
 * LinearDefinition.java
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
import gate.creole.*;


/** represents a lists.def file <br>
 *  The normal usage of the class will be
 *  * construct it
 *  * setURL
 *  * load
 *  * change
 *  * store
 */
public class LinearDefinition extends gate.creole.AbstractLanguageResource
                              implements Set {

  private final static String ENCODING = "UTF-8";

  private Set nodes = new HashSet();
  private URL url;

  /** set of lists as strings*/
  private Set lists = new HashSet();

  /** a mapping between a list and a node */
  private Map nodesByList = new HashMap();

  /** a map of gazetteer lists by nodes. this is loaded on loadLists*/
  private Map gazListsByNode;

  public LinearDefinition() {
  }

  /**
   * loads the gazetteer lists and maps them to the nodes
   * @return a map of nodes vs GazetteerLists
   * @throws ResourceInstantiationException
   */
  public Map loadLists() throws ResourceInstantiationException {
    try {
      if ( null == gazListsByNode ) {
        gazListsByNode = new HashMap();
        Iterator inodes = nodes.iterator();
        while (inodes.hasNext()) {
          LinearNode node = (LinearNode)inodes.next();

          GazetteerList list = new GazetteerList();
          String path = url.getPath();
          int slash = path.lastIndexOf("/");
          if (-1 == slash ) {
            slash = 0;
          } else {
            path = path.substring(0,slash+1);
          }
          URL lurl = new URL(url,node.getList());
          list.setURL(lurl);
          list.load();

          gazListsByNode.put(node,list);
        } // while inodes
      } // if null
    } catch (Exception ex) {
      throw new ResourceInstantiationException(ex);
    }
    return gazListsByNode;
  }  // loadLists()



  /**get the url of this linear definition
   * @return the url of this linear definition   */
  public URL getURL() {
    return url;
  }


  /**set the url of this linear definition
   * @param aUrl the url of this linear definition   */
  public void setURL(URL aUrl) {
    url = aUrl;
  }

  /**
   * load linear definition if url is set
   */
  public void load() throws ResourceInstantiationException {
    if (null == url) {
      throw new ResourceInstantiationException("URL not set (null).");
    }
    try {
      BufferedReader defReader =
      new BufferedReader(new InputStreamReader((url).openStream(), ENCODING));

      String line;
      LinearNode node;
      while (null != (line = defReader.readLine())) {
        node = new LinearNode(line);
        this.add(node);
      } //while

      defReader.close();
    } catch (Exception x){
      throw new ResourceInstantiationException(x);
    }
  } // load();

  /**
   * sotres this to a definition file
   */
  public void store() throws ResourceInstantiationException{
    if (null == url) {
      throw new ResourceInstantiationException("URL not set.(null)");
    }
    try {
      File fileo = new File(url.getFile());
      fileo.delete();
      BufferedWriter defWriter = new BufferedWriter(new FileWriter(fileo));
      Iterator inodes = nodes.iterator();
      while (inodes.hasNext()) {
        defWriter.write(inodes.next().toString());
        defWriter.newLine();
      }
      defWriter.close();
    } catch(Exception x) {
      throw new ResourceInstantiationException(x);
    }

  } // store();

  /**
   * note that a new set is created so the adding and removing of lists will
   * not affect the internal members. Also there is no setLists method since the leading
   * member of the class is nodes, and lists cannot be added individually without being
   * associated with a node.
   * @return a set of the lists in this
   */
  public Set getLists() {
    return new HashSet(lists);
  }

  /*---implementation of interface java.util.Set---*/

  public int size() {
    return nodes.size();
  }

  public boolean isEmpty() {
    return 0 == nodes.size();
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
    if (o instanceof LinearNode) {
      String list = ((LinearNode)o).getList();
      if (!nodesByList.containsKey(list)) {
        result = nodes.add(o);
        nodesByList.put(list,o);
        lists.add(list);
      } // if unique
    } // if a linear node
    return result;
  } // add()

  public boolean remove(Object o) {
    boolean result = false;
    if (o instanceof LinearNode) {
      result = nodes.remove(o);
      String list = ((LinearNode)o).getList();
      lists.remove(list);
      nodesByList.remove(list);
    } // if linear node
    return result;
  }// remove

  public boolean containsAll(Collection c) {
    return nodes.containsAll(c);
  }

  public boolean addAll(Collection c) {
    boolean result = false;
    Iterator iter = c.iterator();
    Object o;
    while (iter.hasNext()) {
      o = iter.next();
      if (o instanceof LinearNode)  {
        result |= add(o);
      } // instance of linearnode
    } // while
    return result;
  } // addAll()


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

    LinearNode node;
    Iterator inodes = nodes.iterator();
    while(inodes.hasNext()) {
      node = (LinearNode) inodes.next();
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
    if ( o instanceof LinearDefinition ) {
      LinearDefinition def = (LinearDefinition) o;
      result &= nodes.equals(def.nodes);
      result &= lists.equals(def.lists);
      result &= nodesByList.equals(def.lists);
    }// if
    return result;
  } // equals()

 /*---end of implementation of interface java.util.Set---*/

 /*-----------internal classes -------------*/
 /**this class provides an iterator which is safe to be iterated and objects removed from it*/

  private class SafeIterator implements Iterator {
    private Iterator iter = LinearDefinition.this.nodes.iterator();
    private boolean removeCalled = false;
    private Object last = null;

    public boolean hasNext() {
      return iter.hasNext();
    }

    public Object next() {
      removeCalled = false;
      last = iter.next();
      return last;
    }

    public void remove() {
      if (!removeCalled && null!=last ) {
        LinearDefinition.this.remove(last);
      }// if possible remove
      removeCalled = true;
    } // remove

  } // class SafeIterator

} // class LinearDefinition