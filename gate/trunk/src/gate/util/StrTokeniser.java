/*
 *  StrTokeniser.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Derived from Sun code by Valy, sometime or other.
 *
 *  Copyright (c) 1995, 1996 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  This software is the confidential and proprietary information of Sun
 *  Microsystems, Inc. ("Confidential Information").  You shall not
 *  disclose such Confidential Information and shall use it only in
 *  accordance with the terms of the license agreement you entered into
 *  with Sun.
 *
 *  SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 *  SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 *  PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 *  SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 *  THIS SOFTWARE OR ITS DERIVATIVES.
 *
 *  CopyrightVersion 1.1_beta
 */



//This class needed to be changed so we can get hold of the current position of
//the tokeniser so we can calculate properly the offsets in the documents.
//Wish the current position was not private.

package gate.util;

import java.lang.*;
import java.util.*;

public class StrTokeniser implements Enumeration {

  /** Debug flag */
  private static final boolean DEBUG = false;

  private int currentPosition;

  private int maxPosition;

  private String str;

  private String delimiters;

  private boolean retTokens;

  /**
   * Constructs a string tokenizer for the specified string. The
   * characters in the <code>delim</code> argument are the delimiters
   * for separating tokens.
   * <p>
   * If the <code>returnTokens</code> flag is <code>true</code>, then
   * the delimiter characters are also returned as tokens. Each
   * delimiter is returned as a string of length one. If the flag is
   * <code>false</code>, the delimiter characters are skipped and only
   * serve as separators between tokens.
   *
   * @param   str            a string to be parsed.
   * @param   delim          the delimiters.
   * @param   returnTokens   flag indicating whether to return the delimiters
   *                         as tokens.
   * @since   JDK1.0
   */
  public StrTokeniser(String str, String delim, boolean returnTokens) {
    currentPosition = 0;
    this.str = str;
    maxPosition = str.length();
    delimiters = delim;
    retTokens = returnTokens;
  }

  /**
   * Constructs a string tokenizer for the specified string. The
   * characters in the <code>delim</code> argument are the delimiters
   * for separating tokens.
   *
   * @param   str     a string to be parsed.
   * @param   delim   the delimiters.
   * @since   JDK1.0
   */
  public StrTokeniser(String str, String delim) {
    this(str, delim, false);
  }

  /**
   * Constructs a string tokenizer for the specified string. The
   * tokenizer uses the default delimiter set, which is
   * <code>"&#92;t&#92;n&#92;r"</code>: the space character, the tab
   * character, the newline character, and the carriage-return character.
   *
   * @param   str   a string to be parsed.
   * @since   JDK1.0
   */
  public StrTokeniser(String str) {
    this(str, " \t\n\r", false);
  } // StrTokeniser

  /**
   * Skips delimiters.
   */
  private void skipDelimiters() {
    while (!retTokens &&
           (currentPosition < maxPosition) &&
           (delimiters.indexOf(str.charAt(currentPosition)) >= 0)) {
        currentPosition++;
    }
  } // skipDelimiters

  /**
   * Tests if there are more tokens available from this tokenizer's string.
   *
   * @return  <code>true</code> if there are more tokens available from this
   *          tokenizer's string; <code>false</code> otherwise.
   * @since   JDK1.0
   */
  public boolean hasMoreTokens() {
    skipDelimiters();
    return (currentPosition < maxPosition);
  } // hasMoreTokens

  /**
   * Returns the next token from this string tokenizer.
   *
   * @return     the next token from this string tokenizer.
   * @exception  NoSuchElementException  if there are no more tokens in this
   *               tokenizer's string.
   * @since      JDK1.0
   */
  public String nextToken() {
    skipDelimiters();

    if (currentPosition >= maxPosition) {
        throw new NoSuchElementException();
    }

    int start = currentPosition;
    while ((currentPosition < maxPosition) &&
           (delimiters.indexOf(str.charAt(currentPosition)) < 0)) {
        currentPosition++;
    }
    if (retTokens && (start == currentPosition) &&
        (delimiters.indexOf(str.charAt(currentPosition)) >= 0)) {
        currentPosition++;
    }
    return str.substring(start, currentPosition);
  } // nextToken

  /**
   * Returns the next token in this string tokenizer's string. The new
   * delimiter set remains the default after this call.
   *
   * @param      delim   the new delimiters.
   * @return     the next token, after switching to the new delimiter set.
   * @exception  NoSuchElementException  if there are no more tokens in this
   *               tokenizer's string.
   * @since   JDK1.0
   */
  public String nextToken(String delim) {
    delimiters = delim;
    return nextToken();
  } // nextToken(String delim)

  /**
   * Returns the same value as the <code>hasMoreTokens</code>
   * method. It exists so that this class can implement the
   * <code>Enumeration</code> interface.
   *
   * @return  <code>true</code> if there are more tokens;
   *          <code>false</code> otherwise.
   * @see     java.util.Enumeration
   * @see     java.util.StringTokenizer#hasMoreTokens()
   * @since   JDK1.0
   */
  public boolean hasMoreElements() {
    return hasMoreTokens();
  }

  /**
   * Returns the same value as the <code>nextToken</code> method,
   * except that its declared return value is <code>Object</code> rather than
   * <code>String</code>. It exists so that this class can implement the
   * <code>Enumeration</code> interface.
   *
   * @return     the next token in the string.
   * @exception  NoSuchElementException  if there are no more tokens in this
   *               tokenizer's string.
   * @see        java.util.Enumeration
   * @see        java.util.StringTokenizer#nextToken()
   * @since      JDK1.0
   */
  public Object nextElement() {
    return nextToken();
  }

  /**
   * Returns the current position of the tikeniser so it can be used
   * for calculating the offset. That's the only difference from the
   * original StringTokenizer class from the jdk.
   */
  public int getCurrentPosition() {
    return currentPosition;
  }

  /**
   * Calculates the number of times that this tokenizer's
   * <code>nextToken</code> method can be called before it generates an
   * exception.
   *
   * @return  the number of tokens remaining in the string using the current
   *          delimiter set.
   * @see     java.util.StringTokenizer#nextToken()
   * @since   JDK1.0
   */
  public int countTokens() {
    int count = 0;
    int currpos = currentPosition;

    while (currpos < maxPosition) {
      /*
       * This is just skipDelimiters(); but it does not affect
       * currentPosition.
       */
      while (!retTokens &&
        (currpos < maxPosition) &&
        (delimiters.indexOf(str.charAt(currpos)) >= 0)) {
        currpos++;
      }

      if (currpos >= maxPosition) {
        break;
      }

      int start = currpos;

      while ((currpos < maxPosition) &&
        (delimiters.indexOf(str.charAt(currpos)) < 0)) {
        currpos++;
      }

      if (retTokens && (start == currpos) &&
        (delimiters.indexOf(str.charAt(currpos)) >= 0)) {
        currpos++;
      }
      count++;

    }
    return count;

  } // countTokens

} // class StrTokeniser
