/*
 * GateException.java
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
