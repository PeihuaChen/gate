/*
 *  ExecutionException.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 23/Oct/2000
 *
 *  $Id$
 */

package gate.creole;

import gate.util.*;

/** This exception indicates failure during <CODE>run()</CODE>
  * invocations on ProcessingResources.
  * These cannot be thrown at run time because <CODE>run()</CODE>
  * is inheritted from  <CODE>runnable</CODE> and doesn't throw anything.
  */
public class ExecutionException extends GateException {
  /** Debug flag */
  private static final boolean DEBUG = false;

  public ExecutionException() {
    super();
  }

  public ExecutionException(String s) {
    super(s);
  }

  public ExecutionException(Exception e) {
    super(e.toString());
  }
} // ExecutionException
