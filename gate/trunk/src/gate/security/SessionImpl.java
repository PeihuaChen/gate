/*
 *  SessionImpl.java
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

import junit.framework.*;

import gate.persist.PersistenceException;

public class SessionImpl implements Session {

  /** ID of the session */
  private Long  id;

  /** User associated with the session */
  private User  user;

  /** Group associated with the session
   *  a user may be member of many groups, but at
   *  login time only one could be specified */
  private Group group;

  /** sesion timeout (in minutes)
   *  @see  AccessControllerImpl.DEFAULT_SESSION_TIMEOUT_MIN
   *  */
  private int   timeout;

  /** TRUE if user associated with the session is in the
   *  ADMINS user group, otherwise FALSE */
  private boolean isPrivileged;

  /** --- */
  public SessionImpl(Long id,User usr,Group grp, int timeout, boolean isPrivileged) {

    this.id = id;
    this.user = usr;
    this.group = grp;
    this.timeout = timeout;
    this.isPrivileged = isPrivileged;
  }

  /* Session interface */

  /** returns the session ID */
  public Long getID() {

    return this.id;
  }

  /** returns the user associated with the session */
  public User getUser() {

    return this.user;
  }

  /**
   *  returns the group associated with the session
   *  a user may be member of many groups, but at
   *  login time only one could be specified
   *
   */
  public Group getGroup() {

    return this.group;
  }

  /** TRUE if user associated with the session is in the
   *  ADMINS user group, otherwise FALSE */
  public boolean isPrivilegedSession() {

    return this.isPrivileged;
  }



  /* misc methods */


  /** returns the timeout (in minutes) of the session
   *
   *  @see  AccessControllerImpl.DEFAULT_SESSION_TIMEOUT_MIN
   *
   *  */
  public int getTimeout() {

    return this.timeout;
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
    Assert.assertTrue(obj instanceof Session);

    Session s2 = (Session)obj;

    return (this.id.equals(s2.getID()));
  }

}
