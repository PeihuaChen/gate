/*
 * GateException.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 * 
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 * 
 * Hamish Cunningham, 19/01/2000
 *
 * $Id$
 */

package gate.util;

/** A superclass for exceptions in the GATE packages. Can be used
  * to catch any internal exception thrown by the GATE libraries.
  * (Of course
  * other types of exception may be thrown, but these will be from other
  * sources such as the Java core API.)
  */
public class GateException extends Exception {
  /** Debug flag */
  private static final boolean DEBUG = false;

  public GateException() {
    super();
  }

  public GateException(String s) {
    super(s);
  }

  public GateException(Exception e) {
    super(e.toString());
  }
} // GateException