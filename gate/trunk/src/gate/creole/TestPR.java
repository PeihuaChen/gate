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
import gnu.regexp.*;

import gate.*;
import gate.util.*;
import gate.corpora.TestDocument;
import gate.creole.tokeniser.*;
import gate.creole.gazetteer.*;
import gate.creole.splitter.*;
import gate.creole.orthomatcher.*;
import gate.persist.*;
import gate.annotation.*;
import gate.creole.ANNIEConstants;

/** Test the PRs on three documents */
public class TestPR extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  protected static Document doc1;
  protected static Document doc2;
  protected static Document doc3;

  protected static List annotationTypes = new ArrayList(10);

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

    annotationTypes.add(ANNIEConstants.SENTENCE_ANNOTATION_TYPE);
    annotationTypes.add(ANNIEConstants.ORGANIZATION_ANNOTATION_TYPE);
    annotationTypes.add(ANNIEConstants.LOCATION_ANNOTATION_TYPE);
    annotationTypes.add(ANNIEConstants.PERSON_ANNOTATION_TYPE);
    annotationTypes.add(ANNIEConstants.DATE_ANNOTATION_TYPE);
    annotationTypes.add(ANNIEConstants.MONEY_ANNOTATION_TYPE);
    annotationTypes.add(ANNIEConstants.LOOKUP_ANNOTATION_TYPE);
    annotationTypes.add(ANNIEConstants.TOKEN_ANNOTATION_TYPE);
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
    assertTrue("Found in "+doc1.getSourceUrl().getFile()+ " "+
      doc1.getAnnotations().size() +
      " Token annotations, instead of the expected 1284.",
      doc1.getAnnotations().size()== 1284);

    //run the tokeniser for doc2
    tokeniser.setDocument(doc2);
    tokeniser.execute();
    assertTrue("Found in "+ doc2.getSourceUrl().getFile()+ " "+
      doc2.getAnnotations().size() +
      " Token annotations, instead of the expected 2138.",
      doc2.getAnnotations().size()== 2138);

    //run the tokeniser for doc3
    tokeniser.setDocument(doc3);
    tokeniser.execute();
    assertTrue("Found in "+ doc3.getSourceUrl().getFile()+ " "+
      doc3.getAnnotations().size() +
      " Token annotations, instead of the expected 2806.",
      doc3.getAnnotations().size()== 2806);

    Factory.deleteResource(tokeniser);
  }// testTokenizer

  public void testGazetteer() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    DefaultGazetteer gaz = (DefaultGazetteer) Factory.createResource(
                          "gate.creole.gazetteer.DefaultGazetteer", params);

    //run gazetteer for doc1
    gaz.setDocument(doc1);
    gaz.execute();
    assertTrue("Found in "+ doc1.getSourceUrl().getFile()+ " "+
      doc1.getAnnotations().get(ANNIEConstants.LOOKUP_ANNOTATION_TYPE).size() +
      " Lookup annotations, instead of the expected 62.",
      doc1.getAnnotations().get(ANNIEConstants.LOOKUP_ANNOTATION_TYPE).size()== 62);

    //run gazetteer for doc2
    gaz.setDocument(doc2);
    gaz.execute();
    assertTrue("Found in "+ doc2.getSourceUrl().getFile()+ " "+
      doc2.getAnnotations().get(ANNIEConstants.LOOKUP_ANNOTATION_TYPE).size() +
      " Lookup annotations, instead of the expected 106.",
      doc2.getAnnotations().get(ANNIEConstants.LOOKUP_ANNOTATION_TYPE).size()== 106);

    //run gazetteer for doc3
    gaz.setDocument(doc3);
    gaz.execute();
    assertTrue("Found in "+ doc3.getSourceUrl().getFile()+ " "+
      doc3.getAnnotations().get(ANNIEConstants.LOOKUP_ANNOTATION_TYPE).size() +
      " Lookup annotations, instead of the expected 121.",
      doc3.getAnnotations().get(ANNIEConstants.LOOKUP_ANNOTATION_TYPE).size()== 121);
    Factory.deleteResource(gaz);
  }//testGazetteer

  public void testSplitter() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    SentenceSplitter splitter = (SentenceSplitter) Factory.createResource(
                          "gate.creole.splitter.SentenceSplitter", params);

    //run splitter for doc1
    splitter.setDocument(doc1);
    splitter.execute();
    assertTrue("Found in "+ doc1.getSourceUrl().getFile()+ " "+
      doc1.getAnnotations().get(ANNIEConstants.SENTENCE_ANNOTATION_TYPE).size() +
      " Sentence annotations, instead of the expected 22.",
      doc1.getAnnotations().get(ANNIEConstants.SENTENCE_ANNOTATION_TYPE).size()== 22);

    assertTrue("Found in "+ doc1.getSourceUrl().getFile()+ " "+
      doc1.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 36.",
      doc1.getAnnotations().get("Split").size()== 36);


    //run splitter for doc2
    splitter.setDocument(doc2);
    splitter.execute();
    assertTrue("Found in "+ doc2.getSourceUrl().getFile()+ " "+
      doc2.getAnnotations().get(ANNIEConstants.SENTENCE_ANNOTATION_TYPE).size() +
      " Sentence annotations, instead of the expected 52.",
      doc2.getAnnotations().get(ANNIEConstants.SENTENCE_ANNOTATION_TYPE).size()== 52);

    assertTrue("Found in "+ doc2.getSourceUrl().getFile()+ " "+
      doc2.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 71.",
      doc2.getAnnotations().get("Split").size()== 71);

    //run splitter for doc3
    splitter.setDocument(doc3);
    splitter.execute();

    assertTrue("Found in "+ doc3.getSourceUrl().getFile()+ " "+
      doc3.getAnnotations().get(ANNIEConstants.SENTENCE_ANNOTATION_TYPE).size() +
      " Sentence annotations, instead of the expected 66.",
      doc3.getAnnotations().get(ANNIEConstants.SENTENCE_ANNOTATION_TYPE).size()== 66);

    assertTrue("Found in "+ doc3.getSourceUrl().getFile()+ " "+
      doc3.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 84.",
      doc3.getAnnotations().get("Split").size()== 84);
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
    fType.add(ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME);
    AnnotationSet annots =
      doc1.getAnnotations().get(ANNIEConstants.TOKEN_ANNOTATION_TYPE, fType);

    assertTrue("Found in "+ doc1.getSourceUrl().getFile()+ " "+ annots.size() +
      " Token annotations with category feature, instead of the expected 675.",
      annots.size() == 675);

    //run the tagger for doc2
    tagger.setDocument(doc2);
    tagger.execute();
    annots = doc2.getAnnotations().get(ANNIEConstants.TOKEN_ANNOTATION_TYPE, fType);
    assertTrue("Found in "+  doc2.getSourceUrl().getFile()+ " "+annots.size() +
      " Token annotations with category feature, instead of the expected 1131.",
      annots.size() == 1131);

    //run the tagger for doc3
    tagger.setDocument(doc3);
    tagger.execute();
    annots = doc3.getAnnotations().get(ANNIEConstants.TOKEN_ANNOTATION_TYPE, fType);
    assertTrue("Found in "+ doc3.getSourceUrl().getFile()+ " "+ annots.size() +
      " Token annotations with category feature, instead of the expected 1443.",
      annots.size() == 1443);
    Factory.deleteResource(tagger);
  }//testTagger()

  public void testTransducer() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    ANNIETransducer transducer = (ANNIETransducer) Factory.createResource(
                          "gate.creole.ANNIETransducer", params);

    //run the transducer for doc1
    transducer.setDocument(doc1);
    transducer.execute();
    assertTrue("Found in "+ doc1.getSourceUrl().getFile()+ " "+
      doc1.getAnnotations().get(ANNIEConstants.ORGANIZATION_ANNOTATION_TYPE).size() +
      " Organization annotations, instead of the expected 17",
      doc1.getAnnotations().get(ANNIEConstants.ORGANIZATION_ANNOTATION_TYPE).size()== 17);
    assertTrue("Found in "+doc1.getSourceUrl().getFile()+ " "+
      doc1.getAnnotations().get(ANNIEConstants.LOCATION_ANNOTATION_TYPE).size() +
      " Location annotations, instead of the expected 3",
      doc1.getAnnotations().get(ANNIEConstants.LOCATION_ANNOTATION_TYPE).size()== 3);
    assertTrue("Found in "+doc1.getSourceUrl().getFile()+ " "+
      doc1.getAnnotations().get(ANNIEConstants.PERSON_ANNOTATION_TYPE).size() +
      " Person annotations, instead of the expected 3",
      doc1.getAnnotations().get(ANNIEConstants.PERSON_ANNOTATION_TYPE).size()== 3);
    assertTrue("Found in "+doc1.getSourceUrl().getFile()+ " "+
      doc1.getAnnotations().get(ANNIEConstants.DATE_ANNOTATION_TYPE).size() +
      " Date annotations, instead of the expected 6",
      doc1.getAnnotations().get(ANNIEConstants.DATE_ANNOTATION_TYPE).size()== 6);
    assertTrue("Found in "+doc1.getSourceUrl().getFile()+ " "+
      doc1.getAnnotations().get(ANNIEConstants.MONEY_ANNOTATION_TYPE).size() +
      " Money annotations, instead of the expected 1",
      doc1.getAnnotations().get(ANNIEConstants.MONEY_ANNOTATION_TYPE).size()== 1);

    //run the transducer for doc2
    transducer.setDocument(doc2);
    transducer.execute();
    assertTrue("Found in "+doc2.getSourceUrl().getFile()+ " "+
      doc2.getAnnotations().get(ANNIEConstants.ORGANIZATION_ANNOTATION_TYPE).size() +
      " Organization annotations, instead of the expected 18",
      doc2.getAnnotations().get(ANNIEConstants.ORGANIZATION_ANNOTATION_TYPE).size()== 18);
    assertTrue("Found in "+doc2.getSourceUrl().getFile()+ " "+
      doc2.getAnnotations().get(ANNIEConstants.LOCATION_ANNOTATION_TYPE).size() +
      " Location annotations, instead of the expected 9",
      doc2.getAnnotations().get(ANNIEConstants.LOCATION_ANNOTATION_TYPE).size()== 9);
    assertTrue("Found in "+doc2.getSourceUrl().getFile()+ " "+
      doc2.getAnnotations().get(ANNIEConstants.PERSON_ANNOTATION_TYPE).size() +
      " Person annotations, instead of the expected 1",
      doc2.getAnnotations().get(ANNIEConstants.PERSON_ANNOTATION_TYPE).size()== 1);
    assertTrue("Found in "+doc2.getSourceUrl().getFile()+ " "+
      doc2.getAnnotations().get(ANNIEConstants.DATE_ANNOTATION_TYPE).size() +
      " Date annotations, instead of the expected 6",
      doc2.getAnnotations().get(ANNIEConstants.DATE_ANNOTATION_TYPE).size()== 6);
    assertTrue("Found in "+doc2.getSourceUrl().getFile()+ " "+
      doc2.getAnnotations().get(ANNIEConstants.MONEY_ANNOTATION_TYPE).size() +
      " Money annotations, instead of the expected 3",
      doc2.getAnnotations().get(ANNIEConstants.MONEY_ANNOTATION_TYPE).size()== 3);

    //run the transducer for doc3
    transducer.setDocument(doc3);
    transducer.execute();
    assertTrue("Found in "+doc3.getSourceUrl().getFile()+ " "+
      doc3.getAnnotations().get(ANNIEConstants.ORGANIZATION_ANNOTATION_TYPE).size() +
      " Organization annotations, instead of the expected 9",
      doc3.getAnnotations().get(ANNIEConstants.ORGANIZATION_ANNOTATION_TYPE).size()== 9);
    assertTrue("Found in "+doc3.getSourceUrl().getFile()+ " "+
      doc3.getAnnotations().get(ANNIEConstants.LOCATION_ANNOTATION_TYPE).size() +
      " Location annotations, instead of the expected 12",
      doc3.getAnnotations().get(ANNIEConstants.LOCATION_ANNOTATION_TYPE).size()== 12);
    assertTrue("Found in "+doc3.getSourceUrl().getFile()+ " "+
      doc3.getAnnotations().get(ANNIEConstants.PERSON_ANNOTATION_TYPE).size() +
      " Person annotations, instead of the expected 8",
      doc3.getAnnotations().get(ANNIEConstants.PERSON_ANNOTATION_TYPE).size()== 8);
    assertTrue("Found in "+doc3.getSourceUrl().getFile()+ " "+
      doc3.getAnnotations().get(ANNIEConstants.DATE_ANNOTATION_TYPE).size() +
      " Date annotations, instead of the expected 7",
      doc3.getAnnotations().get(ANNIEConstants.DATE_ANNOTATION_TYPE).size()== 7);
    assertTrue("Found in "+doc3.getSourceUrl().getFile()+ " "+
      doc3.getAnnotations().get(ANNIEConstants.MONEY_ANNOTATION_TYPE).size() +
      " Money annotations, instead of the expected 4",
      doc3.getAnnotations().get(ANNIEConstants.MONEY_ANNOTATION_TYPE).size()== 4);

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
    fType.add(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME);
    AnnotationSet annots =
                  doc1.getAnnotations().get(null,fType);

    assertTrue("Found in "+doc1.getSourceUrl().getFile()+ " "+ annots.size() +
      " annotations with matches feature, instead of the expected 30.",
      annots.size() == 30);

    //run the orthomatcher for doc2
    orthomatcher.setDocument(doc2);
    orthomatcher.execute();
    annots = doc2.getAnnotations().get(null,fType);
    assertTrue("Found in "+doc2.getSourceUrl().getFile()+ " "+ annots.size() +
      " annotations with matches feature, instead of the expected 35.",
      annots.size() == 33);

    //run the orthomatcher for doc3
    orthomatcher.setDocument(doc3);
    orthomatcher.execute();

    annots = doc3.getAnnotations().get(null,fType);
    assertTrue("Found in "+doc3.getSourceUrl().getFile()+ " "+ annots.size() +
      " annotations with matches feature, instead of the expected 24.",
      annots.size() == 24);
    Factory.deleteResource(orthomatcher);
  }//testOrthomatcher

  /** A test for comparing the annotation sets*/
  public void testAllPR() throws Exception {

    // verify if the saved data store is the same with the just processed file
    // first document
    String urlBaseName = Gate.locateGateFiles();
//    RE re1 = new RE("build/gate.jar!");
//    RE re2 = new RE("jar:");
//    urlBaseName = re1.substituteAll( urlBaseName,"classes");
//    urlBaseName = re2.substituteAll( urlBaseName,"");

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
                    ("gate.persist.SerialDataStore",
                     storageDir.toExternalForm());

    //get LR id
    String lrId = (String)ds.getLrIds
                                ("gate.corpora.DocumentImpl").get(0);


    // get the document from data store
    FeatureMap features = Factory.newFeatureMap();
    features.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    features.put(DataStore.LR_ID_FEATURE_NAME, lrId);
    Document document = (Document) Factory.createResource(
                                      "gate.corpora.DocumentImpl",
                                      features);
    compareAnnots(document, doc1);

    // second document
    storageDir = null;
    storageDir = new URL(urlBase, "tests/gu");

    //open the data store
    ds = Factory.openDataStore("gate.persist.SerialDataStore",
                               storageDir.toExternalForm());
    //get LR id
    lrId = (String)ds.getLrIds("gate.corpora.DocumentImpl").get(0);
    // get the document from data store
    features = Factory.newFeatureMap();
    features.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    features.put(DataStore.LR_ID_FEATURE_NAME, lrId);
    document = (Document) Factory.createResource(
                                      "gate.corpora.DocumentImpl",
                                      features);
    compareAnnots(document,doc2);

    // third document
    storageDir = null;
    storageDir = new URL(urlBase, "tests/in");

    //open the data store
    ds = Factory.openDataStore("gate.persist.SerialDataStore",
                               storageDir.toExternalForm());
    //get LR id
    lrId = (String)ds.getLrIds("gate.corpora.DocumentImpl").get(0);
    // get the document from data store
    features = Factory.newFeatureMap();
    features.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    features.put(DataStore.LR_ID_FEATURE_NAME, lrId);
    document = (Document) Factory.createResource(
                                "gate.corpora.DocumentImpl",
                                features);
    compareAnnots(document,doc3);
  } // testAllPR()

  public void compareAnnots(Document keyDocument, Document responseDocument)
              throws Exception{

    // create annotation schema
    AnnotationSchema annotationSchema = new AnnotationSchema();
    String annotType = null;

    // organization type
    Iterator iteratorTypes = annotationTypes.iterator();
    while (iteratorTypes.hasNext()){
      // get the type of annotation
      annotType = (String)iteratorTypes.next();

      annotationSchema.setAnnotationName(annotType);

      // create an annotation diff
      AnnotationDiff annotDiff = new AnnotationDiff();
      annotDiff.setKeyDocument(keyDocument);
      annotDiff.setResponseDocument(responseDocument);
      annotDiff.setAnnotationSchema(annotationSchema);
      annotDiff.setKeyAnnotationSetName(null);
      annotDiff.setResponseAnnotationSetName(null);

      Set significantFeatures = new HashSet(Arrays.asList(
                    new String[]{"NMRule", "kind", "orgType", "rule",
                                 "rule1", "rule2", "locType", "gender",
                                 "majorType", "minorType", "category",
                                 "length", "orth", "string", "subkind",
                                 "symbolkind"}));
      annotDiff.setKeyFeatureNamesSet(significantFeatures);
      annotDiff.setTextMode(new Boolean(true));

      annotDiff.init();

      if (DEBUG){
        if (annotDiff.getFMeasureAverage() != 1.0) {
          assertTrue("missing annotations " +
            annotDiff.getAnnotationsOfType(AnnotationDiff.MISSING_TYPE)
            + " spurious annotations " +
            annotDiff.getAnnotationsOfType(AnnotationDiff.SPURIOUS_TYPE)
            + " partially-correct annotations " +
            annotDiff.getAnnotationsOfType(
                            AnnotationDiff.PARTIALLY_CORRECT_TYPE),false);
        }
      }//if

      assertTrue(annotType+ " precision average in "+
        responseDocument.getSourceUrl().getFile()+
        " is "+ annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
        annotDiff.getPrecisionAverage()== 1.0);
      assertTrue(annotType+" recall average in "
        +responseDocument.getSourceUrl().getFile()+
        " is " + annotDiff.getRecallAverage()+ " instead of 1.0 ",
        annotDiff.getRecallAverage()== 1.0);
      assertTrue(annotType+" f-measure average in "
        +responseDocument.getSourceUrl().getFile()+
        " is "+ annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
        annotDiff.getFMeasureAverage()== 1.0);
     }//while
   }// public void compareAnnots

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
} // class TestPR
