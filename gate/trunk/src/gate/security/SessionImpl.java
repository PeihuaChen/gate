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
 */

package gate.security;

import gate.persist.PersistenceException;

public class SessionImpl implements Session {

  /** --- */
  private Long  id;

  /** --- */
  private User  user;

  /** --- */
  private Group group;

  /** --- */
  private int   timeout;

  /** --- */
  public SessionImpl(Long id,User usr,Group grp, int timeout) {

    this.id = id;
    this.user = usr;
    this.group = grp;
    this.timeout = timeout;
  }

  /* Session interface */

  /** --- */
  public Long getID() {

    return this.id;
  }

  /** --- */
  public User getUser() {

    return this.user;
  }

  /** --- */
  public Group getGroup() {

    return this.group;
  }

  /* misc methods */
  public int getTimeout() {

    return this.timeout;
  }


}