/*
 *  LRDBWrapper.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 * 
 *  Valentin Tablan, 01/03/2000
 *
 *  $Id$
 */


package gate;
import java.sql.SQLException;
import gate.util.*;

/** This interface should be implemented by any persistent language resource.
  */
public interface LRDBWrapper extends LanguageResource {

  /** Rolls back all the actions performed since the last beginTransaction call
    */
  public void rollback() throws SQLException;

  /**
    * Starts a transaction. All database actions should be enclosed between a
    * beginTransaction() and a commit()/rollback() call.
    */
  public void beginTransaction() throws SQLException;

  /** Commits the reads/writes */
  public void commit() throws SQLException;

  /** Disconnects from the Datastore object, that is returns the connection */
  public void disconnect() throws GateException;
}
