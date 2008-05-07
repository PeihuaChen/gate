/*
 *  TestConstraints
 *
 *  Copyright (c) 1998-2008, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Eric Sword, 03/09/08
 *
 *  $Id$
 */
package gate.jape;

import gate.*;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.jape.parser.ParseCpsl;
import gate.jape.parser.ParseException;
import gate.util.*;

import java.io.StringReader;
import java.util.*;

import junit.framework.*;

/**
 * Tests for Constraint predicate logic
 */
public class TestConstraints extends TestCase {
  protected static final String JAPE_PREFIX =
    "Phase: first\n Options: control = appelt\nRule: RuleOne\n";

  protected static final String DEFAULT_DATA_FILE = "jape/InputTexts/AveShort";

  /** Construction */
  public TestConstraints(String name) {
    super(name);
  }

  /** Fixture set up */
  public void setUp() {
    // Out.println("TestConstraints.setUp()");
  } // setUp

  public void testGoodOperators() throws Exception {
    String japeFile = "/jape/operators/operator_tests.jape";
    String[] expectedResults = {"AndEqual", "SimpleRegEx",
        "NotEqualandGreaterEqual", "NotEqual", "EqualAndNotEqualRexEx",
        "EqualAndNotExistance", "OntoTest"};

    Set<Annotation> actualResults = doTest(DEFAULT_DATA_FILE, japeFile);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);
  }

  public void testBadCompare() throws Exception {
    String japeFile = "/jape/operators/bad_operator_tests.jape";

    Set<Annotation> orderedResults = doTest(DEFAULT_DATA_FILE, japeFile);
    assertTrue("No results should be found", orderedResults.isEmpty());
  }

  public void testBadPattern() throws Exception {
    String japeString = JAPE_PREFIX + "({A.f1=~\"[a.*\"}):abc" + "-->{}";

    try {
      parseJapeString(japeString);
      assertTrue("Should have thrown exception for bad grammer", false);
    }
    catch(RuntimeException e) {
      // insert test of error message if really want
    }

    japeString = JAPE_PREFIX + "({A.f1=~[a.*}):abc" + "-->{}";
    try {
      parseJapeString(japeString);
      assertTrue("Should have thrown exception for bad grammer", false);
    }
    catch(ParseException e) {
      // insert test of error message if really want
    }
  }

  protected Set<Annotation> doTest(String dataFile, String japeFile)
          throws ResourceInstantiationException, JapeException,
          ExecutionException {
    Corpus c = createCorpus(dataFile);
    // add some annotations
    Document doc = (Document)c.get(0);
    createAnnots(doc);

    Set<Annotation> orderedResults = runTransducer(c, japeFile);
    return orderedResults;
  }

  protected void compareResults(String[] expectedResults,
          Set<Annotation> actualResults) {
    int i = 0;

    assertEquals("Different number of results expected",
            expectedResults.length, actualResults.size());

    for(Annotation annot : actualResults) {
      String ruleName = (String)annot.getFeatures().get("rule");
      assertEquals("Rule " + expectedResults[i] + " did not match as expected",
              expectedResults[i], ruleName);
      i++;
    }
  }

  protected Corpus createCorpus(String fileName)
          throws ResourceInstantiationException {
    Corpus c = Factory.newCorpus("TestJape corpus");

    try {
      c.add(Factory.newDocument(Files.getGateResourceAsString(fileName)));
    }
    catch(Exception e) {
      e.printStackTrace(Err.getPrintWriter());
    }

    if(c.isEmpty()) {
      assertTrue("Missing corpus !", false);
    }
    return c;
  }

  protected AnnotationSet createAnnots(Document doc) {
    AnnotationSet defaultAS = doc.getAnnotations();

    try {
      FeatureMap feat = Factory.newFeatureMap();
      feat.put("f1", "atext");
      feat.put("f2", "2");
      feat.put("f3", 3);

      defaultAS.add(new Long(2), new Long(4), "A", feat);
      feat = Factory.newFeatureMap();
      feat.put("f1", "btext");
      feat.put("f2", "2");
      feat.put("f4", "btext4");
      defaultAS.add(new Long(2), new Long(3), "B", feat);

      defaultAS.add(new Long(4), new Long(6), "B", feat);

      feat = Factory.newFeatureMap();
      feat.put("f1", "cctext");
      feat.put("f2", "2");
      feat.put("f3", 3l);
      feat.put("f4", "ctext4");
      defaultAS.add(new Long(6), new Long(7), "B", feat);
      defaultAS.add(new Long(6), new Long(8), "C", feat);

      feat = Factory.newFeatureMap();
      feat.put("f1", "cctext");
      feat.put("f2", "1");
      feat.put("f4", "ctext4");
      defaultAS.add(new Long(8), new Long(10), "C", feat);

      feat = Factory.newFeatureMap();
      feat.put("f1", "dtext");
      feat.put("f3", 3l);
      defaultAS.add(new Long(12), new Long(14), "D", feat);

      feat = Factory.newFeatureMap();
      feat.put("f1", "dtext");
      defaultAS.add(new Long(14), new Long(16), "D", feat);

      feat = Factory.newFeatureMap();
      feat.put("ontology", "http://gate.ac.uk/tests/demo.owl");
      feat.put("class", "Businessman");
      defaultAS.add(new Long(16), new Long(18), "D", feat);

    }
    catch(gate.util.InvalidOffsetException ioe) {
      ioe.printStackTrace(Err.getPrintWriter());
    }
    return defaultAS;
  }

  protected Set<Annotation> runTransducer(Corpus c, String japeFile)
          throws JapeException, ExecutionException {
    Document doc;
    Batch batch = new Batch(Files.getGateResource(japeFile), "UTF-8");
    batch.transduce(c);
    // check the results
    doc = (Document)c.get(0);
    Set<Annotation> orderedResults = new TreeSet<Annotation>(
            new OffsetComparator());
    orderedResults.addAll(doc.getAnnotations().get("Result"));
    return orderedResults;
  }

  /**
   * Fast routine for parsing a small string of jape rules.
   *
   * @param japeRules
   * @throws Exception
   */
  protected void parseJapeString(String japeRules) throws Exception {
    StringReader sr = new StringReader(japeRules);
    ParseCpsl parser = Factory.newJapeParser(sr, new HashMap());
    Transducer transducer = parser.MultiPhaseTransducer();
    transducer.finish();
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestConstraints.class);
  } // suite

  // main method for running this test as a standalone test
  public static void main(String[] args) {
    for(int i = 0; i < 6; i++) {
      System.gc();
      Out.println("Run " + i + "   ==============");
      try {
        TestConstraints testConstraints = new TestConstraints(
                "Test Constraints");
        testConstraints.setUp();
        // testConstraints
      }
      catch(Exception e) {
        e.printStackTrace(Err.getPrintWriter());
      }
    }
  }
} // class TestJape
