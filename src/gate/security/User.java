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
 *  $Id$
 */

package gate.security;

import java.util.*;

import gate.persist.PersistenceException;


public interface User {

  public static final int OBJECT_CHANGE_NAME        = 1001;
  public static final int OBJECT_CHANGE_PASSWORD    = 1002;

  /** --- */
  public Long getID();

  /** --- */
  public String getName();

  /** --- */
  public List getGroups();

  /** --- */
  public void setName(String newName, Session s)
    throws PersistenceException,SecurityException;

  /** --- */
  public void setPassword(String newPass, Session s)
    throws PersistenceException,SecurityException;
}
