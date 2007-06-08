/*
 *  EvaluationBasedOnDocs.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: EvaluationBasedOnDocs.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learning;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.ResourceInstantiationException;
import gate.util.AnnotationDiffer;
import gate.util.GateException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Do evaluation by spliting the documents into training and testing datasets.
 * Two methods of spliting are implemented, namely k-fold and hold-out test.
 */
public class EvaluationBasedOnDocs {
  /** Corpus referring to the corpus used as data. */
  Corpus corpusOn;
  /** Number of documents in the corpus. */
  int numDoc;
  /**
   * Showing if one document used for training or testing in one evaluation.
   */
  boolean[] isUsedForTraining;
  /** The sub-directory for storing the data file produced by ML api. */
  File wdResults;
  /** Name of the annotation set as input. */
  String inputASName;
  /** Storing the macro averaged overall F-meausre results of the evaluation. */
  public EvaluationMeasuresComputation macroMeasuresOfResults = new EvaluationMeasuresComputation();
  /** Storing the macro averaged results of every label. */
  HashMap labels2MMR = new HashMap();
  /**
   * Label to number of the runs having that label, used for macro-averaged for
   * each label.
   */
  HashMap labels2RunsNum = new HashMap();
  /**
   * Label to number of the instances with that label, averaged over the number
   * of runs for that label.
   */
  HashMap labels2InstNum = new HashMap();

  /** Constructor. */
  public EvaluationBasedOnDocs(Corpus corpus, File wdRes, String inputAsN) {
    corpusOn = corpus;
    numDoc = corpus.size();
    isUsedForTraining = new boolean[numDoc];
    wdResults = wdRes;
    inputASName = inputAsN;
  }

  /** Main method for evluation. */
  public void evaluation(LearningEngineSettings learningSettings,
    LightWeightLearningApi lightWeightApi, PrintWriter logFileIn)
    throws GateException {
    // k-fold
    if(learningSettings.evaluationconfig.mode == EvaluationConfiguration.kfold)
      kfoldEval(learningSettings, lightWeightApi, logFileIn);
    // Hold-out testing
    else if(learningSettings.evaluationconfig.mode == EvaluationConfiguration.split)
      holdoutEval(learningSettings, lightWeightApi, logFileIn);
    else throw new GateException("The evaluation configuration mode as "
      + learningSettings.evaluationconfig.mode + " is not implemented!");
    if(LogService.debug >= 0) {
      logFileIn.println("\nAveraged results for each label as:");
      printFmeasureForEachLabel(labels2InstNum, labels2MMR, logFileIn);
      System.out.println("\nOverall results as:");
      macroMeasuresOfResults.printResults();
      logFileIn.println("\nOverall results (micro-averaged):");
      macroMeasuresOfResults.printResults(logFileIn);
    }
  }

  /** K-fold evalution. */
  public void kfoldEval(LearningEngineSettings learningSettings,
    LightWeightLearningApi lightWeightApi, PrintWriter logFileIn)
    throws GateException {
    int k = learningSettings.evaluationconfig.kk;
    logFileIn.println("K-fold evaluation: k=" + k);
    int lenPerFold = (new Double(Math.floor((double)numDoc / k))).intValue();
    if(lenPerFold < 1) lenPerFold = 1;
    int beginIndex, endIndex;
    if(LogService.debug > 0) {
      System.out.println("Kfold k=" + new Integer(k) + ", numDoc="
        + new Integer(numDoc) + ", len=" + new Integer(lenPerFold) + ".");
    }
    for(int nr = 0; nr < k; ++nr) {
      EvaluationMeasuresComputation measuresOfResults = new EvaluationMeasuresComputation();
      // Label to measure of result of the label
      HashMap labels2MR = new HashMap();
      beginIndex = nr * lenPerFold;
      endIndex = (nr + 1) * lenPerFold;
      if(endIndex > numDoc) endIndex = numDoc;
      if(beginIndex > endIndex) beginIndex = endIndex;
      int i;
      for(i = 0; i < beginIndex; ++i)
        isUsedForTraining[i] = true;
      for(i = beginIndex; i < endIndex; ++i)
        isUsedForTraining[i] = false;
      for(i = endIndex; i < numDoc; ++i)
        isUsedForTraining[i] = true;
      logFileIn.println("Fold " + nr);
      logFileIn.println("Number of docs for training: "
        + (int)(numDoc - endIndex + beginIndex));
      logFileIn.println("Number of docs for application: "
        + (int)(endIndex - beginIndex));
      // call the training or application
      oneRun(learningSettings, lightWeightApi, isUsedForTraining,
        measuresOfResults, labels2MR, labels2InstNum, logFileIn);
      // Add to the macro averaged figures
      add2MacroMeasure(measuresOfResults, labels2MR, macroMeasuresOfResults,
        labels2MMR, labels2RunsNum);
    }
    macroMeasuresOfResults.macroAverage(k);
    for(Object obj : labels2MMR.keySet())
      ((EvaluationMeasuresComputation)labels2MMR.get(obj))
        .macroAverage(new Integer(labels2RunsNum.get(obj).toString())
          .intValue());
  }

