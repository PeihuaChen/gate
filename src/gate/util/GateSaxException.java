/*
 * GateSaxException.java
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
 * Cristian URSU, 23/OCT/2000
 *
 * $Id$
 */

package gate.util;

import org.xml.sax.*;
/** An inherited class from  SAX exception in the GATE packages. Can be used
  * to catch any internal exception thrown by the GATE SAX libraries.
  */
public class GateSaxException extends org.xml.sax.SAXException {
  /** Debug flag */
  private static final boolean DEBUG = false;

  public GateSaxException(String aMessage, Exception anException) {
    super(aMessage,anException);
  }

  public GateSaxException(String aMessage) {
    super(aMessage);
  }

  public GateSaxException(Exception anException) {
    super(anException.toString());
  }
} // GateSaxException