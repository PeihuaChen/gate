/*
 *	TestJdk.java
 *
 *	Hamish Cunningham, 16/Mar/00
 *
 *	$Id$
 */

package gate.util;

import java.util.*;
import java.io.*;
import junit.framework.*;

/** Tests for the Jdk class
  */
public class TestJdk extends TestCase
{
  /** Instance of the Jdk class */
  private Jdk jdk;

  /** Construction */
  public TestJdk(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
    jdk = new Jdk();
  } // setUp

  /** Jdk tool directory finder */
  public void testFinder() throws Exception {
    String toolsDir = jdk.getToolsHome().getPath();
    assert(
      "Tools dir was found to be: " + toolsDir,
      toolsDir.startsWith("w:\\jdk\\jdk1") ||
      toolsDir.startsWith("W:\\jdk\\jdk1") ||
      toolsDir.startsWith("W:\\JBuilder3\\java") ||
      toolsDir.startsWith("w:\\JBuilder3\\java") ||
      toolsDir.startsWith("/usr/local/")
    );
  } // testFinder()

  /** Jdk compiler */
  public void testCompiler() throws Exception {
    Byte[] classBytes =
      jdk.compile(new File("z:\\gate2\\src\\gate\\util\\Jdk.java"));

    assert(classBytes != null && classBytes.length != 0);
  } // testCompiler()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestJdk.class);
  } // suite

} // class TestJdk
