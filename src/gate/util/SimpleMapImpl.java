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

  public SimpleMapImpl() {
    m_keys = new Object[m_capacity];
    m_values = new Object[m_capacity];
  }

  public int size() {
    return m_size;
  }

  public boolean isEmpty() {
    return (m_size == 0);
  }

  public Collection values()
  {
    throw new UnsupportedOperationException(
      "SimpleMapImpl.values() not implemented!");
  }

  public Set keySet()
  {
    HashSet s = new HashSet(size());
    Object k;
    for (int i=0; i<m_capacity; i++) {
      k = m_keys[i];
      if (k !=null) {
        s.add(k);
      }
    } //for
    return s;
  } // keySet

  public void clear()
  {
    for (int i=0; i<m_capacity; i++) {
      m_keys[i] = m_values[i] = null;
    } // for
    m_size =0;
  } // clear

  public  boolean containsKey(Object key) {
    return (getPostionByKey(key) != -1);
  }// containsKey

  public boolean containsValue(Object value) {
    return (getPostionByValue(value) != -1);
  }

  public Object get(Object key) {
    int pos = getPostionByKey(key);
    return (pos == -1) ? null : m_values[pos];
  } // get

  public Object put(Object key, Object value)
  {
    int pos = getPostionByKey(key);
    if (pos >= 0) {
      Object oldVal = m_values[pos];
      m_values[pos] = value;
      return oldVal;
    }

    if (m_size == m_capacity) {
      increaseCapacity();
    }

    Object ak;
    int i;
    for (i=0; i<m_capacity; i++) {
      if (m_keys[i] != null)
          continue;

      m_keys[i] = key;
      m_values[i] = value;
      m_size++;
      break;
    } // for

    if (i==m_capacity) {
      // more specific and correct exception should ne thrwon!!! Naso
      throw new IllegalArgumentException(
      "System error! Map implementation does not work correctly!");
    }
    return null;
  } // put

  public Object remove(Object key)
  {
    int pos = getPostionByKey(key);
    if (pos == -1)
        return null;

    Object oldVal = m_values[pos];
    m_keys[pos] = null;
    m_values[pos] = null;
    m_size--;

    return oldVal;
  } // remove

  public void putAll(Map t)
  {
    if (t == null) {
      throw new UnsupportedOperationException(
      "SimpleMapImpl.putAll argument is null");
    }

    if (t instanceof SimpleMapImpl) {
      SimpleMapImpl sfm = (SimpleMapImpl)t;
      Object key;
      for (int i=0; i<sfm.m_capacity; i++) {
          key = sfm.m_keys[i];
          if (key == null)
              continue;
          put(key, sfm.m_values[i]);
      } //for
    } else {
      Iterator entries = t.entrySet().iterator();
      Map.Entry e;
      while (entries.hasNext()) {
        e = (Map.Entry)entries.next();
        if (e.getKey() == null) continue;
        put(e.getKey(), e.getValue());
      } // while
    } // if
  } // putAll


  private int getPostionByKey(Object key) {
    Object ak;
    if (key == null) {
      return -1;
    }

    for (int i=0; i<m_capacity; i++) {
      ak = m_keys[i];
      if (ak == null)
          continue;

      if (key.equals(ak))
          return i;
    } // for
    return -1;
  } // getPostionByKey

  private int getPostionByValue(Object value) {
    Object av;
    for (int i=0; i<m_capacity; i++) {
      if (m_keys == null)
        continue;
      av = m_values[i];
      if (value == null) {
        if (av == null)
          return i;
      }
      else
        if (value.equals(av))
            return i;
    } // for

    return -1;
  } // getPostionByValue

  // Modification Operations
  private void increaseCapacity()
  {
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
    for (int i=0; i<m_capacity; i++) {
      k = m_keys[i];
      if (k == null)
        continue;
      s.add(new Entry(k.hashCode(), k, m_values[i]));
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
    for (int i=0; i<m_capacity; i++) {
      k = m_keys[i];
      if (k == null)
        continue;
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

  /*
  public int hashCode() {
    int hash=0;
    Object key;
    for (int i=0; i<m_capacity; i++) {
      key = m_keys[i];
      if (key == null)
      continue;
      HashMap hm;
      hash += m_keys[i].hashCode();
      hash += m_values[i].hashCode();
    } // for

    return hash;
  } // hashCode
*/

  public int hashCode() {
    int h = 0;
    Iterator i = entrySet().iterator();
    while (i.hasNext())
      h += i.next().hashCode();
    return h;
  }


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
  }

 /** Freeze the serialization UID. */
  static final long serialVersionUID = -6747241616127229116L;
} //SimpleMapImpl
