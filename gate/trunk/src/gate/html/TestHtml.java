/*
 *	TestHtml.java
 *
 *	Cristian URSU,  8/May/2000
 *
 *	$Id$
 */

package gate.html;

import java.util.*;
import java.net.*;
import java.io.*;

import javax.swing.*;

import junit.framework.*;
import org.w3c.www.mime.*;

import gate.util.*;
import gate.gui.*;
import gate.*;


/** Test class for XML facilities
  *
  */
public class TestHtml extends TestCase
{
  /** Construction */
  public TestHtml(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp


  public static void main(String args[]){
    TestHtml app = new TestHtml("TestHtml");
    try{
      app.testUnpackMarkup();
    }catch (Exception e){
      e.printStackTrace (System.err);
    }
  }


  /** A test */
  public void testUnpackMarkup() throws Exception{
    assert(true);
    // create the markupElementsMap map
    Map markupElementsMap = null;

    gate.Document doc = null;
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
    // init detects if Derwent or www.gate.ac.uk are reachable

    Gate.init();
    // create a new gate document
    if (Gate.isGateHomeReachable())
        doc = gate.Transients.newDocument(
            new URL ("http://derwent.dcs.shef.ac.uk/gate.ac.uk/tests/html/test1.htm")
        );
    else if (Gate.isGateAcUkReachable())
             doc = gate.Transients.newDocument(
                new URL ("http://www.gate.ac.uk/tests/html/test1.htm")
            );
         else 
          throw new LazyProgrammerException("Derwent and www.gate.ak.uk are not reachable");
    // get the docFormat that deals with it.
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat(
      doc, doc.getSourceURL()
    );
    assert(docFormat instanceof gate.corpora.HtmlDocumentFormat);
    // set's the map
    docFormat.setMarkupElementsMap(markupElementsMap);
     /*
      // register a progress listener with it
      docFormat.addStatusListener(new StatusListener(){
          public void statusChanged(String text){
            System.out.println(text);
          }
          public void processFinished(){
          }
      });
    */
    docFormat.unpackMarkup (doc,"DocumentContent");
      /*
      // timing the operation
      Date startTime = new Date();
        docFormat.unpackMarkup (doc,"DocumentContent");
      Date endTime = new Date();
      long  time1 = endTime.getTime () - startTime.getTime ();
      int docSize = doc.getContent().size().intValue();
      System.out.println("unpacMarkup() time for " + doc.getSourceURL () + "(" +
        docSize/1024 + "." + docSize % 1024 + " K)" + "=" + time1 / 1000 + "." +
        time1 % 1000 + " sec," + " processing rate = " + docSize/time1*1000/1024 +
        "." + (docSize/time1*1000)%1024 + " K/second");
      */
    //*/

  } // testUnpackMarkup()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestHtml.class);
  } // suite

} // class TestXml
