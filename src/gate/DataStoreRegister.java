/*
 *  DataStoreRegister.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 23/Jan/2001
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.net.*;

import gate.util.*;
import gate.persist.*;
import gate.event.*;

/** Records all the open DataStores.
  */
public class DataStoreRegister extends HashSet {

  /** All the DataStore classes available. This is a map of class name
   *  to descriptive text.
   */
  public static Map getDataStoreClassNames() {
    Map names = new HashMap();

// no plugability here at present.... at some future point there should
// be a capability to add new data store classes via creole.xml metadata
// and resource jars
    names.put(
      "gate.persist.SerialDataStore",
      "SerialDataStore: file-based storage using Java serialisation"
    );

    return names;
  } // getDataStoreClassNames()

  /**
   * Adds the specified element to this set if it is not already
   * present. Overriden here for event registration code.
   */
  public boolean add(Object o) {
    return super.add(o);
  } // add

  /**
   * Removes the given element from this set if it is present.
   * Overriden here for event registration code.
   */
  public boolean remove(Object o) {
    boolean res = super.remove(o);
    if(res) fireDatastoreClosed(new CreoleEvent((DataStore)o,
                                        CreoleEvent.DATASTORE_CLOSED)
                        );
    return res;
  } // remove

  /**
   * Removes all of the elements from this set.
   * Overriden here for event registration code.
   */
  public void clear() {
    Set datastores = new HashSet(this);
    super.clear();

    Iterator iter = datastores.iterator();
    while(iter.hasNext()){
      fireDatastoreClosed(new CreoleEvent((DataStore) iter.next(),
                                          CreoleEvent.DATASTORE_CLOSED)
                          );
    }// while
  }// clear()

  public synchronized void removeCreoleListener(CreoleListener l) {
    if (creoleListeners != null && creoleListeners.contains(l)) {
      Vector v = (Vector) creoleListeners.clone();
      v.removeElement(l);
      creoleListeners = v;
    }
  }// removeCreoleListener(CreoleListener l)

  public synchronized void addCreoleListener(CreoleListener l) {
    Vector v =
      creoleListeners == null ? new Vector(2) : (Vector) creoleListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      creoleListeners = v;
    }// if
  }// addCreoleListener(CreoleListener l)

  protected void fireResourceLoaded(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).resourceLoaded(e);
      }// for
    }// if
  }// fireResourceLoaded(CreoleEvent e)

  protected void fireResourceUnloaded(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).resourceUnloaded(e);
      }// for
    }// if
  }// fireResourceUnloaded(CreoleEvent e)

  protected void fireDatastoreOpened(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).datastoreOpened(e);
      }// for
    }// if
  }// fireDatastoreOpened(CreoleEvent e)

  protected void fireDatastoreCreated(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).datastoreCreated(e);
      }// for
    }// if
  }// fireDatastoreCreated(CreoleEvent e)

  protected void fireDatastoreClosed(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).datastoreClosed(e);
      }// for
    }// if
  }// fireDatastoreClosed(CreoleEvent e)

  private transient Vector creoleListeners;

} // class DataStoreRegister
