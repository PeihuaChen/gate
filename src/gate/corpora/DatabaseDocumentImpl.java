/*
 *  DatabaseDocumentImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 16/Oct/2001
 *
 *  $Id$
 */

package gate.corpora;


import java.sql.*;
import java.io.*;
import java.util.*;
import java.net.*;

import oracle.jdbc.driver.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.persist.*;
import gate.annotation.*;
import gate.creole.*;
import gate.event.*;

public class DatabaseDocumentImpl extends DocumentImpl
                                  implements  //DatastoreListener,
                                              //Document,
                                              EventAwareDocument {

  private static final boolean DEBUG = false;

  private boolean     isContentRead;
  private Object      contentLock;
  private Connection  jdbcConn;
  private String      jdbcSchema;
  protected int       dbType;

  private boolean     contentChanged;
  private boolean     featuresChanged;
  private boolean     nameChanged;
  private boolean     documentChanged;

  private Collection  removedAnotationSets;
  private Collection  addedAnotationSets;

  private Document    parentDocument;

  /**
   * The listener for the events coming from the features.
   */
  protected EventsHandler eventHandler;


  public DatabaseDocumentImpl() {

    //super();
    contentLock = new Object();

    this.namedAnnotSets = new HashMap();
//    this.defaultAnnots = new DatabaseAnnotationSetImpl(this);

    this.isContentRead = false;

    this.contentChanged = false;
    this.featuresChanged = false;
    this.nameChanged = false;
    this.documentChanged = false;

    this.removedAnotationSets = new Vector();
    this.addedAnotationSets = new Vector();

    parentDocument = null;
  }

  private void setDatabaseInfo(Connection conn)
    throws PersistenceException {

    String url = null;

    try {
      url = conn.getMetaData().getURL();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("cannot get jdbc metadata: ["+sqle.getMessage()+"]");
    }

    this.jdbcSchema = DBHelper.getSchemaPrefix(url);
    this.dbType = DBHelper.getDatabaseType(url);
    Assert.assertNotNull(this.jdbcSchema);
    Assert.assertTrue(this.dbType == DBHelper.ORACLE_DB ||
                      this.dbType == DBHelper.POSTGRES_DB);

  }


  public DatabaseDocumentImpl(Connection conn)
    throws PersistenceException {

    //super();
    contentLock = new Object();

    this.namedAnnotSets = new HashMap();
//    this.defaultAnnots = new DatabaseAnnotationSetImpl(this);

    this.isContentRead = false;
    this.jdbcConn = conn;
    setDatabaseInfo(this.jdbcConn);

    this.contentChanged = false;
    this.featuresChanged = false;
    this.nameChanged = false;
    this.documentChanged = false;

    this.removedAnotationSets = new Vector();
    this.addedAnotationSets = new Vector();

    parentDocument = null;
  }


/*  public DatabaseDocumentImpl(Connection _conn,
                              String _name,
                              DatabaseDataStore _ds,
                              Long _persistenceID,
                              DocumentContent _content,
                              FeatureMap _features,
                              Boolean _isMarkupAware,
                              URL _sourceURL,
                              Long _urlStartOffset,
                              Long _urlEndOffset,
                              AnnotationSet _default,
                              Map _named) {

    //this.jdbcConn =  _conn;
    this(_conn);

    this.name = _name;
    this.dataStore = _ds;
    this.lrPersistentId = _persistenceID;
    this.content = _content;
    this.isContentRead = true;
    this.features = _features;
    this.markupAware = _isMarkupAware;
    this.sourceUrl = _sourceURL;
    this.sourceUrlStartOffset = _urlStartOffset;
    this.sourceUrlEndOffset = _urlEndOffset;

    //annotations
    //1. default
    _setAnnotations(null,_default);

    //2. named (if any)
    if (null != _named) {
      Iterator itNamed = _named.values().iterator();
      while (itNamed.hasNext()){
        AnnotationSet currSet = (AnnotationSet)itNamed.next();
        //add them all to the DBAnnotationSet
        _setAnnotations(currSet.getName(),currSet);
      }
    }

    //3. add the listeners for the features
    if (eventHandler == null)
      eventHandler = new EventsHandler();
    this.features.addFeatureMapListener(eventHandler);

    //4. add self as listener for the data store, so that we'll know when the DS is
    //synced and we'll clear the isXXXChanged flags
    this.dataStore.addDatastoreListener(this);
  }
*/

  /** The content of the document: a String for text; MPEG for video; etc. */
  public DocumentContent getContent() {

    //1. if this is a child document then return the content of the parent resource
    if (null != this.parentDocument) {
      return this.parentDocument.getContent();
    }
    else {
      //2. assert that no one is reading from DB now
      synchronized(this.contentLock) {
        if (false == this.isContentRead) {
          _readContent();
          this.isContentRead = true;
        }
      }

      //return content
      return super.getContent();
    }
  }

  private void _readContent() {

    //preconditions
    if (null == getLRPersistenceId()) {
      throw new GateRuntimeException("can't construct a DatabaseDocument - not associated " +
                                    " with any data store");
    }

    if (false == getLRPersistenceId() instanceof Long) {
      throw new GateRuntimeException("can't construct a DatabaseDocument -  " +
                                      " invalid persistence ID");
    }

    Long lrID = (Long)getLRPersistenceId();
    //0. preconditions
    Assert.assertNotNull(lrID);
    Assert.assertTrue(false == this.isContentRead);
    Assert.assertNotNull(this.content);

    //1. read from DB
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {

      String sql = " select v1.enc_name, " +
                   "        v1.dc_character_content, " +
                   "        v1.dc_binary_content, " +
                   "        v1.dc_content_type " +
                   " from  "+this.jdbcSchema+"v_content v1 " +
                   " where  v1.lr_id = ? ";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,lrID.longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      if (false == rs.next()) {
        throw new SynchronisationException("empty reault set");
      }

      if (this.dbType == DBHelper.ORACLE_DB) {

        String encoding = rs.getString("enc_name");
        if (encoding.equals(DBHelper.DUMMY_ENCODING)) {
          //no encoding was specified for this document
          encoding = "";
        }
        Clob   clb = rs.getClob("dc_character_content");
        Blob   blb = rs.getBlob("dc_binary_content");
        long   contentType = rs.getLong("dc_content_type");

        Assert.assertTrue(DBHelper.CHARACTER_CONTENT == contentType);

        StringBuffer buff = new StringBuffer();
        OracleDataStore.readCLOB(clb,buff);

        //2. set data members that were not previously initialized
        this.content = new DocumentContentImpl(buff.toString());
        this.encoding = encoding;
      }

      else if (this.dbType == DBHelper.POSTGRES_DB) {

        String encoding = rs.getString("enc_name");
        if (encoding.equals(DBHelper.DUMMY_ENCODING)) {
          //no encoding was specified for this document
          encoding = "";
        }

        String content = rs.getString("dc_character_content");
        long   contentType = rs.getLong("dc_content_type");

        Assert.assertTrue(DBHelper.CHARACTER_CONTENT == contentType);

        //2. set data members that were not previously initialized
        this.content = new DocumentContentImpl(content);
        this.encoding = encoding;
      }

      else {
        Assert.fail();
      }
    }
    catch(SQLException sqle) {
      throw new SynchronisationException("can't read content from DB: ["+ sqle.getMessage()+"]");
    }
    catch(IOException ioe) {
      throw new SynchronisationException(ioe);
    }
    finally {
      try {
        DBHelper.cleanup(rs);
        DBHelper.cleanup(pstmt);
      }
      catch(PersistenceException pe) {
        throw new SynchronisationException("JDBC error: ["+ pe.getMessage()+"]");
      }
    }
  }


  /** Get the encoding of the document content source */
  public String getEncoding() {

    //1. assert that no one is reading from DB now
    synchronized(this.contentLock) {
      if (false == this.isContentRead) {
        _readContent();

        this.isContentRead = true;
      }
    }

    return super.getEncoding();
  }

  /** Returns a map with the named annotation sets. It returns <code>null</code>
   *  if no named annotaton set exists. */
  public Map getNamedAnnotationSets() {

    Vector annNames = new Vector();

    PreparedStatement pstmt = null;
    ResultSet rs = null;

    //1. get the names of all sets
    try {
      String sql = " select as_name " +
                   " from  "+this.jdbcSchema+"v_annotation_set " +
                   " where  lr_id = ? " +
                   "  and as_name is not null";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)this.lrPersistentId).longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      while (rs.next()) {
        annNames.add(rs.getString("as_name"));
      }
    }
    catch(SQLException sqle) {
      throw new SynchronisationException("can't get named annotatios: ["+ sqle.getMessage()+"]");
    }
    finally {
      try {
        DBHelper.cleanup(rs);
        DBHelper.cleanup(pstmt);
      }
      catch(PersistenceException pe) {
        throw new SynchronisationException("JDBC error: ["+ pe.getMessage()+"]");
      }
    }

    //2. read annotations
    for (int i=0; i< annNames.size(); i++) {
      //delegate because of the data is already read getAnnotations() will just return
      getAnnotations((String)annNames.elementAt(i));
    }

    //3. delegate to the parent method
    return super.getNamedAnnotationSets();

  } // getNamedAnnotationSets


  /** Get the default set of annotations. The set is created if it
    * doesn't exist yet.
    */
  public AnnotationSet getAnnotations() {

    //1. read from DB
    _getAnnotations(null);

    //2. is there such set in the DB?
    if (null == this.defaultAnnots) {
      //create a DatabaseAnnotationSetImpl
      //NOTE: we create the set and then delegate to the super mehtod, otherwise
      //the super mehtod will create AnnotationSetImpl instead of DatabaseAnnotationSetImpl
      //which will not work with DatabaseDocumentImpl
      AnnotationSet aset = new DatabaseAnnotationSetImpl(this);

      //set internal member
      this.defaultAnnots = aset;

      //3. fire events
      fireAnnotationSetAdded(new DocumentEvent(this,
                                                DocumentEvent.ANNOTATION_SET_ADDED,
                                                null));
    }

    //4. delegate
    return super.getAnnotations();
  } // getAnnotations()


  /** Get a named set of annotations. Creates a new set if one with this
    * name doesn't exist yet.
    * If the provided name is null then it returns the default annotation set.
    */
  public AnnotationSet getAnnotations(String name) {

    //0. preconditions
    Assert.assertNotNull(name);

    //1. read from DB if the set is there at all
    _getAnnotations(name);

    //2. is there such set in the DB?
    if (false == this.namedAnnotSets.keySet().contains(name)) {
      //create a DatabaseAnnotationSetImpl
      //NOTE: we create the set and then delegate to the super mehtod, otherwise
      //the super mehtod will create AnnotationSetImpl instead of DatabaseAnnotationSetImpl
      //which will not work with DatabaseDocumentImpl
      AnnotationSet aset = new DatabaseAnnotationSetImpl(this,name);

      //add to internal collection
      this.namedAnnotSets.put(name,aset);

      //add the set name to the list with the recently created sets
      this.addedAnotationSets.add(name);

      //3. fire events
      DocumentEvent evt = new DocumentEvent(this, DocumentEvent.ANNOTATION_SET_ADDED, name);
      fireAnnotationSetAdded(evt);
    }

    //3. delegate
    return super.getAnnotations(name);
  }


  private void _getAnnotations(String name) {

    AnnotationSet as = null;

    //preconditions
    if (null == getLRPersistenceId()) {
      throw new GateRuntimeException("can't construct a DatabaseDocument - not associated " +
                                    " with any data store");
    }

    if (false == getLRPersistenceId() instanceof Long) {
      throw new GateRuntimeException("can't construct a DatabaseDocument -  " +
                                      " invalid persistence ID");
    }

    //have we already read this set?

    if (null == name) {
      //default set
      if (this.defaultAnnots != null) {
        //the default set is alredy read - do nothing
        //super methods will take care
        return;
      }
    }
    else {
      //named set
      if (this.namedAnnotSets.containsKey(name)) {
        //we've already read it - do nothing
        //super methods will take care
        return;
      }
    }

    Long lrID = (Long)getLRPersistenceId();
    Long asetID = null;
    //0. preconditions
    Assert.assertNotNull(lrID);

    //1. read a-set info
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      String sql = " select as_id " +
                   " from  "+this.jdbcSchema+"v_annotation_set " +
                   " where  lr_id = ? ";
      //do we have aset name?
      String clause = null;
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
        //ok, there is such aset in the DB
        asetID = new Long(rs.getLong(1));
      }
      else {
        //wow, there is no such aset, so create new ...
        //... by delegating to the super method
        return;
      }

      //1.5 cleanup
      DBHelper.cleanup(rs);
      DBHelper.cleanup(pstmt);

      //2. read annotation Features
      HashMap featuresByAnnotationID = _readFeatures(asetID);

      //3. read annotations
      AnnotationSetImpl transSet = new AnnotationSetImpl(this);

      String hint;

      if (this.dbType == DBHelper.ORACLE_DB) {
        hint = "/*+ use_nl(v.t_annotation v.t_as_annotation) " +
              "     use_nl(v.t_annotation_type v.t_annotation) "+
              " */";
      }
      else {
        hint = "";
      }

      String sql1 = " select "+hint+
                    "        ann_local_id, " +
                    "        at_name, " +
                    "        start_offset, " +
                    "        end_offset " +
                    " from  "+this.jdbcSchema+"v_annotation  v" +
                    " where  asann_as_id = ? ";

      if (DEBUG) Out.println(">>>>> asetID=["+asetID+"]");

      pstmt = this.jdbcConn.prepareStatement(sql1);
      pstmt.setLong(1,asetID.longValue());

      if (this.dbType == DBHelper.ORACLE_DB) {
        ((OraclePreparedStatement)pstmt).setRowPrefetch(DBHelper.CHINK_SIZE_LARGE);
      }
      pstmt.execute();
      rs = pstmt.getResultSet();

      while (rs.next()) {
        //1. read data memebers
        Integer annID = new Integer(rs.getInt(1));
        String type = rs.getString(2);
        Long startOffset = new Long(rs.getLong(3));
        Long endOffset = new Long(rs.getLong(4));

        if (DEBUG) Out.println("ann_local_id=["+annID+"]");
        if (DEBUG) Out.println("start_off=["+startOffset+"]");
        if (DEBUG) Out.println("end_off=["+endOffset+"]");

        //2. get the features
        FeatureMap fm = (FeatureMap)featuresByAnnotationID.get(annID);
        //fm should NOT be null
        if (null == fm) {
          fm =  new SimpleFeatureMapImpl();
        }

        //3. add to annotation set
        transSet.add(annID,startOffset,endOffset,type,fm);
      }//while

      //1.5, create a-set
      if (null == name) {
        as = new DatabaseAnnotationSetImpl(this, transSet);
      }
      else {
        as = new DatabaseAnnotationSetImpl(this,name, transSet);
      }
    }
    catch(SQLException sqle) {
      throw new SynchronisationException("can't read annotations from DB: ["+ sqle.getMessage()+"]");
    }
    catch(InvalidOffsetException oe) {
      throw new SynchronisationException(oe);
    }
    catch(PersistenceException pe) {
      throw new SynchronisationException("JDBC error: ["+ pe.getMessage()+"]");
    }
    finally {
      try {
        DBHelper.cleanup(rs);
        DBHelper.cleanup(pstmt);
      }
      catch(PersistenceException pe) {
        throw new SynchronisationException("JDBC error: ["+ pe.getMessage()+"]");
      }
    }


    //4. update internal data members
    if (name == null) {
      //default as
      this.defaultAnnots = as;
    }
    else {
      //named as
      this.namedAnnotSets.put(name,as);
    }

    //don't return the new aset, the super method will take care
    return;
  }




  private HashMap _readFeatures(Long asetID) {

    PreparedStatement pstmt = null;
    ResultSet rs = null;

    //1
    String      prevKey = DBHelper.DUMMY_FEATURE_KEY;
    String      currKey = null;

    Integer     prevAnnID = null;
    Integer     currAnnID = null;

    Object      currFeatureValue = null;
    Vector      currFeatureArray = new Vector();

    HashMap     currFeatures = new HashMap();
    FeatureMap  annFeatures = null;

    HashMap     featuresByAnnotID = new HashMap();

    //2. read the features from DB

    try {

      if (this.dbType == DBHelper.ORACLE_DB) {
        String sql = " select /*+ use_nl(v.t_annotation v.t_as_annotation) "+
                     "            use_nl(v.t_feature v.t_annotation) "+
                     "            index(v.t_feature xt_feature_01) "+
                     "            use_nl(v.t_feature_key v.t_feature) "+
                     "           full(v.t_feature_key)           "+
                     "        */                                  "+
                     "                                            " +
                     "        ann_local_id, " +
                     "        key, " +
                     "        ft_value_type, " +
                     "        ft_number_value, " +
                     "        ft_character_value, " +
                     "        ft_long_character_value, " +
                     "        ft_binary_value " +
                     " from  "+this.jdbcSchema+"v_annotation_features " +
                     " where  set_id = ? " +
                     " order by ann_local_id,key ";

        pstmt = this.jdbcConn.prepareStatement(sql);
        pstmt.setLong(1,asetID.longValue());
        ((OraclePreparedStatement)pstmt).setRowPrefetch(DBHelper.CHINK_SIZE_LARGE);
        pstmt.execute();
        rs = pstmt.getResultSet();
      }

      else if (this.dbType == DBHelper.POSTGRES_DB) {

        String sql = " select " +
                     "        ann_local_id, " +
                     "        key, " +
                     "        ft_value_type, " +
                     "        ft_int_value, " +
                     "        ft_float_value, " +
                     "        ft_character_value, " +
                     "        ft_binary_value " +
                     " from  "+this.jdbcSchema+"v_annotation_features " +
                     " where  set_id = ? " +
                     " order by ann_local_id,key ";

        pstmt = this.jdbcConn.prepareStatement(sql);
        pstmt.setLong(1,asetID.longValue());
        pstmt.execute();
        rs = pstmt.getResultSet();
      }

      else {
        Assert.fail();
      }

      while (rs.next()) {
        //NOTE: because there are LOBs in the resulset
        //the columns should be read in the order they appear
        //in the query

        prevAnnID = currAnnID;
        currAnnID = new Integer(rs.getInt("ann_local_id"));

        //2.1 is this a new Annotation?
        if (!currAnnID.equals(prevAnnID) && prevAnnID != null) {
          //new one
          //2.1.1 normalize the hashmap with the features, and add
          //the elements into a new FeatureMap
          annFeatures = new SimpleFeatureMapImpl();
          Set entries = currFeatures.entrySet();
          Iterator itFeatureArrays = entries.iterator();

          while(itFeatureArrays.hasNext()) {
            Map.Entry currEntry = (Map.Entry)itFeatureArrays.next();
            String key = (String)currEntry.getKey();
            Vector val = (Vector)currEntry.getValue();

            //add to feature map normalized array
            Assert.assertTrue(val.size() >= 1);

            if (val.size() == 1) {
              //the single elemnt of the array
              annFeatures.put(key,val.firstElement());
            }
            else {
              //the whole array
              annFeatures.put(key,val);
            }
          }//while

          //2.1.2. add the featuremap for this annotation to the hashmap
          featuresByAnnotID.put(prevAnnID,annFeatures);
          //2.1.3. clear temp hashtable with feature vectors
          currFeatures.clear();
/*??*/          prevAnnID = currAnnID;
        }//if -- is new annotation

        currKey = rs.getString("key");
        Long valueType = new Long(rs.getLong("ft_value_type"));

        //we don't quite know what is the type of the NUMBER
        //stored in DB
        Object numberValue = null;

        //for all numeric types + boolean -> read from DB as appropriate
        //Java object
        switch(valueType.intValue()) {

          case DBHelper.VALUE_TYPE_BOOLEAN:

            if (this.dbType == DBHelper.ORACLE_DB) {
              numberValue = new Boolean(rs.getBoolean("ft_number_value"));
            }
            else if (this.dbType == DBHelper.POSTGRES_DB){
              numberValue = new Boolean(rs.getBoolean("ft_int_value"));
            }
            else {
              Assert.fail();
            }

            break;


          case DBHelper.VALUE_TYPE_FLOAT:

            if (this.dbType == DBHelper.ORACLE_DB) {
              numberValue = new Float(rs.getFloat("ft_number_value"));
            }
            else if (this.dbType == DBHelper.POSTGRES_DB){
              numberValue = new Float(rs.getFloat("ft_float_value"));
            }
            else {
              Assert.fail();
            }

            break;

          case DBHelper.VALUE_TYPE_INTEGER:

            if (this.dbType == DBHelper.ORACLE_DB) {
              numberValue = new Integer(rs.getInt("ft_number_value"));
            }
            else if (this.dbType == DBHelper.POSTGRES_DB){
              numberValue = new Integer(rs.getInt("ft_int_value"));
            }
            else {
              Assert.fail();
            }

            break;

          case DBHelper.VALUE_TYPE_LONG:

            if (this.dbType == DBHelper.ORACLE_DB) {
              numberValue = new Long(rs.getLong("ft_number_value"));
            }
            else if (this.dbType == DBHelper.POSTGRES_DB){
              numberValue = new Long(rs.getLong("ft_int_value"));
            }
            else {
              Assert.fail();
            }

            break;

          default:
            //do nothing, will be handled in the next switch statement
        }

        //don't forget to read the rest of the current row
        String stringValue = rs.getString("ft_character_value");
        Clob clobValue = null;
        Blob blobValue = null;

        if (this.dbType == DBHelper.ORACLE_DB) {
          clobValue = rs.getClob("ft_long_character_value");
          blobValue = rs.getBlob("ft_binary_value");
        }

        switch(valueType.intValue()) {

          case DBHelper.VALUE_TYPE_NULL:
            currFeatureValue = null;
            break;

          case DBHelper.VALUE_TYPE_BINARY:
            throw new MethodNotImplementedException();

          case DBHelper.VALUE_TYPE_BOOLEAN:
          case DBHelper.VALUE_TYPE_FLOAT:
          case DBHelper.VALUE_TYPE_INTEGER:
          case DBHelper.VALUE_TYPE_LONG:
            currFeatureValue = numberValue;
            break;

          case DBHelper.VALUE_TYPE_STRING:

            if (this.dbType == DBHelper.ORACLE_DB && null == stringValue) {
              //this one is tricky too
              //if the string is < 4000 bytes long then it's stored as varchar2
              //otherwise as CLOB

              StringBuffer temp = new StringBuffer();
              OracleDataStore.readCLOB(clobValue,temp);
              currFeatureValue = temp.toString();
            }
            else { /* PostgresDB or (Oracle DB + value is stored in varchar column) */
              currFeatureValue = stringValue;
            }
            break;

          default:
            throw new SynchronisationException("Invalid feature type found in DB, value is ["+valueType+"]");
        }//switch

        //ok, we got the key/value pair now
        //2.2 is this a new feature key?
        if (false == currFeatures.containsKey(currKey)) {
          //new key
          Vector keyValue = new Vector();
          keyValue.add(currFeatureValue);
          currFeatures.put(currKey,keyValue);
        }
        else {
          //key is present, append to existing vector
          ((Vector)currFeatures.get(currKey)).add(currFeatureValue);
        }

        prevKey = currKey;
      }//while


      //2.3 process the last Annotation left
      annFeatures = new SimpleFeatureMapImpl();

      Set entries = currFeatures.entrySet();
      Iterator itFeatureArrays = entries.iterator();

      while(itFeatureArrays.hasNext()) {
        Map.Entry currEntry = (Map.Entry)itFeatureArrays.next();
        String key = (String)currEntry.getKey();
        Vector val = (Vector)currEntry.getValue();

        //add to feature map normalized array
        Assert.assertTrue(val.size() >= 1);

        if (val.size() == 1) {
          //the single elemnt of the array
          annFeatures.put(key,val.firstElement());
        }
        else {
          //the whole array
          annFeatures.put(key,val);
        }
      }//while

      //2.3.1. add the featuremap for this annotation to the hashmap
      if (null != currAnnID) {
        // do we have features at all for this annotation?
        featuresByAnnotID.put(currAnnID,annFeatures);
      }

      //3. return the hashmap
      return featuresByAnnotID;
    }
    catch(SQLException sqle) {
      throw new SynchronisationException("can't read content from DB: ["+ sqle.getMessage()+"]");
    }
    catch(IOException sqle) {
      throw new SynchronisationException("can't read content from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      try {
        DBHelper.cleanup(rs);
        DBHelper.cleanup(pstmt);
      }
      catch(PersistenceException pe) {
        throw new SynchronisationException("JDBC error: ["+ pe.getMessage()+"]");
      }
    }
  }


  /** Set method for the document content */
  public void setContent(DocumentContent content) {

    //if the document is a child document then setContent()is prohibited
    if (null != this.parentDocument) {
      Err.prln("content of document ["+this.name+"] cannot be changed!");
      return;
    }
    else {
      super.setContent(content);
      this.contentChanged = true;
    }
  }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) {
    //1. save them first, so we can remove the listener
    FeatureMap oldFeatures = this.features;

    super.setFeatures(features);

    this.featuresChanged = true;

    //4. sort out the listeners
    if (eventHandler != null)
      oldFeatures.removeFeatureMapListener(eventHandler);
    else
      eventHandler = new EventsHandler();
    this.features.addFeatureMapListener(eventHandler);
  }

  /** Sets the name of this resource*/
  public void setName(String name){
    super.setName(name);

    this.nameChanged = true;
  }


  private List getAnnotationsForOffset(AnnotationSet aDumpAnnotSet,Long offset){
    throw new MethodNotImplementedException();
  }


  public void setNextNodeId(int nextID){
    Assert.assertTrue(nextID >= 0);
    this.nextNodeId = nextID;
  }


  public boolean isResourceChanged(int changeType) {

    switch(changeType) {

      case EventAwareLanguageResource.DOC_CONTENT:
        return this.contentChanged;
      case EventAwareLanguageResource.RES_FEATURES:
        return this.featuresChanged;
      case EventAwareLanguageResource.RES_NAME:
        return this.nameChanged;
      case EventAwareLanguageResource.DOC_MAIN:
        return this.documentChanged;
      default:
        throw new IllegalArgumentException();
    }

  }

  private void _setAnnotations(String setName,Collection annotations) {

    if (null == setName) {
      Assert.assertTrue(null == this.defaultAnnots);
      this.defaultAnnots = new DatabaseAnnotationSetImpl(this,annotations);

      //add to the set of loaded a-sets but do not add its annotations to the
      //list of new annotations
//      this.loadedAnnotSets.add(this.defaultAnnots);
    }
    else {
      Assert.assertTrue(false == this.namedAnnotSets.containsKey(setName));
      AnnotationSet annSet = new DatabaseAnnotationSetImpl(this,setName,annotations);
      this.namedAnnotSets.put(setName,annSet);

      //add to the set of loaded a-sets but do not add its annotations to the
      //list of new annotations
//      this.loadedAnnotSets.add(annSet);
    }
  }

  /** Set method for the document's URL */
  public void setSourceUrl(URL sourceUrl) {

    this.documentChanged = true;
    super.setSourceUrl(sourceUrl);
  } // setSourceUrl


  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method sets the
    * end offset.
    */
  public void setSourceUrlEndOffset(Long sourceUrlEndOffset) {

    this.documentChanged = true;
    super.setSourceUrlEndOffset(sourceUrlEndOffset);
  } // setSourceUrlStartOffset


  /** Documents may be packed within files; in this case an optional pair of
    * offsets refer to the location of the document. This method sets the
    * start offset.
    */
  public void setSourceUrlStartOffset(Long sourceUrlStartOffset) {

    this.documentChanged = true;
    super.setSourceUrlStartOffset(sourceUrlStartOffset);
  } // setSourceUrlStartOffset

  /** Make the document markup-aware. This will trigger the creation
   *  of a DocumentFormat object at Document initialisation time; the
   *  DocumentFormat object will unpack the markup in the Document and
   *  add it as annotations. Documents are <B>not</B> markup-aware by default.
   *
   *  @param b markup awareness status.
   */
  public void setMarkupAware(Boolean newMarkupAware) {

    this.documentChanged = true;
    super.setMarkupAware(newMarkupAware);
  }

  /**
   * All the events from the features are handled by
   * this inner class.
   */
  class EventsHandler implements gate.event.FeatureMapListener {
    public void featureMapUpdated(){
      //tell the document that its features have been updated
      featuresChanged = true;
    }
  }

  /**
   * Overriden to remove the features listener, when the document is closed.
   */
  public void cleanup() {
    super.cleanup();
    if (eventHandler != null)
      this.features.removeFeatureMapListener(eventHandler);
  }///inner class EventsHandler


  /**
   * Called by a datastore when a new resource has been adopted
   */
  public void resourceAdopted(DatastoreEvent evt){
  }

  /**
   * Called by a datastore when a resource has been deleted
   */
  public void resourceDeleted(DatastoreEvent evt){

    Assert.assertNotNull(evt);
    Assert.assertNotNull(evt.getResourceID());

    //unregister self as listener from the DataStore
    if (evt.getResourceID().equals(this.getLRPersistenceId())) {
      //someone deleted this document
      getDataStore().removeDatastoreListener(this);
    }

  }//resourceDeleted

  /**
   * Called by a datastore when a resource has been wrote into the datastore
   */
  public void resourceWritten(DatastoreEvent evt){

    Assert.assertNotNull(evt);
    Assert.assertNotNull(evt.getResourceID());

    //is the event for us?
    if (evt.getResourceID().equals(this.getLRPersistenceId())) {
      //wow, the event is for me
      //clear all flags, the content is synced with the DB
      this.contentChanged =
        this.documentChanged =
          this.featuresChanged =
            this.nameChanged = false;

      this.removedAnotationSets.clear();
      this.addedAnotationSets.clear();
    }


  }

  public Collection getLoadedAnnotationSets() {

    //never return the data member - return a clone
    Assert.assertNotNull(this.namedAnnotSets);
    Vector result = new Vector(this.namedAnnotSets.values());
    if (null != this.defaultAnnots) {
      result.add(this.defaultAnnots);
    }

    return result;
  }


  public Collection getRemovedAnnotationSets() {

    //return a clone
    return new Vector(this.removedAnotationSets);
  }

  public Collection getAddedAnnotationSets() {

    //return a clone
    return new Vector(this.addedAnotationSets);
  }

  public void removeAnnotationSet(String name) {

    //1. add to the list of removed a-sets
    this.removedAnotationSets.add(name);

    //if the set was read from the DB then it is registered as datastore listener and ...
    //there may be chnges in it
    //NOTE that default set cannot be reoved, so we just ignore it

    if (this.namedAnnotSets.keySet().contains(name)) {
      //set was loaded
      AnnotationSet aset = (AnnotationSet)this.namedAnnotSets.get(name);

      Assert.assertNotNull(aset);
      Assert.assertTrue(aset instanceof DatabaseAnnotationSetImpl);

      //3. unregister it as a DataStoreListener
      this.dataStore.removeDatastoreListener((DatastoreListener)aset);
    }

    //4. delegate
    super.removeAnnotationSet(name);
  }

  /**
   * Returns true of an LR has been modified since the last sync.
   * Always returns false for transient LRs.
   */
  public boolean isModified() {
    return this.isResourceChanged(EventAwareLanguageResource.DOC_CONTENT) ||
            this.isResourceChanged(EventAwareLanguageResource.RES_FEATURES) ||
              this.isResourceChanged(EventAwareLanguageResource.RES_NAME) ||
                this.isResourceChanged(EventAwareLanguageResource.DOC_MAIN);
  }


  /**
   * Returns the parent LR of this LR.
   * Only relevant for LRs that support shadowing. Most do not by default.
   */
  public LanguageResource getParent()
    throws PersistenceException,SecurityException {

    return this.parentDocument;
  }//getParent

  /**
   * Sets the parent LR of this LR.
   * Only relevant for LRs that support shadowing. Most do not by default.
   */
  public void setParent(LanguageResource parentLR)
    throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(parentLR);

    if (false == parentLR instanceof DatabaseDocumentImpl) {
      throw new IllegalArgumentException("invalid parent resource set");
    }

    //1.
    this.parentDocument = (Document)parentLR;

  }//setParent

  public void setInitData__$$__(Object data)
    throws PersistenceException {

    HashMap initData = (HashMap)data;

    this.jdbcConn = (Connection)initData.get("JDBC_CONN");
    setDatabaseInfo(this.jdbcConn);
    this.dataStore = (DatabaseDataStore)initData.get("DS");
    this.lrPersistentId = (Long)initData.get("LR_ID");
    this.name = (String)initData.get("DOC_NAME");
    this.content = (DocumentContent)initData.get("DOC_CONTENT");
    this.isContentRead = true;
    this.features = (FeatureMap)initData.get("DOC_FEATURES");
    this.markupAware = (Boolean)initData.get("DOC_MARKUP_AWARE");
    this.sourceUrl = (URL)initData.get("DOC_SOURCE_URL");
    this.sourceUrlStartOffset = (Long)initData.get("DOC_SOURCE_URL_START");
    this.sourceUrlEndOffset = (Long)initData.get("DOC_SOURCE_URL_END");

    Integer nextNodeID = (Integer)initData.get("DOC_NEXT_NODE_ID");
    if (null != nextNodeID) {
      this.setNextNodeId(nextNodeID.intValue());
    }

    Integer nextAnnID = (Integer)initData.get("DOC_NEXT_ANN_ID");
    if (null != nextAnnID) {
      this.setNextAnnotationId(nextAnnID.intValue());
    }

    this.parentDocument = (Document)initData.get("PARENT_LR");

    //annotations
    //1. default
    AnnotationSet _default = (AnnotationSet)initData.get("DOC_DEFAULT_ANNOTATIONS");
    if (null != _default) {
      _setAnnotations(null,_default);
    }

    //2. named (if any)
    Map _named = (Map)initData.get("DOC_NAMED_ANNOTATION_SETS");
    if (null != _named) {
      Iterator itNamed = _named.values().iterator();
      while (itNamed.hasNext()){
        AnnotationSet currSet = (AnnotationSet)itNamed.next();
        //add them all to the DBAnnotationSet
        _setAnnotations(currSet.getName(),currSet);
      }
    }

    //3. add the listeners for the features (if any)
    if (null != this.features) {
      if (eventHandler == null)
        eventHandler = new EventsHandler();
      this.features.addFeatureMapListener(eventHandler);
    }

    //4. add self as listener for the data store, so that we'll know when the DS is
    //synced and we'll clear the isXXXChanged flags
    if (null != this.dataStore) {
      this.dataStore.addDatastoreListener(this);
    }

  }

  public Object getInitData__$$__(Object initData) {
    return null;
  }

}