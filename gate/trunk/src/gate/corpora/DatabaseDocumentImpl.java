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

import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.persist.*;
import gate.annotation.*;

public class DatabaseDocumentImpl extends DocumentImpl {

  private static final boolean DEBUG = false;

  private boolean     isContentRead;
  private Object      contentLock;
  private Connection  jdbcConn;

  private boolean     contentChanged;
  private boolean     featuresChanged;
  private boolean     nameChanged;

  //this one should be the same as the values returned
  //in persist.get_id_lot PL/SQL package
  //it sux actually
  private static final int SEQUENCE_POOL_SIZE = 10;

  private Integer sequencePool[];
  private int poolMarker;

  public static final int DOC_NAME = 1001;
  public static final int DOC_CONTENT = 1002;
  public static final int DOC_FEATURES = 1003;

  public DatabaseDocumentImpl(Connection conn) {

    //super();
    contentLock = new Object();

    this.namedAnnotSets = new HashMap();
//    this.defaultAnnots = new DatabaseAnnotationSetImpl(this);

    this.isContentRead = false;
    this.jdbcConn = conn;

    this.contentChanged = false;
    this.featuresChanged = false;
    this.nameChanged = false;

    sequencePool = new Integer[this.SEQUENCE_POOL_SIZE];
    poolMarker = this.SEQUENCE_POOL_SIZE;
  }

  public DatabaseDocumentImpl(Connection _conn,
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
    setAnnotations(null,_default);

    //2. named
    Iterator itNamed = _named.values().iterator();
    while (itNamed.hasNext()){
      AnnotationSet currSet = (AnnotationSet)itNamed.next();
      //add them all to the DBAnnotationSet
      setAnnotations(currSet.getName(),currSet);
    }
  }

  /** The content of the document: a String for text; MPEG for video; etc. */
  public DocumentContent getContent() {

    //1. assert that no one is reading from DB now
    synchronized(this.contentLock) {
      if (false == this.isContentRead) {
        _readContent();
        this.isContentRead = true;
      }
    }

    //return content
    return super.getContent();
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
    Assert.assert(false == this.isContentRead);
    Assert.assertNotNull(this.content);

    //1. read from DB
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      String sql = " select v1.enc_name, " +
                   "        v1.dc_character_content_id, " +
                   "        v1.dc_binary_content_id, " +
                   "        v1.dc_content_type " +
                   " from  "+Gate.DB_OWNER+".v_doc_content v1, " +
                   "       "+Gate.DB_OWNER+".t_document t2, " +
                   " where  t2.doc_content_id = v1.dc_id " +
                   "        and t2.doc_lr_id = ? ";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,lrID.longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      rs.next();

      String encoding = rs.getString(1);
      Clob   clb = rs.getClob(2);
      Blob   blb = rs.getBlob(3);
      long   contentType = rs.getLong(4);

      Assert.assert(DBHelper.CHARACTER_CONTENT == contentType);

      StringBuffer buff = new StringBuffer();
      OracleDataStore.readCLOB(clb,buff);

      //2. set data members that were not previously initialized
      this.content = new DocumentContentImpl(buff.toString());
      this.encoding = encoding;
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
    throw new MethodNotImplementedException();
  } // getNamedAnnotationSets


  /** Get the default set of annotations. The set is created if it
    * doesn't exist yet.
    */
  public AnnotationSet getAnnotations() {

    //read from DB
    _getAnnotations(null);

    return super.getAnnotations();
  } // getAnnotations()


  /** Get a named set of annotations. Creates a new set if one with this
    * name doesn't exist yet.
    * If the provided name is null then it returns the default annotation set.
    */
  public AnnotationSet getAnnotations(String name) {

    //read from DB
    _getAnnotations(name);

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
        //the default set is alredy red - do nothing
        //super methods will take care
        return;
      }
      else {
        this.defaultAnnots = new DatabaseAnnotationSetImpl(this);
        //go on with processing
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
                   " from  "+Gate.DB_OWNER+".v_annotation_set " +
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
          asetID = new Long(rs.getString(1));
        }
        else {
          //wow, there is no such aset, so create new ...
          //... by delegating to the super method
          return;
        }

