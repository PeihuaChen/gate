/*
 *  TestXml.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  8/May/2000
 *
 *  $Id$
 */

package gate.xml;

import java.util.*;
import java.net.*;
import java.io.*;
import java.beans.*;

import gate.util.*;
import gate.gui.*;
import gate.*;

import junit.framework.*;
import org.w3c.www.mime.*;


/** Test class for XML facilities
  *
  */
public class TestXml extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestXml(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  public void testGateDocumentToAndFromXmlWithDifferentKindOfFormats()
                                                               throws Exception{
    List urlList = new LinkedList();
    List urlDescription = new LinkedList();
    URL url = null;

    url = Gate.getUrl("tests/xml/xces.xml");
    assert("Coudn't create a URL object for tests/xml/xces.xml ", url != null);
    urlList.add(url);
    urlDescription.add(" an XML document ");

    url = null;
    url = Gate.getUrl("tests/html/test1.htm");
    assert("Coudn't create a URL object for tests/html/test.htm",url != null);
    urlList.add(url);
    urlDescription.add(" an HTML document ");

    url = null;
    url = Gate.getUrl("tests/rtf/Sample.rtf");
    assert("Coudn't create a URL object for defg ",url != null);
    urlList.add(url);
    urlDescription.add(" a RTF document ");

    url = null;
    url = Gate.getUrl("tests/email/test2.eml");
    assert("Coudn't create a URL object for defg ",url != null);
    urlList.add(url);
    urlDescription.add(" an EMAIL document ");

    Iterator iter = urlList.iterator();
    Iterator descrIter = urlDescription.iterator();
    while(iter.hasNext()){
      runCompleteTestWithAFormat((URL) iter.next(),(String)descrIter.next());
    }// End While


  }// testGateDocumentToAndFromXmlWithDifferentKindOfFormats

  private void runCompleteTestWithAFormat(URL url, String urlDescription)
                                                             throws Exception{
    // Load the xml Key Document and unpack it
    gate.Document keyDocument = null;
    keyDocument = gate.Factory.newDocument(url);

    assert("Coudn't create a Gate document instance for " +
            url.toString() +
            " Can't continue." , keyDocument != null);

    gate.DocumentFormat keyDocFormat = null;
    keyDocFormat = gate.DocumentFormat.getDocumentFormat(
      keyDocument, keyDocument.getSourceUrl()
    );

    assert("Fail to recognize " +
            url.toString() +
            " as being " + urlDescription + " !", keyDocFormat != null);

    // Unpack the markup
    keyDocFormat.unpackMarkup(keyDocument);

    // Save the size of the document snd the number of annotations
    long keyDocumentSize = keyDocument.getContent().size().longValue();
    int keyDocumentAnnotationSetSize = keyDocument.getAnnotations().size();


    // Export the Gate document called keyDocument as  XML, into a temp file,
    // using UTF-8 encoding
    File xmlFile = null;
    xmlFile = Files.writeTempFile(null);

    assert("The temp Gate XML file is null. Can't continue.",xmlFile != null);

    // Prepare to write into the xmlFile using UTF-8 encoding
    OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(xmlFile),"UTF-8");
    // Write (test the toXml() method)
    writer.write(keyDocument.toXml());
    writer.flush();
    writer.close();

    // Load the XML Gate document form the tmp file into memory
    gate.Document gateDoc = null;
    gateDoc = gate.Factory.newDocument(xmlFile.toURL());

    assert("Coudn't create a Gate document instance for " +
                xmlFile.toURL().toString() +
                " Can't continue." , gateDoc != null);

    gate.DocumentFormat gateDocFormat = null;
    gateDocFormat =
            DocumentFormat.getDocumentFormat(gateDoc,gateDoc.getSourceUrl());

    assert("Fail to recognize " +
      xmlFile.toURL().toString() +
      " as being a Gate XML document !", gateDocFormat != null);

    gateDocFormat.unpackMarkup(gateDoc);

    // Save the size of the document snd the number of annotations
    long gateDocSize = keyDocument.getContent().size().longValue();
    int gateDocAnnotationSetSize = keyDocument.getAnnotations().size();

    assert("Exporting as Gate XML resulted in document content size lost." +
      " Something went wrong.", keyDocumentSize == gateDocSize);

    assert("Exporting as Gate XML resulted in annotation lost." +
      " No. of annotations missing =  " +
      Math.abs(keyDocumentAnnotationSetSize - gateDocAnnotationSetSize),
      keyDocumentAnnotationSetSize == gateDocAnnotationSetSize);

    //Don't need tmp Gate XML file.
    xmlFile.delete();
  }//runCompleteTestWithAFormat

  /** A test */
  public void testUnpackMarkup() throws Exception{
    // create the markupElementsMap map
    Map markupElementsMap = null;
    gate.Document doc = null;
    /*
    markupElementsMap = new HashMap();
    // populate it
    markupElementsMap.put ("S","Sentence");
    markupElementsMap.put ("s","Sentence");
    */
    // Create the element2String map
    Map anElement2StringMap = null;
    anElement2StringMap = new HashMap();
    // Populate it
    anElement2StringMap.put("S","\n");
    anElement2StringMap.put("s","\n");

    doc = gate.Factory.newDocument(Gate.getUrl("tests/xml/xces.xml"));
 //doc = gate.Factory.newDocument(new URL("file:///d:/tmp/gateResource.xml"));

    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat(
      doc, doc.getSourceUrl()
    );

    assert( "Bad document Format was produced. XmlDocumentFormat was expected",
            docFormat instanceof gate.corpora.XmlDocumentFormat
          );

    // Set the maps
    docFormat.setMarkupElementsMap(markupElementsMap);
    docFormat.setElement2StringMap(anElement2StringMap);

    docFormat.unpackMarkup (doc);
    AnnotationSet annotSet = doc.getAnnotations();

  } // testUnpackMarkup()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXml.class);
  } // suite

} // class TestXml
