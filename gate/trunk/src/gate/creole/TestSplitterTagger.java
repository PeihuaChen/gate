/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 17/05/01
 *
 *  $Id$
 */
package gate.creole;

import java.net.URL;
import junit.framework.*;

import gate.*;
import gate.creole.*;
import gate.corpora.TestDocument;
import gate.util.*;
import gate.creole.splitter.*;

/**
 * Test code for the SentenceSplitter and the POS tagger.
 */
public class TestSplitterTagger extends TestCase{

/** Construction */
  public TestSplitterTagger(String name) { super(name); }

  /** Fixture set up */
  public void setUp() throws GateException {
  } // setUp

  /** Put things back as they should be after running tests
    * (reinitialise the CREOLE register).
    */
  public void tearDown() throws Exception {
  } // tearDown

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestSplitterTagger.class);
  } // suite


  public void testSplitterTagger() throws Exception{
    //get a document
    Document doc = Factory.newDocument(
      new URL(TestDocument.getTestServerName() + "tests/doc0.html")
    );

    //create a splitter
    FeatureMap params = Factory.newFeatureMap();
    SentenceSplitter splitter = (SentenceSplitter) Factory.createResource(
                          "gate.creole.splitter.SentenceSplitter", params);

    //runtime stuff
    splitter.setDocument(doc);
    splitter.setOutputASName("splitterAS");
    splitter.run();
    //check for exceptions
    splitter.check();
    assert(!doc.getAnnotations("splitterAS").isEmpty());

    //now check the tagger

  }
}