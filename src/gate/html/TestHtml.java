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
import junit.framework.*;
import org.w3c.www.mime.*;
import gate.util.*;
import gate.gui.*;
import gate.*;
import javax.swing.*;

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
             // new URL("http://www.funideas.com/visual_gallery.htm")
            new URL ("http://www.dcs.shef.ac.uk/~hamish/GateIntro.html")
            //new URL ("http://www.javasoft.com")
            //new URL ("http://www.w3.org/TR/REC-xml")
            //new URL ("http://www.dcs.shef.ac.uk/~cursu")
            //new URL ("http://www.webhelp.com/home.html")
            //new URL ("http://big2.hotyellow98.com/sys/signup.cgi")
            //new URL ("http://www.epilot.com/SearchResults.asp?keyword=costume+baie&page=&source=&TokenID=82C7BE897D9643EDB3CB8A28E398A488")
    );
    // get the docFormat that deals with it.
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
      doc.getSourceURL()
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
