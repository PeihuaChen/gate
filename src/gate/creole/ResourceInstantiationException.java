/*
 *  ResourceInstantiationException.java
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

/** This exception indicates failure during instantiation of resources,
  * which may be due to a number of causes:
  * <UL>
  * <LI>
  * the resource metadata contains parameters that aren't available on
  * the resource;
  * <LI>
  * the class for the resource cannot be found (e.g. because a Jar URL was
  * incorrectly specified);
  * <LI>
  * because access to the resource class is denied by the class loader;
  * <LI>
  * because of insufficient or incorrect resource metadata.
  * </UL>
  */
public class ResourceInstantiationException extends GateException {
  /** Debug flag */
  private static final boolean DEBUG = false;
  private Exception exception = null;

  public ResourceInstantiationException() {
    super();
  }

  public ResourceInstantiationException(String s) {
    super(s);
  }

  public ResourceInstantiationException(Exception e) {
    this.exception = e;
  }

  /**
   * Overriden so we can print the enclosed exception's stacktrace too.
   */
  public void printStackTrace(){
    printStackTrace(System.err);
  }

  /**
   * Overriden so we can print the enclosed exception's stacktrace too.
   */
  public void printStackTrace(java.io.PrintStream s) {
    super.printStackTrace(s);
    s.print("  Caused by:\n");
    if(exception != null) exception.printStackTrace(s);
  }

  /**
   * Overriden so we can print the enclosed exception's stacktrace too.
   */
  public void printStackTrace(java.io.PrintWriter s) {
    super.printStackTrace(s);
    s.print("  Caused by:\n");
    if(exception != null) exception.printStackTrace(s);
  }

} // ResourceInstantiationException
