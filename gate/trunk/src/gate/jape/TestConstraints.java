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
  protected AnnotationCreator basicAnnotCreator = new BaseAnnotationCreator() {
    public AnnotationSet createAnnots(Document doc) {
      setDoc(doc);

      FeatureMap feat = Factory.newFeatureMap();
      feat.put("f1", "atext");
      feat.put("f2", "2");
      feat.put("f3", 3);
      add(2, 4, "A", feat);

      feat = Factory.newFeatureMap();
      feat.put("f1", "btext");
      feat.put("f2", "2");
      feat.put("f4", "btext4");
      add(2, 3, "B", feat);
      add(4, 6, "B", feat);

      feat = Factory.newFeatureMap();
      feat.put("f1", "cctext");
      feat.put("f2", "2");
      feat.put("f3", 3l);
      feat.put("f4", "ctext4");
      add(6, 7, "B", feat);
      add(6, 8, "C", feat);

      feat = Factory.newFeatureMap();
      feat.put("f1", "cctext");
      feat.put("f2", "1");
      feat.put("f4", "ctext4");
      add(8, 10, "C", feat);

      feat = Factory.newFeatureMap();
      feat.put("f1", "dtext");
      feat.put("f3", 3l);
      add(12, 14, "D", feat);

      feat = Factory.newFeatureMap();
      feat.put("f2", 2l);
      add(14, 16, "D", feat);

      feat = Factory.newFeatureMap();
      feat.put("ontology", "http://gate.ac.uk/tests/demo.owl");
      feat.put("class", "Businessman");
      add(16, 18, "D", feat);

      feat = Factory.newFeatureMap();
      feat.put("ontology", "http://gate.ac.uk/tests/demo.owl");
      feat.put("class", "Country");
      add(18, 19, "D", feat);
      return as;
    }
  };

  public void testMetaPropertyAccessors() throws Exception {
    String data = "foo bar blah word4    word5  ";
    String japeFile = "/jape/operators/meta_property_tests.jape";
    String[] expectedResults = {"LengthAccessorEqual", "StringAccessorEqual", "CleanStringAccessorEqual"};

    AnnotationCreator ac = new BaseAnnotationCreator() {
      public AnnotationSet createAnnots(Document doc) {
        setDoc(doc);
        FeatureMap feat = Factory.newFeatureMap();
        feat.put("f1", "aval");
        feat.put("f2", "2");
        feat.put("f3", 3);
        add(4, 7, "A", feat);

        add(8, 12, "A");
        add(12, 28, "B");
        return as;
      }
    };

    Set<Annotation> actualResults = doTest(createDoc(data), japeFile, ac);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);
  }

  public void testCustomPredicates() throws Exception {
    String japeFile = "/jape/operators/custom_predicates_tests.jape";
    String[] expectedResults = {"Contains", "IsContained"};

    AnnotationCreator ac = new BaseAnnotationCreator() {
      public AnnotationSet createAnnots(Document doc) {
        setDoc(doc);
        add(4, 7, "A");

        FeatureMap feat = Factory.newFeatureMap();
        feat.put("f2", "bar");
        add(5, 6, "B", feat);

        add(12, 28, "B");
        add(14, 20, "A");

        return as;
      }
    };

    Set<Annotation> actualResults = doTest(DEFAULT_DATA_FILE, japeFile, ac);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);
  }

  public void testCustomPredicatesWithConstraints() throws Exception {
    String japeFile = "/jape/operators/custom_predicates_tests.jape";
    String[] expectedResults = {"ContainsWithConstraints","ContainsWithMetaProperty"};

    AnnotationCreator ac = new BaseAnnotationCreator() {
      public AnnotationSet createAnnots(Document doc) {
        setDoc(doc);
        FeatureMap cFeat = Factory.newFeatureMap();
        cFeat.put("f1", "foo");
        add(4, 7, "C", cFeat);
        add(4, 8, "C", Factory.newFeatureMap());

        FeatureMap bFeat = Factory.newFeatureMap();
        bFeat.put("f2", "bar");
        add(5, 6, "B", bFeat);

        //this combo won't work because B doesn't have the feature and isn't long enough
        add(8, 10, "C", cFeat);
        add(8, 9, "B");

        add(11, 13, "C");
        //a combo that should work
        add(12, 16, "C", cFeat);
        add(12, 15, "C");
        add(12, 16, "B");

        //here's one with no B at all
        add(17, 20, "C", cFeat);

        return as;
      }
    };

    Set<Annotation> actualResults = doTest(DEFAULT_DATA_FILE, japeFile, ac);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);
  }

  public void testRanges() throws Exception {
    String japeFile = "/jape/operators/range_tests.jape";
    String[] expectedResults = {"OneToTwoB", "ThreeA", "OneToTwoB", "ZeroToThreeC", "ThreeToFourB", "ThreeToFourB", "ZeroToThreeC", "ZeroToThreeC"};

    AnnotationCreator ac = new BaseAnnotationCreator() {
      public AnnotationSet createAnnots(Document doc) {
        setDoc(doc);

        //OneToTwoB check
        addInc("F");
        addInc("B");
        addInc("G");

        //ThreeA check
        addInc("A");
        addInc("A");
        addInc("A");

        //should not trigger OneToTwoB
        addInc("F");
        addInc("G");

        //ThreeA check - should not match
        addInc("A");
        addInc("A");

        //OneToTwoB - trigger it once for two different variants
        addInc("F");
        addInc("B");
        addInc("G");
        addInc("F");
        addInc("B");
        addInc("B");
        addInc("G");

        //ZeroToThreeC check - no Cs
        addInc("D");
        addInc("E");

        //ThreeToFourB
        addInc("F");
        addInc("B");
        addInc("B");
        addInc("B");
        addInc("G");
        addInc("F");
        addInc("B");
        addInc("B");
        addInc("B");
        addInc("B");
        addInc("G");

        //ZeroToThreeC check - 1 C
        addInc("D");
        addInc("C");
        addInc("E");

        //ZeroToThreeC check - 3 C
        addInc("D");
        addInc("C");
        addInc("C");
        addInc("C");
        addInc("E");

        //ZeroToThreeC check - 4 C = no match
        addInc("D");
        addInc("C");
        addInc("C");
        addInc("C");
        addInc("C");
        addInc("E");

        return as;
      }
    };

    Set<Annotation> actualResults = doTest(DEFAULT_DATA_FILE, japeFile, ac);
    Out.println(actualResults);
    compareResults(expectedResults, actualResults);
  }

  public void testKleeneOperators() throws Exception {
    String japeFile = "/jape/operators/kleene_tests.jape";
    String[] expectedResults = {"OptionalB", "PlusA", "OptionalB", "PlusA", "StarC", "StarC", "StarC"};


    AnnotationCreator ac = new BaseAnnotationCreator() {
      public AnnotationSet createAnnots(Document doc) {
        setDoc(doc);

        //OptionalB check
        addInc("C");
        addInc("B");
        addInc("C");

        //PlusA check
        addInc("A");
        addInc("A");
        addInc("A");

        //OptionalB check
        addInc("C");
        addInc("C");
        //PlusA
        addInc("A");
        addInc("A");

        //no match
        addInc("B");

        //StarC
        addInc("D");
        addInc("E");

        //StarC
        addInc("D");
        addInc("C");
        addInc("E");

        //StarC
        addInc("D");
        addInc("C");
        addInc("C");
        addInc("C");
        addInc("E");

        return as;
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
