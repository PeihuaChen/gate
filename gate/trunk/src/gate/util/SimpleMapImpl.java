/*
 *  SimpleMapImpl.java
 *
 *  Copyright (c) 2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  D.Ognyanoff, 5/Nov/2001
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import java.io.*;

/**
 * Implements Map interface in using less memory. Very simple but usefull
 * for small number of items on it.
 */

class SimpleMapImpl implements Map, java.lang.Cloneable, java.io.Serializable {

  /**
   * The capacity of the map
   */
  int m_capacity=3;

  /**
   * The current number of elements of the map
   */
  int m_size=0;

  /**
   * Array keeping the keys of the entries in the map. It is "synchrnized"
   * with the m_values array - the Nth position in both arrays correspond
   * to one and the same entry
   */
  Object m_keys[];

  /**
   * Array keeping the values of the entries in the map. It is "synchrnized"
   * with the m_keys array - the Nth position in both arrays correspond
   * to one and the same entry
   */
  Object m_values[];

  /**
   * Constructor
   */
  public SimpleMapImpl() {
    m_keys = new Object[m_capacity];
    m_values = new Object[m_capacity];
  } // SimpleMapImpl()

  /**
   * return the number of elements in the map
   */
  public int size() {
    return m_size;
  } // size()

  /**
   * return true if there are no elements in the map
   */
  public boolean isEmpty() {
    return (m_size == 0);
  } // isEmpty()

  /**
   * Not supported. This method is here only to conform the Map interface
   */
  public Collection values() {
    throw new UnsupportedOperationException(
      "SimpleMapImpl.values() not implemented!");
  } // values()

  /**
   * return the set of the keys in the map. The changes in the set DO NOT
   * affect the map.
   */
  public Set keySet()
  {
    HashSet s = new HashSet(size());
    Object k;
    for (int i=0; i<m_size; i++) {
      k = m_keys[i];
      if (k == nullKey)
           s.add(null);
        else
           s.add(k);
    } //for
    return s;
  } // keySet()

  /**
   * clear the map
   */
  public void clear()
  {
    for (int i=0; i<m_size; i++) {
      m_keys[i] = null;
      m_values[i] = null;
    } // for
    m_size =0;
  } // clear

  /**
   * return true if the key is in the map
   */
  public boolean containsKey(Object key) {
    return (getPostionByKey(key) != -1);
  }// containsKey

  /**
   * return true if the map contains that value
   */
  public boolean containsValue(Object value) {
    return (getPostionByValue(value) != -1);
  }// containsValue

  /**
   * return the value associated with the key. If the key is
   * not in the map returns null.
   */
  public Object get(Object key) {
    int pos = getPostionByKey(key);
    return (pos == -1) ? null : m_values[pos];
  } // get

  /**
   * put a value in the map using the given key. If the key exist in the map
   * the value is replaced and the old one is returned.
   */
  public Object put(Object key, Object value) {
    Object gKey;
    if (key == null) {
      key = nullKey;
      gKey = nullKey;
    } else
      gKey = theKeysHere.get(key);
    // if the key is already in the 'all keys' map - try to find it in that instance
    // comparing by reference
    if (gKey != null) {
      for (int i=0; i<m_size; i++) {
        if (gKey == m_keys[i]) {
          // we found the reference - return the value
          Object oldVal = m_values[i];
          m_values[i] = value;
          return oldVal;
        }
      } // for
    } else {// if(gKey != null)
      // no, the key is not in the 'all keys' map - put it there
      theKeysHere.put(key, key);
      gKey = key;
    }
    // enlarge the containers if necessary
    if (m_size == m_capacity)
      increaseCapacity();

    // put the key and value to the map
    m_keys[m_size] = gKey;
    m_values[m_size] = value;
    m_size++;
    return null;
  } // put

  /**
   * remove value from the map using it's key.
   */
  public Object remove(Object key) {
    int pos = getPostionByKey(key);
    if (pos == -1)
        return null;

    // save the value to return it at the end
    Object oldVal = m_values[pos];
    m_size--;
    // move the last element key and value removing the element
    if (m_size != 0) {
        m_keys[pos] = m_keys[m_size];
        m_values[pos] = m_values[m_size];
    }
    // clear the last position
    m_keys[m_size] = null;
    m_values[m_size] = null;

    // return the value
    return oldVal;
  } // remove

  /**
   * put all the elements from a map
   */
  public void putAll(Map t)
  {
    if (t == null) {
      throw new UnsupportedOperationException(
      "SimpleMapImpl.putAll argument is null");
    } // if (t == null)

    if (t instanceof SimpleMapImpl) {
      SimpleMapImpl sfm = (SimpleMapImpl)t;
      Object key;
      for (int i=0; i<sfm.m_size; i++) {
        key = sfm.m_keys[i];
        put(key, sfm.m_values[i]);
      } //for
    } else { // if (t instanceof SimpleMapImpl)
      Iterator entries = t.entrySet().iterator();
      Map.Entry e;
      while (entries.hasNext()) {
        e = (Map.Entry)entries.next();
        put(e.getKey(), e.getValue());
      } // while
    } // if(t instanceof SimpleMapImpl)
  } // putAll

transient Object g_akey = null;
  /**
   * return positive value as index of the key in the map.
   * Negative value means that the key is not present in the map
   */
  private int getPostionByKey(Object key) {
    if (key == null)
      key = nullKey;
    // check the 'all keys' map for the very first key occurence
    key = theKeysHere.get(key);
    if (key == null)
      return -1;

    for (int i=0; i<m_size; i++) {
      if (key == m_keys[i])
        return i;
    } // for
    return -1;
  } // getPostionByKey

