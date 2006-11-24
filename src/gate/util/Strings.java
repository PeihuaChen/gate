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
  
  /**
   * A method to unescape Java strings, returning a string containing escape
   * sequences into the respective character. i.e. "\" followed by "t" is turned
   * into the tab character.
   * 
   * @param str the string to unescape
   * @return a new unescaped string of the one passed in
   */
  public static String unescape(String str) {
    if (str == null) return str;
    
    StringBuilder sb = new StringBuilder(); // string to build
    
    StringBuilder unicodeStr = new StringBuilder(4); // store unicode sequences
    
    boolean inUnicode = false;
    boolean hadSlash = false;
    
    for (char ch: str.toCharArray()) {
      if (inUnicode) {
        unicodeStr.append(ch);
        if (unicodeStr.length() == 4) {
          try {
            int unicodeValue = Integer.parseInt(unicodeStr.toString(), 16);
            sb.append((char) unicodeValue);
            unicodeStr.setLength(0);
            inUnicode = false;
            hadSlash = false;
          } catch (NumberFormatException e) {
            throw new RuntimeException("Couldn't parse unicode value: " + unicodeStr, e);
          }
        }
        continue;
      }
      if (hadSlash) {
        hadSlash = false;
        switch (ch) {
          case '\\':
            sb.append('\\');
            break;
          case '\'':
            sb.append('\'');
            break;
          case '\"':
            sb.append('"');
            break;
          case 'r':
            sb.append('\r');
            break;
          case 'f':
            sb.append('\f');
              break;
          case 't':
            sb.append('\t');
            break;
          case 'n':
            sb.append('\n');
            break;
          case 'b':
            sb.append('\b');
            break;
          case 'u':
            inUnicode = true;
            break;
          default :
            sb.append(ch);
            break;
        }
        continue;
      } else if (ch == '\\') {
        hadSlash = true;
        continue;
      }
      sb.append(ch);
    }
    if (hadSlash) {
      sb.append('\\');
    }
    return sb.toString();
  }

} // class Strings
