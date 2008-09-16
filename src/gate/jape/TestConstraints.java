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
import gate.jape.parser.ParseException;
import gate.util.Err;
import gate.util.Out;

import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for Constraint predicate logic
 */
public class TestConstraints extends BaseJapeTests {

  protected static final String JAPE_PREFIX =
    "Phase: first\n Options: control = appelt\nRule: RuleOne\n";

  public TestConstraints(String name) {
    super(name);
  }

  public void testGoodOperators() throws Exception {
    String japeFile = "/jape/operators/operator_tests.jape";
    String[] expectedResults = {"AndEqual", "RegExMatch",
        "NotEqualandGreaterEqual", "NotEqual", "EqualAndNotEqualRegEx",
        "EqualAndNotExistance", "OntoTest", "OntoTest2"};

    Set<Annotation> actualResults = doTest(DEFAULT_DATA_FILE, japeFile, basicAnnotCreator);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);
  }

  public void testBadCompare() throws Exception {
    String japeFile = "/jape/operators/bad_operator_tests.jape";

    Set<Annotation> orderedResults = doTest(DEFAULT_DATA_FILE, japeFile, basicAnnotCreator);
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

  /**
   * Visually, this creates an annot set like this:
         1         2
12345678901234567890
 AA
 B
   BB
     B
     CC
       CC
           DD
             DD
               DD
                 D
12345678901234567890
         1         2
   */
  protected AnnotationCreator basicAnnotCreator = new AnnotationCreator() {
    public AnnotationSet createAnnots(Document doc) {
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
        feat.put("f2", 2l);
        defaultAS.add(new Long(14), new Long(16), "D", feat);

        feat = Factory.newFeatureMap();
        feat.put("ontology", "http://gate.ac.uk/tests/demo.owl");
        feat.put("class", "Businessman");
        defaultAS.add(new Long(16), new Long(18), "D", feat);

        feat = Factory.newFeatureMap();
        feat.put("ontology", "http://gate.ac.uk/tests/demo.owl");
        feat.put("class", "Country");
        defaultAS.add(new Long(18), new Long(19), "D", feat);

      }
      catch(gate.util.InvalidOffsetException ioe) {
        ioe.printStackTrace(Err.getPrintWriter());
      }
      return defaultAS;
    }
  };

  public void testMetaPropertyAccessors() throws Exception {
    String data = "foo bar blah word4    word5  ";
    String japeFile = "/jape/operators/meta_property_tests.jape";
    String[] expectedResults = {"LengthAccessorEqual", "StringAccessorEqual", "CleanStringAccessorEqual"};

    AnnotationCreator ac = new AnnotationCreator() {
      public AnnotationSet createAnnots(Document doc) {
        AnnotationSet defaultAS = doc.getAnnotations();

        try {
          FeatureMap feat = Factory.newFeatureMap();
          feat.put("f1", "aval");
          feat.put("f2", "2");
          feat.put("f3", 3);
          defaultAS.add(new Long(4), new Long(7), "A", feat);

          feat = Factory.newFeatureMap();
          defaultAS.add(new Long(8), new Long(12), "A", feat);

          feat = Factory.newFeatureMap();
          defaultAS.add(new Long(12), new Long(28), "B", feat);
        }
        catch(gate.util.InvalidOffsetException ioe) {
          ioe.printStackTrace(Err.getPrintWriter());
        }
        return defaultAS;
      }
    };

    Set<Annotation> actualResults = doTest(createDoc(data), japeFile, ac);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);
  }

  public void testCustomPredicates() throws Exception {
    String japeFile = "/jape/operators/custom_predicates_tests.jape";
    String[] expectedResults = {"Contains", "IsContained"};

    AnnotationCreator ac = new AnnotationCreator() {
      public AnnotationSet createAnnots(Document doc) {
        AnnotationSet defaultAS = doc.getAnnotations();

        try {
          FeatureMap feat = Factory.newFeatureMap();
          defaultAS.add(new Long(4), new Long(7), "A", feat);

          feat = Factory.newFeatureMap();
          feat.put("f2", "bar");
          defaultAS.add(new Long(5), new Long(6), "B", feat);

          feat = Factory.newFeatureMap();
          defaultAS.add(new Long(12), new Long(28), "B", feat);
          feat = Factory.newFeatureMap();
          defaultAS.add(new Long(14), new Long(20), "A", feat);
        }
        catch(gate.util.InvalidOffsetException ioe) {
          ioe.printStackTrace(Err.getPrintWriter());
        }
        return defaultAS;
      }
    };

    Set<Annotation> actualResults = doTest(DEFAULT_DATA_FILE, japeFile, ac);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);
  }

  public void testCustomPredicatesWithConstraints() throws Exception {
    String japeFile = "/jape/operators/custom_predicates_tests.jape";
    String[] expectedResults = {"ContainsWithConstraints","ContainsWithMetaProperty"};

    AnnotationCreator ac = new AnnotationCreator() {
      public AnnotationSet createAnnots(Document doc) {
        AnnotationSet defaultAS = doc.getAnnotations();

        try {
          FeatureMap cFeat = Factory.newFeatureMap();
          cFeat = Factory.newFeatureMap();
          cFeat.put("f1", "foo");
          defaultAS.add(new Long(4), new Long(7), "C", cFeat);
          defaultAS.add(new Long(4), new Long(8), "C", Factory.newFeatureMap());

          FeatureMap bFeat = Factory.newFeatureMap();
          bFeat.put("f2", "bar");
          defaultAS.add(new Long(5), new Long(6), "B", bFeat);

          //this combo won't work because B doesn't have the feature and isn't long enough
          defaultAS.add(new Long(8), new Long(10), "C", cFeat);
          defaultAS.add(new Long(8), new Long(9), "B", Factory.newFeatureMap());

          defaultAS.add(new Long(11), new Long(13), "C", Factory.newFeatureMap());
          //a combo that should work
          defaultAS.add(new Long(12), new Long(16), "C", cFeat);
          defaultAS.add(new Long(12), new Long(15), "C", Factory.newFeatureMap());
          defaultAS.add(new Long(12), new Long(16), "B", Factory.newFeatureMap());

          //here's one with no B at all
          defaultAS.add(new Long(17), new Long(20), "C", cFeat);

        }
        catch(gate.util.InvalidOffsetException ioe) {
          ioe.printStackTrace(Err.getPrintWriter());
        }
        return defaultAS;
      }
    };

    Set<Annotation> actualResults = doTest(DEFAULT_DATA_FILE, japeFile, ac);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);
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
        TestConstraints testConstraints = new TestConstraints("Test Constraints");
        testConstraints.setUp();
        // testConstraints
      }
      catch(Exception e) {
        e.printStackTrace(Err.getPrintWriter());
      }
    }
  }
} // class TestJape
