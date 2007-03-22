package gate.learningLightWeight.learners.learnersFromWeka;

import gate.learningLightWeight.LabelsOfFV;
import gate.learningLightWeight.LabelsOfFeatureVectorDoc;
import java.io.Serializable;
import weka.core.Attribute;
import weka.core.Instances;

public abstract class WekaLearner implements Serializable {
  /** The name of learner.*/
  String learnerName=null;
  
  /**The weka classifier.*/
  public weka.classifiers.Classifier wekaCl;
  
  public void training(Instances instancesData) {
    try {
      System.out.println("Learning start:");
      wekaCl.buildClassifier(instancesData);
      //System.out.println("learned model in String:"+wekaCl.toString());
    } catch(Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
  public void applying(Instances instancesData, LabelsOfFeatureVectorDoc []  labelsFVDoc, boolean distributionOutput) {
    int numInst=0;
    int numClasses = instancesData.numClasses()-1; //not count the label for null class
    //Get the map from the output to values(true label of the problem)
    int [] trueLabels = new int[numClasses+1]; //From the attribute index to true labels
    for(int i=0; i<=numClasses; ++i) {
      trueLabels[i] = Integer.parseInt(instancesData.classAttribute().value(i));
      //System.out.println("label i="+i+", value="+trueLabels[i]+".");
    }
      
    
    System.out.println("Application starts...");
    try {
      if(distributionOutput) {
        double [] distr;
        for(int iDoc = 0; iDoc<labelsFVDoc.length; ++iDoc) {
          int num = labelsFVDoc[iDoc].multiLabels.length;
          for(int i=0; i<num; ++i) {
            distr = wekaCl.distributionForInstance(instancesData.instance(numInst++));
            labelsFVDoc[iDoc].multiLabels[i] = new LabelsOfFV(numClasses);
            labelsFVDoc[iDoc].multiLabels[i].probs = new float[numClasses];
            double sum=0.0;
            for(int j=0; j<distr.length; ++j)
              sum += distr[j]*distr[j];
            sum = Math.sqrt(sum);
            if(sum<0.00000000001)
              sum = 1.0;
            for(int j=0; j<distr.length; ++j)
              distr[j] /= sum;
            //System.out.println("iDoc="+iDoc+", i="+i+", outpuT:");
            //for(int j=0; j<distr.length; ++j)
              //System.out.println("j="+j+", dist="+distr[j]);
            //System.out.println("numClasses="+numClasses);
            for(int j=0; j<=numClasses; ++j) 
              if(trueLabels[j] !=  -1) {
                //System.out.println("j="+j+", probj="+trueLabels[j]+ ", distr="+distr[j]);
                labelsFVDoc[iDoc].multiLabels[i].probs[trueLabels[j]-1] = (float)distr[j]; //as the first element is for null class
              }
          }
        }
      } else {
        double outputV;
        for(int iDoc = 0; iDoc<labelsFVDoc.length; ++iDoc) {
          int num = labelsFVDoc[iDoc].multiLabels.length;
          for(int i=0; i<num; ++i) {
            outputV = wekaCl.classifyInstance(instancesData.instance(numInst++));
            labelsFVDoc[iDoc].multiLabels[i] = new LabelsOfFV(numClasses);
            labelsFVDoc[iDoc].multiLabels[i].probs = new float[numClasses];
            //for(int j=0; j<numClasses; ++j) 
              //labelsFVDoc[iDoc].multiLabels[i].probs[j]= 0.0f;
            if(trueLabels[(int)outputV] != -1)
              labelsFVDoc[iDoc].multiLabels[i].probs[trueLabels[(int)outputV]-1] = 1.0f;
            
            //System.out.println("iDoc="+iDoc+", i="+i+", label="+Integer.parseInt(valueClass));
          }
        } 
      }
    } catch(Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public String getLearnerName() {
    return learnerName;
  }
}
