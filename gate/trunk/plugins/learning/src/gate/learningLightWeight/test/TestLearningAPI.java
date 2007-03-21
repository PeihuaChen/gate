package gate.learningLightWeight.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import gate.Corpus;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.learningLightWeight.EvaluationBasedOnDocs;
import gate.learningLightWeight.LearningAPIMain;
import gate.util.ExtensionFileFilter;
import gate.util.GateException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestLearningAPI extends TestCase {
  // public static void main(String[] args) {
  // TestLearningAPI test1 = new TestLearningAPI("aa");
  // test1.testSVMChunkLearnng();
  // }
  private static boolean initialized = false;
  private static File learningHome;

  public TestLearningAPI(String arg0) throws GateException,
    MalformedURLException {
    super(arg0);
    if(!initialized) {
      Gate.init();
      learningHome = new File(new File(Gate.getGateHome(),"plugins") ,"learning");
      Gate.getCreoleRegister().addDirectory(learningHome.toURL());
      initialized = true;
    }
  }

  LearningAPIMain learningApi;
  Corpus corpus;
  gate.creole.SerialAnalyserController controller;

  protected void setUp() throws Exception {
    super.setUp();
    // load your plugin and do all necessary one time loading here
    // copy code from init();
    // Configuration file
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

  void loadSettings(String configFileName, String corpusDirName, String inputasN)
    throws GateException, IOException {
	    System.out.println("Learning Home : "+learningHome.getAbsolutePath());
	    FeatureMap parameters = Factory.newFeatureMap();
    URL configFileURL = new File(configFileName).toURL();
    parameters.put("configFileURL", configFileURL);
    learningApi = (LearningAPIMain)Factory.createResource(
      "gate.learningLightWeight.LearningAPIMain", parameters);
    // Load the corpus
    corpus = Factory.newCorpus("DataSet");
    ExtensionFileFilter fileFilter = new ExtensionFileFilter();
    fileFilter.addExtension("xml");
    URL tempURL = new File(corpusDirName).toURL();
    corpus.populate(tempURL, fileFilter, "UTF-8", false);
    // Set the inputAS
    learningApi.setInputASName(inputasN);
    controller = (gate.creole.SerialAnalyserController)Factory
      .createResource("gate.creole.SerialAnalyserController");
    controller.setCorpus(corpus);
    controller.add(learningApi);
  }

  private void clearOneTest() {
    corpus.clear();
    Factory.deleteResource(corpus);
    Factory.deleteResource(learningApi);
    controller.remove(learningApi);
    controller.cleanup();
    Factory.deleteResource(controller);
  }

  
  public void testSVMChunkLearnng() throws IOException, GateException {
    // Initialisation
    File chunklearningHome = new File(new File(learningHome, "test") , "chunklearning");
    String configFileURL = new File(chunklearningHome, "engines-svm.xml").getAbsolutePath();
    String corpusDirName = new File(chunklearningHome, "data-ontonews").getAbsolutePath();

    String inputASN = "Key";
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    learningApi.setEvaluationMode(true);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 50);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 13);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 32);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 31);
    //Remove the resources
    clearOneTest();
  }
  
  public void testNBChunkLearnng() throws IOException, GateException {
    // Initialisation
    File chunklearningHome = new File(new File(learningHome, "test") , "chunklearning");
    String configFileURL = new File(chunklearningHome, "engines-naivebayesweka.xml").getAbsolutePath();
    String corpusDirName = new File(chunklearningHome, "data-ontonews").getAbsolutePath();

    String inputASN = "Key";
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    learningApi.setEvaluationMode(true);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 6);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 3);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 74);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 63);
    //Remove the resources
    clearOneTest();
  }

  public void testSVMClassification() throws GateException, IOException {
    // Initialisation
    File scHome = new File(new File(learningHome, "test") , "sentence-classification");
    String configFileURL = new File(scHome,"engines-svm.xml").getAbsolutePath();
    String corpusDirName = new File(scHome,"data-h").getAbsolutePath();
    String inputASN = null;
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    learningApi.setEvaluationMode(true);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 24);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 34);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 41);
    //Remove the resources
    clearOneTest();
  }
  
  public void testSVMKernelClassification() throws GateException, IOException {
    // Initialisation
    File scHome = new File(new File(learningHome, "test") , "sentence-classification");
    String configFileURL = new File(scHome,"engines-svm-quadratickernel.xml").getAbsolutePath();
    String corpusDirName = new File(scHome,"data-h").getAbsolutePath();

    String inputASN = null;
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    learningApi.setEvaluationMode(true);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 27);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 39);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 38);
    //Remove the resources
    clearOneTest();
  }
  
  public void testKNNClassification() throws GateException, IOException {
    // Initialisation
    File scHome = new File(new File(learningHome, "test") , "sentence-classification");
    String configFileURL = new File(scHome,"engines-knnweka.xml").getAbsolutePath();
    String corpusDirName = new File(scHome,"data-h").getAbsolutePath();

    String inputASN = null;
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    learningApi.setEvaluationMode(true);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 11);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 62);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 54);
    //Remove the resources
    clearOneTest();
  }
  
  public void testC45Classification() throws GateException, IOException {
    // Initialisation
    File scHome = new File(new File(learningHome, "test") , "sentence-classification");
    String configFileURL = new File(scHome,"engines-c45weka.xml").getAbsolutePath();
    String corpusDirName = new File(scHome,"data-h").getAbsolutePath();

    String inputASN = null;
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    learningApi.setEvaluationMode(true);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 25);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 63);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 40);
    //Remove the resources
    clearOneTest();
  }
  
  public void testSVMRelationLearning() throws GateException, IOException {
    // Initialisation relation-learning
    File scHome = new File(new File(learningHome, "test") , "relation-learning");
    String configFileURL = new File(scHome,"engines-svm.xml").getAbsolutePath();
    String corpusDirName = new File(scHome,"data-acerelation").getAbsolutePath();

    String inputASN = "Key";
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    learningApi.setEvaluationMode(true);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 4);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 27);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 110);
    //Remove the resources
    clearOneTest();
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestLearningAPI.class);
  } // suite
}
