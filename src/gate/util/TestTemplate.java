/*
 *	TestTemplate.java
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
