/*
 *  UserImpl.java
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
import gate.util.*;


public class UserImpl
  implements User, ObjectModificationListener {

  /** --- */
  private Long    id;

  /** --- */
  private String  name;

  /** --- */
  private List    groups;

  /** --- */
  private Connection conn;

  /** --- */
  private AccessControllerImpl ac;

  /** --- */
  private Vector omModificationListeners;
  /** --- */
  private Vector omCreationListeners;
  /** --- */
  private Vector omDeletionListeners;


  /** --- */
  public UserImpl(Long id, String name, List groups,AccessControllerImpl ac,Connection conn) {

    this.id = id;
    this.name = name;
    this.groups = groups;
    this.ac = ac;
    this.conn = conn;

    this.omModificationListeners = new Vector();
    this.omCreationListeners = new Vector();
    this.omDeletionListeners = new Vector();

    //register self as listener for the security factory events
    //of type OBJECT_DELETED (groups)
    //don't forget that only AC can delete groups, so he's the only
    //source of such events
    this.ac.registerObjectModificationListener(
                                this,
                                ObjectModificationEvent.OBJECT_DELETED);

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
      //1.  check the session
      if (this.ac.isValidSession(s) == false || s.getID() != this.id) {
        throw new SecurityException("invalid session supplied");
      }

      //2. update database

      stmt = this.conn.prepareCall(
              "{ call "+Gate.DB_OWNER+".security.set_user_name(?,?)} ");
      stmt.setLong(1,this.id.longValue());
      stmt.setString(2,newName);
      stmt.execute();
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't change user name in DB: ["+ sqle.getMessage()+"]");
    }
    finally {
      DBHelper.cleanup(stmt);
    }

    //4. create ObjectModificationEvent
    ObjectModificationEvent e = new ObjectModificationEvent(
                                          this,
                                          ObjectModificationEvent.OBJECT_MODIFIED,
                                          this.OBJECT_CHANGE_NAME);

    //5. update member variable
    this.name = newName;

    //6. fire ObjectModificationEvent for all who care
    fireObjectModifiedEvent(e);
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

      stmt = this.conn.prepareCall(
              "{ call "+Gate.DB_OWNER+".security.set_user_password(?,?)} ");
      stmt.setLong(1,this.id.longValue());
      stmt.setString(2,newPass);
      stmt.execute();
      //release stmt???
    }
    catch(SQLException sqle) {
      throw new PersistenceException("can't change user password in DB: ["+ sqle.getMessage()+"]");
    }

  }

  /**
   *
   *  this one is necessary for the contains() operations in Lists
   *  It is possible that two users have two different UserImpl that refer
   *  to the very same user in the DB, because they got it fromt he security
   *  factory at different times. So we assume that two instances refer the same
   *  GATE user if ID1==ID2 && NAME1==NAME2
   *
   *  */
  public boolean equals(Object obj)
  {
    Assert.assert(obj instanceof User);

    User usr2 = (User)obj;

    return (this.id.equals(usr2.getID()));
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
        this.omCreationListeners.add(l);
        break;
      case ObjectModificationEvent.OBJECT_DELETED :
        this.omDeletionListeners.add(l);
        break;
      case ObjectModificationEvent.OBJECT_MODIFIED :
        this.omModificationListeners.add(l);
        break;
      default:
        Assert.fail();
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


  private void fireObjectModifiedEvent(ObjectModificationEvent e) {

    //sanity check
    if (e.getType() != ObjectModificationEvent.OBJECT_MODIFIED) {
      throw new IllegalArgumentException();
    }

    for (int i=0; i< this.omModificationListeners.size(); i++) {
      ((ObjectModificationListener)omModificationListeners.elementAt(i)).objectModified(e);
    }
  }

  //ObjectModificationListener interface
  public void objectCreated(ObjectModificationEvent e) {
    //ignore, we don't care about creations
    return;
  }

  public void objectModified(ObjectModificationEvent e) {

    //only groups can disturb the user
/*    Assert.assert(e.getSubType() == Group.OBJECT_CHANGE_ADDUSER ||
                  e.getSubType() == Group.OBJECT_CHANGE_REMOVEUSER ||
                  e.getSubType() == Group.OBJECT_CHANGE_NAME);
*/
    //we get this event only if a group adds/removes user to it
    Group grp = (Group)e.getSource();

    switch(e.getSubType()) {

      case Group.OBJECT_CHANGE_ADDUSER:

        //1.check that the groupis not already in collection
        Assert.assert(false == this.groups.contains(grp));
        //2.add group to collection
        this.groups.add(grp);
        //3. the group has laredy registered
        //the user as listener for this group
        ;
        break;

      case Group.OBJECT_CHANGE_REMOVEUSER:
        //1.check that the group is in collection
        Assert.assert(true == this.groups.contains(grp));
        //2.remove group from collection
        this.groups.remove(grp);
        //3. the group has laredy UNregistered
        //the user as listener for this group
        ;
        break;

      case Group.OBJECT_CHANGE_NAME:
        //do nothing
        break;

      default:
        throw new IllegalArgumentException();
    }


  }

  public void objectDeleted(ObjectModificationEvent e) {

    if (e.getSource() instanceof Group) {

      Group grp = (Group)e.getSource();
      //check if the Group being deleted is one we belong to
      if (true == this.groups.contains(grp)) {
        this.groups.remove(grp);
      }

    }
  }

  public void processGateEvent(GateEvent e){
    throw new MethodNotImplementedException();
  }

}
