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

import gate.*;
import gate.event.*;
import gate.persist.*;
import gate.util.MethodNotImplementedException;


public class GroupImpl
  implements Group, ObjectModificationListener {

  /** --- */
  private Long    id;

  /** --- */
  private String  name;

  /** --- */
  private List    users;

  /** --- */
  private Connection conn;
  /** --- */
  private int dbType;

  /** --- */
  private AccessControllerImpl ac;

  /** --- */
  private Vector omModificationListeners;
  /** --- */
  private Vector omCreationListeners;
  /** --- */
  private Vector omDeletionListeners;



  public GroupImpl(Long id, String name, List users,AccessControllerImpl ac,Connection conn) {

    this.id = id;
    this.name = name;
    this.users = users;
    this.ac = ac;
    this.conn = conn;

    try {
      String jdbcURL = conn.getMetaData().getURL();
      this.dbType = DBHelper.getDatabaseType(jdbcURL);
      Assert.assertTrue(this.dbType == DBHelper.ORACLE_DB ||
                        this.dbType == DBHelper.POSTGRES_DB);
    }
    catch(SQLException sqex) {
      sqex.printStackTrace();
    }

    this.omModificationListeners = new Vector();
    this.omCreationListeners = new Vector();
    this.omDeletionListeners = new Vector();

    //register self as listener for the security factory events
    //of type OBJECT_DELETED (users)
    //don't forget that only AC can delete users, so he's the only
    //source of such events
    this.ac.registerObjectModificationListener(
                                this,
                                ObjectModificationEvent.OBJECT_DELETED);

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

    /** NOTE that we're returning a copy of the actuall collection of users
     *  so that someone would not accidentaly modify it */
    Vector copy = new Vector();
    copy.addAll(this.users);
    return copy;

  }


  /** --- */
  public void setName(String newName, Session s)
    throws PersistenceException,SecurityException {

    //first check the session and then check whether the user is member of the group
    if (this.ac.isValidSession(s) == false) {
      throw new SecurityException("invalid session supplied");
    }

    //2.1 check if the user is privileged
    if (false == s.isPrivilegedSession() ) {
      throw new SecurityException("insufficient privileges to change group name");
    }

    CallableStatement stmt = null;
    PreparedStatement pstmt = null;

    //Oracle / Postgres ?

    if (this.dbType == DBHelper.ORACLE_DB) {

      //1. update database
      try {

        stmt = this.conn.prepareCall(
                "{ call "+Gate.DB_OWNER+".security.set_group_name(?,?)} ");
        stmt.setLong(1,this.id.longValue());
        stmt.setString(2,newName);
        stmt.execute();
        //release stmt???
      }
      catch(SQLException sqle) {
        throw new PersistenceException("can't change group name in DB: ["+ sqle.getMessage()+"]");
      }
      finally {
        DBHelper.cleanup(stmt);
      }
    }

    else if (this.dbType == DBHelper.POSTGRES_DB) {

      try {

        String sql = "select security_set_group_name(?,?) ";
        pstmt = this.conn.prepareStatement(sql);
        pstmt.setLong(1,this.id.longValue());
        pstmt.setString(2,newName);
        pstmt.execute();
        //release stmt???
      }
      catch(SQLException sqle) {
        throw new PersistenceException("can't change group name in DB: ["+ sqle.getMessage()+"]");
      }
      finally {
        DBHelper.cleanup(pstmt);
      }

    }

    else {
      throw new IllegalArgumentException();
    }

    //2. update memebr variable
    this.name = newName;

    //3. create ObjectModificationEvent
    ObjectModificationEvent e = new ObjectModificationEvent(
                                          this,
                                          ObjectModificationEvent.OBJECT_MODIFIED,
                                          this.OBJECT_CHANGE_NAME);


    //4. fire ObjectModificationEvent for all who care
    this.fireObjectModifiedEvent(e);

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
    if (false == this.ac.isValidSession(s)) {
      throw new SecurityException("invalid session provided");
    }

    //2.1 check if the user is privileged
    if (false == s.isPrivilegedSession() ) {
      throw new SecurityException("insufficient privileges to add users");
    }

    //3. update DB
    CallableStatement stmt = null;
    PreparedStatement pstmt = null;

    //Oracle / Postgres ?

    if (this.dbType == DBHelper.ORACLE_DB) {

      try {
        stmt = this.conn.prepareCall(
                  "{ call "+Gate.DB_OWNER+".security.add_user_to_group(?,?)} ");
        stmt.setLong(1,this.id.longValue());
        stmt.setLong(2,usr.getID().longValue());
        stmt.execute();
        //release stmt???
      }
      catch(SQLException sqle) {
        throw new PersistenceException("can't add user to group in DB: ["+ sqle.getMessage()+"]");
      }
      finally {
        DBHelper.cleanup(stmt);
      }
    }

    else if (this.dbType == DBHelper.POSTGRES_DB) {

      try {
        String sql = "select security_add_user_to_group(?,?) ";
        pstmt = this.conn.prepareStatement(sql);
        pstmt.setLong(1,this.id.longValue());
        pstmt.setLong(2,usr.getID().longValue());
        pstmt.execute();
        //release stmt???
      }
      catch(SQLException sqle) {
        throw new PersistenceException("can't add user to group in DB: ["+ sqle.getMessage()+"]");
      }
      finally {
        DBHelper.cleanup(pstmt);
      }

    }

    else {
      throw new IllegalArgumentException();
    }


    //4. create ObjectModificationEvent
    ObjectModificationEvent e = new ObjectModificationEvent(
                                          this,
                                          ObjectModificationEvent.OBJECT_MODIFIED,
                                          this.OBJECT_CHANGE_ADDUSER);

    //5. update usr collection
    this.users.add(usr);

    //6. notify user about the change
    ((ObjectModificationListener)usr).objectModified(e);

    //7. fire ObjectModificationEvent for all other who care
    fireObjectModifiedEvent(e);
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

    //2.1 check if the user is privileged
    if (false == s.isPrivilegedSession() ) {
      throw new SecurityException("insufficient privileges to remove users");
    }

    //3. update DB
    CallableStatement stmt = null;
    PreparedStatement pstmt = null;

    //Oracle / Postgres ?

    if (this.dbType == DBHelper.ORACLE_DB) {
      try {
        stmt = this.conn.prepareCall(
                  "{ call "+Gate.DB_OWNER+".security.remove_user_from_group(?,?)} ");
        stmt.setLong(1,this.id.longValue());
        stmt.setLong(2,usr.getID().longValue());
        stmt.execute();
        //release stmt???
      }
      catch(SQLException sqle) {
        throw new PersistenceException("can't remove user from group in DB: ["+ sqle.getMessage()+"]");
      }
      finally {
        DBHelper.cleanup(stmt);
      }
    }

    else if (this.dbType == DBHelper.POSTGRES_DB) {
      try {
        String sql = "select security_remove_user_from_group(?,?) ";
        pstmt = this.conn.prepareStatement(sql);
        pstmt.setLong(1,this.id.longValue());
        pstmt.setLong(2,usr.getID().longValue());
        pstmt.execute();
        //release stmt???
      }
      catch(SQLException sqle) {
        throw new PersistenceException("can't remove user from group in DB: ["+ sqle.getMessage()+"]");
      }
      finally {
        DBHelper.cleanup(pstmt);
      }
    }

    else {
      throw new IllegalArgumentException();
    }

    //4. create ObjectModificationEvent
    ObjectModificationEvent e = new ObjectModificationEvent(
                                          this,
                                          ObjectModificationEvent.OBJECT_MODIFIED,
                                          this.OBJECT_CHANGE_REMOVEUSER);

    //5. update usr collection
    this.users.remove(usr);

    //6. notify user about the change
    ((ObjectModificationListener)usr).objectModified(e);

    //7. fire ObjectModificationEvent for all other who care
    fireObjectModifiedEvent(e);
  }


  //ObjectModificationListener interface
  public void objectCreated(ObjectModificationEvent e) {

    //ignore, we don't care about creations
    return;
  }

  public void objectModified(ObjectModificationEvent e) {

    //ignore, we don't care about modifications
    return;
  }

  public void objectDeleted(ObjectModificationEvent e) {

    if (e.getSource() instanceof User) {

      User usr = (User)e.getSource();
      //check if the user being deleted is one we contain
      if (true == this.users.contains(usr)) {
        this.users.remove(usr);
      }

    }
  }

  public void processGateEvent(GateEvent e){
    throw new MethodNotImplementedException();
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
    Assert.assertTrue(obj instanceof Group);

    Group group2 = (Group)obj;

    return (this.id.equals(group2.getID()));
  }


  public void registerObjectModificationListener(ObjectModificationListener l,
                                                 int eventType) {

    if (eventType != ObjectModificationEvent.OBJECT_CREATED &&
        eventType != ObjectModificationEvent.OBJECT_DELETED &&
        eventType != ObjectModificationEvent.OBJECT_MODIFIED) {

        throw new IllegalArgumentException();
    }

    switch(eventType) {
      case ObjectModificationEvent.OBJECT_CREATED :
        //we never generate such events
        Assert.fail();
//        this.omCreationListeners.add(l);
        break;
      case ObjectModificationEvent.OBJECT_DELETED :
        //we never generate such events
        Assert.fail();
//        this.omDeletionListeners.add(l);
        break;
      case ObjectModificationEvent.OBJECT_MODIFIED :
        this.omModificationListeners.add(l);
        break;
      default:
        Assert.fail();
    }

  }

  private void fireObjectModifiedEvent(ObjectModificationEvent e) {

    //sanity check
    if (e.getType() != ObjectModificationEvent.OBJECT_MODIFIED) {
      throw new IllegalArgumentException();
    }

    for (int i=0; i< this.omModificationListeners.size(); i++) {
      ((ObjectModificationListener)omModificationListeners.elementAt(i)).objectModified(e);
    }
  }

  public void unregisterObjectModificationListener(ObjectModificationListener l,
                                                   int eventType) {

    if (eventType != ObjectModificationEvent.OBJECT_CREATED &&
        eventType != ObjectModificationEvent.OBJECT_DELETED &&
        eventType != ObjectModificationEvent.OBJECT_MODIFIED) {

        throw new IllegalArgumentException();
    }

    switch(eventType) {
      case ObjectModificationEvent.OBJECT_CREATED :
        this.omCreationListeners.remove(l);
        break;
      case ObjectModificationEvent.OBJECT_DELETED :
        this.omDeletionListeners.remove(l);
        break;
      case ObjectModificationEvent.OBJECT_MODIFIED :
        this.omModificationListeners.remove(l);
        break;
      default:
        Assert.fail();
    }
  }

  /*package*/ void setUsers(Vector userIDs) {

    for (int i=0; i< userIDs.size(); i++) {
      Long usr_id = (Long)userIDs.elementAt(i);
      User usr = null;

      try {
        usr = (User)this.ac.findUser(usr_id);
      }
      catch(SecurityException se) {
        Assert.fail();
      }
      catch(PersistenceException se) {
        Assert.fail();
      }

      //is valid?
      Assert.assertNotNull(usr);
      Assert.assertTrue(usr instanceof User);
      //add to our collection, which was empty so far
      this.users.add(usr);
    }


  }

}
