/*
 *  ConstantParameter.java
 * 
 *  Yaoyong Li 22/03/2007
 *
 *  $Id: ConstantParameter.java, v 1.0 2007-03-22 12:58:16 +0000 yaoyong $
 */
package gate.learningLightWeight;
/**
 * Define the constant parameters used accross different classes
 * in the ML api.
 */
public class ConstantParameters {
  /** Maximal number of the unique NLP features. */
  public static final long MAXIMUMFEATURES = 150000; // 5000000;
  /** Default name of configuration file. */
  public static final String NAMECONFIGURATIONFILE = "engines.xml";
  /** Name of file extension for all the files saved from the ML Api. */
  public static final String FILETYPEOFSAVEDFILE = ".save";
  /** 
   * Name of the sub-directory under the working directory, which is
   * used for storing all files produced by the ML Api, such as
   * those for NLP featuers, feature vectors and learned model.
   */
  public static final String SUBDIRFORRESULTS = "savedFiles";
  /** Name of log file of ML Api. */
  public static final String FILENAMEOFLOGFILE = "logFileForNLPLearning"
    + FILETYPEOFSAVEDFILE;
  /** Name of the file storing the NLP feature list. */
  public static final String FILENAMEOFNLPFeatureList = "NLPFeaturesList"
    + FILETYPEOFSAVEDFILE;
  /** Name of the file storing the NLP data from all documents. */
  public static final String FILENAMEOFNLPFeaturesData = "NLPFeatureData"
    + FILETYPEOFSAVEDFILE;
  /** Name of the file for label list. */
  public static final String FILENAMEOFLabelList = "LabelsList"
    + FILETYPEOFSAVEDFILE;
  /** Name of the file storing the feature vectors in sparse format. */
  public static final String FILENAMEOFFeatureVectorData = "featureVectorsData"
    + FILETYPEOFSAVEDFILE;
  //public static final String PARTFILENAMEOFDocNLPFeaturess = "DOCNLPFeaetures";
  /** Name of the file storing the learned models*/
  public static final String FILENAMEOFModels = "learnedModels"
    + FILETYPEOFSAVEDFILE;
  /** 
   * Name of the file storing NLP feature data with label indexes, instead 
   * labels itsesl.
   */
  public static final String FILENAMEOFNLPDataLabel = "NLPFeatureDataLabels"
    + FILETYPEOFSAVEDFILE;
  /** Name of the file storing chunk length statistics. */
  public static final String FILENAMEOFChunkLenStats = "ChunkLenStats"
    + FILETYPEOFSAVEDFILE;
  /** Name of the file storing the unique label indexes from training data. */
  public static final String FILENAMEOFLabelsInData = "LabelListInData"
    + FILETYPEOFSAVEDFILE;
  /** Training mode. */
  public static final String LEARNINGMODE1 = "TRAINING";
  /** Application mode. */
  public static final String LEARNINGMODE2 = "APPLICATION";
  /** Evaluation mode. */
  public static final String LEARNINGMODE3 = "EVALUATION";
  /** A String used to separate items in a String. */
  public static final String ITEMSEPARATOR = new String(" ");
  /** A String used to replace the separator of items in a String. */
  public static final String ITEMSEPREPLACEMENT = new String("_");
  /** Name of the non-feature. */
  public static final String NAMENONFEATURE = new String("_NA");
  /** The suffix of start token of an entity. */
  public static final String SUFFIXSTARTTOKEN = new String("_BB");
  /** 
   * Separator in a pair of index and value used feature vector
   * in sparse format.
   */
  public static final String INDEXVALUESEPARATOR = new String(":");
}
