/*
 *  Matcher.java - transducer class
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 24/07/98
 *
 *  $Id$
 */


package gate.jape;

import gate.Document;


/**
  * Interface to be implemented by classes providing matching on documents,
  * e.g. PatternElement and LeftHandSide.
  */
public interface Matcher extends java.io.Serializable
{
  /** Does this element match the document at this position? */
  abstract public boolean matches(
    Document doc, int position, MutableInteger newPosition
  );

  /** Reset: clear annotation caches etc. */
  abstract public void reset();

  /** Finish: replace dynamic data structures with Java arrays; called
    * after parsing.
    */
  abstract public void finish();

} // class Matcher


// $Log$
// Revision 1.7  2005/01/11 13:51:36  ian
// Updating copyrights to 1998-2005 in preparation for v3.0
//
// Revision 1.6  2004/07/21 17:10:08  akshay
// Changed copyright from 1998-2001 to 1998-2004
//
// Revision 1.5  2004/03/25 13:01:14  valyt
// Imports optimisation throughout the Java sources
// (to get rid of annoying warnings in Eclipse)
//
// Revision 1.4  2001/09/13 12:09:50  kalina
// Removed completely the use of jgl.objectspace.Array and such.
// Instead all sources now use the new Collections, typically ArrayList.
// I ran the tests and I ran some documents and compared with keys.
// JAPE seems to work well (that's where it all was). If there are problems
// maybe look at those new structures first.
//
// Revision 1.3  2000/11/08 16:35:03  hamish
// formatting
//
// Revision 1.2  2000/10/10 15:36:36  oana
// Changed System.out in Out and System.err in Err;
// Added the DEBUG variable seted on false;
// Added in the header the licence;
//
// Revision 1.1  2000/02/23 13:46:08  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:02  hamish
// added gate2
//
// Revision 1.4  1998/11/01 21:21:38  hamish
// use Java arrays in transduction where possible
//
// Revision 1.3  1998/10/29 12:09:08  hamish
// added serializable
//
// Revision 1.2  1998/08/12 15:39:38  hamish
// added padding toString methods
//
// Revision 1.1  1998/08/03 19:51:23  hamish
// rollback added
