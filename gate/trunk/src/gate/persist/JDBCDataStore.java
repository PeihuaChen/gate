  /*
 *  JDBCDataStore.java
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
import java.io.*;

import gate.*;
import gate.util.*;
import gate.event.*;
import gate.security.*;


public abstract class JDBCDataStore
extends AbstractFeatureBearer implements DatabaseDataStore{

  /** --- */
/*  private static final String jdbcOracleDriverName = "oracle.jdbc.driver.OracleDriver";
  private static final String jdbcPostgresDriverName = "postgresql.Driver";
  private static final String jdbcSapDBDriverName = "com.sap.dbtech.jdbc.DriverSapDB";
*/

  /** --- */
  protected Connection  jdbcConn;
  private   String      dbURL;
  private   String      driverName;

  protected   AccessController  ac;
  protected   Session           session;
  protected   String            name;

  //!!! ACHTUNG !!!
  // these 4 constants should *always* be synchronzied with the ones in the
  // related SQL packages/scripts [for Oracle - security.spc]
  // i.e. if u don't have a serious reason do *not* change anything

  /** world read/ group write */
  public static final int ACCESS_WR_GW = 1;
  /** group read/ group write */
  public static final int ACCESS_GR_GW = 2;
  /** group read/ owner write */
  public static final int ACCESS_GR_OW = 3;
  /** owner read/ owner write */
  public static final int ACCESS_OR_OW = 4;


  /** Do not use this class directly - use one of the subclasses */
  protected JDBCDataStore() {
    throw new MethodNotImplementedException();
  }

  /** --- */
/*  private void loadDrivers()
    throws ClassNotFoundException {

    if (this.driverName != null) {
      Class.forName(this.driverName);
    }
    else {
      Class.forName(this.jdbcOracleDriverName);
      Class.forName(this.jdbcPostgresDriverName);
      Class.forName(this.jdbcSapDBDriverName);
    }
  }
*/

  /** --- */
/*  protected void cleanup(ResultSet rs)
    throws PersistenceException {

    try {
      if (rs!=null)
        rs.close();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("an SQL exception occured ["+ sqle.getMessage()+"]");
    }
  }
*/
  /** --- */
/*  protected void cleanup(Statement stmt)
    throws PersistenceException {
    try {
      if (stmt!=null)
        stmt.close();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("an SQL exception occured ["+ sqle.getMessage()+"]");
    }
  }
*/
  /** --- */
/*  protected Connection connect(URL connectURL)
    throws SQLException,ClassNotFoundException{

    loadDrivers();
    Connection conn = DriverManager.getConnection(connectURL.toString());

    return conn;
  }
*/

  /** --- */
