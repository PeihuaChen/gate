/*
 *	TestXml.java
 *
 *	Cristian URSU,  8/May/2000
 *
 *	$Id$
 */

package gate.xml;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.util.*;
import gate.gui.*;

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
    //gate.Document doc = gate.Transients.newDocument(
    //          new URL("http://www.dcs.shef.ac.uk/~cursu/xml/input/bnc.xml")


    gate.Document doc = gate.Transients.newDocument(
             // new URL("http://redmires.dcs.shef.ac.uk/gate/tests/xml/xces/xces.xml")
             // new URL("http://www.dcs.shef.ac.uk/~cursu/xml/input/J52.xml")
             //   new URL("file:///d:/tmp/J52.xml")
             // new URL("http://www.dcs.shef.ac.uk/~cursu/xml/input/bnc.xml")
                new URL("http://www.dcs.shef.ac.uk/~cursu/xml/input/xces/xces.xml")
             // new URL("http://redmires.dcs.shef.ac.uk/gate/tests/xml/bnc.xml")
    );


    /*
    gate.Document doc = gate.Transients.newDocument(
              new URL("http://www.dcs.shef.ac.uk/~cursu/xml/input/Sentence.xml")
    );
    */

    /*
    File f = Files.writeTempFile(Files.getResourceAsStream("texts/Sentence.xml"));
    URL u = f.toURL();
    gate.Document doc = gate.Transients.newDocument(u);
    f.delete ();
    */
   /*
    gate.Document doc = gate.Transients.newDocument(
      Files.getResourceAsString("texts/Sentence.xml")
    );
    */
    // get the docFormat that deals with it.
    // the parameter MimeType doesn't affect right now the behaviour
    /*
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
      new MimeType("text","xml")
    );
    */
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
      doc.getSourceURL()
    );
    assert(docFormat instanceof gate.corpora.XmlDocumentFormat);
    //*
    // set's the map
    docFormat.setMarkupElementsMap(markupElementsMap);
    // register a progress listener with it
      /*
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
      //File f = Files.writeTempFile(doc.getSourceURL().openStream());
      int docSize = doc.getContent().size().intValue();
      //f.delete();
      System.out.println("unpacMarkup() time for " + doc.getSourceURL () + "(" +
        docSize/1024 + "." + docSize % 1024 + " K)" + "=" + time1 / 1000 + "." +
        time1 % 1000 + " sec," + " processing rate = " + docSize/time1*1000/1024 +
        "." + (docSize/time1*1000)%1024 + " K/second");
      */
   //*/
  } // testUnpackMarkup()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXml.class);
  } // suite

} // class TestXml
