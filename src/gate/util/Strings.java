/*
 *  Strings.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
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

import java.io.*;

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

  /** Local fashion for path separators. */
  private static String pathSep = System.getProperty("path.separator");

  /** Get local fashion for path separators (e.g. ":"). */
  public static String getPathSep() { return pathSep; }

  /** Local fashion for file separators. */
  private static String fileSep = System.getProperty("file.separator");

  /** Get local fashion for file separators (e.g. "/"). */
  public static String getFileSep() { return fileSep; }

  /** Add n pad characters to pad. */
  public static String addPadding(String pad, int n) {
    StringBuffer s = new StringBuffer(pad);
    for(int i = 0; i < n; i++)
      s.append(padChar);

    return s.toString();
  } // padding

  /** Helper method to add line numbers to a string */
  public static String addLineNumbers(String text) {
    // construct a line reader for the text
    BufferedReader reader = new BufferedReader(new StringReader(text));
    String line = null;
    StringBuffer result = new StringBuffer();

    try {
      for(int lineNum = 1; ( line = reader.readLine() ) != null; lineNum++) {
        String pad;
        if(lineNum < 10) pad = " ";
        else pad = "";
        result.append(pad + lineNum + "  " + line + Strings.getNl());
      }
    } catch(IOException ie) { }

    return result.toString();
  } // addLineNumbers

} // class Strings
