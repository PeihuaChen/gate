/*
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
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
   * Create a String representation of a List of String with the format
   * [value, value].
   * Escape with a backslash the separator character.
   * @param list list to convert to one String
   * @return a String that represent the list
   * @see #toList(String, String)
   */
  public static String toString(List<String> list) {
    StringBuilder builder = new StringBuilder();
    builder.append('[');
    for (String element : list) {
      // escape list separator
      String escapedElement = element.replaceAll(",", "\\\\,");
      builder.append(escapedElement).append(", ");
    }
    if (builder.length() > 1) { // remove last list separator
      builder.delete(builder.length()-2, builder.length());
    }
    builder.append("]");
    return builder.toString();
  }

  /**
   * Create a String representation of a Map of String*String with the format
   * {key=value, key=value}.
   * Escape with a backslash the separator characters.
   * @param map map to convert to one String
   * @return a String that represent the map
   * @see #toMap(String)
   */
  public static String toString(Map<String, String> map) {
    StringBuilder builder = new StringBuilder();
    builder.append('{');
    for (Map.Entry<String, String> entry : map.entrySet()) {
      // escape list separator
      String escapedKey = entry.getKey().replaceAll("=", "\\\\=")
        .replaceAll(",", "\\\\,");
      String escapedValue = entry.getValue().replaceAll("=", "\\\\=")
        .replaceAll(",", "\\\\,");
      builder.append(escapedKey).append("=").append(escapedValue).append(", ");
    }
    if (builder.length() > 1) { // remove last list separator
      builder.delete(builder.length()-2, builder.length());
    }
    builder.append("}");
    return builder.toString();
  }

  /**
   * Get back a List of String from its String representation.
   * Unescape backslashed separator characters.
   * @param string String to convert to a List
   * @param separator String that delimits the element of the list
   * @return a List
   * @see #toString(java.util.List)
   */
  public static List<String> toList(String string, String separator) {
    List<String> list = new ArrayList<String>();
    if (string == null
     || string.length() < 3) {
      return list;
    }
    // remove last character
    String value = string.substring(0, string.length()-1);
    int index = 1;
    int startIndex = 1;
    int separatorIndex;
    // split on list separator
    while ((separatorIndex = value.indexOf(separator, index)) != -1) {
      // check that the separator is not an escaped character
      if (value.charAt(separatorIndex-1) != '\\') {
        list.add(value.substring(startIndex, separatorIndex)
          // unescape separator
          .replaceAll("\\\\"+separator.charAt(0), ""+separator.charAt(0)));
        startIndex = separatorIndex + separator.length();
      }
      index = separatorIndex + separator.length();
    }
    // last element of the list
    list.add(value.substring(startIndex));
    return list;
  }

  /**
   * Get back a Map of String*String from its String representation.
   * Unescape backslashed separator characters.
   * @param string String to convert to a Map
   * @return a Map
   * @see #toString(java.util.Map)
   */
  public static Map<String, String> toMap(String string) {
    Map<String, String> map = new HashMap<String, String>();
    if (string == null
     || string.length() < 3) {
      return map;
    }
    List<String> firstList = toList(string, ", ");
    for (String element : firstList) {
      List<String> secondList = toList("["+element+"]", "=");
      if (secondList.size() == 2) {
        map.put(secondList.get(0), secondList.get(1));
      } else {
        Err.prln("Ignoring element: [" + element + "]");
        Err.prln("Expecting: [key=value]");
      }
    }
    return map;
  }

  /**
   * Crop the text in the middle if too long.
   * @param text text to crop
   * @param maxLength maximum length of the text
   * @return cropped text if needed otherwise the same text
   */
  public static String crop(String text, int maxLength) {
    if (text.length() > maxLength) {
      text = text.substring(0, (maxLength/2)-2) + "..."
        + text.substring(text.length() - (maxLength/2));
    }
    return text;
  }

} // class Strings
