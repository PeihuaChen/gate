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


public class DatabaseDocumentImpl extends DocumentImpl {

  protected boolean     isContentRead;
  protected Object      contentLock;
  protected Connection  jdbcConn;
  protected HashMap     annotSetsRead;

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
    annotSetsRead = new HashMap();

    this.isContentRead = false;
    this.jdbcConn = conn;
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

  protected void _readContent() {

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


  protected void _getAnnotations(String name) {

    //have we already read this set?
    if (this.annotSetsRead.containsKey(name)) {
      //we've already read it - do nothing
      //super methods will take care
      return;
    }
    else {
      Long lrID = (Long)getLRPersistenceId();
      //0. preconditions
      Assert.assertNotNull(lrID);

      //1. read a-set info


      //2. read annotations
      PreparedStatement pstmt = null;
      ResultSet rs = null;

      try {
        String sql = " select v1.ann_id, " +
                     "        v1.ann_id, " +
                     "        v1.at_name " +
                     " from  "+Gate.DB_OWNER+".v_annot_set v1 " +
                     "       "+Gate.DB_OWNER+".v_document v2 " +
                     " where  v1.lr_id = ? " +
                     "        and v1.doc_id = v2.as_doc_id " +
                     "        and v2.and as_name = ? ";

      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,lrID.longValue());
      if (null == name) {
        pstmt.setNull(2,java.sql.Types.VARCHAR);
      }
      else {
        pstmt.setString(2,name);
      }
      pstmt.execute();
      rs = pstmt.getResultSet();

      rs.next();

      //3, add to a-set

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

      //4. update internal data members

      //5. read features for the annotations in this a-set
      throw new MethodNotImplementedException();

      //6. update annotations
    }
  }


}