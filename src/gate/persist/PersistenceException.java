/*
 *  PersistenceException.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 19/Jan/2001
 *
 *  $Id$
 */

package gate.persist;

import gate.util.*;

/** This exception indicates failure during persistence operations.
  */
public class PersistenceException extends GateException {
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Default construction */
  public PersistenceException() { super(); }

  /** Construction from string */
  public PersistenceException(String s) { super(s); }

  /** Construction from exception */
  public PersistenceException(Exception e) { super(e.toString()); }

} // PersistenceException