  /**
   * return the index of the key in the map comparing them by reference only.
   * This method is used in subsume check to speed it up.
   */
  protected int getSubsumeKey(Object key) {
    for (int i=0; i<m_size; i++) {
      if (key == m_keys[i])
        return i;
    } // for
    return -1;
  } // getPostionByKey

  /**
   * return positive value as index of the value in the map.
   */
  private int getPostionByValue(Object value) {
    Object av;
    for (int i=0; i<m_size; i++) {
      av = m_values[i];
      if (value == null) {
        if (av == null)
          return i;
      } else {//if (value == null)
        if (value.equals(av))
          return i;
      } //if (value == null)
    } // for

    return -1;
  } // getPostionByValue

  // Modification Operations
  private void increaseCapacity() {
    int m_oldCapacity = m_capacity;
    m_capacity *= 2;
    Object m_oldKeys[] = m_keys;
    m_keys = new Object[m_capacity];

    Object m_oldValues[] = m_values;
    m_values = new Object[m_capacity];

    System.arraycopy(m_oldKeys, 0, m_keys, 0, m_oldCapacity);
    System.arraycopy(m_oldValues, 0, m_values, 0, m_oldCapacity);
  } // increaseCapacity

  /**
   * Auxiliary classes needed for the support of entrySet() method
   */
  private static class Entry implements Map.Entry {
    int hash;
    Object key;
    Object value;

    Entry(int hash, Object key, Object value) {
      this.hash = hash;
      this.key = key;
      this.value = value;
    }

    protected Object clone() {
      return new Entry(hash, key, value);
    }

    public Object getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }

    public Object setValue(Object value) {
      Object oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Map.Entry))
            return false;
        Map.Entry e = (Map.Entry)o;

        return (key==null ? e.getKey()==null : key.equals(e.getKey())) &&
            (value==null ? e.getValue()==null : value.equals(e.getValue()));
    }

    public int hashCode() {
        return hash ^ (key==null ? 0 : key.hashCode());
    }

    public String toString() {
        return key+"="+value;
    }
  } // Entry


  public Set entrySet() {
    HashSet s = new HashSet(size());
    Object v, k;
    for (int i=0; i<m_size; i++) {
      k = m_keys[i];
      s.add(new Entry(k.hashCode(), ((k==nullKey)?null:k), m_values[i]));
    } //for
    return s;
  } // entrySet

  // Comparison and hashing
  public boolean equals(Object o) {
    if (!(o instanceof Map)) {
      return false;
    }

    Map m = (Map)o;
    if (m.size() != m_size) {
      return false;
    }

    Object v, k;
    for (int i=0; i<m_size; i++) {
      k = m_keys[i];
      v = m.get(k);
      if (v==null) {
        if (m_values[i]!=null)
          return false;
      }
      else if (!v.equals(m_values[i])){
        return false;
      }
    } // for

    return true;
  } // equals

  /**
   * return the hashCode for the map
   */
  public int hashCode() {
    int h = 0;
    Iterator i = entrySet().iterator();
    while (i.hasNext())
      h += i.next().hashCode();
    return h;
  } // hashCode

  /**
   * Create a copy of the map including the data.
   */
  public Object clone() {
    SimpleMapImpl newMap;
    try {
      newMap = (SimpleMapImpl)super.clone();
    } catch (CloneNotSupportedException e) {
      throw(new InternalError(e.toString()));
    }

    newMap.m_size = m_size;
    newMap.m_keys = new Object[m_capacity];
    System.arraycopy(m_keys, 0, newMap.m_keys, 0, m_capacity);

    newMap.m_values = new Object[m_capacity];
    System.arraycopy(m_values, 0, newMap.m_values, 0, m_capacity);

    return newMap;
  } // clone

  public String toString() {
    int max = size() - 1;
    StringBuffer buf = new StringBuffer();
    Iterator i = entrySet().iterator();

    buf.append("{");
    for (int j = 0; j <= max; j++) {
      Entry e = (Entry) (i.next());
      buf.append(e.getKey() + "=" + e.getValue());
      if (j < max)
        buf.append(", ");
    }
    buf.append("}");
    return buf.toString();
  } // toString

  /**
   * readObject - calls the default readObject() and then initialises the
   * transient data
   *
   * @serialData Read serializable fields. No optional data read.
   */
  private void readObject(ObjectInputStream s)
      throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    if (theKeysHere == null) {
      theKeysHere = new HashMap();
      theKeysHere.put(nullKey, nullKey);
    }
    for (int i=0; i<m_keys.length; i++) {
      // if the key is in the 'all keys' map
      Object o = theKeysHere.get(m_keys[i]);
      if (o != null) // yes - so reuse the reference
        m_keys[i] = o;
      else // no - put it in the 'all keys' map
        theKeysHere.put(m_keys[i], m_keys[i]);
    }//for

  }//readObject


 /** Freeze the serialization UID. */
  static final long serialVersionUID = -6747241616127229116L;

 /** the Object instance that will represent the NULL keys in the map */
  transient static Object nullKey = new Object();
  transient public static HashMap theKeysHere = new HashMap();
  static {
    theKeysHere.put(nullKey, nullKey);
  } // static code

  /** the static keys colleaction */
} //SimpleMapImpl
