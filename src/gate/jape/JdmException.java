/*
 *  JdmException.java
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
 *  Kalina Bontcheva, 23/02/2000
 *
 *  $Id$
 *
 *  Description:  This is JDM aimed at repeating the functionality of GDM
 */

package gate.jape;

/**
  * THIS CLASS SHOULDN'T BE HERE. Please let's all ignore it, and maybe
  * it will go away.
  */
public class JdmException extends gate.util.GateException {

  /** Debug flag */
  private static final boolean DEBUG = false;

	public JdmException() {
  	super();
  }

  public JdmException(String s) {
  	super(s);
  }
}