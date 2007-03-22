package gate.learningLightWeight.learners.learnersFromWeka;

import gate.learningLightWeight.LabelsOfFV;
import gate.learningLightWeight.LabelsOfFeatureVectorDoc;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.Instances;

public class NaiveBayesC extends WekaLearner{
    
    public NaiveBayesC() {
      wekaCl = new NaiveBayes();
      learnerName = "NaiveBayes";
    }

}
