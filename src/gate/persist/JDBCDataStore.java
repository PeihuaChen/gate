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
import gate.corpora.*;
import gate.annotation.*;

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
  public String getLrName(Object lrId)
    throws PersistenceException {

    if (false == lrId instanceof Long) {
      throw new IllegalArgumentException();
    }

    Long ID = (Long)lrId;

    PreparedStatement pstmt = null;
    ResultSet rset = null;

    try {
      String sql = " select lr_name " +
                  " from   "+this.dbSchema+"t_lang_resource " +
                  " where  lr_id = ?";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,ID.longValue());
      pstmt.execute();
      rset = pstmt.getResultSet();

      rset.next();
      String result = rset.getString("lr_name");

      return result;
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't get LR name from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(pstmt);
      DBHelper.cleanup(rset);
    }
  }



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

  public void delete(String lrClassName, Object lrId)
  throws PersistenceException,SecurityException {
    //0. preconditions
    if (false == lrId instanceof Long) {
      throw new IllegalArgumentException();
    }

    if (!lrClassName.equals(DBHelper.DOCUMENT_CLASS) &&
        !lrClassName.equals(DBHelper.CORPUS_CLASS)) {
      throw new IllegalArgumentException("Only Corpus and Document classes are supported" +
                                          " by Database data store");
    }

    //1. check session
    if (null == this.session) {
      throw new SecurityException("session not set");
    }

    if (false == this.ac.isValidSession(this.session)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. check permissions
    if (false == canWriteLR(lrId)) {
      throw new SecurityException("insufficient privileges");
    }

    //3. try to lock document, so that we'll be sure no one is editing it
    //NOTE: use the private method
    User lockingUser = this.getLockingUser((Long)lrId);
    User currUser = this.session.getUser();

    if (null != lockingUser && false == lockingUser.equals(currUser)) {
      //oops, someone is editing now
      throw new PersistenceException("LR locked by another user");
    }

    boolean transFailed = false;
    try {
      //4. autocommit should be FALSE because of LOBs
      beginTrans();

      //5. perform changes, if anything goes wrong, rollback
      if (lrClassName.equals(DBHelper.DOCUMENT_CLASS)) {
        deleteDocument((Long)lrId);
      }
      else {
        deleteCorpus((Long)lrId);
      }

      //6. done, commit
      commitTrans();
    }
    catch(PersistenceException pe) {
      transFailed = true;
      throw(pe);
    }
    finally {
      //problems?
      if (transFailed) {
        rollbackTrans();
      }
    }

    //7, unlock
    //do nothing - the resource does not exist anymore

    //8. delete from the list of dependent resources
    boolean resourceFound = false;
    Iterator it = this.dependentResources.iterator();
    while (it.hasNext()) {
      LanguageResource lr = (LanguageResource)it.next();
      if (lr.getLRPersistenceId().equals(lrId)) {
        resourceFound = true;
        it.remove();
        break;
      }
    }

    //Assert.assertTrue(resourceFound);

    //9. let the world know about it
    fireResourceDeleted(
      new DatastoreEvent(this, DatastoreEvent.RESOURCE_DELETED, null, lrId));

    //10. unload the resource form the GUI
    try {
      unloadLR((Long)lrId);
    }
    catch(GateException ge) {
      Err.prln("can't unload resource from GUI...");
    }
  }



  /**
   * Save: synchonise the in-memory image of the LR with the persistent
   * image.
   */
  public void sync(LanguageResource lr)
  throws PersistenceException,SecurityException {

    //4.delegate (open a new transaction)
    _sync(lr,true);
  }


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
  public LanguageResource adopt(LanguageResource lr, SecurityInfo secInfo)
  throws PersistenceException,SecurityException {
    //open a new transaction
    return _adopt(lr,secInfo,true);
  }


  protected LanguageResource _adopt(LanguageResource lr,
                                  SecurityInfo secInfo,
                                  boolean openNewTrans)
  throws PersistenceException,SecurityException {

    LanguageResource result = null;

    //-1. preconditions
    Assert.assertNotNull(lr);
    Assert.assertNotNull(secInfo);
    if (false == lr instanceof Document &&
        false == lr instanceof Corpus) {
      //only documents and corpuses could be serialized in DB
      throw new IllegalArgumentException("only Documents and Corpuses could "+
                                          "be serialized in DB");
    }

    //0. check SecurityInfo
    if (false == this.ac.isValidSecurityInfo(secInfo)) {
      throw new SecurityException("Invalid security settings supplied");
    }

    //1. user session should be set
    if (null == this.session) {
      throw new SecurityException("user session not set");
    }

    //2. check the LR's current DS
    DataStore currentDS = lr.getDataStore();
    if(currentDS == null) {
      // an orphan - do the adoption (later)
    }
    else if(currentDS.equals(this)){         // adopted already
      return lr;
    }
    else {                      // someone else's child
      throw new PersistenceException(
        "Can't adopt a resource which is already in a different datastore");
    }


    //3. is the LR one of Document or Corpus?
    if (false == lr instanceof Document &&
        false == lr instanceof Corpus) {

      throw new IllegalArgumentException("Database datastore is implemented only for "+
                                        "Documents and Corpora");
    }

    //4.is the document already stored in this storage?
    Object persistID = lr.getLRPersistenceId();
    if (persistID != null) {
      throw new PersistenceException("This LR is already stored in the " +
                                      " database (persistance ID is =["+(Long)persistID+"] )");
    }

    boolean transFailed = false;
    try {
      //5 autocommit should be FALSE because of LOBs
      if (openNewTrans) {
//        this.jdbcConn.setAutoCommit(false);
        beginTrans();
      }

      //6. perform changes, if anything goes wrong, rollback
      if (lr instanceof Document) {
        result =  createDocument((Document)lr,secInfo);
//System.out.println("result ID=["+result.getLRPersistenceId()+"]");
      }
      else {
        //adopt each document from the corpus in a separate transaction context
        result =  createCorpus((Corpus)lr,secInfo,true);
      }

      //7. done, commit
      if (openNewTrans) {
//        this.jdbcConn.commit();
        commitTrans();
      }
    }
/*
    catch(SQLException sqle) {
      transFailed = true;
      throw new PersistenceException("Cannot start/commit a transaction, ["+sqle.getMessage()+"]");
    }
*/
    catch(PersistenceException pe) {
      transFailed = true;
      throw(pe);
    }
    catch(SecurityException se) {
      transFailed = true;
      throw(se);
    }
    finally {
      //problems?
      if (transFailed) {
System.out.println("trans failed ...rollback");
        rollbackTrans();
/*        try {
          this.jdbcConn.rollback();
        }
        catch(SQLException sqle) {
          throw new PersistenceException(sqle);
        }
*/
      }
    }

    //8. let the world know
    fireResourceAdopted(
        new DatastoreEvent(this, DatastoreEvent.RESOURCE_ADOPTED,
                           result,
                           result.getLRPersistenceId())
    );

    //9. fire also resource written event because it's now saved
    fireResourceWritten(
      new DatastoreEvent(this, DatastoreEvent.RESOURCE_WRITTEN,
                          result,
                          result.getLRPersistenceId()
      )
    );

    //10. add the resource to the list of dependent resources - i.e. the ones that the
    //data store should take care upon closing [and call sync()]
    this.dependentResources.add(result);

    return result;
  }


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
  public List getLrNames(String lrType) throws PersistenceException {

    Vector lrNames = new Vector();
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      stmt = this.jdbcConn.prepareStatement(
                " SELECT lr_name " +
                " FROM   "+this.dbSchema+"t_lang_resource LR, " +
                "        t_lr_type LRTYPE " +
                " WHERE  LR.lr_type_id = LRTYPE.lrtp_id " +
                "        AND LRTYPE.lrtp_type = ? " +
                " ORDER BY lr_name desc"
                );
      stmt.setString(1,lrType);

      //Oracle special
      if (this.dbType == DBHelper.ORACLE_DB) {
        ((OraclePreparedStatement)stmt).setRowPrefetch(DBHelper.CHINK_SIZE_SMALL);
      }

      stmt.execute();
      rs = stmt.getResultSet();

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

  /**
   * Checks if the user (identified by the sessionID)
   *  has read access to the LR
   */
  public boolean canReadLR(Object lrID)
    throws PersistenceException, SecurityException{

    return canAccessLR((Long) lrID,DBHelper.READ_ACCESS);
  }



  /**
   * Checks if the user (identified by the sessionID)
   * has write access to the LR
   */
  public boolean canWriteLR(Object lrID)
    throws PersistenceException, SecurityException{

    return canAccessLR((Long) lrID,DBHelper.WRITE_ACCESS);
  }

  /**
   * Checks if the user (identified by the sessionID)
   * has some access (read/write) to the LR
   */
  protected boolean canAccessLR(Long lrID,int mode)
    throws PersistenceException, SecurityException{

    //abstract
    throw new MethodNotImplementedException();
  }

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


  /** get security information for LR . */
  public SecurityInfo getSecurityInfo(LanguageResource lr)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(lr);
    Assert.assertNotNull(lr.getLRPersistenceId());
    Assert.assertTrue(lr.getLRPersistenceId() instanceof Long);
    Assert.assertEquals(this,lr.getDataStore());
    Assert.assertTrue(lr instanceof DatabaseDocumentImpl ||
                      lr instanceof DatabaseCorpusImpl);

    PreparedStatement pstmt = null;
    ResultSet rs = null;

    //1. read data
    Long userID = null;
    Long groupID = null;
    int  perm;
    try {
      String sql =  "   select lr_owner_user_id, "+
                    "          lr_owner_group_id, " +
                    "          lr_access_mode "+
                    "   from   "+this.dbSchema+"t_lang_resource "+
                    "   where  lr_id = ?";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)lr.getLRPersistenceId()).longValue());
      rs = pstmt.executeQuery();

      if (false == rs.next()) {
        throw new PersistenceException("Invalid LR ID supplied - no data found");
      }

      userID = new Long(rs.getLong("lr_owner_user_id"));
      groupID = new Long(rs.getLong("lr_owner_group_id"));
      perm = rs.getInt("lr_access_mode");

      Assert.assertTrue(perm == SecurityInfo.ACCESS_GR_GW ||
                        perm == SecurityInfo.ACCESS_GR_OW ||
                        perm == SecurityInfo.ACCESS_OR_OW ||
                        perm == SecurityInfo.ACCESS_WR_GW);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("Can't read document permissions from DB, error is [" +
                                      sqle.getMessage() +"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }

    //2. get data from AccessController
    User usr = null;
    Group grp = null;
    try {
      usr = this.ac.findUser(userID);
      grp = this.ac.findGroup(groupID);
    }
    catch (SecurityException se) {
      throw new PersistenceException("Invalid security settings found in DB [" +
                                      se.getMessage() +"]");
    }

    //3. construct SecurityInfo
    SecurityInfo si = new SecurityInfo(perm,usr,grp);


    return si;
  }

  /** creates a LR of type Corpus  */
  protected Corpus createCorpus(Corpus corp,SecurityInfo secInfo, boolean newTransPerDocument)
    throws PersistenceException,SecurityException {

    //1. create an LR entry for the corpus (T_LANG_RESOURCE table)
    Long lrID = createLR(DBHelper.CORPUS_CLASS,corp.getName(),secInfo,null);

    //2.create am entry in the T_COPRUS table
    Long corpusID = null;
    //DB stuff
    CallableStatement cstmt = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      if (this.dbType == DBHelper.ORACLE_DB) {
        cstmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+".persist.create_corpus(?,?) }");
        cstmt.setLong(1,lrID.longValue());
        cstmt.registerOutParameter(2,java.sql.Types.BIGINT);
        cstmt.execute();
        corpusID = new Long(cstmt.getLong(2));
      }
      else if (this.dbType == DBHelper.POSTGRES_DB) {
        pstmt = this.jdbcConn.prepareStatement("select persist_create_corpus(?) ");
        pstmt.setLong(1,lrID.longValue());
        pstmt.execute();
        rs = pstmt.getResultSet();

        if (false == rs.next()) {
          throw new PersistenceException("empty result set");
        }

        corpusID = new Long(rs.getLong(1));
      }
      else {
        Assert.fail();
      }
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't create corpus [step 2] in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(cstmt);
      DBHelper.cleanup(pstmt);
      DBHelper.cleanup(rs);
    }

    //3. for each document in the corpus call createDocument()
    Iterator itDocuments = corp.iterator();
    Vector dbDocs = new Vector();

    while (itDocuments.hasNext()) {
      Document doc = (Document)itDocuments.next();

      //3.1. ensure that the document is either transient or is from the ...
      // same DataStore
      if (doc.getLRPersistenceId() == null) {
        //transient document

        //now this is a bit ugly patch, the transaction related functionality
        //should not be in this method
        if (newTransPerDocument) {
          beginTrans();
        }

        //do call iterator::remove before the call to createDocument because
        //...there is a factory::deleteResource() call for the transient document there
        //...and the iterator gets confused
        itDocuments.remove();

        //create doc in database and return DB ddoc
        Document dbDoc = createDocument(doc,corpusID,secInfo);

        if (newTransPerDocument) {
          commitTrans();
        }

        dbDocs.add(dbDoc);
        //8. let the world know
        fireResourceAdopted(new DatastoreEvent(this,
                                                DatastoreEvent.RESOURCE_ADOPTED,
                                                dbDoc,
                                                dbDoc.getLRPersistenceId()
                                              )
                            );

        //9. fire also resource written event because it's now saved
        fireResourceWritten(new DatastoreEvent(this,
                                                DatastoreEvent.RESOURCE_WRITTEN,
                                                dbDoc,
                                                dbDoc.getLRPersistenceId()
                                              )
                           );

        //10.
        //DON'T make explicit Factory call, since createDocument called above
        ///...takes care to call Factory.deleteResource for the transient document
      }
      else if (doc.getDataStore().equals(this)) {
        //persistent doc from the same DataStore
        fireResourceAdopted(
            new DatastoreEvent(this, DatastoreEvent.RESOURCE_ADOPTED,
                               doc,
                               doc.getLRPersistenceId()));

        //6. fire also resource written event because it's now saved
        fireResourceWritten(
          new DatastoreEvent(this, DatastoreEvent.RESOURCE_WRITTEN,
                              doc,
                              doc.getLRPersistenceId()));
      }
      else {
        //persistent doc from other datastore
        //skip
        gate.util.Err.prln("document ["+doc.getLRPersistenceId()+"] is adopted from another "+
                            " datastore. Skipped.");
      }
    }

    //4. create features
    if (this.dbType == DBHelper.ORACLE_DB) {
      createFeaturesBulk(lrID,DBHelper.FEATURE_OWNER_CORPUS,corp.getFeatures());
    }
    else if (this.dbType == DBHelper.POSTGRES_DB) {
      createFeatures(lrID,DBHelper.FEATURE_OWNER_CORPUS,corp.getFeatures());
    }
    else {
      Assert.fail();
    }


    //5. create a DatabaseCorpusImpl and return it
