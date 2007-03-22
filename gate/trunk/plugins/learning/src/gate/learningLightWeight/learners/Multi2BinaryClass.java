package gate.learningLightWeight.learners;

import gate.learningLightWeight.LabelsOfFV;
import gate.learningLightWeight.SparseFeatureVector;
import gate.learningLightWeight.LabelsOfFeatureVectorDoc;

public class Multi2BinaryClass {
  
  static int oneVsOthers(DataForLearning dataFVinDoc, String className, 
          short [] labels, SparseFeatureVector [] fvs) {
    int kk=0;
    for(int i=0; i<dataFVinDoc.getNumTrainingDocs(); ++i) { //for each document
      //int [] labelFVs = dataFVinDoc.labelsFVDoc[i].labels;
      SparseFeatureVector [] fvsInDoc = dataFVinDoc.trainingFVinDoc[i].getFvs();
      for(int j=0; j<fvsInDoc.length; ++j) { //for each instance
        fvs[kk] = fvsInDoc[j];
        //For the class
        LabelsOfFV multiLabel = dataFVinDoc.labelsFVDoc[i].multiLabels[j];
        labels[kk] = -1;
        for(int j1=0; j1<multiLabel.num; ++j1) {
          //if(className.equals(multiLabel.labels[j1])) {
          if(className.equals(new Integer(multiLabel.labels[j1]).toString())) {
            labels[kk] = 1;
            break;
          }
        }
        ++kk;
      }
    }
    return kk;
  }
  
  static int oneVsAnother(DataForLearning dataFVinDoc, String className1, String className2,
          short [] labels, SparseFeatureVector [] fvs) {
    int kk=0;
    for(int i=0; i<dataFVinDoc.getNumTrainingDocs(); ++i) { //for each document
      //int [] labelFVs = dataFVinDoc.labelsFVDoc[i].labels;
      SparseFeatureVector [] fvsInDoc = dataFVinDoc.trainingFVinDoc[i].getFvs();
      for(int j=0; j<fvsInDoc.length; ++j) { //for each instance
        LabelsOfFV multiLabel = dataFVinDoc.labelsFVDoc[i].multiLabels[j];
        boolean isCounted = false;
        short labelT = 0;
        for(int j1=0; j1<multiLabel.num; ++j1) {
          //if(className1.equals(multiLabel.labels[j1])) {
          if(className1.equals(new Integer(multiLabel.labels[j1]).toString())) {
            //labels[kk] = 1;
            //isCounted = true;
            labelT = 1;
            if(isCounted)
              isCounted = false;
            else isCounted = true;
            //break;
          } else if(className2.equals(new Integer(multiLabel.labels[j1]).toString())) {
            //labels[kk] = 1;
            //isCounted = true;
            labelT = -1;
            if(isCounted)
              isCounted = false;
            else isCounted = true;
          }
        }
        if(isCounted) {
          labels[kk] = labelT;
          fvs[kk] = fvsInDoc[j];
          ++kk;
        }
      }
    }
    return kk;
  }
  
  static int oneVsNull(DataForLearning dataFVinDoc, String className,
          short [] labels, SparseFeatureVector [] fvs) {
    int kk=0;
    for(int i=0; i<dataFVinDoc.getNumTrainingDocs(); ++i) { //for each document
      //int [] labelFVs = dataFVinDoc.labelsFVDoc[i].labels;
      SparseFeatureVector [] fvsInDoc = dataFVinDoc.trainingFVinDoc[i].getFvs();
      for(int j=0; j<fvsInDoc.length; ++j) { //for each instance
        LabelsOfFV multiLabel = dataFVinDoc.labelsFVDoc[i].multiLabels[j];
        boolean isCounted = false;
        if(multiLabel.num==0) {
          labels[kk] = -1;
          isCounted = true;
        } else {
          for(int j1=0; j1<multiLabel.num; ++j1) {
            //if(className.equals(multiLabel.labels[j1])) {
            if(className.equals(new Integer(multiLabel.labels[j1]).toString())) {
              labels[kk] = 1;
              isCounted = true;
              break;
            }
          }
        }
        if(isCounted) {
          fvs[kk] = fvsInDoc[j];
          ++kk;
        }
      }
    }
    return kk;
  }
  
}
