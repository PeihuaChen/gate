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

import gate.LanguageResource;
import gate.security.SecurityInfo;
import gate.util.*;

public class PostgresDataStore extends JDBCDataStore {

  /** Name of this resource */
  public static final String DS_COMMENT = "GATE PostgreSQL datastore";

  /** the icon for this resource */
  public static final String DS_ICON_NAME = "pgsql_ds.gif";

  /** Debug flag */
  private static final boolean DEBUG = true;

  public PostgresDataStore() {
    super();
  }

  public List getLrTypes() throws gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    Vector lrTypes = new Vector();
    Statement stmt = null;
    ResultSet rs = null;

    try {
      stmt = this.jdbcConn.createStatement();
      rs = stmt.executeQuery(" SELECT lrtp_type " +
                             " FROM   t_lr_type");

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

  public List getLrIds(String lrType) throws gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public String getComment() {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public boolean canWriteLR(Object lrID) throws gate.security.SecurityException, gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public void setSecurityInfo(LanguageResource parm1, SecurityInfo parm2) throws gate.persist.PersistenceException, gate.security.SecurityException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public String getIconName() {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public List getLrNames(String lrType) throws gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public SecurityInfo getSecurityInfo(LanguageResource parm1) throws gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public List findLrIds(List constraints, String lrType) throws gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }
  public boolean lockLr(LanguageResource parm1) throws gate.persist.PersistenceException, gate.security.SecurityException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }
  public LanguageResource getLr(String lrClassName, Object lrPersistenceId) throws gate.security.SecurityException, gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }
  public String getLrName(Object lrId) throws gate.persist.PersistenceException {
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
  public boolean canReadLR(Object lrID) throws gate.security.SecurityException, gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }
  public LanguageResource adopt(LanguageResource lr, SecurityInfo secInfo) throws gate.security.SecurityException, gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }

  public List findLrIds(List constraints) throws gate.persist.PersistenceException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }
  public void unlockLr(LanguageResource parm1) throws gate.persist.PersistenceException, gate.security.SecurityException {
    /**@todo: implement this gate.persist.JDBCDataStore abstract method*/
    throw new MethodNotImplementedException();
  }
}