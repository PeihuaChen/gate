/*
 *  Strings.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Hamish Cunningham, 22/02/2000
 *
 *  $Id$
 */

package gate.util;

/** Some utilities for use with Strings. */
public class Strings {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** What character to pad with. */
  private static char padChar = ' ';

  /** Local fashion for newlines this year. */
  private static String newline = System.getProperty("line.separator");

  /** Get local fashion for newlines. */
  public static String getNl() { return newline; }

  /** Add n pad characters to pad. */
  public static String addPadding(String pad, int n) {
    StringBuffer s = new StringBuffer(pad);
    for(int i = 0; i < n; i++)
      s.append(padChar);

    return s.toString();
  } // padding

} // class Strings
