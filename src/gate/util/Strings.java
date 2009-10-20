/*
 *  Copyright (c) 1998-2009, The University of Sheffield.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    return addLineNumbers(text, 1);
  }
  
  public static String addLineNumbers(String text, int startLine) {
    // construct a line reader for the text
    BufferedReader reader = new BufferedReader(new StringReader(text));
    String line = null;
    StringBuffer result = new StringBuffer();

    try {
      for(int lineNum = startLine; ( line = reader.readLine() ) != null; lineNum++) {
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

  /**
   * Convert about any object to a human readable string.<br>
   * Use {@link Arrays#deepToString(Object[])} to convert an array or
   * a collection.
   * @param object object to be converted to a string
   * @return a string representation of the object, the empty string if null.
   */
  public static String toString(Object object) {
    if (object == null) {
      return "";
    } else if (object instanceof Object[]) {
      return Arrays.deepToString((Object[])object);
    } else if (object instanceof Collection) {
      return Arrays.deepToString(((Collection)object).toArray());
    } else {
      return object.toString();
    }
  }

  /**
   * If the string has the format [value, value] like with
   * {@link Arrays#deepToString(Object[])} then returns a List of String.
   *
   * @param string String to convert to a List
   * @return a List
   */
  public static List<String> toList(String string) {
    if (string == null
     || string.length() < 3) {
      return new ArrayList<String>();
    }
    return new ArrayList<String>(Arrays.asList(
      string.substring(1, string.length()-1) // remove brackets []
        .split(", "))); // split on list separator
  }

  /**
   * If the string has the format [[value, value], [value, value]]
   * like with {@link Arrays#deepToString(Object[])}
   * then returns a List of List of String.
   *
   * @param string String to convert to a List of List
   * @return a List of List
   */
  public static List<List<String>> toListOfList(String string) {
    List<List<String>> listOfList = new ArrayList<List<String>>();
    if (string == null
     || string.length() < 5) {
      listOfList.add(new ArrayList<String>());
      return listOfList;
    }
    string = string.substring(2, string.length()-2); // remove brackets [[]]
    String[] lists = string.split("\\], \\[");
    for (String list : lists) {
      // split on list separator
      listOfList.add(new ArrayList<String>(Arrays.asList(list.split(", "))));
    }
    return listOfList;
  }

  /**
   * If the string has the format {key=value, key=value} like with
   * {@link Arrays#deepToString(Object[])} then returns a Map of String*String.
   *
   * @param string String to convert to a Map
   * @return a Map
   */
  public static Map<String, String> toMap(String string) {
    Map<String, String> map = new HashMap<String, String>();
    if (string == null
     || string.length() < 3) {
      return map;
    }
    try {
      String[] entries = string.substring(1, string.length()-1).split(", "); 
      for (String entry : entries) {
        String[] keyValue = entry.split("=", 2);
        map.put(keyValue[0], keyValue[1]);
      }
    } catch(ArrayIndexOutOfBoundsException e) {
      Err.println("The string has not the format: {key=value, key=value}");
      Err.println(string);
      throw e;
    }
    return map;
  }

} // class Strings
