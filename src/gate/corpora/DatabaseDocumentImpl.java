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

import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.persist.*;
import gate.annotation.*;

public class DatabaseDocumentImpl extends DocumentImpl {

  private boolean     isContentRead;
  private Object      contentLock;
  private Connection  jdbcConn;
  private HashSet     annotationsRead;

  private boolean     contentChanged;
  private boolean     featuresChanged;

  public DatabaseDocumentImpl(Connection conn) {

    //super();

    //preconditions
    if (null == getLRPersistenceId()) {
      throw new GateRuntimeException("can't construct a DatabaseDocument - not associated " +
                                    " with any data store");
    }

    if (false == getLRPersistenceId() instanceof Long) {
      throw new GateRuntimeException("can't construct a DatabaseDocument -  " +
                                      " invalid persistence ID");
    }

    contentLock = new Object();

    this.namedAnnotSets = new HashMap();
    this.defaultAnnots = new AnnotationSetImpl(this);

    this.isContentRead = false;
    this.jdbcConn = conn;

    this.contentChanged = false;
    this.featuresChanged = false;
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


    //have we already read this set?

    if (null == name) {
      //default set
      if (this.defaultAnnots != null) {
        //the default set is alredy red - do nothing
        //super methods will take care
        return;
      }
      else {
        this.defaultAnnots = new AnnotationSetImpl(this);
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
      String sql = " select v1.as_id " +
                   " from  "+Gate.DB_OWNER+".v_annotation_set v1 " +
                   " where  v1.lr_id = ? ";
      //do we have aset name?
      String clause = null;
      if (null != name) {
        clause =   "        and v1.as_name = ? ";
      }
      else {
        clause =   "        and v1.as_name is null ";
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
          as = new AnnotationSetImpl(this);
        }
        else {
          as = new AnnotationSetImpl(this,name);
        }
      }
      catch(SQLException sqle) {
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

      //read Features
      HashMap featuresByAnnotationID = _readFeatures(asetID);

      //3. read annotations

      try {
        String sql = " select ann_id, " +
                     "        at_name " +
                     "        start_offset " +
                     "        end_offset " +
                     " from  "+Gate.DB_OWNER+".v_annotation  " +
                     " where  asann_as_id_id = ? ";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,asetID.longValue());
      pstmt.execute();
      rs = pstmt.getResultSet();

      while (rs.next()) {
        //1. read data memebers
        Long annID = new Long(rs.getLong(1));
        String type = rs.getString(2);
        Long startOffset = new Long(rs.getLong(3));
        Long endOffset = new Long(rs.getLong(4));

        //2. get the features
        FeatureMap fm = (FeatureMap)featuresByAnnotationID.get(annID);
        //fm may be null

        //3. add to annotation set
        as.add(startOffset,endOffset,type,fm);
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
      String sql = " select annotation_id, " +
                   "        ft_key, " +
                   "        ft_value_type, " +
                   "        ft_number_value, " +
                   "        ft_character_value, " +
                   "        ft_long_character_value, " +
                   "        ft_binary_value " +
                   " from  "+Gate.DB_OWNER+".v_annotation_features " +
                   " where  set_id = ? " +
                   " order by annotation_id,ft_key ";

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


  public boolean isContentChanged() {
    return this.contentChanged;
  }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) {
    super.setFeatures(features);

    this.featuresChanged = true;
  }


  public boolean isFeatureChanged() {
    return this.featuresChanged;
  }

}