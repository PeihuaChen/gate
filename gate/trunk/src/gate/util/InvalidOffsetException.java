/*
 *	InvalidOffsetException.java
 *
 *	Valentin Tablan, Jan/2000
 *
 *	$Id$
 */

package gate.util;

/** Used to signal an attempt to create a node with an invalid offset.
  * An invalid offset is a negative value, or
  * an offset bigger than the document size, or a start greater than an end. 
  */
public class InvalidOffsetException extends GateException {

  public InvalidOffsetException() {
  }

  public InvalidOffsetException(String s) {
    super(s);
  }

} // InvalidOffsetException
