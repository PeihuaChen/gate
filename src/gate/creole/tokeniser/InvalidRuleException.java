/*
 *	InvalidRuleException.java
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
 *	Valentin Tablan, 21.06.2000
 *
 *	$Id$
 */

package gate.creole.tokeniser;

/** Fired when an invalid tokeniser rule is found
  */
public class InvalidRuleException extends TokeniserException {
  /** Debug flag */
  private static final boolean DEBUG = false;

  public InvalidRuleException(String s) {
    super(s);
  }

} // class InvalidRuleException
