/*
 *	TestDocument.java
 *
 *	Hamish Cunningham, 21/Jan/00
 *
 *	$Id$
 */

package gate;
import java.util.*;
import junit.framework.*;
import gate.util.*;

/** 
  * Tests for the Document classes
  */
public class TestDocument extends TestCase
{
  /** Construction */
  public TestDocument(String name) { super(name); }

  /** A test test */
  public void testSomething() {
    assertEquals(1, 1);
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestDocument.class);
  } // main

} // class TestDocument
