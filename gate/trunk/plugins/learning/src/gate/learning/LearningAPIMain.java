/*
 *  LearningAPIMain.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: LearningAPIMain.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learning;

import gate.Document;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * The main object of the ML Api. It does initialiation, read parameter values
 * from GUI, and run the selected learning mode. It can also be called by java
 * code, as an API (an GATE class), for using this learning api.
 */
public class LearningAPIMain extends AbstractLanguageAnalyser implements
                                                             ProcessingResource {
  /** This is where the model(s) should be saved */
  private URL configFileURL;
  /**
   * Name of the AnnotationSet contains annotations specified in the DATASET
   * element of configuration file.
   */
  private String inputASName;
  /**
   * Run-time parameter learningMode, having three modes: training, application,
   * and evaluation.
   */
  private RunMode learningMode;
  /** Learning settings specified in the configuration file. */
  private LearningEngineSettings learningSettings;
  /**
   * The lightweight learning object for getting the features, training and
   * application.
   */
  LightWeightLearningApi lightWeightApi = null;
  /** The File for NLP learning Log. */
  private File logFile;
  /** Used by lightWeightApi, specifying training or application. */
  private boolean isTraining;
  /** Subdirectory for storing the data file produced by learning api. */
  private File wdResults = null;
  /** Doing evaluation. */
  private EvaluationBasedOnDocs evaluation;

  /** Trivial constructor. */
  public LearningAPIMain() {
    // do nothing
  }

  /** Initialise this resource, and return it. */
  public gate.Resource init() throws ResourceInstantiationException {
    fireStatusChanged("Checking and reading learning settings!");
    // here all parameters are needs to be checked
    // check for the model storage directory
    if(configFileURL == null)
      throw new ResourceInstantiationException(
        "WorkingDirectory is required to store the learned model and cannot be null");
    // it is not null, check it is a file: URL
    if(!"file".equals(configFileURL.getProtocol())) { throw new ResourceInstantiationException(
      "WorkingDirectory must be a file: URL"); }
    // Get the working directory which the configuration
    // file reside in.
    File wd = new File(configFileURL.getFile()).getParentFile();
    // it must be a directory
    if(!wd.isDirectory()) { throw new ResourceInstantiationException(wd
      + " must be a reference to directory"); }
    try {
      // Load the learning setting file
      // by reading the configuration file
      learningSettings = LearningEngineSettings
        .loadLearningSettingsFromFile(configFileURL);
    } catch(Exception e) {
      throw new ResourceInstantiationException(e);
    }
    try {
      // Creat the sub-directory of the workingdirectroy where the data
      // files will be stored in
      wdResults = new File(wd,
        gate.learning.ConstantParameters.SUBDIRFORRESULTS);
      wdResults.mkdir();
      logFile = new File(new File(wd, ConstantParameters.SUBDIRFORRESULTS),
        ConstantParameters.FILENAMEOFLOGFILE);
      PrintWriter logFileIn = new PrintWriter(new FileWriter(logFile, true));
      logFileIn.println("\n\n*************************\n");
      logFileIn.println("A new session for NLP learning is starting.\n");
      logFileIn.println("The initiliased time of NLP learning: "
        + new Date().toString());
      logFileIn.println("Working directory: " + wd.getAbsolutePath());
      logFileIn.println("The feature files and models are saved at: "
        + wdResults.getAbsolutePath());
      // Call the lightWeightLearningApi
      lightWeightApi = new LightWeightLearningApi(wd);
      // more initialisation
      lightWeightApi.furtherInit(wdResults, learningSettings);
      logFileIn.println("Learner name: "
        + learningSettings.learnerSettings.getLearnerName());
      logFileIn.println("Learner nick name: "
        + learningSettings.learnerSettings.getLearnerNickName());
      logFileIn.println("Learner parameter settings: "
        + learningSettings.learnerSettings.learnerName);
      logFileIn.println("Surroud mode (or chunk learning): "
        + learningSettings.surround);
      logFileIn.println();
      logFileIn.close();
    } catch(Exception e) {
      throw new ResourceInstantiationException(e);
    }
    fireProcessFinished();
    // System.out.println("initialisation finished.");
    return this;
  } // init()

  /**
   * Run the resource.
   * 
   * @throws ExecutionException
   * @throws
   */
  public void execute() throws ExecutionException {
    // now we need to see if the corpus is provided
    if(corpus == null)
      throw new ExecutionException("Provided corpus is null!");
    if(corpus.size() == 0)
      throw new ExecutionException("No Document found in corpus!");
    // first, get the NLP features from the documents, according to the
    // feature types specified in DataSetDefinition file
    int positionDoc = corpus.indexOf(document);
    // docsName.add(positionDoc, document.getName());
    if(positionDoc == 0) {
      if(LogService.debug >= 0)
        System.out.println("Pre-processing the "+corpus.size()+" documents...");
      try {
        PrintWriter logFileIn = new PrintWriter(new FileWriter(logFile, true));
        logFileIn.println("\n*** A new run starts.");
        logFileIn
          .println("\nThe execution time (pre-processing the first document): "
            + new Date().toString());
        // logFileIn.println("EvaluationMode: " + evaluationMode);
        // logFileIn.println("TrainingMode: " + trainingMode);
        // logFileIn.println("InputAS: " + inputASName);
        logFileIn.close();
      } catch(IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    if(positionDoc == corpus.size() - 1) {
      // first select the training data and test data according to the
      // learning setting
      // set the inputASName in here, because it is a runtime parameter
      if(LogService.debug >= 0) {
        System.out.println("Learning starts.");
        System.out.println("For the information about this leanring see the log file "+
        wdResults.getAbsolutePath()+File.separator+
        ConstantParameters.FILENAMEOFLOGFILE);
      }
      lightWeightApi.inputASName = inputASName;
      int numDoc = corpus.size();
      try {
        PrintWriter logFileIn = new PrintWriter(new FileWriter(logFile, true));
        logFileIn.println("The learning start at " + new Date().toString());
        logFileIn.println("The number of documents in dataset: " + numDoc);
        // if only need the feature data
        if(learningSettings.isOnlyFeatureData) {// if only want feature
          // data
          isTraining = true;
          for(int i = 0; i < numDoc; ++i)
            lightWeightApi.annotations2FVs((Document)corpus.get(i), i,
              wdResults, isTraining, learningSettings);
          lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
            learningSettings);
          if(LogService.debug >= 0) displayDataFilesInformation();
        } else { // run the whole procedure of learning
          switch(learningMode){
            case TRAINING:
              // empty the data file
              EvaluationBasedOnDocs.emptyDatafile(wdResults, lightWeightApi);
              if(LogService.debug >= 0) System.out.println("Training mode");
              logFileIn.println("Training mode.");
              isTraining = true;
              if(LogService.debug>0)
                System.out.println("Training lightweight, wdResults="
                + wdResults.toString() + ".");
              for(int i = 0; i < numDoc; ++i)
                lightWeightApi.annotations2FVs((Document)corpus.get(i), i,
                  wdResults, isTraining, learningSettings);
              lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
                learningSettings);
              // if fitering the training data
              if(learningSettings.fiteringTrainingData
                && learningSettings.filteringRatio > 0.0)
                lightWeightApi.FilteringNegativeInstsInJava(corpus.size(),
                  logFileIn, learningSettings);
              // using the java code for training
              lightWeightApi.trainingJava(corpus.size(), logFileIn,
                learningSettings);
              break;
            case APPLICATION:
              // if application
              if(LogService.debug >= 0) System.out.println("Application mode");
              logFileIn.println("Application mode.");
              isTraining = false;
              String classTypeOriginal = learningSettings.datasetDefinition
                .getClassAttribute().getType();
              for(int i = 0; i < numDoc; ++i)
                lightWeightApi.annotations2FVs((Document)corpus.get(i), i,
                  wdResults, isTraining, learningSettings);
              lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
                learningSettings);
              // Applying th model
              lightWeightApi.applyModelInJava(corpus, classTypeOriginal,
                logFileIn, learningSettings);
              break;
            case EVALUATION:
              if(LogService.debug>=0) System.out.println("Evaluation mode");
              logFileIn.println("Evaluation mode.");
              evaluation = new EvaluationBasedOnDocs(corpus, wdResults,
                inputASName);
              evaluation
                .evaluation(learningSettings, lightWeightApi, logFileIn);
              break;
            default:
              throw new GateException("The learning mode is not defined!");
          }
        }
      } catch(IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch(GateException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if(LogService.debug >= 0)
        System.out.println("This learning session finished!.");
    } // end of learning (position=corpus.size()-1)
    
    
  }

  /** Print out the information for featureData only option. */
  private void displayDataFilesInformation() {
    System.out.println("The NLP features for all the documents are in the file"
      + wdResults.getAbsolutePath() + File.pathSeparator
      + ConstantParameters.FILENAMEOFNLPFeaturesData);
    System.out.println("The feature vectors in sparse format are in the file"
      + wdResults.getAbsolutePath() + File.pathSeparator
      + ConstantParameters.FILENAMEOFFeatureVectorData);
    System.out.println("The Label list is in the file"
      + wdResults.getAbsolutePath() + File.pathSeparator
      + ConstantParameters.FILENAMEOFLabelList);
    System.out.println("The NLP features list is in the file"
      + wdResults.getAbsolutePath() + File.pathSeparator
      + ConstantParameters.FILENAMEOFNLPFeatureList);
    System.out
      .println("The statistics of entity length for each class is in the file"
        + wdResults.getAbsolutePath() + File.pathSeparator
        + ConstantParameters.FILENAMEOFChunkLenStats);
  }

  public void setConfigFileURL(URL workingDirectory) {
    this.configFileURL = workingDirectory;
  }

  public URL getConfigFileURL() {
    return this.configFileURL;
  }

  public void setInputASName(String iasn) {
    this.inputASName = iasn;
  }

  public String getInputASName() {
    return this.inputASName;
  }

  public RunMode getLearningMode() {
    return this.learningMode;
  }

  public void setLearningMode(RunMode learningM) {
    this.learningMode = learningM;
  }

  public EvaluationBasedOnDocs getEvaluation() {
    return evaluation;
  }

  public EvaluationBasedOnDocs setEvaluation(EvaluationBasedOnDocs eval) {
    return this.evaluation = eval;
  }
}
