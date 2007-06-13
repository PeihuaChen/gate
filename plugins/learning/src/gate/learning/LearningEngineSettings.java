/*
 *  LearningEngineSettings.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: LearningEngineSettings.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learning;

import java.io.File;
import java.util.Iterator;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

/**
 * Reading and storing the learning settings from the configuration file.
 */
public class LearningEngineSettings {
  /** Storing date set definition. */
  public DataSetDefinition datasetDefinition;
  /** Threshold of the probability for the boundary token of chunk. */
  float thrBoundaryProb = 0.4f;
  /** The threshold of the probability for the chunk. */
  float thrEntityProb = 0.2f;
  /** The threshold of the probability for classifation. */
  float thrClassificationProb = 0.2f;
  /** Name used in the configuration file for boundary token prob threshold. */
  final static String thrBoundaryProbStr = "thresholdProbabilityBoundary";
  /** Name used in the configuration file for entity prob threshold. */
  final static String thrEntityProbStr = "thresholdProbabilityEntity";
  /** Name used in the configuration file for classification prob. threshold. */
  final static String thrClassificationProbStr = "thresholdProbabilityClassification";
  /**
   * Two difference methods of converting multi-class problem into binary class
   * problems.
   */
  short multi2BinaryMode = 1;
  /** One against others. */
  public final static short OneVSOtherMode = 1;
  /** One against Another one. */
  public final static short OneVSAnotehrMode = 2;
  /**
   * Name used in the configuration file for the the method of multi to binary
   * mode conversion.
   */
  final static String multi2BinaryN = "multiClassification2BinaryMethod";
  /** The settings of learner specified. */
  public LearnerSettings learnerSettings;
  /** The surround mode. */
  boolean surround;
  /** The option if the label list is updatable. */
  public boolean isLabelListUpdatable = true;
  /** The option if the NLP Feature List is updatable. */
  public boolean isNLPFeatListUpdatable = true;
  /** The option if doing filtering training data or not. */
  public boolean fiteringTrainingData = false;
  /**
   * Ratio of negative examples filiterd out to the total number of negative
   * exampels in training set.
   */
  public float filteringRatio = 0.0f;
  /**
   * Filtering the negative examples which are nearest to classification
   * hyper-plane or furthest from.
   */
  public boolean filteringNear = false;
  /**
   * If the user only want to feature data to be used in his learning
   * algorithms.
   */
  //public boolean isOnlyFeatureData = false;
  /** The setting for evaluation. */
  public EvaluationConfiguration evaluationconfig = null;
  
  /** The verbosity level for writing information into log file. 
   * 0: no real output.
   * 1: normal output including results and setting information.
   * 2: warning information.
   * */
  public int verbosityLogService = LogService.NORMAL;

