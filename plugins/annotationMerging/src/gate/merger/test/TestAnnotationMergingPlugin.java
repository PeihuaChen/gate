/*
 *  TestLearningAPI.java
 * 
 *  Yaoyong Li 08/10/2007
 *
 *  $Id: TestAnnotationMergingPlugin.java, v 1.0 2007-10-08 11:44:16 +0000 yaoyong $
 */
package gate.merger.test;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.GateConstants;
import gate.merger.AnnotationMergingMain;
import gate.merger.MergingMethodsEnum;
import gate.util.ExtensionFileFilter;
import gate.util.GateException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Comparator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestAnnotationMergingPlugin extends TestCase {
  private static File learningHome;
  /** Use it to do initialisation only once. */
  private static boolean initialized = false;

  /**
   * Construction
   * 
   * @throws GateException
   * @throws MalformedURLException
   */
  public TestAnnotationMergingPlugin(String name) throws GateException,
    MalformedURLException {
    super(name);
    if(!initialized) {
      Gate.init();
      learningHome = new File(new File(Gate.getGateHome(), "plugins"),
        "annotationMerging");
      Gate.getCreoleRegister().addDirectory(learningHome.toURL());
      initialized = true;
    }
  }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /**
   * Put things back as they should be after running tests.
   */
  public void tearDown() throws Exception {
  } // tearDown

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestAnnotationMergingPlugin.class);
  } // suite

  /** The test for AnnotationMerging. */
  public void testAnnotationMergingPlugin() throws Exception {

    Boolean savedSpaceSetting = Gate.getUserConfig().getBoolean(
      GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME);
    Gate.getUserConfig().put(
      GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME, Boolean.FALSE);
    //Create a object for merging
    AnnotationMergingMain mergerOne = (AnnotationMergingMain)Factory
      .createResource("gate.merger.AnnotationMergingMain");
    //A corpus
    Corpus corpus = Factory.newCorpus("DataSet");
    ExtensionFileFilter fileFilter = new ExtensionFileFilter();
    fileFilter.addExtension("xml");
    String corpusDirName = new File(learningHome, "testdata").getAbsolutePath();
    File[] xmlFiles = new File(corpusDirName).listFiles(fileFilter);
    Arrays.sort(xmlFiles, new Comparator<File>() {
      public int compare(File a, File b) {
        return a.getName().compareTo(b.getName());
      }
    });
    for(File f : xmlFiles) {
      if(!f.isDirectory()) {
        Document doc = Factory.newDocument(f.toURI().toURL(), "UTF-8");
        doc.setName(f.getName());
        corpus.add(doc);
      }
    }

    gate.creole.SerialAnalyserController controller;
    controller = (gate.creole.SerialAnalyserController)Factory
      .createResource("gate.creole.SerialAnalyserController");
    controller.setCorpus(corpus);
    controller.add(mergerOne);
    mergerOne.setAnnSetOutput("mergerAnns");
    mergerOne.setAnnSetsForMerging("a-0;a-1;a-2");
    mergerOne.setAnnTypesAndFeats("SENT->senRel;OPINION_SRC->type");
    MergingMethodsEnum methodMerger = MergingMethodsEnum.MajorityVoting;
    mergerOne.setMergingMethod(methodMerger);
    controller.execute();
    Document doc = (Document)corpus.get(0);
    AnnotationSet anns = doc.getAnnotations("mergerAnns").get("SENT");
    int num = obtainAnns(anns, "senRel", "YES");
    assertEquals(num, 6);
    anns = doc.getAnnotations("mergerAnns").get("OPINION_SRC");
    num = anns.size();
    assertEquals(num, 6);
    doc.removeAnnotationSet("mergerAnns");

    methodMerger = MergingMethodsEnum.MergingByAnnotatorNum;
    mergerOne.setMergingMethod(methodMerger);
    mergerOne.setMinimalAnnNum("1");
    controller.execute();
    doc = (Document)corpus.get(0);
    anns = doc.getAnnotations("mergerAnns").get("SENT");
    num = obtainAnns(anns, "senRel", "YES");
    assertEquals(num, 6);
    anns = doc.getAnnotations("mergerAnns").get("OPINION_SRC");
    num = anns.size();
    assertEquals(num, 10);
    System.out.println("completed");
    corpus.clear();
    Factory.deleteResource(corpus);
    Factory.deleteResource(mergerOne);
    controller.remove(mergerOne);
    controller.cleanup();
    Factory.deleteResource(controller);

    // finally {
    Gate.getUserConfig().put(
      GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME,
      savedSpaceSetting);
    // }
  }

  private int obtainAnns(AnnotationSet anns, String f, String v) {
    int num = 0;
    for(Annotation ann : anns) {
      if(ann.getFeatures().containsKey(f) && ann.getFeatures().get(f).equals(v))
        ++num;
    }
    return num;
  }

}
