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

import gate.persist.PersistenceException;
import gate.util.MethodNotImplementedException;


public class AccessControllerImpl implements AccessController {

  private HashMap     sessions;
  private HashMap     timeouts;
  private Connection  conn;
  private String      jfbcURL;
  private String      jdbcDriverName;


  public AccessControllerImpl() {
  }

  /** --- */
  public Group findGroup(String name){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public Group findGroup(Long id){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public User findUser(String name){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public User findUser(Long id){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public Session findSession(Long id){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public Group createGroup(String name){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public void deleteGroup(Long id, Session s){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public void deleteGroup(Group grp, Session s){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public User createUser(String name, String passwd){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public User deleteUser(User usr, Session s){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public User deleteUser(Long id, Session s){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public Session login(String usr_name, String passwd,Long prefGroupID){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public void logout(Session s){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public void setSessionTimeout(Session s, int timeoutMins){

    throw new MethodNotImplementedException();
  }

  /** --- */
  public boolean isValidSession(Session s){

    throw new MethodNotImplementedException();
  }

  /* implementation private methods */

//  private
}