/*
 *  Scratch.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 22/03/00
 *
 *  $Id$
 */


package gate.util;

import java.util.*;
import gate.*;
import gate.creole.*;

import org.xml.sax.*;
import javax.xml.parsers.*;


/** A scratch pad for experimenting.
  */
public class Scratch
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  public static void main(String args[]) {
   doIt();
  } // main

  public static void doIt() {
    for(int i = 0; i < 100; i++)
     Out.prln(random());
  } // doIt

  /** Generate a random integer for file naming. */
  protected static int random() {
    return randomiser.nextInt(9999);
  } // random

  /** Random number generator */
  protected static Random randomiser = new Random();

} // class Scratch

