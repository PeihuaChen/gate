/*
 *  JapeException.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 23/02/2000
 *
 *  $Id$
 */

package gate.jape;

import gate.annotation.*;
import gate.util.*;

/** Superclass of all JAPE exceptions. */
public class JapeException extends GateException {

  /** Debug flag */
  private static final boolean DEBUG = false;

  public JapeException(String message) {
    super(message);
  }

  public JapeException() {
    super();
  }

} // class JapeException
