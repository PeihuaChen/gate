/*
 *  TokeniserException.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 * 
 *  Valentin Tablan, 27/06/2000
 *
 *  $Id$
 */

package gate.creole.tokeniser;

import gate.util.*;

/** The top level exception for all the exceptions fired by the tokeniser */
public class TokeniserException extends GateException {
  /** Debug flag */
  private static final boolean DEBUG = false;

  public TokeniserException(String text){ super(text); }

} // class TokeniserException