/*  protected void disconnect() {
    throw new MethodNotImplementedException();
//    this.jdbcConn.close();

  }
*/

  /*  interface DataStore  */

  /**
   * Returns the comment displayed by the GUI for this DataStore
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
  public String getLrName(Object lrId) throws PersistenceException {
    throw new MethodNotImplementedException();
  }

  /** Set the URL for the underlying storage mechanism. */
  public void setStorageUrl(String storageUrl) throws PersistenceException {

    if (!storageUrl.startsWith("jdbc:")) {
      throw new PersistenceException("Incorrect JDBC url (should start with \"jdbc:\")");
    }
    else {
      this.dbURL = storageUrl;
    }

  }

  /** Get the URL for the underlying storage mechanism. */
  public String getStorageUrl() {

    return this.dbURL;

  }

  /**
   * Create a new data store. <B>NOTE:</B> for some data stores
   * creation is an system administrator task; in such cases this
   * method will throw an UnsupportedOperationException.
   */
  public void create()
  throws PersistenceException, UnsupportedOperationException {

    throw new UnsupportedOperationException("create() is not supported for DatabaseDataStore");
  }

  /** Open a connection to the data store. */
  public void open() throws PersistenceException {
    try {

      //1, get connection to the DB
      jdbcConn = DBHelper.connect(dbURL);

      //2. create security factory
      this.ac = new AccessControllerImpl();

      //3. open and init the security factory with the same DB repository
      ac.open(dbURL);

      //4. login should be somewhere here?

    }
    catch(SQLException sqle) {
      throw new PersistenceException("could not get DB connection ["+ sqle.getMessage() +"]");
    }
    catch(ClassNotFoundException clse) {
      throw new PersistenceException("cannot locate JDBC driver ["+ clse.getMessage() +"]");
    }
  }

  /** Close the data store. */
  public void close() throws PersistenceException {

    //1. close security factory
    ac.close();

    //2. close the JDBC connection
    try {
      //rollback uncommited transactions
      this.jdbcConn.rollback();
      this.jdbcConn.close();
    }
    catch (SQLException sqle) {
      throw new PersistenceException("cannot close JDBC connection, DB error is ["+
                                      sqle.getMessage() +"]");
    }
  }

  /**
   * Delete the data store. <B>NOTE:</B> for some data stores
   * deletion is an system administrator task; in such cases this
   * method will throw an UnsupportedOperationException.
   */
  public void delete()
  throws PersistenceException, UnsupportedOperationException {

    throw new UnsupportedOperationException("delete() is not supported for DatabaseDataStore");
  }

  /**
   * Delete a resource from the data store.
   * @param lrId a data-store specific unique identifier for the resource
   * @param lrClassName class name of the type of resource
   */
  public void delete(String lrClassName, Object lrId)
  throws PersistenceException {
    throw new MethodNotImplementedException();
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
    throw new MethodNotImplementedException();
  }

  /** Get a list of the IDs of LRs of a particular type that are present. */
  public List getLrIds(String lrType) throws PersistenceException {
    throw new MethodNotImplementedException();
  }

  /** Get a list of the names of LRs of a particular type that are present. */
  public List getLrNames(String lrType) throws PersistenceException {
    throw new MethodNotImplementedException();
  }

  /**
   * Checks if the user (identified by the sessionID)
   *  has read access to the LR
   */
  public boolean canReadLR(Object lrID, Session s)
    throws PersistenceException, gate.security.SecurityException{

    throw new MethodNotImplementedException();
  }
  /**
   * Checks if the user (identified by the sessionID)
   * has write access to the LR
   */
  public boolean canWriteLR(Object lrID, Session s)
    throws PersistenceException, gate.security.SecurityException{

    throw new MethodNotImplementedException();
  }



  /*  interface DatabaseDataStore  */

  /** --- */
  public void beginTrans()
    throws PersistenceException,UnsupportedOperationException{

    try {
      this.jdbcConn.setAutoCommit(false);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("cannot begin transaction, DB error is: ["
                                                      +sqle.getMessage()+"]");
    }
  }


  /** --- */
  public void commitTrans()
    throws PersistenceException,UnsupportedOperationException{

    try {
      this.jdbcConn.commit();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("cannot commit transaction, DB error is: ["
                                                      +sqle.getMessage()+"]");
    }

  }

  /** --- */
  public void rollbackTrans()
    throws PersistenceException,UnsupportedOperationException{

    try {
      this.jdbcConn.rollback();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("cannot commit transaction, DB error is: ["
                                                      +sqle.getMessage()+"]");
    }

  }

  /** --- */
  public Long timestamp()
    throws PersistenceException{

    //implemented by the subclasses
    throw new MethodNotImplementedException();
  }

  /** --- */
  public void deleteSince(Long timestamp)
    throws PersistenceException{

    throw new MethodNotImplementedException();
  }

  /** --- */
  public void setDriver(String driverName)
    throws PersistenceException{

    this.driverName = driverName;
  }
    /** Sets the name of this resource*/
  public void setName(String name){
    this.name = name;
  }

  /** Returns the name of this resource*/
  public String getName(){
    return name;
  }




  /** --- */
  protected void readCLOB(java.sql.Clob src, StringBuffer dest)
    throws SQLException, IOException {

    throw new MethodNotImplementedException();
  }


  /** --- */
  protected void writeCLOB(StringBuffer src,java.sql.Clob dest)
    throws SQLException,IOException  {

    throw new MethodNotImplementedException();
  }

}
