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

import gate.*;
import gate.event.*;
import gate.persist.*;
import gate.util.MethodNotImplementedException;


public class AccessControllerImpl
  implements AccessController, ObjectModificationListener {

  public static final int DEFAULT_SESSION_TIMEOUT_MIN = 60;

  public static final int LOGIN_OK = 1;
  public static final int LOGIN_FAILED = 2;

  /* these should be the same as in the security PL/SQL package definition */

  // user defined error numbers in Oracle are in -21000 ... -21999
  private static final int DB_ERROR_START = -20100;
  private static final int DB_DUPLICATE_GROUP_NAME = DB_ERROR_START -1;
  private static final int DB_DUPLICATE_USER_NAME = DB_ERROR_START -2;
  private static final int DB_INVALID_USER_NAME = DB_ERROR_START -3;
  private static final int DB_INVALID_USER_PASS = DB_ERROR_START -4;
  private static final int DB_INVALID_USER_GROUP = DB_ERROR_START -5;


  private HashMap     sessions;
  private HashMap     sessionLastUsed;
  private HashMap     sessionTimeouts;

  private Connection  jdbcConn;
  private URL         jdbcURL;

//private Vector      users;
//  private Vector      groups;

  private HashMap     usersByID;
  private HashMap     usersByName;

  private HashMap     groupsByID;
  private HashMap     groupsByName;

  private static Random r;

  private static long MY_VERY_SECRET_CONSTANT;
  private static final int RANDOM_MAX = 1024;

  static {
    r = new Random();
    MY_VERY_SECRET_CONSTANT = r.nextInt(RANDOM_MAX) * r.nextInt(RANDOM_MAX)
                                  + Math.round(Math.PI * Math.E);
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
  public void open(String url)
    throws PersistenceException{

    Assert.assertNotNull(url);

    try {

      //1. get connection to the database
      jdbcConn = DBHelper.connect(url);

      Assert.assertNotNull(jdbcConn);

      //2. initialize group/user collections
      //init, i.e. read users and groups from DB
      init();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("could not get DB connection ["+ sqle.getMessage() +"]");
    }
    catch(ClassNotFoundException clse) {
      throw new PersistenceException("cannot locate JDBC driver ["+ clse.getMessage() +"]");
    }
  }

  /** --- */
  public void close()
    throws PersistenceException{

   throw new MethodNotImplementedException();
   //1. deregister self as listener
   //2. delete all groups/users collections
   //3.
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
      stmt = this.jdbcConn.prepareCall(
              "{ call "+Gate.DB_OWNER+".security.create_group(?,?)} ");
      stmt.setString(1,name);
      //numbers generated from Oracle sequences are BIGINT
      stmt.registerOutParameter(2,java.sql.Types.BIGINT);
      stmt.execute();
      new_id = new Long(stmt.getLong(2));
    }
    catch(SQLException sqle) {
      throw new PersistenceException(
                "can't create a group in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

    //2. create GroupImpl for the new group and
    // users list is empty
    GroupImpl grp = new GroupImpl(new_id,name,new Vector(),this,this.jdbcConn);

    //3. register as objectModification listener for this group
    grp.registerObjectModificationListener(this);

    //4.put in collections
    this.groupsByID.put(new_id,grp);
    this.groupsByName.put(name,grp);

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
      stmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".security.delete_group(?) } ");
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
      stmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".security.create_user(?,?,?)} ");
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

    //2. create UserImpl for the new user
    // groups list is empty
    UserImpl usr = new UserImpl(new_id,name,new Vector(),this,this.jdbcConn);

    //3. register as objectModification listener for this group
    usr.registerObjectModificationListener(this);

    //4. put in collections
    this.usersByID.put(new_id,usr);
    this.usersByName.put(name,usr);

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
      stmt = this.jdbcConn.prepareCall(
                  "{ call "+Gate.DB_OWNER+".security.delete_user(?) } ");
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
      stmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".security.login(?,?,?)} ");
      stmt.setString(1,usr_name);
      stmt.setString(2,passwd);
      stmt.setLong(3,prefGroupID.longValue());
      stmt.execute();
    }
    catch(SQLException sqle) {
      switch(sqle.getErrorCode())
      {
        case DB_INVALID_USER_NAME :
          throw new SecurityException("Login failed: incorrect user");
        case DB_INVALID_USER_PASS :
          throw new SecurityException("Login failed: incorrect password");
        case DB_INVALID_USER_GROUP :
          throw new SecurityException("Login failed: incorrect group");
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

    //4. add session to sessions collection
    this.sessions.put(s.getID(),s);

    //5. set the session timeouts and keep alives
    this.sessionTimeouts.put(sessionID,new Long(DEFAULT_SESSION_TIMEOUT_MIN));
    touchSession(s); //this one changes the keepAlive time

    return s;
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

    this.sessionTimeouts.put(s.getID(),new Long(timeoutMins));
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
    long lastUsedMin = (currTimeMS-lastUsedMS)/1000*60;

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
                      (r.nextInt(RANDOM_MAX))*
                          Runtime.getRuntime().freeMemory()*
                              MY_VERY_SECRET_CONSTANT);
  }


  private boolean canDeleteGroup(Group grp)
    throws PersistenceException, SecurityException{

    //1. check group localy
    if (false == this.groupsByID.containsValue(grp)) {
      throw new SecurityException("no such group (id=["+grp.getID()+"])");
    }

    //2. check DB
    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".security.can_delete_group(?,?) }");
      stmt.setLong(1,grp.getID().longValue());
      stmt.registerOutParameter(2,java.sql.Types.INTEGER);
      stmt.execute();
      boolean res = stmt.getBoolean(2);

      return res;
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't perform document checks, DB error is: ["+
                                          sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

  }


  private boolean canDeleteUser(User usr)
    throws PersistenceException, SecurityException{

    //1. check group localy
    if (false == this.usersByID.containsValue(usr)) {
      throw new SecurityException("no such user (id=["+usr.getID()+"])");
    }

    //2. check DB
    CallableStatement stmt = null;

    try {
      stmt = this.jdbcConn.prepareCall(
                "{ call "+Gate.DB_OWNER+".security.can_delete_user(?,?) }");
      stmt.setLong(1,usr.getID().longValue());
      stmt.registerOutParameter(2,java.sql.Types.INTEGER);
      stmt.execute();
      boolean res = stmt.getBoolean(2);

      return res;
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't perform document checks, DB error is: ["+
                                          sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

  }

  private void init()
    throws PersistenceException {

    //1. read all groups and users from DB
    Statement stmt = null;
    ResultSet rs = null;
    String    sql;
    Hashtable   groupNames = new Hashtable();
    Hashtable   groupMembers= new Hashtable();
    Hashtable   userNames= new Hashtable();
    Hashtable   userGroups= new Hashtable();

    try {
      stmt = this.jdbcConn.createStatement();

      //1.1 read groups
      sql = " SELECT grp_id, " +
            "        grp_name "+
            " FROM   "+Gate.DB_OWNER+".t_group";
      rs = stmt.executeQuery(sql);



      while (rs.next()) {
        //access by index is faster
        //first column index is 1
        long grp_id = rs.getLong(1);
        String grp_name = rs.getString(2);
        groupNames.put(new Long(grp_id),grp_name);
        groupMembers.put(new Long(grp_id),new Vector());
      }
      DBHelper.cleanup(rs);


      //1.2 read users
      sql = " SELECT usr_id, " +
            "        usr_login "+
            " FROM   "+Gate.DB_OWNER+".t_user";
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        //access by index is faster
        //first column index is 1
        long usr_id = rs.getLong(1);
        String usr_name = rs.getString(2);
        userNames.put(new Long(usr_id),usr_name);
        userGroups.put(new Long(usr_id),new Vector());
      }
      DBHelper.cleanup(rs);


      //1.3 read user/group relations
      sql = " SELECT    UGRP_GROUP_ID, " +
            "           UGRP_USER_ID "+
            " FROM      "+Gate.DB_OWNER+".t_user_group " +
            " ORDER BY  UGRP_GROUP_ID asc";
      rs = stmt.executeQuery(sql);

      while (rs.next()) {
        //access by index is faster
        //first column index is 1
        Long grp_id = new Long(rs.getLong(1));
        Long usr_id = new Long(rs.getLong(2));

        //append user to group members list
        Vector currMembers = (Vector)groupMembers.get(grp_id);
        currMembers.add(usr_id);

        Vector currGroups = (Vector)userGroups.get(usr_id);
        currGroups.add(grp_id);
      }
      DBHelper.cleanup(rs);
    }
    catch(SQLException sqle) {
      throw new PersistenceException("DB error is: ["+
                                          sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(rs);
      DBHelper.cleanup(stmt);
    }

    //2. create USerImpl's and GroupImpl's and put them in collections

    //2.1 create Groups
    Enumeration enGroups = groupNames.keys();
    while (enGroups.hasMoreElements()) {
      Long grpId = (Long)enGroups.nextElement();
      Vector grpMembers = (Vector)groupMembers.get(grpId);
      String grpName = (String)groupNames.get(grpId);

      GroupImpl grp = new GroupImpl(grpId,grpName,grpMembers,this,this.jdbcConn);
      //register as listener for thsi group
      grp.registerObjectModificationListener(this);

      //add to collection
      this.groupsByID.put(grp.getID(),grp);
      this.groupsByName.put(grp.getName(),grp);
    }

    //2.1 create Users
    Enumeration enUsers = userNames.keys();
    while (enUsers.hasMoreElements()) {
      Long usrId = (Long)enUsers.nextElement();
      Vector usrGroups = (Vector)userGroups.get(usrId);
      String usrName = (String)userNames.get(usrId);

      UserImpl usr = new UserImpl(usrId,usrName,usrGroups,this,this.jdbcConn);
      //register as listener for thsi user
      usr.registerObjectModificationListener(this);

      //add to collection
      this.usersByID.put(usr.getID(),usr);
      this.usersByName.put(usr.getName(),usr);
    }
  }


  /* ObjectModificationListener methods */

  public void objectCreated(ObjectModificationEvent e) {
    throw new MethodNotImplementedException();
  }

  public void objectModified(ObjectModificationEvent e) {

    Object source = e.getSource();
    int type = e.getType();
    int subtype = e.getSubType();

    Assert.assert(source instanceof Group || source instanceof User);
    Assert.assert(type == ObjectModificationEvent.OBJECT_MODIFIED);

    if (source instanceof Group) {

      Assert.assert(subtype == Group.OBJECT_CHANGE_ADDUSER ||
                    subtype == Group.OBJECT_CHANGE_NAME ||
                    subtype == Group.OBJECT_CHANGE_REMOVEUSER);

      //the name of the group could be different now (IDs are fixed)
      if (subtype == Group.OBJECT_CHANGE_NAME) {
        //rehash
        //any better idea how to do it?
        Set mappings = this.groupsByName.entrySet();
        Iterator it = mappings.iterator();

        boolean found = false;
        while (it.hasNext()) {
          Map.Entry mapEntry = (Map.Entry)it.next();
          String key = (String)mapEntry.getKey();
          Group  grp = (Group)mapEntry.getValue();

          if (false == key.equals(grp.getName())) {
            //gotcha
            this.groupsByName.remove(key);
            this.groupsByName.put(grp.getName(),grp);
            found = true;
            break;
          }
        }

        Assert.assert(found);
      }
    }
    else {

      Assert.assert(source instanceof User);

      //the name of the user could be different now (IDs are fixed)

      Assert.assert(subtype == User.OBJECT_CHANGE_NAME ||
                    subtype == User.OBJECT_CHANGE_PASSWORD);

      //the name of the group could be different now (IDs are fixed)
      if (subtype == User.OBJECT_CHANGE_NAME) {
        //rehash
        //any better idea how to do it?
        Set mappings = this.usersByName.entrySet();
        Iterator it = mappings.iterator();

        boolean found = false;
        while (it.hasNext()) {
          Map.Entry mapEntry = (Map.Entry)it.next();
          String key = (String)mapEntry.getKey();
          User  usr = (User)mapEntry.getValue();

          if (false == key.equals(usr.getName())) {
            //gotcha
            this.groupsByName.remove(key);
            this.groupsByName.put(usr.getName(),usr);
            found = true;
            break;
          }
        }

        Assert.assert(found);
      }
    }


  }

  public void objectDeleted(ObjectModificationEvent e) {
    throw new MethodNotImplementedException();
  }

  public void processGateEvent(GateEvent e){
    throw new MethodNotImplementedException();
  }
}
