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
import oracle.jdbc.driver.*;

import gate.*;
import gate.util.*;
import gate.event.*;
import gate.security.*;
import gate.security.SecurityException;


public abstract class JDBCDataStore extends AbstractFeatureBearer
                                    implements DatabaseDataStore,
                                                CreoleListener {

  /** --- */
  private static final boolean DEBUG = false;

  /** jdbc url for the database */
  private   String      dbURL;
  protected String      dbSchema;
  protected int         dbType;

  protected String      datastoreComment;
  protected String      iconName;

  /** jdbc driver name */
//  private   String      driverName;

  /**
   *  GUID of the datastore
   *  read from T_PARAMETER table
   *  */
  private   String      dbID;

  /** security session identifying all access to the datastore */
  protected   Session           session;

  /** datastore name? */
  protected   String            name;

  /** jdbc connection, all access to the database is made through this connection
   */
  protected transient Connection  jdbcConn;

  /** Security factory that contols access to objects in the datastore
   *  the security session is from this factory
   *  */
  protected transient AccessController  ac;

  /** anyone interested in datastore related events */
  private   transient Vector datastoreListeners;

  /** resources that should be sync-ed if datastore is close()-d */
  protected transient Vector dependentResources;

  /** Do not use this class directly - use one of the subclasses */
  protected JDBCDataStore() {

    this.datastoreListeners = new Vector();
    this.dependentResources = new Vector();
  }


  /*  interface DataStore  */

  /**
   * Save: synchonise the in-memory image of the LR with the persistent
   * image.
   */
  public String getComment() {

    Assert.assertNotNull(this.datastoreComment);
    return this.datastoreComment;
  }

  /**
   * Returns the name of the icon to be used when this datastore is displayed
   * in the GUI
   */
  public String getIconName() {
    Assert.assertNotNull(this.iconName);
    return this.iconName;
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
      this.dbSchema = DBHelper.getSchemaPrefix(this.dbURL);
      this.dbType = DBHelper.getDatabaseType(this.dbURL);
      Assert.assertNotNull(this.dbSchema);
      Assert.assertTrue(this.dbType > 0);
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
//      this.ac = new AccessControllerImpl();
      this.ac = Factory.createAccessController(dbURL);

      //3. open and init the security factory with the same DB repository
      ac.open();

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
      LanguageResource lr = (LanguageResource)this.dependentResources.elementAt(i);

      try {
        sync(lr);
      }
      catch(SecurityException se) {
        //do nothing
        //there was an oper and modified resource for which the user has no write
        //privileges
        //not doing anything is perfectly ok because the resource won't bechanged in DB
      }

      //unload UI component
      Factory.deleteResource(lr);
    }

    //1. close security factory
    ac.close();

    DBHelper.disconnect(this.jdbcConn);

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
    throws PersistenceException,SecurityException;


  /**
   * Save: synchonise the in-memory image of the LR with the persistent
   * image.
   */
  public abstract void sync(LanguageResource lr)
    throws PersistenceException,SecurityException;


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
  throws PersistenceException,SecurityException;

  /** Get a list of the types of LR that are present in the data store. */
  public List getLrTypes() throws PersistenceException {

    Vector lrTypes = new Vector();
    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = this.jdbcConn.createStatement();
      rs = stmt.executeQuery(" SELECT lrtp_type " +
                             " FROM   "+this.dbSchema+"t_lr_type LRTYPE ");

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
      stmt = this.jdbcConn.prepareStatement(
                      " SELECT lr_id " +
                      " FROM   "+this.dbSchema+"t_lang_resource LR, " +
                      "        "+this.dbSchema+"t_lr_type LRTYPE " +
                      " WHERE  LR.lr_type_id = LRTYPE.lrtp_id " +
                      "        AND LRTYPE.lrtp_type = ? " +
                      " ORDER BY lr_name"
                      );
      stmt.setString(1,lrType);

      //oracle special
      if (this.dbType == DBHelper.ORACLE_DB) {
        ((OraclePreparedStatement)stmt).setRowPrefetch(DBHelper.CHINK_SIZE_SMALL);
      }

      stmt.execute();
      rs = stmt.getResultSet();

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
  public abstract List getLrNames(String lrType) throws PersistenceException;

  /**
   * Checks if the user (identified by the sessionID)
   *  has read access to the LR
   */
  public abstract boolean canReadLR(Object lrID)
    throws PersistenceException, gate.security.SecurityException;


  /**
   * Checks if the user (identified by the sessionID)
   * has write access to the LR
   */
  public abstract boolean canWriteLR(Object lrID)
    throws PersistenceException, gate.security.SecurityException;


  /*  interface DatabaseDataStore  */

  /**
   * starts a transaction
   * note that if u're already in transaction context this will not open
   * nested transaction
   * i.e. many consecutive calls to beginTrans() make no difference if no commit/rollback
   * is made meanwhile
   *  */
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


  /**
   * commits transaction
   * note that this will commit all the uncommited calls made so far
   *  */
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

  /** rollsback a transaction */
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

  /** not used */
  public Long timestamp()
    throws PersistenceException{

    //implemented by the subclasses
    throw new MethodNotImplementedException();
  }

  /** not used */
  public void deleteSince(Long timestamp)
    throws PersistenceException{

    throw new MethodNotImplementedException();
  }

  /** specifies the driver to be used to connect to the database? */
/*  public void setDriver(String driverName)
    throws PersistenceException{

    this.driverName = driverName;
  }
*/
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

    if (null == value)
      return DBHelper.VALUE_TYPE_NULL;
    else if (value instanceof Integer)
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
    else if (value instanceof Serializable) {
      return DBHelper.VALUE_TYPE_BINARY;
    }

    //this should never happen
    throw new IllegalArgumentException();
  }

  /** --- */
  public String getDatabaseID() {
    return this.dbID;
  }

  /** reads the GUID from the database */
/*  protected abstract String readDatabaseID()
    throws PersistenceException;
*/
  /**
   *  reads the ID of the database
   *  every database should have unique string ID
   */
  protected String readDatabaseID() throws PersistenceException{

    PreparedStatement pstmt = null;
    ResultSet rs = null;
    String  result = null;

    //1. read from DB
    try {
      String sql = " select par_value_string " +
                   " from  "+this.dbSchema+"t_parameter " +
                   " where  par_key = ? ";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setString(1,DBHelper.DB_PARAMETER_GUID);
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (false == rs.next()) {
        throw new PersistenceException("Can't read database parameter ["+
                                          DBHelper.DB_PARAMETER_GUID+"]");
      }
      result = rs.getString(1);
    }
    catch(SQLException sqle) {
        throw new PersistenceException("Can't read database parameter ["+
                                          sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }

    if (DEBUG) {
      Out.println("reult=["+result+"]");
    }

    return result;
  }


  /**
   * Removes a a previously registered {@link gate.event.DatastoreListener}
   * from the list listeners for this datastore
   */
  public void removeDatastoreListener(DatastoreListener l) {

    Assert.assertNotNull(this.datastoreListeners);

    synchronized(this.datastoreListeners) {
      this.datastoreListeners.remove(l);
    }
  }


  /**
   * Registers a new {@link gate.event.DatastoreListener} with this datastore
   */
  public void addDatastoreListener(DatastoreListener l) {

    Assert.assertNotNull(this.datastoreListeners);

    //this is not thread safe
/*    if (false == this.datastoreListeners.contains(l)) {
      Vector temp = (Vector)this.datastoreListeners.clone();
      temp.add(l);
      this.datastoreListeners = temp;
    }
*/
    synchronized(this.datastoreListeners) {
      if (false == this.datastoreListeners.contains(l)) {
        this.datastoreListeners.add(l);
      }
    }
  }

  protected void fireResourceAdopted(DatastoreEvent e) {

    Assert.assertNotNull(datastoreListeners);
    Vector temp = this.datastoreListeners;

    int count = temp.size();
    for (int i = 0; i < count; i++) {
      ((DatastoreListener)temp.elementAt(i)).resourceAdopted(e);
    }
  }


  protected void fireResourceDeleted(DatastoreEvent e) {

    Assert.assertNotNull(datastoreListeners);
    Vector temp = this.datastoreListeners;

    int count = temp.size();
    for (int i = 0; i < count; i++) {
      ((DatastoreListener)temp.elementAt(i)).resourceDeleted(e);
    }
  }


  protected void fireResourceWritten(DatastoreEvent e) {
    Assert.assertNotNull(datastoreListeners);
    Vector temp = this.datastoreListeners;

    int count = temp.size();
    for (int i = 0; i < count; i++) {
      ((DatastoreListener)temp.elementAt(i)).resourceWritten(e);
    }
  }

  public void resourceLoaded(CreoleEvent e) {
    if(DEBUG)
      System.out.println("resource loaded...");
  }

  public void resourceRenamed(Resource resource, String oldName,
                              String newName){
  }


  public void resourceUnloaded(CreoleEvent e) {

    Assert.assertNotNull(e.getResource());
    if(! (e.getResource() instanceof LanguageResource))
      return;

    //1. check it's our resource
    LanguageResource lr = (LanguageResource)e.getResource();

    //this is a resource from another DS, so no need to do anything
    if(lr.getDataStore() != this)
      return;

    //2. remove from the list of reosurce that should be sunced if DS is closed
    this.dependentResources.remove(lr);

    //3. don't save it, this may not be the user's choice

    //4. remove the reource as listener for events from the DataStore
    //otherwise the DS will continue sending it events when the reource is
    // no longer active
    this.removeDatastoreListener((DatastoreListener)lr);
  }

  public void datastoreOpened(CreoleEvent e) {
    if(DEBUG)
      System.out.println("datastore opened...");
  }

  public void datastoreCreated(CreoleEvent e) {
    if(DEBUG)
      System.out.println("datastore created...");
  }

  public void datastoreClosed(CreoleEvent e) {
    if(DEBUG)
      System.out.println("datastore closed...");
    //sync all dependent resources
  }

  /** identify user using this datastore */
  public void setSession(Session s)
    throws gate.security.SecurityException {

    this.session = s;
  }



  /** identify user using this datastore */
  public Session getSession(Session s)
    throws gate.security.SecurityException {

    return this.session;
  }

  /** Get a list of LRs that satisfy some set or restrictions */
  public abstract List findLrIds(List constraints) throws PersistenceException;

  /**
   *  Get a list of LRs that satisfy some set or restrictions and are
   *  of a particular type
   */
  public abstract List findLrIds(List constraints, String lrType)
  throws PersistenceException;

}
