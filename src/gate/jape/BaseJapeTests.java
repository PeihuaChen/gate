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
import gate.jape.parser.ParseCpsl;
import gate.util.Err;
import gate.util.Files;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import gate.util.OffsetComparator;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

/**
 * Tests for Constraint predicate logic
 */
public abstract class BaseJapeTests extends TestCase {

  private static boolean gateAwake = false;
  protected static final String DEFAULT_DATA_FILE = "jape/InputTexts/AveShort";
  protected String transducerClass;
  public BaseJapeTests(String name) {
    super(name);
    this.transducerClass = gate.creole.Transducer.class.getName();
    //this.transducerClass = "gate.jape.plus.Transducer";

    if (gateAwake)
      return;
    try {
      BasicConfigurator.configure();
      assertTrue(new File("./plugins").isDirectory());
      System.setProperty("gate.home", ".");
      Gate.init();      
      registerCREOLE("./plugins/Ontology");
      
      // JAPE implementation - uncomment only one
      // Regular one
      registerCREOLE("./plugins/ANNIE");
      
      // JAPE Plus
      // registerCREOLE("../gate-futures/jplus");

      // JAPE PDA Plus      
      // registerCREOLE("../gate-futures/jpdaplus");
      gateAwake = true;
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch(Exception e) {
      throw new RuntimeException(e);
    }    
  }

  private void registerCREOLE(String pathname) throws GateException,
          MalformedURLException, IOException {
    Gate.getCreoleRegister().registerDirectories(new File(pathname).getCanonicalFile().toURI().toURL());
  }

  protected Set<Annotation> doTest(String dataFile, String japeFile, AnnotationCreator ac, String ontologyURL)
  throws Exception {
    Document doc = Factory.newDocument(Files.getGateResourceAsString(dataFile));
    return doTest(doc, japeFile, ac, ontologyURL);
  }  

  protected Set<Annotation> doTest(String dataFile, String japeFile, AnnotationCreator ac)
  throws Exception {
    return doTest(dataFile, japeFile, ac, null);
  }

  protected Set<Annotation> doTest(Document doc, String japeFile,
          AnnotationCreator ac, String ontologyURL) throws Exception {
    ac.createAnnots(doc);
    Set<Annotation> orderedResults = runTransducer(doc, japeFile, ontologyURL);
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

  private Set<Annotation> runTransducer(Document doc, String japeFile, String ontologyURL)
  throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    if (transducerClass.contains("plus")) {
      params.put("sourceType", "JAPE");
      params.put("sourceURL", Files.getGateResource(japeFile));
    }
    else {
      params.put("grammarURL", Files.getGateResource(japeFile));
    }
    params.put("encoding", "UTF-8");
    params.put("outputASName", "Output");
    if (ontologyURL != null) {
      Object ontology = createOntology(ontologyURL);
      params.put("ontology", ontology);   
    }
    AbstractLanguageAnalyser transducer = (AbstractLanguageAnalyser)Factory.createResource(transducerClass, params);
    transducer.setDocument(doc);
    transducer.execute();
    // check the results
    Set<Annotation> orderedResults = new TreeSet<Annotation>(
            new OffsetComparator());
    orderedResults.addAll(doc.getAnnotations("Output").get("Result"));
    return orderedResults;
  }

  private Object createOntology(String ontologyURL) throws Exception { 
    FeatureMap params = Factory.newFeatureMap();
    params.put("rdfXmlURL", new URL(ontologyURL));
    params.put("loadImports", true);
    return Factory.createResource("gate.creole.ontology.impl.sesame.OWLIMOntology", params);
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
