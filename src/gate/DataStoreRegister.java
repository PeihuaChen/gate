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
    return super.remove(o);
  } // remove

  /**
   * Removes all of the elements from this set.
   * Overriden here for event registration code.
   */
  public void clear() {
    super.clear();
  } // clear

} // class DataStoreRegister
