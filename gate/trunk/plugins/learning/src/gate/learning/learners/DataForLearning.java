/*
 *  DataForLearning.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: DataForLearning.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learning.learners;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import gate.learning.DocFeatureVectors;
import gate.learning.LabelsOfFeatureVectorDoc;
import gate.learning.SparseFeatureVector;
import gate.learning.learners.svm.svm_node;
/**
 * Data used for learning, read from the feature vector file.
 */
public class DataForLearning {
  /** Number of training (or test) documents. */
  private int numTrainingDocs;
  /** Training feature vectors, array for each document. */
  public DocFeatureVectors[] trainingFVinDoc = null;
  /** Training feature vectors in svm_node format, for libSVM. */
  public svm_node[][] svmNodeFVs = null;
  /** Labels for each feature vector, array for each document. */
  public LabelsOfFeatureVectorDoc[] labelsFVDoc = null;
  /** All the unique labels in the dataset */
  String[] allUniqueLabels;
  /** Total number of training examples. */
  int numTraining = 0;
  /** Total number of NLP features in FVs. */
  int totalNumFeatures = 0;
  /** Trivial constructor. */
  public DataForLearning() {
  }
  /** Constructor with the number of documents. */
  public DataForLearning(int num) {
    this.numTrainingDocs = num;
  }

  /** Read the feature vectors from data file for training or application. */
  public void readingFVsFromFile(File trainingData) {
    // the array to store the training data
    trainingFVinDoc = new DocFeatureVectors[numTrainingDocs];
    labelsFVDoc = new LabelsOfFeatureVectorDoc[numTrainingDocs];
    // read the training data from the file
    // first open the training data file
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(
        new FileInputStream(trainingData), "UTF-8"));
      String line;
      String[] items;
      for(int i = 0; i < numTrainingDocs; ++i) {
        line = in.readLine();
        while(line.startsWith("#"))
          line = in.readLine();
        items = line
          .split(gate.learning.ConstantParameters.ITEMSEPARATOR);
        int num;
        num = (new Integer(items[1])).intValue();
        trainingFVinDoc[i] = new DocFeatureVectors();
        labelsFVDoc[i] = new LabelsOfFeatureVectorDoc();
        trainingFVinDoc[i].readDocFVFromFile(in, num, labelsFVDoc[i]);
      }
      // compute the total number of training examples
      numTraining = 0;
      for(int i = 0; i < numTrainingDocs; ++i)
        numTraining += trainingFVinDoc[i].getNumInstances();
      // compute the total number of features
      totalNumFeatures = 0;
      for(int i = 0; i < numTrainingDocs; ++i) {
        SparseFeatureVector[] fvs = trainingFVinDoc[i].getFvs();
        for(int j = 0; j < trainingFVinDoc[i].getNumInstances(); ++j) {
          int[] indexes = fvs[j].getIndexes();
          if(totalNumFeatures < indexes[indexes.length - 1])
            totalNumFeatures = indexes[indexes.length - 1];
        }
      }
      // add 3 for safety, because the index is counted from 1, not 0
      totalNumFeatures += 5;
    } catch(IOException e) {
    }
    return;
  }

  /** Read the feature vectors from data file for training or application. */
  public void readingFVsMultiLabelFromFile(File trainingData) {
    // the array to store the training data
    trainingFVinDoc = new DocFeatureVectors[numTrainingDocs];
    labelsFVDoc = new LabelsOfFeatureVectorDoc[numTrainingDocs];
    // read the training data from the file
    // first open the training data file
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(
        trainingData), "UTF-8"));
      String line;
      String[] items;
      for(int i = 0; i < numTrainingDocs; ++i) {
        line = in.readLine();
        while(line.startsWith("#"))
          line = in.readLine();
        items = line
          .split(gate.learning.ConstantParameters.ITEMSEPARATOR);
        int num;
        num = (new Integer(items[1])).intValue();
        trainingFVinDoc[i] = new DocFeatureVectors();
        labelsFVDoc[i] = new LabelsOfFeatureVectorDoc();
        trainingFVinDoc[i].readDocFVFromFile(in, num, labelsFVDoc[i]);
      }
      // compute the total number of training examples
      numTraining = 0;
      for(int i = 0; i < numTrainingDocs; ++i)
        numTraining += trainingFVinDoc[i].getNumInstances();
      // compute the total number of features
      totalNumFeatures = 0;
      for(int i = 0; i < numTrainingDocs; ++i) {
        SparseFeatureVector[] fvs = trainingFVinDoc[i].getFvs();
        for(int j = 0; j < trainingFVinDoc[i].getNumInstances(); ++j) {
          int[] indexes = fvs[j].getIndexes();
          if(totalNumFeatures < indexes[indexes.length - 1])
            totalNumFeatures = indexes[indexes.length - 1];
        }
      }
      // add 3 for safety, because the index is counted from 1, not 0
      totalNumFeatures += 5;
    } catch(IOException e) {
    }
    return;
  }

  public int getTotalNumFeatures() {
    return this.totalNumFeatures;
  }

  public int getNumTrainingDocs() {
    return this.numTrainingDocs;
  }

  public int getNumTraining() {
    return this.numTraining;
  }
}
