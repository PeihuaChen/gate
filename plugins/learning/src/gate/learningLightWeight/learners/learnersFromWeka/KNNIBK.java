package gate.learningLightWeight.learners.learnersFromWeka;

import gate.learningLightWeight.LabelsOfFV;
import gate.learningLightWeight.LabelsOfFeatureVectorDoc;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.Instances;

public class KNNIBK extends WekaLearner{
  
  public KNNIBK() {
    wekaCl = new IBk();
    learnerName = "KNN";
  }
  
  public KNNIBK(int k) {
    wekaCl = new IBk(k);
    learnerName = "KNN";
  }
}
