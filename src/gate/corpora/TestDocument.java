/*
 *	TestDocument.java
 *
 *	Hamish Cunningham, 21/Jan/00
 *
 *	$Id$
 */

package gate.corpora;

import java.util.*;
import java.net.*;
import java.io.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.annotation.*;

/** Tests for the Document classes
  */
public class TestDocument extends TestCase
{
  /** Construction */
  public TestDocument(String name) { super(name); setUp();}

  /** Base of the test server URL */
  protected static String testServer = "http://redmires.dcs.shef.ac.uk/";

  /** Name of test document 1 */
  protected String testDocument1;

  /** Fixture set up */
  public void setUp() {
    testDocument1 = "texts/doc0.html";
  } // setUp

  /** Get the name of the test server */
  public static String getTestServerName() { return testServer; }

  /** Test ordering */
  public void testCompareTo() throws Exception{
    Document doc1 = null;
    Document doc2 = null;
    Document doc3 = null;


    doc1 = new DocumentImpl(new URL(testServer + "gate/tests/def/"));
    doc2 = new DocumentImpl(new URL(testServer + "gate/tests/defg/"));
    doc3 = new DocumentImpl(new URL(testServer + "gate/tests/abc/"));

    assert(doc1.compareTo(doc2) < 0);
    assert(doc1.compareTo(doc1) == 0);
    assert(doc1.compareTo(doc3) > 0);
  } // testCompareTo()

  /** A comprehensive test */
  public void testLotsOfThings() {
    // check that the test URL is available
    URL u = null;
    File f = null;
    try{
      f = Files.writeTempFile(Files.getGateResourceAsStream(testDocument1));
      u = f.toURL();
    } catch (Exception e){
      e.printStackTrace(System.err);
    }

    // get some text out of the test URL
    BufferedReader uReader = null;
    try {
      uReader = new BufferedReader(new InputStreamReader(u.openStream()));
      assertEquals(uReader.readLine(), "<HTML>");
    } catch(UnknownHostException e) { // no network connection
      return;
    } catch(IOException e) {
      fail(e.toString());
    }
    f.delete();
    /*
    Document doc = new TextualDocument(testServer + testDocument1);
    AnnotationGraph ag = new AnnotationGraphImpl();

    Tokeniser t = ...   doc.getContent()
    tokenise doc using java stream tokeniser

    add several thousand token annotation
    select a subset
    */
  } // testLotsOfThings

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestDocument.class);
  } // suite

} // class TestDocument
