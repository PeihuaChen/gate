/*
 *	NoSuchObjectException.java
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
 *	Valentin Tablan, 06 Mar 2000
 *
 *  $Id$
 */

package gate.util;

/**Raised when there is an attempt to read an inexistant object from the
  *database(i.e. when an invalid object ID occurs).
  */
public class NoSuchObjectException extends GateException {
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  public NoSuchObjectException() {
  }

  public NoSuchObjectException(String s) {
    super(s);
  }

} // NoSuchObjectException