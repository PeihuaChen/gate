/*
 *  GroupImpl.java
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
 *  $Id$
 */

package gate.security;

import java.util.*;
import java.sql.*;

import junit.framework.*;

import gate.persist.PersistenceException;
import gate.util.MethodNotImplementedException;


public class GroupImpl implements Group{

  /** --- */
  private Long    id;

  /** --- */
  private String  name;

  /** --- */
  private List    users;

  /** --- */
  private Connection conn;

  /** --- */
  private AccessController ac;


  public GroupImpl(Long id, String name, List users,AccessController ac,Connection conn) {

    this.id = id;
    this.name = name;
    this.users = users;
    this.ac = ac;
    this.conn = conn;
  }

  /** --- */
  public Long getID() {

    return id;
  }

  /** --- */
  public String getName() {

    return name;
  }

  /** --- */
  public List getUsers() {

    return users;
  }


  /** --- */
  public void setName(String newName, Session s)
    throws PersistenceException,SecurityException {

    CallableStatement stmt = null;

    try {
      //first check the session and then check whether the user is member of the group
      if (this.ac.isValidSession(s) == false ||
          this.users.contains(s.getUser()) == false) {
        throw new SecurityException("invalid session supplied");
      }

      stmt = this.conn.prepareCall("{ call security.set_group_name(?,?)} ");
      stmt.setLong(1,this.id.longValue());
      stmt.setString(2,newName);
      stmt.execute();
      //release stmt???
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't change user name in DB: ["+ sqle.getMessage()+"]");
    }
  }


  /** --- */
  public void addUser(Long userID, Session s)
    throws PersistenceException,SecurityException{

    User usr = this.ac.findUser(userID);
    addUser(usr,s);
  }

  /** --- */
  public void addUser(User usr, Session s)
    throws PersistenceException,SecurityException{

    //1. check if the user is not already in group
    if (this.users.contains(usr)) {
      throw new SecurityException("User id=["+usr.getID()+"] is alredy member of group");
    }

    //2. check the session
    if (this.ac.isValidSession(s) == false) {
      throw new SecurityException("invalid session provided");
    }

    //3. update DB
    CallableStatement stmt = null;

    try {
      stmt = this.conn.prepareCall("{ call security.add_user_to_group(?,?)} ");
      stmt.setLong(1,this.id.longValue());
      stmt.setLong(2,usr.getID().longValue());
      stmt.execute();
      //release stmt???
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't add user to group in DB: ["+ sqle.getMessage()+"]");
    }

    //4. update usr collection
    this.users.add(usr);

  }


  /** --- */
  public void removeUser(Long userID, Session s)
    throws PersistenceException,SecurityException {

    User usr = this.ac.findUser(userID);
    removeUser(usr,s);
  }


  /** --- */
  public void removeUser(User usr, Session s)
    throws PersistenceException,SecurityException{

    //1. check if the user member of group
    if (this.users.contains(usr) == false) {
      throw new SecurityException("User id=["+usr.getID()+"] is NOT a member of group");
    }

    //2. check the session
    if (this.ac.isValidSession(s) == false) {
      throw new SecurityException("invalid session provided");
    }

    //3. update DB
    CallableStatement stmt = null;

    try {
      stmt = this.conn.prepareCall("{ call security.remove_user_from_group(?,?)} ");
      stmt.setLong(1,this.id.longValue());
      stmt.setLong(2,usr.getID().longValue());
      stmt.execute();
      //release stmt???
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't remove user from group in DB: ["+ sqle.getMessage()+"]");
    }

    //4. update usr collection
    this.users.remove(usr);
  }


  /**
   *
   *  this one is necessary for the contains() operations in Lists
   *  It is possible that two users have two different GroupImpl that refer
   *  to the very same GATE group in the DB, because they got it from the security
   *  factory at different times. So we assume that two instances refer the same
   *  GATE group if NAME1==NAME2
   *
   *  */
  public boolean equals(Object obj)
  {
    Assert.assert(obj instanceof Group);

    Group group2 = (Group)obj;

    return (this.name == group2.getName());
  }

}
