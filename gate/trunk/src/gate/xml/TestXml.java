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
import org.w3c.www.mime.*;

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


  public static void main(String args[]){
    TestXml app = new TestXml("TestXml");
    try{
      app.testSomething ();
    }catch (Exception e){
      System.out.println(e);
    }
  }
 

  /** A test */
  public void testSomething() throws Exception{
    assert(true);

    // create the markupElementsMap map
    Map markupElementsMap = null;
    /*
    markupElementsMap = new HashMap();
    // populate it
    markupElementsMap.put ("S","Sentence");
    markupElementsMap.put ("s","Sentence");
    markupElementsMap.put ("W","Word");
    markupElementsMap.put ("w","Word");
    markupElementsMap.put ("p","Paragraph");
    markupElementsMap.put ("h1","Header 1");
    markupElementsMap.put ("H1","Header 1");
    markupElementsMap.put ("A","link");
    markupElementsMap.put ("a","link");
    */
    // create a new gate document
    gate.Document doc = gate.Transients.newDocument(
              "http://www.dcs.shef.ac.uk/~cursu/xml/input/bnc.xml"
    );
    // get the docFormat that deals with it.
    // the parameter MimeType doesn't affect right now the behaviour
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
      (new MimeType("text","xml")).toString()
    );

    // set's the map
    docFormat.setMarkupElementsMap(markupElementsMap);
    docFormat.unpackMarkup (doc,"DocumentContent");

    // graphic visualisation
    /*
    if (docFormat != null){
        docFormat.unpackMarkup (doc);
        gate.jape.gui.JapeGUI japeGUI = new gate.jape.gui.JapeGUI();
        gate.Corpus corpus = gate.Transients.newCorpus("XML Test");
        corpus.add(doc);
        japeGUI.setCorpus(corpus);
    }
    */
  } // testSomething()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXml.class);
  } // suite

} // class TestXml
