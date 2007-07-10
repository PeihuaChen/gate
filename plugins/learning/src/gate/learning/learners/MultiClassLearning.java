/*
 *  MultiClassLearning.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: MultiClassLearning.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learning.learners;

import gate.learning.LabelsOfFV;
import gate.learning.LogService;
import gate.learning.SparseFeatureVector;
import gate.learning.DocFeatureVectors.LongCompactor;
import gate.util.GateException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
/** 
 * Learning and application by converting the multi-class
 * problem into several binary class problems.
 */
public class MultiClassLearning {
  /** The training data -- FVs in doc format. */
  public DataForLearning dataFVinDoc;
  /** Labels for training instances */
  short[] labelsTraining;
  /** Feature vectors for training instances. */
  SparseFeatureVector[] fvsTraining;
  /** Number of classes for learning */
  public int numClasses;
  /** The name of class and the number of instances in training document */
  public HashMap class2NumberInstances;
  /** Use the one against all others, or use the one against another. 
   * 1 for one against all others, 
   * 2 for one against another.
   */
  short multi2BinaryMode = 1; 
  /**
   * The number of instances in the training data without label (or with label
   * null).
   */
  int numNull = 0;
  /** Constructor*/
  public MultiClassLearning() {
  }
  /** Constructor with conversion mode. */
  public MultiClassLearning(short mode) {
    multi2BinaryMode = mode;
  }

  /** Get the training data -- feature vectors and labels. */
  public void getDataFromFile(int numDocs, File trainingDataFile) {
    dataFVinDoc = new DataForLearning(numDocs);
    dataFVinDoc.readingFVsFromFile(trainingDataFile);
    // First, get the unique labels from the trainign data
    class2NumberInstances = new HashMap();
    numNull = obtainUniqueLabels(dataFVinDoc, class2NumberInstances);
    numClasses = class2NumberInstances.size();
    return;
  }
  /** Reset the labels for learning for training data filtering. */
  public int resetClassInData() {
    // Reset the data's class as 1 or -1
    int numNeg = 0;
    for(int i = 0; i < dataFVinDoc.labelsFVDoc.length; ++i) {
      // LabelsOfFeatureVectorDoc labelsDoc = dataFVinDoc.labelsFVDoc[i];
      for(int j = 0; j < dataFVinDoc.labelsFVDoc[i].multiLabels.length; ++j) {
        if(dataFVinDoc.labelsFVDoc[i].multiLabels[j].num > 0) { // if it has
                                                                // label
          LabelsOfFV simpLabels = new LabelsOfFV(1);
          simpLabels.labels = new int[1];
          simpLabels.labels[0] = 1;
          dataFVinDoc.labelsFVDoc[i].multiLabels[j] = simpLabels;
        } else ++numNeg;
      }
    }
    // Reset the label collection
    class2NumberInstances = new HashMap();
    numNull = obtainUniqueLabels(dataFVinDoc, class2NumberInstances);
    numClasses = class2NumberInstances.size();
    return numNeg;
  }