  /** Hold-out testing. */
  public void holdoutEval(LearningEngineSettings learningSettings,
    LightWeightLearningApi lightWeightApi, PrintWriter logFileIn)
    throws GateException {
    int k = learningSettings.evaluationconfig.kk;
    logFileIn.println("Hold-out test: runs=" + k
      + ", ratio of training docs is "
      + learningSettings.evaluationconfig.ratio);
    int trainingNum = (new Double(Math
      .floor((numDoc * learningSettings.evaluationconfig.ratio))).intValue());
    if(trainingNum > numDoc) trainingNum = numDoc;
    if(LogService.debug > 0) {
      System.out.println("Split, k=" + new Integer(k) + ", trainingNum="
        + new Integer(trainingNum) + ".");
    }
    int testNum = numDoc - trainingNum;
    Random randGenerator = new Random(1000);
    for(int nr = 0; nr < k; ++nr) {
      EvaluationMeasuresComputation measuresOfResults = new EvaluationMeasuresComputation();
      // Label to measure of result of the label
      HashMap labels2MR = new HashMap();
      // Select the training examples randomly from the data
      int[] indexRand = new int[testNum];
      for(int i = 0; i < testNum; ++i) {
        int newNum = 0;
        boolean isDuplicate = true;
        int maxNumSel = 0;
        while(isDuplicate && maxNumSel < 1000) {
          newNum = randGenerator.nextInt(numDoc);
          isDuplicate = false;
          for(int j = 0; j < i; ++j)
            if(indexRand[j] == newNum) {
              isDuplicate = true;
              break;
            }
        }
        if(isDuplicate) {
          // If cannot select a non-duplicate index in
          // 1000 times, selected the one
          boolean isOk = false;
          for(int j = 0; j < numDoc; ++j) {
            for(int j1 = 0; j1 < i; ++j1)
              if(indexRand[j1] != j) {
                newNum = j;
                isOk = true;
                break;
              }
            if(isOk) break;
          }
        }
        if(LogService.debug > 0) {
          System.out.println("i=" + new Integer(i) + ", newNum="
            + new Integer(newNum));
        }
        indexRand[i] = newNum;
      }
      for(int i = 0; i < numDoc; ++i)
        isUsedForTraining[i] = true;
      for(int i = 0; i < testNum; ++i)
        isUsedForTraining[indexRand[i]] = false;
      logFileIn.println("Run " + nr);
      logFileIn.println("Number of docs for training: "
        + (int)(numDoc - testNum));
      logFileIn.println("Number of docs for application: " + (int)testNum);
      // One run, call the training and application and do evaluation
      oneRun(learningSettings, lightWeightApi, isUsedForTraining,
        measuresOfResults, labels2MR, labels2InstNum, logFileIn);
      // Add to the macro averaged figures
      add2MacroMeasure(measuresOfResults, labels2MR, macroMeasuresOfResults,
        labels2MMR, labels2RunsNum);
    }
    macroMeasuresOfResults.macroAverage(k);
    for(Object obj : labels2MMR.keySet()) {
      int num = new Integer(labels2RunsNum.get(obj).toString()).intValue();
      ((EvaluationMeasuresComputation)labels2MMR.get(obj)).macroAverage(num);
      // Averaged the number of instances for all the runs
      num = (int)(new Float(labels2InstNum.get(obj).toString()).floatValue() / num);
      labels2InstNum.put(obj, new Integer(num));
    }
  }

