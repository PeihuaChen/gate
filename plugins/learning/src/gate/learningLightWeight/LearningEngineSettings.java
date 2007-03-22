package gate.learningLightWeight;

import java.io.File;
import java.util.Iterator;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import gate.creole.ResourceInstantiationException;
import gate.util.GateException;

public class LearningEngineSettings {
  File theDSDFile; // the file containing the dataset definition

  String theDSDFileName = null;

  /** Date set definition. */
  public DataSetDefinition datasetDefinition;

  /** The threshold of the probability for the boundary token of chunk. */
  float thrBoundaryProb = 0.42f;

  /** The threshold of the probability for the chunk. */
  float thrEntityProb = 0.2f;

  /** The threshold of the probability for classifation. */
  float thrClassificationProb = 0.2f;

  final static String thrBoundaryProbStr = "thresholdProbabilityBoundary";

  final static String thrEntityProbStr = "thresholdProbabilityEntity";

  final static String thrClassificationProbStr = "thresholdProbabilityClassification";

  /**
   * Two difference methods of converting multi-class problem into
   * binary class problems.
   */
  short multi2BinaryMode = 1;

  public final static short OneVSOtherMode = 1;

  public final static short OneVSAnotehrMode = 2;

  final static String multi2BinaryN = "multiClassification2BinaryMethod";

  public Learner learners;

  String learningExecutable = null; // the executable for learning

  String paramsOfLearning = null;

  String applicationExecutable = null; // the executable for test

  String paramsOfApplication = null;

  boolean surround;

  public boolean isLabelListUpdatable = true;

  public boolean isNLPFeatListUpdatable = true;

  public boolean fiteringTrainingData = false;

  public float filteringRatio = 0.0f;

  public boolean filteringNear = false;

  /**
   * If the user only want to feature data to be used in his learning
   * algorithms.
   */
  public boolean isOnlyFeatureData = false;

  public EvaluationConfiguration evaluationconfig = null;

  public LearningEngineSettings() {
  }

  // public static LearningEngineSettings
  // loadLearningSettingsFromFile(File xmlengines, File directory,
  // boolean batch)
  public static LearningEngineSettings loadLearningSettingsFromFile(
          java.net.URL xmlengines) throws GateException {
    // parses the content of the XML
    // for the informations that are not in the configuration such as
    // the sub
    // engines
    SAXBuilder saxBuilder = new SAXBuilder(false);
    org.jdom.Document jdomDoc = null;
    System.out.println("xmlFile=" + xmlengines.toString());
    try {
      jdomDoc = saxBuilder.build(xmlengines);
    }
    catch(Exception e) {

    }
    Element rootElement = jdomDoc.getRootElement();
    if(!rootElement.getName().equals("ML-CONFIG"))
      throw new ResourceInstantiationException(
              "Root element of dataset defintion file is \""
                      + rootElement.getName() + "\" instead of \"ML-CONFIG\"!");

    // Create a learning setting object
    LearningEngineSettings learningSettings = new LearningEngineSettings();

    learningSettings.surround = false;
    if(rootElement.getChild("SURROUND") != null) {
      String value = rootElement.getChild("SURROUND").getAttribute("value")
              .getValue();
      learningSettings.surround = "true".equalsIgnoreCase(value);
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

    learningSettings.isOnlyFeatureData = false;

    if(rootElement.getChild("ONLY-FEATURE-DATA") != null) {
      String value = rootElement.getChild("ONLY-FEATURE-DATA").getAttribute(
              "value").getValue();
      learningSettings.isOnlyFeatureData = "true".equalsIgnoreCase(value);
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
    }
    catch(RuntimeException e) {
    }

    // Loading the dataset definition
    try {
      Element datasetElement = rootElement.getChild("DATASET");
      learningSettings.datasetDefinition = new DataSetDefinition(datasetElement);
    }
    catch(Exception e) {
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
    learningSettings.learners = new Learner();
    Element UEelement = rootElement.getChild("ENGINE");
    if(UEelement == null)
      throw new GateException(
              "The Engine element in the configureation file is missing or invalid");
    else {
      learningSettings.learners.learnerNickName = UEelement.getAttribute(
              "nickname").getValue();
      learningSettings.learners.learnerName = UEelement.getAttribute(
              "implementationName").getValue();
      if(UEelement.getAttribute("options") != null)
        learningSettings.paramsOfLearning = UEelement.getAttribute("options")
                .getValue();
    }

    return learningSettings;

  }

  public boolean getSurround() {
    return surround;
  }

  public String getParamsOfLearning() {
    return paramsOfLearning;
  }

}
