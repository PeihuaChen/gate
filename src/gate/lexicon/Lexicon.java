/*
 *  Lexicon.java
 *
 *  Copyright (c) 1998-2003, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 28/January/2003
 *
 *  $Id$
 */

package gate.lexicon;

import gate.LanguageResource;


public interface Lexicon extends LanguageResource {
  public static final String POS_ADJECTIVE  = "adjective";
  public static final String POS_ADVERB     = "adverb";
  public static final String POS_NOUN       = "noun";
  public static final String POS_VERB       = "verb";
  public static final String POS_CONJ       = "conjunction";
  public static final String POS_OTHER       = "other";

  public static final Object [] POS_TYPES =
      {POS_ADJECTIVE, POS_ADVERB, POS_NOUN, POS_VERB, POS_CONJ, POS_OTHER};

  public Object getLexiconId();

  public void setLexiconId(Object Id);
}