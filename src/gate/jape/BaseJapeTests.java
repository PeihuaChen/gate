/*
 *  TestConstraints
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
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
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.jape.parser.ParseCpsl;
import gate.util.*;

import java.io.File;
import java.io.StringReader;
import java.util.*;

import org.apache.log4j.BasicConfigurator;

import junit.framework.TestCase;

/**
 * Tests for Constraint predicate logic
 */
public class BaseJapeTests extends TestCase {

  private static boolean gateAwake = false;
  protected static final String DEFAULT_DATA_FILE = "jape/InputTexts/AveShort";
  protected String transducerClass;
  public BaseJapeTests(String name) {
    super(name);
    this.transducerClass = gate.creole.Transducer.class.getName();
    
    if (gateAwake)
      return;
    try {
      BasicConfigurator.configure();
      assertTrue(new File("./plugins").isDirectory());
      System.setProperty("gate.home", ".");
      Gate.init();
      Gate.getCreoleRegister().registerDirectories(new File("./plugins/ANNIE").toURI().toURL());
      gateAwake = true;
    }
    catch(Exception e) {
      throw new RuntimeException(e);
    }    
  }

  protected Set<Annotation> doTest(String dataFile, String japeFile, AnnotationCreator ac)
          throws Exception {
    Document doc = Factory.newDocument(Files.getGateResourceAsString(dataFile));
    return doTest(doc, japeFile, ac);
  }

  protected Set<Annotation> doTest(Document doc, String japeFile,
          AnnotationCreator ac) throws ResourceInstantiationException,
          JapeException, ExecutionException {
    ac.createAnnots(doc);
    Set<Annotation> orderedResults = runTransducer(doc, japeFile);
    return orderedResults;
  }

  protected void compareResults(String[] expectedResults,
          Set<Annotation> actualResults) {
    int i = 0;

    assertEquals(expectedResults.length, actualResults.size());

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
      fail("Error creating corpus");
    }
    return null;
  }

  protected Corpus createCorpus(Document doc)
          throws ResourceInstantiationException {
    Corpus c = Factory.newCorpus("TestJape corpus");
    c.add(doc);
    return c;
  }

  protected Set<Annotation> runTransducer(Document doc, String japeFile)
          throws JapeException, ExecutionException, ResourceInstantiationException {
    FeatureMap params = Factory.newFeatureMap();
    params.put("grammarURL", Files.getGateResource(japeFile));
    params.put("encoding", "UTF-8");
    params.put("outputASName", "Output");
    AbstractLanguageAnalyser transducer = (AbstractLanguageAnalyser)Factory.createResource(transducerClass, params);
    transducer.setDocument(doc);
    transducer.execute();
    // check the results
    Set<Annotation> orderedResults = new TreeSet<Annotation>(
            new OffsetComparator());
    orderedResults.addAll(doc.getAnnotations("Output").get("Result"));
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
