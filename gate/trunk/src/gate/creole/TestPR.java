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
 *  Oana Hamza,
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;
import java.net.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.corpora.TestDocument;
import gate.creole.tokeniser.*;
import gate.creole.gazetteer.*;
import gate.creole.splitter.*;
import gate.creole.orthomatcher.*;

/** Test the PRs on three documents */
public class TestPR extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestPR(String name) { super(name); }

  /** Fixture set up */
  public void setUp() throws Exception {
  } // setUp

  /** Put things back as they should be after running tests.
    */
  public void tearDown() throws Exception {
  } // tearDown

  /** A test */
  public void testAllPR() throws Exception {

    //get 3 documents
    Document doc1 = Factory.newDocument(
      new URL(TestDocument.getTestServerName() +
        "tests/ft-bt-03-aug-2001.html")
    );
    Document doc2 = Factory.newDocument(
      new URL(TestDocument.getTestServerName() +
        "tests/gu-Am-Brit-4-aug-2001.html")
    );

    Document doc3 = Factory.newDocument(
      new URL(TestDocument.getTestServerName() +
        "tests/in-outlook-09-aug-2001.html")
    );

      //create a default tokeniser
     FeatureMap params = Factory.newFeatureMap();
     DefaultTokeniser tokeniser = (DefaultTokeniser) Factory.createResource(
                    "gate.creole.tokeniser.DefaultTokeniser", params);

    //create a default gazetteer
    params = Factory.newFeatureMap();
    DefaultGazetteer gaz = (DefaultGazetteer) Factory.createResource(
                          "gate.creole.gazetteer.DefaultGazetteer", params);

    //create a splitter
    params = Factory.newFeatureMap();
    SentenceSplitter splitter = (SentenceSplitter) Factory.createResource(
                          "gate.creole.splitter.SentenceSplitter", params);
    //create a tagger
    params = Factory.newFeatureMap();
    POSTagger tagger = (POSTagger) Factory.createResource(
                          "gate.creole.POSTagger", params);
    //create a grammar
    params = Factory.newFeatureMap();
    ANNIETransducer transducer = (ANNIETransducer) Factory.createResource(
                          "gate.creole.ANNIETransducer", params);

    //create a orthomatcher
    params = Factory.newFeatureMap();
    OrthoMatcher orthomatcher = (OrthoMatcher) Factory.createResource(
                          "gate.creole.orthomatcher.OrthoMatcher", params);

    //run the tokeniser for doc1
    tokeniser.setDocument(doc1);
    tokeniser.execute();
    assert("Found "+ doc1.getAnnotations().size() +
      " Token annotations, instead of the expected 1286.",
      doc1.getAnnotations().size()== 1286);

    //run the tokeniser for doc2
    tokeniser.setDocument(doc2);
    tokeniser.execute();
    assert("Found "+ doc2.getAnnotations().size() +
      " Token annotations, instead of the expected 2144.",
      doc2.getAnnotations().size()== 2144);

    //run the tokeniser for doc3
    tokeniser.setDocument(doc3);
    tokeniser.execute();
    assert("Found "+ doc3.getAnnotations().size() +
      " Token annotations, instead of the expected 2812.",
      doc3.getAnnotations().size()== 2812);

    //run gazetteer for doc1
    gaz.setDocument(doc1);
    gaz.execute();
    assert("Found "+ doc1.getAnnotations().get("Lookup").size() +
      " Lookup annotations, instead of the expected 47.",
      doc1.getAnnotations().get("Lookup").size()== 47);

    //run gazetteer for doc2
    gaz.setDocument(doc2);
    gaz.execute();
    assert("Found "+ doc2.getAnnotations().get("Lookup").size() +
      " Lookup annotations, instead of the expected 99.",
      doc2.getAnnotations().get("Lookup").size()== 99);

    //run gazetteer for doc3
    gaz.setDocument(doc3);
    gaz.execute();
    assert("Found "+ doc3.getAnnotations().get("Lookup").size() +
      " Lookup annotations, instead of the expected 112.",
      doc3.getAnnotations().get("Lookup").size()== 112);

    //run splitter for doc1
    splitter.setDocument(doc1);
    splitter.execute();
    assert("Found "+ doc1.getAnnotations().get("Sentence").size() +
      " Sentence annotations, instead of the expected 27.",
      doc1.getAnnotations().get("Sentence").size()== 27);

    assert("Found "+ doc1.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 45.",
      doc1.getAnnotations().get("Split").size()== 45);

    //run splitter for doc2
    splitter.setDocument(doc2);
    splitter.execute();
    assert("Found "+ doc2.getAnnotations().get("Sentence").size() +
      " Sentence annotations, instead of the expected 64.",
      doc2.getAnnotations().get("Sentence").size()== 64);

    assert("Found "+ doc2.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 88.",
      doc2.getAnnotations().get("Split").size()== 88);

    //run splitter for doc3
    splitter.setDocument(doc3);
    splitter.execute();
    assert("Found "+ doc3.getAnnotations().get("Sentence").size() +
      " Sentence annotations, instead of the expected 87.",
      doc3.getAnnotations().get("Sentence").size()== 87);

    assert("Found "+ doc3.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 109.",
      doc3.getAnnotations().get("Split").size()== 109);

    //run the tagger for doc1
    tagger.setDocument(doc1);
    tagger.execute();
    int count = 0;
    Iterator tokIter = doc1.getAnnotations().get("Token").iterator();
    while(tokIter.hasNext()){
      Annotation token = (Annotation)tokIter.next();
      if (token.getFeatures().containsKey("category"))
        count = count + 1;
    }
    assert("Found "+ count +
      " Token annotations with category feature, instead of the expected 657.",
      count == 657);

    //run the tagger for doc2
    tagger.setDocument(doc2);
    tagger.execute();
    count = 0;
    tokIter = doc2.getAnnotations().get("Token").iterator();
    while(tokIter.hasNext()){
      Annotation token = (Annotation)tokIter.next();
      if (token.getFeatures().containsKey("category"))
        count = count + 1;
    }
    assert("Found "+ count +
      " Token annotations with category feature, instead of the expected 1081.",
      count == 1081);

    //run the tagger for doc3
    tagger.setDocument(doc3);
    tagger.execute();
    count = 0;
    tokIter = doc3.getAnnotations().get("Token").iterator();
    while(tokIter.hasNext()){
      Annotation token = (Annotation)tokIter.next();
      if (token.getFeatures().containsKey("category"))
        count = count + 1;
    }
    assert("Found "+ count +
      " Token annotations with category feature, instead of the expected 1376.",
      count == 1376);

    //run the transducer for doc1
    transducer.setDocument(doc1);
    transducer.execute();
    assert("Found "+ doc1.getAnnotations().get("Organization").size() +
      " Organization annotations, instead of the expected 16",
      doc1.getAnnotations().get("Organization").size()== 16);
    assert("Found "+ doc1.getAnnotations().get("Location").size() +
      " Location annotations, instead of the expected 3",
      doc1.getAnnotations().get("Location").size()== 3);
    assert("Found "+ doc1.getAnnotations().get("Person").size() +
      " Person annotations, instead of the expected 3",
      doc1.getAnnotations().get("Person").size()== 3);
    assert("Found "+ doc1.getAnnotations().get("Date").size() +
      " Date annotations, instead of the expected 6",
      doc1.getAnnotations().get("Date").size()== 6);
    assert("Found "+ doc1.getAnnotations().get("Money").size() +
      " Money annotations, instead of the expected 1",
      doc1.getAnnotations().get("Money").size()== 1);

    //run the transducer for doc2
    transducer.setDocument(doc2);
    transducer.execute();
    assert("Found "+ doc2.getAnnotations().get("Organization").size() +
      " Organization annotations, instead of the expected 18",
      doc2.getAnnotations().get("Organization").size()== 18);
    assert("Found "+ doc2.getAnnotations().get("Location").size() +
      " Location annotations, instead of the expected 9",
      doc2.getAnnotations().get("Location").size()== 9);
    assert("Found "+ doc2.getAnnotations().get("Person").size() +
      " Person annotations, instead of the expected 1",
      doc2.getAnnotations().get("Person").size()== 1);
    assert("Found "+ doc2.getAnnotations().get("Date").size() +
      " Date annotations, instead of the expected 6",
      doc2.getAnnotations().get("Date").size()== 6);
    assert("Found "+ doc2.getAnnotations().get("Money").size() +
      " Money annotations, instead of the expected 3",
      doc2.getAnnotations().get("Money").size()== 3);

    //run the transducer for doc3
    transducer.setDocument(doc3);
    transducer.execute();
    assert("Found "+ doc3.getAnnotations().get("Organization").size() +
      " Organization annotations, instead of the expected 9",
      doc3.getAnnotations().get("Organization").size()== 9);
    assert("Found "+ doc3.getAnnotations().get("Location").size() +
      " Location annotations, instead of the expected 12",
      doc3.getAnnotations().get("Location").size()== 12);
    assert("Found "+ doc3.getAnnotations().get("Person").size() +
      " Person annotations, instead of the expected 8",
      doc3.getAnnotations().get("Person").size()== 8);
    assert("Found "+ doc3.getAnnotations().get("Date").size() +
      " Date annotations, instead of the expected 7",
      doc3.getAnnotations().get("Date").size()== 7);
    assert("Found "+ doc3.getAnnotations().get("Money").size() +
      " Money annotations, instead of the expected 4",
      doc3.getAnnotations().get("Money").size()== 4);

    // run the orthomatcher for doc1
    orthomatcher.setDocument(doc1);
    orthomatcher.execute();
    count = 0;
    Iterator orthoIter = doc1.getAnnotations().iterator();
    while(orthoIter.hasNext()){
      Annotation annot = (Annotation)orthoIter.next();
      if (annot.getFeatures().containsKey("matches"))
        count = count + 1;
    }
    assert("Found "+ count +
      " annotations with matches feature, instead of the expected 29.",
      count == 29);

    //run the orthomatcher for doc2
    orthomatcher.setDocument(doc2);
    orthomatcher.execute();
    count = 0;
    orthoIter = doc2.getAnnotations().iterator();
    while(orthoIter.hasNext()){
      Annotation annot = (Annotation)orthoIter.next();
      if (annot.getFeatures().containsKey("matches"))
        count = count + 1;
    }
    assert("Found "+ count +
      " annotations with matches feature, instead of the expected 35.",
      count == 35);

    //run the orthomatcher for doc3
    orthomatcher.setDocument(doc3);
    orthomatcher.execute();
    count = 0;
    orthoIter = doc3.getAnnotations().iterator();
    while(orthoIter.hasNext()){
      Annotation annot = (Annotation)orthoIter.next();
      if (annot.getFeatures().containsKey("matches"))
        count = count + 1;
    }
    assert("Found "+ count +
      " annotations with matches feature, instead of the expected 22.",
      count == 22);

  } // testAllPR()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestPR.class);
  } // suite

  public static void main(String[] args) {
    try{
      Gate.init();
      TestPR testPR = new TestPR("");
      testPR.setUp();
      testPR.testAllPR();
      testPR.tearDown();
    } catch(Exception e) {
      e.printStackTrace();
    }
  } // main
} // class TestTemplate