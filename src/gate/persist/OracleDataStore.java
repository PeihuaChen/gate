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
 *  $Id$
 */

package gate.persist;

import java.sql.*;
import java.net.*;
import java.util.*;

import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.event.*;
import gate.security.*;


public class OracleDataStore extends JDBCDataStore {

  private static final int ORACLE_TRUE = 1;
  private static final int ORACLE_FALSE = 0;

  private static final int READ_ACCESS = 0;
  private static final int WRITE_ACCESS = 1;

  public OracleDataStore() {
  }

  /**
   * Save: synchonise the in-memory image of the LR with the persistent
   * image.
   */
  public String getComment() {
    throw new MethodNotImplementedException();
  }

  /**
   * Returns the name of the icon to be used when this datastore is displayed
   * in the GUI
   */
  public String getIconName() {
    throw new MethodNotImplementedException();
  }


  /**
   * Removes a a previously registered {@link gate.event.DatastoreListener}
   * from the list listeners for this datastore
   */
  public void removeDatastoreListener(DatastoreListener l) {
    throw new MethodNotImplementedException();
  }


  /**
   * Registers a new {@link gate.event.DatastoreListener} with this datastore
   */
  public void addDatastoreListener(DatastoreListener l) {
    throw new MethodNotImplementedException();
  }

  /** Get the name of an LR from its ID. */
  public String getLrName(Object lrId)
    throws PersistenceException {

    if (false == lrId instanceof Long) {
      throw new IllegalArgumentException();
    }

    Long ID = (Long)lrId;

    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall("{ call persist.get_lr_name(?,?) }");
      stmt.setLong(1,ID.longValue());
      stmt.registerOutParameter(2,java.sql.Types.VARCHAR);
      stmt.execute();
      String result = stmt.getString(2);

      return result;
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't get LR name from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

  }

  /** Set the URL for the underlying storage mechanism. */
  public void setStorageUrl(URL storageUrl) throws PersistenceException {

    super.setStorageUrl(storageUrl);

  }

  /** Get the URL for the underlying storage mechanism. */
  public URL getStorageUrl() {

    return super.getStorageUrl();
  }

  /**
   * Create a new data store. <B>NOTE:</B> for some data stores
   * creation is an system administrator task; in such cases this
   * method will throw an UnsupportedOperationException.
   */
  public void create()
  throws PersistenceException, UnsupportedOperationException {

    super.create();
  }

  /** Open a connection to the data store. */
  public void open() throws PersistenceException {

    super.open();
  }

  /** Close the data store. */
  public void close() throws PersistenceException {

    super.close();
  }

  /**
   * Delete the data store. <B>NOTE:</B> for some data stores
   * deletion is an system administrator task; in such cases this
   * method will throw an UnsupportedOperationException.
   */
  public void delete()
  throws PersistenceException, UnsupportedOperationException {

    super.delete();
  }

  /**
   * Delete a resource from the data store.
   * @param lrId a data-store specific unique identifier for the resource
   * @param lrClassName class name of the type of resource
   */
  public void delete(String lrClassName, Object lrId)
  throws PersistenceException {

    if (false == lrId instanceof Long) {
      throw new IllegalArgumentException();
    }

    Long ID = (Long)lrId;

    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall("{ call persist.delete_lr(?,?) }");
      stmt.setLong(1,ID.longValue());
      stmt.setString(2,lrClassName);
      stmt.execute();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't delete LR from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }
  }

  /**
   * Save: synchonise the in-memory image of the LR with the persistent
   * image.
   */
  public void sync(LanguageResource lr) throws PersistenceException {
    throw new MethodNotImplementedException();
  }

  /**
   * Set method for the autosaving behaviour of the data store.
   * <B>NOTE:</B> many types of datastore have no auto-save function,
   * in which case this will throw an UnsupportedOperationException.
   */
  public void setAutoSaving(boolean autoSaving)
  throws UnsupportedOperationException {
    throw new MethodNotImplementedException();
  }

  /** Get the autosaving behaviour of the LR. */
  public boolean isAutoSaving() {
    throw new MethodNotImplementedException();
  }

