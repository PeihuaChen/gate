/*
 *	TestSgml.java
 *
 *	Cristian URSU,  8/May/2000
 *
 *	$Id$
 */

package gate.sgml;

import java.util.*;
import java.net.*;
import java.io.*;
import junit.framework.*;
import org.w3c.www.mime.*;
import gate.util.*;
import gate.gui.*;
import gate.*;
import javax.swing.*;

/** Test class for XML facilities
  *
  */
public class TestSgml extends TestCase
{
  /** Construction */
  public TestSgml(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp


  public static void main(String args[]){
    TestSgml app = new TestSgml("TestSgml");
    try{
      app.testSgmlLoading ();
    }catch (Exception e){
      e.printStackTrace (System.err);
    }
  }



  public void testSgmlLoading() throws Exception {
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
            new URL ("http://www.dcs.shef.ac.uk/~cursu/sgml/Hds.sgm")
            //new URL ("http://www.dcs.shef.ac.uk/~cursu/sgml/K79.sgm")
            //new URL ("http://www.dcs.shef.ac.uk/~cursu/sgml/Fly.sgm")
           //new URL ("file:///d:/tmp/Hds.SGML")
    );
    // get the docFormat that deals with it.
    // the parameter MimeType doesn't affect right now the behaviour
    //*
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
        new MimeType("text","sgml")
    );
    //*/

    /*
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
      doc.getSourceURL()
    );
    assert(docFormat instanceof gate.corpora.SgmlDocumentFormat);
    */
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
      // get the size of the doc
      long  time1 = endTime.getTime () - startTime.getTime ();
      int docSize = doc.getContent().size().intValue();
      System.out.println("unpacMarkup() time for " + doc.getSourceURL () + "(" +
        docSize/1024 + "." + docSize % 1024 + " K)" + "=" + time1 / 1000 + "." +
        time1 % 1000 + " sec," + " processing rate = " + docSize/time1*1000/1024 +
        "." + (docSize/time1*1000)%1024 + " K/second");
      */
    //*/
  }// testSgml

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestSgml.class);
  } // suite

} // class TestSgml
