/*
 *	TestFiles.java
 *
 *	Hamish Cunningham, 10/June/00
 *
 *	$Id$
 */

package gate.util;

import java.util.*;
import java.io.*;
import junit.framework.*;
import java.net.*;

/** Files test class.
  */
public class TestFiles extends TestCase
{
  /** Construction */
  public TestFiles(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Test the getResourceAs... methods. */
  public void testGetResources() throws Exception {
    assert(true);
    String japeResName = "jape/combined/testloc.jape";
    String firstLine = "// testloc.jape";

    InputStreamReader resReader =
      new InputStreamReader(Files.getResourceAsStream(japeResName));
    BufferedReader bufResReader = new BufferedReader(resReader);
    assert(bufResReader.readLine().equals(firstLine));
    resReader.close();

    String resString = Files.getResourceAsString(japeResName);
    assert(resString.startsWith(firstLine));

    byte[] resBytes = Files.getResourceAsByteArray(japeResName);

    /*
    System.out.println(new String(resBytes));
    System.out.println(resBytes.length);
    System.out.println(resString);
    System.out.println(resString.length());
    */

    char resChars[] = new char[firstLine.length()];
    for(int i=0; i<resChars.length; i++) resChars[i] = (char)resBytes[i];
    resString = new String(resChars);
    assert(resString, resString.equals(firstLine));

  } // testGetResources()

  /** Test the writeTempFile... method. */
  public void testWriteTempFile() throws Exception {
    assert(true);
    String japeResName = "jape/combined/testloc.jape";
    String firstLine = "// testloc.jape";

    File f = Files.writeTempFile(Files.getResourceAsStream(japeResName));
    BufferedReader bfr = new BufferedReader(new FileReader(f));

    String firstLn = bfr.readLine();
    assert("first line from jape/combined/testloc.jape doesn't match", firstLine.equals(firstLn));
    
    f.delete ();
  } // testWriteTempFile()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestFiles.class);
  } // suite

} // class TestFiles
