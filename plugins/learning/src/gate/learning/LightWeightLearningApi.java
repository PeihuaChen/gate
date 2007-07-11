/*
 *  LightWeightLearningApi.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: LightWeightLearningApi.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learning;

import gate.AnnotationSet;
import gate.Annotation;
import gate.Corpus;
import gate.Factory;
import gate.FeatureMap;
import gate.Node;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import gate.Document;
import gate.learning.learners.ChunkOrEntity;
import gate.learning.learners.MultiClassLearning;
import gate.learning.learners.PostProcessing;
import gate.learning.learners.SupervisedLearner;
import gate.learning.learners.weka.WekaLearner;
import gate.learning.learners.weka.WekaLearning;
import gate.util.GateException;
import gate.util.OffsetComparator;

/**
 * Do all the main learning tasks, such as obtaining the feature vectors from
 * GATE annotations, training and application. Also filtering out some negative
 * examples if want.
 */
public class LightWeightLearningApi extends Object {
  /** This is where the model(s) should be saved */
  private File wd;
  /**
   * The annotationSet containing annotations considered in the DATASET element
   * of configuration file.
   */
  public String inputASName;
  /** Object of the NLP feature list. */
  public NLPFeaturesList featuresList;
  /** Object of the label list. */
  public Label2Id labelsAndId;
  /** Position of all features specified in the configuration file. */
  int[] featurePositionTotal;
  /** The left-most position among all features. */
  int maxNegPositionTotal = 0;
  /** The right-most position among all features. */
  int maxPosPositionTotal = 0;
  /** The weight for the Ngram features*/
  float ngramWeight=1.0f;
  /**
   * HashMap for the chunkLenStats, for post-processing of chunk learning.
   */
  HashMap chunkLenHash;
  /** Refering to the NLP feature file for writing. */
  public BufferedWriter outNLPFeatures = null;

  /** Constructor, with working directory setting. */
  public LightWeightLearningApi(File wd) {
    this.wd = wd;
  }

  /**
   * Further initialisation for the main object LearningAPIMain().
   */
  public void furtherInit(File wdResults, LearningEngineSettings engineSettings) {
    // read the NLP feature list
    featuresList = new NLPFeaturesList();
    featuresList.loadFromFile(wdResults,
      ConstantParameters.FILENAMEOFNLPFeatureList);
    labelsAndId = new Label2Id();
    labelsAndId.loadLabelAndIdFromFile(wdResults,
      ConstantParameters.FILENAMEOFLabelList);
    chunkLenHash = ChunkLengthStats.loadChunkLenStats(wdResults,
      ConstantParameters.FILENAMEOFChunkLenStats);
    
    // Get the feature position of all features
    // Keep the order of the three types of features as that in
    // NLPFeaturesOfDoc.obtainDocNLPFeatures()
    int num;
    num = engineSettings.datasetDefinition.arrs.featurePosition.length;
    if(engineSettings.datasetDefinition.dataType == DataSetDefinition.RelationData) {
      num += engineSettings.datasetDefinition.arg1.arrs.featurePosition.length
        + engineSettings.datasetDefinition.arg2.arrs.featurePosition.length;
    }
    this.featurePositionTotal = new int[num];
    num = 0;
    if(engineSettings.datasetDefinition.dataType == DataSetDefinition.RelationData) {
      for(int i = 0; i < engineSettings.datasetDefinition.arg1.arrs.featurePosition.length; ++i)
        this.featurePositionTotal[num++] = engineSettings.datasetDefinition.arg1.arrs.featurePosition[i];
      for(int i = 0; i < engineSettings.datasetDefinition.arg2.arrs.featurePosition.length; ++i)
        this.featurePositionTotal[num++] = engineSettings.datasetDefinition.arg2.arrs.featurePosition[i];
    }
    for(int i = 0; i < engineSettings.datasetDefinition.arrs.featurePosition.length; ++i)
      this.featurePositionTotal[num++] = engineSettings.datasetDefinition.arrs.featurePosition[i];
    maxNegPositionTotal = 0;
    if(maxNegPositionTotal < engineSettings.datasetDefinition.arrs.maxNegPosition)
      maxNegPositionTotal = engineSettings.datasetDefinition.arrs.maxNegPosition;
    if(engineSettings.datasetDefinition.dataType == DataSetDefinition.RelationData) {
      if(maxNegPositionTotal < engineSettings.datasetDefinition.arg1.arrs.maxNegPosition)
        maxNegPositionTotal = engineSettings.datasetDefinition.arg1.arrs.maxNegPosition;
      if(maxNegPositionTotal < engineSettings.datasetDefinition.arg2.arrs.maxNegPosition + 
             engineSettings.datasetDefinition.arg1.maxTotalPosition+2)
        maxNegPositionTotal = engineSettings.datasetDefinition.arg2.arrs.maxNegPosition
          + engineSettings.datasetDefinition.arg1.maxTotalPosition+2;
    }
    //Get the ngram weight from the datasetdefintion.
    ngramWeight = 1.0f;
    if(engineSettings.datasetDefinition.ngrams!= null && 
      engineSettings.datasetDefinition.ngrams.size()>0 &&
      ((Ngram)engineSettings.datasetDefinition.ngrams.get(0)).weight != 1.0)
      ngramWeight = ((Ngram)engineSettings.datasetDefinition.ngrams.get(0)).weight;
    if(engineSettings.datasetDefinition.dataType == DataSetDefinition.RelationData) {
      if(engineSettings.datasetDefinition.arg1.ngrams != null &&
        engineSettings.datasetDefinition.arg1.ngrams.size()>0 &&
        ((Ngram)engineSettings.datasetDefinition.arg1.ngrams.get(0)).weight!= 1.0)
        ngramWeight = ((Ngram)engineSettings.datasetDefinition.arg1.ngrams.get(0)).weight;
      if(engineSettings.datasetDefinition.arg2.ngrams != null &&
        engineSettings.datasetDefinition.arg2.ngrams.size()>0 &&
        ((Ngram)engineSettings.datasetDefinition.arg2.ngrams.get(0)).weight!= 1.0)
        ngramWeight = ((Ngram)engineSettings.datasetDefinition.arg2.ngrams.get(0)).weight;
    }
    
  }

