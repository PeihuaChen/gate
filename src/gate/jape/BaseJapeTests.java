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

  protected Set<Annotation> doTest(Document doc, String japeFile,
          AnnotationCreator ac) throws ResourceInstantiationException,
          JapeException, ExecutionException {
    Corpus c = createCorpus(doc);
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
    try {
      Document doc = Factory.newDocument(Files.getGateResourceAsString(fileName));
      return createCorpus(doc);
    }
    catch(Exception e) {
      e.printStackTrace(Err.getPrintWriter());
      assertTrue("Error creating corpus", false);
    }
    return null;
  }

  protected Corpus createCorpus(Document doc)
          throws ResourceInstantiationException {
    Corpus c = Factory.newCorpus("TestJape corpus");
    c.add(doc);
    return c;
  }


  /**
   * Creates a document with the given text
   * @param text
   * @return
   */
  protected Document createDoc(String text) {
    FeatureMap params = Factory.newFeatureMap();
    params.put("stringContent", text);
    params.put("markupAware", "true");
    params.put("mimeType", "text/plain");
    Document d = null;
    try {
        d = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);
    }
    catch (ResourceInstantiationException e) {
        throw new RuntimeException(e);
    }
    return d;
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
    ParseCpsl parser = Factory.newJapeParser(sr, new HashMap<Object, Object>());
    Transducer transducer = parser.MultiPhaseTransducer();
    transducer.finish();
  }

  /**
   * Callback interface used in the doTest method.
   *
   * @version $Revision$
   * @author esword
   */
  public static interface AnnotationCreator {
    public void setDoc(Document doc);
    public AnnotationSet createAnnots(Document doc);
    public AnnotationCreator addInc(String type);
    public AnnotationCreator add(int start, int end, String type);
    public AnnotationCreator add(int start, int end, String type, FeatureMap fm);
    public AnnotationCreator add(String type);
  }

  public static abstract class BaseAnnotationCreator implements AnnotationCreator {
    protected AnnotationSet as;
    protected int curOffset = 0;
    protected int annotLength = 2;
    protected static FeatureMap emptyFeat = Factory.newFeatureMap();

    public void setDoc(Document doc) {
      as = doc.getAnnotations();
    }

    /**
     * Add an annotation of the given type over the given range.  Does not increment curOffset.
     */
    public AnnotationCreator add(int start, int end, String type) {
      return add(start, end, type, emptyFeat);
    }

    /**
     * Add an annotation of the given type over the given range.  Does not increment curOffset.
     */
    public AnnotationCreator add(int start, int end, String type, FeatureMap fm) {
      try {
        as.add(new Long(start), new Long(end), type, fm);
      }
      catch(InvalidOffsetException ioe) {
        ioe.printStackTrace(Err.getPrintWriter());
      }
      return this;
    }

    /**
     * Add an annotation of the given type at the current offset and increment the placement
     * counter.
     */
    public AnnotationCreator addInc(String type) {
      add(type);
      curOffset += annotLength;
      return this;
    }

    /**
     * Add annot at the current offset
     */
    public AnnotationCreator add(String type) {
      try {
        as.add(new Long(curOffset), new Long(curOffset + annotLength), type, emptyFeat);
      }
      catch(InvalidOffsetException ioe) {
        ioe.printStackTrace(Err.getPrintWriter());
      }
      return this;
    }
  }
}
