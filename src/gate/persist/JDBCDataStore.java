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
  public abstract void delete(String lrClassName, Object lrId)
    throws PersistenceException,SecurityException;


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

  protected abstract Corpus createCorpus(Corpus corp,SecurityInfo secInfo, boolean newTransPerDocument)
    throws PersistenceException,SecurityException;

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
//    createFeatures(lrID,DBHelper.FEATURE_OWNER_DOCUMENT,docFeatures);
    createFeaturesBulk(lrID,DBHelper.FEATURE_OWNER_DOCUMENT,docFeatures);

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
  protected abstract void syncCorpus(Corpus corp)
    throws PersistenceException,SecurityException;

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
  protected abstract void _syncAnnotationSets(Document doc,Collection removedSets,Collection addedSets)
    throws PersistenceException;

  /** helper for sync() - never call directly */
  protected abstract void _syncAnnotations(Document doc)
    throws PersistenceException;

}
