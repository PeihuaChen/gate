/*
 *  WordNet.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 16/May/2002
 *
 *  $Id$
 */

package gate.wordnet;

import java.util.*;

import gate.*;
import gate.event.*;


/** Represents WordNet LKB.
 */
public interface WordNet extends LanguageResource {

  public static final int POS_ADJECTIVE  = 1001;
  public static final int POS_ADVERB     = 1002;
  public static final int POS_NOUN       = 1003;
  public static final int POS_VERB       = 1004;

  public Iterator getSynsets();

  public Iterator getSynsets(int pos);

  public Iterator getUniqueBeginners();


}

