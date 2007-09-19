/*
 *  LearningAPIMain.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: LearningAPIMain.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learning;

import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.ProcessingResource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
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
  private RunMode learningModeAppl;
  private RunMode learningModeMiTraining;
  private RunMode learningModeVIEWSVMMODEL;
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
  /** The MI learning information object.*/
  MiLearningInformation miLearningInfor = null;
  
  /** The three counters for batch application. */
  int startDocIdApp;
  int endDocIdApp;
  int maxNumApp;

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
    if(LogService.minVerbosityLevel > 0)
      System.out.println("Configuration File=" + configFileURL.toString());
    try {
      if(!new File(configFileURL.toURI()).exists()) {
        //System.out.println("Error: the configuration file specified does not exist!!");
        throw new ResourceInstantiationException(
          "Error: the configuration file specified does not exist!!");
      }
    } catch(URISyntaxException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    miLearningInfor = new MiLearningInformation();
    
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
      if(LogService.minVerbosityLevel>0) {
        System.out.println("\n\n*************************");
        System.out.println("A new session for NLP learning is starting.\n");
      }
      wdResults = new File(wd,
        gate.learning.ConstantParameters.SUBDIRFORRESULTS);
      wdResults.mkdir();
      logFile = new File(new File(wd, ConstantParameters.SUBDIRFORRESULTS),
        ConstantParameters.FILENAMEOFLOGFILE);
      //PrintWriter logFileIn = new PrintWriter(new FileWriter(logFile, true));
      LogService.init(logFile, true, learningSettings.verbosityLogService);
      StringBuffer logMessage = new StringBuffer();
      logMessage.append("\n\n*************************\n");
      logMessage.append("A new session for NLP learning is starting.\n");
      logMessage.append("The initiliased time of NLP learning: "
        + new Date().toString()+"\n");
      logMessage.append("Working directory: " + wd.getAbsolutePath()+"\n");
      logMessage.append("The feature files and models are saved at: "
        + wdResults.getAbsolutePath()+"\n");
      // Call the lightWeightLearningApi
      lightWeightApi = new LightWeightLearningApi(wd);
      // more initialisation
      lightWeightApi.furtherInit(wdResults, learningSettings);
      logMessage.append("Learner name: "
        + learningSettings.learnerSettings.getLearnerName()+"\n");
      logMessage.append("Learner nick name: "
        + learningSettings.learnerSettings.getLearnerNickName()+"\n");
      logMessage.append("Learner parameter settings: "
        + learningSettings.learnerSettings.learnerName+"\n");
      logMessage.append("Surroud mode (or chunk learning): "
        + learningSettings.surround);
      LogService.logMessage(logMessage.toString(), 1);
      LogService.close();
    } catch(Exception e) {
      throw new ResourceInstantiationException(e);
    }
    
    learningModeAppl = RunMode.APPLICATION;
    maxNumApp = learningSettings.docNumIntevalApp;
    learningModeMiTraining = RunMode.MITRAINING;
    learningModeVIEWSVMMODEL = RunMode.VIEWPRIMALFORMMODELS;
    fireProcessFinished();
    // System.out.println("initialisation finished.");
    return this;
  } // init()

  /**
   * Run the resource.
   * 
   * @throws ExecutionException
   */
  public void execute() throws ExecutionException {
    if( learningMode.equals(learningModeVIEWSVMMODEL)) {
      if(corpus == null || corpus.size() == 0 || corpus.indexOf(document) == 0)
        lightWeightApi.viewSVMmodelsInNLPFeatures(new File(wdResults,
          ConstantParameters.FILENAMEOFModels), learningSettings);
      return;
    }
    // now we need to see if the corpus is provided
    if(corpus == null)
      throw new ExecutionException("Provided corpus is null!");
    if(corpus.size() == 0)
      throw new ExecutionException("No Document found in corpus!");
    // first, get the NLP features from the documents, according to the
    // feature types specified in DataSetDefinition file
    int positionDoc = corpus.indexOf(document);
    //To see if the corpus is from a datastore or not
    
    
    // docsName.add(positionDoc, document.getName());
    if(positionDoc == 0) {
      lightWeightApi.inputASName = inputASName;
      /** Obtain the MI learning information of the last time learning. */
      if(learningMode.equals(this.learningModeMiTraining)) {
        miLearningInfor = new MiLearningInformation();
        File miLeFile = new File(wdResults, ConstantParameters.FILENAMEOFMILearningInfor);
        miLearningInfor.readDataFromFile(miLeFile);
      }
      /** Set the information for batch application. */
      startDocIdApp = 0;
      endDocIdApp = 0;
      if(LogService.minVerbosityLevel > 0)
        System.out.println("Pre-processing the " + corpus.size()
          + " documents...");
      try {
        //PrintWriter logFileIn = new PrintWriter(new FileWriter(logFile, true));
        LogService.init(logFile, true, learningSettings.verbosityLogService);
        LogService.logMessage("\n*** A new run starts.", 1);
        LogService.logMessage("\nThe execution time (pre-processing the first document): "
            + new Date().toString(), 1);
        if(LogService.minVerbosityLevel > 0) {
          System.out.println("Learning starts.");
          System.out
            .println("For the information about this learning see the log file "
              + wdResults.getAbsolutePath() + File.separator
              + ConstantParameters.FILENAMEOFLOGFILE);
        }
        LogService.close();
        // logFileIn.println("EvaluationMode: " + evaluationMode);
        // logFileIn.println("TrainingMode: " + trainingMode);
        // logFileIn.println("InputAS: " + inputASName);
      } catch(IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    
    /*  //for apply the model to each document
     if(learningMode.equals(learningModeAppl)) {
      LogService.logMessage("** Application mode:", 1);
      isTraining = false;
      lightWeightApi.annotations2NLPFeatures(this.document, 0,
          wdResults, isTraining, learningSettings);
      lightWeightApi.finishFVs(wdResults, 1, isTraining,
        learningSettings);
      lightWeightApi.nlpfeatures2FVs(wdResults, 1, isTraining, learningSettings);
      // Applying th model
      String classTypeOriginal = learningSettings.datasetDefinition
      .getClassAttribute().getType();
      try {
        lightWeightApi.applyModelInJavaPerDoc(this.document, classTypeOriginal, learningSettings);
      } catch(GateException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }*/
    // Apply the model to a bunch of documents
    if(learningMode.equals(learningModeAppl)) {
      ++endDocIdApp;
      if(endDocIdApp- startDocIdApp == maxNumApp) {
        try {
          BufferedWriter outNLPFeatures = null;
          BufferedReader inNLPFeatures = null;
          BufferedWriter outFeatureVectors = null;
          //EvaluationBasedOnDocs.emptyDatafile(wdResults, false);
          if(LogService.minVerbosityLevel> 0) System.out.println("** " +
              "Application mode for document from "+ startDocIdApp + " to "+ endDocIdApp+"(not included):");
          LogService.logMessage("** Application mode for document from "+ startDocIdApp + " to "+ endDocIdApp+"(not included):", 1);
          isTraining = false;
          String classTypeOriginal = learningSettings.datasetDefinition
            .getClassAttribute().getType();
          outNLPFeatures = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(wdResults,
            ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
          int numDoc;
          numDoc = endDocIdApp - startDocIdApp;
          //time bug
          //Date date1 = new Date();
          //long time1 = date1.getTime();
          for(int i = startDocIdApp; i < endDocIdApp; ++i) {
            Document toProcess = (Document)corpus.get(i);
            lightWeightApi.annotations2NLPFeatures(toProcess, i-startDocIdApp,
              outNLPFeatures, isTraining, learningSettings);
            if(toProcess.getDataStore()!= null && corpus.getDataStore() != null) {//(isDatastore)
              corpus.getDataStore().sync(corpus);
              Factory.deleteResource(toProcess);
            }
          }
          outNLPFeatures.flush();
          outNLPFeatures.close();
          lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
            learningSettings);
//        time bug
          //Date date2 = new Date();
          //long time2 = date2.getTime();
          //time1 = time2 - time1;
          //System.out.println("docName="+document.getName());
          //System.out.println("time for getting NLP features is "+time1);
          
          /** Open the normal NLP feature file. */
          inNLPFeatures = new BufferedReader(new InputStreamReader(new FileInputStream(new File(wdResults,
            ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
          outFeatureVectors = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
            new File(wdResults,ConstantParameters.FILENAMEOFFeatureVectorDataApp)), "UTF-8"));
          lightWeightApi.nlpfeatures2FVs(wdResults, inNLPFeatures, outFeatureVectors, numDoc, isTraining, learningSettings);
          inNLPFeatures.close();
          outFeatureVectors.flush();
          outFeatureVectors.close();
//        time bug
          //Date date3 = new Date();
          //long time3 = date3.getTime();
          //time2 = time3 - time2;
          //System.out.println("time for getting feature vector is "+time2);
          
          // Applying th model
          String fvFileName = wdResults.toString() + File.separator
          + ConstantParameters.FILENAMEOFFeatureVectorDataApp;
          lightWeightApi.applyModelInJava(corpus, startDocIdApp, endDocIdApp, classTypeOriginal,
            learningSettings, fvFileName);
          
//        time bug
          //Date date4 = new Date();
          //long time4 = date4.getTime();
          //time3 = time4 - time3;
          //System.out.println("time for applying models is "+time3);
          
          
          startDocIdApp = endDocIdApp;
          
        } catch(IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch(GateException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    if(positionDoc == corpus.size() - 1) {
      // first select the training data and test data according to the
      // learning setting
      // set the inputASName in here, because it is a runtime parameter
      int numDoc = corpus.size();
      try {
        //PrintWriter logFileIn = new PrintWriter(new FileWriter(logFile, true));
        LogService.init(logFile, true, learningSettings.verbosityLogService);
        LogService.logMessage("The learning start at " + new Date().toString(), 1);
        LogService.logMessage("The number of documents in dataset: " + numDoc, 1);
        //Open the NLP feature file for storing the NLP feature vectors
        BufferedWriter outNLPFeatures = null;
        BufferedReader inNLPFeatures = null;
        BufferedWriter outFeatureVectors = null;
        // if only need the feature data
        switch(learningMode) {
          case ProduceFeatureFilesOnly:
            // if only want feature data
            EvaluationBasedOnDocs.emptyDatafile(wdResults, true);
            if(LogService.minVerbosityLevel > 0) System.out.println("** Producing the feature files only!");
            LogService.logMessage("** Producing the feature files only!", 1);
            isTraining = true;
            outNLPFeatures = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(wdResults,
              ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
            for(int i = 0; i < numDoc; ++i) {
              Document toProcess = (Document)corpus.get(i);
              lightWeightApi.annotations2NLPFeatures(toProcess, i,
                outNLPFeatures, isTraining, learningSettings);
              if(toProcess.getDataStore() != null)
                Factory.deleteResource(toProcess);
            }
            outNLPFeatures.flush();
            outNLPFeatures.close();
            lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
              learningSettings);
            /** Open the normal NLP feature file. */
            inNLPFeatures = new BufferedReader(new InputStreamReader(new FileInputStream(new File(wdResults,
              ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
            outFeatureVectors = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
              new File(wdResults,ConstantParameters.FILENAMEOFFeatureVectorData)), "UTF-8"));
            lightWeightApi.nlpfeatures2FVs(wdResults, inNLPFeatures, outFeatureVectors, numDoc, isTraining, learningSettings);
            inNLPFeatures.close();
            outFeatureVectors.flush();
            outFeatureVectors.close();
            if(LogService.minVerbosityLevel > 0) displayDataFilesInformation();
            break;
          case TRAINING:
              // empty the data file
              EvaluationBasedOnDocs.emptyDatafile(wdResults, true);
              if(LogService.minVerbosityLevel > 0) System.out.println("** Training mode:");
              LogService.logMessage("** Training mode:", 1);
              isTraining = true;
              outNLPFeatures = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(wdResults,
                ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
              for(int i = 0; i < numDoc; ++i) {
                Document toProcess = (Document)corpus.get(i);
                lightWeightApi.annotations2NLPFeatures(toProcess, i,
                  outNLPFeatures, isTraining, learningSettings);
                if(toProcess.getDataStore() != null)
                  Factory.deleteResource(toProcess);
              }
              outNLPFeatures.flush();
              outNLPFeatures.close();
              lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
                learningSettings);
              /** Open the normal NLP feature file. */
              inNLPFeatures = new BufferedReader(new InputStreamReader(new FileInputStream(new File(wdResults,
                ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
              outFeatureVectors = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(wdResults,ConstantParameters.FILENAMEOFFeatureVectorData)), "UTF-8"));
              lightWeightApi.nlpfeatures2FVs(wdResults, inNLPFeatures, outFeatureVectors, numDoc, isTraining, learningSettings);
              inNLPFeatures.close();
              outFeatureVectors.flush();
              outFeatureVectors.close();
              // if fitering the training data
              if(learningSettings.fiteringTrainingData
                && learningSettings.filteringRatio > 0.0)
                lightWeightApi.FilteringNegativeInstsInJava(corpus.size(),
                  learningSettings);
              // using the java code for training
              lightWeightApi.trainingJava(corpus.size(), learningSettings);
              break;
            case APPLICATION:
              // if application
              /*EvaluationBasedOnDocs.emptyDatafile(wdResults, false);
              if(LogService.minVerbosityLevel> 0) System.out.println("** Application mode:");
              LogService.logMessage("** Application mode:", 1);
              isTraining = false;
              String classTypeOriginal = learningSettings.datasetDefinition
                .getClassAttribute().getType();
              outNLPFeatures = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(wdResults,
                ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
              for(int i = 0; i < numDoc; ++i) {
                lightWeightApi.annotations2NLPFeatures((Document)corpus.get(i), i,
                  outNLPFeatures, isTraining, learningSettings);
              }
              outNLPFeatures.flush();
              outNLPFeatures.close();
              lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
                learningSettings);
              // Open the normal NLP feature file.
              inNLPFeatures = new BufferedReader(new InputStreamReader(new FileInputStream(new File(wdResults,
                ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
              lightWeightApi.nlpfeatures2FVs(wdResults, inNLPFeatures, numDoc, isTraining, learningSettings);
              inNLPFeatures.close();
              // Applying th model
              lightWeightApi.applyModelInJava(corpus, 0, corpus.size(), classTypeOriginal,
                learningSettings);*/
              //EvaluationBasedOnDocs.emptyDatafile(wdResults, false);
              if(endDocIdApp>startDocIdApp) {
              if(LogService.minVerbosityLevel> 0) System.out.println("** " +
                  "Application mode for document from "+ startDocIdApp + " to "+ endDocIdApp+"(not included):");
              LogService.logMessage("** Application mode for document from "+ startDocIdApp + " to "+ endDocIdApp+"(not included):", 1);
              isTraining = false;
              String classTypeOriginal = learningSettings.datasetDefinition
                .getClassAttribute().getType();
              outNLPFeatures = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(wdResults,
                ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
              numDoc = endDocIdApp - startDocIdApp;
              for(int i = startDocIdApp; i < endDocIdApp; ++i) {
                Document toProcess = (Document)corpus.get(i);
                lightWeightApi.annotations2NLPFeatures(toProcess, i-startDocIdApp,
                  outNLPFeatures, isTraining, learningSettings);
                if(toProcess.getDataStore()!= null && corpus.getDataStore() != null) {//(isDatastore)
                  Factory.deleteResource(toProcess);
                  corpus.getDataStore().sync(corpus);
                }
              }
              outNLPFeatures.flush();
              outNLPFeatures.close();
              lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
                learningSettings);
              /** Open the normal NLP feature file. */
              inNLPFeatures = new BufferedReader(new InputStreamReader(new FileInputStream(new File(wdResults,
                ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
              outFeatureVectors = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(wdResults,ConstantParameters.FILENAMEOFFeatureVectorDataApp)), "UTF-8"));
              lightWeightApi.nlpfeatures2FVs(wdResults, inNLPFeatures, outFeatureVectors, numDoc, isTraining, learningSettings);
              inNLPFeatures.close();
              outFeatureVectors.flush();
              outFeatureVectors.close();
              // Applying th model
              String fvFileName = wdResults.toString() + File.separator
              + ConstantParameters.FILENAMEOFFeatureVectorDataApp;
              lightWeightApi.applyModelInJava(corpus, startDocIdApp, endDocIdApp, classTypeOriginal,
                learningSettings, fvFileName);
              //Update the datastore for the added annotations
              }
              break;
            case EVALUATION:
              if(LogService.minVerbosityLevel > 0) System.out.println("** Evaluation mode:");
              LogService.logMessage("** Evaluation mode:", 1);
              evaluation = new EvaluationBasedOnDocs(corpus, wdResults,
                inputASName);
              evaluation
                .evaluation(learningSettings, lightWeightApi);
              break;
            case MITRAINING:
              if(LogService.minVerbosityLevel > 0) System.out.println("** MITRAINING mode:");
              LogService.logMessage("** MITRAINING mode:", 1);
              isTraining = true;
              /**Need to write the NLP features into a temporary file, then copy it into the NLP file. */
              BufferedWriter outNLPFeaturesTemp = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(wdResults,
                ConstantParameters.FILENAMEOFNLPFeaturesDataTemp)), "UTF-8"));
              for(int i = 0; i < numDoc; ++i) {
                lightWeightApi.annotations2NLPFeatures((Document)corpus.get(i), i,
                  outNLPFeaturesTemp, isTraining, learningSettings);
              }
              outNLPFeaturesTemp.flush();
              outNLPFeaturesTemp.close();
              lightWeightApi.finishFVs(wdResults, numDoc, isTraining,
                learningSettings);
              lightWeightApi.copyNLPFeat2NormalFile(wdResults, miLearningInfor.miNumDocsTraining);
              /** Use the temp NLP feature file instead of the normal one for MI-training. */
              inNLPFeatures = new BufferedReader(new InputStreamReader(new FileInputStream(new File(wdResults,
                ConstantParameters.FILENAMEOFNLPFeaturesDataTemp)), "UTF-8"));
              outFeatureVectors = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                new File(wdResults,ConstantParameters.FILENAMEOFFeatureVectorData), true), "UTF-8"));
              lightWeightApi.nlpfeatures2FVs(wdResults, inNLPFeatures, outFeatureVectors, numDoc, isTraining, learningSettings);
              inNLPFeatures.close();
              outFeatureVectors.flush();
              outFeatureVectors.close();
              System.gc(); //to make effort to delete the files.
              miLearningInfor.miNumDocsTraining += numDoc;
              miLearningInfor.miNumDocsFromLast += numDoc;
              if(miLearningInfor.miNumDocsFromLast>=learningSettings.miDocInterval) {
                //Start learning
                //              if fitering the training data
                if(learningSettings.fiteringTrainingData
                  && learningSettings.filteringRatio > 0.0)
                  lightWeightApi.FilteringNegativeInstsInJava(miLearningInfor.miNumDocsTraining,
                    learningSettings);
                // using the java code for training
                lightWeightApi.trainingJava(miLearningInfor.miNumDocsTraining, learningSettings);
                //Reset the num from last training as 0
                miLearningInfor.miNumDocsFromLast = 0;
              }
              File miLeFile = new File(wdResults, ConstantParameters.FILENAMEOFMILearningInfor);
              miLearningInfor.writeDataIntoFile(miLeFile);
              break;
            default:
              throw new GateException("The learning mode is not defined!");
        }
        LogService.logMessage("This learning session finished!.", 1);
        LogService.close();
      } catch(IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch(GateException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      if(LogService.minVerbosityLevel > 0)
        System.out.println("This learning session finished!.");
      
    } // end of learning (position=corpus.size()-1)
  }

  /** Print out the information for featureData only option. */
  private void displayDataFilesInformation() {
    StringBuffer logMessage = new StringBuffer();
    logMessage.append("NLP features for all the documents are in the file"
      + wdResults.getAbsolutePath() + File.separator
      + ConstantParameters.FILENAMEOFNLPFeaturesData+"\n");
    logMessage.append("Feature vectors in sparse format are in the file"
      + wdResults.getAbsolutePath() + File.separator
      + ConstantParameters.FILENAMEOFFeatureVectorData+"\n");
    logMessage.append("Label list is in the file"
      + wdResults.getAbsolutePath() + File.separator
      + ConstantParameters.FILENAMEOFLabelList+"\n");
    logMessage.append("NLP features list is in the file"
      + wdResults.getAbsolutePath() + File.separator
      + ConstantParameters.FILENAMEOFNLPFeatureList+"\n");
    logMessage.append("The statistics of entity length for each class is in the file"
        + wdResults.getAbsolutePath() + File.separator
        + ConstantParameters.FILENAMEOFChunkLenStats+"\n");
    System.out.println(logMessage.toString());
    LogService.logMessage(logMessage.toString(),1);
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
