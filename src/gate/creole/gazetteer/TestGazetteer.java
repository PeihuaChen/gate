/*
 *  TestGazetteer.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 25/10/2000
 *
 *  $Id$
 */

package gate.creole.gazetteer;

import java.net.URL;

import junit.framework.*;

import gate.*;
import gate.corpora.TestDocument;

public class TestGazetteer extends TestCase {

  public TestGazetteer(String name) {
    super(name);
  }

  /** Fixture set up */
  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  } // tearDown

  /** Test the default tokeniser */
  public void testDefaultGazetteer() throws Exception {
    //get a document
    Document doc = Factory.newDocument(
      new URL(TestDocument.getTestServerName() + "tests/doc0.html")
    );

    //create a default gazetteer
    FeatureMap params = Factory.newFeatureMap();
    params.put(DefaultGazetteer.DEF_GAZ_LISTS_URL_PARAMETER_NAME,
      new URL("gate:/creole/gazeteer/default/lists.def"));
    DefaultGazetteer gaz = (DefaultGazetteer) Factory.createResource(
                          "gate.creole.gazetteer.DefaultGazetteer", params);

    //runtime stuff
    gaz.setDocument(doc);
    gaz.setAnnotationSetName("GazetteerAS");
    gaz.execute();
    assertTrue(!doc.getAnnotations("GazetteerAS").isEmpty());
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestGazetteer.class);
  } // suite

  public static void main(String[] args) {
    try{
      Gate.init();
      TestGazetteer testGaz = new TestGazetteer("");
      testGaz.setUp();
      testGaz.testDefaultGazetteer();
      testGaz.tearDown();
    } catch(Exception e) {
      e.printStackTrace();
    }
  } // main

} // TestGazetteer
