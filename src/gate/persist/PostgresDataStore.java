/*
 *  PostgresDataStore.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 18/Mar/2001
 *
 *  $Id$
 */

package gate.persist;

import java.util.*;
import java.sql.*;

import junit.framework.*;

import gate.LanguageResource;
import gate.security.*;
import gate.security.SecurityException;
import gate.util.*;
import gate.corpora.*;
import gate.*;

public class PostgresDataStore extends JDBCDataStore {

  /** Name of this resource */
  private static final String DS_COMMENT = "GATE PostgreSQL datastore";

  /** the icon for this resource */
  public static final String DS_ICON_NAME = "pgsql_ds.gif";

  /** Debug flag */
  private static final boolean DEBUG = true;

  public PostgresDataStore() {

    super();
    this.datastoreComment = DS_COMMENT;
    this.iconName = DS_ICON_NAME;
  }

  public void setSecurityInfo(LanguageResource parm1, SecurityInfo parm2) throws gate.persist.PersistenceException, gate.security.SecurityException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public List findLrIds(List constraints, String lrType) throws gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public LanguageResource getLr(String lrClassName, Object lrPersistenceId) throws gate.security.SecurityException, gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public void delete(String lrClassName, Object lrId) throws gate.security.SecurityException, gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }
  public void sync(LanguageResource lr) throws gate.security.SecurityException, gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }


  public List findLrIds(List constraints) throws gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }


  /**
   * Releases the exlusive lock on a resource from the persistent store.
   */
  public void unlockLr(LanguageResource lr)
  throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(lr);
    Assert.assertTrue(lr instanceof DatabaseDocumentImpl ||
                      lr instanceof DatabaseCorpusImpl);
    Assert.assertNotNull(lr.getLRPersistenceId());
    Assert.assertEquals(lr.getDataStore(),this);

    //1. check session
    if (null == this.session) {
      throw new SecurityException("session not set");
    }

    if (false == this.ac.isValidSession(this.session)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. check permissions
    if (false == canWriteLR(lr.getLRPersistenceId())) {
      throw new SecurityException("no write access granted to the user");
    }

    //3. try to unlock
    PreparedStatement pstmt = null;
    boolean lockSucceeded = false;

    try {
      String sql = " perform persist_unlock_lr(?,?) ";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,((Long)lr.getLRPersistenceId()).longValue());
      pstmt.setLong(2,this.session.getUser().getID().longValue());
      pstmt.execute();
      //we don't care about the result set
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {
        case DBHelper.X_ORACLE_INVALID_LR:
          throw new PersistenceException("invalid LR ID supplied ["+sqle.getMessage()+"]");
        default:
          throw new PersistenceException(
                "can't unlock LR in DB : ["+ sqle.getMessage()+"]");
      }
    }
    finally {
      DBHelper.cleanup(pstmt);
    }
  }


  /**
   * Checks if the user (identified by the sessionID)
   * has some access (read/write) to the LR
   */

  protected boolean canAccessLR(Long lrID,int mode)
    throws PersistenceException, SecurityException{

    throw new MethodNotImplementedException();
  }

  /** Adopt a resource for persistence. */
/*  public LanguageResource adopt(LanguageResource lr,SecurityInfo secInfo)
    throws PersistenceException,gate.security.SecurityException {

    throw new MethodNotImplementedException();
  }
*/

  /**
   * Try to acquire exlusive lock on a resource from the persistent store.
   * Always call unlockLR() when the lock is no longer needed
   */
  public boolean lockLr(LanguageResource lr)
  throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(lr);
    Assert.assertTrue(lr instanceof DatabaseDocumentImpl ||
                      lr instanceof DatabaseCorpusImpl);
    Assert.assertNotNull(lr.getLRPersistenceId());
    Assert.assertEquals(lr.getDataStore(),this);

    //1. delegate
    return _lockLr((Long)lr.getLRPersistenceId());
  }


  /**
   *  helper for lockLR()
   *  never call directly
   */
  private boolean _lockLr(Long lrID)
  throws PersistenceException,SecurityException {

    //0. preconditions
    Assert.assertNotNull(lrID);

    //1. check session
    if (null == this.session) {
      throw new SecurityException("session not set");
    }

    if (false == this.ac.isValidSession(this.session)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. check permissions
    if (false == canWriteLR(lrID)) {
      throw new SecurityException("no write access granted to the user");
    }

    //3. try to lock
    PreparedStatement pstmt = null;
    ResultSet rset = null;
    boolean lockSucceeded = false;

    try {
      pstmt = this.jdbcConn.prepareStatement(" select persist_lock_lr(?,?,?) ");
      pstmt.setLong(1,lrID.longValue());
      pstmt.setLong(2,this.session.getUser().getID().longValue());
      pstmt.setLong(3,this.session.getGroup().getID().longValue());

      pstmt.execute();
      rset = pstmt.getResultSet();
      rset.next();

      lockSucceeded = rset.getBoolean(4);
    }
    catch(SQLException sqle) {

      switch(sqle.getErrorCode()) {
        case DBHelper.X_ORACLE_INVALID_LR:
          throw new PersistenceException("invalid LR ID supplied ["+sqle.getMessage()+"]");
        default:
          throw new PersistenceException(
                "can't lock LR in DB : ["+ sqle.getMessage()+"]");
      }
    }
    finally {
      DBHelper.cleanup(rset);
      DBHelper.cleanup(pstmt);
    }

    return lockSucceeded;
  }

  protected Corpus createCorpus(Corpus corp,SecurityInfo secInfo, boolean newTransPerDocument)
    throws PersistenceException,SecurityException {

    throw new MethodNotImplementedException();
  }

  protected Document createDocument(Document doc, Long corpusID,SecurityInfo secInfo)
    throws PersistenceException,SecurityException {

    throw new MethodNotImplementedException();
  }

  protected Document createDocument(Document doc,SecurityInfo secInfo)
    throws PersistenceException,SecurityException {

    throw new MethodNotImplementedException();
  }


  /**
   *  helper for adopt()
   *  never call directly
   */
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
    PreparedStatement pstmt = null;
    ResultSet rset = null;

    try {
      String sql = " select persist_create_lr(?,?,?,?,?,?) ";
      pstmt = this.jdbcConn.prepareStatement(sql);
      pstmt.setLong(1,si.getUser().getID().longValue());
      pstmt.setLong(2,si.getGroup().getID().longValue());
      pstmt.setString(3,lrType);
      pstmt.setString(4,lrName);
      pstmt.setInt(5,si.getAccessMode());
      if (null == lrParentID) {
        pstmt.setNull(6,java.sql.Types.INTEGER);
      }
      else {
        pstmt.setLong(6,lrParentID.longValue());
      }

      pstmt.execute();
      rset = pstmt.getResultSet();
      if (false == rset.next()) {
        throw new PersistenceException("empty result set");
      }

      Long result =  new Long(rset.getLong("1"));

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
      DBHelper.cleanup(rset);
      DBHelper.cleanup(pstmt);
    }
  }

}