  /** Adopt a resource for persistence. */
  public LanguageResource adopt(LanguageResource lr)
  throws PersistenceException {
    throw new MethodNotImplementedException();
  }

  /**
   * Get a resource from the persistent store.
   * <B>Don't use this method - use Factory.createResource with
   * DataStore and DataStoreInstanceId parameters set instead.</B>
   */
  public LanguageResource getLr(String lrClassName, Object lrPersistenceId)
  throws PersistenceException {
    throw new MethodNotImplementedException();
  }

  /** Get a list of the types of LR that are present in the data store. */
  public List getLrTypes() throws PersistenceException {

    Vector lrTypes = new Vector();
    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = this.jdbcConn.createStatement();
      rs = stmt.executeQuery(" SELECT lrtp_type " +
                             " FROM   t_lr_type LRTYPE ");

      while (rs.next()) {
        //access by index is faster
        String lrType = rs.getString(1);
        lrTypes.add(lrType);
      }

      return lrTypes;
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't get LR types from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(stmt);
    }
  }

  /** Get a list of the IDs of LRs of a particular type that are present. */
  public List getLrIds(String lrType) throws PersistenceException {

    Vector lrIDs = new Vector();
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = this.jdbcConn.prepareStatement(" SELECT lr_id " +
                                            " FROM   t_lang_resource LR, " +
                                            "        t_lr_type LRTYPE " +
                                            " WHERE  LR.lr_type_id = LRTYPE.lrtp_id " +
                                            "        AND LRTYPE.lrtp_type = ?"
                                            );
      stmt.setString(1,lrType);
      rs = stmt.executeQuery();

      while (rs.next()) {
        //access by index is faster
        Long lrID = new Long(rs.getLong(1));
        lrIDs.add(lrID);
      }

      return lrIDs;
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't get LR types from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(stmt);
    }

  }

  /** Get a list of the names of LRs of a particular type that are present. */
  public List getLrNames(String lrType) throws PersistenceException {

    Vector lrNames = new Vector();
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = this.jdbcConn.prepareStatement(" SELECT lr_name " +
                                            " FROM   t_lang_resource LR, " +
                                            "        t_lr_type LRTYPE " +
                                            " WHERE  LR.lr_type_id = LRTYPE.lrtp_id " +
                                            "        AND LRTYPE.lrtp_type = ?"
                                            );
      stmt.setString(1,lrType);
      rs = stmt.executeQuery();

      while (rs.next()) {
        //access by index is faster
        String lrName = rs.getString(1);
        lrNames.add(lrName);
      }

      return lrNames;
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't get LR types from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(stmt);
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

  /**
   * Checks if the user (identified by the sessionID)
   *  has read access to the LR
   */
  public boolean canReadLR(Object lrID, Session s)
    throws PersistenceException, gate.security.SecurityException{

    return canAccessLR((Long) lrID,s,READ_ACCESS);
  }

  /**
   * Checks if the user (identified by the sessionID)
   * has write access to the LR
   */
  public boolean canWriteLR(Object lrID, Session s)
    throws PersistenceException, gate.security.SecurityException{

    return canAccessLR((Long) lrID,s,WRITE_ACCESS);
  }


  /**
   * Checks if the user (identified by the sessionID)
   * has some access (read/write) to the LR
   */
  private boolean canAccessLR(Long lrID, Session s,int mode)
    throws PersistenceException, gate.security.SecurityException{

    Assert.assert(READ_ACCESS == mode || WRITE_ACCESS == mode);

    //first check the session and then check whether the user is member of the group
    if (this.ac.isValidSession(s) == false) {
      throw new gate.security.SecurityException("invalid session supplied");
    }

    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall("{ call security.has_access_to_lr(?,?,?,?,?)} ");
      stmt.setLong(1,lrID.longValue());
      stmt.setLong(2,s.getUser().getID().longValue());
      stmt.setLong(3,s.getGroup().getID().longValue());
      stmt.setLong(4,mode);

      stmt.registerOutParameter(5,java.sql.Types.INTEGER);
      stmt.execute();
      int result = stmt.getInt(5);

      return (ORACLE_TRUE == result);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't check permissions in DB: ["+ sqle.getMessage()+"]");
    }
  }


}
