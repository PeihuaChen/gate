/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 15/Oct/2001
 *
 *  $Id$
 */

package gate.util;


import java.util.*;
import java.lang.ref.*;

/**
 * A map that uses weak references to store its values. It differs from
 * {@link java.util.WeakHashMap} as that one uses weak references for its keys.
 * Objects that are values in this map become candidates for garbage collection
 * when all the other references to them are removed; when this happens they are
 * automatically removed from this map.
 *
 * This map uses a {@link HashMap} as the support map which will holds weak
 * references to the actual values.
 */
public class WeakValueHashMap extends AbstractMap{

  /**
   * Creates a new WeakValueHashMap.
   */
  public WeakValueHashMap(){
    refQueue = new ReferenceQueue();
    supportMap = new HashMap();
  }


  /**
   * A customised {@link java.lang.ref.WeakReference} which holds a value.
   * It also keeps a pointer the the key used to add this value into the map
   * in order to allow the deletion of the entry from the map when the value has
   * been collected.
   */
  static protected class WeakValue extends WeakReference{

    /**
     * Creates a new WeakValue object.
     */
    public WeakValue(Object key, Object value, ReferenceQueue refQueue){
      super(value, refQueue);
      this.key = key;
    }

    /**
     * gets the key used to add this value into the support map.
     */
    public Object getKey(){
      return key;
    }

    /**
     * The key this value is linked to by the HashMap.
     * It need to be stored here in order to be able to remove the entry from
     * the map when the value has been collected.
     */
    protected Object key;
  }

  /**
   * A simple {@link Map.Entry} implementation
   */
  protected static class Entry implements Map.Entry {
    Object key;
    Object value;

    Entry(Object key, Object value) {
      this.key = key;
      this.value = value;
    }


