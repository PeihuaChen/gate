/*
 *  OracleDataStore.java
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

import java.sql.*;
import java.net.*;
import java.util.*;

import gate.*;
import gate.util.*;
import gate.event.*;


public class OracleDataStore extends JDBCDataStore {

  public OracleDataStore() {
  }

  /**
   * Save: synchonise the in-memory image of the LR with the persistent
   * image.
   */
  public void sync(LanguageResource lr) throws PersistenceException {

    try {
      jdbcConn.setAutoCommit(false);

      jdbcConn.commit();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("sync failed: ["+ sqle.getMessage()+"]");
    }

  }

  /** Gets a timestamp marker that will be used for all changes made in
   *  the database so that subsequent calls to deleteSince() could restore (partly)
   *  the database state as it was before the update. <B>NOTE:</B> Restoring the previous
   *  state may not be possible at all (i.e. if DELETE is performed)
   *   */
  public Long timestamp()
    throws PersistenceException{

    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall("{ call gate.get_timestamp(?)} ");
      //numbers generated from Oracle sequences are BIGINT
      stmt.registerOutParameter(1,java.sql.Types.BIGINT);
      stmt.execute();
      long result = stmt.getLong(1);

      return new Long(result);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't get a timestamp from DB: ["+ sqle.getMessage()+"]");
    }

  }

}