///    Corpus dbCorpus = new DatabaseCorpusImpl(corp.getName(),
///                                             this,
///                                              lrID,
///                                              corp.getFeatures(),
///                                              dbDocs);
///

    Corpus dbCorpus = null;
    FeatureMap params = Factory.newFeatureMap();
    HashMap initData = new HashMap();

    initData.put("DS",this);
    initData.put("LR_ID",lrID);
    initData.put("CORP_NAME",corp.getName());
    initData.put("CORP_FEATURES",corp.getFeatures());
    initData.put("CORP_SUPPORT_LIST",dbDocs);

    params.put("initData__$$__", initData);

    try {
      //here we create the persistent LR via Factory, so it's registered
      //in GATE
      dbCorpus = (Corpus)Factory.createResource("gate.corpora.DatabaseCorpusImpl", params);
    }
    catch (gate.creole.ResourceInstantiationException ex) {
      throw new GateRuntimeException(ex.getMessage());
    }

    //6. done
    return dbCorpus;
  }

  /**
   * helper for adopt
   * creates a LR of type Document
   */
  protected Document createDocument(Document doc,SecurityInfo secInfo)
  throws PersistenceException,SecurityException {

    //delegate, set to Null
    return createDocument(doc,null,secInfo);
  }


  /**
   * helper for adopt
   * creates a LR of type Document
   */
  protected Document createDocument(Document doc, Long corpusID,SecurityInfo secInfo)
  throws PersistenceException,SecurityException {

    //-1. preconditions
    Assert.assertNotNull(doc);
    Assert.assertNotNull(secInfo);

    //0. check securoity settings
    if (false == this.ac.isValidSecurityInfo(secInfo)) {
      throw new SecurityException("Invalid security settings");
    }

    //1. get the data to be stored
    AnnotationSet defaultAnnotations = doc.getAnnotations();
    DocumentContent docContent = doc.getContent();
    FeatureMap docFeatures = doc.getFeatures();
    String docName  = doc.getName();
    URL docURL = doc.getSourceUrl();
    Boolean docIsMarkupAware = doc.getMarkupAware();
    Long docStartOffset = doc.getSourceUrlStartOffset();
    Long docEndOffset = doc.getSourceUrlEndOffset();
    String docEncoding = null;
    try {
      docEncoding = (String)doc.
        getParameterValue(Document.DOCUMENT_ENCODING_PARAMETER_NAME);
    }
    catch(gate.creole.ResourceInstantiationException re) {
      throw new PersistenceException("cannot create document: error getting " +
                                     " document encoding ["+re.getMessage()+"]");
    }


    //3. create a Language Resource (an entry in T_LANG_RESOURCE) for this document
    Long lrID = createLR(DBHelper.DOCUMENT_CLASS,docName,secInfo,null);

    //4. create a record in T_DOCUMENT for this document
    Long docID = createDoc(lrID,
                            docURL,
                            docEncoding,
                            docStartOffset,
                            docEndOffset,
                            docIsMarkupAware,
                            corpusID);


    //5. fill document content (record[s] in T_DOC_CONTENT)

    //do we have content at all?
    if (docContent.size().longValue() > 0) {
//      updateDocumentContent(docContentID,docContent);
      updateDocumentContent(docID,docContent);
    }

    //6. insert annotations, etc

    //6.1. create default annotation set
    createAnnotationSet(lrID,defaultAnnotations);

    //6.2. create named annotation sets
    Map namedAnns = doc.getNamedAnnotationSets();
    //the map may be null
    if (null != namedAnns) {
      Set setAnns = namedAnns.entrySet();
      Iterator itAnns = setAnns.iterator();

      while (itAnns.hasNext()) {
        Map.Entry mapEntry = (Map.Entry)itAnns.next();
        //String currAnnName = (String)mapEntry.getKey();
        AnnotationSet currAnnSet = (AnnotationSet)mapEntry.getValue();

        //create a-sets
        createAnnotationSet(lrID,currAnnSet);
      }
    }

    //7. create features
    if (this.dbType == DBHelper.ORACLE_DB) {
      createFeaturesBulk(lrID,DBHelper.FEATURE_OWNER_DOCUMENT,docFeatures);
    }
    else if (this.dbType == DBHelper.POSTGRES_DB) {
      createFeatures(lrID,DBHelper.FEATURE_OWNER_DOCUMENT,docFeatures);
    }
    else {
      Assert.fail();
    }


    //9. create a DatabaseDocument wrapper and return it

/*    Document dbDoc = new DatabaseDocumentImpl(this.jdbcConn,
                                              doc.getName(),
                                              this,
                                              lrID,
                                              doc.getContent(),
                                              doc.getFeatures(),
                                              doc.getMarkupAware(),
                                              doc.getSourceUrl(),
                                              doc.getSourceUrlStartOffset(),
                                              doc.getSourceUrlEndOffset(),
                                              doc.getAnnotations(),
                                              doc.getNamedAnnotationSets());
*/
    Document dbDoc = null;
    FeatureMap params = Factory.newFeatureMap();

    HashMap initData = new HashMap();
    initData.put("JDBC_CONN",this.jdbcConn);
    initData.put("DS",this);
    initData.put("LR_ID",lrID);
    initData.put("DOC_NAME",doc.getName());
    initData.put("DOC_CONTENT",doc.getContent());
    initData.put("DOC_FEATURES",doc.getFeatures());
    initData.put("DOC_MARKUP_AWARE",doc.getMarkupAware());
    initData.put("DOC_SOURCE_URL",doc.getSourceUrl());
    if(doc instanceof DocumentImpl){
      initData.put("DOC_STRING_CONTENT",
                   ((DocumentImpl)doc).getStringContent());
    }
    initData.put("DOC_SOURCE_URL_START",doc.getSourceUrlStartOffset());
    initData.put("DOC_SOURCE_URL_END",doc.getSourceUrlEndOffset());
    initData.put("DOC_DEFAULT_ANNOTATIONS",doc.getAnnotations());
    initData.put("DOC_NAMED_ANNOTATION_SETS",doc.getNamedAnnotationSets());

    params.put("initData__$$__", initData);

    try {
      //here we create the persistent LR via Factory, so it's registered
      //in GATE
      dbDoc = (Document)Factory.createResource("gate.corpora.DatabaseDocumentImpl", params);
    }
    catch (gate.creole.ResourceInstantiationException ex) {
      throw new GateRuntimeException(ex.getMessage());
    }

    //unload the transient document
System.out.println("unloading "+doc.getName() +"...");
    Factory.deleteResource(doc);

    return dbDoc;
  }

  protected abstract Long createLR(String lrType,
                          String lrName,
                          SecurityInfo si,
                          Long lrParentID)
    throws PersistenceException,SecurityException;


  protected abstract Long createDoc(Long _lrID,
                          URL _docURL,
                          String _docEncoding,
                          Long _docStartOffset,
                          Long _docEndOffset,
                          Boolean _docIsMarkupAware,
                          Long _corpusID)
    throws PersistenceException;

  protected abstract void updateDocumentContent(Long docID,DocumentContent content)
    throws PersistenceException;

  protected abstract void createAnnotationSet(Long lrID, AnnotationSet aset)
    throws PersistenceException;

  protected abstract void createFeaturesBulk(Long entityID, int entityType, FeatureMap features)
    throws PersistenceException;

  protected abstract void createFeatures(Long entityID, int entityType, FeatureMap features)
    throws PersistenceException;

  /**
   * Save: synchonise the in-memory image of the LR with the persistent
   * image.
   */
  protected void _sync(LanguageResource lr, boolean openNewTrans)
    throws PersistenceException,SecurityException {

    //0.preconditions
    Assert.assertNotNull(lr);
    Long lrID = (Long)lr.getLRPersistenceId();

    if (false == lr instanceof Document &&
        false == lr instanceof Corpus) {
      //only documents and corpuses could be serialized in DB
      throw new IllegalArgumentException("only Documents and Corpuses could "+
                                          "be serialized in DB");
    }

    // check that this LR is one of ours (i.e. has been adopted)
    if( null == lr.getDataStore() || false == lr.getDataStore().equals(this))
      throw new PersistenceException(
        "This LR is not stored in this DataStore"
      );


    //1. check session
    if (null == this.session) {
      throw new SecurityException("session not set");
    }

    if (false == this.ac.isValidSession(this.session)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. check permissions
    if (false == canWriteLR(lrID)) {
      throw new SecurityException("insufficient privileges");
    }

    //3. is the resource locked?
    User lockingUser = getLockingUser(lr);
    User currUser = this.session.getUser();

    if (lockingUser != null && false == lockingUser.equals(currUser)) {
      throw new PersistenceException("document is locked by another user and cannot be synced");
    }


    boolean transFailed = false;
    try {
      //2. autocommit should be FALSE because of LOBs
      if (openNewTrans) {
        beginTrans();
      }

      //3. perform changes, if anything goes wrong, rollback
      if (lr instanceof Document) {
        syncDocument((Document)lr);
      }
      else {
        syncCorpus((Corpus)lr);
      }

      //4. done, commit
      if (openNewTrans) {
        commitTrans();
      }
    }
    catch(PersistenceException pe) {
      transFailed = true;
      throw(pe);
    }
    finally {
      //problems?
      if (transFailed) {
        rollbackTrans();
      }
    }

    // let the world know about it
    fireResourceWritten(
      new DatastoreEvent(this, DatastoreEvent.RESOURCE_WRITTEN, lr, lr.getLRPersistenceId()));
  }

  /**
   * Releases the exlusive lock on a resource from the persistent store.
   */
  protected User getLockingUser(LanguageResource lr)
    throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(lr);
    Assert.assertTrue(lr instanceof DatabaseDocumentImpl ||
                      lr instanceof DatabaseCorpusImpl);
    Assert.assertNotNull(lr.getLRPersistenceId());
    Assert.assertEquals(lr.getDataStore(),this);

    //delegate
    return getLockingUser((Long)lr.getLRPersistenceId());
  }



  /**
   * Releases the exlusive lock on a resource from the persistent store.
   */
  protected User getLockingUser(Long lrID)
  throws PersistenceException,SecurityException {

    //1. check session
    if (null == this.session) {
      throw new SecurityException("session not set");
    }

    if (false == this.ac.isValidSession(this.session)) {
      throw new SecurityException("invalid session supplied");
    }

    //3. read from DB
    PreparedStatement pstmt = null;
    Long userID = null;
    ResultSet rs = null;

    try {

      String sql = null;

      if (this.dbType == DBHelper.ORACLE_DB) {
        sql = "   select  nvl(lr_locking_user_id,0) as user_id" +
              "   from "+this.dbSchema+"t_lang_resource " +
              "   where   lr_id = ?";
      }
      else if (this.dbType == DBHelper.POSTGRES_DB) {
        sql = "   select  coalesce(lr_locking_user_id,0) as user_id" +
              "   from t_lang_resource " +
              "   where   lr_id = ?";
      }
      else {
        throw new IllegalArgumentException();
      }

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,lrID.longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (false == rs.next()) {
        throw new PersistenceException("LR not found in DB");
      }

      long result = rs.getLong("user_id");

      return result == 0  ? null
                          : this.ac.findUser(new Long(result));
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't get locking user from DB : ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }
  }

  /** helper for sync() - saves a Corpus in the database */
  protected void syncCorpus(Corpus corp)
    throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(corp);
    Assert.assertTrue(corp instanceof DatabaseCorpusImpl);
    Assert.assertEquals(this,corp.getDataStore());
    Assert.assertNotNull(corp.getLRPersistenceId());

    EventAwareCorpus dbCorpus = (EventAwareCorpus)corp;

    //1. sync the corpus name?
    if (dbCorpus.isResourceChanged(EventAwareLanguageResource.RES_NAME)) {
      _syncLR(corp);
    }

    //2. sync the corpus features?
    if (dbCorpus.isResourceChanged(EventAwareLanguageResource.RES_FEATURES)) {
      _syncFeatures(corp);
    }

    //2.5 get removed documents and detach (not remove) them from the corpus in the
    //database
    List removedDocLRIDs = dbCorpus.getRemovedDocuments();
    if (removedDocLRIDs.size() > 0) {
      _syncRemovedDocumentsFromCorpus(removedDocLRIDs,(Long)corp.getLRPersistenceId());
    }

    //3. get all documents
    //--Iterator it = corp.iterator();
    Iterator it = dbCorpus.getLoadedDocuments().iterator();

    while (it.hasNext()) {
      Document dbDoc = (Document)it.next();
      //note - document may be NULL which means it was not loaded (load on demand)
      //just ignore it then
      if (null == dbDoc) {
        continue;
      }

      //adopt/sync?
      if (null == dbDoc.getLRPersistenceId()) {
        //doc was never adopted, adopt it

        //3.1 remove the transient doc from the corpus
        it.remove();

        //3.2 get the security info for the corpus
        SecurityInfo si = getSecurityInfo(corp);


        Document adoptedDoc = null;
        try {
          //3.3. adopt the doc with the sec info
//System.out.println("adopting ["+dbDoc.getName()+"] ...");
          //don't open a new transaction, since sync() already has opended one
          adoptedDoc = (Document)_adopt(dbDoc,si,true);

          //3.4. add doc to corpus in DB
          addDocumentToCorpus((Long)adoptedDoc.getLRPersistenceId(),
                              (Long)corp.getLRPersistenceId());
        }
        catch(SecurityException se) {
          throw new PersistenceException(se);
        }

        //3.5 add back to corpus the new DatabaseDocument
        corp.add(adoptedDoc);
      }
      else {
        //don't open a new transaction, the sync() called for corpus has already
        //opened one
        try {
          _sync(dbDoc,true);

          // let the world know about it
          fireResourceWritten( new DatastoreEvent(this,
                                                  DatastoreEvent.RESOURCE_WRITTEN,
                                                  dbDoc,
                                                  dbDoc.getLRPersistenceId()
                                                  )
                              );

          //if the document is form the same DS but did not belong to the corpus add it now
          //NOTE: if the document already belongs to the corpus then nothing will be changed
          //in the DB
          addDocumentToCorpus((Long)dbDoc.getLRPersistenceId(),
                              (Long)corp.getLRPersistenceId());
        }
        catch(SecurityException se) {
          gate.util.Err.prln("document cannot be synced: ["+se.getMessage()+"]");
        }
      }
    }
  }

  /** helper for sync() - saves a Document in the database */
  /** helper for sync() - saves a Document in the database */
  protected void syncDocument(Document doc)
    throws PersistenceException, SecurityException {

    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);
    Assert.assertTrue(doc.getLRPersistenceId() instanceof Long);

    Long lrID = (Long)doc.getLRPersistenceId();
    EventAwareLanguageResource dbDoc = (EventAwareLanguageResource)doc;
    //1. sync LR
    // only name can be changed here
    if (true == dbDoc.isResourceChanged(EventAwareLanguageResource.RES_NAME)) {
      _syncLR(doc);
    }

    //2. sync Document
    if (true == dbDoc.isResourceChanged(EventAwareLanguageResource.DOC_MAIN)) {
      _syncDocumentHeader(doc);
    }

    //3. [optional] sync Content
    if (true == dbDoc.isResourceChanged(EventAwareLanguageResource.DOC_CONTENT)) {
      _syncDocumentContent(doc);
    }

    //4. [optional] sync Features
    if (true == dbDoc.isResourceChanged(EventAwareLanguageResource.RES_FEATURES)) {
      _syncFeatures(doc);
    }

    //5. [optional] delete from DB named sets that were removed from the document
    Collection removedSets = ((EventAwareDocument)dbDoc).getRemovedAnnotationSets();
    Collection addedSets = ((EventAwareDocument)dbDoc).getAddedAnnotationSets();
    if (false == removedSets.isEmpty() || false == addedSets.isEmpty()) {
      _syncAnnotationSets(doc,removedSets,addedSets);
    }

    //6. [optional] sync Annotations
    _syncAnnotations(doc);
  }


  /**
   *  helper for sync()
   *  NEVER call directly
   */
  protected abstract void _syncLR(LanguageResource lr)
    throws PersistenceException,SecurityException;

  /** helper for sync() - never call directly */
  protected abstract void _syncDocumentHeader(Document doc)
    throws PersistenceException;

  /** helper for sync() - never call directly */
  protected abstract void _syncDocumentContent(Document doc)
    throws PersistenceException;

  /** helper for sync() - never call directly */
  protected abstract void _syncFeatures(LanguageResource lr)
    throws PersistenceException;

  /** helper for sync() - never call directly */
  protected void _syncAnnotationSets(Document doc,Collection removedSets,Collection addedSets)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(doc);
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);
    Assert.assertNotNull(doc.getLRPersistenceId());
    Assert.assertEquals(((DatabaseDataStore)doc.getDataStore()).getDatabaseID(),
                      this.getDatabaseID());
    Assert.assertNotNull(removedSets);
    Assert.assertNotNull(addedSets);

    Long lrID = (Long)doc.getLRPersistenceId();

    //1. delete from DB removed a-sets
    PreparedStatement stmt = null;

    try {

      if (this.dbType == DBHelper.ORACLE_DB) {
        stmt = this.jdbcConn.prepareCall("{ call "+this.dbSchema+"persist.delete_annotation_set(?,?) }");
      }
      else if (this.dbType == DBHelper.POSTGRES_DB) {
        stmt = this.jdbcConn.prepareStatement("select persist_delete_annotation_set(?,?)");
      }
      else {
        Assert.fail();
      }

      Iterator it = removedSets.iterator();
      while (it.hasNext()) {
        String setName = (String)it.next();
        stmt.setLong(1,lrID.longValue());
        stmt.setString(2,setName);
        stmt.execute();
      }
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't remove annotation set from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

    //2. create in DB new a-sets
    Iterator it = addedSets.iterator();
    while (it.hasNext()) {
      String setName = (String)it.next();
      AnnotationSet aset = doc.getAnnotations(setName);

      Assert.assertNotNull(aset);
      Assert.assertTrue(aset instanceof DatabaseAnnotationSetImpl);

      createAnnotationSet(lrID,aset);
    }
  }


  /** helper for sync() - never call directly */
  protected void _syncAnnotations(Document doc)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(doc);
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);
    Assert.assertNotNull(doc.getLRPersistenceId());
    Assert.assertEquals(((DatabaseDataStore)doc.getDataStore()).getDatabaseID(),
                      this.getDatabaseID());


    EventAwareDocument ead = (EventAwareDocument)doc;
    //1. get the sets read from the DB for this document
    //chnaged annotations can occur only in such sets
    Collection loadedSets = ead.getLoadedAnnotationSets();

    Iterator it = loadedSets.iterator();
    while (it.hasNext()) {
      AnnotationSet as = (AnnotationSet)it.next();
      //check that this set is neither NEW nor DELETED
      //they should be already synced
      if (ead.getAddedAnnotationSets().contains(as.getName()) ||
          ead.getRemovedAnnotationSets().contains(as.getName())) {
        //oops, ignore it
        continue;
      }

      EventAwareAnnotationSet eas = (EventAwareAnnotationSet)as;
      Assert.assertNotNull(as);

      Collection anns = null;
      anns = eas.getAddedAnnotations();
      Assert.assertNotNull(anns);
      if (anns.size()>0) {
        _syncAddedAnnotations(doc,as,anns);
      }

      anns = eas.getRemovedAnnotations();
      Assert.assertNotNull(anns);
      if (anns.size()>0) {
        _syncRemovedAnnotations(doc,as,anns);
      }

      anns = eas.getChangedAnnotations();
      Assert.assertNotNull(anns);
      if (anns.size()>0) {
        _syncChangedAnnotations(doc,as,anns);
      }
    }
  }

  /** helper for sync() - never call directly */
  protected void _syncAddedAnnotations(Document doc, AnnotationSet as, Collection changes)
    throws PersistenceException {

    //0.preconditions
    Assert.assertNotNull(doc);
    Assert.assertNotNull(as);
    Assert.assertNotNull(changes);
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);
    Assert.assertTrue(as instanceof DatabaseAnnotationSetImpl);
    Assert.assertTrue(changes.size() > 0);


    PreparedStatement pstmt = null;
    ResultSet rs = null;
    CallableStatement cstmt = null;
    Long lrID = (Long)doc.getLRPersistenceId();
    Long asetID = null;

    try {
      //1. get the a-set ID in the database
      String sql = " select as_id  " +
                   " from  "+this.dbSchema+"v_annotation_set " +
                   " where  lr_id = ? ";
      //do we have aset name?
      String clause = null;
      String name = as.getName();
      if (null != name) {
        clause =   "        and as_name = ? ";
      }
      else {
        clause =   "        and as_name is null ";
      }
      sql = sql + clause;

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,lrID.longValue());
      if (null != name) {
        pstmt.setString(2,name);
      }
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (rs.next()) {
        asetID = new Long(rs.getLong("as_id"));
      }
      else {
        throw new PersistenceException("cannot find annotation set with" +
                                      " name=["+name+"] , LRID=["+lrID+"] in database");
      }

      //cleanup
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);

      //3. insert the new annotations from this set

      //3.1. prepare call
      if (this.dbType == DBHelper.ORACLE_DB) {

        cstmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".persist.create_annotation(?,?,?,?,?,?,?,?,?) }");

        Long annGlobalID = null;
        Iterator it = changes.iterator();

        while (it.hasNext()) {

          //3.2. insert annotation
          Annotation ann = (Annotation)it.next();

          Node start = (Node)ann.getStartNode();
          Node end = (Node)ann.getEndNode();
          String type = ann.getType();

          cstmt.setLong(1,lrID.longValue());
          cstmt.setLong(2,ann.getId().longValue());
          cstmt.setLong(3,asetID.longValue());
          cstmt.setLong(4,start.getId().longValue());
          cstmt.setLong(5,start.getOffset().longValue());
          cstmt.setLong(6,end.getId().longValue());
          cstmt.setLong(7,end.getOffset().longValue());
          cstmt.setString(8,type);
          cstmt.registerOutParameter(9,java.sql.Types.BIGINT);

          cstmt.execute();
          annGlobalID = new Long(cstmt.getLong(9));

          //3.3. set annotation features
          FeatureMap features = ann.getFeatures();
          Assert.assertNotNull(features);

          if (this.dbType == DBHelper.ORACLE_DB) {
            createFeaturesBulk(annGlobalID,DBHelper.FEATURE_OWNER_ANNOTATION,features);
          }
          else if (this.dbType == DBHelper.POSTGRES_DB) {
            createFeatures(annGlobalID,DBHelper.FEATURE_OWNER_ANNOTATION,features);
          }
          else {
            Assert.fail();
          }
        }
      }
      else if (this.dbType == DBHelper.POSTGRES_DB) {

        sql = "select persist_create_annotation(?,?,?,?,?,?,?,?)";
        pstmt = this.jdbcConn.prepareStatement(sql);

        Long annGlobalID = null;
        Iterator it = changes.iterator();

        while (it.hasNext()) {

          //3.2. insert annotation
          Annotation ann = (Annotation)it.next();

          Node start = (Node)ann.getStartNode();
          Node end = (Node)ann.getEndNode();
          String type = ann.getType();

          pstmt.setLong(1,lrID.longValue());
          pstmt.setLong(2,ann.getId().longValue());
          pstmt.setLong(3,asetID.longValue());
          pstmt.setLong(4,start.getId().longValue());
          pstmt.setLong(5,start.getOffset().longValue());
          pstmt.setLong(6,end.getId().longValue());
          pstmt.setLong(7,end.getOffset().longValue());
          pstmt.setString(8,type);
          pstmt.execute();

          rs = pstmt.getResultSet();

          if (false == rs.next()) {
            throw new PersistenceException("empty result set");
          }
          annGlobalID = new Long(rs.getLong(1));

          //3.3. set annotation features
          FeatureMap features = ann.getFeatures();
          Assert.assertNotNull(features);
          createFeatures(annGlobalID,DBHelper.FEATURE_OWNER_ANNOTATION,features);
        }
      }

      else {
        throw new IllegalArgumentException();
      }

    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't add annotations in DB : ["+
                                      sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
      DBHelper.cleanup(cstmt);
    }
  }

  /** helper for sync() - never call directly */
  protected void _syncRemovedAnnotations(Document doc,AnnotationSet as, Collection changes)
    throws PersistenceException {

    //0.preconditions
    Assert.assertNotNull(doc);
    Assert.assertNotNull(as);
    Assert.assertNotNull(changes);
    Assert.assertTrue(doc instanceof DatabaseDocumentImpl);
    Assert.assertTrue(as instanceof DatabaseAnnotationSetImpl);
    Assert.assertTrue(changes.size() > 0);


    PreparedStatement pstmt = null;
    ResultSet rs = null;
    Long lrID = (Long)doc.getLRPersistenceId();
    Long docID = null;
    Long asetID = null;

    try {
      //1. get the a-set ID in the database
      String sql = " select as_id,  " +
                   "        as_doc_id " +
                   " from  "+this.dbSchema+"v_annotation_set " +
                   " where  lr_id = ? ";
      //do we have aset name?
      String clause = null;
      String name = as.getName();
      if (null != name) {
        clause =   "        and as_name = ? ";
      }
      else {
        clause =   "        and as_name is null ";
      }
      sql = sql + clause;

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,lrID.longValue());
      if (null != name) {
        pstmt.setString(2,name);
      }
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (rs.next()) {
        asetID = new Long(rs.getLong("as_id"));
        docID = new Long(rs.getLong("as_doc_id"));
      }
      else {
        throw new PersistenceException("cannot find annotation set with" +
                                      " name=["+name+"] , LRID=["+lrID+"] in database");
      }

      //3. delete the removed annotations from this set

      //cleanup
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);

      //3.1. prepare call

      if (this.dbType == DBHelper.ORACLE_DB) {
        pstmt = this.jdbcConn.prepareCall("{ call "+this.dbSchema+"persist.delete_annotation(?,?) }");
      }
      else if (this.dbType == DBHelper.POSTGRES_DB) {
        pstmt = this.jdbcConn.prepareStatement("select persist_delete_annotation(?,?)");
      }
      else {
        throw new IllegalArgumentException();
      }

      Iterator it = changes.iterator();

      while (it.hasNext()) {

        //3.2. insert annotation
        Annotation ann = (Annotation)it.next();

        pstmt.setLong(1,docID.longValue()); //annotations are linked with documents, not LRs!
        pstmt.setLong(2,ann.getId().longValue());
        pstmt.execute();
      }
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't delete annotations in DB : ["+
                                      sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }
  }


  /** helper for sync() - never call directly */
  protected void _syncChangedAnnotations(Document doc,AnnotationSet as, Collection changes)
    throws PersistenceException {

    //technically this approach sux
    //at least it works

    //1. delete
    _syncRemovedAnnotations(doc,as,changes);
    //2. recreate
    _syncAddedAnnotations(doc,as,changes);
  }

  /**
   * Get a resource from the persistent store.
   * <B>Don't use this method - use Factory.createResource with
   * DataStore and DataStoreInstanceId parameters set instead.</B>
   */
  public LanguageResource getLr(String lrClassName, Object lrPersistenceId)
  throws PersistenceException,SecurityException {

    LanguageResource result = null;

    //0. preconditions
    Assert.assertNotNull(lrPersistenceId);

    //1. check session
    if (null == this.session) {
      throw new SecurityException("session not set");
    }

    if (false == this.ac.isValidSession(this.session)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. check permissions
    if (false == canReadLR(lrPersistenceId)) {
      throw new SecurityException("insufficient privileges");
    }

    //3. get resource from DB
    if (lrClassName.equals(DBHelper.DOCUMENT_CLASS)) {
      result = readDocument(lrPersistenceId);
      Assert.assertTrue(result instanceof DatabaseDocumentImpl);
    }
    else if (lrClassName.equals(DBHelper.CORPUS_CLASS)) {
      result = readCorpus(lrPersistenceId);
      Assert.assertTrue(result instanceof DatabaseCorpusImpl);
    }
    else {
      throw new IllegalArgumentException("resource class should be either Document or Corpus");
    }

    //4. postconditions
    Assert.assertNotNull(result.getDataStore());
    Assert.assertTrue(result.getDataStore() instanceof DatabaseDataStore);
    Assert.assertNotNull(result.getLRPersistenceId());

    //5. register the read doc as listener for sync events
    addDatastoreListener((DatastoreListener)result);

    //6. add the resource to the list of dependent resources - i.e. the ones that the
    //data store should take care upon closing [and call sync()]
    this.dependentResources.add(result);

    //7. done
    return result;
  }

  /** helper method for getLR - reads LR of type Document */
  private DatabaseDocumentImpl readDocument(Object lrPersistenceId)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(lrPersistenceId);

    if (false == lrPersistenceId instanceof Long) {
      throw new IllegalArgumentException();
    }

    // 1. dummy document to be initialized
    DatabaseDocumentImpl result = new DatabaseDocumentImpl(this.jdbcConn);

    PreparedStatement pstmt = null;
    ResultSet rs = null;

    //3. read from DB
    try {
      String sql = " select lr_name, " +
                   "        lrtp_type, " +
                   "        lr_id, " +
                   "        lr_parent_id, " +
                   "        doc_id, " +
                   "        doc_url, " +
                   "        doc_start, " +
                   "        doc_end, " +
                   "        doc_is_markup_aware " +
                   " from  "+this.dbSchema+"v_document " +
                   " where  lr_id = ? ";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)lrPersistenceId).longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (false == rs.next()) {
        //ooops mo data found
        throw new PersistenceException("Invalid LR ID supplied - no data found");
      }

      //4. fill data

      //4.0 name
      String lrName = rs.getString("lr_name");
      Assert.assertNotNull(lrName);
      result.setName(lrName);

      //4.1 parent
      Long parentID = null;
      long parent_id = rs.getLong("lr_parent_id");
      if (false == rs.wasNull()) {
        parentID = new Long(parent_id);

        //read parent resource
        LanguageResource parentLR = this.getLr(DBHelper.DOCUMENT_CLASS,parentID);
        Assert.assertNotNull(parentLR);
        Assert.assertTrue(parentLR instanceof DatabaseDocumentImpl);

        result.setParent(parentLR);
      }


      //4.2. markup aware
      if (this.dbType == DBHelper.ORACLE_DB) {
        long markup = rs.getLong("doc_is_markup_aware");
        Assert.assertTrue(markup == DBHelper.FALSE || markup == DBHelper.TRUE);
        if (markup == DBHelper.FALSE) {
          result.setMarkupAware(Boolean.FALSE);
        }
        else {
          result.setMarkupAware(Boolean.TRUE);

        }
      }
      else if (this.dbType == DBHelper.POSTGRES_DB) {
        boolean markup = rs.getBoolean("doc_is_markup_aware");
        result.setMarkupAware(new Boolean(markup));
      }
      else {
        throw new IllegalArgumentException();
      }


      //4.3 datastore
      result.setDataStore(this);

      //4.4. persist ID
      Long persistID = new Long(rs.getLong("lr_id"));
      result.setLRPersistenceId(persistID);

      //4.5  source url
      String url = rs.getString("doc_url");
      if(url != null && url.length() > 0) result.setSourceUrl(new URL(url));

      //4.6. start offset
      Long start = null;
      long longVal = rs.getLong("doc_start");
      //null?
      //if NULL is stored in the DB, Oracle returns 0 which is not what we want
      if (false == rs.wasNull()) {
        start = new Long(longVal);
      }
      result.setSourceUrlStartOffset(start);