  /**
   * Obtain the features and labels and form feature vectors from the GATE
   * annotation of each document.
   */
  public void annotations2NLPFeatures(Document doc, int numDocs, File wdResults,
    boolean isTraining, LearningEngineSettings engineSettings) {
    AnnotationSet annotations = null;
    if(inputASName == null || inputASName.trim().length() == 0) {
      annotations = doc.getAnnotations();
    } else {
      annotations = doc.getAnnotations(inputASName);
    }
    if(numDocs == 0) {
      try {
        outNLPFeatures = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(wdResults,
          ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
      } catch(IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    // obtain the NLP features for the document
    String docName = doc.getName().replaceAll(ConstantParameters.ITEMSEPARATOR,
      "_");
    if(docName.contains("_"))
      docName = docName.substring(0, docName.lastIndexOf("_"));
    if(LogService.minVerbosityLevel > 1)
      System.out.println(numDocs + " docname=" + docName + ".");
    NLPFeaturesOfDoc nlpFeaturesDoc = new NLPFeaturesOfDoc(annotations,
      engineSettings.datasetDefinition.getInstanceType(), docName);
    nlpFeaturesDoc.obtainDocNLPFeatures(annotations,
      engineSettings.datasetDefinition);
    // update the NLP features list
    if(isTraining && engineSettings.isNLPFeatListUpdatable) {
      featuresList.addFeaturesFromDoc(nlpFeaturesDoc);
    }
    if(isTraining && engineSettings.isLabelListUpdatable) {
      // update the class name list
      labelsAndId.updateMultiLabelFromDoc(nlpFeaturesDoc.classNames);
    }
    // Only after the label list was updated, update the chunk length
    // list for each label
    if(isTraining)
      ChunkLengthStats.updateChunkLensStats(annotations,
        engineSettings.datasetDefinition, chunkLenHash, labelsAndId);
    nlpFeaturesDoc.writeNLPFeaturesToFile(outNLPFeatures, docName, numDocs,
      featurePositionTotal);
 
    return;
  }

  /** Normalising the feature vectors. */
  static void normaliseFVs(DocFeatureVectors docFV) {
    for(int i = 0; i < docFV.numInstances; ++i) {
      double sum = 0;
      for(int j = 0; j < docFV.fvs[i].len; ++j)
        sum += docFV.fvs[i].values[j] * docFV.fvs[i].values[j];
      sum = Math.sqrt(sum);
      for(int j = 0; j < docFV.fvs[i].len; ++j)
        docFV.fvs[i].values[j] /= sum;
    }
  }

  /**
   * Finishing the conversion from annotations to feature vectors by writing
   * back the label and nlp feature list into files, and closing the java
   * writers.
   */
  public void finishFVs(File wdResults, int numDocs, boolean isTraining,
    LearningEngineSettings engineSettings) {
    try {
      outNLPFeatures.close();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if(isTraining && engineSettings.isNLPFeatListUpdatable)
      featuresList.writeListIntoFile(wdResults,
        ConstantParameters.FILENAMEOFNLPFeatureList);
    if(isTraining && engineSettings.isLabelListUpdatable)
      labelsAndId.writeLabelAndIdToFile(wdResults,
        ConstantParameters.FILENAMEOFLabelList);
    if(isTraining)
      ChunkLengthStats.writeChunkLensStatsIntoFile(wdResults,
        ConstantParameters.FILENAMEOFChunkLenStats, chunkLenHash);
  }

  /** Convert the NLP features into feature vectors and write them into file. */
  public void nlpfeatures2FVs(File wdResults, int numDocs, boolean isTraining, 
    LearningEngineSettings engineSettings) {
    
      try {
        BufferedWriter outFeatureVectors = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
          new File(wdResults,ConstantParameters.FILENAMEOFFeatureVectorData)), "UTF-8"));
        BufferedReader inNLPFeatures = new BufferedReader(new InputStreamReader(new 
          FileInputStream(new File(wdResults,
            ConstantParameters.FILENAMEOFNLPFeaturesData)), "UTF-8"));
        //Read the first line out which is about feature names
        inNLPFeatures.readLine();
        for(int i=0; i<numDocs; ++i) {
          NLPFeaturesOfDoc nlpFeatDoc = new NLPFeaturesOfDoc();
          nlpFeatDoc.readNLPFeaturesFromFile(inNLPFeatures);
          
          DocFeatureVectors docFV = new DocFeatureVectors();
          docFV.obtainFVsFromNLPFeatures(nlpFeatDoc, featuresList,
            featurePositionTotal, maxNegPositionTotal, featuresList.totalNumDocs, ngramWeight);
          
          if(isTraining) {
            LabelsOfFeatureVectorDoc labelsDoc = new LabelsOfFeatureVectorDoc();
            labelsDoc.obtainMultiLabelsFromNLPDocSurround(nlpFeatDoc,
              labelsAndId, engineSettings.surround);
            addDocFVsMultiLabelToFile(numDocs, outFeatureVectors,
              labelsDoc.multiLabels, docFV);
          } else {
            int[] labels = new int[nlpFeatDoc.numInstances];
            addDocFVsToFile(numDocs, outFeatureVectors, labels, docFV);
          }
        }
        outFeatureVectors.flush();
        outFeatureVectors.close();
        inNLPFeatures.close();
      } catch(IOException e) {
        System.out.println("Error occured in reading the NLP data from file for converting to FVs" +
            "or writing the FVs data into file!");
      }
  }
  
  /** Write the FVs of one document into file. */
  void addDocFVsToFile(int index, BufferedWriter out, int[] labels,
    DocFeatureVectors docFV) {
    try {
      out.write(new Integer(index) + " " + new Integer(docFV.numInstances)
        + " " + docFV.docId);
      out.newLine();
      for(int i = 0; i < docFV.numInstances; ++i) {
        StringBuffer line = new StringBuffer();
        line.append(new Integer(i + 1) + " " + new Integer(labels[i]));
        for(int j = 0; j < docFV.fvs[i].len; ++j)
          line.append(" " + docFV.fvs[i].indexes[j] + ":"
            + docFV.fvs[i].values[j]);
        out.write(line.toString());
        out.newLine();
      }
    } catch(IOException e) {
    }
  }

  /** Write the FVs with labels of one document into file. */
  void addDocFVsMultiLabelToFile(int index, BufferedWriter out,
    LabelsOfFV[] multiLabels, DocFeatureVectors docFV) {
    try {
      out.write(new Integer(index) + ConstantParameters.ITEMSEPARATOR
        + new Integer(docFV.numInstances) + ConstantParameters.ITEMSEPARATOR
        + docFV.docId);
      out.newLine();
      for(int i = 0; i < docFV.numInstances; ++i) {
        StringBuffer line = new StringBuffer();
        line.append(new Integer(i + 1) + ConstantParameters.ITEMSEPARATOR
          + multiLabels[i].num);
        for(int j = 0; j < multiLabels[i].num; ++j)
          line.append(ConstantParameters.ITEMSEPARATOR
            + multiLabels[i].labels[j]);
        for(int j = 0; j < docFV.fvs[i].len; ++j)
          line.append(ConstantParameters.ITEMSEPARATOR
            + docFV.fvs[i].indexes[j] + ConstantParameters.INDEXVALUESEPARATOR
            + docFV.fvs[i].values[j]);
        out.write(line.toString());
        out.newLine();
      }
    } catch(IOException e) {
    }
  }

  /**
   * Training using the Java implementatoin of learning algorithms.
   */
  public void trainingJava(int numDocs,
    LearningEngineSettings engineSettings) throws GateException {
    LogService.logMessage("\nTraining starts.\n", 1);
    // The files for training data and model
    File wdResults = new File(wd, ConstantParameters.SUBDIRFORRESULTS);
    String fvFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFFeatureVectorData;
    String nlpFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFNLPFeaturesData;
    String modelFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFModels;
    String labelInDataFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFLabelsInData;
    String nlpDataLabelFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFNLPDataLabel;
    File dataFile = new File(fvFileName);
    File nlpDataFile = new File(nlpFileName);
    File modelFile = new File(modelFileName);
    File labelInData = new File(labelInDataFileName);
    File nlpDataLabelFile = new File(nlpDataLabelFileName);
    int learnerType = obtainLearnerType(engineSettings.learnerSettings.learnerName);
    switch(learnerType){
      case 1: // for weka learner
        LogService.logMessage("Use weka learner.", 1);
        WekaLearning wekaL = new WekaLearning();
        short featureType = WekaLearning
          .obtainWekaLeanerDataType(engineSettings.learnerSettings.learnerName);
        // Convert and read training data
        switch(featureType){
          case WekaLearning.NLPFEATUREFVDATA:
            // Transfer the labels in nlpDataFile into
            // the label in the sparse data
            // and collect the labels and write them into a file
            convertNLPLabelsTDataLabel(nlpDataFile, dataFile, labelInData,
              nlpDataLabelFile, numDocs, engineSettings.surround);
            wekaL.readNLPFeaturesFromFile(nlpDataLabelFile, numDocs,
              this.featuresList, true, labelsAndId.label2Id.size(),
              engineSettings.surround);
            break;
          case WekaLearning.SPARSEFVDATA:
            wekaL.readSparseFVsFromFile(dataFile, numDocs, true,
              labelsAndId.label2Id.size(), engineSettings.surround);
            break;
        }
        // Get the wekaLearner from the learnername
        WekaLearner wekaLearner = WekaLearning.obtainWekaLearner(
          engineSettings.learnerSettings.learnerName,
          engineSettings.learnerSettings.paramsOfLearning);
        LogService.logMessage("Weka learner name: " + wekaLearner.getLearnerName(), 1);
        // Training.
        wekaL.train(wekaLearner, modelFile);
        break;
      case 2: // for learner of multi to binary conversion
        if(LogService.minVerbosityLevel > 1) 
          System.out.println("Using the SVM");
        LogService.logMessage("Multi to binary conversion.", 1);
        MultiClassLearning chunkLearning = new MultiClassLearning(
          engineSettings.multi2BinaryMode);
        // read data
        chunkLearning.getDataFromFile(numDocs, dataFile);
        LogService.logMessage("The number of classes in dataset: "
          + chunkLearning.numClasses, 1);
        // get a learner
        String learningCommand = engineSettings.learnerSettings.paramsOfLearning;
        learningCommand = learningCommand.trim();
        learningCommand = learningCommand.replaceAll("[ \t]+", " ");
        String dataSetFile = null;
        SupervisedLearner paumLearner = MultiClassLearning
          .obtainLearnerFromName(engineSettings.learnerSettings.learnerName,
            learningCommand, dataSetFile);
        paumLearner
          .setLearnerExecutable(engineSettings.learnerSettings.executableTraining);
        paumLearner
          .setLearnerParams(engineSettings.learnerSettings.paramsOfLearning);
        LogService.logMessage("The learners: " + paumLearner.getLearnerName(), 1);
        // training
        chunkLearning.training(paumLearner, modelFile);
        break;
      default:
        System.out.println("Error! Wrong learner type.");
      LogService.logMessage("Error! Wrong learner type.", 0);
    }
  }

  /**
   * Apply the model to data, also using the learning algorithm implemented in
   * Java.
   */
  public void applyModelInJava(Corpus corpus, String labelName,
    LearningEngineSettings engineSettings)
    throws GateException {
    LogService.logMessage("\nApplication starts.", 1);
    // The files for training data and model
    File wdResults = new File(wd, ConstantParameters.SUBDIRFORRESULTS);
    String fvFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFFeatureVectorData;
    String nlpFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFNLPFeaturesData;
    String modelFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFModels;
    // String labelInDataFileName = wdResults.toString() + File.separator
    // + ConstantParameters.FILENAMEOFLabelsInData;
    File dataFile = new File(fvFileName);
    File nlpDataFile = new File(nlpFileName);
    File modelFile = new File(modelFileName);
    int learnerType;
    learnerType = obtainLearnerType(engineSettings.learnerSettings.learnerName);
    int numClasses = 0;
    // Store the label information from the model application
    LabelsOfFeatureVectorDoc[] labelsFVDoc = null;
    short featureType = WekaLearning.SPARSEFVDATA;
    switch(learnerType){
      case 1: // for weka learner
        LogService.logMessage("Use weka learner.", 1);
        WekaLearning wekaL = new WekaLearning();
        // Check if the learner uses the sparse feaature vectors or NLP
        // features
        featureType = WekaLearning
          .obtainWekaLeanerDataType(engineSettings.learnerSettings.learnerName);
        int numDocs = corpus.size();
        switch(featureType){
          case WekaLearning.NLPFEATUREFVDATA:
            wekaL.readNLPFeaturesFromFile(nlpDataFile, numDocs,
              this.featuresList, false, labelsAndId.label2Id.size(),
              engineSettings.surround);
            break;
          case WekaLearning.SPARSEFVDATA:
            wekaL.readSparseFVsFromFile(dataFile, numDocs, false,
              labelsAndId.label2Id.size(), engineSettings.surround);
            break;
        }
        // Check if the weka learner has distribute output of classify
        boolean distributionOutput = WekaLearning
          .obtainWekaLearnerOutputType(engineSettings.learnerSettings.learnerName);
        // Get the wekaLearner from the learnername
        WekaLearner wekaLearner = WekaLearning.obtainWekaLearner(
          engineSettings.learnerSettings.learnerName,
          engineSettings.learnerSettings.paramsOfLearning);
        LogService.logMessage("Weka learner name: " + wekaLearner.getLearnerName(), 1);
        // Training.
        wekaL.apply(wekaLearner, modelFile, distributionOutput);
        labelsFVDoc = wekaL.labelsFVDoc;
        numClasses = labelsAndId.label2Id.size() * 2; // subtract the
        // null class
        break;
      case 2: // for learner of multi to binary conversion
        LogService.logMessage("Multi to binary conversion.", 1);
        //System.out.println("** multi to binary:");
        MultiClassLearning chunkLearning = new MultiClassLearning(
          engineSettings.multi2BinaryMode);
        // read data
        chunkLearning.getDataFromFile(corpus.size(), dataFile);
        // get a learner
        String learningCommand = engineSettings.learnerSettings.paramsOfLearning;
        learningCommand = learningCommand.trim();
        learningCommand = learningCommand.replaceAll("[ \t]+", " ");
        String dataSetFile = null;
        SupervisedLearner paumLearner = MultiClassLearning
          .obtainLearnerFromName(engineSettings.learnerSettings.learnerName,
            learningCommand, dataSetFile);
        paumLearner
          .setLearnerExecutable(engineSettings.learnerSettings.executableTraining);
        paumLearner
          .setLearnerParams(engineSettings.learnerSettings.paramsOfLearning);
        LogService.logMessage("The learners: " + paumLearner.getLearnerName(), 1);
        // apply
        chunkLearning.apply(paumLearner, modelFile);
        labelsFVDoc = chunkLearning.dataFVinDoc.labelsFVDoc;
        numClasses = chunkLearning.numClasses;
        break;
      default:
        System.out.println("Error! Wrong learner type.");
        LogService.logMessage("Error! Wrong learner type.", 0);
    }
    if(engineSettings.surround) {
      String featName = engineSettings.datasetDefinition.arrs.classFeature;
      String instanceType = engineSettings.datasetDefinition.getInstanceType();
      labelsAndId = new Label2Id();
      labelsAndId.loadLabelAndIdFromFile(wdResults,
        ConstantParameters.FILENAMEOFLabelList);
      // post-processing and add new annotation to the text
      PostProcessing postPr = new PostProcessing(
        engineSettings.thrBoundaryProb, engineSettings.thrEntityProb,
        engineSettings.thrClassificationProb);
      //System.out.println("** Application mode:");
      for(int i = 0; i < corpus.size(); ++i) {
        HashSet chunks = new HashSet();
        postPr.postProcessingChunk((short)3, labelsFVDoc[i].multiLabels,
          numClasses, chunks, chunkLenHash);
        //System.out.println("** documentName="+((Document)corpus.get(i)).getName());
        addAnnsInDoc((Document)corpus.get(i), chunks, instanceType, featName,
          labelName, labelsAndId);
      }
    } else {
      String featName = engineSettings.datasetDefinition.arrs.classFeature;
      String instanceType = engineSettings.datasetDefinition.getInstanceType();
      labelsAndId = new Label2Id();
      labelsAndId.loadLabelAndIdFromFile(wdResults,
        ConstantParameters.FILENAMEOFLabelList);
      // post-processing and add new annotation to the text
      // PostProcessing postPr = new PostProcessing(0.42, 0.2);
      PostProcessing postPr = new PostProcessing(
        engineSettings.thrBoundaryProb, engineSettings.thrEntityProb,
        engineSettings.thrClassificationProb);
      for(int i = 0; i < corpus.size(); ++i) {
        int[] selectedLabels = new int[labelsFVDoc[i].multiLabels.length];
        float[] valuesLabels = new float[labelsFVDoc[i].multiLabels.length];
        postPr.postProcessingClassification((short)3,
          labelsFVDoc[i].multiLabels, selectedLabels, valuesLabels);
        addAnnsInDocClassification((Document)corpus.get(i), selectedLabels,
          valuesLabels, instanceType, featName, labelName, labelsAndId,
          engineSettings);
      }
    }
  }

  /** Add the annotation into documents for chunk learning. */
  private void addAnnsInDoc(Document doc, HashSet chunks, String instanceType,
    String featName, String labelName, Label2Id labelsAndId) {
    AnnotationSet annsDoc = null;
    if(inputASName == null || inputASName.trim().length() == 0) {
      annsDoc = doc.getAnnotations();
    } else {
      annsDoc = doc.getAnnotations(inputASName);
    }
    AnnotationSet anns = annsDoc.get(instanceType);
    ArrayList annotationArray = (anns == null || anns.isEmpty())
      ? new ArrayList()
      : new ArrayList(anns);
    Collections.sort(annotationArray, new OffsetComparator());
    for(Object obj : chunks) {
      ChunkOrEntity entity = (ChunkOrEntity)obj;
      FeatureMap features = Factory.newFeatureMap();
      features.put(featName, labelsAndId.id2Label.get(
        new Integer(entity.name).toString()).toString());
      features.put("prob", entity.prob);
      Annotation token1 = (Annotation)annotationArray.get(entity.start);
      Annotation token2 = (Annotation)annotationArray.get(entity.end);
      Node entityS = token1.getStartNode();
      Node entityE = token2.getEndNode();
      if(entityS != null && entityE != null)
        annsDoc.add(entityS, entityE, labelName, features);
    }
  }

  /** Add the annotation into documents for classification. */
  private void addAnnsInDocClassification(Document doc, int[] selectedLabels,
    float[] valuesLabels, String instanceType, String featName,
    String labelName, Label2Id labelsAndId,
    LearningEngineSettings engineSettings) {
    AnnotationSet annsDoc = null;
    if(inputASName == null || inputASName.trim().length() == 0) {
      annsDoc = doc.getAnnotations();
    } else {
      annsDoc = doc.getAnnotations(inputASName);
    }
    AnnotationSet anns = annsDoc.get(instanceType);
    ArrayList annotationArray = (anns == null || anns.isEmpty())
      ? new ArrayList()
      : new ArrayList(anns);
    Collections.sort(annotationArray, new OffsetComparator());
    // For the relation extraction
    String arg1F = null;
    String arg2F = null;
    if(engineSettings.datasetDefinition.dataType == DataSetDefinition.RelationData) {
      AttributeRelation relAtt = (AttributeRelation)engineSettings.datasetDefinition.classAttribute;
      arg1F = relAtt.getArg1();
      arg2F = relAtt.getArg2();
    }
    for(int i = 0; i < annotationArray.size(); ++i) {
      if(selectedLabels[i] < 0) continue;
      FeatureMap features = Factory.newFeatureMap();
      features.put(featName, labelsAndId.id2Label.get(
        new Integer(selectedLabels[i] + 1).toString()).toString());
      features.put("prob", valuesLabels[i]);
      Annotation ann = (Annotation)annotationArray.get(i);
      // For relation data, need the argument features
      if(engineSettings.datasetDefinition.dataType == DataSetDefinition.RelationData) {
        String arg1V = ann.getFeatures().get(
          engineSettings.datasetDefinition.arg1Feat).toString();
        String arg2V = ann.getFeatures().get(
          engineSettings.datasetDefinition.arg2Feat).toString();
        features.put(arg1F, arg1V);
        features.put(arg2F, arg2V);
      }
      annsDoc.add(ann.getStartNode(), ann.getEndNode(), labelName, features);
    }
  }

  /** Convert the string labels in the nlp data file into the index labels. */
  public void convertNLPLabelsTDataLabel(File nlpDataFile, File dataFile,
    File labelInDataFile, File nlpDataLabelFile, int numDocs,
    boolean surroundingMode) {
    try {
      BufferedReader inData = new BufferedReader(new InputStreamReader(new FileInputStream
        (dataFile), "UTF-8"));
      BufferedReader inNlpData = new BufferedReader(new InputStreamReader(new FileInputStream
        (nlpDataFile), "UTF-8"));
      BufferedWriter outNlpDataLabel = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
        nlpDataLabelFile), "UTF-8"));
      HashSet uniqueLabels = new HashSet();
      // The head line of NLP feature file
      String line = inNlpData.readLine();
      outNlpDataLabel.append(line);
      outNlpDataLabel.newLine();
      String[] items;
      // For each document
      for(int iDoc = 0; iDoc < numDocs; ++iDoc) {
        line = inNlpData.readLine();
        outNlpDataLabel.append(line);
        outNlpDataLabel.newLine();
        items = line.split(ConstantParameters.ITEMSEPARATOR);
        int num = Integer.parseInt(items[2]);
        int numLabels;
        inData.readLine();
        // For each instance
        for(int i = 0; i < num; ++i) {
          // Read the line from data file and get the data label
          line = inData.readLine();
          items = line.split(ConstantParameters.ITEMSEPARATOR);
          numLabels = Integer.parseInt(items[1]);
          StringBuffer labels = new StringBuffer();
          labels.append(items[1]);
          for(int j = 0; j < numLabels; ++j) {
            labels.append(ConstantParameters.ITEMSEPARATOR);
            labels.append(items[j + 2]);
            if(!uniqueLabels.contains(items[j + 2]))
              uniqueLabels.add(items[j + 2]);
          }
          outNlpDataLabel.append(labels.toString());
          // Read the line from NLP feature and get the features
          line = inNlpData.readLine();
          items = line.split(ConstantParameters.ITEMSEPARATOR);
          numLabels = Integer.parseInt(items[0]);
          StringBuffer nlpFeats = new StringBuffer();
          for(int j = numLabels + 1; j < items.length; ++j) {
            nlpFeats.append(ConstantParameters.ITEMSEPARATOR);
            nlpFeats.append(items[j]);
          }
          outNlpDataLabel.append(nlpFeats);
          outNlpDataLabel.newLine();
        }
      }
      outNlpDataLabel.flush();
      outNlpDataLabel.close();
      inData.close();
      inNlpData.close();
      BufferedWriter labelInData = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
        labelInDataFile), "UTF-8"));
      labelInData.append(uniqueLabels.size() + " #total_labels");
      labelInData.newLine();
      for(Object obj : uniqueLabels) {
        labelInData.append(obj.toString());
        labelInData.newLine();
      }
      labelInData.flush();
      labelInData.close();
    } catch(FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Flitering out the negative examples of the training data using the SVM.
   * 
   * @throws GateException
   */
  public void FilteringNegativeInstsInJava(int numDocs,
    LearningEngineSettings engineSettings) throws GateException {
    LogService.logMessage("\nFiltering starts.", 1);
    // The files for training data and model
    File wdResults = new File(wd, ConstantParameters.SUBDIRFORRESULTS);
    String fvFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFFeatureVectorData;
    String modelFileName = wdResults.toString() + File.separator
      + ConstantParameters.FILENAMEOFModels;
    File dataFile = new File(fvFileName);
    File modelFile = new File(modelFileName);
    // for learner of multi to binary conversion
    LogService.logMessage("Multi to binary conversion.", 1);
    MultiClassLearning chunkLearning = new MultiClassLearning(
      engineSettings.multi2BinaryMode);
    // read data
    chunkLearning.getDataFromFile(numDocs, dataFile);
    // Back up the label data before it is reset.
    LabelsOfFeatureVectorDoc[] labelsFVDocB = new LabelsOfFeatureVectorDoc[numDocs];
    for(int i = 0; i < numDocs; ++i) {
      labelsFVDocB[i] = new LabelsOfFeatureVectorDoc();
      labelsFVDocB[i].multiLabels = new LabelsOfFV[chunkLearning.dataFVinDoc.labelsFVDoc[i].multiLabels.length];
      for(int j = 0; j < chunkLearning.dataFVinDoc.labelsFVDoc[i].multiLabels.length; ++j)
        labelsFVDocB[i].multiLabels[j] = chunkLearning.dataFVinDoc.labelsFVDoc[i].multiLabels[j];
    }
    // Reset the class label of data for binary class for filtering
    // purpose
    int numNeg; // number of negative example in the training data
    numNeg = chunkLearning.resetClassInData();
    LogService.logMessage("The number of classes in dataset: "
      + chunkLearning.numClasses, 1);
    // Use the SVM only for filtering
    String dataSetFile = null;
    SupervisedLearner paumLearner = MultiClassLearning.obtainLearnerFromName(
      "SVMLibSvmJava", "-c 1.0 -t 0 -m 100 -tau 1.0 ", dataSetFile);
    paumLearner
      .setLearnerExecutable(engineSettings.learnerSettings.executableTraining);
    paumLearner
      .setLearnerParams(engineSettings.learnerSettings.paramsOfLearning);
    LogService.logMessage("The learners: " + paumLearner.getLearnerName(), 1);
    // training
    chunkLearning.training(paumLearner, modelFile);
    // applying the learning model to training example and get the
    // confidence score for each example
    chunkLearning.apply(paumLearner, modelFile);
    // Store the scores of negative examples.
    float[] scoresNegB = new float[numNeg];
    float[] scoresNeg = new float[numNeg];
    int kk = 0;
    for(int i = 0; i < labelsFVDocB.length; ++i) {
      for(int j = 0; j < labelsFVDocB[i].multiLabels.length; ++j)
        if(labelsFVDocB[i].multiLabels[j].num == 0)
          scoresNeg[kk++] = chunkLearning.dataFVinDoc.labelsFVDoc[i].multiLabels[j].probs[0];
    }
    // If want to remove the negative that are close to positive one,
    // reverse the scores.
    if(engineSettings.filteringNear) for(int i = 0; i < numNeg; ++i)
      scoresNeg[i] = -scoresNeg[i];
    // Back up the score before sorting
    for(int i = 0; i < numNeg; ++i)
      scoresNegB[i] = scoresNeg[i];
    // Sort those scores
    Arrays.sort(scoresNeg);
    // int index = numNeg -
    // (int)Math.floor(numNeg*engineSettings.filteringRatio);
    int index = (int)Math.floor(numNeg * engineSettings.filteringRatio);
    if(index >= numNeg) index = numNeg - 1;
    if(index < 0) index = 0;
    float thrFiltering = scoresNeg[index];
    boolean[] isFiltered = new boolean[numNeg];
    for(int i = 0; i < numNeg; ++i)
      if(scoresNegB[i] < thrFiltering)
        isFiltered[i] = true;
      else isFiltered[i] = false;
    // Write the filtered data into the data file
    BufferedWriter out;
    try {
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile), "UTF-8"));
      numNeg = 0;
      for(int i = 0; i < labelsFVDocB.length; ++i) {
        int kk1 = 0;
        int numK = 0; // num of instances in the doc to be kept
        for(int j = 0; j < labelsFVDocB[i].multiLabels.length; ++j)
          if(labelsFVDocB[i].multiLabels[j].num == 0) {
            if(!isFiltered[numNeg + kk1]) ++numK;
            ++kk1;
          } else {
            ++numK;
          }
        out.write(i + ConstantParameters.ITEMSEPARATOR + numK
          + ConstantParameters.ITEMSEPARATOR
          + chunkLearning.dataFVinDoc.trainingFVinDoc[i].docId);
        out.newLine();
        kk1 = 0;
        for(int j = 0; j < labelsFVDocB[i].multiLabels.length; ++j) {
          if(labelsFVDocB[i].multiLabels[j].num > 0
            || !isFiltered[numNeg + kk1]) {
            StringBuffer line = new StringBuffer();
            line.append(j + ConstantParameters.ITEMSEPARATOR
              + labelsFVDocB[i].multiLabels[j].num);
            for(int j1 = 0; j1 < labelsFVDocB[i].multiLabels[j].num; ++j1) {
              line.append(ConstantParameters.ITEMSEPARATOR
                + labelsFVDocB[i].multiLabels[j].labels[j1]);
            }
            SparseFeatureVector fv = chunkLearning.dataFVinDoc.trainingFVinDoc[i].fvs[j];
            for(int j1 = 0; j1 < fv.len; ++j1)
              line.append(ConstantParameters.ITEMSEPARATOR + fv.indexes[j1]
                + ConstantParameters.INDEXVALUESEPARATOR + fv.values[j1]);
            out.write(line.toString());
            out.newLine();
          }
          if(labelsFVDocB[i].multiLabels[j].num == 0) ++kk1;
        }
        numNeg += kk1;
      }
      out.flush();
      out.close();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Determining the type of the learner, it is from Weka or not.
   */
  public static int obtainLearnerType(String learnerName) throws GateException {
    if(learnerName.equals("SVMLibSvmJava") || learnerName.equals("C4.5Weka")
      || learnerName.equals("KNNWeka") || learnerName.equals("NaiveBayesWeka")) {
      if(learnerName.endsWith("Weka")) {
        return 1;
      } else {
        return 2;
      }
    } else {
      throw new GateException("The learning engine named as \"" + learnerName
        + "\" is not defined!");
    }
  }
}
