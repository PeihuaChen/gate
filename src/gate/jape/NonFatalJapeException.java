/*
 *  NonFatalJapeException.java
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Mark A. Greenwood, 19/10/2009
 *
 */
package gate.jape;

public class NonFatalJapeException extends JapeException {

  public NonFatalJapeException(Throwable cause) {
    super(cause);
  }
  
  public NonFatalJapeException(String message, Throwable cause) {
    super(message, cause);
  }
}
