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
      toolsDir.startsWith("H:\\JBuilder3\\java") ||
      toolsDir.startsWith("h:\\JBuilder3\\java") ||
      toolsDir.startsWith("/usr/local/")
    );
  } // testFinder()

  /** Jdk compiler */
  public void testCompiler() throws Exception {
    String nl = Strings.getNl();
    String javaSource =
      "import java.io.*;" + nl +
      "public class X {" + nl +
      "  public X() { /*System.out.println(\"X construcing\");*/ } " + nl +
      "  public static void main(String[] args)" + nl +
      "    { System.out.println(\"Hello from X\"); }" + nl +
      "  public static String getSomething() { return \"something\"; }" + nl +
      "} " + nl
      ;

    sun.toolsx.javac.Main compiler = new sun.toolsx.javac.Main(
      System.out, "TestJdk"
    );
    String argv[] = new String[3];
    argv[0] = "-nodisk";
    argv[1] = "X.java";
    argv[2] = javaSource;
    compiler.compile(argv);
    List compilerOutput = compiler.getCompilerOutput();

    Iterator iter = compilerOutput.iterator();
    while(iter.hasNext()) {
      byte[] classBytes = (byte[]) iter.next();
      assert(
	"no bytes returned from compiler",
	classBytes != null && classBytes.length > 0
      );

      /* if you want to write it to disk...
      FileOutputStream outFile = 
        new FileOutputStream("z:\\gate2\\build\\X.class");
      outFile.write(classBytes);
      outFile.close();
      */

      // try and instantiate one
      Class theXClass = jdk.defineClass("X", classBytes);
      Object theXObject = jdk.instantiateClass(theXClass);
      assert("couldn't instantiate the X class", theXObject != null);
      assert(
        "X instantiated wrongly",
	theXObject.getClass().getName().equals("X")
      );
    } // while

  } // testCompiler()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestJdk.class);
  } // suite

} // class TestJdk
