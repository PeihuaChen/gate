/*
 *  NoSuchObjectException.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Valentin Tablan, 06 Mar 2000
 *
 *  $Id$
 */

package gate.util;

/** Raised when there is an attempt to read an inexistant object from the
  * database(i.e. when an invalid object ID occurs).
  */
public class NoSuchObjectException extends GateException {

  /** Debug flag */
  private static final boolean DEBUG = false;

  public NoSuchObjectException() {
  }

  public NoSuchObjectException(String s) {
    super(s);
  }

} // NoSuchObjectException
