/*
 *	TestDocument.java
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
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestDocument(String name) { super(name); setUp();}

  /** Base of the test server URL */
  protected static String testServer = null;

  /** Name of test document 1 */
  protected String testDocument1;

  /** Fixture set up */
  public void setUp() {
    try{
      Gate.init();
      testServer = Gate.getUrl().toExternalForm();
    } catch (GateException e){
      e.printStackTrace(Err.getPrintWriter());
    }

    testDocument1 = "tests/html/test2.htm";
  } // setUp

  /** Get the name of the test server */
  public static String getTestServerName() {
    if(testServer != null) return testServer;
    else{
      try { testServer = Gate.getUrl().toExternalForm(); }
      catch(Exception e) { }
      return testServer;
    }
  }

  /** Test ordering */
  public void testCompareTo() throws Exception{
    Document doc1 = null;
    Document doc2 = null;
    Document doc3 = null;


    doc1 = new DocumentImpl(new URL(testServer + "tests/def/"));
    doc2 = new DocumentImpl(new URL(testServer + "tests/defg/"));
    doc3 = new DocumentImpl(new URL(testServer + "tests/abc/"));

    assert(doc1.compareTo(doc2) < 0);
    assert(doc1.compareTo(doc1) == 0);
    assert(doc1.compareTo(doc3) > 0);
  } // testCompareTo()

  /** A comprehensive test */
  public void testLotsOfThings() {
    // check that the test URL is available
    URL u = null;
    try{
      u = new URL(testServer + testDocument1);
    } catch (Exception e){
      e.printStackTrace(Err.getPrintWriter());
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