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
  public static Connection connect(URL connectURL)
    throws SQLException,ClassNotFoundException{

    loadDrivers();
    Connection conn = DriverManager.getConnection(connectURL.toString());

    return conn;
  }

}
