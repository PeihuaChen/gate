/*
 *  TestControllers.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 16/Mar/00
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;
import junit.framework.*;

/** Tests for controller classes
  */
public class TestControllers extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestControllers(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** A test */
  public void testSomething() throws Exception {
    assert(true);
  } // testSomething()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestControllers.class);
  } // suite

} // class TestControllers
