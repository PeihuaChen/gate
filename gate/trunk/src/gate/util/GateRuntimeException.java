/*
 * GateRuntimeException.java
 *
 * Copyright (c) 1998-2004, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan, 03/11/2000
 *
 * $Id$
 */
package gate.util;

/**
 * Exception used to signal a runtime exception within Gate.
 */
public class GateRuntimeException extends RuntimeException {

  public GateRuntimeException() {
  }

  public GateRuntimeException(String message) {
    super(message);
  }
  
  public GateRuntimeException(String message, Throwable cause) {
    super(message);
    this.throwable = cause;
  }
  
  public GateRuntimeException(Throwable e) {
    this.throwable = e;
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
    s.flush();
    super.printStackTrace(s);
    s.print("  Caused by:\n");
    if(throwable != null) throwable.printStackTrace(s);
  }

  /**
   * Overriden so we can print the enclosed exception's stacktrace too.
   */
  public void printStackTrace(java.io.PrintWriter s) {
    s.flush();
    super.printStackTrace(s);
    s.print("  Caused by:\n");
    if(throwable != null) throwable.printStackTrace(s);
  }
  
  
  Throwable throwable;  
}