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
import java.io.*;

import oracle.sql.CLOB;
import oracle.sql.BLOB;

import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.event.*;
import gate.security.*;
import gate.security.SecurityException; //hide the more general exception
import gate.corpora.*;

public class OracleDataStore extends JDBCDataStore {

  private static final int ORACLE_TRUE = 1;
  private static final int ORACLE_FALSE = 0;

  private static final int READ_ACCESS = 0;
  private static final int WRITE_ACCESS = 1;

  private static final int ORACLE_VARCHAR_LIMIT_BYTES = 4000;
  private static final int UTF_BYTES_PER_CHAR_MAX = 3;
  private static final int ORACLE_VARCHAR_MAX_SYMBOLS =
                                  ORACLE_VARCHAR_LIMIT_BYTES/UTF_BYTES_PER_CHAR_MAX;

  private static final int INTERNAL_BUFFER_SIZE = 16*1024;

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
      stmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+".persist.get_lr_name(?,?) }");
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
  public void setStorageUrl(String storageUrl) throws PersistenceException {

    super.setStorageUrl(storageUrl);

  }

  /** Get the URL for the underlying storage mechanism. */
  public String getStorageUrl() {

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
      stmt = this.jdbcConn.prepareCall(
                      "{ call "+Gate.DB_OWNER+".persist.delete_lr(?,?) }");
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
  throws UnsupportedOperationException,PersistenceException {

    super.setAutoSaving(autoSaving);
  }

  /** Get the autosaving behaviour of the LR. */
  public boolean isAutoSaving() {
    throw new MethodNotImplementedException();
  }

  /** Adopt a resource for persistence. */
  public LanguageResource adopt(LanguageResource lr,SecurityInfo secInfo)
  throws PersistenceException,SecurityException {

    LanguageResource result = null;

    //-1. preconditions
    Assert.assertNotNull(lr);
    Assert.assertNotNull(secInfo);

    //0. check SecurityInfo
    if (false == this.ac.isValidSecurityInfo(secInfo)) {
      throw new SecurityException("Invalid security settings supplied");
    }

    //0.5 check the LR's current DS
    DataStore currentDS = lr.getDataStore();
    if(currentDS == this){         // adopted already
      return lr;
    }
    else if(currentDS == null) {  // an orphan - do the adoption
      lr.setDataStore(this);
      // let the world know
      //fireResourceAdopted(
      //    new DatastoreEvent(this, DatastoreEvent.RESOURCE_ADOPTED, lr, null)
      //);
    }
    else {                      // someone else's child
      throw new PersistenceException(
        "Can't adopt a resource which is already in a different datastore");
    }


    //1. is the LR one of Document or Corpus?
    if (false == lr instanceof Document &&
        false == lr instanceof Corpus) {

      throw new IllegalArgumentException("Database datastore is implemented only for "+
                                        "Documents and Corpora");
    }

    //2.is the document already stored in this storage?
    Object persistID = lr.getLRPersistenceId();
    if (persistID != null) {
      throw new PersistenceException("This LR is already stored in the " +
                                      " database (persistance ID is =["+(Long)persistID+"] )");
    }

    //3. perform changes
    //autocommit should be disabled because of LOBs
    this.beginTrans();

    if (lr instanceof Document) {
      result =  createDocument((Document)lr,secInfo);
    }
    else {
      result =  createCorpus((Corpus)lr,secInfo);
    }

    //4. commit
    this.commitTrans();

    return result;
  }


  /** -- */
  protected Long createLR(String lrType,
                        String lrName,
                        SecurityInfo si,
                        Long lrParentID)
  throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(lrName);

    //1. check the session
//    if (this.ac.isValidSession(s) == false) {
//      throw new SecurityException("invalid session provided");
//    }

    //2. create a record in DB
    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                    "{ call "+Gate.DB_OWNER+".persist.create_lr(?,?,?,?,?,?,?) }");
      stmt.setLong(1,si.getUser().getID().longValue());
      stmt.setLong(2,si.getGroup().getID().longValue());
      stmt.setString(3,lrType);
      stmt.setString(4,lrName);
      stmt.setInt(5,si.getAccessMode());
      if (null == lrParentID) {
        stmt.setNull(6,java.sql.Types.BIGINT);
      }
      else {
        stmt.setLong(6,lrParentID.longValue());
      }
      //Oracle numbers are BIGNINT
      stmt.registerOutParameter(7,java.sql.Types.BIGINT);
      stmt.execute();

      Long result =  new Long(stmt.getLong(7));
      return result;
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {
        case DBHelper.X_ORACLE_INVALID_LR_TYPE:
          throw new PersistenceException("can't create LR [step 3] in DB, invalid LR Type");
        default:
          throw new PersistenceException(
                "can't create LR [step 3] in DB : ["+ sqle.getMessage()+"]");
      }
    }
    finally {
      DBHelper.cleanup(stmt);
    }
  }


  /** -- */
  protected void updateDocumentContent(Long docContentID,DocumentContent content)
  throws PersistenceException {

    //1. get LOB locators from DB
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    CallableStatement cstmt = null;
    try {
      String sql =  "select dc_content_type, " +
                    "       dc_character_content, " +
                    "       dc_binary_content " +
                    "from "+gate.Gate.DB_OWNER+".t_doc_content " +
                    "where  dc_id = ? " +
                    "for update ";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,docContentID.longValue());
      rs = pstmt.executeQuery();

      //rs = pstmt.getResultSet();

      rs.next();
      //important: read the objects in the order they appear in
      //the ResultSet, otherwise data may be lost
      long contentType = rs.getLong("DC_CONTENT_TYPE");
      Clob clob = (Clob)rs.getClob("dc_character_content");
      Blob blob = (Blob)rs.getBlob("dc_binary_content");

      Assert.assert(contentType == DBHelper.CHARACTER_CONTENT ||
                    contentType == DBHelper.BINARY_CONTENT ||
                    contentType == DBHelper.EMPTY_CONTENT);


      //2. write data using the LOB locators
      //NOTE: so far only character content is supported
      writeCLOB(content.toString(),clob);
      long newContentType = DBHelper.CHARACTER_CONTENT;

      //3. update content type
      cstmt = this.jdbcConn.prepareCall("{ call "+Gate.DB_OWNER+".persist.change_content_type(?,?) }");
      cstmt.setLong(1,docContentID.longValue());
      cstmt.setLong(2,newContentType);
      cstmt.execute();
    }
    catch(IOException ioe) {
      throw new PersistenceException("can't update document content in DB : ["+
                                      ioe.getMessage()+"]");
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't update document content in DB : ["+
                                      sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
      DBHelper.cleanup(cstmt);
    }

  }


  /** -- */
  protected LanguageResource createDocument(Document doc,SecurityInfo secInfo)
  throws PersistenceException,SecurityException {

    //delegate, set to Null
    return createDocument(doc,null,secInfo);
  }


  /** -- */
  protected LanguageResource createDocument(Document doc, Long corpusID,SecurityInfo secInfo)
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
      docEncoding = (String)doc.getParameterValue("encoding");
      Assert.assertNotNull(docEncoding);
    }
    catch(gate.creole.ResourceInstantiationException re) {
      throw new PersistenceException("cannot create document: error getting " +
                                     " document encoding ["+re.getMessage()+"]");
    }


    //3. create a Language Resource (an entry in T_LANG_RESOURCE) for this document
    Long lrID = createLR(DBHelper.DOCUMENT_CLASS,docName,secInfo,null);

    //4. create a record in T_DOCUMENT for this document
    CallableStatement stmt = null;
    Long docID = null;
    Long docContentID = null;

    try {
      stmt = this.jdbcConn.prepareCall(
          "{ call "+Gate.DB_OWNER+".persist.create_document(?,?,?,?,?,?,?,?,?) }");
      stmt.setLong(1,lrID.longValue());
      stmt.setString(2,docURL.toString());
      stmt.setString(3,docEncoding);
      stmt.setLong(4,(null==docStartOffset)? 0 : docStartOffset.longValue());
      stmt.setLong(5,(null==docEndOffset)? 0 : docEndOffset.longValue());
      stmt.setBoolean(6,docIsMarkupAware.booleanValue());
      //is the document part of a corpus?
      if (null == corpusID) {
        stmt.setNull(7,java.sql.Types.BIGINT);
      }
      else {
        stmt.setLong(7,corpusID.longValue());
      }

      //results
      stmt.registerOutParameter(8,java.sql.Types.BIGINT);
      stmt.registerOutParameter(9,java.sql.Types.BIGINT);

      stmt.execute();
      docID = new Long(stmt.getLong(8));
      docContentID = new Long(stmt.getLong(9));
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't create document [step 4] in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

    //5. fill document content (record[s] in T_DOC_CONTENT)

    //do we have content at all?
//Out.prln("SIZE=["+docContent.size()+"]");
    if (docContent.size().longValue() > 0) {
      updateDocumentContent(docContentID,docContent);
    }

    //6. insert annotations, etc

    //6.1. create default annotation set
    createAnnotationSet(docID,defaultAnnotations);

    //6.2. create named annotation sets
    Map namedAnns = doc.getNamedAnnotationSets();
    Set setAnns = namedAnns.entrySet();
    Iterator itAnns = setAnns.iterator();

    while (itAnns.hasNext()) {
      Map.Entry mapEntry = (Map.Entry)itAnns.next();
      //String currAnnName = (String)mapEntry.getKey();
      AnnotationSet currAnnSet = (AnnotationSet)mapEntry.getValue();

      //create a-sets
      createAnnotationSet(docID,currAnnSet);
    }

    //7. create features
    createFeatures(docID,DBHelper.FEATURE_OWNER_DOCUMENT,docFeatures);

    //8. done
    return doc;
  }



  /** -- */
  protected void createAnnotationSet(Long docID, AnnotationSet aset)
    throws PersistenceException {

    //1. create a-set
    String asetName = aset.getName();
    Long asetID = null;

    //DB stuff
    CallableStatement stmt = null;
      try {
        stmt = this.jdbcConn.prepareCall(
            "{ call "+Gate.DB_OWNER+".persist.create_annotation_set(?,?,?) }");
        stmt.setLong(1,docID.longValue());

        if (null == asetName) {
          stmt.setNull(2,java.sql.Types.VARCHAR);
        }
        else {
          stmt.setString(2,asetName);
        }
        stmt.registerOutParameter(3,java.sql.Types.BIGINT);
        stmt.execute();

        asetID = new Long(stmt.getLong(3));
      }
      catch(SQLException sqle) {
        throw new PersistenceException("can't create a-set [step 1] in DB: ["+ sqle.getMessage()+"]");
      }
      finally {
        DBHelper.cleanup(stmt);
      }


    //2. insert annotations/nodes for DEFAULT a-set
    //for now use a stupid cycle
    //TODO: pass all the data with one DB call (?)
    Iterator itAnnotations = aset.iterator();
    while (itAnnotations.hasNext()) {
      Annotation ann = (Annotation)itAnnotations.next();
      Node start = (Node)ann.getStartNode();
      Node end = (Node)ann.getEndNode();
      String type = ann.getType();
      FeatureMap annFeatures = ann.getFeatures();

      //DB stuff
      Long annID = null;
      try {
        stmt = this.jdbcConn.prepareCall(
            "{ call "+Gate.DB_OWNER+".persist.create_annotation(?,?,?,?,?,?) }");
        stmt.setLong(1,docID.longValue());
        stmt.setLong(2,asetID.longValue());
        stmt.setLong(3,start.getOffset().longValue());
        stmt.setLong(4,end.getOffset().longValue());
        stmt.setString(5,type);
        stmt.registerOutParameter(6,java.sql.Types.BIGINT);

        stmt.execute();

        annID = new Long(stmt.getLong(6));
      }
      catch(SQLException sqle) {
        switch(sqle.getErrorCode()) {
          case DBHelper.X_ORACLE_INVALID_ANNOTATION_TYPE:
            throw new PersistenceException(
                "can't create annotation in DB, [invalid annotation type]");
          default:
            throw new PersistenceException(
                "can't create annotation in DB: ["+ sqle.getMessage()+"]");
      }

      }
      finally {
        DBHelper.cleanup(stmt);
      }

      //2.1. set annotation features
      FeatureMap features = ann.getFeatures();
      Assert.assertNotNull(features);
      createFeatures(annID,DBHelper.FEATURE_OWNER_ANNOTATION,features);
    }
  }



  /** -- */
  protected LanguageResource createCorpus(Corpus corp,SecurityInfo secInfo)
  throws PersistenceException,SecurityException {

    //1. create an LR entry for the corpus (T_LANG_RESOURCE table)
    Long lrID = createLR(DBHelper.CORPUS_CLASS,corp.getName(),secInfo,null);

    //2.create am entry in the T_COPRUS table
    Long corpusID = null;
    //DB stuff
    CallableStatement stmt = null;
      try {
        stmt = this.jdbcConn.prepareCall(
            "{ call "+Gate.DB_OWNER+".persist.create_corpus(?,?) }");
        stmt.setLong(1,lrID.longValue());
        stmt.registerOutParameter(2,java.sql.Types.BIGINT);
        stmt.execute();
        corpusID = new Long(stmt.getLong(2));
      }
      catch(SQLException sqle) {
        throw new PersistenceException("can't create corpus [step 2] in DB: ["+ sqle.getMessage()+"]");
      }
      finally {
        DBHelper.cleanup(stmt);
      }

    //3. for each document in the corpus call createDocument()
    Iterator itDocuments = corp.iterator();
    while (itDocuments.hasNext()) {
      Document doc = (Document)itDocuments.next();

      createDocument(doc,corpusID,secInfo);
    }

    //4. create features
    createFeatures(corpusID,DBHelper.FEATURE_OWNER_CORPUS,corp.getFeatures());

    //5. done
    return corp;
  }


  /**
   * Get a resource from the persistent store.
   * <B>Don't use this method - use Factory.createResource with
   * DataStore and DataStoreInstanceId parameters set instead.</B>
   */
  public LanguageResource getLr(String lrClassName, Object lrPersistenceId)
  throws PersistenceException {

    Document docResult = null;
//    CorpusImpl corpResult  = null;

    docResult = readDocument(lrPersistenceId);

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
                             " FROM   "+Gate.DB_OWNER+".t_lr_type LRTYPE ");

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
                      " FROM   "+Gate.DB_OWNER+".t_lang_resource LR, " +
                      "        "+Gate.DB_OWNER+".t_lr_type LRTYPE " +
                      " WHERE  LR.lr_type_id = LRTYPE.lrtp_id " +
                      "        AND LRTYPE.lrtp_type = ? "
                      );
      stmt.setString(1,lrType);
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
                " FROM   "+Gate.DB_OWNER+".t_lang_resource LR, " +
                "        t_lr_type LRTYPE " +
                " WHERE  LR.lr_type_id = LRTYPE.lrtp_id " +
                "        AND LRTYPE.lrtp_type = ? "
                );
      stmt.setString(1,lrType);
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


  /** Gets a timestamp marker that will be used for all changes made in
   *  the database so that subsequent calls to deleteSince() could restore (partly)
   *  the database state as it was before the update. <B>NOTE:</B> Restoring the previous
   *  state may not be possible at all (i.e. if DELETE is performed)
   *   */
  public Long timestamp()
    throws PersistenceException{

    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".persist.get_timestamp(?)} ");
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
    throws PersistenceException, SecurityException{

    return canAccessLR((Long) lrID,s,READ_ACCESS);
  }

  /**
   * Checks if the user (identified by the sessionID)
   * has write access to the LR
   */
  public boolean canWriteLR(Object lrID, Session s)
    throws PersistenceException, SecurityException{

    return canAccessLR((Long) lrID,s,WRITE_ACCESS);
  }


  /**
   * Checks if the user (identified by the sessionID)
   * has some access (read/write) to the LR
   */
  protected boolean canAccessLR(Long lrID, Session s,int mode)
    throws PersistenceException, SecurityException{

    Assert.assert(READ_ACCESS == mode || WRITE_ACCESS == mode);

    //first check the session and then check whether the user is member of the group
    if (this.ac.isValidSession(s) == false) {
      throw new SecurityException("invalid session supplied");
    }

    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".security.has_access_to_lr(?,?,?,?,?)} ");
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
    finally {
      DBHelper.cleanup(stmt);
    }
  }

  /** --- */
  public static void readCLOB(java.sql.Clob src, StringBuffer dest)
    throws SQLException, IOException {

    int readLength = 0;

    //1. empty the dest buffer
    dest.delete(0,dest.length());

    //2. get Oracle CLOB
    CLOB clo = (CLOB)src;

    //3. create temp buffer
    int buffSize = Math.max(INTERNAL_BUFFER_SIZE,clo.getBufferSize());
    char[] readBuffer = new char[buffSize];

    //3. get Unicode stream
    Reader input = clo.getCharacterStream();

    //4. read
    BufferedReader buffInput = new BufferedReader(input,INTERNAL_BUFFER_SIZE);

    while ((readLength = buffInput.read(readBuffer, 0, INTERNAL_BUFFER_SIZE)) != -1) {
      dest.append(readBuffer, 0, readLength);
    }

    //5.close streams
    buffInput.close();
    input.close();

  }


  /** --- */
  public static void writeCLOB(String src,java.sql.Clob dest)
    throws SQLException, IOException {

    //preconditions
    Assert.assertNotNull(src);

    //1. get Oracle CLOB
    CLOB clo = (CLOB)dest;

    //2. get Unicode stream
    Writer output = clo.getCharacterOutputStream();

    //3. write
    BufferedWriter buffOutput = new BufferedWriter(output,INTERNAL_BUFFER_SIZE);
    buffOutput.write(src.toString());

    //4. flushing is a good idea [although BufferedWriter::close() calls it this is
    //implementation specific]
    buffOutput.flush();
    output.flush();

    //5.close streams
    buffOutput.close();
    output.close();
  }

  /** --- */
  public static void writeCLOB(StringBuffer src,java.sql.Clob dest)
    throws SQLException, IOException {

    //delegate
    writeCLOB(src.toString(),dest);
  }

  protected Long _createFeature(Long entityID, int entityType,String key,Object value, int valueType)
    throws PersistenceException {

    //1. store in DB
    Long featID = null;
    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".persist.create_feature(?,?,?,?,?,?,?)} ");

      //1.1 set known values + NULLs
      stmt.setLong(1,entityID.longValue());
      stmt.setLong(2,entityType);
      stmt.setString(3,key);
      stmt.setNull(4,java.sql.Types.NUMERIC);
      stmt.setNull(5,java.sql.Types.VARCHAR);
      stmt.setLong(6,valueType);
      stmt.registerOutParameter(7,java.sql.Types.BIGINT);

      //1.2 set proper data
      switch(valueType) {

        case DBHelper.VALUE_TYPE_BOOLEAN:

          boolean b = ((Boolean)value).booleanValue();
          stmt.setLong(4, b ? this.ORACLE_TRUE : this.ORACLE_FALSE);
          break;

        case DBHelper.VALUE_TYPE_INTEGER:

          stmt.setLong(4,((Integer)value).intValue());
          break;

        case DBHelper.VALUE_TYPE_LONG:

          stmt.setLong(4,((Long)value).longValue());
          break;

        case DBHelper.VALUE_TYPE_FLOAT:

          Double d = (Double)value;
          stmt.setDouble(4,d.doubleValue());

        case DBHelper.VALUE_TYPE_BINARY:

          //ignore
          //will be handled later in processing

        case DBHelper.VALUE_TYPE_STRING:

          String s = (String)value;
          //does it fin into a varchar2?
          if (fitsInVarchar2(s)) {
            stmt.setString(5,s);
          }
          //else : will be handled later in processing

      }


      stmt.execute();
      featID = new Long(stmt.getLong(7));
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {

        case DBHelper.X_ORACLE_INVALID_FEATURE_TYPE:
          throw new PersistenceException("can't create feature [step 1],"+
                      "[invalid feature type] in DB: ["+ sqle.getMessage()+"]");
        default:
          throw new PersistenceException("can't create feature [step 1] in DB: ["+
                                                      sqle.getMessage()+"]");
      }
    }
    finally {
      DBHelper.cleanup(stmt);
    }

    return featID;
  }


  protected void _updateFeatureLOB(Long featID,Object value, int valueType)
    throws PersistenceException {

    //NOTE: at this point value is never an array,
    // although the type may claim so

    //0. preconditions
    Assert.assert(valueType == DBHelper.VALUE_TYPE_BINARY ||
                  valueType == DBHelper.VALUE_TYPE_BINARY_ARR ||
                  valueType == DBHelper.VALUE_TYPE_STRING ||
                  valueType == DBHelper.VALUE_TYPE_STRING_ARR);


    //1. get the row to be updated
    PreparedStatement stmtA = null;
    ResultSet rsA = null;
    Clob clobValue = null;
    Blob blobValue = null;

    try {
      String sql = " select ft_long_character_value, " +
                   "        ft_binary_value " +
                   " from  "+Gate.DB_OWNER+".t_feature " +
                   " where  ft_id = ? ";

      stmtA = this.jdbcConn.prepareStatement(sql);
      stmtA.setLong(1,featID.longValue());
      stmtA.execute();
      rsA = stmtA.getResultSet();

      rsA.next();
      //NOTE: if the result set contains LOBs always read them
      // in the order they appear in the SQL query
      // otherwise data will be lost
      clobValue = rsA.getClob(1);
      blobValue = rsA.getBlob(2);

      //blob or clob?
      if (valueType == DBHelper.VALUE_TYPE_BINARY ||
          valueType == DBHelper.VALUE_TYPE_BINARY_ARR) {
        //blob
        throw new MethodNotImplementedException();
      }
      else {
        //clob
        String s = (String)value;
        writeCLOB(s,clobValue);
      }
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't create feature [step 2] in DB: ["+ sqle.getMessage()+"]");
    }
    catch(IOException ioe) {
      throw new PersistenceException("can't create feature [step 2] in DB: ["+ ioe.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rsA);
      DBHelper.cleanup(stmtA);
    }

  }

  /** --- */
  protected void createFeature(Long entityID, int entityType,String key, Object value)
    throws PersistenceException {

    //1. what kind of feature value is this?
    int valueType = findFeatureType(value);

    //2. how many elements do we store?
    Vector elementsToStore = new Vector();

    switch(valueType) {
      case DBHelper.VALUE_TYPE_BINARY:
      case DBHelper.VALUE_TYPE_BOOLEAN:
      case DBHelper.VALUE_TYPE_FLOAT:
      case DBHelper.VALUE_TYPE_INTEGER:
      case DBHelper.VALUE_TYPE_LONG:
      case DBHelper.VALUE_TYPE_STRING:
        elementsToStore.add(value);
        break;

      default:
        //arrays
        List arr = (List)value;
        Iterator itValues = arr.iterator();

        while (itValues.hasNext()) {
          elementsToStore.add(itValues.next());
        }

        //normalize , i.e. ignore arrays
        if (valueType == DBHelper.VALUE_TYPE_BINARY_ARR)
          valueType = DBHelper.VALUE_TYPE_BINARY;
        else if (valueType == DBHelper.VALUE_TYPE_BOOLEAN_ARR)
          valueType = DBHelper.VALUE_TYPE_BOOLEAN;
        else if (valueType == DBHelper.VALUE_TYPE_FLOAT_ARR)
          valueType = DBHelper.VALUE_TYPE_FLOAT;
        else if (valueType == DBHelper.VALUE_TYPE_INTEGER_ARR)
          valueType = DBHelper.VALUE_TYPE_INTEGER;
        else if (valueType == DBHelper.VALUE_TYPE_LONG_ARR)
          valueType = DBHelper.VALUE_TYPE_LONG;
        else if (valueType == DBHelper.VALUE_TYPE_STRING_ARR)
          valueType = DBHelper.VALUE_TYPE_STRING;
    }



    //3. for all elements:
    for (int i=0; i< elementsToStore.size(); i++) {

        Object currValue = elementsToStore.elementAt(i);

        //3.1. create a dummy feature [LOB hack]
        Long featID = _createFeature(entityID,entityType,key,value,valueType);

        //3.2. update CLOBs if needed
        if (valueType == DBHelper.VALUE_TYPE_STRING) {

          //does this string fit into a varchar2 or into clob?
          String s = (String)currValue;
          if (false == this.fitsInVarchar2(s)) {
            // Houston, we have a problem
            // put the string into a clob
            _updateFeatureLOB(featID,value,valueType);
          }
        }

        //3.3. BLOBs
        if (valueType == DBHelper.VALUE_TYPE_BINARY) {
          throw new MethodNotImplementedException();
        }
    }


  }


  private boolean fitsInVarchar2(String s) {

    return s.getBytes().length < this.ORACLE_VARCHAR_LIMIT_BYTES;
  }

  /** --- */
  private void createFeatures(Long entityID, int entityType, FeatureMap features)
    throws PersistenceException {

      /* when some day Java has macros, this will be a macro */
      Set entries = features.entrySet();
      Iterator itFeatures = entries.iterator();
      while (itFeatures.hasNext()) {
        Map.Entry entry = (Map.Entry)itFeatures.next();
        String key = (String)entry.getKey();
        Object value = entry.getValue();
        createFeature(entityID,entityType,key,value);
      }
  }

  /** get security information for LR . */
  public SecurityInfo getSecurityInfo(LanguageResource lr)
    throws PersistenceException {

    throw new MethodNotImplementedException();
  }

  /** set security information for LR . */
  public void setSecurityInfo(LanguageResource lr,SecurityInfo si)
    throws PersistenceException, SecurityException {

    throw new MethodNotImplementedException();
  }


  protected Document readDocument(Object lrPersistenceId)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(lrPersistenceId);

    if (false == lrPersistenceId instanceof Long) {
      throw new IllegalArgumentException();
    }

    // 1. dummy document to be initialized
    Document result = new DatabaseDocumentImpl(this.jdbcConn);

    PreparedStatement pstmt = null;
    ResultSet rs = null;

    //3. read from DB
    try {
      String sql = " select lr_name, " +
                   "        lrtp_type, " +
                   "        lr_id, " +
                   "        doc_url, " +
                   "        doc_start, " +
                   "        doc_end, " +
                   "        doc_is_markup_aware " +
                   " from  "+Gate.DB_OWNER+".v_document " +
                   " where  lr_id = ? ";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)lrPersistenceId).longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      rs.next();

      //4. fill data

      //4.1 name
      String lrName = rs.getString("lr_name");
      Assert.assertNotNull(lrName);
      result.setName(lrName);

      //4.2. markup aware
      long markup = rs.getLong("doc_is_markup_aware");
      Assert.assert(markup == this.ORACLE_FALSE || markup == this.ORACLE_TRUE);
      if (markup == this.ORACLE_FALSE) {
        result.setMarkupAware(Boolean.FALSE);
      }
      else {
        result.setMarkupAware(Boolean.TRUE);
      }

      //4.3 datastore
      result.setDataStore(this);

      //4.4. persist ID
      Long persistID = new Long(rs.getLong("lr_id"));
      result.setLRPersistenceId(persistID);

      //4.5  source url
      String url = rs.getString("doc_url");
      result.setSourceUrl(new URL(url));

      //4.6. start offset
      Long start = new Long(rs.getLong("doc_start"));
      result.setSourceUrlStartOffset(start);

      //4.7. end offset
      Long end = new Long(rs.getLong("doc_end"));
      result.setSourceUrlEndOffset(end);

      //4.8 features
      FeatureMap features = readFeatures((Long)lrPersistenceId,DBHelper.FEATURE_OWNER_DOCUMENT);
      result.setFeatures(features);
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

  protected FeatureMap readFeatures(Long entityID, int entityType)
    throws PersistenceException {

    //0. preconditions
    Assert.assertNotNull(entityID);
    Assert.assert(entityType == DBHelper.FEATURE_OWNER_ANNOTATION ||
                  entityType == DBHelper.FEATURE_OWNER_CORPUS ||
                  entityType == DBHelper.FEATURE_OWNER_DOCUMENT);


    PreparedStatement pstmt = null;
    ResultSet rs = null;
    FeatureMap fm = new SimpleFeatureMapImpl();

    //1. read from DB
    try {
      String sql = " select v1.ft_key, " +
                   "        v1.ft_value_type, " +
                   "        v1.ft_number_value, " +
                   "        v1.ft_binary_value, " +
                   "        v1.ft_character_value, " +
                   "        v1.ft_long_character_value " +
                   " from  "+Gate.DB_OWNER+".t_feature v1 " +
                   " where  v1.ft_entity_id = ? " +
                   "        and v1.ft_enetity_type = ? " +
                   " order by v1.ft_key";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,entityID.longValue());
      pstmt.setLong(2,entityType);
      pstmt.execute();
      rs = pstmt.getResultSet();

      //3. fill feature map
      Vector arrFeatures = new Vector();
      String prevKey = null;
      String currKey = null;
      Object currFeature = null;


      while (rs.next()) {
        //NOTE: because there are LOBs in the resulset
        //the columns should be read in the order they appear
        //in the query
        currKey = rs.getString(1);

        Long valueType = new Long(rs.getLong(2));

        //we don't quite know what is the type of the NUMBER
        //stored in DB
        Object numberValue = null;

        //for all numeric types + boolean -> read from DB as appropriate
        //Java object
        switch(valueType.intValue()) {

          case DBHelper.VALUE_TYPE_BOOLEAN:
            numberValue = new Boolean(rs.getBoolean(3));
            break;

          case DBHelper.VALUE_TYPE_FLOAT:
            numberValue = new Float(rs.getFloat(3));
            break;

          case DBHelper.VALUE_TYPE_INTEGER:
            numberValue = new Integer(rs.getInt(3));
            break;

          case DBHelper.VALUE_TYPE_LONG:
            numberValue = new Long(rs.getLong(3));
            break;
        }

        //don't forget to read the rest of the current row
        Blob blobValue = rs.getBlob(4);
        String stringValue = rs.getString(5);
        Clob clobValue = rs.getClob(6);

        switch(valueType.intValue()) {
          case DBHelper.VALUE_TYPE_BINARY:
            throw new MethodNotImplementedException();

          case DBHelper.VALUE_TYPE_BOOLEAN:
          case DBHelper.VALUE_TYPE_FLOAT:
          case DBHelper.VALUE_TYPE_INTEGER:
          case DBHelper.VALUE_TYPE_LONG:
            currFeature = numberValue;
            break;

          case DBHelper.VALUE_TYPE_STRING:
            //this one is tricky too
            //if the string is < 4000 bytes long then it's stored as varchar2
            //otherwise as CLOB
            if (null == stringValue) {
              //oops, we got CLOB
              StringBuffer temp = new StringBuffer();
              readCLOB(clobValue,temp);
              currFeature = temp.toString();
            }
            else {
              currFeature = stringValue;
            }
            break;

          default:
            throw new PersistenceException("Invalid feature type found in DB");
        }//switch

        //new feature or part of an array?
        if (currKey != prevKey && prevKey != null) {
          //part of array
          arrFeatures.add(currFeature);
        }
        else {
          //add prev feature to feature map

          //is the prev feature an array or a single object?
          if (arrFeatures.size() > 1) {
            fm.put(prevKey,arrFeatures);
          }
          else if (arrFeatures.size() == 1) {
            fm.put(prevKey,arrFeatures.elementAt(0));
          }
          else {
            //do nothing, this is the dummy feature
            ;
          }//if

          //now clear the array from previous fesature(s) and put the new
          //one there
          arrFeatures.clear();

          prevKey = currKey;
          arrFeatures.add(currFeature);
        }//if
      }//while

      //add the last feature
      if (arrFeatures.size() > 1) {
        fm.put(currKey,arrFeatures);
      }
      else if (arrFeatures.size() == 1) {
        fm.put(currKey,arrFeatures.elementAt(0));
      }

    }//try
    catch(SQLException sqle) {
      throw new PersistenceException("can't read features from DB: ["+ sqle.getMessage()+"]");
    }
    catch(IOException ioe) {
      throw new PersistenceException("can't read features from DB: ["+ ioe.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);
    }

    return fm;
  }



}
