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

  public static final int DEFAULT_SESSION_TIMEOUT_MIN = 60;

  public static final int LOGIN_OK = 1;
  public static final int LOGIN_FAILED = 2;

  /* these should be the same as in the security PL/SQL package definition */
  private static final int DB_DUPLICATE_GROUP_NAME = -20101;
  private static final int DB_DUPLICATE_USER_NAME = -20102;
  private static final int DB_INVALID_USER_NAME = -20103;
  private static final int DB_INVALID_USER_PASS = -20104;



  private HashMap     sessions;
  private HashMap     sessionLastUsed;
  private HashMap     sessionTimeouts;

  private Connection  jdbcConn;
  private URL         jdbcURL;

  private Vector      users;
//  private Vector      groups;

  private HashMap     usersByID;
  private HashMap     usersByName;

  private HashMap     groupsByID;
  private HashMap     groupsByName;

  private static long MY_VERY_SECRET_CONSTANT;

  static {
    MY_VERY_SECRET_CONSTANT = Math.round(Math.random()*1024) *
                                Math.round(Math.random()*1024) +
                                    Math.round(Math.PI * Math.E);
  }

  /** --- */
  public AccessControllerImpl() {

    sessions = new HashMap();
    sessionLastUsed = new HashMap();
    sessionTimeouts = new HashMap();

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

    return grp;
  }

  /** --- */
  public Group findGroup(Long id)
    throws PersistenceException,SecurityException {

    Group grp = (Group)this.groupsByID.get(id);

    if (null == grp) {
      throw new SecurityException("No such group");
    }

    return grp;
  }

  /** --- */
  public User findUser(String name)
    throws PersistenceException,SecurityException {

    User usr = (User)this.usersByName.get(name);

    if (null == usr) {
      throw new SecurityException("No such user");
    }

    return usr;
  }

  /** --- */
  public User findUser(Long id)
    throws PersistenceException,SecurityException {

    User usr = (User)this.usersByID.get(id);

    if (null == usr) {
      throw new SecurityException("No such user");
    }

    return usr;
  }

  /** --- */
  public Session findSession(Long id)
    throws SecurityException {

    Session s = (Session)this.sessions.get(id);

    if (null==s) {
      throw new SecurityException("No such session ID!");
    }

    return s;
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
      throw new PersistenceException("can't create a group in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
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

    Group grp = (Group)this.groupsByID.get(id);
    if (null == grp) {
      throw new SecurityException("incorrect group id supplied ( id = ["+id+"])");
    }

    //delegate
    deleteGroup(grp,s);
  }

  /** --- */
  public void deleteGroup(Group grp, Session s)
    throws PersistenceException,SecurityException {

    //1. check session
    if (false == isValidSession(s)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. is user member of group?
    User usr = s.getUser();
    if (false == grp.getUsers().contains(usr)) {
      throw new SecurityException("user is not a member of the group");
    }

    //3. delete in DB
    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall("{ call security.delete_group(?) } ");
      stmt.setLong(1,grp.getID().longValue());
      stmt.execute();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't delete a group from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

    //4. delete from collections
    this.groupsByID.remove(grp.getID());
    this.groupsByName.remove(grp.getName());

    //5. notify all other listeners
    throw new MethodNotImplementedException();

  }

  /** --- */
  public User createUser(String name, String passwd)
    throws PersistenceException,SecurityException {

    Assert.assertNotNull(name);

    //1. create user in DB
    CallableStatement stmt = null;
    Long new_id;

    try {
      stmt = this.jdbcConn.prepareCall("{ call security.create_user(?,?,?)} ");
      stmt.setString(1,name);
      stmt.setString(2,passwd);
      //numbers generated from Oracle sequences are BIGINT
      stmt.registerOutParameter(3,java.sql.Types.BIGINT);
      stmt.execute();
      new_id = new Long(stmt.getLong(3));
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't create a user in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

    //2. create UserImpl for the new user and put in collections
    // groups list is empty
    UserImpl usr = new UserImpl(new_id,name,new Vector(),this,this.jdbcConn);

    this.usersByID.put(new_id,usr);
    this.usersByName.put(new_id,usr);

    return usr;
  }

  /** --- */
  public void deleteUser(User usr, Session s)
    throws PersistenceException,SecurityException {

    //1. check session
    if (false == isValidSession(s)) {
      throw new SecurityException("invalid session supplied");
    }

    //2. is user to be deleted the same from the session?
    User sessionUsr = s.getUser();
    //equals() is custom, so "==" is ok
    if (sessionUsr != usr) {
      throw new SecurityException("session user is not the user to be deleted");
    }

    //3. delete in DB
    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall("{ call security.delete_user(?) } ");
      stmt.setLong(1,usr.getID().longValue());
      stmt.execute();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't delete user from DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

    //4. delete from collections
    this.usersByID.remove(usr.getID());
    this.usersByName.remove(usr.getName());

    //5. delete the user's session
    logout(s);

    //6. notify all other listeners
    throw new MethodNotImplementedException();

  }

  /** --- */
  public void deleteUser(Long id, Session s)
    throws PersistenceException,SecurityException {

    User usr = (User)usersByID.get(id);
    if (null == usr) {
      throw new SecurityException("incorrect user id supplied ( id = ["+id+"])");
    }

    //delegate
    deleteUser(usr,s);
  }

  /** --- */
  public Session login(String usr_name, String passwd,Long prefGroupID)
    throws PersistenceException,SecurityException {

    //1. check the user locally
    User usr = (User)this.usersByName.get(usr_name);
    if (null == usr) {
      throw new SecurityException("no such user (username=["+usr_name+"])");
    }

    //2. check group localy
    Group grp = (Group)this.groupsByID.get(prefGroupID);
    if (null == grp) {
      throw new SecurityException("no such group (id=["+prefGroupID+"])");
    }

    //2. check user/pass in DB
    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall("{ call security.login(?,?,?)} ");
      stmt.setString(1,usr_name);
      stmt.setString(2,passwd);
      stmt.setLong(3,prefGroupID.longValue());
    }
    catch(SQLException sqle) {
      switch(sqle.getErrorCode())
      {
        case DB_INVALID_USER_NAME :
          throw new SecurityException("Login failed: incorrect user");
        case DB_INVALID_USER_PASS :
          throw new SecurityException("Login failed: incorrect password");
        default:
          throw new PersistenceException("can't login user, DB error is: ["+
                                          sqle.getMessage()+"]");
      }
    }
    finally {
      DBHelper.cleanup(stmt);
    }


    //3. create a Session and set User/Group
    Long sessionID = createSessionID();
    while (this.sessions.containsKey(sessionID)) {
      sessionID = createSessionID();
    }

    SessionImpl s = new SessionImpl(sessionID,
                                    usr,
                                    grp,
                                    DEFAULT_SESSION_TIMEOUT_MIN);


    //4. set the session timeouts and keep alives
    this.sessionTimeouts.put(sessionID,new Long(DEFAULT_SESSION_TIMEOUT_MIN));
    touchSession(s); //this one changes the keepAlive time

    throw new MethodNotImplementedException();
  }

  /** --- */
  public void logout(Session s)
    throws SecurityException {

    Assert.assertNotNull(s);
    Long SID = s.getID();

    Session removedSession = (Session)this.sessions.remove(SID);
    Assert.assertNotNull(removedSession);

    Object lastUsed = this.sessionLastUsed.remove(SID);
    Assert.assertNotNull(lastUsed);

    Object timeout = this.sessionTimeouts.remove(SID);
    Assert.assertNotNull(timeout);

  }

  /** --- */
  public void setSessionTimeout(Session s, int timeoutMins)
    throws SecurityException {

    throw new MethodNotImplementedException();
  }

  /** --- */
  public boolean isValidSession(Session s) {

    //1. do we have such session?
    Session s1 = (Session)this.sessions.get(s.getID());

    if (null == s1) {
      return false;
    }

    //2. has it expired meanwhile?
    Assert.assertNotNull(this.sessionLastUsed.get(s.getID()));

    long lastUsedMS = ((Long)this.sessionLastUsed.get(s.getID())).longValue();
    long sessTimeoutMin = ((Long)this.sessionTimeouts.get(s.getID())).longValue();
    long currTimeMS = System.currentTimeMillis();
    //timeout is in minutes
    long lastUsedMin = (currTimeMS-lastUsedMS)*1000/60;

    if (lastUsedMin > sessTimeoutMin) {
      //session expired
      return false;
    }

    //everything ok
    //touch session
    touchSession(s);

    return true;
  }


  /*  private methods */

  private void touchSession(Session s) {

    this.sessionLastUsed.put(s.getID(),  new Long(System.currentTimeMillis()));
  }


  private Long createSessionID() {

    //need a hint?
    return new Long(((System.currentTimeMillis() << 16) >> 16)*
                      (Math.round(Math.random()*1024))*
                          Runtime.getRuntime().freeMemory()*
                              MY_VERY_SECRET_CONSTANT);
  }


  private void init() {

    //1. read all groups and users from DB

    //2. create USerImpl's and GroupImpl's and put them in collections

    throw new MethodNotImplementedException();
  }
}
