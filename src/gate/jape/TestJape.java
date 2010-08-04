/*
 *  TestJape.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 23/Feb/00
 *
 *  $Id$
 */

package gate.jape;

import gate.*;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.persist.PersistenceException;
import gate.util.*;
import gate.util.persistence.PersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;



/** Tests for the Corpus classes
  */
public class TestJape extends BaseJapeTests
{
  private static final Logger log = Logger.getLogger(TestJape.class);

  /** Construction */
  public TestJape(String name) { super(name); }

  /** Batch run */
  public void testBatch() throws Exception{
    Corpus c = Factory.newCorpus("TestJape corpus");
    c.add(
      Factory.newDocument(Files.getGateResourceAsString("texts/doc0.html"))
    );
    //add some annotations on the first (only) document in corpus c
    Document doc = (Document)c.get(0);
    AnnotationSet defaultAS = doc.getAnnotations();

    try {
      FeatureMap feat = Factory.newFeatureMap();
      // defaultAS.add(new Long( 0), new Long( 2), "A",feat);
      defaultAS.add(new Long( 2), new Long( 4), "A",feat);
      // defaultAS.add(new Long( 4), new Long( 6), "A",feat);
      // defaultAS.add(new Long( 6), new Long( 8), "A",feat);
      defaultAS.add(new Long( 4), new Long(6), "B",feat);
      // defaultAS.add(new Long(10), new Long(12), "B",feat);
      // defaultAS.add(new Long(12), new Long(14), "B",feat);
      // defaultAS.add(new Long(14), new Long(16), "B",feat);
      // defaultAS.add(new Long(16), new Long(18), "B",feat);
      defaultAS.add(new Long(6), new Long(8), "C",feat);
      defaultAS.add(new Long(8), new Long(10), "C",feat);
      // defaultAS.add(new Long(22), new Long(24), "C",feat);
      // defaultAS.add(new Long(24), new Long(26), "C",feat);
    } catch(gate.util.InvalidOffsetException ioe) {
      ioe.printStackTrace(Err.getPrintWriter());
    }

    Batch batch = new Batch(
            Files.getGateResource("/jape/TestABC.jape"), "UTF-8");
    // test code: print the first line of the jape stream
    // Out.println(
    //   new BufferedReader(new InputStreamReader(japeFileStream)).readLine()
    // );

    // test the transducers
    batch.transduce(c);
    // check the results
    doc = (Document)c.get(0);
    // defaultAS = doc.getAnnotations();
    // Out.println(defaultAS);
  } // testBatch()

  /**
   * This test loads a saved application which runs several JAPE grammars
   * using different application modes on a specially prepared document.
   * The resulting annotations are checked against gold-standard versions
   * saved in the test document.
   * @throws IOException
   * @throws ResourceInstantiationException
   * @throws PersistenceException
   * @throws ExecutionException
   */
  public void testApplicationModes() throws PersistenceException, ResourceInstantiationException, IOException, ExecutionException{
    //load the application
    URL applicationURL = Files.getGateResource(
            "gate.ac.uk/tests/jape/jape-test.xgapp");
    CorpusController application = (CorpusController)
        PersistenceManager.loadObjectFromUrl(applicationURL);
    //load the test file
    Document testDoc = Factory.newDocument(Files.getGateResource(
            "gate.ac.uk/tests/jape/test-doc.xml"), "UTF-8");
    Corpus testCorpus = Factory.newCorpus("JAPE Test Corpus");
    testCorpus.add(testDoc);
    //run the application
    application.setCorpus(testCorpus);
    application.execute();
    //check the results
    AnnotationDiffer annDiff = new AnnotationDiffer();
    annDiff.setSignificantFeaturesSet(null);
    for(String testName : new String[]{"appelt", "brill", "all", "once"}){
      AnnotationSet keySet = testDoc.getAnnotations(testName);
      AnnotationSet responseSet = testDoc.getAnnotations(testName + "-test");
      annDiff.calculateDiff(keySet, responseSet);
      double fMeasure = annDiff.getFMeasureStrict(1);
      assertEquals("Incorrect F-measure for test " + testName,
              (double)1, fMeasure);
    }
    //cleanup
    application.setCorpus(null);
    Factory.deleteResource(application);
    testCorpus.remove(0);
    Factory.deleteResource(testDoc);
    Factory.deleteResource(testCorpus);
  }

