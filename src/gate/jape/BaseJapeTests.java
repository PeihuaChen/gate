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
import gate.util.*;

import java.io.StringReader;
import java.util.*;

import junit.framework.TestCase;

/**
 * Tests for Constraint predicate logic
 */
public class BaseJapeTests extends TestCase {

  protected static final String DEFAULT_DATA_FILE = "jape/InputTexts/AveShort";

  public BaseJapeTests(String name) {
    super(name);
  }

  /** Fixture set up */
  public void setUp() {
    // Out.println("TestConstraints.setUp()");
  } // setUp

  protected Set<Annotation> doTest(String dataFile, String japeFile, AnnotationCreator ac)
          throws ResourceInstantiationException, JapeException,
          ExecutionException {
    Corpus c = createCorpus(dataFile);
    // add some annotations
    Document doc = (Document)c.get(0);
    ac.createAnnots(doc);

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

  /**
   * Callback interface used in the doTest method.
   *
   * @version $Revision$
   * @author esword
   */
  protected static interface AnnotationCreator {
    public AnnotationSet createAnnots(Document doc);
  }
}
