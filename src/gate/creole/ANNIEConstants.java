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

  } // AnnieConstants