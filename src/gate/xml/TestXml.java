/*
 *	TestXml.java
 *
 *	Cristian URSU,  8/May/2000
 *
 *	$Id$
 */

package gate.xml;

import java.util.*;
import java.io.*;
import junit.framework.*;

// xml tools
import javax.xml.parsers.*;
import org.xml.sax.*;

/** Test class for XML facilities
  *
  */
public class TestXml extends TestCase
{
  /** Construction */
  public TestXml(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** A test */
  public void testSomething() throws Exception {
    assert(true);

	  try {

		  // Get a "parser factory", an an object that creates parsers
		  SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

		  // Set up the factory to create the appropriate type of parser

      // non validating one
		  saxParserFactory.setValidating(false);
      // non namesapace aware one
		  saxParserFactory.setNamespaceAware(false);

      // create it
		  SAXParser parser = saxParserFactory.newSAXParser();

      // use it
		  parser.parse(new File("V:\\XMLFILES\\TEST\\Sentence.xml"),
           new CustomDocumentHandler("file:///V:/XMLFILES/TEST/Sentence.xml") );

	  } catch (Exception ex) {
		  System.err.println("Exception : " + ex);
		  //System.exit(2);
	  }

  } // testSomething()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXml.class);
  } // suite

} // class TestXml
