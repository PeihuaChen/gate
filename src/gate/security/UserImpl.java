/*
 *  User.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 19/Sep/2001
 *
 */

package gate.security;

import java.util.*;
import java.sql.*;

import gate.persist.PersistenceException;


public class UserImpl implements User {

  /** --- */
  private Long    id;

  /** --- */
  private String  name;

  /** --- */
  private List    groups;

  /** --- */
  private Connection conn;

  /** --- */
  private AccessControler ac;


  /** --- */
  public UserImpl(Long id, String name, List groups,AccessControler ac,Connection conn) {

    this.id = id;
    this.name = name;
    this.groups = groups;
    this.ac = ac;
    this.conn = conn;
  }


  /* Interface USER */

  /** --- */
  public Long getID() {

    return id;
  }

  /** --- */
  public String getName() {

    return name;
  }

  /** --- */
  public List getGroups() {

    return groups;
  }

  /** --- */
  public void setName(String newName, Session s)
    throws PersistenceException,SecurityException {

    CallableStatement stmt = null;

    try {
      //first check the session
      if (this.ac.isValidSession(s) == false || s.getID() != this.id) {
        throw new SecurityException("invalid session supplied");
      }

      stmt = this.conn.prepareCall("{ call security.set_user_name(?,?)} ");
      stmt.setLong(0,this.id.longValue());
      stmt.setString(1,newName);
      stmt.execute();
      //release stmt???
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't change user name in DB: ["+ sqle.getMessage()+"]");
    }

  }

  /** --- */
  public void setPassword(String newPass, Session s)
    throws PersistenceException,SecurityException {

    CallableStatement stmt = null;

    try {
      //first check the session
      if (this.ac.isValidSession(s) == false || s.getID() != this.id) {
        throw new SecurityException("invalid session supplied");
      }

      stmt = this.conn.prepareCall("{ call security.set_user_password(?,?)} ");
      stmt.setLong(0,this.id.longValue());
      stmt.setString(1,newPass);
      stmt.execute();
      //release stmt???
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't change user password in DB: ["+ sqle.getMessage()+"]");
    }

  }

}