  /** One run of the evaluation: training, testing and measuring results. */
  private void oneRun(LearningEngineSettings learningSettings,
    LightWeightLearningApi lightWeightApi, boolean isUsedForTraining[],
    EvaluationMeasuresComputation measuresOfResults, HashMap labels2MR,
    HashMap labels2InstNum, PrintWriter logFileIn) throws GateException {
    // first learning using the document for training
    // empty the data file
    // emptyDatafile(wdResults, lightWeightApi);
    lightWeightApi.labelsAndId.clearAllData();
    boolean isTraining = true;
    int numDoc = 0;
    for(int i = 0; i < corpusOn.size(); ++i)
      if(isUsedForTraining[i]) {
        lightWeightApi.annotations2FVs((Document)corpusOn.get(i), numDoc,
          wdResults, isTraining, learningSettings);
        ++numDoc;
      }
    lightWeightApi.finishFVs(wdResults, numDoc, isTraining, learningSettings);
    // if fitering the training data
    if(learningSettings.fiteringTrainingData
      && learningSettings.filteringRatio > 0.0)
      lightWeightApi.FilteringNegativeInstsInJava(numDoc, logFileIn,
        learningSettings);
    // lightWeightApi.trainDirect();
    lightWeightApi.trainingJava(numDoc, logFileIn, learningSettings);
    // then application to the test set
    // We have to use two class types for the evaluation purpose
    String classTypeOriginal = null;
    String classTypeTest = null;
    String classFeature = null;
    classTypeOriginal = learningSettings.datasetDefinition.getClassAttribute()
      .getType();
    classTypeTest = classTypeOriginal.concat("Test");
    classFeature = learningSettings.datasetDefinition.getClassAttribute()
      .getFeature();
    learningSettings.datasetDefinition.getClassAttribute().setType(
      classTypeTest);
    if(LogService.debug > 0)
      System.out.println("classType=" + classTypeOriginal + ", testType="
        + classTypeTest + ".");
    isTraining = false;
    numDoc = 0;
    for(int i = 0; i < corpusOn.size(); ++i)
      if(!isUsedForTraining[i]) {
        lightWeightApi.annotations2FVs((Document)corpusOn.get(i), numDoc,
          wdResults, isTraining, learningSettings);
        ++numDoc;
      }
    lightWeightApi.finishFVs(wdResults, numDoc, isTraining, learningSettings);
    // lightWeightApi.finishDocAnnotation();
    Corpus corpusTest;
    try {
      corpusTest = Factory.newCorpus("testCorpus");
      numDoc = 0;
      for(int i = 0; i < corpusOn.size(); ++i)
        if(!isUsedForTraining[i]) {
          corpusTest.add((Document)corpusOn.get(i));
          ++numDoc;
        }
      lightWeightApi.applyModelInJava(corpusTest, classTypeTest, logFileIn,
        learningSettings);
      corpusTest.clear();
      Factory.deleteResource(corpusTest);
    } catch(ResourceInstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // Do the evaluation on test using the AnnotationDiff
    // First get all the labels in the training data,
    // so that we can do evaluation on each single label
    // as well as on all labels
    HashMap uniqueLabels = new HashMap();
    for(int i = 0; i < corpusOn.size(); ++i)
      if(isUsedForTraining[i]) {
        AnnotationSet keyAnns = getInputAS((Document)corpusOn.get(i)).get(
          classTypeOriginal);
        for(Object obj : keyAnns) {
          if(((Annotation)obj).getFeatures().get(classFeature) != null) {
            String label = ((Annotation)obj).getFeatures().get(classFeature)
              .toString();
            if(uniqueLabels.containsKey(label))
              uniqueLabels.put(label, new Integer(new Integer(uniqueLabels.get(
                label).toString()).intValue() + 1));
            else uniqueLabels.put(label, "1");
          }
        }
      }
    // Then create one evaluationMeasure object for each label
    for(Object obj : uniqueLabels.keySet()) {
      EvaluationMeasuresComputation emc = new EvaluationMeasuresComputation();
      labels2MR.put(obj, emc);
    }
    // Copy the number of instances for each label into the macro one
    for(Object obj : uniqueLabels.keySet()) {
      if(labels2InstNum.containsKey(obj)) {
        int num = new Integer(uniqueLabels.get(obj).toString()).intValue();
        labels2InstNum.put(obj, new Integer(new Integer(labels2InstNum.get(obj)
          .toString()).intValue()
          + num));
      } else labels2InstNum.put(obj, uniqueLabels.get(obj));
    }
    // Do the evaluation on the test set
    if(learningSettings.datasetDefinition.dataType == DataSetDefinition.RelationData) {
      // For relation type, we cannot use the evalutation method AnnDiff
      // of Gate
      AttributeRelation relAttr = (AttributeRelation)learningSettings.datasetDefinition
        .getClassAttribute();
      String arg1F = relAttr.getArg1();
      String arg2F = relAttr.getArg2();
      for(int i = 0; i < corpusOn.size(); ++i) {
        if(!isUsedForTraining[i]) {
          evaluateAnnotationsRel((Document)corpusOn.get(i), classTypeOriginal,
            classTypeTest, classFeature, arg1F, arg2F, uniqueLabels.keySet(),
            labels2MR);
        }
      }
    } else {
      /*
       * for(int i=0; i<corpusOn.size(); ++i) { if(! isUsedForTraining[i]) {
       * evaluateAnnotations((Document)corpusOn.get(i), classTypeOriginal,
       * classTypeTest, classFeature, uniqueLabels.keySet(), labels2MR); } }
       */
      // evaluation on each label using the AnnDiff method of Gate
      for(int i = 0; i < corpusOn.size(); ++i)
        if(!isUsedForTraining[i]) {
          evaluateAnnDiff((Document)corpusOn.get(i), classTypeOriginal,
            classTypeTest, classFeature, uniqueLabels.keySet(), labels2MR);
        }
    }
    // Pool the results of all labels together
    for(Object obj : uniqueLabels.keySet())
      measuresOfResults.add((EvaluationMeasuresComputation)labels2MR.get(obj));
    measuresOfResults.computeFmeasure();
    measuresOfResults.computeFmeasureLenient();
    for(Object obj : uniqueLabels.keySet()) {
      EvaluationMeasuresComputation emc = (EvaluationMeasuresComputation)labels2MR
        .get(obj);
      emc.computeFmeasure();
      emc.computeFmeasureLenient();
    }
    if(LogService.debug > 0) {
      System.out.println("in one run:");
      // Print the results of each label
      printFmeasureForEachLabel(uniqueLabels, labels2MR, logFileIn);
      System.out.println("Results of All labels");
      measuresOfResults.printResults();
      logFileIn.println("\nResults of overall labels (micro-averaged)");
      measuresOfResults.printResults(logFileIn);
    }
    // finally, change the class type back for training,
    // and remove the test annotations
    learningSettings.datasetDefinition.getClassAttribute().setType(
      classTypeOriginal);
    for(int i = 0; i < corpusOn.size(); ++i) {
      if(!isUsedForTraining[i]) {
        AnnotationSet annsInput = getInputAS((Document)corpusOn.get(i));
        AnnotationSet anns = annsInput.get(classTypeTest);
        Iterator iter = anns.iterator();
        while(iter.hasNext())
          annsInput.remove((Annotation)iter.next());
      }
    }
  }

  /** Empty the label list and feature list. */
  public static void emptyDatafile(File wdResults,
    LightWeightLearningApi lightWeightApi) {
    (new File(wdResults, ConstantParameters.FILENAMEOFNLPFeatureList)).delete();
    (new File(wdResults, ConstantParameters.FILENAMEOFLabelList)).delete();
    try {
      (new File(wdResults, ConstantParameters.FILENAMEOFNLPFeatureList))
        .createNewFile();
      lightWeightApi.outFeatureVectors = new BufferedWriter(new FileWriter(
        new File(wdResults, ConstantParameters.FILENAMEOFFeatureVectorData)));
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** Add the F-measure of each label to the overal F-measure */
  public void add2MacroMeasure(EvaluationMeasuresComputation measuresOfResults,
    HashMap labels2MR, EvaluationMeasuresComputation macroMeasuresOfResults,
    HashMap labels2MMR, HashMap labels2RunsNum) {
    macroMeasuresOfResults.add(measuresOfResults);
    // for each label
    for(Object obj : labels2MR.keySet()) {
      String label = obj.toString();
      if(labels2MMR.containsKey(label)) {// the label is in the macro
        // Array already
        ((EvaluationMeasuresComputation)labels2MMR.get(label))
          .add(((EvaluationMeasuresComputation)(labels2MR.get(label))));
        labels2RunsNum.put(label, new Integer((new Integer(labels2RunsNum.get(
          label).toString()).intValue() + 1)));
      } else {
        // labels2MMR.put(label, new EvaluationMeasuresComputation());
        EvaluationMeasuresComputation emc = new EvaluationMeasuresComputation();
        emc.add((EvaluationMeasuresComputation)(labels2MR.get(label)));
        labels2MMR.put(label, emc);
        labels2RunsNum.put(label, "1");
      }
    }
  }

  /** Print the F-measure results for each label. */
  public void printFmeasureForEachLabel(HashMap uniqueLabels,
    HashMap labels2MR, PrintWriter logFileIn) {
    System.out.println("\nResults of single label");
    logFileIn.println("\nResults of single label");
    List labels = new ArrayList(uniqueLabels.keySet());
    Collections.sort(labels);
    for(int i = 0; i < labels.size(); ++i) {
      String labelName = labels.get(i).toString();
      System.out.println(i + " LabelName=" + labelName
        + ", number of instances="
        + new Integer(uniqueLabels.get(labelName).toString()));
      ((EvaluationMeasuresComputation)labels2MR.get(labelName)).printResults();
      logFileIn.println(i + " LabelName=" + labelName
        + ", number of instances="
        + new Integer(uniqueLabels.get(labelName).toString()));
      ((EvaluationMeasuresComputation)labels2MR.get(labelName))
        .printResults(logFileIn);
    }
  }

  /** Evaluate the test document by using the AnnotationDiff class. */
  public void evaluateAnnDiff(Document doc, String classTypeOriginal,
    String classTypeTest, String classFeat, Set labelSet, HashMap labels2MR) {
    // for each label
    AnnotationSet annsOriginal = getInputAS(doc).get(classTypeOriginal);
    AnnotationSet annsTest = getInputAS(doc).get(classTypeTest);
    HashSet annsKey = new HashSet();
    HashSet annsRes = new HashSet();
    HashSet signSet = new HashSet();
    signSet.add(classFeat);
    for(Object obj : labelSet) {
      String label = obj.toString();
      annsKey.clear();
      annsRes.clear();
      for(Object objAnn : annsOriginal)
        if(((FeatureMap)((Annotation)objAnn).getFeatures()).get(classFeat) != null)
          if(((FeatureMap)((Annotation)objAnn).getFeatures()).get(classFeat)
            .toString().equals(label)) annsKey.add(objAnn);
      for(Object objAnn : annsTest)
        if(((FeatureMap)((Annotation)objAnn).getFeatures()).get(classFeat) != null)
          if(((FeatureMap)((Annotation)objAnn).getFeatures()).get(classFeat)
            .toString().equals(label)) annsRes.add(objAnn);
      AnnotationDiffer annDiff = new AnnotationDiffer();
      annDiff.setSignificantFeaturesSet(signSet);
      annDiff.calculateDiff(annsKey, annsRes);
      EvaluationMeasuresComputation emc = (EvaluationMeasuresComputation)labels2MR
        .get(label);
      emc.correct += annDiff.getCorrectMatches();
      emc.partialCor += annDiff.getPartiallyCorrectMatches();
      emc.missing += annDiff.getMissing();
      emc.spurious += annDiff.getSpurious();
    }
  }

  /**
   * Evaluate the test document by comparing the annotations with the golden
   * standard
   */
  public void evaluateAnnotations(Document doc, String classTypeOriginal,
    String classTypeTest, String classFeat, Set labelSet, HashMap labels2MR) {
    AnnotationSet annsOriginal = getInputAS(doc).get(classTypeOriginal);
    AnnotationSet annsTest = getInputAS(doc).get(classTypeTest);
    HashSet annsKey = new HashSet();
    HashSet annsRes = new HashSet();
    // For each label
    for(Object obj : labelSet) {
      String label = obj.toString();
      annsKey.clear();
      annsRes.clear();
      for(Object objAnn : annsOriginal) {
        Object label0 = ((Annotation)objAnn).getFeatures().get(classFeat);
        if(label0 != null && label0.equals(label)) annsKey.add(objAnn);
      }
      for(Object objAnn : annsTest) {
        Object label0 = ((Annotation)objAnn).getFeatures().get(classFeat);
        if(label0 != null && label0.equals(label)) annsRes.add(objAnn);
      }
      EvaluationMeasuresComputation measuresOfResults = (EvaluationMeasuresComputation)labels2MR
        .get(label);
      for(Object annOrig : annsKey) {
        boolean isMatch = false;
        for(Object annTest : annsRes)
          if(((Annotation)annTest).coextensive(((Annotation)annOrig))) {
            measuresOfResults.correct++;
            isMatch = true;
            break;
          }
        if(!isMatch) measuresOfResults.missing++;
      }
      for(Object annTest : annsRes) {
        boolean isMatch = false;
        for(Object annOrig : annsKey)
          if(((Annotation)annTest).coextensive(((Annotation)annOrig))) {
            isMatch = true;
            break;
          }
        if(!isMatch) measuresOfResults.spurious++;
      }
    }
  }

  /**
   * Evaluate the test document by comparing the annotations with the golden
   * standard, for Relation learning
   */
  public void evaluateAnnotationsRel(Document doc, String classTypeOriginal,
    String classTypeTest, String classFeat, String arg1F, String arg2F,
    Set labelSet, HashMap labels2MR) {
    AnnotationSet annsOriginal = getInputAS(doc).get(classTypeOriginal);
    AnnotationSet annsTest = getInputAS(doc).get(classTypeTest);
    HashSet annsKey = new HashSet();
    HashSet annsRes = new HashSet();
    // For each label
    for(Object obj : labelSet) {
      String label = obj.toString();
      annsKey.clear();
      annsRes.clear();
      for(Object objAnn : annsOriginal) {
        Object label0 = ((Annotation)objAnn).getFeatures().get(classFeat);
        if(label0 != null && label0.equals(label)) annsKey.add(objAnn);
      }
      for(Object objAnn : annsTest) {
        Object label0 = ((Annotation)objAnn).getFeatures().get(classFeat);
        if(label0 != null && label0.equals(label)) annsRes.add(objAnn);
      }
      EvaluationMeasuresComputation measuresOfResults = (EvaluationMeasuresComputation)labels2MR
        .get(label);
      for(Object annOrig : annsKey) {
        Object arg1VO = ((Annotation)annOrig).getFeatures().get(arg1F);
        Object arg2VO = ((Annotation)annOrig).getFeatures().get(arg2F);
        boolean isMatch = false;
        for(Object annTest : annsRes) {
          FeatureMap feats = ((Annotation)annTest).getFeatures();
          if(arg1VO.equals(feats.get(arg1F)) && arg2VO.equals(feats.get(arg2F))) {
            measuresOfResults.correct++;
            isMatch = true;
            break;
          }
        }
        if(!isMatch) measuresOfResults.missing++;
      }
      for(Object annTest : annsRes) {
        Object arg1VO = ((Annotation)annTest).getFeatures().get(arg1F);
        Object arg2VO = ((Annotation)annTest).getFeatures().get(arg2F);
        boolean isMatch = false;
        for(Object annOrig : annsKey) {
          FeatureMap feats = ((Annotation)annOrig).getFeatures();
          if(arg1VO.equals(feats.get(arg1F)) && arg2VO.equals(feats.get(arg2F))) {
            isMatch = true;
            break;
          }
        }
        if(!isMatch) measuresOfResults.spurious++;
      }
    }
  }

  private AnnotationSet getInputAS(Document doc) {
    if(inputASName == null || inputASName.trim().length() == 0) {
      return doc.getAnnotations();
    } else {
      return doc.getAnnotations(inputASName);
    }
  }
}