        //1.5, create a-set
        if (null == name) {
          as = new DatabaseAnnotationSetImpl(this);
        }
        else {
          as = new DatabaseAnnotationSetImpl(this,name);
        }
      }
      catch(SQLException sqle) {
        throw new SynchronisationException("can't read annotations from DB: ["+ sqle.getMessage()+"]");
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

      //read Features
      HashMap featuresByAnnotationID = _readFeatures(asetID);

      //3. read annotations

      try {
        String sql = " select ann_local_id, " +
                     "        at_name, " +
                     "        start_offset, " +
                     "        end_offset " +
                     " from  "+Gate.DB_OWNER+".v_annotation  " +
                     " where  asann_as_id = ? ";

      if (DEBUG) Out.println(">>>>> asetID=["+asetID+"]");

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,asetID.longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      while (rs.next()) {
        //1. read data memebers
        Integer annID = new Integer(rs.getInt(1));
        String type = rs.getString(2);
        Long startOffset = new Long(rs.getLong(3));
        Long endOffset = new Long(rs.getLong(4));

        if (DEBUG) Out.println("annID=["+annID+"]");
        if (DEBUG) Out.println("start=["+startOffset+"]");
        if (DEBUG) Out.println("end=["+endOffset+"]");

        //2. get the features
        FeatureMap fm = (FeatureMap)featuresByAnnotationID.get(annID);
        //fm may should NOT be null
        if (null == fm) {
          fm =  new SimpleFeatureMapImpl();
        }

        //3. add to annotation set
        as.add(annID,startOffset,endOffset,type,fm);
      }
    }
    catch(SQLException sqle) {
      throw new SynchronisationException("can't read content from DB: ["+ sqle.getMessage()+"]");
    }
    catch(InvalidOffsetException oe) {
      throw new SynchronisationException(oe);
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

    Long        prevAnnID = null;
    Long        currAnnID = null;

    Object      currFeatureValue = null;
    Vector      currFeatureArray = new Vector();

    HashMap     currFeatures = new HashMap();
    FeatureMap  annFeatures = null;

    HashMap     featuresByAnnotID = new HashMap();

    //2. read the features from DB
    try {
      String sql = " select ann_local_id, " +
                   "        ft_key, " +
                   "        ft_value_type, " +
                   "        ft_number_value, " +
                   "        ft_character_value, " +
                   "        ft_long_character_value, " +
                   "        ft_binary_value " +
                   " from  "+Gate.DB_OWNER+".v_annotation_features " +
                   " where  set_id = ? " +
                   " order by ann_local_id,ft_key ";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,asetID.longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      while (rs.next()) {
        //NOTE: because there are LOBs in the resulset
        //the columns should be read in the order they appear
        //in the query

        currAnnID = new Long(rs.getLong(1));

        //2.1 is this a new Annotation?
        if (currAnnID != prevAnnID && prevAnnID != null) {
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
            Assert.assert(val.size() >= 1);

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
          prevAnnID = currAnnID;
        }//if

        currKey = rs.getString(2);
        Long valueType = new Long(rs.getLong(3));

        //we don't quite know what is the type of the NUMBER
        //stored in DB
        Object numberValue = null;

        //for all numeric types + boolean -> read from DB as appropriate
        //Java object
        switch(valueType.intValue()) {

          case DBHelper.VALUE_TYPE_BOOLEAN:
            numberValue = new Boolean(rs.getBoolean(4));
            break;

          case DBHelper.VALUE_TYPE_FLOAT:
            numberValue = new Float(rs.getFloat(4));
            break;

          case DBHelper.VALUE_TYPE_INTEGER:
            numberValue = new Integer(rs.getInt(4));
            break;

          case DBHelper.VALUE_TYPE_LONG:
            numberValue = new Long(rs.getLong(4));
            break;

          default:
            //do nothing, will be handled in the next switch statement
        }

        //don't forget to read the rest of the current row
        String stringValue = rs.getString(5);
        Clob clobValue = rs.getClob(6);
        Blob blobValue = rs.getBlob(7);

        switch(valueType.intValue()) {

          case DBHelper.VALUE_TYPE_BINARY:
            throw new MethodNotImplementedException();

          case DBHelper.VALUE_TYPE_BOOLEAN:
          case DBHelper.VALUE_TYPE_FLOAT:
          case DBHelper.VALUE_TYPE_INTEGER:
          case DBHelper.VALUE_TYPE_LONG:
            currFeatureValue = numberValue;
            break;

          case DBHelper.VALUE_TYPE_STRING:
            //this one is tricky too
            //if the string is < 4000 bytes long then it's stored as varchar2
            //otherwise as CLOB
            if (null == stringValue) {
              //oops, we got CLOB
              StringBuffer temp = new StringBuffer();
              OracleDataStore.readCLOB(clobValue,temp);
              currFeatureValue = temp.toString();
            }
            else {
              currFeatureValue = stringValue;
            }
            break;

          default:
            throw new SynchronisationException("Invalid feature type found in DB");
        }//switch

        //ok, we got the key/value pair now
        //2.2 is this a new feature key?
        if (false == currFeatures.containsKey(currKey)) {
          //new key
          Vector keyValue = new Vector();
          keyValue.add(currFeatureValue);
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
        Assert.assert(val.size() >= 1);

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
      featuresByAnnotID.put(prevAnnID,annFeatures);

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

    super.setContent(content);

    this.contentChanged = true;
  }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) {
    super.setFeatures(features);

    this.featuresChanged = true;
  }

  /** Sets the name of this resource*/
  public void setName(String name){
    super.setName(name);

    this.nameChanged = true;
  }


  private List getAnnotationsForOffset(AnnotationSet aDumpAnnotSet,Long offset){
    throw new MethodNotImplementedException();
  }

  /** Generate and return the next annotation ID */
  public Integer getNextAnnotationId() {
/*
    //1.try to get ID fromt he pool
    if (DEBUG) {
      Out.println(">>> get annID called...");
    }
    //is there anything left in the pool?
    if (this.SEQUENCE_POOL_SIZE == this.poolMarker) {
      //oops, pool is empty
      fillSequencePool();
      this.poolMarker = 0;
    }

    return this.sequencePool[this.poolMarker++];
*/
    return super.getNextAnnotationId();
  } // getNextAnnotationId


  public void setNextAnnotationId(int aNextAnnotationId){

    //if u get this exception then u definitely don't have an idea what u're doing
    throw new UnsupportedOperationException("Annotation IDs cannot be changed in " +
                                            "database stores");
  }// setNextAnnotationId();


  private void fillSequencePool() {

    if(DEBUG) {
      Out.println("filling ID lot...");
    }

    CallableStatement stmt = null;
    try {
      stmt = this.jdbcConn.prepareCall(
            "{ call "+Gate.DB_OWNER+".persist.get_id_lot(?,?,?,?,?,?,?,?,?,?) }");
      stmt.registerOutParameter(1,java.sql.Types.BIGINT);
      stmt.registerOutParameter(2,java.sql.Types.BIGINT);
      stmt.registerOutParameter(3,java.sql.Types.BIGINT);
      stmt.registerOutParameter(4,java.sql.Types.BIGINT);
      stmt.registerOutParameter(5,java.sql.Types.BIGINT);
      stmt.registerOutParameter(6,java.sql.Types.BIGINT);
      stmt.registerOutParameter(7,java.sql.Types.BIGINT);
      stmt.registerOutParameter(8,java.sql.Types.BIGINT);
      stmt.registerOutParameter(9,java.sql.Types.BIGINT);
      stmt.registerOutParameter(10,java.sql.Types.BIGINT);
      stmt.execute();

      for (int i=0; i < this.SEQUENCE_POOL_SIZE; i++) {
        //JDBC countsa from 1, not from 0
        this.sequencePool[0] = new Integer(stmt.getInt(i+1));
      }
    }
    catch(SQLException sqle) {
      throw new SynchronisationException("can't get Annotation ID pool: ["+ sqle.getMessage()+"]");
    }
    finally {
      try {
        DBHelper.cleanup(stmt);
      }
      catch(PersistenceException pe) {
        throw new SynchronisationException("JDBC error: ["+ pe.getMessage()+"]");
      }
    }
  }

  public boolean isDocumentChanged(int changeType) {

    switch(changeType) {

      case DatabaseDocumentImpl.DOC_CONTENT:
        return this.contentChanged;
      case DatabaseDocumentImpl.DOC_FEATURES:
        return this.featuresChanged;
      case DatabaseDocumentImpl.DOC_NAME:
        return this.nameChanged;
      default:
        throw new IllegalArgumentException();
    }

  }

  private void setAnnotations(String setName,Collection annotations) {

    if (null == setName) {
      Assert.assert(null == this.defaultAnnots);
      this.defaultAnnots = new DatabaseAnnotationSetImpl(annotations);
    }
    else {
      Assert.assert(false == this.namedAnnotSets.containsKey(setName));
      AnnotationSet annSet = new DatabaseAnnotationSetImpl(this,setName,annotations);
      this.namedAnnotSets.put(setName,annSet);
    }
  }
}