//      initData.put("DOC_SOURCE_URL_START",start);

      //4.7. end offset
      Long end = null;
      longVal = rs.getLong("doc_end");
      //null?
      //if NULL is stored in the DB, Oracle returns 0 which is not what we want
      if (false == rs.wasNull()) {
        end = new Long(longVal);
      }
      result.setSourceUrlEndOffset(end);
//      initData.put("DOC_SOURCE_URL_END",end);

      //4.8 features
      FeatureMap features = readFeatures((Long)lrPersistenceId,DBHelper.FEATURE_OWNER_DOCUMENT);
      result.setFeatures(features);
      //initData.put("DOC_FEATURES",features);

      //4.9 set the nextAnnotationID correctly
      long doc_id = rs.getLong("doc_id");

      //cleanup
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);

      sql = " select  max(ann_local_id),'ann_id'" +
            " from "+this.dbSchema+"t_annotation " +
            " where ann_doc_id = ?" +
            " union " +
            " select max(node_local_id),'node_id' " +
            " from "+this.dbSchema+"t_node " +
            " where node_doc_id = ?";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,doc_id);
      pstmt.setLong(2,doc_id);
      pstmt.execute();
      rs = pstmt.getResultSet();

      int maxAnnID = 0 , maxNodeID = 0;
      //ann id
      if (false == rs.next()) {
        //ooops no data found
        throw new PersistenceException("Invalid LR ID supplied - no data found");
      }
      if (rs.getString(2).equals("ann_id"))
        maxAnnID = rs.getInt(1);
      else
        maxNodeID = rs.getInt(1);

      if (false == rs.next()) {
        //ooops no data found
        throw new PersistenceException("Invalid LR ID supplied - no data found");
      }
      if (rs.getString(2).equals("node_id"))
        maxNodeID = rs.getInt(1);
      else
        maxAnnID = rs.getInt(1);

      result.setNextNodeId(maxNodeID+1);