  /**
   * This test sets up a JAPE transducer based on a grammar
   * (RhsError.jape) that will throw a null pointer exception.
   * The test succeeds so long as we get that exception.
   */
  public void testRhsErrorMessages() {
    boolean gotException = false;

    try {
      if(log.isDebugEnabled()) {
        log.info(
          "Opening Jape grammar... " + Gate.getUrl("tests/RhsError.jape")
        );
      }
      // a JAPE batcher
      Batch batch = new Batch(Gate.getUrl("tests/RhsError.jape"), "UTF-8");

      // a document with an annotation
      Document doc = Factory.newDocument("This is a Small Document.");
      FeatureMap features = Factory.newFeatureMap();
      features.put("orth", "upperInitial");
      doc.getAnnotations().add(new Long(0), new Long(8), "Token", features);

      // run jape on the document
      batch.transduce(doc);
    } catch(Exception e) {
      if(log.isDebugEnabled())
        log.info("Exception in Jape rule: " + e);
      gotException = true;
    }

    assertTrue("Bad JAPE grammar didn't throw an exception", gotException);

  }  // testRhsErrorMessages

  public void testBrill() throws IOException, GateException, Exception {
    String japeFile = "/gate.ac.uk/tests/jape/control_mode_tests/brill_test.jape";
    String[] expectedResults = {"Find_A", "Find_A", "Find_A_B", "Find_A_B", "Find_A_B_C"};

    AnnotationCreator annotCreator = new BaseAnnotationCreator() {
      public AnnotationSet createAnnots(Document doc) {
        setDoc(doc);

        add(2, 4, "A");
        add(2, 5, "A");
        add(3, 5, "A");
        add(4, 6, "B");
        add(5, 7, "B");
        add(6, 8, "C");
        add(8, 10, "D");

        return as;
      }
    };

    Set<Annotation> actualResults = doTest(DEFAULT_DATA_FILE, japeFile, annotCreator);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);

  } // testBrill()

  public void testAppeltMode() throws IOException, GateException, Exception {
    String japeFile = "/gate.ac.uk/tests/jape/control_mode_tests/appelt_test.jape";
    String[] expectedResults = {"Find_A_B_C"};

    AnnotationCreator annotCreator = new BaseAnnotationCreator() {
      public AnnotationSet createAnnots(Document doc) {
        setDoc(doc);

        add(2, 4, "A");
        add(4, 6, "B");
        add(2, 3, "C");
        add(3, 8, "D");
        add(2, 3, "A");
        add(3, 4, "B");
        add(4, 9, "C");
        return as;
      }
    };

    Set<Annotation> actualResults = doTest(DEFAULT_DATA_FILE, japeFile, annotCreator);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);

  } // testAppelt()


  public void testAllMode() throws IOException, GateException, Exception {
    String japeFile = "/gate.ac.uk/tests/jape/control_mode_tests/all_mode_test.jape";
    String[] expectedResults = {"Find_A", "Find_A", "Find_A_B", "Find_A_B", "Find_A_B_C",
            "Find_A", "Find_A_B", "Find_B_C"};

    AnnotationCreator annotCreator = new BaseAnnotationCreator() {
      public AnnotationSet createAnnots(Document doc) {
        setDoc(doc);

        add(2, 4, "A");
        add(2, 5, "A");
        add(3, 5, "A");
        add(4, 6, "B");
        add(5, 7, "B");
        add(6, 8, "C");
        add(8, 10, "D");
        return as;
      }
    };

    Set<Annotation> actualResults = doTest(DEFAULT_DATA_FILE, japeFile, annotCreator);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);

  } // testAppelt()


//  /**
//   * This test sets up a JAPE transducer based on a grammar
//   * (RhsError2.jape) that will throw a compiler error.
//   * The test succeeds so long as we get that exception.
//   */
//  public void testRhsErrorMessages2() {
//    boolean gotException = false;
//
//    // disable System.out so that the compiler can't splash its error on screen
//    if(DEBUG) System.out.println("hello 1");
//    PrintStream sysout = System.out;
//    System.setOut(new PrintStream(new ByteArrayOutputStream()));
//    if(DEBUG) System.out.println("hello 2");
//
//    // run a JAPE batch on the faulty grammar
//    try {
//      if(DEBUG) {
//        Out.print(
//          "Opening Jape grammar... " + Gate.getUrl("tests/RhsError2.jape")
//        );
//      }
//      // a JAPE batcher
//      Batch batch = new Batch(Gate.getUrl("tests/RhsError2.jape"), "UTF-8");
//    } catch(Exception e) {
//      if(DEBUG) Out.prln(e);
//      gotException = true;
//    } finally {
//
//      // re-enable System.out
//      System.setOut(sysout);
//      if(DEBUG) System.out.println("hello 3");
//    }
//
//    assertTrue("Bad JAPE grammar (2) didn't throw an exception", gotException);
//
//  }  // testRhsErrorMessages2
//

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestJape.class);
  } // suite

} // class TestJape
