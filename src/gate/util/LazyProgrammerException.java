/*
 *	LazyProgrammerException.java
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
    /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

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
  static String defaultMessage = 
    " It was Valentin's fault. I never touched it.";

} // LazyProgrammerException