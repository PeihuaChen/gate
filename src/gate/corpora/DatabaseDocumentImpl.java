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

import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.persist.*;


public class DatabaseDocumentImpl extends DocumentImpl {

  protected boolean     isContentRead;
  protected Object      contentLock;
  protected Connection  jdbcConn;

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
      String sql = " select t1.dc_encoding_id, " +
                   "        t1.dc_character_content_id, " +
                   "        t1.dc_binary_content_id, " +
                   "        t1.dc_content_type " +
                   " from  "+Gate.DB_OWNER+".t_document_content t1, " +
                   "       "+Gate.DB_OWNER+".t_document t2, " +
                   " where  t2.doc_content_id = t1.dc_id " +
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

      this.content = new DocumentContentImpl(buff.toString());
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
        throw new SynchronisationException("can't read content from DB: ["+ pe.getMessage()+"]");
      }
    }

  }

}