/*
 *  AccessControler.java
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

import gate.persist.PersistenceException;


public interface AccessControler {

  /** --- */
  public Group findGroup(String name);

  /** --- */
  public Group findGroup(Long id);

  /** --- */
  public User findUser(String name);

  /** --- */
  public User findUser(Long id);

  /** --- */
  public Session findSession(Long id);

  /** --- */
  public Group createGroup(String name);

  /** --- */
  public void deleteGroup(Long id, Session s);

  /** --- */
  public void deleteGroup(Group grp, Session s);

  /** --- */
  public User createUser(String name, String passwd);

  /** --- */
  public User deleteUser(User usr, Session s);

  /** --- */
  public User deleteUser(Long id, Session s);

  /** --- */
  public Session login(String usr_name, String passwd,Long prefGroupID);

  /** --- */
  public void logout(Session s);

  /** --- */
  public void setSessionTimeout(Session s, int timeoutMins);

}