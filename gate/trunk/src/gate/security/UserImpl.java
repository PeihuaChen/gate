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
  private AccessController ac;

  /** --- */
  private Vector omListeners;

  /** --- */
  public UserImpl(Long id, String name, List groups,AccessController ac,Connection conn) {

    this.id = id;
    this.name = name;
    this.groups = groups;
    this.ac = ac;
    this.conn = conn;

    this.omListeners = new Vector();
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
    for (int i=0; i< this.omListeners.size(); i++) {
      ((ObjectModificationListener)this.omListeners.elementAt(i)).objectModified(e);
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

  public void registerObjectModificationListener(ObjectModificationListener l) {

    this.omListeners.add(l);
  }
}
