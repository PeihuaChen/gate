/*
 * ParseCpslConstants.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Hamish Cunningham, 23/02/2000
 *
 * $Id$
 */

/* Generated By:JavaCC: Do not edit this line. ParseCpslConstants.java */

package gate.jape.parser;

public interface ParseCpslConstants {

  int EOF = 0;
  int space = 1;
  int spaces = 2;
  int newline = 3;
  int digits = 4;
  int letter = 5;
  int letters = 6;
  int lettersAndDigits = 7;
  int letterOrDigitOrDash = 8;
  int lettersAndDigitsAndDashes = 9;
  int multiphase = 10;
  int phases = 11;
  int phase = 12;
  int input = 13;
  int option = 14;
  int rule = 15;
  int macro = 16;
  int priority = 17;
  int pling = 18;
  int kleeneOp = 19;
  int integer = 20;
  int string = 21;
  int bool = 22;
  int ident = 23;
  int floatingPoint = 24;
  int exponent = 25;
  int colon = 26;
  int semicolon = 27;
  int period = 28;
  int bar = 29;
  int comma = 30;
  int leftBrace = 31;
  int rightBrace = 32;
  int leftBracket = 33;
  int rightBracket = 34;
  int assign = 35;
  int equals = 36;
  int colonplus = 37;
  int whiteSpace = 38;
  int singleLineCStyleComment = 39;
  int singleLineCpslStyleComment = 40;
  int commentStart = 41;
  int commentChars = 42;
  int commentEnd = 43;
  int other = 44;

  int DEFAULT = 0;
  int WithinComment = 1;

  String[] tokenImage = {
    "<EOF>",
    "<space>",
    "<spaces>",
    "<newline>",
    "<digits>",
    "<letter>",
    "<letters>",
    "<lettersAndDigits>",
    "<letterOrDigitOrDash>",
    "<lettersAndDigitsAndDashes>",
    "\"Multiphase:\"",
    "\"Phases:\"",
    "\"Phase:\"",
    "\"Input:\"",
    "\"Options:\"",
    "\"Rule:\"",
    "\"Macro:\"",
    "\"Priority:\"",
    "\"!\"",
    "<kleeneOp>",
    "<integer>",
    "<string>",
    "<bool>",
    "<ident>",
    "<floatingPoint>",
    "<exponent>",
    "\":\"",
    "\";\"",
    "\".\"",
    "\"|\"",
    "\",\"",
    "\"{\"",
    "\"}\"",
    "\"(\"",
    "\")\"",
    "\"=\"",
    "\"==\"",
    "\":+\"",
    "<whiteSpace>",
    "<singleLineCStyleComment>",
    "<singleLineCpslStyleComment>",
    "<commentStart>",
    "<commentChars>",
    "<commentEnd>",
    "<other>",
    "\"-->\"",
  };

} // interface ParseCpslConstants
