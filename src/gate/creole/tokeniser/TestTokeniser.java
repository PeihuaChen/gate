/*
 * TestTokeniser.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan, 25/10/2000
 *
 * $Id$
 */

package gate.creole.tokeniser;

/**
 * Title:        Gate2
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      University Of Sheffield
 * @author Hamish, Kalina, Valy, Cristi
 * @version 1.0
 */
import java.util.*;
import java.io.*;
import java.net.*;
import java.beans.*;
import java.lang.reflect.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.corpora.TestDocument;

public class TestTokeniser extends TestCase{

  public TestTokeniser(String name) {
    super(name);
  }

  /** Fixture set up */
  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  } // tearDown

  /** Test the default tokeniser */
  public void testDefaultTokeniser() throws Exception {
    //get a document
    Document doc = Factory.newDocument(
      new URL(TestDocument.getTestServerName() + "tests/doc0.html")
    );
    //create a default tokeniser
   FeatureMap params = Factory.newFeatureMap();
   DefaultTokeniser tokeniser = (DefaultTokeniser) Factory.createResource(
                          "gate.creole.tokeniser.DefaultTokeniser", params);

    AnnotationSet tokeniserAS = doc.getAnnotations("TokeniserAS");
    tokeniser.setDocument(doc);
    tokeniser.setAnnotationSet(tokeniserAS);
    tokeniser.run();
    assert(!tokeniserAS.isEmpty());
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestTokeniser.class);
  } // suite

  public static void main(String[] args) {
    try{
      Gate.init();
      TestTokeniser testTokeniser1 = new TestTokeniser("");
      testTokeniser1.setUp();
      testTokeniser1.testDefaultTokeniser();
      testTokeniser1.tearDown();
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}