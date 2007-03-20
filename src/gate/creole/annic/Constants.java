/*
 *  Constants.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: Constants.html,v 1.0 2007/03/19 16:22:01 niraj Exp $
 */
package gate.creole.annic;

/**
 * Constants used by annic classes.
 * @author niraj
 *
 */
public class Constants {
  
    /**
     * Name of the document_id_field that is stored in index.
     */
  public final static String DOCUMENT_ID = "DOCUMENT_ID";

  /**
   * name of the index_location_url parameter.
   */
  public final static String INDEX_LOCATION_URL = "INDEX_LOCATION_URL";

  /**
   * Name of the annotation_set_name parameter.
   */
  public final static String ANNOTATION_SET_NAME = "ANNOTATION_SET_NAME";

  /**
   * Name of the features_to_exclude parameter.
   */
  public final static String FEATURES_TO_EXCLUDE = "FEATURES_TO_EXCLUDE";

  /**
   * Name of the base_token_annotation_type parameter.
   */
  public final static String BASE_TOKEN_ANNOTATION_TYPE = "BASE_TOKEN_ANNOTATION_TYPE";

  /**
   * Name of the index_unit_annotation_type parameter.
   */
  public final static String INDEX_UNIT_ANNOTATION_TYPE = "INDEX_UNIT_ANNOTATION_TYPE";

  /**
   * Name of the corpus_index_feature parameter.
   */
  public final static String CORPUS_INDEX_FEATURE = "CorpusIndexFeature";

  /**
   * default value for the corpus_index_feature
   */
  public final static String CORPUS_INDEX_FEATURE_VALUE = "AnnicIR";

  /**
   * Name of the corpus_size parameter.
   */
  public final static String CORPUS_SIZE = "CORPUS_SIZE";

  /**
   * Name of the context_window parameter.
   */
  public final static String CONTEXT_WINDOW = "CONTEXT_WINDOW";

  /**
   * Name of the index_locations parameter.
   */
  public final static String INDEX_LOCATIONS = "INDEX_LOCATIONS";

  /**
   * Name of the no_of_index_units_per_document parameter.
   */
  public final static String NO_OF_INDEX_UNITS_PER_DOCUMENT = "NO_OF_INDEX_UNITS_PER_DOCUMENT";

  /**
   * folder name used for creating a folder which is then used for serializing the files
   */
  public final static String SERIALIZED_FOLDER_NAME = "serialized-files";

  /**
   * Name of the corpus_id parameter.
   */
  public final static String CORPUS_ID = "CORPUS_ID";
}
