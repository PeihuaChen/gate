/*
 *	TestAnnotation.java
 *
 *	Hamish Cunningham, 7/Feb/00
 *
 *	$Id$
 */

package gate.annotation;

import java.util.*;
import junit.framework.*;
import gate.*;
import gate.util.*;

/** Tests for the Annotation classes
  */
public class TestAnnotation extends TestCase
{
  /** Construction */
  public TestAnnotation(String name) { super(name); }

  /** Base of the test server URL */
  protected String testServer;

  /** Name of test document 1 */
  protected String testDocument1;

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** A test test */
  public void testSomething() {
  } // testSomething

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestAnnotation.class);
  } // main

} // class TestAnnotation