  /** Learn the models and write them into a file */
  public void training(SupervisedLearner learner, File modelFile) {
    int totalNumFeatures = dataFVinDoc.getTotalNumFeatures();
    Set classesName = class2NumberInstances.keySet();
    ArrayList array1 = new ArrayList(classesName);
    LongCompactor c = new LongCompactor();
    Collections.sort(array1, c);
    if(LogService.minVerbosityLevel>1)
      System.out.println("total Number of classes for learning is "
        + array1.size());
    LogService.logMessage("total Number of classes for learning is "
      + array1.size(), 1);
    // Open the mode file for writing the model into it
    BufferedWriter modelsBuff;
    try {
      modelsBuff = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(modelFile), "UTF-8"));
      // convert the multi-class to binary class -- labels conversion
      labelsTraining = new short[dataFVinDoc.numTraining];
      fvsTraining = new SparseFeatureVector[dataFVinDoc.numTraining];
      switch(multi2BinaryMode){
        case 1: // if using the one vs all others appoach
          // Write some meta information into the model as a header
          LogService.logMessage(
            "One against others for multi to binary class conversion.", 1);
          writeTrainingMetaData(modelsBuff, numClasses, numNull, dataFVinDoc
            .getNumTrainingDocs(), dataFVinDoc.getTotalNumFeatures(), modelFile
            .getAbsolutePath(), learner);
          for(int i = 0; i < array1.size(); ++i) {
            Multi2BinaryClass.oneVsOthers(dataFVinDoc,
              array1.get(i).toString(), labelsTraining, fvsTraining);
            int numTraining = labelsTraining.length;
            int numP = 0;
            for(int i1 = 0; i1 < numTraining; ++i1)
              if(labelsTraining[i1] > 0) ++numP;
            modelsBuff.append("Class=" + array1.get(i).toString()
              + " numTraining=" + numTraining + " numPos=" + numP + "\n");
            long time1 = new Date().getTime();
            learner.training(modelsBuff, fvsTraining, totalNumFeatures,
              labelsTraining, numTraining);
            long time2 = new Date().getTime();
            time2 -= time1;
            LogService.logMessage("Training time for class "
              + array1.get(i).toString() + ": " + time2 + "ms", 1);
          }
          break;
        case 2: // if using the one vs another appoach
          // new numClasses
          int numClasses0;
          if(numNull > 0)
            numClasses0 = (numClasses + 1) * numClasses / 2;
          else numClasses0 = (numClasses - 1) * numClasses / 2;
          LogService.logMessage("One against another for multi to binary class conversion.\n"+
            "So actually we have " + numClasses0
            + " binary classes.", 1);
          writeTrainingMetaData(modelsBuff, numClasses0, numNull, dataFVinDoc
            .getNumTrainingDocs(), dataFVinDoc.getTotalNumFeatures(), modelFile
            .getAbsolutePath(), learner);
          // first for null vs label
          if(numNull > 0) {
            for(int j = 0; j < array1.size(); ++j) {
              int numTraining;
              numTraining = Multi2BinaryClass.oneVsNull(dataFVinDoc, array1
                .get(j).toString(), labelsTraining, fvsTraining);
              int numP = 0;
              for(int i1 = 0; i1 < numTraining; ++i1) {
                if(labelsTraining[i1] > 0) ++numP;
              }
              modelsBuff.append("Class1=_NULL" + " Class2="
                + array1.get(j).toString() + " numTraining=" + numTraining
                + " numPos=" + numP + "\n");
              long time1 = new Date().getTime();
              learner.training(modelsBuff, fvsTraining, totalNumFeatures,
                labelsTraining, numTraining);
              long time2 = new Date().getTime();
              time2 -= time1;
              LogService.logMessage("Training time for class null against "
                + array1.get(j).toString() + ": " + time2 + "ms", 1);
            }
          }
          // then for one vs. another
          for(int i = 0; i < array1.size(); ++i)
            for(int j = i + 1; j < array1.size(); ++j) {
              int numTraining;
              numTraining = Multi2BinaryClass.oneVsAnother(dataFVinDoc, array1
                .get(i).toString(), array1.get(j).toString(), labelsTraining,
                fvsTraining);
              int numP = 0;
              for(int i1 = 0; i1 < numTraining; ++i1) {
                if(labelsTraining[i1] > 0) ++numP;
              }
              modelsBuff.append("Class1=_NULL" + " Class2="
                + array1.get(j).toString() + " numTraining=" + numTraining
                + " numPos=" + numP + "\n");
              long time1 = new Date().getTime();
              learner.training(modelsBuff, fvsTraining, totalNumFeatures,
                labelsTraining, numTraining);
              long time2 = new Date().getTime();
              time2 -= time1;
              LogService.logMessage("Training time for class "
                + array1.get(i).toString() + " against "
                + array1.get(j).toString() + ": " + time2 + "ms", 1);
              // }
            }
          break;
        default:
          System.out.println("Incorrect multi2BinaryMode value="
            + multi2BinaryMode);
        LogService.logMessage("Incorrect multi2BinaryMode value="
            + multi2BinaryMode,0);
      }
      modelsBuff.close();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /** Apply the model to the data. */
  public void apply(SupervisedLearner learner, File modelFile) {
    // Open the mode file and read the model
    BufferedReader modelsBuff;
    try {
      modelsBuff = new BufferedReader(new InputStreamReader(
        new FileInputStream(modelFile), "UTF-8"));
      // Read the training meta information from the model file's header
      // include the total number of features and number of tags (numClasses)
      int totalNumFeatures;
      String learnerNameFromModel = learner.getLearnerName();
      totalNumFeatures = ReadTrainingMetaData(modelsBuff, learnerNameFromModel);
      if(LogService.minVerbosityLevel>1)
        System.out.println(" *** numClasses=" + numClasses + " totalfeatures="
        + totalNumFeatures);
      // compare with the meta data of test data
      if(totalNumFeatures < dataFVinDoc.getTotalNumFeatures())
        totalNumFeatures = dataFVinDoc.getTotalNumFeatures();
      // Apply the model to test feature vectors
      long time1 = new Date().getTime();
      switch(multi2BinaryMode){
        case 1:
          LogService.logMessage("One against others for multi to binary class conversion.\n"
            +"Number of classes in model: " + numClasses, 1);
          // Use the tau modification in all cases
          learner.isUseTauALLCases = true;
          learner.applying(modelsBuff, dataFVinDoc, totalNumFeatures,
            numClasses);
          if(LogService.minVerbosityLevel>1)
            System.out.println("**** One against all others, numNull=" + numNull);
          break;
        case 2:
          LogService.logMessage("One against another for multi to binary class conversion.", 1);
          // not use the tau modification in all cases
          learner.isUseTauALLCases = false;
          learner.applying(modelsBuff, dataFVinDoc, totalNumFeatures,
            numClasses);
          PostProcessing postProc = new PostProcessing();
          // Get the number of classes of the problem, since the numClasses
          // refers to the number
          // of classes in the one against another method.
          int numClassesL = numClasses * 2;
          numClassesL = rootQuaEqn(numClassesL);
          if(numNull == 0) numClassesL += 1;
          LogService.logMessage("Number of classes in training data: "
            + numClassesL+"\nActuall number of binary classes in model: "
            + numClasses, 1);
          if(LogService.minVerbosityLevel>1)
            System.out.println("**** One against another, numNull=" + numNull);
          if(numNull > 0)
            postProc.voteForOneVSAnotherNull(dataFVinDoc, numClassesL);
          else postProc.voteForOneVSAnother(dataFVinDoc, numClassesL);
          // Set the number of classes with the correct value.
          numClasses = numClassesL;
          break;
        default:
          System.out.println("Incorrect multi2BinaryMode value="
            + multi2BinaryMode);
        LogService.logMessage("Incorrect multi2BinaryMode value="
            + multi2BinaryMode, 1);
      }
      long time2 = new Date().getTime();
      time2 -= time1;
      LogService.logMessage("Application time for class: " + time2 + "ms", 1);
      modelsBuff.close();
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  /** Get the number of classes in the problem from the number of classes
   * in the one vs. another method, by solving a quadratic equation. 
   */
  private int rootQuaEqn(int numClassesL) {
    // The positive root of quadratic equation x^2+x-numClassesL=0.
    return (int)((-1 + Math.sqrt(1.0 + numClassesL * 4)) / 2.0);
  }
  /** Writting the meta information about the learning into the model file. */
  public void writeTrainingMetaData(BufferedWriter modelsBuff, int numClasses,
    int numNull, int numTrainingDocs, long totalFeatures, String modelFile,
    SupervisedLearner learner) throws IOException {
    modelsBuff.append(numTrainingDocs + " #numTrainingDocs\n");
    modelsBuff.append(numClasses + " #numClasses\n");
    modelsBuff.append(numNull + " #numNullLabelInstances\n");
    modelsBuff.append(totalFeatures + " #totalFeatures\n");
    modelsBuff.append(modelFile + " #modelFile\n");
    modelsBuff.append(learner.getLearnerName() + " #learnerName\n");
    modelsBuff.append(learner.getLearnerExecutable() + " #learnerExecutable\n");
    modelsBuff.append(learner.getLearnerParams() + " #learnerParams\n");
    return;
  }
  /** Read the meta data from the header of the file. */
  public int ReadTrainingMetaData(BufferedReader modelsBuff,
    String learnerNameFromModel) throws IOException {
    int totalFeatures;
    String line;
    modelsBuff.readLine(); // read the traing documents
    line = modelsBuff.readLine(); // read the number of classes
    numClasses = new Integer(line.substring(0, line.indexOf(" "))).intValue();
    line = modelsBuff.readLine(); // read the number of classes
    numNull = new Integer(line.substring(0, line.indexOf(" "))).intValue();
    line = modelsBuff.readLine(); // read the total number of features
    totalFeatures = new Integer(line.substring(0, line.indexOf(" ")))
      .intValue();
    modelsBuff.readLine(); // read the model file name
    line = modelsBuff.readLine(); // read the learner's name
    learnerNameFromModel = line.substring(0, line.indexOf(" "));
    modelsBuff.readLine(); // read the learnerExecutable string
    modelsBuff.readLine(); // read the learnerParams string
    return totalFeatures;
  }
  /** Obtain the unqilabels from the training data. */
  int obtainUniqueLabels(DataForLearning dataFVinDoc,
    HashMap class2NumberInstances) {
    int numN = 0;
    for(int i = 0; i < dataFVinDoc.getNumTrainingDocs(); ++i)
      for(int j = 0; j < dataFVinDoc.labelsFVDoc[i].multiLabels.length; ++j) {
        // int label = dataFVinDoc.labelsFVDoc[i].labels[j];
        LabelsOfFV multiLabel = dataFVinDoc.labelsFVDoc[i].multiLabels[j];
        if(multiLabel.num == 0) ++numN;
        for(int j1 = 0; j1 < multiLabel.num; ++j1) {
          if(Integer.valueOf(multiLabel.labels[j1]) > 0) {
            if(class2NumberInstances.containsKey(multiLabel.labels[j1]))
              class2NumberInstances.put(multiLabel.labels[j1],
                (new Integer(class2NumberInstances.get(multiLabel.labels[j1])
                  .toString())) + 1);
            else class2NumberInstances.put(multiLabel.labels[j1], "1");
          }
        }
      }
    return numN;
  }

  /**
   * Obtain the learner from the learner's name speficied by the learning
   * configuration file.
   * @throws GateException 
   */
  public static SupervisedLearner obtainLearnerFromName(String learnerName,
    String commandLine, String dataFilesName) throws GateException {
    SupervisedLearner learner = null;
    if(learnerName.equalsIgnoreCase("SVMLibSvmJava")) {
      learner = new SvmLibSVM();
      learner.setLearnerName(learnerName);
      learner.setCommandLine(commandLine + " " + dataFilesName);
      learner.getParametersFromCommmand();
    } else {
      throw new GateException("The learner's name \""+learnerName +
        "\" is not defined!");
    }
    return learner;
  }
}
