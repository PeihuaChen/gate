/*
 *  DBHelper.java
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

public class DBHelper {

  /** --- */
  private static final String jdbcOracleDriverName = "oracle.jdbc.driver.OracleDriver";
  private static final String jdbcPostgresDriverName = "postgresql.Driver";
  private static final String jdbcSapDBDriverName = "com.sap.dbtech.jdbc.DriverSapDB";


  //ACHTUNG!
  //DO NOT EDIT THESE CONSTANTS WITHOUT
  //SYNCHRONIZING WITH ERROR.SPC PL/SQL PACKAGE
  //note that while Oracle returns negative error numbers
  //the SQLException::getErrorCode() returns positive ones
  //
  public static final int X_ORACLE_START = 20100;
  public static final int X_ORACLE_DUPLICATE_GROUP_NAME =      X_ORACLE_START + 1 ;
  public static final int X_ORACLE_DUPLICATE_USER_NAME =       X_ORACLE_START + 2 ;
  public static final int X_ORACLE_INVALID_USER_NAME =         X_ORACLE_START + 3 ;
  public static final int X_ORACLE_INVALID_USER_PASS =         X_ORACLE_START + 4 ;
  public static final int X_ORACLE_INVALID_USER_GROUP =        X_ORACLE_START + 5 ;
  public static final int X_ORACLE_INVALID_LR =                X_ORACLE_START + 6 ;
  public static final int X_ORACLE_INVALID_ACCESS_MODE =       X_ORACLE_START + 7 ;
  public static final int X_ORACLE_INVALID_ARGUMENT =          X_ORACLE_START + 8 ;
  public static final int X_ORACLE_NOT_IMPLEMENTED =           X_ORACLE_START + 9 ;
  public static final int X_ORACLE_GROUP_OWNS_RESOURCES =      X_ORACLE_START + 10 ;
  public static final int X_ORACLE_USER_OWNS_RESOURCES =       X_ORACLE_START + 11 ;
  public static final int X_ORACLE_INCOMPLETE_DATA  =          X_ORACLE_START + 12 ;


  private static final boolean DEBUG = true;

  private static boolean  driversLoaded;

  static {

    driversLoaded = false;
  }


  protected DBHelper() {

    //no way
    //contains only static methods
  }

  /** --- */
  private static synchronized void loadDrivers()
    throws ClassNotFoundException {

    if (!driversLoaded) {
      Class.forName(jdbcOracleDriverName);
      Class.forName(jdbcPostgresDriverName);
      Class.forName(jdbcSapDBDriverName);

      driversLoaded = true;
    }
  }


  /** --- */
  public static void cleanup(ResultSet rs)
    throws PersistenceException {

    try {
      if (rs!=null)
        rs.close();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("an SQL exception occured ["+ sqle.getMessage()+"]");
    }
  }

  /** --- */
  public static void cleanup(Statement stmt)
    throws PersistenceException {
    try {
      if (stmt!=null)
        stmt.close();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("an SQL exception occured ["+ sqle.getMessage()+"]");
    }
  }

  /** --- */
  public static Connection connect(String connectURL)
    throws SQLException,ClassNotFoundException{

    loadDrivers();
    Connection conn = DriverManager.getConnection(connectURL);

    if (DEBUG) {
      DatabaseMetaData meta = conn.getMetaData();
      gate.util.Out.println(
            "JDBC driver name=["+meta.getDriverName() +
            "] version=["+ meta.getDriverVersion() +"]");
    }

    return conn;
  }

}
