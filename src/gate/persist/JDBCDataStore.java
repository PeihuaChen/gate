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

  /** used to store corpus' features */
  protected static final int FEATURE_OWNER_CORPUS  = 1;
  /** used to store document's features */
  protected static final int FEATURE_OWNER_DOCUMENT  = 2;
  /** used to store annotation's features */
  protected static final int FEATURE_OWNER_ANNOTATION  = 3;


  /** feature value is int  */
  protected static final int VALUE_TYPE_INTEGER           = 101;
  /** feature value is long */
  protected static final int VALUE_TYPE_LONG              = 102;
  /** feature value is boolean */
  protected static final int VALUE_TYPE_BOOLEAN           = 103;
  /** feature value is string less than 4000 bytes */
  protected static final int VALUE_TYPE_STRING            = 104;
  /** feature value is binary */
  protected static final int VALUE_TYPE_BINARY            = 105;
  /** feature value is float */
  protected static final int VALUE_TYPE_FLOAT             = 106;
  /** feature value is array of ints */
  protected static final int VALUE_TYPE_INTEGER_ARR       = 107;
  /** feature value is array of longs */
  protected static final int VALUE_TYPE_LONG_ARR          = 108;
  /** feature value is array of bools */
  protected static final int VALUE_TYPE_BOOLEAN_ARR       = 109;
  /** feature value is array of strings */
  protected static final int VALUE_TYPE_STRING_ARR        = 110;
  /** feature value is array of binary values */
  protected static final int VALUE_TYPE_BINARY_ARR        = 111;
  /** feature value is array of floats */
  protected static final int VALUE_TYPE_FLOAT_ARR         = 112;
  /** feature value is array of floats */
  protected static final int VALUE_TYPE_EMPTY_ARR         = 113;


  /** Do not use this class directly - use one of the subclasses */
  protected JDBCDataStore() {
    throw new MethodNotImplementedException();
  }


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
  public abstract String getLrName(Object lrId) throws PersistenceException;


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
  public abstract void delete(String lrClassName, Object lrId)
    throws PersistenceException;


  /**
   * Save: synchonise the in-memory image of the LR with the persistent
   * image.
   */
  public abstract void sync(LanguageResource lr)
    throws PersistenceException;


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
  public abstract LanguageResource adopt(LanguageResource lr,SecurityInfo secInfo)
    throws PersistenceException,gate.security.SecurityException;

  /**
   * Get a resource from the persistent store.
   * <B>Don't use this method - use Factory.createResource with
   * DataStore and DataStoreInstanceId parameters set instead.</B>
   */
  public abstract LanguageResource getLr(String lrClassName, Object lrPersistenceId)
  throws PersistenceException;

  /** Get a list of the types of LR that are present in the data store. */
  public abstract List getLrTypes() throws PersistenceException;


  /** Get a list of the IDs of LRs of a particular type that are present. */
  public abstract List getLrIds(String lrType) throws PersistenceException;


  /** Get a list of the names of LRs of a particular type that are present. */
  public abstract List getLrNames(String lrType) throws PersistenceException;

  /**
   * Checks if the user (identified by the sessionID)
   *  has read access to the LR
   */
  public abstract boolean canReadLR(Object lrID, Session s)
    throws PersistenceException, gate.security.SecurityException;


  /**
   * Checks if the user (identified by the sessionID)
   * has write access to the LR
   */
  public abstract boolean canWriteLR(Object lrID, Session s)
    throws PersistenceException, gate.security.SecurityException;


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
  protected abstract void readCLOB(java.sql.Clob src, StringBuffer dest)
    throws SQLException, IOException;


  /** --- */
  protected abstract void writeCLOB(StringBuffer src,java.sql.Clob dest)
    throws SQLException,IOException;

  /** --- */
  protected abstract void writeCLOB(String src,java.sql.Clob dest)
    throws SQLException,IOException;


  /** --- */
  protected int findFeatureType(Object value) {

    if (value instanceof Integer)
      return this.VALUE_TYPE_INTEGER;
    else if (value instanceof Long)
      return this.VALUE_TYPE_LONG;
    else if (value instanceof Boolean)
      return this.VALUE_TYPE_BOOLEAN;
    else if (value instanceof Double ||
             value instanceof Float)
      return this.VALUE_TYPE_FLOAT;
    else if (value instanceof String)
      return this.VALUE_TYPE_STRING;
    else if (value instanceof List) {
      //is the array empty?
      List arr = (List)value;

      if (arr.isEmpty()) {
        return this.VALUE_TYPE_EMPTY_ARR;
      }
      else {
        Object element = arr.get(0);

        if (element  instanceof Integer)
          return this.VALUE_TYPE_INTEGER_ARR;
        else if (element  instanceof Long)
          return this.VALUE_TYPE_LONG_ARR;
        else if (element instanceof Boolean)
          return this.VALUE_TYPE_BOOLEAN_ARR;
        else if (element instanceof Double ||
                 element instanceof Float)
          return this.VALUE_TYPE_FLOAT_ARR;
        else if (element instanceof String)
          return this.VALUE_TYPE_STRING_ARR;
      }
    }

    //this should never happen
    throw new IllegalArgumentException();
  }

}
