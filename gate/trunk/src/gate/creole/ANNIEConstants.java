/*
 *  AnnieConstants.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 16/Oct/2001
 *
 *  $Id$
 */

package gate.creole;

/** This interface defines constants used by the ANNIE processing resources. */
public interface ANNIEConstants {

  /**
   * This is an array of strings containing all class names for all ANNIE PRs
   */
  public static final String[] PR_NAMES = {
    "gate.creole.tokeniser.DefaultTokeniser",
    "gate.creole.gazetteer.DefaultGazetteer",
    "gate.creole.splitter.SentenceSplitter",
    "gate.creole.POSTagger",
    "gate.creole.ANNIETransducer",
    "gate.creole.orthomatcher.OrthoMatcher"
  };

  /** The name of the feature on Documents that holds coreference matches. */
  public static final String DOCUMENT_COREF_FEATURE_NAME = "MatchesAnnots";

  /** The name of the feature on Annotations that holds coreference matches. */
  public static final String ANNOTATION_COREF_FEATURE_NAME = "matches";

  public static final String TOKEN_ANNOTATION_TYPE = "Token";
  public static final String TOKEN_STRING_FEATURE_NAME = "string";
  public static final String TOKEN_CATEGORY_FEATURE_NAME = "category";
  public static final String TOKEN_KIND_FEATURE_NAME = "kind";
  public static final String TOKEN_LENGTH_FEATURE_NAME = "length";
  public static final String TOKEN_ORTH_FEATURE_NAME = "orth";

  public static final String SPACE_TOKEN_ANNOTATION_TYPE = "SpaceToken";

  public static final String LOOKUP_ANNOTATION_TYPE = "Lookup";
  public static final String LOOKUP_MAJOR_TYPE_FEATURE_NAME = "majorType";
  public static final String LOOKUP_MINOR_TYPE_FEATURE_NAME = "minorType";

  public static final String SENTENCE_ANNOTATION_TYPE = "Sentence";

  public static final String PERSON_ANNOTATION_TYPE = "Person";
  public static final String PERSON_GENDER_FEATURE_NAME = "gender";

  public static final String ORGANIZATION_ANNOTATION_TYPE = "Organization";
  public static final String LOCATION_ANNOTATION_TYPE = "Location";
  public static final String MONEY_ANNOTATION_TYPE = "Money";
  public static final String DATE_ANNOTATION_TYPE = "Date";


  } // AnnieConstants