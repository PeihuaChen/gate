/*
 *	TestHtml.java
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


/** Test class for HTML facilities
  */
public class TestHtml extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestHtml(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** A test */
  public void testUnpackMarkup() throws Exception {
    assert(true);

    // create the markupElementsMap map
    Map markupElementsMap = null;

    gate.Document doc = null;
    /*
    markupElementsMap = new HashMap();
    // populate it
    markupElementsMap.put ("h1","Header 1");
    markupElementsMap.put ("H1","Header 1");
    markupElementsMap.put ("A","link");
    markupElementsMap.put ("a","link");
    */

    doc = gate.Factory.newDocument(Gate.getUrl("tests/html/test1.htm"));
    // get the docFormat that deals with it.
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat(
                                                        doc, doc.getSourceUrl()
                                                        );
    assert( "Bad document Format was produced. HtmlDocumentFormat was expected",
            docFormat instanceof gate.corpora.HtmlDocumentFormat
          );


    // set's the map
    docFormat.setMarkupElementsMap(markupElementsMap);
    docFormat.unpackMarkup (doc,"DocumentContent");
  } // testUnpackMarkup()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestHtml.class);
  } // suite

}//class TestHtml