/*
 *	TestXml.java
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
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

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
      e.printStackTrace (Err.getPrintWriter());
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
    // create the element2String map
    Map anElement2StringMap = null;
    anElement2StringMap = new HashMap();
    // populate it
    anElement2StringMap.put("S","\n");
    anElement2StringMap.put("s","\n");


    // init detects if Derwent or www.gate.ac.uk are reachable
    Gate.init();
    // create a new gate document
    if (Gate.isGateHomeReachable())
        doc = gate.Transients.newDocument(
         new URL ("http://derwent.dcs.shef.ac.uk/gate.ac.uk/tests/xml/xces.xml")
        );
    else if (Gate.isGateAcUkReachable())
             doc = gate.Transients.newDocument(
                new URL ("http://www.gate.ac.uk/tests/xml/xces.xml")
            );
         else
          throw new LazyProgrammerException(
                                "Derwent and www.gate.ak.uk are not reachable"
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
    // get the docFormat that deals with it.
    // the parameter MimeType doesn't affect right now the behaviour
    /*
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat(doc,
      new MimeType("text","xml")
    );
    */
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat(
      doc, doc.getSourceURL()
    );
    assert(docFormat instanceof gate.corpora.XmlDocumentFormat);
    //*
    // set's the map
    docFormat.setMarkupElementsMap(markupElementsMap);
    docFormat.setElement2StringMap(anElement2StringMap);
    // register a progress listener with it
      /*
      docFormat.addStatusListener(new StatusListener(){
          public void statusChanged(String text){
            Out.println(text);
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
      Out.println("unpacMarkup() time for " + doc.getSourceURL () + "(" +
       docSize/1024 + "." + docSize % 1024 + " K)" + "=" + time1 / 1000 + "." +
       time1 % 1000 + " sec," + " processing rate = " + docSize/time1*1000/1024+
        "." + (docSize/time1*1000)%1024 + " K/second");
      */
   //*/
  } // testUnpackMarkup()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXml.class);
  } // suite

} // class TestXml