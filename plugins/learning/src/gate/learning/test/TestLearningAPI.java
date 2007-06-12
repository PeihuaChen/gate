/*
 *  TestLearningAPI.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: TestLearningAPI.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learning.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import gate.Corpus;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.learning.ConstantParameters;
import gate.learning.EvaluationBasedOnDocs;
import gate.learning.LearningAPIMain;
import gate.learning.LogService;
import gate.learning.RunMode;
import gate.util.ExtensionFileFilter;
import gate.util.GateException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
/**
 * Test the three types of NLP learning implemented in
 *  ML Api by using the test methods and small datasets.
 */
public class TestLearningAPI extends TestCase {
  /** Use it to do initialisation only once. */
  private static boolean initialized = false;
  /** Learning home for reading the data and configuration file. */
  private static File learningHome;
  /** Constructor, setting the home directory. */
  public TestLearningAPI(String arg0) throws GateException,
    MalformedURLException {
    super(arg0);
    if(!initialized) {
      Gate.init();
      learningHome = new File(new File(Gate.getGateHome(), "plugins"),
        "learning");
      Gate.getCreoleRegister().addDirectory(learningHome.toURL());
      initialized = true;
    }
  }
  /** The ML Api object to be tested. */
  LearningAPIMain learningApi;
  /** Corpus used for testing. */
  Corpus corpus;
  /** The controller include the ML Api as one PR. */
  gate.creole.SerialAnalyserController controller;
  /** Set up method (does nothing because it may have
   * different behaviour in different enviroment. 
   */
  protected void setUp() throws Exception {
    super.setUp();
  }
  /**  Release some resources.*/
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  /** Loading the configurationg file and corpus for testing. 
   * And make settings as in the GATE Gui. 
   */
  void loadSettings(String configFileName, String corpusDirName, String inputasN)
    throws GateException, IOException {
    LogService.minVerbosityLevel = 0;
    if(LogService.minVerbosityLevel>0)
      System.out.println("Learning Home : " + learningHome.getAbsolutePath());
    FeatureMap parameters = Factory.newFeatureMap();
    URL configFileURL = new File(configFileName).toURL();
    parameters.put("configFileURL", configFileURL);
    learningApi = (LearningAPIMain)Factory.createResource(
      "gate.learning.LearningAPIMain", parameters);
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
  /** Clear up the resources used after one test. */
  private void clearOneTest() {
    corpus.clear();
    Factory.deleteResource(corpus);
    Factory.deleteResource(learningApi);
    controller.remove(learningApi);
    controller.cleanup();
    Factory.deleteResource(controller);
  }
  /** Test the chunk learning by using the SVM with linear kernel and
   * a small part of the OntoNews corpus. 
   */
  public void testSVMChunkLearnng() throws IOException, GateException {
    // Initialisation
    System.out.print("Testing the SVM with liner kernenl on chunk learning...");
    File chunklearningHome = new File(new File(learningHome, "test"),
      "chunklearning");
    String configFileURL = new File(chunklearningHome, "engines-svm.xml")
      .getAbsolutePath();
    String corpusDirName = new File(chunklearningHome, "data-ontonews")
      .getAbsolutePath();
    //Remove the label list file, feature list file and chunk length files. 
    String wdResults = new File(chunklearningHome, 
      ConstantParameters.SUBDIRFORRESULTS).getAbsolutePath();
    emptySavedFiles(wdResults);
    String inputASN = "Key";
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    RunMode runM=RunMode.EVALUATION;
    learningApi.setLearningMode(runM);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 50);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 13);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 32);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 31);
    
    System.out.println("completed");
    // Remove the resources
    clearOneTest();
  }
  /** Test the chunk learning by using the Naive Bayes method and
   * a small part of the OntoNews corpus. */
  public void testNBChunkLearnng() throws IOException, GateException {
    // Initialisation
    System.out.print("Testing the Naive Bayes method on chunk learning...");
    File chunklearningHome = new File(new File(learningHome, "test"),
      "chunklearning");
    String configFileURL = new File(chunklearningHome,
      "engines-naivebayesweka.xml").getAbsolutePath();
    String corpusDirName = new File(chunklearningHome, "data-ontonews")
      .getAbsolutePath();
    //Remove the label list file, feature list file and chunk length files. 
    String wdResults = new File(chunklearningHome, 
      ConstantParameters.SUBDIRFORRESULTS).getAbsolutePath();
    emptySavedFiles(wdResults);
    String inputASN = "Key";
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    RunMode runM=RunMode.EVALUATION;
    learningApi.setLearningMode(runM);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    /*assertEquals(evaluation.macroMeasuresOfResults.correct, 3);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 1);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 19);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 68);*/
    assertEquals(evaluation.macroMeasuresOfResults.correct, 27);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 3);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 31);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 42);
    // Remove the resources
    clearOneTest();
    System.out.println("completed");
  }
  /** Test the text classification by using the SVM with linear kernel
   * and the data for sentence classification. 
   */
  public void testSVMClassification() throws GateException, IOException {
    // Initialisation
    System.out.print("Testing the SVM with linear kernel on text classification...");
    File scHome = new File(new File(learningHome, "test"),
      "sentence-classification");
    String configFileURL = new File(scHome, "engines-svm.xml")
      .getAbsolutePath();
    String corpusDirName = new File(scHome, "data-h").getAbsolutePath();
    //Remove the label list file, feature list file and chunk length files. 
    String wdResults = new File(scHome, 
      ConstantParameters.SUBDIRFORRESULTS).getAbsolutePath();
    emptySavedFiles(wdResults);
    String inputASN = null;
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    RunMode runM=RunMode.EVALUATION;
    learningApi.setLearningMode(runM);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 26);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 44);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 39);
    // Remove the resources
    clearOneTest();
    System.out.println("completed");
  }
  /** Test the text classification by using the SVM with quadratic kernel
   * and the data for sentence classification. 
   */
  public void testSVMKernelClassification() throws GateException, IOException {
    System.out.print("Testing the SVM with quadratic kernel on text classification...");
    // Initialisation
    File scHome = new File(new File(learningHome, "test"),
      "sentence-classification");
    String configFileURL = new File(scHome, "engines-svm-quadratickernel.xml")
      .getAbsolutePath();
    String corpusDirName = new File(scHome, "data-h").getAbsolutePath();
    //Remove the label list file, feature list file and chunk length files. 
    String wdResults = new File(scHome, 
      ConstantParameters.SUBDIRFORRESULTS).getAbsolutePath();
    emptySavedFiles(wdResults);
    String inputASN = null;
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    RunMode runM=RunMode.EVALUATION;
    learningApi.setLearningMode(runM);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 27);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 46);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 38);
    // Remove the resources
    clearOneTest();
    System.out.println("completed");
  }
  /** Test the text classification by using the KNN
   * and the data for sentence classification. 
   */
  public void testKNNClassification() throws GateException, IOException {
    System.out.print("Testing the KNN on text classification...");
    // Initialisation
    File scHome = new File(new File(learningHome, "test"),
      "sentence-classification");
    String configFileURL = new File(scHome, "engines-knnweka.xml")
      .getAbsolutePath();
    String corpusDirName = new File(scHome, "data-h").getAbsolutePath();
    //Remove the label list file, feature list file and chunk length files. 
    String wdResults = new File(scHome, 
      ConstantParameters.SUBDIRFORRESULTS).getAbsolutePath();
    emptySavedFiles(wdResults);
    String inputASN = null;
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    RunMode runM=RunMode.EVALUATION;
    learningApi.setLearningMode(runM);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 12);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 62);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 53);
    // Remove the resources
    clearOneTest();
    System.out.println("completed");
  }
  /** Test the text classification by using the C4.5 algorithm
   * and the data for sentence classification. 
   */
  public void testC45Classification() throws GateException, IOException {
    System.out.print("Testing the C4.5 on text classification...");
    // Initialisation
    File scHome = new File(new File(learningHome, "test"),
      "sentence-classification");
    String configFileURL = new File(scHome, "engines-c45weka.xml")
      .getAbsolutePath();
    String corpusDirName = new File(scHome, "data-h").getAbsolutePath();
    //Remove the label list file, feature list file and chunk length files. 
    String wdResults = new File(scHome, 
      ConstantParameters.SUBDIRFORRESULTS).getAbsolutePath();
    emptySavedFiles(wdResults);
    String inputASN = null;
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    RunMode runM=RunMode.EVALUATION;
    learningApi.setLearningMode(runM);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    assertEquals(evaluation.macroMeasuresOfResults.correct, 25);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 63);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 40);
    // Remove the resources
    clearOneTest();
    System.out.println("completed");
  }
  /** Test the relation extraction by using the SVM with linear kernel
   * and a small part of data from ACE-04 relation extraction. 
   */
  public void testSVMRelationLearning() throws GateException, IOException {
    System.out.print("Testing the SVM with linear kernel on relation extraction...");
    // Initialisation relation-learning
    File scHome = new File(new File(learningHome, "test"), "relation-learning");
    String configFileURL = new File(scHome, "engines-svm.xml")
      .getAbsolutePath();
    String corpusDirName = new File(scHome, "data-acerelation")
      .getAbsolutePath();
    //Remove the label list file, feature list file and chunk length files. 
    String wdResults = new File(scHome, 
      ConstantParameters.SUBDIRFORRESULTS).getAbsolutePath();
    emptySavedFiles(wdResults);
    String inputASN = "Key";
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    RunMode runM=RunMode.EVALUATION;
    learningApi.setLearningMode(runM);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    /*assertEquals(evaluation.macroMeasuresOfResults.correct, 4);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 27);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 110);*/
    assertEquals(evaluation.macroMeasuresOfResults.correct, 6);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 31);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 108);
    // Remove the resources
    clearOneTest();
    System.out.println("completed");
  }
  /** Test the relation extraction by using the SVM with linear kernel
   * and a small part of data from ACE-04 relation extraction. 
   */
  public void testSVMRelationLearningWithNgramFeatures() throws GateException, IOException {
    System.out.print("Testing the SVM with Ngram features on relation extraction ...");
    // Initialisation relation-learning
    File scHome = new File(new File(learningHome, "test"), "relation-learning");
    String configFileURL = new File(scHome, "engines-svm-ngram.xml")
      .getAbsolutePath();
    String corpusDirName = new File(scHome, "data-acerelation")
      .getAbsolutePath();
    //Remove the label list file, feature list file and chunk length files. 
    String wdResults = new File(scHome, 
      ConstantParameters.SUBDIRFORRESULTS).getAbsolutePath();
    emptySavedFiles(wdResults);
    String inputASN = "Key";
    loadSettings(configFileURL, corpusDirName, inputASN);
    // Set the evaluation mode
    RunMode runM=RunMode.EVALUATION;
    learningApi.setLearningMode(runM);
    controller.execute();
    // Using the evaluation mode for testing
    EvaluationBasedOnDocs evaluation = learningApi.getEvaluation();
    // Compare the overall results with the correct numbers
    /*assertEquals(evaluation.macroMeasuresOfResults.correct, 4);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 27);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 110);*/
    assertEquals(evaluation.macroMeasuresOfResults.correct, 7);
    assertEquals(evaluation.macroMeasuresOfResults.partialCor, 0);
    assertEquals(evaluation.macroMeasuresOfResults.spurious, 20);
    assertEquals(evaluation.macroMeasuresOfResults.missing, 107);
    // Remove the resources
    clearOneTest();
    System.out.println("completed");
  }
  /** Empty the label list, NLP feature list and the chunk lenght list file
   * before each test in order to obtain the consistent results of each test.
   */
  private void emptySavedFiles(String savedFilesDir) {
    (new File(savedFilesDir, ConstantParameters.FILENAMEOFNLPFeatureList)).delete();
    (new File(savedFilesDir, ConstantParameters.FILENAMEOFLabelList)).delete();
    (new File(savedFilesDir, ConstantParameters.FILENAMEOFChunkLenStats)).delete();
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestLearningAPI.class);
  } // suite
}
