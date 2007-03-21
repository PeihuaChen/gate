package gate.learningLightWeight.learners.learnersFromWeka;

import gate.learningLightWeight.LabelsOfFV;
import gate.learningLightWeight.LabelsOfFeatureVectorDoc;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;

public class C45 extends WekaLearner{
  
  public C45() {
    wekaCl = new J48();
    learnerName = "C4.5";
  }

}
