/*
 *  TestBumpyStack.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 10/June/00
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import java.io.*;
import junit.framework.*;
import java.net.*;

/** BumpyStack test class.
  */
public class TestBumpyStack extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestBumpyStack(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestBumpyStack.class);
  } // suite

  /** Test the bumpiness of the thing. */
  public void testBumpiness() throws Exception {
    WeakBumpyStack bumper = new WeakBumpyStack();

    String s1 = new String("s1");
    String s2 = new String("s2");
    String s3 = new String("s3");

    bumper.push(s3);
    bumper.push(s2);
    bumper.push(s1);

    assert(
      "managed to bump non-existent element",
      ! bumper.bump(new String("something"))
    );

    assert("stack wrong length (I): " + bumper.size(), bumper.size() == 3);
    assert("couldn't bump s2", bumper.bump(s2));
    assert("s2 not front of stack", ((String) bumper.pop()).equals("s2"));
    assert("stack wrong length (II)" + bumper.size(), bumper.size() == 2);
  } // testBumpiness()

} // class TestBumpyStack
