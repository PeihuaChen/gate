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


  /** --- */
  public AccessControllerImpl() {

    sessions = new HashMap();
    keepAliveTimes = new HashMap();
  }

  /** --- */
  public void open(URL url)
    throws PersistenceException,SecurityException {

    Assert.assertNotNull(url);

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

  }

  /** --- */
  public void close()
    throws PersistenceException,SecurityException {

   throw new MethodNotImplementedException();
  }

  /** --- */
  public Group findGroup(String name)
    throws PersistenceException{

    throw new MethodNotImplementedException();
  }

  /** --- */
  public Group findGroup(Long id)
    throws PersistenceException {

    throw new MethodNotImplementedException();
  }

  /** --- */
  public User findUser(String name)
    throws PersistenceException{

    throw new MethodNotImplementedException();
  }

  /** --- */
  public User findUser(Long id)
    throws PersistenceException {

    throw new MethodNotImplementedException();
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

    throw new MethodNotImplementedException();
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

  /* implementation private methods */

//  private
}