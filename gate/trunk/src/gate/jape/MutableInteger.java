/* 
 *  MutableInteger.java - A mutable wrapper for int, so you can return
 *                       integer values via a method parameter
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
 *  Hamish Cunningham, 24/07/98
 *
 *  $Id$
 */


package gate.jape;


/**
  * A mutable wrapper for int, so you can return
  * integer values via a method parameter. If public data members bother you
  * I suggest you get a hobby, or have more sex or something.
  */
public class MutableInteger implements java.io.Serializable
{
  /** Debug flag */
  private static final boolean DEBUG = false;

	public int value = 0;

} // class MutableInteger

