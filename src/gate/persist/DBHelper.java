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

import gate.util.*;

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
  public static final int X_ORACLE_INVALID_LR_TYPE  =          X_ORACLE_START + 13 ;
  public static final int X_ORACLE_INVALID_ANNOTATION_TYPE =   X_ORACLE_START + 14 ;
  public static final int X_ORACLE_INVALID_FEATURE_TYPE =      X_ORACLE_START + 15 ;
  public static final int X_ORACLE_INVALID_CONTENT_TYPE =      X_ORACLE_START + 16 ;
  public static final int X_ORACLE_INVALID_ANNOTATION =        X_ORACLE_START + 17 ;

  public static final int TRUE = 1;
  public static final int FALSE = 0;

  public static final int CHARACTER_CONTENT = 1;
  public static final int BINARY_CONTENT = 2;
  public static final int EMPTY_CONTENT = 3;

  public static final String DOCUMENT_CLASS = "gate.corpora.DatabaseDocumentImpl";
  public static final String CORPUS_CLASS =  "gate.corpora.CorpusImpl";

  public static final String  DB_PARAMETER_GUID = "DB_GUID";

  //dummy key
  //hopefully no one will create a feature with such key
  public static final String DUMMY_FEATURE_KEY =  "--NO--SUCH--KEY--";
  //dummy ID
  public static final Long DUMMY_ID;


  //!!! ACHTUNG !!!
  // these 4 constants should *always* be synchronzied with the ones in the
  // related SQL packages/scripts [for Oracle - security.spc]
  // i.e. if u don't have a serious reason do *not* change anything

  /** used to store corpus' features */
  protected static final int FEATURE_OWNER_CORPUS  = 1;
  /** used to store document's features */
  protected static final int FEATURE_OWNER_DOCUMENT  = 2;
  /** used to store annotation's features */
  protected static final int FEATURE_OWNER_ANNOTATION  = 3;


  /** feature value is int  */
  public static final int VALUE_TYPE_INTEGER           = 101;
  /** feature value is long */
  public static final int VALUE_TYPE_LONG              = 102;
  /** feature value is boolean */
  public static final int VALUE_TYPE_BOOLEAN           = 103;
  /** feature value is string less than 4000 bytes */
  public static final int VALUE_TYPE_STRING            = 104;
  /** feature value is binary */
  public static final int VALUE_TYPE_BINARY            = 105;
  /** feature value is float */
  public static final int VALUE_TYPE_FLOAT             = 106;
  /** feature value is array of ints */
  public static final int VALUE_TYPE_INTEGER_ARR       = 107;
  /** feature value is array of longs */
  public static final int VALUE_TYPE_LONG_ARR          = 108;
  /** feature value is array of bools */
  public static final int VALUE_TYPE_BOOLEAN_ARR       = 109;
  /** feature value is array of strings */
  public static final int VALUE_TYPE_STRING_ARR        = 110;
  /** feature value is array of binary values */
  public static final int VALUE_TYPE_BINARY_ARR        = 111;
  /** feature value is array of floats */
  public static final int VALUE_TYPE_FLOAT_ARR         = 112;
  /** feature value is array of floats */
  public static final int VALUE_TYPE_EMPTY_ARR         = 113;


  private static final boolean DEBUG = false;



  private static boolean  driversLoaded;

  static {
    DUMMY_ID = new Long(Long.MIN_VALUE);
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
      gate.util.Err.println(
            "JDBC driver name=["+meta.getDriverName() +
            "] version=["+ meta.getDriverVersion() +"]");
    }

    return conn;
  }

  /** --- */
  public static void readCLOB(java.sql.Clob src, StringBuffer dest)
    throws SQLException {

    throw new MethodNotImplementedException();
  }


  /** --- */
  public static void writeCLOB(StringBuffer src,java.sql.Clob dest)
    throws SQLException {

    throw new MethodNotImplementedException();
  }

}
