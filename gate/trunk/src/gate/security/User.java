/*
 *  User.java
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

import gate.persist.PersistenceException;


public interface User {

  /** --- */
  public Long getID();

  /** --- */
  public String getName();

  /** --- */
  public List getGroups();

  /** --- */
  public void setName(String newName, Long sessionID)
    throws PersistenceException,SecurityException;

  /** --- */
  public void setPassword(String newPass, Long sessionID)
    throws PersistenceException,SecurityException;
}