    // Map.Entry Ops

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
        if (!(o instanceof Map.Entry)) return false;
        Map.Entry e = (Map.Entry)o;
        return (key==null ? e.getKey()==null : key.equals(e.getKey())) &&
           (value==null ? e.getValue()==null : value.equals(e.getValue()));
    }

  }

  /* Internal class for entry sets */
  protected class EntrySet extends AbstractSet {
    EntrySet(){
      processQueue();
      supportEntrySet = supportMap.entrySet();
    }

    public Iterator iterator() {
      return new Iterator(){

        public boolean hasNext(){
          return supportEntrySetIter.hasNext();
        }

        public Object next(){
          Map.Entry entry = (Map.Entry)supportEntrySetIter.next();
          WeakValue wv = (WeakValue)entry.getValue();
          Entry res = new Entry(entry.getKey(), wv.get());
          return res;
        }

        public void remove(){
          supportEntrySetIter.remove();
        }
        Iterator supportEntrySetIter = supportEntrySet.iterator();
      };
    }

    public int size(){
      return supportEntrySet.size();
    }

    Set supportEntrySet;
  }


  /**
   * Returns a Set view of the mappings contained in this map.  Each
   * element in the returned collection is a <tt>Map.Entry</tt>.  The
   * collection is backed by the map, so changes to the map are reflected in
   * the collection, and vice-versa.  The collection supports element
   * removal, which removes the corresponding mapping from the map, via the
   * <tt>Iterator.remove</tt>, <tt>Collection.remove</tt>,
   * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt> operations.
   * It does not support the <tt>add</tt> or <tt>addAll</tt> operations.
   *
   * @return a collection view of the mappings contained in this map.
   * @see Map.Entry
   */
    public Set entrySet() {
      if (entrySet == null) entrySet = new EntrySet();
      return entrySet;
    }

  /**
   * Associates the specified value with the specified key in this map.
   * If the map previously contained a mapping for this key, the old
   * value is replaced.
   *
   * @param key key with which the specified value is to be associated.
   * @param value value to be associated with the specified key.
   * @return previous value associated with specified key, or <tt>null</tt>
   *	       if there was no mapping for key.  A <tt>null</tt> return can
   *	       also indicate that the HashMap previously associated
   *	       <tt>null</tt> with the specified key.
   */
  public Object put(Object key, Object value) {
    processQueue();
    WeakValue wv = new WeakValue(key, value, refQueue);
    WeakValue oldWv = (WeakValue)supportMap.put(key, wv);
    return oldWv == null ? null : oldWv.get();
  }


  /**
   * Returns <tt>true</tt> if this map maps one or more keys to this value.
   * More formally, returns <tt>true</tt> if and only if this map contains
   * at least one mapping to a value <tt>v</tt> such that <tt>(value==null ?
   * v==null : value.equals(v))</tt>.  This operation will require
   * time linear in the map size.<p>
   *
   * @param value value whose presence in this map is to be tested.
   *
   * @return <tt>true</tt> if this map maps one or more keys to this value.
   */
  public boolean containsValue(Object value) {
    processQueue();
    Iterator i = entrySet().iterator();
    WeakValue wValue;
    if (value==null) {
      while (i.hasNext()) {
        Entry e = (Entry) i.next();
        wValue = (WeakValue)e.getValue();
        if(wValue.get() == null) return true;
      }
    } else {
      while (i.hasNext()) {
        Entry e = (Entry) i.next();
        wValue = (WeakValue)e.getValue();
        if(value.equals(wValue.get())) return true;
      }
    }
    return false;
  }



  /**
   * Compares the specified object with this map for equality.  Returns
   * <tt>true</tt> if the given object is also a map and the two maps
   * represent the same mappings.  More formally, two maps <tt>t1</tt> and
   * <tt>t2</tt> represent the same mappings if
   * <tt>t1.keySet().equals(t2.keySet())</tt> and for every key <tt>k</tt>
   * in <tt>t1.keySet()</tt>, <tt> (t1.get(k)==null ? t2.get(k)==null :
   * t1.get(k).equals(t2.get(k))) </tt>.  This ensures that the
   * <tt>equals</tt> method works properly across different implementations
   * of the map interface.<p>
   *
   * This implementation first checks if the specified object is this map;
   * if so it returns <tt>true</tt>.  Then, it checks if the specified
   * object is a map whose size is identical to the size of this set; if
   * not, it it returns <tt>false</tt>.  If so, it iterates over this map's
   * <tt>entrySet</tt> collection, and checks that the specified map
   * contains each mapping that this map contains.  If the specified map
   * fails to contain such a mapping, <tt>false</tt> is returned.  If the
   * iteration completes, <tt>true</tt> is returned.
   *
   * @param o object to be compared for equality with this map.
   * @return <tt>true</tt> if the specified object is equal to this map.
   */
  public boolean equals(Object o) {
    processQueue();
    if (o == this) return true;

    if (!(o instanceof Map)) return false;
    Map t = (Map) o;
    if (t.size() != size()) return false;

    Iterator i = entrySet().iterator();
    while (i.hasNext()) {
      Entry e = (Entry) i.next();
      Object key = e.getKey();
      Object value = ((WeakValue)e.getValue()).get();
      if (value == null) {
        if (!(t.get(key)==null && t.containsKey(key))) return false;
      } else {
        if (!value.equals(t.get(key))) return false;
      }
    }
    return true;
  }


  /**
   * Returns the value to which this map maps the specified key.  Returns
   * <tt>null</tt> if the map contains no mapping for this key.  A return
   * value of <tt>null</tt> does not <i>necessarily</i> indicate that the
   * map contains no mapping for the key; it's also possible that the map
   * explicitly maps the key to <tt>null</tt>.  The containsKey operation
   * may be used to distinguish these two cases. <p>
   *
   * @param key key whose associated value is to be returned.
   * @return the value to which this map maps the specified key.
   *
   * @throws ClassCastException if the specified key is of an inappropriate
   * 		  type for this map.
   *
   * @throws NullPointerException if the key is <tt>null</tt> and this map
   *		  does not not permit <tt>null</tt> keys.
   *
   * @see #containsKey(Object)
   */
  public Object get(Object key) {
    processQueue();
    WeakValue wValue = (WeakValue)supportMap.get(key);
    return wValue == null ? null : wValue.get();
  }


  /**
   * Checks the queue for any new weak references that have been cleared and
   * queued and removes them from the underlying map.
   *
   * This method should be called by every public method before realising its
   * internal logic.
   */
  protected void processQueue(){
    WeakValue wv;
    while ((wv = (WeakValue)refQueue.poll()) != null) {
      supportMap.remove(wv.getKey());
    }
  }

  ReferenceQueue refQueue;
  /**
   * A map that maps keys to {@link WeakValue}s.
   */
  protected Map supportMap;

  protected EntrySet entrySet;
}