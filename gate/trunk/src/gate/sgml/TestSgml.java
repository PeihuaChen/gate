/*
 *  TestSgml.java
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

/** Test class for SGML facilities
  */
public class TestSgml extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestSgml(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  public void testSgmlLoading() throws Exception {
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
    */

    FeatureMap params = Factory.newFeatureMap();
    params.put("sourceUrl", Gate.getUrl("tests/sgml/Hds.sgm"));
    params.put("markupAware", "false");
    doc = (Document)Factory.createResource("gate.corpora.DocumentImpl",
                                                    params);

//    doc = gate.Factory.newDocument(new URL("file:///d:/tmp/Learner/wui2fn08.cls.sgm"));
    // get the docFormat that deals with it.
    // the parameter MimeType doesn't affect right now the behaviour
    //*
    gate.DocumentFormat docFormat = gate.DocumentFormat.getDocumentFormat (
        doc, doc.getSourceUrl()
    );
    assert( "Bad document Format was produced. SgmlDocumentFormat was expected",
            docFormat instanceof gate.corpora.SgmlDocumentFormat
          );

    // set's the map
    docFormat.setMarkupElementsMap(markupElementsMap);
    docFormat.unpackMarkup (doc,"DocumentContent");
  }// testSgml

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestSgml.class);
  } // suite

} // class TestSgml
