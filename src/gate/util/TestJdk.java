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
  public void testCompiler() throws GateException {
    String nl = Strings.getNl();
    String javaSource =
      "package gate.util;" + nl +
      "import java.io.*;" + nl +
      "public class X {" + nl +
      "  public X() { /*System.out.println(\"X construcing\");*/ } " + nl +
      "  public static void main(String[] args)" + nl +
      "    { System.out.println(\"Hello from X\"); }" + nl +
      "  public static String getSomething() { return \"something\"; }" + nl +
      "} " + nl
      ;

    byte[] classBytes = jdk.compile(javaSource, "gate/util/X.java");
    assert(
      "no bytes returned from compiler",
      classBytes != null && classBytes.length > 0
    );

    /* if you want to write it to disk...
    FileOutputStream outFile =
      new FileOutputStream("z:\\gate2\\classes\\gate\\util\\X.class");
    outFile.write(classBytes);
    outFile.close();
    */

    // try and instantiate one
    Class theXClass = jdk.defineClass("gate/util/X", classBytes);
    Object theXObject = jdk.instantiateClass(theXClass);
    assert("couldn't instantiate the X class", theXObject != null);
    assert(
      "X instantiated wrongly, name = " + theXObject.getClass().getName(),
      theXObject.getClass().getName().equals("gate.util.X")
    );

  } // testCompiler()

  /** Jdk compiler test 2. Does nothing if it can't find the
    * gate class files in the usual places.
    */
  public void testCompiler2() throws GateException {
    byte[] thisClassBytes = null;
    String thisClassSource = null;

    // try and get the bytes from the usual place on NT
    try {
      File sf = new File("z:\\gate2\\src\\gate\\util\\X.java");
      File bf = new File("z:\\gate2\\classes\\gate\\util\\X.class");
      thisClassBytes = Files.getByteArray(bf);
      thisClassSource = Files.getString(sf);
    } catch(IOException e) {
    }

    // try and get them from the usual Solaris place
    if(thisClassBytes == null || thisClassBytes.length == 0)
      try { 
        File sf = new File(
"/share/nlp/projects/gate/webpages/gate.ac.uk/gate2/src/gate/util/TestJdk.java"
        );
        File bf = new File(
"/share/nlp/projects/gate/webpages/gate.ac.uk/gate2/classes/gate/util/TestJdk.class"
        );
        thisClassBytes = Files.getByteArray(bf);
        thisClassSource = Files.getString(sf);
      } catch(IOException e) {

        // we couldn't find the bytes; in an ideal world we'd get it
        // from gate.jar....
        return;
      }

    // compile the source
    Jdk jdk = new Jdk();
    byte[] compiledBytes =
      jdk.compile(thisClassSource, "gate/util/TestJdk.java");
    assert(
      "compiled binary doesn't equal on-disk binary",
      compiledBytes.equals(thisClassBytes)
    );

  } // testCompiler2()


  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestJdk.class);
  } // suite

} // class TestJdk
