/*
 *	TestDocument.java
 *
 *	Hamish Cunningham, 21/Jan/00
 *
 *	$Id$
 */

package gate;
import java.util.*;
import java.net.*;
import java.io.*;
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
  } // testSomething

  /** A comprehensive test */
  public void testLotsOfThings() {
    // check that the test URL is available
    URL u = null;
    try {
      u = new URL("http", "derwent.dcs.shef.ac.uk", 8000, "tests/doc0.html");
    } catch(MalformedURLException e) {
      fail(e.toString());
    }

    // get some text out of the test URL
    BufferedReader uReader = null;
    try {
      uReader = new BufferedReader(new InputStreamReader(u.openStream()));
      assertEquals(uReader.readLine(), "<HTML>");
    } catch(IOException e) {
      fail(e.toString());
    }

    //Document doc = new TextualDocument("sfd");
  } // testLotsOfThings

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestDocument.class);
  } // main

} // class TestDocument
