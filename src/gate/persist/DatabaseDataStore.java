/*
 *  PersistenceException.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 18/Sep/2001
 *
 */


package gate.persist;

import gate.*;

public interface DatabaseDataStore extends DataStore {

  /** --- */
  public void beginTrans()
    throws PersistenceException,UnsupportedOperationException;


  /** --- */
  public void commitTrans()
    throws PersistenceException,UnsupportedOperationException;

  /** --- */
  public void rollbackTrans()
    throws PersistenceException,UnsupportedOperationException;

  /** --- */
  public Long timestamp()
    throws PersistenceException;

  /** --- */
  public void deleteSince(Long timestamp)
    throws PersistenceException;

}