//      initData.put("DOC_NEXT_NODE_ID",new Integer(maxNodeID+1));
      result.setNextAnnotationId(maxAnnID+1);
//      initData.put("DOC_NEXT_ANN_ID",new Integer(maxAnnID+1));


//      params.put("initData__$$__", initData);
//      try {
        //here we create the persistent LR via Factory, so it's registered
        //in GATE
//        result = (DatabaseDocumentImpl)Factory.createResource("gate.corpora.DatabaseDocumentImpl", params);
//      }
//      catch (gate.creole.ResourceInstantiationException ex) {
//        throw new GateRuntimeException(ex.getMessage());
//      }
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't read LR from DB: ["+ sqle.getMessage()+"]");
    }
    catch(Exception e) {
      throw new PersistenceException(e);
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }

    return result;
  }


  /**
   *  helper method for getLR - reads LR of type Corpus
   */
  private DatabaseCorpusImpl readCorpus(Object lrPersistenceId)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(lrPersistenceId);

    if (false == lrPersistenceId instanceof Long) {
      throw new IllegalArgumentException();
    }

    //3. read from DB
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    DatabaseCorpusImpl result = null;

    try {
      String sql = " select lr_name " +
                   " from  "+this.dbSchema+"t_lang_resource " +
                   " where  lr_id = ? ";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)lrPersistenceId).longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (false == rs.next()) {
        //ooops mo data found
        throw new PersistenceException("Invalid LR ID supplied - no data found");
      }

      //4. fill data

      //4.1 name
      String lrName = rs.getString("lr_name");
      Assert.assertNotNull(lrName);

      //4.8 features
      FeatureMap features = readFeatures((Long)lrPersistenceId,DBHelper.FEATURE_OWNER_CORPUS);

      //4.9 cleanup
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);

      sql = " select lr_id ," +
            "         lr_name " +
            " from "+this.dbSchema+"t_document        doc, " +
            "      "+this.dbSchema+"t_lang_resource   lr, " +
            "      "+this.dbSchema+"t_corpus_document corpdoc, " +
            "      "+this.dbSchema+"t_corpus          corp " +
            " where lr.lr_id = doc.doc_lr_id " +
            "       and doc.doc_id = corpdoc.cd_doc_id " +
            "       and corpdoc.cd_corp_id = corp.corp_id " +
            "       and corp_lr_id = ? ";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)lrPersistenceId).longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      Vector documentData = new Vector();
      while (rs.next()) {
        Long docLRID = new Long(rs.getLong("lr_id"));
        String docName = rs.getString("lr_name");
        documentData.add(new DocumentData(docName, docLRID));
      }
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);

      result = new DatabaseCorpusImpl(lrName,
                                      this,
                                      (Long)lrPersistenceId,
                                      features,
                                      documentData);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't read LR from DB: ["+ sqle.getMessage()+"]");
    }
    catch(Exception e) {
      throw new PersistenceException(e);
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }

    return result;
  }

  /**
   *  reads the features of an entity
   *  entities are of type LR or Annotation
   */
  protected abstract FeatureMap readFeatures(Long entityID, int entityType)
    throws PersistenceException;

  /**
   *  helper method for delete()
   *  never call it directly beause proper events will not be fired
   */
  protected abstract void deleteDocument(Long lrId)
    throws PersistenceException;

  /**
   *  helper method for delete()
   *  never call it directly beause proper events will not be fired
   */
  protected abstract void deleteCorpus(Long lrId)
    throws PersistenceException;

  /**
   *   unloads a LR from the GUI
   */
  protected void unloadLR(Long lrID)
  throws GateException{

    //0. preconfitions
    Assert.assertNotNull(lrID);

    //1. get all LRs in the system
    List resources = Gate.getCreoleRegister().getAllInstances("gate.LanguageResource");

    Iterator it = resources.iterator();
    while (it.hasNext()) {
      LanguageResource lr = (LanguageResource)it.next();
      if (lrID.equals(lr.getLRPersistenceId()) &&
          this.equals(lr.getDataStore())) {
        //found it - unload it
        Factory.deleteResource(lr);
        break;
      }
    }
  }

  /** helper for sync() - never call directly */
  protected abstract void _syncRemovedDocumentsFromCorpus(List docLRIDs, Long corpLRID)
    throws PersistenceException;

  /**
   *   adds document to corpus in the database
   *   if the document is already part of the corpus nothing
   *   changes
   */
  protected abstract void addDocumentToCorpus(Long docID,Long corpID)
  throws PersistenceException,SecurityException;


}