  /** Loading the learning settings from the configuration file. */
  public static LearningEngineSettings loadLearningSettingsFromFile(
    java.net.URL xmlengines) throws GateException {
    SAXBuilder saxBuilder = new SAXBuilder(false);
    org.jdom.Document jdomDoc = null;
    if(LogService.minVerbosityLevel > 0)
      System.out.println("xmlFile=" + xmlengines.toString());
    try {
      jdomDoc = saxBuilder.build(xmlengines);
    } catch(Exception e) {
    }
    Element rootElement = jdomDoc.getRootElement();
    if(!rootElement.getName().equals("ML-CONFIG"))
      throw new ResourceInstantiationException(
        "Root element of dataset defintion file is \"" + rootElement.getName()
          + "\" instead of \"ML-CONFIG\"!");
    // Create a learning setting object
    LearningEngineSettings learningSettings = new LearningEngineSettings();
    learningSettings.surround = false;
    if(rootElement.getChild("SURROUND") != null) {
      String value = rootElement.getChild("SURROUND").getAttribute("value")
        .getValue();
      learningSettings.surround = "true".equalsIgnoreCase(value);
    }
    /** Get the setting for verbosity. */
    learningSettings.verbosityLogService = LogService.NORMAL;
    if(rootElement.getChild("VERBOSITY") != null) {
      String value = rootElement.getChild("VERBOSITY").getAttribute("level")
        .getValue();
      learningSettings.verbosityLogService = Integer.parseInt(value);
    }
    learningSettings.fiteringTrainingData = false;
    learningSettings.filteringRatio = 0.0f;
    learningSettings.filteringNear = false;
    if(rootElement.getChild("FILTERING") != null) {
      String value = rootElement.getChild("FILTERING").getAttribute("ratio")
        .getValue();
      learningSettings.filteringRatio = Float.parseFloat(value);
      value = rootElement.getChild("FILTERING").getAttribute("dis").getValue();
      learningSettings.filteringNear = "near".equalsIgnoreCase(value);
      learningSettings.fiteringTrainingData = true;
    }
    learningSettings.isLabelListUpdatable = true;
    if(rootElement.getChild("IS-LABEL-UPDATABLE") != null) {
      String value = rootElement.getChild("IS-LABEL-UPDATABLE").getAttribute(
        "value").getValue();
      learningSettings.isLabelListUpdatable = "true".equalsIgnoreCase(value);
    }
    learningSettings.isNLPFeatListUpdatable = true;
    if(rootElement.getChild("IS-NLPFEATURELIST-UPDATABLE") != null) {
      String value = rootElement.getChild("IS-NLPFEATURELIST-UPDATABLE")
        .getAttribute("value").getValue();
      learningSettings.isNLPFeatListUpdatable = "true".equalsIgnoreCase(value);
    }
    learningSettings.multi2BinaryMode = 1;
    if(rootElement.getChild("multiClassification2Binary") != null) {
      String value = rootElement.getChild("multiClassification2Binary")
        .getAttribute("method").getValue();
      if(value.equalsIgnoreCase("one-vs-another"))
        learningSettings.multi2BinaryMode = 2;
    }
    /* Read the evaluation method: k-fold CV or k-run hold-out */
    try {
      Element evalelem = rootElement.getChild("EVALUATION");
      learningSettings.evaluationconfig = EvaluationConfiguration
        .fromXML(evalelem);
    } catch(RuntimeException e) {
    }
    // Loading the dataset definition
    try {
      Element datasetElement = rootElement.getChild("DATASET");
      learningSettings.datasetDefinition = new DataSetDefinition(datasetElement);
    } catch(Exception e) {
      throw new GateException(
        "The DSD element in the configureation file is missing or invalid");
    }
    // Threshold settings
    Iterator parameters = rootElement.getChildren("PARAMETER").iterator();
    while(parameters.hasNext()) {
      Element paramelem = (Element)parameters.next();
      String name = paramelem.getAttribute("name").getValue();
      String value = paramelem.getAttribute("value").getValue();
      if(name.equals(LearningEngineSettings.thrBoundaryProbStr))
        learningSettings.thrBoundaryProb = Float.parseFloat(value);
      if(name.equals(LearningEngineSettings.thrEntityProbStr))
        learningSettings.thrEntityProb = Float.parseFloat(value);
      if(name.equals(LearningEngineSettings.thrClassificationProbStr))
        learningSettings.thrClassificationProb = Float.parseFloat(value);
    }
    // read the setting for the engine by creating a learner subject
    learningSettings.learnerSettings = new LearnerSettings();
    Element UEelement = rootElement.getChild("ENGINE");
    if(UEelement == null)
      throw new GateException(
        "The Engine element in the configureation file is missing or invalid");
    else {
      if(UEelement.getAttribute("nickname") != null)
        learningSettings.learnerSettings.learnerNickName = UEelement
          .getAttribute("nickname").getValue();
      else learningSettings.learnerSettings.learnerNickName = "A_Learner";
      if(UEelement.getAttribute("implementationName") != null)
        learningSettings.learnerSettings.learnerName = UEelement.getAttribute(
          "implementationName").getValue();
      else throw new GateException("The ENGINE element in the configuration "
        + "does not specify the leaner's name!");
      if(UEelement.getAttribute("options") != null)
        learningSettings.learnerSettings.paramsOfLearning = UEelement
          .getAttribute("options").getValue();
      if(UEelement.getAttribute("executableTraining") != null)
        learningSettings.learnerSettings.executableTraining = UEelement
          .getAttribute("executableTraining").getValue();
    }
    return learningSettings;
  }
}
