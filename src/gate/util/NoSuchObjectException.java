/*
 *	NoSuchObjectException.java
 *
 *	Valentin Tablan, 06 Mar 2000
 */

package gate.util;

/**Raised when there is an attempt to read an inexistant object from the
  *database(i.e. when an invalid object ID occurs).
  */
public class NoSuchObjectException extends GateException {

  public NoSuchObjectException() {
  }

  public NoSuchObjectException(String s) {
    super(s);
  }

} // NoSuchObjectException
