/*
 *	TestTemplate.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *  
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *  
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *  
 *	Hamish Cunningham, 16/Mar/00
 *
 *	$Id$
 */

package gate.util;

import java.util.*;
import java.io.*;
import junit.framework.*;

/** Template test class - to add a new part of the test suite:
  * <UL>
  * <LI>
  * copy this class and change "Template" to the name of the new tests;
  * <LI>
  * add a line to TestGate.java in the suite method referencing your new
  * class;
  * <LI>
  * add test methods to this class.
  * </UL>
  */
public class TestTemplate extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestTemplate(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** A test */
  public void testSomething() throws Exception {
    assert(true);
  } // testSomething()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestTemplate.class);
  } // suite

} // class TestTemplate