/*
 *  AccessControllerImpl.java
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
import java.net.*;

import junit.framework.*;

import gate.persist.*;
import gate.util.MethodNotImplementedException;


public class AccessControllerImpl implements AccessController {

  private HashMap     sessions;
  private HashMap     keepAliveTimes;
  private Connection  jdbcConn;
  private URL         jdbcURL;

  private Vector      users;
  private Vector      groups;

  private HashMap     usersByID;
  private HashMap     usersByName;

  private HashMap     groupsByID;
  private HashMap     groupsByName;

  /** --- */
  public AccessControllerImpl() {

    sessions = new HashMap();
    keepAliveTimes = new HashMap();

    usersByID =  new HashMap();
    usersByName=  new HashMap();

    groupsByID = new HashMap();
    groupsByName = new HashMap();
  }

  /** --- */
  public void open(URL url)
    throws PersistenceException,SecurityException {

    Assert.assertNotNull(url);

    //1. get connection to the database
    try {
      jdbcConn = DBHelper.connect(url);

      Assert.assertNull(jdbcConn);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("could not get DB connection ["+ sqle.getMessage() +"]");
    }
    catch(ClassNotFoundException clse) {
      throw new PersistenceException("cannot locate JDBC driver ["+ clse.getMessage() +"]");
    }

    //2. initialize group/user collections
    throw new MethodNotImplementedException();

  }

  /** --- */
  public void close()
    throws PersistenceException,SecurityException {

   throw new MethodNotImplementedException();
  }

  /** --- */
  public Group findGroup(String name)
    throws PersistenceException,SecurityException{

    Group grp = (Group)this.groupsByName.get(name);

    if (null == grp) {
      throw new SecurityException("No such group");
    }
    else {
      return grp;
    }

  }

  /** --- */
  public Group findGroup(Long id)
    throws PersistenceException,SecurityException {

    Group grp = (Group)this.groupsByID.get(id);

    if (null == grp) {
      throw new SecurityException("No such group");
    }
    else {
      return grp;
    }
  }

  /** --- */
  public User findUser(String name)
    throws PersistenceException,SecurityException {

    User usr = (User)this.usersByName.get(name);

    if (null == usr) {
      throw new SecurityException("No such user");
    }
    else {
      return usr;
    }
  }

  /** --- */
  public User findUser(Long id)
    throws PersistenceException,SecurityException {

    User usr = (User)this.usersByID.get(id);

    if (null == usr) {
      throw new SecurityException("No such user");
    }
    else {
      return usr;
    }
  }

  /** --- */
  public Session findSession(Long id)
    throws SecurityException {

    Session s = (Session)this.sessions.get(id);

    if (null==s) {
      throw new SecurityException("No such session ID!");
    }
    else {
      return s;
    }
  }

  /** --- */
  public Group createGroup(String name)
    throws PersistenceException {

    Assert.assertNotNull(name);

    //1. create group in DB
    CallableStatement stmt = null;
    Long new_id;

    try {
      stmt = this.jdbcConn.prepareCall("{ call security.create_group(?,?)} ");
      stmt.setString(1,name);
      //numbers generated from Oracle sequences are BIGINT
      stmt.registerOutParameter(2,java.sql.Types.BIGINT);
      stmt.execute();
      new_id = new Long(stmt.getLong(1));
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't get a timestamp from DB: ["+ sqle.getMessage()+"]");
    }

    //2. create GroupImpl for the new group and put in collections
    // users list is empty
    GroupImpl grp = new GroupImpl(new_id,name,new Vector(),this,this.jdbcConn);

    this.groupsByID.put(new_id,grp);
    this.groupsByName.put(new_id,grp);

    return grp;
  }

  /** --- */
  public void deleteGroup(Long id, Session s)
    throws PersistenceException,SecurityException {

    throw new MethodNotImplementedException();
  }

  /** --- */
  public void deleteGroup(Group grp, Session s)
    throws PersistenceException,SecurityException {

    throw new MethodNotImplementedException();
  }

  /** --- */
  public User createUser(String name, String passwd)
    throws PersistenceException,SecurityException {

    throw new MethodNotImplementedException();
  }

  /** --- */
  public User deleteUser(User usr, Session s)
    throws PersistenceException,SecurityException {

    throw new MethodNotImplementedException();
  }

  /** --- */
  public User deleteUser(Long id, Session s)
    throws PersistenceException,SecurityException {

    throw new MethodNotImplementedException();
  }

  /** --- */
  public Session login(String usr_name, String passwd,Long prefGroupID)
    throws PersistenceException,SecurityException {

    throw new MethodNotImplementedException();
  }

  /** --- */
  public void logout(Session s)
    throws SecurityException {

    Assert.assertNotNull(s);
    Long SID = s.getID();

    Session removedSession = (Session)this.sessions.remove(SID);
    Assert.assertNotNull(removedSession);

    Object time = this.keepAliveTimes.remove(SID);
    Assert.assertNotNull(time);
  }

  /** --- */
  public void setSessionTimeout(Session s, int timeoutMins)
    throws SecurityException {

    throw new MethodNotImplementedException();
  }

  /** --- */
  public boolean isValidSession(Session s)
    throws SecurityException {

    throw new MethodNotImplementedException();
  }

  /** --- */
/*  public void setGroupName(Group grp, String newName, Session s)
    throws PersistenceException, SecurityException {

    CallableStatement stmt = null;

    try {
      //first check the session and then check whether the user is member of the group
      if (isValidSession(s) == false) {
        throw new SecurityException("invalid session supplied");
      }

      stmt = this.jdbcConn.prepareCall("{ call security.set_group_name(?,?)} ");
      stmt.setLong(1,grp.getID().longValue());
      stmt.setString(2,newName);
      stmt.execute();
      //release stmt???
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't change user name in DB: ["+ sqle.getMessage()+"]");
    }

  }
*/

}
