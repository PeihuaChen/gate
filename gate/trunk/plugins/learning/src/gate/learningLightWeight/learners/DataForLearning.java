package gate.learningLightWeight.learners;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import gate.learningLightWeight.DocFeatureVectors;
import gate.learningLightWeight.LabelsOfFeatureVectorDoc;
import gate.learningLightWeight.SparseFeatureVector;
import gate.learningLightWeight.learners.libSVM.svm_node;

public class DataForLearning {
  /** Number of training (or test) documents. */
  private int numTrainingDocs;
  
  /** Training feature vectors, array for each document. */
  public DocFeatureVectors [] trainingFVinDoc = null;
  
  /** Training feature vectors in svm_node format, for libSVM. */
  public svm_node [][] svmNodeFVs = null;
  
  /** Labels for each feature vector, array for each document.*/
  public LabelsOfFeatureVectorDoc [] labelsFVDoc = null;
  
  /** All the unique labels in the dataset */
  String [] allUniqueLabels;
  
  /** total number of training examples. */
  int numTraining=0;
  /** total number of features in FVs. */
  int totalNumFeatures=0;
  
  public DataForLearning() {
    
  }
  
  public DataForLearning(int num) {
    this.numTrainingDocs = num;
  }
  
  /** Read the feature vectors from data file for training or application */
  public void readingFVsFromFile(File trainingData) {
    //the array to store the training data
    trainingFVinDoc = new DocFeatureVectors [numTrainingDocs];
    labelsFVDoc = new LabelsOfFeatureVectorDoc [numTrainingDocs];
    
    //read the training data from the file
    //first open the training data file
    try {
      BufferedReader in = new BufferedReader (new FileReader(trainingData));
      
      String line;
      String [] items;
      for(int i=0; i<numTrainingDocs; ++i) {
        line = in.readLine();
        while(line.startsWith("#"))
          line = in.readLine();
        items = line.split(gate.learningLightWeight.ConstantParameters.ITEMSEPARATOR);
        int num;
        num = (new Integer(items[1])).intValue();
        trainingFVinDoc[i] = new DocFeatureVectors();
        labelsFVDoc[i] = new LabelsOfFeatureVectorDoc();
        trainingFVinDoc[i].readDocFVFromFile(in, num, labelsFVDoc[i]);
      }
      //compute the total number of training examples
      numTraining = 0;
      for(int i=0; i<numTrainingDocs; ++i)
        numTraining +=trainingFVinDoc[i].getNumInstances();
      //compute the total number of features
      totalNumFeatures =0;
      for(int i=0; i<numTrainingDocs; ++i) {
        SparseFeatureVector [] fvs = trainingFVinDoc[i].getFvs();
        for(int j=0; j<trainingFVinDoc[i].getNumInstances(); ++j) {
          int [] indexes = fvs[j].getIndexes();
          if(totalNumFeatures<indexes[indexes.length-1])
            totalNumFeatures = indexes[indexes.length-1];
        }
      }
      //add 3 for safety, because the index is counted from 1, not 0
      totalNumFeatures += 5; 
      
    } 
    catch (IOException e) {
    }
    
    return;
  }
  
  /** Read the feature vectors from data file for training or application */
  public void readingFVsMultiLabelFromFile(File trainingData) {
    //the array to store the training data
    trainingFVinDoc = new DocFeatureVectors [numTrainingDocs];
    labelsFVDoc = new LabelsOfFeatureVectorDoc [numTrainingDocs];
    
    //read the training data from the file
    //first open the training data file
    try {
      BufferedReader in = new BufferedReader (new FileReader(trainingData));
      
      String line;
      String [] items;
      for(int i=0; i<numTrainingDocs; ++i) {
        line = in.readLine();
        while(line.startsWith("#"))
          line = in.readLine();
        items = line.split(gate.learningLightWeight.ConstantParameters.ITEMSEPARATOR);
        int num;
        num = (new Integer(items[1])).intValue();
        trainingFVinDoc[i] = new DocFeatureVectors();
        labelsFVDoc[i] = new LabelsOfFeatureVectorDoc();
        trainingFVinDoc[i].readDocFVFromFile(in, num, labelsFVDoc[i]);
      }
      //compute the total number of training examples
      numTraining = 0;
      for(int i=0; i<numTrainingDocs; ++i)
        numTraining +=trainingFVinDoc[i].getNumInstances();
      //compute the total number of features
      totalNumFeatures =0;
      for(int i=0; i<numTrainingDocs; ++i) {
        SparseFeatureVector [] fvs = trainingFVinDoc[i].getFvs();
        for(int j=0; j<trainingFVinDoc[i].getNumInstances(); ++j) {
          int [] indexes = fvs[j].getIndexes();
          if(totalNumFeatures<indexes[indexes.length-1])
            totalNumFeatures = indexes[indexes.length-1];
        }
      }
      //add 3 for safety, because the index is counted from 1, not 0
      totalNumFeatures += 5; 
      
    } 
    catch (IOException e) {
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
