/*
 *	InvalidDatabaseException.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *  
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *  
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *  
 *	Valentin Tablan, 21 Feb 2000
 *
 *  $Id$
 */

package gate.util;
/**Used to signal an attempt to connect to a database in an invalid format,
  *that is a database tha does not have the right structure (see Gate2
  *documentation for details on required database structure).
  */
public class InvalidDatabaseException extends GateException {

  /** Debug flag */
  private static final boolean DEBUG = false;

  public InvalidDatabaseException() {
  }

  public InvalidDatabaseException(String s) {
    super(s);
  }

}//InvalidDatabaseException

