/*
 *	LazyProgrammerException.java
 *
 *	Hamish Cunningham, 14/Feb/00
 *
 *	$Id$
 */

package gate.util;

/** What to throw in a method that hasn't been implemented yet. 
  * Yes, there are good reasons never to throw RuntimeExceptions
  * and thereby sidestep Java's exception checking mechanism. But
  * we're so lazy we don't care. And anyway, none of these are
  * ever supposed to make it into released versions (who are we
  * kidding?).
  */
public class LazyProgrammerException extends RuntimeException {

  /** In a fit of complete laziness we didn't even document this
    * class properly.
    */
  public LazyProgrammerException() {
    super(defaultMessage);
  }

  /** In a fit of complete laziness we didn't even document this
    * class properly.
    */
  public LazyProgrammerException(String s) {
    super(s + defaultMessage);
  }

  /** In a fit of complete laziness we didn't even document this
    * class properly.
    */
  String defaultMessage = 
    " It was Valentin's fault. I never touched it.";

} // LazyProgrammerException
