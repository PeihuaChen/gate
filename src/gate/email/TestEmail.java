/*
 *	TestEmail.java
 *
 *	Cristian URSU,  7/Aug/2000
 *
 *	$Id$
 */

package gate.email;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.util.*;
import gate.gui.*;

import junit.framework.*;
import org.w3c.www.mime.*;


/** Test class for Email facilities
  *
  */
public class TestEmail extends TestCase
{
  /** Construction */
  public TestEmail(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp


  public static void main(String args[]){
    TestEmail app = new TestEmail("TestEmail");
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
              // new URL("http://www.dcs.shef.ac.uk/~cursu/email/test1.eml")
              // new URL("http://www.dcs.shef.ac.uk/~cursu/email/test2.eml")
              // new URL("http://www.dcs.shef.ac.uk/~cursu/email/test3.eml")
              //new URL("http://www.dcs.shef.ac.uk/~cursu/email/test4.eml")
              new URL("http://www.dcs.shef.ac.uk/~cursu/email/test5.eml")
              // new URL("http://www.dcs.shef.ac.uk/~cursu/email/test.eml")
              //  new URL("http://www.dcs.shef.ac.uk/~cursu/email/miguel.mail.eml")
    );


    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat(
      doc, doc.getSourceURL()
    );
    assert(docFormat instanceof gate.corpora.EmailDocumentFormat);

    // register a status listener with it
    docFormat.addStatusListener(new StatusListener(){
          public void statusChanged(String text){
            System.out.println(text);
          }
          public void processFinished(){
          }
      });

      //docFormat.unpackMarkup (doc,"DocumentContent");
      docFormat.unpackMarkup(doc);
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
    return new TestSuite(TestEmail.class);
  } // suite

} // class TestXml
