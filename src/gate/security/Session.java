/*
 *  Session.java
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

import gate.persist.PersistenceException;

public interface Session {

  /** returns the session ID */
  public Long getID();

  /** returns the user associated with the session */
  public User getUser();

  /** returns the group associated with the session */
  public Group getGroup();

  /** TRUE if user associated with the session is in the
   *  ADMINS user group, otherwise FALSE */
  public boolean isPrivilegedSession();


}
