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

import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.event.*;
import gate.security.*;


public abstract class JDBCDataStore extends AbstractFeatureBearer
                                    implements DatabaseDataStore,
                                                CreoleListener {

  /** --- */
/*  private static final String jdbcOracleDriverName = "oracle.jdbc.driver.OracleDriver";
  private static final String jdbcPostgresDriverName = "postgresql.Driver";
  private static final String jdbcSapDBDriverName = "com.sap.dbtech.jdbc.DriverSapDB";
*/

  /** --- */
  private   String      dbURL;
  private   String      driverName;
  private   String      dbID;

  protected   Session           session;
  protected   String            name;

  protected transient Connection  jdbcConn;
  protected transient AccessController  ac;
  private   transient Vector datastoreListeners;
  protected transient Vector dependentResources;

  /** Do not use this class directly - use one of the subclasses */
  protected JDBCDataStore() {

    this.datastoreListeners = new Vector();
    this.dependentResources = new Vector();
  }


  /*  interface DataStore  */

  /**
   * Returns the comment displayed by the GUI for this DataStore
   */
  public abstract String getComment();

  /**
   * Returns the name of the icon to be used when this datastore is displayed
   * in the GUI
   */
  public abstract String getIconName();


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

      //4. get DB ID
      this.dbID = this.readDatabaseID();

    }
    catch(SQLException sqle) {
      throw new PersistenceException("could not get DB connection ["+ sqle.getMessage() +"]");
    }
    catch(ClassNotFoundException clse) {
      throw new PersistenceException("cannot locate JDBC driver ["+ clse.getMessage() +"]");
    }

    //5. register for Creole events
    Gate.getCreoleRegister().addCreoleListener(this);
  }

  /** Close the data store. */
  public void close() throws PersistenceException {

    //-1. Unregister for Creole events
    Gate.getCreoleRegister().removeCreoleListener(this);

    //0. sync all dependednt resources
    for (int i=0; i< this.dependentResources.size(); i++) {
      LanguageResource lr = (LanguageResource)this.dependentResources.elementAt(0);
      sync(lr);
      //unload UI component
      Factory.deleteResource(lr);
    }

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

    //finally unregister this datastore from the GATE register of datastores
    Gate.getDataStoreRegister().remove(this);
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
  throws UnsupportedOperationException,PersistenceException {
    try {
      this.jdbcConn.setAutoCommit(true);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("cannot change autosave mode ["+sqle.getMessage()+"]");
    }

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
  protected int findFeatureType(Object value) {

    if (value instanceof Integer)
      return DBHelper.VALUE_TYPE_INTEGER;
    else if (value instanceof Long)
      return DBHelper.VALUE_TYPE_LONG;
    else if (value instanceof Boolean)
      return DBHelper.VALUE_TYPE_BOOLEAN;
    else if (value instanceof Double ||
             value instanceof Float)
      return DBHelper.VALUE_TYPE_FLOAT;
    else if (value instanceof String)
      return DBHelper.VALUE_TYPE_STRING;
    else if (value instanceof List) {
      //is the array empty?
      List arr = (List)value;

      if (arr.isEmpty()) {
        return DBHelper.VALUE_TYPE_EMPTY_ARR;
      }
      else {
        Object element = arr.get(0);

        if (element  instanceof Integer)
          return DBHelper.VALUE_TYPE_INTEGER_ARR;
        else if (element  instanceof Long)
          return DBHelper.VALUE_TYPE_LONG_ARR;
        else if (element instanceof Boolean)
          return DBHelper.VALUE_TYPE_BOOLEAN_ARR;
        else if (element instanceof Double ||
                 element instanceof Float)
          return DBHelper.VALUE_TYPE_FLOAT_ARR;
        else if (element instanceof String)
          return DBHelper.VALUE_TYPE_STRING_ARR;
      }
    }

    //this should never happen
    throw new IllegalArgumentException();
  }

  /** --- */
  public String getDatabaseID() {
    return this.dbID;
  }

  /** --- */
  public abstract String readDatabaseID()
    throws PersistenceException;

  /**
   * Removes a a previously registered {@link gate.event.DatastoreListener}
   * from the list listeners for this datastore
   */
  public void removeDatastoreListener(DatastoreListener l) {
//System.out.println(">> ["+l.hashCode()+"] listener being removed...");
    Assert.assertNotNull(this.datastoreListeners);
//    Assert.assertTrue(this.datastoreListeners.contains(l));

    Vector temp = (Vector)this.datastoreListeners.clone();
    temp.remove(l);

    this.datastoreListeners = temp;
  }


  /**
   * Registers a new {@link gate.event.DatastoreListener} with this datastore
   */
  public void addDatastoreListener(DatastoreListener l) {
//System.out.println(">> ["+l.hashCode()+"] listener added...");
    Assert.assertNotNull(this.datastoreListeners);
    if (false == this.datastoreListeners.contains(l)) {
      Vector temp = (Vector)this.datastoreListeners.clone();
      temp.add(l);
      this.datastoreListeners = temp;
    }
  }

  protected void fireResourceAdopted(DatastoreEvent e) {

    Assert.assertNotNull(datastoreListeners);
    int count = datastoreListeners.size();
    for (int i = 0; i < count; i++) {
      ((DatastoreListener)datastoreListeners.elementAt(i)).resourceAdopted(e);
    }
  }


  protected void fireResourceDeleted(DatastoreEvent e) {

    Assert.assertNotNull(datastoreListeners);
    int count = datastoreListeners.size();
    for (int i = 0; i < count; i++) {
      ((DatastoreListener)datastoreListeners.elementAt(i)).resourceDeleted(e);
    }
  }


  protected void fireResourceWritten(DatastoreEvent e) {
//System.out.println("lrid=["+e.getResourceID()+"] written...");
    Assert.assertNotNull(datastoreListeners);
    int count = datastoreListeners.size();
    for (int i = 0; i < count; i++) {
      ((DatastoreListener)datastoreListeners.elementAt(i)).resourceWritten(e);
    }
  }


  public void resourceLoaded(CreoleEvent e) {
System.out.println("resource loaded...");
  }


  public void resourceUnloaded(CreoleEvent e) {

    Assert.assertNotNull(e.getResource());
System.out.println(e);
    Assert.assertNotNull(e.getDatastore());
    LanguageResource lr = (LanguageResource)e.getResource();

    this.dependentResources.remove(lr);
    //don't save it, this may not be the user's choice
  }

  public void datastoreOpened(CreoleEvent e) {
System.out.println("datastore opened...");
  }

  public void datastoreCreated(CreoleEvent e) {
System.out.println("datastore created...");
  }

  public void datastoreClosed(CreoleEvent e) {
System.out.println("datastore closed...");
    //sync all dependent resources
  }

}
