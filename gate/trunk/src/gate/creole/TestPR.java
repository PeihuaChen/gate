/*
 *  TestPR.java
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
import gate.persist.*;

/** Test the PRs on three documents */
public class TestPR extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  protected static Document doc1;
  protected static Document doc2;
  protected static Document doc3;

  /** Construction */
  public TestPR(String name) { super(name); }

  /** Fixture set up */
  public void setUp() throws Exception {
    //get 3 documents
    if (doc1 == null)
      doc1 = Factory.newDocument(
        new URL(TestDocument.getTestServerName() +
        "tests/ft-bt-03-aug-2001.html")
      );

    if (doc2 == null)
      doc2 = Factory.newDocument(
        new URL(TestDocument.getTestServerName() +
          "tests/gu-Am-Brit-4-aug-2001.html")
      );

    if (doc3 == null)
      doc3 = Factory.newDocument(
        new URL(TestDocument.getTestServerName() +
          "tests/in-outlook-09-aug-2001.html")
      );

  } // setUp

  /** Put things back as they should be after running tests.
    */
  public void tearDown() throws Exception {
  } // tearDown

  public void testTokenizer() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    DefaultTokeniser tokeniser = (DefaultTokeniser) Factory.createResource(
                    "gate.creole.tokeniser.DefaultTokeniser", params);


    //run the tokeniser for doc1
    tokeniser.setDocument(doc1);
    tokeniser.execute();
    assert("Found in ft-bt-03-aug-2001.html "+ doc1.getAnnotations().size() +
      " Token annotations, instead of the expected 1286.",
      doc1.getAnnotations().size()== 1286);

    //run the tokeniser for doc2
    tokeniser.setDocument(doc2);
    tokeniser.execute();
    assert("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().size() +
      " Token annotations, instead of the expected 2144.",
      doc2.getAnnotations().size()== 2144);

    //run the tokeniser for doc3
    tokeniser.setDocument(doc3);
    tokeniser.execute();
    assert("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().size() +
      " Token annotations, instead of the expected 2812.",
      doc3.getAnnotations().size()== 2812);

    Factory.deleteResource(tokeniser);
  }// testTokenizer

  public void testGazetteer() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    DefaultGazetteer gaz = (DefaultGazetteer) Factory.createResource(
                          "gate.creole.gazetteer.DefaultGazetteer", params);

    //run gazetteer for doc1
    gaz.setDocument(doc1);
    gaz.execute();
    assert("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Lookup").size() +
      " Lookup annotations, instead of the expected 47.",
      doc1.getAnnotations().get("Lookup").size()== 47);

    //run gazetteer for doc2
    gaz.setDocument(doc2);
    gaz.execute();
    assert("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Lookup").size() +
      " Lookup annotations, instead of the expected 99.",
      doc2.getAnnotations().get("Lookup").size()== 99);

    //run gazetteer for doc3
    gaz.setDocument(doc3);
    gaz.execute();
    assert("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Lookup").size() +
      " Lookup annotations, instead of the expected 112.",
      doc3.getAnnotations().get("Lookup").size()== 112);
    Factory.deleteResource(gaz);
  }//testGazetteer

  public void testSplitter() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    SentenceSplitter splitter = (SentenceSplitter) Factory.createResource(
                          "gate.creole.splitter.SentenceSplitter", params);

    //run splitter for doc1
    splitter.setDocument(doc1);
    splitter.execute();
    assert("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Sentence").size() +
      " Sentence annotations, instead of the expected 22.",
      doc1.getAnnotations().get("Sentence").size()== 22);

    assert("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 36.",
      doc1.getAnnotations().get("Split").size()== 36);


    //run splitter for doc2
    splitter.setDocument(doc2);
    splitter.execute();
    assert("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Sentence").size() +
      " Sentence annotations, instead of the expected 53.",
      doc2.getAnnotations().get("Sentence").size()== 53);

    assert("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 71.",
      doc2.getAnnotations().get("Split").size()== 71);

    //run splitter for doc3
    splitter.setDocument(doc3);
    splitter.execute();

    assert("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Sentence").size() +
      " Sentence annotations, instead of the expected 65.",
      doc3.getAnnotations().get("Sentence").size()== 65);

    assert("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 85.",
      doc3.getAnnotations().get("Split").size()== 85);
    Factory.deleteResource(splitter);
  }//testSplitter

  public void testTagger() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    POSTagger tagger = (POSTagger) Factory.createResource(
                          "gate.creole.POSTagger", params);


    //run the tagger for doc1
    tagger.setDocument(doc1);
    tagger.execute();

    HashSet fType = new HashSet();
    fType.add("category");
    AnnotationSet annots =
                  doc1.getAnnotations().get("Token", fType);

    assert("Found in ft-bt-03-aug-2001.html "+ annots.size() +
      " Token annotations with category feature, instead of the expected 675.",
      annots.size() == 675);

    //run the tagger for doc2
    tagger.setDocument(doc2);
    tagger.execute();
    annots = doc2.getAnnotations().get("Token", fType);
    assert("Found in gu-Am-Brit-4-aug-2001.html "+ annots.size() +
      " Token annotations with category feature, instead of the expected 1131.",
      annots.size() == 1131);

    //run the tagger for doc3
    tagger.setDocument(doc3);
    tagger.execute();
    annots = doc3.getAnnotations().get("Token", fType);
    assert("Found in in-outlook-09-aug-2001.html "+ annots.size() +
      " Token annotations with category feature, instead of the expected 1426.",
      annots.size() == 1426);
    Factory.deleteResource(tagger);
  }//testTagger()

  public void testTransducer() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    ANNIETransducer transducer = (ANNIETransducer) Factory.createResource(
                          "gate.creole.ANNIETransducer", params);

    //run the transducer for doc1
    transducer.setDocument(doc1);
    transducer.execute();
    assert("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Organization").size() +
      " Organization annotations, instead of the expected 16",
      doc1.getAnnotations().get("Organization").size()== 16);
    assert("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Location").size() +
      " Location annotations, instead of the expected 3",
      doc1.getAnnotations().get("Location").size()== 3);
    assert("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Person").size() +
      " Person annotations, instead of the expected 3",
      doc1.getAnnotations().get("Person").size()== 3);
    assert("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Date").size() +
      " Date annotations, instead of the expected 6",
      doc1.getAnnotations().get("Date").size()== 6);
    assert("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Money").size() +
      " Money annotations, instead of the expected 1",
      doc1.getAnnotations().get("Money").size()== 1);

    //run the transducer for doc2
    transducer.setDocument(doc2);
    transducer.execute();
    assert("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Organization").size() +
      " Organization annotations, instead of the expected 18",
      doc2.getAnnotations().get("Organization").size()== 18);
    assert("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Location").size() +
      " Location annotations, instead of the expected 9",
      doc2.getAnnotations().get("Location").size()== 9);
    assert("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Person").size() +
      " Person annotations, instead of the expected 1",
      doc2.getAnnotations().get("Person").size()== 1);
    assert("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Date").size() +
      " Date annotations, instead of the expected 6",
      doc2.getAnnotations().get("Date").size()== 6);
    assert("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Money").size() +
      " Money annotations, instead of the expected 3",
      doc2.getAnnotations().get("Money").size()== 3);

    //run the transducer for doc3
    transducer.setDocument(doc3);
    transducer.execute();
    assert("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Organization").size() +
      " Organization annotations, instead of the expected 9",
      doc3.getAnnotations().get("Organization").size()== 9);
    assert("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Location").size() +
      " Location annotations, instead of the expected 12",
      doc3.getAnnotations().get("Location").size()== 12);
    assert("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Person").size() +
      " Person annotations, instead of the expected 8",
      doc3.getAnnotations().get("Person").size()== 8);
    assert("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Date").size() +
      " Date annotations, instead of the expected 7",
      doc3.getAnnotations().get("Date").size()== 7);
    assert("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Money").size() +
      " Money annotations, instead of the expected 4",
      doc3.getAnnotations().get("Money").size()== 4);

    Factory.deleteResource(transducer);
  }//testTransducer

  public void testOrthomatcher() throws Exception {
    FeatureMap params = Factory.newFeatureMap();

    OrthoMatcher orthomatcher = (OrthoMatcher) Factory.createResource(
                          "gate.creole.orthomatcher.OrthoMatcher", params);


    // run the orthomatcher for doc1
    orthomatcher.setDocument(doc1);
    orthomatcher.execute();

    HashSet fType = new HashSet();
    fType.add("matches");
    AnnotationSet annots =
                  doc1.getAnnotations().get(null,fType);

    assert("Found in ft-bt-03-aug-2001.html "+ annots.size() +
      " annotations with matches feature, instead of the expected 29.",
      annots.size() == 29);

    //run the orthomatcher for doc2
    orthomatcher.setDocument(doc2);
    orthomatcher.execute();
    annots = doc2.getAnnotations().get(null,fType);
    assert("Found in gu-Am-Brit-4-aug-2001.html "+ annots.size() +
      " annotations with matches feature, instead of the expected 35.",
      annots.size() == 35);

    //run the orthomatcher for doc3
    orthomatcher.setDocument(doc3);
    orthomatcher.execute();

    annots = doc3.getAnnotations().get(null,fType);
    assert("Found in in-outlook-09-aug-2001.html "+ annots.size() +
      " annotations with matches feature, instead of the expected 20.",
      annots.size() == 20);
    Factory.deleteResource(orthomatcher);
  }//testOrthomatcher

  /** A test for comparing the annotation sets*/
  public void testAllPR() throws Exception {

    // verify if the saved data store is the same with the just processed file
    // first document
    String urlBaseName = Gate.locateGateFiles();
    if (urlBaseName.endsWith("/gate/build/gate.jar!/")) {
      StringBuffer buff = new StringBuffer(
                            urlBaseName.substring(
                              0,
                              urlBaseName.lastIndexOf("build/gate.jar!/"))
                            );
      buff.append("classes/");
      buff.delete(0, "jar:file:".length());
      buff.insert(0, "file://");
      urlBaseName = buff.toString();
    }

    URL urlBase = new URL(urlBaseName + "gate/resources/gate.ac.uk/");
    URL storageDir = null;
    storageDir = new URL(urlBase, "tests/ft");

    //open the data store
    DataStore ds = Factory.openDataStore
                    ("gate.persist.SerialDataStore", storageDir);
    //get LR id
    String lr_id_feature_map = (String)ds.getLrIds
                                ("gate.corpora.DocumentImpl").get(0);
    // get the document from data store
    Document document =
      (Document) ds.getLr("gate.corpora.DocumentImpl", lr_id_feature_map);
    // get annotation set
    AnnotationSet annotSet = document.getAnnotations();
    // get the annotation set from the first processed document
    AnnotationSet annotSet1 = doc1.getAnnotations();
    // compare the annotation set
    // assert("The annotation set from data store and processed document are " +
    //      "not equal for ft-bt-03-aug-2001.html ",annotSet.equals(annotSet1));
    assert("The Organization set from data store and processed " +
      "document are not equal for ft-bt-03-aug-2001.html ",
      annotSet.get("Organization").equals(annotSet1.get("Organization")));

    assert("The Location set from data store and processed " +
      "document are not equal for ft-bt-03-aug-2001.html ",
      annotSet.get("Location").equals(annotSet1.get("Location")));

    assert("The Person set from data store and processed " +
      "document are not equal for ft-bt-03-aug-2001.html ",
      annotSet.get("Person").equals(annotSet1.get("Person")));

    assert("The Date set from data store and processed " +
      "document are not equal for ft-bt-03-aug-2001.html ",
      annotSet.get("Date").equals(annotSet1.get("Date")));

    assert("The Money set from data store and processed " +
      "document are not equal for ft-bt-03-aug-2001.html ",
      annotSet.get("Money").equals(annotSet1.get("Money")));

    assert("The Token set from data store and processed " +
      "document are not equal for ft-bt-03-aug-2001.html ",
      annotSet.get("Token").equals(annotSet1.get("Token")));

    assert("The Lookup set from data store and processed " +
      "document are not equal for ft-bt-03-aug-2001.html ",
      annotSet.get("Lookup").equals(annotSet1.get("Lookup")));

    assert("The Sentence set from data store and processed " +
      "document are not equal for ft-bt-03-aug-2001.html ",
      annotSet.get("Sentence").equals(annotSet1.get("Sentence")));

    assert("The Split set from data store and processed " +
      "document are not equal for ft-bt-03-aug-2001.html ",
      annotSet.get("Split").equals(annotSet1.get("Split")));

    // second document
    storageDir = null;
    storageDir = new URL(urlBase, "tests/gu");

    //open the data store
    ds = Factory.openDataStore
                    ("gate.persist.SerialDataStore", storageDir);
    //get LR id
    lr_id_feature_map = (String)ds.getLrIds
                                ("gate.corpora.DocumentImpl").get(0);
    // get the document from data store
    document =
      (Document) ds.getLr("gate.corpora.DocumentImpl", lr_id_feature_map);
    // get annotation set
    annotSet = document.getAnnotations();
    // get the annotation set from the second processed document
    AnnotationSet annotSet2 = doc2.getAnnotations();
    /*assert("The annotation set from data store and processed document are " +
      "not equal for gu-Am-Brit-4-aug-2001.html ",annotSet.equals(annotSet2));*/
    assert("The Organization set from data store and processed " +
      "document are not equal for gu-Am-Brit-4-aug-2001.html ",
      annotSet.get("Organization").equals(annotSet2.get("Organization")));

    assert("The Location set from data store and processed " +
      "document are not equal for gu-Am-Brit-4-aug-2001.html ",
      annotSet.get("Location").equals(annotSet2.get("Location")));

    assert("The Person set from data store and processed " +
      "document are not equal for gu-Am-Brit-4-aug-2001.html ",
      annotSet.get("Person").equals(annotSet2.get("Person")));

    assert("The Date set from data store and processed " +
      "document are not equal for gu-Am-Brit-4-aug-2001.html ",
      annotSet.get("Date").equals(annotSet2.get("Date")));

    assert("The Money set from data store and processed " +
      "document are not equal for gu-Am-Brit-4-aug-2001.html ",
      annotSet.get("Money").equals(annotSet2.get("Money")));

    assert("The Token set from data store and processed " +
      "document are not equal for gu-Am-Brit-4-aug-2001.html ",
      annotSet.get("Token").equals(annotSet2.get("Token")));

    assert("The Lookup set from data store and processed " +
      "document are not equal for gu-Am-Brit-4-aug-2001.html ",
      annotSet.get("Lookup").equals(annotSet2.get("Lookup")));

    assert("The Sentence set from data store and processed " +
      "document are not equal for gu-Am-Brit-4-aug-2001.html ",
      annotSet.get("Sentence").equals(annotSet2.get("Sentence")));

    assert("The Split set from data store and processed " +
      "document are not equal for gu-Am-Brit-4-aug-2001.html ",
      annotSet.get("Split").equals(annotSet2.get("Split")));

    // third document
    storageDir = null;
    storageDir = new URL(urlBase, "tests/in");

    //open the data store
    ds = Factory.openDataStore
                    ("gate.persist.SerialDataStore", storageDir);
    //get LR id
    lr_id_feature_map = (String)ds.getLrIds
                                ("gate.corpora.DocumentImpl").get(0);
    // get the document from data store
    document =
      (Document) ds.getLr("gate.corpora.DocumentImpl", lr_id_feature_map);
    // get annotation set
    annotSet = document.getAnnotations();
    // get the annotation set from the third processed document
    AnnotationSet annotSet3 = doc3.getAnnotations();
/*    assert("The annotation set from data store and processed document are " +
      "not equal for in-outlook-09-aug-2001.html  ",annotSet.equals(annotSet3));*/
    assert("The Organization set from data store and processed " +
      "document are not equal for ft-bt-03-aug-2001.html ",
      annotSet.get("Organization").equals(annotSet3.get("Organization")));

    assert("The Location set from data store and processed " +
      "document are not equal for in-outlook-09-aug-2001.html ",
      annotSet.get("Location").equals(annotSet3.get("Location")));

    assert("The Person set from data store and processed " +
      "document are not equal for in-outlook-09-aug-2001.html ",
      annotSet.get("Person").equals(annotSet3.get("Person")));

    assert("The Date set from data store and processed " +
      "document are not equal for in-outlook-09-aug-2001.html ",
      annotSet.get("Date").equals(annotSet3.get("Date")));

    assert("The Money set from data store and processed " +
      "document are not equal for in-outlook-09-aug-2001.html ",
      annotSet.get("Money").equals(annotSet3.get("Money")));

    assert("The Token set from data store and processed " +
      "document are not equal for in-outlook-09-aug-2001.html ",
      annotSet.get("Token").equals(annotSet3.get("Token")));

    assert("The Lookup set from data store and processed " +
      "document are not equal for in-outlook-09-aug-2001.html ",
      annotSet.get("Lookup").equals(annotSet3.get("Lookup")));

    assert("The Sentence set from data store and processed " +
      "document are not equal for in-outlook-09-aug-2001.html ",
      annotSet.get("Sentence").equals(annotSet3.get("Sentence")));

    assert("The Split set from data store and processed " +
      "document are not equal for in-outlook-09-aug-2001.html ",
      annotSet.get("Split").equals(annotSet3.get("Split")));

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
      testPR.testTokenizer();
      testPR.testGazetteer();
      testPR.testSplitter();
      testPR.testTagger();
      testPR.testTransducer();
      testPR.testOrthomatcher();
      testPR.testAllPR();
      testPR.tearDown();
    } catch(Exception e) {
      e.printStackTrace();
    }
  } // main
} // class TestTemplate