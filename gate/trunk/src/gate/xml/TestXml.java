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
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat(
      doc, doc.getSourceUrl()
    );
    assert( "Bad document Format was produced. XmlDocumentFormat was expected",
            docFormat instanceof gate.corpora.XmlDocumentFormat
          );

    // Set the maps
    docFormat.setMarkupElementsMap(markupElementsMap);
    docFormat.setElement2StringMap(anElement2StringMap);

    docFormat.unpackMarkup (doc,"DocumentContent");
    AnnotationSet annotSet = doc.getAnnotations();


  } // testUnpackMarkup()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXml.class);
  } // suite

} // class TestXml
