/*
 *  AbstractLanguageResource.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 24/Oct/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.persist.*;


/** A convenience implementation of LanguageResource with some default code.
  */
abstract public class AbstractLanguageResource
extends AbstractResource implements LanguageResource
{
  /** Get the data store that this LR lives in. Null for transient LRs. */
  public DataStore getDataStore() { return dataStore; }

  /** Set the data store that this LR lives in. */
  public void setDataStore(DataStore dataStore) throws PersistenceException {
    this.dataStore = dataStore;
  } // setDataStore(DS)

  /** The data store this LR lives in. */
  transient protected DataStore dataStore;

  /** Save: synchonise the in-memory image of the LR with the persistent
    * image.
    */
  public void sync() throws PersistenceException {
    if(dataStore == null)
      throw new PersistenceException("LR has no DataStore");

    dataStore.sync(this);
  } // sync()

} // class AbstractLanguageResource
