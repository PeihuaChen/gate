/*
 *  UnicodeType.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 * 
 *  Valentin Tablan, 03/07/2000
 *
 *  $Id$
 */

package gate.creole.tokeniser;

/** Used as an object wrapper that holds an Unicode type (the byte value of
  * the static member of java.lang.Character).
  */
class UnicodeType {
  /** Debug flag */
  private static final boolean DEBUG = false;

  int type;
  
  UnicodeType(int type){ this.type = type;}
} // class UnicodeType
